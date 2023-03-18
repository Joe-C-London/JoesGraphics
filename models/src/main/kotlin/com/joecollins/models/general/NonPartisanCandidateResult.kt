package com.joecollins.models.general

data class NonPartisanCandidateResult(val candidate: NonPartisanCandidate, val elected: Boolean) {

    val isElected = elected

    val winner = candidate.takeIf { elected }

    companion object {

        fun elected(candidate: NonPartisanCandidate?) = if (candidate == null) null else NonPartisanCandidateResult(candidate, true)

        fun leading(candidate: NonPartisanCandidate?) = if (candidate == null) null else NonPartisanCandidateResult(candidate, false)
    }
}
