package dev.lucasangelo.voxpopuli.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.ui.component.CleanScaffold
import dev.lucasangelo.voxpopuli.ui.component.FloatingTopBar
import dev.lucasangelo.voxpopuli.viewmodel.SettingsViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel()
) {
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