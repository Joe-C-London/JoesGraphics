package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrame
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow

sealed class AbstractMap<T> {
    lateinit var shapes: Flow.Publisher<out Map<T, Shape>>
    var focus: Flow.Publisher<out List<T>?>? = null
    var additionalHighlight: Flow.Publisher<out List<T>?>? = null
    var faded: Flow.Publisher<out List<T>?>? = null
    lateinit var header: Flow.Publisher<out String?>

    internal val additionalHighlightOrFocus by lazy { additionalHighlight ?: focus }

    abstract val mapFrame: MapFrame

    companion object {
        internal val FOCUS_GREY = Color.LIGHT_GRAY
        internal val BACKGROUND_GREY = Color(220, 220, 220)
        internal val FADED_GREY = Color(235, 235, 235)

        internal fun <T> listShapes(shapes: Map<out T, Shape>, keys: List<T>?): List<Shape>? {
            return keys
                ?.filter { shapes.containsKey(it) }
                ?.map { shapes[it]!! }
        }

        internal fun extractColor(focus: List<Shape>?, faded: List<Shape>?, shape: Shape, winner: Color?): Color {
            val isInFocus = focus.isNullOrEmpty() || focus.contains(shape)
            val isInFaded = faded != null && faded.contains(shape)
            return if (isInFocus) {
                (winner ?: FOCUS_GREY)
            } else if (isInFaded) {
                FADED_GREY
            } else {
                BACKGROUND_GREY
            }
        }
    }
}
