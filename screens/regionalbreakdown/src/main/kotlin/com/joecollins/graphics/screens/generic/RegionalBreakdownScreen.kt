package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Coalition
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PollsReporting
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.text.DecimalFormat
import java.util.LinkedList
import java.util.concurrent.Flow

class RegionalBreakdownScreen private constructor(
    titleLabel: Flow.Publisher<out String?>,
    multiSummaryFrame: MultiSummaryFrame,
    altText: Flow.Publisher<String>,
) : GenericPanel(pad(multiSummaryFrame), titleLabel, altText),
    AltTextProvider {
    sealed interface Entry {
        val header: Flow.Publisher<out String>
        val values: Flow.Publisher<out List<Pair<Color, String>>>
        val altText: Flow.Publisher<String>
    }

    private object BlankEntry : Entry {
        override val header: Flow.Publisher<out String>
            get() = "".asOneTimePublisher()
        override val values: Flow.Publisher<out List<Pair<Color, String>>>
            get() = listOf<Pair<Color, String>>().asOneTimePublisher()

        override val altText: Flow.Publisher<String>
            get() = "".asOneTimePublisher()
    }

    class SeatEntries internal constructor(
        header: Flow.Publisher<String>,
        abbreviatedHeader: Flow.Publisher<String> = header,
        seats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
        prev: Flow.Publisher<out Map<out PartyOrCoalition, Int>>?,
        diff: Flow.Publisher<out Map<out PartyOrCoalition, Int>>?,
        total: Flow.Publisher<Int>? = null,
        private val maxColumns: Flow.Publisher<Int>?,
        private val showZero: Boolean,
    ) {
        private val consolidatedDiff = diff ?: toDiff(seats, prev)

        private val partyOrder = seats.run {
            if (consolidatedDiff == null) {
                map(::extractPartyOrder)
            } else {
                merge(consolidatedDiff, ::extractPartyOrder)
            }
        }
            .run {
                if (maxColumns == null) {
                    this
                } else {
                    merge(maxColumns) { parties, max -> takeTopParties(parties, max) }
                }
            }

        internal val entries: MutableList<Entry> = LinkedList<Entry>().apply {
            add(SeatEntry(header, abbreviatedHeader, seats, consolidatedDiff, total, null))
        }

        private fun toDiff(
            seats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prev: Flow.Publisher<out Map<out PartyOrCoalition, Int>>?,
        ): Flow.Publisher<out Map<out PartyOrCoalition, Int>>? {
            if (prev == null) return null
            return seats.merge(prev) { s, p ->
                sequenceOf(s.keys, p.keys)
                    .flatten()
                    .distinct()
                    .associateWith { (s[it] ?: 0) - (p[it] ?: 0) }
            }
        }

        fun <T> section(
            items: Iterable<T>,
            header: T.() -> Flow.Publisher<String>,
            abbreviatedHeader: T.() -> Flow.Publisher<String> = header,
            seats: T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prev: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>)? = null,
            diff: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>)? = null,
            total: (T.() -> Flow.Publisher<Int>)? = null,
            coalitionMap: (T.() -> Flow.Publisher<Map<Coalition, Party>>?)? = null,
        ) {
            val consolidatedDiff = diff ?: prev?.let { p -> { toDiff(seats(), p()) } }
            entries.add(BlankEntry)
            entries.addAll(
                items.map {
                    SeatEntry(
                        header = it.header(),
                        abbreviatedHeader = it.abbreviatedHeader(),
                        seats = it.seats(),
                        diff = consolidatedDiff?.invoke(it),
                        total = total?.invoke(it),
                        coalitionMap = coalitionMap?.invoke(it),
                    )
                },
            )
        }

        private inner class SeatEntry(
            override val header: Flow.Publisher<String>,
            private val abbreviatedHeader: Flow.Publisher<String>,
            seats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            diff: Flow.Publisher<out Map<out PartyOrCoalition, Int>>?,
            total: Flow.Publisher<Int>? = null,
            coalitionMap: Flow.Publisher<Map<Coalition, Party>>?,
        ) : Entry {
            private val transformedPartyOrder = transformPartyOrder(partyOrder, coalitionMap)

            private val filteredSeats = transformedPartyOrder.compose { po ->
                Aggregators.adjustKey(seats) { party -> if (po.contains(party)) party else Party.OTHERS }
            }

            private val filteredDiff = if (diff == null) {
                null
            } else {
                transformedPartyOrder.compose { po ->
                    Aggregators.adjustKey(diff) { party -> if (po.contains(party)) party else Party.OTHERS }
                }
            }

            private val seatsWithDiff = filteredSeats.run {
                if (filteredDiff == null) {
                    map { s -> s to null }
                } else {
                    merge(filteredDiff) { s, d -> s to d }
                }
            }

            override val values: Flow.Publisher<out List<Pair<Color, String>>> = run {
                val seatItems = seatsWithDiff.merge(transformedPartyOrder) { (s, d), po ->
                    po.map { party -> if ((s[party] ?: 0 == 0) && !showZero) (Color.WHITE to "") else party.color to seatDiffString(s[party] ?: 0, if (d == null) null else (d[party] ?: 0)) }
                }
                val totalItem = if (total == null) {
                    emptyList<Pair<Color, String>>().asOneTimePublisher()
                } else {
                    filteredSeats.merge(total) { s, t ->
                        listOf(Color.WHITE to "${s.values.sum()}/$t")
                    }
                }
                seatItems.merge(totalItem) { a, b -> a + b }
            }

            private fun seatDiffString(seats: Int, diff: Int?): String = "$seats" + (if (diff == null) "" else " (${diff.let { if (it == 0) "±0" else DecimalFormat("+0;-0").format(it) }})")

            override val altText: Flow.Publisher<String> = run {
                val seatItems = seatsWithDiff.merge(transformedPartyOrder) { (s, d), po ->
                    po.filter { party -> s.contains(party) || (d?.contains(party) ?: false) }
                        .map { party -> party.abbreviation + " " + seatDiffString(s[party] ?: 0, if (d == null) null else (d[party] ?: 0)) }
                }
                val totalItem = if (total == null) {
                    emptyList<String>().asOneTimePublisher()
                } else {
                    filteredSeats.merge(total) { s, t ->
                        listOf("${s.values.sum()}/$t")
                    }
                }
                seatItems.merge(totalItem) { a, b -> a + b }
                    .merge(abbreviatedHeader) { i, h -> h + ": " + i.joinToString(", ") }
            }
        }
    }

    class VoteEntries internal constructor(
        header: Flow.Publisher<String>,
        abbreviatedHeader: Flow.Publisher<String> = header,
        votes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
        prev: Flow.Publisher<out Map<out PartyOrCoalition, Int>>?,
        reporting: ReportingString? = null,
        private val maxColumns: Flow.Publisher<Int>?,
    ) {
        private val partyOrder = votes.map(::extractPartyOrder)
            .run {
                if (maxColumns == null) {
                    this
                } else {
                    merge(maxColumns) { parties, max -> takeTopParties(parties, max) }
                }
            }

        internal val entries: MutableList<Entry> = LinkedList<Entry>().apply {
            add(VoteEntry(header, abbreviatedHeader, votes, prev, reporting, null))
        }

        fun <T> section(
            items: Iterable<T>,
            header: T.() -> Flow.Publisher<String>,
            abbreviatedHeader: T.() -> Flow.Publisher<String> = header,
            votes: T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prev: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>)? = null,
            reporting: (T.() -> ReportingString)? = null,
            coalitionMap: (T.() -> Flow.Publisher<Map<Coalition, Party>>?)? = null,
        ) {
            entries.add(BlankEntry)
            entries.addAll(
                items.map {
                    VoteEntry(
                        it.header(),
                        it.abbreviatedHeader(),
                        it.votes(),
                        prev?.invoke(it),
                        reporting?.invoke(it),
                        coalitionMap?.invoke(it),
                    )
                },
            )
        }

        private inner class VoteEntry(
            override val header: Flow.Publisher<String>,
            private val abbreviatedHeader: Flow.Publisher<String>,
            votes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            prev: Flow.Publisher<out Map<out PartyOrCoalition, Int>>?,
            reporting: ReportingString? = null,
            coalitionMap: Flow.Publisher<Map<Coalition, Party>>?,
        ) : Entry {
            private val transformedPartyOrder = transformPartyOrder(partyOrder, coalitionMap)

            private val pct = transformedPartyOrder.compose { po ->
                Aggregators.adjustKey(votes) { if (po.contains(it)) it else Party.OTHERS }
            }
                .map { Aggregators.toPct(it) }
            private val prevPct = if (prev == null) {
                null
            } else {
                transformedPartyOrder.compose { po ->
                    Aggregators.adjustKey(prev) { if (po.contains(it)) it else Party.OTHERS }
                }
                    .map { Aggregators.toPct(it) }
            }

            private val pctWithDiff = pct.run {
                if (prevPct == null) {
                    map { p -> p to null }
                } else {
                    merge(prevPct) { p, pp ->
                        val diff = sequenceOf(p.keys, pp.keys)
                            .flatten()
                            .distinct()
                            .associateWith { (p[it] ?: 0.0) - (pp[it] ?: 0.0) }
                        p to diff
                    }
                }
            }

            override val values: Flow.Publisher<out List<Pair<Color, String>>> = run {
                val pctEntries = pctWithDiff.merge(transformedPartyOrder) { (p, d), po ->
                    po.map { party -> party.color to pctDiffString(p[party] ?: 0.0, if (d == null) null else (d[party] ?: 0.0)) }
                }
                val reportingEntries = reporting?.reporting()?.map { listOf(Color.WHITE to it) } ?: emptyList<Pair<Color, String>>().asOneTimePublisher()
                pctEntries.merge(reportingEntries) { a, b -> a + b }
            }

            override val altText: Flow.Publisher<String> = run {
                val pctEntries = pctWithDiff.merge(transformedPartyOrder) { (p, d), po ->
                    po.map { party -> party.abbreviation + " " + pctDiffString(p[party] ?: 0.0, if (d == null) null else (d[party] ?: 0.0)) }
                }
                val reportingEntries = reporting?.reporting()?.map { listOf(it) } ?: emptyList<String>().asOneTimePublisher()
                pctEntries.merge(reportingEntries) { a, b -> a + b }
                    .merge(abbreviatedHeader) { i, h -> "$h: ${i.joinToString(", ")}" }
            }

            private fun pctDiffString(pct: Double, diff: Double?): String = DecimalFormat("0.0%").format(pct) + (if (diff == null) "" else " (${diff.let { if (it == 0.0) "±0.0" else DecimalFormat("+0.0;-0.0").format(it * 100) }})")
        }
    }

    sealed class ReportingString {
        internal abstract fun reporting(): Flow.Publisher<String>
    }

    class PctReportingString(private val pctReporting: Flow.Publisher<Double>) : ReportingString() {
        override fun reporting(): Flow.Publisher<String> = pctReporting.map { pct -> DecimalFormat("0.0%").format(pct) + " IN" }
    }

    class PollsReportingString(private val pollsReporting: Flow.Publisher<PollsReporting>) : ReportingString() {
        override fun reporting(): Flow.Publisher<String> = pollsReporting.map { (reporting, total) -> "$reporting/$total" }
    }

    companion object {
        fun of(
            entries: List<Entry>,
            header: Flow.Publisher<String>,
            title: Flow.Publisher<String?>,
            progressLabel: Flow.Publisher<String?>? = null,
        ): RegionalBreakdownScreen = RegionalBreakdownScreen(
            title,
            createFrame(entries, header, progressLabel),
            createAltText(entries, header, progressLabel, title),
        )

        fun seats(
            topRowHeader: Flow.Publisher<String>,
            topRowAbbreviatedHeader: Flow.Publisher<String> = topRowHeader,
            topRowSeats: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            topRowPrev: Flow.Publisher<out Map<out PartyOrCoalition, Int>>? = null,
            topRowDiff: Flow.Publisher<out Map<out PartyOrCoalition, Int>>? = null,
            topRowTotal: Flow.Publisher<Int>? = null,
            maxColumns: Flow.Publisher<Int>? = null,
            showZero: Boolean = true,
            builder: SeatEntries.() -> Unit,
        ): List<Entry> = SeatEntries(
            topRowHeader,
            topRowAbbreviatedHeader,
            topRowSeats,
            topRowPrev,
            topRowDiff,
            topRowTotal,
            maxColumns,
            showZero,
        ).apply(builder).entries

        fun votes(
            topRowHeader: Flow.Publisher<String>,
            topRowAbbreviatedHeader: Flow.Publisher<String> = topRowHeader,
            topRowVotes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            topRowPrev: Flow.Publisher<out Map<out PartyOrCoalition, Int>>? = null,
            topRowReporting: ReportingString? = null,
            maxColumns: Flow.Publisher<Int>? = null,
            builder: VoteEntries.() -> Unit,
        ): List<Entry> = VoteEntries(
            topRowHeader,
            topRowAbbreviatedHeader,
            topRowVotes,
            topRowPrev,
            topRowReporting,
            maxColumns,
        ).apply(builder).entries

        fun pct(pctReporting: Flow.Publisher<Double>) = PctReportingString(pctReporting)
        fun polls(pollsReporting: Flow.Publisher<PollsReporting>) = PollsReportingString(pollsReporting)

        private fun createFrame(
            entries: List<Entry>,
            header: Flow.Publisher<out String>,
            progressLabel: Flow.Publisher<String?>?,
        ): MultiSummaryFrame = MultiSummaryFrame(
            headerPublisher = header,
            progressLabel = progressLabel,
            rowsPublisher =
            entries.map {
                it.header.merge(it.values) { h, v -> MultiSummaryFrame.Row(h, v) }
            }
                .combine(),
        )

        private fun createAltText(
            entries: List<Entry>,
            header: Flow.Publisher<out String>,
            progressLabel: Flow.Publisher<String?>?,
            title: Flow.Publisher<out String?>,
        ): Flow.Publisher<String> {
            val headerLine = (if (progressLabel == null) header else header.merge(progressLabel) { h, p -> if (p == null) h else "$h [$p]" })
                .merge(title) { h, t -> sequenceOf(t, h).filterNotNull().joinToString("\n") }
            val rows = entries.map { it.altText }.combine().map { it.joinToString("\n") }
            return headerLine.merge(rows) { h, v -> "$h\n\n$v" }
        }

        private fun extractPartyOrder(result: Map<out PartyOrCoalition, Int>): List<PartyOrCoalition> = extractPartyOrder(result, null)

        private fun extractPartyOrder(
            result: Map<out PartyOrCoalition, Int>,
            diff: Map<out PartyOrCoalition, Int>?,
        ): List<PartyOrCoalition> = sequenceOf(result.keys.asSequence(), diff?.keys?.asSequence())
            .filterNotNull()
            .flatten()
            .distinct()
            .filter { party -> (result[party] ?: 0) > 0 || (diff != null && (diff[party] ?: 0) != 0) }
            .sortedByDescending { party -> party.overrideSortOrder ?: (result[party] ?: 0) }
            .toList()

        private fun takeTopParties(parties: List<PartyOrCoalition>, max: Int?): List<PartyOrCoalition> = if (max == null || parties.size <= max) {
            parties
        } else {
            listOf(parties.take(max - 1), listOf(Party.OTHERS)).flatten()
        }

        private fun transformPartyOrder(
            partyOrder: Flow.Publisher<out List<PartyOrCoalition>>,
            partyMapping: Flow.Publisher<out Map<Coalition, Party>>?,
        ): Flow.Publisher<out List<PartyOrCoalition>> = if (partyMapping == null) {
            partyOrder
        } else {
            partyOrder.merge(partyMapping) { po, pm ->
                pm.forEach { (coalition, party) ->
                    if (!coalition.constituentParties.contains(party)) {
                        throw IllegalArgumentException("$party is not a constituent party of $coalition")
                    }
                }
                po.map { pm[it] ?: it }
            }
        }
    }
}
