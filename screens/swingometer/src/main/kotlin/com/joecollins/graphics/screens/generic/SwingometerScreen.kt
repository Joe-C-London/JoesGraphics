package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.SwingometerFrame
import com.joecollins.graphics.components.SwingometerFrameBuilder
import com.joecollins.graphics.components.SwingometerFrameBuilder.dots
import com.joecollins.graphics.components.SwingometerFrameBuilder.every
import com.joecollins.graphics.components.SwingometerFrameBuilder.labels
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyOrCandidate.Companion.convertToParty
import com.joecollins.models.general.PartyOrCandidate.Companion.convertToPartyOrCandidate
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.ResultColorUtils.getColor
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow
import kotlin.math.absoluteValue
import kotlin.math.ceil
import kotlin.math.roundToInt

class SwingometerScreen private constructor(title: Flow.Publisher<out String?>, frame: SwingometerFrame, altText: Flow.Publisher<String>) : GenericPanel(pad(frame), title, altText) {

    companion object {
        fun <T> Flow.Publisher<out Map<T, Map<out Party, Int>>>.convertToPartyOrCandidateForSwingometer() = map { it.mapValues { e -> e.value.convertToPartyOrCandidate() } }

        fun <T> of(
            prevVotes: Flow.Publisher<out Map<T, Map<out PartyOrCandidate, Int>>>,
            results: Flow.Publisher<out Map<T, PartyResult?>>,
            swing: Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
            parties: Flow.Publisher<out Pair<PartyOrCoalition, PartyOrCoalition>>,
            seatLabelIncrement: Flow.Publisher<out Int>? = null,
            seatFilter: Flow.Publisher<out Set<T>?>? = null,
            range: Flow.Publisher<Number>? = null,
            carryovers: Flow.Publisher<Map<Party, Int>>? = null,
            weights: (T.() -> Int)? = null,
            header: Flow.Publisher<out String?>,
            progressLabel: Flow.Publisher<out String?>? = null,
            title: Flow.Publisher<out String?>,
        ): SwingometerScreen {
            val inputs = Inputs<T>()
            prevVotes.subscribe(Subscriber { inputs.prevVotes = it })
            results.subscribe(Subscriber { inputs.results = it })
            parties.subscribe(Subscriber { inputs.parties = it })
            swing.subscribe(Subscriber { inputs.partySwings = it })
            seatLabelIncrement?.subscribe(Subscriber { inputs.seatLabelIncrement = it })
            seatFilter?.subscribe(Subscriber { inputs.seatFilter = it })
            range?.subscribe(Subscriber { inputs.range = it })
            carryovers?.subscribe(Subscriber { inputs.carryovers = it })
            inputs.weights = weights
            return SwingometerScreen(
                title,
                createSwingometer(inputs, header, progressLabel),
                createAltText(inputs, header, progressLabel, title),
            )
        }

        fun calculateSwing(
            currTotal: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prevTotal: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
        ) = currTotal.merge(prevTotal) { curr, prev ->
            val ct = curr.values.sum().toDouble()
            val pt = prev.values.sum().toDouble()
            if (ct == 0.0 || pt == 0.0) return@merge emptyMap()
            val allParties: Set<PartyOrCoalition> = sequenceOf(curr.keys.asSequence(), prev.keys.asSequence()).flatten().toSet()
            allParties.associateWith { p: PartyOrCoalition? ->
                (curr[p] ?: 0) / ct - (prev[p] ?: 0) / pt
            }
        }

        private fun createSwingometer(
            inputs: Inputs<*>,
            header: Flow.Publisher<out String?>,
            progressLabel: Flow.Publisher<out String?>?,
        ): SwingometerFrame = SwingometerFrameBuilder.build(
            colors = inputs.colorsPublisher,
            value = inputs.valuePublisher,
            range = inputs.rangePublisher,
            bucketSize = 0.005.asOneTimePublisher(),
            tickInterval = every(0.01.asOneTimePublisher()) { (it.toDouble() * 100).roundToInt().toString() },
            leftToWin = inputs.leftToWinPublisher,
            rightToWin = inputs.rightToWinPublisher,
            outerLabels = labels(
                labels = inputs.outerLabelsPublisher,
                position = { first },
                label = { third },
                color = { second },
            ),
            dots = dots(
                dots = inputs.dotsPublisher,
                position = { position },
                color = { color },
                label = { label ?: "" },
                solid = { visible },
            ),
            header = header,
            rightHeaderLabel = progressLabel,
        )

        private fun createAltText(
            inputs: Inputs<*>,
            header: Flow.Publisher<out String?>,
            progressLabel: Flow.Publisher<out String?>?,
            title: Flow.Publisher<out String?>,
        ): Flow.Publisher<String> {
            val headerText: Flow.Publisher<out String?> = title.merge(header) { t, h -> "$t\n$h" }
                .run {
                    if (progressLabel == null) {
                        this
                    } else {
                        merge(progressLabel) { t, p -> t + (if (p.isNullOrBlank()) "" else " [$p]") }
                    }
                }
            val swing = inputs.valuePublisher.merge(inputs.partiesPublisher) { value, (left, right) ->
                if (value == 0.0) return@merge "NO SWING BETWEEN ${left.abbreviation} AND ${right.abbreviation}"
                val from = if (value < 0) right else left
                val to = if (value < 0) left else right
                DecimalFormat("0.0%").format(value.absoluteValue) + " SWING ${from.abbreviation} TO ${to.abbreviation}"
            }
            val seats = inputs.seatsPublisher.map { (party, seats) ->
                "${party?.abbreviation ?: "EACH"} WOULD HAVE $seats ON UNIFORM SWING"
            }
            val leftToWin = inputs.leftToWinPublisher.merge(inputs.partiesPublisher) { toWin, (left, right) ->
                if (toWin.isInfinite()) {
                    null
                } else if (toWin > 0) {
                    "${left.abbreviation} NEEDS ${DecimalFormat("0.0%").format(toWin)} SWING FROM ${right.abbreviation} TO GAIN MAJORITY"
                } else {
                    "${left.abbreviation} NEEDS TO AVOID ${DecimalFormat("0.0%").format(-toWin)} SWING TO ${right.abbreviation} TO HOLD MAJORITY"
                }
            }
            val rightToWin = inputs.rightToWinPublisher.merge(inputs.partiesPublisher) { toWin, (left, right) ->
                if (toWin.isInfinite()) {
                    null
                } else if (toWin > 0) {
                    "${right.abbreviation} NEEDS ${DecimalFormat("0.0%").format(toWin)} SWING FROM ${left.abbreviation} TO GAIN MAJORITY"
                } else {
                    "${right.abbreviation} NEEDS TO AVOID ${DecimalFormat("0.0%").format(-toWin)} SWING TO ${left.abbreviation} TO HOLD MAJORITY"
                }
            }
            val toWin = leftToWin.merge(rightToWin) { l, r ->
                if (l == null && r == null) {
                    null
                } else {
                    sequenceOf(l, r).filterNotNull().joinToString("\n")
                }
            }
            return headerText.merge(swing) { h, s -> "$h\n\n$s" }
                .merge(seats) { h, s -> "$h\n$s" }
                .merge(toWin) { h, w -> h + (if (w == null) "" else "\n\n$w") }
        }
    }

