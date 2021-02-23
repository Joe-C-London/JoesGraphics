package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import java.awt.BorderLayout

class LowerThirdHeadlineOnly : LowerThird() {
    private val headlinePanel = HeadlinePanel()
    private var headlineBinding = fixedBinding("")
    private var subheadBinding = fixedBinding<String?>(null)

    val headline: String?
        get() = headlinePanel.headline

    fun setHeadlineBinding(headlineBinding: Binding<String>) {
        this.headlineBinding.unbind()
        this.headlineBinding = headlineBinding
        this.headlineBinding.bind { headlinePanel.headline = it }
    }

    val subhead: String?
        get() = headlinePanel.subhead

    fun setSubheadBinding(subheadBinding: Binding<String?>) {
        this.subheadBinding.unbind()
        this.subheadBinding = subheadBinding
        this.subheadBinding.bind { headlinePanel.subhead = it }
    }

    init {
        add(headlinePanel, BorderLayout.CENTER)
    }
}
