package com.oscar.orbimusic.presentation.ui.icons

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.oscar.orbimusic.R
object PlayerIcons {
    val Play: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_player_play)

    val Pause: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_player_pause)

    val Next: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_player_next)

    val Previous: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_player_previous)

    val Shuffle: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_player_shuffle)

    val Repeat: ImageVector
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_player_repeat)
}
