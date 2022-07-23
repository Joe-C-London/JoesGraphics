package com.joecollins.graphics.components

import com.joecollins.graphics.components.MultiSummaryFrameBuilder.Companion.tooClose
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.junit.Assert
import org.junit.Test
import java.awt.Color
import java.util.concurrent.TimeUnit

class MultiSummaryFrameBuilderTest {
    @Test
    fun testTooCloseToCallBuilder() {
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN)
        val ind = Party("Independent", "IND", Color.GRAY)
        val ridings = listOf(
            Riding("Klondike"), // 0
            Riding("Kluane"), // 1
            Riding("Lake Laberge"), // 2
            Riding("Mayo-Tatchun"), // 3
            Riding("Mount Lorne-Southern Lakes"), // 4
            Riding("Pelly-Nisutlin"), // 5
            Riding("Vuntut Gwitchin"), // 6
            Riding("Watson Lake"), // 7
        )
        val frame = tooClose(
            ridings, { it.isTooClose }, { it.margin },
            { it.name.uppercase().asOneTimePublisher() }, { it.boxes },
            2
        )
            .withHeader("TOO CLOSE TO CALL".asOneTimePublisher())
            .build()
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.header }, IsNot(IsEqual("")))
        Assert.assertEquals("TOO CLOSE TO CALL", frame.header)
        Assert.assertEquals(0, frame.numRows.toLong())

        // add first (36)
        ridings[5].setResults(mapOf(yp to 140, ndp to 104, lib to 76, grn to 11))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(1))
        Assert.assertEquals("PELLY-NISUTLIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(0, 0))
        Assert.assertEquals("YP: 140", frame.getValue(0, 0))

        // add to top (3/36)
        ridings[6].setResults(mapOf(yp to 35, ndp to 2, lib to 38))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(2))
        Assert.assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Assert.assertEquals("LIB: 38", frame.getValue(0, 0))
        Assert.assertEquals("PELLY-NISUTLIN", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 140", frame.getValue(1, 0))

        // add beyond limit (3/36/40)
        ridings[7].setResults(mapOf(yp to 150, ndp to 110, lib to 106, ind to 19))
        Assert.assertEquals(2, frame.numRows.toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getRowHeader(0) }, IsEqual("VUNTUT GWITCHIN"))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getNumValues(0) }, IsNot(IsEqual(0)))
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Assert.assertEquals("LIB: 38", frame.getValue(0, 0))
        Assert.assertEquals("PELLY-NISUTLIN", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 140", frame.getValue(1, 0))

        // existing updated, sorted to bottom (3/40/72)
        ridings[5].setResults(mapOf(yp to 280, ndp to 207, lib to 152, grn to 22))
        Assert.assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        Assert.assertEquals(Color.RED, frame.getColor(0, 0))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getRowHeader(1) }, IsEqual("WATSON LAKE"))
        Assert.assertEquals("LIB: 38", frame.getValue(0, 0))
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
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getRowHeader(1) }, IsEqual("WATSON LAKE"))
        Assert.assertEquals("LIB: 77", frame.getValue(0, 0))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 150", frame.getValue(1, 0))

        // remove from in view (40)
        ridings[6].setWinner(lib)
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(1))
        Assert.assertEquals("WATSON LAKE", frame.getRowHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(0, 0))
        Assert.assertEquals("YP: 150", frame.getValue(0, 0))

        // add to top (25/40)
        ridings[1].setResults(mapOf(yp to 169, ndp to 76, lib to 144))
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numRows }, IsEqual(2))
        Assert.assertEquals("KLUANE", frame.getRowHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(0, 0))
        Assert.assertEquals("YP: 169", frame.getValue(0, 0))
        Assert.assertEquals("WATSON LAKE", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 150", frame.getValue(1, 0))

        // update in view, sorted (40/49)
        ridings[1].setResults(mapOf(yp to 338, ndp to 153, lib to 289))
        Assert.assertEquals(2, frame.numRows.toLong())
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.getRowHeader(0) }, IsEqual("WATSON LAKE"))
        Assert.assertEquals(Color.BLUE, frame.getColor(0, 0))
        Assert.assertEquals("YP: 150", frame.getValue(0, 0))
        Assert.assertEquals("KLUANE", frame.getRowHeader(1))
        Assert.assertEquals(Color.BLUE, frame.getColor(1, 0))
        Assert.assertEquals("YP: 338", frame.getValue(1, 0))
    }

    private class Riding(val name: String) {
        private var results: Map<Party, Int> = HashMap()
        private var winner: Party? = null

        fun setResults(results: Map<Party, Int>) {
            this.results = results
            updateTooClose()
            updateMargin()
            updateBoxes()
        }

        fun setWinner(winner: Party?) {
            this.winner = winner
            updateTooClose()
        }

        val isTooClose = Publisher(calculateTooClose())
        private fun updateTooClose() = isTooClose.submit(calculateTooClose())
        private fun calculateTooClose() = winner == null && results.values.sum() > 0

        val margin = Publisher(calculateMargin())
        private fun updateMargin() = margin.submit(calculateMargin())
        private fun calculateMargin(): Int {
            val topTwoVotes = results.values.sortedDescending()
            return if (topTwoVotes.isEmpty())
                0
            else
                topTwoVotes[0] - topTwoVotes[1]
        }

        val boxes = Publisher(calculateBoxes())
        private fun updateBoxes() = boxes.submit(calculateBoxes())
        private fun calculateBoxes() =
            results.entries.asSequence()
                .sortedByDescending { it.value }
                .map {
                    Pair(
                        it.key.color,
                        it.key.abbreviation + ": " + it.value
                    )
                }
                .toList()
    }
}
