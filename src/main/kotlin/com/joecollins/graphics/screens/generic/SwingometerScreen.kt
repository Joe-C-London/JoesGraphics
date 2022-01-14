package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.SwingometerFrame
import com.joecollins.graphics.components.SwingometerFrameBuilder
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.ceil
import kotlin.math.roundToInt

class SwingometerScreen private constructor(title: JLabel, frame: SwingometerFrame) : JPanel() {
    class Builder<T>(
        prevVotesPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        resultsPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
        partiesPublisher: Flow.Publisher<out Pair<Party, Party>>,
        partySwingsPublisher: Flow.Publisher<out Map<Party, Double>>,
        headerPublisher: Flow.Publisher<out String?>
    ) {
        private class Inputs<T> {
            private var _prevVotes: Map<T, Map<Party, Int>> = HashMap()
            private var _results: Map<T, PartyResult?> = HashMap()
            private var _filteredSeats: Set<T>? = null
            private var _parties: Pair<Party, Party>? = null
            private var _partySwings: Map<Party, Double> = HashMap()
            private var _range: Number = 0.09999
            private var _seatLabelIncrement = Int.MAX_VALUE

            var prevVotes: Map<T, Map<Party, Int>>
                get() = _prevVotes
                set(prevVotes) {
                    synchronized(this) {
                        this._prevVotes = prevVotes
                        updateLeftAndRightToWin()
                        updateOuterLabels()
                        updateDots()
                    }
                }

            var results: Map<T, PartyResult?>
                get() = _results
                set(results) {
                    synchronized(this) {
                        this._results = results
                        updateDots()
                    }
                }

            var seatFilter: Set<T>?
                get() = _filteredSeats
                set(filteredSeats) {
                    synchronized(this) {
                        this._filteredSeats = filteredSeats
                        updateDots()
                    }
                }

            var parties: Pair<Party, Party>
                get() = _parties!!
                set(parties) {
                    synchronized(this) {
                        this._parties = parties
                        updateColors()
                        updateValue()
                        updateLeftAndRightToWin()
                        updateOuterLabels()
                        updateDots()
                    }
                }

            var partySwings: Map<Party, Double>
                get() = _partySwings
                set(partySwings) {
                    synchronized(this) {
                        this._partySwings = partySwings
                        updateValue()
                    }
                }

            var range: Number
                get() = _range
                set(range) {
                    synchronized(this) {
                        this._range = range
                        rangePublisher.submit(range)
                    }
                }

            var seatLabelIncrement: Int
                get() = _seatLabelIncrement
                set(seatLabelIncrement) {
                    synchronized(this) {
                        this._seatLabelIncrement = seatLabelIncrement
                        updateOuterLabels()
                    }
                }

            val colorsPublisher = Publisher<Pair<Color, Color>>()
            private fun updateColors() = colorsPublisher.submit(calculateColors())
            private fun calculateColors() =
                Pair(parties.first.color, parties.second.color)

            val valuePublisher = Publisher<Double>()
            private fun updateValue() { if (_parties != null) valuePublisher.submit(calculateValue()) }
            private fun calculateValue(): Double {
                val left = partySwings[parties.first] ?: 0.0
                val right = partySwings[parties.second] ?: 0.0
                return (right - left) / 2
            }

            val rangePublisher = Publisher(_range)

            val leftToWinPublisher = Publisher<Double>()
            private fun calculateLeftToWin() =
                getSwingNeededForMajority(
                    prevVotes, parties.first, parties.second
                )

            val rightToWinPublisher = Publisher<Double>()
            private fun calculateRightToWin() =
                getSwingNeededForMajority(
                    prevVotes, parties.second, parties.first
                )

            private fun updateLeftAndRightToWin() {
                if (_parties != null) {
                    leftToWinPublisher.submit(calculateLeftToWin())
                    rightToWinPublisher.submit(calculateRightToWin())
                }
            }

            val outerLabelsPublisher = Publisher<List<Triple<Double, Color, String>>>()
            private fun updateOuterLabels() { if (_parties != null) outerLabelsPublisher.submit(calculateOuterLabels()) }
            private fun calculateOuterLabels(): List<Triple<Double, Color, String>> {
                val leftSwingList = createSwingList(
                    prevVotes.values, parties.first, parties.second
                )
                val rightSwingList = createSwingList(
                    prevVotes.values, parties.second, parties.first
                )
                val ret: MutableList<Triple<Double, Color, String>> = LinkedList()
                val leftSeats = getNumSeats(leftSwingList)
                val rightSeats = getNumSeats(rightSwingList)
                val majority = prevVotes.size / 2 + 1
                addZeroLabel(ret, parties, leftSeats, rightSeats)
                addMajorityLabels(ret, parties, leftSwingList, rightSwingList, majority)
                addLeadChangeLabel(
                    ret, parties, leftSwingList, rightSwingList, leftSeats, rightSeats
                )
                addIncrementLabels(
                    ret,
                    leftSwingList,
                    rightSwingList,
                    leftSeats,
                    rightSeats,
                    prevVotes,
                    seatLabelIncrement,
                    parties
                )
                filterNearbyLabels(ret)
                return ret
            }

            val dotsPublisher = Publisher<List<Triple<Double, Color, Boolean>>>()
            private fun updateDots() {
                if (_parties != null) dotsPublisher.submit(calculateDots())
            }
            private fun calculateDots(): List<Triple<Double, Color, Boolean>> =
                prevVotes.entries.asSequence()
                    .map { e ->
                        Triple(
                            e.value,
                            results[e.key] ?: PartyResult(null, false),
                            seatFilter?.contains(e.key) ?: true
                        )
                    }
                    .filter { e ->
                        val winner = e.first.entries
                            .maxByOrNull { it.value }!!
                            .key
                        winner == parties.first || winner == parties.second
                    }
                    .map { e ->
                        val total = e.first.values.sum()
                        val left = e.first[parties.first] ?: 0
                        val right = e.first[parties.second] ?: 0
                        Triple(
                            0.5 * (left - right) / total,
                            e.second.color,
                            e.third
                        )
                    }
                    .toList()
        }

        private val inputs = Inputs<T>()
        private val header: Flow.Publisher<out String?>

        fun withSeatLabelIncrements(incrementPublisher: Flow.Publisher<out Int>): Builder<T> {
            incrementPublisher.subscribe(Subscriber { inputs.seatLabelIncrement = it })
            return this
        }

        fun withSeatFilter(seatsFilterPublisher: Flow.Publisher<out Set<T>?>): Builder<T> {
            seatsFilterPublisher.subscribe(Subscriber { inputs.seatFilter = it })
            return this
        }

        fun build(title: Flow.Publisher<out String?>): SwingometerScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            title.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            val swingometer = createSwingometer()
            return SwingometerScreen(headerLabel, swingometer)
        }

