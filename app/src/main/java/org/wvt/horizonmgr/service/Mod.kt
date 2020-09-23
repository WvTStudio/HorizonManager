package org.wvt.horizonmgr.service

import kotlinx.coroutines.channels.ReceiveChannel
import java.net.URL

/* Mod 存在好几种情况
   1. 在管理器内从汉化组源下载的模组
   2. 在管理器内从官方源下载的模组
   3. 在浏览器从官方源或汉化组源下载的模组
   4. 未知渠道下载的模组

   5. 从以后要做的 ICCN 平台中下载的模组

   1, 2, 3, 4

   由于官方源和汉化组源下载的模组都不包含相关信息源，因此与情况4无差
   情况 3 和 4 都属于未解析的模组
本地安装的 Mod 可以尝试解析成 ICCNMod
 */

/**
 * 代表一个从 ICCN 上下载下来的 MOD
 */
class ICCNMod private constructor() {
    suspend fun getDetails() {

    }

    suspend fun getDownloadUrl() {
    }
}

class UnInstalledICCNMod

/**
 * 代表本地安装的 MOD
 */
class CustomMod