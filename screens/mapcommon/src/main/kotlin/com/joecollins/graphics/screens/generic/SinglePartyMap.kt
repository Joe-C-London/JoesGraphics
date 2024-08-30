package com.joecollins.graphics.screens.generic

import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition

class SinglePartyMap<T> internal constructor() : AbstractSingleResultMap<T, PartyOrCoalition>({
    this?.color ?: Party.OTHERS.color
}) {
    companion object {
        fun <T> createSinglePartyMap(builder: SinglePartyMap<T>.() -> Unit) = SinglePartyMap<T>().apply(builder)
    }
}
