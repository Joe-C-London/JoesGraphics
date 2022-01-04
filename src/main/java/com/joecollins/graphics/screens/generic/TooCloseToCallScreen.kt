package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Aggregators.adjustKey
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class TooCloseToCallScreen private constructor(titleLabel: JLabel, multiSummaryFrame: MultiSummaryFrame) : JPanel() {
    private class Input<T> {
        private var votes: Map<T, Map<Candidate, Int>> = HashMap()
        private var results: Map<T, PartyResult?> = HashMap()
        private var pctReporting: Map<T, Double> = HashMap()
        private var maxRows = Int.MAX_VALUE
        private var numCandidates = 2

        fun setVotes(votes: Map<T, Map<Candidate, Int>>) {
            this.votes = votes
            update()
        }

        fun setResults(results: Map<T, PartyResult?>) {
            this.results = results
            update()
        }

        fun setPctReporting(pctReporting: Map<T, Double>) {
            this.pctReporting = pctReporting
            update()
        }

        fun setMaxRows(maxRows: Int) {
            this.maxRows = maxRows
            update()
        }

        fun setNumCandidates(numCandidates: Int) {
            this.numCandidates = numCandidates
            update()
        }

        private val entriesPublisher = Publisher(calculateEntries())
        fun toEntries() = entriesPublisher

        private fun update() = synchronized(this) { entriesPublisher.submit(calculateEntries()) }

        private fun calculateEntries() = votes.entries.asSequence()
            .map {
                Entry(
                    it.key,
                    it.value,
                    results[it.key],
                    pctReporting[it.key] ?: 0.0,
                    numCandidates
                )
            }
            .filter { it.votes.values.sum() > 0 }
            .filter { it.result == null || !it.result.isElected }
            .sortedBy { it.lead }
            .take(maxRows)
            .toList()
    }

    private class Entry<T> constructor(
        val key: T,
        val votes: Map<Candidate, Int>,
        val result: PartyResult?,
        val pctReporting: Double,
        val numCandidates: Int
    ) {
        val topCandidates: List<Map.Entry<Candidate, Int>> = votes.entries
            .sortedByDescending { it.value }
            .toList()
        var lead = when (topCandidates.size) {
            0 -> 0
            1 -> topCandidates[0].value
            else -> topCandidates[0].value - topCandidates[1].value
        }
    }

    class Builder<T>(
        private val header: Flow.Publisher<out String?>,
        private val votes: Flow.Publisher<out Map<T, Map<Candidate, Int>>>,
        private val results: Flow.Publisher<out Map<T, PartyResult?>>,
        private val rowHeaderFunc: (T) -> String
    ) {
        private var pctReporting: Flow.Publisher<out Map<T, Double>>? = null
        private var rowsLimit: Flow.Publisher<out Int>? = null
        private var numCandidates: Flow.Publisher<out Int>? = null

        fun withPctReporting(pctReportingPublisher: Flow.Publisher<out Map<T, Double>>): Builder<T> {
            pctReporting = pctReportingPublisher
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

        fun build(titlePublisher: Flow.Publisher<out String?>): TooCloseToCallScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titlePublisher.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            return TooCloseToCallScreen(headerLabel, createFrame())
        }

        private fun createFrame(): MultiSummaryFrame {
            val input = Input<T>()
            votes.subscribe(Subscriber { input.setVotes(it) })
            results.subscribe(Subscriber { input.setResults(it) })
            pctReporting?.subscribe(Subscriber { input.setPctReporting(it) })
            rowsLimit?.subscribe(Subscriber { input.setMaxRows(it) })
            numCandidates?.subscribe(Subscriber { input.setNumCandidates(it) })
            val entries = input.toEntries()
            val thousandsFormatter = DecimalFormat("#,##0")
            val pctFormatter = DecimalFormat("0.0%")
            val frame = MultiSummaryFrame(
                headerPublisher = header,
                rowsPublisher = entries.mapElements {
                    val header = rowHeaderFunc(it.key)
                    val values = run {
                        val ret: MutableList<Pair<Color, String>> = sequenceOf(
                            it.topCandidates.asSequence()
                                .map { v: Map.Entry<Candidate, Int> ->
                                    Pair(
                                        v.key.party.color,
                                        v.key.party.abbreviation.uppercase() +
                                            ": " +
                                            thousandsFormatter.format(v.value)
                                    )
                                },
                            generateSequence { Pair(Color.WHITE, "") }
                        )
                            .flatten()
                            .take(it.numCandidates)
                            .toMutableList()
                        ret.add(Pair(Color.WHITE, "LEAD: " + thousandsFormatter.format(it.lead.toLong())))
                        if (pctReporting != null) {
                            ret.add(Pair(Color.WHITE, pctFormatter.format(it.pctReporting) + " IN"))
                        }
                        ret
                    }
                    MultiSummaryFrame.Row(header, values)
                }
            )
            return frame
        }
    }

    companion object {
        @JvmStatic fun <T> of(
            votesPublisher: Flow.Publisher<out Map<T, Map<Candidate, Int>>>,
            resultPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
            labelFunc: (T) -> String,
            headerPublisher: Flow.Publisher<out String?>
        ): Builder<T> {
            return Builder(headerPublisher, votesPublisher, resultPublisher, labelFunc)
        }

        @JvmStatic fun <T> ofParty(
            votesPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            resultPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
            labelFunc: (T) -> String,
            headerPublisher: Flow.Publisher<out String?>
        ): Builder<T> {
            return Builder(
                headerPublisher,
                votesPublisher.map { votes ->
                    votes.mapValues { adjustKey(it.value) { p: Party -> Candidate("", p) } }
                },
                resultPublisher,
                labelFunc
            )
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
