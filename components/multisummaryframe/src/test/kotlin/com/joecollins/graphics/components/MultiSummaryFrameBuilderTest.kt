package com.joecollins.graphics.components

import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color

class MultiSummaryFrameBuilderTest {
    @Test
    fun testTooCloseToCallBuilder() {
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN)
        val ind = Party("Independent", "IND", Color.GRAY)
        val ridings = listOf(
            Riding("Klondike"),
            Riding("Kluane"),
            Riding("Lake Laberge"),
            Riding("Mayo-Tatchun"),
            Riding("Mount Lorne-Southern Lakes"),
            Riding("Pelly-Nisutlin"),
            Riding("Vuntut Gwitchin"),
            Riding("Watson Lake"),
        )
        val frame = MultiSummaryFrameBuilder.dynamicallyFiltered(
            items = ridings,
            display = { isTooClose },
            orderBy = { margin },
            rowHeader = { name.uppercase().asOneTimePublisher() },
            rowLabels = { boxes },
            limit = 2,
            header = "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
        assertEquals("TOO CLOSE TO CALL", frame.header)
        assertEquals(0, frame.numRows.toLong())

        // add first (36)
        ridings[5].setResults(mapOf(yp to 140, ndp to 104, lib to 76, grn to 11))
        assertEquals(1, frame.numRows)
        assertEquals("PELLY-NISUTLIN", frame.getRowHeader(0))
        assertEquals(Color.BLUE, frame.getColor(0, 0))
        assertEquals("YP: 140", frame.getValue(0, 0))

        // add to top (3/36)
        ridings[6].setResults(mapOf(yp to 35, ndp to 2, lib to 38))
        assertEquals(2, frame.numRows)
        assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        assertEquals(Color.RED, frame.getColor(0, 0))
        assertEquals("LIB: 38", frame.getValue(0, 0))
        assertEquals("PELLY-NISUTLIN", frame.getRowHeader(1))
        assertEquals(Color.BLUE, frame.getColor(1, 0))
        assertEquals("YP: 140", frame.getValue(1, 0))

        // add beyond limit (3/36/40)
        ridings[7].setResults(mapOf(yp to 150, ndp to 110, lib to 106, ind to 19))
        assertEquals(2, frame.numRows.toLong())
        assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        assertEquals(Color.RED, frame.getColor(0, 0))
        assertEquals("LIB: 38", frame.getValue(0, 0))
        assertEquals("PELLY-NISUTLIN", frame.getRowHeader(1))
        assertEquals(Color.BLUE, frame.getColor(1, 0))
        assertEquals("YP: 140", frame.getValue(1, 0))

        // existing updated, sorted to bottom (3/40/72)
        ridings[5].setResults(mapOf(yp to 280, ndp to 207, lib to 152, grn to 22))
        assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        assertEquals(Color.RED, frame.getColor(0, 0))
        assertEquals("WATSON LAKE", frame.getRowHeader(1))
        assertEquals("LIB: 38", frame.getValue(0, 0))
        assertEquals(Color.BLUE, frame.getColor(1, 0))
        assertEquals("YP: 150", frame.getValue(1, 0))

        // bottom (out of view) removed (3/40)
        ridings[5].setWinner(yp)
        assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        assertEquals(Color.RED, frame.getColor(0, 0))
        assertEquals("LIB: 38", frame.getValue(0, 0))
        assertEquals("WATSON LAKE", frame.getRowHeader(1))
        assertEquals(Color.BLUE, frame.getColor(1, 0))
        assertEquals("YP: 150", frame.getValue(1, 0))

        // update in view (7/40)
        ridings[6].setResults(mapOf(yp to 70, ndp to 3, lib to 77))
        assertEquals("VUNTUT GWITCHIN", frame.getRowHeader(0))
        assertEquals(Color.RED, frame.getColor(0, 0))
        assertEquals("WATSON LAKE", frame.getRowHeader(1))
        assertEquals("LIB: 77", frame.getValue(0, 0))
        assertEquals(Color.BLUE, frame.getColor(1, 0))
        assertEquals("YP: 150", frame.getValue(1, 0))

        // remove from in view (40)
        ridings[6].setWinner(lib)
        assertEquals(1, frame.numRows)
        assertEquals("WATSON LAKE", frame.getRowHeader(0))
        assertEquals(Color.BLUE, frame.getColor(0, 0))
        assertEquals("YP: 150", frame.getValue(0, 0))

        // add to top (25/40)
        ridings[1].setResults(mapOf(yp to 169, ndp to 76, lib to 144))
        assertEquals(2, frame.numRows)
        assertEquals("KLUANE", frame.getRowHeader(0))
        assertEquals(Color.BLUE, frame.getColor(0, 0))
        assertEquals("YP: 169", frame.getValue(0, 0))
        assertEquals("WATSON LAKE", frame.getRowHeader(1))
        assertEquals(Color.BLUE, frame.getColor(1, 0))
        assertEquals("YP: 150", frame.getValue(1, 0))

        // update in view, sorted (40/49)
        ridings[1].setResults(mapOf(yp to 338, ndp to 153, lib to 289))
        assertEquals(2, frame.numRows.toLong())
        assertEquals("WATSON LAKE", frame.getRowHeader(0))
        assertEquals(Color.BLUE, frame.getColor(0, 0))
        assertEquals("YP: 150", frame.getValue(0, 0))
        assertEquals("KLUANE", frame.getRowHeader(1))
        assertEquals(Color.BLUE, frame.getColor(1, 0))
        assertEquals("YP: 338", frame.getValue(1, 0))
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
            return if (topTwoVotes.isEmpty()) {
                0
            } else {
                topTwoVotes[0] - topTwoVotes[1]
            }
        }

        val boxes = Publisher(calculateBoxes())
        private fun updateBoxes() = boxes.submit(calculateBoxes())
        private fun calculateBoxes() =
            results.entries.asSequence()
                .sortedByDescending { it.value }
                .map {
                    Pair(
                        it.key.color,
                        it.key.abbreviation + ": " + it.value,
                    )
                }
                .toList()
    }
}