        private fun createSwingometer(): SwingometerFrame {
            val dotsList = createDotsForSwingometer()
            val colorsPublisher = inputs.colorsPublisher
            val valuePublisher = inputs.valuePublisher
            val rangePublisher = inputs.rangePublisher
            val leftToWinPublisher = inputs.leftToWinPublisher
            val rightToWinPublisher = inputs.rightToWinPublisher
            return SwingometerFrameBuilder.basic(colorsPublisher, valuePublisher)
                .withDotsSolid(dotsList, { obj -> obj.first }, { obj -> obj.second }) { obj -> obj.third }
                .withHeader(header)
                .withRange(rangePublisher)
                .withTickInterval(0.01.asOneTimePublisher()) { n: Number -> (n.toDouble() * 100).roundToInt().toString() }
                .withLeftNeedingToWin(leftToWinPublisher)
                .withRightNeedingToWin(rightToWinPublisher)
                .withBucketSize(0.005.asOneTimePublisher())
                .withOuterLabels(outerLabels, { obj -> obj.first }, { obj -> obj.third }) { obj -> obj.second }
                .build()
        }

        private val outerLabels: Flow.Publisher<List<Triple<Double, Color, String>>>
            get() {
                return inputs.outerLabelsPublisher
            }

        companion object {

            private fun <T> addIncrementLabels(
                list: MutableList<Triple<Double, Color, String>>,
                leftSwingList: List<Double>,
                rightSwingList: List<Double>,
                leftSeats: Int,
                rightSeats: Int,
                prevVotes: Map<T, Map<Party, Int>>,
                seatLabelIncrement: Int,
                parties: Pair<Party, Party>
            ) {
                var i = 0
                while (i < prevVotes.size) {
                    if (i <= (leftSeats + rightSeats) / 2) {
                        i += seatLabelIncrement
                        continue
                    }
                    if (i <= leftSwingList.size) {
                        list.add(
                            Triple(-leftSwingList[i - 1], parties.first.color, i.toString())
                        )
                    }
                    if (i <= rightSwingList.size) {
                        list.add(
                            Triple(rightSwingList[i - 1], parties.second.color, i.toString())
                        )
                    }
                    i += seatLabelIncrement
                }
            }

            private fun filterNearbyLabels(ret: MutableList<Triple<Double, Color, String>>) {
                val ranges: MutableSet<ClosedRange<Double>> = HashSet()
                val it = ret.iterator()
                while (it.hasNext()) {
                    val item = it.next()
                    if (ranges.any { range -> range.contains(item.first) }) {
                        it.remove()
                    } else {
                        ranges.add((item.first - 0.005).rangeTo(item.first + 0.005))
                    }
                }
            }

            private fun addLeadChangeLabel(
                list: MutableList<Triple<Double, Color, String>>,
                parties: Pair<Party, Party>,
                leftSwingList: List<Double>,
                rightSwingList: List<Double>,
                leftSeats: Int,
                rightSeats: Int
            ) {
                if (leftSeats != rightSeats) {
                    val newLeadSeats = ceil(0.5 * (leftSeats + rightSeats)).toInt()
                    val swing: Double
                    val color: Color
                    if (leftSeats > rightSeats) {
                        swing = rightSwingList
                            .drop(newLeadSeats - 1)
                            .firstOrNull()
                            ?: Double.POSITIVE_INFINITY
                        color = if ((leftSeats + rightSeats) % 2 == 0) Color.BLACK else parties.second.color
                    } else {
                        swing = -1 *
                            (
                                leftSwingList
                                    .drop(newLeadSeats - 1)
                                    .firstOrNull()
                                    ?: Double.POSITIVE_INFINITY
                                )
                        color = if ((leftSeats + rightSeats) % 2 == 0) Color.BLACK else parties.first.color
                    }
                    list.add(Triple(swing, color, newLeadSeats.toString()))
                }
            }

            private fun addMajorityLabels(
                list: MutableList<Triple<Double, Color, String>>,
                parties: Pair<Party, Party>,
                leftSwingList: List<Double>,
                rightSwingList: List<Double>,
                majority: Int
            ) {
                val leftMajority = -1 *
                    (
                        leftSwingList
                            .drop((majority - 1))
                            .firstOrNull()
                            ?: Double.POSITIVE_INFINITY
                        )
                val rightMajority = rightSwingList.drop((majority - 1)).firstOrNull() ?: Double.POSITIVE_INFINITY
                if (leftMajority != rightMajority || leftMajority < 0) {
                    list.add(
                        Triple(leftMajority, parties.first.color, majority.toString())
                    )
                }
                if (leftMajority != rightMajority || rightMajority > 0) {
                    list.add(
                        Triple(rightMajority, parties.second.color, majority.toString())
                    )
                }
            }

            private fun addZeroLabel(
                list: MutableList<Triple<Double, Color, String>>,
                parties: Pair<Party, Party>,
                leftSeats: Int,
                rightSeats: Int
            ) {
                list.add(
                    when {
                        leftSeats > rightSeats -> Triple(0.0, parties.first.color, leftSeats.toString())
                        rightSeats > leftSeats -> Triple(0.0, parties.second.color, rightSeats.toString())
                        else -> Triple(0.0, Color.BLACK, rightSeats.toString())
                    }
                )
            }

            private fun getNumSeats(swings: List<Double>): Int {
                return swings.filter { s: Double -> s < 0 }.count()
            }

            private fun <T> getSwingNeededForMajority(
                votes: Map<T, Map<Party, Int>>,
                focusParty: Party?,
                compParty: Party?
            ): Double {
                val majority = votes.size / 2 + 1
                return createSwingList(votes.values, focusParty, compParty)
                    .drop(majority - 1)
                    .firstOrNull()
                    ?: Double.POSITIVE_INFINITY
            }

            private fun createSwingList(
                results: Collection<Map<Party, Int>>,
                focusParty: Party?,
                compParty: Party?
            ): List<Double> {
                return results.asSequence()
                    .filter { m ->
                        val winner = m.entries.maxByOrNull { it.value }!!.key
                        winner == focusParty || winner == compParty
                    }
                    .map { m ->
                        val total = m.values.sum()
                        val focus = m[focusParty] ?: 0
                        val comp = m[compParty] ?: 0
                        0.5 * (comp - focus) / total
                    }
                    .sorted()
                    .toList()
            }
        }

        private fun createDotsForSwingometer(): Flow.Publisher<List<Triple<Double, Color, Boolean>>> {
            return inputs.dotsPublisher
        }

        init {
            prevVotesPublisher.subscribe(Subscriber { inputs.prevVotes = it })
            resultsPublisher.subscribe(Subscriber { inputs.results = it })
            partiesPublisher.subscribe(Subscriber { inputs.parties = it })
            partySwingsPublisher.subscribe(Subscriber { inputs.partySwings = it })
            header = headerPublisher
        }
    }

    companion object {
        @JvmStatic fun <T> of(
            prevVotes: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            results: Flow.Publisher<out Map<T, PartyResult?>>,
            swing: Flow.Publisher<out Map<Party, Double>>,
            parties: Flow.Publisher<out Pair<Party, Party>>,
            header: Flow.Publisher<out String?>
        ): Builder<T> {
            return Builder(prevVotes, results, parties, swing, header)
        }
    }

    init {
        background = Color.WHITE
        layout = BorderLayout()
        add(title, BorderLayout.NORTH)
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.border = EmptyBorder(5, 5, 5, 5)
        panel.layout = GridLayout(1, 1)
        panel.add(frame)
        add(panel, BorderLayout.CENTER)
    }
}
