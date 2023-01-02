package com.joecollins.graphics.components

import com.joecollins.graphics.components.HemicycleFrameBuilder.Tiebreaker
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

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
        val frame = HemicycleFrameBuilder.of(
            rows,
            dots,
            { it.first.asOneTimePublisher() },
            { it.second.asOneTimePublisher() },
            Tiebreaker.FRONT_ROW_FROM_RIGHT,
        )
            .withLeftSeatBars(leftSeatBars, { it.first }, { it.second }, "GREEN: 8".asOneTimePublisher())
            .withRightSeatBars(rightSeatBars, { it.first }, { it.second }, "PROGRESSIVE CONSERVATIVE: 13".asOneTimePublisher())
            .withMiddleSeatBars(middleSeatBars, { it.first }, { it.second }, "LIBERAL: 6".asOneTimePublisher())
            .withLeftChangeBars(leftChangeBars, { it.first }, { it.second }, 1.asOneTimePublisher(), "GRN: +7".asOneTimePublisher())
            .withRightChangeBars(rightChangeBars, { it.first }, { it.second }, 8.asOneTimePublisher(), "PC: +5".asOneTimePublisher())
            .withHeader("PEI".asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(3))
        Assertions.assertEquals(7, frame.getRowCount(0).toLong())
        Assertions.assertEquals(9, frame.getRowCount(1).toLong())
        Assertions.assertEquals(11, frame.getRowCount(2).toLong())
        Assertions.assertEquals(27, frame.numDots.toLong())
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
            Assertions.assertEquals(expectedDots[i], frame.getDotColor(i), "Dot color $i")
            Assertions.assertEquals(expectedBorders[i], frame.getDotBorder(i), "Dot border $i")
        }
        Assertions.assertEquals(1, frame.leftSeatBarCount.toLong())
        Assertions.assertEquals(Color.GREEN, frame.getLeftSeatBarColor(0))
        Assertions.assertEquals(8, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals("GREEN: 8", frame.getLeftSeatBarLabel())
        Assertions.assertEquals(1, frame.rightSeatBarCount.toLong())
        Assertions.assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        Assertions.assertEquals(13, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals("PROGRESSIVE CONSERVATIVE: 13", frame.getRightSeatBarLabel())
        Assertions.assertEquals(1, frame.middleSeatBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getMiddleSeatBarColor(0))
        Assertions.assertEquals(6, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals("LIBERAL: 6", frame.getMiddleSeatBarLabel())
        Assertions.assertEquals(1, frame.leftChangeBarCount.toLong())
        Assertions.assertEquals(Color.GREEN, frame.getLeftChangeBarColor(0))
        Assertions.assertEquals(7, frame.getLeftChangeBarSize(0).toLong())
        Assertions.assertEquals(1, frame.getLeftChangeBarStart().toLong())
        Assertions.assertEquals("GRN: +7", frame.getLeftChangeBarLabel())
        Assertions.assertEquals(1, frame.rightChangeBarCount.toLong())
        Assertions.assertEquals(Color.BLUE, frame.getRightChangeBarColor(0))
        Assertions.assertEquals(5, frame.getRightChangeBarSize(0).toLong())
        Assertions.assertEquals(8, frame.getRightChangeBarStart().toLong())
        Assertions.assertEquals("PC: +5", frame.getRightChangeBarLabel())
        Assertions.assertEquals("PEI", frame.header)
    }

    @Test
    fun testBasicLeadingElected() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)

        class Riding(val name: String, val leader: Party, val hasWon: Boolean, val prev: Party)

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
        val frame = HemicycleFrameBuilder.ofElectedLeading(
            listOf(ridings.size),
            ridings,
            { PartyResult(it.leader, it.hasWon).asOneTimePublisher() },
            { it.prev },
            lib,
            yp,
            { e, l -> "LIB: $e/$l" },
            { e, l -> "YP: $e/$l" },
            { e, l -> "OTH: $e/$l" },
            { e, l -> l > 0 },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            "YUKON".asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(19))
        Assertions.assertEquals(19, frame.numDots.toLong())
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(lighten(Color.RED), frame.getDotColor(0))
        Assertions.assertEquals("YUKON", frame.header)
        Assertions.assertEquals(2, frame.leftSeatBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getLeftSeatBarColor(0))
        Assertions.assertEquals(3, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.RED), frame.getLeftSeatBarColor(1))
        Assertions.assertEquals(8, frame.getLeftSeatBarSize(1).toLong())
        Assertions.assertEquals("LIB: 3/11", frame.getLeftSeatBarLabel())
        Assertions.assertEquals(2, frame.rightSeatBarCount.toLong())
        Assertions.assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        Assertions.assertEquals(3, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.BLUE), frame.getRightSeatBarColor(1))
        Assertions.assertEquals(3, frame.getRightSeatBarSize(1).toLong())
        Assertions.assertEquals("YP: 3/6", frame.getRightSeatBarLabel())
        Assertions.assertEquals(2, frame.middleSeatBarCount.toLong())
        Assertions.assertEquals(Color.DARK_GRAY, frame.getMiddleSeatBarColor(0))
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.DARK_GRAY), frame.getMiddleSeatBarColor(1))
        Assertions.assertEquals(2, frame.getMiddleSeatBarSize(1).toLong())
        Assertions.assertEquals("OTH: 0/2", frame.getMiddleSeatBarLabel())
        Assertions.assertEquals(2, frame.getLeftChangeBarStart().toLong())
        Assertions.assertEquals(2, frame.leftChangeBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getLeftChangeBarColor(0))
        Assertions.assertEquals(2, frame.getLeftChangeBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.RED), frame.getLeftChangeBarColor(1))
        Assertions.assertEquals(7, frame.getLeftChangeBarSize(1).toLong())
        Assertions.assertEquals("+2/+9", frame.getLeftChangeBarLabel())
        Assertions.assertEquals(11, frame.getRightChangeBarStart().toLong())
        Assertions.assertEquals(0, frame.rightChangeBarCount.toLong())
        Assertions.assertEquals("", frame.getRightChangeBarLabel())
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
        val frame = HemicycleFrameBuilder.ofElectedLeading(
            listOf(results.size),
            results,
            { it.publisher },
            { it.prev },
            dem,
            gop,
            { e, l -> "DEM: $e/$l" },
            { e, l -> "GOP: $e/$l" },
            { e, l -> "OTH: $e/$l" },
            { e, l -> l > 0 },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            "TEST".asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(30))
        Assertions.assertEquals(30, frame.numDots.toLong())
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getDotColor(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftSeatBarCount }, IsEqual(2))
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.rightSeatBarCount }, IsEqual(2))
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.middleSeatBarCount }, IsEqual(2))
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        result.setResult(gop, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(lighten(Color.RED)))
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(lighten(Color.RED), frame.getDotColor(0))
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(30, frame.getRightSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        result.setResult(dem, true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(Color.BLUE))
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(Color.BLUE, frame.getDotColor(0))
        Assertions.assertEquals(30, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
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
        val frame = HemicycleFrameBuilder.ofElectedLeading(
            listOf(results.sumOf { it.numSeats }),
            results,
            { it.numSeats },
            { it.publisher },
            { it.prev },
            dem,
            gop,
            { e, l -> "DEM: $e/$l" },
            { e, l -> "GOP: $e/$l" },
            { e, l -> "OTH: $e/$l" },
            { e, l -> true },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            "TEST".asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(30))
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getDotColor(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftSeatBarCount }, IsEqual(2))
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.rightSeatBarCount }, IsEqual(2))
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.middleSeatBarCount }, IsEqual(2))
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftChangeBarCount }, IsEqual(2))
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.rightChangeBarCount }, IsEqual(2))
        Assertions.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
        result.setResult(gop, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(lighten(Color.RED)))
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(lighten(Color.RED), frame.getDotColor(0))
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(30, frame.getRightSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
        result.setResult(dem, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(lighten(Color.BLUE)))
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(lighten(Color.BLUE), frame.getDotColor(0))
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(30, frame.getLeftSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assertions.assertEquals(30, frame.getLeftChangeBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assertions.assertEquals(-30, frame.getRightChangeBarSize(1).toLong())
        result.setResult(dem, true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(Color.BLUE))
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(Color.BLUE, frame.getDotColor(0))
        Assertions.assertEquals(30, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assertions.assertEquals(30, frame.getLeftChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Assertions.assertEquals(-30, frame.getRightChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
    }

    @Test
    fun testBarsUpdateProperlyOnIncompleteResults() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party, val numSeats: Int) {
            val publisher = Publisher<PartyResult?>(leader?.let { PartyResult(it, hasWon) })

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                publisher.submit(leader?.let { PartyResult(it, hasWon) })
            }
        }

        val result = Result(null, false, gop, 30)
        val results = listOf(result)
        val frame = HemicycleFrameBuilder.ofElectedLeading(
            listOf(results.sumOf { it.numSeats }),
            results,
            { it.numSeats },
            { it.publisher },
            { it.prev },
            dem,
            gop,
            { e, l -> "DEM: $e/$l" },
            { e, l -> "GOP: $e/$l" },
            { e, l -> "OTH: $e/$l" },
            { e, l -> true },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            "TEST".asOneTimePublisher(),
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(30))
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getDotColor(0))
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
        result.setResult(null, false)
        Assertions.assertEquals(Color.RED, frame.getDotBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getDotColor(0))
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
    }
}
