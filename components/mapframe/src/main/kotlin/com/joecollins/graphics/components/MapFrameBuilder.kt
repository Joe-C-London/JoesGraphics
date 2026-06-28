package com.joecollins.graphics.components

import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import org.locationtech.jts.geom.Geometry
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.util.concurrent.Flow

object MapFrameBuilder {

    fun from(
        shapes: Flow.Publisher<out List<Pair<Geometry, Color>>>,
        header: Flow.Publisher<out String?>,
        focus: Flow.Publisher<out List<Geometry>?>? = null,
        notes: Flow.Publisher<out String?>? = null,
        borderColor: Flow.Publisher<out Color>? = null,
        outline: Flow.Publisher<out List<Geometry>>? = null,
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
        shape: T.() -> Geometry,
        color: T.() -> Flow.Publisher<out Color>,
        header: Flow.Publisher<out String?>,
        focus: Flow.Publisher<out List<T>?>? = null,
        notes: Flow.Publisher<out String?>? = null,
        borderColor: Flow.Publisher<out Color>? = null,
        outline: Flow.Publisher<out List<Geometry>>? = null,
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

    private fun generateBounds(shapes: List<Geometry>?) = shapes
        ?.asSequence()
        ?.map { it.awtBounds() }
        ?.reduceOrNull { a, b ->
            val ret = Rectangle2D.Double(a.x, a.y, a.width, a.height)
            ret.add(b)
            ret
        }
}
