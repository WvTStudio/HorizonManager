package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R

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
                ripple(bounded = false, radius = 24.dp)
            ),
            onClick = { dropDown = true }
        ) { Icon(Icons.Rounded.ArrowDropDown, stringResource(R.string.button_action_expand)) }
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
