package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.text.DecimalFormat
import java.util.ArrayList
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class RegionalBreakdownScreen private constructor(titleLabel: JLabel, multiSummaryFrame: MultiSummaryFrame) : JPanel() {
    interface Entry {
        val headerPublisher: Flow.Publisher<out String>
        val valuePublisher: Flow.Publisher<out List<Pair<Color, String>>>
    }

    private class BlankEntry : Entry {
        override val headerPublisher: Flow.Publisher<out String>
            get() = "".asOneTimePublisher()
        override val valuePublisher: Flow.Publisher<out List<Pair<Color, String>>>
            get() = listOf<Pair<Color, String>>().asOneTimePublisher()
    }

    private open class SeatEntry : Entry {
        enum class Property

        private var _partyOrder: List<Party> = emptyList()
        private var _name = ""
        private var _seats: Map<Party, Int> = emptyMap()
        private var _totalSeats = 0

        var partyOrder: List<Party>
            get() = _partyOrder
            set(partyOrder) {
                _partyOrder = partyOrder
                updateValue()
            }
        val partyOrderPublisher = Publisher(_partyOrder)

        var name: String
            get() = _name
            set(name) {
                _name = name
                namePublisher.submit(name)
            }

        var seats: Map<Party, Int>
            get() = _seats
            set(seats) {
                _seats = seats
                updateValue()
            }

        var totalSeats: Int
            get() = _totalSeats
            set(totalSeats) {
                _totalSeats = totalSeats
                updateValue()
            }

        private val namePublisher = Publisher(_name)
        override val headerPublisher: Flow.Publisher<out String>
            get() = namePublisher

        private val _valuePublisher = Publisher(calculateValue())
        override val valuePublisher: Flow.Publisher<out List<Pair<Color, String>>>
            get() = _valuePublisher
        protected fun updateValue() = synchronized(this) { _valuePublisher.submit(calculateValue()) }

        private fun calculateValue(): MutableList<Pair<Color, String>> {
            val ret: MutableList<Pair<Color, String>> = this.partyOrder.map { this.getPartyLabel(it) }.toMutableList()
            ret.add(
                Pair(
                    Color.WHITE,
                    seats.values.sum().toString() + "/" + totalSeats
                )
            )
            return ret
        }

        protected open fun getPartyLabel(party: Party): Pair<Color, String> {
            return Pair(party.color, (seats[party] ?: 0).toString())
        }
    }

    private class SeatDiffEntry : SeatEntry() {
        private var _diff: Map<Party, Int> = emptyMap()

        var diff: Map<Party, Int>
            get() = _diff
            set(diff) {
                _diff = diff
                updateValue()
            }

        override fun getPartyLabel(party: Party): Pair<Color, String> {
            val seats = seats[party] ?: 0
            val diff = diff[party] ?: 0
            return Pair(
                party.color,
                seats.toString() + " (" + (if (diff == 0) "\u00b10" else DIFF_FORMAT.format(diff.toLong())) + ")"
            )
        }

        companion object {
            private val DIFF_FORMAT = DecimalFormat("+0;-0")
        }
    }

    private class SeatPrevEntry : SeatEntry() {
        private var _prev: Map<Party, Int> = emptyMap()

        var prev: Map<Party, Int>
            get() = _prev
            set(prev) {
                _prev = prev
                updateValue()
            }

        override fun getPartyLabel(party: Party): Pair<Color, String> {
            val seats = seats[party] ?: 0
            val diff = seats - (prev[party] ?: 0)
            return Pair(
                party.color,
                seats.toString() + " (" + (if (diff == 0) "\u00b10" else DIFF_FORMAT.format(diff.toLong())) + ")"
            )
        }

        companion object {
            private val DIFF_FORMAT = DecimalFormat("+0;-0")
        }
    }

    open class MultiPartyResultBuilder(
        titlePublisher: Flow.Publisher<out String>
    ) {
        protected val title: Flow.Publisher<out String> = titlePublisher
        protected val entries: MutableList<Entry> = ArrayList()
        protected var partyOrder: Flow.Publisher<out List<Party>>? = null

        fun build(titlePublisher: Flow.Publisher<out String?>): RegionalBreakdownScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titlePublisher.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            return RegionalBreakdownScreen(headerLabel, createFrame())
        }

        private fun createFrame(): MultiSummaryFrame {
            return MultiSummaryFrame(
                headerPublisher = title,
                rowsPublisher =
                entries.map {
                    it.headerPublisher.merge(it.valuePublisher) { h, v -> MultiSummaryFrame.Row(h, v) }
                }
                    .combine()
            )
        }
    }

    class SeatBuilder(
        totalHeaderPublisher: Flow.Publisher<out String>,
        totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
        numTotalSeatsPublisher: Flow.Publisher<out Int>,
        titlePublisher: Flow.Publisher<out String>
    ) : MultiPartyResultBuilder(titlePublisher) {

        fun withBlankRow(): SeatBuilder {
            entries.add(BlankEntry())
            return this
        }

        fun withRegion(
            namePublisher: Flow.Publisher<out String>,
            seatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            numSeatsPublisher: Flow.Publisher<out Int>,
            partyMapPublisher: Flow.Publisher<out Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher()
        ): SeatBuilder {
            val newEntry = SeatEntry()
            transformPartyOrder(partyOrder!!, partyMapPublisher).subscribe(Subscriber { newEntry.partyOrder = it })
            namePublisher.subscribe(Subscriber { newEntry.name = it })
            seatsPublisher.subscribe(Subscriber { newEntry.seats = it })
            numSeatsPublisher.subscribe(Subscriber { newEntry.totalSeats = it })
            entries.add(newEntry)
            return this
        }

        init {
            partyOrder = totalSeatsPublisher.map { result: Map<Party, Int> -> extractPartyOrder(result) }
            val topEntry = SeatEntry()
            partyOrder!!.subscribe(Subscriber { topEntry.partyOrder = it })
            totalHeaderPublisher.subscribe(Subscriber { topEntry.name = it })
            totalSeatsPublisher.subscribe(Subscriber { topEntry.seats = it })
            numTotalSeatsPublisher.subscribe(Subscriber { topEntry.totalSeats = it })
            entries.add(topEntry)
        }
    }

    class SeatDiffBuilder(
        totalHeaderPublisher: Flow.Publisher<out String>,
        totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
        seatDiffPublisher: Flow.Publisher<out Map<Party, Int>>,
        numTotalSeatsPublisher: Flow.Publisher<out Int>,
        titlePublisher: Flow.Publisher<out String>
    ) : MultiPartyResultBuilder(titlePublisher) {
        fun withBlankRow(): SeatDiffBuilder {
            entries.add(BlankEntry())
            return this
        }

        fun withRegion(
            namePublisher: Flow.Publisher<out String>,
            seatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            diffPublisher: Flow.Publisher<out Map<Party, Int>>,
            numSeatsPublisher: Flow.Publisher<out Int>,
            partyMapPublisher: Flow.Publisher<out Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher()
        ): SeatDiffBuilder {
            val newEntry = SeatDiffEntry()
            transformPartyOrder(partyOrder!!, partyMapPublisher).subscribe(Subscriber { newEntry.partyOrder = it })
            namePublisher.subscribe(Subscriber { newEntry.name = it })
            seatsPublisher.subscribe(Subscriber { newEntry.seats = it })
            diffPublisher.subscribe(Subscriber { newEntry.diff = it })
            numSeatsPublisher.subscribe(Subscriber { newEntry.totalSeats = it })
            entries.add(newEntry)
            return this
        }

        init {
            partyOrder =
                totalSeatsPublisher
                    .merge(seatDiffPublisher) { result, diff -> extractPartyOrder(result, diff) }
            val topEntry = SeatDiffEntry()
            partyOrder!!.subscribe(Subscriber { topEntry.partyOrder = it })
            totalHeaderPublisher.subscribe(Subscriber { topEntry.name = it })
            totalSeatsPublisher.subscribe(Subscriber { topEntry.seats = it })
            seatDiffPublisher.subscribe(Subscriber { topEntry.diff = it })
            numTotalSeatsPublisher.subscribe(Subscriber { topEntry.totalSeats = it })
            entries.add(topEntry)
        }
    }

    class SeatPrevBuilder(
        totalHeaderPublisher: Flow.Publisher<out String>,
        totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
        prevSeatPublisher: Flow.Publisher<out Map<Party, Int>>,
        numTotalSeatsPublisher: Flow.Publisher<out Int>,
        titlePublisher: Flow.Publisher<out String>
    ) : MultiPartyResultBuilder(titlePublisher) {

        fun withBlankRow(): SeatPrevBuilder {
            entries.add(BlankEntry())
            return this
        }

        fun withRegion(
            namePublisher: Flow.Publisher<out String>,
            seatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            prevPublisher: Flow.Publisher<out Map<Party, Int>>,
            numSeatsPublisher: Flow.Publisher<out Int>,
            partyMapPublisher: Flow.Publisher<out Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher()
        ): SeatPrevBuilder {
            val newEntry = SeatPrevEntry()
            transformPartyOrder(partyOrder!!, partyMapPublisher).subscribe(Subscriber { newEntry.partyOrder = it })
            namePublisher.subscribe(Subscriber { newEntry.name = it })
            seatsPublisher.subscribe(Subscriber { newEntry.seats = it })
            prevPublisher.subscribe(Subscriber { newEntry.prev = it })
            numSeatsPublisher.subscribe(Subscriber { newEntry.totalSeats = it })
            entries.add(newEntry)
            return this
        }

        init {
            partyOrder =
                totalSeatsPublisher
                    .merge(
                        prevSeatPublisher
                    ) { result: Map<Party, Int>, diff: Map<Party, Int> -> extractPartyOrder(result, diff) }
            val topEntry = SeatPrevEntry()
            partyOrder!!.subscribe(Subscriber { topEntry.partyOrder = it })
            totalHeaderPublisher.subscribe(Subscriber { topEntry.name = it })
            totalSeatsPublisher.subscribe(Subscriber { topEntry.seats = it })
            prevSeatPublisher.subscribe(Subscriber { topEntry.prev = it })
            numTotalSeatsPublisher.subscribe(Subscriber { topEntry.totalSeats = it })
            entries.add(topEntry)
        }
    }

    companion object {
        @JvmStatic fun seats(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            numTotalSeatsPublisher: Flow.Publisher<out Int>,
            titlePublisher: Flow.Publisher<out String>
        ): SeatBuilder {
            return SeatBuilder(
                totalHeaderPublisher, totalSeatsPublisher, numTotalSeatsPublisher, titlePublisher
            )
        }

        @JvmStatic fun seatsWithDiff(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            seatDiffPublisher: Flow.Publisher<out Map<Party, Int>>,
            numTotalSeatsPublisher: Flow.Publisher<out Int>,
            titlePublisher: Flow.Publisher<out String>
        ): SeatDiffBuilder {
            return SeatDiffBuilder(
                totalHeaderPublisher, totalSeatsPublisher, seatDiffPublisher, numTotalSeatsPublisher, titlePublisher
            )
        }

        @JvmStatic fun seatsWithPrev(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            prevSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            numTotalSeatsPublisher: Flow.Publisher<out Int>,
            titlePublisher: Flow.Publisher<out String>
        ): SeatPrevBuilder {
            return SeatPrevBuilder(
                totalHeaderPublisher,
                totalSeatsPublisher,
                prevSeatsPublisher,
                numTotalSeatsPublisher,
                titlePublisher
            )
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

        private fun transformPartyOrder(
            partyOrder: Flow.Publisher<out List<Party>>,
            partyMapping: Flow.Publisher<out Map<Party, Party>>
        ): Flow.Publisher<out List<Party>> =
            partyOrder.merge(partyMapping) { po, pm -> po.map { pm[it] ?: it } }
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