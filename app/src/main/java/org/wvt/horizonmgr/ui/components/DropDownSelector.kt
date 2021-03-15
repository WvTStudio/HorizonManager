package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DropDownSelector(
    modifier: Modifier = Modifier,
    items: List<String>,
    selectedIndex: Int,
    onSelected: (index: Int) -> Unit
) {
    val sourceInteractionState = remember { MutableInteractionSource() }
    var dropDown by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.clickable(
            onClick = { dropDown = true },
            interactionSource = sourceInteractionState,
            indication = null
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Crossfade(items[selectedIndex]) {
            Text(it, style = MaterialTheme.typography.body1)
        }
        IconButton(
            modifier = Modifier.indication(
                sourceInteractionState,
                rememberRipple(bounded = false, radius = 24.dp)
            ),
            onClick = { dropDown = true }
        ) { Icon(Icons.Filled.ArrowDropDown, "展开") }
        DropdownMenu(
            expanded = dropDown,
            onDismissRequest = { dropDown = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(onClick = {
                    dropDown = false
                    onSelected(index)
                }) { Text(item) }
            }
        }
    }
}
