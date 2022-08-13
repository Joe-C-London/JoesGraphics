package com.joecollins.graphics.components

import com.joecollins.graphics.components.SwingometerFrameBuilder.Companion.basic
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow
import java.util.concurrent.TimeUnit

class SwingometerFrameBuilderTest {
    @Test
    fun testBasic() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = basic(colors, value)
            .withHeader("SWINGOMETER".asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsEqual("SWINGOMETER"))
        Assert.assertEquals(Color.BLUE, frame.leftColor)
        Assert.assertEquals(Color.RED, frame.rightColor)
        Assert.assertEquals(-1.0, frame.value.toDouble(), 0.0)
    }

    @Test
    fun testNaNIsZero() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(Double.NaN)
        val frame = basic(colors, value)
            .withHeader("SWINGOMETER".asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsEqual("SWINGOMETER"))
        Assert.assertEquals(Color.BLUE, frame.leftColor)
        Assert.assertEquals(Color.RED, frame.rightColor)
        Assert.assertEquals(0.0, frame.value.toDouble(), 0.0)
    }

    @Test
    fun testMax() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val range = Publisher(10.0)
        val bucketSize = Publisher(0.5)
        val frame = basic(colors, value)
            .withRange(range)
            .withBucketSize(bucketSize)
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.value.toDouble() }, IsEqual(-1.0))
        Assert.assertEquals(10.0, frame.range.toDouble(), 0.0)
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
        value.submit(14.8)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.value.toDouble() }, IsEqual(14.8))
        Assert.assertEquals(15.0, frame.range.toDouble(), 0.0)
        Assert.assertEquals(30, frame.numBucketsPerSide.toLong())
        value.submit(-11.2)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.value.toDouble() }, IsEqual(-11.2))
        Assert.assertEquals(11.5, frame.range.toDouble(), 0.0)
        Assert.assertEquals(23, frame.numBucketsPerSide.toLong())
        value.submit(2.7)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.value.toDouble() }, IsEqual(2.7))
        Assert.assertEquals(10.0, frame.range.toDouble(), 0.0)
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
        value.submit(-3.4)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.value.toDouble() }, IsEqual(-3.4))
        Assert.assertEquals(10.0, frame.range.toDouble(), 0.0)
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
    }

    @Test
    fun testTicks() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val range = Publisher(10.0)
        val tickInterval = Publisher(1.0)
        val frame = basic(colors, value)
            .withRange(range)
            .withTickInterval(tickInterval) { DecimalFormat("0").format(it) }
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numTicks }, IsEqual(19))
        val ticks = (0 until frame.numTicks).associate { frame.getTickPosition(it) to frame.getTickText(it) }
        Assert.assertEquals("9", ticks[-9.0])
        Assert.assertEquals("0", ticks[0.0])
        Assert.assertEquals("5", ticks[5.0])
        value.submit(11.3)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numTicks }, IsEqual(23))
    }

    @Test
    fun testNeededToWin() {
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val leftToWin = Publisher(-2.0)
        val rightToWin = Publisher(4.0)
        val frame = basic(colors, value)
            .withLeftNeedingToWin(leftToWin)
            .withRightNeedingToWin(rightToWin)
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftToWin.toDouble() }, IsEqual(-2.0))
        Assert.assertEquals(4.0, frame.rightToWin.toDouble(), 0.0)
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
                OuterLabel(Color.BLUE, "500", -13.86)
            )
                .asOneTimePublisher()
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = basic(colors, value)
            .withOuterLabels(labels, { it.position }, { it.label }, { it.color })
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numOuterLabels }, IsEqual(10))
        Assert.assertEquals(Color.RED, frame.getOuterLabelColor(0))
        Assert.assertEquals("350", frame.getOuterLabelText(1))
        Assert.assertEquals(7.855, frame.getOuterLabelPosition(2).toDouble(), 1e-6)
        Assert.assertEquals(Color.BLUE, frame.getOuterLabelColor(9))
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
                Dot(-0.76, Color.BLUE)
            )
                .asOneTimePublisher()
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = basic(colors, value)
            .withDots(dots, { it.position }, { it.color })
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(7))
        Assert.assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertEquals("", frame.getDotLabel(2))
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
                Dot(-0.76, Color.BLUE, "6")
            )
                .asOneTimePublisher()
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = basic(colors, value)
            .withDots(dots, { it.position }, { it.color }, { it.label })
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(7))
        Assert.assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertEquals("10", frame.getDotLabel(2))
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
                Dot(-0.76, Color.BLUE, false)
            )
                .asOneTimePublisher()
        val colors = Publisher(Pair(Color.BLUE, Color.RED))
        val value = Publisher(-1.0)
        val frame = basic(colors, value)
            .withDotsSolid(dots, { it.position }, { it.color }, { it.solid })
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(7))
        Assert.assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertTrue(frame.isDotSolid(2))
        Assert.assertFalse(frame.isDotSolid(4))
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
        val frame = basic(colors, value)
            .withFixedDots(dots, { it.position }, { it.getColor() })
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(7))
        Assert.assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertEquals("", frame.getDotLabel(2))
        dots[1].setColor(Color.BLUE)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(1) }, IsEqual(Color.BLUE))
    }
}
