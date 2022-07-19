package com.joecollins.graphics.components

import com.joecollins.graphics.components.HeatMapFrameBuilder.Companion.of
import com.joecollins.graphics.components.HeatMapFrameBuilder.Companion.ofElectedLeading
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.TimeUnit

class HeatMapFrameBuilderTest {
    @Test
    fun testHeatMapBasic() {
        val dots: MutableList<Pair<Color, Color>> = ArrayList()
        dots.addAll(Collections.nCopies(1, Pair(Color.GREEN, Color.GREEN)))
        dots.addAll(Collections.nCopies(7, Pair(Color.GREEN, Color.RED)))
        dots.addAll(Collections.nCopies(6, Pair(Color.RED, Color.RED)))
        dots.addAll(Collections.nCopies(5, Pair(Color.BLUE, Color.RED)))
        dots.addAll(Collections.nCopies(8, Pair(Color.BLUE, Color.BLUE)))
        val seatBars = Publisher(listOf(Pair(Color.GREEN, 8)))
        val changeBars = Publisher(listOf(Pair(Color.GREEN, +7)))
        val frame = of(
            3.asOneTimePublisher(),
            dots,
            { it.first.asOneTimePublisher() },
            { it.second.asOneTimePublisher() },
            { null.asOneTimePublisher() }
        )
            .withSeatBars(seatBars, { it.first }, { it.second }, "GREEN: 8".asOneTimePublisher())
            .withChangeBars(
                changeBars, { it.first }, { it.second },
                1.asOneTimePublisher(),
                "GRN: +7".asOneTimePublisher()
            )
            .withHeader("PEI".asOneTimePublisher())
            .withBorder(Color.GREEN.asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(3))
        Assert.assertEquals(27, frame.numSquares.toLong())
        val expectedFills = sequenceOf(
            Collections.nCopies(8, Color.GREEN),
            Collections.nCopies(6, Color.RED),
            Collections.nCopies(13, Color.BLUE)
        )
            .flatten()
            .toList()
        val expectedBorders = sequenceOf(
            Collections.nCopies(1, Color.GREEN),
            Collections.nCopies(18, Color.RED),
            Collections.nCopies(8, Color.BLUE)
        )
            .flatten()
            .toList()
        for (i in 0 until frame.numSquares) {
            Assert.assertEquals("Square fill $i", expectedFills[i], frame.getSquareFill(i))
            Assert.assertEquals("Square border $i", expectedBorders[i], frame.getSquareBorder(i))
        }
        Assert.assertEquals(1, frame.seatBarCount.toLong())
        Assert.assertEquals(Color.GREEN, frame.getSeatBarColor(0))
        Assert.assertEquals(8, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals("GREEN: 8", frame.seatBarLabel)
        Assert.assertEquals(1, frame.changeBarCount.toLong())
        Assert.assertEquals(Color.GREEN, frame.getChangeBarColor(0))
        Assert.assertEquals(7, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(1, frame.changeBarStart.toLong())
        Assert.assertEquals("GRN: +7", frame.changeBarLabel)
        Assert.assertEquals("PEI", frame.header)
        Assert.assertEquals(Color.GREEN, frame.borderColor)
    }

    @Test
    fun testBasicLeadingElected() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)

        class Riding(val name: String, val leader: Party, val hasWon: Boolean, val prev: Party)

        val ridings: MutableList<Riding> = ArrayList()
        ridings.add(Riding("Vuntut Gwitchin", lib, false, lib))
        ridings.add(Riding("Klondike", lib, true, lib))
        ridings.add(Riding("Takhini-Copper King", ndp, false, ndp))
        ridings.add(Riding("Whitehorse Centre", ndp, false, ndp))
        ridings.add(Riding("Mayo-Tatchun", lib, true, ndp))
        ridings.add(Riding("Mount Lorne-Southern Lakes", lib, false, ndp))
        ridings.add(Riding("Riverdale South", lib, false, ndp))
        ridings.add(Riding("Copperbelt South", yp, false, ndp))
        ridings.add(Riding("Porter Creek South", lib, false, yp))
        ridings.add(Riding("Watson Lake", yp, true, yp))
        ridings.add(Riding("Porter Creek Centre", lib, false, yp))
        ridings.add(Riding("Riverdale North", lib, true, yp))
        ridings.add(Riding("Kluane", yp, false, yp))
        ridings.add(Riding("Mountainview", lib, false, yp))
        ridings.add(Riding("Copperbelt North", lib, false, yp))
        ridings.add(Riding("Pelly-Nisutlin", yp, true, yp))
        ridings.add(Riding("Porter Creek North", yp, false, yp))
        ridings.add(Riding("Lake Laberge", yp, true, yp))
        ridings.add(Riding("Whitehorse West", lib, false, yp))
        val frame = ofElectedLeading(
            3.asOneTimePublisher(),
            ridings,
            { PartyResult(it.leader, it.hasWon).asOneTimePublisher() },
            { it.prev },
            lib,
            { e: Int, l: Int -> "LIB: $e/$l" },
            { _: Int, l: Int -> l > 0 },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "YUKON".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(19))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        Assert.assertEquals("YUKON", frame.header)
        Assert.assertEquals(Color.RED, frame.borderColor)
        Assert.assertEquals(2, frame.seatBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getSeatBarColor(0))
        Assert.assertEquals(3, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        Assert.assertEquals(8, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals("LIB: 3/11", frame.seatBarLabel)
        Assert.assertEquals(2, frame.changeBarStart.toLong())
        Assert.assertEquals(2, frame.changeBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getChangeBarColor(0))
        Assert.assertEquals(2, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        Assert.assertEquals(7, frame.getChangeBarSize(1).toLong())
        Assert.assertEquals("+2/+9", frame.changeBarLabel)
    }

    @Test
    fun testBasicLeadingElectedFiltered() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)

        class Riding(val name: String, val leader: Party, val hasWon: Boolean, val prev: Party, val isWhitehorse: Boolean)

        val ridings: MutableList<Riding> = ArrayList()
        ridings.add(Riding("Vuntut Gwitchin", lib, false, lib, false))
        ridings.add(Riding("Klondike", lib, true, lib, false))
        ridings.add(Riding("Takhini-Copper King", ndp, false, ndp, true))
        ridings.add(Riding("Whitehorse Centre", ndp, false, ndp, true))
        ridings.add(Riding("Mayo-Tatchun", lib, true, ndp, false))
        ridings.add(Riding("Mount Lorne-Southern Lakes", lib, false, ndp, false))
        ridings.add(Riding("Riverdale South", lib, false, ndp, true))
        ridings.add(Riding("Copperbelt South", yp, false, ndp, true))
        ridings.add(Riding("Porter Creek South", lib, false, yp, true))
        ridings.add(Riding("Watson Lake", yp, true, yp, true))
        ridings.add(Riding("Porter Creek Centre", lib, false, yp, true))
        ridings.add(Riding("Riverdale North", lib, true, yp, true))
        ridings.add(Riding("Kluane", yp, false, yp, false))
        ridings.add(Riding("Mountainview", lib, false, yp, false))
        ridings.add(Riding("Copperbelt North", lib, false, yp, true))
        ridings.add(Riding("Pelly-Nisutlin", yp, true, yp, false))
        ridings.add(Riding("Porter Creek North", yp, false, yp, true))
        ridings.add(Riding("Lake Laberge", yp, true, yp, false))
        ridings.add(Riding("Whitehorse West", lib, false, yp, true))
        val filter = Publisher<(Riding) -> Boolean> { true }
        val frame = ofElectedLeading(
            3.asOneTimePublisher(),
            ridings,
            { PartyResult(it.leader, it.hasWon).asOneTimePublisher() },
            { it.prev },
            lib,
            { e: Int, l: Int -> "LIB: $e/$l" },
            { _: Int, l: Int -> l > 0 },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "YUKON".asOneTimePublisher(),
            filterFunc = filter
        )

        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(19))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        Assert.assertEquals(Color.ORANGE, frame.getSquareBorder(2))
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSquareFill(2))
        Assert.assertEquals("YUKON", frame.header)
        Assert.assertEquals(Color.RED, frame.borderColor)
        Assert.assertEquals(2, frame.seatBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getSeatBarColor(0))
        Assert.assertEquals(3, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        Assert.assertEquals(8, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals("LIB: 3/11", frame.seatBarLabel)
        Assert.assertEquals(2, frame.changeBarStart.toLong())
        Assert.assertEquals(2, frame.changeBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getChangeBarColor(0))
        Assert.assertEquals(2, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        Assert.assertEquals(7, frame.getChangeBarSize(1).toLong())
        Assert.assertEquals("+2/+9", frame.changeBarLabel)

        filter.submit { it.isWhitehorse }
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareBorder(0) }, IsEqual(Color.WHITE))
        Assert.assertEquals(Color.WHITE, frame.getSquareBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assert.assertEquals(Color.ORANGE, frame.getSquareBorder(2))
        Assert.assertEquals(lighten(Color.ORANGE), frame.getSquareFill(2))
        Assert.assertEquals("YUKON", frame.header)
        Assert.assertEquals(Color.RED, frame.borderColor)
        Assert.assertEquals(2, frame.seatBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getSeatBarColor(0))
        Assert.assertEquals(3, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getSeatBarColor(1))
        Assert.assertEquals(8, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals("LIB: 3/11", frame.seatBarLabel)
        Assert.assertEquals(2, frame.changeBarStart.toLong())
        Assert.assertEquals(2, frame.changeBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getChangeBarColor(0))
        Assert.assertEquals(2, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getChangeBarColor(1))
        Assert.assertEquals(7, frame.getChangeBarSize(1).toLong())
        Assert.assertEquals("+2/+9", frame.changeBarLabel)
    }

    @Test
    fun testLeadingElectedSeatsRepeated() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party) {
            val publisher = Publisher(if (leader == null) null else PartyResult(leader, hasWon))

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                publisher.submit(if (leader == null) null else PartyResult(leader, hasWon))
            }
        }

