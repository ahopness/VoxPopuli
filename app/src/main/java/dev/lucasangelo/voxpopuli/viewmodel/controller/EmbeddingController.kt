package dev.lucasangelo.voxpopuli.viewmodel.controller

import android.content.Context
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.data.room.sourceCategoryInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EmbeddingController (
    private val context: Context,
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val textEmbedder: TextEmbedder,
) {
    fun updateProfileEmbedding(updatedProfile: Profile) = scope.launch(Dispatchers.IO) {
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

        repository.updateProfile(updatedProfile.copy(
            embedding = profileEmbedding
        ) )
    }
}