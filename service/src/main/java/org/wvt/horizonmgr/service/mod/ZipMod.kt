package org.wvt.horizonmgr.service.mod

import java.util.zip.ZipFile

/**
 * 该类代表一个 Zip 形式的，还未安装的 MOD
 * 该压缩包的文件结构请见 [org.wvt.horizonmgr.service.horizonmgr2.mod.InstalledMod]
 *
 */
class ZipMod internal constructor(
    private val zipFile: ZipFile
) {
    // TODO: 2020/12/5  
}