        val result = Result(null, false, gop)
        val results = Collections.nCopies(30, result)
        val frame = ofElectedLeading(
            results.size.asOneTimePublisher(),
            results,
            { it.publisher },
            { it.prev },
            dem,
            { e: Int, l: Int -> "DEM: $e/$l" },
            { _: Int, l: Int -> l > 0 },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "TEST".asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(30))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assert.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getSeatBarSize(1).toLong())
        result.setResult(gop, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(lighten(Color.RED)))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        Assert.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getSeatBarSize(1).toLong())
        result.setResult(dem, true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(Color.BLUE, frame.getSquareFill(0))
        Assert.assertEquals(30, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getSeatBarSize(1).toLong())
    }

    @Test
    fun testLeadingElectedMultiseat() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party, val numSeats: Int) {
            val publisher = Publisher(if (leader == null) null else PartyResult(leader, hasWon))

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                publisher.submit(if (leader == null) null else PartyResult(leader, hasWon))
            }
        }

        val result = Result(null, false, gop, 30)
        val results = listOf(result)
        val frame = ofElectedLeading(
            results.sumOf { it.numSeats }.asOneTimePublisher(),
            results,
            { it.publisher },
            { it.prev },
            dem,
            { e: Int, l: Int -> "DEM: $e/$l" },
            { _: Int, _: Int -> true },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "TEST".asOneTimePublisher(),
            seatsFunc = { it.numSeats }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(30))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assert.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(1).toLong())
        result.setResult(gop, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(lighten(Color.RED)))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(lighten(Color.RED), frame.getSquareFill(0))
        Assert.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(1).toLong())
        result.setResult(dem, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(lighten(Color.BLUE)))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(30, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(30, frame.getChangeBarSize(1).toLong())
        result.setResult(dem, true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getSquareFill(0) }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(30, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals(30, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(1).toLong())
    }

    @Test
    fun testBarsUpdateProperlyOnIncompleteResults() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party, val numSeats: Int) {
            val publisher = Publisher(PartyResult(leader, hasWon))

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                publisher.submit(PartyResult(leader, hasWon))
            }
        }

        val result = Result(null, false, gop, 30)
        val results = listOf(result)
        val frame = ofElectedLeading(
            results.sumOf { it.numSeats }.asOneTimePublisher(),
            results,
            { it.publisher },
            { it.prev },
            dem,
            { e: Int, l: Int -> "DEM: $e/$l" },
            { _: Int, _: Int -> true },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            "TEST".asOneTimePublisher(),
            seatsFunc = { it.numSeats }
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numSquares }, IsEqual(30))
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assert.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(1).toLong())
        result.setResult(null, false)
        Assert.assertEquals(Color.RED, frame.getSquareBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getSquareFill(0))
        Assert.assertEquals(0, frame.getSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getChangeBarSize(1).toLong())
    }
}
