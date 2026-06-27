package dev.lucasangelo.voxpopuli.data

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AppRepository @Inject constructor(
    private val dao: AppDao,
    @ApplicationContext context: Context
) {
    suspend fun insertSource(source: SourceEntity) = dao.insertSource(source)
    fun getAllSources() = dao.getAllSources()
    suspend fun deleteSource(source: SourceEntity) = dao.deleteSource(source)
    suspend fun updateSource(source: SourceEntity) = dao.updateSource(source)

    suspend fun insertPost(post: PostEntity) {
        dao.insertPost(
            post.copy(id = post.title.hashCode())
        )
    }
    fun getAllPosts() = dao.getAllPosts()
    fun getAllPostsIn(category: SourceCategory) = dao.getAllPostsIn(category)
    fun getAllBookmarkedPosts() = dao.getAllBookmarkedPosts()
    suspend fun deletePost(post: PostEntity) = dao.deletePost(post)
    suspend fun updatePost(post: PostEntity) = dao.updatePost(post)

}