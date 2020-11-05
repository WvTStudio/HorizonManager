package org.wvt.horizonmgr.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.receiveAsFlow
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.wvt.horizonmgr.utils.CoroutineDownloader
import org.wvt.horizonmgr.utils.ProgressDeferred
import org.wvt.horizonmgr.utils.forEach
import org.wvt.horizonmgr.utils.forEachIndexed
import java.io.File
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import kotlin.coroutines.EmptyCoroutineContext

class WebAPI private constructor(context: Context) {
    companion object {
        private var instance: WeakReference<WebAPI?> = WeakReference(null)

        fun createInstance(context: Context): WebAPI {
            val h = WebAPI(context)
            instance = WeakReference(h)
            return h
        }

        fun getInstance(): WebAPI {
            return instance.get()!!
        }

        fun getOrCreate(context: Context): WebAPI {
            return instance.get() ?: createInstance(context)
        }
    }

    private val downloadDir = context.filesDir.resolve("downloads")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    private val downloadModsDir = downloadDir.resolve("mods")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    private val downloadPacksDir = downloadDir.resolve("packs")
        get() = field.also { if (!it.exists()) it.mkdirs() }

    class UserInfo(val id: Int, val account: String, val name: String, val avatarUrl: String)

    suspend fun login(email: String, password: String): UserInfo {
        val response = post(
            "https://adodoz.cn/app_login.php",
            "username" to email,
            "password" to password
        )
        return parseJson {
            val json = JSONObject(response)
            val status = try {
                json.getInt("status")
            } catch (e: java.lang.Exception) {
                throw LoginFailedException()
            }
            if (status != 2) throw LoginFailedException()
            val uJson = json.getJSONObject("user_info")
            val id = uJson.getInt("user_id")
            val username = uJson.getString("user_name")
            val avatarUrl = uJson.getString("user_avatar")
            return@parseJson UserInfo(id, email, username, avatarUrl)
        }
    }

    data class RegisterErrorEntry(val status: String, val code: String, val detail: String)
    class RegisterException(val errors: List<RegisterErrorEntry>) :
        ServiceException(0, "注册失败", null)

    /**
     * @return Uid of the new user
     */
    suspend fun register(username: String, email: String, password: String): String {
        val (session, token) = run {
            val httpResponse = request(
                url = "https://adodoz.cn",
                body = null,
                method = HttpRequestMethod.GET,
                headers = mapOf("accept" to listOf("*/*"))
            )
            val session = httpResponse.headers["set-cookie"]?.find { it.contains("flarum_session") }
                ?: throw ServerException("session not found")
            val token = httpResponse.headers["x-csrf-token"]?.firstOrNull()
                ?: throw ServerException("token not found")
            return@run session to token
        }

        val response = request(
            url = "https://adodoz.cn/register",
            body = """{"username":"$username","email":"$email","password":"$password"}""",
            method = HttpRequestMethod.POST,
            headers = mapOf(
                "content-type" to listOf("application/json", "charset=UTF-8"),
                "referer" to listOf("https://adodoz.cn/"),
                "cookie" to listOf(session),
                "user-agent" to listOf("Horizon Manager"),
                "x-csrf-token" to listOf(token)
            )
        )
        return parseJson {
            if (response.resultCode == 201) { // success
                // see response_reg_201.json
                val data = JSONObject(response.content!!).getJSONObject("data")
                return@parseJson data.getString("id")
            } else { // error
                val errors = mutableListOf<RegisterErrorEntry>()
                // see response_reg_422.json
                JSONObject(response.errorContent!!).getJSONArray("errors").forEach<JSONObject> {
                    errors.add(
                        RegisterErrorEntry(
                            status = it.getString("status"),
                            code = it.getString("code"),
                            detail = it.getString("detail")
                        )
                    )
                }
                throw RegisterException(errors)
            }
        }
    }

    data class DonateEntry(val name: String, val money: String)

    suspend fun getDonates(): List<DonateEntry> {
        val response = request("https://adodoz.cn/hzmgr/v1/donates.json")
        val result = mutableListOf<DonateEntry>()
        parseJson {
            JSONArray(response.content).forEach<JSONObject> {
                val name = it.getString("name")
                val money = it.getString("money")
                if (money.isBlank()) return@forEach
                result.add(DonateEntry(name, money))
            }
        }
        return result
    }

    data class QQGroupEntry(
        val name: String,
        val description: String,
        val url: String,
        val status: String,
        val avatar: String
    )

