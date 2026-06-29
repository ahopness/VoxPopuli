package dev.lucasangelo.voxpopuli.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
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
import dev.lucasangelo.voxpopuli.util.feedTypeMetas
import dev.lucasangelo.voxpopuli.util.formatInstant
import dev.lucasangelo.voxpopuli.util.invertMatrix
import dev.lucasangelo.voxpopuli.util.sourceCategoryMetas
import dev.lucasangelo.voxpopuli.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

enum class FeedType { BOOKMARKS, CURATED, NEW, CATEGORY, SOURCE }
@Composable
fun FeedScreen(
    type: FeedType,
    sources: Map<Long, SourceEntity>,
    customCategory: SourceCategory? = null,
    customSource: SourceEntity? = null,
    viewModel: FeedViewModel = hiltViewModel(
        key = remember(type, customCategory, customSource) {
            when (type) {
                FeedType.BOOKMARKS -> "feed_bookmarks"
                FeedType.CURATED -> "feed_curated"
                FeedType.NEW -> "feed_new"
                FeedType.CATEGORY -> "feed_category_${customCategory?.name}"
                FeedType.SOURCE -> "feed_source_${customSource?.id}"
            }
        },
        creationCallback = { factory: FeedViewModel.Factory ->
            factory.create(type, customCategory, customSource)
        }
    )
) {
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
                        val source = sources[post.sourceId] //NOTE: causes NullPointerException sometimes, idk why, prob race condition

                        if (source != null)
                            Post(
                                post,
                                source,
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
    source: SourceEntity,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            Modifier
                .matchParentSize()
                .alpha(0.5f)
        ) {
            AsyncImage(
                model = source.logoUrl,
                contentDescription = source.name,
                contentScale = ContentScale.Crop,
                colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                    timesAssign(ColorMatrix().apply { setToSaturation(0f) })
                    timesAssign(invertMatrix)
                }),
                modifier = Modifier
                    .fillMaxSize()
                    .blur(25.dp)
                    .alpha(0.5f)
            )
            AsyncImage(
                model = post.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )
            Box(Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(
                    colors = listOf(Color.Black, Color.Transparent)
                ) )
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
//            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                AsyncImage(
                    model = source.logoUrl,
                    contentDescription = source.name,
                    colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply {
                        timesAssign(ColorMatrix().apply { setToSaturation(0f) })
                        timesAssign(invertMatrix)
                    }),
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = formatInstant(post.publishedAt, "MMMM d")
                        .replaceFirstChar { it.titlecase() },
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(post.title)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier =
                    if (post.comments.isNotEmpty())
                        Modifier.fillMaxWidth()
                    else
                        Modifier
            ) {
                if (post.comments.isNotEmpty())
                    Icon(
                        painter = painterResource(R.drawable.icon_comment),
                        contentDescription = stringResource(R.string.read_comments),
                        modifier = Modifier
                            .offset(x = (-16).dp)
                            .height(54.dp)
                    )

                Icon(
                    painter = painterResource(R.drawable.icon_bookmark),
                    contentDescription = stringResource(R.string.bookmark_post),
                    modifier = Modifier
                        .offset(x = (16).dp)
                        .height(54.dp)
                )
            }
        }
    }
}