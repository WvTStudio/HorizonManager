package org.wvt.horizonmgr.ui.fileselector

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed

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
            val target = sizes.take(data.depth).sum()
            scrollState.animateScrollTo(target)
        }
    }

    val contentColor = LocalContentColor.current

    Row(
        modifier = Modifier.horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(72.dp))
        data.paths.fastForEachIndexed { index, item ->
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
                    tint = contentColor.copy(alpha = 0.5f),
                    contentDescription = null
                )
            }
        }
        Spacer(modifier = Modifier.width(32.dp))
    }
}
