package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import kotlin.math.abs
import org.junit.Assert
import org.junit.Test

class SwingometerFrameTest {
    @Test
    fun testColors() {
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = fixedBinding(emptyList())
        )
        Assert.assertEquals(Color.BLUE, frame.leftColor)
        Assert.assertEquals(Color.RED, frame.rightColor)
    }

    @Test
    fun testValue() {
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = fixedBinding(emptyList())
        )
        Assert.assertEquals(3, frame.value)
    }

    @Test
    fun testRange() {
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = fixedBinding(emptyList())
        )
        Assert.assertEquals(10, frame.range)
    }

    @Test
    fun testTicks() {
        val ticks = (-10..10).toList()
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = fixedBinding(emptyList()),
            ticksBinding = fixedBinding(
                ticks.map { SwingometerFrame.Tick(it, abs(it).toString()) }
            )
        )
        Assert.assertEquals(21, frame.numTicks.toLong())
        for (i in 0..20) {
            Assert.assertEquals("Position at index $i", i - 10, frame.getTickPosition(i))
        }
        for (i in 0..10) {
            val text = i.toString()
            val leftPos = 10 - i
            val rightPos = 10 + i
            Assert.assertEquals("Text at index $leftPos", text, frame.getTickText(leftPos))
            Assert.assertEquals("Text at index $rightPos", text, frame.getTickText(rightPos))
        }
    }

    @Test
    fun testWinningPoint() {
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            leftToWinBinding = fixedBinding(3.0),
            rightToWinBinding = fixedBinding(-2.0),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = fixedBinding(emptyList())
        )
        Assert.assertEquals(3.0, frame.leftToWin)
        Assert.assertEquals(-2.0, frame.rightToWin)
    }

    @Test
    fun testOuterLabels() {
        val labels = fixedBinding(listOf(
            Triple(0.0, "50", Color.BLACK),
            Triple(5.0, "75", Color.RED),
            Triple(-5.0, "60", Color.BLUE)
        ))
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = fixedBinding(emptyList()),
            outerLabelsBinding = labels.mapElements { SwingometerFrame.OuterLabel(it.first, it.second, it.third) }
        )
        Assert.assertEquals(3, frame.numOuterLabels.toLong())
        Assert.assertEquals(0.0, frame.getOuterLabelPosition(0))
        Assert.assertEquals("75", frame.getOuterLabelText(1))
        Assert.assertEquals(Color.BLUE, frame.getOuterLabelColor(2))
    }

    @Test
    fun testBuckets() {
        val dots = fixedBinding(listOf(
            Pair(0.3, Color.BLUE),
            Pair(-0.7, Color.RED),
            Pair(2.4, Color.BLACK)
        ))
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = dots.mapElements { SwingometerFrame.Dot(it.first, it.second) }
        )
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
        Assert.assertEquals(3, frame.numDots.toLong())
        Assert.assertEquals(0.3, frame.getDotPosition(0))
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
    }

    @Test
    fun testDotLabels() {
        val dots = fixedBinding(listOf(
            Triple(0.3, Color.BLUE, "A"),
            Triple(-0.7, Color.RED, "B"),
            Triple(2.4, Color.BLACK, "C")
        ))
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = dots.mapElements { SwingometerFrame.Dot(it.first, it.second, it.third) }
        )
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
        Assert.assertEquals(3, frame.numDots.toLong())
        Assert.assertEquals(0.3, frame.getDotPosition(0))
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertEquals("C", frame.getDotLabel(2))
        Assert.assertTrue(frame.isDotSolid(2))
    }

    @Test
    fun testDotEmpty() {
        val dots = fixedBinding(listOf(
            Triple(0.3, Color.BLUE, true),
            Triple(-0.7, Color.RED, false),
            Triple(2.4, Color.BLACK, true)
        ))
        val frame = SwingometerFrame(
            headerBinding = fixedBinding(null),
            valueBinding = fixedBinding(3),
            rangeBinding = fixedBinding(10),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = dots.mapElements { SwingometerFrame.Dot(it.first, it.second, solid = it.third) }
        )
        Assert.assertEquals(20, frame.numBucketsPerSide.toLong())
        Assert.assertEquals(3, frame.numDots.toLong())
        Assert.assertEquals(0.3, frame.getDotPosition(0))
        Assert.assertEquals(Color.RED, frame.getDotColor(1))
        Assert.assertTrue(frame.isDotSolid(0))
        Assert.assertFalse(frame.isDotSolid(1))
        Assert.assertTrue(frame.isDotSolid(2))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderBasic() {
        val ticks = (-9..9).toList()
        val outerLabels = fixedBinding(listOf(
            Triple(0.0, 51, Color.RED),
            Triple(-1.55, 51, Color.BLUE),
            Triple(-7.8, 52, Color.BLUE),
            Triple(-8.3, 54, Color.BLUE),
            Triple(2.85, 55, Color.RED),
            Triple(4.55, 60, Color.RED),
            Triple(9.75, 65, Color.RED)
        ))
        val dots = createSwingometerDotsWithoutLabels()
        val frame = SwingometerFrame(
            headerBinding = fixedBinding("2018 SENATE SWINGOMETER"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(-4.0),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            leftToWinBinding = fixedBinding(1.55),
            rightToWinBinding = fixedBinding(-0.60),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = dots.mapElements { SwingometerFrame.Dot(it.second, it.third) },
            ticksBinding = fixedBinding(
                ticks.map { SwingometerFrame.Tick(it, it.toString()) }
            ),
            outerLabelsBinding = outerLabels.mapElements { SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third) }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "Unlabelled", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderBasicTinyDots() {
        val ticks = (-9..9).toList()
        val outerLabels = fixedBinding(listOf(
            Triple(0.0, 51, Color.RED),
            Triple(-1.55, 51, Color.BLUE),
            Triple(-7.8, 52, Color.BLUE),
            Triple(-8.3, 54, Color.BLUE),
            Triple(2.85, 55, Color.RED),
            Triple(4.55, 60, Color.RED),
            Triple(9.75, 65, Color.RED)
        ))
        val dots = createSwingometerDotsWithoutLabels()
        val frame = SwingometerFrame(
            headerBinding = fixedBinding("2018 SENATE SWINGOMETER"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(-4.0),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            leftToWinBinding = fixedBinding(1.55),
            rightToWinBinding = fixedBinding(-0.60),
            numBucketsPerSideBinding = fixedBinding(80),
            dotsBinding = dots.mapElements { SwingometerFrame.Dot(it.second, it.third) },
            ticksBinding = fixedBinding(
                ticks.map { SwingometerFrame.Tick(it, it.toString()) }
            ),
            outerLabelsBinding = outerLabels.mapElements { SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third) }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "Unlabelled-TinyDots", frame)
    }

    private fun createSwingometerDotsWithoutLabels(): Binding<List<Triple<String, Double, Color>>> {
        val dots = ArrayList<Triple<String, Double, Color>>()
        dots.add(Triple("WY", -27.00, Color.RED))
        dots.add(Triple("UT", -17.65, Color.RED))
        dots.add(Triple("TN", -17.25, Color.RED))
        dots.add(Triple("MS (S)", -11.00, Color.RED))
        dots.add(Triple("MS", -8.30, Color.RED))
        dots.add(Triple("TX", -7.90, Color.RED))
        dots.add(Triple("NE", -7.80, Color.RED))
        dots.add(Triple("AZ", -1.55, Color.BLUE))
        dots.add(Triple("NV", -0.60, Color.BLUE))
        dots.add(Triple("ND", +0.45, Color.RED))
        dots.add(Triple("MT", +1.85, Color.BLUE))
        dots.add(Triple("WI", +2.75, Color.BLUE))
        dots.add(Triple("IN", +2.90, Color.RED))
        dots.add(Triple("NM", +2.85, Color.BLUE))
        dots.add(Triple("VA", +2.95, Color.BLUE))
        dots.add(Triple("OH", +3.00, Color.BLUE))
        dots.add(Triple("MA", +3.70, Color.BLUE))
        dots.add(Triple("PA", +4.55, Color.BLUE))
        dots.add(Triple("MN (S)", +5.15, Color.BLUE))
        dots.add(Triple("CT", +5.85, Color.BLUE))
        dots.add(Triple("FL", +6.50, Color.RED))
        dots.add(Triple("MO", +7.90, Color.RED))
        dots.add(Triple("NJ", +9.75, Color.BLUE))
        dots.add(Triple("MI", +10.40, Color.BLUE))
        dots.add(Triple("WA", +10.50, Color.BLUE))
        dots.add(Triple("ME", +11.10, Color.BLUE))
        dots.add(Triple("WV", +12.05, Color.BLUE))
        dots.add(Triple("CA", +12.50, Color.BLUE))
        dots.add(Triple("HI", +12.60, Color.BLUE))
        dots.add(Triple("MD", +14.85, Color.BLUE))
        dots.add(Triple("RI", +14.90, Color.BLUE))
        dots.add(Triple("MN", +17.30, Color.BLUE))
        dots.add(Triple("DE", +18.70, Color.BLUE))
        dots.add(Triple("NY", +22.30, Color.BLUE))
        dots.add(Triple("VT", +23.05, Color.BLUE))
        return fixedBinding(dots)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderLabels() {
        val ticks = (-9..9).toList()
        val outerLabels = fixedBinding(listOf(
            Triple(0.0, 332, Color.BLUE),
            Triple(-3.91, 350, Color.BLUE),
            Triple(-5.235, 400, Color.BLUE),
            Triple(-7.895, 450, Color.BLUE),
            Triple(2.68, 270, Color.RED),
            Triple(5.075, 350, Color.RED),
            Triple(8.665, 400, Color.RED)
        ))
        val dots = createSwingometerDotsWithLabels()
        val frame = SwingometerFrame(
            headerBinding = fixedBinding("2016 PRESIDENT SWINGOMETER"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(0.885),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            leftToWinBinding = fixedBinding(-2.68),
            rightToWinBinding = fixedBinding(2.68),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = dots.mapElements {
                SwingometerFrame.Dot(
                    it.second,
                    it.third,
                    label = it.first.second.toString()
                )
            },
            ticksBinding = fixedBinding(
                ticks.map { SwingometerFrame.Tick(it, it.toString()) }
            ),
            outerLabelsBinding = outerLabels.mapElements { SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third) }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "Labels", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderEmptyDots() {
        val ticks = (-9..9).toList()
        val outerLabels = fixedBinding(listOf(
            Triple(0.0, 332, Color.BLUE),
            Triple(-3.91, 350, Color.BLUE),
            Triple(-5.235, 400, Color.BLUE),
            Triple(-7.895, 450, Color.BLUE),
            Triple(2.68, 270, Color.RED),
            Triple(5.075, 350, Color.RED),
            Triple(8.665, 400, Color.RED)
        ))
        val dots = createSwingometerDotsWithLabels()
        val frame = SwingometerFrame(
            headerBinding = fixedBinding("2016 PRESIDENT SWINGOMETER"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(0.885),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            leftToWinBinding = fixedBinding(-2.68),
            rightToWinBinding = fixedBinding(2.68),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = dots.mapElements {
                SwingometerFrame.Dot(
                    it.second,
                    it.third,
                    label = it.first.second.toString(),
                    solid = it.second > -5 && it.second < 0
                )
            },
            ticksBinding = fixedBinding(
                ticks.map { SwingometerFrame.Tick(it, it.toString()) }
            ),
            outerLabelsBinding = outerLabels.mapElements { SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third) }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "EmptyDots", frame)
    }

    private fun createSwingometerDotsWithLabels(): Binding<List<Triple<Pair<String, Int>, Double, Color>>> {
        val dots = ArrayList<Triple<Pair<String, Int>, Double, Color>>()
        dots.add(Triple(Pair("UT", 6), -24.02, Color.RED))
        dots.add(Triple(Pair("NE-03", 1), -21.31, Color.RED))
        dots.add(Triple(Pair("WY", 3), -20.41, Color.RED))
        dots.add(Triple(Pair("OK", 7), -17.27, Color.RED))
        dots.add(Triple(Pair("ID", 4), -15.945, Color.RED))
        dots.add(Triple(Pair("WV", 5), -13.42, Color.RED))
        dots.add(Triple(Pair("AR", 6), -11.855, Color.RED))
        dots.add(Triple(Pair("KY", 8), -11.345, Color.RED))
        dots.add(Triple(Pair("AL", 9), -11.095, Color.RED))
        dots.add(Triple(Pair("NE-AL", 2), -10.885, Color.RED))
        dots.add(Triple(Pair("KS", 6), -10.86, Color.RED))
        dots.add(Triple(Pair("TN", 11), -10.20, Color.RED))
        dots.add(Triple(Pair("ND", 3), -9.815, Color.RED))
        dots.add(Triple(Pair("SD", 3), -9.01, Color.RED))
        dots.add(Triple(Pair("LA", 8), -8.60, Color.RED))
        dots.add(Triple(Pair("NE-01", 1), -8.30, Color.RED))
        dots.add(Triple(Pair("TX", 38), -7.895, Color.RED))
        dots.add(Triple(Pair("AK", 3), -6.995, Color.RED))
        dots.add(Triple(Pair("MT", 3), -6.825, Color.RED))
        dots.add(Triple(Pair("MS", 6), -5.75, Color.RED))
        dots.add(Triple(Pair("SC", 9), -5.235, Color.RED))
        dots.add(Triple(Pair("IN", 11), -5.10, Color.RED))
        dots.add(Triple(Pair("MO", 10), -4.69, Color.RED))
        dots.add(Triple(Pair("AZ", 11), -4.53, Color.RED))
        dots.add(Triple(Pair("GA", 16), -3.91, Color.RED))
        dots.add(Triple(Pair("NE-02", 1), -3.575, Color.RED))
        dots.add(Triple(Pair("NC", 15), -1.02, Color.RED))
        dots.add(Triple(Pair("FL", 29), +0.44, Color.RED))
        dots.add(Triple(Pair("OH", 18), +1.49, Color.RED))
        dots.add(Triple(Pair("VA", 13), +1.94, Color.BLUE))
        dots.add(Triple(Pair("CO", 9), +2.68, Color.BLUE))
        dots.add(Triple(Pair("PA", 20), +2.69, Color.RED))
        dots.add(Triple(Pair("NH", 4), +2.79, Color.BLUE))
        dots.add(Triple(Pair("IA", 6), +2.905, Color.RED))
        dots.add(Triple(Pair("NV", 6), +3.34, Color.BLUE))
        dots.add(Triple(Pair("MN", 10), +3.845, Color.BLUE))
        dots.add(Triple(Pair("ME-02", 1), +4.28, Color.RED))
        dots.add(Triple(Pair("WI", 10), +4.47, Color.RED))
        dots.add(Triple(Pair("MI", 16), +4.75, Color.RED))
        dots.add(Triple(Pair("NM", 5), +5.075, Color.BLUE))
        dots.add(Triple(Pair("OR", 7), +6.045, Color.BLUE))
        dots.add(Triple(Pair("WA", 12), +7.435, Color.BLUE))
        dots.add(Triple(Pair("ME-AL", 2), +7.645, Color.BLUE))
        dots.add(Triple(Pair("IL", 20), +8.435, Color.BLUE))
        dots.add(Triple(Pair("CT", 7), +8.665, Color.BLUE))
        dots.add(Triple(Pair("NJ", 14), +8.895, Color.BLUE))
        dots.add(Triple(Pair("DE", 3), +9.315, Color.BLUE))
        dots.add(Triple(Pair("ME-01", 1), +10.695, Color.BLUE))
        dots.add(Triple(Pair("CA", 55), +11.56, Color.BLUE))
        dots.add(Triple(Pair("MA", 11), +11.57, Color.BLUE))
        dots.add(Triple(Pair("MD", 10), +13.035, Color.BLUE))
        dots.add(Triple(Pair("RI", 4), +13.92, Color.BLUE))
        dots.add(Triple(Pair("NY", 29), +14.09, Color.BLUE))
        dots.add(Triple(Pair("VT", 3), +17.80, Color.BLUE))
        dots.add(Triple(Pair("HI", 4), +21.355, Color.BLUE))
        dots.add(Triple(Pair("DC", 3), +46.815, Color.BLUE))
        return fixedBinding(dots)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMultiLineLabels() {
        val ticks = (-9..9).toList()
        val outerLabels = fixedBinding(listOf(
            Triple(0.0, 332, Color.BLUE),
            Triple(-3.91, 350, Color.BLUE),
            Triple(-5.235, 400, Color.BLUE),
            Triple(-7.895, 450, Color.BLUE),
            Triple(2.68, 270, Color.RED),
            Triple(5.075, 350, Color.RED),
            Triple(8.665, 400, Color.RED)
        ))
        val dots = createSwingometerDotsWithLabels()
        val frame = SwingometerFrame(
            headerBinding = fixedBinding("2016 PRESIDENT SWINGOMETER"),
            rangeBinding = fixedBinding(10),
            valueBinding = fixedBinding(0.885),
            leftColorBinding = fixedBinding(Color.BLUE),
            rightColorBinding = fixedBinding(Color.RED),
            leftToWinBinding = fixedBinding(-2.68),
            rightToWinBinding = fixedBinding(2.68),
            numBucketsPerSideBinding = fixedBinding(20),
            dotsBinding = dots.mapElements {
                SwingometerFrame.Dot(
                    it.second,
                    it.third,
                    label = if (it.first.first.contains("-")) {
                        it.first.first.replace("-".toRegex(), "\n-")
                    } else {
                        "${it.first.first}\n(${it.first.second})"
                    }
                )
            },
            ticksBinding = fixedBinding(
                ticks.map { SwingometerFrame.Tick(it, it.toString()) }
            ),
            outerLabelsBinding = outerLabels.mapElements { SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third) }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "MultiLineLabels", frame)
    }
}
