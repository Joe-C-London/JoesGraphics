package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.NonPartisanCandidateResult
import com.joecollins.models.general.Party

class SingleNonPartisanResultMap<T> internal constructor() :
    AbstractSingleResultMap<T, NonPartisanCandidateResult>({
        when {
            this == null -> Party.OTHERS.color
            this.elected -> leader.color
            else -> ColorUtils.lighten(leader.color)
        }
    }) {
    companion object {
        fun <T> createSingleNonPartisanResultMap(builder: SingleNonPartisanResultMap<T>.() -> Unit) = SingleNonPartisanResultMap<T>().apply(builder)
    }
}
