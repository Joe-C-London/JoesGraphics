package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import javax.swing.JPanel

class LowerThirdHeadlineAndSummary : LowerThird() {
    private val headlinePanel = HeadlinePanel()
    private val partySummary = SummaryWithLabels()
    private var headlineBinding = Binding.fixedBinding<String?>("")
    private var subheadBinding = Binding.fixedBinding<String?>(null)

    protected val headline: String?
        get() = headlinePanel.headline

    fun setHeadlineBinding(headlineBinding: Binding<String?>) {
        this.headlineBinding.unbind()
        this.headlineBinding = headlineBinding
        this.headlineBinding.bind { headlinePanel.headline = it }
    }

    protected val subhead: String?
        get() = headlinePanel.subhead

    fun setSubheadBinding(subheadBinding: Binding<String?>) {
        this.subheadBinding.unbind()
        this.subheadBinding = subheadBinding
        this.subheadBinding.bind { headlinePanel.subhead = it }
    }

    fun setNumSummaryEntriesBinding(numEntriesBinding: Binding<Int>) {
        partySummary.setNumEntriesBinding(numEntriesBinding)
    }

    protected val numSummaryEntries: Int
        get() = partySummary.numEntries

    fun setSummaryEntriesBinding(entriesBinding: IndexedBinding<SummaryWithLabels.Entry>) {
        partySummary.setEntriesBinding(entriesBinding)
    }

    protected fun getEntryColor(index: Int): Color {
        return partySummary.getEntryColor(index)
    }

    protected fun getEntryLabel(index: Int): String {
        return partySummary.getEntryLabel(index)
    }

    protected fun getEntryValue(index: Int): String {
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
