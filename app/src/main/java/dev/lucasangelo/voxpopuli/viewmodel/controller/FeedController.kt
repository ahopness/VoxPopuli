package dev.lucasangelo.voxpopuli.viewmodel.controller

import android.util.Log
import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.room.PostEntity
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.time.Duration
import java.time.Instant

class FeedController(
    private val repository: AppRepository,
    private val scope: CoroutineScope,
    private val sources: suspend () -> List<SourceEntity>,
    val feed: StateFlow<List<PostEntity>>,
) {
    val allSources: StateFlow<List<SourceEntity>> = repository.getAllSources()
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val isLoading = MutableStateFlow(false)
    val loadingProgress = MutableStateFlow(0)
    val errorMessage = MutableStateFlow<String?>(null)

    fun requestFeedUpdate(debounced: Boolean = true) { scope.launch {
        isLoading.value = true
        loadingProgress.value = 0
        errorMessage.value = null

        try {
            val semaphore = Semaphore(6)
            sources().map { source -> async { semaphore.withPermit {
                if (debounced) {
                    val fetchingThreshold = Duration.ofMinutes(30)
                    val lastFetched = repository.getLastFetchedFromSource(source.id)
                    val timeSinceLastFetch = Duration.between(lastFetched, Instant.now())

                    if (timeSinceLastFetch < fetchingThreshold) {
                        loadingProgress.update { it+1 }
                        return@withPermit
                    }
                }

                repository.fetchSource(source)

                loadingProgress.update { it+1 }
            } } }.awaitAll()
        } catch (e: Exception) {
            Log.e("VoxPopuli::FeedController::FeedViewModel", "Failed to update feed", e)
            errorMessage.value = e.localizedMessage ?: "An error occurred!"
        } finally {
            isLoading.value = false
            loadingProgress.value = 0
        }
    } }

    fun updateProfileEmbedding(post: PostEntity) = scope.launch {
        val profile = repository.profile.first()

        val alpha = 0.1f

        val u = profile.embedding.map { it * (1f - alpha) }
        val v = post.embedding.map { it * alpha }

        repository.updateProfile(profile.copy(
            embedding = u.zip(v) { a, b -> a + b }
        ))
    }

    fun bookmarkPost(post: PostEntity) = scope.launch {
        repository.updatePost(post.copy(bookmarked = !post.bookmarked))
    }
}