    /**
     * @return GroupName to intentUrl
     */
    suspend fun getQQGroupList(): List<QQGroupEntry> {
        val response = request("https://adodoz.cn/hzmgr/v1/qqgroups.json")
        val result = mutableListOf<QQGroupEntry>()
        parseJson {
            JSONArray(response.content).forEach<JSONObject> {
                result.add(
                    QQGroupEntry(
                        name = it.getString("name"),
                        description = it.getString("description"),
                        url = it.getString("url"),
                        status = it.getString("status"),
                        avatar = it.getString("avatar")
                    )
                )
            }
        }
        return result
    }

    data class OnlineModInfo(
        val index: Int,
        val id: Int,
        val name: String,
        val description: String,
        val iconUrl: String,
        val version: String,
        val updateTime: String?,
        val downloadUrl: suspend () -> String
    )

    fun downloadMod(modInfo: OnlineModInfo): ProgressDeferred<Float, File> =
        object : ProgressDeferred<Float, File> {
            private val scope = CoroutineScope(Dispatchers.IO)
            private val channel = Channel<Float>(Channel.UNLIMITED)
            private val job = scope.async<File> {
                val url = modInfo.downloadUrl()
                val file = downloadModsDir.resolve(modInfo.name + ".zip")
                val output = file.outputStream()
                val task = CoroutineDownloader.download(url, output)
                task.progressChannel().receiveAsFlow().conflate().collect {
                    channel.send(it)
                }
                task.await()
                file
            }

            override suspend fun await(): File = job.await()
            override suspend fun progressChannel(): ReceiveChannel<Float> = channel
        }

    suspend fun getModsFromOfficial(): List<OnlineModInfo> {
        val response = get("https://adodoz.cn/mods/allmodinfo.json")
        val result = mutableListOf<OnlineModInfo>()
        parseJson {
            val jsonArray = JSONArray(response)
            jsonArray.forEachIndexed<JSONObject> { index, item ->
                with(item) {
                    val id = getInt("id")
                    if (id < 0) return@forEachIndexed // 脏数据
                    val title = getString("title")
                    val description = getString("description")
                    val icon = getString("icon")
                    val versionName = getString("version_name")
                    val lastUpdate = getString("last_update")
                    result.add(
                        OnlineModInfo(
                            index = index,
                            id = id,
                            name = title,
                            description = description,
                            iconUrl = "https://adodoz.cn/mods/img/$icon",
                            version = versionName,
                            updateTime = lastUpdate,
                            downloadUrl = { "https://adodoz.cn/mods/$id.zip" }
                        )
                    )
                }
            }
        }
        return result
    }

    suspend fun getModsFromCN(): List<OnlineModInfo> {
        val response = get("https://dev.adodoz.cn/api/mod/list")
        val result = mutableListOf<OnlineModInfo>()
        parseJson {
            val jsonRoot = JSONObject(response)
            if (jsonRoot.getString("state") != "success") throw ServiceException(0, "获取失败")
            val dataArray = jsonRoot.getJSONArray("data")
            for (i in 0 until dataArray.length()) {
                val item = dataArray.getJSONObject(i)
                val state = item.getInt("state")
                if (state != 1) continue // 脏数据
                val id = item.getInt("id")
                val name = item.getString("name")
                val version = item.getString("version")
                val icon = item.getString("icon")
//                    val icon = item.getJSONObject("pic").getString("0")
                val absrtact = item.getString("absrtact")
                val time = item.getString("time")

                result.add(
                    OnlineModInfo(
                        index = i,
                        id = id,
                        name = name,
                        description = absrtact,
                        iconUrl = "https://dev.adodoz.cn/$icon",
                        version = version,
                        updateTime = time,
                        downloadUrl = { getDownloadUrlInCN(id) }
                    )
                )
            }
        }
        return result
    }

    private suspend fun getDownloadUrlInCN(modId: Int): String {
        val response = get("https://dev.adodoz.cn/api/mod/download?id=$modId")
        return "https://dev.adodoz.cn/" + parseJson {
            val jsonRoot = JSONObject(response)
            if (jsonRoot.getString("state") != "success") throw ServiceException(0, "获取失败")
            with(jsonRoot.getJSONObject("data")) {
                getString("url").also { Log.d("WebService", "URL: $it") }
            }
        }
    }

