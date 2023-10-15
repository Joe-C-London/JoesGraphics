package com.joecollins.graphics.components

import com.joecollins.graphics.components.HeatMapFrameBuilder.changeBars
import com.joecollins.graphics.components.HeatMapFrameBuilder.seatBars
import com.joecollins.graphics.components.HeatMapFrameBuilder.squares
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.text.DecimalFormat

class HeatMapFrameBuilderTest {
    @Test
    fun testHeatMapBasic() {
        val dots = sequenceOf(
            sequenceOf(Pair(Color.GREEN, Color.GREEN)),
            generateSequence { Pair(Color.GREEN, Color.RED) }.take(7),
            generateSequence { Pair(Color.RED, Color.RED) }.take(6),
            generateSequence { Pair(Color.BLUE, Color.RED) }.take(5),
            generateSequence { Pair(Color.BLUE, Color.BLUE) }.take(8),
        ).flatten().toList()
        val seatBars = Publisher(listOf(Pair(Color.GREEN, 8)))
        val changeBars = Publisher(listOf(Pair(Color.GREEN, +7)))
        val frame = HeatMapFrameBuilder.build(
            squares = squares<Pair<Color, Color>> {
                numRows = 3.asOneTimePublisher()
                entries = dots
                fill = { first.asOneTimePublisher() }
                border = { second.asOneTimePublisher() }
            },
            seatBars = seatBars<Pair<Color, Int>> {
                bars = seatBars
                colorFunc = { it.first }
                seatFunc = { it.second }
                labelPublisher = "GREEN: 8".asOneTimePublisher()
            },
            changeBars = changeBars<Pair<Color, Int>> {
                bars = changeBars
                colorFunc = { it.first }
                seatFunc = { it.second }
                startPublisher = 1.asOneTimePublisher()
                labelPublisher = "GRN: +7".asOneTimePublisher()
            },
            header = "PEI".asOneTimePublisher(),
            borderColor = Color.GREEN.asOneTimePublisher(),
        )
        assertEquals(3, frame.numRows)
        assertEquals(27, frame.numSquares.toLong())
        val expectedFills = sequenceOf(
            generateSequence { Color.GREEN }.take(8),
            generateSequence { Color.RED }.take(6),
            generateSequence { Color.BLUE }.take(13),
        )
            .flatten()
            .toList()
        val expectedBorders = sequenceOf(
            generateSequence { Color.GREEN }.take(1),
            generateSequence { Color.RED }.take(18),
            generateSequence { Color.BLUE }.take(8),
        )
            .flatten()
            .toList()
        for (i in 0 until frame.numSquares) {
            assertEquals(expectedFills[i], frame.getSquareFill(i), "Square fill $i")
            assertEquals(expectedBorders[i], frame.getSquareBorder(i), "Square border $i")
        }
        assertEquals(1, frame.seatBarCount.toLong())
        assertEquals(Color.GREEN, frame.getSeatBarColor(0))
        assertEquals(8, frame.getSeatBarSize(0).toLong())
        assertEquals("GREEN: 8", frame.seatBarLabel)
        assertEquals(1, frame.changeBarCount.toLong())
        assertEquals(Color.GREEN, frame.getChangeBarColor(0))
        assertEquals(7, frame.getChangeBarSize(0).toLong())
        assertEquals(1, frame.changeBarStart.toLong())
        assertEquals("GRN: +7", frame.changeBarLabel)
        assertEquals("PEI", frame.header)
        assertEquals(Color.GREEN, frame.borderColor)
    }

