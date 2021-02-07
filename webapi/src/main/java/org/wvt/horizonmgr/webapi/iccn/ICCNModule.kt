package org.wvt.horizonmgr.webapi.iccn

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.content.*
import io.ktor.http.*
import io.ktor.utils.io.errors.*
import org.json.JSONException
import org.json.JSONObject
import org.wvt.horizonmgr.webapi.*

/**
 * 该类是 ICCN Forum 的 API
 * 目前仅提供登录和注册功能，登录功能可以获取用户的一些基本信息
 */
class ICCNModule {
    private val client = HttpClient(CIO) {
        install(HttpCallValidator) {
            validateResponse {} // 禁用 response 有效性检查
        }
    }

    class LoginFailedException : ServiceException("登录失败")

    /**
     * 登录。向 `https://adodoz.cn/app_login.php` 发送 POST 表单，参数为 `username` 和 `password`。
     *
     * 服务器返回的 JSON 为：
     *
     * ```json
     * {
     *   ...
     *   "user_info": {
     *     "user_id": <int>,
     *     "user_name": <str>,
     *     "user_avatar": <str>
     *   }
     * }
     * ```
     *
     * @throws [LoginFailedException] 登录失败
     * [NetworkException] 网络错误
     * [JsonParseException] 解析 Json 时出错
     */
    suspend fun login(account: String, password: String): UserClient {
        val jsonStr = try {
            client.submitForm<String>(
                url = "https://adodoz.cn/app_login.php",
                formParameters = Parameters.build {
                    set("username", account)
                    set("password", password)
                }
            )
        } catch (e: IOException) {
            throw NetworkException("发送登录数据失败", e)
        }
        val json = try {
            JSONObject(jsonStr)
        } catch (e: JSONException) {
            throw JsonParseException(jsonStr, e)
        }
        val status = try {
            json.getInt("status")
        } catch (e: JSONException) {
            throw LoginFailedException()
        }
        if (status != 2) throw LoginFailedException()

        val id: Int
        val username: String
        val avatarUrl: String

        try {
            with(json.getJSONObject("user_info")) {
                id = getInt("user_id")
                username = getString("user_name")
                avatarUrl = getString("user_avatar")
            }
        } catch (e: JSONException) {
            throw JsonParseException(jsonStr, e)
        }

        return UserClient(id.toString(), username, avatarUrl, account)
    }

    data class RegisterErrorEntry(val status: String, val code: String, val detail: String)
    class RegisterFailedException(val errors: List<RegisterErrorEntry>) :
        ServiceException("注册失败", null)

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
        // Step 1 - Get session and token
        val session: String
        val token: String

        val homePageResponse = try {
            client.get<HttpResponse>("https://adodoz.cn")
        } catch (e: IOException) {
            throw NetworkException("获取 ICCN 主页失败", e)
        }
        session = homePageResponse.headers["set-cookie"]
            ?.takeIf { it.contains("flarum_session") }
            ?: throw ServerException("session not found")
        token = homePageResponse.headers["x-csrf-token"]
            ?: throw ServerException("token not found")

        // Step 2 - Register
        val regResponse = try {
            client.post<HttpResponse>("https://adodoz.cn/register") {
                body = TextContent(
                    contentType = ContentType.Application.Json,
                    text = JSONObject().apply {
                        put("username", username)
                        put("email", email)
                        put("password", password)
                    }.toString()
                )
                headers {
                    set("referer", "https://adodoz.cn/")
                    set("cookie", session)
                    set("user-agent", "Horizon Manager")
                    set("x-csrf-token", token)
                }
            }
        } catch (e: IOException) {
            throw NetworkException("发送注册数据失败", e)
        }

        // Step3 - Parse register result
        val responseContent = regResponse.readText()
        if (regResponse.status.value == 201) {
            // succeed
            return try {
                JSONObject(responseContent)
                    .getJSONObject("data")
                    .getString("id")
            } catch (e: JSONException) {
                // Generally, the cause is that "id" does not exist.
                throw JsonParseException(responseContent, e)
            }
        } else {
            // error
            val errors = mutableListOf<RegisterErrorEntry>()
            // see response_reg_422.json
            try {
                JSONObject(responseContent).getJSONArray("errors")
                    .forEach<JSONObject> {
                        val entry = RegisterErrorEntry(
                            status = it.getString("status"),
                            code = it.getString("code"),
                            detail = it.getString("detail")
                        )
                        errors.add(entry)
                    }
            } catch (e: JSONException) {
                throw JsonParseException(responseContent, e)
            }
            throw RegisterFailedException(errors)
        }
    }
}

data class UserClient internal constructor(
    val uid: String,
    val name: String,
    val avatarUrl: String?,
    val account: String
)