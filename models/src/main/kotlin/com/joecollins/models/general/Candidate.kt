package com.joecollins.models.general

@ConsistentCopyVisibility
data class Candidate private constructor(val name: String, val party: Party, val incumbencyType: IncumbencyType?, override val overrideSortOrder: Int? = party.overrideSortOrder) : CanOverrideSortOrder() {

    constructor(name: String, party: Party, incumbencyType: IncumbencyType? = null) : this(name, party, incumbencyType, party.overrideSortOrder)
    constructor(name: String, party: Party, incumbent: Boolean) : this(name, party, if (incumbent) IncumbencyType.DEFAULT else null, party.overrideSortOrder)

    override fun toString(): String = name + " (" + party + ")" + (if (incumbent) "*" else "")

    val incumbent = incumbencyType != null

    fun isIncumbent() = incumbent

    companion object {
        val OTHERS = Candidate("", Party.OTHERS, null, -1)
    }
}
