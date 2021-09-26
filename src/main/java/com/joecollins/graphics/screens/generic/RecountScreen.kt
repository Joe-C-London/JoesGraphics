package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.mapElements
import com.joecollins.bindings.toFixedBinding
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Candidate
import java.awt.BorderLayout
import java.awt.Color
import java.text.DecimalFormat
import java.util.HashMap
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class RecountScreen private constructor(headerLabel: JLabel, frame: MultiSummaryFrame) : JPanel() {

    init {
        background = Color.WHITE
        border = EmptyBorder(5, 5, 5, 5)
        layout = BorderLayout()
        add(headerLabel, BorderLayout.NORTH)
        add(frame, BorderLayout.CENTER)
    }

    companion object {
        fun <T> of(
            candidateVotes: Binding<Map<T, Map<Candidate, Int>>>,
            rowHeaderFunc: (T) -> String,
            voteThreshold: Int,
            header: Binding<String>
        ): Builder<T> {
            return Builder(header, candidateVotes, rowHeaderFunc, voteThreshold = voteThreshold)
        }

        fun <T> of(
            candidateVotes: Binding<Map<T, Map<Candidate, Int>>>,
            rowHeaderFunc: (T) -> String,
            pctThreshold: Double,
            header: Binding<String>
        ): Builder<T> {
            return Builder(header, candidateVotes, rowHeaderFunc, pctThreshold = pctThreshold)
        }
    }

    private class Input<T>(val voteThreshold: Int?, val pctThreshold: Double?) : Bindable<Input<T>, Input.Property>() {
        enum class Property {
            VOTES, PCT_REPORTING
        }

        private var votes: Map<T, Map<Candidate, Int>> = HashMap()
        private var pctReporting: Map<T, Double>? = null

        fun setVotes(votes: Map<T, Map<Candidate, Int>>) {
            this.votes = votes
            onPropertyRefreshed(Property.VOTES)
        }

        fun setPctReporting(pctReporting: Map<T, Double>) {
            this.pctReporting = pctReporting
            onPropertyRefreshed(Property.PCT_REPORTING)
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
                                t.pctReporting?.get(it.key) ?: 1.0,
                                voteThreshold,
                                pctThreshold
                            )
                        }
                        .filter { it.votes.values.sum() > 0 }
                        .filter { it.pctReporting == null || it.pctReporting >= 1.0 - 1e-6 }
                        .filter { it.isTooClose }
                        .sortedBy { if (it.pctThreshold == null) it.margin.toDouble() else it.pctMargin }
                        .toList()
                },
                Property.VOTES,
                Property.PCT_REPORTING)
        }
    }

    private class Entry<T> constructor(
        val key: T,
        val votes: Map<Candidate, Int>,
        val pctReporting: Double?,
        val voteThreshold: Int?,
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
        private val header: Binding<String>,
        private val candidateVotes: Binding<Map<T, Map<Candidate, Int>>>,
        private val rowHeaderFunc: (T) -> String,
        private val voteThreshold: Int? = null,
        private val pctThreshold: Double? = null
    ) {

        private var pctReporting: Binding<Map<T, Double>>? = null

        fun withPctReporting(pctReporting: Binding<Map<T, Double>>): Builder<T> {
            this.pctReporting = pctReporting
            return this
        }

        fun build(titleBinding: Binding<String>): RecountScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            titleBinding.bind { headerLabel.text = it }
            return RecountScreen(headerLabel, buildFrame())
        }

        private fun buildFrame(): MultiSummaryFrame {
            val voteFormatter = DecimalFormat("#,##0")
            val pctFormatter = DecimalFormat("0.00%")
            val input = Input<T>(voteThreshold, pctThreshold)
            candidateVotes.bind { input.setVotes(it) }
            pctReporting?.bind { input.setPctReporting(it) }
            return MultiSummaryFrame(
                headerBinding = header,
                rowsBinding = input.toEntries().mapElements { e ->
                    val partyCells = e.topCandidates.take(2)
                        .map { it.key.party.color to "${it.key.party.abbreviation.uppercase()}: ${voteFormatter.format(it.value)}" }
                    val marginCell = Color.WHITE to "MARGIN: ${e.margin}" + (if (e.pctThreshold == null) "" else " (${pctFormatter.format(e.pctMargin)})")
                    MultiSummaryFrame.Row(rowHeaderFunc(e.key), listOf(partyCells, listOf(marginCell)).flatten())
                },
                notesBinding = when {
                    voteThreshold != null -> "Automatic recount triggered if the margin is $voteThreshold votes or fewer"
                    pctThreshold != null -> "Automatic recount triggered if the margin is ${pctFormatter.format(pctThreshold)} or less"
                    else -> null
                }.toFixedBinding()
            )
        }
    }
}
