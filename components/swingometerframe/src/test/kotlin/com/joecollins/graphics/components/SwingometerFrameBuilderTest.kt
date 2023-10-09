package com.joecollins.graphics.components

import com.joecollins.graphics.components.SwingometerFrameBuilder.dots
import com.joecollins.graphics.components.SwingometerFrameBuilder.every
import com.joecollins.graphics.components.SwingometerFrameBuilder.labels
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow

class SwingometerFrameBuilderTest {
    @Test
    fun testBasic() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            header = "SWINGOMETER".asOneTimePublisher(),
        )
        assertEquals("SWINGOMETER", frame.header)
        assertEquals(Color.BLUE, frame.leftColor)
        assertEquals(Color.RED, frame.rightColor)
        assertEquals(-1.0, frame.value.toDouble(), 0.0)
    }

    @Test
    fun testNaNIsZero() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(Double.NaN)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            header = "SWINGOMETER".asOneTimePublisher(),
        )
        assertEquals("SWINGOMETER", frame.header)
        assertEquals(Color.BLUE, frame.leftColor)
        assertEquals(Color.RED, frame.rightColor)
        assertEquals(0.0, frame.value.toDouble(), 0.0)
    }

    @Test
    fun testMax() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val range = Publisher(10.0)
        val bucketSize = Publisher(0.5)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            range = range,
            bucketSize = bucketSize,
            header = null.asOneTimePublisher(),
        )
        assertEquals(-1.0, frame.value.toDouble(), 1e-6)
        assertEquals(10.0, frame.range.toDouble(), 0.0)
        assertEquals(20, frame.numBucketsPerSide.toLong())

        value.submit(14.8)
        assertEquals(14.8, frame.value.toDouble(), 1e-6)
        assertEquals(15.0, frame.range.toDouble(), 0.0)
        assertEquals(30, frame.numBucketsPerSide.toLong())

        value.submit(-11.2)
        assertEquals(-11.2, frame.value.toDouble(), 1e-6)
        assertEquals(11.5, frame.range.toDouble(), 0.0)
        assertEquals(23, frame.numBucketsPerSide.toLong())

        value.submit(2.7)
        assertEquals(2.7, frame.value.toDouble(), 1e-6)
        assertEquals(10.0, frame.range.toDouble(), 0.0)
        assertEquals(20, frame.numBucketsPerSide.toLong())

        value.submit(-3.4)
        assertEquals(-3.4, frame.value.toDouble())
        assertEquals(10.0, frame.range.toDouble(), 0.0)
        assertEquals(20, frame.numBucketsPerSide.toLong())
    }

    @Test
    fun testTicks() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val range = Publisher(10.0)
        val tickInterval = Publisher(1.0)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            range = range,
            tickInterval = every(tickInterval) { DecimalFormat("0").format(it) },
            header = null.asOneTimePublisher(),
        )
        assertEquals(19, frame.numTicks)
        val ticks = (0 until frame.numTicks).associate { frame.getTickPosition(it) to frame.getTickText(it) }
        assertEquals("9", ticks[-9.0])
        assertEquals("0", ticks[0.0])
        assertEquals("5", ticks[5.0])
        value.submit(11.3)
        assertEquals(23, frame.numTicks)
    }

    @Test
    fun testNeededToWin() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val leftToWin = Publisher(-2.0)
        val rightToWin = Publisher(4.0)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            leftToWin = leftToWin,
            rightToWin = rightToWin,
            header = null.asOneTimePublisher(),
        )
        assertEquals(-2.0, frame.leftToWin.toDouble(), 0.0)
        assertEquals(4.0, frame.rightToWin.toDouble(), 0.0)
    }

    @Test
    fun testOuterLabels() {
        class OuterLabel(val color: Color, val label: String, val position: Double)

        val labels =
            listOf(
                OuterLabel(Color.RED, "306", 0.0),
                OuterLabel(Color.RED, "350", 2.66),
                OuterLabel(Color.RED, "400", 7.855),
                OuterLabel(Color.RED, "450", 11.245),
                OuterLabel(Color.RED, "500", 15.055),
                OuterLabel(Color.BLUE, "270", -0.385),
                OuterLabel(Color.BLUE, "350", -2.565),
                OuterLabel(Color.BLUE, "400", -4.495),
                OuterLabel(Color.BLUE, "450", -9.455),
                OuterLabel(Color.BLUE, "500", -13.86),
            )
                .asOneTimePublisher()
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            outerLabels = labels(
                labels = labels,
                position = { position },
                label = { label },
                color = { color },
            ),
            header = null.asOneTimePublisher(),
        )
        assertEquals(10, frame.numOuterLabels)
        assertEquals(Color.RED, frame.getOuterLabelColor(0))
        assertEquals("350", frame.getOuterLabelText(1))
        assertEquals(7.855, frame.getOuterLabelPosition(2).toDouble(), 1e-6)
        assertEquals(Color.BLUE, frame.getOuterLabelColor(9))
    }

    @Test
    fun testDotsWithoutLabels() {
        class Dot(val position: Double, val color: Color)

        val dots =
            listOf(
                Dot(0.115, Color.RED),
                Dot(0.36, Color.RED),
                Dot(0.385, Color.RED),
                Dot(0.6, Color.RED),
                Dot(-0.185, Color.BLUE),
                Dot(-0.76, Color.BLUE),
                Dot(-0.76, Color.BLUE),
            )
                .asOneTimePublisher()
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            dots = dots(
                dots = dots,
                position = { position },
                color = { color },
            ),
            header = null.asOneTimePublisher(),
        )
        assertEquals(7, frame.numDots)
        assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        assertEquals(Color.RED, frame.getDotColor(1))
        assertEquals("", frame.getDotLabel(2))
    }

    @Test
    fun testDotsWithLabels() {
        class Dot(val position: Double, val color: Color, val label: String)

        val dots =
            listOf(
                Dot(0.115, Color.RED, "16"),
                Dot(0.36, Color.RED, "20"),
                Dot(0.385, Color.RED, "10"),
                Dot(0.6, Color.RED, "29"),
                Dot(-0.185, Color.BLUE, "4"),
                Dot(-0.76, Color.BLUE, "10"),
                Dot(-0.76, Color.BLUE, "6"),
            )
                .asOneTimePublisher()
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            dots = dots(
                dots = dots,
                position = { position },
                color = { color },
                label = { label },
            ),
            header = null.asOneTimePublisher(),
        )
        assertEquals(7, frame.numDots)
        assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        assertEquals(Color.RED, frame.getDotColor(1))
        assertEquals("10", frame.getDotLabel(2))
    }

    @Test
    fun testDotsSolidOrEmpty() {
        class Dot(val position: Double, val color: Color, val solid: Boolean)

        val dots =
            listOf(
                Dot(0.115, Color.RED, true),
                Dot(0.36, Color.RED, true),
                Dot(0.385, Color.RED, true),
                Dot(0.6, Color.RED, true),
                Dot(-0.185, Color.BLUE, false),
                Dot(-0.76, Color.BLUE, true),
                Dot(-0.76, Color.BLUE, false),
            )
                .asOneTimePublisher()
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            dots = dots(
                dots = dots,
                position = { position },
                color = { color },
                solid = { solid },
            ),
            header = null.asOneTimePublisher(),
        )
        assertEquals(7, frame.numDots)
        assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        assertEquals(Color.RED, frame.getDotColor(1))
        Assertions.assertTrue(frame.isDotSolid(2))
        Assertions.assertFalse(frame.isDotSolid(4))
    }

    @Test
    fun testFixedDots() {
        class Dot(val position: Double, private var color: Color) {
            private val colorPublisher = Publisher(color)
            fun getColor(): Flow.Publisher<Color> {
                return colorPublisher
            }

            fun setColor(color: Color) {
                this.color = color
                colorPublisher.submit(color)
            }
        }

        val dots = listOf(
            Dot(0.115, Color.RED),
            Dot(0.36, Color.RED),
            Dot(0.385, Color.RED),
            Dot(0.6, Color.RED),
            Dot(-0.185, Color.BLUE),
            Dot(-0.76, Color.BLUE),
            Dot(-0.76, Color.BLUE),
        )
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = SwingometerFrameBuilder.build(
            colors = colors,
            value = value,
            dots = dots(
                dots = dots,
                position = { position },
                color = { getColor() },
            ),
            header = null.asOneTimePublisher(),
        )
        assertEquals(7, frame.numDots)
        assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        assertEquals(Color.RED, frame.getDotColor(1))
        assertEquals("", frame.getDotLabel(2))
        dots[1].setColor(Color.BLUE)
        assertEquals(Color.BLUE, frame.getDotColor(1))
    }
}
