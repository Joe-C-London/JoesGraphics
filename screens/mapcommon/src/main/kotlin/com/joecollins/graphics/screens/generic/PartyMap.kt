package com.joecollins.graphics.screens.generic

import com.joecollins.models.general.PartyOrCoalition

class PartyMap<T> internal constructor() : AbstractMultiResultMap<T, PartyOrCoalition>(PartyOrCoalition::color) {

    companion object {
        fun <T> createPartyMap(builder: PartyMap<T>.() -> Unit) = PartyMap<T>().apply(builder)
    }
}
