package com.joecollins.graphics.components

import com.joecollins.graphics.components.SwingFrameBuilder.Companion.basic
import com.joecollins.graphics.components.SwingFrameBuilder.Companion.prevCurr
import com.joecollins.graphics.components.SwingFrameBuilder.Companion.prevCurrNormalised
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.util.concurrent.TimeUnit

class SwingFrameBuilderTest {
    private class SwingProperties(val leftColor: Color, val rightColor: Color, val value: Number, val text: String)

    @Test
    fun basicTest() {
        val swingProps = Publisher(SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"))
        val frame = basic(
            swingProps,
            { p: SwingProperties -> p.leftColor },
            { p: SwingProperties -> p.rightColor },
            { p: SwingProperties -> p.value },
            { p: SwingProperties -> p.text }
        )
            .withRange(0.10.asOneTimePublisher())
            .withHeader("SWING".asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getLeftColor() }, IsEqual(Color.RED))
        Assert.assertEquals(Color.BLUE, frame.getRightColor())
        Assert.assertEquals(Color.RED, frame.getBottomColor())
        Assert.assertEquals(0.02, frame.getValue().toDouble(), 1e-6)
        Assert.assertEquals("2% SWING", frame.getBottomText())
        Assert.assertEquals(0.10, frame.getRange())
        Assert.assertEquals("SWING", frame.header)
        swingProps.submit(SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING"))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getLeftColor() }, IsEqual(Color.GREEN))
        Assert.assertEquals(Color.ORANGE, frame.getRightColor())
        Assert.assertEquals(ColorUtils.contrastForBackground(Color.ORANGE), frame.getBottomColor())
        Assert.assertEquals(-0.05, frame.getValue().toDouble(), 1e-6)
        Assert.assertEquals("5% SWING", frame.getBottomText())
    }

    @Test
    fun testNeutralBottomColor() {
        val swingProps = Publisher(SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"))
        val neutralColor = Publisher(Color.GRAY)
        val frame = basic(
            swingProps,
            { p: SwingProperties -> p.leftColor },
            { p: SwingProperties -> p.rightColor },
            { p: SwingProperties -> p.value },
            { p: SwingProperties -> p.text }
        )
            .withRange(0.10.asOneTimePublisher())
            .withNeutralColor(neutralColor)
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getBottomColor() }, IsEqual(Color.RED))
        swingProps.submit(SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING"))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getBottomColor() }, IsEqual(ColorUtils.contrastForBackground(Color.ORANGE)))
        neutralColor.submit(Color.LIGHT_GRAY)
        Awaitility.await().atMost(600, TimeUnit.MILLISECONDS).pollDelay(500, TimeUnit.MILLISECONDS)
            .until({ frame.getBottomColor() }, IsEqual(ColorUtils.contrastForBackground(Color.ORANGE)))
        swingProps.submit(SwingProperties(Color.GREEN, Color.ORANGE, 0.00, "NO SWING"))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getBottomColor() }, IsEqual(ColorUtils.contrastForBackground(Color.LIGHT_GRAY)))
        neutralColor.submit(Color.BLACK)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getBottomColor() }, IsEqual(Color.BLACK))
    }

    @Test
    fun testSwingPrevCurrTwoMainPartiesSwingRight() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 25, con to 15, ndp to 10).asOneTimePublisher()
        val currBinding = mapOf(lib to 16, con to 13, ndp to 11).asOneTimePublisher()
        // LIB: 50.00 -> 40.00 (-10.00)
        // CON: 30.00 -> 32.25 (+ 2.25)
        // NDP: 20.00 -> 27.75 (+ 7.75)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(
            prevBinding, currBinding, compareBy { partyOrder.indexOf(it) }
        )
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ swingFrame.getLeftColor() }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, swingFrame.getRightColor())
        Assert.assertEquals(Color.BLUE, swingFrame.getBottomColor())
        Assert.assertEquals(0.0625, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("6.2% SWING LIB TO CON", swingFrame.getBottomText())
    }

    @Test
    fun testSwingPrevCurrTwoMainPartiesSwingLeft() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 25, con to 15, ndp to 10).asOneTimePublisher()
        val currBinding = mapOf(lib to 26, con to 10, ndp to 4).asOneTimePublisher()
        // LIB: 50.00 -> 65.00 (+15.00)
        // CON: 30.00 -> 25.00 (- 5.00)
        // NDP: 20.00 -> 10.00 (-10.00)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(prevBinding, currBinding, compareBy { partyOrder.indexOf(it) }).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ swingFrame.getLeftColor() }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, swingFrame.getRightColor())
        Assert.assertEquals(Color.RED, swingFrame.getBottomColor())
        Assert.assertEquals(-0.1, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("10.0% SWING CON TO LIB", swingFrame.getBottomText())
    }

    @Test
    fun testSwingPrevCurrPartiesNotInComparator() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 25, con to 15, ndp to 10).asOneTimePublisher()
        val currBinding = mapOf(lib to 26, con to 10, ndp to 4).asOneTimePublisher()
        // LIB: 50.00 -> 65.00 (+15.00)
        // CON: 30.00 -> 25.00 (- 5.00)
        // NDP: 20.00 -> 10.00 (-10.00)
        val partyOrder: List<Party> = listOf()
        val swingFrame = prevCurr(
            prevBinding, currBinding, compareBy { partyOrder.indexOf(it) }
        )
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ swingFrame.getBottomText() }, IsEqual("10.0% SWING CON TO LIB"))
        Assert.assertEquals(
            setOf(Color.BLUE, Color.RED),
            setOf(swingFrame.getLeftColor(), swingFrame.getRightColor())
        )
        //    assertEquals(Color.RED, swingFrame.getBottomColor());
        //    assertEquals(0.1 * (swingFrame.getLeftColor().equals(Color.BLUE) ? -1 : 1),
        // swingFrame.getValue().doubleValue(), 1e-6);
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("10.0% SWING CON TO LIB", swingFrame.getBottomText())
    }

    @Test
    fun testSwingPrevCurrPartiesSwingLeftFromRight() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 15, con to 25, ndp to 10).asOneTimePublisher()
        val currBinding = mapOf(lib to 6, con to 10, ndp to 24).asOneTimePublisher()
        // LIB: 30.00 -> 15.00 (-15.00)
        // CON: 50.00 -> 25.00 (-25.00)
        // NDP: 20.00 -> 60.00 (+40.00)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(prevBinding, currBinding, compareBy { partyOrder.indexOf(it) }).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ swingFrame.getLeftColor() }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.ORANGE, swingFrame.getRightColor())
        Assert.assertEquals(ColorUtils.contrastForBackground(Color.ORANGE), swingFrame.getBottomColor())
        Assert.assertEquals(-0.325, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("32.5% SWING CON TO NDP", swingFrame.getBottomText())
    }

    @Test
    fun testNoSwingBetweenParties() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 15, con to 25, ndp to 10).asOneTimePublisher()
        val currBinding = mapOf(lib to 15, con to 25, ndp to 10).asOneTimePublisher()
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(prevBinding, currBinding, Comparator.comparing { o: Party -> partyOrder.indexOf(o) }).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ swingFrame.getLeftColor() }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, swingFrame.getRightColor())
        Assert.assertEquals(ColorUtils.contrastForBackground(Color.LIGHT_GRAY), swingFrame.getBottomColor())
        Assert.assertEquals(0.0, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("NO SWING", swingFrame.getBottomText())
    }

    @Test
    fun testNoSwingAvailable() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 15, con to 25, ndp to 10).asOneTimePublisher()
        val currBinding = emptyMap<Party, Int>().asOneTimePublisher()
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(prevBinding, currBinding, Comparator.comparing { o: Party -> partyOrder.indexOf(o) }).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ swingFrame.getLeftColor() }, IsEqual(Color.LIGHT_GRAY))
        Assert.assertEquals(Color.LIGHT_GRAY, swingFrame.getRightColor())
        Assert.assertEquals(ColorUtils.contrastForBackground(Color.LIGHT_GRAY), swingFrame.getBottomColor())
        Assert.assertEquals(0.0, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("NOT AVAILABLE", swingFrame.getBottomText())
    }

    @Test
    fun testSwingPrevCurrTwoMainPartiesNormalised() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 0.40, con to 0.30, ndp to 0.20).asOneTimePublisher()
        val currBinding = mapOf(lib to 0.38, con to 0.35, ndp to 0.18).asOneTimePublisher()
        // LIB: 40.00 -> 38.00 (- 2.00)
        // CON: 30.00 -> 35.00 (+ 5.00)
        // NDP: 20.00 -> 18.00 (- 2.00)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurrNormalised(prevBinding, currBinding, Comparator.comparing { o: Party -> partyOrder.indexOf(o) }).build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ swingFrame.getLeftColor() }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, swingFrame.getRightColor())
        Assert.assertEquals(Color.BLUE, swingFrame.getBottomColor())
        Assert.assertEquals(0.035, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("3.5% SWING LIB TO CON", swingFrame.getBottomText())
    }
}
