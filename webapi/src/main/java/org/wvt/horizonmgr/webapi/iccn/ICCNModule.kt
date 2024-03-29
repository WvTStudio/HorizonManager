package org.wvt.horizonmgr.webapi.iccn

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import io.ktor.utils.io.errors.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*
import org.wvt.horizonmgr.webapi.*

/**
 * 该类是 ICCN Forum 的 API
 * 目前仅提供登录和注册功能，登录功能可以获取用户的一些基本信息
 */
class ICCNModule {
    class LoginFailedException : ServiceException("登录失败")

    /**
     * 登录。向 `https://adodoz.cn/app_login.php` 发送 POST 表单，参数为 `username` 和 `password`。
     *
     * 服务器返回的 JSON 为：
     *
     * ```json
     * {
     *   "status": <int>,
     *   "user_info": {
     *     "user_id": <int>,
     *     "user_name": <str>,
     *     "user_avatar": <str>
     *   }
     * }
     * ```
     * 失败时返回
     * ```json
     * {
     *   "code": 404,
     *   "msg": <str>
     * }
     * ```
     * @throws [LoginFailedException] 登录失败
     * [NetworkException] 网络错误
     * [JsonParseException] 解析 Json 时出错
     */
    suspend fun login(account: String, password: String): UserClient {
        val client = HttpClient(CIO)
        val jsonStr = try {
            client.submitForm<String>(
                url = "https://adodoz.cn/app_login.php",
                formParameters = Parameters.build {
                    append("username", account)
                    append("password", password)
                }
            )
        } catch (e: IOException) {
            throw NetworkException("发送登录数据失败", e)
        } catch (e: Exception) {
            throw UnexpectedException(e)
        }

        val json = try {
            Json.parseToJsonElement(jsonStr).jsonObject
        } catch (e: SerializationException) {
            throw JsonParseException(jsonStr, e)
        }

        val status = json["status"]?.jsonPrimitive?.intOrNull

        if (status != null) {
            // 成功流程
            if (status != 2) throw LoginFailedException()

            val id: Int
            val username: String
            val avatarUrl: String

            try {
                val userInfo = json["user_info"]?.jsonObject
                    ?: throw MissingJsonField("user_info", jsonStr)
                id = userInfo["user_id"]?.jsonPrimitive?.intOrNull
                    ?: throw MissingJsonField("user_id", jsonStr)
                username = userInfo["user_name"]?.jsonPrimitive?.content
                    ?: throw MissingJsonField("user_name", jsonStr)
                avatarUrl = userInfo["user_avatar"]?.jsonPrimitive?.content
                    ?: throw MissingJsonField("user_avatar", jsonStr)
            } catch (e: SerializationException) {
                throw JsonParseException(jsonStr, e)
            }

            return UserClient(id.toString(), username, avatarUrl, account)
        } else {
            // 失败流程
            val code = json["code"]?.jsonPrimitive?.intOrNull
            if (code == 404) throw LoginFailedException()
            else throw ServiceException("Unknown code: $code")
        }
    }

    data class RegisterErrorEntry(val status: String, val code: String, val detail: String)
    class RegisterFailedException(val errors: List<RegisterErrorEntry>) :
        ServiceException("注册失败", null)


    @Serializable
    data class RegisterJson(
        @SerialName("username")
        val userName: String,
        val email: String,
        val password: String
    )

