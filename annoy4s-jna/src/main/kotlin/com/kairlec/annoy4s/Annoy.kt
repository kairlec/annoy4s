package com.kairlec.annoy4s

import com.kairlec.annoy4s.Metric.*
import com.sun.jna.Native
import com.sun.jna.Pointer
import java.io.Closeable
import java.nio.file.Path
import kotlin.io.path.*

enum class Metric {
    Angular,
    Euclidean,
    Manhattan,
    Hamming
}

interface AnnoyDataConverter<T> {
    fun save(data: T): String
    fun load(data: String): T
}

data class AnnoyDataHolder<T>(
    val data: T,
    val w: FloatArray
)

class Annoy<T> private constructor(
    private val idToIndex: Map<T, Int>,
    private val indexToId: List<T>,
    private val annoyIndex: Pointer,
    val dimension: Int,
    val metric: Metric
) : AutoCloseable, Closeable {
    val ids = indexToId

    override fun close() {
        annoyLib.deleteIndex(annoyIndex)
    }

    fun query(vector: FloatArray, maxReturnSize: Int, searchK: Int = -1): Collection<Pair<T, Float>> {
        val result = IntArray(maxReturnSize) { -1 }
        val distances = FloatArray(maxReturnSize) { -1.0f }
        annoyLib.getNnsByVector(annoyIndex, vector, maxReturnSize, searchK, result, distances)
        return result.filter { it != -1 }.map { indexToId[it] }.zip(distances.toList())
    }

    fun query(id: T, maxReturnSize: Int, searchK: Int = -1): Collection<Pair<T, Float>>? {
        return idToIndex[id]?.let { index ->
            val result = IntArray(maxReturnSize) { -1 }
            val distances = FloatArray(maxReturnSize) { -1.0f }
            annoyLib.getNnsByItem(annoyIndex, index, maxReturnSize, searchK, result, distances)
            result.filter { it != -1 }.map { indexToId[it] }.zip(distances.toList())
        }
    }

    fun getItem(id: T): FloatArray? {
        return idToIndex[id]?.let { index ->
            val result = FloatArray(dimension) { Float.NEGATIVE_INFINITY }
            annoyLib.getItem(annoyIndex, index, result)
            if (result.any { it != Float.NEGATIVE_INFINITY }) {
                result
            } else {
                null
            }
        }
    }

    fun save(outputDir: Path, converter: AnnoyDataConverter<T>) {
        outputDir.resolve("ids").writeLines(ids.map { converter.save(it) })
        outputDir.resolve("dimension").writeText(dimension.toString())
        outputDir.resolve("metric").writeText(metric.name)
        annoyLib.save(annoyIndex, outputDir.resolve("index").absolutePathString())
    }

    companion object {
        internal val annoyLib = Native.load("annoy", AnnoyLibrary::class.java)

        fun <T> create(
            input: Collection<AnnoyDataHolder<T>>,
            numOfTrees: Int,
            metric: Metric = Angular,
            verbose: Boolean = false,
            outputDir: Path? = null,
            converter: AnnoyDataConverter<T>? = null,
        ): Annoy<T> {
            require(input.isNotEmpty()) { "input is empty" }
            if (outputDir != null) {
                if (outputDir.exists()) {
                    require(outputDir.isDirectory()) { "output is not a directory" }
                } else {
                    outputDir.createDirectories()
                }
            }
            val dimension = input.first().w.size
            val annoyIndex = when (metric) {
                Angular -> annoyLib.createAngular(dimension)
                Euclidean -> annoyLib.createEuclidean(dimension)
                Manhattan -> annoyLib.createManhattan(dimension)
                Hamming -> annoyLib.createHamming(dimension)
            }
            annoyLib.verbose(annoyIndex, verbose)
            input.forEachIndexed { index, annoyData ->
                annoyLib.addItem(annoyIndex, index, annoyData.w)
            }
            annoyLib.build(annoyIndex, numOfTrees)

            val data = input.map { it.data }
            val idToIndex = data.withIndex().associate { it.value to it.index }
            val annoy = Annoy(idToIndex, data, annoyIndex, dimension, metric)
            if (outputDir != null) {
                requireNotNull(converter) { "converter is null" }
                annoy.save(outputDir, converter)
            }
            return annoy
        }

        fun <T> load(
            inputDir: Path,
            converter: AnnoyDataConverter<T>
        ): Annoy<T> {
            val ids = inputDir.resolve("ids").readLines()
            val dimension = inputDir.resolve("dimension").readText().toInt()
            val metric = Metric.valueOf(inputDir.resolve("metric").readText())
            val annoyIndex = when (metric) {
                Angular -> annoyLib.createAngular(dimension)
                Euclidean -> annoyLib.createEuclidean(dimension)
                Manhattan -> annoyLib.createManhattan(dimension)
                Hamming -> annoyLib.createHamming(dimension)
            }
            annoyLib.load(annoyIndex, inputDir.resolve("index").absolutePathString())
            val data = ids.map { converter.load(it) }
            val idToIndex = data.withIndex().associate { it.value to it.index }
            return Annoy(idToIndex, data, annoyIndex, dimension, metric)
        }
    }
}