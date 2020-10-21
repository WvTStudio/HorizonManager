package org.wvt.horizonmgr

import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.service.HorizonManager2
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.service.WebAPI
import org.wvt.horizonmgr.service.webapi2.WebAPI2

interface DependenciesContainer {
    val horizonManager: HorizonManager
    val horizonManager2: HorizonManager2
    val localCache: LocalCache
    val webapi: WebAPI
    val webapi2: WebAPI2
}