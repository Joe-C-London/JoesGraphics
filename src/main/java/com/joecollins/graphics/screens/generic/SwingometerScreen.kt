package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.components.SwingometerFrame
import com.joecollins.graphics.components.SwingometerFrameBuilder
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.util.HashMap
import java.util.HashSet
import java.util.LinkedList
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.ceil
import kotlin.math.roundToInt
import org.apache.commons.lang3.Range
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.apache.commons.lang3.tuple.Pair
import org.apache.commons.lang3.tuple.Triple

class SwingometerScreen private constructor(title: JLabel, frame: SwingometerFrame) : JPanel() {
    class Builder<T>(
        prevVotesBinding: Binding<Map<T, Map<Party, Int>>>,
        resultsBinding: Binding<Map<T, PartyResult?>>,
        partiesBinding: Binding<Pair<Party, Party>>,
        partySwingsBinding: Binding<Map<Party, Double>>,
        headerBinding: Binding<String?>
    ) {
        private class Inputs<T> : Bindable<Inputs<T>, Inputs.Property>() {
            enum class Property {
                PREV, RESULTS, FILTERED_SEATS, PARTIES, SWINGS, RANGE, LABEL_INCREMENT
            }

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
                this._prevVotes = prevVotes
                onPropertyRefreshed(Property.PREV)
            }

            var results: Map<T, PartyResult?>
            get() = _results
            set(results) {
                this._results = results
                onPropertyRefreshed(Property.RESULTS)
            }

            var seatFilter: Set<T>?
            get() = _filteredSeats
            set(filteredSeats) {
                this._filteredSeats = filteredSeats
                onPropertyRefreshed(Property.FILTERED_SEATS)
            }

            var parties: Pair<Party, Party>
            get() = _parties!!
            set(parties) {
                this._parties = parties
                onPropertyRefreshed(Property.PARTIES)
            }

            var partySwings: Map<Party, Double>
            get() = _partySwings
            set(partySwings) {
                this._partySwings = partySwings
                onPropertyRefreshed(Property.SWINGS)
            }

            var range: Number
            get() = _range
            set(range) {
                this._range = range
                onPropertyRefreshed(Property.RANGE)
            }

            var seatLabelIncrement: Int
            get() = _seatLabelIncrement
            set(seatLabelIncrement) {
                this._seatLabelIncrement = seatLabelIncrement
                onPropertyRefreshed(Property.LABEL_INCREMENT)
            }
        }

        private val inputs = Inputs<T>()
        private val header: BindingReceiver<String?>

        fun withSeatLabelIncrements(incrementBinding: Binding<Int>): Builder<T> {
            incrementBinding.bind { inputs.seatLabelIncrement = it }
            return this
        }

        fun withSeatFilter(seatsFilterBinding: Binding<Set<T>?>): Builder<T> {
            seatsFilterBinding.bind { inputs.seatFilter = it }
            return this
        }

        fun build(title: Binding<String?>): SwingometerScreen {
            val headerLabel = JLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            title.bind { headerLabel.text = it }
            val swingometer = createSwingometer()
            return SwingometerScreen(headerLabel, swingometer)
        }

