package com.joecollins.models.general

import kotlin.contracts.contract

data class PartyResult(override val leader: Party, override val elected: Boolean) : ElectionResult<Party> {

    companion object {

        val TIE = PartyResult.leading(Party.TIE)

        @OptIn(kotlin.contracts.ExperimentalContracts::class)
        fun elected(party: Party?): PartyResult? {
            contract {
                returnsNotNull() implies (party != null)
                returns(null) implies (party == null)
            }
            return if (party == null) null else PartyResult(party, true)
        }

        @JvmName("electedNotNull")
        fun elected(party: Party) = PartyResult(party, true)

        @OptIn(kotlin.contracts.ExperimentalContracts::class)
        fun leading(party: Party?): PartyResult? {
            contract {
                returnsNotNull() implies (party != null)
                returns(null) implies (party == null)
            }
            return if (party == null) null else PartyResult(party, false)
        }

        @JvmName("leadingNotNull")
        fun leading(party: Party) = PartyResult(party, false)
    }
}