    private class Inputs<T> {
        var prevVotes: Map<T, Map<out PartyOrCandidate, Int>> = emptyMap()
            set(value) {
                synchronized(this) {
                    field = value
                    updateLeftAndRightToWin()
                    updateOuterLabels()
                    updateDots()
                    updateSeats()
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
                    updateParties()
                    updateValue()
                    updateLeftAndRightToWin()
                    updateOuterLabels()
                    updateDots()
                    updateSeats()
                }
            }

        var partySwings: Map<out PartyOrCoalition, Double> = emptyMap()
            set(value) {
                synchronized(this) {
                    field = value
                    updateValue()
                    updateSeats()
                }
            }

        var range: Number = 0.09999
            set(value) {
                synchronized(this) {
                    field = value
                    rangePublisher.submit(range)
                    updateOuterLabels()
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
                    updateSeats()
                }
            }

        var weights: (T.() -> Int)? = null
            set(value) {
                synchronized(this) {
                    field = value
                    updateLeftAndRightToWin()
                    updateOuterLabels()
                    updateDots()
                    updateSeats()
                }
            }

        val weightsAbsolute get() = weights ?: { 1 }

        val partiesPublisher = Publisher<Pair<PartyOrCoalition, PartyOrCoalition>>()
        val colorsPublisher = partiesPublisher.map {
            it.first.color to it.second.color
        }
        private fun updateParties() = partiesPublisher.submit(parties)

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
        private fun calculateLeftToWin() = getSwingNeededForMajority(
            prevVotes,
            parties.first,
            parties.second,
            carryovers,
        )

