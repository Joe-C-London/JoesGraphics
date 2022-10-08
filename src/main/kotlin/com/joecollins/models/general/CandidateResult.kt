package com.joecollins.models.general

import com.joecollins.graphics.utils.ColorUtils
import java.awt.Color

data class CandidateResult(val candidate: Candidate, val elected: Boolean) {

    val isElected = elected

    val winner = candidate.takeIf { elected }

    fun toPartyResult(): PartyResult {
        return PartyResult(candidate.party, elected)
    }

    companion object {
        val CandidateResult?.color: Color get() {
            return when {
                this == null -> Color.BLACK
                elected -> candidate.party.color
                else -> ColorUtils.lighten(candidate.party.color)
            }
        }

        fun elected(candidate: Candidate?) = if (candidate == null) null else CandidateResult(candidate, true)

        fun leading(candidate: Candidate?) = if (candidate == null) null else CandidateResult(candidate, false)
    }
}
