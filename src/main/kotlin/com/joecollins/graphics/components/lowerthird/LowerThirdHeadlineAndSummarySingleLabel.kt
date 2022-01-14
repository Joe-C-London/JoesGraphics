package com.joecollins.graphics.components.lowerthird

import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.Image
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.Flow
import javax.swing.JPanel

class LowerThirdHeadlineAndSummarySingleLabel internal constructor(
    leftImagePublisher: Flow.Publisher<out Image>,
    placePublisher: Flow.Publisher<out String>,
    timezonePublisher: Flow.Publisher<out ZoneId>,
    private val headlinePublisher: Flow.Publisher<out String?>,
    private val subheadPublisher: Flow.Publisher<out String?>,
    summaryHeaderPublisher: Flow.Publisher<out String>,
    summaryEntriesPublisher: Flow.Publisher<out List<SummaryWithoutLabels.Entry>>,
    clock: Clock,
    showTimeZone: Boolean = false
) : LowerThird(leftImagePublisher, placePublisher, timezonePublisher, clock, showTimeZone) {

    constructor(
        leftImagePublisher: Flow.Publisher<out Image>,
        placePublisher: Flow.Publisher<out String>,
        timezonePublisher: Flow.Publisher<out ZoneId>,
        headlinePublisher: Flow.Publisher<out String?>,
        subheadPublisher: Flow.Publisher<out String?>,
        summaryHeaderPublisher: Flow.Publisher<out String>,
        summaryEntriesPublisher: Flow.Publisher<out List<SummaryWithoutLabels.Entry>>,
        showTimeZone: Boolean = false
    ) : this(
        leftImagePublisher,
        placePublisher,
        timezonePublisher,
        headlinePublisher,
        subheadPublisher,
        summaryHeaderPublisher,
        summaryEntriesPublisher,
        Clock.systemDefaultZone(),
        showTimeZone
    )

    private val headlinePanel = HeadlinePanel()
    private val partySummary = SummaryWithoutLabels(summaryHeaderPublisher, summaryEntriesPublisher)

    internal val headline: String?
        get() = headlinePanel.headline

    internal val subhead: String?
        get() = headlinePanel.subhead

    internal val summaryHeader: String
        get() = partySummary.headline

    internal val numSummaryEntries: Int
        get() = partySummary.numEntries

    internal fun getEntryColor(index: Int): Color {
        return partySummary.getEntryColor(index)
    }

    internal fun getEntryValue(index: Int): String {
        return partySummary.getEntryValue(index)
    }

    init {
        val center = JPanel()
        center.layout = GridLayout(1, 2)
        add(center, BorderLayout.CENTER)
        center.add(headlinePanel)
        center.add(partySummary)
        this.headlinePublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.headline = it }))
        this.subheadPublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.subhead = it }))
    }
}
