package org.wvt.horizonmgr.utils

import org.json.JSONArray

inline fun <T> JSONArray.toArray(): Array<T> {
    val result = arrayOfNulls<Any>(length())
    for (i in 0 until length()) {
        result[i] = get(i) as T
    }
    return result as Array<T>
}

inline fun <T> JSONArray.forEach(action: (item: T) -> Unit) {
    for (i in 0 until length()) {
        @Suppress("UNCHECKED_CAST")
        action(get(i) as T)
    }
}