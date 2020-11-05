package org.wvt.horizonmgr.ui.onlineresources

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animate
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BaseTextField
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.layout.id
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import org.wvt.horizonmgr.ui.components.DropDownSelector

private val fastDuration = tween<Float>(durationMillis = 100, easing = FastOutLinearInEasing)

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

    val offset = animate(if (expand) 16.dp else 0.dp)
//    val leftOffset = animate(if (expand) 16.dp else 0.dp)
//    val rightOffset = animate(if (expand) 16.dp else 0.dp)
//    val topOffset = animate(if (expand) 16.dp else 0.dp)

    val emphasis = AmbientEmphasisLevels.current

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
                        .drawLayer(alpha = actionOpacity),
                    onClick = {
                        if (expand) expand = false
                        else onNavClicked()
                    }) {
                    Crossfade(current = expand, animation = fastDuration) {
                        if (it) Icon(asset = Icons.Filled.ArrowBack)
                        else Icon(asset = Icons.Filled.Menu)
                    }
                }
                Crossfade(current = expand, animation = fastDuration) {
                    Box(
                        modifier = Modifier.align(Alignment.CenterStart)
                            .padding(start = 72.dp + offset, end = 24.dp + offset)
                            .fillMaxSize(),
                        alignment = Alignment.CenterStart
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
                            SearchBoxTextField(
                                modifier = Modifier.align(Alignment.CenterStart),
                                filterValue = filterValue,
                                onValueChange = { filterValue = it },
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
                    .drawLayer(alpha = actionOpacity),
                    onClick = {
                        if (expand) onFilterValueConfirm(filterValue.text)
                        else expand = true
                    }
                ) {
                    Crossfade(current = expand, animation = fastDuration) {
                        if (it) Icon(asset = Icons.Filled.Search)
                        else Icon(asset = Icons.Filled.Tune)
                    }
                }
            }

            // Content
            Box(Modifier.wrapContentHeight().layoutId("content")) {
                ProvideEmphasis(emphasis = emphasis.medium) {
                    Column(
                        Modifier.padding(top = 8.dp, start = 32.dp, end = 16.dp, bottom = 16.dp)
                    ) {
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
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SearchBoxTextField(
    modifier: Modifier = Modifier,
    filterValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onImeActionPerformed: (ImeAction) -> Unit
) {
    BaseTextField(
        modifier = modifier,
        value = filterValue,
        onValueChange = {
            if (!it.text.contains('\n')) {
                onValueChange(it)
            }
        },
        textStyle = TextStyle(
            color = MaterialTheme.colors.onSurface.copy(0.72f),
            fontSize = 18.sp
        ),
        imeAction = ImeAction.Search,
        onImeActionPerformed = onImeActionPerformed,
        onTextInputStarted = { it.showSoftwareKeyboard() }
    )
}

@Composable
private fun AppBarLayout(
    expand: Boolean,
    children: @Composable () -> Unit
) {
    val progress = animate(if (expand) 1f else 0f)
    Layout(children) { m: List<Measurable>, c: Constraints ->
        check(m.size == 3)

        if (progress == 0f) { // Not expand
            val appbar = m.first { it.id == "appbar" }.measure(c)
            val appbarHeight = appbar.height
            layout(
                appbar.width,
                appbarHeight
            ) {
                appbar.placeRelative(0, 0) // TopAppBar
            }
        } else { // Expanding | Expanded
            val appbar = m.first { it.id == "appbar" }.measure(c)
            val appbarHeight = appbar.height

            val mContent = m.first { it.id == "content" }.measure(c)
            val mContentHeight = mContent.height

            val searchBoxBG = m.first { it.id == "searchbox_bg" }.measure(c)

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