package com.joecollins.models.general

import java.awt.Color

data class Party(override val name: String, override val abbreviation: String, override val color: Color) : PartyOrCoalition {
    override val constituentParties = listOf(this)

    override fun toString(): String = abbreviation

    companion object {
        @JvmField val OTHERS = Party("Others", "OTH", Color.DARK_GRAY)
    }
}
