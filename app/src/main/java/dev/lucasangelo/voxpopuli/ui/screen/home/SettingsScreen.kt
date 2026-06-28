package dev.lucasangelo.voxpopuli.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.ui.component.CleanScaffold
import dev.lucasangelo.voxpopuli.ui.component.FloatingTopBar

@Composable
fun SettingsScreen() {
    CleanScaffold(
        topBar = {
            FloatingTopBar(
                title = stringResource(R.string.feed_settings),
                canGoBack = false,
            )
        }
    ) {

    }
}