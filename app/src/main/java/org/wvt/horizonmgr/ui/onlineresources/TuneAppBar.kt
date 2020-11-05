package org.wvt.horizonmgr.ui.onlineresources

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.foundation.BaseTextField
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Layout
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.id
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import org.wvt.horizonmgr.ui.components.DropDownSelector

@Deprecated("Deprecated", level = DeprecationLevel.ERROR)
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TuneAppBar(
    onNavClicked: () -> Unit,
    onFilterValueConfirm: (value: String) -> Unit,
    sources: List<OnlineViewModel.Source>,
    selectedSource: OnlineViewModel.Source,
    onSourceSelect: (index: OnlineViewModel.Source) -> Unit,
    sortModes: List<OnlineViewModel.SortMode>,
    selectedSortMode: OnlineViewModel.SortMode,
    onSortModeSelect: (index: OnlineViewModel.SortMode) -> Unit
) {
    var filterValue by remember { mutableStateOf(TextFieldValue()) }
    var expand by remember { mutableStateOf(false) }
    val emphasis = AmbientEmphasisLevels.current

    ExpandableAppBarLayout(
        onNavClicked = onNavClicked,
        onActionClick = { expand = !expand },
        expand = expand,
        title = { Text(text = "在线资源") }
    ) {
        ProvideEmphasis(emphasis = emphasis.medium) {
            Column(
                Modifier.padding(top = 8.dp, start = 32.dp, end = 16.dp, bottom = 16.dp)
            ) {
                // 搜索框
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SearchBox(
                        modifier = Modifier.fillMaxWidth(),
                        value = filterValue,
                        onValueChange = { filterValue = it },
                        onClose = { expand = false },
                        onConfirm = { onFilterValueConfirm(filterValue.text) }
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(asset = Icons.Filled.Language)
                    DropDownSelector(
                        modifier = Modifier.padding(start = 16.dp),
                        items = sources.map { it.label },
                        selectedIndex = sources.indexOf(selectedSource),
                        onSelected = { onSourceSelect(sources[it]) }
                    )
                }
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(asset = Icons.Filled.Sort)
                    DropDownSelector(
                        modifier = Modifier.padding(start = 16.dp),
                        items = sortModes.map { it.label },
                        selectedIndex = sortModes.indexOf(selectedSortMode),
                        onSelected = { onSortModeSelect(sortModes[it]) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchBox(
    modifier: Modifier = Modifier,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onClose: () -> Unit,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(4.dp),
        elevation = 4.dp
    ) {
        Row(
            Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back Button
            IconButton(
                modifier = Modifier.padding(start = 8.dp),
                onClick = onClose
            ) {
                Icon(asset = Icons.Filled.ArrowBack)
            }
            Box(Modifier.weight(1f)) {
                // Hint
                if (value.text.isEmpty()) Text(
                    text = "搜索",
                    style = MaterialTheme.typography.body1.merge(
                        TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colors.onSurface.copy(0.32f)
                        )
                    ),
                )
                // TextField
                BaseTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.body1,
                    imeAction = ImeAction.Search,
                    onImeActionPerformed = { onConfirm() }
                )
            }
            // Search Button
            IconButton(
                modifier = Modifier.padding(end = 8.dp),
                onClick = onConfirm
            ) {
                Icon(asset = Icons.Filled.Search)
            }
        }
    }
}

@Composable
private fun ExpandableAppBarLayout(
    onNavClicked: () -> Unit,
    onActionClick: () -> Unit,
    expand: Boolean,
    title: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    val progress = animate(if (expand) 1f else 0f)
    Surface(
        elevation = 4.dp,
        color = MaterialTheme.colors.surface
    ) {
        Layout(children = {
            TopAppBar(
                modifier = Modifier.layoutId("appbar"),
                navigationIcon = {
                    IconButton(onClick = onNavClicked) {
                        Icon(asset = Icons.Filled.Menu)
                    }
                },
                title = title,
                actions = {
                    IconButton(onClick = onActionClick) {
                        Crossfade(current = expand) {
                            if (it) Icon(asset = Icons.Filled.Close)
                            else Icon(asset = Icons.Filled.FilterAlt)
                        }
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp
            )
            Surface(
                modifier = Modifier.fillMaxWidth().drawOpacity(progress).layoutId("content"),
                color = Color.Transparent,
                content = content
            )
        }, measureBlock = { measurables, constraints ->
            if (measurables.size != 2) error("Need 2 children")

            if (progress == 0f) { // Not expand
                val appbar = measurables.first { it.id == "appbar" }.measure(constraints)
                val appbarHeight = appbar.height
                layout(
                    constraints.maxWidth,
                    appbarHeight
                ) {
                    appbar.placeRelative(0, 0) // TopAppBar
                }
            } else { // Expanding | Expanded
                val appbar = measurables.first { it.id == "appbar" }.measure(constraints)
                val appbarHeight = appbar.height

                val mContent = measurables.first { it.id == "content" }.measure(constraints)
                val mContentHeight = mContent.height

                // 在 appbarHeight 和 appbarHeight + mContentHeight 之间变换
                val height = lerp(appbarHeight, appbarHeight + mContentHeight, progress)

                layout(
                    constraints.maxWidth,
                    height
                ) {
                    appbar.placeRelative(0, 0) // TopAppBar
                    mContent.placeRelative(0, appbarHeight) // Content
                }
            }
        })
    }
}