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

object BarFrameBuilder {
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

    class BasicBar(val label: List<String>, val color: Color, val value: Number, val valueLabel: List<String> = listOf(value.toString()), val shape: Shape? = null) {
        constructor(label: List<String>, color: Color, value: Number, shape: Shape? = null) : this(label, color, value, listOf(value.toString()), shape)
        constructor(label: String, color: Color, value: Number, shape: Shape? = null) : this(label, color, value, value.toString(), shape)
        constructor(label: String, color: Color, value: Number, valueLabel: String, shape: Shape? = null) : this(listOf(label), color, value, listOf(valueLabel), shape)
    }

    class DualBar(
        val label: List<String>,
        val color: Color,
        val value1: Number,
        val value2: Number,
        val valueLabel: List<String>,
        val shape: Shape? = null,
    ) {
        constructor(label: String, color: Color, value1: Number, value2: Number, valueLabel: String, shape: Shape? = null) : this(listOf(label), color, value1, value2, listOf(valueLabel), shape)
    }

    data class Limit(val max: Number? = null, val wingspan: Number? = null) {
        init {
            if (sequenceOf(max, wingspan).filterNotNull().count() != 1) {
                throw IllegalArgumentException("Must have precisely one limit")
            }
        }
    }

    data class Target<T : Number>(val publisher: Flow.Publisher<T>, val label: T.() -> String) {
        val lines = publisher.map { target ->
            listOf(BarFrame.Line(target, target.label()))
        }
    }

    data class Lines<T> private constructor(val publisher: Flow.Publisher<List<T>>, val label: T.() -> String, val value: T.() -> Number) {
        val lines = publisher.map { values ->
            values.map { BarFrame.Line(it.value(), it.label()) }
        }

        companion object {
            fun <T : Number> of(publisher: Flow.Publisher<List<T>>, labelFunc: T.() -> String): Lines<T> = Lines(publisher, labelFunc) { this }

            fun <T> of(publisher: Flow.Publisher<List<T>>, labelFunc: T.() -> String, valueFunc: T.() -> Number): Lines<T> = Lines(publisher, labelFunc, valueFunc)
        }
    }

    fun basic(
        barsPublisher: Flow.Publisher<out List<BasicBar>>,
        headerPublisher: Flow.Publisher<out String?> = null.asOneTimePublisher(),
        rightHeaderLabelPublisher: Flow.Publisher<out String?>? = null,
        subheadPublisher: Flow.Publisher<out String?>? = null,
        notesPublisher: Flow.Publisher<out String?>? = null,
        borderColorPublisher: Flow.Publisher<out Color>? = null,
        subheadColorPublisher: Flow.Publisher<out Color>? = null,
        maxPublisher: Flow.Publisher<out Number>? = null,
        wingspanPublisher: Flow.Publisher<out Number>? = null,
        limitsPublisher: Flow.Publisher<Limit>? = convertToLimits(maxPublisher, wingspanPublisher),
        targetPublisher: Target<*>? = null,
        linesPublisher: Lines<*>? = null,
        minBarCountPublisher: Flow.Publisher<out Int>? = null,
    ): BarFrame {
        val rangeFinder = RangeFinder()
        barsPublisher.subscribe(
            Subscriber { bars ->
                val values = bars.map { it.value.toDouble() }
                rangeFinder.highest = values.fold(0.0, ::max)
                rangeFinder.lowest = values.fold(0.0, ::min)
            },
        )
        rangeFinder.setupLimits(limitsPublisher)
        return BarFrame(
            barsPublisher = barsPublisher.map { bars ->
                bars.map {
                    BarFrame.Bar(it.label, it.valueLabel, it.shape, listOf(Pair(it.color, it.value)))
                }
            }.run {
                if (minBarCountPublisher == null) {
                    this
                } else {
                    this.merge(minBarCountPublisher, BarFrameBuilder::createMinBars)
                }
            },
            headerPublisher = headerPublisher,
            headerLabelsPublisher = rightHeaderLabelPublisher?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
            subheadTextPublisher = subheadPublisher,
            notesPublisher = notesPublisher,
            borderColorPublisher = borderColorPublisher,
            subheadColorPublisher = subheadColorPublisher,
            minPublisher = rangeFinder.minPublisher,
            maxPublisher = rangeFinder.maxPublisher,
            linesPublisher = linesPublisher?.lines ?: targetPublisher?.lines,
        )
    }

    private fun RangeFinder.setupLimits(
        limitsPublisher: Flow.Publisher<Limit>?,
    ) {
        limitsPublisher?.subscribe(
            Subscriber { limits ->
                if (limits.max != null) {
                    minFunction = { 0 }
                    maxFunction = { max(limits.max.toDouble(), it.highest.toDouble()) }
                } else if (limits.wingspan != null) {
                    val f = { rf: RangeFinder ->
                        max(
                            limits.wingspan.toDouble(),
                            max(abs(rf.lowest.toDouble()), abs(rf.highest.toDouble())),
                        )
                    }
                    minFunction = { -f(it) }
                    maxFunction = { +f(it) }
                } else {
                    minFunction = { it.lowest }
                    maxFunction = { it.highest }
                }
            },
        )
    }

