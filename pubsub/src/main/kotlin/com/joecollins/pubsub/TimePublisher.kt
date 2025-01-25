package com.joecollins.pubsub

import com.joecollins.pubsub.TimePublisher.Companion.byClock
import com.joecollins.utils.ExecutorUtils
import java.time.Clock
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class TimePublisher private constructor(tickFrequency: Duration = 100.milliseconds, clock: Clock = Clock.systemDefaultZone()) : AbstractPublisher<Instant>() {

    companion object {
        private val byClock = HashMap<Clock, TimePublisher>()

        fun forClock(clock: Clock = Clock.systemDefaultZone()): TimePublisher = byClock.computeIfAbsent(clock) {
            TimePublisher(clock = clock)
        }
    }

    init {
        ExecutorUtils.scheduleTicking({
            submit(clock.instant())
        }, tickFrequency.inWholeMilliseconds.toInt())
    }

    override fun afterSubscribe() {
    }

    override fun afterUnsubscribe() {
    }
}
