package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.MapFrameBuilder
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow

abstract class AbstractSingleResultMap<T, R> internal constructor(private val colorFunc: R?.() -> Color) : AbstractMap<T>() {
    lateinit var selectedShape: Flow.Publisher<out T>
    lateinit var leader: Flow.Publisher<out R?>

    internal val color: Flow.Publisher<Color> by lazy {
        leader.map(colorFunc)
    }

    final override val mapFrame by lazy {
        createFrame(shapes, selectedShape, color, focus, additionalHighlightOrFocus, faded, header)
    }

    companion object {
        fun <T> createMapFrame(item: Flow.Publisher<out AbstractSingleResultMap<out T, *>>): MapFrame = createFrame(
            item.compose { b -> b.shapes },
            item.compose { b -> b.selectedShape },
            item.compose { b -> b.color },
            item.compose { b -> b.focus ?: null.asOneTimePublisher() },
            item.compose { b -> b.additionalHighlightOrFocus ?: null.asOneTimePublisher() },
            item.compose { b -> b.faded ?: null.asOneTimePublisher() },
            item.compose { b -> b.header },
        )

        private fun <T> createFrame(
            shapes: Flow.Publisher<out Map<out T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            color: Flow.Publisher<out Color>,
            focus: Flow.Publisher<out Collection<T>?>?,
            additionalHighlight: Flow.Publisher<out Collection<T>?>?,
            faded: Flow.Publisher<out Collection<T>?>?,
            header: Flow.Publisher<out String?>,
        ): MapFrame {
            val leaderWithShape =
                selectedShape.merge(color) { left, right -> Pair(left, right) }
            val mapFocus = focus?.merge(shapes) { foc, shp -> listShapes(shp, foc) }
            val additionalFocusShapes =
                additionalHighlight?.merge(shapes) { foc, shp -> listShapes(shp, foc) }
            val shapeWinners = shapes
                .merge(leaderWithShape) { shp, ldr ->
                    shp.entries.map { (t, shape) ->
                        Pair(shape, ldr.takeIf { t == it.first }?.second ?: FOCUS_GREY)
                    }
                }
            val allFocusShapes = when {
                mapFocus != null && additionalFocusShapes != null -> mapFocus.merge(additionalFocusShapes) { a, b ->
                    listOfNotNull(a, b).takeIf { it.isNotEmpty() }?.flatten()
                }
                mapFocus != null -> mapFocus
                additionalFocusShapes != null -> additionalFocusShapes
                else -> null.asOneTimePublisher()
            }
            val allFadedShapes = when {
                faded != null -> faded.merge(shapes) { fad, shp -> listShapes(shp, fad) }
                else -> null.asOneTimePublisher()
            }
            val focusedShapeWinners =
                shapeWinners.merge(allFocusShapes.merge(allFadedShapes) { foc, fad -> foc to fad }) { sw, (foc, fad) ->
                    sw.map {
                        it.first to extractColor(foc, fad, it.first, it.second)
                    }
                }
            return MapFrameBuilder.from(
                shapes = focusedShapeWinners,
                header = header,
                focus = mapFocus,
            )
        }
    }
}
