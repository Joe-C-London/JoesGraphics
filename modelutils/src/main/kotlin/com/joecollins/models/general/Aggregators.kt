package com.joecollins.models.general

import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapReduce
import com.joecollins.pubsub.merge
import java.util.concurrent.Flow
import kotlin.math.max

object Aggregators {

    fun <T, K> combine(items: Collection<T>, result: (T) -> Flow.Publisher<out Map<out K, Int>>) = combine(items, result, HashMap())

    fun <T, K> combine(items: Collection<T>, result: (T) -> Flow.Publisher<out Map<out K, Int>>, identity: Map<K, Int> = HashMap()): Flow.Publisher<Map<K, Int>> {
        if (items.isEmpty()) {
            return identity.asOneTimePublisher()
        }
        val seededKeys = identity.keys
        return items.map(result).mapReduce(
            identity,
            { a, r ->
                val ret = LinkedHashMap(a)
                r.forEach { (k, v) -> ret.merge(k, v) { a, b -> a + b } }
                ret
            },
            { a, r ->
                val ret = LinkedHashMap(a)
                r.forEach { (k, v) -> ret.merge(k, -v) { a, b -> a + b } }
                ret.filter { seededKeys.contains(it.key) || it.value != 0 || !r.containsKey(it.key) }
            },
        )
    }

    fun <T, K> combineDual(items: Collection<T>, result: (T) -> Flow.Publisher<Map<K, Pair<Int, Int>>>) = combineDual(items, result, HashMap())

    fun <T, K> combineDual(items: Collection<T>, result: (T) -> Flow.Publisher<Map<K, Pair<Int, Int>>>, identity: Map<K, Pair<Int, Int>> = HashMap()): Flow.Publisher<Map<K, Pair<Int, Int>>> {
        val seededKeys = identity.keys
        val sum: (Pair<Int, Int>, Pair<Int, Int>) -> Pair<Int, Int> =
            { a, b -> Pair(a.first + b.first, a.second + b.second) }
        return items.map(result).mapReduce(
            identity,
            { a, r ->
                val ret = LinkedHashMap(a)
                r.forEach { (k, v) -> ret.merge(k, v, sum) }
                ret
            },
            { a, r ->
                val ret = LinkedHashMap(a)
                r.forEach { (k, v) -> ret.merge(k, Pair(-v.first, -v.second), sum) }
                ret.filter { e -> seededKeys.contains(e.key) || e.value.first != 0 || e.value.second != 0 || !r.containsKey(e.key) }
            },
        )
    }

    fun <T> sum(items: Collection<T>, value: (T) -> Flow.Publisher<Int>): Flow.Publisher<Int> {
        return items.map(value).mapReduce(0, { t, v -> t + v }, { t, v -> t - v })
    }

    fun <T> count(items: Collection<T>, value: (T) -> Flow.Publisher<Boolean>): Flow.Publisher<Int> {
        return sum(items) { t -> value(t).map { if (it) 1 else 0 } }
    }

    fun <K> adjustForPctReporting(result: Flow.Publisher<Map<K, Int>>, pctReporting: Flow.Publisher<Double>): Flow.Publisher<Map<K, Int>> {
        return result.merge(pctReporting) { r, p ->
            val ret: LinkedHashMap<K, Int> = LinkedHashMap()
            r.forEach { (k, v) -> ret[k] = (v * p).toInt() }
            ret
        }
    }

    fun <T> combinePctReporting(items: Collection<T>, pctReportingFunc: (T) -> Flow.Publisher<Double>) = combinePctReporting(items, pctReportingFunc) { 1.0.asOneTimePublisher() }

    fun <T> combinePctReporting(items: Collection<T>, pctReportingFunc: (T) -> Flow.Publisher<Double>, weightFunc: (T) -> Flow.Publisher<Double>): Flow.Publisher<Double> {
        val totalWeight = items.map(weightFunc).combine().map { it.sum().coerceAtLeast(1e-6) }
        return items.map { e ->
            val weight = weightFunc(e).merge(totalWeight) { w, tw -> w / tw }
            pctReportingFunc(e).merge(weight) { p, w -> p * w }
        }.combine().map { it.sum() }
    }

    fun <K1, K2> adjustKey(result: Flow.Publisher<out Map<out K1, Int>>, func: (K1) -> K2) = result.map { adjustKey(it, func) }

