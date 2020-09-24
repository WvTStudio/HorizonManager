package org.wvt.horizonmgr.service.webapi2.usermodule

import java.net.URL

interface User {
    suspend fun getAccount(): String
    suspend fun getName(): String
    suspend fun getAvatar(): URL
    suspend fun getDescription(): String
}