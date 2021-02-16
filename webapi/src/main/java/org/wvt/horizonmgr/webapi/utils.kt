package org.wvt.horizonmgr.webapi

import org.json.JSONArray
import org.json.JSONException

@Deprecated("Deprecated", replaceWith = ReplaceWith("try block catch(e: JSONException) {\nthrow JsonParseException(jsonStr, e)\n}", "org.json.JSONException"))
internal inline fun <T> parseJson(block: () -> T): T {
    return try {
        block()
    } catch (e: WebAPIException) {
        throw e
    } catch (e: JSONException) {
        throw ParseException(cause = e)
    } catch (e: Exception) {
        throw UnexpectedException(e)
    }
}

internal fun <T> JSONArray.toArray(): Array<T> {
    val result = arrayOfNulls<Any>(length())
    for (i in 0 until length()) {
        @Suppress("UNCHECKED_CAST")
        result[i] = get(i) as T
    }
    @Suppress("UNCHECKED_CAST")
    return result as Array<T>
}

internal inline fun <T> JSONArray.forEach(action: (item: T) -> Unit) {
    for (i in 0 until length()) {
        @Suppress("UNCHECKED_CAST")
        action(get(i) as T)
    }
}

internal inline fun <T> JSONArray.forEachIndexed(action: (index: Int, item: T) -> Unit) {
    for (i in 0 until length()) {
        @Suppress("UNCHECKED_CAST")
        action(i, get(i) as T)
    }
}