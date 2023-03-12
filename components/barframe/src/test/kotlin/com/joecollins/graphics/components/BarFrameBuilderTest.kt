package com.joecollins.graphics.components

import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.BarFrameBuilder.Companion.basic
import com.joecollins.graphics.components.BarFrameBuilder.Companion.dual
import com.joecollins.graphics.components.BarFrameBuilder.Companion.dualReversed
import com.joecollins.graphics.components.BarFrameBuilder.DualBar
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.text.DecimalFormat

class BarFrameBuilderTest {
    private class Wrapper<T>(val value: T)

    @Test
    fun testSimpleBars() {
        val result = Publisher<Map<Pair<String, Color>, Int>>(emptyMap())
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                },
        )
            .build()
        assertEquals(0, frame.numBars.toLong())
        assertEquals(0, frame.numLines.toLong())
        result.submit(
            mapOf(
                Pair("CLINTON", Color.ORANGE) to 2842,
                Pair("SANDERS", Color.GREEN) to 1865,
            ),
        )
        assertEquals(2, frame.numBars)
        assertEquals("CLINTON", frame.getLeftText(0))
        assertEquals("SANDERS", frame.getLeftText(1))
        assertEquals("2,842", frame.getRightText(0))
        assertEquals("1,865", frame.getRightText(1))
        assertEquals(Color.ORANGE, frame.getSeries(0)[0].first)
        assertEquals(Color.GREEN, frame.getSeries(1)[0].first)
        assertEquals(2842, frame.getSeries(0)[0].second)
        assertEquals(1865, frame.getSeries(1)[0].second)
        assertEquals(0, frame.min.toInt().toLong())
        assertEquals(2842, frame.max.toInt().toLong())
    }

    @Test
    fun testSimpleBarsWithValueObject() {
        val result = Publisher<Map<Pair<String, Color>, Wrapper<Int>>>(emptyMap())
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value.value, THOUSANDS.format(it.value.value)) }
                        .toList()
                },
        )
            .build()
        assertEquals(0, frame.numBars.toLong())
        assertEquals(0, frame.numLines.toLong())
        result.submit(
            mapOf(
                Pair("CLINTON", Color.ORANGE) to Wrapper(2842),
                Pair("SANDERS", Color.GREEN) to Wrapper(1865),
            ),
        )
        assertEquals(2, frame.numBars)
        assertEquals("CLINTON", frame.getLeftText(0))
        assertEquals("SANDERS", frame.getLeftText(1))
        assertEquals("2,842", frame.getRightText(0))
        assertEquals("1,865", frame.getRightText(1))
        assertEquals(Color.ORANGE, frame.getSeries(0)[0].first)
        assertEquals(Color.GREEN, frame.getSeries(1)[0].first)
        assertEquals(2842, frame.getSeries(0)[0].second)
        assertEquals(1865, frame.getSeries(1)[0].second)
        assertEquals(0, frame.min.toInt().toLong())
        assertEquals(2842, frame.max.toInt().toLong())
    }

    @Test
    fun testSimpleBarsRange() {
        val result = Publisher<Map<Pair<String, Color>, Int>>(emptyMap())
        val max = Publisher(2500)
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                },
        )
            .withMax(max)
            .build()
        assertEquals(0, frame.min.toInt())
        assertEquals(2500, frame.max.toInt())
        result.submit(
            mapOf(
                Pair("CLINTON", Color.ORANGE) to 2205,
                Pair("SANDERS", Color.GREEN) to 1846,
            ),
        )
        assertEquals(0, frame.min.toInt())
        assertEquals(2500, frame.max.toInt())
        result.submit(
            mapOf(
                Pair("CLINTON", Color.ORANGE) to 2842,
                Pair("SANDERS", Color.GREEN) to 1865,
            ),
        )
        assertEquals(0, frame.min.toInt())
        assertEquals(2842, frame.max.toInt())
        max.submit(3000)
        assertEquals(0, frame.min.toInt())
        assertEquals(3000, frame.max.toInt())
        max.submit(2500)
        assertEquals(0, frame.min.toInt())
        assertEquals(2842, frame.max.toInt())
    }

    @Test
    fun testHeaderSubheadAndNotes() {
        val result = Publisher<Map<Pair<String, Color>, Int>>(emptyMap())
        val header = Publisher<String?>("HEADER")
        val subhead = Publisher<String?>("SUBHEAD")
        val notes = Publisher<String?>("NOTES")
        val borderColor = Publisher(Color.BLACK)
        val subheadColor = Publisher(Color.GRAY)
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                },
        )
            .withHeader(header)
            .withSubhead(subhead)
            .withNotes(notes)
            .withBorder(borderColor)
            .withSubheadColor(subheadColor)
            .build()
        assertEquals("HEADER", frame.header)
        assertEquals("SUBHEAD", frame.subheadText)
        assertEquals("NOTES", frame.notes)
        assertEquals(Color.BLACK, frame.borderColor)
        assertEquals(Color.GRAY, frame.subheadColor)

        header.submit("DEMOCRATIC PRIMARY")
        assertEquals("DEMOCRATIC PRIMARY", frame.header)

        subhead.submit("PLEDGED DELEGATES")
        assertEquals("PLEDGED DELEGATES", frame.subheadText)

        notes.submit("SOURCE: DNC")
        assertEquals("SOURCE: DNC", frame.notes)

        borderColor.submit(Color.BLUE)
        assertEquals(Color.BLUE, frame.borderColor)

        subheadColor.submit(Color.BLUE)
        assertEquals(Color.BLUE, frame.subheadColor)
    }

    @Test
    fun testTarget() {
        val result = Publisher<Map<Pair<String, Color>, Int>>(emptyMap())
        val target = Publisher(2382)
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                },
        )
            .withTarget(target) { THOUSANDS.format(it) + " TO WIN" }
            .build()
        assertEquals(1, frame.numLines)
        assertEquals(2382, frame.getLineLevel(0))
        assertEquals("2,382 TO WIN", frame.getLineLabel(0))
    }

    @Test
    fun testMultiLines() {
        val result = Publisher<Map<Pair<String, Color>, Int>>(emptyMap())
        val lines = Publisher<List<Int>>(emptyList())
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                },
        )
            .withLines(lines) { it.toString() + " QUOTA" + (if (it == 1) "" else "S") }
            .build()
        assertEquals(0, frame.numLines.toLong())
        lines.submit(listOf(1, 2, 3, 4, 5))
        assertEquals(5, frame.numLines)
        assertEquals(1, frame.getLineLevel(0))
        assertEquals(2, frame.getLineLevel(1))
        assertEquals(3, frame.getLineLevel(2))
        assertEquals(4, frame.getLineLevel(3))
        assertEquals(5, frame.getLineLevel(4))
        assertEquals("1 QUOTA", frame.getLineLabel(0))
        assertEquals("2 QUOTAS", frame.getLineLabel(1))
        assertEquals("3 QUOTAS", frame.getLineLabel(2))
        assertEquals("4 QUOTAS", frame.getLineLabel(3))
        assertEquals("5 QUOTAS", frame.getLineLabel(4))
    }

    @Test
    fun testMultiLinesBespokeLabels() {
        val result = Publisher<Map<Pair<String, Color>, Int>>(emptyMap())
        val lines = Publisher<List<Pair<String, Int>>>(emptyList())
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                },
        )
            .withLines(lines, { it.first }) { it.second }
            .build()
        assertEquals(0, frame.numLines.toLong())
        lines.submit(
            listOf(
                Pair("The line is here", 1),
                Pair("and here", 2),
            ),
        )
        assertEquals(2, frame.numLines)
        assertEquals(1, frame.getLineLevel(0))
        assertEquals(2, frame.getLineLevel(1))
        assertEquals("The line is here", frame.getLineLabel(0))
        assertEquals("and here", frame.getLineLabel(1))
    }

    @Test
    fun testMultiLinesBinding() {
        val result = Publisher<Map<Pair<String, Color>, Int>>(emptyMap())
        val lines = Publisher(listOf<Int>())
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value }
                        .map { BasicBar(it.key.first, it.key.second, it.value, THOUSANDS.format(it.value)) }
                        .toList()
                },
        )
            .withLines(lines) { it.toString() + " QUOTA" + (if (it == 1) "" else "S") }
            .build()
        assertEquals(0, frame.numLines.toLong())
        lines.submit(listOf(1, 2, 3, 4, 5))
        assertEquals(5, frame.numLines)
        assertEquals(1, frame.getLineLevel(0))
        assertEquals(2, frame.getLineLevel(1))
        assertEquals(3, frame.getLineLevel(2))
        assertEquals(4, frame.getLineLevel(3))
        assertEquals(5, frame.getLineLevel(4))
        assertEquals("1 QUOTA", frame.getLineLabel(0))
        assertEquals("2 QUOTAS", frame.getLineLabel(1))
        assertEquals("3 QUOTAS", frame.getLineLabel(2))
        assertEquals("4 QUOTAS", frame.getLineLabel(3))
        assertEquals("5 QUOTAS", frame.getLineLabel(4))
    }

    @Test
    fun testLeftShape() {
        val result = Publisher<Map<Pair<String, Color>, Pair<Int, Boolean>>>(emptyMap())
        val shape = Rectangle2D.Double(0.0, 0.0, 1.0, 1.0)
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.first }
                        .map { BasicBar(it.key.first, it.key.second, it.value.first, THOUSANDS.format(it.value.first), if (it.value.second) shape else null) }
                        .toList()
                },
        )
            .build()
        assertEquals(0, frame.numBars.toLong())
        assertEquals(0, frame.numLines.toLong())
        result.submit(
            mapOf(
                Pair("CLINTON", Color.ORANGE) to Pair(2842, true),
                Pair("SANDERS", Color.GREEN) to Pair(1865, false),
            ),
        )
        assertEquals(2, frame.numBars)
        assertEquals("CLINTON", frame.getLeftText(0))
        assertEquals("SANDERS", frame.getLeftText(1))
        assertEquals("2,842", frame.getRightText(0))
        assertEquals("1,865", frame.getRightText(1))
        assertEquals(Color.ORANGE, frame.getSeries(0)[0].first)
        assertEquals(Color.GREEN, frame.getSeries(1)[0].first)
        assertEquals(2842, frame.getSeries(0)[0].second)
        assertEquals(1865, frame.getSeries(1)[0].second)
        assertEquals(shape, frame.getLeftIcon(0))
        Assertions.assertNull(frame.getLeftIcon(1))
    }

    @Test
    fun testSimpleDiffBars() {
        val result = Publisher<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.first }
                        .map { BasicBar(it.key.first, it.key.second, it.value.second, DIFF.format(it.value.second)) }
                        .toList()
                },
        )
            .build()
        assertEquals(0, frame.numBars.toLong())
        assertEquals(0, frame.numLines.toLong())
        result.submit(
            mapOf(
                Pair("LIB", Color.RED) to Pair(157, -27),
                Pair("CON", Color.BLUE) to Pair(121, +22),
                Pair("NDP", Color.ORANGE) to Pair(24, -20),
                Pair("BQ", Color.CYAN) to Pair(32, +22),
                Pair("GRN", Color.GREEN) to Pair(3, +2),
                Pair("IND", Color.GRAY) to Pair(1, +1),
            ),
        )
        assertEquals(6, frame.numBars)
        assertEquals("LIB", frame.getLeftText(0))
        assertEquals("CON", frame.getLeftText(1))
        assertEquals("BQ", frame.getLeftText(2))
        assertEquals("NDP", frame.getLeftText(3))
        assertEquals("GRN", frame.getLeftText(4))
        assertEquals("IND", frame.getLeftText(5))
        assertEquals("-27", frame.getRightText(0))
        assertEquals("+22", frame.getRightText(1))
        assertEquals("+22", frame.getRightText(2))
        assertEquals("-20", frame.getRightText(3))
        assertEquals("+2", frame.getRightText(4))
        assertEquals("+1", frame.getRightText(5))
        assertEquals(Color.RED, frame.getSeries(0)[0].first)
        assertEquals(Color.BLUE, frame.getSeries(1)[0].first)
        assertEquals(Color.CYAN, frame.getSeries(2)[0].first)
        assertEquals(Color.ORANGE, frame.getSeries(3)[0].first)
        assertEquals(Color.GREEN, frame.getSeries(4)[0].first)
        assertEquals(Color.GRAY, frame.getSeries(5)[0].first)
        assertEquals(-27, frame.getSeries(0)[0].second)
        assertEquals(+22, frame.getSeries(1)[0].second)
        assertEquals(+22, frame.getSeries(2)[0].second)
        assertEquals(-20, frame.getSeries(3)[0].second)
        assertEquals(+2, frame.getSeries(4)[0].second)
        assertEquals(+1, frame.getSeries(5)[0].second)
        assertEquals(-27, frame.min.toInt())
        assertEquals(22, frame.max.toInt())
    }

    @Test
    fun testSimpleDiffWingspan() {
        val result = Publisher<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val range = Publisher(10)
        val frame = basic(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.first }
                        .map { BasicBar(it.key.first, it.key.second, it.value.second, DIFF.format(it.value.second)) }
                        .toList()
                },
        )
            .withWingspan(range)
            .build()
        assertEquals(10.0, frame.max.toDouble(), 1e-6)
        assertEquals(-10.0, frame.min.toDouble(), 1e-6)

        result.submit(
            mapOf(
                Pair("LIB", Color.RED) to Pair(157, -27),
                Pair("CON", Color.BLUE) to Pair(121, +22),
                Pair("NDP", Color.ORANGE) to Pair(24, -20),
                Pair("BQ", Color.CYAN) to Pair(32, +22),
                Pair("GRN", Color.GREEN) to Pair(3, +2),
                Pair("IND", Color.GRAY) to Pair(1, +1),
            ),
        )
        assertEquals(27.0, frame.max.toDouble(), 1e-6)
        assertEquals(-27.0, frame.min.toDouble(), 1e-6)
    }

    @Test
    fun testDualValueBars() {
        val result = Publisher<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val frame = dual(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.second }
                        .map { DualBar(it.key.first, it.key.second, it.value.first, it.value.second, it.value.first.toString() + "/" + it.value.second) }
                        .toList()
                },
        )
            .build()
        assertEquals(0, frame.numBars.toLong())
        assertEquals(0, frame.numLines.toLong())
        result.submit(
            mapOf(
                Pair("LIBERAL", Color.RED) to Pair(26, 157),
                Pair("CONSERVATIVE", Color.BLUE) to Pair(4, 121),
                Pair("NEW DEMOCRATIC PARTY", Color.ORANGE) to Pair(1, 24),
                Pair("BLOC QU\u00c9B\u00c9COIS", Color.CYAN) to Pair(0, 32),
                Pair("GREEN", Color.GREEN) to Pair(1, 3),
                Pair("INDEPENDENT", Color.GRAY) to Pair(0, 1),
            ),
        )
        assertEquals(6, frame.numBars)
        assertEquals("LIBERAL", frame.getLeftText(0))
        assertEquals("CONSERVATIVE", frame.getLeftText(1))
        assertEquals("BLOC QU\u00c9B\u00c9COIS", frame.getLeftText(2))
        assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3))
        assertEquals("GREEN", frame.getLeftText(4))
        assertEquals("INDEPENDENT", frame.getLeftText(5))
        assertEquals("26/157", frame.getRightText(0))
        assertEquals("4/121", frame.getRightText(1))
        assertEquals("0/32", frame.getRightText(2))
        assertEquals("1/24", frame.getRightText(3))
        assertEquals("1/3", frame.getRightText(4))
        assertEquals("0/1", frame.getRightText(5))
        assertEquals(0, frame.getSeries(0)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(1)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(2)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(3)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(4)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(5)[0].second.toInt().toLong())
        assertEquals(Color.RED, frame.getSeries(0)[1].first)
        assertEquals(Color.BLUE, frame.getSeries(1)[1].first)
        assertEquals(Color.CYAN, frame.getSeries(2)[1].first)
        assertEquals(Color.ORANGE, frame.getSeries(3)[1].first)
        assertEquals(Color.GREEN, frame.getSeries(4)[1].first)
        assertEquals(Color.GRAY, frame.getSeries(5)[1].first)
        assertEquals(26, frame.getSeries(0)[1].second.toInt().toLong())
        assertEquals(4, frame.getSeries(1)[1].second.toInt().toLong())
        assertEquals(0, frame.getSeries(2)[1].second.toInt().toLong())
        assertEquals(1, frame.getSeries(3)[1].second.toInt().toLong())
        assertEquals(1, frame.getSeries(4)[1].second.toInt().toLong())
        assertEquals(0, frame.getSeries(5)[1].second.toInt().toLong())
        assertEquals(lighten(Color.RED), frame.getSeries(0)[2].first)
        assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].first)
        assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].first)
        assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].first)
        assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].first)
        assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].first)
        assertEquals((157 - 26).toLong(), frame.getSeries(0)[2].second.toInt().toLong())
        assertEquals((121 - 4).toLong(), frame.getSeries(1)[2].second.toInt().toLong())
        assertEquals((32 - 0).toLong(), frame.getSeries(2)[2].second.toInt().toLong())
        assertEquals((24 - 1).toLong(), frame.getSeries(3)[2].second.toInt().toLong())
        assertEquals((3 - 1).toLong(), frame.getSeries(4)[2].second.toInt().toLong())
        assertEquals((1 - 0).toLong(), frame.getSeries(5)[2].second.toInt().toLong())
        assertEquals(0, frame.min.toInt().toLong())
        assertEquals(157, frame.max.toInt().toLong())
    }

    @Test
    fun testDualReversedValueBars() {
        val result = Publisher<Map<Pair<String, Color>, Pair<Int, Int>>>(emptyMap())
        val frame = dualReversed(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.second }
                        .map { DualBar(it.key.first, it.key.second, it.value.first, it.value.second, it.value.first.toString() + "/" + it.value.second) }
                        .toList()
                },
        )
            .build()
        assertEquals(0, frame.numBars.toLong())
        assertEquals(0, frame.numLines.toLong())
        result.submit(
            mapOf(
                Pair("LIBERAL", Color.RED) to Pair(26, 157),
                Pair("CONSERVATIVE", Color.BLUE) to Pair(4, 121),
                Pair("NEW DEMOCRATIC PARTY", Color.ORANGE) to Pair(1, 24),
                Pair("BLOC QU\u00c9B\u00c9COIS", Color.CYAN) to Pair(0, 32),
                Pair("GREEN", Color.GREEN) to Pair(1, 3),
                Pair("INDEPENDENT", Color.GRAY) to Pair(0, 1),
            ),
        )
        assertEquals(6, frame.numBars)
        assertEquals("LIBERAL", frame.getLeftText(0))
        assertEquals("CONSERVATIVE", frame.getLeftText(1))
        assertEquals("BLOC QU\u00c9B\u00c9COIS", frame.getLeftText(2))
        assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3))
        assertEquals("GREEN", frame.getLeftText(4))
        assertEquals("INDEPENDENT", frame.getLeftText(5))
        assertEquals("26/157", frame.getRightText(0))
        assertEquals("4/121", frame.getRightText(1))
        assertEquals("0/32", frame.getRightText(2))
        assertEquals("1/24", frame.getRightText(3))
        assertEquals("1/3", frame.getRightText(4))
        assertEquals("0/1", frame.getRightText(5))
        assertEquals(0, frame.getSeries(0)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(1)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(2)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(3)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(4)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(5)[0].second.toInt().toLong())
        assertEquals(lighten(Color.RED), frame.getSeries(0)[1].first)
        assertEquals(lighten(Color.BLUE), frame.getSeries(1)[1].first)
        assertEquals(lighten(Color.CYAN), frame.getSeries(2)[1].first)
        assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].first)
        assertEquals(lighten(Color.GREEN), frame.getSeries(4)[1].first)
        assertEquals(lighten(Color.GRAY), frame.getSeries(5)[1].first)
        assertEquals(26, frame.getSeries(0)[1].second.toInt().toLong())
        assertEquals(4, frame.getSeries(1)[1].second.toInt().toLong())
        assertEquals(0, frame.getSeries(2)[1].second.toInt().toLong())
        assertEquals(1, frame.getSeries(3)[1].second.toInt().toLong())
        assertEquals(1, frame.getSeries(4)[1].second.toInt().toLong())
        assertEquals(0, frame.getSeries(5)[1].second.toInt().toLong())
        assertEquals(Color.RED, frame.getSeries(0)[2].first)
        assertEquals(Color.BLUE, frame.getSeries(1)[2].first)
        assertEquals(Color.CYAN, frame.getSeries(2)[2].first)
        assertEquals(Color.ORANGE, frame.getSeries(3)[2].first)
        assertEquals(Color.GREEN, frame.getSeries(4)[2].first)
        assertEquals(Color.GRAY, frame.getSeries(5)[2].first)
        assertEquals((157 - 26).toLong(), frame.getSeries(0)[2].second.toInt().toLong())
        assertEquals((121 - 4).toLong(), frame.getSeries(1)[2].second.toInt().toLong())
        assertEquals((32 - 0).toLong(), frame.getSeries(2)[2].second.toInt().toLong())
        assertEquals((24 - 1).toLong(), frame.getSeries(3)[2].second.toInt().toLong())
        assertEquals((3 - 1).toLong(), frame.getSeries(4)[2].second.toInt().toLong())
        assertEquals((1 - 0).toLong(), frame.getSeries(5)[2].second.toInt().toLong())
        assertEquals(0, frame.min.toInt().toLong())
        assertEquals(157, frame.max.toInt().toLong())
    }

    @Test
    fun testDualChangeBars() {
        val result = Publisher<Map<Pair<String, Color>, Triple<Int, Int, Int>>>(emptyMap())
        val frame = dual(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.third }
                        .map { DualBar(it.key.first, it.key.second, it.value.first, it.value.second, """${DIFF.format(it.value.first)}/${DIFF.format(it.value.second)}""") }
                        .toList()
                },
        )
            .build()
        assertEquals(0, frame.numBars.toLong())
        assertEquals(0, frame.numLines.toLong())
        result.submit(
            mapOf(
                Pair("LIB", Color.RED) to Triple(-6, -27, 157),
                Pair("CON", Color.BLUE) to Triple(+4, +22, 121),
                Pair("NDP", Color.ORANGE) to Triple(+1, -20, 24),
                Pair("BQ", Color.CYAN) to Triple(0, +22, 32),
                Pair("GRN", Color.GREEN) to Triple(+1, +2, 3),
                Pair("IND", Color.GRAY) to Triple(0, +1, 1),
            ),
        )
        assertEquals(6, frame.numBars)
        assertEquals("LIB", frame.getLeftText(0))
        assertEquals("CON", frame.getLeftText(1))
        assertEquals("BQ", frame.getLeftText(2))
        assertEquals("NDP", frame.getLeftText(3))
        assertEquals("GRN", frame.getLeftText(4))
        assertEquals("IND", frame.getLeftText(5))
        assertEquals("-6/-27", frame.getRightText(0))
        assertEquals("+4/+22", frame.getRightText(1))
        assertEquals("+0/+22", frame.getRightText(2))
        assertEquals("+1/-20", frame.getRightText(3))
        assertEquals("+1/+2", frame.getRightText(4))
        assertEquals("+0/+1", frame.getRightText(5))
        assertEquals(Color.RED, frame.getSeries(0)[0].first)
        assertEquals(Color.BLUE, frame.getSeries(1)[0].first)
        assertEquals(Color.CYAN, frame.getSeries(2)[0].first)
        assertEquals(Color.ORANGE, frame.getSeries(3)[0].first)
        assertEquals(Color.GREEN, frame.getSeries(4)[0].first)
        assertEquals(Color.GRAY, frame.getSeries(5)[0].first)
        assertEquals(0, frame.getSeries(0)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(1)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(2)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(3)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(4)[0].second.toInt().toLong())
        assertEquals(0, frame.getSeries(5)[0].second.toInt().toLong())
        assertEquals(Color.RED, frame.getSeries(0)[1].first)
        assertEquals(Color.BLUE, frame.getSeries(1)[1].first)
        assertEquals(Color.CYAN, frame.getSeries(2)[1].first)
        assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].first)
        assertEquals(Color.GREEN, frame.getSeries(4)[1].first)
        assertEquals(Color.GRAY, frame.getSeries(5)[1].first)
        assertEquals(-6, frame.getSeries(0)[1].second.toInt().toLong())
        assertEquals(+4, frame.getSeries(1)[1].second.toInt().toLong())
        assertEquals(0, frame.getSeries(2)[1].second.toInt().toLong())
        assertEquals(+1, frame.getSeries(3)[1].second.toInt().toLong())
        assertEquals(+1, frame.getSeries(4)[1].second.toInt().toLong())
        assertEquals(0, frame.getSeries(5)[1].second.toInt().toLong())
        assertEquals(lighten(Color.RED), frame.getSeries(0)[2].first)
        assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].first)
        assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].first)
        assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].first)
        assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].first)
        assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].first)
        assertEquals((-27 - -6).toLong(), frame.getSeries(0)[2].second.toInt().toLong())
        assertEquals((+22 - +4).toLong(), frame.getSeries(1)[2].second.toInt().toLong())
        assertEquals((+22 - 0).toLong(), frame.getSeries(2)[2].second.toInt().toLong())
        assertEquals(-20, frame.getSeries(3)[2].second.toInt().toLong())
        assertEquals((+2 - +1).toLong(), frame.getSeries(4)[2].second.toInt().toLong())
        assertEquals((+1 - 0).toLong(), frame.getSeries(5)[2].second.toInt().toLong())
        assertEquals(-27, frame.min.toInt().toLong())
        assertEquals(+22, frame.max.toInt().toLong())
    }

    @Test
    fun testDualChangeRangeBars() {
        val result = Publisher<Map<Pair<String, Color>, Triple<Int, Int, Int>>>(emptyMap())
        val frame = dual(
            result
                .map { map ->
                    map.entries.asSequence()
                        .sortedByDescending { it.value.third }
                        .map { DualBar(it.key.first, it.key.second, it.value.first, it.value.second, "(${DIFF.format(it.value.first)})-(${DIFF.format(it.value.second)})") }
                        .toList()
                },
        )
            .build()
        assertEquals(0, frame.numBars.toLong())
        assertEquals(0, frame.numLines.toLong())
        result.submit(
            mapOf(
                Pair("LIB", Color.RED) to Triple(-27, -6, 157),
                Pair("CON", Color.BLUE) to Triple(+4, +22, 121),
                Pair("NDP", Color.ORANGE) to Triple(-20, +1, 24),
                Pair("BQ", Color.CYAN) to Triple(0, +22, 32),
                Pair("GRN", Color.GREEN) to Triple(+1, +2, 3),
                Pair("IND", Color.GRAY) to Triple(0, +1, 1),
            ),
        )
        assertEquals(6, frame.numBars)
        assertEquals("LIB", frame.getLeftText(0))
        assertEquals("CON", frame.getLeftText(1))
        assertEquals("BQ", frame.getLeftText(2))
        assertEquals("NDP", frame.getLeftText(3))
        assertEquals("GRN", frame.getLeftText(4))
        assertEquals("IND", frame.getLeftText(5))
        assertEquals("(-27)-(-6)", frame.getRightText(0))
        assertEquals("(+4)-(+22)", frame.getRightText(1))
        assertEquals("(+0)-(+22)", frame.getRightText(2))
        assertEquals("(-20)-(+1)", frame.getRightText(3))
        assertEquals("(+1)-(+2)", frame.getRightText(4))
        assertEquals("(+0)-(+1)", frame.getRightText(5))
        assertEquals(Color.RED, frame.getSeries(0)[1].first)
        assertEquals(Color.BLUE, frame.getSeries(1)[1].first)
        assertEquals(Color.CYAN, frame.getSeries(2)[1].first)
        assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[1].first)
        assertEquals(Color.GREEN, frame.getSeries(4)[1].first)
        assertEquals(Color.GRAY, frame.getSeries(5)[1].first)
        assertEquals(-6, frame.getSeries(0)[1].second.toInt().toLong())
        assertEquals(+4, frame.getSeries(1)[1].second.toInt().toLong())
        assertEquals(0, frame.getSeries(2)[1].second.toInt().toLong())
        assertEquals(-20, frame.getSeries(3)[1].second.toInt().toLong())
        assertEquals(+1, frame.getSeries(4)[1].second.toInt().toLong())
        assertEquals(0, frame.getSeries(5)[1].second.toInt().toLong())
        assertEquals(lighten(Color.RED), frame.getSeries(0)[2].first)
        assertEquals(lighten(Color.BLUE), frame.getSeries(1)[2].first)
        assertEquals(lighten(Color.CYAN), frame.getSeries(2)[2].first)
        assertEquals(lighten(Color.ORANGE), frame.getSeries(3)[2].first)
        assertEquals(lighten(Color.GREEN), frame.getSeries(4)[2].first)
        assertEquals(lighten(Color.GRAY), frame.getSeries(5)[2].first)
        assertEquals((-27 - -6).toLong(), frame.getSeries(0)[2].second.toInt().toLong())
        assertEquals((+22 - +4).toLong(), frame.getSeries(1)[2].second.toInt().toLong())
        assertEquals((+22 - 0).toLong(), frame.getSeries(2)[2].second.toInt().toLong())
        assertEquals(+1, frame.getSeries(3)[2].second.toInt().toLong())
        assertEquals((+2 - +1).toLong(), frame.getSeries(4)[2].second.toInt().toLong())
        assertEquals((+1 - 0).toLong(), frame.getSeries(5)[2].second.toInt().toLong())
        assertEquals(-27, frame.min.toInt().toLong())
        assertEquals(+22, frame.max.toInt().toLong())
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
            BasicBar("Northern Ireland", Color.BLACK, 3),
        )
        val frame = basic(regions.asOneTimePublisher()).build()
        assertEquals(12, frame.numBars)
        assertEquals("East Midlands", frame.getLeftText(0))
        assertEquals("South East England", frame.getLeftText(5))
        assertEquals("Northern Ireland", frame.getLeftText(11))
        assertEquals("5", frame.getRightText(0))
        assertEquals("10", frame.getRightText(5))
        assertEquals("3", frame.getRightText(11))
        assertEquals(5, frame.getSeries(0)[0].second)
        assertEquals(10, frame.getSeries(5)[0].second)
        assertEquals(3, frame.getSeries(11)[0].second)
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
            DualBar("Northern Ireland", Color.BLACK, 16, 18, "18 > 16"),
        )
        val frame = dual(regions.asOneTimePublisher()).build()
        assertEquals(12, frame.numBars)
        assertEquals("East Midlands", frame.getLeftText(0))
        assertEquals("South East England", frame.getLeftText(5))
        assertEquals("Northern Ireland", frame.getLeftText(11))
        assertEquals("46 > 44", frame.getRightText(0))
        assertEquals("84 > 83", frame.getRightText(5))
        assertEquals("18 > 16", frame.getRightText(11))
        assertEquals(Color.BLACK, frame.getSeries(0)[0].first)
        assertEquals(Color.BLACK, frame.getSeries(5)[0].first)
        assertEquals(Color.BLACK, frame.getSeries(11)[0].first)
        assertEquals(0.0, frame.getSeries(0)[0].second.toDouble(), 0.0)
        assertEquals(0.0, frame.getSeries(5)[0].second.toDouble(), 0.0)
        assertEquals(0.0, frame.getSeries(11)[0].second.toDouble(), 0.0)
        assertEquals(Color.BLACK, frame.getSeries(0)[1].first)
        assertEquals(Color.BLACK, frame.getSeries(5)[1].first)
        assertEquals(Color.BLACK, frame.getSeries(11)[1].first)
        assertEquals(44.0, frame.getSeries(0)[1].second.toDouble(), 0.0)
        assertEquals(83.0, frame.getSeries(5)[1].second.toDouble(), 0.0)
        assertEquals(16.0, frame.getSeries(11)[1].second.toDouble(), 0.0)
        assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(0)[2].first)
        assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(5)[2].first)
        assertEquals(ColorUtils.lighten(Color.BLACK), frame.getSeries(11)[2].first)
        assertEquals(2.0, frame.getSeries(0)[2].second.toDouble(), 0.0)
        assertEquals(1.0, frame.getSeries(5)[2].second.toDouble(), 0.0)
        assertEquals(2.0, frame.getSeries(11)[2].second.toDouble(), 0.0)
        assertEquals(84.0, frame.max.toDouble(), 1e-6)
        assertEquals(0.0, frame.min.toDouble(), 1e-6)
    }

    @Test
    fun testDualVariousCombos() {
        val doAssert = { exp: Pair<Color, Number>, act: Pair<Color, Number> ->
            assertEquals(exp.first, act.first)
            assertEquals(exp.second.toDouble(), act.second.toDouble(), 1e-6)
        }
        val regions = Publisher(listOf(DualBar("", Color.BLACK, 0.0, 0.0, "")))
        val frame = dual(regions).build()
        assertEquals(1, frame.numBars)
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 0.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, 0.0, 2.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, 2.0, 0.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, 0.0, -2.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, -2.0, 0.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, 1.0, 3.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, 3.0, 1.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, 1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), 2.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, -1.0, -3.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, -1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, -3.0, -1.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(Color.BLACK, -1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -2.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, -1.0, +1.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(lighten(Color.BLACK), -1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), +1.0), frame.getSeries(0)[2])
        regions.submit(listOf(DualBar("", Color.BLACK, +1.0, -1.0, "")))
        doAssert(Pair(Color.BLACK, 0.0), frame.getSeries(0)[0])
        doAssert(Pair(lighten(Color.BLACK), +1.0), frame.getSeries(0)[1])
        doAssert(Pair(lighten(Color.BLACK), -1.0), frame.getSeries(0)[2])
    }

    @Test
    fun expandBarSpace() {
        val bars = Publisher<List<BasicBar>>(listOf())
        val minBars = Publisher(0)
        val barFrame = basic(bars).withMinBarCount(minBars).build()
        assertEquals(0, barFrame.numBars)

        bars.submit(
            listOf(
                BasicBar("JOE BIDEN", Color.BLUE, 306),
                BasicBar("DONALD TRUMP", Color.RED, 232),
            ),
        )
        assertEquals(2, barFrame.numBars)

        minBars.submit(3)
        assertEquals(3, barFrame.numBars)

        minBars.submit(1)
        assertEquals(2, barFrame.numBars)

        bars.submit(emptyList())
        assertEquals(1, barFrame.numBars)
    }

    companion object {
        private val THOUSANDS = DecimalFormat("#,##0")
        private val DIFF = DecimalFormat("+0;-0")

        private fun lighten(color: Color): Color {
            return Color(
                128 + color.red / 2,
                128 + color.green / 2,
                128 + color.blue / 2,
            )
        }
    }
}
