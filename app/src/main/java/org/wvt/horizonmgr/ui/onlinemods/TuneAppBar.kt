package org.wvt.horizonmgr.ui.onlinemods

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
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
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

private val iconFade = tween<Float>(durationMillis = 120, easing = FastOutSlowInEasing)

/*
 Enter:
 duration:250ms  |  width :  0ms transform 250ms
 duration:250ms  |  height:      40ms transform 290ms
 ------------
 Exit:
 duration:200ms  |  width :      40ms transform 240ms
 duration:200ms  |  height:  0ms transform 200ms
 */
private val searchBoxEnter = tween<Dp>(275, 0, FastOutSlowInEasing) // Shrink to enter
private val searchBoxExit = tween<Dp>(225, 40, FastOutSlowInEasing) // Expand to exit

private val searchBoxEnterColor = tween<Color>(275, 40, FastOutSlowInEasing) // Expand to exit
private val searchBoxExitColor = tween<Color>(225, 40, FastOutSlowInEasing) // Expand to exit

private val contentAppear = tween<Float>(275, 80, LinearOutSlowInEasing)
private val contentDisappear = tween<Float>(225, 0, FastOutLinearInEasing)

private val tuneExpand = tween<Float>(275, 40, FastOutSlowInEasing) // Expand to enter
private val tuneShrink = tween<Float>(225, 0, FastOutSlowInEasing) // Shrink to exit

@Composable
internal fun TuneAppBar(
    enable: Boolean,
    onNavClicked: () -> Unit,
    filterText: String,
    onFilterValueConfirm: (value: String) -> Unit,
    repositories: List<String>,
    selectedRepository: Int,
    onRepositorySelect: (index: Int) -> Unit,
    sortModes: List<String>,
    selectedSortMode: Int,
    onSortModeSelect: (index: Int) -> Unit
) {
    var expand by remember { mutableStateOf(false) }
    var filterValue by remember(filterText) { mutableStateOf(TextFieldValue(filterText)) }
    val updateTransition = updateTransition(expand)

    val actionOpacity by updateTransition.animateFloat(
        targetValueByState = { if (it) 0.72f else 1f }
    )
    val searchBoxPadding by updateTransition.animateDp(
        transitionSpec = { if (targetState) searchBoxEnter else searchBoxExit },
        targetValueByState = { if (it) 16.dp else 0.dp }
    )
    val searchBoxCorner by updateTransition.animateDp(
        transitionSpec = { if (targetState) searchBoxEnter else searchBoxExit },
        targetValueByState = { if (it) 4.dp else 0.dp }
    )
    val searchBoxElevation by updateTransition.animateDp(
        transitionSpec = { if (targetState) searchBoxEnter else searchBoxExit },
        targetValueByState = { if (it) 4.dp else 0.dp }
    )
    val searchBoxBackgroundColor by updateTransition.animateColor(
        transitionSpec = { if (targetState) searchBoxEnterColor else searchBoxExitColor },
        targetValueByState = { if (it) MaterialTheme.colors.surface else Color.Transparent }
    )
    val contentOpacity by updateTransition.animateFloat(
        transitionSpec = { if (targetState) contentAppear else contentDisappear },
        targetValueByState = { if (it) 1f else 0f }
    )
    val searchTextFieldFocus = remember { FocusRequester() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .zIndex(4.dp.value),
        elevation = 4.dp,
        color = AppBarBackgroundColor
    ) {
        AppBarLayout(expand = expand) {
            // SearchBox Background
            Surface(
                modifier = Modifier
                    .layoutId("searchbox_bg")
                    .fillMaxWidth()
                    .padding(
                        top = searchBoxPadding,
                        start = searchBoxPadding,
                        end = searchBoxPadding
                    )
                    .height(56.dp)
                    .graphicsLayer(
                        alpha = contentOpacity,
                        shadowElevation = with(LocalDensity.current) { searchBoxElevation.toPx() },
                        shape = RoundedCornerShape(searchBoxCorner)
                    ),
                shape = RoundedCornerShape(searchBoxCorner),
                color = LocalElevationOverlay.current?.apply(
                    color = MaterialTheme.colors.surface,
                    elevation = LocalAbsoluteElevation.current + 4.dp
                ) ?: MaterialTheme.colors.surface, // 手动应用 4dp 海拔的 surface 颜色效果
                content = {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .apply {
                                if (expand) clickable {
                                    searchTextFieldFocus.requestFocus()
                                }
                            }
                    )
                },
            )

            // AppBar & SearchBox
            Box(
                Modifier
                    .layoutId("appbar")
                    .padding(
                        top = searchBoxPadding,
                        start = searchBoxPadding,
                        end = searchBoxPadding
                    )
                    .height(56.dp)
                    .fillMaxWidth()
                    .zIndex(4.dp.value)
            ) {
                // Nav Button
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 4.dp)
                        .graphicsLayer(alpha = actionOpacity),
                    onClick = {
                        if (expand) expand = false
                        else onNavClicked()
                    }
                ) {
                    Crossfade(targetState = expand, animationSpec = iconFade) {
                        if (it) Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "关闭",
                            tint = MaterialTheme.colors.onSurface
                        )
                        else Icon(imageVector = Icons.Filled.Menu, contentDescription = "菜单")
                    }
                }
                Crossfade(targetState = expand, animationSpec = iconFade) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(
                                start = 72.dp,
                                end = 24.dp
                            )
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
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .focusRequester(searchTextFieldFocus),
                                value = filterValue,
                                onValueChange = {
                                    if (!it.text.contains('\n')) {
                                        filterValue = it
                                    }
                                },
                                textStyle = TextStyle(
                                    color = MaterialTheme.colors.onSurface.copy(0.72f),
                                    fontSize = 18.sp
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(onSearch = {
                                    searchTextFieldFocus.freeFocus()
                                    onFilterValueConfirm(filterValue.text)
                                })
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
                if (enable) {
                    // Tune Button
                    IconButton(modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp)
                        .graphicsLayer(alpha = actionOpacity),
                        onClick = {
                            if (expand) { // Search
                                searchTextFieldFocus.freeFocus()
                                onFilterValueConfirm(filterValue.text)
                            } else { // Expand
                                expand = true
                            }
                        }
                    ) {
                        Crossfade(targetState = expand, animationSpec = iconFade) {
                            if (it) Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "搜索",
                                tint = MaterialTheme.colors.onSurface
                            )
                            else Icon(imageVector = Icons.Filled.Tune, contentDescription = "过滤选项")
                        }
                    }
                }
            }

            // Content
            Box(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .graphicsLayer(alpha = contentOpacity)
                    .layoutId("content")
            ) {
                TuneContent(
                    repositories,
                    selectedRepository,
                    onRepositorySelect,
                    sortModes,
                    selectedSortMode,
                    onSortModeSelect
                )
            }
        }
    }
}

