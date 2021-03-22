package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.propertyBinding
import com.joecollins.graphics.components.MultiSummaryFrameBuilder.Companion.tooClose
import com.joecollins.models.general.Party
import java.awt.Color
import java.util.ArrayList
import java.util.HashMap
import org.junit.Assert
import org.junit.Test

class MultiSummaryFrameBuilderTest {
    @Test
    fun testTooCloseToCallBuilder() {
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN)
        val ind = Party("Independent", "IND", Color.GRAY)
        val ridings: MutableList<Riding> = ArrayList()
        ridings.add(Riding("Klondike")) // 0
        ridings.add(Riding("Kluane")) // 1
        ridings.add(Riding("Lake Laberge")) // 2
        ridings.add(Riding("Mayo-Tatchun")) // 3
        ridings.add(Riding("Mount Lorne-Southern Lakes")) // 4
        ridings.add(Riding("Pelly-Nisutlin")) // 5
        ridings.add(Riding("Vuntut Gwitchin")) // 6
        ridings.add(Riding("Watson Lake")) // 7
        val frame = tooClose(
                ridings, { it.isTooClose }, { it.margin },
                { fixedBinding(it.name.toUpperCase()) }, { it.boxes },
                2)
                .withHeader(fixedBinding("TOO CLOSE TO CALL"))
                .build()
        Assert.assertEquals("TOO CLOSE TO CALL", frame.header)
        Assert.assertEquals(0, frame.numRows.toLong())

        // add first (36)
        ridings[5].setResults(mapOf(yp to 140, ndp to 104, lib to 76, grn to 11))
        Assert.assertEquals(1, frame.numRows.toLong())
        Assert.assertEquals("PELLY-NISUTLIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(0, 0))
        Assert.assertEquals("YP: 140", frame.getValue(0, 0))

        // add to top (3/36)
        ridings[6].setResults(mapOf(yp to 35, ndp to 2, lib to 38))
        Assert.assertEquals(2, frame.numRows.toLong())
        Assert.assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Assert.assertEquals("LIB: 38", frame.getValue(0, 0))
        Assert.assertEquals("PELLY-NISUTLIN", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 140", frame.getValue(1, 0))

        // add beyond limit (3/36/40)
        ridings[7].setResults(mapOf(yp to 150, ndp to 110, lib to 106, ind to 19))
        Assert.assertEquals(2, frame.numRows.toLong())
        Assert.assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Assert.assertEquals("LIB: 38", frame.getValue(0, 0))
        Assert.assertEquals("PELLY-NISUTLIN", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 140", frame.getValue(1, 0))

        // existing updated, sorted to bottom (3/40/72)
        ridings[5].setResults(mapOf(yp to 280, ndp to 207, lib to 152, grn to 22))
        Assert.assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Assert.assertEquals("LIB: 38", frame.getValue(0, 0))
        Assert.assertEquals("WATSON LAKE", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 150", frame.getValue(1, 0))

        // bottom (out of view) removed (3/40)
        ridings[5].setWinner(yp)
        Assert.assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Assert.assertEquals("LIB: 38", frame.getValue(0, 0))
        Assert.assertEquals("WATSON LAKE", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 150", frame.getValue(1, 0))

        // update in view (7/40)
        ridings[6].setResults(mapOf(yp to 70, ndp to 3, lib to 77))
        Assert.assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Assert.assertEquals("LIB: 77", frame.getValue(0, 0))
        Assert.assertEquals("WATSON LAKE", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 150", frame.getValue(1, 0))

        // remove from in view (40)
        ridings[6].setWinner(lib)
        Assert.assertEquals(1, frame.numRows.toLong())
        Assert.assertEquals("WATSON LAKE", frame.getRowHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(0, 0))
        Assert.assertEquals("YP: 150", frame.getValue(0, 0))

        // add to top (25/40)
        ridings[1].setResults(mapOf(yp to 169, ndp to 76, lib to 144))
        Assert.assertEquals(2, frame.numRows.toLong())
        Assert.assertEquals("KLUANE", frame.getRowHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(0, 0))
        Assert.assertEquals("YP: 169", frame.getValue(0, 0))
        Assert.assertEquals("WATSON LAKE", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 150", frame.getValue(1, 0))

        // update in view, sorted (40/49)
        ridings[1].setResults(mapOf(yp to 338, ndp to 153, lib to 289))
        Assert.assertEquals(2, frame.numRows.toLong())
        Assert.assertEquals("WATSON LAKE", frame.getRowHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(0, 0))
        Assert.assertEquals("YP: 150", frame.getValue(0, 0))
        Assert.assertEquals("KLUANE", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 338", frame.getValue(1, 0))
    }

    private class Riding(val name: String) : Bindable<Riding, Riding.Property>() {
        enum class Property {
            RESULT, WINNER
        }

        private var results: Map<Party, Int> = HashMap()
        private var winner: Party? = null

        fun setResults(results: Map<Party, Int>) {
            this.results = results
            onPropertyRefreshed(Property.RESULT)
        }

        fun setWinner(winner: Party?) {
            this.winner = winner
            onPropertyRefreshed(Property.WINNER)
        }

        val isTooClose: Binding<Boolean>
            get() = propertyBinding(
                    this,
                    { it.winner == null && it.results.values.sum() > 0 },
                    Property.RESULT,
                    Property.WINNER)

        val margin: Binding<Int>
            get() = propertyBinding(
                    this,
                    {
                        val topTwoVotes = results.values.sortedDescending()
                        if (topTwoVotes.isEmpty())
                            0
                        else
                            topTwoVotes[0] - topTwoVotes[1]
                    },
                    Property.RESULT)

        val boxes: Binding<List<kotlin.Pair<Color, String>>>
            get() = propertyBinding(
                    this,
                    { me ->
                        me.results.entries.asSequence()
                                .sortedByDescending { it.value }
                                .map {
                                    Pair(
                                            it.key.color,
                                            it.key.abbreviation + ": " + it.value)
                                }
                                .toList()
                    },
                    Property.RESULT)
    }
}
