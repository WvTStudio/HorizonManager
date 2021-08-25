package org.wvt.horizonmgr.utils

import android.content.Context

/**
 * 下载一个 OfficialCDN Package 需要以下步骤：
 * 要求每一步都可以回显
 * 解析：
 *  知道分包的 UUID
 *  获取分包文件的 Chunk
 * 下载：
 *  下载每一个 Chunk
 *  合并 Chunk 为一个文件
 * 安装
 */
class PackageDownloader(
    private val context: Context,
    private val coroutineDownloader: CoroutineDownloader2
) {

}