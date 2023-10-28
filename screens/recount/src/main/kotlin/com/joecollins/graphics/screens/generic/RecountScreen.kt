package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.PollsReporting
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow

class RecountScreen private constructor(
    headerLabel: Flow.Publisher<out String?>,
    frame: MultiSummaryFrame,
    altText: Flow.Publisher<String>,
) : GenericPanel(pad(frame), headerLabel, altText), AltTextProvider {

    companion object {
        fun votes(votes: Int) = VoteThreshold(votes)
        fun pct(pct: Double) = PctThreshold(pct)

        fun <T> pct(pctReporting: Flow.Publisher<out Map<T, Double>>) = PctReportingFilter(pctReporting)
        fun <T> polls(pollsReporting: Flow.Publisher<out Map<T, PollsReporting>>) = PollsReportingFilter(pollsReporting)

        fun <T> of(
            candidateVotes: Flow.Publisher<out Map<T, Map<Candidate, Int>>>,
            rowHeader: T.() -> String,
            threshold: Threshold,
            header: Flow.Publisher<out String>,
            reporting: ReportingFilter<T>? = null,
            title: Flow.Publisher<out String>,
        ): RecountScreen {
            return build(
                header,
                candidateVotes,
                rowHeader,
                { party.abbreviation.uppercase() },
                { party.color },
                threshold,
                reporting,
                title,
            )
        }

        fun <T> ofNonPartisan(
            candidateVotes: Flow.Publisher<out Map<T, Map<NonPartisanCandidate, Int>>>,
            rowHeader: T.() -> String,
            threshold: Threshold,
            header: Flow.Publisher<out String>,
            reporting: ReportingFilter<T>? = null,
            title: Flow.Publisher<out String>,
        ): RecountScreen {
            return build(
                header,
                candidateVotes,
                rowHeader,
                { surname.uppercase() },
                { color },
                threshold,
                reporting,
                title,
            )
        }

        private fun <T, CT> build(
            header: Flow.Publisher<out String>,
            candidateVotes: Flow.Publisher<out Map<T, Map<CT, Int>>>,
            rowHeader: T.() -> String,
            label: CT.() -> String,
            color: CT.() -> Color,
            threshold: Threshold,
            reporting: ReportingFilter<T>?,
            title: Flow.Publisher<out String>,
        ): RecountScreen {
            return RecountScreen(
                title,
                buildFrame(header, candidateVotes, rowHeader, label, color, threshold, reporting),
                buildAltText(header, candidateVotes, rowHeader, label, threshold, reporting, title),
            )
        }

        private fun <T, CT> buildFrame(
            header: Flow.Publisher<out String>,
            candidateVotes: Flow.Publisher<out Map<T, Map<CT, Int>>>,
            rowHeader: T.() -> String,
            label: CT.() -> String,
            color: CT.() -> Color,
            threshold: Threshold,
            reporting: ReportingFilter<T>?,
        ): MultiSummaryFrame {
            val voteFormatter = DecimalFormat("#,##0")
            val pctFormatter = DecimalFormat("0.00%")
            val input = buildInput(candidateVotes, threshold, reporting)
            return MultiSummaryFrame(
                headerPublisher = header,
                rowsPublisher = input.toEntries().mapElements { e ->
                    val partyCells = e.topCandidates.take(2)
                        .map { it.key.color() to "${it.key.label()}: ${voteFormatter.format(it.value)}" }
                    val marginCell = Color.WHITE to "MARGIN: ${e.margin}" + (if (e.threshold is VoteThreshold) "" else " (${pctFormatter.format(e.pctMargin)})")
                    MultiSummaryFrame.Row(e.key.rowHeader(), listOf(partyCells, listOf(marginCell)).flatten())
                },
                notesPublisher = footer(threshold).asOneTimePublisher(),
            )
        }

        private fun <T, CT> buildInput(
            candidateVotes: Flow.Publisher<out Map<T, Map<CT, Int>>>,
            threshold: Threshold,
            reporting: ReportingFilter<T>?,
        ): Input<T, CT> {
            val input = Input<T, CT>(threshold)
            candidateVotes.subscribe(Subscriber { input.setVotes(it) })
            reporting?.complete?.subscribe(Subscriber { input.setComplete(it) })
            return input
        }

        private fun footer(
            threshold: Threshold,
        ) = when (threshold) {
            is VoteThreshold -> "Automatic recount triggered if the margin is ${threshold.votes} votes or fewer"
            is PctThreshold -> "Automatic recount triggered if the margin is ${DecimalFormat("0.00%").format(threshold.pct)} or less"
        }

        private fun <T, CT> buildAltText(
            header: Flow.Publisher<out String>,
            candidateVotes: Flow.Publisher<out Map<T, Map<CT, Int>>>,
            rowHeader: T.() -> String,
            label: CT.() -> String,
            threshold: Threshold,
            reporting: ReportingFilter<T>?,
            title: Flow.Publisher<out String>,
        ): Flow.Publisher<String> {
            val headerText = title.merge(header) { t, h -> "$t\n\n$h" }
            val body = buildInput(candidateVotes, threshold, reporting).toEntries().map { entries ->
                entries.joinToString("") { e ->
                    "\n${e.key.rowHeader().uppercase()}: ${
                        e.topCandidates.take(2).joinToString("") { "${it.key.label()}: ${DecimalFormat("#,##0").format(it.value)}; " }
                    }MARGIN: ${e.margin}${if (threshold is VoteThreshold) "" else " (${DecimalFormat("0.00%").format(e.pctMargin)})"}"
                }
            }
            return headerText.merge(body) { h, b -> "$h$b\n\n${footer(threshold)}" }
        }
    }

    private class Input<T, CT>(val threshold: Threshold) {
        private var votes: Map<T, Map<CT, Int>> = HashMap()
        private var complete: Map<T, Boolean>? = null

        fun setVotes(votes: Map<T, Map<CT, Int>>) {
            this.votes = votes
            votesPublisher.submit(votes)
        }

        fun setComplete(complete: Map<T, Boolean>) {
            this.complete = complete
            completePublisher.submit(complete)
        }

        private val votesPublisher = Publisher(votes)
        private val completePublisher = Publisher(complete)

        fun toEntries(): Flow.Publisher<out List<Entry<T, CT>>> {
            return votesPublisher.merge(completePublisher) { votes, complete ->
                votes.entries.asSequence()
                    .map {
                        Entry(
                            it.key,
                            it.value,
                            complete?.get(it.key) ?: true,
                            threshold,
                        )
                    }
                    .filter { it.votes.values.sum() > 0 }
                    .filter { it.complete }
                    .filter { it.isTooClose }
                    .sortedBy { if (it.threshold is VoteThreshold) it.margin.toDouble() else it.pctMargin }
                    .toList()
            }
        }
    }

    private class Entry<T, CT>(
        val key: T,
        val votes: Map<CT, Int>,
        val complete: Boolean,
        val threshold: Threshold,
    ) {
        val topCandidates: List<Map.Entry<CT, Int>> = votes.entries
            .sortedByDescending { it.value }
            .toList()
        val margin = when (topCandidates.size) {
            0 -> 0
            1 -> topCandidates[0].value
            else -> topCandidates[0].value - topCandidates[1].value
        }
        val pctMargin = margin.toDouble() / votes.values.sum()
        val isTooClose = when (threshold) {
            is VoteThreshold -> margin <= threshold.votes
            is PctThreshold -> pctMargin <= threshold.pct
        }
    }

    sealed interface Threshold

    class VoteThreshold(val votes: Int) : Threshold

    class PctThreshold(val pct: Double) : Threshold

    sealed class ReportingFilter<T> {
        internal abstract val complete: Flow.Publisher<out Map<T, Boolean>>
    }

    class PctReportingFilter<T>(pctReporting: Flow.Publisher<out Map<T, Double>>) : ReportingFilter<T>() {
        override val complete: Flow.Publisher<out Map<T, Boolean>> = pctReporting.map {
            it.mapValues { (_, pct) -> pct >= 1.0 }
        }
    }

    class PollsReportingFilter<T>(pollsReporting: Flow.Publisher<out Map<T, PollsReporting>>) : ReportingFilter<T>() {
        override val complete: Flow.Publisher<out Map<T, Boolean>> = pollsReporting.map {
            it.mapValues { (_, polls) -> polls.reporting >= polls.total }
        }
    }
}
