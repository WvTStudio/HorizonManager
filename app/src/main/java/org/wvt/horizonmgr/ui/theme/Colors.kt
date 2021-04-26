package org.wvt.horizonmgr.ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

val DarkColorPalette = darkColors(
    primary = Color(MaterialColors.purple[200]!!),
    primaryVariant = Color(MaterialColors.purple[500]!!),
    secondary = Color(MaterialColors.teal[200]!!),
    onSurface = Color.White
)

val LightColorPalette = lightColors(
    primary = Color(MaterialColors.cyan[500]!!),
    primaryVariant = Color(MaterialColors.cyan[700]!!),
    secondary = Color(MaterialColors.cyan[500]!!),
    secondaryVariant = Color(MaterialColors.cyan[700]!!),
    onSecondary = Color.White,
    onPrimary = Color.White
)

object MaterialColors {
    data class MaterialColor(
        val series: String,
        val bright: String,
        val color: Long
    )

    fun isLightColor(color: Color): Boolean {
        return color.luminance() > 0.5
    }

    fun contentColorFor(color: Color): Color {
        return if (isLightColor(color)) Color.Black else Color.White
    }

    fun parseColor(color: Color): Pair<String, Int>? {
        val colorValue = color.value.shr(32).toLong()
        for ((s, map) in series) {
            for ((i, l) in map) {
                if (l == colorValue) {
                    return s to i
                }
            }
        }
        return null
    }

    val red = mapOf(
        50 to 0xFFFFEBEE,
        100 to 0xFFFFCDD2,
        200 to 0xFFEF9A9A,
        300 to 0xFFE57373,
        400 to 0xFFEF5350,
        500 to 0xFFF44336,
        600 to 0xFFE53935,
        700 to 0xFFD32F2F,
        800 to 0xFFC62828,
        900 to 0xFFB71C1C,
        1000 to 0xFFFF8A80,
        1200 to 0xFFFF5252,
        1400 to 0xFFFF1744,
        1700 to 0xFFD50000,
    )

    val pink = mapOf(
        50 to 0xFFFCE4EC,
        100 to 0xFFF8BBD0,
        200 to 0xFFF48FB1,
        300 to 0xFFF06292,
        400 to 0xFFEC407A,
        500 to 0xFFE91E63,
        600 to 0xFFD81B60,
        700 to 0xFFC2185B,
        800 to 0xFFAD1457,
        900 to 0xFF880E4F,
        1000 to 0xFFFF80AB,
        1200 to 0xFFFF4081,
        1400 to 0xFFF50057,
        1700 to 0xFFC51162,
    )

    val purple = mapOf(
        50 to 0xFFF3E5F5,
        100 to 0xFFE1BEE7,
        200 to 0xFFCE93D8,
        300 to 0xFFBA68C8,
        400 to 0xFFAB47BC,
        500 to 0xFF9C27B0,
        600 to 0xFF8E24AA,
        700 to 0xFF7B1FA2,
        800 to 0xFF6A1B9A,
        900 to 0xFF4A148C,
        1000 to 0xFFEA80FC,
        1200 to 0xFFE040FB,
        1400 to 0xFFD500F9,
        1700 to 0xFFAA00FF
    )

    val deepPurple = mapOf(
        50 to 0xFFEDE7F6,
        100 to 0xFFD1C4E9,
        200 to 0xFFB39DDB,
        300 to 0xFF9575CD,
        400 to 0xFF7E57C2,
        500 to 0xFF673AB7,
        600 to 0xFF5E35B1,
        700 to 0xFF512DA8,
        800 to 0xFF4527A0,
        900 to 0xFF311B92,
        1000 to 0xFFB388FF,
        1200 to 0xFF7C4DFF,
        1400 to 0xFF651FFF,
        1700 to 0xFF6200EA,
    )

    val indigo = mapOf(
        50 to 0xFFE8EAF6,
        100 to 0xFFC5CAE9,
        200 to 0xFF9FA8DA,
        300 to 0xFF7986CB,
        400 to 0xFF5C6BC0,
        500 to 0xFF3F51B5,
        600 to 0xFF3949AB,
        700 to 0xFF303F9F,
        800 to 0xFF283593,
        900 to 0xFF1A237E,
        1000 to 0xFF8C9EFF,
        1200 to 0xFF536DFE,
        1400 to 0xFF3D5AFE,
        1700 to 0xFF304FFE,
    )

