package org.wvt.horizonmgr.ui.locale

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import org.wvt.horizonmgr.ui.components.LocalImage
import org.wvt.horizonmgr.ui.theme.HorizonManagerTheme

@Composable
internal fun LevelItem(
    modifier: Modifier = Modifier,
    title: String,
    screenshot: String?,
    onRenameClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    Card(modifier = modifier) {
        Column {
            Row(Modifier.fillMaxWidth().wrapContentHeight()) {
                Column(
                    modifier = Modifier.padding(
                        top = 16.dp,
                        start = 16.dp,
                        bottom = 16.dp
                    ).weight(1f)
                ) {
                    Text(text = title, style = MaterialTheme.typography.h6)
                }
                LocalImage(
                    path = screenshot,
                    modifier = Modifier.padding(16.dp)
                        .height(72.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .aspectRatio(16f / 9f)
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    modifier = Modifier.padding(start = 16.dp, end = 8.dp, bottom = 8.dp),
                    onClick = onRenameClicked
                ) { Text("重命名") }
                TextButton(
                    modifier = Modifier.padding(start = 8.dp, end = 16.dp, bottom = 8.dp),
                    onClick = onDeleteClicked
                ) { Text("删除") }
            }
        }
    }
}

@Preview
@Composable
private fun LevelItemPreview() {
    HorizonManagerTheme {
        Surface(modifier = Modifier.wrapContentHeight()) {
            LevelItem(
                title = "My world", screenshot = null, onRenameClicked = {}, onDeleteClicked = {}
            )
        }
    }
}