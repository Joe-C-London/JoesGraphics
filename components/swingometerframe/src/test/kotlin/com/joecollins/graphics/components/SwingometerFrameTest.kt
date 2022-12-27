package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class SwingometerFrameTest {
    @Test
    fun testColors() {
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = emptyList<SwingometerFrame.Dot>().asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftColor }, IsEqual(Color.BLUE))
        Assertions.assertEquals(Color.BLUE, frame.leftColor)
        Assertions.assertEquals(Color.RED, frame.rightColor)
    }

    @Test
    fun testValue() {
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = emptyList<SwingometerFrame.Dot>().asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.value }, IsEqual(3))
    }

    @Test
    fun testRange() {
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = emptyList<SwingometerFrame.Dot>().asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.range }, IsEqual(10))
    }

    @Test
    fun testTicks() {
        val ticks = (-10..10).toList()
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = emptyList<SwingometerFrame.Dot>().asOneTimePublisher(),
            ticksPublisher = ticks.map { SwingometerFrame.Tick(it, abs(it).toString()) }.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numTicks }, IsEqual(21))
        for (i in 0..20) {
            Assertions.assertEquals(i - 10, frame.getTickPosition(i), "Position at index $i")
        }
        for (i in 0..10) {
            val text = i.toString()
            val leftPos = 10 - i
            val rightPos = 10 + i
            Assertions.assertEquals(text, frame.getTickText(leftPos), "Text at index $leftPos")
            Assertions.assertEquals(text, frame.getTickText(rightPos), "Text at index $rightPos")
        }
    }

    @Test
    fun testWinningPoint() {
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            leftToWinPublisher = 3.0.asOneTimePublisher(),
            rightToWinPublisher = (-2.0).asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = emptyList<SwingometerFrame.Dot>().asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftToWin }, IsEqual(3.0))
        Assertions.assertEquals(-2.0, frame.rightToWin)
    }

    @Test
    fun testOuterLabels() {
        val labels =
            listOf(
                Triple(0.0, "50", Color.BLACK),
                Triple(5.0, "75", Color.RED),
                Triple(-5.0, "60", Color.BLUE)
            )
                .asOneTimePublisher()
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = emptyList<SwingometerFrame.Dot>().asOneTimePublisher(),
            outerLabelsPublisher = labels.mapElements { SwingometerFrame.OuterLabel(it.first, it.second, it.third) }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numOuterLabels }, IsEqual(3))
        Assertions.assertEquals(0.0, frame.getOuterLabelPosition(0))
        Assertions.assertEquals("75", frame.getOuterLabelText(1))
        Assertions.assertEquals(Color.BLUE, frame.getOuterLabelColor(2))
    }

    @Test
    fun testBuckets() {
        val dots = Publisher(
            listOf(
                Pair(0.3, Color.BLUE),
                Pair(-0.7, Color.RED),
                Pair(2.4, Color.BLACK)
            )
        )
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = dots.mapElements { SwingometerFrame.Dot(it.first, it.second) }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBucketsPerSide }, IsEqual(20))
        Assertions.assertEquals(3, frame.numDots.toLong())
        Assertions.assertEquals(0.3, frame.getDotPosition(0))
        Assertions.assertEquals(Color.RED, frame.getDotColor(1))
    }

    @Test
    fun testDotLabels() {
        val dots = Publisher(
            listOf(
                Triple(0.3, Color.BLUE, "A"),
                Triple(-0.7, Color.RED, "B"),
                Triple(2.4, Color.BLACK, "C")
            )
        )
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = dots.mapElements { SwingometerFrame.Dot(it.first, it.second, it.third) }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(3))
        Assertions.assertEquals(20, frame.numBucketsPerSide.toLong())
        Assertions.assertEquals(0.3, frame.getDotPosition(0))
        Assertions.assertEquals(Color.RED, frame.getDotColor(1))
        Assertions.assertEquals("C", frame.getDotLabel(2))
        Assertions.assertTrue(frame.isDotSolid(2))
    }

    @Test
    fun testDotEmpty() {
        val dots = Publisher(
            listOf(
                Triple(0.3, Color.BLUE, true),
                Triple(-0.7, Color.RED, false),
                Triple(2.4, Color.BLACK, true)
            )
        )
        val frame = SwingometerFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            valuePublisher = 3.asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = dots.mapElements { SwingometerFrame.Dot(it.first, it.second, solid = it.third) }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(3))
        Assertions.assertEquals(20, frame.numBucketsPerSide.toLong())
        Assertions.assertEquals(0.3, frame.getDotPosition(0))
        Assertions.assertEquals(Color.RED, frame.getDotColor(1))
        Assertions.assertTrue(frame.isDotSolid(0))
        Assertions.assertFalse(frame.isDotSolid(1))
        Assertions.assertTrue(frame.isDotSolid(2))
    }

    @Test
    fun testRenderBasic() {
        val ticks = (-9..9).toList()
        val outerLabels = Publisher(
            listOf(
                Triple(0.0, 51, Color.RED),
                Triple(-1.55, 51, Color.BLUE),
                Triple(-7.8, 52, Color.BLUE),
                Triple(-8.3, 54, Color.BLUE),
                Triple(2.85, 55, Color.RED),
                Triple(4.55, 60, Color.RED),
                Triple(9.75, 65, Color.RED)
            )
        )
        val dots = createSwingometerDotsWithoutLabels()
        val frame = SwingometerFrame(
            headerPublisher = "2018 SENATE SWINGOMETER".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = (-4.0).asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            leftToWinPublisher = 1.55.asOneTimePublisher(),
            rightToWinPublisher = (-0.60).asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = dots.mapElements { SwingometerFrame.Dot(it.second, it.third) },
            ticksPublisher = ticks.map { SwingometerFrame.Tick(it, it.toString()) }.asOneTimePublisher(),
            outerLabelsPublisher = outerLabels.mapElements {
                SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third)
            }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "Unlabelled", frame)
    }

    @Test
    fun testRenderBasicTinyDots() {
        val ticks = (-9..9).toList()
        val outerLabels = Publisher(
            listOf(
                Triple(0.0, 51, Color.RED),
                Triple(-1.55, 51, Color.BLUE),
                Triple(-7.8, 52, Color.BLUE),
                Triple(-8.3, 54, Color.BLUE),
                Triple(2.85, 55, Color.RED),
                Triple(4.55, 60, Color.RED),
                Triple(9.75, 65, Color.RED)
            )
        )
        val dots = createSwingometerDotsWithoutLabels()
        val frame = SwingometerFrame(
            headerPublisher = "2018 SENATE SWINGOMETER".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = (-4.0).asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            leftToWinPublisher = 1.55.asOneTimePublisher(),
            rightToWinPublisher = (-0.60).asOneTimePublisher(),
            numBucketsPerSidePublisher = 80.asOneTimePublisher(),
            dotsPublisher = dots.mapElements { SwingometerFrame.Dot(it.second, it.third) },
            ticksPublisher = ticks.map { SwingometerFrame.Tick(it, it.toString()) }.asOneTimePublisher(),
            outerLabelsPublisher = outerLabels.mapElements {
                SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third)
            }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "Unlabelled-TinyDots", frame)
    }

    private fun createSwingometerDotsWithoutLabels(): Publisher<List<Triple<String, Double, Color>>> {
        return Publisher(
            listOf(
                Triple("WY", -27.00, Color.RED),
                Triple("UT", -17.65, Color.RED),
                Triple("TN", -17.25, Color.RED),
                Triple("MS (S)", -11.00, Color.RED),
                Triple("MS", -8.30, Color.RED),
                Triple("TX", -7.90, Color.RED),
                Triple("NE", -7.80, Color.RED),
                Triple("AZ", -1.55, Color.BLUE),
                Triple("NV", -0.60, Color.BLUE),
                Triple("ND", +0.45, Color.RED),
                Triple("MT", +1.85, Color.BLUE),
                Triple("WI", +2.75, Color.BLUE),
                Triple("IN", +2.90, Color.RED),
                Triple("NM", +2.85, Color.BLUE),
                Triple("VA", +2.95, Color.BLUE),
                Triple("OH", +3.00, Color.BLUE),
                Triple("MA", +3.70, Color.BLUE),
                Triple("PA", +4.55, Color.BLUE),
                Triple("MN (S)", +5.15, Color.BLUE),
                Triple("CT", +5.85, Color.BLUE),
                Triple("FL", +6.50, Color.RED),
                Triple("MO", +7.90, Color.RED),
                Triple("NJ", +9.75, Color.BLUE),
                Triple("MI", +10.40, Color.BLUE),
                Triple("WA", +10.50, Color.BLUE),
                Triple("ME", +11.10, Color.BLUE),
                Triple("WV", +12.05, Color.BLUE),
                Triple("CA", +12.50, Color.BLUE),
                Triple("HI", +12.60, Color.BLUE),
                Triple("MD", +14.85, Color.BLUE),
                Triple("RI", +14.90, Color.BLUE),
                Triple("MN", +17.30, Color.BLUE),
                Triple("DE", +18.70, Color.BLUE),
                Triple("NY", +22.30, Color.BLUE),
                Triple("VT", +23.05, Color.BLUE)
            )
        )
    }

    @Test
    fun testRenderLabels() {
        val ticks = (-9..9).toList()
        val outerLabels = Publisher(
            listOf(
                Triple(0.0, 332, Color.BLUE),
                Triple(-3.91, 350, Color.BLUE),
                Triple(-5.235, 400, Color.BLUE),
                Triple(-7.895, 450, Color.BLUE),
                Triple(2.68, 270, Color.RED),
                Triple(5.075, 350, Color.RED),
                Triple(8.665, 400, Color.RED)
            )
        )
        val dots = createSwingometerDotsWithLabels()
        val frame = SwingometerFrame(
            headerPublisher = "2016 PRESIDENT SWINGOMETER".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 0.885.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            leftToWinPublisher = (-2.68).asOneTimePublisher(),
            rightToWinPublisher = 2.68.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = dots.mapElements {
                SwingometerFrame.Dot(it.second, it.third, label = it.first.second.toString())
            },
            ticksPublisher = ticks.map { SwingometerFrame.Tick(it, it.toString()) }.asOneTimePublisher(),
            outerLabelsPublisher = outerLabels.mapElements {
                SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third)
            }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "Labels", frame)
    }

    @Test
    fun testRenderEmptyDots() {
        val ticks = (-9..9).toList()
        val outerLabels = Publisher(
            listOf(
                Triple(0.0, 332, Color.BLUE),
                Triple(-3.91, 350, Color.BLUE),
                Triple(-5.235, 400, Color.BLUE),
                Triple(-7.895, 450, Color.BLUE),
                Triple(2.68, 270, Color.RED),
                Triple(5.075, 350, Color.RED),
                Triple(8.665, 400, Color.RED)
            )
        )
        val dots = createSwingometerDotsWithLabels()
        val frame = SwingometerFrame(
            headerPublisher = "2016 PRESIDENT SWINGOMETER".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 0.885.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            leftToWinPublisher = (-2.68).asOneTimePublisher(),
            rightToWinPublisher = 2.68.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = dots.mapElements {
                SwingometerFrame.Dot(
                    it.second,
                    it.third,
                    label = it.first.second.toString(),
                    solid = it.second > -5 && it.second < 0
                )
            },
            ticksPublisher = ticks.map { SwingometerFrame.Tick(it, it.toString()) }.asOneTimePublisher(),
            outerLabelsPublisher = outerLabels.mapElements {
                SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third)
            }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "EmptyDots", frame)
    }

    @Test
    fun testNeededToWinGoesBeyondBoundary() {
        val outerLabels = Publisher(
            listOf(
                Triple(0.0, 332, Color.BLUE),
                Triple(-3.91, 350, Color.BLUE),
                Triple(-5.235, 400, Color.BLUE),
                Triple(-7.895, 450, Color.BLUE),
                Triple(2.68, 270, Color.RED),
                Triple(5.075, 350, Color.RED),
                Triple(8.665, 400, Color.RED)
            )
        )
        val dots = Publisher(emptyList<SwingometerFrame.Dot>())
        val range = Publisher(10)
        val leftToWin = Publisher(-2.68)
        val rightToWin = Publisher(2.68)
        val value = Publisher(0.885)
        val leftColor = Publisher(Color.BLUE)
        val rightColor = Publisher(Color.RED)

        @Suppress("ReplaceRangeToWithUntil")
        val ticks = range.map { (-it + 1..it - 1).toList() }
        val frame = SwingometerFrame(
            headerPublisher = "2016 PRESIDENT SWINGOMETER".asOneTimePublisher(),
            rangePublisher = range,
            valuePublisher = value,
            leftColorPublisher = leftColor,
            rightColorPublisher = rightColor,
            leftToWinPublisher = leftToWin,
            rightToWinPublisher = rightToWin,
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = dots,
            ticksPublisher = ticks.mapElements { SwingometerFrame.Tick(it, it.toString()) },
            outerLabelsPublisher = outerLabels.mapElements {
                SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third)
            }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "BeyondBoundary-1", frame)

        range.submit(2)
        compareRendering("SwingometerFrame", "BeyondBoundary-2", frame)

        outerLabels.submit(
            listOf(
                Triple(0.0, 332, Color.BLUE),
                Triple(3.91, 350, Color.BLUE),
                Triple(5.235, 400, Color.BLUE),
                Triple(7.895, 450, Color.BLUE),
                Triple(-2.68, 270, Color.RED),
                Triple(-5.075, 350, Color.RED),
                Triple(-8.665, 400, Color.RED)
            )
        )
        leftToWin.submit(2.68)
        rightToWin.submit(-2.68)
        leftColor.submit(Color.RED)
        rightColor.submit(Color.BLUE)
        value.submit(-0.885)
        compareRendering("SwingometerFrame", "BeyondBoundary-3", frame)

        range.submit(10)
        compareRendering("SwingometerFrame", "BeyondBoundary-4", frame)
    }

    private fun createSwingometerDotsWithLabels(): Publisher<List<Triple<Pair<String, Int>, Double, Color>>> {
        return Publisher(
            listOf(
                Triple(Pair("UT", 6), -24.02, Color.RED),
                Triple(Pair("NE-03", 1), -21.31, Color.RED),
                Triple(Pair("WY", 3), -20.41, Color.RED),
                Triple(Pair("OK", 7), -17.27, Color.RED),
                Triple(Pair("ID", 4), -15.945, Color.RED),
                Triple(Pair("WV", 5), -13.42, Color.RED),
                Triple(Pair("AR", 6), -11.855, Color.RED),
                Triple(Pair("KY", 8), -11.345, Color.RED),
                Triple(Pair("AL", 9), -11.095, Color.RED),
                Triple(Pair("NE-AL", 2), -10.885, Color.RED),
                Triple(Pair("KS", 6), -10.86, Color.RED),
                Triple(Pair("TN", 11), -10.20, Color.RED),
                Triple(Pair("ND", 3), -9.815, Color.RED),
                Triple(Pair("SD", 3), -9.01, Color.RED),
                Triple(Pair("LA", 8), -8.60, Color.RED),
                Triple(Pair("NE-01", 1), -8.30, Color.RED),
                Triple(Pair("TX", 38), -7.895, Color.RED),
                Triple(Pair("AK", 3), -6.995, Color.RED),
                Triple(Pair("MT", 3), -6.825, Color.RED),
                Triple(Pair("MS", 6), -5.75, Color.RED),
                Triple(Pair("SC", 9), -5.235, Color.RED),
                Triple(Pair("IN", 11), -5.10, Color.RED),
                Triple(Pair("MO", 10), -4.69, Color.RED),
                Triple(Pair("AZ", 11), -4.53, Color.RED),
                Triple(Pair("GA", 16), -3.91, Color.RED),
                Triple(Pair("NE-02", 1), -3.575, Color.RED),
                Triple(Pair("NC", 15), -1.02, Color.RED),
                Triple(Pair("FL", 29), +0.44, Color.RED),
                Triple(Pair("OH", 18), +1.49, Color.RED),
                Triple(Pair("VA", 13), +1.94, Color.BLUE),
                Triple(Pair("CO", 9), +2.68, Color.BLUE),
                Triple(Pair("PA", 20), +2.69, Color.RED),
                Triple(Pair("NH", 4), +2.79, Color.BLUE),
                Triple(Pair("IA", 6), +2.905, Color.RED),
                Triple(Pair("NV", 6), +3.34, Color.BLUE),
                Triple(Pair("MN", 10), +3.845, Color.BLUE),
                Triple(Pair("ME-02", 1), +4.28, Color.RED),
                Triple(Pair("WI", 10), +4.47, Color.RED),
                Triple(Pair("MI", 16), +4.75, Color.RED),
                Triple(Pair("NM", 5), +5.075, Color.BLUE),
                Triple(Pair("OR", 7), +6.045, Color.BLUE),
                Triple(Pair("WA", 12), +7.435, Color.BLUE),
                Triple(Pair("ME-AL", 2), +7.645, Color.BLUE),
                Triple(Pair("IL", 20), +8.435, Color.BLUE),
                Triple(Pair("CT", 7), +8.665, Color.BLUE),
                Triple(Pair("NJ", 14), +8.895, Color.BLUE),
                Triple(Pair("DE", 3), +9.315, Color.BLUE),
                Triple(Pair("ME-01", 1), +10.695, Color.BLUE),
                Triple(Pair("CA", 55), +11.56, Color.BLUE),
                Triple(Pair("MA", 11), +11.57, Color.BLUE),
                Triple(Pair("MD", 10), +13.035, Color.BLUE),
                Triple(Pair("RI", 4), +13.92, Color.BLUE),
                Triple(Pair("NY", 29), +14.09, Color.BLUE),
                Triple(Pair("VT", 3), +17.80, Color.BLUE),
                Triple(Pair("HI", 4), +21.355, Color.BLUE),
                Triple(Pair("DC", 3), +46.815, Color.BLUE)
            )
        )
    }

    @Test
    fun testRenderMultiLineLabels() {
        val ticks = (-9..9).toList()
        val outerLabels = Publisher(
            listOf(
                Triple(0.0, 332, Color.BLUE),
                Triple(-3.91, 350, Color.BLUE),
                Triple(-5.235, 400, Color.BLUE),
                Triple(-7.895, 450, Color.BLUE),
                Triple(2.68, 270, Color.RED),
                Triple(5.075, 350, Color.RED),
                Triple(8.665, 400, Color.RED)
            )
        )
        val dots = createSwingometerDotsWithLabels()
        val frame = SwingometerFrame(
            headerPublisher = "2016 PRESIDENT SWINGOMETER".asOneTimePublisher(),
            rangePublisher = 10.asOneTimePublisher(),
            valuePublisher = 0.885.asOneTimePublisher(),
            leftColorPublisher = Color.BLUE.asOneTimePublisher(),
            rightColorPublisher = Color.RED.asOneTimePublisher(),
            leftToWinPublisher = (-2.68).asOneTimePublisher(),
            rightToWinPublisher = 2.68.asOneTimePublisher(),
            numBucketsPerSidePublisher = 20.asOneTimePublisher(),
            dotsPublisher = dots.mapElements {
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
            ticksPublisher = ticks.map { SwingometerFrame.Tick(it, it.toString()) }.asOneTimePublisher(),
            outerLabelsPublisher = outerLabels.mapElements {
                SwingometerFrame.OuterLabel(it.first, it.second.toString(), it.third)
            }
        )
        frame.setSize(1024, 512)
        compareRendering("SwingometerFrame", "MultiLineLabels", frame)
    }
}
