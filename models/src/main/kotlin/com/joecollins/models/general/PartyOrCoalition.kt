package com.joecollins.models.general

import java.awt.Color

abstract class PartyOrCoalition internal constructor() : CanOverrideSortOrder() {
    abstract val name: String
    abstract val abbreviation: String
    abstract val color: Color
    abstract val constituentParties: List<Party>

    fun toParty(): Party {
        return if (this is Party) {
            this
        } else {
            Party(this.name, this.abbreviation, this.color)
        }
    }
}
