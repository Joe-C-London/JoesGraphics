package com.joecollins.graphics.components.lowerthird

import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import org.jetbrains.annotations.TestOnly
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.GridLayout
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.Flow
import javax.swing.JPanel

class LowerThirdHeadlineAndSummaryHeaderAndLabels @TestOnly constructor(
    leftImagePublisher: Flow.Publisher<(Graphics2D) -> Dimension>,
    placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
    private val headlinePublisher: Flow.Publisher<out String?>,
    private val subheadPublisher: Flow.Publisher<out String?>,
    summaryHeaderPublisher: Flow.Publisher<out String>,
    summaryEntriesPublisher: Flow.Publisher<out List<SummaryEntry>>,
    clock: Clock,
    showTimeZone: Boolean = false,
) : LowerThird(leftImagePublisher, placePublisher, clock, showTimeZone) {

    constructor(
        leftImagePublisher: Flow.Publisher<(Graphics2D) -> Dimension>,
        placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
        headlinePublisher: Flow.Publisher<out String?>,
        subheadPublisher: Flow.Publisher<out String?>,
        summaryHeaderPublisher: Flow.Publisher<out String>,
        summaryEntriesPublisher: Flow.Publisher<out List<SummaryEntry>>,
        showTimeZone: Boolean = false,
    ) : this(
        leftImagePublisher,
        placePublisher,
        headlinePublisher,
        subheadPublisher,
        summaryHeaderPublisher,
        summaryEntriesPublisher,
        Clock.systemDefaultZone(),
        showTimeZone,
    )

    private val headlinePanel = HeadlinePanel()
    private val partySummary = SummaryWithHeaderAndLabels(summaryHeaderPublisher, summaryEntriesPublisher)

    internal val headline: String?
        get() = headlinePanel.headline

    internal val subhead: String?
        get() = headlinePanel.subhead

    internal val summaryHeader: String
        get() = partySummary.headline

    internal val numSummaryEntries: Int
        get() = partySummary.numEntries

    internal fun getEntryColor(index: Int): Color = partySummary.getEntryColor(index)

    internal fun getEntryLabel(index: Int): String = partySummary.getEntryLabel(index)

    internal fun getEntryValue(index: Int): String = partySummary.getEntryValue(index)

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
