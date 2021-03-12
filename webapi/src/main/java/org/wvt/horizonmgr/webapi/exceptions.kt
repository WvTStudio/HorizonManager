package org.wvt.horizonmgr.webapi

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

open class JsonParseException(
    val jsonSource: String,
    cause: Throwable? = null
) : WebAPIException("解析 Json 时发生错误：${jsonSource.take(20)}...", cause = cause)

class MissingJsonField(
    val missingField: String,
    jsonSource: String,
    cause: Throwable? = null,
    override val message: String = "解析 Json 时出错，缺少字段 $missingField"
) : JsonParseException(jsonSource, cause)

/**
 * 代表业务逻辑上，成功数据以外的错误，例如因账号密码错误导致的登录失败、用户不存在
 */
open class ServiceException(
    message: String,
    cause: Throwable? = null
) : WebAPIException(message, cause)