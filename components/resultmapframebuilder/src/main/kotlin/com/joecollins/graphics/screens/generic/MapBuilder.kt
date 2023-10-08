package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.MapFrameBuilder
import com.joecollins.models.general.NonPartisanCandidateResult
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.ResultColorUtils.getColor
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow

object MapBuilder {

    fun <T> multiResult(
        shapes: Flow.Publisher<out Map<T, Shape>>,
        winners: Flow.Publisher<out Map<T, PartyResult?>>,
        focus: Flow.Publisher<out List<T>?>,
        additionalHighlight: Flow.Publisher<out List<T>?> = null.asOneTimePublisher(),
        header: Flow.Publisher<out String?>,
        notes: Flow.Publisher<out String?>? = null,
        outlines: Flow.Publisher<out List<Shape>>? = null,
    ): MapFrame {
        val shapesToParties = shapes
            .merge(winners) { s, w ->
                s.entries
                    .map {
                        val winnerParty = w[it.key]
                        Pair(it.value, winnerParty)
                    }
                    .toList()
            }
        val mapFocus = shapes.merge(focus) { shp, foc -> createFocusShapes(shp, foc) }
        val mapAdditionalFocus =
            shapes.merge(additionalHighlight) { shp, foc -> createFocusShapes(shp, foc) }
        val allFocusShapes = mapFocus.merge(mapAdditionalFocus) { a, b ->
            when {
                a == null -> b
                b == null -> a
                else -> listOf(a, b).flatten()
            }
        }
        val colours = shapesToParties.merge(allFocusShapes) { r, f ->
            r.map { Pair(it.first, extractColor(f, it.first, it.second)) }
                .toList()
        }
        return MapFrameBuilder.from(
            shapes = colours,
            header = header,
            focus = mapFocus,
            notes = notes,
            outline = outlines,
        )
    }

    fun <T> singleResult(
        shapes: Flow.Publisher<out Map<T, Shape>>,
        selectedShape: Flow.Publisher<out T>,
        leadingParty: Flow.Publisher<out PartyResult?>,
        focus: Flow.Publisher<out List<T>?>,
        additionalHighlight: Flow.Publisher<out List<T>?> = null.asOneTimePublisher(),
        header: Flow.Publisher<out String?>,
        notes: Flow.Publisher<out String?>? = null,
        outlines: Flow.Publisher<out List<Shape>>? = null,
    ): MapFrame {
        return singleResultColored(
            shapes,
            selectedShape,
            leadingParty.map { it.getColor(default = Party.OTHERS.color) },
            focus,
            additionalHighlight,
            header,
            notes,
            outlines,
        )
    }

    fun <T> singleNonPartisanResult(
        shapes: Flow.Publisher<out Map<T, Shape>>,
        selectedShape: Flow.Publisher<out T>,
        leadingCandidate: Flow.Publisher<out NonPartisanCandidateResult?>,
        focus: Flow.Publisher<out List<T>?>,
        additionalHighlight: Flow.Publisher<out List<T>?> = null.asOneTimePublisher(),
        header: Flow.Publisher<out String?>,
        notes: Flow.Publisher<out String?>? = null,
        outlines: Flow.Publisher<out List<Shape>>? = null,
    ): MapFrame {
        return singleResultColored(
            shapes,
            selectedShape,
            leadingCandidate.map { it.getColor(default = Party.OTHERS.color) },
            focus,
            additionalHighlight,
            header,
            notes,
            outlines,
        )
    }

    private fun <T> singleResultColored(
        shapes: Flow.Publisher<out Map<T, Shape>>,
        selectedShape: Flow.Publisher<out T>,
        color: Flow.Publisher<out Color>,
        focus: Flow.Publisher<out List<T>?>,
        additionalHighlight: Flow.Publisher<out List<T>?>,
        header: Flow.Publisher<out String?>,
        notes: Flow.Publisher<out String?>? = null,
        outlines: Flow.Publisher<out List<Shape>>? = null,
    ): MapFrame {
        val leaderWithShape =
            selectedShape.merge(color) { left, right -> Pair(left, right) }
        val mapFocus = shapes.merge(focus) { shp, foc -> createFocusShapes(shp, foc) }
        val additionalFocusShapes =
            shapes.merge(additionalHighlight) { shp, foc -> createFocusShapes(shp, foc) }
        val shapeWinners = shapes
            .merge(leaderWithShape) { shp, ldr ->
                shp.entries
                    .map { (t, shape) ->
                        Pair(shape, ldr.takeIf { t == it.first }?.second ?: Color.LIGHT_GRAY)
                    }
                    .toList()
            }
        val allFocusShapes = mapFocus
            .merge(
                additionalFocusShapes,
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
        return MapFrameBuilder.from(
            shapes = focusedShapeWinners,
            header = header,
            focus = mapFocus,
            notes = notes,
            outline = outlines,
        )
    }

    private fun <T> createFocusShapes(shapes: Map<T, Shape>, focus: List<T>?): List<Shape>? {
        return focus
            ?.filter { shapes.containsKey(it) }
            ?.map { shapes[it]!! }
            ?.toList()
    }

    private fun extractColor(focus: List<Shape>?, shape: Shape, winner: PartyResult?): Color {
        val isInFocus = focus.isNullOrEmpty() || focus.contains(shape)
        return winner?.getColor(default = Party.OTHERS.color)?.takeIf { isInFocus }
            ?: (
                if (isInFocus) {
                    Color.LIGHT_GRAY
                } else {
                    Color(220, 220, 220)
                }
                )
    }
}
