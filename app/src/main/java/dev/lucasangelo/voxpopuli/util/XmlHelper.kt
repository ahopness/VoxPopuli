package dev.lucasangelo.voxpopuli.util

import dev.lucasangelo.voxpopuli.data.okhttp.Rss
import java.io.StringReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.XMLStreamConstants

fun parseRss(xml: String): Rss {
    val factory = XMLInputFactory.newInstance()
    val reader = factory.createXMLStreamReader(StringReader(xml))

    val path = mutableListOf<String>()

    var rssVersion = ""
    var channelTitle = ""
    var channelDescription = ""
    var channelLink = ""
    val items = mutableListOf<Rss.Channel.Item>()

    var itemAuthor = ""
    var itemTitle = ""
    var itemDescription = ""
    var itemLink = ""
    var itemComments = ""
    var itemPubDate = ""

    val currentText = StringBuilder()

    try {
        while (reader.hasNext()) {
            when (reader.next()) {
                XMLStreamConstants.START_ELEMENT -> {
                    val name = reader.localName
                    path.add(name)
                    currentText.setLength(0)
                    if (name == "rss") {
                        rssVersion = reader.getAttributeValue(null, "version") ?: ""
                    } else if (path.size == 3 && path[0] == "rss" && path[1] == "channel" && path[2] == "item") {
                        itemAuthor = ""
                        itemTitle = ""
                        itemDescription = ""
                        itemLink = ""
                        itemComments = ""
                        itemPubDate = ""
                    }
                }
                XMLStreamConstants.CHARACTERS, XMLStreamConstants.CDATA -> {
                    currentText.append(reader.text)
                }
                XMLStreamConstants.END_ELEMENT -> {
                    val name = reader.localName
                    val textValue = currentText.toString().trim()

                    if (path.size == 3 && path[0] == "rss" && path[1] == "channel" && (name == "title" || name == "description" || name == "link")) {
                        when (name) {
                            "title" -> channelTitle = textValue
                            "description" -> channelDescription = textValue
                            "link" -> channelLink = textValue
                        }
                    } else if (path.size == 4 && path[0] == "rss" && path[1] == "channel" && path[2] == "item") {
                        when (name) {
                            "author" -> itemAuthor = textValue
                            "title" -> itemTitle = textValue
                            "description" -> itemDescription = textValue
                            "link" -> itemLink = textValue
                            "comments" -> itemComments = textValue
                            "pubDate" -> itemPubDate = textValue
                        }
                    } else if (path.size == 3 && path[0] == "rss" && path[1] == "channel" && name == "item") {
                        items.add(
                            Rss.Channel.Item(
                                author = itemAuthor,
                                title = itemTitle,
                                description = itemDescription,
                                link = itemLink,
                                comments = itemComments,
                                pubDate = itemPubDate
                            )
                        )
                    }

                    path.removeLastOrNull()
                    currentText.setLength(0)
                }
            }
        }
    } finally {
        reader.close()
    }

    return Rss(
        version = rssVersion,
        channel = Rss.Channel(
            title = channelTitle,
            description = channelDescription,
            link = channelLink,
            item = items
        )
    )
}

