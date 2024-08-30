package com.joecollins.graphics.screens.generic

import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher

class SingleNoResultMap<T> internal constructor() : AbstractSingleResultMap<T, Nothing>({
    Party.OTHERS.color
}) {
    init {
        leader = null.asOneTimePublisher()
    }

    companion object {
        fun <T> createSingleNoResultMap(builder: SingleNoResultMap<T>.() -> Unit) = SingleNoResultMap<T>().apply(builder)
    }
}
