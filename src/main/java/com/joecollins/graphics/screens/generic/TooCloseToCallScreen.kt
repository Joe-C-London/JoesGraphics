package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Aggregators.adjustKey
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.text.DecimalFormat
import java.util.HashMap
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class TooCloseToCallScreen private constructor(titleLabel: JLabel, multiSummaryFrame: MultiSummaryFrame) : JPanel() {
    private class Input<T> : Bindable<Input<T>, Input.Property>() {
        enum class Property {
            VOTES, RESULTS, PCT_REPORTING, MAX_ROWS, NUM_CANDIDATES
        }

        private var votes: Map<T, Map<Candidate, Int>> = HashMap()
        private var results: Map<T, PartyResult> = HashMap()
        private var pctReporting: Map<T, Double> = HashMap()
        private var maxRows = Int.MAX_VALUE
        private var numCandidates = 2

        fun setVotes(votes: Map<T, Map<Candidate, Int>>) {
            this.votes = votes
            onPropertyRefreshed(Property.VOTES)
        }

        fun setResults(results: Map<T, PartyResult>) {
            this.results = results
            onPropertyRefreshed(Property.RESULTS)
        }

        fun setPctReporting(pctReporting: Map<T, Double>) {
            this.pctReporting = pctReporting
            onPropertyRefreshed(Property.PCT_REPORTING)
        }

        fun setMaxRows(maxRows: Int) {
            this.maxRows = maxRows
            onPropertyRefreshed(Property.MAX_ROWS)
        }

        fun setNumCandidates(numCandidates: Int) {
            this.numCandidates = numCandidates
            onPropertyRefreshed(Property.NUM_CANDIDATES)
        }

        fun toEntries(): Binding<List<Entry<T>>> {
            return Binding.propertyBinding(
                    this,
                    { t: Input<T> ->
                        t.votes.entries.asSequence()
                                .map {
                                    Entry(
                                            it.key,
                                            it.value,
                                            t.results[it.key],
                                            t.pctReporting[it.key] ?: 0.0,
                                            t.numCandidates)
                                }
                                .filter { it.votes.values.sum() > 0 }
                                .filter { it.result == null || !it.result.isElected }
                                .sortedBy { it.lead }
                                .take(t.maxRows)
                                .toList()
                    },
                    Property.VOTES,
                    Property.RESULTS,
                    Property.PCT_REPORTING,
                    Property.MAX_ROWS,
                    Property.NUM_CANDIDATES)
        }
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
        header: Binding<String?>,
        votes: Binding<Map<T, Map<Candidate, Int>>>,
        results: Binding<Map<T, PartyResult>>,
        private val rowHeaderFunc: (T) -> String
    ) {
        private val header: BindingReceiver<String?> = BindingReceiver(header)
        private val votes: BindingReceiver<Map<T, Map<Candidate, Int>>> = BindingReceiver(votes)
        private val results: BindingReceiver<Map<T, PartyResult>> = BindingReceiver(results)
        private var pctReporting: BindingReceiver<Map<T, Double>>? = null
        private var rowsLimit: BindingReceiver<Int>? = null
        private var numCandidates: BindingReceiver<Int>? = null

        fun withPctReporting(pctReportingBinding: Binding<Map<T, Double>>): Builder<T> {
            pctReporting = BindingReceiver(pctReportingBinding)
            return this
        }

        fun withMaxRows(rowsLimitBinding: Binding<Int>): Builder<T> {
            rowsLimit = BindingReceiver(rowsLimitBinding)
            return this
        }

        fun withNumberOfCandidates(numCandidatesBinding: Binding<Int>): Builder<T> {
            numCandidates = BindingReceiver(numCandidatesBinding)
            return this
        }

        fun build(titleBinding: Binding<String?>): TooCloseToCallScreen {
            val headerLabel = JLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titleBinding.bind { headerLabel.text = it }
            return TooCloseToCallScreen(headerLabel, createFrame())
        }

        private fun createFrame(): MultiSummaryFrame {
            val input = Input<T>()
            votes.getBinding().bind { input.setVotes(it) }
            results.getBinding().bind { input.setResults(it) }
            pctReporting?.getBinding()?.bind { input.setPctReporting(it) }
            rowsLimit?.getBinding()?.bind { input.setMaxRows(it) }
            numCandidates?.getBinding()?.bind { input.setNumCandidates(it) }
            val entries = input.toEntries()
            val thousandsFormatter = DecimalFormat("#,##0")
            val pctFormatter = DecimalFormat("0.0%")
            val frame = MultiSummaryFrame()
            frame.setHeaderBinding(header.getBinding())
            frame.setRowsBinding(
                entries.mapElements {
                    val header = rowHeaderFunc(it.key)
                    val values = run {
                        val ret: MutableList<Pair<Color, String>> = sequenceOf(
                            it.topCandidates.asSequence()
                                .map { v: Map.Entry<Candidate, Int> ->
                                    Pair(
                                        v.key.party.color,
                                        v.key.party.abbreviation.toUpperCase() +
                                                ": " +
                                                thousandsFormatter.format(v.value))
                                },
                            generateSequence { Pair(Color.WHITE, "") })
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
            votesBinding: Binding<Map<T, Map<Candidate, Int>>>,
            resultBinding: Binding<Map<T, PartyResult>>,
            labelFunc: (T) -> String,
            headerBinding: Binding<String?>
        ): Builder<T> {
            return Builder(headerBinding, votesBinding, resultBinding, labelFunc)
        }

        @JvmStatic fun <T> ofParty(
            votesBinding: Binding<Map<T, Map<Party, Int>>>,
            resultBinding: Binding<Map<T, PartyResult>>,
            labelFunc: (T) -> String,
            headerBinding: Binding<String?>
        ): Builder<T> {
            return Builder(
                    headerBinding,
                    votesBinding.map { votes ->
                        votes.mapValues { adjustKey(it.value) { p: Party -> Candidate("", p) } } },
                    resultBinding,
                    labelFunc)
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
