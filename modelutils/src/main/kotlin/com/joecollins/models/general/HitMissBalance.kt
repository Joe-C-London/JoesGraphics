package com.joecollins.models.general

object HitMissBalance {

    fun <T> calculateBalance(list: List<T>, func: (T) -> Boolean?): IntRange {
        val indexedEntries = list.mapIndexed { index, item -> index to item }
        val hits = indexedEntries.asSequence()
            .filter { func(it.second) == true }
            .map { it.first + 1 }
            .let { sequenceOf(it.sortedDescending(), generateSequence { 0 }).flatten() }
        val misses = indexedEntries.asSequence()
            .filter { func(it.second) == false }
            .map { it.first + 1 }
            .let { sequenceOf(it.sorted(), generateSequence { list.size + 1 }).flatten() }
        return hits.zip(misses).first { (hit, miss) -> hit < miss }.let { it.first until it.second }
    }
}
