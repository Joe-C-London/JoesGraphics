package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.SwingometerFrameBuilder.Companion.basic
import com.joecollins.graphics.utils.BindableWrapper
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

class SwingometerFrameBuilderTest {
    @Test
    fun testBasic() {
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val frame = basic(colors.binding, value.binding)
            .withHeader(fixedBinding("SWINGOMETER"))
            .build()
        Assert.assertEquals(Color.BLUE, frame.leftColor)
        Assert.assertEquals(Color.RED, frame.rightColor)
        Assert.assertEquals(-1.0, frame.value.toDouble(), 0.0)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsEqual("SWINGOMETER"))
    }

    @Test
    fun testNaNIsZero() {
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(Double.NaN)
        val frame = basic(colors.binding, value.binding)
            .withHeader(fixedBinding("SWINGOMETER"))
            .build()
        Assert.assertEquals(Color.BLUE, frame.leftColor)
        Assert.assertEquals(Color.RED, frame.rightColor)
        Assert.assertEquals(0.0, frame.value.toDouble(), 0.0)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsEqual("SWINGOMETER"))
    }

    @Test
    fun testMax() {
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val range = BindableWrapper(10.0)
        val bucketSize = BindableWrapper(0.5)
        val frame = basic(colors.binding, value.binding)
            .withRange(range.binding)
            .withBucketSize(bucketSize.binding)
            .build()
        Assert.assertEquals(-1.0, frame.value.toDouble(), 0.0)
        Assert.assertEquals(10.0, frame.range.toDouble(), 0.0)
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
        value.value = 14.8
        Assert.assertEquals(14.8, frame.value.toDouble(), 0.0)
        Assert.assertEquals(15.0, frame.range.toDouble(), 0.0)
        Assert.assertEquals(30, frame.numBucketsPerSide.toLong())
        value.value = -11.2
        Assert.assertEquals(-11.2, frame.value.toDouble(), 0.0)
        Assert.assertEquals(11.5, frame.range.toDouble(), 0.0)
        Assert.assertEquals(23, frame.numBucketsPerSide.toLong())
        value.value = 2.7
        Assert.assertEquals(2.7, frame.value.toDouble(), 0.0)
        Assert.assertEquals(10.0, frame.range.toDouble(), 0.0)
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
        value.value = -3.4
        Assert.assertEquals(-3.4, frame.value.toDouble(), 0.0)
        Assert.assertEquals(10.0, frame.range.toDouble(), 0.0)
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
    }

    @Test
    fun testTicks() {
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val range = BindableWrapper(10.0)
        val tickInterval = BindableWrapper(1.0)
        val frame = basic(colors.binding, value.binding)
            .withRange(range.binding)
            .withTickInterval(tickInterval.binding) { DecimalFormat("0").format(it) }
            .build()
        Assert.assertEquals(19, frame.numTicks.toLong())
        val ticks = (0 until frame.numTicks).associate { frame.getTickPosition(it) to frame.getTickText(it) }
        Assert.assertEquals("9", ticks[-9.0])
        Assert.assertEquals("0", ticks[0.0])
        Assert.assertEquals("5", ticks[5.0])
        value.value = 11.3
        Assert.assertEquals(23, frame.numTicks.toLong())
    }

    @Test
    fun testNeededToWin() {
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val leftToWin = BindableWrapper(-2.0)
        val rightToWin = BindableWrapper(4.0)
        val frame = basic(colors.binding, value.binding)
            .withLeftNeedingToWin(leftToWin.binding)
            .withRightNeedingToWin(rightToWin.binding)
            .build()
        Assert.assertEquals(-2.0, frame.leftToWin.toDouble(), 0.0)
        Assert.assertEquals(4.0, frame.rightToWin.toDouble(), 0.0)
    }

    @Test
    fun testOuterLabels() {
        class OuterLabel(val color: Color, val label: String, val position: Double)

        val labels = fixedBinding(
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
        )
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val frame = basic(colors.binding, value.binding)
            .withOuterLabels(labels, { it.position }, { it.label }, { it.color })
            .build()
        Assert.assertEquals(10, frame.numOuterLabels.toLong())
        Assert.assertEquals(Color.RED, frame.getOuterLabelColor(0))
        Assert.assertEquals("350", frame.getOuterLabelText(1))
        Assert.assertEquals(7.855, frame.getOuterLabelPosition(2).toDouble(), 1e-6)
        Assert.assertEquals(Color.BLUE, frame.getOuterLabelColor(9))
    }

    @Test
    fun testDotsWithoutLabels() {
        class Dot(val position: Double, val color: Color)

        val dots = fixedBinding(
            listOf(
                Dot(0.115, Color.RED),
                Dot(0.36, Color.RED),
                Dot(0.385, Color.RED),
                Dot(0.6, Color.RED),
                Dot(-0.185, Color.BLUE),
                Dot(-0.76, Color.BLUE),
                Dot(-0.76, Color.BLUE)
            )
        )
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val frame = basic(colors.binding, value.binding)
            .withDots(dots, { it.position }, { it.color })
            .build()
        Assert.assertEquals(7, frame.numDots.toLong())
        Assert.assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertEquals("", frame.getDotLabel(2))
    }

    @Test
    fun testDotsWithLabels() {
        class Dot(val position: Double, val color: Color, val label: String)

        val dots = fixedBinding(
            listOf(
                Dot(0.115, Color.RED, "16"),
                Dot(0.36, Color.RED, "20"),
                Dot(0.385, Color.RED, "10"),
                Dot(0.6, Color.RED, "29"),
                Dot(-0.185, Color.BLUE, "4"),
                Dot(-0.76, Color.BLUE, "10"),
                Dot(-0.76, Color.BLUE, "6")
            )
        )
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val frame = basic(colors.binding, value.binding)
            .withDots(dots, { it.position }, { it.color }, { it.label })
            .build()
        Assert.assertEquals(7, frame.numDots.toLong())
        Assert.assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertEquals("10", frame.getDotLabel(2))
    }

    @Test
    fun testDotsSolidOrEmpty() {
        class Dot(val position: Double, val color: Color, val solid: Boolean)

        val dots = fixedBinding(
            listOf(
                Dot(0.115, Color.RED, true),
                Dot(0.36, Color.RED, true),
                Dot(0.385, Color.RED, true),
                Dot(0.6, Color.RED, true),
                Dot(-0.185, Color.BLUE, false),
                Dot(-0.76, Color.BLUE, true),
                Dot(-0.76, Color.BLUE, false)
            )
        )
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val frame = basic(colors.binding, value.binding)
            .withDotsSolid(dots, { it.position }, { it.color }, { it.solid })
            .build()
        Assert.assertEquals(7, frame.numDots.toLong())
        Assert.assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertTrue(frame.isDotSolid(2))
        Assert.assertFalse(frame.isDotSolid(4))
    }

    internal enum class Property {
        PROP
    }

    @Test
    fun testFixedDots() {
        class Dot(val position: Double, private var color: Color) : Bindable<Dot, Property>() {
            fun getColor(): Binding<Color> {
                return Binding.propertyBinding(this, { it.color }, Property.PROP)
            }

            fun setColor(color: Color) {
                this.color = color
                onPropertyRefreshed(Property.PROP)
            }
        }

        val dots = ArrayList<Dot>()
        dots.add(Dot(0.115, Color.RED))
        dots.add(Dot(0.36, Color.RED))
        dots.add(Dot(0.385, Color.RED))
        dots.add(Dot(0.6, Color.RED))
        dots.add(Dot(-0.185, Color.BLUE))
        dots.add(Dot(-0.76, Color.BLUE))
        dots.add(Dot(-0.76, Color.BLUE))
        val colors = BindableWrapper(Pair(Color.BLUE, Color.RED))
        val value = BindableWrapper(-1.0)
        val frame = basic(colors.binding, value.binding)
            .withFixedDots(dots, { it.position }, { it.getColor() })
            .build()
        Assert.assertEquals(7, frame.numDots.toLong())
        Assert.assertEquals(0.115, frame.getDotPosition(0).toDouble(), 1e-6)
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertEquals("", frame.getDotLabel(2))
        dots[1].setColor(Color.BLUE)
        Assert.assertEquals(Color.BLUE, frame.getDotColor(1))
    }
}
