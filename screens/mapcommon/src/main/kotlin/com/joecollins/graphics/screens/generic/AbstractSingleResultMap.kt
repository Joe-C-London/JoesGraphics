package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrame
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import java.awt.Color
import java.util.concurrent.Flow

abstract class AbstractSingleResultMap<T, R> internal constructor(private val colorFunc: R?.() -> Color) : AbstractMap<T>() {
    lateinit var selectedShape: Flow.Publisher<out T>
    lateinit var leader: Flow.Publisher<out R?>

    internal val color: Flow.Publisher<Color> by lazy {
        leader.map(colorFunc)
    }

    final override val mapFrame by lazy {
        MapBuilder.singleResult(
            shapes = shapes,
            selectedShape = selectedShape,
            color = color,
            focus = focus,
            additionalHighlight = additionalHighlightOrFocus,
            header = header,
        )
    }

    companion object {
        fun <T> createMapFrame(item: Flow.Publisher<out AbstractSingleResultMap<out T, *>>): MapFrame {
            return MapBuilder.singleResult(
                shapes = item.compose { b -> b.shapes },
                selectedShape = item.compose { b -> b.selectedShape },
                color = item.compose { b -> b.color },
                focus = item.compose { b -> b.focus ?: null.asOneTimePublisher() },
                additionalHighlight = item.compose { b -> b.additionalHighlightOrFocus ?: null.asOneTimePublisher() },
                header = item.compose { b -> b.header },
            )
        }
    }
}
