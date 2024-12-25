package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.MapFrameBuilder
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow

abstract class AbstractMultiResultMap<T, R> internal constructor(color: R.() -> Color) : AbstractMap<T>() {
    lateinit var winners: Flow.Publisher<out Map<T, R?>>

    final override val mapFrame by lazy {
        createFrame(shapes, colors, focus, additionalHighlight, faded, header)
    }

    private val colors: Flow.Publisher<out Map<T, Color?>> by lazy {
        winners.map { w: Map<T, R?> -> w.mapValues { (_, v) -> v?.let(color) } }
    }

    companion object {
        private fun <T> createFrame(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, Color?>>,
            focus: Flow.Publisher<out Collection<T>?>? = null,
            additionalHighlight: Flow.Publisher<out List<T>?>? = null,
            faded: Flow.Publisher<out Collection<T>?>? = null,
            header: Flow.Publisher<out String?>,
        ): MapFrame {
            val shapesToParties = shapes.merge(winners) { s, w ->
                s.entries.map { Pair(it.value, w[it.key]) }
            }
            val mapFocus = focus?.merge(shapes) { foc, shp -> listShapes(shp, foc) }
            val mapAdditionalFocus =
                additionalHighlight?.merge(shapes) { foc, shp -> listShapes(shp, foc) }
            val allFocusShapes = when {
                mapFocus != null && mapAdditionalFocus != null -> mapFocus.merge(mapAdditionalFocus) { a, b -> listOfNotNull(a, b).takeIf { it.isNotEmpty() }?.flatten() }
                mapFocus != null -> mapFocus
                mapAdditionalFocus != null -> mapAdditionalFocus
                else -> null.asOneTimePublisher()
            }
            val allFadedShapes = when {
                faded != null -> faded.merge(shapes) { fad, shp -> listShapes(shp, fad) }
                else -> null.asOneTimePublisher()
            }
            val colours = shapesToParties.merge(allFocusShapes.merge(allFadedShapes) { foc, fad -> foc to fad }) { r, (foc, fad) ->
                r.map { Pair(it.first, extractColor(foc, fad, it.first, it.second)) }
            }
            return MapFrameBuilder.from(
                shapes = colours,
                header = header,
                focus = mapFocus,
            )
        }
    }
}
