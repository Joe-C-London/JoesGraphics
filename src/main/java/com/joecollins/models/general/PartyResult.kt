package com.joecollins.models.general

import com.joecollins.graphics.utils.ColorUtils
import java.awt.Color

data class PartyResult(val party: Party?, val elected: Boolean) {

    val isElected = elected

    val color: Color get() {
        return when {
            party == null -> Color.BLACK
            elected -> party.color
            else -> ColorUtils.lighten(party.color)
        }
    }

    companion object {
        @JvmField val NO_RESULT = PartyResult(null, false)

        @JvmStatic fun elected(party: Party?) = PartyResult(party, true)

        @JvmStatic fun leading(party: Party?) = PartyResult(party, false)
    }
}
