package com.joecollins.graphics.components

import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Rectangle2D
import java.util.concurrent.Flow

class MapFrameBuilder {

    private var focusBoxPublisher: Flow.Publisher<out Rectangle2D?>? = null
    private var headerPublisher: Flow.Publisher<out String?>? = null
    private var notesPublisher: Flow.Publisher<out String?>? = null
    private var borderColorPublisher: Flow.Publisher<out Color>? = null
    private var outlinePublisher: Flow.Publisher<out List<Shape>>? = null
    private var shapesPublisher: Flow.Publisher<out List<Pair<Shape, Color>>>? = null

    fun withFocus(focusPublisher: Flow.Publisher<out List<Shape>?>): MapFrameBuilder {
        this.focusBoxPublisher = focusPublisher.map { shapes ->
            shapes
                ?.map { it.bounds2D }
                ?.reduceOrNull { a, b ->
                    val ret = Rectangle2D.Double(a.x, a.y, a.width, a.height)
                    ret.add(b)
                    ret
                }
        }
        return this
    }

    fun withHeader(headerPublisher: Flow.Publisher<out String?>): MapFrameBuilder {
        this.headerPublisher = headerPublisher
        return this
    }

    fun withNotes(notesPublisher: Flow.Publisher<out String?>): MapFrameBuilder {
        this.notesPublisher = notesPublisher
        return this
    }

    fun withBorderColor(borderColorPublisher: Flow.Publisher<out Color>): MapFrameBuilder {
        this.borderColorPublisher = borderColorPublisher
        return this
    }

    fun withOutline(outlinePublisher: Flow.Publisher<out List<Shape>>): MapFrameBuilder {
        this.outlinePublisher = outlinePublisher
        return this
    }

    fun build(): MapFrame {
        return MapFrame(
            headerPublisher = headerPublisher ?: (null as String?).asOneTimePublisher(),
            shapesPublisher = shapesPublisher ?: emptyList<Pair<Shape, Color>>().asOneTimePublisher(),
            focusBoxPublisher = focusBoxPublisher,
            notesPublisher = notesPublisher,
            borderColorPublisher = borderColorPublisher,
            outlineShapesPublisher = outlinePublisher,
        )
    }

    companion object {
        fun from(shapes: Flow.Publisher<out List<Pair<Shape, Color>>>): MapFrameBuilder {
            val mapFrameBuilder = MapFrameBuilder()
            mapFrameBuilder.shapesPublisher = shapes
            return mapFrameBuilder
        }

        @JvmStatic
        fun <T> from(
            itemsBinding: Flow.Publisher<out List<T>>,
            shapeFunc: (T) -> Shape,
            colorFunc: (T) -> Flow.Publisher<out Color>,
        ): MapFrameBuilder {
            val list = itemsBinding.compose { items ->
                items.map { colorFunc(it).map { c -> Pair(shapeFunc(it), c) } }.combine()
            }
            return from(list)
        }
    }
}
