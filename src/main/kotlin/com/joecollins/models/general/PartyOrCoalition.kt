package com.joecollins.models.general

import java.awt.Color

interface PartyOrCoalition {
    val name: String
    val abbreviation: String
    val color: Color
    val constituentParties: List<Party>
}

internal fun PartyOrCoalition.toParty(): Party {
    return if (this is Party) {
        this
    } else {
        Party(this.name, this.abbreviation, this.color)
    }
}
