package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.GenericWindow.Companion.ALT_TEXT_MAX_LENGTH
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PollsReporting
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow

class TooCloseToCallScreen private constructor(
    titleLabel: Flow.Publisher<out String?>,
    multiSummaryFrame: MultiSummaryFrame,
    override val altText: Flow.Publisher<String?>
) : GenericPanel(pad(multiSummaryFrame), titleLabel), AltTextProvider {
    private class Input<T> {
        var votes: Map<T, Map<Candidate, Int>> = HashMap()
            set(value) {
                field = value
                update()
            }
        var results: Map<T, PartyResult?> = HashMap()
            set(value) {
                field = value
                update()
            }
        var headers: Map<T, String> = HashMap()
            set(value) {
                field = value
                update()
            }
        var reporting: Map<T, String> = HashMap()
            set(value) {
                field = value
                update()
            }
        var maxRows = Int.MAX_VALUE
            set(value) {
                field = value
                update()
            }
        var numCandidates = 2
            set(value) {
                field = value
                update()
            }
        var showPcts = false
            set(value) {
                field = value
                update()
            }

        private val entriesPublisher = Publisher(calculateEntries())
        fun toEntries() = entriesPublisher

        private fun update() = synchronized(this) { entriesPublisher.submit(calculateEntries()) }

        private fun calculateEntries() = votes.entries.asSequence()
            .map {
                Entry(
                    it.key,
                    headers[it.key] ?: "",
                    it.value,
                    results[it.key],
                    reporting[it.key] ?: "",
                    numCandidates
                )
            }
            .filter { it.votes.values.sum() > 0 }
            .filter { it.result == null || !it.result.isElected }
            .sortedBy { if (showPcts) it.pctLead else it.lead.toDouble() }
            .take(maxRows)
            .toList()
    }

    private class Entry<T> constructor(
        val key: T,
        val header: String,
        val votes: Map<Candidate, Int>,
        val result: PartyResult?,
        val reporting: String,
        val numCandidates: Int
    ) {
        val topCandidates: List<Map.Entry<Candidate, Int>> = votes.entries
            .sortedByDescending { it.value }
            .toList()
        val lead = when (topCandidates.size) {
            0 -> 0
            1 -> topCandidates[0].value
            else -> topCandidates[0].value - topCandidates[1].value
        }
        val pctLead = lead / votes.values.sum().toDouble()
    }

    class Builder<T> internal constructor(
        private val header: Flow.Publisher<out String?>,
        private val entries: Flow.Publisher<out Set<T>>,
        votesFunc: (T) -> Flow.Publisher<out Map<Candidate, Int>>,
        resultsFunc: (T) -> Flow.Publisher<out PartyResult?>,
        rowHeaderFunc: (T) -> Flow.Publisher<String>
    ) {
        private val votes = entries.compose { set -> Aggregators.toMap(set) { votesFunc(it) } }
        private val results = entries.compose { set -> Aggregators.toMap(set) { resultsFunc(it) } }
        private val rowHeaders = entries.compose { set -> Aggregators.toMap(set) { rowHeaderFunc(it) } }
        private var reporting: Flow.Publisher<out Map<T, String>>? = null
        private var rowsLimit: Flow.Publisher<out Int>? = null
        private var numCandidates: Flow.Publisher<out Int>? = null
        private var showPcts: Boolean = false

        fun withPctReporting(pctReportingFunc: (T) -> Flow.Publisher<Double>): Builder<T> {
            reporting = entries.compose { set -> Aggregators.toMap(set) { pctReportingFunc(it).map { pct -> DecimalFormat("0.0%").format(pct) + " IN" } } }
            return this
        }

        fun withPollsReporting(pollsReportingFunc: (T) -> Flow.Publisher<PollsReporting>): Builder<T> {
            reporting = entries.compose { set -> Aggregators.toMap(set) { pollsReportingFunc(it).map { (reporting, total) -> "$reporting/$total" } } }
            return this
        }

        fun withMaxRows(rowsLimitPublisher: Flow.Publisher<out Int>): Builder<T> {
            rowsLimit = rowsLimitPublisher
            return this
        }

        fun withNumberOfCandidates(numCandidatesPublisher: Flow.Publisher<out Int>): Builder<T> {
            numCandidates = numCandidatesPublisher
            return this
        }

        fun sortByPcts(): Builder<T> {
            showPcts = true
            return this
        }

        fun build(titlePublisher: Flow.Publisher<out String?>): TooCloseToCallScreen {
            val input = Input<T>()
            votes.subscribe(Subscriber { input.votes = it })
            results.subscribe(Subscriber { input.results = it })
            rowHeaders.subscribe(Subscriber { input.headers = it })
            reporting?.subscribe(Subscriber { input.reporting = it })
            rowsLimit?.subscribe(Subscriber { input.maxRows = it })
            numCandidates?.subscribe(Subscriber { input.numCandidates = it })
            input.showPcts = showPcts
            return TooCloseToCallScreen(titlePublisher, createFrame(input), createAltText(titlePublisher, input))
        }

        private fun createAltText(titlePublisher: Flow.Publisher<out String?>, input: Input<T>): Flow.Publisher<String?> {
            val header = titlePublisher.merge(header) { title, header ->
                if (title == null && header == null) {
                    null
                } else if (title == null) {
                    header
                } else if (header == null) {
                    "$title\n"
                } else {
                    "$title\n\n$header"
                }
            }
            return header.merge(input.toEntries()) { head, entries ->
                var size = head?.length ?: -1
                var dotDotDot = false
                val entriesText = entries.mapNotNull { e ->
                    val total = e.votes.values.sum().toDouble()
                    val entry = "${e.header}: ${
                    e.topCandidates.take(e.numCandidates).joinToString("; ") { c -> "${c.key.party.abbreviation}: ${if (input.showPcts) DecimalFormat("0.0%").format(c.value / total) else DecimalFormat("#,##0").format(c.value)}" }
                    }; LEAD: ${if (input.showPcts) DecimalFormat("0.0%").format(e.pctLead) else DecimalFormat("#,##0").format(e.lead)}${
                    if (e.reporting.isEmpty()) "" else "; ${e.reporting}"
                    }"
                    if (dotDotDot) {
                        null
                    } else if (size + entry.length < ALT_TEXT_MAX_LENGTH - 10) {
                        size += entry.length + 1
                        entry
                    } else {
                        dotDotDot = true
                        "(...)"
                    }
                }.joinToString("\n").let { it.ifEmpty { "(empty)" } }
                if (entriesText.isEmpty()) {
                    head
                } else if (head.isNullOrEmpty()) {
                    entriesText
                } else {
                    "${head}\n$entriesText"
                }
            }
        }

        private fun createFrame(input: Input<T>): MultiSummaryFrame {
            val entries = input.toEntries()
            val thousandsFormatter = DecimalFormat("#,##0")
            val pctFormatter = DecimalFormat("0.0%")
            val frame = MultiSummaryFrame(
                headerPublisher = header,
                rowsPublisher = entries.mapElements { entry ->
                    val total = entry.votes.values.sum().toDouble()
                    val header = entry.header
                    val values =
                        sequenceOf(
                            sequenceOf(
                                entry.topCandidates.asSequence()
                                    .map { e ->
                                        Pair(
                                            e.key.party.color,
                                            e.key.party.abbreviation.uppercase() +
                                                    ": " +
                                                    (
                                                            if (input.showPcts) {
                                                                pctFormatter.format(e.value / total)
                                                            } else {
                                                                thousandsFormatter.format(e.value)
                                                            }
                                                            )
                                        )
                                    },
                                generateSequence { Pair(Color.WHITE, "") }
                            )
                                .flatten()
                                .take(entry.numCandidates),
                            sequenceOf(Color.WHITE to ("LEAD: " + (if (input.showPcts) pctFormatter.format(entry.pctLead) else thousandsFormatter.format(entry.lead)))),
                            reporting?.let { sequenceOf(Color.WHITE to entry.reporting) } ?: emptySequence()
                        )
                            .flatten()
                            .toList()
                    MultiSummaryFrame.Row(header, values)
                }
            )
            return frame
        }
    }

    companion object {
        fun <T> of(
            entries: Flow.Publisher<Set<T>>,
            votesPublisher: (T) -> Flow.Publisher<out Map<Candidate, Int>>,
            resultPublisher: (T) -> Flow.Publisher<out PartyResult?>,
            labelFunc: (T) -> Flow.Publisher<String>,
            headerPublisher: Flow.Publisher<out String?>
        ): Builder<T> {
            return Builder(headerPublisher, entries, votesPublisher, resultPublisher, labelFunc)
        }

        fun <T> ofParty(
            entries: Flow.Publisher<Set<T>>,
            votesPublisher: (T) -> Flow.Publisher<out Map<Party, Int>>,
            resultPublisher: (T) -> Flow.Publisher<out PartyResult?>,
            labelFunc: (T) -> Flow.Publisher<String>,
            headerPublisher: Flow.Publisher<out String?>
        ): Builder<T> {
            return Builder(
                headerPublisher,
                entries,
                { t: T -> votesPublisher(t).map { v -> Aggregators.adjustKey(v) { p -> Candidate("", p) } } },
                resultPublisher,
                labelFunc
            )
        }
    }
}