@Composable
private fun AppBarLayout(
    expand: Boolean,
    content: @Composable () -> Unit
) {
    val progress by updateTransition(expand).animateFloat(transitionSpec = {
        if (targetState) tuneExpand else tuneShrink
    }, targetValueByState = {
        if (it) 1f else 0f
    })
    Layout(content) { m: List<Measurable>, c: Constraints ->
        check(m.size == 3)

        // Expanding | Expanded
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

@Composable
private fun TuneContent(
    repositories: List<String>,
    selectedRepository: Int,
    onRepositorySelect: (index: Int) -> Unit,
    sortModes: List<String>,
    selectedSortMode: Int,
    onSortModeSelect: (index: Int) -> Unit
) {
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Column(
            Modifier
                .padding(top = 8.dp, start = 32.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            // Tune Option 1: Source
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Language, contentDescription = "源仓库")
                DropDownSelector(
                    modifier = Modifier.padding(horizontal = 16.dp).weight(1f).wrapContentHeight(),
                    items = repositories,
                    selectedIndex = selectedRepository,
                    onSelected = { onRepositorySelect(it) }
                )
            }
            // Tune Option2: Sort Mode
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Filled.Sort, contentDescription = "排序方式")
                DropDownSelector(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                        .wrapContentHeight(),
                    items = sortModes,
                    selectedIndex = selectedSortMode,
                    onSelected = { onSortModeSelect(it) }
                )
            }
        }
    }
}