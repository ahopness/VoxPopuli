package dev.lucasangelo.voxpopuli.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import dev.lucasangelo.voxpopuli.ui.component.CleanScaffold
import dev.lucasangelo.voxpopuli.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.voxpopuli.util.feedTypeMetas
import dev.lucasangelo.voxpopuli.util.sourceCategoryMetas
import dev.lucasangelo.voxpopuli.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

enum class FeedType { BOOKMARKS, CURATED, NEW, CATEGORY, SOURCE }
@Composable
fun FeedScreen(
    type: FeedType,
    customCategory: SourceCategory = SourceCategory.GENERAL,
    customSource: SourceEntity? = null,
    viewModel: FeedViewModel = hiltViewModel(
        creationCallback = { factory: FeedViewModel.Factory ->
            factory.create(type, customCategory, customSource)
        }
    )
) {
    if (type == FeedType.SOURCE  && customSource == null) return

//    LaunchedEffect(Unit) { viewModel.updateFeed() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    CleanScaffold(
        topBar = { FloatingExtendedTopBar(
            listState = listState,
            title =
                when (type) {
                    FeedType.BOOKMARKS ->
                        stringResource(R.string.feed_bookmarks)
                    FeedType.CURATED ->
                        stringResource(R.string.feed_curated)
                    FeedType.NEW ->
                        stringResource(R.string.feed_new)
                    FeedType.CATEGORY ->
                        stringResource(sourceCategoryMetas[customCategory]!!.second)
                    FeedType.SOURCE ->
                        customSource!!.name
                },
            iconContent = { modifier, _ ->
                val clickableModifier = modifier.clickable(onClick = {
                    coroutineScope.launch { listState.animateScrollToItem(0) }
                } )

                if (type == FeedType.SOURCE)
                    AsyncImage(
                        model = customSource!!.logoUrl,
                        contentDescription = null,
                        modifier = clickableModifier
                    )
                else
                    Icon(
                        painter = painterResource( id =
                            when (type) {
                                FeedType.BOOKMARKS,
                                FeedType.CURATED,
                                FeedType.NEW ->
                                    feedTypeMetas[type]!!.first
                                FeedType.CATEGORY ->
                                    sourceCategoryMetas[customCategory]!!.first
                                else ->
                                    -1
                            }
                        ),
                        contentDescription = null,
                        modifier = clickableModifier
                    )
            },
            canGoBack = false,
            actions = emptyList()
        ) }
    ) {

    }
}