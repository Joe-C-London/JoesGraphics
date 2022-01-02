package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sign

class BarFrameBuilder private constructor() {
    private var headerPublisher: Flow.Publisher<out String?>? = null
    private var subheadPublisher: Flow.Publisher<out String?>? = null
    private var notesPublisher: Flow.Publisher<out String?>? = null
    private var borderColorPublisher: Flow.Publisher<out Color>? = null
    private var subheadColorPublisher: Flow.Publisher<out Color>? = null
    private var linesPublisher: Flow.Publisher<out List<BarFrame.Line>>? = null
    private lateinit var barsPublisher: Flow.Publisher<out List<BarFrame.Bar>>
    private var minPublisher: Flow.Publisher<out Number>? = null
    private var maxPublisher: Flow.Publisher<out Number>? = null
    private var minBarCountPublisher: Flow.Publisher<out Int>? = null

    private val rangeFinder = RangeFinder()

    private class RangeFinder {
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
                minPublisher.submit(minFunction(this))
                maxPublisher.submit(maxFunction(this))
            }

        var lowest: Number
            get() { return _lowest }
            set(lowest) {
                _lowest = lowest
                minPublisher.submit(minFunction(this))
                maxPublisher.submit(maxFunction(this))
            }

        var minFunction: (RangeFinder) -> Number
            get() { return _minFunction }
            set(minFunction) {
                _minFunction = minFunction
                minPublisher.submit(minFunction(this))
            }

        var maxFunction: (RangeFinder) -> Number
            get() { return _maxFunction }
            set(maxFunction) {
                _maxFunction = maxFunction
                maxPublisher.submit(maxFunction(this))
            }

        val minPublisher = Publisher(minFunction(this))
        val maxPublisher = Publisher(maxFunction(this))
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

    fun withHeader(headerBinding: Flow.Publisher<out String?>): BarFrameBuilder {
        this.headerPublisher = headerBinding
        return this
    }

    fun withSubhead(subheadBinding: Flow.Publisher<out String?>): BarFrameBuilder {
        this.subheadPublisher = subheadBinding
        return this
    }

    fun withNotes(notesBinding: Flow.Publisher<out String?>): BarFrameBuilder {
        this.notesPublisher = notesBinding
        return this
    }

    fun withBorder(borderColorBinding: Flow.Publisher<out Color>): BarFrameBuilder {
        this.borderColorPublisher = borderColorBinding
        return this
    }

    fun withSubheadColor(subheadColorBinding: Flow.Publisher<out Color>): BarFrameBuilder {
        this.subheadColorPublisher = subheadColorBinding
        return this
    }

    fun withMax(maxBinding: Flow.Publisher<out Number>): BarFrameBuilder {
        rangeFinder.minFunction = { 0 }
        maxBinding.subscribe(Subscriber { max: Number -> rangeFinder.maxFunction = { max(max.toDouble(), it.highest.toDouble()) } })
        return this
    }

    fun withWingspan(wingspanBinding: Flow.Publisher<out Number>): BarFrameBuilder {
        wingspanBinding.subscribe(
            Subscriber { wingspan: Number ->
                val f = { it: RangeFinder ->
                    max(
                        wingspan.toDouble(),
                        max(abs(it.lowest.toDouble()), abs(it.highest.toDouble()))
                    )
                }
                rangeFinder.minFunction = { -f(it) }
                rangeFinder.maxFunction = { +f(it) }
            }
        )
        return this
    }

    fun <T : Number> withTarget(
        targetBinding: Flow.Publisher<out T>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        this.linesPublisher = targetBinding.map {
            val str = labelFunc(it)
            listOf(BarFrame.Line(it, str))
        }
        return this
    }

    fun <T> withLines(
        lines: Flow.Publisher<out List<T>>,
        labelFunc: (T) -> String,
        valueFunc: (T) -> Number
    ): BarFrameBuilder {
        this.linesPublisher = lines.map { l ->
            l.map {
                val label = labelFunc(it)
                val value = valueFunc(it)
                BarFrame.Line(value, label)
            }
        }
        return this
    }

    fun <T : Number> withLines(
        linesBinding: Flow.Publisher<out List<T>>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        return withLines(linesBinding, labelFunc) { it }
    }

