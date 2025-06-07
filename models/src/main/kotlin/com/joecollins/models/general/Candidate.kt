package com.joecollins.models.general

@ConsistentCopyVisibility
data class Candidate private constructor(val name: String, val party: Party, val incumbent: Boolean, override val overrideSortOrder: Int? = party.overrideSortOrder) : CanOverrideSortOrder() {

    constructor(name: String, party: Party, incumbent: Boolean = false) : this(name, party, incumbent, party.overrideSortOrder)

    override fun toString(): String = name + " (" + party + ")" + (if (incumbent) "*" else "")

    fun isIncumbent() = incumbent

    companion object {
        val OTHERS = Candidate("", Party.OTHERS, false, -1)
    }
}
