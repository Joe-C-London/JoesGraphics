package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JPanel

class LowerThirdHeadlineAndSummaryBothEnds : LowerThird() {
    private val headlinePanel = HeadlinePanel()
    private val partySummary = SummaryFromBothEnds()
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

    protected val summaryHeader: String
        get() = partySummary.headline

    fun setSummaryHeaderBinding(summaryHeaderBinding: Binding<String>) {
        partySummary.setHeadlineBinding(summaryHeaderBinding)
    }

    protected val total: Int
        get() = partySummary.total

    fun setTotalBinding(totalBinding: Binding<Int>) {
        partySummary.setTotalBinding(totalBinding)
    }

    protected val left: SummaryFromBothEnds.Entry?
        get() = partySummary.left

    fun setLeftBinding(leftBinding: Binding<SummaryFromBothEnds.Entry?>) {
        partySummary.setLeftBinding(leftBinding)
    }

    protected val right: SummaryFromBothEnds.Entry?
        get() = partySummary.right

    fun setRightBinding(rightBinding: Binding<SummaryFromBothEnds.Entry?>) {
        partySummary.setRightBinding(rightBinding)
    }

    protected val middle: SummaryFromBothEnds.Entry?
        get() = partySummary.middle

    fun setMiddleBinding(middleBinding: Binding<SummaryFromBothEnds.Entry?>) {
        partySummary.setMiddleBinding(middleBinding)
    }

    init {
        val center = JPanel()
        center.layout = GridLayout(1, 2)
        add(center, BorderLayout.CENTER)
        center.add(headlinePanel)
        center.add(partySummary)
    }
}
