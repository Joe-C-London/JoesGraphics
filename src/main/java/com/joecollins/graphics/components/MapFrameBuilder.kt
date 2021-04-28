package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import java.awt.Color
import java.awt.Shape
import java.awt.geom.Rectangle2D
import java.util.ArrayList

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
        @JvmStatic fun from(shapes: Binding<List<Pair<Shape, Color>>>): MapFrameBuilder {
            val mapFrameBuilder = MapFrameBuilder()
            val mapFrame = mapFrameBuilder.mapFrame
            mapFrame.setShapesBinding(shapes)
            return mapFrameBuilder
        }

        @JvmStatic fun <T> from(
            itemsBinding: Binding<List<T>>,
            shapeFunc: (T) -> Shape,
            colorFunc: (T) -> Binding<Color>
        ): MapFrameBuilder {
            val itemsReceiver = BindingReceiver(itemsBinding)
            val list = itemsReceiver.getFlatBinding { items ->
                Binding.listBinding(
                    items.map { colorFunc(it).map { c -> Pair(shapeFunc(it), c) } }
                )
            }
            val ret = from(list)
            ret.bindings.add(itemsBinding)
            return ret
        }
    }
}
