package dev.lucasangelo.voxpopuli.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.ui.util.fastForEach
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant

@HiltViewModel(assistedFactory = FeedViewModel.Factory::class)
class FeedViewModel @AssistedInject constructor(
    @ApplicationContext context: Context,
    private val repository: AppRepository,
    @Assisted val type: FeedType,
    @Assisted val customCategory: SourceCategory,
    @Assisted val customSource: SourceEntity?,
) : ViewModel() {
    @AssistedFactory interface Factory {
        fun create(
            type: FeedType,
            customCategory: SourceCategory = SourceCategory.GENERAL,
            customSource: SourceEntity? = null
        ) : FeedViewModel
    }

    val feed: StateFlow<List<PostEntity>> =
        when(type) {
            FeedType.BOOKMARKS -> repository.getAllBookmarkedPosts()
            FeedType.CURATED -> repository.getAllPosts()
            FeedType.NEW -> repository.getAllNewPosts()
            FeedType.CATEGORY -> repository.getAllPostsIn(customCategory)
            FeedType.SOURCE -> repository.getAllPostsBy(customSource!!.id)
        }
        .map {
            if (type == FeedType.CURATED)
                it.sortedByDescending { post ->
                    post.embedding.cosineSimilarity(other = profile.value.embedding)
                }
            else
                it.sortedByDescending { post ->
                    post.publishedAt
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    val profile: StateFlow<Profile> = repository.profile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Profile()
        )

    fun requestFeedUpdate(debounced: Boolean = true) = viewModelScope.launch {
        _isLoading.value = true
        _errorMessage.value = null

        suspend fun fetchSource(source: SourceEntity) {
            if (debounced) {
                val fetchingThreshold = Duration.ofMinutes(30)
                val lastFetched = repository.getLastFetchedFromSource(source.id)
                val timeSinceLastFetch = Duration.between(lastFetched, Instant.now())

                if (timeSinceLastFetch < fetchingThreshold) return
            }

            repository.fetchSource(source)
        }

        try {
            when (type) {
                FeedType.BOOKMARKS -> { }
                FeedType.CURATED -> {
                    repository.getAllSourcesNow()
                        .filterNot {
                            profile.value
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
        }
    }

    fun onProfileInteraction(postEntity: PostEntity) = viewModelScope.launch {
        val alpha = 0.1f

        val u = profile.value.embedding.map { it * (1f - alpha) }
        val v = postEntity.embedding.map { it * alpha }

        repository.updateProfile(profile.value.copy(
            embedding = u + v
        ))
    }
}