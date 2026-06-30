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
import dev.lucasangelo.voxpopuli.util.sourceCategoryMetas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val repository: AppRepository,
    private val textEmbedder: TextEmbedder,
) : ViewModel() {
    val settings: StateFlow<Settings> = repository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )
    fun updateSettings(settings: Settings) = viewModelScope.launch {
        repository.updateSettings(settings)
    }

    val profile: StateFlow<Profile> = repository.profile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Profile()
        )

    fun updateProfileEmbeddings(updatedProfile: Profile) = viewModelScope.launch(Dispatchers.IO) {
        val ignoredCategoriesStrings = updatedProfile.ignoredCategories.map {
            context.getString(sourceCategoryMetas[it]!!.second)
        }

        var profileEmbedding: List<Float> = emptyList()
        withContext(Dispatchers.Main) { ignoredCategoriesStrings.forEach { category ->
                val categoryEmbedding = textEmbedder.embed(category)
                    .embeddingResult()
                    .embeddings()
                    .first()
                    .floatEmbedding()
                    .toList()

                val alpha = 0.1f
                val u = profileEmbedding.map { it * (1f - alpha) }
                val v = categoryEmbedding.map { it * alpha }
                profileEmbedding = u.zip(v) { a, b -> a + b }
        } }

        repository.updateProfile(updatedProfile.copy(
            embedding = profileEmbedding
        ))
    }
}