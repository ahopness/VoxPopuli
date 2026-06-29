package dev.lucasangelo.voxpopuli.data.okhttp

data class Rss(
    val version: String,
    val channel: Channel,
) {
    data class Channel(
        val title: String,
        val description: String,
        val link: String,

        val item: List<Item>
    ) {
        data class Item(
            val author: String,
            val title: String,
            val description: String,
            val link: String,
            val comments: String,
            val pubDate: String,
        )
    }
}



