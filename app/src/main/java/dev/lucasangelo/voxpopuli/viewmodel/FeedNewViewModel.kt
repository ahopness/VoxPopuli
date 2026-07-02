package dev.lucasangelo.voxpopuli.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.lucasangelo.voxpopuli.data.AppRepository
import dev.lucasangelo.voxpopuli.data.room.PostEntity
import dev.lucasangelo.voxpopuli.viewmodel.controller.FeedController
import dev.lucasangelo.voxpopuli.viewmodel.controller.ProfileController
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FeedNewViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {
    private val feedController = FeedController(
        repository,
        viewModelScope,
        sources = { repository.getAllSourcesNow() },
        feed =
            repository.getAllNewPosts()
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
    fun requestFeedUpdate(debounced: Boolean = true) = feedController.requestFeedUpdate(debounced)
    init { requestFeedUpdate() }

    fun updateProfileEmbedding(post: PostEntity) = feedController.updateProfileEmbedding(post)
    fun bookmarkPost(post: PostEntity) = feedController.bookmarkPost(post)
}