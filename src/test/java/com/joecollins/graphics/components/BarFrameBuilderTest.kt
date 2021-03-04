package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.BarFrameBuilder.Companion.basic
import com.joecollins.graphics.components.BarFrameBuilder.Companion.dual
import com.joecollins.graphics.components.BarFrameBuilder.Companion.dualReversed
import com.joecollins.graphics.components.BarFrameBuilder.DualBar
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.ColorUtils
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.text.DecimalFormat
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.apache.commons.lang3.tuple.Pair
import org.apache.commons.lang3.tuple.Triple
import org.junit.Assert
import org.junit.Test

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
                                    .map { BasicBar(it.key.left, it.key.value, it.value, THOUSANDS.format(it.value)) }
                                    .toList()
                        })
                .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
                ImmutablePair.of("CLINTON", Color.ORANGE) to 2842,
                ImmutablePair.of("SANDERS", Color.GREEN) to 1865)
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("CLINTON", frame.getLeftText(0))
        Assert.assertEquals("SANDERS", frame.getLeftText(1))
        Assert.assertEquals("2,842", frame.getRightText(0))
        Assert.assertEquals("1,865", frame.getRightText(1))
        Assert.assertEquals(Color.ORANGE, frame.getSeries(0)[0].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(1)[0].left)
        Assert.assertEquals(2842, frame.getSeries(0)[0].right)
        Assert.assertEquals(1865, frame.getSeries(1)[0].right)
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
                                    .map { BasicBar(it.key.left, it.key.right, it.value.value, THOUSANDS.format(it.value.value)) }
                                    .toList()
                        })
                .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
                ImmutablePair.of("CLINTON", Color.ORANGE) to Wrapper(2842),
                ImmutablePair.of("SANDERS", Color.GREEN) to Wrapper(1865))
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("CLINTON", frame.getLeftText(0))
        Assert.assertEquals("SANDERS", frame.getLeftText(1))
        Assert.assertEquals("2,842", frame.getRightText(0))
        Assert.assertEquals("1,865", frame.getRightText(1))
        Assert.assertEquals(Color.ORANGE, frame.getSeries(0)[0].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(1)[0].left)
        Assert.assertEquals(2842, frame.getSeries(0)[0].right)
        Assert.assertEquals(1865, frame.getSeries(1)[0].right)
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
                                    .map { BasicBar(it.key.left, it.key.right, it.value, THOUSANDS.format(it.value)) }
                                    .toList()
                        })
                .withMax(max.binding)
                .build()
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(2500, frame.max.toInt().toLong())
        result.value = mapOf(
                ImmutablePair.of("CLINTON", Color.ORANGE) to 2205,
                ImmutablePair.of("SANDERS", Color.GREEN) to 1846)
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(2500, frame.max.toInt().toLong())
        result.value = mapOf(
                ImmutablePair.of("CLINTON", Color.ORANGE) to 2842,
                ImmutablePair.of("SANDERS", Color.GREEN) to 1865)
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(2842, frame.max.toInt().toLong())
        max.value = 3000
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(3000, frame.max.toInt().toLong())
        max.value = 2500
        Assert.assertEquals(0, frame.min.toInt().toLong())
        Assert.assertEquals(2842, frame.max.toInt().toLong())
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
                                    .map { BasicBar(it.key.left, it.key.right, it.value, THOUSANDS.format(it.value)) }
                                    .toList()
                        })
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
        Assert.assertEquals("DEMOCRATIC PRIMARY", frame.header)
        subhead.value = "PLEDGED DELEGATES"
        Assert.assertEquals("PLEDGED DELEGATES", frame.subheadText)
        notes.value = "SOURCE: DNC"
        Assert.assertEquals("SOURCE: DNC", frame.notes)
        borderColor.value = Color.BLUE
        Assert.assertEquals(Color.BLUE, frame.borderColor)
        subheadColor.value = Color.BLUE
        Assert.assertEquals(Color.BLUE, frame.subheadColor)
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
                                    .map { BasicBar(it.key.left, it.key.right, it.value, THOUSANDS.format(it.value)) }
                                    .toList()
                        })
                .withTarget(target.binding) { THOUSANDS.format(it) + " TO WIN" }
                .build()
        Assert.assertEquals(1, frame.numLines.toLong())
        Assert.assertEquals(2382, frame.getLineLevel(0))
        Assert.assertEquals("2,382 TO WIN", frame.getLineLabel(0))
    }

    @Test
    fun testMultiLines() {
        val result = BindableWrapper<Map<Pair<String, Color>, Int>>(emptyMap())
        val lines = BindableList<Int>()
        val frame = basic(
                result.binding
                        .map { map ->
                            map.entries.asSequence()
                                    .sortedByDescending { it.value }
                                    .map { BasicBar(it.key.left, it.key.right, it.value, THOUSANDS.format(it.value)) }
                                    .toList()
                        })
                .withLines(lines) { it.toString() + " QUOTA" + (if (it == 1) "" else "S") }
                .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        lines.addAll(listOf(1, 2, 3, 4, 5))
        Assert.assertEquals(5, frame.numLines.toLong())
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
        val lines = BindableList<Pair<String, Int>>()
        val frame = basic(
                result.binding
                        .map { map ->
                            map.entries.asSequence()
                                    .sortedByDescending { it.value }
                                    .map { BasicBar(it.key.left, it.key.right, it.value, THOUSANDS.format(it.value)) }
                                    .toList()
                        })
                .withLines(lines, { it.left }) { it.right }
                .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        lines.addAll(listOf(
                ImmutablePair.of("The line is here", 1),
                ImmutablePair.of("and here", 2)))
        Assert.assertEquals(2, frame.numLines.toLong())
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
                                    .map { BasicBar(it.key.left, it.key.right, it.value, THOUSANDS.format(it.value)) }
                                    .toList()
                        })
                .withLines(lines.binding) { it.toString() + " QUOTA" + (if (it == 1) "" else "S") }
                .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        lines.value = listOf(1, 2, 3, 4, 5)
        Assert.assertEquals(5, frame.numLines.toLong())
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
                                    .sortedByDescending { it.value.left }
                                    .map { BasicBar(it.key.left, it.key.right, it.value.left, THOUSANDS.format(it.value.left), if (it.value.right) shape else null) }
                                    .toList()
                        })
                .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
                ImmutablePair.of("CLINTON", Color.ORANGE) to ImmutablePair.of(2842, true),
                ImmutablePair.of("SANDERS", Color.GREEN) to ImmutablePair.of(1865, false))
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("CLINTON", frame.getLeftText(0))
        Assert.assertEquals("SANDERS", frame.getLeftText(1))
        Assert.assertEquals("2,842", frame.getRightText(0))
        Assert.assertEquals("1,865", frame.getRightText(1))
        Assert.assertEquals(Color.ORANGE, frame.getSeries(0)[0].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(1)[0].left)
        Assert.assertEquals(2842, frame.getSeries(0)[0].right)
        Assert.assertEquals(1865, frame.getSeries(1)[0].right)
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
                                    .sortedByDescending { it.value.left }
                                    .map { BasicBar(it.key.left, it.key.right, it.value.right, DIFF.format(it.value.right)) }
                                    .toList()
                        })
                .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
                ImmutablePair.of("LIB", Color.RED) to ImmutablePair.of(157, -27),
                ImmutablePair.of("CON", Color.BLUE) to ImmutablePair.of(121, +22),
                ImmutablePair.of("NDP", Color.ORANGE) to ImmutablePair.of(24, -20),
                ImmutablePair.of("BQ", Color.CYAN) to ImmutablePair.of(32, +22),
                ImmutablePair.of("GRN", Color.GREEN) to ImmutablePair.of(3, +2),
                ImmutablePair.of("IND", Color.GRAY) to ImmutablePair.of(1, +1))
        Assert.assertEquals(6, frame.numBars.toLong())
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
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].left)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].left)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[0].left)
        Assert.assertEquals(Color.ORANGE, frame.getSeries(3)[0].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[0].left)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[0].left)
        Assert.assertEquals(-27, frame.getSeries(0)[0].right)
        Assert.assertEquals(+22, frame.getSeries(1)[0].right)
        Assert.assertEquals(+22, frame.getSeries(2)[0].right)
        Assert.assertEquals(-20, frame.getSeries(3)[0].right)
        Assert.assertEquals(+2, frame.getSeries(4)[0].right)
        Assert.assertEquals(+1, frame.getSeries(5)[0].right)
        Assert.assertEquals(-27, frame.min.toInt().toLong())
        Assert.assertEquals(+22, frame.max.toInt().toLong())
    }

    @Test
    fun testSimpleDiffWingspan() {
        val result = BindableWrapper<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val range = BindableWrapper(10)
        val frame = basic(
                result.binding
                        .map { map ->
                            map.entries.asSequence()
                                    .sortedByDescending { it.value.left }
                                    .map { BasicBar(it.key.left, it.key.right, it.value.right, DIFF.format(it.value.right)) }
                                    .toList()
                        })
                .withWingspan(range.binding)
                .build()
        Assert.assertEquals(-10, frame.min.toInt().toLong())
        Assert.assertEquals(+10, frame.max.toInt().toLong())
        result.value = mapOf(
                ImmutablePair.of("LIB", Color.RED) to ImmutablePair.of(157, -27),
                ImmutablePair.of("CON", Color.BLUE) to ImmutablePair.of(121, +22),
                ImmutablePair.of("NDP", Color.ORANGE) to ImmutablePair.of(24, -20),
                ImmutablePair.of("BQ", Color.CYAN) to ImmutablePair.of(32, +22),
                ImmutablePair.of("GRN", Color.GREEN) to ImmutablePair.of(3, +2),
                ImmutablePair.of("IND", Color.GRAY) to ImmutablePair.of(1, +1))
        Assert.assertEquals(-27, frame.min.toInt().toLong())
        Assert.assertEquals(+27, frame.max.toInt().toLong())
    }

    @Test
    fun testDualValueBars() {
        val result = BindableWrapper<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val frame = dual(
                result
                        .binding
                        .map { map ->
                            map.entries.asSequence()
                                    .sortedByDescending { it.value.right }
                                    .map { DualBar(it.key.left, it.key.right, it.value.left, it.value.right, it.value.left.toString() + "/" + it.value.right) }
                                    .toList()
                        })
                .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
                ImmutablePair.of("LIBERAL", Color.RED) to ImmutablePair.of(26, 157),
                ImmutablePair.of("CONSERVATIVE", Color.BLUE) to ImmutablePair.of(4, 121),
                ImmutablePair.of("NEW DEMOCRATIC PARTY", Color.ORANGE) to ImmutablePair.of(1, 24),
                ImmutablePair.of("BLOC QU\u00c9B\u00c9COIS", Color.CYAN) to ImmutablePair.of(0, 32),
                ImmutablePair.of("GREEN", Color.GREEN) to ImmutablePair.of(1, 3),
                ImmutablePair.of("INDEPENDENT", Color.GRAY) to ImmutablePair.of(0, 1))
        Assert.assertEquals(6, frame.numBars.toLong())
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
        Assert.assertEquals(0, frame.getSeries(0)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(1)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(3)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(4)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[0].right.toInt().toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[1].left)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[1].left)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[1].left)
        Assert.assertEquals(Color.ORANGE, frame.getSeries(3)[1].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[1].left)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[1].left)
        Assert.assertEquals(26, frame.getSeries(0)[1].right.toInt().toLong())
        Assert.assertEquals(4, frame.getSeries(1)[1].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[1].right.toInt().toLong())
        Assert.assertEquals(1, frame.getSeries(3)[1].right.toInt().toLong())
        Assert.assertEquals(1, frame.getSeries(4)[1].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[1].right.toInt().toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeries(0)[2].left)
        Assert.assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].left)
        Assert.assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].left)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].left)
        Assert.assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].left)
        Assert.assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].left)
        Assert.assertEquals((157 - 26).toLong(), frame.getSeries(0)[2].right.toInt().toLong())
        Assert.assertEquals((121 - 4).toLong(), frame.getSeries(1)[2].right.toInt().toLong())
        Assert.assertEquals((32 - 0).toLong(), frame.getSeries(2)[2].right.toInt().toLong())
        Assert.assertEquals((24 - 1).toLong(), frame.getSeries(3)[2].right.toInt().toLong())
        Assert.assertEquals((3 - 1).toLong(), frame.getSeries(4)[2].right.toInt().toLong())
        Assert.assertEquals((1 - 0).toLong(), frame.getSeries(5)[2].right.toInt().toLong())
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
                                    .sortedByDescending { it.value.right }
                                    .map { DualBar(it.key.left, it.key.right, it.value.left, it.value.right, it.value.left.toString() + "/" + it.value.right) }
                                    .toList()
                        })
                .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
                ImmutablePair.of("LIBERAL", Color.RED) to ImmutablePair.of(26, 157),
                ImmutablePair.of("CONSERVATIVE", Color.BLUE) to ImmutablePair.of(4, 121),
                ImmutablePair.of("NEW DEMOCRATIC PARTY", Color.ORANGE) to ImmutablePair.of(1, 24),
                ImmutablePair.of("BLOC QU\u00c9B\u00c9COIS", Color.CYAN) to ImmutablePair.of(0, 32),
                ImmutablePair.of("GREEN", Color.GREEN) to ImmutablePair.of(1, 3),
                ImmutablePair.of("INDEPENDENT", Color.GRAY) to ImmutablePair.of(0, 1))
        Assert.assertEquals(6, frame.numBars.toLong())
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
        Assert.assertEquals(0, frame.getSeries(0)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(1)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(3)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(4)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[0].right.toInt().toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeries(0)[1].left)
        Assert.assertEquals(lighten(Color.BLUE), frame.getSeries(1)[1].left)
        Assert.assertEquals(lighten(Color.CYAN), frame.getSeries(2)[1].left)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].left)
        Assert.assertEquals(lighten(Color.GREEN), frame.getSeries(4)[1].left)
        Assert.assertEquals(lighten(Color.GRAY), frame.getSeries(5)[1].left)
        Assert.assertEquals(26, frame.getSeries(0)[1].right.toInt().toLong())
        Assert.assertEquals(4, frame.getSeries(1)[1].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[1].right.toInt().toLong())
        Assert.assertEquals(1, frame.getSeries(3)[1].right.toInt().toLong())
        Assert.assertEquals(1, frame.getSeries(4)[1].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[1].right.toInt().toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[2].left)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[2].left)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[2].left)
        Assert.assertEquals(Color.ORANGE, frame.getSeries(3)[2].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[2].left)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[2].left)
        Assert.assertEquals((157 - 26).toLong(), frame.getSeries(0)[2].right.toInt().toLong())
        Assert.assertEquals((121 - 4).toLong(), frame.getSeries(1)[2].right.toInt().toLong())
        Assert.assertEquals((32 - 0).toLong(), frame.getSeries(2)[2].right.toInt().toLong())
        Assert.assertEquals((24 - 1).toLong(), frame.getSeries(3)[2].right.toInt().toLong())
        Assert.assertEquals((3 - 1).toLong(), frame.getSeries(4)[2].right.toInt().toLong())
        Assert.assertEquals((1 - 0).toLong(), frame.getSeries(5)[2].right.toInt().toLong())
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
                                    .sortedByDescending { it.value.right }
                                    .map { DualBar(it.key.left, it.key.right, it.value.left, it.value.middle, """${DIFF.format(it.value.left)}/${DIFF.format(it.value.middle)}""") }
                                    .toList()
                        })
                .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
                ImmutablePair.of("LIB", Color.RED) to ImmutableTriple.of(-6, -27, 157),
                ImmutablePair.of("CON", Color.BLUE) to ImmutableTriple.of(+4, +22, 121),
                ImmutablePair.of("NDP", Color.ORANGE) to ImmutableTriple.of(+1, -20, 24),
                ImmutablePair.of("BQ", Color.CYAN) to ImmutableTriple.of(0, +22, 32),
                ImmutablePair.of("GRN", Color.GREEN) to ImmutableTriple.of(+1, +2, 3),
                ImmutablePair.of("IND", Color.GRAY) to ImmutableTriple.of(0, +1, 1))
        Assert.assertEquals(6, frame.numBars.toLong())
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
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].left)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].left)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[0].left)
        Assert.assertEquals(Color.ORANGE, frame.getSeries(3)[0].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[0].left)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[0].left)
        Assert.assertEquals(0, frame.getSeries(0)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(1)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(3)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(4)[0].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[0].right.toInt().toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[1].left)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[1].left)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[1].left)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[1].left)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[1].left)
        Assert.assertEquals(-6, frame.getSeries(0)[1].right.toInt().toLong())
        Assert.assertEquals(+4, frame.getSeries(1)[1].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[1].right.toInt().toLong())
        Assert.assertEquals(+1, frame.getSeries(3)[1].right.toInt().toLong())
        Assert.assertEquals(+1, frame.getSeries(4)[1].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[1].right.toInt().toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeries(0)[2].left)
        Assert.assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].left)
        Assert.assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].left)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].left)
        Assert.assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].left)
        Assert.assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].left)
        Assert.assertEquals((-27 - -6).toLong(), frame.getSeries(0)[2].right.toInt().toLong())
        Assert.assertEquals((+22 - +4).toLong(), frame.getSeries(1)[2].right.toInt().toLong())
        Assert.assertEquals((+22 - 0).toLong(), frame.getSeries(2)[2].right.toInt().toLong())
        Assert.assertEquals(-20, frame.getSeries(3)[2].right.toInt().toLong())
        Assert.assertEquals((+2 - +1).toLong(), frame.getSeries(4)[2].right.toInt().toLong())
        Assert.assertEquals((+1 - 0).toLong(), frame.getSeries(5)[2].right.toInt().toLong())
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
                                    .sortedByDescending { it.value.right }
                                    .map { DualBar(it.key.left, it.key.right, it.value.left, it.value.middle, "(${DIFF.format(it.value.left)})-(${DIFF.format(it.value.middle)})") }
                                    .toList()
                        })
                .build()
        Assert.assertEquals(0, frame.numBars.toLong())
        Assert.assertEquals(0, frame.numLines.toLong())
        result.value = mapOf(
                ImmutablePair.of("LIB", Color.RED) to ImmutableTriple.of(-27, -6, 157),
                ImmutablePair.of("CON", Color.BLUE) to ImmutableTriple.of(+4, +22, 121),
                ImmutablePair.of("NDP", Color.ORANGE) to ImmutableTriple.of(-20, +1, 24),
                ImmutablePair.of("BQ", Color.CYAN) to ImmutableTriple.of(0, +22, 32),
                ImmutablePair.of("GRN", Color.GREEN) to ImmutableTriple.of(+1, +2, 3),
                ImmutablePair.of("IND", Color.GRAY) to ImmutableTriple.of(0, +1, 1))
        Assert.assertEquals(6, frame.numBars.toLong())
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
        Assert.assertEquals(Color.RED, frame.getSeries(0)[1].left)
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[1].left)
        Assert.assertEquals(Color.CYAN, frame.getSeries(2)[1].left)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].left)
        Assert.assertEquals(Color.GREEN, frame.getSeries(4)[1].left)
        Assert.assertEquals(Color.GRAY, frame.getSeries(5)[1].left)
        Assert.assertEquals(-6, frame.getSeries(0)[1].right.toInt().toLong())
        Assert.assertEquals(+4, frame.getSeries(1)[1].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(2)[1].right.toInt().toLong())
        Assert.assertEquals(-20, frame.getSeries(3)[1].right.toInt().toLong())
        Assert.assertEquals(+1, frame.getSeries(4)[1].right.toInt().toLong())
        Assert.assertEquals(0, frame.getSeries(5)[1].right.toInt().toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeries(0)[2].left)
        Assert.assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].left)
        Assert.assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].left)
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].left)
        Assert.assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].left)
        Assert.assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].left)
        Assert.assertEquals((-27 - -6).toLong(), frame.getSeries(0)[2].right.toInt().toLong())
        Assert.assertEquals((+22 - +4).toLong(), frame.getSeries(1)[2].right.toInt().toLong())
        Assert.assertEquals((+22 - 0).toLong(), frame.getSeries(2)[2].right.toInt().toLong())
        Assert.assertEquals(+1, frame.getSeries(3)[2].right.toInt().toLong())
        Assert.assertEquals((+2 - +1).toLong(), frame.getSeries(4)[2].right.toInt().toLong())
        Assert.assertEquals((+1 - 0).toLong(), frame.getSeries(5)[2].right.toInt().toLong())
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
                BasicBar("Northern Ireland", Color.BLACK, 3))
        val frame = basic(Binding.fixedBinding(regions)).build()
        Assert.assertEquals(12, frame.numBars.toLong())
        Assert.assertEquals("East Midlands", frame.getLeftText(0))
        Assert.assertEquals("South East England", frame.getLeftText(5))
        Assert.assertEquals("Northern Ireland", frame.getLeftText(11))
        Assert.assertEquals("5", frame.getRightText(0))
        Assert.assertEquals("10", frame.getRightText(5))
        Assert.assertEquals("3", frame.getRightText(11))
        Assert.assertEquals(5, frame.getSeries(0)[0].right)
        Assert.assertEquals(10, frame.getSeries(5)[0].right)
        Assert.assertEquals(3, frame.getSeries(11)[0].right)
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
                DualBar("Northern Ireland", Color.BLACK, 16, 18, "18 > 16"))
        val frame = dual(Binding.fixedBinding(regions)).build()
        Assert.assertEquals(12, frame.numBars.toLong())
        Assert.assertEquals("East Midlands", frame.getLeftText(0))
        Assert.assertEquals("South East England", frame.getLeftText(5))
        Assert.assertEquals("Northern Ireland", frame.getLeftText(11))
        Assert.assertEquals("46 > 44", frame.getRightText(0))
        Assert.assertEquals("84 > 83", frame.getRightText(5))
        Assert.assertEquals("18 > 16", frame.getRightText(11))
        Assert.assertEquals(Color.BLACK, frame.getSeries(0)[0].left)
        Assert.assertEquals(Color.BLACK, frame.getSeries(5)[0].left)
        Assert.assertEquals(Color.BLACK, frame.getSeries(11)[0].left)
        Assert.assertEquals(0.0, frame.getSeries(0)[0].right.toDouble(), 0.0)
        Assert.assertEquals(0.0, frame.getSeries(5)[0].right.toDouble(), 0.0)
        Assert.assertEquals(0.0, frame.getSeries(11)[0].right.toDouble(), 0.0)
        Assert.assertEquals(Color.BLACK, frame.getSeries(0)[1].left)
        Assert.assertEquals(Color.BLACK, frame.getSeries(5)[1].left)
        Assert.assertEquals(Color.BLACK, frame.getSeries(11)[1].left)
        Assert.assertEquals(44.0, frame.getSeries(0)[1].right.toDouble(), 0.0)
        Assert.assertEquals(83.0, frame.getSeries(5)[1].right.toDouble(), 0.0)
        Assert.assertEquals(16.0, frame.getSeries(11)[1].right.toDouble(), 0.0)
        Assert.assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(0)[2].left)
        Assert.assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(5)[2].left)
        Assert.assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(11)[2].left)
        Assert.assertEquals(2.0, frame.getSeries(0)[2].right.toDouble(), 0.0)
        Assert.assertEquals(1.0, frame.getSeries(5)[2].right.toDouble(), 0.0)
        Assert.assertEquals(2.0, frame.getSeries(11)[2].right.toDouble(), 0.0)
        Assert.assertEquals(84.0, frame.max.toDouble(), 0.0)
        Assert.assertEquals(0.0, frame.min.toDouble(), 0.0)
    }

    @Test
    fun testDualVariousCombos() {
        val doAssert = { exp: Pair<Color, Number>, act: Pair<Color, Number> ->
            Assert.assertEquals(exp.left, act.left)
            Assert.assertEquals(exp.right.toDouble(), act.right.toDouble(), 0.0)
        }
        val regions = BindableWrapper(listOf(DualBar("", Color.BLACK, 0.0, 0.0, "")))
        val frame = dual(regions.binding).build()
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), 0.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 0.0, 2.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 2.0, 0.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 0.0, -2.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, -2.0, 0.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 1.0, 3.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, 1.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, 3.0, 1.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, 1.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, -1.0, -3.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, -1.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, -3.0, -1.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(Color.BLACK, -1.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, -1.0, +1.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), -1.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), +1.0), frame.getSeries(0)[2])
        regions.value = listOf(DualBar("", Color.BLACK, +1.0, -1.0, ""))
        doAssert(ImmutablePair.of(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), +1.0), frame.getSeries(0)[1])
        doAssert(ImmutablePair.of(lighten(Color.BLACK), -1.0), frame.getSeries(0)[2])
    }

    companion object {
        private val THOUSANDS = DecimalFormat("#,##0")
        private val DIFF = DecimalFormat("+0;-0")
        private val PCT = DecimalFormat("0.0%")
        private fun lighten(color: Color): Color {
            return Color(
                    128 + color.red / 2, 128 + color.green / 2, 128 + color.blue / 2)
        }
    }
}
