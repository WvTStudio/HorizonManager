package org.wvt.horizonmgr.webapi

import org.wvt.horizonmgr.webapi.mgrmodule.MgrModule
import org.wvt.horizonmgr.webapi.mgrmodule.MgrModuleAdodozV1
import org.wvt.horizonmgr.webapi.modmodule.ModModule
import org.wvt.horizonmgr.webapi.packagemodule.PackageModule
import org.wvt.horizonmgr.webapi.usermodule.UserModule

/**
 * 为已登录用户提供的 API
 */
interface WebAPI {
    val mgrModule: MgrModule
    val modModule: ModModule
    val userModule: UserModule
    val packageModule: PackageModule

    companion object {
        fun getInstance(): WebAPI {
            return WebAPIImpl()
        }
    }
}

internal class WebAPIImpl : WebAPI {
    override val mgrModule: MgrModule = MgrModuleAdodozV1()
    override val modModule: ModModule
        get() = TODO("Not yet implemented")
    override val userModule: UserModule
        get() = TODO("Not yet implemented")
    override val packageModule: PackageModule
        get() = TODO("Not yet implemented")
}