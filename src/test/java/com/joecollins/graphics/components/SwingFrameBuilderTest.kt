package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.SwingFrameBuilder.Companion.basic
import com.joecollins.graphics.components.SwingFrameBuilder.Companion.prevCurr
import com.joecollins.graphics.components.SwingFrameBuilder.Companion.prevCurrNormalised
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.models.general.Party
import java.awt.Color
import org.junit.Assert
import org.junit.Test

class SwingFrameBuilderTest {
    private class SwingProperties(val leftColor: Color, val rightColor: Color, val value: Number, val text: String)

    @Test
    fun basicTest() {
        val swingProps = BindableWrapper(SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"))
        val frame = basic(
                swingProps.binding,
                { p: SwingProperties -> p.leftColor },
                { p: SwingProperties -> p.rightColor },
                { p: SwingProperties -> p.value },
                { p: SwingProperties -> p.text })
                .withRange(fixedBinding(0.10))
                .withHeader(fixedBinding("SWING"))
                .build()
        Assert.assertEquals(Color.RED, frame.getLeftColor())
        Assert.assertEquals(Color.BLUE, frame.getRightColor())
        Assert.assertEquals(Color.RED, frame.getBottomColor())
        Assert.assertEquals(0.02, frame.getValue().toDouble(), 1e-6)
        Assert.assertEquals("2% SWING", frame.getBottomText())
        Assert.assertEquals(0.10, frame.getRange())
        Assert.assertEquals("SWING", frame.header)
        swingProps.value = SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING")
        Assert.assertEquals(Color.GREEN, frame.getLeftColor())
        Assert.assertEquals(Color.ORANGE, frame.getRightColor())
        Assert.assertEquals(Color.ORANGE, frame.getBottomColor())
        Assert.assertEquals(-0.05, frame.getValue().toDouble(), 1e-6)
        Assert.assertEquals("5% SWING", frame.getBottomText())
    }

    @Test
    fun testNeutralBottomColor() {
        val swingProps = BindableWrapper(SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"))
        val neutralColor = BindableWrapper(Color.GRAY)
        val frame = basic(
                swingProps.binding,
                { p: SwingProperties -> p.leftColor },
                { p: SwingProperties -> p.rightColor },
                { p: SwingProperties -> p.value },
                { p: SwingProperties -> p.text })
                .withRange(fixedBinding(0.10))
                .withNeutralColor(neutralColor.binding)
                .build()
        Assert.assertEquals(Color.RED, frame.getBottomColor())
        swingProps.value = SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING")
        Assert.assertEquals(Color.ORANGE, frame.getBottomColor())
        neutralColor.value = Color.LIGHT_GRAY
        Assert.assertEquals(Color.ORANGE, frame.getBottomColor())
        swingProps.value = SwingProperties(Color.GREEN, Color.ORANGE, 0.00, "NO SWING")
        Assert.assertEquals(Color.LIGHT_GRAY, frame.getBottomColor())
        neutralColor.value = Color.BLACK
        Assert.assertEquals(Color.BLACK, frame.getBottomColor())
    }

    @Test
    fun testSwingPrevCurrTwoMainPartiesSwingRight() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 25, con to 15, ndp to 10))
        val currBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 16, con to 13, ndp to 11))
        // LIB: 50.00 -> 40.00 (-10.00)
        // CON: 30.00 -> 32.25 (+ 2.25)
        // NDP: 20.00 -> 27.75 (+ 7.75)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(
                prevBinding, currBinding, compareBy { partyOrder.indexOf(it) })
                .build()
        Assert.assertEquals(Color.BLUE, swingFrame.getLeftColor())
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
        val prevBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 25, con to 15, ndp to 10))
        val currBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 26, con to 10, ndp to 4))
        // LIB: 50.00 -> 65.00 (+15.00)
        // CON: 30.00 -> 25.00 (- 5.00)
        // NDP: 20.00 -> 10.00 (-10.00)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(prevBinding, currBinding, compareBy { it: Party -> partyOrder.indexOf(it) }).build()
        Assert.assertEquals(Color.BLUE, swingFrame.getLeftColor())
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
        val prevBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 25, con to 15, ndp to 10))
        val currBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 26, con to 10, ndp to 4))
        // LIB: 50.00 -> 65.00 (+15.00)
        // CON: 30.00 -> 25.00 (- 5.00)
        // NDP: 20.00 -> 10.00 (-10.00)
        val partyOrder: List<Party> = listOf()
        val swingFrame = prevCurr(
                prevBinding, currBinding, compareBy { it: Party -> partyOrder.indexOf(it) })
                .build()
        Assert.assertEquals(
                setOf(Color.BLUE, Color.RED),
                setOf(swingFrame.getLeftColor(), swingFrame.getRightColor()))
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
        val prevBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 15, con to 25, ndp to 10))
        val currBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 6, con to 10, ndp to 24))
        // LIB: 30.00 -> 15.00 (-15.00)
        // CON: 50.00 -> 25.00 (-25.00)
        // NDP: 20.00 -> 60.00 (+40.00)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(prevBinding, currBinding, compareBy { it: Party -> partyOrder.indexOf(it) }).build()
        Assert.assertEquals(Color.BLUE, swingFrame.getLeftColor())
        Assert.assertEquals(Color.ORANGE, swingFrame.getRightColor())
        Assert.assertEquals(Color.ORANGE, swingFrame.getBottomColor())
        Assert.assertEquals(-0.325, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("32.5% SWING CON TO NDP", swingFrame.getBottomText())
    }

    @Test
    fun testNoSwingBetweenParties() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 15, con to 25, ndp to 10))
        val currBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 15, con to 25, ndp to 10))
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(prevBinding, currBinding, Comparator.comparing { o: Party -> partyOrder.indexOf(o) }).build()
        Assert.assertEquals(Color.BLUE, swingFrame.getLeftColor())
        Assert.assertEquals(Color.RED, swingFrame.getRightColor())
        Assert.assertEquals(Color.LIGHT_GRAY, swingFrame.getBottomColor())
        Assert.assertEquals(0.0, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("NO SWING", swingFrame.getBottomText())
    }

    @Test
    fun testNoSwingAvailable() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding: Binding<Map<Party, Int>> = fixedBinding(mapOf(lib to 15, con to 25, ndp to 10))
        val currBinding: Binding<Map<Party, Int>> = fixedBinding(emptyMap())
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurr(prevBinding, currBinding, Comparator.comparing { o: Party -> partyOrder.indexOf(o) }).build()
        Assert.assertEquals(Color.LIGHT_GRAY, swingFrame.getLeftColor())
        Assert.assertEquals(Color.LIGHT_GRAY, swingFrame.getRightColor())
        Assert.assertEquals(Color.LIGHT_GRAY, swingFrame.getBottomColor())
        Assert.assertEquals(0.0, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("NOT AVAILABLE", swingFrame.getBottomText())
    }

    @Test
    fun testSwingPrevCurrTwoMainPartiesNormalised() {
        val lib = Party("LIBERAL", "LIB", Color.RED)
        val con = Party("CONSERVATIVE", "CON", Color.BLUE)
        val ndp = Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE)
        val prevBinding: Binding<Map<Party, Double>> = fixedBinding(mapOf(lib to 0.40, con to 0.30, ndp to 0.20))
        val currBinding: Binding<Map<Party, Double>> = fixedBinding(mapOf(lib to 0.38, con to 0.35, ndp to 0.18))
        // LIB: 40.00 -> 38.00 (- 2.00)
        // CON: 30.00 -> 35.00 (+ 5.00)
        // NDP: 20.00 -> 18.00 (- 2.00)
        val partyOrder = listOf(ndp, lib, con)
        val swingFrame = prevCurrNormalised(prevBinding, currBinding, Comparator.comparing { o: Party -> partyOrder.indexOf(o) }).build()
        Assert.assertEquals(Color.BLUE, swingFrame.getLeftColor())
        Assert.assertEquals(Color.RED, swingFrame.getRightColor())
        Assert.assertEquals(Color.BLUE, swingFrame.getBottomColor())
        Assert.assertEquals(0.035, swingFrame.getValue().toDouble(), 1e-6)
        Assert.assertEquals(0.1, swingFrame.getRange().toDouble(), 1e-6)
        Assert.assertEquals("3.5% SWING LIB TO CON", swingFrame.getBottomText())
    }
}
