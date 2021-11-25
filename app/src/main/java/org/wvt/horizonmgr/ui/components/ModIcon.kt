package org.wvt.horizonmgr.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.wvt.horizonmgr.R

@Composable
fun ModIcon(
    modifier: Modifier,
    image: ImageBitmap?
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colors.onBackground.copy(0.12f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Crossfade(targetState = image) { image ->
            if (image != null) {
                Image(
                    modifier = modifier,
                    bitmap = image,
                    contentDescription = stringResource(R.string.comp_mod_icon_desc),
                    contentScale = ContentScale.Crop,
                    filterQuality = if (image.height < 256 && image.height < 256) FilterQuality.None
                    else FilterQuality.Medium
                )
            }
        }
    }
}