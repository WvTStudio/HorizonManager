package org.wvt.horizonmgr

import org.wvt.horizonmgr.legacyservice.HorizonManager
import org.wvt.horizonmgr.legacyservice.LocalCache
import org.wvt.horizonmgr.legacyservice.WebAPI

interface DependenciesContainer {
    val horizonManager: HorizonManager
    val localCache: LocalCache
    val webapi: WebAPI
}