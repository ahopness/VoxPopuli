package dev.lucasangelo.voxpopuli.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Insert
    suspend fun insertSource(source: SourceEntity): Long
    @Query("SELECT * FROM SourceEntity")
    fun getAllSources(): Flow<List<SourceEntity>>
    @Delete
    suspend fun deleteSource(source: SourceEntity)
    @Update
    suspend fun updateSource(source: SourceEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPost(post: PostEntity): Int
    @Query( value =
        "SELECT * FROM PostEntity " +
        "INNER JOIN  SourceEntity on SourceEntity.id = PostEntity.sourceId " +
        "WHERE SourceEntity.muted = 0"
    )
    fun getAllPosts(): Flow<List<PostEntity>>
    @Query( value =
        "SELECT * FROM PostEntity " +
        "INNER JOIN  SourceEntity on SourceEntity.id = PostEntity.sourceId " +
        "WHERE SourceEntity.muted = 0 " +
        "AND SourceEntity.category = :category "
    )
    fun getAllPostsIn(category: SourceCategory): Flow<List<PostEntity>>
    @Query( value =
        "SELECT * FROM PostEntity " +
        "INNER JOIN  SourceEntity on SourceEntity.id = PostEntity.sourceId " +
        "WHERE SourceEntity.muted = 0 " +
        "AND PostEntity.bookmarked = 1 "
    )
    fun getAllBookmarkedPosts(): Flow<List<PostEntity>>
    @Delete
    suspend fun deletePost(post: PostEntity)
    @Update
    suspend fun updatePost(post: PostEntity)
}