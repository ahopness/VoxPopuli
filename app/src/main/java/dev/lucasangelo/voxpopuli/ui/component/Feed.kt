package dev.lucasangelo.voxpopuli.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.valentinilk.shimmer.shimmer
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.room.PostEntity
import dev.lucasangelo.voxpopuli.data.room.SourceEntity

@Composable
fun Feed(
    topBarTitle: String,
    topBarIconContent: @Composable (Modifier, () -> Float) -> Unit,
    isLoading: Boolean,
    loadingProgress: Int,
    errorMessage: String?,
    feed: List<PostEntity>,
    listState: LazyListState,
    sources: Map<Long, SourceEntity>,
    onRequestFeedUpdate: (Boolean) -> Unit,
    onPostInteracted: (PostEntity) -> Unit = {},
    onPostBookmarked: (PostEntity) -> Unit,
    description: String? = null,
) {
    val refreshState = rememberPullToRefreshState()

    CleanScaffold(
        topBar = {
            FloatingExtendedTopBar(
            listState = listState,
            canGoBack = false,
            title = topBarTitle,
            iconContent = topBarIconContent,
            actions = emptyList() /*listOf(
                FloatingExtendedTopBarActionItem(
                    name = stringResource(R.string.options),
                    icon = R.drawable.icon_more,
                    onClick = { /* TODO: Edit feed button */ }
                )
            )*/
            )
            Indicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding(),
                isRefreshing = isLoading,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onSurface,
                state = refreshState
            )
        }
    ) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { onRequestFeedUpdate(false) },
            state = refreshState,
            indicator = {}
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item { Spacer(Modifier.height(floatingExtendedTopBarPadding + 16.dp)) }

                if (isLoading) {
                    items(loadingProgress) {
                        Box(
                            Modifier
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .shimmer()
                                .fillMaxWidth()
                                .height(180.dp)
                                .background(Color.Gray.copy(alpha = 0.5f))
                        )
                    }
                } else if (errorMessage != null) {
                    item {
                        Text(
                            text = buildAnnotatedString {
                                appendLine(stringResource(R.string.feed_error))
                                append(errorMessage)
                            },
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 48.dp)
                                .padding(top = 128.dp)
                                .fillMaxWidth(),
                        )
                    }
                } else if (feed.isEmpty()) {
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
                } else {
                    if (description != null)
                        item {
                            Text(
                                text = description,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .padding(horizontal = 48.dp)
                                    .padding(bottom = 64.dp)
                                    .fillMaxWidth()
                            )
                        }

                    items(feed, key = { it.id }) { post ->
                        val source = sources[post.sourceId] ?: return@items //NOTE: causes NullPointerException sometimes, idk why, prob race condition
                        Post(
                            post,
                            source,
                            Modifier
                                .padding(horizontal = 12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    border = BorderStroke(width = 1.dp, color = Color.Gray),
                                    shape = RoundedCornerShape(6.dp)
                                ),
                            onBookmarked = {
                                onPostBookmarked(post)
                            },
                            onInteractedWith = {
                                onPostInteracted(post)
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(floatingNavigationBarPadding + 32.dp)) }
            }
        }
    }
}