        val rightToWinPublisher = Publisher<Double>()
        private fun calculateRightToWin() = getSwingNeededForMajority(
            prevVotes,
            parties.second,
            parties.first,
            carryovers,
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
                carryovers,
            )
            val rightSwingList = createSwingList(
                prevVotes,
                parties.second,
                parties.first,
                carryovers,
            )
            val leftSeats = getNumSeats(leftSwingList)
            val rightSeats = getNumSeats(rightSwingList)
            val majority = (prevVotes.keys.sumOf { it.weightsAbsolute() } + carryovers.values.sum()) / 2 + 1
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
                    carryovers,
                ).asSequence(),
            ).flatten()
                .let { filterNearbyLabels(it, 0.05 * range.toDouble()) }
                .toList()
        }

        val dotsPublisher = Publisher<List<Dot>>()
        private fun updateDots() {
            dotsPublisher.submit(calculateDots())
        }

        private fun calculateDots(): List<Dot> = prevVotes.entries.asSequence()
            .map { e ->
                Triple(
                    e,
                    results[e.key],
                    seatFilter?.contains(e.key) ?: true,
                )
            }
            .filter { e ->
                val winner = e.first.value.entries
                    .maxByOrNull { it.value }!!
                    .key
                    .party
                parties.toList().flatMap { it.constituentParties }.contains(winner)
            }
            .map { e ->
                val total = e.first.value.values.sum()
                val left = parties.first.constituentParties.sumOf { e.first.value.convertToParty()[it] ?: 0 }
                val right = parties.second.constituentParties.sumOf { e.first.value.convertToParty()[it] ?: 0 }
                Dot(
                    0.5 * (left - right) / total,
                    e.second.getColor(default = Color.LIGHT_GRAY),
                    e.third,
                    if (weights == null) null else e.first.key.weightsAbsolute().toString(),
                )
            }
            .toList()

        val seatsPublisher = Publisher<Pair<PartyOrCoalition?, Int>>()
        private fun updateSeats() {
            seatsPublisher.submit(calculateSeats())
        }

        private fun calculateSeats(): Pair<PartyOrCoalition?, Int> {
            val leftSwingList = createSwingList(
                prevVotes,
                parties.first,
                parties.second,
                carryovers,
            )
            val rightSwingList = createSwingList(
                prevVotes,
                parties.second,
                parties.first,
                carryovers,
            )
            val value = calculateValue()
            val leftSeats = leftSwingList.count { it < -value }
            val rightSeats = rightSwingList.count { it < value }
            return if (leftSeats == rightSeats) {
                null to leftSeats
            } else if (leftSeats < rightSeats) {
                parties.second to rightSeats
            } else {
                parties.first to leftSeats
            }
        }

        private fun getSwingNeededForMajority(
            votes: Map<T, Map<out PartyOrCandidate, Int>>,
            focusParty: PartyOrCoalition,
            compParty: PartyOrCoalition,
            carryovers: Map<Party, Int>,
        ): Double {
            val majority = (votes.keys.sumOf { weightsAbsolute(it) } + carryovers.values.sum()) / 2 + 1
            return createSwingList(votes, focusParty, compParty, carryovers)
                .drop(majority - 1)
                .firstOrNull()
                ?: Double.POSITIVE_INFINITY
        }

        private fun createSwingList(
            results: Map<T, Map<out PartyOrCandidate, Int>>,
            focusParty: PartyOrCoalition,
            compParty: PartyOrCoalition,
            carryovers: Map<Party, Int>,
        ): List<Double> {
            val contestedSeats = results.asSequence()
                .filter { m ->
                    val winner = m.value.entries.maxByOrNull { it.value }!!.key.party
                    focusParty.constituentParties.contains(winner) || compParty.constituentParties.contains(winner)
                }
                .flatMap { m ->
                    val total = m.value.values.sum()
                    val focus = focusParty.constituentParties.sumOf { m.value.convertToParty()[it] ?: 0 }
                    val comp = compParty.constituentParties.sumOf { m.value.convertToParty()[it] ?: 0 }
                    val position = if (total == 0) {
                        if (m.value.convertToParty().containsKey(focusParty)) Double.NEGATIVE_INFINITY else Double.POSITIVE_INFINITY
                    } else {
                        0.5 * (comp - focus) / total
                    }
                    val count = weightsAbsolute(m.key)
                    generateSequence { position }.take(count)
                }
            val focusCarries = generateSequence { Double.NEGATIVE_INFINITY }.take(carryovers[focusParty] ?: 0)
            val compCarries = generateSequence { Double.POSITIVE_INFINITY }.take(carryovers[compParty] ?: 0)
            return sequenceOf(contestedSeats, focusCarries, compCarries).flatten()
                .sorted()
                .toList()
        }

        private fun filterNearbyLabels(ret: Sequence<Triple<Double, Color, String>>, margin: Double): Sequence<Triple<Double, Color, String>> {
            val ranges: MutableSet<ClosedRange<Double>> = HashSet()
            return ret.filter { item ->
                if (ranges.any { range -> range.contains(item.first) }) {
                    false
                } else {
                    ranges.add((item.first - margin).rangeTo(item.first + margin))
                    true
                }
            }
        }

        private fun leadChangeLabel(
            parties: Pair<PartyOrCoalition, PartyOrCoalition>,
            leftSwingList: List<Double>,
            rightSwingList: List<Double>,
            leftSeats: Int,
            rightSeats: Int,
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
            majority: Int,
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
                    Triple(leftMajority, parties.first.color, majority.toString()),
                )
            }
            if (leftMajority != rightMajority || rightMajority > 0) {
                ret.add(
                    Triple(rightMajority, parties.second.color, majority.toString()),
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
            rightSeats: Int,
        ) = when {
            leftSeats > rightSeats -> Triple(0.0, parties.first.color, leftSeats.toString())
            rightSeats > leftSeats -> Triple(0.0, parties.second.color, rightSeats.toString())
            else -> Triple(0.0, Color.BLACK, rightSeats.toString())
        }

        private fun getNumSeats(swings: List<Double>): Int = swings.count { it < 0 }

        private fun incrementLabels(
            leftSwingList: List<Double>,
            rightSwingList: List<Double>,
            leftSeats: Int,
            rightSeats: Int,
            prevVotes: Map<T, Map<out PartyOrCandidate, Int>>,
            seatLabelIncrement: Int,
            parties: Pair<PartyOrCoalition, PartyOrCoalition>,
            carryovers: Map<out PartyOrCoalition, Int>,
        ): ArrayList<Triple<Double, Color, String>> {
            val ret = ArrayList<Triple<Double, Color, String>>()
            var i = 0
            while (i < (prevVotes.keys.sumOf { weightsAbsolute(it) } + carryovers.values.sum())) {
                if (i <= (leftSeats + rightSeats) / 2) {
                    i += seatLabelIncrement
                    continue
                }
                if (i <= leftSwingList.size) {
                    ret.add(
                        Triple(-leftSwingList[i - 1], parties.first.color, i.toString()),
                    )
                }
                if (i <= rightSwingList.size) {
                    ret.add(
                        Triple(rightSwingList[i - 1], parties.second.color, i.toString()),
                    )
                }
                i += seatLabelIncrement
            }
            return ret
        }
    }

    private data class Dot(val position: Double, val color: Color, val visible: Boolean, val label: String?)
}
