package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.webapi.iccn.ICCNModule
import kotlin.random.Random

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
        println(user)
    }

    @Test
    fun register() = runBlocking {
        val user = try {
            module.register("hzmgr010106", "hzmgr010103@adodoz.cn", "hzmgrtestpassword")
        } catch (e: ICCNModule.RegisterFailedException) {
            e.errors.forEach {
                println(it)
            }
            return@runBlocking
        }
        println(user)
    }
}