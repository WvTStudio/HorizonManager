package org.wvt.horizonmgr.service

import kotlinx.coroutines.channels.ReceiveChannel
import java.net.URL

/**
 * 为未登录游客提供的 API
 */
interface GuestAPI {

}

/**
 * 为已登录用户提供的 API
 */
interface WebAPI2 {
    suspend fun login(): User
    suspend fun register(): User
    suspend fun getDonateList()

    suspend fun getQQGroupList()
    suspend fun getMods()
}

/**
 * 代表 ICCN 平台
 */
interface ModModule {
    suspend fun getRecommend(): ReceiveChannel<Mod>
    suspend fun getById(id: String): Mod?
    suspend fun search(text: String, sortMode: Int): ReceiveChannel<Mod>
}

interface SortMode {

}

interface Mod {
    suspend fun getName(): String

    /**
     * 返回的不一定是纯文本
     */
    suspend fun getDescription(): String
    suspend fun getDeveloper(): User
    suspend fun getDownloadLink(): URL
}

interface User {
    suspend fun getName()
}

/*sealed class Content {
    class PlainText : Content()
    class HtmlText : Content()
    class MarkdownText : Content()
}*/

interface SplitResult<T> {
    suspend fun getTotalPageCount(): Int
}