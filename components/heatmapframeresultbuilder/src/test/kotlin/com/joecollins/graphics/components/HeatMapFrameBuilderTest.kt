package com.joecollins.graphics.components

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

class HeatMapFrameBuilderTest {
    @Test
    fun testHeatMapBasic() {
        val dots = sequenceOf(
            sequenceOf(Pair(Color.GREEN, Color.GREEN)),
            generateSequence { Pair(Color.GREEN, Color.RED) }.take(7),
            generateSequence { Pair(Color.RED, Color.RED) }.take(6),
            generateSequence { Pair(Color.BLUE, Color.RED) }.take(5),
            generateSequence { Pair(Color.BLUE, Color.BLUE) }.take(8)
        ).flatten().toList()
        val seatBars = Publisher(listOf(Pair(Color.GREEN, 8)))
        val changeBars = Publisher(listOf(Pair(Color.GREEN, +7)))
        val frame = HeatMapFrameBuilder.of(
            3.asOneTimePublisher(),
            dots,
            { it.first.asOneTimePublisher() },
            { it.second.asOneTimePublisher() },
            { null.asOneTimePublisher() }
        )
            .withSeatBars(seatBars, { it.first }, { it.second }, "GREEN: 8".asOneTimePublisher())
            .withChangeBars(
                changeBars,
                { it.first },
                { it.second },
                1.asOneTimePublisher(),
                "GRN: +7".asOneTimePublisher()
            )
            .withHeader("PEI".asOneTimePublisher())
            .withBorder(Color.GREEN.asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(3))
        Assertions.assertEquals(27, frame.numSquares.toLong())
        val expectedFills = sequenceOf(
            generateSequence { Color.GREEN }.take(8),
            generateSequence { Color.RED }.take(6),
            generateSequence { Color.BLUE }.take(13)
        )
            .flatten()
            .toList()
        val expectedBorders = sequenceOf(
            generateSequence { Color.GREEN }.take(1),
            generateSequence { Color.RED }.take(18),
            generateSequence { Color.BLUE }.take(8)
        )
            .flatten()
            .toList()
        for (i in 0 until frame.numSquares) {
            Assertions.assertEquals(expectedFills[i], frame.getSquareFill(i), "Square fill $i")
            Assertions.assertEquals(expectedBorders[i], frame.getSquareBorder(i), "Square border $i")
        }
        Assertions.assertEquals(1, frame.seatBarCount.toLong())
        Assertions.assertEquals(Color.GREEN, frame.getSeatBarColor(0))
        Assertions.assertEquals(8, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals("GREEN: 8", frame.seatBarLabel)
        Assertions.assertEquals(1, frame.changeBarCount.toLong())
        Assertions.assertEquals(Color.GREEN, frame.getChangeBarColor(0))
        Assertions.assertEquals(7, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(1, frame.changeBarStart.toLong())
        Assertions.assertEquals("GRN: +7", frame.changeBarLabel)
        Assertions.assertEquals("PEI", frame.header)
        Assertions.assertEquals(Color.GREEN, frame.borderColor)
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
            Riding("Whitehorse West", lib, false, yp)
        )
        val frame = HeatMapFrameBuilder.ofElectedLeading(
            3.asOneTimePublisher(),
            ridings,
            { PartyResult(it.leader, it.hasWon).asOneTimePublisher() },
            { it.prev },
            lib,
            { e, l -> "LIB: $e/$l" },
            { _, l -> l > 0 },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "YUKON".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(19))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        Assertions.assertEquals("YUKON", frame.header)
        Assertions.assertEquals(Color.RED, frame.borderColor)
        Assertions.assertEquals(2, frame.seatBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getSeatBarColor(0))
        Assertions.assertEquals(3, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        Assertions.assertEquals(8, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals("LIB: 3/11", frame.seatBarLabel)
        Assertions.assertEquals(2, frame.changeBarStart.toLong())
        Assertions.assertEquals(2, frame.changeBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getChangeBarColor(0))
        Assertions.assertEquals(2, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        Assertions.assertEquals(7, frame.getChangeBarSize(1).toLong())
        Assertions.assertEquals("+2/+9", frame.changeBarLabel)
    }

    @Test
    fun testBasicLeadingElectedFiltered() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)

        class Riding(val name: String, val leader: Party, val hasWon: Boolean, val prev: Party, val isWhitehorse: Boolean)

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
            Riding("Whitehorse West", lib, false, yp, true)
        )
        val filter = Publisher<(Riding) -> Boolean> { true }
        val frame = HeatMapFrameBuilder.ofElectedLeading(
            3.asOneTimePublisher(),
            ridings,
            { PartyResult(it.leader, it.hasWon).asOneTimePublisher() },
            { it.prev },
            lib,
            { e, l -> "LIB: $e/$l" },
            { _, l -> l > 0 },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "YUKON".asOneTimePublisher(),
            filterFunc = filter
        )

        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(19))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        Assertions.assertEquals(Color.ORANGE, frame.getSquareBorder(2))
        Assertions.assertEquals(lighten(Color.ORANGE), frame.getSquareFill(2))
        Assertions.assertEquals("YUKON", frame.header)
        Assertions.assertEquals(Color.RED, frame.borderColor)
        Assertions.assertEquals(2, frame.seatBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getSeatBarColor(0))
        Assertions.assertEquals(3, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        Assertions.assertEquals(8, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals("LIB: 3/11", frame.seatBarLabel)
        Assertions.assertEquals(2, frame.changeBarStart.toLong())
        Assertions.assertEquals(2, frame.changeBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getChangeBarColor(0))
        Assertions.assertEquals(2, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        Assertions.assertEquals(7, frame.getChangeBarSize(1).toLong())
        Assertions.assertEquals("+2/+9", frame.changeBarLabel)

        filter.submit { it.isWhitehorse }
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareBorder(0) }, IsEqual(Color.WHITE))
        Assertions.assertEquals(Color.WHITE, frame.getSquareBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assertions.assertEquals(Color.ORANGE, frame.getSquareBorder(2))
        Assertions.assertEquals(lighten(Color.ORANGE), frame.getSquareFill(2))
        Assertions.assertEquals("YUKON", frame.header)
        Assertions.assertEquals(Color.RED, frame.borderColor)
        Assertions.assertEquals(2, frame.seatBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getSeatBarColor(0))
        Assertions.assertEquals(3, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        Assertions.assertEquals(8, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals("LIB: 3/11", frame.seatBarLabel)
        Assertions.assertEquals(2, frame.changeBarStart.toLong())
        Assertions.assertEquals(2, frame.changeBarCount.toLong())
        Assertions.assertEquals(Color.RED, frame.getChangeBarColor(0))
        Assertions.assertEquals(2, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        Assertions.assertEquals(7, frame.getChangeBarSize(1).toLong())
        Assertions.assertEquals("+2/+9", frame.changeBarLabel)
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
        val frame = HeatMapFrameBuilder.ofElectedLeading(
            results.size.asOneTimePublisher(),
            results,
            { it.publisher },
            { it.prev },
            dem,
            { e, l -> "DEM: $e/$l" },
            { _, l -> l > 0 },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "TEST".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(30))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assertions.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getSeatBarSize(1).toLong())
        result.setResult(gop, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(lighten(Color.RED)))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        Assertions.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getSeatBarSize(1).toLong())
        result.setResult(dem, true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(Color.BLUE))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(Color.BLUE, frame.getSquareFill(0))
        Assertions.assertEquals(30, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getSeatBarSize(1).toLong())
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
        val frame = HeatMapFrameBuilder.ofElectedLeading(
            results.sumOf { it.numSeats }.asOneTimePublisher(),
            results,
            { it.publisher },
            { it.prev },
            dem,
            { e, l -> "DEM: $e/$l" },
            { _, _ -> true },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "TEST".asOneTimePublisher(),
            seatsFunc = { it.numSeats }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(30))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assertions.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(1).toLong())
        result.setResult(gop, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(lighten(Color.RED)))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        Assertions.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(1).toLong())
        result.setResult(dem, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(lighten(Color.BLUE)))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(30, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(30, frame.getChangeBarSize(1).toLong())
        result.setResult(dem, true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(Color.BLUE))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(30, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals(30, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(1).toLong())
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
        val frame = HeatMapFrameBuilder.ofElectedLeading(
            results.sumOf { it.numSeats }.asOneTimePublisher(),
            results,
            { it.publisher },
            { it.prev },
            dem,
            { e, l -> "DEM: $e/$l" },
            { _, _ -> true },
            { e, l -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "TEST".asOneTimePublisher(),
            seatsFunc = { it.numSeats }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(30))
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assertions.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(1).toLong())
        result.setResult(null, false)
        Assertions.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assertions.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assertions.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assertions.assertEquals(0, frame.getChangeBarSize(1).toLong())
    }
}
