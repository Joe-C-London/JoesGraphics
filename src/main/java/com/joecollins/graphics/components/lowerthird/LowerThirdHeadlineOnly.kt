package com.joecollins.graphics.components.lowerthird

import com.joecollins.bindings.Binding
import java.awt.BorderLayout
import java.awt.Image
import java.time.Clock
import java.time.ZoneId

class LowerThirdHeadlineOnly internal constructor(
    leftImageBinding: Binding<Image>,
    placeBinding: Binding<String>,
    timezoneBinding: Binding<ZoneId>,
    private val headlineBinding: Binding<String?>,
    private val subheadBinding: Binding<String?>,
    clock: Clock
) : LowerThird(leftImageBinding, placeBinding, timezoneBinding, clock) {

    constructor(
        leftImageBinding: Binding<Image>,
        placeBinding: Binding<String>,
        timezoneBinding: Binding<ZoneId>,
        headlineBinding: Binding<String?>,
        subheadBinding: Binding<String?>
    ) : this(
        leftImageBinding,
        placeBinding,
        timezoneBinding,
        headlineBinding,
        subheadBinding,
        Clock.systemDefaultZone()
    )

    private val headlinePanel = HeadlinePanel()

    val headline: String?
        get() = headlinePanel.headline

    val subhead: String?
        get() = headlinePanel.subhead

    init {
        add(headlinePanel, BorderLayout.CENTER)
        this.headlineBinding.bind { headlinePanel.headline = it }
        this.subheadBinding.bind { headlinePanel.subhead = it }
    }
}
