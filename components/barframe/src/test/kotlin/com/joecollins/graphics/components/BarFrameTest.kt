package com.joecollins.graphics.components

import com.joecollins.graphics.ImageGenerator.createHalfTickShape
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.text.DecimalFormat
import kotlin.math.sign

class BarFrameTest {
    @Test
    fun testNumBars() {
        val results = Publisher(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1),
            ),
        )
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements { BarFrame.Bar("", "", null, listOf()) },
        )
        assertEquals(6, frame.numBars)
    }

    @Test
    fun testAddRemoveBars() {
        val list = mutableListOf<ElectionResult>()
        val results = Publisher(list)
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements { BarFrame.Bar("", "", null, listOf()) },
        )
        assertEquals(0, frame.numBars.toLong())
        list.add(ElectionResult("LIBERAL", Color.RED, 1))
        results.submit(list)
        assertEquals(1, frame.numBars)

        list.addAll(
            listOf(
                ElectionResult("CONSERVATIVE", Color.BLUE, 1),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1),
            ),
        )
        results.submit(list)
        assertEquals(3, frame.numBars)

        list.removeAt(2)
        results.submit(list)
        assertEquals(2, frame.numBars)

        list.removeIf { it.getPartyName() != "LIBERAL" }
        results.submit(list)
        assertEquals(1, frame.numBars)

        list.clear()
        results.submit(list)
        assertEquals(0, frame.numBars)
    }

    @Test
    fun testLeftTextBinding() {
        val results = Publisher(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1),
            ),
        )
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements { BarFrame.Bar(it.getPartyName(), "", null, listOf()) },
        )
        assertEquals(6, frame.numBars)
        assertEquals("LIBERAL", frame.getLeftText(0))
        assertEquals("CONSERVATIVE", frame.getLeftText(1))
        assertEquals("BLOC QUEBECOIS", frame.getLeftText(2))
        assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3))
        assertEquals("GREEN", frame.getLeftText(4))
        assertEquals("INDEPENDENT", frame.getLeftText(5))
    }

    @Test
    fun testRightTextBinding() {
        val results = Publisher(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1),
            ),
        )
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    null,
                    listOf(),
                )
            },
        )
        assertEquals(6, frame.numBars)
        assertEquals("157", frame.getRightText(0))
        assertEquals("121", frame.getRightText(1))
        assertEquals("32", frame.getRightText(2))
        assertEquals("24", frame.getRightText(3))
        assertEquals("3", frame.getRightText(4))
        assertEquals("1", frame.getRightText(5))
    }

    @Test
    fun testSeriesBinding() {
        val list = mutableListOf(
            ElectionResult("LIBERAL", Color.RED, 2, 157),
            ElectionResult("CONSERVATIVE", Color.BLUE, 1, 121),
            ElectionResult("BLOC QUEBECOIS", Color.CYAN, 0, 32),
            ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1, 24),
            ElectionResult("GREEN", Color.GREEN, 0, 3),
            ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 0, 1),
        )
        val results = Publisher(list)
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                        Pair(lighten(it.getPartyColor()), it.getSeatEstimate() - it.getNumSeats()),
                    ),
                )
            },
        )
        val lightRed = Color(255, 127, 127)
        assertEquals(6, frame.numBars)

        var libSeries = frame.getSeries(0)
        assertEquals(Color.RED, libSeries[0].first)
        assertEquals(2, libSeries[0].second.toInt().toLong())
        assertEquals(lightRed, libSeries[1].first)
        assertEquals(155, libSeries[1].second.toInt().toLong())
        list[0].setSeatEstimate(158)
        results.submit(list)
        libSeries = frame.getSeries(0)
        assertEquals(Color.RED, libSeries[0].first)
        assertEquals(2, libSeries[0].second.toInt().toLong())
        assertEquals(lightRed, libSeries[1].first)
        assertEquals(156, libSeries[1].second.toInt().toLong())

        list[0].setNumSeats(3)
        results.submit(list)
        libSeries = frame.getSeries(0)
        assertEquals(Color.RED, libSeries[0].first)
        assertEquals(3, libSeries[0].second.toInt().toLong())
        assertEquals(lightRed, libSeries[1].first)
        assertEquals(155, libSeries[1].second.toInt().toLong())
    }

    @Test
    fun testLeftIconBinding() {
        val list = mutableListOf(
            ElectionResult("LIBERAL", Color.RED, 157),
            ElectionResult("CONSERVATIVE", Color.BLUE, 121),
            ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
            ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
            ElectionResult("GREEN", Color.GREEN, 3),
            ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1),
        )
        val results = Publisher(list)
        val shape: Shape = Ellipse2D.Double()
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    "",
                    "",
                    if (it.getNumSeats() > 150) shape else null,
                    listOf(),
                )
            },
        )
        assertEquals(6, frame.numBars)
        assertEquals(shape, frame.getLeftIcon(0))
        Assertions.assertNull(frame.getLeftIcon(1))
        Assertions.assertNull(frame.getLeftIcon(2))
        Assertions.assertNull(frame.getLeftIcon(3))
        Assertions.assertNull(frame.getLeftIcon(4))
        Assertions.assertNull(frame.getLeftIcon(5))
    }

    @Test
    fun testDefaultMinMax() {
        val list = mutableListOf(
            ElectionResult("LIBERAL", Color.RED, 157),
            ElectionResult("CONSERVATIVE", Color.BLUE, 121),
            ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
            ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
            ElectionResult("GREEN", Color.GREEN, 3),
            ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1),
        )
        val results = Publisher(list)
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    "",
                    "",
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
        )
        assertEquals(0, frame.min.toInt())
        assertEquals(157, frame.max.toInt())

        list[0].setNumSeats(-27)
        list[1].setNumSeats(22)
        list[2].setNumSeats(22)
        list[3].setNumSeats(-20)
        list[4].setNumSeats(2)
        list[5].setNumSeats(1)
        results.submit(list)
        assertEquals(-27, frame.min.toInt())
        assertEquals(22, frame.max.toInt())
    }

    @Test
    fun testFixedMinMax() {
        val list = mutableListOf(
            ElectionResult("LIBERAL", Color.RED, 157),
            ElectionResult("CONSERVATIVE", Color.BLUE, 121),
            ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
            ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
            ElectionResult("GREEN", Color.GREEN, 3),
            ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1),
        )
        val results = Publisher(list)
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    "",
                    "",
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
            minPublisher = (-30).asOneTimePublisher(),
            maxPublisher = 30.asOneTimePublisher(),
        )
        assertEquals(-30, frame.min.toInt())
        assertEquals(30, frame.max.toInt())

        list[0].setNumSeats(-27)
        list[1].setNumSeats(22)
        list[2].setNumSeats(22)
        list[3].setNumSeats(-20)
        list[4].setNumSeats(2)
        list[5].setNumSeats(1)
        results.submit(list)
        assertEquals(-30, frame.min.toInt())
        assertEquals(30, frame.max.toInt())
    }

    @Test
    fun testSubheadText() {
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            subheadTextPublisher = "PROJECTION: LIB MINORITY".asOneTimePublisher(),
            barsPublisher = emptyList<BarFrame.Bar>().asOneTimePublisher(),
        )
        assertEquals("PROJECTION: LIB MINORITY", frame.subheadText)
    }

    @Test
    fun testDefaultSubheadColor() {
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = emptyList<BarFrame.Bar>().asOneTimePublisher(),
        )
        assertEquals(Color.BLACK, frame.subheadColor)
    }

    @Test
    fun testSubheadColor() {
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            subheadTextPublisher = "PROJECTION: LIB MINORITY".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = emptyList<BarFrame.Bar>().asOneTimePublisher(),
        )
        assertEquals(Color.RED, frame.subheadColor)
    }

    @Test
    fun testLines() {
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = emptyList<BarFrame.Bar>().asOneTimePublisher(),
            linesPublisher = listOf(BarFrame.Line(170, "170 SEATS FOR MAJORITY")).asOneTimePublisher(),
        )
        assertEquals(1, frame.numLines)
        assertEquals(170, frame.getLineLevel(0))
        assertEquals("170 SEATS FOR MAJORITY", frame.getLineLabel(0))
    }

    @Test
    fun testTestNonBindableElements() {
        val results = Publisher<List<Triple<String, Color, Int>>>(listOf())
        val frame = BarFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            barsPublisher = results.mapElements { BarFrame.Bar(it.first, "", null, listOf()) },
        )
        assertEquals(0, frame.numBars.toLong())
        results.submit(
            listOf(
                Triple("LIBERALS", Color.RED, 3),
                Triple("CONSERVATIVES", Color.BLUE, 2),
                Triple("NDP", Color.ORANGE, 1),
            ),
        )
        assertEquals(3, frame.numBars)
        assertEquals("LIBERALS", frame.getLeftText(0))
        assertEquals("CONSERVATIVES", frame.getLeftText(1))
        assertEquals("NDP", frame.getLeftText(2))

        results.submit(
            listOf(
                Triple("LIBERALS", Color.RED, 3),
                Triple("CONSERVATIVES", Color.BLUE, 3),
            ),
        )
        assertEquals(2, frame.numBars)
        assertEquals(2, frame.numBars.toLong())
        assertEquals("LIBERALS", frame.getLeftText(0))
        assertEquals("CONSERVATIVES", frame.getLeftText(1))
    }

    @Test
    fun testRenderSingleSeriesAllPositive() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "2019 CANADIAN ELECTION RESULT".asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
            maxPublisher = 160.asOneTimePublisher(),
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesAllPositive", barFrame)
    }

    @Test
    fun testRenderSingleSeriesWithSubhead() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24),
                ElectionResult("GREEN", Color.GREEN, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "2019 CANADIAN ELECTION RESULT".asOneTimePublisher(),
            subheadTextPublisher = "PROJECTION: LIB MINORITY".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
            maxPublisher = 160.asOneTimePublisher(),
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesWithSubhead", barFrame)
    }

    @Test
    fun testRenderSingleSeriesShrinkToFit() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 177),
                ElectionResult("CONSERVATIVE", Color.BLUE, 95),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 39),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 10),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 8),
                ElectionResult("GREEN", Color.GREEN, 2),
                ElectionResult("CO-OPERATIVE COMMONWEALTH FEDERATION", Color.ORANGE.darker(), 1),
                ElectionResult("PEOPLE'S PARTY", Color.MAGENTA.darker(), 1),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "SEATS AT DISSOLUTION".asOneTimePublisher(),
            subheadTextPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesShrinkToFit", barFrame)
    }

    @Test
    fun testRenderMultiSeriesAllPositive() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 34, 157),
                ElectionResult("CONSERVATIVE", Color.BLUE, 21, 121),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 2, 32),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 4, 24),
                ElectionResult("GREEN", Color.GREEN, 1, 3),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 0, 1),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "2019 CANADIAN ELECTION RESULT".asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    it.getNumSeats().toString() + "/" + it.getSeatEstimate(),
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                        Pair(lighten(it.getPartyColor()), it.getSeatEstimate() - it.getNumSeats()),
                    ),
                )
            },
            maxPublisher = 160.asOneTimePublisher(),
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "MultiSeriesAllPositive", barFrame)
    }

    @Test
    fun testRenderSingleSeriesBothDirections() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("LIB", Color.RED, -27),
                ElectionResult("CON", Color.BLUE, +22),
                ElectionResult("BQ", Color.CYAN, +22),
                ElectionResult("NDP", Color.ORANGE, -20),
                ElectionResult("GRN", Color.GREEN, +2),
                ElectionResult("IND", Color.LIGHT_GRAY, +1),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "RESULT CHANGE SINCE 2015".asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    DecimalFormat("+0;-0").format(it.getNumSeats().toLong()),
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
            maxPublisher = 28.asOneTimePublisher(),
            minPublisher = (-28).asOneTimePublisher(),
        )
        barFrame.setSize(512, 256)
        compareRendering(
            "BarFrame",
            "SingleSeriesBothDirections",
            barFrame,
        )
    }

    @Test
    fun testRenderMultiSeriesBothDirections() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("LIB", Color.RED, -7, -27),
                ElectionResult("CON", Color.BLUE, +4, +22),
                ElectionResult("BQ", Color.CYAN, +0, +22),
                ElectionResult("NDP", Color.ORANGE, +2, -20),
                ElectionResult("GRN", Color.GREEN, +1, +2),
                ElectionResult("IND", Color.LIGHT_GRAY, +0, +1),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "RESULT CHANGE SINCE 2015".asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${CHANGE_FORMAT.format(it.getNumSeats().toLong())}/${
                        CHANGE_FORMAT.format(
                            it.getSeatEstimate().toLong(),
                        )
                    }",
                    null,
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                        Pair(
                            lighten(it.getPartyColor()),
                            it.getSeatEstimate() - if (sign(it.getSeatEstimate().toFloat()) == sign(
                                    it.getNumSeats().toFloat(),
                                )
                            ) {
                                it.getNumSeats()
                            } else {
                                0
                            },
                        ),
                    ),
                )
            },
            maxPublisher = 28.asOneTimePublisher(),
            minPublisher = (-28).asOneTimePublisher(),
        )
        barFrame.setSize(512, 256)
        compareRendering(
            "BarFrame",
            "MultiSeriesBothDirections",
            barFrame,
        )
    }

    @Test
    fun testRenderTwoLinedBars() {
        val results: Publisher<List<RidingResult>> = Publisher(
            listOf(
                RidingResult("BARDISH CHAGGER", "LIBERAL", Color.RED, 31085, 0.4879),
                RidingResult("JERRY ZHANG", "CONSERVATIVE", Color.BLUE, 15615, 0.2451),
                RidingResult("LORI CAMPBELL", "NEW DEMOCRATIC PARTY", Color.ORANGE, 9710, 0.1524),
                RidingResult("KIRSTEN WRIGHT", "GREEN", Color.GREEN, 6184, 0.0971),
                RidingResult("ERIKA TRAUB", "PEOPLE'S PARTY", Color.MAGENTA.darker(), 1112, 0.0175),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "WATERLOO".asOneTimePublisher(),
            subheadTextPublisher = "LIB HOLD".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    "${it.getCandidateName()}\n${it.getPartyName()}",
                    "${THOUSANDS_FORMAT.format(it.getNumVotes().toLong())}\n${PERCENT_FORMAT.format(it.getVotePct())}",
                    listOf(Pair(it.getPartyColor(), it.getNumVotes())),
                )
            },
            maxPublisher = results.map { r -> r.sumOf { it.getNumVotes() } / 2 },
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "TwoLinedBars", barFrame)
    }

    @Test
    fun testRenderTwoLinedBarWithIcon() {
        val results: Publisher<List<RidingResult>> = Publisher(
            listOf(
                RidingResult("BARDISH CHAGGER", "LIBERAL", Color.RED, 31085, 0.4879, true),
                RidingResult("JERRY ZHANG", "CONSERVATIVE", Color.BLUE, 15615, 0.2451),
                RidingResult("LORI CAMPBELL", "NEW DEMOCRATIC PARTY", Color.ORANGE, 9710, 0.1524),
                RidingResult("KIRSTEN WRIGHT", "GREEN", Color.GREEN, 6184, 0.0971),
                RidingResult("ERIKA TRAUB", "PEOPLE'S PARTY", Color.MAGENTA.darker(), 1112, 0.0175),
            ),
        )
        val shape = createTickShape()
        val barFrame = BarFrame(
            headerPublisher = "WATERLOO".asOneTimePublisher(),
            subheadTextPublisher = "LIB HOLD".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    "${it.getCandidateName()}\n${it.getPartyName()}",
                    "${THOUSANDS_FORMAT.format(it.getNumVotes().toLong())}\n${PERCENT_FORMAT.format(it.getVotePct())}",
                    if (it.isElected()) shape else null,
                    listOf(Pair(it.getPartyColor(), it.getNumVotes())),
                )
            },
            maxPublisher = results.map { r -> r.sumOf { it.getNumVotes() } / 2 },
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "TwoLinedBarWithIcon", barFrame)
    }

    @Test
    fun testRenderTwoLinedBarWithNegativeIcon() {
        val results: Publisher<List<RidingResult>> = Publisher(
            listOf(
                RidingResult("BARDISH CHAGGER", "LIB", Color.RED, 31085, -0.010, true),
                RidingResult("JERRY ZHANG", "CON", Color.BLUE, 15615, -0.077),
                RidingResult("LORI CAMPBELL", "NDP", Color.ORANGE, 9710, +0.003),
                RidingResult("KIRSTEN WRIGHT", "GRN", Color.GREEN, 6184, +0.068),
                RidingResult("ERIKA TRAUB", "PPC", Color.MAGENTA.darker(), 1112, +0.017),
            ),
        )
        val shape = createTickShape()
        val barFrame = BarFrame(
            headerPublisher = "WATERLOO".asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    PERCENT_FORMAT.format(it.getVotePct()),
                    if (it.isElected()) shape else null,
                    listOf(Pair(it.getPartyColor(), it.getVotePct())),
                )
            },
        )
        barFrame.setSize(512, 256)
        compareRendering(
            "BarFrame",
            "TwoLinedBarWithNegativeIcon",
            barFrame,
        )
    }

    @Test
    fun testRenderVerticalLine() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("LIBERAL", Color.RED, 177),
                ElectionResult("CONSERVATIVE", Color.BLUE, 95),
                ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 39),
                ElectionResult("BLOC QUEBECOIS", Color.CYAN, 10),
                ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 8),
                ElectionResult("GREEN", Color.GREEN, 2),
                ElectionResult("CO-OPERATIVE COMMONWEALTH FEDERATION", Color.ORANGE.darker(), 1),
                ElectionResult("PEOPLE'S PARTY", Color.MAGENTA.darker(), 1),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "SEATS AT DISSOLUTION".asOneTimePublisher(),
            subheadTextPublisher = "170 FOR MAJORITY".asOneTimePublisher(),
            subheadColorPublisher = Color.RED.asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
            linesPublisher = listOf(BarFrame.Line(170, "MAJORITY")).asOneTimePublisher(),
            maxPublisher = 225.asOneTimePublisher(),
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "VerticalLine", barFrame)
    }

    @Test
    fun testRenderAccents() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("COALITION AVENIR QU\u00c9BEC: FRAN\u00c7OIS LEGAULT", Color.BLUE, 74),
                ElectionResult("LIB\u00c9RAL: PHILIPPE COUILLARD", Color.RED, 31),
                ElectionResult("PARTI QU\u00c9BECOIS: JEAN-FRAN\u00c7OIS LIS\u00c9E", Color.CYAN, 10),
                ElectionResult("QU\u00c9BEC SOLIDAIRE: MANON MASS\u00c9", Color.ORANGE, 10),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "\u00c9LECTION 2018".asOneTimePublisher(),
            subheadTextPublisher = "MAJORIT\u00c9: 63".asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
            linesPublisher = listOf(BarFrame.Line(63, "MAJORIT\u00c9")).asOneTimePublisher(),
            maxPublisher = 83.asOneTimePublisher(),
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "Accents", barFrame)
    }

    @Test
    fun testRenderMultiLineAccents() {
        val results: Publisher<List<ElectionResult>> = Publisher(
            listOf(
                ElectionResult("COALITION AVENIR QU\u00c9BEC\nFRAN\u00c7OIS LEGAULT", Color.BLUE, 74),
                ElectionResult("LIB\u00c9RAL\nPHILIPPE COUILLARD", Color.RED, 31),
                ElectionResult("PARTI QU\u00c9BECOIS\nJEAN-FRAN\u00c7OIS LIS\u00c9E", Color.CYAN, 10),
                ElectionResult("QU\u00c9BEC SOLIDAIRE\nMANON MASS\u00c9", Color.ORANGE, 10),
            ),
        )
        val barFrame = BarFrame(
            headerPublisher = "\u00c9LECTION 2018".asOneTimePublisher(),
            subheadTextPublisher = "MAJORIT\u00c9: 63".asOneTimePublisher(),
            barsPublisher = results.mapElements {
                BarFrame.Bar(
                    it.getPartyName(),
                    "${it.getNumSeats()}",
                    listOf(
                        Pair(it.getPartyColor(), it.getNumSeats()),
                    ),
                )
            },
            linesPublisher = listOf(BarFrame.Line(63, "MAJORIT\u00c9")).asOneTimePublisher(),
            maxPublisher = 83.asOneTimePublisher(),
        )
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "MultiLineAccents", barFrame)
    }

    @Test
    fun testBarFrameOverlaps() {
        val lines = Publisher(listOf(Triple("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "RIGHT\nSIDE", false)))
        val barFrame = BarFrame(
            headerPublisher = "BAR FRAME".asOneTimePublisher(),
            subheadTextPublisher = "".asOneTimePublisher(),
            barsPublisher = lines.mapElements {
                BarFrame.Bar(
                    it.first,
                    it.second,
                    if (it.third) createHalfTickShape() else null,
                    listOf(Pair(Color.RED, 1)),
                )
            },
            linesPublisher = listOf(BarFrame.Line(0.5, "")).asOneTimePublisher(),
            maxPublisher = 1.asOneTimePublisher(),
        )
        barFrame.setSize(256, 128)
        compareRendering("BarFrame", "FrameOverlap-1", barFrame)
        lines.submit(listOf(Triple("LEFT\nSIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", false)))
        compareRendering("BarFrame", "FrameOverlap-2", barFrame)
        lines.submit(listOf(Triple("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", false)))
        compareRendering("BarFrame", "FrameOverlap-3", barFrame)
        lines.submit(listOf(Triple("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "RIGHT\nSIDE", true)))
        compareRendering("BarFrame", "FrameOverlap-4", barFrame)
        lines.submit(listOf(Triple("LEFT\nSIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", true)))
        compareRendering("BarFrame", "FrameOverlap-5", barFrame)
        lines.submit(listOf(Triple("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", true)))
        compareRendering("BarFrame", "FrameOverlap-6", barFrame)
    }

    private fun createTickShape(): Shape {
        val shape = Area(Rectangle(0, 0, 100, 100))
        shape.subtract(Area(Polygon(intArrayOf(10, 40, 90, 80, 40, 20), intArrayOf(50, 80, 30, 20, 60, 40), 6)))
        return shape
    }

    private class ElectionResult constructor(private var partyName: String, private var partyColor: Color, private var numSeats: Int, private var seatEstimate: Int = numSeats) {

        fun getPartyName(): String = partyName

        fun getPartyColor(): Color = partyColor

        fun getNumSeats(): Int = numSeats

        fun setNumSeats(numSeats: Int) {
            this.numSeats = numSeats
        }

        fun getSeatEstimate(): Int = seatEstimate

        fun setSeatEstimate(seatEstimate: Int) {
            this.seatEstimate = seatEstimate
        }
    }

    private class RidingResult constructor(
        private val candidateName: String,
        private val partyName: String,
        private val partyColor: Color,
        private val numVotes: Int,
        private val votePct: Double,
        private val elected: Boolean = false,
    ) {
        fun getCandidateName(): String = candidateName

        fun getPartyName(): String = partyName

        fun getPartyColor(): Color = partyColor

        fun getNumVotes(): Int = numVotes

        fun getVotePct(): Double = votePct

        fun isElected(): Boolean = elected
    }

    companion object {
        private val CHANGE_FORMAT = DecimalFormat("+0;-0")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")
        private val PERCENT_FORMAT = DecimalFormat("0.0%")
        private fun lighten(color: Color?): Color = Color(
            (color!!.red + 255) / 2,
            (color.green + 255) / 2,
            (color.blue + 255) / 2,
        )
    }
}