    fun withMinBarCount(minBarCountBinding: Flow.Publisher<out Int>): BarFrameBuilder {
        this.minBarCountPublisher = minBarCountBinding
        return this
    }

    fun build(): BarFrame {
        val barsPublisher = (
            minBarCountPublisher?.let { minPublisher ->
                this.barsPublisher.merge(minPublisher) { bars, min ->
                    if (bars.size >= min) bars
                    else sequenceOf(bars, MutableList(min - bars.size) { BarFrame.Bar("", "", emptyList()) })
                        .flatten()
                        .toList()
                }
            }
                ?: this.barsPublisher
            )
        return BarFrame(
            headerPublisher = headerPublisher ?: (null as String?).asOneTimePublisher(),
            subheadTextPublisher = subheadPublisher,
            subheadColorPublisher = subheadColorPublisher,
            notesPublisher = notesPublisher,
            borderColorPublisher = borderColorPublisher,
            barsPublisher = barsPublisher,
            linesPublisher = linesPublisher,
            minPublisher = minPublisher,
            maxPublisher = maxPublisher
        )
    }

    companion object {
        @JvmStatic fun basic(publisher: Flow.Publisher<out List<BasicBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val rangeFinder = builder.rangeFinder
            builder.barsPublisher = publisher.map { bars ->
                bars.map {
                    BarFrame.Bar(it.label, it.valueLabel, it.shape, listOf(Pair(it.color, it.value)))
                }
            }
            builder.minPublisher = rangeFinder.minPublisher
            builder.maxPublisher = rangeFinder.maxPublisher
            publisher.subscribe(
                Subscriber { map: List<BasicBar> ->
                    rangeFinder.highest = map
                        .map { it.value }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> max(a, b) }
                    rangeFinder.lowest = map
                        .map { it.value }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> min(a, b) }
                }
            )
            return builder
        }

        @JvmStatic fun dual(bars: Flow.Publisher<out List<DualBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val rangeFinder = builder.rangeFinder
            val differentDirections =
                { bar: DualBar -> sign(bar.value1.toDouble()) * sign(bar.value2.toDouble()) == -1.0 }
            val reverse = { bar: DualBar ->
                (
                    differentDirections(bar) ||
                        abs(bar.value1.toDouble()) < abs(bar.value2.toDouble())
                    )
            }
            val first = { bar: DualBar -> if (reverse(bar)) bar.value1 else bar.value2 }
            val second = { bar: DualBar -> if (reverse(bar)) bar.value2 else bar.value1 }
            builder.barsPublisher = bars.map { b ->
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
            builder.minPublisher = rangeFinder.minPublisher
            builder.maxPublisher = rangeFinder.maxPublisher
            bars.subscribe(
                Subscriber { map: List<DualBar> ->
                    rangeFinder.highest = map
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> max(a, b) }
                    rangeFinder.lowest = map
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> min(a, b) }
                }
            )
            return builder
        }

        @JvmStatic fun dualReversed(bars: Flow.Publisher<out List<DualBar>>): BarFrameBuilder {
            val builder = BarFrameBuilder()
            val rangeFinder = builder.rangeFinder
            val differentDirections =
                { bar: DualBar -> sign(bar.value1.toDouble()) * sign(bar.value2.toDouble()) == -1.0 }
            val reverse = { bar: DualBar ->
                (
                    differentDirections(bar) ||
                        abs(bar.value1.toDouble()) < abs(bar.value2.toDouble())
                    )
            }
            val first = { bar: DualBar -> if (reverse(bar)) bar.value1 else bar.value2 }
            val second = { bar: DualBar -> if (reverse(bar)) bar.value2 else bar.value1 }
            builder.barsPublisher = bars.map { b ->
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
            builder.minPublisher = rangeFinder.minPublisher
            builder.maxPublisher = rangeFinder.maxPublisher
            bars.subscribe(
                Subscriber { map: List<DualBar> ->
                    rangeFinder.highest = map
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { a: Double, b: Double -> max(a, b) }
                    rangeFinder.lowest = map
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> min(a, b) }
                }
            )
            return builder
        }
    }
}
