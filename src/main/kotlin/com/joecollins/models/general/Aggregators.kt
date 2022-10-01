package com.joecollins.models.general

import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
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
            }
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
            }
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

    fun <T> combinePctReporting(items: Collection<T>, pctReportingFunc: (T) -> Flow.Publisher<Double>) = combinePctReporting(items, pctReportingFunc) { 1.0 }

    fun <T> combinePctReporting(items: Collection<T>, pctReportingFunc: (T) -> Flow.Publisher<Double>, weightFunc: (T) -> Double): Flow.Publisher<Double> {
        val totalWeight = items.map(weightFunc).sum()
        return items.map { e ->
            val weight = weightFunc(e)
            pctReportingFunc(e).map { it * weight }
        }.mapReduce(
            0.0,
            { a, p -> a + (p / totalWeight) },
            { a, p -> a - (p / totalWeight) }
        )
    }

    fun <K1, K2> adjustKey(result: Flow.Publisher<out Map<out K1, Int>>, func: (K1) -> K2) = result.map { adjustKey(it, func) }

    fun <K1, K2> adjustKey(result: Map<K1, Int>, func: (K1) -> K2): Map<K2, Int> {
        val ret: LinkedHashMap<K2, Int> = LinkedHashMap()
        result.forEach { (k, v) -> ret.merge(func(k), v) { a, b -> a + b } }
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
        val topAndRequired: (Map.Entry<K, Int?>) -> Boolean = { top.contains(it) || mustIncludeSet.contains(it.key) }
        var needOthers = false
        val ret: LinkedHashMap<K, T> = LinkedHashMap()
        result.entries.forEach { e ->
            if (topAndRequired(e) || e.value != null) {
                val key = if (topAndRequired(e)) e.key else others
                if (ret.containsKey(key)) {
                    ret.merge(key, e.value!!) { a, b -> (a!!.toInt() + b!!.toInt()) as T }
                } else {
                    ret[key] = e.value
                }
            } else {
                needOthers = true
            }
        }
        if (needOthers) {
            ret[others] = null as T
        }
        return ret
    }

    fun <K, V> toMap(keys: Collection<K>, func: (K) -> Flow.Publisher<V>) = toMap(keys, { it }, func)

    fun <T, K, V> toMap(entries: Collection<T>, keyFunc: (T) -> K, func: (T) -> Flow.Publisher<V>): Flow.Publisher<Map<K, V>> {
        val ret = Publisher<Map<K, V>>()
        val map = HashMap<K, V>()
        val publishersMap = HashMap<K, Flow.Publisher<V>>()
        entries.forEach { e ->
            val key = keyFunc(e)
            val publisher = func(e)
            publishersMap[key] = publisher
            publisher.subscribe(
                Subscriber {
                    synchronized(map) {
                        map[key] = it
                        if (map.size == entries.size) ret.submit(map)
                    }
                }
            )
        }
        return ret
    }
}