    private fun convertToLimits(
        maxPublisher: Flow.Publisher<out Number>?,
        wingspanPublisher: Flow.Publisher<out Number>?,
    ) = (
        maxPublisher?.let { max -> max.map { Limit(max = it) } }
            ?: wingspanPublisher?.let { wingspan -> wingspan.map { Limit(wingspan = it) } }
        )

    private fun createMinBars(
        bars: List<BarFrame.Bar>,
        min: Int,
    ) = if (bars.size >= min) {
        bars
    } else {
        sequenceOf(bars, MutableList(min - bars.size) { BarFrame.Bar(emptyList(), emptyList(), emptyList()) })
            .flatten()
            .toList()
    }

    fun dual(
        barsPublisher: Flow.Publisher<out List<DualBar>>,
        headerPublisher: Flow.Publisher<out String?> = null.asOneTimePublisher(),
        rightHeaderLabelPublisher: Flow.Publisher<out String?>? = null,
        subheadPublisher: Flow.Publisher<out String?>? = null,
        notesPublisher: Flow.Publisher<out String?>? = null,
        borderColorPublisher: Flow.Publisher<out Color>? = null,
        subheadColorPublisher: Flow.Publisher<out Color>? = null,
        maxPublisher: Flow.Publisher<out Number>? = null,
        wingspanPublisher: Flow.Publisher<out Number>? = null,
        limitsPublisher: Flow.Publisher<Limit>? = convertToLimits(maxPublisher, wingspanPublisher),
        targetPublisher: Target<*>? = null,
        linesPublisher: Lines<*>? = null,
        minBarCountPublisher: Flow.Publisher<out Int>? = null,
    ): BarFrame {
        val rangeFinder = RangeFinder()
        barsPublisher.subscribe(
            Subscriber { bars ->
                val values = bars.flatMap { sequenceOf(it.value1, it.value2).map(Number::toDouble) }
                rangeFinder.highest = values.fold(0.0, ::max)
                rangeFinder.lowest = values.fold(0.0, ::min)
            },
        )
        rangeFinder.setupLimits(limitsPublisher)
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
        return BarFrame(
            barsPublisher = barsPublisher.map { b ->
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
                                second(it).toDouble() - if (differentDirections(it)) 0.0 else first(it).toDouble(),
                            ),
                        ),
                    )
                }
            }.run {
                if (minBarCountPublisher == null) {
                    this
                } else {
                    this.merge(minBarCountPublisher, BarFrameBuilder::createMinBars)
                }
            },
            headerPublisher = headerPublisher,
            headerLabelsPublisher = rightHeaderLabelPublisher?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
            subheadTextPublisher = subheadPublisher,
            notesPublisher = notesPublisher,
            borderColorPublisher = borderColorPublisher,
            subheadColorPublisher = subheadColorPublisher,
            minPublisher = rangeFinder.minPublisher,
            maxPublisher = rangeFinder.maxPublisher,
            linesPublisher = linesPublisher?.lines ?: targetPublisher?.lines,
        )
    }

    fun dualReversed(
        barsPublisher: Flow.Publisher<out List<DualBar>>,
        headerPublisher: Flow.Publisher<out String?> = null.asOneTimePublisher(),
        rightHeaderLabelPublisher: Flow.Publisher<out String?>? = null,
        subheadPublisher: Flow.Publisher<out String?>? = null,
        notesPublisher: Flow.Publisher<out String?>? = null,
        borderColorPublisher: Flow.Publisher<out Color>? = null,
        subheadColorPublisher: Flow.Publisher<out Color>? = null,
        maxPublisher: Flow.Publisher<out Number>? = null,
        wingspanPublisher: Flow.Publisher<out Number>? = null,
        limitsPublisher: Flow.Publisher<Limit>? = convertToLimits(maxPublisher, wingspanPublisher),
        targetPublisher: Target<*>? = null,
        linesPublisher: Lines<*>? = null,
        minBarCountPublisher: Flow.Publisher<out Int>? = null,
    ): BarFrame {
        val rangeFinder = RangeFinder()
        barsPublisher.subscribe(
            Subscriber { bars ->
                val values = bars.flatMap { sequenceOf(it.value1, it.value2).map(Number::toDouble) }
                rangeFinder.highest = values.fold(0.0, ::max)
                rangeFinder.lowest = values.fold(0.0, ::min)
            },
        )
        rangeFinder.setupLimits(limitsPublisher)
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
        return BarFrame(
            barsPublisher = barsPublisher.map { b ->
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
                                second(it).toDouble() - if (differentDirections(it)) 0.0 else first(it).toDouble(),
                            ),
                        ),
                    )
                }
            }.run {
                if (minBarCountPublisher == null) {
                    this
                } else {
                    this.merge(minBarCountPublisher, BarFrameBuilder::createMinBars)
                }
            },
            headerPublisher = headerPublisher,
            headerLabelsPublisher = rightHeaderLabelPublisher?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
            subheadTextPublisher = subheadPublisher,
            notesPublisher = notesPublisher,
            borderColorPublisher = borderColorPublisher,
            subheadColorPublisher = subheadColorPublisher,
            minPublisher = rangeFinder.minPublisher,
            maxPublisher = rangeFinder.maxPublisher,
            linesPublisher = linesPublisher?.lines ?: targetPublisher?.lines,
        )
    }
}
