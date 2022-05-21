package com.joecollins.models.general

import java.awt.Color

data class Party(val name: String, val abbreviation: String, val color: Color) {
    override fun toString(): String = abbreviation

    companion object {
        @JvmField val OTHERS = Party("Others", "OTH", Color.DARK_GRAY)
    }
}
