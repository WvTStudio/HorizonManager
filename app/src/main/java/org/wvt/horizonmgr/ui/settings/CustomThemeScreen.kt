package org.wvt.horizonmgr.ui.settings

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import org.wvt.horizonmgr.ui.theme.*

private enum class CheckedColorType {
    LIGHT_PRIMARY, LIGHT_PRIMARY_VARIANT,
    LIGHT_SECONDARY, LIGHT_SECONDARY_VARIANT,
    DARK_PRIMARY, DARK_PRIMARY_VARIANT,
    DARK_SECONDARY
}

data class CustomColor(
    var primary: Pair<String, Int>,
    var primaryVariant: Pair<String, Int>,
    var secondary: Pair<String, Int>,
    var secondaryVariant: Pair<String, Int>
)

@Composable
fun CustomThemeScreen(requestClose: () -> Unit) {
    val colors = remember { MaterialColors.series }
    var checkedColorType by remember { mutableStateOf(CheckedColorType.LIGHT_PRIMARY) }
    val themeConfig = LocalThemeConfig.current
    val themeController = LocalThemeController.current

    var lightColor by remember {
        mutableStateOf(
            CustomColor(
                primary = MaterialColors.parseColor(themeConfig.lightColor.primary)
                    ?: "cyan" to 500,
                primaryVariant = MaterialColors.parseColor(themeConfig.lightColor.primaryVariant)
                    ?: "cyan" to 700,
                secondary = MaterialColors.parseColor(themeConfig.lightColor.secondary)
                    ?: "teal" to 500,
                secondaryVariant = MaterialColors.parseColor(themeConfig.lightColor.secondaryVariant)
                    ?: "teal" to 700
            )
        )
    }

    var darkColor by remember {
        mutableStateOf(
            CustomColor(
                primary = MaterialColors.parseColor(themeConfig.darkColor.primary) ?: "cyan" to 500,
                primaryVariant = MaterialColors.parseColor(themeConfig.darkColor.primaryVariant)
                    ?: "cyan" to 700,
                secondary = MaterialColors.parseColor(themeConfig.darkColor.secondary)
                    ?: "teal" to 500,
                secondaryVariant = MaterialColors.parseColor(themeConfig.darkColor.secondaryVariant)
                    ?: "teal" to 700
            )
        )
    }

    var accentColor by remember { mutableStateOf(themeConfig.appbarAccent) }

    fun getColor(color: Pair<String, Int>): Color {
        return Color(colors[color.first]!![color.second]!!)
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                modifier = Modifier.zIndex(4f),
                navigationIcon = {
                    IconButton(onClick = requestClose) {
                        Icon(imageVector = Icons.Filled.ArrowBack, "返回")
                    }
                }, title = {
                    Text("自定义主题")
                }, backgroundColor = AppBarBackgroundColor
            )

            MaterialColorPalette(
                modifier = Modifier.height(256.dp),
                colors = colors,
                onSelect = { s, i ->
                    when (checkedColorType) {
                        CheckedColorType.LIGHT_PRIMARY -> lightColor =
                            lightColor.copy(primary = s to i)
                        CheckedColorType.LIGHT_PRIMARY_VARIANT -> lightColor =
                            lightColor.copy(primaryVariant = s to i)
                        CheckedColorType.LIGHT_SECONDARY -> lightColor =
                            lightColor.copy(secondary = s to i)
                        CheckedColorType.LIGHT_SECONDARY_VARIANT -> lightColor =
                            lightColor.copy(secondaryVariant = s to i)
                        CheckedColorType.DARK_PRIMARY -> darkColor =
                            darkColor.copy(primary = s to i)
                        CheckedColorType.DARK_PRIMARY_VARIANT -> darkColor =
                            darkColor.copy(primaryVariant = s to i)
                        CheckedColorType.DARK_SECONDARY -> darkColor =
                            darkColor.copy(secondary = s to i)
                    }
                },
                selected = when (checkedColorType) {
                    CheckedColorType.LIGHT_PRIMARY -> lightColor.primary
                    CheckedColorType.LIGHT_PRIMARY_VARIANT -> lightColor.primaryVariant
                    CheckedColorType.LIGHT_SECONDARY -> lightColor.secondary
                    CheckedColorType.LIGHT_SECONDARY_VARIANT -> lightColor.secondaryVariant
                    CheckedColorType.DARK_PRIMARY -> darkColor.primary
                    CheckedColorType.DARK_PRIMARY_VARIANT -> darkColor.primaryVariant
                    CheckedColorType.DARK_SECONDARY -> darkColor.secondary
                }
            )
            Divider(Modifier.fillMaxWidth())
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())) {
                Text(
                    modifier = Modifier.padding(top = 16.dp, start = 24.dp),
                    text = "亮色主题",
                    color = MaterialTheme.colors.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectColorItem(text = "主题色",
                        color = getColor(lightColor.primary),
                        selected = checkedColorType == CheckedColorType.LIGHT_PRIMARY,
                        onSelect = { checkedColorType = CheckedColorType.LIGHT_PRIMARY })
                    SelectColorItem(text = "主题色 - 变体",
                        color = getColor(lightColor.primaryVariant),
                        selected = checkedColorType == CheckedColorType.LIGHT_PRIMARY_VARIANT,
                        onSelect = {
                            checkedColorType = CheckedColorType.LIGHT_PRIMARY_VARIANT
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectColorItem(text = "补充色",
                        color = getColor(lightColor.secondary),
                        selected = checkedColorType == CheckedColorType.LIGHT_SECONDARY,
                        onSelect = { checkedColorType = CheckedColorType.LIGHT_SECONDARY })
                    SelectColorItem(text = "补充色 - 变体",
                        color = getColor(lightColor.secondaryVariant),
                        selected = checkedColorType == CheckedColorType.LIGHT_SECONDARY_VARIANT,
                        onSelect = {
                            checkedColorType = CheckedColorType.LIGHT_SECONDARY_VARIANT
                        }
                    )
                }
                val interactionSource = remember { MutableInteractionSource() }
                Row(
                    Modifier
                        .clickable(
                            indication = null,
                            interactionSource = interactionSource,
                            onClick = { accentColor = !accentColor })
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 42.dp, end = 42.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        modifier = Modifier.indication(
                            interactionSource,
                            rememberRipple(bounded = false, radius = 24.dp)
                        ),
                        checked = accentColor,
                        onCheckedChange = { accentColor = it }
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("强调色状态栏")
                }
                Divider(
                    Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth()
                )
                Text(
                    modifier = Modifier.padding(start = 24.dp),
                    text = "暗色主题",
                    color = MaterialTheme.colors.primary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectColorItem(text = "主题色",
                        color = getColor(darkColor.primary),
                        selected = checkedColorType == CheckedColorType.DARK_PRIMARY,
                        onSelect = { checkedColorType = CheckedColorType.DARK_PRIMARY })
                    SelectColorItem(text = "主题色 - 变体",
                        color = getColor(darkColor.primaryVariant),
                        selected = checkedColorType == CheckedColorType.DARK_PRIMARY_VARIANT,
                        onSelect = {
                            checkedColorType = CheckedColorType.DARK_PRIMARY_VARIANT
                        }
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SelectColorItem(text = "补充色",
                        color = getColor(darkColor.secondary),
                        selected = checkedColorType == CheckedColorType.DARK_SECONDARY,
                        onSelect = { checkedColorType = CheckedColorType.DARK_SECONDARY })
                }
                Spacer(modifier = Modifier.height(64.dp))
            }
        }

        Button(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            onClick = {
                themeController.setLightColor(
                    lightColors(
                        primary = getColor(lightColor.primary),
                        primaryVariant = getColor(lightColor.primaryVariant),
                        onPrimary = MaterialColors.contentColorFor(getColor(lightColor.primary)),
                        secondary = getColor(lightColor.secondary),
                        secondaryVariant = getColor(lightColor.secondaryVariant),
                        onSecondary = MaterialColors.contentColorFor(getColor(lightColor.secondary))
                    )
                )
                themeController.setDarkColor(
                    darkColors(
                        primary = getColor(darkColor.primary),
                        primaryVariant = getColor(darkColor.primaryVariant),
                        onPrimary = MaterialColors.contentColorFor(getColor(darkColor.primary)),
                        secondary = getColor(darkColor.secondary),
                        onSecondary = MaterialColors.contentColorFor(getColor(darkColor.secondary))
                    )
                )
                themeController.setAppbarAccent(accentColor)
            }
        ) {
            Icon(imageVector = Icons.Filled.Check, "保存")
            Text("保存")
        }
        /*Button(modifier = Modifier.padding(16.dp).align(Alignment.BottomStart),
            onClick = {
                themeController.setLightColor(LightColorPalette)
                themeController.setDarkColor(DarkColorPalette)
            }) {
            Icon(asset = Icons.Filled.Restore)
            Text("重置")
        }*/
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SelectColorItem(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val shrinkTween = remember { tween<Float>(250, 0, LinearOutSlowInEasing) }
    val expandTween = remember { tween<Float>(250, 40, LinearOutSlowInEasing) }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val scale by updateTransition(targetState = pressed, label = "transition").animateFloat(
        transitionSpec = { if (targetState) shrinkTween else expandTween },
        targetValueByState = {
            if (selected) {
                if (it) 1f else 1.1f
            } else {
                if (it) 0.9f else 1f
            }
        }, label = "scale"
    )

    Column(modifier) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(text = text)
        }

        Surface(
            modifier = Modifier
                .size(128.dp, 96.dp)
                .padding(top = 8.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale),
            color = animateColorAsState(color).value,
            elevation = animateDpAsState(if (selected) 8.dp else 0.dp).value,
            shape = RoundedCornerShape(size = 4.dp),
            onClick = onSelect,
            interactionSource = interactionSource,
            indication = null
        ) {}
    }
}

