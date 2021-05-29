package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class SwingFrameTest {
    @Test
    fun testSwingRange() {
        val frame = SwingFrame(
            headerBinding = fixedBinding(null),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(3),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            bottomTextBinding = fixedBinding("4.7% SWING LIB TO CON"),
            bottomColorBinding = fixedBinding(Color.BLUE)
        )
        Assert.assertEquals(10, frame.getRange())
    }

    @Test
    fun testSwingValue() {
        val frame = SwingFrame(
            headerBinding = fixedBinding(null),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(3),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            bottomTextBinding = fixedBinding("4.7% SWING LIB TO CON"),
            bottomColorBinding = fixedBinding(Color.BLUE)
        )
        Assert.assertEquals(3, frame.getValue())
    }

    @Test
    fun testLeftRightColors() {
        val frame = SwingFrame(
            headerBinding = fixedBinding(null),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(3),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            bottomTextBinding = fixedBinding("4.7% SWING LIB TO CON"),
            bottomColorBinding = fixedBinding(Color.BLUE)
        )
        Assert.assertEquals(Color.BLUE, frame.getLeftColor())
        Assert.assertEquals(Color.RED, frame.getRightColor())
    }

    @Test
    fun testBottomText() {
        val frame = SwingFrame(
            headerBinding = fixedBinding(null),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(3),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            bottomTextBinding = fixedBinding("4.7% SWING LIB TO CON"),
            bottomColorBinding = fixedBinding(Color.BLUE)
        )
        Assert.assertEquals("4.7% SWING LIB TO CON", frame.getBottomText())
        Assert.assertEquals(Color.BLUE, frame.getBottomColor())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderNoSwing() {
        val frame = SwingFrame(
            headerBinding = fixedBinding<String?>("SWING SINCE 2015"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(0),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            bottomTextBinding = fixedBinding("WAITING FOR RESULTS..."),
            bottomColorBinding = fixedBinding(Color.BLACK)
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "NoSwing", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSwingRight() {
        val frame = SwingFrame(
            headerBinding = fixedBinding<String?>("SWING SINCE 2015"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(4.7),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            bottomTextBinding = fixedBinding("4.7% SWING LIB TO CON"),
            bottomColorBinding = fixedBinding(Color.BLUE)
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "SwingRight", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSwingLeft() {
        val frame = SwingFrame(
            headerBinding = fixedBinding<String?>("SWING SINCE 2015"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(-1.3),
            leftColorBinding = fixedBinding(Color.RED),
            rightColorBinding = fixedBinding(Color.ORANGE),
            bottomTextBinding = fixedBinding("1.3% SWING LIB TO NDP"),
            bottomColorBinding = fixedBinding(Color.ORANGE)
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "SwingLeft", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMaxSwingRight() {
        val frame = SwingFrame(
            headerBinding = fixedBinding<String?>("SWING SINCE 2015"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(19.9),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            bottomTextBinding = fixedBinding("19.9% SWING LIB TO CON"),
            bottomColorBinding = fixedBinding(Color.BLUE)
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "MaxSwingRight", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMaxSwingLeft() {
        val frame = SwingFrame(
            headerBinding = fixedBinding<String?>("SWING SINCE 2015"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(-21.6),
            leftColorBinding = fixedBinding(Color.RED),
            rightColorBinding = fixedBinding(Color.GREEN),
            bottomTextBinding = fixedBinding("21.6% SWING LIB TO GRN"),
            bottomColorBinding = fixedBinding(Color.GREEN)
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "MaxSwingLeft", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderAccents() {
        val frame = SwingFrame(
            headerBinding = fixedBinding<String?>("CHANGES APR\u00c8S 2014"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(0),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            bottomTextBinding = fixedBinding("VOIX PAS R\u00c9\u00c7US"),
            bottomColorBinding = fixedBinding(Color.BLACK)
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "Accents", frame)
    }
}