    data class ICPackage(
        val uuid: String,
        val manifestStr: String,
        val changelog: String,
        val graphicsUrl: String,
        val recommended: Boolean,
        val chunks: List<Chunk>
    ) {
        data class Chunk(
            val chunkIndex: Int,
            val url: String
        )
    }

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun getPackages(): List<ICPackage> {
        val response =
            get("https://cdn.jsdelivr.net/gh/WvTStudio/horizon-cloud-config@master/packs.json")

        val result = mutableListOf<ICPackage>()
        parseJson {
            val root = JSONObject(response)
            // parse recommended package uuid
            val suggestions = buildList<String> {
                root.getJSONArray("suggestions").forEach(::add)
            }
            // parse packages
            root.getJSONArray("packs").forEach<JSONObject> {
                // start a new coroutine
                launch {
                    val manifestStr = async {
                        get(it.getString("manifest"))
                    }
                    val changeLog: Deferred<String> = async {
                        // TODO 解析多语言
                        val url = it.getJSONObject("changelog").getString("en")
                        parseChangelog(url)
                    }

                    // parse file chunks
                    val chunks: List<ICPackage.Chunk> = it.getJSONObject("package").let {
                        buildList {
                            it.keys().forEach { key ->
                                val url = it.getString(key)
                                // The chunk name is in pattern of "partXXXX", and start at 1
                                // - 1 to make it starts at 0
                                val index = key.removePrefix("part").toInt() - 1
                                add(ICPackage.Chunk(index, url))
                            }
                        }
                    }

                    val uuid = it.getString("uuid")
                    val graphicsUrl = it.getString("graphics")
                    val recommended = suggestions.contains(uuid)
                    result.add(
                        ICPackage(
                            manifestStr = manifestStr.await(),
                            changelog = changeLog.await(),
                            graphicsUrl = graphicsUrl,
                            uuid = uuid,
                            recommended = recommended,
                            chunks = chunks
                        )
                    )
                }
            }
            // 子协程的异常会在此处抛出
            joinAll()
        }
        return result
    }

    fun downloadPackage(pack: ICPackage): ProgressDeferred<Float, Pair<File, File>> =
        object : ProgressDeferred<Float, Pair<File, File>> {
            private val scope = CoroutineScope(EmptyCoroutineContext + Dispatchers.IO)
            private val channel = Channel<Float>(Channel.UNLIMITED)
            private val job = scope.async {
                val zipFile = downloadPacksDir.resolve("${pack.uuid}.zip")
                val graphicsFile = downloadDir.resolve("${pack.uuid}_graphics.zip")

                channel.send(0f)

                // 这个比较小就不算进度了吧
                val graphicsJob = async {
                    graphicsFile.outputStream().use {
                        CoroutineDownloader.download(pack.graphicsUrl, it).await()
                    }
                }

                val packJob = async {
                    // TODO: Use multi-thread download
                    zipFile.outputStream().use { stream ->
                        // 遍历区块
                        pack.chunks.sortedBy { it.chunkIndex }.forEach { chunk ->
                            val task = CoroutineDownloader.download(chunk.url, stream)
                            task.progressChannel().receiveAsFlow().conflate().collect {
                                channel.send((chunk.chunkIndex + it) / pack.chunks.size)
                            }
                            task.await() // 等待该区块下载完成后再下载其他区块
                        }
                    }
                }

                graphicsJob.await()
                packJob.await()
                channel.close()
                return@async zipFile to graphicsFile
            }

            override suspend fun await(): Pair<File, File> = job.await()

            override suspend fun progressChannel(): ReceiveChannel<Float> = channel
        }

    fun multiDownloadPackage() {
        // TODO: 2020/11/3
        object : MultiThreadDownloadTask {

            override suspend fun await(): Long {
                TODO("Not yet implemented")
            }

            override suspend fun getState(): ReceiveChannel<MultiThreadDownloadTask.State> {
                TODO("Not yet implemented")
            }

            override suspend fun getChunks(): MultiThreadDownloadTask.Chunks {
                TODO("Not yet implemented")
            }

            override suspend fun cancel() {
                TODO("Not yet implemented")
            }
        }
    }

    data class News(
        val id: Int,
        val title: String,
        val brief: String,
        val coverUrl: String
    )

    data class NewsContent(
        val id: Int,
        val title: String,
        val brief: String,
        val coverUrl: String,
        val content: String
    )

    /**
     * see news.json
     */
    suspend fun getNews(): List<News> {
        val response = request("https://adodoz.cn/hzmgr/v1/news.json")
        val result = mutableListOf<News>()

        parseJson {
            val json = JSONArray(response.content)
            json.forEachIndexed<JSONObject> { index, item ->
                val id = item.getInt("id")
                val title = item.getString("title")
                val cover = item.getString("cover")
                val brief = item.getString("brief")
                result.add(News(id, title, brief, cover))
            }
        }

        return result
    }

