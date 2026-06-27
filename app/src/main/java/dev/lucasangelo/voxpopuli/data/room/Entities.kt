package dev.lucasangelo.voxpopuli.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Entity
data class SourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val logoUrl: String,
    val category: SourceCategory,

    val feedUrl: String,

    val lastFetched: Instant = Instant.EPOCH,

    val muted: Boolean = false
)
@Serializable
enum class SourceCategory {
    GENERAL,
    ENTERTAINMENT,
    GAMING,
    TECHNOLOGY,
    PROGRAMMING,
    BUSINESS,
    SCIENCE,
    SPORTS,
    FASHION,
    POLITICS
}

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = SourceEntity::class,
            parentColumns = ["id"],
            childColumns = ["sourceId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sourceId", "createdAt"]),
        Index("bookmarked"),
    ]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,

    val publishedAt: Instant = Instant.EPOCH,

    val sourceId: Long,

    val author: String,
    val title: String,
    val description: String,
    val pubDate: String,
    val link: String,
    val comments: String,

    val embedding: List<Float> = emptyList(),

    val bookmarked: Boolean = false,
)

