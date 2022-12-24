package com.joecollins.models.general

data class CandidateResult(val candidate: Candidate, val elected: Boolean) {

    val isElected = elected

    val winner = candidate.takeIf { elected }

    fun toPartyResult(): PartyResult {
        return PartyResult(candidate.party, elected)
    }

    companion object {

        val TIE = CandidateResult(Candidate("", Party.TIE), false)

        fun elected(candidate: Candidate?) = if (candidate == null) null else CandidateResult(candidate, true)

        fun leading(candidate: Candidate?) = if (candidate == null) null else CandidateResult(candidate, false)
    }
}
