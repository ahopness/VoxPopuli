package dev.lucasangelo.voxpopuli.ui.component

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.room.PostEntity
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import dev.lucasangelo.voxpopuli.util.formatInstant

@Composable
fun Post(
    post: PostEntity,
    source: SourceEntity,
    modifier: Modifier = Modifier,
    onBookmarked: (PostEntity) -> Unit,
    onInteractedWith: (PostEntity) -> Unit,
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onInteractedWith(post)
                openUrl(context, post.link)
            })
    ) {
        Box(
            Modifier
                .matchParentSize()
                .alpha(0.5f)
        ) {
            MonochromeAsyncImage(
                model = source.logoUrl,
                contentDescription = source.name,
                contentScale = ContentScale.Crop,
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
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black, Color.Transparent)
                    )
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                MonochromeAsyncImage(
                    model = source.logoUrl,
                    contentDescription = source.name,
                    modifier = Modifier.height(16.dp)
                )

                Text(
                    text = formatInstant(post.publishedAt, "MMMM d")
                        .replaceFirstChar { it.titlecase() },
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = post.title,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

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
                            .clickable(onClick = { openUrl(context, post.comments) })
                    )

                Icon(
                    painter =
                        if (post.bookmarked)
                            painterResource(R.drawable.icon_bookmarked)
                        else
                            painterResource(R.drawable.icon_bookmark),
                    contentDescription = stringResource(R.string.bookmark_post),
                    modifier = Modifier
                        .offset(x = (16).dp)
                        .height(54.dp)
                        .clickable(onClick = { onBookmarked(post) })
                )
            }
        }
    }
}
fun openUrl(context: Context, url: String) {
    val intent = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setUrlBarHidingEnabled(true)
        .build()
    intent.launchUrl(context, url.toUri())
}