        private fun createSwingometer(): SwingometerFrame {
            val dotsList = createDotsForSwingometer()
            return SwingometerFrameBuilder.basic(
                    Binding.propertyBinding(
                            inputs,
                            { inputs: Inputs<T> ->
                                ImmutablePair.of(
                                        inputs.parties.left.color, inputs.parties.right.color)
                            },
                            Inputs.Property.PARTIES),
                    Binding.propertyBinding(
                            inputs,
                            { inputs: Inputs<T> ->
                                val left = inputs.partySwings[inputs.parties.left] ?: 0.0
                                val right = inputs.partySwings[inputs.parties.right] ?: 0.0
                                (right - left) / 2
                            },
                            Inputs.Property.PARTIES,
                            Inputs.Property.SWINGS))
                    .withDotsSolid(dotsList, { obj -> obj.left }, { obj -> obj.middle }) { obj -> obj.right }
                    .withHeader(header.getBinding())
                    .withRange(Binding.propertyBinding(inputs, { inputs: Inputs<T> -> inputs.range }, Inputs.Property.RANGE))
                    .withTickInterval(Binding.fixedBinding(0.01)) { n: Number -> (n.toDouble() * 100).roundToInt().toString() }
                    .withLeftNeedingToWin(
                            Binding.propertyBinding(
                                    inputs,
                                    { inputs: Inputs<T> ->
                                        getSwingNeededForMajority(
                                                inputs.prevVotes, inputs.parties.left, inputs.parties.right)
                                    },
                                    Inputs.Property.PREV,
                                    Inputs.Property.PARTIES))
                    .withRightNeedingToWin(
                            Binding.propertyBinding(
                                    inputs,
                                    { inputs: Inputs<T> ->
                                        getSwingNeededForMajority(
                                                inputs.prevVotes, inputs.parties.right, inputs.parties.left)
                                    },
                                    Inputs.Property.PREV,
                                    Inputs.Property.PARTIES))
                    .withBucketSize(Binding.fixedBinding(0.005))
                    .withOuterLabels(outerLabels, { obj -> obj.left }, { obj -> obj.right }) { obj -> obj.middle }
                    .build()
        }

        private val outerLabels: BindableList<Triple<Double, Color, String>>
            get() {
                val labels = BindableList<Triple<Double, Color, String>>()
                val binding: Binding<List<Triple<Double, Color, String>>> = Binding.propertyBinding(
                        inputs,
                        { inputs: Inputs<T> ->
                            val leftSwingList = createSwingList(
                                    inputs.prevVotes.values, inputs.parties.left, inputs.parties.right)
                            val rightSwingList = createSwingList(
                                    inputs.prevVotes.values, inputs.parties.right, inputs.parties.left)
                            val ret: MutableList<Triple<Double, Color, String>> = LinkedList()
                            val leftSeats = getNumSeats(leftSwingList)
                            val rightSeats = getNumSeats(rightSwingList)
                            val majority = inputs.prevVotes.size / 2 + 1
                            addZeroLabel(ret, inputs.parties, leftSeats, rightSeats)
                            addMajorityLabels(ret, inputs.parties, leftSwingList, rightSwingList, majority)
                            addLeadChangeLabel(
                                    ret, inputs.parties, leftSwingList, rightSwingList, leftSeats, rightSeats)
                            addIncrementLabels(
                                    ret,
                                    leftSwingList,
                                    rightSwingList,
                                    leftSeats,
                                    rightSeats,
                                    inputs.prevVotes,
                                    inputs.seatLabelIncrement,
                                    inputs.parties)
                            filterNearbyLabels(ret)
                            ret
                        },
                        Inputs.Property.PREV,
                        Inputs.Property.PARTIES,
                        Inputs.Property.LABEL_INCREMENT)
                binding.bind { labels.setAll(it) }
                return labels
            }

