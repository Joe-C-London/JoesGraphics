package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.models.general.Candidate
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

class RecountScreen private constructor(headerLabel: Flow.Publisher<out String?>, frame: MultiSummaryFrame) : GenericPanel(pad(frame), headerLabel) {

    companion object {
        fun <T> of(
            candidateVotes: Flow.Publisher<out Map<T, Map<Candidate, Int>>>,
            rowHeaderFunc: (T) -> String,
            voteThreshold: Int,
            header: Flow.Publisher<out String>
        ): Builder<T> {
            return Builder(header, candidateVotes, rowHeaderFunc, voteThreshold = voteThreshold)
        }

        fun <T> of(
            candidateVotes: Flow.Publisher<out Map<T, Map<Candidate, Int>>>,
            rowHeaderFunc: (T) -> String,
            pctThreshold: Double,
            header: Flow.Publisher<out String>
        ): Builder<T> {
            return Builder(header, candidateVotes, rowHeaderFunc, pctThreshold = pctThreshold)
        }
    }

    private class Input<T>(val voteThreshold: Int?, val pctThreshold: Double?) {
        private var votes: Map<T, Map<Candidate, Int>> = HashMap()
        private var pctReporting: Map<T, Double>? = null

        fun setVotes(votes: Map<T, Map<Candidate, Int>>) {
            this.votes = votes
            votesPublisher.submit(votes)
        }

        fun setPctReporting(pctReporting: Map<T, Double>) {
            this.pctReporting = pctReporting
            pctReportingPublisher.submit(pctReporting)
        }

        private val votesPublisher = Publisher(votes)
        private val pctReportingPublisher = Publisher(pctReporting)

        fun toEntries(): Flow.Publisher<out List<Entry<T>>> {
            return votesPublisher.merge(pctReportingPublisher) { votes, pctReporting ->
                votes.entries.asSequence()
                    .map {
                        Entry(
                            it.key,
                            it.value,
                            pctReporting?.get(it.key) ?: 1.0,
                            voteThreshold,
                            pctThreshold
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

    private class Entry<T> constructor(
        val key: T,
        val votes: Map<Candidate, Int>,
        val pctReporting: Double?,
        voteThreshold: Int?,
        val pctThreshold: Double?
    ) {
        val topCandidates: List<Map.Entry<Candidate, Int>> = votes.entries
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

    class Builder<T>(
        private val header: Flow.Publisher<out String>,
        private val candidateVotes: Flow.Publisher<out Map<T, Map<Candidate, Int>>>,
        private val rowHeaderFunc: (T) -> String,
        private val voteThreshold: Int? = null,
        private val pctThreshold: Double? = null
    ) {

        private var pctReporting: Flow.Publisher<out Map<T, Double>>? = null

        fun withPctReporting(pctReporting: Flow.Publisher<out Map<T, Double>>): Builder<T> {
            this.pctReporting = pctReporting
            return this
        }

        fun withPollsReporting(pollsReporting: Flow.Publisher<out Map<T, PollsReporting>>): Builder<T> {
            this.pctReporting = pollsReporting.map { m -> m.mapValues { e -> e.value.toPct() } }
            return this
        }

        fun build(titlePublisher: Flow.Publisher<out String>): RecountScreen {
            return RecountScreen(titlePublisher, buildFrame())
        }

        private fun buildFrame(): MultiSummaryFrame {
            val voteFormatter = DecimalFormat("#,##0")
            val pctFormatter = DecimalFormat("0.00%")
            val input = Input<T>(voteThreshold, pctThreshold)
            candidateVotes.subscribe(Subscriber { input.setVotes(it) })
            pctReporting?.subscribe(Subscriber { input.setPctReporting(it) })
            return MultiSummaryFrame(
                headerPublisher = header,
                rowsPublisher = input.toEntries().mapElements { e ->
                    val partyCells = e.topCandidates.take(2)
                        .map {it.key.party.color to "${it.key.party.abbreviation.uppercase()}: ${voteFormatter.format(it.value)}" }
                    val marginCell = Color.WHITE to "MARGIN: ${e.margin}" + (if (e.pctThreshold == null) "" else " (${pctFormatter.format(e.pctMargin)})")
                    MultiSummaryFrame.Row(rowHeaderFunc(e.key), listOf(partyCells, listOf(marginCell)).flatten())
                },
                notesPublisher = when {
                    voteThreshold != null -> "Automatic recount triggered if the margin is $voteThreshold votes or fewer"
                    pctThreshold != null -> "Automatic recount triggered if the margin is ${pctFormatter.format(pctThreshold)} or less"
                    else -> null
                }.asOneTimePublisher()
            )
        }
    }
}