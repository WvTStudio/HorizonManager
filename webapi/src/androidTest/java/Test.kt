import org.junit.Test
import org.wvt.horizonmgr.webapi.iccn.ICCNModule
import kotlin.random.Random

class Test {
    private val module = ICCNModule()

    @Test
    suspend fun registerAndLoginTest() {
        val random = "%06d".format(Random.nextInt(100000).toString())
        val username = "hzmgr$random"
        val password = "hzmgrtestpassword"
        val re = module.register(username, "${username}@adodoz.cn", password)
        print("username: $re")
        val r = module.login(username, password)
        print(r)
    }
}