package dev.lucasangelo.voxpopuli.util

import kotlin.math.sqrt

fun List<Float>.cosineSimilarity(other: List<Float>): Double {
    if (this.size != other.size || this.isEmpty()) return 0.0

    var dotProduct = 0.0
    var normA = 0.0
    var normB = 0.0

    for (i in this.indices) {
        val a = this[i]
        val b = other[i]
        dotProduct += a * b
        normA += a * a
        normB += b * b
    }

    return if (normA == 0.0 || normB == 0.0) 0.0 else dotProduct / (sqrt(normA) * sqrt(normB))
}