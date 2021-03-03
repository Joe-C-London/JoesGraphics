package com.joecollins.models.general

import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.models.general.Aggregators.adjustForPctReporting
import com.joecollins.models.general.Aggregators.adjustKey
import com.joecollins.models.general.Aggregators.combine
import com.joecollins.models.general.Aggregators.combineDual
import com.joecollins.models.general.Aggregators.combinePctReporting
import com.joecollins.models.general.Aggregators.sum
import com.joecollins.models.general.Aggregators.toMap
import com.joecollins.models.general.Aggregators.toPct
import com.joecollins.models.general.Aggregators.topAndOthers
import java.util.ArrayList
import org.apache.commons.lang3.mutable.Mutable
import org.apache.commons.lang3.mutable.MutableDouble
import org.apache.commons.lang3.mutable.MutableInt
import org.apache.commons.lang3.mutable.MutableObject
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair
import org.junit.Assert
import org.junit.Test

class AggregatorsTest {
    @Test
    fun testKeyChange() {
        val input = BindableWrapper(mapOf("ABC" to 5, "DEF" to 7))
        val output: Mutable<Map<String, Int>> = MutableObject()
        adjustKey(input.binding) { it.substring(0, 1) }.bind { output.setValue(it) }
        Assert.assertEquals(mapOf("A" to 5, "D" to 7), output.value)
        input.value = mapOf("ABC" to 10, "DEF" to 9, "GHI" to 1)
        Assert.assertEquals(mapOf("A" to 10, "D" to 9, "G" to 1), output.value)
    }

    @Test
    fun testKeyChangeWithMerge() {
        val input = BindableWrapper(mapOf("ABC" to 5, "AZY" to 7))
        val output: Mutable<Map<String, Int>> = MutableObject()
        adjustKey(input.binding) { it.substring(0, 1) }.bind { output.setValue(it) }
        Assert.assertEquals(mapOf("A" to 12), output.value)
        input.value = mapOf("ABC" to 10, "DEF" to 6, "DCB" to 2)
        Assert.assertEquals(mapOf("A" to 10, "D" to 8), output.value)
    }

