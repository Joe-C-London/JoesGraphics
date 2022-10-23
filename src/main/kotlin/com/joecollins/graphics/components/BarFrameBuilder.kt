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
    private var rightHeaderLabelPublisher: Flow.Publisher<out String?>? = null
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
        var highest: Number = 0
            set(value) {
                field = value
                synchronized(this) {
                    minPublisher.submit(minFunction(this))
                    maxPublisher.submit(maxFunction(this))
                }
            }

        var lowest: Number = 0
            set(value) {
                field = value
                synchronized(this) {
                    minPublisher.submit(minFunction(this))
                    maxPublisher.submit(maxFunction(this))
                }
            }

        var minFunction: (RangeFinder) -> Number = { rf -> rf.lowest }
            set(value) {
                field = value
                synchronized(this) {
                    minPublisher.submit(minFunction(this))
                }
            }

        var maxFunction: (RangeFinder) -> Number = { rf -> rf.highest }
            set(value) {
                field = value
                synchronized(this) {
                    maxPublisher.submit(maxFunction(this))
                }
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

    fun withHeader(
        headerPublisher: Flow.Publisher<out String?>,
        rightLabelPublisher: Flow.Publisher<out String?> = null.asOneTimePublisher()
    ): BarFrameBuilder {
        this.headerPublisher = headerPublisher
        this.rightHeaderLabelPublisher = rightLabelPublisher
        return this
    }

    fun withSubhead(subheadPublisher: Flow.Publisher<out String?>): BarFrameBuilder {
        this.subheadPublisher = subheadPublisher
        return this
    }

    fun withNotes(notesPublisher: Flow.Publisher<out String?>): BarFrameBuilder {
        this.notesPublisher = notesPublisher
        return this
    }

    fun withBorder(borderColorPublisher: Flow.Publisher<out Color>): BarFrameBuilder {
        this.borderColorPublisher = borderColorPublisher
        return this
    }

    fun withSubheadColor(subheadColorPublisher: Flow.Publisher<out Color>): BarFrameBuilder {
        this.subheadColorPublisher = subheadColorPublisher
        return this
    }

    fun withMax(maxPublisher: Flow.Publisher<out Number>): BarFrameBuilder {
        return withLimits(maxPublisher.map { Limit(max = it) })
    }

    fun withWingspan(wingspanPublisher: Flow.Publisher<out Number>): BarFrameBuilder {
        return withLimits(wingspanPublisher.map { Limit(wingspan = it) })
    }

    data class Limit(val max: Number? = null, val wingspan: Number? = null) {
        init {
            if (sequenceOf(max, wingspan).filterNotNull().count() != 1) {
                throw IllegalArgumentException("Must have precisely one limit")
            }
        }
    }
    fun withLimits(limitsPublisher: Flow.Publisher<Limit>): BarFrameBuilder {
        limitsPublisher.subscribe(
            Subscriber { limit ->
                if (limit.max != null) {
                    rangeFinder.minFunction = { 0 }
                    rangeFinder.maxFunction = { max(limit.max.toDouble(), it.highest.toDouble()) }
                }
                if (limit.wingspan != null) {
                    val f = { rf: RangeFinder ->
                        max(
                            limit.wingspan.toDouble(),
                            max(abs(rf.lowest.toDouble()), abs(rf.highest.toDouble()))
                        )
                    }
                    rangeFinder.minFunction = { -f(it) }
                    rangeFinder.maxFunction = { +f(it) }
                }
            }
        )
        return this
    }

    fun <T : Number> withTarget(
        targetPublisher: Flow.Publisher<out T>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        this.linesPublisher = targetPublisher.map {
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
        linesPublisher: Flow.Publisher<out List<T>>,
        labelFunc: (T) -> String
    ): BarFrameBuilder {
        return withLines(linesPublisher, labelFunc) { it }
    }

    fun withMinBarCount(minBarCountPublisher: Flow.Publisher<out Int>): BarFrameBuilder {
        this.minBarCountPublisher = minBarCountPublisher
        return this
    }

    fun build(): BarFrame {
        val barsPublisher = (
            minBarCountPublisher?.let { minPublisher ->
                this.barsPublisher.merge(minPublisher) { bars, min ->
                    if (bars.size >= min) {
                        bars
                    } else {
                        sequenceOf(bars, MutableList(min - bars.size) { BarFrame.Bar("", "", emptyList()) })
                            .flatten()
                            .toList()
                    }
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
            maxPublisher = maxPublisher,
            headerLabelsPublisher = rightHeaderLabelPublisher?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) }
        )
    }

    companion object {
        fun basic(publisher: Flow.Publisher<out List<BasicBar>>): BarFrameBuilder {
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
                Subscriber { bars ->
                    rangeFinder.highest = bars
                        .map { it.value }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> max(a, b) }
                    rangeFinder.lowest = bars
                        .map { it.value }
                        .map { it.toDouble() }
                        .fold(0.0) { a, b -> min(a, b) }
                }
            )
            return builder
        }

        fun dual(bars: Flow.Publisher<out List<DualBar>>): BarFrameBuilder {
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
                        it.label,
                        it.valueLabel,
                        it.shape,
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

        fun dualReversed(bars: Flow.Publisher<out List<DualBar>>): BarFrameBuilder {
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
                        it.label,
                        it.valueLabel,
                        it.shape,
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
                Subscriber { b ->
                    rangeFinder.highest = b
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { x, y -> max(x, y) }
                    rangeFinder.lowest = b
                        .flatMap { sequenceOf(it.value1, it.value2) }
                        .map { it.toDouble() }
                        .fold(0.0) { x, y -> min(x, y) }
                }
            )
            return builder
        }
    }
}
