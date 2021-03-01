package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.MapFrameBuilder
import com.joecollins.models.general.PartyResult
import java.awt.Color
import java.awt.Shape
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair

class MapBuilder<T> {
    private val winners: BindingReceiver<List<Pair<Shape, Color>>>
    private val mapFocus: BindingReceiver<List<Shape>?>
    private val mapHeader: BindingReceiver<String?>

    constructor(
        shapes: Binding<Map<T, Shape>>,
        winners: Binding<Map<T, PartyResult?>>,
        focus: Binding<List<T>?>,
        headerBinding: Binding<String?>
    ) {
        val shapesReceiver: BindingReceiver<Map<T, Shape>> = BindingReceiver(shapes)
        val shapesToParties = shapesReceiver
                .getBinding()
                .merge(winners) { s, w ->
                    s.entries
                            .map {
                                val winnerParty = w[it.key]
                                ImmutablePair.of(it.value, winnerParty)
                            }
                            .toList()
                }
        mapFocus = BindingReceiver(shapesReceiver.getBinding().merge(focus) { shp, foc -> createFocusShapes(shp, foc) })
        this.winners = BindingReceiver(
                shapesToParties.merge(
                        mapFocus.getBinding()
                ) { r: List<ImmutablePair<Shape, PartyResult?>>, f: List<Shape>? ->
                    r.map { ImmutablePair.of(it.left, extractColor(f, it.left, it.right)) }
                            .toList()
                })
        mapHeader = BindingReceiver(headerBinding)
    }

    constructor(
        shapes: Binding<Map<T, Shape>>,
        selectedShape: Binding<T?>,
        leadingParty: Binding<PartyResult?>,
        focus: Binding<List<T>?>,
        header: Binding<String?>
    ) : this(shapes, selectedShape, leadingParty, focus, Binding.fixedBinding(null), header)

    constructor(
        shapes: Binding<Map<T, Shape>>,
        selectedShape: Binding<T?>,
        leadingParty: Binding<PartyResult?>,
        focus: Binding<List<T>?>,
        additionalHighlight: Binding<List<T>?>,
        header: Binding<String?>
    ) {
        val shapesReceiver: BindingReceiver<Map<T, Shape>> = BindingReceiver(shapes)
        val leaderWithShape: Binding<ImmutablePair<T, PartyResult>> = selectedShape.merge(leadingParty) { left, right -> ImmutablePair(left, right) }
        mapFocus = BindingReceiver(shapesReceiver.getBinding().merge(focus) { shp, foc -> createFocusShapes(shp, foc) })
        val additionalFocusShapes = shapesReceiver.getBinding().merge(additionalHighlight) { shp, foc -> createFocusShapes(shp, foc) }
        mapHeader = BindingReceiver(header)
        val shapeWinners: Binding<List<Pair<Shape, Color>>> = shapesReceiver
                .getBinding()
                .merge(
                        leaderWithShape
                ) { shp, ldr ->
                    shp.entries
                            .map {
                                val color =
                                    if (it.key == ldr.left) {
                                        ldr.right?.color ?: Color.BLACK
                                    } else {
                                        Color.LIGHT_GRAY
                                    }
                                ImmutablePair.of(it.value, color)
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
        val focusedShapeWinners = shapeWinners.merge(
                allFocusShapes
        ) { sw: List<Pair<Shape, Color>>, f: List<Shape>? ->
            if (f == null) {
                sw
            } else {
                sw.map {
                            if (f.contains(it.left)) {
                                it
                            } else {
                                ImmutablePair.of(it.left, Color(220, 220, 220))
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

    fun createMapFrame(): MapFrame {
        return MapFrameBuilder.from(winners.getBinding())
                .withFocus(mapFocus.getBinding())
                .withHeader(mapHeader.getBinding())
                .build()
    }

    companion object {
        private fun extractColor(focus: List<Shape>?, shape: Shape, winner: PartyResult?): Color {
            return winner?.color
                    ?: (if (focus == null || focus.isEmpty() || focus.contains(shape)) {
                        Color.LIGHT_GRAY
                    } else {
                        Color(220, 220, 220)
                    })
        }
    }
}
