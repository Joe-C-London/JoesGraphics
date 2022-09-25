package com.joecollins.models.general

import com.joecollins.graphics.utils.ColorUtils
import java.awt.Color

data class PartyResult(val party: Party, val elected: Boolean) {

    val isElected = elected

    val winner = party.takeIf { elected }

    companion object {
        @JvmField val NO_RESULT: PartyResult? = null

        @JvmField val TIE = PartyResult(Party("TIE", "TIE", Color.DARK_GRAY), false)

        fun elected(party: Party?) = if (party == null) null else PartyResult(party, true)

        fun leading(party: Party?) = if (party == null) null else PartyResult(party, false)

        val PartyResult?.color: Color get() {
            return when {
                this == null -> Color.BLACK
                this.elected -> party.color
                else -> ColorUtils.lighten(party.color)
            }
        }
    }
}
