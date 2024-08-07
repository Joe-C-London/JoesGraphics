package com.joecollins.models.general

data class PartyResult(override val leader: Party, override val elected: Boolean) : ElectionResult<Party> {

    companion object {

        val TIE = PartyResult(Party.TIE, false)

        fun elected(party: Party?) = if (party == null) null else PartyResult(party, true)

        fun leading(party: Party?) = if (party == null) null else PartyResult(party, false)
    }
}
