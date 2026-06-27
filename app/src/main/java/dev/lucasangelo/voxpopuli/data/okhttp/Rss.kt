package dev.lucasangelo.voxpopuli.data.okhttp

import kotlinx.serialization.Serializable

@Serializable
data class Rss(
    val version: String,
    val channel: RssChannel,
)

@Serializable
data class RssChannel(
    val title: String,
    val description: String,
    val link: String,

    val item: List<RssChannelItem>
)

@Serializable
data class RssChannelItem(
    val author: String,
    val title: String,
    val description: String,
    val link: String,
    val comments: String,
    val pubDate: String,
)