    /**
     * see news-1.json
     */
    suspend fun getNewsContent(id: Int): NewsContent? {
        val response = request("https://adodoz.cn/hzmgr/v1/news/${id}.json")
        if (response.resultCode != 200) return null

        return parseJson {
            val json = JSONObject(response.content)
            return@parseJson NewsContent(
                id = json.getInt("id"),
                content = json.getString("content"),
                title = json.getString("title"),
                brief = json.getString("brief"),
                coverUrl = json.getString("coverUrl")
            )
        }
    }


    data class LatestVersion(
        val channel: String,
        val latestVersionCode: Int
    )

    data class Changelog(
        val versionCode: Int,
        val versionName: String,
        val changelog: String
    )

    /**
     * see versioninfo.json
     */
    suspend fun getLatestVersions(): List<LatestVersion> {
        val response = request("https://adodoz.cn/hzmgr/v1/versioninfo.json")
        val result = mutableListOf<LatestVersion>()

        parseJson {
            JSONArray(response.content).forEach<JSONObject> {
                result.add(
                    LatestVersion(
                        channel = it.getString("channel"),
                        latestVersionCode = it.getInt("latestVersionCode")
                    )
                )
            }
        }

        return result
    }

    /**
     * see changelogs.json
     */
    suspend fun getChangelogs(): List<Changelog> {
        val response = request("https://adodoz.cn/hzmgr/v1/changelogs.json")
        val result = mutableListOf<Changelog>()

        parseJson {
            JSONArray(response.content).forEach<JSONObject> {
                result.add(
                    Changelog(
                        versionCode = it.getInt("versionCode"),
                        versionName = it.getString("versionName"),
                        changelog = it.getString("changelog")
                    )
                )
            }
        }

        return result
    }

    private suspend fun parseChangelog(changelogUrl: String): String {
        return get(changelogUrl)
    }


    open class WebAPIException(override val message: String, cause: Throwable? = null) :
        Exception(message, cause)

    /**
     * 代表未知原因、无法解决、无法预料的错误
     */
    class UnexpectedException(cause: Throwable? = null) : WebAPIException("出现未知错误", cause = cause)

    /**
     * 代表网络出错，例如无网络、连接超时、连接中断
     */
    open class NetworkException(message: String? = null, cause: Throwable? = null) :
        WebAPIException(message ?: "网络错误，请稍后重试", cause)

    class RequestTimeoutException : NetworkException("请求超时")
    class NoInternetException : NetworkException("无互联网连接")
    class BadRequestException : NetworkException("请求错误")

    /**
     * 代表服务器出错，例如服务器拒绝返回数据、返回了其他状态码
     */
    open class ServerException(message: String? = null, cause: Throwable? = null) :
        WebAPIException(message ?: "服务器出现错误，请稍后重试", cause)

    class OtherResultCodeException(val resultCode: Int, val content: String?) :
        ServerException("服务器返回了异常的状态码：$resultCode")

    /**
     * 代表客户端在解析服务器数据时发生的错误
     */
    open class ParseException(message: String? = null, cause: Throwable? = null) :
        WebAPIException(message ?: "无法解析数据", cause)

    /**
     * 代表业务逻辑上，成功数据以外的错误，例如因账号密码错误导致的登录失败、用户不存在
     */
    open class ServiceException(val errorCode: Int = 0, message: String, cause: Throwable? = null) :
        WebAPIException(message, cause)

    class LoginFailedException : ServiceException(0, "登陆失败，请检查账户和密码后重试")

    private enum class HttpRequestMethod {
        GET, POST
    }

    interface HttpResponse {
        val resultCode: Int
        val headers: Map<String, List<String>>
        val content: String?
        val errorContent: String?
    }

    data class HttpResponseData(
        override val resultCode: Int,
        override val content: String?,
        override val errorContent: String?,
        override val headers: Map<String, List<String>>
    ) : HttpResponse

