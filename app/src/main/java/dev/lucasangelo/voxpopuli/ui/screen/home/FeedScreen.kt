package dev.lucasangelo.voxpopuli.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.room.PostEntity
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import dev.lucasangelo.voxpopuli.ui.component.CleanScaffold
import dev.lucasangelo.voxpopuli.ui.component.FloatingExtendedTopBar
import dev.lucasangelo.voxpopuli.ui.component.FloatingExtendedTopBarActionItem
import dev.lucasangelo.voxpopuli.ui.component.floatingExtendedTopBarPadding
import dev.lucasangelo.voxpopuli.ui.component.floatingNavigationBarPadding
import dev.lucasangelo.voxpopuli.util.LinkMetadata
import dev.lucasangelo.voxpopuli.util.feedTypeMetas
import dev.lucasangelo.voxpopuli.util.fetchLinkMetadata
import dev.lucasangelo.voxpopuli.util.overlayTransparencyColor
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
        key = remember(type, customCategory, customSource) {
            when (type) {
                FeedType.BOOKMARKS -> "feed_bookmarks"
                FeedType.CURATED -> "feed_curated"
                FeedType.NEW -> "feed_new"
                FeedType.CATEGORY -> "feed_category_${customCategory.name}"
                FeedType.SOURCE -> "feed_source_${customSource?.id}"
            }
        },
        creationCallback = { factory: FeedViewModel.Factory ->
            factory.create(type, customCategory, customSource)
        }
    )
) {
    if (type == FeedType.SOURCE  && customSource == null) return

    LaunchedEffect(Unit) { viewModel.requestFeedUpdate() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    CleanScaffold(
        topBar = { FloatingExtendedTopBar(
            listState = listState,
            canGoBack = false,
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
                            }
                        ),
                        contentDescription = null,
                        modifier = clickableModifier
                    )
            },
            actions = listOf(
                FloatingExtendedTopBarActionItem(
                    name = stringResource(R.string.options),
                    icon = R.drawable.icon_more,
                    onClick = { /* TODO: Edit feed button */ }
                )
            )
        ) }
    ) {
        val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
        val feed by viewModel.feed.collectAsStateWithLifecycle()

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item { Spacer(Modifier.height(floatingExtendedTopBarPadding + 16.dp)) }

            if (isLoading)
                item {
                    Text(
                        text = stringResource(R.string.thinking),
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .padding(top = 128.dp)
                            .fillMaxWidth(),
                    )
                }
            else
                if(feed.isEmpty())
                    item {
                        Text(
                            text = stringResource(R.string.feed_empty),
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 48.dp)
                                .padding(top = 128.dp)
                                .fillMaxWidth(),
                        )
                    }
                else
                    items(feed, key = { it.id }) { post ->
                        Post(
                            post,
                            Modifier
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    border = BorderStroke(width = 1.dp, color = Color.Gray),
                                    shape = RoundedCornerShape(6.dp)
                                )
                        )
                    }


            item { Spacer(Modifier.height(floatingNavigationBarPadding + 32.dp)) }
        }
    }
}

@Composable
fun Post(
    post: PostEntity,
    modifier: Modifier = Modifier
) {
    if (post.title.isEmpty()) return

    val context = LocalContext.current
    var metadata by remember(post.link) { mutableStateOf<LinkMetadata?>(null) }
    LaunchedEffect(post.link) {
        metadata = fetchLinkMetadata(post.link, context)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        metadata?.let {
            AsyncImage(
                model = it.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.5f)
            )
        }
        Box(Modifier
            .fillMaxSize()
            .background(Brush.horizontalGradient(
                colors = listOf(Color.Black, Color.Transparent)
            ) )
        )

        Text(
            post.title,
            modifier = Modifier
                .padding(24.dp)
                .align(Alignment.CenterStart)
        )
    }
}