    @Test
    fun testBasicLeadingElected() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)

        class Riding(@Suppress("unused") val name: String, val leader: Party, val hasWon: Boolean, val prev: Party)

        val ridings = listOf(
            Riding("Vuntut Gwitchin", lib, false, lib),
            Riding("Klondike", lib, true, lib),
            Riding("Takhini-Copper King", ndp, false, ndp),
            Riding("Whitehorse Centre", ndp, false, ndp),
            Riding("Mayo-Tatchun", lib, true, ndp),
            Riding("Mount Lorne-Southern Lakes", lib, false, ndp),
            Riding("Riverdale South", lib, false, ndp),
            Riding("Copperbelt South", yp, false, ndp),
            Riding("Porter Creek South", lib, false, yp),
            Riding("Watson Lake", yp, true, yp),
            Riding("Porter Creek Centre", lib, false, yp),
            Riding("Riverdale North", lib, true, yp),
            Riding("Kluane", yp, false, yp),
            Riding("Mountainview", lib, false, yp),
            Riding("Copperbelt North", lib, false, yp),
            Riding("Pelly-Nisutlin", yp, true, yp),
            Riding("Porter Creek North", yp, false, yp),
            Riding("Lake Laberge", yp, true, yp),
            Riding("Whitehorse West", lib, false, yp),
        )
        val frame = HeatMapFrameBuilder.buildElectedLeading(
            3.asOneTimePublisher(),
            ridings,
            { PartyResult(leader, hasWon).asOneTimePublisher() },
            { prev },
            lib,
            { "LIB: $elected/$total" },
            { total > 0 },
            { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            "YUKON".asOneTimePublisher(),
        )
        assertEquals(19, frame.numSquares)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        assertEquals("YUKON", frame.header)
        assertEquals(Color.RED, frame.borderColor)
        assertEquals(2, frame.seatBarCount.toLong())
        assertEquals(Color.RED, frame.getSeatBarColor(0))
        assertEquals(3, frame.getSeatBarSize(0).toLong())
        assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        assertEquals(8, frame.getSeatBarSize(1).toLong())
        assertEquals("LIB: 3/11", frame.seatBarLabel)
        assertEquals(2, frame.changeBarStart.toLong())
        assertEquals(2, frame.changeBarCount.toLong())
        assertEquals(Color.RED, frame.getChangeBarColor(0))
        assertEquals(2, frame.getChangeBarSize(0).toLong())
        assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        assertEquals(7, frame.getChangeBarSize(1).toLong())
        assertEquals("+2/+9", frame.changeBarLabel)
    }

    @Test
    fun testBasicLeadingElectedFiltered() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)

        class Riding(@Suppress("unused") val name: String, val leader: Party, val hasWon: Boolean, val prev: Party, val isWhitehorse: Boolean)

        val ridings = listOf(
            Riding("Vuntut Gwitchin", lib, false, lib, false),
            Riding("Klondike", lib, true, lib, false),
            Riding("Takhini-Copper King", ndp, false, ndp, true),
            Riding("Whitehorse Centre", ndp, false, ndp, true),
            Riding("Mayo-Tatchun", lib, true, ndp, false),
            Riding("Mount Lorne-Southern Lakes", lib, false, ndp, false),
            Riding("Riverdale South", lib, false, ndp, true),
            Riding("Copperbelt South", yp, false, ndp, true),
            Riding("Porter Creek South", lib, false, yp, true),
            Riding("Watson Lake", yp, true, yp, true),
            Riding("Porter Creek Centre", lib, false, yp, true),
            Riding("Riverdale North", lib, true, yp, true),
            Riding("Kluane", yp, false, yp, false),
            Riding("Mountainview", lib, false, yp, false),
            Riding("Copperbelt North", lib, false, yp, true),
            Riding("Pelly-Nisutlin", yp, true, yp, false),
            Riding("Porter Creek North", yp, false, yp, true),
            Riding("Lake Laberge", yp, true, yp, false),
            Riding("Whitehorse West", lib, false, yp, true),
        )
        val filter = Publisher<(Riding) -> Boolean> { true }
        val frame = HeatMapFrameBuilder.buildElectedLeading(
            3.asOneTimePublisher(),
            ridings,
            { PartyResult(leader, hasWon).asOneTimePublisher() },
            { prev },
            lib,
            { "LIB: $elected/$total" },
            { total > 0 },
            { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            "YUKON".asOneTimePublisher(),
            filter = filter,
        )

        assertEquals(19, frame.numSquares)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        assertEquals(Color.ORANGE, frame.getSquareBorder(2))
        assertEquals(lighten(Color.ORANGE), frame.getSquareFill(2))
        assertEquals("YUKON", frame.header)
        assertEquals(Color.RED, frame.borderColor)
        assertEquals(2, frame.seatBarCount.toLong())
        assertEquals(Color.RED, frame.getSeatBarColor(0))
        assertEquals(3, frame.getSeatBarSize(0).toLong())
        assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        assertEquals(8, frame.getSeatBarSize(1).toLong())
        assertEquals("LIB: 3/11", frame.seatBarLabel)
        assertEquals(2, frame.changeBarStart.toLong())
        assertEquals(2, frame.changeBarCount.toLong())
        assertEquals(Color.RED, frame.getChangeBarColor(0))
        assertEquals(2, frame.getChangeBarSize(0).toLong())
        assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        assertEquals(7, frame.getChangeBarSize(1).toLong())
        assertEquals("+2/+9", frame.changeBarLabel)

        filter.submit { it.isWhitehorse }
        assertEquals(Color.WHITE, frame.getSquareBorder(0))
        assertEquals(Color.WHITE, frame.getSquareFill(0))
        assertEquals(Color.ORANGE, frame.getSquareBorder(2))
        assertEquals(lighten(Color.ORANGE), frame.getSquareFill(2))
        assertEquals("YUKON", frame.header)
        assertEquals(Color.RED, frame.borderColor)
        assertEquals(2, frame.seatBarCount.toLong())
        assertEquals(Color.RED, frame.getSeatBarColor(0))
        assertEquals(3, frame.getSeatBarSize(0).toLong())
        assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        assertEquals(8, frame.getSeatBarSize(1).toLong())
        assertEquals("LIB: 3/11", frame.seatBarLabel)
        assertEquals(2, frame.changeBarStart.toLong())
        assertEquals(2, frame.changeBarCount.toLong())
        assertEquals(Color.RED, frame.getChangeBarColor(0))
        assertEquals(2, frame.getChangeBarSize(0).toLong())
        assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        assertEquals(7, frame.getChangeBarSize(1).toLong())
        assertEquals("+2/+9", frame.changeBarLabel)
    }

    @Test
    fun testLeadingElectedSeatsRepeated() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party) {
            val publisher = Publisher(leader?.let { PartyResult(it, hasWon) })

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                publisher.submit(if (leader == null) null else PartyResult(leader, hasWon))
            }
        }

        val result = Result(null, false, gop)
        val results = generateSequence { result }.take(30).toList()
        val frame = HeatMapFrameBuilder.buildElectedLeading(
            results.size.asOneTimePublisher(),
            results,
            { publisher },
            { prev },
            dem,
            { "DEM: $elected/$total" },
            { total > 0 },
            { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            "TEST".asOneTimePublisher(),
        )
        assertEquals(30, frame.numSquares)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(Color.WHITE, frame.getSquareFill(0))
        assertEquals(0, frame.getSeatBarSize(0).toLong())
        assertEquals(0, frame.getSeatBarSize(1).toLong())

        result.setResult(gop, false)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        assertEquals(0, frame.getSeatBarSize(0).toLong())
        assertEquals(0, frame.getSeatBarSize(1).toLong())

        result.setResult(dem, true)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(Color.BLUE, frame.getSquareFill(0))
        assertEquals(30, frame.getSeatBarSize(0).toLong())
        assertEquals(0, frame.getSeatBarSize(1).toLong())
    }

    @Test
    fun testLeadingElectedMultiseat() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party, val numSeats: Int) {
            val publisher = Publisher(leader?.let { PartyResult(it, hasWon) })

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                publisher.submit(if (leader == null) null else PartyResult(leader, hasWon))
            }
        }

        val result = Result(null, false, gop, 30)
        val results = listOf(result)
        val frame = HeatMapFrameBuilder.buildElectedLeading(
            results.sumOf { it.numSeats }.asOneTimePublisher(),
            results,
            { publisher },
            { prev },
            dem,
            { "DEM: $elected/$total" },
            { true },
            { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            "TEST".asOneTimePublisher(),
            seats = { numSeats },
        )
        assertEquals(30, frame.numSquares)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(Color.WHITE, frame.getSquareFill(0))
        assertEquals(0, frame.getSeatBarSize(0).toLong())
        assertEquals(0, frame.getSeatBarSize(1).toLong())
        assertEquals(0, frame.getChangeBarSize(0).toLong())
        assertEquals(0, frame.getChangeBarSize(1).toLong())

        result.setResult(gop, false)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        assertEquals(0, frame.getSeatBarSize(0).toLong())
        assertEquals(0, frame.getSeatBarSize(1).toLong())
        assertEquals(0, frame.getChangeBarSize(0).toLong())
        assertEquals(0, frame.getChangeBarSize(1).toLong())

        result.setResult(dem, false)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(lighten(Color.BLUE), frame.getSquareFill(0))
        assertEquals(0, frame.getSeatBarSize(0).toLong())
        assertEquals(30, frame.getSeatBarSize(1).toLong())
        assertEquals(0, frame.getChangeBarSize(0).toLong())
        assertEquals(30, frame.getChangeBarSize(1).toLong())

        result.setResult(dem, true)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(Color.BLUE, frame.getSquareFill(0))
        assertEquals(30, frame.getSeatBarSize(0).toLong())
        assertEquals(0, frame.getSeatBarSize(1).toLong())
        assertEquals(30, frame.getChangeBarSize(0).toLong())
        assertEquals(0, frame.getChangeBarSize(1).toLong())
    }

    @Test
    fun testBarsUpdateProperlyOnIncompleteResults() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party, val numSeats: Int) {
            val publisher = Publisher(leader?.let { PartyResult(it, hasWon) })

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                publisher.submit(leader?.let { PartyResult(it, hasWon) })
            }
        }

        val result = Result(null, false, gop, 30)
        val results = listOf(result)
        val frame = HeatMapFrameBuilder.buildElectedLeading(
            results.sumOf { it.numSeats }.asOneTimePublisher(),
            results,
            { publisher },
            { prev },
            dem,
            { "DEM: $elected/$total" },
            { true },
            { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            "TEST".asOneTimePublisher(),
            seats = { numSeats },
        )
        assertEquals(30, frame.numSquares)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(Color.WHITE, frame.getSquareFill(0))
        assertEquals(0, frame.getSeatBarSize(0).toLong())
        assertEquals(0, frame.getSeatBarSize(1).toLong())
        assertEquals(0, frame.getChangeBarSize(0).toLong())
        assertEquals(0, frame.getChangeBarSize(1).toLong())

        result.setResult(null, false)
        assertEquals(Color.RED, frame.getSquareBorder(0))
        assertEquals(Color.WHITE, frame.getSquareFill(0))
        assertEquals(0, frame.getSeatBarSize(0).toLong())
        assertEquals(0, frame.getSeatBarSize(1).toLong())
        assertEquals(0, frame.getChangeBarSize(0).toLong())
        assertEquals(0, frame.getChangeBarSize(1).toLong())
    }
}
