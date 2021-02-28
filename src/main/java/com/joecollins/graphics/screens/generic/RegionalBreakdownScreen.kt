package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Party
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.text.DecimalFormat
import java.util.ArrayList
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair

class RegionalBreakdownScreen private constructor(titleLabel: JLabel, multiSummaryFrame: MultiSummaryFrame) : JPanel() {
    interface Entry {
        val headerBinding: Binding<String>
        val valueBinding: Binding<List<Pair<Color, String>>>
    }

    private class BlankEntry : Entry {
        override val headerBinding: Binding<String>
            get() = Binding.fixedBinding("")
        override val valueBinding: Binding<List<Pair<Color, String>>>
            get() = Binding.fixedBinding(listOf())
    }

    private open class SeatEntry : Bindable<SeatEntry, SeatEntry.Property>(), Entry {
        enum class Property {
            PARTY_ORDER, NAME, SEATS, TOTAL_SEATS
        }

        private var _partyOrder: List<Party> = emptyList()
        private var _name = ""
        private var _seats: Map<Party, Int> = emptyMap()
        private var _totalSeats = 0

        var partyOrder: List<Party>
        get() = _partyOrder
        set(partyOrder) {
            _partyOrder = partyOrder
            onPropertyRefreshed(Property.PARTY_ORDER)
        }

        var name: String
        get() = _name
        set(name) {
            _name = name
            onPropertyRefreshed(Property.NAME)
        }

        var seats: Map<Party, Int>
        get() = _seats
        set(seats) {
            _seats = seats
            onPropertyRefreshed(Property.SEATS)
        }

        var totalSeats: Int
        get() = _totalSeats
        set(totalSeats) {
            _totalSeats = totalSeats
            onPropertyRefreshed(Property.TOTAL_SEATS)
        }

        override val headerBinding: Binding<String>
            get() = Binding.propertyBinding(this, { t: SeatEntry -> t.name }, Property.NAME)
        override val valueBinding: Binding<List<Pair<Color, String>>>
            get() = Binding.propertyBinding(
                    this,
                    { t: SeatEntry ->
                        val ret: MutableList<Pair<Color, String>> = t.partyOrder.map { t.getPartyLabel(it) }.toMutableList()
                        ret.add(
                                ImmutablePair.of(
                                        Color.WHITE,
                                        seats.values.sum().toString() + "/" + totalSeats))
                        ret
                    },
                    Property.PARTY_ORDER,
                    Property.SEATS,
                    Property.TOTAL_SEATS)

        protected open fun getPartyLabel(party: Party): Pair<Color, String> {
            return ImmutablePair.of(party.color, (seats[party] ?: 0).toString())
        }
    }

    private class SeatDiffEntry : SeatEntry() {
        private var _diff: Map<Party, Int> = java.util.Map.of()

        var diff: Map<Party, Int>
        get() = _diff
        set(diff) {
            _diff = diff
            onPropertyRefreshed(Property.SEATS)
        }

        override fun getPartyLabel(party: Party): ImmutablePair<Color, String> {
            val seats = seats[party] ?: 0
            val diff = diff[party] ?: 0
            return ImmutablePair.of(
                    party.color,
                    seats.toString() + " (" + (if (diff == 0) "\u00b10" else DIFF_FORMAT.format(diff.toLong())) + ")")
        }

        companion object {
            private val DIFF_FORMAT = DecimalFormat("+0;-0")
        }
    }

    private class SeatPrevEntry : SeatEntry() {
        private var _prev: Map<Party, Int> = java.util.Map.of()

        var prev: Map<Party, Int>
        get() = _prev
        set(prev) {
            _prev = prev
            onPropertyRefreshed(Property.SEATS)
        }

        override fun getPartyLabel(party: Party): ImmutablePair<Color, String> {
            val seats = seats[party] ?: 0
            val diff = seats - (prev[party] ?: 0)
            return ImmutablePair.of(
                    party.color,
                    seats.toString() + " (" + (if (diff == 0) "\u00b10" else DIFF_FORMAT.format(diff.toLong())) + ")")
        }

        companion object {
            private val DIFF_FORMAT = DecimalFormat("+0;-0")
        }
    }

