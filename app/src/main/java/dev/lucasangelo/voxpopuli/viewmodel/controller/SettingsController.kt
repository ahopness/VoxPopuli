package dev.lucasangelo.voxpopuli.viewmodel.controller

import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.datastore.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsController(
    private val repository: AppRepository,
    private val scope: CoroutineScope
) {
    val settings: StateFlow<Settings?> = repository.settings
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun updateSettings(settings: Settings) = scope.launch {
        repository.updateSettings(settings)
    }
}