package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.Image
import java.time.Clock
import java.time.ZoneId
import javax.swing.JPanel

class LowerThirdHeadlineAndSummary internal constructor(
    leftImageBinding: Binding<Image>,
    placeBinding: Binding<String>,
    timezoneBinding: Binding<ZoneId>,
    private val headlineBinding: Binding<String?>,
    private val subheadBinding: Binding<String?>,
    summaryEntriesBinding: Binding<List<SummaryWithLabels.Entry>>,
    clock: Clock
) : LowerThird(leftImageBinding, placeBinding, timezoneBinding, clock) {

    constructor(
        leftImageBinding: Binding<Image>,
        placeBinding: Binding<String>,
        timezoneBinding: Binding<ZoneId>,
        headlineBinding: Binding<String?>,
        subheadBinding: Binding<String?>,
        summaryEntriesBinding: Binding<List<SummaryWithLabels.Entry>>
    ) : this(
        leftImageBinding,
        placeBinding,
        timezoneBinding,
        headlineBinding,
        subheadBinding,
        summaryEntriesBinding,
        Clock.systemDefaultZone()
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
        this.headlineBinding.bind { headlinePanel.headline = it }
        this.subheadBinding.bind { headlinePanel.subhead = it }
    }
}
