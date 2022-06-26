package com.kairlec.annoy4s

import com.kairlec.annoy4s.Metric.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.random.Random


private val euclideanInputLines = listOf(
    AnnoyDataHolder(10, floatArrayOf(1.0f, 1.0f)),
    AnnoyDataHolder(11, floatArrayOf(2.0f, 1.0f)),
    AnnoyDataHolder(12, floatArrayOf(2.0f, 2.0f)),
    AnnoyDataHolder(13, floatArrayOf(3.0f, 2.0f)),
)

private val angularInputLines = listOf(
    AnnoyDataHolder(10, floatArrayOf(2.0f, 0.0f)),
    AnnoyDataHolder(11, floatArrayOf(1.0f, 1.0f)),
    AnnoyDataHolder(12, floatArrayOf(0.0f, 3.0f)),
    AnnoyDataHolder(13, floatArrayOf(-5.0f, 0.0f)),
)

private val stringAngularInputLines = listOf(
    AnnoyDataHolder("a", floatArrayOf(2.0f, 0.0f)),
    AnnoyDataHolder("b", floatArrayOf(1.0f, 1.0f)),
    AnnoyDataHolder("c", floatArrayOf(0.0f, 3.0f)),
    AnnoyDataHolder("d", floatArrayOf(-5.0f, 0.0f)),
)

private val manhattanInputLines = listOf(
    AnnoyDataHolder(10, floatArrayOf(2.0f, 0.0f)),
    AnnoyDataHolder(11, floatArrayOf(1.0f, 1.0f)),
    AnnoyDataHolder(12, floatArrayOf(0.0f, 3.0f)),
    AnnoyDataHolder(13, floatArrayOf(-5.0f, 0.0f)),
)

private val hammingInputLines = listOf(
    AnnoyDataHolder('a', floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f)),
    AnnoyDataHolder('b', floatArrayOf(1.0f, 1.0f, 0.0f, 0.0f)),
    AnnoyDataHolder('c', floatArrayOf(1.0f, 1.0f, 0.0f, 1.0f)),
    AnnoyDataHolder('d', floatArrayOf(0.0f, 1.0f, 1.0f, 1.0f)),
)

typealias InputVectors<T> = List<AnnoyDataHolder<T>>

fun <T> Annoy<T>.testSaveLoad(converter: AnnoyDataConverter<T>): Annoy<T> {
    val dir = Files.createTempDirectory("annoy-")
    try {
        save(dir, converter)
        return Annoy.load(dir, converter)
    } finally {
        dir.toFile().deleteRecursively()
    }
}

fun <T> checkAnnoy(annoy: Annoy<T>, inputVectors: InputVectors<T>, metric: Metric) {
    annoy.ids shouldContainExactly inputVectors.map { it.data }
    annoy.dimension shouldBe inputVectors.first().w.size
    annoy.metric shouldBe metric
}

fun checkEuclideanResult(res: Collection<Pair<Int, Float>>) {
    res.map { it.first } shouldContainExactly euclideanInputLines.map { it.data }
    res.zip(listOf(0.0f, 1.0f, 1.414f, 2.236f)) { a, b ->
        a.second shouldBe b.plusOrMinus(0.001f)
    }
}

fun checkAngularResult(res: Collection<Pair<Int, Float>>) {
    res.map { it.first } shouldContainExactly angularInputLines.map { it.data }
    res.zip(listOf(0.0f, 0.765f, 1.414f, 2.0f)) { a, b ->
        a.second shouldBe b.plusOrMinus(0.001f)
    }
}

fun checkStringAngularResult(res: Collection<Pair<String, Float>>) {
    res.map { it.first } shouldContainExactly stringAngularInputLines.map { it.data }
    res.zip(listOf(0.0f, 0.765f, 1.414f, 2.0f)) { a, b ->
        a.second shouldBe b.plusOrMinus(0.001f)
    }
}

fun checkManhattanResult(res: Collection<Pair<Int, Float>>) {
    res.map { it.first } shouldContainExactly manhattanInputLines.map { it.data }
    res.zip(listOf(0.0f, 2.0f, 5.0f, 7.0f)) { a, b ->
        a.second shouldBe b.plusOrMinus(0.001f)
    }
}

fun checkHammingResult(res: Collection<Pair<Char, Float>>) {
    res.map { it.first } shouldContainExactly hammingInputLines.map { it.data }
    res.zip(listOf(0.0f, 1.0f, 2.0f, 4.0f)) { a, b ->
        a.second shouldBe b.plusOrMinus(0.001f)
    }
}

