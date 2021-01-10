package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.wvt.horizonmgr.webapi.news.MgrNewsModule

class MgrNewsModuleTest {
    private val module = MgrNewsModule()

    @Test
    fun getSuggestionsAndDetails() = runBlocking {
        module.getNewsSuggestions().forEach {
            println(it)
            module.getNews(it.newsId)?.let {
                println(it)
            }
        }
    }
}