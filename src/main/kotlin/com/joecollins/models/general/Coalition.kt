package com.joecollins.models.general

import java.awt.Color

data class Coalition(override val name: String, override val abbreviation: String, override val color: Color, override val constituentParties: List<Party>) : PartyOrCoalition {
    constructor(name: String, abbreviation: String, vararg constituentParties: Party) : this(name, abbreviation, constituentParties.first().color, constituentParties.toList())
    constructor(name: String, abbreviation: String, color: Color, vararg constituentParties: Party) : this(name, abbreviation, color, constituentParties.toList())

    override fun toString(): String = abbreviation
}
