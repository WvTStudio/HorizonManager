package org.wvt.horizonmgr.utils

import org.json.JSONArray

fun <T> JSONArray.toArray(): Array<T> {
    val result = arrayOfNulls<Any>(length())
    for (i in 0 until length()) {
        @Suppress("UNCHECKED_CAST")
        result[i] = get(i) as T
    }
    @Suppress("UNCHECKED_CAST")
    return result as Array<T>
}

inline fun <T> JSONArray.forEach(action: (item: T) -> Unit) {
    for (i in 0 until length()) {
        @Suppress("UNCHECKED_CAST")
        action(get(i) as T)
    }
}

inline fun <T> JSONArray.forEachIndexed(action: (index: Int, item: T) -> Unit) {
    for (i in 0 until length()) {
        @Suppress("UNCHECKED_CAST")
        action(i, get(i) as T)
    }
}