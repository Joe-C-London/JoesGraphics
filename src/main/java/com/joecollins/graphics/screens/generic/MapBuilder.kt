package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.MapFrameBuilder
import com.joecollins.models.general.PartyResult
import java.awt.Color
import java.awt.Shape

class MapBuilder<T> {
    private val winners: BindingReceiver<List<Pair<Shape, Color>>>
    private val mapFocus: BindingReceiver<List<Shape>?>
    private val mapHeader: BindingReceiver<String?>
    private var notes: Binding<String?>? = null

    constructor(
        shapes: Binding<Map<T, Shape>>,
        winners: Binding<Map<T, PartyResult?>>,
        focus: Binding<List<T>?>,
        headerBinding: Binding<String?>
    ) : this(shapes, winners, Pair(focus, fixedBinding(null)), headerBinding)

    constructor(
        shapes: Binding<Map<T, Shape>>,
        winners: Binding<Map<T, PartyResult?>>,
        focus: Pair<Binding<List<T>?>, Binding<List<T>?>>,
        headerBinding: Binding<String?>
    ) {
        val shapesReceiver: BindingReceiver<Map<T, Shape>> = BindingReceiver(shapes)
        val shapesToParties = shapesReceiver
                .getBinding()
                .merge(winners) { s, w ->
                    s.entries
                            .map {
                                val winnerParty = w[it.key]
                                Pair(it.value, winnerParty)
                            }
                            .toList()
                }
        mapFocus = BindingReceiver(shapesReceiver.getBinding().merge(focus.first) { shp, foc -> createFocusShapes(shp, foc) })
        val additionalFocus = BindingReceiver(shapesReceiver.getBinding().merge(focus.second) { shp, foc -> createFocusShapes(shp, foc) })
        val allFocusShapes = mapFocus.getBinding().merge(additionalFocus.getBinding()) { a, b ->
            when {
                a == null -> b
                b == null -> a
                else -> listOf(a, b).flatten()
            }
        }
        this.winners = BindingReceiver(
                shapesToParties.merge(allFocusShapes) { r: List<Pair<Shape, PartyResult?>>, f: List<Shape>? ->
                    r.map { Pair(it.first, extractColor(f, it.first, it.second)) }
                            .toList()
                })
        mapHeader = BindingReceiver(headerBinding)
    }

    constructor(
        shapes: Binding<Map<T, Shape>>,
        selectedShape: Binding<T>,
        leadingParty: Binding<PartyResult?>,
        focus: Binding<List<T>?>,
        header: Binding<String?>
    ) : this(shapes, selectedShape, leadingParty, focus, Binding.fixedBinding(null), header)

    constructor(
        shapes: Binding<Map<T, Shape>>,
        selectedShape: Binding<T>,
        leadingParty: Binding<PartyResult?>,
        focus: Binding<List<T>?>,
        additionalHighlight: Binding<List<T>?>,
        header: Binding<String?>
    ) {
        val shapesReceiver: BindingReceiver<Map<T, Shape>> = BindingReceiver(shapes)
        val leaderWithShape: Binding<Pair<T, PartyResult?>> = selectedShape.merge(leadingParty) { left, right -> Pair(left, right) }
        mapFocus = BindingReceiver(shapesReceiver.getBinding().merge(focus) { shp, foc -> createFocusShapes(shp, foc) })
        val additionalFocusShapes = shapesReceiver.getBinding().merge(additionalHighlight) { shp, foc -> createFocusShapes(shp, foc) }
        mapHeader = BindingReceiver(header)
        val shapeWinners: Binding<List<Pair<Shape, Color>>> = shapesReceiver
                .getBinding()
                .merge(leaderWithShape) { shp, ldr ->
                    shp.entries
                            .map {
                                val color =
                                    if (it.key == ldr.first) {
                                        ldr.second?.color ?: Color.BLACK
                                    } else {
                                        Color.LIGHT_GRAY
                                    }
                                Pair(it.value, color)
                            }
                            .toList()
                }
        val allFocusShapes = mapFocus
                .getBinding()
                .merge(
                        additionalFocusShapes
                ) { l1: List<Shape>?, l2: List<Shape>? ->
                    when {
                        l1 == null -> l2
                        l2 == null -> l1
                        else -> listOf(l1, l2).flatten().distinct()
                    }
                }
        val focusedShapeWinners = shapeWinners.merge(allFocusShapes) { sw: List<Pair<Shape, Color>>, f: List<Shape>? ->
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
        winners = BindingReceiver(focusedShapeWinners)
    }

    private fun <T> createFocusShapes(shapes: Map<T, Shape>, focus: List<T>?): List<Shape>? {
        return focus
                ?.filter { key: T -> shapes.containsKey(key) }
                ?.map { key: T -> shapes[key]!! }
                ?.toList()
    }

    fun withNotes(notes: Binding<String?>): MapBuilder<T> {
        this.notes = notes
        return this
    }

    fun createMapFrame(): MapFrame {
        return MapFrameBuilder.from(winners.getBinding())
                .withFocus(mapFocus.getBinding())
                .withHeader(mapHeader.getBinding())
                .let { map -> notes?.let { n -> map.withNotes(n) } ?: map }
                .build()
    }

    companion object {
        private fun extractColor(focus: List<Shape>?, shape: Shape, winner: PartyResult?): Color {
            val isInFocus = focus == null || focus.isEmpty() || focus.contains(shape)
            return winner?.color?.takeIf { isInFocus }
                    ?: (if (isInFocus) {
                        Color.LIGHT_GRAY
                    } else {
                        Color(220, 220, 220)
                    })
        }
    }
}
