package org.wvt.horizonmgr.ui.modulemanager

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.wvt.horizonmgr.ui.components.EmptyPage

@Composable
fun ICResTab() {
    Box(Modifier.fillMaxSize()) {
        EmptyPage(Modifier.align(Alignment.Center)) {
            Text("暂不支持")
        }
    }
}