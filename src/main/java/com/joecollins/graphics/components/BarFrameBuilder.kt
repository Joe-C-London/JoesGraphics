package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.utils.ColorUtils
import java.awt.Color
import java.awt.Shape
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class BarFrameBuilder private constructor() {
    private var headerBinding: Binding<String?>? = null
    private var subheadBinding: Binding<String?>? = null
    private var notesBinding: Binding<String?>? = null
    private var borderColorBinding: Binding<Color>? = null
    private var subheadColorBinding: Binding<Color>? = null
    private var linesBinding: Binding<List<BarFrame.Line>>? = null
    private lateinit var barsBinding: Binding<List<BarFrame.Bar>>
    private var minBinding: Binding<Number>? = null
    private var maxBinding: Binding<Number>? = null
    private var minBarCountBinding: Binding<Int>? = null

    private val rangeFinder = RangeFinder()

    private class RangeFinder : Bindable<RangeFinder, RangeFinder.Property>() {
        enum class Property {
            MIN, MAX
        }

        private var _highest: Number = 0
        private var _lowest: Number = 0
        private var _minFunction = { rf: RangeFinder -> rf.lowest }
        private var _maxFunction = { rf: RangeFinder -> rf.highest }

        var highest: Number
        get() { return _highest }
        set(highest) {
            _highest = highest
            onPropertyRefreshed(Property.MIN)
            onPropertyRefreshed(Property.MAX)
        }

        var lowest: Number
        get() { return _lowest }
        set(lowest) {
            _lowest = lowest
            onPropertyRefreshed(Property.MIN)
            onPropertyRefreshed(Property.MAX)
        }

        var minFunction: (RangeFinder) -> Number
        get() { return _minFunction }
        set(minFunction) {
            _minFunction = minFunction
            onPropertyRefreshed(Property.MIN)
        }

        var maxFunction: (RangeFinder) -> Number
        get() { return _maxFunction }
        set(maxFunction) {
            _maxFunction = maxFunction
            onPropertyRefreshed(Property.MAX)
        }
    }

    class BasicBar @JvmOverloads constructor(val label: String, val color: Color, val value: Number, val valueLabel: String = value.toString(), val shape: Shape? = null) {
        constructor(label: String, color: Color, value: Number, shape: Shape?) : this(label, color, value, value.toString(), shape)
    }

    class DualBar @JvmOverloads constructor(
        val label: String,
        val color: Color,
        val value1: Number,
        val value2: Number,
        val valueLabel: String,
        val shape: Shape? = null
    )

    fun withHeader(headerBinding: Binding<String?>): BarFrameBuilder {
        this.headerBinding = headerBinding
        return this
    }

    fun withSubhead(subheadBinding: Binding<String?>): BarFrameBuilder {
        this.subheadBinding = subheadBinding
        return this
    }

    fun withNotes(notesBinding: Binding<String?>): BarFrameBuilder {
        this.notesBinding = notesBinding
        return this
    }

    fun withBorder(borderColorBinding: Binding<Color>): BarFrameBuilder {
        this.borderColorBinding = borderColorBinding
        return this
    }

    fun withSubheadColor(subheadColorBinding: Binding<Color>): BarFrameBuilder {
        this.subheadColorBinding = subheadColorBinding
        return this
    }

    fun withMax(maxBinding: Binding<Number>): BarFrameBuilder {
        rangeFinder.minFunction = { 0 }
        maxBinding.bind { max: Number -> rangeFinder.maxFunction = { max(max.toDouble(), it.highest.toDouble()) } }
        return this
    }

    fun withWingspan(wingspanBinding: Binding<Number>): BarFrameBuilder {
        wingspanBinding.bind { wingspan: Number ->
            val f = { it: RangeFinder ->
                max(
                    wingspan.toDouble(),
                    max(abs(it.lowest.toDouble()), abs(it.highest.toDouble()))
                )
            }
            rangeFinder.minFunction = { -f(it) }
            rangeFinder.maxFunction = { +f(it) }
        }
        return this
    }

    fun <T : Number> withTarget(
        targetBinding: Binding<T>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        this.linesBinding = targetBinding.map {
            val str = labelFunc(it)
            listOf(BarFrame.Line(it, str))
        }
        return this
    }

    fun <T> withLines(
        lines: Binding<List<T>>,
        labelFunc: (T) -> String,
        valueFunc: (T) -> Number
    ): BarFrameBuilder {
        this.linesBinding = lines.map { l ->
            l.map {
                val label = labelFunc(it)
                val value = valueFunc(it)
                BarFrame.Line(value, label)
            }
        }
        return this
    }

    fun <T : Number> withLines(
        linesBinding: Binding<List<T>>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        return withLines(linesBinding, labelFunc) { it }
    }

    fun withMinBarCount(minBarCountBinding: Binding<Int>): BarFrameBuilder {
        this.minBarCountBinding = minBarCountBinding
        return this
    }

    fun build(): BarFrame {
        val barsBinding = (minBarCountBinding?.let { minBinding ->
            this.barsBinding.merge(minBinding) { bars, min ->
                if (bars.size >= min) bars
                else sequenceOf(bars, MutableList(min - bars.size) { BarFrame.Bar("", "", emptyList()) })
                    .flatten()
                    .toList()
            }
        }
            ?: this.barsBinding)
        return BarFrame(
            headerBinding = headerBinding ?: Binding.fixedBinding(null),
            subheadTextBinding = subheadBinding,
            subheadColorBinding = subheadColorBinding,
            notesBinding = notesBinding,
            borderColorBinding = borderColorBinding,
            barsBinding = barsBinding,
            linesBinding = linesBinding,
            minBinding = minBinding,
            maxBinding = maxBinding
        )
    }

    companion object {
        @JvmStatic fun basic(binding: Binding<List<BasicBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val rangeFinder = builder.rangeFinder
            val bindingReceiver = BindingReceiver(binding)
            builder.barsBinding = bindingReceiver.getBinding { bars ->
                bars.map {
                    BarFrame.Bar(it.label, it.valueLabel, it.shape, listOf(Pair(it.color, it.value)))
                }
            }
            builder.minBinding = Binding.propertyBinding(
                    rangeFinder, { rf: RangeFinder -> rf.minFunction(rf) }, RangeFinder.Property.MIN)
            builder.maxBinding = (
                    Binding.propertyBinding(
                            rangeFinder, { rf: RangeFinder -> rf.maxFunction(rf) }, RangeFinder.Property.MAX))
            bindingReceiver.getBinding().bind { map: List<BasicBar> ->
                rangeFinder.highest = map
                    .map { it.value }
                    .map { it.toDouble() }
                    .fold(0.0) { a, b -> max(a, b) }
                rangeFinder.lowest = map
                    .map { it.value }
                    .map { it.toDouble() }
                    .fold(0.0) { a, b -> min(a, b) }
            }
            return builder
        }

        @JvmStatic fun dual(bars: Binding<List<DualBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val rangeFinder = builder.rangeFinder
            val differentDirections = { bar: DualBar -> sign(bar.value1.toDouble()) * sign(bar.value2.toDouble()) == -1.0 }
            val reverse = { bar: DualBar ->
                (differentDirections(bar) ||
                        abs(bar.value1.toDouble()) < abs(bar.value2.toDouble()))
            }
            val first = { bar: DualBar -> if (reverse(bar)) bar.value1 else bar.value2 }
            val second = { bar: DualBar -> if (reverse(bar)) bar.value2 else bar.value1 }
            val barsReceiver = BindingReceiver(bars)
            builder.barsBinding = barsReceiver.getBinding { b ->
                b.map {
                    BarFrame.Bar(
                        it.label, it.valueLabel, it.shape,
                        listOf(
                            Pair(it.color, 0),
                            Pair(if (differentDirections(it)) ColorUtils.lighten(it.color) else it.color, first(it)),
                            Pair(
                                ColorUtils.lighten(it.color),
                                second(it).toDouble() - if (differentDirections(it)) 0.0 else first(it).toDouble()
                            )
                        )
                    )
                }
            }
            builder.minBinding = Binding.propertyBinding(
                    rangeFinder, { it.minFunction(it) }, RangeFinder.Property.MIN)
            builder.maxBinding = Binding.propertyBinding(
                    rangeFinder, { it.maxFunction(it) }, RangeFinder.Property.MAX)
            barsReceiver.getBinding().bind { map: List<DualBar> ->
                rangeFinder.highest = map
                    .flatMap { sequenceOf(it.value1, it.value2) }
                    .map { it.toDouble() }
                    .fold(0.0) { a, b -> max(a, b) }
                rangeFinder.lowest = map
                    .flatMap { sequenceOf(it.value1, it.value2) }
                    .map { it.toDouble() }
                    .fold(0.0) { a, b -> min(a, b) }
            }
            return builder
        }

        @JvmStatic fun dualReversed(bars: Binding<List<DualBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val rangeFinder = builder.rangeFinder
            val differentDirections = { bar: DualBar -> sign(bar.value1.toDouble()) * sign(bar.value2.toDouble()) == -1.0 }
            val reverse = { bar: DualBar ->
                (differentDirections(bar) ||
                        abs(bar.value1.toDouble()) < abs(bar.value2.toDouble()))
            }
            val first = { bar: DualBar -> if (reverse(bar)) bar.value1 else bar.value2 }
            val second = { bar: DualBar -> if (reverse(bar)) bar.value2 else bar.value1 }
            val barsReceiver = BindingReceiver(bars)
            builder.barsBinding = barsReceiver.getBinding { b ->
                b.map {
                    BarFrame.Bar(
                        it.label, it.valueLabel, it.shape,
                        listOf(
                            Pair(it.color, 0),
                            Pair(ColorUtils.lighten(it.color), first(it)),
                            Pair(
                                if (differentDirections(it)) ColorUtils.lighten(it.color) else it.color,
                                second(it).toDouble() - if (differentDirections(it)) 0.0 else first(it).toDouble()
                            )
                        )
                    )
                }
            }
            builder.minBinding = Binding.propertyBinding(
                    rangeFinder, { it.minFunction(it) }, RangeFinder.Property.MIN)
            builder.maxBinding = Binding.propertyBinding(
                    rangeFinder, { it.maxFunction(it) }, RangeFinder.Property.MAX)
            barsReceiver.getBinding().bind { map: List<DualBar> ->
                rangeFinder.highest = map
                    .flatMap { sequenceOf(it.value1, it.value2) }
                    .map { it.toDouble() }
                    .fold(0.0) { a: Double, b: Double -> max(a, b) }
                rangeFinder.lowest = map
                    .flatMap { sequenceOf(it.value1, it.value2) }
                    .map { it.toDouble() }
                    .fold(0.0) { a, b -> min(a, b) }
            }
            return builder
        }
    }
}
