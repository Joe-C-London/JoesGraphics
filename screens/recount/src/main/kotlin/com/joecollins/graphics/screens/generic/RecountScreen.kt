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
        fun <T> of(
            candidateVotes: Flow.Publisher<out Map<T, Map<Candidate, Int>>>,
            rowHeaderFunc: (T) -> String,
            voteThreshold: Int,
            header: Flow.Publisher<out String>,
        ): Builder<T, Candidate> {
            return Builder(
                header,
                candidateVotes,
                rowHeaderFunc,
                { it.party.abbreviation.uppercase() },
                { it.party.color },
                voteThreshold = voteThreshold,
            )
        }

        fun <T> of(
            candidateVotes: Flow.Publisher<out Map<T, Map<Candidate, Int>>>,
            rowHeaderFunc: (T) -> String,
            pctThreshold: Double,
            header: Flow.Publisher<out String>,
        ): Builder<T, Candidate> {
            return Builder(
                header,
                candidateVotes,
                rowHeaderFunc,
                { it.party.abbreviation.uppercase() },
                { it.party.color },
                pctThreshold = pctThreshold,
            )
        }

        fun <T> ofNonPartisan(
            candidateVotes: Flow.Publisher<out Map<T, Map<NonPartisanCandidate, Int>>>,
            rowHeaderFunc: (T) -> String,
            voteThreshold: Int,
            header: Flow.Publisher<out String>,
        ): Builder<T, NonPartisanCandidate> {
            return Builder(
                header,
                candidateVotes,
                rowHeaderFunc,
                { it.surname.uppercase() },
                { it.color },
                voteThreshold = voteThreshold,
            )
        }

        fun <T> ofNonPartisan(
            candidateVotes: Flow.Publisher<out Map<T, Map<NonPartisanCandidate, Int>>>,
            rowHeaderFunc: (T) -> String,
            pctThreshold: Double,
            header: Flow.Publisher<out String>,
        ): Builder<T, NonPartisanCandidate> {
            return Builder(
                header,
                candidateVotes,
                rowHeaderFunc,
                { it.surname.uppercase() },
                { it.color },
                pctThreshold = pctThreshold,
            )
        }
    }

    private class Input<T, CT>(val voteThreshold: Int?, val pctThreshold: Double?) {
        private var votes: Map<T, Map<CT, Int>> = HashMap()
        private var pctReporting: Map<T, Double>? = null

        fun setVotes(votes: Map<T, Map<CT, Int>>) {
            this.votes = votes
            votesPublisher.submit(votes)
        }

        fun setPctReporting(pctReporting: Map<T, Double>) {
            this.pctReporting = pctReporting
            pctReportingPublisher.submit(pctReporting)
        }

        private val votesPublisher = Publisher(votes)
        private val pctReportingPublisher = Publisher(pctReporting)

        fun toEntries(): Flow.Publisher<out List<Entry<T, CT>>> {
            return votesPublisher.merge(pctReportingPublisher) { votes, pctReporting ->
                votes.entries.asSequence()
                    .map {
                        Entry(
                            it.key,
                            it.value,
                            pctReporting?.get(it.key) ?: 1.0,
                            voteThreshold,
                            pctThreshold,
                        )
                    }
                    .filter { it.votes.values.sum() > 0 }
                    .filter { it.pctReporting == null || it.pctReporting >= 1.0 - 1e-6 }
                    .filter { it.isTooClose }
                    .sortedBy { if (it.pctThreshold == null) it.margin.toDouble() else it.pctMargin }
                    .toList()
            }
        }
    }

    private class Entry<T, CT> constructor(
        val key: T,
        val votes: Map<CT, Int>,
        val pctReporting: Double?,
        voteThreshold: Int?,
        val pctThreshold: Double?,
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
        val isTooClose = when {
            voteThreshold != null -> margin <= voteThreshold
            pctThreshold != null -> pctMargin <= pctThreshold
            else -> false
        }
    }

    class Builder<T, CT>(
        private val header: Flow.Publisher<out String>,
        private val candidateVotes: Flow.Publisher<out Map<T, Map<CT, Int>>>,
        private val rowHeaderFunc: (T) -> String,
        private val labelFunc: (CT) -> String,
        private val colorFunc: (CT) -> Color,
        private val voteThreshold: Int? = null,
        private val pctThreshold: Double? = null,
    ) {

        private var pctReporting: Flow.Publisher<out Map<T, Double>>? = null

        fun withPctReporting(pctReporting: Flow.Publisher<out Map<T, Double>>): Builder<T, CT> {
            this.pctReporting = pctReporting
            return this
        }

        fun withPollsReporting(pollsReporting: Flow.Publisher<out Map<T, PollsReporting>>): Builder<T, CT> {
            this.pctReporting = pollsReporting.map { m -> m.mapValues { e -> e.value.toPct() } }
            return this
        }

        fun build(titlePublisher: Flow.Publisher<out String>): RecountScreen {
            return RecountScreen(titlePublisher, buildFrame(), buildAltText(titlePublisher))
        }

        private fun buildFrame(): MultiSummaryFrame {
            val voteFormatter = DecimalFormat("#,##0")
            val pctFormatter = DecimalFormat("0.00%")
            val input = buildInput()
            return MultiSummaryFrame(
                headerPublisher = header,
                rowsPublisher = input.toEntries().mapElements { e ->
                    val partyCells = e.topCandidates.take(2)
                        .map { colorFunc(it.key) to "${labelFunc(it.key)}: ${voteFormatter.format(it.value)}" }
                    val marginCell = Color.WHITE to "MARGIN: ${e.margin}" + (if (e.pctThreshold == null) "" else " (${pctFormatter.format(e.pctMargin)})")
                    MultiSummaryFrame.Row(rowHeaderFunc(e.key), listOf(partyCells, listOf(marginCell)).flatten())
                },
                notesPublisher = footer().asOneTimePublisher(),
            )
        }

        private fun buildInput(): Input<T, CT> {
            val input = Input<T, CT>(voteThreshold, pctThreshold)
            candidateVotes.subscribe(Subscriber { input.setVotes(it) })
            pctReporting?.subscribe(Subscriber { input.setPctReporting(it) })
            return input
        }

        private fun footer() = when {
            voteThreshold != null -> "Automatic recount triggered if the margin is $voteThreshold votes or fewer"
            pctThreshold != null -> "Automatic recount triggered if the margin is ${DecimalFormat("0.00%").format(pctThreshold)} or less"
            else -> null
        }

        private fun buildAltText(titlePublisher: Flow.Publisher<out String>): Flow.Publisher<String> {
            val header = titlePublisher.merge(header) { t, h -> "$t\n\n$h" }
            val body = buildInput().toEntries().map { entries ->
                entries.joinToString("") { e ->
                    "\n${rowHeaderFunc(e.key).uppercase()}: ${
                        e.topCandidates.take(2).joinToString("") { "${labelFunc(it.key)}: ${DecimalFormat("#,##0").format(it.value)}; " }
                    }MARGIN: ${e.margin}${if (pctThreshold == null) "" else " (${DecimalFormat("0.00%").format(e.pctMargin)})"}"
                }
            }
            return header.merge(body) { h, b -> "$h$b\n\n${footer()}" }
        }
    }
}
