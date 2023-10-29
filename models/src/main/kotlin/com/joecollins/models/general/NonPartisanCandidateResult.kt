package com.joecollins.models.general

data class NonPartisanCandidateResult(override val leader: NonPartisanCandidate, override val elected: Boolean) : ElectionResult<NonPartisanCandidate> {

    companion object {

        fun elected(candidate: NonPartisanCandidate?) = if (candidate == null) null else NonPartisanCandidateResult(candidate, true)

        fun leading(candidate: NonPartisanCandidate?) = if (candidate == null) null else NonPartisanCandidateResult(candidate, false)
    }
}
