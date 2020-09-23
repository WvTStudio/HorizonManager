package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.Row
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.ripple.RippleIndication
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
    val sourceInteractionState = remember { InteractionState() }
    var dropDown by remember { mutableStateOf(false) }

    Crossfade(current = items[selectedIndex]) {
        Row(
            modifier = modifier.clickable(
                onClick = {
                    dropDown = true
                },
                interactionState = sourceInteractionState,
                indication = null
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(it, style = MaterialTheme.typography.body1)
            DropdownMenu(
                expanded = dropDown,
                onDismissRequest = { dropDown = false },
                toggle = {
                    IconButton(
                        modifier = Modifier.indication(
                            sourceInteractionState,
                            RippleIndication(bounded = false, radius = 24.dp)
                        ),
                        onClick = {
                            dropDown = true
                        }
                    ) {
                        Icon(Icons.Filled.ArrowDropDown)
                    }
                }, dropdownContent = {
                    items.forEachIndexed { index, item ->
                        DropdownMenuItem(onClick = {
                            dropDown = false
                            onSelected(index)
                        }) { Text(item) }
                    }
                }
            )
        }
    }
}