    fun <K1, K2> adjustKey(result: Map<K1, Int>, func: (K1) -> K2): Map<K2, Int> {
        return adjustKey(result, func) { a, b -> a + b }
    }

    fun <K1, K2, V : Any> adjustKey(result: Flow.Publisher<out Map<out K1, V>>, keyFunc: (K1) -> K2, valueMergeFunc: (V, V) -> V): Flow.Publisher<Map<K2, V>> = result.map { adjustKey(it, keyFunc, valueMergeFunc) }

    fun <K1, K2, V : Any> adjustKey(result: Map<K1, V>, keyFunc: (K1) -> K2, valueMergeFunc: (V, V) -> V): Map<K2, V> {
        val ret: LinkedHashMap<K2, V> = LinkedHashMap()
        result.forEach { (k, v) -> ret.merge(keyFunc(k), v, valueMergeFunc) }
        return ret
    }

    fun <K> toPct(result: Flow.Publisher<Map<K, Int>>) = result.map { toPct(it) }

    fun <K> toPct(result: Map<K, Int>): Map<K, Double> {
        val total = result.values.sum()
        return result.mapValues { if (total == 0) 0.0 else (1.0 * it.value / total) }
    }

    @Suppress("UNCHECKED_CAST")
    fun <K, T : Int?> topAndOthers(result: Flow.Publisher<Map<K, T>>, limit: Int, others: K) = topAndOthers(result, limit, others, (Array<Any?>(0) { null } as Array<K>).asOneTimePublisher())

    fun <K, T : Int?> topAndOthers(result: Flow.Publisher<out Map<K, T>>, limit: Int, others: K, mustInclude: Flow.Publisher<out Array<K>>) = result.merge(mustInclude) { m, w -> topAndOthers(m, limit, others, *w) }

    fun <K, T : Int?> topAndOthers(result: Map<out K, T>, limit: Int, others: K, vararg mustInclude: K): Map<K, T> {
        return topAndOthers(result, limit, others, mustInclude.toList())
    }

    fun <K, T : Int?> topAndOthers(result: Map<out K, T>, limit: Int, others: K, mustInclude: Collection<K>): Map<K, T> {
        if (result.size <= limit) {
            return result.mapKeys { it.key }
        }
        val mustIncludeSet = mustInclude.filterNotNull().toSet()
        val top = result.entries.asSequence()
            .filter { !mustIncludeSet.contains(it.key) }
            .filter { it.key != others }
            .sortedByDescending { it.value ?: -1 }
            .take(max(0, limit - 1 - mustIncludeSet.size))
            .toSet()
        val topOrRequired: (Map.Entry<K, Int?>) -> Boolean = { top.contains(it) || mustIncludeSet.contains(it.key) }
        val ret: Map<K, T> = result.entries
            .map { e -> (if (topOrRequired(e)) e.key else others) to e.value }
            .groupBy({ it.first }, { it.second })
            .mapValues {
                it.value.reduce { x, y ->
                    @Suppress("UNCHECKED_CAST")
                    (if (x == null || y == null) null else (x + y)) as T
                }
            }
        return ret
    }

    fun <K, V> toMap(keys: Collection<K>, func: (K) -> Flow.Publisher<V>) = toMap(keys, { it }, func)

    fun <T, K, V> toMap(entries: Collection<T>, keyFunc: (T) -> K, func: (T) -> Flow.Publisher<V>): Flow.Publisher<Map<K, V>> {
        return entries.map { e ->
            val k = keyFunc(e)
            func(e).map { v -> k to v }
        }.combine().map { it.toMap() }
    }

    fun <P : PartyOrCoalition> partyChanges(result: Flow.Publisher<out Map<out P, Int>>, partyChanges: Flow.Publisher<out Map<out P, P>>): Flow.Publisher<Map<P, Int>> {
        return partyChanges(result, partyChanges) { a, b -> a + b }
    }

    fun <P : PartyOrCoalition, V : Any> partyChanges(result: Flow.Publisher<out Map<out P, V>>, partyChanges: Flow.Publisher<out Map<out P, P>>, mergeFunc: (V, V) -> V): Flow.Publisher<Map<P, V>> {
        return result.merge(partyChanges) { r, c ->
            adjustKey(r, { c[it] ?: it }, mergeFunc)
        }
    }
}
