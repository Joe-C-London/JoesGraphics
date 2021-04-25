package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel

class LowerThirdHeadlineAndSummarySingleLabel : LowerThird() {
    private val headlinePanel = HeadlinePanel()
    private val partySummary = SummaryWithoutLabels()
    private var headlineBinding = Binding.fixedBinding<String?>("")
    private var subheadBinding = Binding.fixedBinding<String?>(null)

    internal val headline: String?
        get() = headlinePanel.headline

    fun setHeadlineBinding(headlineBinding: Binding<String?>) {
        this.headlineBinding.unbind()
        this.headlineBinding = headlineBinding
        this.headlineBinding.bind { headlinePanel.headline = it }
    }

    internal val subhead: String?
        get() = headlinePanel.subhead

    fun setSubheadBinding(subheadBinding: Binding<String?>) {
        this.subheadBinding.unbind()
        this.subheadBinding = subheadBinding
        this.subheadBinding.bind { headlinePanel.subhead = it }
    }

    internal val summaryHeader: String
        get() = partySummary.headline

    fun setSummaryHeaderBinding(summaryHeaderBinding: Binding<String>) {
        partySummary.setHeadlineBinding(summaryHeaderBinding)
    }

    internal val numSummaryEntries: Int
        get() = partySummary.numEntries

    fun setSummaryEntriesBinding(entriesBinding: Binding<List<SummaryWithoutLabels.Entry>>) {
        partySummary.setEntriesBinding(entriesBinding)
    }

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
    }
}
