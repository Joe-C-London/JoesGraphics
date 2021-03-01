package com.joecollins.models.general

import com.joecollins.bindings.Binding
import java.lang.Integer.max
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair

object Aggregators {

    @JvmStatic fun <T, K> combine(items: Collection<T>, result: (T) -> Binding<Map<K, Int>>) = combine(items, result, HashMap())

    @JvmStatic fun <T, K> combine(items: Collection<T>, result: (T) -> Binding<Map<K, Int>>, identity: Map<K, Int> = HashMap()): Binding<Map<K, Int>> {
        val seededKeys = identity.keys
        return Binding.mapReduceBinding(
            items.map(result),
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

    @JvmStatic fun <T, K> combineDual(items: Collection<T>, result: (T) -> Binding<Map<K, Pair<Int, Int>>>) = combineDual(items, result, HashMap())

    @JvmStatic fun <T, K> combineDual(items: Collection<T>, result: (T) -> Binding<Map<K, Pair<Int, Int>>>, identity: Map<K, Pair<Int, Int>> = HashMap()): Binding<Map<K, Pair<Int, Int>>> {
        val seededKeys = identity.keys
        val sum: (Pair<Int, Int>, Pair<Int, Int>) -> Pair<Int, Int> =
            { a, b -> ImmutablePair.of(a.left + b.left, a.right + b.right) }
        return Binding.mapReduceBinding(
            items.map(result),
            identity,
            { a, r ->
                val ret = LinkedHashMap(a)
                r.forEach { (k, v) -> ret.merge(k, v, sum) }
                ret
            },
            { a, r ->
                val ret = LinkedHashMap(a)
                r.forEach { (k, v) -> ret.merge(k, ImmutablePair.of(-v.left, -v.right), sum) }
                ret.filter { e -> seededKeys.contains(e.key) || e.value.left != 0 || e.value.right != 0 || !r.containsKey(e.key) }
            }
        )
    }

    @JvmStatic fun <T> sum(items: Collection<T>, value: (T) -> Binding<Int>): Binding<Int> {
        return Binding.mapReduceBinding(
            items.map(value), 0, { t, v -> t + v }, { t, v -> t - v }
        )
    }

    @JvmStatic fun <K> adjustForPctReporting(result: Binding<Map<K, Int>>, pctReporting: Binding<Double>): Binding<Map<K, Int>> {
        return result.merge(pctReporting) { r, p ->
            val ret: LinkedHashMap<K, Int> = LinkedHashMap()
            r.forEach { (k, v) -> ret[k] = (v * p).toInt() }
            ret
        }
    }

    @JvmStatic fun <T> combinePctReporting(items: Collection<T>, pctReportingFunc: (T) -> Binding<Double>) = combinePctReporting(items, pctReportingFunc) { 1.0 }

    @JvmStatic fun <T> combinePctReporting(items: Collection<T>, pctReportingFunc: (T) -> Binding<Double>, weightFunc: (T) -> Double): Binding<Double> {
        val totalWeight = items.map(weightFunc).sum()
        return Binding.mapReduceBinding(
            items.map { e ->
                val weight = weightFunc(e)
                pctReportingFunc(e).map { it * weight } },
            0.0,
            { a, p -> a + (p / totalWeight) },
            { a, p -> a - (p / totalWeight) }
        )
    }

    @JvmStatic fun <K1, K2> adjustKey(result: Binding<Map<K1, Int>>, func: (K1) -> K2) = result.map { adjustKey(it, func) }

    @JvmStatic fun <K1, K2> adjustKey(result: Map<K1, Int>, func: (K1) -> K2): Map<K2, Int> {
        val ret: LinkedHashMap<K2, Int> = LinkedHashMap()
        result.forEach { (k, v) -> ret.merge(func(k), v) { a, b -> a + b } }
        return ret
    }

    @JvmStatic fun <K> toPct(result: Binding<Map<K, Int>>) = result.map { toPct(it) }

    @JvmStatic fun <K> toPct(result: Map<K, Int>): Map<K, Double> {
        val total = result.values.sum()
        return result.mapValues { if (total == 0) 0.0 else (1.0 * it.value / total) }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic fun <K> topAndOthers(result: Binding<Map<K, Int>>, limit: Int, others: K) = topAndOthers(result, limit, others, Binding.fixedBinding(Array<Any?>(0) { null } as Array<K>))

    @JvmStatic fun <K> topAndOthers(result: Binding<Map<K, Int>>, limit: Int, others: K, mustInclude: Binding<Array<K>>) = result.merge(mustInclude) { m, w -> topAndOthers(m, limit, others, *w) }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic fun <K, T : Int?> topAndOthers(result: Map<K, T>, limit: Int, others: K, vararg mustInclude: K): Map<K, T> {
        if (result.size <= limit) {
            return result
        }
        val mustIncludeSet = mustInclude.filterNotNull().toSet()
        val top = result.entries
            .filter { !mustIncludeSet.contains(it.key) }
            .sortedByDescending { it.value ?: -1 }
            .take(max(0, limit - 1 - mustIncludeSet.size))
            .toSet()
        val topAndRequired: (Map.Entry<K, Int?>) -> Boolean = { top.contains(it) || mustIncludeSet.contains(it.key) }
        var needOthers = false
        val ret: LinkedHashMap<K, T> = LinkedHashMap()
        result.entries.forEach { e ->
            if (topAndRequired(e) || e.value != null) {
                val key = if (topAndRequired(e)) e.key else others
                if (ret.containsKey(key)) ret.merge(key, e.value!!) { a, b -> (a!!.toInt() + b!!.toInt()) as T }
                else ret[key] = e.value
            } else {
                needOthers = true
            }
        }
        if (needOthers) {
            ret[others] = null as T
        }
        return ret
    }

    @JvmStatic fun <K, V> toMap(keys: Collection<K>, bindingFunc: (K) -> Binding<V>) = toMap(keys, { it }, bindingFunc)

    @JvmStatic fun <T, K, V> toMap(entries: Collection<T>, keyFunc: (T) -> K, bindingFunc: (T) -> Binding<V>): Binding<Map<K, V>> {
        return object : Binding<Map<K, V>> {
            private var bindings: List<Binding<V>>? = null
            private var map: Map<K, V>? = null

            override val value get() = entries.map { keyFunc(it) to bindingFunc(it).value }.toMap()
            override fun bind(onUpdate: (Map<K, V>) -> Unit) {
                check(bindings == null) { "Binding is already used" }
                map = HashMap()
                val bindingsMap = HashMap<K, Binding<V>>()
                entries.forEach { e ->
                    val key = keyFunc(e)
                    val binding = bindingFunc(e)
                    bindingsMap[key] = binding
                    binding.bind {
                        val updated = HashMap<K, V>(map)
                        updated[key] = it
                        map = updated
                        if (bindings != null) onUpdate(updated)
                    }
                }
                onUpdate(map!!)
                bindings = bindingsMap.values.toList()
            }
            override fun unbind() {
                val bindings = this.bindings
                if (bindings != null) {
                    bindings.forEach { it.unbind() }
                    this.bindings = null
                    map = null
                }
            }
        }
    }
}
