package dev.lucasangelo.voxpopuli.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.scale
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import dev.lucasangelo.voxpopuli.ui.component.Feed
import dev.lucasangelo.voxpopuli.ui.component.MonochromeAsyncImage
import dev.lucasangelo.voxpopuli.viewmodel.FeedSourceViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedSourceScreen(
    customSource: SourceEntity,
    viewModel: FeedSourceViewModel = hiltViewModel(
        key = "feed_source_${customSource.id}",
        creationCallback = { factory: FeedSourceViewModel.Factory ->
            factory.create(customSource)
        }
    )
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val loadingProgress by viewModel.loadingProgress.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val sources by viewModel.sources.collectAsStateWithLifecycle()
    val sourcesMap = remember(sources) { sources.associateBy { it.id } }

    val feed by viewModel.feed.collectAsStateWithLifecycle()

    Feed(
        topBarTitle = customSource.name,
        topBarIconContent = { modifier, _ ->
            MonochromeAsyncImage(
                model = customSource.logoUrl,
                contentDescription = null,
                modifier = modifier
                    .scale(0.8f)
                    .clickable(onClick = {
                        coroutineScope.launch { listState.animateScrollToItem(0) }
                    } )
            )
        },
        isLoading,
        loadingProgress,
        errorMessage,
        feed,
        listState,
        sourcesMap,
        onRequestFeedUpdate = { viewModel.requestFeedUpdate(it) },
        onPostInteracted = { viewModel.updateProfileEmbedding(it) },
        onPostBookmarked = { viewModel.bookmarkPost(it) }
    )
}