    /**
     * 注册一个账号。
     *
     * **第一步** 向 `https://adodoz.cn` 发送 GET 请求。
     *
     * 获取 `set-cookie` Header 的值，主要是其 `flarum_session` 参数的值，作为 `session`
     * 获取 `x-csrf-token` Header 的值，作为 `token`
     *
     * **第二步** 向 `https://adodoz.cn/register` 发送 POST 请求，内容如下：
     *
     * ```json
     * {
     *   "username": <str>,
     *   "email": <str>,
     *   "password": <str> // 无需签名
     * }
     * ```
     *
     * 请求使用如下 Header：
     *
     * - `cookie` - 使用第一步中 `set-cookie` 作为 `cookie` Header 的值
     * - `x-csrf-token` - 的值作为 `x-csrf-token`
     *
     * 如果注册成功，服务器会返回 201 状态，并且返回如下 Json 数据：
     *
     * ```json
     * {
     *   "data": {
     *     "id": <str>, // User ID
     *     "attributes": {...}
     *   }
     * }
     * ```
     *
     * 其他状态码视为注册失败，并且返回如下 Json：
     *
     * ```json
     * {
     *   "errors": [ // 造成失败的原因
     *     {
     *       "status": <str>,
     *       "code": <str>, // 验证错误是 "validation_error"
     *       "detail": <str>, // 错误信息
     *       "source": {...}
     *     }, ..
     *   ]
     * }
     * ```
     *
     * @return UID
     * @throws [RegisterFailedException] 服务器返回了注册失败的消息。RegisterFailedException.errors 属性包含了服务器返回的所有导致注册失败的因素
     * [NetworkException] 网络错误
     * [JsonParseException] 解析 Json 时出错
     */
    suspend fun register(username: String, email: String, password: String): String {
        val client = HttpClient(CIO)

        // Step 1 - Get session and token
        var session: String? = null
        var token: String? = null

        val homePageResponse = try {
            client.get<HttpResponse>("https://forum.adodoz.cn") {
                header(HttpHeaders.UserAgent, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36")
            }
        } catch (e: IOException) {
            throw NetworkException("获取 ICCN 主页失败", e)
        } catch (e: Exception) {
            throw UnexpectedException(e)
        }

        val headers = homePageResponse.headers.toMap()

        // Find session and token
        for (header in headers) {
            if (header.key.equals("set-cookie", true)) {
                val cookie = header.value.joinToString()
                if (cookie.contains("flarum_session")) {
                    session = header.value.joinToString()
                } else {
                    throw ServiceException("Found cookie, but no flarum_session")
                }
            } else if (header.key.equals("x-csrf-token", true)) {
                token = header.value.getOrNull(0)
                    ?: throw ServerException("Find x-csrf-token, but no value")
                break
            }
        }

        if (session == null) throw ServerException("flarum_session not found")
        if (token == null) throw ServerException("x-csrf-token not found")

        // Step 2 - Register
        val regResponse = try {
            client.post<HttpResponse>("https://forum.adodoz.cn/register") {
                header("referer", "https://adodoz.cn/")
                header("cookie", session)
                header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.106 Safari/537.36")
                header("x-csrf-token", token)
                contentType(ContentType.Application.Json)
                body = Json.encodeToString(RegisterJson(username, email, password))
            }
        } catch (e: IOException) {
            throw NetworkException("发送注册数据失败", e)
        } catch (e: ClientRequestException) {
            val responseContent = e.response.readText()
            // error
            val errors = mutableListOf<RegisterErrorEntry>()
            // see response_reg_422.json
            val json = try {
                Json.parseToJsonElement(responseContent).jsonObject
            } catch (e: SerializationException) {
                throw JsonParseException(responseContent, e)
            }
            json["errors"]?.jsonArray?.forEach {
                val error = it.jsonObject
                val entry = RegisterErrorEntry(
                    status = error["status"]?.jsonPrimitive?.content
                        ?: throw MissingJsonField("status", responseContent),
                    code = error["code"]?.jsonPrimitive?.content
                        ?: throw MissingJsonField("code", responseContent),
                    detail = error["detail"]?.jsonPrimitive?.content
                        ?: throw MissingJsonField("detail", responseContent)
                )
                errors.add(entry)
            }
            throw RegisterFailedException(errors)
        } catch (e: Exception) {
            throw UnexpectedException(e)
        }

        // Step3 - Parse register result
        val responseContent = regResponse.readText()
        // succeed
        val json = try {
            Json.parseToJsonElement(responseContent).jsonObject
        } catch (e: SerializationException) {
            throw JsonParseException(responseContent, e)
        }
        val data = json["data"]?.jsonObject ?: throw MissingJsonField("data", responseContent)
        return data["id"]?.jsonPrimitive?.content
            ?: throw MissingJsonField("data -> id", responseContent)
    }
}

data class UserClient internal constructor(
    val uid: String,
    val name: String,
    val avatarUrl: String?,
    val account: String
)