    open class MultiPartyResultBuilder(
        titleBinding: Binding<String>
    ) {
        protected val title: BindingReceiver<String> = BindingReceiver(titleBinding)
        protected val entries: MutableList<Entry> = ArrayList()
        protected var partyOrder: BindingReceiver<List<Party>>? = null

        fun build(titleBinding: Binding<String?>): RegionalBreakdownScreen {
            val headerLabel = JLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titleBinding.bind { headerLabel.text = it }
            return RegionalBreakdownScreen(headerLabel, createFrame())
        }

        private fun createFrame(): MultiSummaryFrame {
            val frame = MultiSummaryFrame()
            frame.setHeaderBinding(title.getBinding())
            frame.setNumRowsBinding(Binding.fixedBinding(entries.size))
            frame.setRowHeaderBinding(IndexedBinding.listBinding(entries) { obj: Entry -> obj.headerBinding })
            frame.setValuesBinding(IndexedBinding.listBinding(entries) { obj: Entry -> obj.valueBinding })
            return frame
        }
    }

    class SeatBuilder(
        totalHeaderBinding: Binding<String>,
        totalSeatsBinding: Binding<Map<Party, Int>>,
        numTotalSeatsBinding: Binding<Int>,
        titleBinding: Binding<String>
    ) : MultiPartyResultBuilder(titleBinding) {

        fun withBlankRow(): SeatBuilder {
            entries.add(BlankEntry())
            return this
        }

        fun withRegion(
            nameBinding: Binding<String>,
            seatsBinding: Binding<Map<Party, Int>>,
            numSeatsBinding: Binding<Int>
        ): SeatBuilder {
            val newEntry = SeatEntry()
            partyOrder!!.getBinding().bind { newEntry.partyOrder = it }
            nameBinding.bind { newEntry.name = it }
            seatsBinding.bind { newEntry.seats = it }
            numSeatsBinding.bind { newEntry.totalSeats = it }
            entries.add(newEntry)
            return this
        }

        init {
            val totalSeats = BindingReceiver(totalSeatsBinding)
            partyOrder = BindingReceiver(totalSeats.getBinding { result: Map<Party, Int> -> extractPartyOrder(result) })
            val topEntry = SeatEntry()
            partyOrder!!.getBinding().bind { topEntry.partyOrder = it }
            totalHeaderBinding.bind { topEntry.name = it }
            totalSeats.getBinding().bind { topEntry.seats = it }
            numTotalSeatsBinding.bind { topEntry.totalSeats = it }
            entries.add(topEntry)
        }
    }

    class SeatDiffBuilder(
        totalHeaderBinding: Binding<String>,
        totalSeatsBinding: Binding<Map<Party, Int>>,
        seatDiffBinding: Binding<Map<Party, Int>>,
        numTotalSeatsBinding: Binding<Int>,
        titleBinding: Binding<String>
    ) : MultiPartyResultBuilder(titleBinding) {
        fun withBlankRow(): SeatDiffBuilder {
            entries.add(BlankEntry())
            return this
        }

        fun withRegion(
            nameBinding: Binding<String>,
            seatsBinding: Binding<Map<Party, Int>>,
            diffBinding: Binding<Map<Party, Int>>,
            numSeatsBinding: Binding<Int>
        ): SeatDiffBuilder {
            val newEntry = SeatDiffEntry()
            partyOrder!!.getBinding().bind { newEntry.partyOrder = it }
            nameBinding.bind { newEntry.name = it }
            seatsBinding.bind { newEntry.seats = it }
            diffBinding.bind { newEntry.diff = it }
            numSeatsBinding.bind { newEntry.totalSeats = it }
            entries.add(newEntry)
            return this
        }

        init {
            val totalSeats = BindingReceiver(totalSeatsBinding)
            val seatDiff = BindingReceiver(seatDiffBinding)
            partyOrder = BindingReceiver(
                    totalSeats
                            .getBinding()
                            .merge(seatDiff.getBinding()) { result, diff -> extractPartyOrder(result, diff) })
            val topEntry = SeatDiffEntry()
            partyOrder!!.getBinding().bind { topEntry.partyOrder = it }
            totalHeaderBinding.bind { topEntry.name = it }
            totalSeats.getBinding().bind { topEntry.seats = it }
            seatDiff.getBinding().bind { topEntry.diff = it }
            numTotalSeatsBinding.bind { topEntry.totalSeats = it }
            entries.add(topEntry)
        }
    }

