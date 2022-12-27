package com.joecollins.graphics.components.lowerthird

import com.joecollins.models.general.PartyOrCoalition
import java.awt.Color
import java.text.DecimalFormat

object SummaryEntriesSingleLabel {

    fun createSeatEntries(seats: Map<out PartyOrCoalition, Int>): List<SummaryWithoutLabels.Entry> {
        val ret = seats.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.key.overrideSortOrder ?: it.value }
            .map { SummaryWithoutLabels.Entry(it.key.color, it.value.toString()) }
            .toMutableList()
        if (ret.isEmpty()) {
            ret.add(SummaryWithoutLabels.Entry(Color.WHITE, "WAITING..."))
        }
        return ret
    }

    fun createVoteEntries(votes: Map<out PartyOrCoalition, Int>): List<SummaryWithoutLabels.Entry> {
        val total = votes.values.sum().toDouble()
        val ret = votes.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.key.overrideSortOrder ?: it.value }
            .map { SummaryWithoutLabels.Entry(it.key.color, DecimalFormat("0.0%").format(it.value / total)) }
            .toMutableList()
        if (ret.isEmpty()) {
            ret.add(SummaryWithoutLabels.Entry(Color.WHITE, "WAITING..."))
        }
        return ret
    }
}
