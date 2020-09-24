package org.wvt.horizonmgr.service.webapi2

import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.service.webapi2.modmodule.ModModule
import org.wvt.horizonmgr.service.webapi2.packagemodule.PackageModule
import org.wvt.horizonmgr.service.webapi2.usermodule.UserModule

/**
 * 为已登录用户提供的 API
 */
interface WebAPI2 {
    suspend fun getDonateList(): List<WebAPI.DonateEntry>
    suspend fun getQQGroupList(): List<WebAPI.QQGroupEntry>

    suspend fun getPackages(): List<WebAPI.ICPackage>

    suspend fun modModule(): ModModule
    suspend fun userModule(): UserModule
    suspend fun packageModule(): PackageModule
}