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
data class Profile(
    val ignoredCategories: List<SourceCategory> = emptyList(),
    val embedding: List<Float> = emptyList(),
)

object ProfileSerializer : Serializer<Profile> {
    override val defaultValue: Profile = Profile()

    override suspend fun readFrom(input: InputStream): Profile =
        try {
            Json.decodeFromString<Profile>(
                input.readBytes().decodeToString()
            )
        } catch (serialization: SerializationException) {
            throw CorruptionException("Unable to read Profile", serialization)
        }

    override suspend fun writeTo(t: Profile, output: OutputStream) {
        withContext(Dispatchers.IO) {
            output.write(
                Json.encodeToString(t).encodeToByteArray()
            )
        }
    }
}