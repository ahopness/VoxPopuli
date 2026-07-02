package dev.lucasangelo.voxpopuli.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.regex.Pattern

suspend fun fetchImageMetadata(client: OkHttpClient, urlString: String): String? = withContext(Dispatchers.IO) {
    try {
        val request = Request.Builder()
            .url(urlString)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null

            val body = response.body
            body.charStream().buffered().use { reader ->
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
        }
        return@withContext null
    } catch (e: Exception) {
        return@withContext null
    }
}