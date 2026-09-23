package com.joecollins.graphics.components

import com.joecollins.graphics.geometry.Bounds
import com.joecollins.graphics.geometry.SafeGeometry
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import java.awt.Color
import java.util.concurrent.Flow

object MapFrameBuilder {

    fun from(
        shapes: Flow.Publisher<out List<Pair<SafeGeometry, Color>>>,
        header: Flow.Publisher<out String?>,
        focus: Flow.Publisher<out List<SafeGeometry>?>? = null,
        notes: Flow.Publisher<out String?>? = null,
        borderColor: Flow.Publisher<out Color>? = null,
        outline: Flow.Publisher<out List<SafeGeometry>>? = null,
    ): MapFrame = MapFrame(
        headerPublisher = header,
        shapesPublisher = shapes,
        focusBoxPublisher = focus?.map(this::generateBounds),
        notesPublisher = notes,
        borderColorPublisher = borderColor,
        outlineShapesPublisher = outline,
    )

    fun <T> from(
        items: Flow.Publisher<out List<T>>,
        shape: T.() -> SafeGeometry,
        color: T.() -> Flow.Publisher<out Color>,
        header: Flow.Publisher<out String?>,
        focus: Flow.Publisher<out List<T>?>? = null,
        notes: Flow.Publisher<out String?>? = null,
        borderColor: Flow.Publisher<out Color>? = null,
        outline: Flow.Publisher<out List<SafeGeometry>>? = null,
    ): MapFrame = from(
        shapes = items.compose { list ->
            list.map { it.color().map { c -> Pair(it.shape(), c) } }.combine()
        },
        header = header,
        focus = focus?.map { list -> list?.map(shape) },
        notes = notes,
        borderColor = borderColor,
        outline = outline,
    )

    // Combine (overlap) the bounding boxes of all the passed-in geometries into one box.
    private fun generateBounds(shapes: List<SafeGeometry>?): Bounds? = shapes
        ?.asSequence()
        ?.map { it.bounds }
        ?.reduceOrNull { a, b -> a.expandToInclude(b) }
}
