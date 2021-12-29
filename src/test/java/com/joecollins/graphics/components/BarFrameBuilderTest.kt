package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.BarFrameBuilder.Companion.basic
import com.joecollins.graphics.components.BarFrameBuilder.Companion.dual
import com.joecollins.graphics.components.BarFrameBuilder.Companion.dualReversed
import com.joecollins.graphics.components.BarFrameBuilder.DualBar
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.ColorUtils
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class BarFrameBuilderTest {
    private class Wrapper<T>(val value: T)

    @Test
    fun testSimpleBars() {
        val result = BindableWrapper<Map<Pair<String, Color>, Int>>(emptyMap())
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                }
        )
            .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
            Pair("CLINTON", Color.ORANGE) to 2842,
            Pair("SANDERS", Color.GREEN) to 1865
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(2))
        Assert.assertEquals("CLINTON", frame.getLeftText(0))
        Assert.assertEquals("SANDERS", frame.getLeftText(1))
        Assert.assertEquals("2,842", frame.getRightText(0))
        Assert.assertEquals("1,865", frame.getRightText(1))
        Assert.assertEquals(Color.ORANGE, frame.getSeries(0)[0].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(1)[0].first)
        Assert.assertEquals(2842, frame.getSeries(0)[0].second)
        Assert.assertEquals(1865, frame.getSeries(1)[0].second)
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(2842, frame.max.toInt().toLong())
    }

    @Test
    fun testSimpleBarsWithValueObject() {
        val result = BindableWrapper<Map<Pair<String, Color>, Wrapper<Int>>>(emptyMap())
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value.value, THOUSANDS.format(it.value.value)) }
                        .toList()
                }
        )
            .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
            Pair("CLINTON", Color.ORANGE) to Wrapper(2842),
            Pair("SANDERS", Color.GREEN) to Wrapper(1865)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(2))
        Assert.assertEquals("CLINTON", frame.getLeftText(0))
        Assert.assertEquals("SANDERS", frame.getLeftText(1))
        Assert.assertEquals("2,842", frame.getRightText(0))
        Assert.assertEquals("1,865", frame.getRightText(1))
        Assert.assertEquals(Color.ORANGE, frame.getSeries(0)[0].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(1)[0].first)
        Assert.assertEquals(2842, frame.getSeries(0)[0].second)
        Assert.assertEquals(1865, frame.getSeries(1)[0].second)
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(2842, frame.max.toInt().toLong())
    }

    @Test
    fun testSimpleBarsRange() {
        val result = BindableWrapper<Map<Pair<String, Color>, Int>>(emptyMap())
        val max = BindableWrapper(2500)
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                }
        )
            .withMax(max.binding)
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(2500))
        result.value = mapOf(
            Pair("CLINTON", Color.ORANGE) to 2205,
            Pair("SANDERS", Color.GREEN) to 1846
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(2500))
        result.value = mapOf(
            Pair("CLINTON", Color.ORANGE) to 2842,
            Pair("SANDERS", Color.GREEN) to 1865
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(2842))
        max.value = 3000
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(3000))
        max.value = 2500
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(2842))
    }

    @Test
    fun testHeaderSubheadAndNotes() {
        val result = BindableWrapper<Map<Pair<String, Color>, Int>>(emptyMap())
        val header = BindableWrapper<String?>("HEADER")
        val subhead = BindableWrapper<String?>("SUBHEAD")
        val notes = BindableWrapper<String?>("NOTES")
        val borderColor = BindableWrapper(Color.BLACK)
        val subheadColor = BindableWrapper(Color.GRAY)
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                }
        )
            .withHeader(header.binding)
            .withSubhead(subhead.binding)
            .withNotes(notes.binding)
            .withBorder(borderColor.binding)
            .withSubheadColor(subheadColor.binding)
            .build()
        Assert.assertEquals("HEADER", frame.header)
        Assert.assertEquals("SUBHEAD", frame.subheadText)
        Assert.assertEquals("NOTES", frame.notes)
        Assert.assertEquals(Color.BLACK, frame.borderColor)
        Assert.assertEquals(Color.GRAY, frame.subheadColor)
        header.value = "DEMOCRATIC PRIMARY"
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsEqual("DEMOCRATIC PRIMARY"))
        subhead.value = "PLEDGED DELEGATES"
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.subheadText }, IsEqual("PLEDGED DELEGATES"))
        notes.value = "SOURCE: DNC"
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.notes }, IsEqual("SOURCE: DNC"))
        borderColor.value = Color.BLUE
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.borderColor }, IsEqual(Color.BLUE))
        subheadColor.value = Color.BLUE
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.subheadColor }, IsEqual(Color.BLUE))
    }

    @Test
    fun testTarget() {
        val result = BindableWrapper<Map<Pair<String, Color>, Int>>(emptyMap())
        val target = BindableWrapper(2382)
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                }
        )
            .withTarget(target.binding) { THOUSANDS.format(it) + " TO WIN" }
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numLines }, IsEqual(1))
        Assert.assertEquals(2382, frame.getLineLevel(0))
        Assert.assertEquals("2,382 TO WIN", frame.getLineLabel(0))
    }

    @Test
    fun testMultiLines() {
        val result = BindableWrapper<Map<Pair<String, Color>, Int>>(emptyMap())
        val lines = BindableWrapper<List<Int>>(emptyList())
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                }
        )
            .withLines(lines.binding) { it.toString() + " QUOTA" + (if (it == 1) "" else "S") }
            .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        lines.value = listOf(1, 2, 3, 4, 5)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numLines }, IsEqual(5))
        Assert.assertEquals(1, frame.getLineLevel(0))
        Assert.assertEquals(2, frame.getLineLevel(1))
        Assert.assertEquals(3, frame.getLineLevel(2))
        Assert.assertEquals(4, frame.getLineLevel(3))
        Assert.assertEquals(5, frame.getLineLevel(4))
        Assert.assertEquals("1 QUOTA", frame.getLineLabel(0))
        Assert.assertEquals("2 QUOTAS", frame.getLineLabel(1))
        Assert.assertEquals("3 QUOTAS", frame.getLineLabel(2))
        Assert.assertEquals("4 QUOTAS", frame.getLineLabel(3))
        Assert.assertEquals("5 QUOTAS", frame.getLineLabel(4))
    }

    @Test
    fun testMultiLinesBespokeLabels() {
        val result = BindableWrapper<Map<Pair<String, Color>, Int>>(emptyMap())
        val lines = BindableWrapper<List<Pair<String, Int>>>(emptyList())
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                }
        )
            .withLines(lines.binding, { it.first }) { it.second }
            .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        lines.value = listOf(
            Pair("The line is here", 1),
            Pair("and here", 2)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numLines }, IsEqual(2))
        Assert.assertEquals(1, frame.getLineLevel(0))
        Assert.assertEquals(2, frame.getLineLevel(1))
        Assert.assertEquals("The line is here", frame.getLineLabel(0))
        Assert.assertEquals("and here", frame.getLineLabel(1))
    }

    @Test
    fun testMultiLinesBinding() {
        val result = BindableWrapper<Map<Pair<String, Color>, Int>>(emptyMap())
        val lines = BindableWrapper(listOf<Int>())
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                }
        )
            .withLines(lines.binding) { it.toString() + " QUOTA" + (if (it == 1) "" else "S") }
            .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        lines.value = listOf(1, 2, 3, 4, 5)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numLines }, IsEqual(5))
        Assert.assertEquals(1, frame.getLineLevel(0))
        Assert.assertEquals(2, frame.getLineLevel(1))
        Assert.assertEquals(3, frame.getLineLevel(2))
        Assert.assertEquals(4, frame.getLineLevel(3))
        Assert.assertEquals(5, frame.getLineLevel(4))
        Assert.assertEquals("1 QUOTA", frame.getLineLabel(0))
        Assert.assertEquals("2 QUOTAS", frame.getLineLabel(1))
        Assert.assertEquals("3 QUOTAS", frame.getLineLabel(2))
        Assert.assertEquals("4 QUOTAS", frame.getLineLabel(3))
        Assert.assertEquals("5 QUOTAS", frame.getLineLabel(4))
    }

    @Test
    fun testLeftShape() {
        val result = BindableWrapper<Map<Pair<String, Color>, Pair<Int, Boolean>>>(emptyMap())
        val shape = Rectangle2D.Double(0.0, 0.0, 1.0, 1.0)
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.first }
                        .map { BasicBar(it.key.first, it.key.second, it.value.first, THOUSANDS.format(it.value.first), if (it.value.second) shape else null) }
                        .toList()
                }
        )
            .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
            Pair("CLINTON", Color.ORANGE) to Pair(2842, true),
            Pair("SANDERS", Color.GREEN) to Pair(1865, false)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(2))
        Assert.assertEquals("CLINTON", frame.getLeftText(0))
        Assert.assertEquals("SANDERS", frame.getLeftText(1))
        Assert.assertEquals("2,842", frame.getRightText(0))
        Assert.assertEquals("1,865", frame.getRightText(1))
        Assert.assertEquals(Color.ORANGE, frame.getSeries(0)[0].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(1)[0].first)
        Assert.assertEquals(2842, frame.getSeries(0)[0].second)
        Assert.assertEquals(1865, frame.getSeries(1)[0].second)
        Assert.assertEquals(shape, frame.getLeftIcon(0))
        Assert.assertNull(frame.getLeftIcon(1))
    }

    @Test
    fun testSimpleDiffBars() {
        val result = BindableWrapper<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.first }
                        .map { BasicBar(it.key.first, it.key.second, it.value.second, DIFF.format(it.value.second)) }
                        .toList()
                }
        )
            .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
            Pair("LIB", Color.RED) to Pair(157, -27),
            Pair("CON", Color.BLUE) to Pair(121, +22),
            Pair("NDP", Color.ORANGE) to Pair(24, -20),
            Pair("BQ", Color.CYAN) to Pair(32, +22),
            Pair("GRN", Color.GREEN) to Pair(3, +2),
            Pair("IND", Color.GRAY) to Pair(1, +1)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        Assert.assertEquals("LIB", frame.getLeftText(0))
        Assert.assertEquals("CON", frame.getLeftText(1))
        Assert.assertEquals("BQ", frame.getLeftText(2))
        Assert.assertEquals("NDP", frame.getLeftText(3))
        Assert.assertEquals("GRN", frame.getLeftText(4))
        Assert.assertEquals("IND", frame.getLeftText(5))
        Assert.assertEquals("-27", frame.getRightText(0))
        Assert.assertEquals("+22", frame.getRightText(1))
        Assert.assertEquals("+22", frame.getRightText(2))
        Assert.assertEquals("-20", frame.getRightText(3))
        Assert.assertEquals("+2", frame.getRightText(4))
        Assert.assertEquals("+1", frame.getRightText(5))
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].first)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].first)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[0].first)
        Assert.assertEquals(Color.ORANGE, frame.getSeries(3)[0].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[0].first)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[0].first)
        Assert.assertEquals(-27, frame.getSeries(0)[0].second)
        Assert.assertEquals(+22, frame.getSeries(1)[0].second)
        Assert.assertEquals(+22, frame.getSeries(2)[0].second)
        Assert.assertEquals(-20, frame.getSeries(3)[0].second)
        Assert.assertEquals(+2, frame.getSeries(4)[0].second)
        Assert.assertEquals(+1, frame.getSeries(5)[0].second)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(-27))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(22))
    }

    @Test
    fun testSimpleDiffWingspan() {
        val result = BindableWrapper<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val range = BindableWrapper(10)
        val frame = basic(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.first }
                        .map { BasicBar(it.key.first, it.key.second, it.value.second, DIFF.format(it.value.second)) }
                        .toList()
                }
        )
            .withWingspan(range.binding)
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toDouble() }, IsEqual(10.0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toDouble() }, IsEqual(-10.0))
        result.value = mapOf(
            Pair("LIB", Color.RED) to Pair(157, -27),
            Pair("CON", Color.BLUE) to Pair(121, +22),
            Pair("NDP", Color.ORANGE) to Pair(24, -20),
            Pair("BQ", Color.CYAN) to Pair(32, +22),
            Pair("GRN", Color.GREEN) to Pair(3, +2),
            Pair("IND", Color.GRAY) to Pair(1, +1)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toDouble() }, IsEqual(27.0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toDouble() }, IsEqual(-27.0))
    }

    @Test
    fun testDualValueBars() {
        val result = BindableWrapper<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val frame = dual(
            result
                .binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.second }
                        .map { DualBar(it.key.first, it.key.second, it.value.first, it.value.second, it.value.first.toString() + "/" + it.value.second) }
                        .toList()
                }
        )
            .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
            Pair("LIBERAL", Color.RED) to Pair(26, 157),
            Pair("CONSERVATIVE", Color.BLUE) to Pair(4, 121),
            Pair("NEW DEMOCRATIC PARTY", Color.ORANGE) to Pair(1, 24),
            Pair("BLOC QU\u00c9B\u00c9COIS", Color.CYAN) to Pair(0, 32),
            Pair("GREEN", Color.GREEN) to Pair(1, 3),
            Pair("INDEPENDENT", Color.GRAY) to Pair(0, 1)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(1))
        Assert.assertEquals("BLOC QU\u00c9B\u00c9COIS", frame.getLeftText(2))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3))
        Assert.assertEquals("GREEN", frame.getLeftText(4))
        Assert.assertEquals("INDEPENDENT", frame.getLeftText(5))
        Assert.assertEquals("26/157", frame.getRightText(0))
        Assert.assertEquals("4/121", frame.getRightText(1))
        Assert.assertEquals("0/32", frame.getRightText(2))
        Assert.assertEquals("1/24", frame.getRightText(3))
        Assert.assertEquals("1/3", frame.getRightText(4))
        Assert.assertEquals("0/1", frame.getRightText(5))
        Assert.assertEquals(0, frame.getSeries(0)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(1)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(3)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(4)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[0].second.toInt().toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[1].first)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[1].first)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[1].first)
        Assert.assertEquals(Color.ORANGE, frame.getSeries(3)[1].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[1].first)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[1].first)
        Assert.assertEquals(26, frame.getSeries(0)[1].second.toInt().toLong())
        Assert.assertEquals(4, frame.getSeries(1)[1].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[1].second.toInt().toLong())
        Assert.assertEquals(1, frame.getSeries(3)[1].second.toInt().toLong())
        Assert.assertEquals(1, frame.getSeries(4)[1].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[1].second.toInt().toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeries(0)[2].first)
        Assert.assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].first)
        Assert.assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].first)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].first)
        Assert.assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].first)
        Assert.assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].first)
        Assert.assertEquals((157 - 26).toLong(), frame.getSeries(0)[2].second.toInt().toLong())
        Assert.assertEquals((121 - 4).toLong(), frame.getSeries(1)[2].second.toInt().toLong())
        Assert.assertEquals((32 - 0).toLong(), frame.getSeries(2)[2].second.toInt().toLong())
        Assert.assertEquals((24 - 1).toLong(), frame.getSeries(3)[2].second.toInt().toLong())
        Assert.assertEquals((3 - 1).toLong(), frame.getSeries(4)[2].second.toInt().toLong())
        Assert.assertEquals((1 - 0).toLong(), frame.getSeries(5)[2].second.toInt().toLong())
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(157, frame.max.toInt().toLong())
    }

    @Test
    fun testDualReversedValueBars() {
        val result = BindableWrapper<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val frame = dualReversed(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.second }
                        .map { DualBar(it.key.first, it.key.second, it.value.first, it.value.second, it.value.first.toString() + "/" + it.value.second) }
                        .toList()
                }
        )
            .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
            Pair("LIBERAL", Color.RED) to Pair(26, 157),
            Pair("CONSERVATIVE", Color.BLUE) to Pair(4, 121),
            Pair("NEW DEMOCRATIC PARTY", Color.ORANGE) to Pair(1, 24),
            Pair("BLOC QU\u00c9B\u00c9COIS", Color.CYAN) to Pair(0, 32),
            Pair("GREEN", Color.GREEN) to Pair(1, 3),
            Pair("INDEPENDENT", Color.GRAY) to Pair(0, 1)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(1))
        Assert.assertEquals("BLOC QU\u00c9B\u00c9COIS", frame.getLeftText(2))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3))
        Assert.assertEquals("GREEN", frame.getLeftText(4))
        Assert.assertEquals("INDEPENDENT", frame.getLeftText(5))
        Assert.assertEquals("26/157", frame.getRightText(0))
        Assert.assertEquals("4/121", frame.getRightText(1))
        Assert.assertEquals("0/32", frame.getRightText(2))
        Assert.assertEquals("1/24", frame.getRightText(3))
        Assert.assertEquals("1/3", frame.getRightText(4))
        Assert.assertEquals("0/1", frame.getRightText(5))
        Assert.assertEquals(0, frame.getSeries(0)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(1)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(3)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(4)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[0].second.toInt().toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeries(0)[1].first)
        Assert.assertEquals(lighten(Color.BLUE), frame.getSeries(1)[1].first)
        Assert.assertEquals(lighten(Color.CYAN), frame.getSeries(2)[1].first)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].first)
        Assert.assertEquals(lighten(Color.GREEN), frame.getSeries(4)[1].first)
        Assert.assertEquals(lighten(Color.GRAY), frame.getSeries(5)[1].first)
        Assert.assertEquals(26, frame.getSeries(0)[1].second.toInt().toLong())
        Assert.assertEquals(4, frame.getSeries(1)[1].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[1].second.toInt().toLong())
        Assert.assertEquals(1, frame.getSeries(3)[1].second.toInt().toLong())
        Assert.assertEquals(1, frame.getSeries(4)[1].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[1].second.toInt().toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[2].first)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[2].first)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[2].first)
        Assert.assertEquals(Color.ORANGE, frame.getSeries(3)[2].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[2].first)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[2].first)
        Assert.assertEquals((157 - 26).toLong(), frame.getSeries(0)[2].second.toInt().toLong())
        Assert.assertEquals((121 - 4).toLong(), frame.getSeries(1)[2].second.toInt().toLong())
        Assert.assertEquals((32 - 0).toLong(), frame.getSeries(2)[2].second.toInt().toLong())
        Assert.assertEquals((24 - 1).toLong(), frame.getSeries(3)[2].second.toInt().toLong())
        Assert.assertEquals((3 - 1).toLong(), frame.getSeries(4)[2].second.toInt().toLong())
        Assert.assertEquals((1 - 0).toLong(), frame.getSeries(5)[2].second.toInt().toLong())
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(157, frame.max.toInt().toLong())
    }

    @Test
    fun testDualChangeBars() {
        val result = BindableWrapper<Map<Pair<String, Color>, Triple<Int, Int, Int>>>(emptyMap())
        val frame = dual(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.third }
                        .map { DualBar(it.key.first, it.key.second, it.value.first, it.value.second, """${DIFF.format(it.value.first)}/${DIFF.format(it.value.second)}""") }
                        .toList()
                }
        )
            .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
            Pair("LIB", Color.RED) to Triple(-6, -27, 157),
            Pair("CON", Color.BLUE) to Triple(+4, +22, 121),
            Pair("NDP", Color.ORANGE) to Triple(+1, -20, 24),
            Pair("BQ", Color.CYAN) to Triple(0, +22, 32),
            Pair("GRN", Color.GREEN) to Triple(+1, +2, 3),
            Pair("IND", Color.GRAY) to Triple(0, +1, 1)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        Assert.assertEquals("LIB", frame.getLeftText(0))
        Assert.assertEquals("CON", frame.getLeftText(1))
        Assert.assertEquals("BQ", frame.getLeftText(2))
        Assert.assertEquals("NDP", frame.getLeftText(3))
        Assert.assertEquals("GRN", frame.getLeftText(4))
        Assert.assertEquals("IND", frame.getLeftText(5))
        Assert.assertEquals("-6/-27", frame.getRightText(0))
        Assert.assertEquals("+4/+22", frame.getRightText(1))
        Assert.assertEquals("+0/+22", frame.getRightText(2))
        Assert.assertEquals("+1/-20", frame.getRightText(3))
        Assert.assertEquals("+1/+2", frame.getRightText(4))
        Assert.assertEquals("+0/+1", frame.getRightText(5))
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].first)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].first)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[0].first)
        Assert.assertEquals(Color.ORANGE, frame.getSeries(3)[0].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[0].first)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[0].first)
        Assert.assertEquals(0, frame.getSeries(0)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(1)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(3)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(4)[0].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[0].second.toInt().toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[1].first)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[1].first)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[1].first)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[1].first)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[1].first)
        Assert.assertEquals(-6, frame.getSeries(0)[1].second.toInt().toLong())
        Assert.assertEquals(+4, frame.getSeries(1)[1].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[1].second.toInt().toLong())
        Assert.assertEquals(+1, frame.getSeries(3)[1].second.toInt().toLong())
        Assert.assertEquals(+1, frame.getSeries(4)[1].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[1].second.toInt().toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeries(0)[2].first)
        Assert.assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].first)
        Assert.assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].first)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].first)
        Assert.assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].first)
        Assert.assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].first)
        Assert.assertEquals((-27 - -6).toLong(), frame.getSeries(0)[2].second.toInt().toLong())
        Assert.assertEquals((+22 - +4).toLong(), frame.getSeries(1)[2].second.toInt().toLong())
        Assert.assertEquals((+22 - 0).toLong(), frame.getSeries(2)[2].second.toInt().toLong())
        Assert.assertEquals(-20, frame.getSeries(3)[2].second.toInt().toLong())
        Assert.assertEquals((+2 - +1).toLong(), frame.getSeries(4)[2].second.toInt().toLong())
        Assert.assertEquals((+1 - 0).toLong(), frame.getSeries(5)[2].second.toInt().toLong())
        Assert.assertEquals(-27, frame.min.toInt().toLong())
        Assert.assertEquals(+22, frame.max.toInt().toLong())
    }

    @Test
    fun testDualChangeRangeBars() {
        val result = BindableWrapper<Map<Pair<String, Color>, Triple<Int, Int, Int>>>(emptyMap())
        val frame = dual(
            result.binding
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.third }
                        .map { DualBar(it.key.first, it.key.second, it.value.first, it.value.second, "(${DIFF.format(it.value.first)})-(${DIFF.format(it.value.second)})") }
                        .toList()
                }
        )
            .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
            Pair("LIB", Color.RED) to Triple(-27, -6, 157),
            Pair("CON", Color.BLUE) to Triple(+4, +22, 121),
            Pair("NDP", Color.ORANGE) to Triple(-20, +1, 24),
            Pair("BQ", Color.CYAN) to Triple(0, +22, 32),
            Pair("GRN", Color.GREEN) to Triple(+1, +2, 3),
            Pair("IND", Color.GRAY) to Triple(0, +1, 1)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        Assert.assertEquals("LIB", frame.getLeftText(0))
        Assert.assertEquals("CON", frame.getLeftText(1))
        Assert.assertEquals("BQ", frame.getLeftText(2))
        Assert.assertEquals("NDP", frame.getLeftText(3))
        Assert.assertEquals("GRN", frame.getLeftText(4))
        Assert.assertEquals("IND", frame.getLeftText(5))
        Assert.assertEquals("(-27)-(-6)", frame.getRightText(0))
        Assert.assertEquals("(+4)-(+22)", frame.getRightText(1))
        Assert.assertEquals("(+0)-(+22)", frame.getRightText(2))
        Assert.assertEquals("(-20)-(+1)", frame.getRightText(3))
        Assert.assertEquals("(+1)-(+2)", frame.getRightText(4))
        Assert.assertEquals("(+0)-(+1)", frame.getRightText(5))
        Assert.assertEquals(Color.RED, frame.getSeries(0)[1].first)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[1].first)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[1].first)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].first)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[1].first)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[1].first)
        Assert.assertEquals(-6, frame.getSeries(0)[1].second.toInt().toLong())
        Assert.assertEquals(+4, frame.getSeries(1)[1].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[1].second.toInt().toLong())
        Assert.assertEquals(-20, frame.getSeries(3)[1].second.toInt().toLong())
        Assert.assertEquals(+1, frame.getSeries(4)[1].second.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[1].second.toInt().toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeries(0)[2].first)
        Assert.assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].first)
        Assert.assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].first)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].first)
        Assert.assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].first)
        Assert.assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].first)
        Assert.assertEquals((-27 - -6).toLong(), frame.getSeries(0)[2].second.toInt().toLong())
        Assert.assertEquals((+22 - +4).toLong(), frame.getSeries(1)[2].second.toInt().toLong())
        Assert.assertEquals((+22 - 0).toLong(), frame.getSeries(2)[2].second.toInt().toLong())
        Assert.assertEquals(+1, frame.getSeries(3)[2].second.toInt().toLong())
        Assert.assertEquals((+2 - +1).toLong(), frame.getSeries(4)[2].second.toInt().toLong())
        Assert.assertEquals((+1 - 0).toLong(), frame.getSeries(5)[2].second.toInt().toLong())
        Assert.assertEquals(-27, frame.min.toInt().toLong())
        Assert.assertEquals(+22, frame.max.toInt().toLong())
    }

    @Test
    fun testBasicBars() {
        val regions = listOf(
            BasicBar("East Midlands", Color.BLACK, 5),
            BasicBar("East of England", Color.BLACK, 7),
            BasicBar("London", Color.BLACK, 8),
            BasicBar("North East England", Color.BLACK, 3),
            BasicBar("North West England", Color.BLACK, 8),
            BasicBar("South East England", Color.BLACK, 10),
            BasicBar("South West England", Color.BLACK, 6),
            BasicBar("West Midlands", Color.BLACK, 7),
            BasicBar("Yorkshire and the Humber", Color.BLACK, 6),
            BasicBar("Scotland", Color.BLACK, 6),
            BasicBar("Wales", Color.BLACK, 4),
            BasicBar("Northern Ireland", Color.BLACK, 3)
        )
        val frame = basic(Binding.fixedBinding(regions)).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(12))
        Assert.assertEquals("East Midlands", frame.getLeftText(0))
        Assert.assertEquals("South East England", frame.getLeftText(5))
        Assert.assertEquals("Northern Ireland", frame.getLeftText(11))
        Assert.assertEquals("5", frame.getRightText(0))
        Assert.assertEquals("10", frame.getRightText(5))
        Assert.assertEquals("3", frame.getRightText(11))
        Assert.assertEquals(5, frame.getSeries(0)[0].second)
        Assert.assertEquals(10, frame.getSeries(5)[0].second)
        Assert.assertEquals(3, frame.getSeries(11)[0].second)
    }

    @Test
    fun testDualBarsAllPositive() {
        val regions = listOf(
            DualBar("East Midlands", Color.BLACK, 44, 46, "46 > 44"),
            DualBar("East of England", Color.BLACK, 56, 58, "58 > 56"),
            DualBar("London", Color.BLACK, 68, 73, "73 > 68"),
            DualBar("North East England", Color.BLACK, 26, 29, "29 > 26"),
            DualBar("North West England", Color.BLACK, 68, 75, "75 > 68"),
            DualBar("South East England", Color.BLACK, 83, 84, "84 > 83"),
            DualBar("South West England", Color.BLACK, 53, 55, "55 > 53"),
            DualBar("West Midlands", Color.BLACK, 54, 59, "59 > 54"),
            DualBar("Yorkshire and the Humber", Color.BLACK, 50, 54, "54 > 50"),
            DualBar("Scotland", Color.BLACK, 52, 59, "59 > 52"),
            DualBar("Wales", Color.BLACK, 30, 40, "40 > 30"),
            DualBar("Northern Ireland", Color.BLACK, 16, 18, "18 > 16")
        )
        val frame = dual(Binding.fixedBinding(regions)).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(12))
        Assert.assertEquals("East Midlands", frame.getLeftText(0))
        Assert.assertEquals("South East England", frame.getLeftText(5))
        Assert.assertEquals("Northern Ireland", frame.getLeftText(11))
        Assert.assertEquals("46 > 44", frame.getRightText(0))
        Assert.assertEquals("84 > 83", frame.getRightText(5))
        Assert.assertEquals("18 > 16", frame.getRightText(11))
        Assert.assertEquals(Color.BLACK, frame.getSeries(0)[0].first)
        Assert.assertEquals(Color.BLACK, frame.getSeries(5)[0].first)
        Assert.assertEquals(Color.BLACK, frame.getSeries(11)[0].first)
        Assert.assertEquals(0.0, frame.getSeries(0)[0].second.toDouble(), 0.0)
        Assert.assertEquals(0.0, frame.getSeries(5)[0].second.toDouble(), 0.0)
        Assert.assertEquals(0.0, frame.getSeries(11)[0].second.toDouble(), 0.0)
        Assert.assertEquals(Color.BLACK, frame.getSeries(0)[1].first)
        Assert.assertEquals(Color.BLACK, frame.getSeries(5)[1].first)
        Assert.assertEquals(Color.BLACK, frame.getSeries(11)[1].first)
        Assert.assertEquals(44.0, frame.getSeries(0)[1].second.toDouble(), 0.0)
        Assert.assertEquals(83.0, frame.getSeries(5)[1].second.toDouble(), 0.0)
        Assert.assertEquals(16.0, frame.getSeries(11)[1].second.toDouble(), 0.0)
        Assert.assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(0)[2].first)
        Assert.assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(5)[2].first)
        Assert.assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(11)[2].first)
        Assert.assertEquals(2.0, frame.getSeries(0)[2].second.toDouble(), 0.0)
        Assert.assertEquals(1.0, frame.getSeries(5)[2].second.toDouble(), 0.0)
        Assert.assertEquals(2.0, frame.getSeries(11)[2].second.toDouble(), 0.0)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toDouble() }, IsEqual(84.0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toDouble() }, IsEqual(0.0))
    }

    @Test
    fun testDualVariousCombos() {
        val doAssert = { exp: Pair<Color, Number>, act: Pair<Color, Number> ->
            Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
                .until({ act.first }, IsEqual(exp.first))
            Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
                .until({ act.second.toDouble() }, IsEqual(exp.second.toDouble()))
        }
        val regions = BindableWrapper(listOf(DualBar("", Color.BLACK, 0.0, 0.0, "")))
        val frame = dual(regions.binding).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(1))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 0.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 0.0, 2.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 2.0, 0.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 0.0, -2.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, -2.0, 0.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 1.0, 3.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 3.0, 1.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, -1.0, -3.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, -1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, -3.0, -1.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, -1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, -1.0, +1.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(lighten(Color.BLACK), -1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), +1.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, +1.0, -1.0, ""))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(lighten(Color.BLACK), +1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -1.0), frame.getSeries(0)[2])
    }

    @Test
    fun expandBarSpace() {
        val bars = BindableWrapper<List<BasicBar>>(listOf())
        val minBars = BindableWrapper(0)
        val barFrame = basic(bars.binding).withMinBarCount(minBars.binding).build()
        Assert.assertEquals(0, barFrame.numBars)

        bars.value = listOf(
            BasicBar("JOE BIDEN", Color.BLUE, 306),
            BasicBar("DONALD TRUMP", Color.RED, 232)
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ barFrame.numBars }, IsEqual(2))

        minBars.value = 3
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ barFrame.numBars }, IsEqual(3))

        minBars.value = 1
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ barFrame.numBars }, IsEqual(2))

        bars.value = emptyList()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ barFrame.numBars }, IsEqual(1))
    }

    companion object {
        private val THOUSANDS = DecimalFormat("#,##0")
        private val DIFF = DecimalFormat("+0;-0")
        private val PCT = DecimalFormat("0.0%")
        private fun lighten(color: Color): Color {
            return Color(
                128 + color.red / 2, 128 + color.green / 2, 128 + color.blue / 2
            )
        }
    }
}
