package com.joecollins.models.general

import kotlin.contracts.contract

data class NonPartisanCandidateResult(override val leader: NonPartisanCandidate, override val elected: Boolean) : ElectionResult<NonPartisanCandidate> {

    companion object {

        @OptIn(kotlin.contracts.ExperimentalContracts::class)
        fun elected(candidate: NonPartisanCandidate?): NonPartisanCandidateResult? {
            contract {
                returnsNotNull() implies (candidate != null)
                returns(null) implies (candidate == null)
            }
            return if (candidate == null) null else NonPartisanCandidateResult(candidate, true)
        }

        @JvmName("electedNotNull")
        fun elected(candidate: NonPartisanCandidate) = NonPartisanCandidateResult(candidate, true)

        @OptIn(kotlin.contracts.ExperimentalContracts::class)
        fun leading(candidate: NonPartisanCandidate?): NonPartisanCandidateResult? {
            contract {
                returnsNotNull() implies (candidate != null)
                returns(null) implies (candidate == null)
            }
            return if (candidate == null) null else NonPartisanCandidateResult(candidate, false)
        }

        @JvmName("leadingNotNull")
        fun leading(candidate: NonPartisanCandidate) = NonPartisanCandidateResult(candidate, false)
    }
}
