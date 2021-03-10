package org.wvt.horizonmgr.ui.fileselector

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

@Immutable
data class PathTabData(
    val paths: List<String>,
    val depth: Int
)

@Composable
internal fun PathTab(
    data: PathTabData,
    onSelectDepth: (depth: Int) -> Unit
) {
    val scrollState = rememberScrollState()

    // 每一段路径的宽度
    val sizes = remember { mutableStateListOf<Int>() }

    LaunchedEffect(data.depth) {
        if (sizes.isNotEmpty()) {
            var value = 0
            for (i in 0..data.depth) {
                value += sizes.getOrNull(i) ?: 0
            }
            value -= sizes.getOrNull(data.depth) ?: 0 / 2
            scrollState.animateScrollBy(value.toFloat())
        }
    }

    val contentColor = LocalContentColor.current

    LazyRow(
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(start = 72.dp, end = 32.dp)
    ) {
        itemsIndexed(data.paths) { index, item ->
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelectDepth(index) } // TODO: 2021/2/26 希望 Tab 的点击能有涟漪
                    .padding(top = 16.dp, bottom = 16.dp)
                    .onGloballyPositioned { sizes.add(index, it.size.width) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item,
                    color = animateColorAsState(
                        if (data.depth == index) contentColor
                        else contentColor.copy(alpha = 0.5f)
                    ).value // 选中高亮
                )
            }

            // paths.size = 1:
            //     index: 0 (>)
            //     size:  1 (>)
            // paths.size = 4:
            //     index: 0 > 1 > 2 > 3 (>)
            //     size:  4 > 4 > 4 > 4 (>)
            if (index + 1 < data.paths.size) {
                Icon(
                    modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                    imageVector = Icons.Filled.ChevronRight,
                    tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                    contentDescription = null
                )
            }
        }
    }
}
