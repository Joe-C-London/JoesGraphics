package com.joecollins.graphics.components.lowerthird

import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Image
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.Flow

class LowerThirdHeadlineOnly internal constructor(
    leftImagePublisher: Flow.Publisher<out Image>,
    placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
    private val headlinePublisher: Flow.Publisher<out String?>,
    private val subheadPublisher: Flow.Publisher<out String?>,
    clock: Clock,
    showTimeZone: Boolean = false,
) : LowerThird(leftImagePublisher, placePublisher, clock, showTimeZone) {

    constructor(
        leftImagePublisher: Flow.Publisher<out Image>,
        placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
        headlinePublisher: Flow.Publisher<out String?>,
        subheadPublisher: Flow.Publisher<out String?>,
        showTimeZone: Boolean = false,
    ) : this(
        leftImagePublisher,
        placePublisher,
        headlinePublisher,
        subheadPublisher,
        Clock.systemDefaultZone(),
        showTimeZone,
    )

    private val headlinePanel = HeadlinePanel()

    val headline: String?
        get() = headlinePanel.headline

    val subhead: String?
        get() = headlinePanel.subhead

    init {
        add(headlinePanel, BorderLayout.CENTER)
        this.headlinePublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.headline = it }))
        this.subheadPublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.subhead = it }))
    }
}
