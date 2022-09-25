package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.MapFrameBuilder
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.color
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow

class MapBuilder<T> {
    private val winners: Flow.Publisher<out List<Pair<Shape, Color>>>
    private val mapFocus: Flow.Publisher<out List<Shape>?>
    private val mapHeader: Flow.Publisher<out String?>
    private var outlines: Flow.Publisher<out List<Shape>>? = null
    private var notes: Flow.Publisher<out String?>? = null

    constructor(
        shapes: Flow.Publisher<out Map<T, Shape>>,
        winners: Flow.Publisher<out Map<T, PartyResult?>>,
        focus: Flow.Publisher<out List<T>?>,
        headerPublisher: Flow.Publisher<out String?>
    ) : this(shapes, winners, Pair(focus, (null as List<T>?).asOneTimePublisher()), headerPublisher)

    constructor(
        shapes: Flow.Publisher<out Map<T, Shape>>,
        winners: Flow.Publisher<out Map<T, PartyResult?>>,
        focus: Pair<Flow.Publisher<out List<T>?>, Flow.Publisher<out List<T>?>>,
        headerPublisher: Flow.Publisher<out String?>
    ) {
        val shapesToParties = shapes
            .merge(winners) { s, w ->
                s.entries
                    .map {
                        val winnerParty = w[it.key]
                        Pair(it.value, winnerParty)
                    }
                    .toList()
            }
        mapFocus = shapes.merge(focus.first) { shp, foc -> createFocusShapes(shp, foc) }
        val additionalFocus =
            shapes.merge(focus.second) { shp, foc -> createFocusShapes(shp, foc) }
        val allFocusShapes = mapFocus.merge(additionalFocus) { a, b ->
            when {
                a == null -> b
                b == null -> a
                else -> listOf(a, b).flatten()
            }
        }
        this.winners =
            shapesToParties.merge(allFocusShapes) { r, f ->
                r.map { Pair(it.first, extractColor(f, it.first, it.second)) }
                    .toList()
            }
        mapHeader = headerPublisher
    }

    constructor(
        shapes: Flow.Publisher<out Map<T, Shape>>,
        selectedShape: Flow.Publisher<out T>,
        leadingParty: Flow.Publisher<out PartyResult?>,
        focus: Flow.Publisher<out List<T>?>,
        header: Flow.Publisher<out String?>
    ) : this(shapes, selectedShape, leadingParty, focus, (null as List<T>?).asOneTimePublisher(), header)

    constructor(
        shapes: Flow.Publisher<out Map<T, Shape>>,
        selectedShape: Flow.Publisher<out T>,
        leadingParty: Flow.Publisher<out PartyResult?>,
        focus: Flow.Publisher<out List<T>?>,
        additionalHighlight: Flow.Publisher<out List<T>?>,
        header: Flow.Publisher<out String?>
    ) {
        val leaderWithShape =
            selectedShape.merge(leadingParty) { left, right -> Pair(left, right) }
        mapFocus = shapes.merge(focus) { shp, foc -> createFocusShapes(shp, foc) }
        val additionalFocusShapes =
            shapes.merge(additionalHighlight) { shp, foc -> createFocusShapes(shp, foc) }
        mapHeader = header
        val shapeWinners = shapes
            .merge(leaderWithShape) { shp, ldr ->
                shp.entries
                    .map {
                        val color =
                            if (it.key == ldr.first) {
                                ldr.second.color
                            } else {
                                Color.LIGHT_GRAY
                            }
                        Pair(it.value, color)
                    }
                    .toList()
            }
        val allFocusShapes = mapFocus
            .merge(
                additionalFocusShapes
            ) { l1: List<Shape>?, l2: List<Shape>? ->
                when {
                    l1 == null -> l2
                    l2 == null -> l1
                    else -> listOf(l1, l2).flatten().distinct()
                }
            }
        val focusedShapeWinners =
            shapeWinners.merge(allFocusShapes) { sw, f ->
                if (f == null) {
                    sw
                } else {
                    sw.map {
                        if (f.contains(it.first)) {
                            it
                        } else {
                            Pair(it.first, Color(220, 220, 220))
                        }
                    }
                        .toList()
                }
            }
        winners = focusedShapeWinners
    }

    private fun <T> createFocusShapes(shapes: Map<T, Shape>, focus: List<T>?): List<Shape>? {
        return focus
            ?.filter { shapes.containsKey(it) }
            ?.map { shapes[it]!! }
            ?.toList()
    }

    fun withNotes(notes: Flow.Publisher<out String?>): MapBuilder<T> {
        this.notes = notes
        return this
    }

    fun withOutlines(outlines: Flow.Publisher<out List<Shape>>): MapBuilder<T> {
        this.outlines = outlines
        return this
    }

    fun createMapFrame(): MapFrame {
        return MapFrameBuilder.from(winners)
            .withFocus(mapFocus)
            .withHeader(mapHeader)
            .let { map -> notes?.let { n -> map.withNotes(n) } ?: map }
            .let { map -> outlines?.let { n -> map.withOutline(n) } ?: map }
            .build()
    }

    companion object {
        private fun extractColor(focus: List<Shape>?, shape: Shape, winner: PartyResult?): Color {
            val isInFocus = focus == null || focus.isEmpty() || focus.contains(shape)
            return winner?.color?.takeIf { isInFocus }
                ?: (
                    if (isInFocus) {
                        Color.LIGHT_GRAY
                    } else {
                        Color(220, 220, 220)
                    }
                    )
        }
    }
}
