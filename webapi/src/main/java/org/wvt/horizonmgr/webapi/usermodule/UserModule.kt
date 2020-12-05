package org.wvt.horizonmgr.webapi.usermodule

import kotlinx.coroutines.channels.ReceiveChannel

interface UserModule {
    suspend fun login(account: String, password: String): User
    suspend fun register(): User

    suspend fun search(): ReceiveChannel<User>
    suspend fun getByAccount(account: String): User?
}