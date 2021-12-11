package org.wvt.horizonmgr.webapi

import kotlinx.coroutines.runBlocking
import org.wvt.horizonmgr.webapi.news.MgrNewsModule
import kotlin.test.Test

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