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

class LowerThirdHeadlineAndSummary internal constructor(
    leftImagePublisher: Flow.Publisher<out Image>,
    placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
    private val headlineBinding: Flow.Publisher<out String?>,
    private val subheadBinding: Flow.Publisher<out String?>,
    summaryEntriesBinding: Flow.Publisher<out List<SummaryWithLabels.Entry>>,
    clock: Clock,
    showTimeZone: Boolean = false
) : LowerThird(leftImagePublisher, placePublisher, clock, showTimeZone) {

    constructor(
        leftImagePublisher: Flow.Publisher<out Image>,
        placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
        headlinePublisher: Flow.Publisher<out String?>,
        subheadPublisher: Flow.Publisher<out String?>,
        summaryEntriesPublisher: Flow.Publisher<out List<SummaryWithLabels.Entry>>,
        showTimeZone: Boolean = false
    ) : this(
        leftImagePublisher,
        placePublisher,
        headlinePublisher,
        subheadPublisher,
        summaryEntriesPublisher,
        Clock.systemDefaultZone(),
        showTimeZone
    )

    private val headlinePanel = HeadlinePanel()
    private val partySummary = SummaryWithLabels(summaryEntriesBinding)

    internal val headline: String?
        get() = headlinePanel.headline

    internal val subhead: String?
        get() = headlinePanel.subhead

    internal val numSummaryEntries: Int
        get() = partySummary.numEntries

    internal fun getEntryColor(index: Int): Color {
        return partySummary.getEntryColor(index)
    }

    internal fun getEntryLabel(index: Int): String {
        return partySummary.getEntryLabel(index)
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
        this.headlineBinding.subscribe(Subscriber(eventQueueWrapper { headlinePanel.headline = it }))
        this.subheadBinding.subscribe(Subscriber(eventQueueWrapper { headlinePanel.subhead = it }))
    }
}
