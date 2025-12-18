package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.ElectionResult
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.Party
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
    altText: Flow.Publisher<(Int) -> String>,
) : GenericPanel(pad(multiSummaryFrame), titleLabel, altText),
    AltTextProvider {
    private class Input<T, CT> {
        var votes: Map<T, Map<CT, Int>> = HashMap()
            set(value) {
                field = value
                update()
            }
        var results: Map<T, ElectionResult<*>?> = HashMap()
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
        var sortOrder = SortOrder.VOTES
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
                    headers[it.key] ?: "",
                    it.value,
                    results[it.key]?.elected,
                    reporting[it.key] ?: "",
                    numCandidates,
                )
            }
            .filter { it.votes.values.sum() > 0 }
            .filter { it.declared != true }
            .sortedBy { sortOrder.sortOrder(it).toDouble() }
            .take(maxRows)
            .toList()
    }

    internal class Entry<CT>(
        val header: String,
        val votes: Map<CT, Int>,
        val declared: Boolean?,
        val reporting: String,
        val numCandidates: Int,
    ) {
        val topCandidates: List<Map.Entry<CT, Int>> = votes.entries
            .sortedByDescending { it.value }
            .toList()
        val lead = when (topCandidates.size) {
            0 -> 0
            1 -> topCandidates[0].value
            else -> topCandidates[0].value - topCandidates[1].value
        }
        val pctLead = lead / votes.values.sum().toDouble()
    }

    class Vote<T, CT> internal constructor(
        internal val votes: T.() -> Flow.Publisher<out Map<CT, Int>>,
        internal val label: CT.() -> String,
        internal val color: CT.() -> Color,
    )

    companion object {
        fun <T> pct(pctReporting: T.() -> Flow.Publisher<Double>) = PctReportingString(pctReporting)
        fun <T> polls(pollsReporting: T.() -> Flow.Publisher<PollsReporting>) = PollsReportingString(pollsReporting)

        val PCT = SortOrder.PCT
        val VOTES = SortOrder.VOTES

        fun <T> candidateVotes(votes: T.() -> Flow.Publisher<out Map<Candidate, Int>>) = Vote(
            votes = votes,
            label = { party.abbreviation.uppercase() },
            color = { party.color },
        )

        fun <T> partyVotes(votes: T.() -> Flow.Publisher<out Map<Party, Int>>) = Vote(
            votes = votes,
            label = { abbreviation.uppercase() },
            color = { color },
        )

        fun <T> nonPartisanVotes(votes: T.() -> Flow.Publisher<out Map<NonPartisanCandidate, Int>>) = Vote(
            votes = votes,
            label = { shortDisplayName.uppercase() },
            color = { color },
        )

        fun <T, CT> of(
            entries: Flow.Publisher<Set<T>>,
            votes: Vote<T, CT>,
            result: T.() -> Flow.Publisher<out ElectionResult<*>?>,
            reporting: ReportingString<T>? = null,
            label: T.() -> Flow.Publisher<String>,
            maxRows: Flow.Publisher<out Int>? = null,
            numCandidates: Flow.Publisher<out Int>? = null,
            sortOrder: SortOrder = SortOrder.VOTES,
            header: Flow.Publisher<out String?>,
            title: Flow.Publisher<out String?>,
            showLead: Boolean = true,
        ): TooCloseToCallScreen {
            val input = Input<T, CT>()
            entries.compose { set -> Aggregators.toMap(set) { votes.votes(it) } }.subscribe(Subscriber { input.votes = it })
            entries.compose { set -> Aggregators.toMap(set) { result(it) } }.subscribe(Subscriber { input.results = it })
            entries.compose { set -> Aggregators.toMap(set) { it.label() } }.subscribe(Subscriber { input.headers = it })
            reporting?.reporting(entries)?.subscribe(Subscriber { input.reporting = it })
            maxRows?.subscribe(Subscriber { input.maxRows = it })
            numCandidates?.subscribe(Subscriber { input.numCandidates = it })
            input.sortOrder = sortOrder
            return TooCloseToCallScreen(
                title,
                createFrame(header, input, votes, reporting, showLead),
                createAltText(title, header, input, votes, showLead),
            )
        }

        private fun <T, CT> createFrame(
            header: Flow.Publisher<out String?>,
            input: Input<T, CT>,
            vote: Vote<T, CT>,
            reporting: ReportingString<T>?,
            showLead: Boolean,
        ): MultiSummaryFrame {
            val entries = input.toEntries()
            val frame = MultiSummaryFrame(
                headerPublisher = header,
                rowsPublisher = entries.mapElements { entry ->
                    val total = entry.votes.values.sum().toDouble()
                    val head = entry.header
                    val values =
                        sequenceOf(
                            sequenceOf(
                                entry.topCandidates.asSequence()
                                    .map { e ->
                                        Pair(
                                            vote.color(e.key),
                                            vote.label(e.key) +
                                                ": " +
                                                (input.sortOrder.candidateDisplay(e.value, total)),
                                        )
                                    },
                                generateSequence { Pair(Color.WHITE, "") },
                            )
                                .flatten()
                                .take(entry.numCandidates),
                            if (showLead) sequenceOf(Color.WHITE to ("LEAD: " + (input.sortOrder.leadString(entry)))) else emptySequence(),
                            reporting?.let { sequenceOf(Color.WHITE to entry.reporting) } ?: emptySequence(),
                        )
                            .flatten()
                            .toList()
                    MultiSummaryFrame.Row(head, values)
                },
            )
            return frame
        }

        private fun <T, CT> createAltText(
            title: Flow.Publisher<out String?>,
            header: Flow.Publisher<out String?>,
            input: Input<T, CT>,
            vote: Vote<T, CT>,
            showLead: Boolean,
        ): Flow.Publisher<(Int) -> String> {
            val headerText = title.merge(header) { t, h ->
                if (t == null && h == null) {
                    null
                } else if (t == null) {
                    h
                } else if (h == null) {
                    "$t\n"
                } else {
                    "$t\n\n$h"
                }
            }
            return headerText.merge(input.toEntries()) { head, entries ->
                { maxLength: Int ->
                    var size = head?.length ?: -1
                    var dotDotDot = false
                    val entriesText = entries.mapNotNull { e ->
                        val total = e.votes.values.sum().toDouble()
                        val entry = "${e.header}: ${
                            e.topCandidates.take(e.numCandidates).joinToString("; ") { c ->
                                "${vote.label(c.key)}: ${input.sortOrder.candidateDisplay(c.value, total) }"
                            }
                        }" + (if (showLead) "; LEAD: ${input.sortOrder.leadString(e)}" else "") +
                            (if (e.reporting.isEmpty()) "" else "; ${e.reporting}")
                        if (dotDotDot) {
                            null
                        } else if (size + entry.length < maxLength - 10) {
                            size += entry.length + 1
                            entry
                        } else {
                            dotDotDot = true
                            "(...)"
                        }
                    }.joinToString("\n").let { it.ifEmpty { "(empty)" } }
                    if (head.isNullOrEmpty()) {
                        entriesText
                    } else {
                        "${head}\n$entriesText"
                    }
                }
            }
        }
    }

    sealed class ReportingString<T> {
        internal abstract fun reporting(entries: Flow.Publisher<out Set<T>>): Flow.Publisher<out Map<T, String>>
    }

    class PctReportingString<T>(private val pctReporting: T.() -> Flow.Publisher<Double>) : ReportingString<T>() {
        override fun reporting(entries: Flow.Publisher<out Set<T>>): Flow.Publisher<out Map<T, String>> = entries.compose { e -> Aggregators.toMap(e) { it.pctReporting().map { pct -> DecimalFormat("0.0%").format(pct) + " IN" } } }
    }

    class PollsReportingString<T>(private val pollsReporting: T.() -> Flow.Publisher<PollsReporting>) : ReportingString<T>() {
        override fun reporting(entries: Flow.Publisher<out Set<T>>): Flow.Publisher<out Map<T, String>> = entries.compose { e -> Aggregators.toMap(e) { it.pollsReporting().map { (reporting, total) -> "$reporting/$total" } } }
    }

    enum class SortOrder(
        internal val sortOrder: Entry<*>.() -> Number,
        internal val candidateDisplay: (Int, Double) -> String,
        internal val leadString: Entry<*>.() -> String,
    ) {
        VOTES(sortOrder = { lead }, candidateDisplay = { votes, _ -> DecimalFormat("#,##0").format(votes) }, leadString = { DecimalFormat("#,##0").format(lead) }),
        PCT(sortOrder = { pctLead }, candidateDisplay = { votes, total -> DecimalFormat("0.0%").format(votes / total) }, leadString = { DecimalFormat("0.0%").format(pctLead) }),
    }
}
