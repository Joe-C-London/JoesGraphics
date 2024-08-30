package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult

class SingleResultMap<T> internal constructor() : AbstractSingleResultMap<T, PartyResult>({
    when {
        this == null -> Party.OTHERS.color
        this.elected -> leader.color
        else -> ColorUtils.lighten(leader.color)
    }
}) {
    companion object {
        fun <T> createSingleResultMap(builder: SingleResultMap<T>.() -> Unit) = SingleResultMap<T>().apply(builder)
    }
}
