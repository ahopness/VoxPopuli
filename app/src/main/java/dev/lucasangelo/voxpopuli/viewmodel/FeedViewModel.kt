package dev.lucasangelo.voxpopuli.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import dev.lucasangelo.voxpopuli.data.room.PostEntity
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedType
import dev.lucasangelo.voxpopuli.util.cosineSimilarity
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

@HiltViewModel(assistedFactory = FeedViewModel.Factory::class)
class FeedViewModel @AssistedInject constructor(
    @ApplicationContext context: Context,
    private val repository: AppRepository,
    @Assisted val type: FeedType,
    @Assisted val customCategory: SourceCategory?,
    @Assisted val customSource: SourceEntity?,
) : ViewModel() {
    @AssistedFactory interface Factory {
        fun create(
            type: FeedType,
            customCategory: SourceCategory? = null,
            customSource: SourceEntity? = null,
        ) : FeedViewModel
    }

    val feed: StateFlow<List<PostEntity>> = combine(
            when(type) {
                FeedType.BOOKMARKS -> repository.getAllBookmarkedPosts()
                FeedType.CURATED -> repository.getAllPosts()
                FeedType.NEW -> repository.getAllNewPosts()
                FeedType.CATEGORY -> repository.getAllPostsIn(customCategory!!)
                FeedType.SOURCE -> repository.getAllPostsBy(customSource!!.id)
            },
            repository.profile,
            repository.getAllSources()
        ) { posts, profile, sources ->
            val sourcesMap = sources.associateBy { it.id }

            if (type == FeedType.CURATED)
                posts
                    .filterNot { post ->
                        val source = sourcesMap[post.sourceId]
                        source != null && profile.ignoredCategories.contains(source.category)
                    }
                    .sortedByDescending { post ->
                        post.embedding.cosineSimilarity(to = profile.embedding)
                    }
            else
                posts
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _loadingProgress = MutableStateFlow(0)
    val loadingProgress = _loadingProgress.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    init { requestFeedUpdate(true) }
    fun requestFeedUpdate(debounced: Boolean = true) = viewModelScope.launch {
        _isLoading.value = true
        _loadingProgress.value = 0
        _errorMessage.value = null

        suspend fun fetchSource(source: SourceEntity) {
            if (debounced) {
                val fetchingThreshold = Duration.ofMinutes(30)
                val lastFetched = repository.getLastFetchedFromSource(source.id)
                val timeSinceLastFetch = Duration.between(lastFetched, Instant.now())

                if (timeSinceLastFetch < fetchingThreshold) {
                    _loadingProgress.update { it+1 }
                    return
                }
            }

            repository.fetchSource(source)

            _loadingProgress.update { it+1 }
        }

        try {
            when (type) {
                FeedType.BOOKMARKS -> { }
                FeedType.CURATED -> {
                    val currentProfile = repository.profile.first()
                    repository.getAllSourcesNow()
                        .filterNot {
                            currentProfile
                                .ignoredCategories.contains(it.category)
                        }
                        .map {
                            async { fetchSource(it) }
                        }
                        .awaitAll()
                }
                FeedType.NEW -> {
                    repository.getAllSourcesNow()
                        .map {
                            async { fetchSource(it) }
                        }
                        .awaitAll()
                }
                FeedType.CATEGORY -> {
                    repository.getAllSourcesNow()
                        .filter {
                            it.category == customCategory
                        }
                        .map {
                            async { fetchSource(it) }
                        }
                        .awaitAll()
                }
                FeedType.SOURCE -> {
                    fetchSource(
                        repository.getSource(customSource!!.id)
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("FeedViewModel", "Failed to update feed", e)
            _errorMessage.value = e.localizedMessage ?: "An error occurred!"
        } finally {
            _isLoading.value = false
            _loadingProgress.value = 0
        }
    }

    fun updateProfileEmbedding(post: PostEntity) = viewModelScope.launch {
        val profile = repository.profile.first()

        val alpha = 0.1f

        val u = profile.embedding.map { it * (1f - alpha) }
        val v = post.embedding.map { it * alpha }

        repository.updateProfile(profile.copy(
            embedding = u.zip(v) { a, b -> a + b }
        ))
    }

    fun bookmarkPost(post: PostEntity) = viewModelScope.launch {
        repository.updatePost(post.copy(bookmarked = !post.bookmarked))
    }
}