package com.joecollins.models.general

data class PartyOrCandidate private constructor(private val _party: Party?, private val _candidate: Candidate?) : CanOverrideSortOrder() {

    constructor(party: Party) : this(party, null)

    constructor(name: String, party: Party = INDEPENDENT) : this(Candidate(name, party))

    constructor(candidate: Candidate) : this(null, candidate)

    val party = _party ?: _candidate!!.party

    val name = _party?.name ?: _candidate!!.name

    val color = party.color

    override fun toString(): String = _party?.toString() ?: _candidate!!.toString()

    companion object {
        private val INDEPENDENT = Party("Independent", "IND", Party.OTHERS.color)

        val OTHERS = PartyOrCandidate(Party.OTHERS)

        fun Map<out Party, Int>.convertToPartyOrCandidate(): Map<PartyOrCandidate, Int> = mapKeys { PartyOrCandidate(it.key) }

        fun Map<out PartyOrCandidate, Int>.convertToParty(): Map<Party, Int> = entries.groupingBy { it.key.party }.fold(0) { a, e -> a + e.value }
    }

    override val overrideSortOrder: Int? = party.overrideSortOrder
}
