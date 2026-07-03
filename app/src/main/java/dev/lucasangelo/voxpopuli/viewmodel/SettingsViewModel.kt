package dev.lucasangelo.voxpopuli.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import dev.lucasangelo.voxpopuli.data.datastore.Settings
import dev.lucasangelo.voxpopuli.viewmodel.controller.EmbeddingController
import dev.lucasangelo.voxpopuli.viewmodel.controller.ProfileController
import dev.lucasangelo.voxpopuli.viewmodel.controller.SettingsController
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val repository: AppRepository,
    private val textEmbedder: TextEmbedder,
) : ViewModel() {
    private val settingsController = SettingsController(repository, viewModelScope)
    val settings = settingsController.settings
    fun updateSettings(settings: Settings) = settingsController.updateSettings(settings)

    private val profileController = ProfileController(repository, viewModelScope)
    val profile = profileController.profile
    fun updateProfile(profile: Profile) = profileController.updateProfile(profile)

    private val embeddingController = EmbeddingController(context, repository, viewModelScope, textEmbedder)
    fun updateProfileEmbedding(updatedProfile: Profile) = embeddingController.updateProfileEmbedding(updatedProfile)
}