@Composable
private fun MaterialColorPalette(
    modifier: Modifier = Modifier,
    colors: Map<String, Map<Int, Long>>,
    onSelect: (series: String, bright: Int) -> Unit,
    selected: Pair<String, Int>
) {
    val tags = remember {
        listOf(
            "50", "100", "200", "300", "400", "500", "600", "700", "800", "900",
            "A100", "A200", "A400", "A700"
        )
    }
    Column(
        modifier
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
    ) {
        Row {

            Column {
                Spacer(
                    Modifier
                        .padding(1.dp)
                        .height(42.dp)
                )
                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                    for (i in colors) {
                        Box(
                            modifier = Modifier
                                .padding(1.dp)
                                .height(42.dp)
                                .width(72.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = i.key,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            Column {
                Row {
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        tags.forEach {
                            Box(
                                modifier = Modifier
                                    .padding(1.dp)
                                    .size(42.dp),
                                contentAlignment = Alignment.Center
                            ) { Text(text = it) }
                        }
                    }
                }
                for ((series, map) in colors) {
                    Row {
                        for ((bright, color) in map) {
                            ColorItem(
                                modifier = Modifier.padding(1.dp),
                                color = Color(color),
                                selected = selected.first == series && selected.second == bright,
                                onSelect = { onSelect(series, bright) }
                            )
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
private fun ColorItem(
    modifier: Modifier = Modifier,
    color: Color,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val roundPercent = animateIntAsState(if (selected) 50 else 0).value
    val contentColor = remember(color) { MaterialColors.contentColorFor(color) }

    Surface(
        modifier = modifier.size(42.dp),
        color = color,
        shape = RoundedCornerShape(percent = roundPercent),
        onClick = onSelect
    ) {
        AnimatedVisibility(
            visible = selected, enter = remember { fadeIn() }, exit = remember { fadeOut() }
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "T", color = contentColor)
            }
        }
    }
}

@Preview
@Composable
private fun ColorItemPreview() {
    PreviewTheme {
        var selected by remember { mutableStateOf(true) }
        Surface(Modifier.padding(32.dp)) {
            ColorItem(
                modifier = Modifier.padding(1.dp),
                color = Color(MaterialColors.purple[500]!!),
                selected = selected,
                onSelect = { selected = true }
            )
        }
    }
}