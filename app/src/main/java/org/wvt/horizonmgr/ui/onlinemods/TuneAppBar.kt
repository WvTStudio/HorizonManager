package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.ui.components.DropDownSelector

private val fastDuration = tween<Float>(durationMillis = 100, easing = FastOutLinearInEasing)

private val searchBoxEnter = tween<Dp>(250, 50, FastOutSlowInEasing)
private val searchBoxExit = tween<Dp>(200, 50, FastOutSlowInEasing)

private val contentAppear = tween<Float>(250, 80, LinearOutSlowInEasing)
private val contentDisappear = tween<Float>(200, 0, FastOutLinearInEasing)

private val tuneExpand = tween<Float>(300, 0, FastOutSlowInEasing)
private val tuneShrink = tween<Float>(250, 0, FastOutSlowInEasing)

@Composable
internal fun TuneAppBar2(
    onNavClicked: () -> Unit,
    onFilterValueConfirm: (value: String) -> Unit,
    sources: List<OnlineViewModel.Source>,
    selectedSource: OnlineViewModel.Source,
    onSourceSelect: (index: OnlineViewModel.Source) -> Unit,
    sortModes: List<OnlineViewModel.SortMode>,
    selectedSortMode: OnlineViewModel.SortMode,
    onSortModeSelect: (index: OnlineViewModel.SortMode) -> Unit
) {
    var expand by remember { mutableStateOf(false) }
//    val searchBoxScale = animate(if (expand) 1f else 1.5f)
//    val searchBoxOpacity = animate(if (expand) 1f else 0f)

    var filterValue by remember { mutableStateOf(TextFieldValue()) }

    val actionOpacity = animate(if (expand) 0.72f else 1f)

    val offset = animate(
        if (expand) 16.dp else 0.dp,
        animSpec = if (expand) searchBoxEnter else searchBoxExit
    )

    val contentOpacity = animate(
        if (expand) 1f else 0f,
        if (expand) contentAppear else contentDisappear
    )
//    val leftOffset = animate(if (expand) 16.dp else 0.dp)
//    val rightOffset = animate(if (expand) 16.dp else 0.dp)
//    val topOffset = animate(if (expand) 16.dp else 0.dp)

    Surface(
        modifier = Modifier.fillMaxWidth().wrapContentHeight().zIndex(4.dp.value),
        elevation = 4.dp
    ) {
        AppBarLayout(expand = expand) {
            // SearchBox Background
            Surface(
                modifier = Modifier.layoutId("searchbox_bg")
                    .fillMaxWidth()
                    .padding(top = offset, start = offset, end = offset)
                    .height(56.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = 4.dp,
                content = emptyContent()
            )

            // AppBar & SearchBox
            Box(
                Modifier.layoutId("appbar")
                    .padding(top = offset)
                    .height(56.dp)
                    .fillMaxWidth()
                    .zIndex(4.dp.value)
            ) {
                // Nav Button
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart)
                        .padding(start = 4.dp + offset)
                        .graphicsLayer(alpha = actionOpacity),
                    onClick = {
                        if (expand) expand = false
                        else onNavClicked()
                    }) {
                    Crossfade(current = expand, animation = fastDuration) {
                        if (it) Icon(imageVector = Icons.Filled.ArrowBack)
                        else Icon(imageVector = Icons.Filled.Menu)
                    }
                }
                Crossfade(current = expand, animation = fastDuration) {
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart)
                            .padding(start = 72.dp + offset, end = 24.dp + offset)
                            .fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (it) { // TextField
                            // Hint
                            if (filterValue.text.isEmpty()) Text(
                                modifier = Modifier.align(Alignment.CenterStart),
                                text = "搜索",
                                style = TextStyle(
                                    color = MaterialTheme.colors.onSurface.copy(0.32f),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                            )
                            // TextField
                            BasicTextField(
                                modifier = Modifier.align(Alignment.CenterStart),
                                value = filterValue,
                                onValueChange = { if (!it.text.contains('\n')) { filterValue = it } },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colors.onSurface.copy(0.72f),
                                    fontSize = 18.sp
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                onImeActionPerformed = { onFilterValueConfirm(filterValue.text) }
                            )
                        } else { // Title
                            Text(
                                modifier = Modifier.align(Alignment.CenterStart),
                                text = "在线资源",
                                style = MaterialTheme.typography.h6
                            )
                        }
                    }
                }
                // Tune Button
                IconButton(modifier = Modifier.align(Alignment.CenterEnd)
                    .padding(end = 4.dp + offset)
                    .graphicsLayer(alpha = actionOpacity),
                    onClick = {
                        if (expand) onFilterValueConfirm(filterValue.text)
                        else expand = true
                    }
                ) {
                    Crossfade(current = expand, animation = fastDuration) {
                        if (it) Icon(imageVector = Icons.Filled.Search)
                        else Icon(imageVector = Icons.Filled.Tune)
                    }
                }
            }

            // Content
            Box(
                Modifier.wrapContentHeight().graphicsLayer(alpha = contentOpacity).layoutId("content")
            ) {
                Providers(AmbientContentAlpha provides ContentAlpha.medium) {
                    Column(
                        Modifier.padding(top = 8.dp, start = 32.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        // Tune Option 1: Source
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Language)
                            DropDownSelector(
                                modifier = Modifier.padding(start = 16.dp),
                                items = sources.map { it.label },
                                selectedIndex = sources.indexOf(selectedSource),
                                onSelected = { onSourceSelect(sources[it]) }
                            )
                        }
                        // Tune Option2: Sort Mode
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Filled.Sort)
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
    }
}

@Composable
private fun AppBarLayout(
    expand: Boolean,
    content: @Composable () -> Unit
) {
    val progress =
        animate(if (expand) 1f else 0f, if (expand) tuneExpand else tuneShrink)
    Layout(content) { m: List<Measurable>, c: Constraints ->
        check(m.size == 3)

        if (progress == 0f) { // Not expand
            val appbar = m.first { it.layoutId == "appbar" }.measure(c)
            val appbarHeight = appbar.height
            layout(
                appbar.width,
                appbarHeight
            ) {
                appbar.placeRelative(0, 0) // TopAppBar
            }
        } else { // Expanding | Expanded
            val appbar = m.first { it.layoutId == "appbar" }.measure(c)
            val appbarHeight = appbar.height

            val mContent = m.first { it.layoutId == "content" }.measure(c)
            val mContentHeight = mContent.height

            val searchBoxBG = m.first { it.layoutId == "searchbox_bg" }.measure(c)

            // 在 appbarHeight 和 appbarHeight + mContentHeight 之间变换
            val height = lerp(appbarHeight, appbarHeight + mContentHeight, progress)

            layout(
                maxOf(appbar.width, mContent.width),
                height
            ) {
                searchBoxBG.placeRelative(0, 0) // SearchBox Background
                appbar.placeRelative(0, 0) // TopAppBar
                mContent.placeRelative(0, appbarHeight) // Content
            }
        }
    }
}