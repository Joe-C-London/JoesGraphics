package com.joecollins.models.general

import com.joecollins.pubsub.map
import java.awt.Color
import java.util.concurrent.Flow

interface PartyOrCoalition {
    val name: String
    val abbreviation: String
    val color: Color
    val constituentParties: List<Party>
    val asParty: Party
}

fun <V> Map<out PartyOrCoalition, V>.mapToParty(): Map<Party, V> {
    return this.mapKeys { it.key.asParty }
}

fun <V> Flow.Publisher<out Map<out PartyOrCoalition, V>>.mapToParty(): Flow.Publisher<Map<Party, V>> {
    return this.map { it.mapToParty() }
}
