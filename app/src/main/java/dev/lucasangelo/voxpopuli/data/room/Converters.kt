package dev.lucasangelo.voxpopuli.data.room

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.time.Instant

class Converters {
    @TypeConverter
    fun instantToLong(value: Instant): Long =
        value.toEpochMilli()
    @TypeConverter
    fun longToInstant(value: Long): Instant =
        Instant.ofEpochMilli(value)

    @TypeConverter
    fun floatListToByteArray(floats: List<Float>): ByteArray {
        val byteBuffer = ByteBuffer.allocate(floats.size * 4)
        byteBuffer.asFloatBuffer().put(floats.toFloatArray())
        return byteBuffer.array()
    }
    @TypeConverter
    fun byteListToFloatArray(bytes: ByteArray): List<Float> {
        val byteBuffer = ByteBuffer.wrap(bytes)
        val floatBuffer = byteBuffer.asFloatBuffer()
        val floats = FloatArray(floatBuffer.limit())
        floatBuffer.get(floats)
        return floats.toList()
    }
}