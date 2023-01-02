package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.mapElements
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.util.concurrent.TimeUnit

class HeatMapFrameTest {
    @Test
    fun testDots() {
        val borderColors = sequenceOf(
            generateSequence { Color.BLUE }.take(8),
            generateSequence { Color.RED }.take(18),
            sequenceOf(DARK_GREEN),
        )
            .flatten()
            .toList()
        val fillColors = listOf(
            Color.BLUE,
            Color.BLUE,
            Color.BLUE,
            Color.BLUE,
            Color.BLUE,
            Color.BLUE,
            Color.BLUE,
            Color.BLUE,
            DARK_GREEN,
            DARK_GREEN,
            DARK_GREEN,
            Color.BLUE,
            DARK_GREEN,
            Color.RED,
            Color.RED,
            Color.BLUE,
            DARK_GREEN,
            Color.RED,
            DARK_GREEN,
            Color.RED,
            Color.BLUE,
            Color.BLUE,
            DARK_GREEN,
            Color.BLUE,
            Color.RED,
            Color.RED,
            DARK_GREEN,
        )
        val frame = HeatMapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            numRowsPublisher = 3.asOneTimePublisher(),
            squaresPublisher = borderColors.zip(fillColors) { border, fill ->
                HeatMapFrame.Square(borderColor = border, fillColor = fill)
            }.asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(3))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(27))
        Assertions.assertEquals(Color.BLUE, frame.getSquareBorder(0))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(12))
        Assertions.assertEquals(DARK_GREEN, frame.getSquareBorder(26))
        Assertions.assertEquals(Color.BLUE, frame.getSquareFill(0))
        Assertions.assertEquals(DARK_GREEN, frame.getSquareFill(12))
        Assertions.assertEquals(DARK_GREEN, frame.getSquareFill(26))
    }

    @Test
    fun testSeatBar() {
        val frame = HeatMapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            numRowsPublisher = 1.asOneTimePublisher(),
            squaresPublisher = emptyList<HeatMapFrame.Square>().asOneTimePublisher(),
            seatBarsPublisher =
            listOf(
                HeatMapFrame.Bar(Color.BLUE, 8),
                HeatMapFrame.Bar(Color(128, 128, 255), 5),
            )
                .asOneTimePublisher(),
            seatBarLabelPublisher = "8/13".asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.seatBarCount }, IsEqual(2))
        Assertions.assertEquals(Color.BLUE, frame.getSeatBarColor(0))
        Assertions.assertEquals(5, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals("8/13", frame.seatBarLabel)
    }

    @Test
    fun testChangeBar() {
        val frame = HeatMapFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            numRowsPublisher = 1.asOneTimePublisher(),
            squaresPublisher = emptyList<HeatMapFrame.Square>().asOneTimePublisher(),
            changeBarsPublisher =
            listOf(
                HeatMapFrame.Bar(Color.BLUE, 3),
                HeatMapFrame.Bar(Color(128, 128, 255), 2),
            )
                .asOneTimePublisher(),
            changeBarLabelPublisher = "+3/+5".asOneTimePublisher(),
            changeBarStartPublisher = 5.asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.changeBarCount }, IsEqual(2))
        Assertions.assertEquals(Color.BLUE, frame.getChangeBarColor(0))
        Assertions.assertEquals(2, frame.getChangeBarSize(1).toLong())
        Assertions.assertEquals("+3/+5", frame.changeBarLabel)
        Assertions.assertEquals(5, frame.changeBarStart.toLong())
    }

    @Test
    fun testRenderEvenWide() {
        val results = peiResults
        val squares = Publisher(
            sequenceOf(
                20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                8, 27, 24, 17,
            )
                .map { results[it]!! }
                .toList(),
        )
        val seatBars = Publisher(
            listOf(Pair(Color.BLUE, 8), Pair(Color(128, 128, 255), 5)),
        )
        val seatLabel = Publisher("8/13")
        val changeBars = Publisher(
            listOf(Pair(Color.BLUE, 3), Pair(Color(128, 128, 255), 2)),
        )
        val changeLabel = Publisher("+3/+5")
        val changeStart = Publisher(8)
        val borderColor = Publisher(Color.BLUE)
        val header = Publisher<String?>("PROGRESSIVE CONSERVATIVE HEAT MAP")
        val frame = HeatMapFrame(
            headerPublisher = header,
            numRowsPublisher = 3.asOneTimePublisher(),
            squaresPublisher = squares,
            seatBarsPublisher = seatBars.mapElements { HeatMapFrame.Bar(it.first, it.second) },
            seatBarLabelPublisher = seatLabel,
            changeBarsPublisher = changeBars.mapElements { HeatMapFrame.Bar(it.first, it.second) },
            changeBarLabelPublisher = changeLabel,
            changeBarStartPublisher = changeStart,
            borderColorPublisher = borderColor,
        )
        frame.setSize(1024, 512)
        compareRendering("HeatMapFrame", "EvenWide-1", frame)
        squares.submit(
            sequenceOf(
                24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                19, 6, 20, 17,
            )
                .map { results[it]!! }
                .toList(),
        )
        seatBars.submit(listOf(Pair(Color.RED, 2), Pair(Color(255, 128, 128), 4)))
        seatLabel.submit("2/6")
        changeBars.submit(listOf(Pair(Color.RED, -4), Pair(Color(255, 128, 128), -8)))
        changeLabel.submit("-4/-12")
        changeStart.submit(18)
        borderColor.submit(Color.RED)
        header.submit("LIBERAL HEAT MAP")
        compareRendering("HeatMapFrame", "EvenWide-2", frame)
    }

    @Test
    fun testRenderEvenHigh() {
        val results = peiResults
        val squares = Publisher(
            sequenceOf(
                20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                8, 27, 24, 17,
            )
                .map { results[it]!! }
                .toList(),
        )
        val seatBars = Publisher(
            listOf(Pair(Color.BLUE, 8), Pair(Color(128, 128, 255), 5)),
        )
        val seatLabel = Publisher("8/13")
        val changeBars = Publisher(
            listOf(Pair(Color.BLUE, 3), Pair(Color(128, 128, 255), 2)),
        )
        val changeLabel = Publisher("+3/+5")
        val changeStart = Publisher(8)
        val borderColor = Publisher(Color.BLUE)
        val header = Publisher<String?>("PROGRESSIVE CONSERVATIVE HEAT MAP")
        val frame = HeatMapFrame(
            headerPublisher = header,
            numRowsPublisher = 9.asOneTimePublisher(),
            squaresPublisher = squares,
            seatBarsPublisher = seatBars.mapElements { HeatMapFrame.Bar(it.first, it.second) },
            seatBarLabelPublisher = seatLabel,
            changeBarsPublisher = changeBars.mapElements { HeatMapFrame.Bar(it.first, it.second) },
            changeBarLabelPublisher = changeLabel,
            changeBarStartPublisher = changeStart,
            borderColorPublisher = borderColor,
        )
        frame.setSize(1024, 512)
        compareRendering("HeatMapFrame", "EvenHigh-1", frame)
        squares.submit(
            sequenceOf(
                24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                19, 6, 20, 17,
            )
                .map { results[it]!! }
                .toList(),
        )
        seatBars.submit(listOf(Pair(Color.RED, 2), Pair(Color(255, 128, 128), 4)))
        seatLabel.submit("2/6")
        changeBars.submit(listOf(Pair(Color.RED, -4), Pair(Color(255, 128, 128), -8)))
        changeLabel.submit("-4/-12")
        changeStart.submit(18)
        borderColor.submit(Color.RED)
        header.submit("LIBERAL HEAT MAP")
        compareRendering("HeatMapFrame", "EvenHigh-2", frame)
    }

    @Test
    fun testRenderUneven() {
        val results = peiResults
        val squares = Publisher(
            sequenceOf(
                20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                8, 27, 24, 17,
            )
                .map { results[it]!! }
                .toList(),
        )
        val seatBars = Publisher(
            listOf(Pair(Color.BLUE, 8), Pair(Color(128, 128, 255), 5)),
        )
        val seatLabel = Publisher("8/13")
        val changeBars = Publisher(
            listOf(Pair(Color.BLUE, 3), Pair(Color(128, 128, 255), 2)),
        )
        val changeLabel = Publisher("+3/+5")
        val changeStart = Publisher(8)
        val borderColor = Publisher(Color.BLUE)
        val header = Publisher<String?>("PROGRESSIVE CONSERVATIVE HEAT MAP")
        val frame = HeatMapFrame(
            headerPublisher = header,
            numRowsPublisher = 5.asOneTimePublisher(),
            squaresPublisher = squares,
            seatBarsPublisher = seatBars.mapElements { HeatMapFrame.Bar(it.first, it.second) },
            seatBarLabelPublisher = seatLabel,
            changeBarsPublisher = changeBars.mapElements { HeatMapFrame.Bar(it.first, it.second) },
            changeBarLabelPublisher = changeLabel,
            changeBarStartPublisher = changeStart,
            borderColorPublisher = borderColor,
        )
        frame.setSize(1024, 512)
        compareRendering("HeatMapFrame", "Uneven-1", frame)
        squares.submit(
            sequenceOf(
                24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                19, 6, 20, 17,
            )
                .map { results[it]!! }
                .toList(),
        )
        seatBars.submit(listOf(Pair(Color.RED, 2), Pair(Color(255, 128, 128), 4)))
        seatLabel.submit("2/6")
        changeBars.submit(listOf(Pair(Color.RED, -4), Pair(Color(255, 128, 128), -8)))
        changeLabel.submit("-4/-12")
        changeStart.submit(18)
        borderColor.submit(Color.RED)
        header.submit("LIBERAL HEAT MAP")
        compareRendering("HeatMapFrame", "Uneven-2", frame)

        frame.moveMouse(500, 250)
        compareRendering("HeatMapFrame", "Uneven-3", frame)

        frame.moveMouse(600, 250)
        compareRendering("HeatMapFrame", "Uneven-4", frame)

        frame.moveMouse(-1, -1)
        compareRendering("HeatMapFrame", "Uneven-2", frame)
    }

    @Test
    fun testRenderChangeReversals() {
        val results = peiResults
        val squares = Publisher(
            sequenceOf(
                20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                8, 27, 24, 17,
            )
                .map { results[it]!! }
                .toList(),
        )
        val seatBars = Publisher(
            listOf(Pair(Color.BLUE, 2), Pair(Color(128, 128, 255), 2)),
        )
        val seatLabel = Publisher("2/4")
        val changeBars = Publisher(
            listOf(Pair(Color.BLUE, 3), Pair(Color(128, 128, 255), 2)),
        )
        val changeLabel = Publisher("+3/+5")
        val changeStart = Publisher(8)
        val borderColor = Publisher(Color.BLUE)
        val header = Publisher<String?>("PROGRESSIVE CONSERVATIVE HEAT MAP")
        val frame = HeatMapFrame(
            headerPublisher = header,
            numRowsPublisher = 5.asOneTimePublisher(),
            squaresPublisher = squares,
            seatBarsPublisher = seatBars.mapElements { HeatMapFrame.Bar(it.first, it.second) },
            seatBarLabelPublisher = seatLabel,
            changeBarsPublisher = changeBars.mapElements { HeatMapFrame.Bar(it.first, it.second) },
            changeBarLabelPublisher = changeLabel,
            changeBarStartPublisher = changeStart,
            borderColorPublisher = borderColor,
        )
        frame.setSize(1024, 512)
        changeBars.submit(listOf(Pair(Color.BLUE, 2), Pair(Color(128, 128, 255), -1)))
        changeLabel.submit("+2/+1")
        compareRendering("HeatMapFrame", "ChangeReversals-1", frame)
        changeBars.submit(listOf(Pair(Color.BLUE, -2), Pair(Color(128, 128, 255), 1)))
        changeLabel.submit("-2/-1")
        compareRendering("HeatMapFrame", "ChangeReversals-2", frame)
        changeBars.submit(listOf(Pair(Color.BLUE, 1), Pair(Color(128, 128, 255), -2)))
        changeLabel.submit("+1/-1")
        compareRendering("HeatMapFrame", "ChangeReversals-3", frame)
        changeBars.submit(listOf(Pair(Color.BLUE, -1), Pair(Color(128, 128, 255), 2)))
        changeLabel.submit("-1/+1")
        compareRendering("HeatMapFrame", "ChangeReversals-4", frame)
    }

    private val peiResults: Map<Int, HeatMapFrame.Square>
        get() = mapOf(
            1 to HeatMapFrame.Square(borderColor = Color.BLUE, fillColor = Color.BLUE, label = "Souris-Elmira (1)"),
            2 to HeatMapFrame.Square(borderColor = Color.BLUE, fillColor = Color.BLUE, label = "Georgetown-Pownall (2)"),
            3 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.BLUE, label = "Montague-Kilmuir (3)"),
            4 to HeatMapFrame.Square(borderColor = Color.BLUE, fillColor = Color.BLUE, label = "Belfast-Murray River (4)"),
            5 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = DARK_GREEN, label = "Mermaid-Stratford (5)"),
            6 to HeatMapFrame.Square(borderColor = Color.BLUE, fillColor = Color.BLUE, label = "Stratford-Keppoch (6)"),
            7 to HeatMapFrame.Square(borderColor = Color.BLUE, fillColor = Color.BLUE, label = "Morell-Donagh (7)"),
            8 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.BLUE, label = "Stanhope-Marshfield (8)"),
            9 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.BLUE, label = "Charlottetown-Hillsborough Park (9)"),
            10 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.RED, label = "Charlottetown-Winsloe (10)"),
            11 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = DARK_GREEN, label = "Charlottetown-Belvedere (11)"),
            12 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = DARK_GREEN, label = "Charlottetown-Victoria Park (12)"),
            13 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = DARK_GREEN, label = "Charlottetown-Brighton (13)"),
            14 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.RED, label = "Charlottetown-West Royalty (14)"),
            15 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.BLUE, label = "Brackley-Hunter River (15)"),
            16 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.RED, label = "Cornwall-Meadowbank (16)"),
            17 to HeatMapFrame.Square(borderColor = DARK_GREEN, fillColor = DARK_GREEN, label = "New Haven-Rocky Point (17)"),
            18 to HeatMapFrame.Square(borderColor = Color.BLUE, fillColor = Color.BLUE, label = "Rustico-Emerald (18)"),
            19 to HeatMapFrame.Square(borderColor = Color.BLUE, fillColor = Color.BLUE, label = "Borden-Kinkora (19)"),
            20 to HeatMapFrame.Square(borderColor = Color.BLUE, fillColor = Color.BLUE, label = "Kensington-Malpeque (20)"),
            21 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = DARK_GREEN, label = "Summerside-Wilmot (21)"),
            22 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = DARK_GREEN, label = "Summerside-South Drive (22)"),
            23 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = DARK_GREEN, label = "Tyne Valley-Sherbrooke (23)"),
            24 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.RED, label = "Evangeline-Miscouche (24)"),
            25 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.RED, label = "O'Leary-Inverness (25)"),
            26 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.BLUE, label = "Alberton-Bloomfield (26)"),
            27 to HeatMapFrame.Square(borderColor = Color.RED, fillColor = Color.RED, label = "Tignish-Palmer Road (27)"),
        )

    companion object {
        private val DARK_GREEN = Color.GREEN.darker()
    }
}
