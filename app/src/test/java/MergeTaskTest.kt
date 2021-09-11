import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.utils.MergeTask
import java.io.File

class MergeTaskTest {
    @Test
    fun test() {
        runBlocking {
            MergeTask(listOf(File("a"), File("b"), File("c")), File("o")).execute { current, total ->
                println(current.toDouble() / total)
            }
        }
    }
}