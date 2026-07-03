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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.cancellation.CancellationException
import kotlin.system.measureTimeMillis

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

    private val embedderMutex = Mutex()
    suspend fun fetchSource(source: SourceEntity) = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(source.feedUrl)
            .build()

        try {
            var downloadTime = 0L
            var parseTime = 0L
            var processingTime = 0L

            okHttpClient
                .newCall(request)
                .execute()
                .use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val downloadStart = System.currentTimeMillis()
                    val xml = response.body.string()
                    downloadTime = System.currentTimeMillis() - downloadStart

                    val parseStart = System.currentTimeMillis()
                    val rss = parseRss(xml)
                    parseTime = System.currentTimeMillis() - parseStart

                    processingTime = measureTimeMillis {
                        val postsToInsert = coroutineScope {
                            rss.channel.item.map { item -> async {
                                val imageUrl = fetchImageMetadata(okHttpClient, item.link) ?: ""

                                val id = item.title.hashCode()

                                val publishedInstant =
                                    try {
                                        OffsetDateTime.parse(item.pubDate, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
                                    } catch (e: Exception) {
                                        // NOTE: npr and ny times dont follow the standard, this is a (stupid) fallback for them
                                        Instant.now()
                                    }

                                val embedding = embedderMutex.withLock {
                                    textEmbedder.embed(item.title)
                                        .embeddingResult()
                                        .embeddings()
                                        .first()
                                        .floatEmbedding()
                                        .toList()
                                }

                                if (item.link.isNotEmpty()) {
                                    PostEntity(
                                        id,
                                        publishedAt = publishedInstant,
                                        sourceId = source.id,
                                        imageUrl,
                                        item.author,
                                        item.title,
                                        item.description,
                                        item.pubDate,
                                        item.link,
                                        comments = if (item.comments.isDigitsOnly()) "" else item.comments,
                                        embedding
                                    )
                                } else {
                                    return@async null
                                }
                            } }.awaitAll().filterNotNull()
                        }
                        dao.insertPosts(postsToInsert)
                    }

                    updateSource(
                        source.copy(
                            lastFetched = Instant.now()
                        )
                    )
                }
            Log.i("VoxPopuli::AppRepository::fetchSource", "SUCCESS: " +
                    "Source fetched at @ ${source.feedUrl}\n" +
                    "\tTotal: ${downloadTime + parseTime + processingTime}ms\n" +
                    "\t\tDownload RSS: ${downloadTime}ms\n" +
                    "\t\tParse XML: ${parseTime}ms\n" +
                    "\t\tPost Processing: ${processingTime}ms"
            )
        } catch (e: CancellationException) {
            Log.e("VoxPopuli::AppRepository::fetchSource", "SAFE FAIL: Coroutine canceled @ ${source.feedUrl}", e)
            throw e
        } catch (e: Exception) {
            Log.e("VoxPopuli::AppRepository::fetchSource", "FAILED: Something went wrong @ ${source.feedUrl}", e)
        }
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
}