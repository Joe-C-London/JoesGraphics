package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import java.awt.BorderLayout
import java.awt.GridLayout
import java.awt.Image
import java.time.Clock
import java.time.ZoneId
import javax.swing.JPanel

class LowerThirdHeadlineAndSummaryBothEnds internal constructor(
    leftImageBinding: Binding<Image>,
    placeBinding: Binding<String>,
    timezoneBinding: Binding<ZoneId>,
    private val headlineBinding: Binding<String?>,
    private val subheadBinding: Binding<String?>,
    summaryHeaderBinding: Binding<String>,
    summaryTotalBinding: Binding<Int>,
    summaryLeftBinding: Binding<SummaryFromBothEnds.Entry?>,
    summaryRightBinding: Binding<SummaryFromBothEnds.Entry?>,
    summaryMiddleBinding: Binding<SummaryFromBothEnds.Entry?> = Binding.fixedBinding(null),
    clock: Clock,
    showTimeZone: Boolean = false
) : LowerThird(leftImageBinding, placeBinding, timezoneBinding, clock, showTimeZone) {

    constructor(
        leftImageBinding: Binding<Image>,
        placeBinding: Binding<String>,
        timezoneBinding: Binding<ZoneId>,
        headlineBinding: Binding<String?>,
        subheadBinding: Binding<String?>,
        summaryHeaderBinding: Binding<String>,
        summaryTotalBinding: Binding<Int>,
        summaryLeftBinding: Binding<SummaryFromBothEnds.Entry?>,
        summaryRightBinding: Binding<SummaryFromBothEnds.Entry?>,
        summaryMiddleBinding: Binding<SummaryFromBothEnds.Entry?> = Binding.fixedBinding(null),
        showTimeZone: Boolean = false
    ) : this(
        leftImageBinding,
        placeBinding,
        timezoneBinding,
        headlineBinding,
        subheadBinding,
        summaryHeaderBinding,
        summaryTotalBinding,
        summaryLeftBinding,
        summaryRightBinding,
        summaryMiddleBinding,
        Clock.systemDefaultZone(),
        showTimeZone
    )

    private val headlinePanel = HeadlinePanel()
    private val partySummary = SummaryFromBothEnds(summaryHeaderBinding, summaryTotalBinding, summaryLeftBinding, summaryRightBinding, summaryMiddleBinding)

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
        this.headlineBinding.bind { headlinePanel.headline = it }
        this.subheadBinding.bind { headlinePanel.subhead = it }
    }
}