    val blue = mapOf(
        50 to 0xFFE3F2FD,
        100 to 0xFFBBDEFB,
        200 to 0xFF90CAF9,
        300 to 0xFF64B5F6,
        400 to 0xFF42A5F5,
        500 to 0xFF2196F3,
        600 to 0xFF1E88E5,
        700 to 0xFF1976D2,
        800 to 0xFF1565C0,
        900 to 0xFF0D47A1,
        1000 to 0xFF82B1FF,
        1200 to 0xFF448AFF,
        1400 to 0xFF2979FF,
        1700 to 0xFF2962FF,
    )

    val lightBlue = mapOf(
        50 to 0xFFE1F5FE,
        100 to 0xFFB3E5FC,
        200 to 0xFF81D4FA,
        300 to 0xFF4FC3F7,
        400 to 0xFF29B6F6,
        500 to 0xFF03A9F4,
        600 to 0xFF039BE5,
        700 to 0xFF0288D1,
        800 to 0xFF0277BD,
        900 to 0xFF01579B,
        1000 to 0xFF80D8FF,
        1200 to 0xFF40C4FF,
        1400 to 0xFF00B0FF,
        1700 to 0xFF0091EA,
    )
    val cyan = mapOf(
        50 to 0xFFE0F7FA,
        100 to 0xFFB2EBF2,
        200 to 0xFF80DEEA,
        300 to 0xFF4DD0E1,
        400 to 0xFF26C6DA,
        500 to 0xFF00BCD4,
        600 to 0xFF00ACC1,
        700 to 0xFF0097A7,
        800 to 0xFF00838F,
        900 to 0xFF006064,
        1000 to 0xFF84FFFF,
        1200 to 0xFF18FFFF,
        1400 to 0xFF00E5FF,
        1700 to 0xFF00B8D4,
    )
    val teal = mapOf(
        50 to 0xFFE0F2F1,
        100 to 0xFFB2DFDB,
        200 to 0xFF80CBC4,
        300 to 0xFF4DB6AC,
        400 to 0xFF26A69A,
        500 to 0xFF009688,
        600 to 0xFF00897B,
        700 to 0xFF00796B,
        800 to 0xFF00695C,
        900 to 0xFF004D40,
        1000 to 0xFFA7FFEB,
        1200 to 0xFF64FFDA,
        1400 to 0xFF1DE9B6,
        1700 to 0xFF00BFA5,
    )
    val green = mapOf(
        50 to 0xFFE8F5E9,
        100 to 0xFFC8E6C9,
        200 to 0xFFA5D6A7,
        300 to 0xFF81C784,
        400 to 0xFF66BB6A,
        500 to 0xFF4CAF50,
        600 to 0xFF43A047,
        700 to 0xFF388E3C,
        800 to 0xFF2E7D32,
        900 to 0xFF1B5E20,
        1000 to 0xFFB9F6CA,
        1200 to 0xFF69F0AE,
        1400 to 0xFF00E676,
        1700 to 0xFF00C853,
    )

