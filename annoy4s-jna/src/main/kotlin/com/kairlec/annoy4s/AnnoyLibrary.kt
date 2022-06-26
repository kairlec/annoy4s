package com.kairlec.annoy4s

import com.sun.jna.Library
import com.sun.jna.Pointer

internal interface AnnoyLibrary : Library {
    fun createAngular(f: Int): Pointer
    fun createEuclidean(f: Int): Pointer
    fun createManhattan(f: Int): Pointer
    fun createHamming(f: Int): Pointer
    fun deleteIndex(ptr: Pointer)
    fun addItem(ptr: Pointer, item: Int, w: FloatArray)
    fun build(ptr: Pointer, q: Int)
    fun save(ptr: Pointer, filename: String): Boolean
    fun unload(ptr: Pointer)
    fun load(ptr: Pointer, filename: String): Boolean
    fun getDistance(ptr: Pointer, i: Int, j: Int): Float
    fun getNnsByItem(ptr: Pointer, item: Int, n: Int, searchK: Int, result: IntArray, distances: FloatArray)
    fun getNnsByVector(ptr: Pointer, w: FloatArray, n: Int, searchK: Int, result: IntArray, distances: FloatArray)
    fun getNItems(ptr: Pointer): Int
    fun verbose(ptr: Pointer, v: Boolean)
    fun getItem(ptr: Pointer, item: Int, v: FloatArray)
}