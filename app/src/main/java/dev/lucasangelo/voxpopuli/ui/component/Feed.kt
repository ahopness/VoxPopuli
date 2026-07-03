package dev.lucasangelo.voxpopuli.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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
    onPostInteracted: (PostEntity) -> Unit,
    onPostBookmarked: (PostEntity) -> Unit,
) {
    CleanScaffold(
        topBar = { FloatingExtendedTopBar(
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
        ) }
    ) {
        PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = { onRequestFeedUpdate(false) }
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item { Spacer(Modifier.height(floatingExtendedTopBarPadding + 16.dp)) }

                if (isLoading)
                    item {
                        Text(
                            text = buildAnnotatedString {
                                appendLine(stringResource(R.string.loading))
                                append(loadingProgress.toString()); append(" ")
                                append(stringResource(R.string.fetched))

                            },
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(horizontal = 48.dp)
                                .padding(top = 128.dp)
                                .fillMaxWidth(),
                        )
                    }
                else if (errorMessage != null)
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
                else if (feed.isEmpty())
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


                item { Spacer(Modifier.height(floatingNavigationBarPadding + 32.dp)) }
            }
        }
    }
}
