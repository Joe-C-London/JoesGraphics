package com.joecollins.graphics.components.lowerthird

import com.joecollins.models.general.Candidate
import com.joecollins.models.general.PartyOrCoalition
import java.awt.Color
import java.text.DecimalFormat

object SummaryEntries {
    fun createSeatEntries(seats: Map<out PartyOrCoalition, Int>, totalSeats: Int = 0, partiesToShow: Set<PartyOrCoalition> = emptySet()): List<SummaryEntry> {
        val ret = sequenceOf(
            partiesToShow.asSequence(),
            seats.entries.asSequence().filter { it.value > 0 }.map { it.key },
        )
            .flatten()
            .distinct()
            .map { it to (seats[it] ?: 0) }
            .sortedByDescending { it.first.overrideSortOrder ?: it.second }
            .map { SummaryEntry(it.first.color, it.first.abbreviation, it.second.toString()) }
            .toMutableList()
        val inDoubt = totalSeats - seats.values.sum()
        if (inDoubt > 0) {
            ret.add(SummaryEntry(Color.WHITE, "?", inDoubt.toString()))
        }
        return ret
    }

    fun createVoteEntries(votes: Map<out PartyOrCoalition, Int>): List<SummaryEntry> {
        val total = votes.values.sum().toDouble()
        val ret = votes.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.key.overrideSortOrder ?: it.value }
            .map {
                SummaryEntry(
                    it.key.color,
                    it.key.abbreviation,
                    DecimalFormat("0.0%").format(it.value / total),
                )
            }
            .toMutableList()
        if (ret.isEmpty()) {
            ret.add(SummaryEntry(Color.WHITE, "", "WAITING..."))
        }
        return ret
    }

    fun createVoteEntries(votes: Map<Candidate, Int>, labelFunc: (Candidate) -> String): List<SummaryEntry> {
        val total = votes.values.sum().toDouble()
        val ret = votes.entries
            .filter { it.value > 0 }
            .sortedByDescending { it.key.overrideSortOrder ?: it.value }
            .map {
                SummaryEntry(
                    it.key.party.color,
                    if (it.key == Candidate.OTHERS) Candidate.OTHERS.party.abbreviation else labelFunc(it.key),
                    DecimalFormat("0.0%").format(it.value / total),
                )
            }
            .toMutableList()
        if (ret.isEmpty()) {
            ret.add(SummaryEntry(Color.WHITE, "", "WAITING..."))
        }
        return ret
    }
}
