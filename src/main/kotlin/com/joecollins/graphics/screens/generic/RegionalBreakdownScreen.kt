package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Party
import com.joecollins.models.general.PollsReporting
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow

class RegionalBreakdownScreen private constructor(titleLabel: Flow.Publisher<out String?>, multiSummaryFrame: MultiSummaryFrame) : GenericPanel(pad(multiSummaryFrame), titleLabel) {
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
        var partyOrder: List<Party> = emptyList()
            set(value) {
                field = value
                updateValue()
            }

        var name = ""
            set(value) {
                field = value
                namePublisher.submit(value)
            }

        var seats: Map<Party, Int> = emptyMap()
            set(value) {
                field = value
                updateValue()
            }

        var totalSeats = 0
            set(value) {
                field = value
                updateValue()
            }

        val filteredSeats get() = Aggregators.adjustKey(seats) { p -> if (partyOrder.contains(p)) p else Party.OTHERS }

        val partyOrderPublisher = Publisher(partyOrder)
        private val namePublisher = Publisher(name)
        override val headerPublisher: Flow.Publisher<out String>
            get() = namePublisher

        override val valuePublisher = Publisher<List<Pair<Color, String>>>()
        protected fun updateValue() = synchronized(this) { valuePublisher.submit(calculateValue()) }

        private fun calculateValue(): List<Pair<Color, String>> {
            return sequenceOf(
                this.partyOrder.asSequence().map { this.getPartyLabel(it) },
                sequenceOf(Color.WHITE to (seats.values.sum().toString() + "/" + totalSeats))
            ).flatten().toList()
        }

