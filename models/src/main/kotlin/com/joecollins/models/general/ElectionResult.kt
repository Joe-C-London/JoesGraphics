package com.joecollins.models.general

interface ElectionResult<T> {
    val leader: T
    val elected: Boolean

    val winner: T? get() = leader.takeIf { elected }
}
