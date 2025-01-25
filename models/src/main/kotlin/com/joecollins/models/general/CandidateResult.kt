package com.joecollins.models.general

import kotlin.contracts.contract

data class CandidateResult(override val leader: Candidate, override val elected: Boolean) : ElectionResult<Candidate> {

    fun toPartyResult(): PartyResult = PartyResult(leader.party, elected)

    companion object {

        val TIE = CandidateResult(Candidate("", Party.TIE), false)

        @OptIn(kotlin.contracts.ExperimentalContracts::class)
        fun elected(candidate: Candidate?): CandidateResult? {
            contract {
                returnsNotNull() implies (candidate != null)
                returns(null) implies (candidate == null)
            }
            return if (candidate == null) null else CandidateResult(candidate, true)
        }

        @JvmName("electedNotNull")
        fun elected(candidate: Candidate) = CandidateResult(candidate, true)

        @OptIn(kotlin.contracts.ExperimentalContracts::class)
        fun leading(candidate: Candidate?): CandidateResult? {
            contract {
                returnsNotNull() implies (candidate != null)
                returns(null) implies (candidate == null)
            }
            return if (candidate == null) null else CandidateResult(candidate, false)
        }

        @JvmName("leadingNotNull")
        fun leading(candidate: Candidate) = CandidateResult(candidate, false)
    }
}
