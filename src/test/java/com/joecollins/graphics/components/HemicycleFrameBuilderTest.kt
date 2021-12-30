package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.HemicycleFrameBuilder.Companion.of
import com.joecollins.graphics.components.HemicycleFrameBuilder.Companion.ofElectedLeading
import com.joecollins.graphics.components.HemicycleFrameBuilder.Tiebreaker
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.ColorUtils.lighten
import com.joecollins.models.general.Party
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.Collections
import java.util.concurrent.TimeUnit

@Suppress("UNUSED_ANONYMOUS_PARAMETER")
class HemicycleFrameBuilderTest {
    @Test
    fun testHemicycleBasic() {
        val rows = listOf(7, 9, 11)
        val dots: MutableList<Pair<Color, Color>> = ArrayList()
        dots.addAll(Collections.nCopies(1, Pair(Color.GREEN, Color.GREEN)))
        dots.addAll(Collections.nCopies(7, Pair(Color.GREEN, Color.RED)))
        dots.addAll(Collections.nCopies(6, Pair(Color.RED, Color.RED)))
        dots.addAll(Collections.nCopies(5, Pair(Color.BLUE, Color.RED)))
        dots.addAll(Collections.nCopies(8, Pair(Color.BLUE, Color.BLUE)))
        val leftSeatBars = BindableWrapper(listOf(Pair(Color.GREEN, 8)))
        val rightSeatBars = BindableWrapper(listOf(Pair(Color.BLUE, 13)))
        val middleSeatBars = BindableWrapper(listOf(Pair(Color.RED, 6)))
        val leftChangeBars = BindableWrapper(listOf(Pair(Color.GREEN, +7)))
        val rightChangeBars = BindableWrapper(listOf(Pair(Color.BLUE, +5)))
        val frame = of(
            rows,
            dots,
            { fixedBinding(it.first) },
            { fixedBinding(it.second) },
            Tiebreaker.FRONT_ROW_FROM_RIGHT
        )
            .withLeftSeatBars(leftSeatBars.binding, { it.first }, { it.second }, fixedBinding("GREEN: 8"))
            .withRightSeatBars(rightSeatBars.binding, { it.first }, { it.second }, fixedBinding("PROGRESSIVE CONSERVATIVE: 13"))
            .withMiddleSeatBars(middleSeatBars.binding, { it.first }, { it.second }, fixedBinding("LIBERAL: 6"))
            .withLeftChangeBars(leftChangeBars.binding, { it.first }, { it.second }, fixedBinding(1), fixedBinding("GRN: +7"))
            .withRightChangeBars(rightChangeBars.binding, { it.first }, { it.second }, fixedBinding(8), fixedBinding("PC: +5"))
            .withHeader(fixedBinding("PEI"))
            .build()
        Assert.assertEquals(3, frame.numRows.toLong())
        Assert.assertEquals(7, frame.getRowCount(0).toLong())
        Assert.assertEquals(9, frame.getRowCount(1).toLong())
        Assert.assertEquals(11, frame.getRowCount(2).toLong())
        Assert.assertEquals(27, frame.numDots.toLong())
        val expectedDots = listOf(
            Color.GREEN, Color.GREEN, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.GREEN, Color.GREEN, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE, Color.BLUE
        )
        val expectedBorders = listOf(
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, //
            Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE, //
            Color.GREEN, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.RED, Color.BLUE, Color.BLUE, Color.BLUE
        )
        for (i in 0 until frame.numDots) {
            Assert.assertEquals("Dot color $i", expectedDots[i], frame.getDotColor(i))
            Assert.assertEquals("Dot border $i", expectedBorders[i], frame.getDotBorder(i))
        }
        Assert.assertEquals(1, frame.leftSeatBarCount.toLong())
        Assert.assertEquals(Color.GREEN, frame.getLeftSeatBarColor(0))
        Assert.assertEquals(8, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals("GREEN: 8", frame.getLeftSeatBarLabel())
        Assert.assertEquals(1, frame.rightSeatBarCount.toLong())
        Assert.assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        Assert.assertEquals(13, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals("PROGRESSIVE CONSERVATIVE: 13", frame.getRightSeatBarLabel())
        Assert.assertEquals(1, frame.middleSeatBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getMiddleSeatBarColor(0))
        Assert.assertEquals(6, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals("LIBERAL: 6", frame.getMiddleSeatBarLabel())
        Assert.assertEquals(1, frame.leftChangeBarCount.toLong())
        Assert.assertEquals(Color.GREEN, frame.getLeftChangeBarColor(0))
        Assert.assertEquals(7, frame.getLeftChangeBarSize(0).toLong())
        Assert.assertEquals(1, frame.getLeftChangeBarStart().toLong())
        Assert.assertEquals("GRN: +7", frame.getLeftChangeBarLabel())
        Assert.assertEquals(1, frame.rightChangeBarCount.toLong())
        Assert.assertEquals(Color.BLUE, frame.getRightChangeBarColor(0))
        Assert.assertEquals(5, frame.getRightChangeBarSize(0).toLong())
        Assert.assertEquals(8, frame.getRightChangeBarStart().toLong())
        Assert.assertEquals("PC: +5", frame.getRightChangeBarLabel())
        Assert.assertEquals("PEI", frame.header)
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
            listOf(ridings.size),
            ridings,
            { fixedBinding(HemicycleFrameBuilder.Result(it.leader, it.hasWon)) },
            { it.prev },
            lib,
            yp,
            { e: Int, l: Int -> "LIB: $e/$l" },
            { e: Int, l: Int -> "YP: $e/$l" },
            { e: Int, l: Int -> "OTH: $e/$l" },
            { e: Int, l: Int -> l > 0 },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            fixedBinding("YUKON")
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(19))
        Assert.assertEquals(19, frame.numDots.toLong())
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(lighten(Color.RED), frame.getDotColor(0))
        Assert.assertEquals("YUKON", frame.header)
        Assert.assertEquals(2, frame.leftSeatBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getLeftSeatBarColor(0))
        Assert.assertEquals(3, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getLeftSeatBarColor(1))
        Assert.assertEquals(8, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals("LIB: 3/11", frame.getLeftSeatBarLabel())
        Assert.assertEquals(2, frame.rightSeatBarCount.toLong())
        Assert.assertEquals(Color.BLUE, frame.getRightSeatBarColor(0))
        Assert.assertEquals(3, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.BLUE), frame.getRightSeatBarColor(1))
        Assert.assertEquals(3, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals("YP: 3/6", frame.getRightSeatBarLabel())
        Assert.assertEquals(2, frame.middleSeatBarCount.toLong())
        Assert.assertEquals(Color.GRAY, frame.getMiddleSeatBarColor(0))
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.GRAY), frame.getMiddleSeatBarColor(1))
        Assert.assertEquals(2, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals("OTH: 0/2", frame.getMiddleSeatBarLabel())
        Assert.assertEquals(2, frame.getLeftChangeBarStart().toLong())
        Assert.assertEquals(2, frame.leftChangeBarCount.toLong())
        Assert.assertEquals(Color.RED, frame.getLeftChangeBarColor(0))
        Assert.assertEquals(2, frame.getLeftChangeBarSize(0).toLong())
        Assert.assertEquals(lighten(Color.RED), frame.getLeftChangeBarColor(1))
        Assert.assertEquals(7, frame.getLeftChangeBarSize(1).toLong())
        Assert.assertEquals("+2/+9", frame.getLeftChangeBarLabel())
        Assert.assertEquals(11, frame.getRightChangeBarStart().toLong())
        Assert.assertEquals(0, frame.rightChangeBarCount.toLong())
        Assert.assertEquals("", frame.getRightChangeBarLabel())
    }

    internal enum class Property {
        PROP
    }

    @Test
    fun testLeadingElectedSeatsRepeated() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party) : Bindable<Result, Property>() {
            val binding: Binding<HemicycleFrameBuilder.Result?>
                get() = Binding.propertyBinding(
                    this,
                    {
                        if (leader == null) null
                        else HemicycleFrameBuilder.Result(leader, hasWon)
                    },
                    Property.PROP
                )

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                onPropertyRefreshed(Property.PROP)
            }
        }

