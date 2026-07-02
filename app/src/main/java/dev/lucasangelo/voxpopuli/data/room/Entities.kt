package dev.lucasangelo.voxpopuli.data.room

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import dev.lucasangelo.voxpopuli.R
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
val sourceCategoryInfo: Map<SourceCategory, Pair<Int, Int>> = mapOf(
    SourceCategory.GENERAL to Pair(R.drawable.link_general, R.string.category_general),
    SourceCategory.ENTERTAINMENT to Pair(R.drawable.link_entertainment, R.string.category_entertainment),
    SourceCategory.GAMING to Pair(R.drawable.link_gaming, R.string.category_gaming),
    SourceCategory.TECHNOLOGY to Pair(R.drawable.link_technology, R.string.category_technology),
    SourceCategory.PROGRAMMING to Pair(R.drawable.link_programming, R.string.category_programming),
    SourceCategory.BUSINESS to Pair(R.drawable.link_buisness, R.string.category_business),
    SourceCategory.SCIENCE to Pair(R.drawable.link_science, R.string.category_science),
    SourceCategory.SPORTS to Pair(R.drawable.link_sports, R.string.category_sports),
    SourceCategory.FASHION to Pair(R.drawable.link_fashion, R.string.category_fashion),
    SourceCategory.POLITICS to Pair(R.drawable.link_politics, R.string.category_politics),
)

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
        Index(value = ["sourceId", "publishedAt"]),
        Index("bookmarked"),
    ]
)
data class PostEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Int = 0,

    val publishedAt: Instant = Instant.EPOCH,

    val sourceId: Long,

    val imageUrl: String,
    val author: String,
    val title: String,
    val description: String,
    val pubDate: String,
    val link: String,
    val comments: String,

    val embedding: List<Float> = emptyList(),

    val bookmarked: Boolean = false,
)