package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.ui.theme.PreviewTheme

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MaterialBanner(
    modifier: Modifier,
    visible: Boolean,
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor),
    icon: @Composable (BoxScope.() -> Unit)? = null,
    text: @Composable BoxScope.() -> Unit,
    dismissButton: @Composable () -> Unit,
    confirmButton: @Composable () -> Unit
) {
    Box(modifier) {
        AnimatedVisibility(
            visible = visible, initiallyVisible = false,
            enter = expandVertically(Alignment.Bottom),
            exit = shrinkVertically(Alignment.Bottom)
        ) {
            Card(Modifier.padding(16.dp), backgroundColor = backgroundColor, contentColor = contentColor) {
                Column(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .paddingFromBaseline(top = 36.dp)
                    ) {
                        if (icon != null) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 16.dp, start = 16.dp)
                                    .size(40.dp),
                                content = icon
                            )
                        }
                        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.body2) {
                            Box(modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp), content = text)
                        }
                    }
                    Row(
                        modifier = Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 8.dp, bottom = 8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        confirmButton()
                        Spacer(Modifier.width(8.dp))
                        dismissButton()
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun MaterialBannerPreview() {
    PreviewTheme {
        var visible by remember { mutableStateOf(true) }
        MaterialBanner(
            modifier = Modifier.fillMaxWidth(),
            visible = visible,
            text = { Text("Test banner text") },
            dismissButton = {
                TextButton(onClick = { visible = false }) {
                    Text("DISMISS")
                }
            },
            confirmButton = {
                TextButton(onClick = { /*TODO*/ }) {
                    Text("CONFIRM")
                }
            }
        )
    }
}