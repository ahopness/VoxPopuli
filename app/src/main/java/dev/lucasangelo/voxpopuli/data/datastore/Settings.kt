package dev.lucasangelo.voxpopuli.data.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import dev.lucasangelo.voxpopuli.data.room.SourceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class Settings(
    val showOnboarding: Boolean = true,
    val tabSelection: TabSelection = TabSelection.CATEGORIES,
)
@Serializable
enum class TabSelection { CATEGORIES, SOURCES }

object SettingsSerializer : Serializer<Settings> {
    override val defaultValue: Settings = Settings()

    override suspend fun readFrom(input: InputStream): Settings =
        try {
            Json.decodeFromString<Settings>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Settings", serialization)
        }

    override suspend fun writeTo(t: Settings, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(t).encodeToByteArray()
            )
        }
    }
}