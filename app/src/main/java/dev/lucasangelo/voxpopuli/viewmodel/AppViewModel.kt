package dev.lucasangelo.voxpopuli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.viewmodel.controller.SettingsController
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val repository: AppRepository,
) : ViewModel() {
    private val settingsController = SettingsController(repository, viewModelScope)
    val settings = settingsController.settings
}