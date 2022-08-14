package com.joecollins.models.general

import com.joecollins.graphics.utils.ColorUtils
import java.awt.Color

data class CandidateResult(val candidate: Candidate?, val elected: Boolean) {

    val isElected = elected

    val color: Color get() {
        return when {
            candidate == null -> Color.BLACK
            elected -> candidate.party.color
            else -> ColorUtils.lighten(candidate.party.color)
        }
    }

    val winner = candidate?.takeIf { elected }

    fun toPartyResult(): PartyResult {
        return PartyResult(candidate?.party, elected)
    }

    companion object {
        @JvmField val NO_RESULT = CandidateResult(null, false)

        @JvmField val TIE = CandidateResult(Candidate("TIE", Party("TIE", "TIE", Color.DARK_GRAY)), false)

        fun elected(Candidate: Candidate?) = CandidateResult(Candidate, true)

        fun leading(Candidate: Candidate?) = CandidateResult(Candidate, false)
    }
}
