package dev.lucasangelo.voxpopuli.data

import android.content.Context
import android.util.Log
import androidx.core.text.isDigitsOnly
import androidx.datastore.core.DataStore
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.lucasangelo.voxpopuli.data.datastore.Profile
import dev.lucasangelo.voxpopuli.data.datastore.Settings
import dev.lucasangelo.voxpopuli.data.room.AppDao
import dev.lucasangelo.voxpopuli.data.room.PostEntity
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.data.room.SourceEntity
import dev.lucasangelo.voxpopuli.util.fetchImageMetadata
import dev.lucasangelo.voxpopuli.util.parseRss
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val settingsDataStore: DataStore<Settings>,
    private val profileDataStore: DataStore<Profile>,
    private val dao: AppDao,
    private val textEmbedder: TextEmbedder,
    private val okHttpClient: OkHttpClient,
) {
    val settings: Flow<Settings> =
        settingsDataStore.data
    suspend fun updateSettings(settings: Settings) =
        settingsDataStore.updateData { settings }

    val profile: Flow<Profile> =
        profileDataStore.data
    suspend fun updateProfile(profile: Profile) =
        profileDataStore.updateData { profile }

    suspend fun insertSource(source: SourceEntity) =
        dao.insertSource(source)
    suspend fun getSource(sourceId: Long): SourceEntity =
        dao.getSource(sourceId)
    suspend fun getLastFetchedFromSource(sourceId: Long): Instant =
        dao.getLastFetchedFromSource(sourceId)
    fun getAllSources(): Flow<List<SourceEntity>> =
        dao.getAllSources()
    suspend fun getAllSourcesNow(): List<SourceEntity> =
        dao.getAllSourcesNow()
    suspend fun deleteSource(source: SourceEntity) =
        dao.deleteSource(source)
    suspend fun updateSource(source: SourceEntity) =
        dao.updateSource(source)

    suspend fun insertPost(post: PostEntity) {
        if (post.link.isEmpty())
            return

        val id = post.title.hashCode()

        var publishedInstant: Instant
        try {
            publishedInstant = OffsetDateTime.parse(
                post.pubDate,
                DateTimeFormatter.RFC_1123_DATE_TIME
            ).toInstant()
        } catch (e: Exception) {
            return
        }

        val embedding = textEmbedder.embed(post.title)
            .embeddingResult()
            .embeddings()
            .first()
            .floatEmbedding()
            .toList()

        dao.insertPost(
            post.copy(
                id = id,
                publishedAt = publishedInstant,
                embedding = embedding
            )
        )
    }
    fun getAllPosts(): Flow<List<PostEntity>> =
        dao.getAllPosts()
    fun getAllNewPosts(): Flow<List<PostEntity>> =
        dao.getAllNewPosts()
    fun getAllPostsIn(category: SourceCategory): Flow<List<PostEntity>> =
        dao.getAllPostsIn(category)
    fun getAllPostsBy(sourceId: Long): Flow<List<PostEntity>> =
        dao.getAllPostsBy(sourceId)
    fun getAllBookmarkedPosts(): Flow<List<PostEntity>> =
        dao.getAllBookmarkedPosts()
    suspend fun deletePost(post: PostEntity) =
        dao.deletePost(post)
    suspend fun updatePost(post: PostEntity) =
        dao.updatePost(post)

    suspend fun fetchSource(source: SourceEntity) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(source.feedUrl)
            .build()

        val response = okHttpClient
            .newCall(request)
            .execute()

        if (!response.isSuccessful) {
            Log.i("AppRepository::fetchSource@OkHttp3", response.code.toString())
            return@withContext
        }

        val xml = response.body.string()
        val rss = parseRss(xml)

        coroutineScope {
            val deferred = rss.channel.item.map { async {
                val imageUrl = fetchImageMetadata(it.link) ?: ""
                insertPost(PostEntity(
                    sourceId = source.id,
                    imageUrl = imageUrl,
                    author = it.author,
                    title = it.title,
                    description = it.description,
                    pubDate = it.pubDate,
                    link = it.link,
                    comments = if (it.comments.isDigitsOnly()) "" else it.comments, // NOTE: happens sometimes, no idea why
                ))
                Log.i("AppRepository::fetchSource", "FOUND: ${it.title}")
            } }
            deferred.awaitAll()
        }

        updateSource(source.copy(
            lastFetched = Instant.now()
        ))
    }
}