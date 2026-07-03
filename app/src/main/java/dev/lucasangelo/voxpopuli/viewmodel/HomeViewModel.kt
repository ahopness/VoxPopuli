package dev.lucasangelo.voxpopuli.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import dev.lucasangelo.voxpopuli.viewmodel.controller.SettingsController
import dev.lucasangelo.voxpopuli.viewmodel.controller.SourcesController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext context: Context,
    private val repository: AppRepository,
) : ViewModel() {
    private val settingsController = SettingsController(repository, viewModelScope)
    val settings = settingsController.settings

    private val sourcesController = SourcesController(repository, viewModelScope)
    val sources = sourcesController.sources
}