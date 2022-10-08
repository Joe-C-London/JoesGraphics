package com.joecollins.models.general

data class Candidate private constructor(val name: String, val party: Party, val incumbent: Boolean, override val overrideSortOrder: Int?) : CanOverrideSortOrder() {

    constructor(name: String, party: Party, incumbent: Boolean) : this(name, party, incumbent, null)

    constructor(name: String, party: Party) : this(name, party, false)

    override fun toString(): String = name + " (" + party + ")" + (if (incumbent) "*" else "")

    fun isIncumbent() = incumbent

    companion object {
        @JvmField val OTHERS = Candidate("Others", Party.OTHERS, false, -1)
    }
}