    val lightGreen = mapOf(
        50 to 0xFFF1F8E9,
        100 to 0xFFDCEDC8,
        200 to 0xFFC5E1A5,
        300 to 0xFFAED581,
        400 to 0xFF9CCC65,
        500 to 0xFF8BC34A,
        600 to 0xFF7CB342,
        700 to 0xFF689F38,
        800 to 0xFF558B2F,
        900 to 0xFF33691E,
        1000 to 0xFFCCFF90,
        1200 to 0xFFB2FF59,
        1400 to 0xFF76FF03,
        1700 to 0xFF64DD17,
    )
    val lime = mapOf(
        50 to 0xFFF9FBE7,
        100 to 0xFFF0F4C3,
        200 to 0xFFE6EE9C,
        300 to 0xFFDCE775,
        400 to 0xFFD4E157,
        500 to 0xFFCDDC39,
        600 to 0xFFC0CA33,
        700 to 0xFFAFB42B,
        800 to 0xFF9E9D24,
        900 to 0xFF827717,
        1000 to 0xFFF4FF81,
        1200 to 0xFFEEFF41,
        1400 to 0xFFC6FF00,
        1700 to 0xFFAEEA00,
    )
    val yellow = mapOf(
        50 to 0xFFFFFDE7,
        100 to 0xFFFFF9C4,
        200 to 0xFFFFF59D,
        300 to 0xFFFFF176,
        400 to 0xFFFFEE58,
        500 to 0xFFFFEB3B,
        600 to 0xFFFDD835,
        700 to 0xFFFBC02D,
        800 to 0xFFF9A825,
        900 to 0xFFF57F17,
        1000 to 0xFFFFFF8D,
        1200 to 0xFFFFFF00,
        1400 to 0xFFFFEA00,
        1700 to 0xFFFFD600,
    )
    val amber = mapOf(
        50 to 0xFFFFF8E1,
        100 to 0xFFFFECB3,
        200 to 0xFFFFE082,
        300 to 0xFFFFD54F,
        400 to 0xFFFFCA28,
        500 to 0xFFFFC107,
        600 to 0xFFFFB300,
        700 to 0xFFFFA000,
        800 to 0xFFFF8F00,
        900 to 0xFFFF6F00,
        1000 to 0xFFFFE57F,
        1200 to 0xFFFFD740,
        1400 to 0xFFFFC400,
        1700 to 0xFFFFAB00,
    )
    val orange = mapOf(
        50 to 0xFFFFF3E0,
        100 to 0xFFFFE0B2,
        200 to 0xFFFFCC80,
        300 to 0xFFFFB74D,
        400 to 0xFFFFA726,
        500 to 0xFFFF9800,
        600 to 0xFFFB8C00,
        700 to 0xFFF57C00,
        800 to 0xFFEF6C00,
        900 to 0xFFE65100,
        1000 to 0xFFFFD180,
        1200 to 0xFFFFAB40,
        1400 to 0xFFFF9100,
        1700 to 0xFFFF6D00,
    )
    val deepOrange = mapOf(
        50 to 0xFFFBE9E7,
        100 to 0xFFFFCCBC,
        200 to 0xFFFFAB91,
        300 to 0xFFFF8A65,
        400 to 0xFFFF7043,
        500 to 0xFFFF5722,
        600 to 0xFFF4511E,
        700 to 0xFFE64A19,
        800 to 0xFFD84315,
        900 to 0xFFBF360C,
        1000 to 0xFFFF9E80,
        1200 to 0xFFFF6E40,
        1400 to 0xFFFF3D00,
        1700 to 0xFFDD2C00,
    )
    val brown = mapOf(
        50 to 0xFFEFEBE9,
        100 to 0xFFD7CCC8,
        200 to 0xFFBCAAA4,
        300 to 0xFFA1887F,
        400 to 0xFF8D6E63,
        500 to 0xFF795548,
        600 to 0xFF6D4C41,
        700 to 0xFF5D4037,
        800 to 0xFF4E342E,
        900 to 0xFF3E2723
    )
    val gray = mapOf(
        50 to 0xFFFAFAFA,
        100 to 0xFFF5F5F5,
        200 to 0xFFEEEEEE,
        300 to 0xFFE0E0E0,
        400 to 0xFFBDBDBD,
        500 to 0xFF9E9E9E,
        600 to 0xFF757575,
        700 to 0xFF616161,
        800 to 0xFF424242,
        900 to 0xFF212121
    )
    val blueGray = mapOf(
        50 to 0xFFECEFF1,
        100 to 0xFFCFD8DC,
        200 to 0xFFB0BEC5,
        300 to 0xFF90A4AE,
        400 to 0xFF78909C,
        500 to 0xFF607D8B,
        600 to 0xFF546E7A,
        700 to 0xFF455A64,
        800 to 0xFF37474F,
        900 to 0xFF263238
    )
    val black = 0xFF000000
    val white = 0xFFFFFFFF

    val series = mapOf<String, Map<Int, Long>>(
        "Red" to red,
        "Pink" to pink,
        "Purple" to purple,
        "Deep Purple" to deepPurple,
        "Indigo" to indigo,
        "Blue" to blue,
        "Light Blue" to lightBlue,
        "Cyan" to cyan,
        "Teal" to teal,
        "Green" to green,
        "Light Green" to lightGreen,
        "Lime" to lime,
        "Yellow" to yellow,
        "Amber" to amber,
        "Orange" to orange,
        "Deep Orange" to deepOrange,
        "Brown" to brown,
        "Gray" to gray,
        "Blue Gray" to blueGray,
    )
}