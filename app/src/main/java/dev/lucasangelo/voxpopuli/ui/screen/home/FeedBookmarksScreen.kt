package dev.lucasangelo.voxpopuli.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.ui.component.Feed
import dev.lucasangelo.voxpopuli.viewmodel.FeedBookmarksViewModel
import kotlinx.coroutines.launch

@Composable
fun FeedBookmarksScreen(
    viewModel: FeedBookmarksViewModel = hiltViewModel(
        key = "feed_bookmarks"
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
        topBarTitle = stringResource(R.string.feed_bookmarks),
        topBarIconContent = { modifier, _ ->
            Icon(
                painter = painterResource(R.drawable.icon_bookmark),
                contentDescription = null,
                modifier = modifier.clickable(onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                })
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