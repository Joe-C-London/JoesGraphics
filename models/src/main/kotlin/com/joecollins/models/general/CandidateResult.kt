package com.joecollins.models.general

data class CandidateResult(override val leader: Candidate, override val elected: Boolean) : ElectionResult<Candidate> {

    fun toPartyResult(): PartyResult {
        return PartyResult(leader.party, elected)
    }

    companion object {

        val TIE = CandidateResult(Candidate("", Party.TIE), false)

        fun elected(candidate: Candidate?) = if (candidate == null) null else CandidateResult(candidate, true)

        fun leading(candidate: Candidate?) = if (candidate == null) null else CandidateResult(candidate, false)
    }
}
