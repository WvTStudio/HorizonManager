package org.wvt.horizonmgr.ui.fileselector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

@Composable
internal fun CustomAppBar(
    onCancel: () -> Unit,
    data: PathTabData,
    onSelectDepth: (depth: Int) -> Unit
) {
    Surface(
        modifier = Modifier.zIndex(4.dp.value),
        elevation = 4.dp,
        color = AppBarBackgroundColor
    ) {
        Column {
            TopAppBar(
                title = { Text("选择文件") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            "返回"
                        )
                    }
                },
                backgroundColor = AppBarBackgroundColor,
                elevation = 0.dp
            )
            // 路径指示器
            Surface(modifier = Modifier.fillMaxWidth(), color = AppBarBackgroundColor) {
                PathTab(data, onSelectDepth)
            }
        }
    }
}

