package com.joecollins.models.general

import java.awt.Color

@Suppress("DataClassPrivateConstructor")
data class Party private constructor(override val name: String, override val abbreviation: String, override val color: Color, override val overrideSortOrder: Int?) : PartyOrCoalition() {

    constructor(name: String, abbreviation: String, color: Color) : this(name, abbreviation, color, null)

    override val constituentParties = listOf(this)

    override fun toString(): String = abbreviation

    companion object {
        val OTHERS = Party("Others", "OTH", Color.DARK_GRAY, -1)
        val TIE = Party("TIE", "TIE", Color.DARK_GRAY, -2)
    }
}
