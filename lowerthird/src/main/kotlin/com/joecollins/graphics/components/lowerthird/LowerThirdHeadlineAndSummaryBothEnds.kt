package com.joecollins.graphics.components.lowerthird

import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Image
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.Flow
import javax.swing.JPanel

class LowerThirdHeadlineAndSummaryBothEnds internal constructor(
    leftImagePublisher: Flow.Publisher<out Image>,
    placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
    private val headlinePublisher: Flow.Publisher<out String?>,
    private val subheadPublisher: Flow.Publisher<out String?>,
    summaryHeaderPublisher: Flow.Publisher<out String>,
    summaryTotalPublisher: Flow.Publisher<out Int>,
    summaryLeftPublisher: Flow.Publisher<out SummaryFromBothEnds.Entry?>,
    summaryRightPublisher: Flow.Publisher<out SummaryFromBothEnds.Entry?>,
    summaryMiddlePublisher: Flow.Publisher<out SummaryFromBothEnds.Entry?> = (null as SummaryFromBothEnds.Entry?).asOneTimePublisher(),
    clock: Clock,
    showTimeZone: Boolean = false
) : LowerThird(leftImagePublisher, placePublisher, clock, showTimeZone) {

    constructor(
        leftImagePublisher: Flow.Publisher<out Image>,
        placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
        headlinePublisher: Flow.Publisher<out String?>,
        subheadPublisher: Flow.Publisher<out String?>,
        summaryHeaderPublisher: Flow.Publisher<out String>,
        summaryTotalPublisher: Flow.Publisher<out Int>,
        summaryLeftPublisher: Flow.Publisher<out SummaryFromBothEnds.Entry?>,
        summaryRightPublisher: Flow.Publisher<out SummaryFromBothEnds.Entry?>,
        summaryMiddlePublisher: Flow.Publisher<out SummaryFromBothEnds.Entry?> = (null as SummaryFromBothEnds.Entry?).asOneTimePublisher(),
        showTimeZone: Boolean = false
    ) : this(
        leftImagePublisher,
        placePublisher,
        headlinePublisher,
        subheadPublisher,
        summaryHeaderPublisher,
        summaryTotalPublisher,
        summaryLeftPublisher,
        summaryRightPublisher,
        summaryMiddlePublisher,
        Clock.systemDefaultZone(),
        showTimeZone
    )

    private val headlinePanel = HeadlinePanel()
    private val partySummary = SummaryFromBothEnds(summaryHeaderPublisher, summaryTotalPublisher, summaryLeftPublisher, summaryRightPublisher, summaryMiddlePublisher)

    internal val headline: String?
        get() = headlinePanel.headline

    internal val subhead: String?
        get() = headlinePanel.subhead

    internal val summaryHeader: String
        get() = partySummary.headline

    internal val total: Int
        get() = partySummary.total

    internal val left: SummaryFromBothEnds.Entry?
        get() = partySummary.left

    internal val right: SummaryFromBothEnds.Entry?
        get() = partySummary.right

    internal val middle: SummaryFromBothEnds.Entry?
        get() = partySummary.middle

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
