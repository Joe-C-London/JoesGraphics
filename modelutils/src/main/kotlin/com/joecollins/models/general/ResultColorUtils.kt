package com.joecollins.models.general

import com.joecollins.graphics.utils.ColorUtils
import java.awt.Color

@Suppress("UNUSED_PARAMETER")
object ResultColorUtils {

    fun CandidateResult?.getColor(toForceNamedParams: Unit = Unit, default: Color): Color = when {
        this == null -> default
        elected -> leader.party.color
        else -> ColorUtils.lighten(leader.party.color)
    }

    fun PartyResult?.getColor(toForceNamedParams: Unit = Unit, default: Color): Color = when {
        this == null -> default
        this.elected -> leader.color
        else -> ColorUtils.lighten(leader.color)
    }

    fun NonPartisanCandidateResult?.getColor(toForceNamedParams: Unit = Unit, default: Color): Color = when {
        this == null -> default
        this.elected -> leader.color
        else -> ColorUtils.lighten(leader.color)
    }
}
