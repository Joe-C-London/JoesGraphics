package com.joecollins.models.general

import com.joecollins.graphics.utils.BoundResult
import com.joecollins.models.general.Aggregators.adjustForPctReporting
import com.joecollins.models.general.Aggregators.adjustKey
import com.joecollins.models.general.Aggregators.combine
import com.joecollins.models.general.Aggregators.combineDual
import com.joecollins.models.general.Aggregators.combinePctReporting
import com.joecollins.models.general.Aggregators.count
import com.joecollins.models.general.Aggregators.sum
import com.joecollins.models.general.Aggregators.toMap
import com.joecollins.models.general.Aggregators.toPct
import com.joecollins.models.general.Aggregators.topAndOthers
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.map
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.hamcrest.number.IsCloseTo
import org.junit.Test
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class AggregatorsTest {
    @Test
    fun testKeyChange() {
        val input = Publisher(mapOf("ABC" to 5, "DEF" to 7))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        adjustKey(input) { it.substring(0, 1) }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("A" to 5, "D" to 7)))
        input.submit(mapOf("ABC" to 10, "DEF" to 9, "GHI" to 1))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("A" to 10, "D" to 9, "G" to 1)))
    }

    @Test
    fun testKeyChangeWithMerge() {
        val input = Publisher(mapOf("ABC" to 5, "AZY" to 7))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        adjustKey(input) { it.substring(0, 1) }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("A" to 12)))
        input.submit(mapOf("ABC" to 10, "DEF" to 6, "DCB" to 2))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("A" to 10, "D" to 8)))
    }

    @Test
    fun testCombine() {
        val inputs: MutableList<Publisher<Map<String, Int>>> = ArrayList()
        inputs.add(Publisher(mapOf("ABC" to 8, "DEF" to 6)))
        inputs.add(Publisher(mapOf("ABC" to 7, "GHI" to 3)))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        combine(inputs) { it }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 8 + 7, "DEF" to 6, "GHI" to 3)))
        inputs[0].submit(mapOf("ABC" to 12, "DEF" to 7))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 12 + 7, "DEF" to 7, "GHI" to 3)))
        inputs[1].submit(mapOf("ABC" to 3))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 12 + 3, "DEF" to 7)))
        inputs[0].submit(mapOf("ABC" to 6, "DEF" to 0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 6 + 3, "DEF" to 0)))
        inputs[1].submit(mapOf("ABC" to 4))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 6 + 4, "DEF" to 0)))
    }

    @Test
    fun testCombineWithSeed() {
        val seed = mapOf("ABC" to 0, "DEF" to 0)
        val inputs: MutableList<Publisher<Map<String, Int>>> = ArrayList()
        inputs.add(Publisher(mapOf("ABC" to 8, "DEF" to 6)))
        inputs.add(Publisher(mapOf("ABC" to 7, "GHI" to 3)))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        combine(inputs, { it }, seed).subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 8 + 7, "DEF" to 6, "GHI" to 3)))
        inputs[0].submit(mapOf("ABC" to 12, "DEF" to 7))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 12 + 7, "DEF" to 7, "GHI" to 3)))
        inputs[1].submit(mapOf("ABC" to 3))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 12 + 3, "DEF" to 7)))
        inputs[0].submit(mapOf("ABC" to 6))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 6 + 3, "DEF" to 0)))
        inputs[1].submit(mapOf("ABC" to 4))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 6 + 4, "DEF" to 0)))
    }

    @Test
    fun testCombineDual() {
        val inputs: MutableList<Publisher<Map<String, Pair<Int, Int>>>> = ArrayList()
        inputs.add(
            Publisher(
                mapOf("ABC" to Pair(4, 8), "DEF" to Pair(1, 6))
            )
        )
        inputs.add(
            Publisher(
                mapOf("ABC" to Pair(2, 7), "GHI" to Pair(0, 3))
            )
        )
        val output: BoundResult<Map<String, Pair<Int, Int>>> = BoundResult()
        combineDual(inputs) { it }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until(
                { output.value },
                IsEqual(
                    mapOf(
                        "ABC" to
                            Pair(6, 15),
                        "DEF" to
                            Pair(1, 6),
                        "GHI" to
                            Pair(0, 3)
                    )
                )
            )
        inputs[0].submit(mapOf("ABC" to Pair(5, 12), "DEF" to Pair(4, 7)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until(
                { output.value },
                IsEqual(
                    mapOf(
                        "ABC" to
                            Pair(7, 19),
                        "DEF" to
                            Pair(4, 7),
                        "GHI" to
                            Pair(0, 3)
                    )
                )
            )
        inputs[1].submit(mapOf("ABC" to Pair(2, 3)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to Pair(7, 15), "DEF" to Pair(4, 7))))
        inputs[0].submit(mapOf("ABC" to Pair(0, 6), "DEF" to Pair(0, 0)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to Pair(2, 9), "DEF" to Pair(0, 0))))
        inputs[1].submit(mapOf("ABC" to Pair(4, 4)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to Pair(4, 10), "DEF" to Pair(0, 0))))
    }

    @Test
    fun testCombineDualWithSeeding() {
        val seed: Map<String, Pair<Int, Int>> = mapOf("ABC" to Pair(0, 0), "DEF" to Pair(0, 0))
        val inputs: MutableList<Publisher<Map<String, Pair<Int, Int>>>> = ArrayList()
        inputs.add(
            Publisher(
                mapOf("ABC" to Pair(4, 8), "DEF" to Pair(1, 6))
            )
        )
        inputs.add(
            Publisher(
                mapOf("ABC" to Pair(2, 7), "GHI" to Pair(0, 3))
            )
        )
        val output: BoundResult<Map<String, Pair<Int, Int>>> = BoundResult()
        combineDual(inputs, { it }, seed).subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until(
                { output.value },
                IsEqual(
                    mapOf(
                        "ABC" to
                            Pair(6, 15),
                        "DEF" to
                            Pair(1, 6),
                        "GHI" to
                            Pair(0, 3)
                    )
                )
            )
        inputs[0].submit(mapOf("ABC" to Pair(5, 12), "DEF" to Pair(4, 7)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until(
                { output.value },
                IsEqual(
                    mapOf(
                        "ABC" to
                            Pair(7, 19),
                        "DEF" to
                            Pair(4, 7),
                        "GHI" to
                            Pair(0, 3)
                    )
                )
            )
        inputs[1].submit(mapOf("ABC" to Pair(2, 3)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to Pair(7, 15), "DEF" to Pair(4, 7))))
        inputs[0].submit(mapOf("ABC" to Pair(0, 6)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to Pair(2, 9), "DEF" to Pair(0, 0))))
        inputs[1].submit(mapOf("ABC" to Pair(4, 4)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to Pair(4, 10), "DEF" to Pair(0, 0))))
    }

    @Test
    fun testNestedCombinedStillPropagates() {
        val inputs1: MutableList<Publisher<Map<String, Int>>> = ArrayList()
        inputs1.add(Publisher(mapOf("ABC" to 8, "DEF" to 6)))
        inputs1.add(Publisher(mapOf("ABC" to 7, "GHI" to 3)))
        val inputs2: MutableList<Publisher<Map<String, Int>>> = ArrayList()
        inputs2.add(Publisher(mapOf("ABC" to 8, "DEF" to 6)))
        inputs2.add(Publisher(mapOf("ABC" to 7, "GHI" to 3)))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        val combined = sequenceOf(inputs1, inputs2)
            .map { inputs -> combine(inputs) { it } }
            .toList()
        combine(combined) { it }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 30, "DEF" to 12, "GHI" to 6)))
        inputs1[0].submit(mapOf("ABC" to 9, "DEF" to 5))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 31, "DEF" to 11, "GHI" to 6)))
    }

    @Test
    fun testNestedCombinedDualStillPropagates() {
        val inputs1: MutableList<Publisher<Map<String, Pair<Int, Int>>>> = ArrayList()
        inputs1.add(
            Publisher(
                mapOf("ABC" to Pair(4, 8), "DEF" to Pair(1, 6))
            )
        )
        inputs1.add(
            Publisher(
                mapOf("ABC" to Pair(2, 7), "GHI" to Pair(0, 3))
            )
        )
        val inputs2: MutableList<Publisher<Map<String, Pair<Int, Int>>>> = ArrayList()
        inputs2.add(
            Publisher(
                mapOf("ABC" to Pair(4, 8), "DEF" to Pair(1, 6))
            )
        )
        inputs2.add(
            Publisher(
                mapOf("ABC" to Pair(2, 7), "GHI" to Pair(0, 3))
            )
        )
        val output: BoundResult<Map<String, Pair<Int, Int>>> = BoundResult()
        val combined = sequenceOf(inputs1, inputs2)
            .map { inputs -> combineDual(inputs) { it } }
            .toList()
        combineDual(combined) { it }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until(
                { output.value },
                IsEqual(
                    mapOf(
                        "ABC" to
                            Pair(12, 30),
                        "DEF" to
                            Pair(2, 12),
                        "GHI" to
                            Pair(0, 6)
                    )
                )
            )
        inputs1[0].submit(mapOf("ABC" to Pair(3, 9), "DEF" to Pair(2, 5)))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until(
                { output.value },
                IsEqual(
                    mapOf(
                        "ABC" to
                            Pair(11, 31),
                        "DEF" to
                            Pair(3, 11),
                        "GHI" to
                            Pair(0, 6)
                    )
                )
            )
    }

    @Test
    fun testAdjustForPctReporting() {
        val votes = Publisher(mapOf("ABC" to 500, "DEF" to 300))
        val pctReporting = Publisher(0.01)
        val output: BoundResult<Map<String, Int>> = BoundResult()
        adjustForPctReporting(votes, pctReporting)
            .subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5, "DEF" to 3)))
        pctReporting.submit(0.10)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 50, "DEF" to 30)))
        votes.submit(mapOf("ABC" to 750, "GHI" to 30))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 75, "GHI" to 3)))
    }

    @Test
    fun testCombinePctReporting() {
        val inputs: MutableList<Publisher<Double>> = ArrayList()
        inputs.add(Publisher(0.5))
        inputs.add(Publisher(0.3))
        val output = BoundResult<Double>()
        combinePctReporting(inputs) { it }
            .subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsCloseTo(0.4, 1e-6))
        inputs[0].submit(0.6)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsCloseTo(0.45, 1e-6))
        inputs[1].submit(0.7)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsCloseTo(0.65, 1e-6))
    }

    @Test
    fun testCombinePctReportingWithWeights() {
        val inputs: MutableList<Pair<Publisher<Double>, Double>> = ArrayList()
        inputs.add(Pair(Publisher(0.5), 2.0))
        inputs.add(Pair(Publisher(0.3), 3.0))
        val output = BoundResult<Double>()
        combinePctReporting(inputs, { it.first }, { it.second })
            .subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsCloseTo(0.38, 1e-6))
        inputs[0].first.submit(0.6)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsCloseTo(0.42, 1e-6))
        inputs[1].first.submit(0.7)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsCloseTo(0.66, 1e-6))
    }

    @Test
    fun testTopAndOthersBelowLimit() {
        val votes = Publisher(mapOf("ABC" to 5, "DEF" to 3))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        topAndOthers(votes, 3, "OTHERS").subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5, "DEF" to 3)))
        votes.submit(mapOf("ABC" to 5, "DEF" to 7))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5, "DEF" to 7)))
    }

    @Test
    fun testTopAndOthersAtLimit() {
        val votes = Publisher(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        topAndOthers(votes, 3, "OTHERS").subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2)))
        votes.submit(mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6)))
    }

    @Test
    fun testTopAndOthersAboveLimit() {
        val votes = Publisher(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2, "JKL" to 4))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        topAndOthers(votes, 3, "OTHERS").subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5, "JKL" to 4, "OTHERS" to 5)))
        votes.submit(mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6, "JKL" to 4))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("DEF" to 7, "GHI" to 6, "OTHERS" to 9)))
    }

    @Test
    fun testTopAndOthersAboveLimitWithOthers() {
        val votes = Publisher(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2, "JKL" to 4, "OTHERS" to 6))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        topAndOthers(votes, 3, "OTHERS").subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5, "JKL" to 4, "OTHERS" to 11)))
        votes.submit(mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6, "JKL" to 4, "OTHERS" to 7))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("DEF" to 7, "GHI" to 6, "OTHERS" to 16)))
    }

    @Test
    fun testTopAndOthersAboveLimitWithMandatoryInclusion() {
        val votes = Publisher(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2, "JKL" to 4))
        val winner = Publisher<String?>(null)
        val output: BoundResult<Map<String, Int>> = BoundResult()
        topAndOthers(
            votes, 3, "OTHERS", winner.map { if (it == null) emptyArray() else arrayOf(it) }
        )
            .subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5, "JKL" to 4, "OTHERS" to 5)))
        votes.submit(mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6, "JKL" to 4))
        winner.submit("ABC")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("DEF" to 7, "ABC" to 5, "OTHERS" to 10)))
        winner.submit(null)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("DEF" to 7, "GHI" to 6, "OTHERS" to 9)))
        winner.submit("DEF")
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .ignoreException(NullPointerException::class.java)
            .until({ output.value }, IsEqual(mapOf("DEF" to 7, "GHI" to 6, "OTHERS" to 9)))
    }

    @Test
    fun testToMap() {
        val inputs = mapOf("ABC" to Publisher(1), "DEF" to Publisher(2))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        toMap(inputs.keys) { inputs[it]!! }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 1, "DEF" to 2)))
        inputs["ABC"]!!.submit(7)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 7, "DEF" to 2)))
    }

    @Test
    fun testToMapTransformedKey() {
        val inputs = mapOf("abc" to Publisher(1), "def" to Publisher(2))
        val output: BoundResult<Map<String, Int>> = BoundResult()
        toMap(inputs.keys, { it.uppercase() }) { inputs[it]!! }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 1, "DEF" to 2)))
        inputs["abc"]!!.submit(7)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 7, "DEF" to 2)))
    }

    @Test
    fun testToPct() {
        val votes = Publisher(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2, "JKL" to 4))
        val output: BoundResult<Map<String, Double>> = BoundResult()
        toPct(votes).subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5.0 / 14, "DEF" to 3.0 / 14, "GHI" to 2.0 / 14, "JKL" to 4.0 / 14)))
        votes.submit(mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6, "JKL" to 4))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 5.0 / 22, "DEF" to 7.0 / 22, "GHI" to 6.0 / 22, "JKL" to 4.0 / 22)))
        votes.submit(mapOf("ABC" to 0, "DEF" to 0, "GHI" to 0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(mapOf("ABC" to 0.0, "DEF" to 0.0, "GHI" to 0.0)))
    }

    @Test
    fun testSum() {
        val inputs = listOf(Publisher(1), Publisher(2), Publisher(3))
        val output = BoundResult<Int>()
        sum(inputs) { it }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(6))
        inputs[1].submit(7)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(11))
    }

    @Test
    fun testCount() {
        val inputs = listOf(Publisher(true), Publisher(false), Publisher(false))
        val output = BoundResult<Int>()
        count(inputs) { it }.subscribe(Subscriber { output.value = it })
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(1))
        inputs[1].submit(true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(2))
        inputs[0].submit(false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(1))
        inputs[2].submit(false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(1))
        inputs[1].submit(true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ output.value }, IsEqual(1))
    }
}
