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

        val categoryEmbeddings = subscribedCategoriesStrings.map { category ->
            textEmbedder.embed(category)
                .embeddingResult()
                .embeddings()
                .first()
                .floatEmbedding()
                .toList()
        }

        val profileEmbedding =
            if (categoryEmbeddings.isNotEmpty()) {
                val count = categoryEmbeddings.size
                val sum = categoryEmbeddings.reduce { acc, list ->
                    acc.zip(list) { a, b -> a + b }
                }
                sum.map { it / count }
            } else {
                emptyList()
            }

        repository.updateProfile(updatedProfile.copy(
            embedding = profileEmbedding
        ) )
    }
}