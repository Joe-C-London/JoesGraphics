package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.PartyResult

class ResultMap<T> internal constructor() :
    AbstractMultiResultMap<T, PartyResult>({
        if (elected) leader.color else ColorUtils.lighten(leader.color)
    }) {
    companion object {
        fun <T> createResultMap(builder: ResultMap<T>.() -> Unit) = ResultMap<T>().apply(builder)
    }
}
