package org.wvt.horizonmgr.webapi.news

import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class MgrArticleModuleTest {

    @Test
    fun getRecommendedArticle() = runBlocking {
        val module = MgrArticleModule()
        module.getRecommendedArticle().forEach {
            println(it)
            println(module.getArticle(it.id))
        }
    }

    @Test
    fun getArticle() = runBlocking {
        val module = MgrArticleModule()
        println(module.getArticle("20210328_0"))
    }
}