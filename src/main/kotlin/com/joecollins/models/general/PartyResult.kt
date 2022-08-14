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

    val winner = party?.takeIf { elected }

    companion object {
        @JvmField val NO_RESULT = PartyResult(null, false)

        @JvmField val TIE = PartyResult(Party("TIE", "TIE", Color.DARK_GRAY), false)

        fun elected(party: Party?) = PartyResult(party, true)

        fun leading(party: Party?) = PartyResult(party, false)
    }
}
