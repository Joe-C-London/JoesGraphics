package com.joecollins.graphics.components

import com.joecollins.graphics.components.HemicycleFrameBuilder.Tiebreaker
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color
import java.text.DecimalFormat

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class HemicycleFrameBuilderTest {
    @Test
    fun testHemicycleBasic() {
        val rows = listOf(7, 9, 11)
        val dots = sequenceOf(
            sequenceOf(Pair(Color.GREEN, Color.GREEN)),
            generateSequence { Pair(Color.GREEN, Color.RED) }.take(7),
            generateSequence { Pair(Color.RED, Color.RED) }.take(6),
            generateSequence { Pair(Color.BLUE, Color.RED) }.take(5),
            generateSequence { Pair(Color.BLUE, Color.BLUE) }.take(8),
        ).flatten().toList()
        val leftSeatBars = Publisher(listOf(Pair(Color.GREEN, 8)))
        val rightSeatBars = Publisher(listOf(Pair(Color.BLUE, 13)))
        val middleSeatBars = Publisher(listOf(Pair(Color.RED, 6)))
        val leftChangeBars = Publisher(listOf(Pair(Color.GREEN, +7)))
        val rightChangeBars = Publisher(listOf(Pair(Color.BLUE, +5)))
        val frame = HemicycleFrameBuilder.build<Pair<Color, Color>, Pair<Color, Int>, Pair<Color, Int>>(
            dots = {
                this.rows = rows
                this.entries = dots
                this.colorFunc = { first.asOneTimePublisher() }
                this.borderFunc = { second.asOneTimePublisher() }
                this.tiebreaker = Tiebreaker.FRONT_ROW_FROM_RIGHT
            },
            leftSeats = {
                bars = leftSeatBars
                colorFunc = { first }
                seatFunc = { second }
                labelPublisher = "GREEN: 8".asOneTimePublisher()
            },
            rightSeats = {
                bars = rightSeatBars
                colorFunc = { first }
                seatFunc = { second }
                labelPublisher = "PROGRESSIVE CONSERVATIVE: 13".asOneTimePublisher()
            },
            middleSeats = {
                bars = middleSeatBars
                colorFunc = { first }
                seatFunc = { second }
                labelPublisher = "LIBERAL: 6".asOneTimePublisher()
            },
            leftChange = {
                bars = leftChangeBars
                colorFunc = { first }
                seatFunc = { second }
                startPublisher = 1.asOneTimePublisher()
                labelPublisher = "GRN: +7".asOneTimePublisher()
            },
            rightChange = {
                bars = rightChangeBars
                colorFunc = { first }
                seatFunc = { second }
                startPublisher = 8.asOneTimePublisher()
                labelPublisher = "PC: +5".asOneTimePublisher()
            },
            header = "PEI".asOneTimePublisher(),
        )
        assertEquals(3, frame.numRows)
        assertEquals(7, frame.getRowCount(0).toLong())
        assertEquals(9, frame.getRowCount(1).toLong())
        assertEquals(11, frame.getRowCount(2).toLong())
        assertEquals(27, frame.numDots.toLong())
        val expectedDots = listOf(
            Color.GREEN, Color.GREEN, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE,
        )
        val expectedBorders = listOf(
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE,
        )
        for (i in 0 until frame.numDots) {
            assertEquals(expectedDots[i], frame.getDotColor(i), "Dot color $i")
            assertEquals(expectedBorders[i], frame.getDotBorder(i), "Dot border $i")
        }
        assertEquals(1, frame.leftSeatBarCount.toLong())
        assertEquals(Color.GREEN, frame.getLeftSeatBarColor(0))
        assertEquals(8, frame.getLeftSeatBarSize(0).toLong())
        assertEquals("GREEN: 8", frame.getLeftSeatBarLabel())
        assertEquals(1, frame.rightSeatBarCount.toLong())
        assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        assertEquals(13, frame.getRightSeatBarSize(0).toLong())
        assertEquals("PROGRESSIVE CONSERVATIVE: 13", frame.getRightSeatBarLabel())
        assertEquals(1, frame.middleSeatBarCount.toLong())
        assertEquals(Color.RED, frame.getMiddleSeatBarColor(0))
        assertEquals(6, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals("LIBERAL: 6", frame.getMiddleSeatBarLabel())
        assertEquals(1, frame.leftChangeBarCount.toLong())
        assertEquals(Color.GREEN, frame.getLeftChangeBarColor(0))
        assertEquals(7, frame.getLeftChangeBarSize(0).toLong())
        assertEquals(1, frame.getLeftChangeBarStart().toLong())
        assertEquals("GRN: +7", frame.getLeftChangeBarLabel())
        assertEquals(1, frame.rightChangeBarCount.toLong())
        assertEquals(Color.BLUE, frame.getRightChangeBarColor(0))
        assertEquals(5, frame.getRightChangeBarSize(0).toLong())
        assertEquals(8, frame.getRightChangeBarStart().toLong())
        assertEquals("PC: +5", frame.getRightChangeBarLabel())
        assertEquals("PEI", frame.header)
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
        val frame = HemicycleFrameBuilder.buildElectedLeading(
            rows = listOf(ridings.size),
            entries = ridings,
            resultFunc = { PartyResult(leader, hasWon).asOneTimePublisher() },
            prevResultFunc = { prev },
            leftParty = lib,
            rightParty = yp,
            leftLabel = { "LIB: $elected/$total" },
            rightLabel = { "YP: $elected/$total" },
            otherLabel = { "OTH: $elected/$total" },
            showChange = { total > 0 },
            changeLabel = { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            tiebreaker = Tiebreaker.FRONT_ROW_FROM_LEFT,
            header = "YUKON".asOneTimePublisher(),
        )
        assertEquals(19, frame.numDots.toLong())
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(lighten(Color.RED), frame.getDotColor(0))
        assertEquals("YUKON", frame.header)
        assertEquals(2, frame.leftSeatBarCount.toLong())
        assertEquals(Color.RED, frame.getLeftSeatBarColor(0))
        assertEquals(3, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(lighten(Color.RED), frame.getLeftSeatBarColor(1))
        assertEquals(8, frame.getLeftSeatBarSize(1).toLong())
        assertEquals("LIB: 3/11", frame.getLeftSeatBarLabel())
        assertEquals(2, frame.rightSeatBarCount.toLong())
        assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        assertEquals(3, frame.getRightSeatBarSize(0).toLong())
        assertEquals(lighten(Color.BLUE), frame.getRightSeatBarColor(1))
        assertEquals(3, frame.getRightSeatBarSize(1).toLong())
        assertEquals("YP: 3/6", frame.getRightSeatBarLabel())
        assertEquals(2, frame.middleSeatBarCount.toLong())
        assertEquals(Color.DARK_GRAY, frame.getMiddleSeatBarColor(0))
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(lighten(Color.DARK_GRAY), frame.getMiddleSeatBarColor(1))
        assertEquals(2, frame.getMiddleSeatBarSize(1).toLong())
        assertEquals("OTH: 0/2", frame.getMiddleSeatBarLabel())
        assertEquals(2, frame.getLeftChangeBarStart().toLong())
        assertEquals(2, frame.leftChangeBarCount.toLong())
        assertEquals(Color.RED, frame.getLeftChangeBarColor(0))
        assertEquals(2, frame.getLeftChangeBarSize(0).toLong())
        assertEquals(lighten(Color.RED), frame.getLeftChangeBarColor(1))
        assertEquals(7, frame.getLeftChangeBarSize(1).toLong())
        assertEquals("+2/+9", frame.getLeftChangeBarLabel())
        assertEquals(11, frame.getRightChangeBarStart().toLong())
        assertEquals(0, frame.rightChangeBarCount.toLong())
        assertEquals("", frame.getRightChangeBarLabel())
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
        val frame = HemicycleFrameBuilder.buildElectedLeading(
            rows = listOf(results.size),
            entries = results,
            resultFunc = { publisher },
            prevResultFunc = { prev },
            leftParty = dem,
            rightParty = gop,
            leftLabel = { "DEM: $elected/$total" },
            rightLabel = { "GOP: $elected/$total" },
            otherLabel = { "OTH: $elected/$total" },
            showChange = { total > 0 },
            changeLabel = { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            tiebreaker = Tiebreaker.FRONT_ROW_FROM_LEFT,
            header = "TEST".asOneTimePublisher(),
        )
        assertEquals(30, frame.numDots.toLong())
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(Color.WHITE, frame.getDotColor(0))
        assertEquals(2, frame.leftSeatBarCount)
        assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(2, frame.rightSeatBarCount)
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        assertEquals(2, frame.middleSeatBarCount)
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())

        result.setResult(gop, false)
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(lighten(Color.RED), frame.getDotColor(0))
        assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(30, frame.getRightSeatBarSize(1).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())

        result.setResult(dem, true)
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(Color.BLUE, frame.getDotColor(0))
        assertEquals(30, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
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
        val frame = HemicycleFrameBuilder.buildElectedLeading(
            rows = listOf(results.sumOf { it.numSeats }),
            entries = results,
            seatsFunc = { numSeats },
            resultFunc = { publisher },
            prevResultFunc = { prev },
            leftParty = dem,
            rightParty = gop,
            leftLabel = { "DEM: $elected/$total" },
            rightLabel = { "GOP: $elected/$total" },
            otherLabel = { "OTH: $elected/$total" },
            showChange = { true },
            changeLabel = { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            tiebreaker = Tiebreaker.FRONT_ROW_FROM_LEFT,
            header = "TEST".asOneTimePublisher(),
        )
        assertEquals(30, frame.numDots)
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(Color.WHITE, frame.getDotColor(0))
        assertEquals(2, frame.leftSeatBarCount)
        assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(2, frame.rightSeatBarCount)
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        assertEquals(2, frame.middleSeatBarCount)
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        assertEquals(2, frame.leftChangeBarCount)
        assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        assertEquals(2, frame.rightChangeBarCount)
        assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        assertEquals(0, frame.getRightChangeBarSize(1).toLong())

        result.setResult(gop, false)
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(lighten(Color.RED), frame.getDotColor(0))
        assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(30, frame.getRightSeatBarSize(1).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        assertEquals(0, frame.getRightChangeBarSize(1).toLong())

        result.setResult(dem, false)
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(lighten(Color.BLUE), frame.getDotColor(0))
        assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(30, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        assertEquals(30, frame.getLeftChangeBarSize(1).toLong())
        assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        assertEquals(-30, frame.getRightChangeBarSize(1).toLong())

        result.setResult(dem, true)
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(Color.BLUE, frame.getDotColor(0))
        assertEquals(30, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        assertEquals(30, frame.getLeftChangeBarSize(0).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        assertEquals(-30, frame.getRightChangeBarSize(0).toLong())
        assertEquals(0, frame.getRightChangeBarSize(1).toLong())
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
        val frame = HemicycleFrameBuilder.buildElectedLeading(
            rows = listOf(results.sumOf { it.numSeats }),
            entries = results,
            seatsFunc = { numSeats },
            resultFunc = { publisher },
            prevResultFunc = { prev },
            leftParty = dem,
            rightParty = gop,
            leftLabel = { "DEM: $elected/$total" },
            rightLabel = { "GOP: $elected/$total" },
            otherLabel = { "OTH: $elected/$total" },
            showChange = { true },
            changeLabel = { DecimalFormat("+0;-0").format(elected) + "/" + DecimalFormat("+0;-0").format(total) },
            tiebreaker = Tiebreaker.FRONT_ROW_FROM_LEFT,
            header = "TEST".asOneTimePublisher(),
        )
        assertEquals(30, frame.numDots)
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(Color.WHITE, frame.getDotColor(0))
        assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        assertEquals(0, frame.getRightChangeBarSize(1).toLong())
        result.setResult(null, false)
        assertEquals(Color.RED, frame.getDotBorder(0))
        assertEquals(Color.WHITE, frame.getDotColor(0))
        assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        assertEquals(0, frame.getRightChangeBarSize(1).toLong())
    }
}
