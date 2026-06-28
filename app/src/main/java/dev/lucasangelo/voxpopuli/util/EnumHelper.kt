package dev.lucasangelo.voxpopuli.util

import dev.lucasangelo.voxpopuli.R
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import dev.lucasangelo.voxpopuli.ui.screen.home.FeedType

val feedTypeMetas: Map<FeedType, Pair<Int, Int>> = mapOf(
    FeedType.BOOKMARKS to Pair(R.drawable.icon_bookmark, R.string.feed_bookmarks),
    FeedType.CURATED to Pair(R.drawable.icon_home, R.string.feed_curated),
    FeedType.NEW to Pair(R.drawable.icon_star, R.string.feed_new),
)

val sourceCategoryMetas: Map<SourceCategory, Pair<Int, Int>> = mapOf(
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