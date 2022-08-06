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
            var prevVotes: Map<T, Map<Party, Int>> = emptyMap()
                set(value) {
                    synchronized(this) {
                        field = value
                        updateLeftAndRightToWin()
                        updateOuterLabels()
                        updateDots()
                    }
                }

            var results: Map<T, PartyResult?> = emptyMap()
                set(value) {
                    synchronized(this) {
                        field = value
                        updateDots()
                    }
                }

            var seatFilter: Set<T>? = null
                set(value) {
                    synchronized(this) {
                        field = value
                        updateDots()
                    }
                }

            var parties: Pair<Party, Party> = Party.OTHERS to Party.OTHERS
                set(value) {
                    synchronized(this) {
                        field = value
                        updateColors()
                        updateValue()
                        updateLeftAndRightToWin()
                        updateOuterLabels()
                        updateDots()
                    }
                }

            var partySwings: Map<Party, Double> = emptyMap()
                set(value) {
                    synchronized(this) {
                        field = value
                        updateValue()
                    }
                }

            var range: Number = 0.09999
                set(value) {
                    synchronized(this) {
                        field = value
                        rangePublisher.submit(range)
                    }
                }

            var seatLabelIncrement: Int = Int.MAX_VALUE
                set(value) {
                    synchronized(this) {
                        field = value
                        updateOuterLabels()
                    }
                }

            val colorsPublisher = Publisher<Pair<Color, Color>>()
            private fun updateColors() = colorsPublisher.submit(calculateColors())
            private fun calculateColors() =
                Pair(parties.first.color, parties.second.color)

            val valuePublisher = Publisher<Double>()
            private fun updateValue() {
                if (parties != null) valuePublisher.submit(calculateValue())
            }

            private fun calculateValue(): Double {
                val left = partySwings[parties.first] ?: 0.0
                val right = partySwings[parties.second] ?: 0.0
                return (right - left) / 2
            }

            val rangePublisher = Publisher(range)

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
                leftToWinPublisher.submit(calculateLeftToWin())
                rightToWinPublisher.submit(calculateRightToWin())
            }

            val outerLabelsPublisher = Publisher<List<Triple<Double, Color, String>>>()
            private fun updateOuterLabels() {
                outerLabelsPublisher.submit(calculateOuterLabels())
            }

            private fun calculateOuterLabels(): List<Triple<Double, Color, String>> {
                val leftSwingList = createSwingList(
                    prevVotes.values, parties.first, parties.second
                )
                val rightSwingList = createSwingList(
                    prevVotes.values, parties.second, parties.first
                )
                val leftSeats = getNumSeats(leftSwingList)
                val rightSeats = getNumSeats(rightSwingList)
                val majority = prevVotes.size / 2 + 1
                return sequenceOf(
                    sequenceOf(zeroLabel(parties, leftSeats, rightSeats)),
                    majorityLabels(parties, leftSwingList, rightSwingList, majority).asSequence(),
                    if (leftSeats != rightSeats) sequenceOf(leadChangeLabel(parties, leftSwingList, rightSwingList, leftSeats, rightSeats)) else emptySequence(),
                    incrementLabels(
                        leftSwingList,
                        rightSwingList,
                        leftSeats,
                        rightSeats,
                        prevVotes,
                        seatLabelIncrement,
                        parties
                    ).asSequence()
                ).flatten()
                    .let { filterNearbyLabels(it) }
                    .toList()
            }

            val dotsPublisher = Publisher<List<Triple<Double, Color, Boolean>>>()
            private fun updateDots() {
                dotsPublisher.submit(calculateDots())
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
        private var progressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()

        fun withSeatLabelIncrements(incrementPublisher: Flow.Publisher<out Int>): Builder<T> {
            incrementPublisher.subscribe(Subscriber { inputs.seatLabelIncrement = it })
            return this
        }

        fun withSeatFilter(seatsFilterPublisher: Flow.Publisher<out Set<T>?>): Builder<T> {
            seatsFilterPublisher.subscribe(Subscriber { inputs.seatFilter = it })
            return this
        }

        fun withRange(rangePublisher: Flow.Publisher<Number>): Builder<T> {
            rangePublisher.subscribe(Subscriber { inputs.range = it })
            return this
        }

        fun withProgressLabel(progressLabelPublisher: Flow.Publisher<String?>): Builder<T> {
            this.progressLabel = progressLabelPublisher
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
                .withDotsSolid(dotsList, { it.first }, { it.second }) { it.third }
                .withHeader(header, rightLabel = progressLabel)
                .withRange(rangePublisher)
                .withTickInterval(0.01.asOneTimePublisher()) { (it.toDouble() * 100).roundToInt().toString() }
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

            private fun <T> incrementLabels(
                leftSwingList: List<Double>,
                rightSwingList: List<Double>,
                leftSeats: Int,
                rightSeats: Int,
                prevVotes: Map<T, Map<Party, Int>>,
                seatLabelIncrement: Int,
                parties: Pair<Party, Party>
            ): ArrayList<Triple<Double, Color, String>> {
                val ret = ArrayList<Triple<Double, Color, String>>()
                var i = 0
                while (i < prevVotes.size) {
                    if (i <= (leftSeats + rightSeats) / 2) {
                        i += seatLabelIncrement
                        continue
                    }
                    if (i <= leftSwingList.size) {
                        ret.add(
                            Triple(-leftSwingList[i - 1], parties.first.color, i.toString())
                        )
                    }
                    if (i <= rightSwingList.size) {
                        ret.add(
                            Triple(rightSwingList[i - 1], parties.second.color, i.toString())
                        )
                    }
                    i += seatLabelIncrement
                }
                return ret
            }

            private fun filterNearbyLabels(ret: Sequence<Triple<Double, Color, String>>): Sequence<Triple<Double, Color, String>> {
                val ranges: MutableSet<ClosedRange<Double>> = HashSet()
                return ret.filter { item ->
                    if (ranges.any { range -> range.contains(item.first) }) {
                        false
                    } else {
                        ranges.add((item.first - 0.005).rangeTo(item.first + 0.005))
                        true
                    }
                }
            }

            private fun leadChangeLabel(
                parties: Pair<Party, Party>,
                leftSwingList: List<Double>,
                rightSwingList: List<Double>,
                leftSeats: Int,
                rightSeats: Int
            ): Triple<Double, Color, String> {
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
                return Triple(swing, color, newLeadSeats.toString())
            }

            private fun majorityLabels(
                parties: Pair<Party, Party>,
                leftSwingList: List<Double>,
                rightSwingList: List<Double>,
                majority: Int
            ): ArrayList<Triple<Double, Color, String>> {
                val ret = ArrayList<Triple<Double, Color, String>>()
                val leftMajority = -1 *
                    (
                        leftSwingList
                            .drop((majority - 1))
                            .firstOrNull()
                            ?: Double.POSITIVE_INFINITY
                        )
                val rightMajority = rightSwingList.drop((majority - 1)).firstOrNull() ?: Double.POSITIVE_INFINITY
                if (leftMajority != rightMajority || leftMajority < 0) {
                    ret.add(
                        Triple(leftMajority, parties.first.color, majority.toString())
                    )
                }
                if (leftMajority != rightMajority || rightMajority > 0) {
                    ret.add(
                        Triple(rightMajority, parties.second.color, majority.toString())
                    )
                }
                return ret
            }

            private fun zeroLabel(
                parties: Pair<Party, Party>,
                leftSeats: Int,
                rightSeats: Int
            ) = when {
                leftSeats > rightSeats -> Triple(0.0, parties.first.color, leftSeats.toString())
                rightSeats > leftSeats -> Triple(0.0, parties.second.color, rightSeats.toString())
                else -> Triple(0.0, Color.BLACK, rightSeats.toString())
            }

            private fun getNumSeats(swings: List<Double>): Int {
                return swings.count { it < 0 }
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
        fun <T> of(
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