    private suspend fun request(
        url: String,
        body: String? = null,
        method: HttpRequestMethod = HttpRequestMethod.GET,
        headers: Map<String, List<String>> = emptyMap()
    ): HttpResponse = coroutineScope {
        withContext(Dispatchers.IO) {
            val conn = try {
                (URL(url).openConnection() as HttpURLConnection)
            } catch (e: Exception) {
                throw BadRequestException()
            }

            with(conn) {
                try {
                    connectTimeout = 8000
                    readTimeout = 10000
                    requestMethod = method.name
                    useCaches = false
                    headers.forEach { (key, values) ->
                        setRequestProperty(key, values.joinToString(";"))
                    }
                } catch (e: Exception) {
                    throw BadRequestException()
                }
            }

            try {
                conn.connect()
            } catch (e: SocketTimeoutException) {
                throw RequestTimeoutException()
            } catch (e: Exception) {
                throw NetworkException(cause = e)
            }

            try {
                if (body != null) conn.outputStream.writer().use { it.write(body) }
            } catch (e: Exception) {
                throw NetworkException(cause = e)
            }

            val resultCode = conn.responseCode
            val responseHeaders = conn.headerFields
            var responseContent: String? = null
            var errorContent: String? = null

            try {
                responseContent = conn.inputStream?.bufferedReader()?.use { it.readText() }
            } catch (e: Exception) {
//                throw NetworkException(cause = e)
            }
            try {
                errorContent = conn.errorStream?.bufferedReader()?.use { it.readText() }
            } catch (e: Exception) {
            }

            conn.disconnect()
            return@withContext HttpResponseData(
                resultCode, responseContent, errorContent, responseHeaders
            )
        }
    }

    /**
     * @throws
     * [BadRequestException] URL错误，请求构建出错
     * [RequestTimeoutException] 连接超时
     * [NetworkException] 连接出错，发送数据出错，接收数据出错
     * [ServerException] 服务器返回异常状态码
     */
    @Deprecated("Use request(): HttpResponse instead")
    private suspend fun request(
        url: String,
        content: String?,
        method: HttpRequestMethod = HttpRequestMethod.POST,
        headers: List<Pair<String, String>> = emptyList()
    ): String {
        return coroutineScope<String> {
            withContext(Dispatchers.IO) {
                val conn = try {
                    (URL(url).openConnection() as HttpURLConnection)
                } catch (e: Exception) {
                    throw BadRequestException()
                }
                with(conn) {
                    try {
                        connectTimeout = 8000
                        readTimeout = 8000
                        requestMethod = method.name
                        useCaches = false
                        headers.forEach { (key, value) ->
                            setRequestProperty(key, value)
                        }
                    } catch (e: Exception) {
                        throw BadRequestException()
                    }
                }
                try {
                    conn.connect()
                } catch (e: SocketTimeoutException) {
                    throw RequestTimeoutException()
                } catch (e: Exception) {
                    throw NetworkException(cause = e)
                }

                try {
                    if (content != null) conn.outputStream.writer().use { it.write(content) }
                } catch (e: Exception) {
                    throw NetworkException(cause = e)
                }

                if (conn.responseCode != HttpURLConnection.HTTP_OK) {
                    val errorContent = try {
                        conn.errorStream?.bufferedReader()?.use { it.readText() }
                    } catch (e: Exception) {
                        throw NetworkException(cause = e)
                    }
                    throw OtherResultCodeException(conn.responseCode, errorContent)
                }

                conn.headerFields

                val result = try {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    throw NetworkException(cause = e)
                }

                conn.disconnect()
                return@withContext result
            }
        }
    }

    private suspend fun get(
        url: String,
        params: List<Pair<String, String>>,
        headers: List<Pair<String, String>> = emptyList()
    ): String {
        val paramsEncoded = params.joinToString(separator = "&") { (k, v) ->
            URLEncoder.encode(k, "utf-8") + "=" + URLEncoder.encode(v, "utf-8")
        }
        val fullUrl = "$url?$paramsEncoded"
        return request(fullUrl, null, HttpRequestMethod.GET, headers)
    }

    private suspend fun get(
        url: String,
        headers: List<Pair<String, String>> = emptyList()
    ): String {
        return request(url, null, HttpRequestMethod.GET, headers)
    }

    private suspend fun post(
        url: String,
        content: String,
        headers: List<Pair<String, String>> = emptyList()
    ): String {
        return request(url, content, HttpRequestMethod.POST, headers)
    }

    private suspend fun post(
        url: String,
        vararg params: Pair<String, String>,
        headers: List<Pair<String, String>> = emptyList()
    ): String {
        val paramsEncoded = params.joinToString(separator = "&") { (k, v) ->
            URLEncoder.encode(k, "utf-8") + "=" + URLEncoder.encode(v, "utf-8")
        }
        return request(url, paramsEncoded, HttpRequestMethod.POST, headers)
    }

    /**
     * Switch to Default Dispatcher, catch errors and transforms them.
     */
    private suspend inline fun <T> parseJson(crossinline block: suspend CoroutineScope.() -> T): T {
        return try {
            withContext(Dispatchers.Default) {
                block(this)
            }
        } catch (e: WebAPIException) {
            throw e
        } catch (e: JSONException) {
            throw ParseException(cause = e)
        } catch (e: Exception) {
            throw UnexpectedException(e)
        }
    }
}