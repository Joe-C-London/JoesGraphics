package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrame
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow

sealed class AbstractMap<T> {
    lateinit var shapes: Flow.Publisher<out Map<T, Shape>>
    var focus: Flow.Publisher<out List<T>?>? = null
    var additionalHighlight: Flow.Publisher<out List<T>?>? = null
    lateinit var header: Flow.Publisher<out String?>

    internal val additionalHighlightOrFocus by lazy { additionalHighlight ?: focus }

    abstract val mapFrame: MapFrame

    companion object {
        internal val FOCUS_GREY = Color.LIGHT_GRAY
        internal val BACKGROUND_GREY = Color(220, 220, 220)

        internal fun <T> createFocusShapes(shapes: Map<out T, Shape>, focus: List<T>?): List<Shape>? {
            return focus
                ?.filter { shapes.containsKey(it) }
                ?.map { shapes[it]!! }
        }

        internal fun extractColor(focus: List<Shape>?, shape: Shape, winner: Color?): Color {
            val isInFocus = focus.isNullOrEmpty() || focus.contains(shape)
            return if (isInFocus) {
                (winner ?: FOCUS_GREY)
            } else {
                BACKGROUND_GREY
            }
        }
    }
}
