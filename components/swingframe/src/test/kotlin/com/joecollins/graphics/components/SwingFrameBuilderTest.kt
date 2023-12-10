package com.joecollins.graphics.components

import com.joecollins.graphics.components.SwingFrameBuilder.basic
import com.joecollins.graphics.components.SwingFrameBuilder.prevCurr
import com.joecollins.graphics.components.SwingFrameBuilder.prevCurrNormalised
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color

class SwingFrameBuilderTest {
    private class SwingProperties(val leftColor: Color, val rightColor: Color, val value: Number, val text: String)

    @Test
    fun basicTest() {
        val swingProps = Publisher(SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"))
        val frame = basic(
            item = swingProps,
            leftColor = { leftColor },
            rightColor = { rightColor },
            value = { value },
            text = { text },
            range = 0.10.asOneTimePublisher(),
            header = "SWING".asOneTimePublisher(),
        )
        assertEquals(Color.RED, frame.getLeftColor())
        assertEquals(Color.BLUE, frame.getRightColor())
        assertEquals(Color.RED, frame.getBottomColor())
        assertEquals(0.02, frame.getValue().toDouble(), 1e-6)
        assertEquals("2% SWING", frame.getBottomText())
        assertEquals(0.10, frame.getRange())
        assertEquals("SWING", frame.header)

        swingProps.submit(SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING"))
        assertEquals(Color.GREEN, frame.getLeftColor())
        assertEquals(Color.ORANGE, frame.getRightColor())
        assertEquals(Color.ORANGE, frame.getBottomColor())
        assertEquals(-0.05, frame.getValue().toDouble(), 1e-6)
        assertEquals("5% SWING", frame.getBottomText())
    }

    @Test
    fun testNeutralBottomColor() {
        val swingProps = Publisher(SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"))
        val neutralColor = Publisher(Color.GRAY)
        val frame = basic(
            item = swingProps,
            leftColor = { leftColor },
            rightColor = { rightColor },
            value = { value },
            text = { text },
            neutralColor = neutralColor,
            range = 0.10.asOneTimePublisher(),
            header = null.asOneTimePublisher(),
        )
        assertEquals(Color.RED, frame.getBottomColor())

        swingProps.submit(SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING"))
        assertEquals(Color.ORANGE, frame.getBottomColor())

        neutralColor.submit(Color.LIGHT_GRAY)
        assertEquals(Color.ORANGE, frame.getBottomColor())

        swingProps.submit(SwingProperties(Color.GREEN, Color.ORANGE, 0.00, "NO SWING"))
        assertEquals(Color.LIGHT_GRAY, frame.getBottomColor())

        neutralColor.submit(Color.BLACK)
        assertEquals(Color.BLACK, frame.getBottomColor())
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
            prev = prevBinding,
            curr = currBinding,
            partyOrder = partyOrder,
            header = null.asOneTimePublisher(),
        )
        assertEquals(Color.BLUE, swingFrame.getLeftColor())
        assertEquals(Color.RED, swingFrame.getRightColor())
        assertEquals(Color.BLUE, swingFrame.getBottomColor())
        assertEquals(0.0625, swingFrame.getValue().toDouble(), 1e-6)
        assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        assertEquals("6.2% SWING LIB TO CON", swingFrame.getBottomText())
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
        val swingFrame = prevCurr(
            prev = prevBinding,
            curr = currBinding,
            partyOrder = partyOrder,
            header = null.asOneTimePublisher(),
        )
        assertEquals(Color.BLUE, swingFrame.getLeftColor())
        assertEquals(Color.RED, swingFrame.getRightColor())
        assertEquals(Color.RED, swingFrame.getBottomColor())
        assertEquals(-0.1, swingFrame.getValue().toDouble(), 1e-6)
        assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        assertEquals("10.0% SWING CON TO LIB", swingFrame.getBottomText())
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
            prev = prevBinding,
            curr = currBinding,
            partyOrder = partyOrder,
            header = null.asOneTimePublisher(),
        )
        assertEquals("10.0% SWING CON TO LIB", swingFrame.getBottomText())
        assertEquals(
            setOf(Color.BLUE, Color.RED),
            setOf(swingFrame.getLeftColor(), swingFrame.getRightColor()),
        )
        //    assertEquals(Color.RED, swingFrame.getBottomColor());
        //    assertEquals(0.1 * (swingFrame.getLeftColor().equals(Color.BLUE) ? -1 : 1),
        // swingFrame.getValue().doubleValue(), 1e-6);
        assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        assertEquals("10.0% SWING CON TO LIB", swingFrame.getBottomText())
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
        val swingFrame = prevCurr(
            prev = prevBinding,
            curr = currBinding,
            partyOrder = partyOrder,
            header = null.asOneTimePublisher(),
        )
        assertEquals(Color.BLUE, swingFrame.getLeftColor())
        assertEquals(Color.ORANGE, swingFrame.getRightColor())
        assertEquals(Color.ORANGE, swingFrame.getBottomColor())
        assertEquals(-0.325, swingFrame.getValue().toDouble(), 1e-6)
        assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        assertEquals("32.5% SWING CON TO NDP", swingFrame.getBottomText())
    }

    @Test
    fun testSwingPrevCurrPartiesSwingSpecifyParties() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 15, con to 25, ndp to 10).asOneTimePublisher()
        val currBinding = mapOf(lib to 6, con to 10, ndp to 24).asOneTimePublisher()
        // LIB: 30.00 -> 15.00 (-15.00)
        // CON: 50.00 -> 25.00 (-25.00)
        // NDP: 20.00 -> 60.00 (+40.00)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(
            prev = prevBinding,
            curr = currBinding,
            partyOrder = partyOrder,
            selectedParties = setOf(lib, con).asOneTimePublisher(),
            header = null.asOneTimePublisher(),
        )
        assertEquals(Color.BLUE, swingFrame.getLeftColor())
        assertEquals(Color.RED, swingFrame.getRightColor())
        assertEquals(Color.RED, swingFrame.getBottomColor())
        assertEquals(-0.05, swingFrame.getValue().toDouble(), 1e-6)
        assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        assertEquals("5.0% SWING CON TO LIB", swingFrame.getBottomText())
    }

    @Test
    fun testNoSwingBetweenParties() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 15, con to 25, ndp to 10).asOneTimePublisher()
        val currBinding = mapOf(lib to 15, con to 25, ndp to 10).asOneTimePublisher()
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(
            prev = prevBinding,
            curr = currBinding,
            partyOrder = partyOrder,
            header = null.asOneTimePublisher(),
        )
        assertEquals(Color.BLUE, swingFrame.getLeftColor())
        assertEquals(Color.RED, swingFrame.getRightColor())
        assertEquals(Color.LIGHT_GRAY, swingFrame.getBottomColor())
        assertEquals(0.0, swingFrame.getValue().toDouble(), 1e-6)
        assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        assertEquals("NO SWING", swingFrame.getBottomText())
    }

    @Test
    fun testNoSwingAvailable() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding = mapOf(lib to 15, con to 25, ndp to 10).asOneTimePublisher()
        val currBinding = emptyMap<Party, Int>().asOneTimePublisher()
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(
            prev = prevBinding,
            curr = currBinding,
            partyOrder = partyOrder,
            header = null.asOneTimePublisher(),
        )
        assertEquals(Color.LIGHT_GRAY, swingFrame.getLeftColor())
        assertEquals(Color.LIGHT_GRAY, swingFrame.getRightColor())
        assertEquals(Color.LIGHT_GRAY, swingFrame.getBottomColor())
        assertEquals(0.0, swingFrame.getValue().toDouble(), 1e-6)
        assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        assertEquals("NOT AVAILABLE", swingFrame.getBottomText())
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
        val swingFrame = prevCurrNormalised(
            prevPublisher = prevBinding,
            currPublisher = currBinding,
            partyOrder = partyOrder,
            header = null.asOneTimePublisher(),
        )
        assertEquals(Color.BLUE, swingFrame.getLeftColor())
        assertEquals(Color.RED, swingFrame.getRightColor())
        assertEquals(Color.BLUE, swingFrame.getBottomColor())
        assertEquals(0.035, swingFrame.getValue().toDouble(), 1e-6)
        assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        assertEquals("3.5% SWING LIB TO CON", swingFrame.getBottomText())
    }
}