class AnnoySpec : StringSpec({
    "create/load and query Euclidean file index" {
        val annoy = Annoy.create(euclideanInputLines, 10, Euclidean)
        val query = annoy.query(10, 4).shouldNotBeNull()
        checkEuclideanResult(query)
        checkAnnoy(annoy, euclideanInputLines, Euclidean)
        val newAnnoy = annoy.testSaveLoad(IntConverter)
        val query2 = newAnnoy.query(10, 4).shouldNotBeNull()
        checkEuclideanResult(query2)
        checkAnnoy(newAnnoy, euclideanInputLines, Euclidean)
        annoy.close()
        newAnnoy.close()
    }
    "create and query Euclidean memory index" {
        val annoy = Annoy.create(euclideanInputLines, 10, metric = Euclidean)
        checkEuclideanResult(annoy.query(10, 4).shouldNotBeNull())
        annoy.close()
    }
    "create/load and query Angular file index" {
        val annoy = Annoy.create(angularInputLines, 10, Angular)
        val query = annoy.query(10, 4).shouldNotBeNull()
        checkAngularResult(query)
        checkAnnoy(annoy, angularInputLines, Angular)
        val newAnnoy = annoy.testSaveLoad(IntConverter)
        val query2 = newAnnoy.query(10, 4).shouldNotBeNull()
        checkAngularResult(query2)
        checkAnnoy(newAnnoy, angularInputLines, Angular)
        annoy.close()
        newAnnoy.close()
    }
    "create and query Angular memory index" {
        val annoy = Annoy.create(angularInputLines, 10, metric = Angular)
        checkAngularResult(annoy.query(10, 4).shouldNotBeNull())
        annoy.close()
    }
    "create/load and query Angular file index with a String as a key" {
        val annoy = Annoy.create(stringAngularInputLines, 10, Angular)
        val query = annoy.query("a", 4).shouldNotBeNull()
        checkStringAngularResult(query)
        checkAnnoy(annoy, stringAngularInputLines, Angular)
        val newAnnoy = annoy.testSaveLoad(StringConverter)
        val query2 = newAnnoy.query("a", 4).shouldNotBeNull()
        checkStringAngularResult(query2)
        checkAnnoy(newAnnoy, stringAngularInputLines, Angular)
        annoy.close()
        newAnnoy.close()
    }
    "return more accurate results for a higher search_K" {
        val data = AnnoySpec::class.java.getResourceAsStream("/searchk-test-vector").shouldNotBeNull().bufferedReader()
            .readLines()
        val annoyData = data.map {
            it.split(" ").let {
                AnnoyDataHolder(it[0].toInt(), it.drop(1).map { it.toFloat() }.toFloatArray())
            }
        }
        val annoy = Annoy.create(annoyData, 2)
        val queryAns1 = arrayOf(1, 54, 55, 60, 76, 8, 32, 33)
        annoy.query(1, 10, 2).shouldNotBeNull().map { it.first }.forEachIndexed { index, it ->
            queryAns1[index] shouldBe it
        }
        val queryAns2 = arrayOf(1, 69, 39, 87, 54, 29, 62, 55, 21, 35)
        annoy.query(1, 10, -1).shouldNotBeNull().map { it.first }.forEachIndexed { index, it ->
            queryAns2[index] shouldBe it
        }
        annoy.close()
    }
    "create/load and query Manhattan file index" {
        val annoy = Annoy.create(manhattanInputLines, 10, Manhattan)
        val query = annoy.query(10, 4).shouldNotBeNull()
        checkManhattanResult(query)
        checkAnnoy(annoy, manhattanInputLines, Manhattan)
        val newAnnoy = annoy.testSaveLoad(IntConverter)
        val query2 = newAnnoy.query(10, 4).shouldNotBeNull()
        checkManhattanResult(query2)
        checkAnnoy(newAnnoy, manhattanInputLines, Manhattan)
        annoy.close()
        newAnnoy.close()
    }
    "return the vector for a given, previously loaded, id" {
        val data = (0..9).map {
            AnnoyDataHolder(it, (0..29).map { Random.nextFloat() }.toFloatArray())
        }
        val map = data.associate { it.data to it.w }
        val annoy = Annoy.create(data, 10)
        map.forEach { (id, vector) ->
            annoy.getItem(id).shouldNotBeNull().forEachIndexed { index, fl ->
                fl shouldBe vector[index].plusOrMinus(0.001f)
            }
        }
        annoy.getItem(-1) shouldBe null
        annoy.close()
    }
    "create/load and query Hamming file index" {
        val annoy = Annoy.create(hammingInputLines, 10, Hamming)
        val query = annoy.query('a', 4).shouldNotBeNull()
        checkHammingResult(query)
        checkAnnoy(annoy, hammingInputLines, Hamming)
        val newAnnoy = annoy.testSaveLoad(CharConverter)
        val query2 = newAnnoy.query('a', 4).shouldNotBeNull()
        checkHammingResult(query2)
        checkAnnoy(newAnnoy, hammingInputLines, Hamming)
        annoy.close()
        newAnnoy.close()
    }
})