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
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.data.room.sourceCategoryInfo
import dev.lucasangelo.voxpopuli.viewmodel.controller.ProfileController
import dev.lucasangelo.voxpopuli.viewmodel.controller.SettingsController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
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

    fun initProfileEmbeddings(updatedProfile: Profile) = viewModelScope.launch(Dispatchers.IO) {
        val subscribedCategories = SourceCategory.entries
            .filterNot { updatedProfile.ignoredCategories.contains(it) }
        val subscribedCategoriesStrings = subscribedCategories.map {
            context.getString(sourceCategoryInfo[it]!!.second)
        }

        var profileEmbedding: List<Float> = emptyList()
        subscribedCategoriesStrings.forEach { category ->
            val categoryEmbedding = textEmbedder.embed(category)
                .embeddingResult()
                .embeddings()
                .first()
                .floatEmbedding()
                .toList()

            profileEmbedding =
                if (profileEmbedding.isEmpty()) {
                    categoryEmbedding
                } else {
                    val alpha = 0.1f
                    val u = profileEmbedding.map { it * (1f - alpha) }
                    val v = categoryEmbedding.map { it * alpha }
                    u.zip(v) { a, b -> a + b }
                }
        }

        updateProfile(updatedProfile.copy(
            embedding = profileEmbedding
        ) )
    }
}