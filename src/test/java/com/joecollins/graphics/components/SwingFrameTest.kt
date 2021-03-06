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
        val frame = SwingFrame()
        frame.setRangeBinding(fixedBinding(10))
        Assert.assertEquals(10, frame.getRange())
    }

    @Test
    fun testSwingValue() {
        val frame = SwingFrame()
        frame.setValueBinding(fixedBinding(3))
        Assert.assertEquals(3, frame.getValue())
    }

    @Test
    fun testLeftRightColors() {
        val frame = SwingFrame()
        frame.setLeftColorBinding(fixedBinding(Color.BLUE))
        frame.setRightColorBinding(fixedBinding(Color.RED))
        Assert.assertEquals(Color.BLUE, frame.getLeftColor())
        Assert.assertEquals(Color.RED, frame.getRightColor())
    }

    @Test
    fun testBottomText() {
        val frame = SwingFrame()
        frame.setBottomTextBinding(fixedBinding("4.7% SWING LIB TO CON"))
        frame.setBottomColorBinding(fixedBinding(Color.BLUE))
        Assert.assertEquals("4.7% SWING LIB TO CON", frame.getBottomText())
        Assert.assertEquals(Color.BLUE, frame.getBottomColor())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderNoSwing() {
        val frame = SwingFrame()
        frame.setHeaderBinding(fixedBinding<String?>("SWING SINCE 2015"))
        frame.setRangeBinding(fixedBinding(10))
        frame.setValueBinding(fixedBinding(0))
        frame.setLeftColorBinding(fixedBinding(Color.BLUE))
        frame.setRightColorBinding(fixedBinding(Color.RED))
        frame.setBottomTextBinding(fixedBinding("WAITING FOR RESULTS..."))
        frame.setBottomColorBinding(fixedBinding(Color.BLACK))
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "NoSwing", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSwingRight() {
        val frame = SwingFrame()
        frame.setHeaderBinding(fixedBinding<String?>("SWING SINCE 2015"))
        frame.setRangeBinding(fixedBinding(10))
        frame.setValueBinding(fixedBinding(4.7))
        frame.setLeftColorBinding(fixedBinding(Color.BLUE))
        frame.setRightColorBinding(fixedBinding(Color.RED))
        frame.setBottomTextBinding(fixedBinding("4.7% SWING LIB TO CON"))
        frame.setBottomColorBinding(fixedBinding(Color.BLUE))
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "SwingRight", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSwingLeft() {
        val frame = SwingFrame()
        frame.setHeaderBinding(fixedBinding<String?>("SWING SINCE 2015"))
        frame.setRangeBinding(fixedBinding(10))
        frame.setValueBinding(fixedBinding(-1.3))
        frame.setLeftColorBinding(fixedBinding(Color.RED))
        frame.setRightColorBinding(fixedBinding(Color.ORANGE))
        frame.setBottomTextBinding(fixedBinding("1.3% SWING LIB TO NDP"))
        frame.setBottomColorBinding(fixedBinding(Color.ORANGE))
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "SwingLeft", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMaxSwingRight() {
        val frame = SwingFrame()
        frame.setHeaderBinding(fixedBinding<String?>("SWING SINCE 2015"))
        frame.setRangeBinding(fixedBinding(10))
        frame.setValueBinding(fixedBinding(19.9))
        frame.setLeftColorBinding(fixedBinding(Color.BLUE))
        frame.setRightColorBinding(fixedBinding(Color.RED))
        frame.setBottomTextBinding(fixedBinding("19.9% SWING LIB TO CON"))
        frame.setBottomColorBinding(fixedBinding(Color.BLUE))
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "MaxSwingRight", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMaxSwingLeft() {
        val frame = SwingFrame()
        frame.setHeaderBinding(fixedBinding<String?>("SWING SINCE 2015"))
        frame.setRangeBinding(fixedBinding(10))
        frame.setValueBinding(fixedBinding(-21.6))
        frame.setLeftColorBinding(fixedBinding(Color.RED))
        frame.setRightColorBinding(fixedBinding(Color.GREEN))
        frame.setBottomTextBinding(fixedBinding("21.6% SWING LIB TO GRN"))
        frame.setBottomColorBinding(fixedBinding(Color.GREEN))
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "MaxSwingLeft", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderAccents() {
        val frame = SwingFrame()
        frame.setHeaderBinding(fixedBinding<String?>("CHANGES APR\u00c8S 2014"))
        frame.setRangeBinding(fixedBinding(10))
        frame.setValueBinding(fixedBinding(0))
        frame.setLeftColorBinding(fixedBinding(Color.BLUE))
        frame.setRightColorBinding(fixedBinding(Color.RED))
        frame.setBottomTextBinding(fixedBinding("VOIX PAS R\u00c9\u00c7US"))
        frame.setBottomColorBinding(fixedBinding(Color.BLACK))
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "Accents", frame)
    }
}
