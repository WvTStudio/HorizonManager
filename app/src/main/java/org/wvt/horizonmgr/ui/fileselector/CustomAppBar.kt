package org.wvt.horizonmgr.ui.fileselector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.R
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor
import org.wvt.horizonmgr.ui.theme.PreviewTheme

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
                title = { Text(stringResource(R.string.fileselector_screen_title)) },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            stringResource(R.string.navigation_action_back)
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

@Preview
@Composable
private fun Preview(){
    PreviewTheme {
        Column {
            CustomAppBar(onCancel = { /*TODO*/ }, data = PathTabData(
                listOf("内部存储", "Android", "com"), 1
            ), onSelectDepth = { /*TODO*/ })
        }
    }
}
