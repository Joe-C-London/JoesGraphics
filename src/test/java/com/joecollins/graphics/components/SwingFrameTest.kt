package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.Throws

class SwingFrameTest {
    @Test
    fun testSwingRange() {
        val frame = SwingFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            bottomTextPublisher = "4.7% SWING LIB TO CON".asOneTimePublisher(),
            bottomColorPublisher = Color.BLUE.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getRange() }, IsEqual(10))
    }

    @Test
    fun testSwingValue() {
        val frame = SwingFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            bottomTextPublisher = "4.7% SWING LIB TO CON".asOneTimePublisher(),
            bottomColorPublisher = Color.BLUE.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getValue() }, IsEqual(3))
    }

    @Test
    fun testLeftRightColors() {
        val frame = SwingFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            bottomTextPublisher = "4.7% SWING LIB TO CON".asOneTimePublisher(),
            bottomColorPublisher = Color.BLUE.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getLeftColor() }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, frame.getRightColor())
    }

    @Test
    fun testBottomText() {
        val frame = SwingFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            bottomTextPublisher = "4.7% SWING LIB TO CON".asOneTimePublisher(),
            bottomColorPublisher = Color.BLUE.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getBottomColor() }, IsEqual(Color.BLUE))
        Assert.assertEquals("4.7% SWING LIB TO CON", frame.getBottomText())
    }

    @Test
    @Throws(IOException::class)
    fun testRenderNoSwing() {
        val frame = SwingFrame(
            headerPublisher = "SWING SINCE 2015".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 0.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            bottomTextPublisher = "WAITING FOR RESULTS...".asOneTimePublisher(),
            bottomColorPublisher = Color.BLACK.asOneTimePublisher()
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "NoSwing", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSwingRight() {
        val frame = SwingFrame(
            headerPublisher = "SWING SINCE 2015".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 4.7.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            bottomTextPublisher = "4.7% SWING LIB TO CON".asOneTimePublisher(),
            bottomColorPublisher = Color.BLUE.asOneTimePublisher()
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "SwingRight", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSwingLeft() {
        val frame = SwingFrame(
            headerPublisher = "SWING SINCE 2015".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = (-1.3).asOneTimePublisher(),
            leftColorPublisher = Color.RED.asOneTimePublisher(),
            rightColorPublisher = Color.ORANGE.asOneTimePublisher(),
            bottomTextPublisher = "1.3% SWING LIB TO NDP".asOneTimePublisher(),
            bottomColorPublisher = Color.ORANGE.asOneTimePublisher()
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "SwingLeft", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMaxSwingRight() {
        val frame = SwingFrame(
            headerPublisher = "SWING SINCE 2015".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 19.9.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            bottomTextPublisher = "19.9% SWING LIB TO CON".asOneTimePublisher(),
            bottomColorPublisher = Color.BLUE.asOneTimePublisher()
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "MaxSwingRight", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMaxSwingLeft() {
        val frame = SwingFrame(
            headerPublisher = "SWING SINCE 2015".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = (-21.6).asOneTimePublisher(),
            leftColorPublisher = Color.RED.asOneTimePublisher(),
            rightColorPublisher = Color.GREEN.asOneTimePublisher(),
            bottomTextPublisher = "21.6% SWING LIB TO GRN".asOneTimePublisher(),
            bottomColorPublisher = Color.GREEN.asOneTimePublisher()
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "MaxSwingLeft", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderAccents() {
        val frame = SwingFrame(
            headerPublisher = "CHANGES APR\u00c8S 2014".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 0.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            bottomTextPublisher = "VOIX PAS R\u00c9\u00c7US".asOneTimePublisher(),
            bottomColorPublisher = Color.BLACK.asOneTimePublisher()
        )
        frame.setSize(256, 128)
        compareRendering("SwingFrame", "Accents", frame)
    }
}
