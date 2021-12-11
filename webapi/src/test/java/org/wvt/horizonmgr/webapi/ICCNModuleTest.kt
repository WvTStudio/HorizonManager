package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.wvt.horizonmgr.webapi.iccn.ICCNModule
import kotlin.random.Random
import kotlin.test.Test

class ICCNModuleTest {
    private val module = ICCNModule()

    @Test
    fun registerAndLoginTest() = runBlocking {
        println("Testing")
        val random = "%06d".format(Random.nextInt(100000))
        val username = "hzmgr$random"
        val password = "hzmgrtestpassword"
        val re = module.register(username, "${username}@adodoz.cn", password)
        println("username: $re")
        val user = module.login(username, password)
        println(user)
    }

    @Test
    fun login() = runBlocking {
        val user = module.login("hzmgr010102@adodoz.cn", "hzmgrtestpassword")
        assert(user.account == "hzmgr010102@adodoz.cn")
    }

    @Test
    fun `Test error login`() = runBlocking {
        try {
            module.login("hzmgr01010@adodoz.cn", "testsetset")
        } catch (e: Exception) {
            assert(e is ICCNModule.LoginFailedException)
            return@runBlocking
        }
        error("Failed")
    }

    @Test
    fun register() = runBlocking {
        val random = Random.nextInt()
        val user = try {
            module.register("hzmgr$random", "hzmgr$random@adodoz.cn", "hzmgrtestpassword")
        } catch (e: ICCNModule.RegisterFailedException) {
            e.errors.forEach {
                println(it)
            }
            return@runBlocking
        }
        println(user)
    }
}