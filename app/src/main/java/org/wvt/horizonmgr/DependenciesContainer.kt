package org.wvt.horizonmgr

import org.wvt.horizonmgr.service.HorizonManager
import org.wvt.horizonmgr.service.LocalCache
import org.wvt.horizonmgr.service.WebAPI

interface DependenciesContainer {
    val horizonManager: HorizonManager
    val localCache: LocalCache
    val webapi: WebAPI
}