        protected open fun getPartyLabel(party: Party): Pair<Color, String> {
            return Pair(party.color, (filteredSeats[party] ?: 0).toString())
        }
    }

    private class SeatDiffEntry : SeatEntry() {
        var diff: Map<Party, Int> = emptyMap()
            set(value) {
                field = value
                updateValue()
            }

        val filteredDiff get() = Aggregators.adjustKey(diff) { p -> if (partyOrder.contains(p)) p else Party.OTHERS }

        override fun getPartyLabel(party: Party): Pair<Color, String> {
            val seats = filteredSeats[party] ?: 0
            val diff = filteredDiff[party] ?: 0
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
        var prev: Map<Party, Int> = emptyMap()
            set(value) {
                field = value
                updateValue()
            }

        val filteredPrev get() = Aggregators.adjustKey(prev) { p -> if (partyOrder.contains(p)) p else Party.OTHERS }

        override fun getPartyLabel(party: Party): Pair<Color, String> {
            val seats = filteredSeats[party] ?: 0
            val diff = seats - (filteredPrev[party] ?: 0)
            return Pair(
                party.color,
                seats.toString() + " (" + (if (diff == 0) "\u00b10" else DIFF_FORMAT.format(diff.toLong())) + ")"
            )
        }

        companion object {
            private val DIFF_FORMAT = DecimalFormat("+0;-0")
        }
    }

    private open class VoteEntry : Entry {
        var partyOrder: List<Party> = emptyList()
            set(value) {
                field = value
                updateValue()
            }

        var name = ""
            set(value) {
                field = value
                namePublisher.submit(value)
            }

        var votes: Map<Party, Int> = emptyMap()
            set(value) {
                field = value
                updateValue()
            }

        var reporting = ""
            set(value) {
                field = value
                updateValue()
            }

        val filteredVotes get() = Aggregators.adjustKey(votes) { p -> if (partyOrder.contains(p)) p else Party.OTHERS }

        val partyOrderPublisher = Publisher(partyOrder)
        private val namePublisher = Publisher(name)
        override val headerPublisher: Flow.Publisher<out String>
            get() = namePublisher

        override val valuePublisher = Publisher<List<Pair<Color, String>>>()
        protected fun updateValue() = synchronized(this) { valuePublisher.submit(calculateValue()) }

        private fun calculateValue(): List<Pair<Color, String>> {
            return sequenceOf(
                this.partyOrder.asSequence().map { this.getPartyLabel(it) },
                sequenceOf(Color.WHITE to reporting)
            ).flatten().toList()
        }

        protected open fun getPartyLabel(party: Party): Pair<Color, String> {
            return Pair(party.color, PCT_FORMAT.format((filteredVotes[party] ?: 0) / votes.values.sum().coerceAtLeast(1).toDouble()))
        }

        companion object {
            private val PCT_FORMAT = DecimalFormat("0.0%")
        }
    }

    private class VotePrevEntry : VoteEntry() {
        var prev: Map<Party, Int> = emptyMap()
            set(value) {
                field = value
                updateValue()
            }

        val filteredPrev get() = Aggregators.adjustKey(prev) { p -> if (partyOrder.contains(p)) p else Party.OTHERS }

        override fun getPartyLabel(party: Party): Pair<Color, String> {
            val votes = (filteredVotes[party] ?: 0) / votes.values.sum().coerceAtLeast(1).toDouble()
            val diff = votes - (filteredPrev[party] ?: 0) / prev.values.sum().coerceAtLeast(1).toDouble()
            return Pair(
                party.color,
                PCT_FORMAT.format(votes) + " (" + (if (diff == 0.0) "\u00b10.0" else DIFF_FORMAT.format(diff * 100)) + ")"
            )
        }

        companion object {
            private val PCT_FORMAT = DecimalFormat("0.0%")
            private val DIFF_FORMAT = DecimalFormat("+0.0;-0.0")
        }
    }

    open class MultiPartyResultBuilder protected constructor(
        titlePublisher: Flow.Publisher<out String>,
        protected val maxColumnsPublisher: Flow.Publisher<Int?>
    ) {
        protected val title: Flow.Publisher<out String> = titlePublisher
        protected val entries: MutableList<Entry> = ArrayList()
        protected var partyOrder: Flow.Publisher<out List<Party>>? = null

        fun build(titlePublisher: Flow.Publisher<out String?>): RegionalBreakdownScreen {
            return RegionalBreakdownScreen(titlePublisher, createFrame())
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

    class SeatBuilder internal constructor(
        totalHeaderPublisher: Flow.Publisher<out String>,
        totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
        numTotalSeatsPublisher: Flow.Publisher<out Int>,
        titlePublisher: Flow.Publisher<out String>,
        maxColumnsPublisher: Flow.Publisher<Int?>
    ) : MultiPartyResultBuilder(titlePublisher, maxColumnsPublisher) {

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
            partyOrder = totalSeatsPublisher.map { result -> extractPartyOrder(result) }
                .merge(maxColumnsPublisher) { parties, max -> takeTopParties(parties, max) }
            val topEntry = SeatEntry()
            partyOrder!!.subscribe(Subscriber { topEntry.partyOrder = it })
            totalHeaderPublisher.subscribe(Subscriber { topEntry.name = it })
            totalSeatsPublisher.subscribe(Subscriber { topEntry.seats = it })
            numTotalSeatsPublisher.subscribe(Subscriber { topEntry.totalSeats = it })
            entries.add(topEntry)
        }
    }

    class SeatDiffBuilder internal constructor(
        totalHeaderPublisher: Flow.Publisher<out String>,
        totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
        seatDiffPublisher: Flow.Publisher<out Map<Party, Int>>,
        numTotalSeatsPublisher: Flow.Publisher<out Int>,
        titlePublisher: Flow.Publisher<out String>,
        maxColumnsPublisher: Flow.Publisher<Int?>
    ) : MultiPartyResultBuilder(titlePublisher, maxColumnsPublisher) {
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
                    .merge(maxColumnsPublisher) { parties, max -> takeTopParties(parties, max) }
            val topEntry = SeatDiffEntry()
            partyOrder!!.subscribe(Subscriber { topEntry.partyOrder = it })
            totalHeaderPublisher.subscribe(Subscriber { topEntry.name = it })
            totalSeatsPublisher.subscribe(Subscriber { topEntry.seats = it })
            seatDiffPublisher.subscribe(Subscriber { topEntry.diff = it })
            numTotalSeatsPublisher.subscribe(Subscriber { topEntry.totalSeats = it })
            entries.add(topEntry)
        }
    }

    class SeatPrevBuilder internal constructor(
        totalHeaderPublisher: Flow.Publisher<out String>,
        totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
        prevSeatPublisher: Flow.Publisher<out Map<Party, Int>>,
        numTotalSeatsPublisher: Flow.Publisher<out Int>,
        titlePublisher: Flow.Publisher<out String>,
        maxColumnsPublisher: Flow.Publisher<Int?>
    ) : MultiPartyResultBuilder(titlePublisher, maxColumnsPublisher) {

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
                    ) { result, diff -> extractPartyOrder(result, diff) }
                    .merge(maxColumnsPublisher) { parties, max -> takeTopParties(parties, max) }
            val topEntry = SeatPrevEntry()
            partyOrder!!.subscribe(Subscriber { topEntry.partyOrder = it })
            totalHeaderPublisher.subscribe(Subscriber { topEntry.name = it })
            totalSeatsPublisher.subscribe(Subscriber { topEntry.seats = it })
            prevSeatPublisher.subscribe(Subscriber { topEntry.prev = it })
            numTotalSeatsPublisher.subscribe(Subscriber { topEntry.totalSeats = it })
            entries.add(topEntry)
        }
    }

    class VoteBuilder<R> internal constructor(
        totalHeaderPublisher: Flow.Publisher<out String>,
        totalVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
        reportingPublisher: Flow.Publisher<out R>,
        titlePublisher: Flow.Publisher<out String>,
        maxColumnsPublisher: Flow.Publisher<Int?>,
        private val reportingFunc: (R) -> String
    ) : MultiPartyResultBuilder(titlePublisher, maxColumnsPublisher) {

        fun withBlankRow(): VoteBuilder<R> {
            entries.add(BlankEntry())
            return this
        }

        fun withRegion(
            namePublisher: Flow.Publisher<out String>,
            votesPublisher: Flow.Publisher<out Map<Party, Int>>,
            pctReportingPublisher: Flow.Publisher<out R>,
            partyMapPublisher: Flow.Publisher<out Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher()
        ): VoteBuilder<R> {
            val newEntry = VoteEntry()
            transformPartyOrder(partyOrder!!, partyMapPublisher).subscribe(Subscriber { newEntry.partyOrder = it })
            namePublisher.subscribe(Subscriber { newEntry.name = it })
            votesPublisher.subscribe(Subscriber { newEntry.votes = it })
            pctReportingPublisher.subscribe(Subscriber { newEntry.reporting = reportingFunc(it) })
            entries.add(newEntry)
            return this
        }

        init {
            partyOrder = totalVotesPublisher.map { result -> extractPartyOrder(result) }
                .merge(maxColumnsPublisher) { parties, max -> takeTopParties(parties, max) }
            val topEntry = VoteEntry()
            partyOrder!!.subscribe(Subscriber { topEntry.partyOrder = it })
            totalHeaderPublisher.subscribe(Subscriber { topEntry.name = it })
            totalVotesPublisher.subscribe(Subscriber { topEntry.votes = it })
            reportingPublisher.subscribe(Subscriber { topEntry.reporting = reportingFunc(it) })
            entries.add(topEntry)
        }
    }

    class VotePrevBuilder<R> internal constructor(
        totalHeaderPublisher: Flow.Publisher<out String>,
        totalVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
        prevVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
        reportingPublisher: Flow.Publisher<out R>,
        titlePublisher: Flow.Publisher<out String>,
        maxColumnsPublisher: Flow.Publisher<Int?>,
        private val reportingFunc: (R) -> String
    ) : MultiPartyResultBuilder(titlePublisher, maxColumnsPublisher) {

        fun withBlankRow(): VotePrevBuilder<R> {
            entries.add(BlankEntry())
            return this
        }

        fun withRegion(
            namePublisher: Flow.Publisher<out String>,
            votesPublisher: Flow.Publisher<out Map<Party, Int>>,
            prevVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
            reportingPublisher: Flow.Publisher<out R>,
            partyMapPublisher: Flow.Publisher<out Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher()
        ): VotePrevBuilder<R> {
            val newEntry = VotePrevEntry()
            transformPartyOrder(partyOrder!!, partyMapPublisher).subscribe(Subscriber { newEntry.partyOrder = it })
            namePublisher.subscribe(Subscriber { newEntry.name = it })
            votesPublisher.subscribe(Subscriber { newEntry.votes = it })
            prevVotesPublisher.subscribe(Subscriber { newEntry.prev = it })
            reportingPublisher.subscribe(Subscriber { newEntry.reporting = reportingFunc(it) })
            entries.add(newEntry)
            return this
        }

        init {
            partyOrder =
                totalVotesPublisher
                    .merge(
                        prevVotesPublisher
                    ) { result, diff -> extractPartyOrder(result, diff) }
                    .merge(maxColumnsPublisher) { parties, max -> takeTopParties(parties, max) }
            val topEntry = VotePrevEntry()
            partyOrder!!.subscribe(Subscriber { topEntry.partyOrder = it })
            totalHeaderPublisher.subscribe(Subscriber { topEntry.name = it })
            totalVotesPublisher.subscribe(Subscriber { topEntry.votes = it })
            prevVotesPublisher.subscribe(Subscriber { topEntry.prev = it })
            reportingPublisher.subscribe(Subscriber { topEntry.reporting = reportingFunc(it) })
            entries.add(topEntry)
        }
    }

    companion object {
        fun seats(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            numTotalSeatsPublisher: Flow.Publisher<out Int>,
            titlePublisher: Flow.Publisher<out String>,
            maxColumnsPublisher: Flow.Publisher<Int?> = Publisher(null)
        ): SeatBuilder {
            return SeatBuilder(
                totalHeaderPublisher, totalSeatsPublisher, numTotalSeatsPublisher, titlePublisher, maxColumnsPublisher
            )
        }

        fun seatsWithDiff(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            seatDiffPublisher: Flow.Publisher<out Map<Party, Int>>,
            numTotalSeatsPublisher: Flow.Publisher<out Int>,
            titlePublisher: Flow.Publisher<out String>,
            maxColumnsPublisher: Flow.Publisher<Int?> = Publisher(null)
        ): SeatDiffBuilder {
            return SeatDiffBuilder(
                totalHeaderPublisher, totalSeatsPublisher, seatDiffPublisher, numTotalSeatsPublisher, titlePublisher, maxColumnsPublisher
            )
        }

        fun seatsWithPrev(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            prevSeatsPublisher: Flow.Publisher<out Map<Party, Int>>,
            numTotalSeatsPublisher: Flow.Publisher<out Int>,
            titlePublisher: Flow.Publisher<out String>,
            maxColumnsPublisher: Flow.Publisher<Int?> = Publisher(null)
        ): SeatPrevBuilder {
            return SeatPrevBuilder(
                totalHeaderPublisher,
                totalSeatsPublisher,
                prevSeatsPublisher,
                numTotalSeatsPublisher,
                titlePublisher,
                maxColumnsPublisher
            )
        }

        fun votes(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
            reportingPublisher: Flow.Publisher<out Double>,
            titlePublisher: Flow.Publisher<out String>,
            maxColumnsPublisher: Flow.Publisher<Int?> = Publisher(null)
        ): VoteBuilder<Double> {
            return VoteBuilder(
                totalHeaderPublisher, totalVotesPublisher, reportingPublisher, titlePublisher, maxColumnsPublisher
            ) { DecimalFormat("0.0%").format(it) + " IN" }
        }

        fun votesPollsReporting(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
            reportingPublisher: Flow.Publisher<out PollsReporting>,
            titlePublisher: Flow.Publisher<out String>,
            maxColumnsPublisher: Flow.Publisher<Int?> = Publisher(null)
        ): VoteBuilder<PollsReporting> {
            return VoteBuilder(
                totalHeaderPublisher, totalVotesPublisher, reportingPublisher, titlePublisher, maxColumnsPublisher
            ) { "${it.reporting}/${it.total}" }
        }

        fun votesWithPrev(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
            prevVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
            pctReportingPublisher: Flow.Publisher<out Double>,
            titlePublisher: Flow.Publisher<out String>,
            maxColumnsPublisher: Flow.Publisher<Int?> = Publisher(null)
        ): VotePrevBuilder<Double> {
            return VotePrevBuilder(
                totalHeaderPublisher, totalVotesPublisher, prevVotesPublisher, pctReportingPublisher, titlePublisher, maxColumnsPublisher
            ) { DecimalFormat("0.0%").format(it) + " IN" }
        }

        fun votesWithPrevPollsReporting(
            totalHeaderPublisher: Flow.Publisher<out String>,
            totalVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
            prevVotesPublisher: Flow.Publisher<out Map<Party, Int>>,
            pctReportingPublisher: Flow.Publisher<out PollsReporting>,
            titlePublisher: Flow.Publisher<out String>,
            maxColumnsPublisher: Flow.Publisher<Int?> = Publisher(null)
        ): VotePrevBuilder<PollsReporting> {
            return VotePrevBuilder(
                totalHeaderPublisher, totalVotesPublisher, prevVotesPublisher, pctReportingPublisher, titlePublisher, maxColumnsPublisher
            ) { "${it.reporting}/${it.total}" }
        }

        private fun extractPartyOrder(result: Map<Party, Int>): List<Party> {
            return result.entries.asSequence()
                .filter { it.value > 0 }
                .sortedByDescending { if (it.key == Party.OTHERS) -1 else it.value }
                .map { it.key }
                .toList()
        }

        private fun extractPartyOrder(
            result: Map<Party, Int>,
            diff: Map<Party, Int>
        ): List<Party> {
            return sequenceOf(result.keys.asSequence(), diff.keys.asSequence()).flatten()
                .distinct()
                .filter { party -> (result[party] ?: 0) > 0 || (diff[party] ?: 0) != 0 }
                .sortedByDescending { party -> if (party == Party.OTHERS) -1 else (result[party] ?: 0) }
                .toList()
        }

        private fun takeTopParties(parties: List<Party>, max: Int?): List<Party> {
            return if (max == null || parties.size <= max)
                parties
            else
                listOf(parties.take(max - 1), listOf(Party.OTHERS)).flatten()
        }

        private fun transformPartyOrder(
            partyOrder: Flow.Publisher<out List<Party>>,
            partyMapping: Flow.Publisher<out Map<Party, Party>>
        ): Flow.Publisher<out List<Party>> =
            partyOrder.merge(partyMapping) { po, pm -> po.map { pm[it] ?: it } }
    }
}
