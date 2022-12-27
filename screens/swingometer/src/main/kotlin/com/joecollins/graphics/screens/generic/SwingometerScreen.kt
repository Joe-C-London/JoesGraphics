package com.joecollins.graphics.screens.generic

import ResultColorUtils.color
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.SwingometerFrame
import com.joecollins.graphics.components.SwingometerFrameBuilder
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow
import kotlin.math.ceil
import kotlin.math.roundToInt

class SwingometerScreen private constructor(title: Flow.Publisher<out String?>, frame: SwingometerFrame) : GenericPanel(pad(frame), title) {
    class Builder<T> internal constructor(
        prevVotesPublisher: Flow.Publisher<out Map<T, Map<out PartyOrCoalition, Int>>>,
        resultsPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
        partiesPublisher: Flow.Publisher<out Pair<PartyOrCoalition, PartyOrCoalition>>,
        partySwingsPublisher: Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
        headerPublisher: Flow.Publisher<out String?>
    ) {
        private inner class Inputs {
            var prevVotes: Map<T, Map<out PartyOrCoalition, Int>> = emptyMap()
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

            var parties: Pair<PartyOrCoalition, PartyOrCoalition> = Party.OTHERS to Party.OTHERS
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

            var partySwings: Map<out PartyOrCoalition, Double> = emptyMap()
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

            var carryovers: Map<Party, Int> = emptyMap()
                set(value) {
                    synchronized(this) {
                        field = value
                        updateLeftAndRightToWin()
                        updateOuterLabels()
                        updateDots()
                    }
                }

            var weights: ((T) -> Int)? = null
                set(value) {
                    synchronized(this) {
                        field = value
                        updateLeftAndRightToWin()
                        updateOuterLabels()
                        updateDots()
                    }
                }

            val colorsPublisher = Publisher<Pair<Color, Color>>()
            private fun updateColors() = colorsPublisher.submit(calculateColors())
            private fun calculateColors() =
                Pair(parties.first.color, parties.second.color)

            val valuePublisher = Publisher<Double>()
            private fun updateValue() {
                valuePublisher.submit(calculateValue())
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
                    prevVotes,
                    parties.first,
                    parties.second,
                    carryovers
                )

            val rightToWinPublisher = Publisher<Double>()
            private fun calculateRightToWin() =
                getSwingNeededForMajority(
                    prevVotes,
                    parties.second,
                    parties.first,
                    carryovers
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
                    prevVotes,
                    parties.first,
                    parties.second,
                    carryovers
                )
                val rightSwingList = createSwingList(
                    prevVotes,
                    parties.second,
                    parties.first,
                    carryovers
                )
                val leftSeats = getNumSeats(leftSwingList)
                val rightSeats = getNumSeats(rightSwingList)
                val majority = (prevVotes.keys.sumOf { weights?.let { f -> f(it) } ?: 1 } + carryovers.values.sum()) / 2 + 1
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
                        parties,
                        carryovers
                    ).asSequence()
                ).flatten()
                    .let { filterNearbyLabels(it) }
                    .toList()
            }

            val dotsPublisher = Publisher<List<Dot>>()
            private fun updateDots() {
                dotsPublisher.submit(calculateDots())
            }

            private fun calculateDots(): List<Dot> =
                prevVotes.entries.asSequence()
                    .map { e ->
                        Triple(
                            e,
                            results[e.key],
                            seatFilter?.contains(e.key) ?: true
                        )
                    }
                    .filter { e ->
                        val winner = e.first.value.entries
                            .maxByOrNull { it.value }!!
                            .key
                        winner == parties.first || winner == parties.second
                    }
                    .map { e ->
                        val total = e.first.value.values.sum()
                        val left = e.first.value[parties.first] ?: 0
                        val right = e.first.value[parties.second] ?: 0
                        Dot(
                            0.5 * (left - right) / total,
                            e.second.color,
                            e.third,
                            weights?.let { f -> f(e.first.key).toString() }
                        )
                    }
                    .toList()
        }

        private val inputs = Inputs()
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

        fun withProgressLabel(progressLabelPublisher: Flow.Publisher<out String?>): Builder<T> {
            this.progressLabel = progressLabelPublisher
            return this
        }

        fun withCarryovers(seats: Flow.Publisher<Map<Party, Int>>): Builder<T> {
            seats.subscribe(Subscriber { inputs.carryovers = it })
            return this
        }

        fun withWeights(weights: (T) -> Int): Builder<T> {
            inputs.weights = weights
            return this
        }

        fun build(title: Flow.Publisher<out String?>): SwingometerScreen {
            return SwingometerScreen(title, createSwingometer())
        }

        private fun createSwingometer(): SwingometerFrame {
            val dotsList = createDotsForSwingometer()
            val colorsPublisher = inputs.colorsPublisher
            val valuePublisher = inputs.valuePublisher
            val rangePublisher = inputs.rangePublisher
            val leftToWinPublisher = inputs.leftToWinPublisher
            val rightToWinPublisher = inputs.rightToWinPublisher
            return SwingometerFrameBuilder.basic(colorsPublisher, valuePublisher)
                .withDots(dotsList, { it.position }, { it.color }, { it.label ?: "" }) { it.visible }
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

        private fun incrementLabels(
            leftSwingList: List<Double>,
            rightSwingList: List<Double>,
            leftSeats: Int,
            rightSeats: Int,
            prevVotes: Map<T, Map<out PartyOrCoalition, Int>>,
            seatLabelIncrement: Int,
            parties: Pair<PartyOrCoalition, PartyOrCoalition>,
            carryovers: Map<out PartyOrCoalition, Int>
        ): ArrayList<Triple<Double, Color, String>> {
            val ret = ArrayList<Triple<Double, Color, String>>()
            var i = 0
            while (i < (prevVotes.keys.sumOf { inputs.weights?.let { f -> f(it) } ?: 1 } + carryovers.values.sum())) {
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
            parties: Pair<PartyOrCoalition, PartyOrCoalition>,
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
            parties: Pair<PartyOrCoalition, PartyOrCoalition>,
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
            if (rightMajority > 0) {
                ret.reverse()
            }
            return ret
        }

        private fun zeroLabel(
            parties: Pair<PartyOrCoalition, PartyOrCoalition>,
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

        private fun getSwingNeededForMajority(
            votes: Map<T, Map<out PartyOrCoalition, Int>>,
            focusParty: PartyOrCoalition?,
            compParty: PartyOrCoalition?,
            carryovers: Map<Party, Int>
        ): Double {
            val majority = (votes.keys.sumOf { inputs.weights?.let { f -> f(it) } ?: 1 } + carryovers.values.sum()) / 2 + 1
            return createSwingList(votes, focusParty, compParty, carryovers)
                .drop(majority - 1)
                .firstOrNull()
                ?: Double.POSITIVE_INFINITY
        }

        private fun createSwingList(
            results: Map<T, Map<out PartyOrCoalition, Int>>,
            focusParty: PartyOrCoalition?,
            compParty: PartyOrCoalition?,
            carryovers: Map<Party, Int>
        ): List<Double> {
            val contestedSeats = results.asSequence()
                .filter { m ->
                    val winner = m.value.entries.maxByOrNull { it.value }!!.key
                    winner == focusParty || winner == compParty
                }
                .flatMap { m ->
                    val total = m.value.values.sum()
                    val focus = m.value[focusParty] ?: 0
                    val comp = m.value[compParty] ?: 0
                    val position: Double
                    if (total == 0) {
                        position = if (m.value.containsKey(focusParty)) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
                    } else {
                        position = 0.5 * (comp - focus) / total
                    }
                    val count = inputs.weights?.let { f -> f(m.key) } ?: 1
                    generateSequence { position }.take(count)
                }
            val focusCarries = generateSequence { Double.NEGATIVE_INFINITY }.take(carryovers[focusParty] ?: 0)
            val compCarries = generateSequence { Double.POSITIVE_INFINITY }.take(carryovers[compParty] ?: 0)
            return sequenceOf(contestedSeats, focusCarries, compCarries).flatten()
                .sorted()
                .toList()
        }

        private data class Dot(val position: Double, val color: Color, val visible: Boolean, val label: String?)

        private fun createDotsForSwingometer(): Flow.Publisher<List<Dot>> {
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
            prevVotes: Flow.Publisher<out Map<T, Map<out PartyOrCoalition, Int>>>,
            results: Flow.Publisher<out Map<T, PartyResult?>>,
            swing: Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
            parties: Flow.Publisher<out Pair<PartyOrCoalition, PartyOrCoalition>>,
            header: Flow.Publisher<out String?>
        ): Builder<T> {
            return Builder(prevVotes, results, parties, swing, header)
        }

        fun <T> of(
            prevVotes: Flow.Publisher<out Map<T, Map<out PartyOrCoalition, Int>>>,
            results: Flow.Publisher<out Map<T, PartyResult?>>,
            currTotal: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prevTotal: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            parties: Flow.Publisher<out Pair<PartyOrCoalition, PartyOrCoalition>>,
            header: Flow.Publisher<out String?>
        ): Builder<T> {
            val swing = currTotal.merge(prevTotal) { curr, prev ->
                val ct = curr.values.sum().toDouble()
                val pt = prev.values.sum().toDouble()
                val allParties: Set<PartyOrCoalition> = sequenceOf(curr.keys.asSequence(), prev.keys.asSequence()).flatten().toSet()
                allParties.associateWith { p: PartyOrCoalition? ->
                    (curr[p] ?: 0) / ct.coerceAtLeast(1e-6) - (prev[p] ?: 0) / pt.coerceAtLeast(1e-6)
                }
            }
            return Builder(prevVotes, results, parties, swing, header)
        }
    }
}