    class SeatPrevBuilder(
        totalHeaderBinding: Binding<String>,
        totalSeatsBinding: Binding<Map<Party, Int>>,
        prevSeatBinding: Binding<Map<Party, Int>>,
        numTotalSeatsBinding: Binding<Int>,
        titleBinding: Binding<String>
    ) : MultiPartyResultBuilder(titleBinding) {

        fun withBlankRow(): SeatPrevBuilder {
            entries.add(BlankEntry())
            return this
        }

        fun withRegion(
            nameBinding: Binding<String>,
            seatsBinding: Binding<Map<Party, Int>>,
            prevBinding: Binding<Map<Party, Int>>,
            numSeatsBinding: Binding<Int>
        ): SeatPrevBuilder {
            val newEntry = SeatPrevEntry()
            partyOrder!!.getBinding().bind { newEntry.partyOrder = it }
            nameBinding.bind { newEntry.name = it }
            seatsBinding.bind { newEntry.seats = it }
            prevBinding.bind { newEntry.prev = it }
            numSeatsBinding.bind { newEntry.totalSeats = it }
            entries.add(newEntry)
            return this
        }

        init {
            val totalSeats = BindingReceiver(totalSeatsBinding)
            val prevSeats = BindingReceiver(prevSeatBinding)
            partyOrder = BindingReceiver(
                    totalSeats
                            .getBinding()
                            .merge(prevSeats.getBinding()) { result: Map<Party, Int>, diff: Map<Party, Int> -> extractPartyOrder(result, diff) })
            val topEntry = SeatPrevEntry()
            partyOrder!!.getBinding().bind { topEntry.partyOrder = it }
            totalHeaderBinding.bind { topEntry.name = it }
            totalSeats.getBinding().bind { topEntry.seats = it }
            prevSeats.getBinding().bind { topEntry.prev = it }
            numTotalSeatsBinding.bind { topEntry.totalSeats = it }
            entries.add(topEntry)
        }
    }

    companion object {
        @JvmStatic fun seats(
            totalHeaderBinding: Binding<String>,
            totalSeatsBinding: Binding<Map<Party, Int>>,
            numTotalSeatsBinding: Binding<Int>,
            titleBinding: Binding<String>
        ): SeatBuilder {
            return SeatBuilder(
                    totalHeaderBinding, totalSeatsBinding, numTotalSeatsBinding, titleBinding)
        }

        @JvmStatic fun seatsWithDiff(
            totalHeaderBinding: Binding<String>,
            totalSeatsBinding: Binding<Map<Party, Int>>,
            seatDiffBinding: Binding<Map<Party, Int>>,
            numTotalSeatsBinding: Binding<Int>,
            titleBinding: Binding<String>
        ): SeatDiffBuilder {
            return SeatDiffBuilder(
                    totalHeaderBinding, totalSeatsBinding, seatDiffBinding, numTotalSeatsBinding, titleBinding)
        }

        @JvmStatic fun seatsWithPrev(
            totalHeaderBinding: Binding<String>,
            totalSeatsBinding: Binding<Map<Party, Int>>,
            prevSeatsBinding: Binding<Map<Party, Int>>,
            numTotalSeatsBinding: Binding<Int>,
            titleBinding: Binding<String>
        ): SeatPrevBuilder {
            return SeatPrevBuilder(
                    totalHeaderBinding,
                    totalSeatsBinding,
                    prevSeatsBinding,
                    numTotalSeatsBinding,
                    titleBinding)
        }

        private fun extractPartyOrder(result: Map<Party, Int>): List<Party> {
            return result.entries.asSequence()
                    .filter { it.value > 0 }
                    .sortedByDescending { it.value }
                    .map { it.key }
                    .toList()
        }

        private fun extractPartyOrder(
            result: Map<Party, Int>,
            diff: Map<Party, Int>
        ): List<Party> {
            return sequenceOf(result.keys.asSequence(), diff.keys.asSequence()).flatten()
                    .distinct()
                    .filter { party -> result[party] ?: 0 > 0 || diff[party] ?: 0 != 0 }
                    .sortedByDescending { party -> result[party] ?: 0 }
                    .toList()
        }
    }

    init {
        layout = BorderLayout()
        background = Color.WHITE
        add(titleLabel, BorderLayout.NORTH)
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.border = EmptyBorder(5, 5, 5, 5)
        panel.layout = GridLayout(1, 1)
        panel.add(multiSummaryFrame)
        add(panel, BorderLayout.CENTER)
    }
}