    @Test
    fun testCombine() {
        val inputs: MutableList<BindableWrapper<Map<String, Int>>> = ArrayList()
        inputs.add(BindableWrapper(mapOf("ABC" to 8, "DEF" to 6)))
        inputs.add(BindableWrapper(mapOf("ABC" to 7, "GHI" to 3)))
        val output: Mutable<Map<String, Int>> = MutableObject()
        combine(inputs) { it.binding }.bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 15, "DEF" to 6, "GHI" to 3), output.value)
        inputs[0].value = mapOf("ABC" to 12, "DEF" to 7)
        Assert.assertEquals(mapOf("ABC" to 19, "DEF" to 7, "GHI" to 3), output.value)
        inputs[1].value = mapOf("ABC" to 3)
        Assert.assertEquals(mapOf("ABC" to 15, "DEF" to 7), output.value)
        inputs[0].value = mapOf("ABC" to 6, "DEF" to 0)
        Assert.assertEquals(mapOf("ABC" to 9, "DEF" to 0), output.value)
        inputs[1].value = mapOf("ABC" to 4)
        Assert.assertEquals(mapOf("ABC" to 10, "DEF" to 0), output.value)
    }

    @Test
    fun testCombineWithSeed() {
        val seed = mapOf("ABC" to 0, "DEF" to 0)
        val inputs: MutableList<BindableWrapper<Map<String, Int>>> = ArrayList()
        inputs.add(BindableWrapper(mapOf("ABC" to 8, "DEF" to 6)))
        inputs.add(BindableWrapper(mapOf("ABC" to 7, "GHI" to 3)))
        val output: Mutable<Map<String, Int>> = MutableObject()
        combine(inputs, { it.binding }, seed).bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 15, "DEF" to 6, "GHI" to 3), output.value)
        inputs[0].value = mapOf("ABC" to 12, "DEF" to 7)
        Assert.assertEquals(mapOf("ABC" to 19, "DEF" to 7, "GHI" to 3), output.value)
        inputs[1].value = mapOf("ABC" to 3)
        Assert.assertEquals(mapOf("ABC" to 15, "DEF" to 7), output.value)
        inputs[0].value = mapOf("ABC" to 6)
        Assert.assertEquals(mapOf("ABC" to 9, "DEF" to 0), output.value)
        inputs[1].value = mapOf("ABC" to 4)
        Assert.assertEquals(mapOf("ABC" to 10, "DEF" to 0), output.value)
    }

    @Test
    fun testCombineDual() {
        val inputs: MutableList<BindableWrapper<Map<String, Pair<Int, Int>>>> = ArrayList()
        inputs.add(
                BindableWrapper(
                        mapOf("ABC" to ImmutablePair.of(4, 8), "DEF" to ImmutablePair.of(1, 6))))
        inputs.add(
                BindableWrapper(
                        mapOf("ABC" to ImmutablePair.of(2, 7), "GHI" to ImmutablePair.of(0, 3))))
        val output: Mutable<Map<String, Pair<Int, Int>>> = MutableObject()
        combineDual(inputs) { it.binding }.bind { output.setValue(it) }
        Assert.assertEquals(
                mapOf(
                        "ABC" to
                        ImmutablePair.of(6, 15),
                        "DEF" to
                        ImmutablePair.of(1, 6),
                        "GHI" to
                        ImmutablePair.of(0, 3)),
                output.value)
        inputs[0].value = mapOf("ABC" to ImmutablePair.of(5, 12), "DEF" to ImmutablePair.of(4, 7))
        Assert.assertEquals(
                mapOf(
                        "ABC" to
                        ImmutablePair.of(7, 19),
                        "DEF" to
                        ImmutablePair.of(4, 7),
                        "GHI" to
                        ImmutablePair.of(0, 3)),
                output.value)
        inputs[1].value = mapOf("ABC" to ImmutablePair.of(2, 3))
        Assert.assertEquals(
                mapOf("ABC" to ImmutablePair.of(7, 15), "DEF" to ImmutablePair.of(4, 7)), output.value)
        inputs[0].value = mapOf("ABC" to ImmutablePair.of(0, 6), "DEF" to ImmutablePair.of(0, 0))
        Assert.assertEquals(
                mapOf("ABC" to ImmutablePair.of(2, 9), "DEF" to ImmutablePair.of(0, 0)), output.value)
        inputs[1].value = mapOf("ABC" to ImmutablePair.of(4, 4))
        Assert.assertEquals(
                mapOf("ABC" to ImmutablePair.of(4, 10), "DEF" to ImmutablePair.of(0, 0)), output.value)
    }

    @Test
    fun testCombineDualWithSeeding() {
        val seed: Map<String, Pair<Int, Int>> = mapOf("ABC" to ImmutablePair.of(0, 0), "DEF" to ImmutablePair.of(0, 0))
        val inputs: MutableList<BindableWrapper<Map<String, Pair<Int, Int>>>> = ArrayList()
        inputs.add(
                BindableWrapper(
                        mapOf("ABC" to ImmutablePair.of(4, 8), "DEF" to ImmutablePair.of(1, 6))))
        inputs.add(
                BindableWrapper(
                        mapOf("ABC" to ImmutablePair.of(2, 7), "GHI" to ImmutablePair.of(0, 3))))
        val output: Mutable<Map<String, Pair<Int, Int>>> = MutableObject()
        combineDual(inputs, { it.binding }, seed).bind { output.setValue(it) }
        Assert.assertEquals(
                mapOf(
                        "ABC" to
                        ImmutablePair.of(6, 15),
                        "DEF" to
                        ImmutablePair.of(1, 6),
                        "GHI" to
                        ImmutablePair.of(0, 3)),
                output.value)
        inputs[0].value = mapOf("ABC" to ImmutablePair.of(5, 12), "DEF" to ImmutablePair.of(4, 7))
        Assert.assertEquals(
                mapOf(
                        "ABC" to
                        ImmutablePair.of(7, 19),
                        "DEF" to
                        ImmutablePair.of(4, 7),
                        "GHI" to
                        ImmutablePair.of(0, 3)),
                output.value)
        inputs[1].value = mapOf("ABC" to ImmutablePair.of(2, 3))
        Assert.assertEquals(
                mapOf("ABC" to ImmutablePair.of(7, 15), "DEF" to ImmutablePair.of(4, 7)), output.value)
        inputs[0].value = mapOf("ABC" to ImmutablePair.of(0, 6))
        Assert.assertEquals(
                mapOf("ABC" to ImmutablePair.of(2, 9), "DEF" to ImmutablePair.of(0, 0)), output.value)
        inputs[1].value = mapOf("ABC" to ImmutablePair.of(4, 4))
        Assert.assertEquals(
                mapOf("ABC" to ImmutablePair.of(4, 10), "DEF" to ImmutablePair.of(0, 0)), output.value)
    }

    @Test
    fun testNestedCombinedStillPropagates() {
        val inputs1: MutableList<BindableWrapper<Map<String, Int>>> = ArrayList()
        inputs1.add(BindableWrapper(mapOf("ABC" to 8, "DEF" to 6)))
        inputs1.add(BindableWrapper(mapOf("ABC" to 7, "GHI" to 3)))
        val inputs2: MutableList<BindableWrapper<Map<String, Int>>> = ArrayList()
        inputs2.add(BindableWrapper(mapOf("ABC" to 8, "DEF" to 6)))
        inputs2.add(BindableWrapper(mapOf("ABC" to 7, "GHI" to 3)))
        val output: Mutable<Map<String, Int>> = MutableObject()
        val combined = sequenceOf(inputs1, inputs2)
                .map { inputs -> combine(inputs) { it.binding } }
                .toList()
        combine(combined) { it }.bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 30, "DEF" to 12, "GHI" to 6), output.value)
        inputs1[0].value = mapOf("ABC" to 9, "DEF" to 5)
        Assert.assertEquals(mapOf("ABC" to 31, "DEF" to 11, "GHI" to 6), output.value)
    }

    @Test
    fun testNestedCombinedDualStillPropagates() {
        val inputs1: MutableList<BindableWrapper<Map<String, Pair<Int, Int>>>> = ArrayList()
        inputs1.add(
                BindableWrapper(
                        mapOf("ABC" to ImmutablePair.of(4, 8), "DEF" to ImmutablePair.of(1, 6))))
        inputs1.add(
                BindableWrapper(
                        mapOf("ABC" to ImmutablePair.of(2, 7), "GHI" to ImmutablePair.of(0, 3))))
        val inputs2: MutableList<BindableWrapper<Map<String, Pair<Int, Int>>>> = ArrayList()
        inputs2.add(
                BindableWrapper(
                        mapOf("ABC" to ImmutablePair.of(4, 8), "DEF" to ImmutablePair.of(1, 6))))
        inputs2.add(
                BindableWrapper(
                        mapOf("ABC" to ImmutablePair.of(2, 7), "GHI" to ImmutablePair.of(0, 3))))
        val output: Mutable<Map<String, Pair<Int, Int>>> = MutableObject()
        val combined = sequenceOf(inputs1, inputs2)
                .map { inputs -> combineDual(inputs) { it.binding } }
                .toList()
        combineDual(combined) { it }.bind { output.setValue(it) }
        Assert.assertEquals(
                mapOf(
                        "ABC" to
                        ImmutablePair.of(12, 30),
                        "DEF" to
                        ImmutablePair.of(2, 12),
                        "GHI" to
                        ImmutablePair.of(0, 6)),
                output.value)
        inputs1[0].value = mapOf("ABC" to ImmutablePair.of(3, 9), "DEF" to ImmutablePair.of(2, 5))
        Assert.assertEquals(
                mapOf(
                        "ABC" to
                        ImmutablePair.of(11, 31),
                        "DEF" to
                        ImmutablePair.of(3, 11),
                        "GHI" to
                        ImmutablePair.of(0, 6)),
                output.value)
    }

    @Test
    fun testAdjustForPctReporting() {
        val votes = BindableWrapper(mapOf("ABC" to 500, "DEF" to 300))
        val pctReporting = BindableWrapper(0.01)
        val output: Mutable<Map<String, Int>> = MutableObject()
        adjustForPctReporting(votes.binding, pctReporting.binding)
                .bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 5, "DEF" to 3), output.value)
        pctReporting.value = 0.10
        Assert.assertEquals(mapOf("ABC" to 50, "DEF" to 30), output.value)
        votes.value = mapOf("ABC" to 750, "GHI" to 30)
        Assert.assertEquals(mapOf("ABC" to 75, "GHI" to 3), output.value)
    }

    @Test
    fun testCombinePctReporting() {
        val inputs: MutableList<BindableWrapper<Double>> = ArrayList()
        inputs.add(BindableWrapper(0.5))
        inputs.add(BindableWrapper(0.3))
        val output = MutableDouble()
        combinePctReporting(inputs) { it.binding }
                .bind { output.setValue(it) }
        Assert.assertEquals(0.4, output.value, 1e-6)
        inputs[0].value = 0.6
        Assert.assertEquals(0.45, output.value, 1e-6)
        inputs[1].value = 0.7
        Assert.assertEquals(0.65, output.value, 1e-6)
    }

    @Test
    fun testCombinePctReportingWithWeights() {
        val inputs: MutableList<Pair<BindableWrapper<Double>, Double>> = ArrayList()
        inputs.add(ImmutablePair.of(BindableWrapper(0.5), 2.0))
        inputs.add(ImmutablePair.of(BindableWrapper(0.3), 3.0))
        val output = MutableDouble()
        combinePctReporting(inputs, { it.left.binding }) { it.right }
                .bind { output.setValue(it) }
        Assert.assertEquals(0.38, output.value, 1e-6)
        inputs[0].left.value = 0.6
        Assert.assertEquals(0.42, output.value, 1e-6)
        inputs[1].left.value = 0.7
        Assert.assertEquals(0.66, output.value, 1e-6)
    }

    @Test
    fun testTopAndOthersBelowLimit() {
        val votes = BindableWrapper(mapOf("ABC" to 5, "DEF" to 3))
        val output: Mutable<Map<String, Int>> = MutableObject()
        topAndOthers(votes.binding, 3, "OTHERS").bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 5, "DEF" to 3), output.value)
        votes.value = mapOf("ABC" to 5, "DEF" to 7)
        Assert.assertEquals(mapOf("ABC" to 5, "DEF" to 7), output.value)
    }

    @Test
    fun testTopAndOthersAtLimit() {
        val votes = BindableWrapper(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2))
        val output: Mutable<Map<String, Int>> = MutableObject()
        topAndOthers(votes.binding, 3, "OTHERS").bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2), output.value)
        votes.value = mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6)
        Assert.assertEquals(mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6), output.value)
    }

    @Test
    fun testTopAndOthersAboveLimit() {
        val votes = BindableWrapper(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2, "JKL" to 4))
        val output: Mutable<Map<String, Int>> = MutableObject()
        topAndOthers(votes.binding, 3, "OTHERS").bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 5, "JKL" to 4, "OTHERS" to 5), output.value)
        votes.value = mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6, "JKL" to 4)
        Assert.assertEquals(mapOf("DEF" to 7, "GHI" to 6, "OTHERS" to 9), output.value)
    }

    @Test
    fun testTopAndOthersAboveLimitWithMandatoryInclusion() {
        val votes = BindableWrapper(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2, "JKL" to 4))
        val winner = BindableWrapper<String?>(null)
        val output: Mutable<Map<String, Int>> = MutableObject()
        topAndOthers(
                votes.binding, 3, "OTHERS", winner.binding.map { if (it == null) emptyArray() else arrayOf(it) })
                .bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 5, "JKL" to 4, "OTHERS" to 5), output.value)
        votes.value = mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6, "JKL" to 4)
        winner.value = "ABC"
        Assert.assertEquals(mapOf("DEF" to 7, "ABC" to 5, "OTHERS" to 10), output.value)
        winner.value = null
        Assert.assertEquals(mapOf("DEF" to 7, "GHI" to 6, "OTHERS" to 9), output.value)
        winner.value = "DEF"
        Assert.assertEquals(mapOf("DEF" to 7, "GHI" to 6, "OTHERS" to 9), output.value)
    }

    @Test
    fun testToMap() {
        val inputs = mapOf("ABC" to BindableWrapper(1), "DEF" to BindableWrapper(2))
        val output: Mutable<Map<String, Int>> = MutableObject()
        val outputBinding = toMap(inputs.keys) { inputs[it]!!.binding }
        outputBinding.bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 1, "DEF" to 2), output.value)
        inputs["ABC"]!!.value = 7
        Assert.assertEquals(mapOf("ABC" to 7, "DEF" to 2), output.value)
        outputBinding.unbind()
        inputs["DEF"]!!.value = 9
        Assert.assertEquals(mapOf("ABC" to 7, "DEF" to 2), output.value)
    }

    @Test
    fun testToMapTransformedKey() {
        val inputs = mapOf("abc" to BindableWrapper(1), "def" to BindableWrapper(2))
        val output: Mutable<Map<String, Int>> = MutableObject()
        val outputBinding = toMap(inputs.keys, { it.toUpperCase() }) { inputs[it]!!.binding }
        outputBinding.bind { output.setValue(it) }
        Assert.assertEquals(mapOf("ABC" to 1, "DEF" to 2), output.value)
        inputs["abc"]!!.value = 7
        Assert.assertEquals(mapOf("ABC" to 7, "DEF" to 2), output.value)
        outputBinding.unbind()
        inputs["def"]!!.value = 9
        Assert.assertEquals(mapOf("ABC" to 7, "DEF" to 2), output.value)
    }

    @Test
    fun testToPct() {
        val votes = BindableWrapper(mapOf("ABC" to 5, "DEF" to 3, "GHI" to 2, "JKL" to 4))
        val output: Mutable<Map<String, Double>> = MutableObject()
        val outputBinding = toPct(votes.binding)
        outputBinding.bind { output.setValue(it) }
        Assert.assertEquals(
                mapOf("ABC" to 5.0 / 14, "DEF" to 3.0 / 14, "GHI" to 2.0 / 14, "JKL" to 4.0 / 14),
                output.value)
        votes.value = mapOf("ABC" to 5, "DEF" to 7, "GHI" to 6, "JKL" to 4)
        Assert.assertEquals(
                mapOf("ABC" to 5.0 / 22, "DEF" to 7.0 / 22, "GHI" to 6.0 / 22, "JKL" to 4.0 / 22),
                output.value)
        votes.value = mapOf("ABC" to 0, "DEF" to 0, "GHI" to 0)
        Assert.assertEquals(mapOf("ABC" to 0.0, "DEF" to 0.0, "GHI" to 0.0), output.value)
    }

    @Test
    fun testSum() {
        val inputs = listOf(BindableWrapper(1), BindableWrapper(2), BindableWrapper(3))
        val output = MutableInt()
        sum(inputs) { it.binding }.bind { output.setValue(it) }
        Assert.assertEquals(6, output.value.toInt().toLong())
        inputs[1].value = 7
        Assert.assertEquals(11, output.value.toInt().toLong())
    }
}
