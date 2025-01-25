package com.joecollins.models.general

data class PollsReporting(val reporting: Int, val total: Int) {
    operator fun plus(other: PollsReporting): PollsReporting = PollsReporting(this.reporting + other.reporting, this.total + other.total)
    operator fun minus(other: PollsReporting): PollsReporting = PollsReporting(this.reporting - other.reporting, this.total - other.total)
    fun toPct(): Double = reporting.toDouble() / total.coerceAtLeast(1)
}
