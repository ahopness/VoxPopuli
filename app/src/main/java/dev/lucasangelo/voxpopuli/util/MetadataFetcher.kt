package dev.lucasangelo.voxpopuli.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.util.regex.Pattern

suspend fun fetchImageMetadata(urlString: String): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL(urlString)
        val connection = url.openConnection()

        connection.getInputStream().bufferedReader().use { reader ->
            val pattern = Pattern.compile("<meta[^>]*property=[\"']og:image[\"'][^>]*content=[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE)
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                if (line!!.contains("</head>", ignoreCase = true)) {
                    break
                }
                val matcher = pattern.matcher(line)
                if (matcher.find()) {
                    return@withContext matcher.group(1)
                }
            }
        }
        return@withContext null
    } catch (e: Exception) {
        return@withContext null
    }
}