        val result = Result(null, false, gop)
        val results = Collections.nCopies(30, result)
        val frame = ofElectedLeading(
            listOf(results.size),
            results,
            { it.binding },
            { it.prev },
            dem,
            gop,
            { e: Int, l: Int -> "DEM: $e/$l" },
            { e: Int, l: Int -> "GOP: $e/$l" },
            { e: Int, l: Int -> "OTH: $e/$l" },
            { e: Int, l: Int -> l > 0 },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            fixedBinding("TEST")
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(30))
        Assert.assertEquals(30, frame.numDots.toLong())
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getDotColor(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftSeatBarCount }, IsEqual(2))
        Assert.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.rightSeatBarCount }, IsEqual(2))
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.middleSeatBarCount }, IsEqual(2))
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        result.setResult(gop, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(lighten(Color.RED)))
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(lighten(Color.RED), frame.getDotColor(0))
        Assert.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(30, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        result.setResult(dem, true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.BLUE, frame.getDotColor(0))
        Assert.assertEquals(30, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
    }

    @Test
    fun testLeadingElectedMultiseat() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party, val numSeats: Int) : Bindable<Result, Property>() {
            val binding: Binding<HemicycleFrameBuilder.Result?>
                get() = Binding.propertyBinding(
                    this,
                    {
                        if (leader == null) return@propertyBinding null
                        HemicycleFrameBuilder.Result(leader, hasWon)
                    },
                    Property.PROP
                )

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                onPropertyRefreshed(Property.PROP)
            }
        }

        val result = Result(null, false, gop, 30)
        val results = listOf(result)
        val frame = ofElectedLeading(
            listOf(results.map { it.numSeats }.sum()),
            results,
            { it.numSeats },
            { it.binding },
            { it.prev },
            dem,
            gop,
            { e: Int, l: Int -> "DEM: $e/$l" },
            { e: Int, l: Int -> "GOP: $e/$l" },
            { e: Int, l: Int -> "OTH: $e/$l" },
            { e: Int, l: Int -> true },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            fixedBinding("TEST")
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(30))
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getDotColor(0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftSeatBarCount }, IsEqual(2))
        Assert.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.rightSeatBarCount }, IsEqual(2))
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.middleSeatBarCount }, IsEqual(2))
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.leftChangeBarCount }, IsEqual(2))
        Assert.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.rightChangeBarCount }, IsEqual(2))
        Assert.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
        result.setResult(gop, false)
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(lighten(Color.RED), frame.getDotColor(0))
        Assert.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(30, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
        result.setResult(dem, false)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(lighten(Color.BLUE)))
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(lighten(Color.BLUE), frame.getDotColor(0))
        Assert.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(30, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assert.assertEquals(30, frame.getLeftChangeBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assert.assertEquals(-30, frame.getRightChangeBarSize(1).toLong())
        result.setResult(dem, true)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getDotColor(0) }, IsEqual(Color.BLUE))
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.BLUE, frame.getDotColor(0))
        Assert.assertEquals(30, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals(30, frame.getLeftChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Assert.assertEquals(-30, frame.getRightChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
    }

    @Test
    fun testBarsUpdateProperlyOnIncompleteResults() {
        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)

        class Result(var leader: Party?, var hasWon: Boolean, val prev: Party, val numSeats: Int) : Bindable<Result, Property>() {
            val binding: Binding<HemicycleFrameBuilder.Result?>
                get() = Binding.propertyBinding(
                    this, { HemicycleFrameBuilder.Result(leader, hasWon) }, Property.PROP
                )

            fun setResult(leader: Party?, hasWon: Boolean) {
                this.leader = leader
                this.hasWon = hasWon
                onPropertyRefreshed(Property.PROP)
            }
        }

        val result = Result(null, false, gop, 30)
        val results = listOf(result)
        val frame = ofElectedLeading(
            listOf(results.map { it.numSeats }.sum()),
            results,
            { it.numSeats },
            { it.binding },
            { it.prev },
            dem,
            gop,
            { e: Int, l: Int -> "DEM: $e/$l" },
            { e: Int, l: Int -> "GOP: $e/$l" },
            { e: Int, l: Int -> "OTH: $e/$l" },
            { e: Int, l: Int -> true },
            { e: Int, l: Int -> DecimalFormat("+0;-0").format(e) + "/" + DecimalFormat("+0;-0").format(l) },
            Tiebreaker.FRONT_ROW_FROM_LEFT,
            fixedBinding("TEST")
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numDots }, IsEqual(30))
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getDotColor(0))
        Assert.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
        result.setResult(null, false)
        Assert.assertEquals(Color.RED, frame.getDotBorder(0))
        Assert.assertEquals(Color.WHITE, frame.getDotColor(0))
        Assert.assertEquals(0, frame.getLeftSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(0).toLong())
        Assert.assertEquals(0, frame.getMiddleSeatBarSize(1).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getLeftChangeBarSize(1).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(0).toLong())
        Assert.assertEquals(0, frame.getRightChangeBarSize(1).toLong())
    }
}
