package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.ImageGenerator.createHalfTickShape
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.io.IOException
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import kotlin.Throws
import kotlin.jvm.JvmOverloads
import kotlin.math.sign

class BarFrameTest {
    @Test
    fun testNumBars() {
        val results = BindableWrapper(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1)
            )
        )
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements { BarFrame.Bar("", "", null, listOf()) }.toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
    }

    @Test
    fun testAddRemoveBars() {
        val list = mutableListOf<ElectionResult>()
        val results = BindableWrapper(list)
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements { BarFrame.Bar("", "", null, listOf()) }.toPublisher()
        )
        Assert.assertEquals(0, frame.numBars.toLong())
        list.add(ElectionResult("LIBERAL", Color.RED, 1))
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(1))
        list.addAll(
            listOf(
                ElectionResult("CONSERVATIVE", Color.BLUE, 1),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1)
            )
        )
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(3))
        list.removeAt(2)
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(2))
        list.removeIf { it.getPartyName() != "LIBERAL" }
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(1))
        list.clear()
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(0))
    }

    @Test
    fun testLeftTextBinding() {
        val results = BindableWrapper(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1)
            )
        )
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements { BarFrame.Bar(it.getPartyName(), "", null, listOf()) }.toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(1))
        Assert.assertEquals("BLOC QUEBECOIS", frame.getLeftText(2))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3))
        Assert.assertEquals("GREEN", frame.getLeftText(4))
        Assert.assertEquals("INDEPENDENT", frame.getLeftText(5))
    }

    @Test
    fun testRightTextBinding() {
        val results = BindableWrapper(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1)
            )
        )
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements { BarFrame.Bar(it.getPartyName(), "${it.getNumSeats()}", null, listOf()) }.toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        Assert.assertEquals("157", frame.getRightText(0))
        Assert.assertEquals("121", frame.getRightText(1))
        Assert.assertEquals("32", frame.getRightText(2))
        Assert.assertEquals("24", frame.getRightText(3))
        Assert.assertEquals("3", frame.getRightText(4))
        Assert.assertEquals("1", frame.getRightText(5))
    }

    @Test
    fun testSeriesBinding() {
        val list = mutableListOf(
            ElectionResult("LIBERAL", Color.RED, 2, 157),
            ElectionResult("CONSERVATIVE", Color.BLUE, 1, 121),
            ElectionResult("BLOC QUEBECOIS", Color.CYAN, 0, 32),
            ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1, 24),
            ElectionResult("GREEN", Color.GREEN, 0, 3),
            ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 0, 1)
        )
        val results = BindableWrapper(list)
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(), "${it.getNumSeats()}", null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                        Pair(lighten(it.getPartyColor()), it.getSeatEstimate() - it.getNumSeats())
                    )
                )
            }.toPublisher()
        )
        val lightRed = Color(255, 127, 127)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        var libSeries = frame.getSeries(0)
        Assert.assertEquals(Color.RED, libSeries[0].first)
        Assert.assertEquals(2, libSeries[0].second.toInt().toLong())
        Assert.assertEquals(lightRed, libSeries[1].first)
        Assert.assertEquals(155, libSeries[1].second.toInt().toLong())
        list[0].setSeatEstimate(158)
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSeries(0)[1].second.toInt() }, IsEqual(156))
        libSeries = frame.getSeries(0)
        Assert.assertEquals(Color.RED, libSeries[0].first)
        Assert.assertEquals(2, libSeries[0].second.toInt().toLong())
        Assert.assertEquals(lightRed, libSeries[1].first)
        Assert.assertEquals(156, libSeries[1].second.toInt().toLong())
        list[0].setNumSeats(3)
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSeries(0)[0].second.toInt() }, IsEqual(3))
        libSeries = frame.getSeries(0)
        Assert.assertEquals(Color.RED, libSeries[0].first)
        Assert.assertEquals(3, libSeries[0].second.toInt().toLong())
        Assert.assertEquals(lightRed, libSeries[1].first)
        Assert.assertEquals(155, libSeries[1].second.toInt().toLong())
    }

    @Test
    fun testLeftIconBinding() {
        val list = mutableListOf(
            ElectionResult("LIBERAL", Color.RED, 157),
            ElectionResult("CONSERVATIVE", Color.BLUE, 121),
            ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
            ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
            ElectionResult("GREEN", Color.GREEN, 3),
            ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1)
        )
        val results = BindableWrapper(list)
        val shape: Shape = Ellipse2D.Double()
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements { BarFrame.Bar("", "", if (it.getNumSeats() > 150) shape else null, listOf()) }.toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(6))
        Assert.assertEquals(shape, frame.getLeftIcon(0))
        Assert.assertNull(frame.getLeftIcon(1))
        Assert.assertNull(frame.getLeftIcon(2))
        Assert.assertNull(frame.getLeftIcon(3))
        Assert.assertNull(frame.getLeftIcon(4))
        Assert.assertNull(frame.getLeftIcon(5))
    }

    @Test
    fun testDefaultMinMax() {
        val list = mutableListOf(
            ElectionResult("LIBERAL", Color.RED, 157),
            ElectionResult("CONSERVATIVE", Color.BLUE, 121),
            ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
            ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
            ElectionResult("GREEN", Color.GREEN, 3),
            ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1)
        )
        val results = BindableWrapper(list)
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    "", "", null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(157))
        list[0].setNumSeats(-27)
        list[1].setNumSeats(22)
        list[2].setNumSeats(22)
        list[3].setNumSeats(-20)
        list[4].setNumSeats(2)
        list[5].setNumSeats(1)
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(-27))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(22))
    }

    @Test
    fun testFixedMinMax() {
        val list = mutableListOf(
            ElectionResult("LIBERAL", Color.RED, 157),
            ElectionResult("CONSERVATIVE", Color.BLUE, 121),
            ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
            ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
            ElectionResult("GREEN", Color.GREEN, 3),
            ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1)
        )
        val results = BindableWrapper(list)
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    "", "", null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher(),
            minPublisher = (-30).asOneTimePublisher(),
            maxPublisher = 30.asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(-30))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(30))

        list[0].setNumSeats(-27)
        list[1].setNumSeats(22)
        list[2].setNumSeats(22)
        list[3].setNumSeats(-20)
        list[4].setNumSeats(2)
        list[5].setNumSeats(1)
        results.value = list
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.min.toInt() }, IsEqual(-30))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.max.toInt() }, IsEqual(30))
    }

    @Test
    fun testSubheadText() {
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            subheadTextPublisher = "PROJECTION: LIB MINORITY".asOneTimePublisher(),
            barsPublisher = emptyList<BarFrame.Bar>().asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.subheadText }, IsEqual("PROJECTION: LIB MINORITY"))
    }

    @Test
    fun testDefaultSubheadColor() {
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = emptyList<BarFrame.Bar>().asOneTimePublisher()
        )
        Assert.assertEquals(Color.BLACK, frame.subheadColor)
    }

    @Test
    fun testSubheadColor() {
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            subheadTextPublisher = "PROJECTION: LIB MINORITY".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = emptyList<BarFrame.Bar>().asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.subheadColor }, IsEqual(Color.RED))
    }

    @Test
    fun testLines() {
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = emptyList<BarFrame.Bar>().asOneTimePublisher(),
            linesPublisher = listOf(BarFrame.Line(170, "170 SEATS FOR MAJORITY")).asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numLines }, IsEqual(1))
        Assert.assertEquals(170, frame.getLineLevel(0))
        Assert.assertEquals("170 SEATS FOR MAJORITY", frame.getLineLabel(0))
    }

    @Test
    fun testTestNonBindableElements() {
        val results = BindableWrapper<List<Triple<String, Color, Int>>>(listOf())
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.binding.mapElements { BarFrame.Bar(it.first, "", null, listOf()) }.toPublisher()
        )
        Assert.assertEquals(0, frame.numBars.toLong())
        results.value = (
            listOf(
                Triple("LIBERALS", Color.RED, 3),
                Triple("CONSERVATIVES", Color.BLUE, 2),
                Triple("NDP", Color.ORANGE, 1)
            )
            )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(3))
        Assert.assertEquals("LIBERALS", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVES", frame.getLeftText(1))
        Assert.assertEquals("NDP", frame.getLeftText(2))
        results.value = (
            listOf(
                Triple("LIBERALS", Color.RED, 3),
                Triple("CONSERVATIVES", Color.BLUE, 3)
            )
            )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numBars }, IsEqual(2))
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("LIBERALS", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVES", frame.getLeftText(1))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSingleSeriesAllPositive() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "2019 CANADIAN ELECTION RESULT".asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(), "${it.getNumSeats()}", null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher(),
            maxPublisher = 160.asOneTimePublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesAllPositive", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSingleSeriesWithSubhead() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "2019 CANADIAN ELECTION RESULT".asOneTimePublisher(),
            subheadTextPublisher = "PROJECTION: LIB MINORITY".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(), "${it.getNumSeats()}", null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher(),
            maxPublisher = 160.asOneTimePublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesWithSubhead", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSingleSeriesShrinkToFit() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 177),
                ElectionResult("CONSERVATIVE", Color.BLUE, 95),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 39),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 10),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 8),
                ElectionResult("GREEN", Color.GREEN, 2),
                ElectionResult("CO-OPERATIVE COMMONWEALTH FEDERATION", Color.ORANGE.darker(), 1),
                ElectionResult("PEOPLE'S PARTY", Color.MAGENTA.darker(), 1)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "SEATS AT DISSOLUTION".asOneTimePublisher(),
            subheadTextPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(), "${it.getNumSeats()}", null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesShrinkToFit", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMultiSeriesAllPositive() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 34, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 21, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 2, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 4, 24),
                ElectionResult("GREEN", Color.GREEN, 1, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 0, 1)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "2019 CANADIAN ELECTION RESULT".asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(), it.getNumSeats().toString() + "/" + it.getSeatEstimate(), null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                        Pair(lighten(it.getPartyColor()), it.getSeatEstimate() - it.getNumSeats())
                    )
                )
            }.toPublisher(),
            maxPublisher = 160.asOneTimePublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "MultiSeriesAllPositive", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSingleSeriesBothDirections() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("LIB", Color.RED, -27),
                ElectionResult("CON", Color.BLUE, +22),
                ElectionResult("BQ", Color.CYAN, +22),
                ElectionResult("NDP", Color.ORANGE, -20),
                ElectionResult("GRN", Color.GREEN, +2),
                ElectionResult("IND", Color.LIGHT_GRAY, +1)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "RESULT CHANGE SINCE 2015".asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(), DecimalFormat("+0;-0").format(it.getNumSeats().toLong()), null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher(),
            maxPublisher = 28.asOneTimePublisher(),
            minPublisher = (-28).asOneTimePublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesBothDirections", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMultiSeriesBothDirections() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("LIB", Color.RED, -7, -27),
                ElectionResult("CON", Color.BLUE, +4, +22),
                ElectionResult("BQ", Color.CYAN, +0, +22),
                ElectionResult("NDP", Color.ORANGE, +2, -20),
                ElectionResult("GRN", Color.GREEN, +1, +2),
                ElectionResult("IND", Color.LIGHT_GRAY, +0, +1)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "RESULT CHANGE SINCE 2015".asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(), "${CHANGE_FORMAT.format(it.getNumSeats().toLong())}/${CHANGE_FORMAT.format(it.getSeatEstimate().toLong())}", null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                        Pair(lighten(it.getPartyColor()), it.getSeatEstimate() - if (sign(it.getSeatEstimate().toFloat()) == sign(it.getNumSeats().toFloat())) it.getNumSeats() else 0)
                    )
                )
            }.toPublisher(),
            maxPublisher = 28.asOneTimePublisher(),
            minPublisher = (-28).asOneTimePublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "MultiSeriesBothDirections", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderTwoLinedBars() {
        val results: BindableWrapper<List<RidingResult>> = BindableWrapper(
            listOf(
                RidingResult("BARDISH CHAGGER", "LIBERAL", Color.RED, 31085, 0.4879),
                RidingResult("JERRY ZHANG", "CONSERVATIVE", Color.BLUE, 15615, 0.2451),
                RidingResult("LORI CAMPBELL", "NEW DEMOCRATIC PARTY", Color.ORANGE, 9710, 0.1524),
                RidingResult("KIRSTEN WRIGHT", "GREEN", Color.GREEN, 6184, 0.0971),
                RidingResult("ERIKA TRAUB", "PEOPLE'S PARTY", Color.MAGENTA.darker(), 1112, 0.0175)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "WATERLOO".asOneTimePublisher(),
            subheadTextPublisher = "LIB HOLD".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    "${it.getCandidateName()}\n${it.getPartyName()}",
                    "${THOUSANDS_FORMAT.format(it.getNumVotes().toLong())}\n${PERCENT_FORMAT.format(it.getVotePct())}",
                    listOf(Pair(it.getPartyColor(), it.getNumVotes()))
                )
            }.toPublisher(),
            maxPublisher = results.binding.map { r -> r.sumOf { it.getNumVotes() } / 2 }.toPublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "TwoLinedBars", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderTwoLinedBarWithIcon() {
        val results: BindableWrapper<List<RidingResult>> = BindableWrapper(
            listOf(
                RidingResult("BARDISH CHAGGER", "LIBERAL", Color.RED, 31085, 0.4879, true),
                RidingResult("JERRY ZHANG", "CONSERVATIVE", Color.BLUE, 15615, 0.2451),
                RidingResult("LORI CAMPBELL", "NEW DEMOCRATIC PARTY", Color.ORANGE, 9710, 0.1524),
                RidingResult("KIRSTEN WRIGHT", "GREEN", Color.GREEN, 6184, 0.0971),
                RidingResult("ERIKA TRAUB", "PEOPLE'S PARTY", Color.MAGENTA.darker(), 1112, 0.0175)
            )
        )
        val shape = createTickShape()
        val barFrame = BarFrame(
            headerPublisher = "WATERLOO".asOneTimePublisher(),
            subheadTextPublisher = "LIB HOLD".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    "${it.getCandidateName()}\n${it.getPartyName()}",
                    "${THOUSANDS_FORMAT.format(it.getNumVotes().toLong())}\n${PERCENT_FORMAT.format(it.getVotePct())}",
                    if (it.isElected()) shape else null,
                    listOf(Pair(it.getPartyColor(), it.getNumVotes()))
                )
            }.toPublisher(),
            maxPublisher = results.binding.map { r -> r.sumOf { it.getNumVotes() } / 2 }.toPublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "TwoLinedBarWithIcon", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderTwoLinedBarWithNegativeIcon() {
        val results: BindableWrapper<List<RidingResult>> = BindableWrapper(
            listOf(
                RidingResult("BARDISH CHAGGER", "LIB", Color.RED, 31085, -0.010, true),
                RidingResult("JERRY ZHANG", "CON", Color.BLUE, 15615, -0.077),
                RidingResult("LORI CAMPBELL", "NDP", Color.ORANGE, 9710, +0.003),
                RidingResult("KIRSTEN WRIGHT", "GRN", Color.GREEN, 6184, +0.068),
                RidingResult("ERIKA TRAUB", "PPC", Color.MAGENTA.darker(), 1112, +0.017)
            )
        )
        val shape = createTickShape()
        val barFrame = BarFrame(
            headerPublisher = "WATERLOO".asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    PERCENT_FORMAT.format(it.getVotePct()),
                    if (it.isElected()) shape else null,
                    listOf(Pair(it.getPartyColor(), it.getVotePct()))
                )
            }.toPublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "TwoLinedBarWithNegativeIcon", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderVerticalLine() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 177),
                ElectionResult("CONSERVATIVE", Color.BLUE, 95),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 39),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 10),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 8),
                ElectionResult("GREEN", Color.GREEN, 2),
                ElectionResult("CO-OPERATIVE COMMONWEALTH FEDERATION", Color.ORANGE.darker(), 1),
                ElectionResult("PEOPLE'S PARTY", Color.MAGENTA.darker(), 1)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "SEATS AT DISSOLUTION".asOneTimePublisher(),
            subheadTextPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher(),
            linesPublisher = listOf(BarFrame.Line(170, "MAJORITY")).asOneTimePublisher(),
            maxPublisher = 225.asOneTimePublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "VerticalLine", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderAccents() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("COALITION AVENIR QU\u00c9BEC: FRAN\u00c7OIS LEGAULT", Color.BLUE, 74),
                ElectionResult("LIB\u00c9RAL: PHILIPPE COUILLARD", Color.RED, 31),
                ElectionResult("PARTI QU\u00c9BECOIS: JEAN-FRAN\u00c7OIS LIS\u00c9E", Color.CYAN, 10),
                ElectionResult("QU\u00c9BEC SOLIDAIRE: MANON MASS\u00c9", Color.ORANGE, 10)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "\u00c9LECTION 2018".asOneTimePublisher(),
            subheadTextPublisher = "MAJORIT\u00c9: 63".asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher(),
            linesPublisher = listOf(BarFrame.Line(63, "MAJORIT\u00c9")).asOneTimePublisher(),
            maxPublisher = 83.asOneTimePublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "Accents", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMultiLineAccents() {
        val results: BindableWrapper<List<ElectionResult>> = BindableWrapper(
            listOf(
                ElectionResult("COALITION AVENIR QU\u00c9BEC\nFRAN\u00c7OIS LEGAULT", Color.BLUE, 74),
                ElectionResult("LIB\u00c9RAL\nPHILIPPE COUILLARD", Color.RED, 31),
                ElectionResult("PARTI QU\u00c9BECOIS\nJEAN-FRAN\u00c7OIS LIS\u00c9E", Color.CYAN, 10),
                ElectionResult("QU\u00c9BEC SOLIDAIRE\nMANON MASS\u00c9", Color.ORANGE, 10)
            )
        )
        val barFrame = BarFrame(
            headerPublisher = "\u00c9LECTION 2018".asOneTimePublisher(),
            subheadTextPublisher = "MAJORIT\u00c9: 63".asOneTimePublisher(),
            barsPublisher = results.binding.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats())
                    )
                )
            }.toPublisher(),
            linesPublisher = listOf(BarFrame.Line(63, "MAJORIT\u00c9")).asOneTimePublisher(),
            maxPublisher = 83.asOneTimePublisher()
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "MultiLineAccents", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testBarFrameOverlaps() {
        val lines = BindableWrapper(listOf(Triple("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "RIGHT\nSIDE", false)))
        val barFrame = BarFrame(
            headerPublisher = "BAR FRAME".asOneTimePublisher(),
            subheadTextPublisher = "".asOneTimePublisher(),
            barsPublisher = lines.binding.mapElements {
                BarFrame.Bar(
                    it.first,
                    it.second,
                    if (it.third) createHalfTickShape() else null,
                    listOf(Pair(Color.RED, 1))
                )
            }.toPublisher(),
            linesPublisher = listOf(BarFrame.Line(0.5, "")).asOneTimePublisher(),
            maxPublisher = 1.asOneTimePublisher()
        )
        barFrame.setSize(256, 128)
        compareRendering("BarFrame", "FrameOverlap-1", barFrame)
        lines.value = listOf(Triple("LEFT\nSIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", false))
        compareRendering("BarFrame", "FrameOverlap-2", barFrame)
        lines.value = listOf(Triple("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", false))
        compareRendering("BarFrame", "FrameOverlap-3", barFrame)
        lines.value = listOf(Triple("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "RIGHT\nSIDE", true))
        compareRendering("BarFrame", "FrameOverlap-4", barFrame)
        lines.value = listOf(Triple("LEFT\nSIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", true))
        compareRendering("BarFrame", "FrameOverlap-5", barFrame)
        lines.value = listOf(Triple("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", true))
        compareRendering("BarFrame", "FrameOverlap-6", barFrame)
    }

    private fun createTickShape(): Shape {
        val shape = Area(Rectangle(0, 0, 100, 100))
        shape.subtract(Area(Polygon(intArrayOf(10, 40, 90, 80, 40, 20), intArrayOf(50, 80, 30, 20, 60, 40), 6)))
        return shape
    }

    private class ElectionResult @JvmOverloads constructor(private var partyName: String, private var partyColor: Color, private var numSeats: Int, private var seatEstimate: Int = numSeats) : Bindable<ElectionResult, ElectionResult.Properties>() {
        enum class Properties {
            PARTY_NAME, PARTY_COLOR, NUM_SEATS, SEAT_ESTIMATE
        }

        fun getPartyName(): String {
            return partyName
        }

        fun setPartyName(partyName: String) {
            this.partyName = partyName
            onPropertyRefreshed(Properties.PARTY_NAME)
        }

        fun getPartyColor(): Color {
            return partyColor
        }

        fun setPartyColor(partyColor: Color) {
            this.partyColor = partyColor
            onPropertyRefreshed(Properties.PARTY_COLOR)
        }

        fun getNumSeats(): Int {
            return numSeats
        }

        fun setNumSeats(numSeats: Int) {
            this.numSeats = numSeats
            onPropertyRefreshed(Properties.NUM_SEATS)
        }

        fun getSeatEstimate(): Int {
            return seatEstimate
        }

        fun setSeatEstimate(seatEstimate: Int) {
            this.seatEstimate = seatEstimate
            onPropertyRefreshed(Properties.SEAT_ESTIMATE)
        }
    }

    private class RidingResult @JvmOverloads constructor(
        private var candidateName: String,
        private var partyName: String,
        private var partyColor: Color,
        private var numVotes: Int,
        private var votePct: Double,
        private var elected: Boolean = false
    ) : Bindable<RidingResult, RidingResult.Properties>() {
        enum class Properties {
            CANDIDATE_NAME, PARTY_NAME, PARTY_COLOR, NUM_VOTES, VOTE_PCT, ELECTED
        }

        fun getCandidateName(): String {
            return candidateName
        }

        fun setCandidateName(candidateName: String) {
            this.candidateName = candidateName
            onPropertyRefreshed(Properties.CANDIDATE_NAME)
        }

        fun getPartyName(): String {
            return partyName
        }

        fun setPartyName(partyName: String) {
            this.partyName = partyName
            onPropertyRefreshed(Properties.PARTY_NAME)
        }

        fun getPartyColor(): Color {
            return partyColor
        }

        fun setPartyColor(partyColor: Color) {
            this.partyColor = partyColor
            onPropertyRefreshed(Properties.PARTY_COLOR)
        }

        fun getNumVotes(): Int {
            return numVotes
        }

        fun setNumVotes(numVotes: Int) {
            this.numVotes = numVotes
            onPropertyRefreshed(Properties.NUM_VOTES)
        }

        fun getVotePct(): Double {
            return votePct
        }

        fun setVotePct(votePct: Double) {
            this.votePct = votePct
            onPropertyRefreshed(Properties.VOTE_PCT)
        }

        fun isElected(): Boolean {
            return elected
        }

        fun setElected(elected: Boolean) {
            this.elected = elected
            onPropertyRefreshed(Properties.ELECTED)
        }
    }

    companion object {
        private val CHANGE_FORMAT = DecimalFormat("+0;-0")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")
        private val PERCENT_FORMAT = DecimalFormat("0.0%")
        private fun lighten(color: Color?): Color {
            return Color(
                (color!!.red + 255) / 2, (color.green + 255) / 2, (color.blue + 255) / 2
            )
        }
    }
}