        private fun addIncrementLabels(
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
                            ImmutableTriple.of(
                                    -leftSwingList[i - 1], parties.left.color, i.toString()))
                }
                if (i <= rightSwingList.size) {
                    list.add(
                            ImmutableTriple.of(
                                    rightSwingList[i - 1], parties.right.color, i.toString()))
                }
                i += seatLabelIncrement
            }
        }

        private fun filterNearbyLabels(ret: MutableList<Triple<Double, Color, String>>) {
            val ranges: MutableSet<Range<Double>> = HashSet()
            val it = ret.iterator()
            while (it.hasNext()) {
                val item = it.next()
                if (ranges.any { range -> range.contains(item.left) }) {
                    it.remove()
                } else {
                    ranges.add(Range.between(item.left - 0.005, item.left + 0.005))
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
                    color = if ((leftSeats + rightSeats) % 2 == 0) Color.BLACK else parties.right.color
                } else {
                    swing = -1 *
                            (leftSwingList
                            .drop(newLeadSeats - 1)
                            .firstOrNull()
                            ?: Double.POSITIVE_INFINITY)
                    color = if ((leftSeats + rightSeats) % 2 == 0) Color.BLACK else parties.left.color
                }
                list.add(ImmutableTriple.of(swing, color, newLeadSeats.toString()))
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
                    (leftSwingList
                    .drop((majority - 1))
                    .firstOrNull()
                            ?: Double.POSITIVE_INFINITY)
            val rightMajority = rightSwingList.drop((majority - 1)).firstOrNull() ?: Double.POSITIVE_INFINITY
            if (leftMajority != rightMajority || leftMajority < 0) {
                list.add(
                        ImmutableTriple.of(
                                leftMajority, parties.left.color, majority.toString()))
            }
            if (leftMajority != rightMajority || rightMajority > 0) {
                list.add(
                        ImmutableTriple.of(
                                rightMajority, parties.right.color, majority.toString()))
            }
        }

        private fun addZeroLabel(
            list: MutableList<Triple<Double, Color, String>>,
            parties: Pair<Party, Party>,
            leftSeats: Int,
            rightSeats: Int
        ) {
            list.add(when {
                leftSeats > rightSeats -> ImmutableTriple.of(0.0, parties.left.color, leftSeats.toString())
                rightSeats > leftSeats -> ImmutableTriple.of(0.0, parties.right.color, rightSeats.toString())
                else -> ImmutableTriple.of(0.0, Color.BLACK, rightSeats.toString())
            })
        }

        private fun getNumSeats(swings: List<Double>): Int {
            return swings.filter { s: Double -> s < 0 }.count()
        }

        private fun getSwingNeededForMajority(
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

        private fun createDotsForSwingometer(): BindableList<Triple<Double, Color, Boolean>> {
            val emptyResult = PartyResult(null, false)
            val dotsBinding: Binding<List<Triple<Double, Color, Boolean>>> = Binding.propertyBinding(
                    inputs,
                    { inputs: Inputs<T> ->
                        inputs.prevVotes.entries.asSequence()
                                .map { e ->
                                    ImmutableTriple.of(
                                            e.value,
                                            inputs.results[e.key] ?: emptyResult,
                                            inputs.seatFilter?.contains(e.key) ?: true)
                                }
                                .filter { e ->
                                    val winner = e.getLeft().entries
                                            .maxByOrNull { it.value }!!
                                            .key
                                    winner == inputs.parties.left || winner == inputs.parties.right
                                }
                                .map { e ->
                                    val total = e.getLeft().values.sum()
                                    val left = e.getLeft()[inputs.parties.left] ?: 0
                                    val right = e.getLeft()[inputs.parties.right] ?: 0
                                    ImmutableTriple.of(
                                            0.5 * (left - right) / total,
                                            (e.getMiddle() ?: PartyResult.NO_RESULT).color,
                                            e.getRight())
                                }
                                .toList()
                    },
                    Inputs.Property.PREV,
                    Inputs.Property.RESULTS,
                    Inputs.Property.PARTIES,
                    Inputs.Property.FILTERED_SEATS)
            val dotsList = BindableList<Triple<Double, Color, Boolean>>()
            dotsBinding.bind { dotsList.setAll(it) }
            return dotsList
        }

        init {
            prevVotesBinding.bind { inputs.prevVotes = it }
            resultsBinding.bind { inputs.results = it }
            partiesBinding.bind { inputs.parties = it }
            partySwingsBinding.bind { inputs.partySwings = it }
            header = BindingReceiver(headerBinding)
        }
    }

    companion object {
        @JvmStatic fun <T> of(
            prevVotes: Binding<Map<T, Map<Party, Int>>>,
            results: Binding<Map<T, PartyResult?>>,
            swing: Binding<Map<Party, Double>>,
            parties: Binding<Pair<Party, Party>>,
            header: Binding<String?>
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
