package com.joecollins.graphics.components.lowerthird

import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import org.jetbrains.annotations.TestOnly
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Image
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.Flow

class LowerThirdHeadlineAndBottomSummary @TestOnly constructor(
    leftImagePublisher: Flow.Publisher<out Image>,
    placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
    private val headlinePublisher: Flow.Publisher<out String?>,
    private val subheadPublisher: Flow.Publisher<out String?>,
    summaryHeaderPublisher: Flow.Publisher<out String>,
    summaryFooterPublisher: Flow.Publisher<out String>,
    summaryEntriesPublisher: Flow.Publisher<out List<SummaryEntry>>,
    clock: Clock,
    showTimeZone: Boolean = false
) : LowerThird(leftImagePublisher, placePublisher, clock, showTimeZone) {

    constructor(
        leftImagePublisher: Flow.Publisher<out Image>,
        placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
        headlinePublisher: Flow.Publisher<out String?>,
        subheadPublisher: Flow.Publisher<out String?>,
        summaryHeaderPublisher: Flow.Publisher<out String>,
        summaryFooterPublisher: Flow.Publisher<out String>,
        summaryEntriesPublisher: Flow.Publisher<out List<SummaryEntry>>,
        showTimeZone: Boolean = false
    ) : this(
        leftImagePublisher,
        placePublisher,
        headlinePublisher,
        subheadPublisher,
        summaryHeaderPublisher,
        summaryFooterPublisher,
        summaryEntriesPublisher,
        Clock.systemDefaultZone(),
        showTimeZone
    )

    private val headlinePanel = HeadlinePanel()
    private val partySummary = BottomSummary(summaryHeaderPublisher, summaryFooterPublisher, summaryEntriesPublisher)

    internal val headline: String?
        get() = headlinePanel.headline

    internal val subhead: String?
        get() = headlinePanel.subhead

    internal val summaryHeader: String
        get() = partySummary.header

    internal val summaryFooter: String
        get() = partySummary.footer

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
        preferredSize = Dimension(1024, 70)
        add(headlinePanel, BorderLayout.CENTER)
        add(partySummary, BorderLayout.SOUTH)
        this.headlinePublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.headline = it }))
        this.subheadPublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.subhead = it }))
    }
}
