package com.joecollins.models.general

data class Candidate(val name: String, val party: Party, val incumbent: Boolean) {

    constructor(name: String, party: Party) : this(name, party, false)

    override fun toString(): String = name + " (" + party + ")" + (if (incumbent) "*" else "")

    fun isIncumbent() = incumbent

    companion object {
        @JvmField val OTHERS = Candidate("Others", Party.OTHERS)
    }
}
