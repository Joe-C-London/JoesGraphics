package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.IndexedBinding
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Rectangle2D
import java.util.ArrayList
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.Pair

class MapFrameBuilder {
    private val mapFrame = MapFrame()
    private val bindings: MutableList<Binding<*>> = ArrayList()

    fun withFocus(focusBinding: Binding<List<Shape>?>): MapFrameBuilder {
        focusBinding.bind { shapes ->
            val bounds = shapes
                    ?.map { it.bounds2D }
                    ?.reduceOrNull { a, b ->
                        val ret = Rectangle2D.Double(a.x, a.y, a.width, a.height)
                        ret.add(b)
                        ret
                    }
            mapFrame.setFocusBoxBinding(Binding.fixedBinding(bounds))
        }
        bindings.add(focusBinding)
        return this
    }

    fun withHeader(headerBinding: Binding<String?>): MapFrameBuilder {
        mapFrame.setHeaderBinding(headerBinding)
        return this
    }

    fun build(): MapFrame {
        return mapFrame
    }

    companion object {
        @JvmStatic fun from(shapes: BindableList<Pair<Shape, Color>>): MapFrameBuilder {
            val mapFrameBuilder = MapFrameBuilder()
            val mapFrame = mapFrameBuilder.mapFrame
            mapFrame.setNumShapesBinding(Binding.sizeBinding(shapes))
            mapFrame.setShapeBinding(IndexedBinding.propertyBinding(shapes) { it.left })
            mapFrame.setColorBinding(IndexedBinding.propertyBinding(shapes) { it.right })
            return mapFrameBuilder
        }

        @JvmStatic fun from(shapes: Binding<List<Pair<Shape, Color>>>): MapFrameBuilder {
            val list = BindableList<Pair<Shape, Color>>()
            shapes.bind { list.setAll(it) }
            val ret = from(list)
            ret.bindings.add(shapes)
            return ret
        }

        @JvmStatic fun <T> from(
            itemsBinding: Binding<List<T>>,
            shapeFunc: (T) -> Shape,
            colorFunc: (T) -> Binding<Color>
        ): MapFrameBuilder {
            val list = BindableList<Pair<Shape, Color>>()
            val bindings: MutableList<Binding<Color>> = ArrayList()
            itemsBinding.bind { items ->
                bindings.forEach { it.unbind() }
                list.clear()
                for (i in items.indices) {
                    val item = items[i]
                    val shape = shapeFunc(item)
                    val colorBinding = colorFunc(item)
                    list.add(ImmutablePair.of(shape, Color.BLACK))
                    colorBinding.bind { color -> list[i] = ImmutablePair.of(shape, color) }
                    bindings.add(colorBinding)
                }
            }
            val ret = from(list)
            ret.bindings.add(itemsBinding)
            return ret
        }
    }
}
