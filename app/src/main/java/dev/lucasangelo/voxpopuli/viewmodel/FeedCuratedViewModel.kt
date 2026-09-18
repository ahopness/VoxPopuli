package dev.lucasangelo.voxpopuli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.room.PostEntity
import dev.lucasangelo.voxpopuli.util.cosineSimilarity
import dev.lucasangelo.voxpopuli.viewmodel.controller.FeedController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FeedCuratedViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    val focusedPost = MutableStateFlow<PostEntity?>(null)

    private val feedController = FeedController(
        repository,
        viewModelScope,
        sources =
            {
                val profile = repository.profile.first()
                repository.getAllSourcesNow()
                    .filterNot {
                        profile
                            .ignoredCategories.contains(it.category)
                    }
            },
        feed = combine(
            repository.getAllPosts(),
            repository.profile,
            repository.getAllSources(),
            focusedPost
        ) { posts, profile, sources, currentFocusedPost ->
            val sourcesMap = sources.associateBy { it.id }
            val targetEmbedding = currentFocusedPost?.embedding ?: profile.embedding

            posts
                .filterNot { post ->
                    val source = sourcesMap[post.sourceId]
                    source != null && profile.ignoredCategories.contains(source.category)
                }
                .sortedWith( comparator =
                    compareByDescending<PostEntity> {
                        it.embedding.cosineSimilarity(to = targetEmbedding)
                    }.thenByDescending {
                        it.publishedAt
                    }
                )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    )

    val sources = feedController.allSources
    val feed = feedController.feed

    val isLoading = feedController.isLoading.asStateFlow()
    val loadingProgress = feedController.loadingProgress.asStateFlow()
    val errorMessage = feedController.errorMessage.asStateFlow()
    fun requestFeedUpdate(debounced: Boolean = true) {
        focusedPost.value = null
        feedController.requestFeedUpdate(debounced)
    }
    init { requestFeedUpdate() }

    fun onPostInteracted(post: PostEntity) {
        if (post.embedding.isNotEmpty()) {
            focusedPost.value = post
        }
    }
    fun bookmarkPost(post: PostEntity) = feedController.bookmarkPost(post)
}
