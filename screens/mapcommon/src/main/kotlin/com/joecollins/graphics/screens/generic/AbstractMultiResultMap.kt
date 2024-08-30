package com.joecollins.graphics.screens.generic

import com.joecollins.pubsub.map
import java.awt.Color
import java.util.concurrent.Flow

abstract class AbstractMultiResultMap<T, R> internal constructor(color: R.() -> Color) : AbstractMap<T>() {
    lateinit var winners: Flow.Publisher<out Map<T, R?>>

    final override val mapFrame by lazy {
        MapBuilder.multiResult(
            shapes = shapes,
            winners = winners.map { m -> m.mapValues { (_, winner) -> winner?.color() } },
            focus = focus,
            additionalHighlight = additionalHighlightOrFocus,
            header = header,
        )
    }
}
