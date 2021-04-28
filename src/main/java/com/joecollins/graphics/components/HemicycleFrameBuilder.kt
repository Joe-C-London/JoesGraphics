package com.joecollins.graphics.components

import com.google.common.annotations.Beta
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import java.awt.Color
import java.awt.Point
import java.util.ArrayList
import java.util.Comparator
import kotlin.math.abs

class HemicycleFrameBuilder {
    enum class Tiebreaker {
        FRONT_ROW_FROM_LEFT, FRONT_ROW_FROM_RIGHT
    }

    var frame = HemicycleFrame()

    fun withHeader(headerBinding: Binding<String?>): HemicycleFrameBuilder {
        frame.setHeaderBinding(headerBinding)
        return this
    }

    fun <T> withLeftSeatBars(
        bars: Binding<List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setLeftSeatBarBinding(bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) })
        frame.setLeftSeatBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withRightSeatBars(
        bars: Binding<List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setRightSeatBarBinding(bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) })
        frame.setRightSeatBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withMiddleSeatBars(
        bars: Binding<List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setMiddleSeatBarBinding(bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) })
        frame.setMiddleSeatBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withLeftChangeBars(
        bars: Binding<List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startBinding: Binding<Int>,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setLeftChangeBarBinding(bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) })
        frame.setLeftChangeBarStartBinding(startBinding)
        frame.setLeftChangeBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withRightChangeBars(
        bars: Binding<List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startBinding: Binding<Int>,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setRightChangeBarBinding(bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) })
        frame.setRightChangeBarStartBinding(startBinding)
        frame.setRightChangeBarLabelBinding(labelBinding)
        return this
    }

    fun build(): HemicycleFrame {
        return frame
    }

    class Result(val winner: Party?, val hasWon: Boolean)
    companion object {
        @JvmStatic fun <T> of(
            rows: List<Int>,
            entries: List<T>,
            colorFunc: (T) -> Binding<Color>,
            tiebreaker: Tiebreaker
        ): HemicycleFrameBuilder {
            return of(rows, entries, colorFunc, colorFunc, tiebreaker)
        }

        @JvmStatic fun <T> of(
            rows: List<Int>,
            entries: List<T>,
            colorFunc: (T) -> Binding<Color>,
            borderFunc: (T) -> Binding<Color>,
            tiebreaker: Tiebreaker
        ): HemicycleFrameBuilder {
            return ofClustered(rows, entries, { 1 }, colorFunc, borderFunc, tiebreaker)
        }

        @Beta
        @JvmStatic fun <T> ofClustered(
            rows: List<Int>,
            entries: List<T>,
            seatsFunc: (T) -> Int,
            colorFunc: (T) -> Binding<Color>,
            tiebreaker: Tiebreaker
        ): HemicycleFrameBuilder {
            return ofClustered(rows, entries, seatsFunc, colorFunc, colorFunc, tiebreaker)
        }

        @Beta
        @JvmStatic fun <T> ofClustered(
            rows: List<Int>,
            entries: List<T>,
            seatsFunc: (T) -> Int,
            colorFunc: (T) -> Binding<Color>,
            borderFunc: (T) -> Binding<Color>,
            tiebreaker: Tiebreaker
        ): HemicycleFrameBuilder {
            val points = rows.indices
                    .flatMap { row -> (0 until rows[row]).map { idx: Int -> Point(row, idx) } }
                    .sortedWith(
                            Comparator.comparingDouble { p: Point -> 180.0 * p.y / (rows[p.x] - 1) }
                                    .thenComparingInt { p: Point -> (if (tiebreaker == Tiebreaker.FRONT_ROW_FROM_LEFT) 1 else -1) * p.x })
                    .map { point: Point -> Pair(point, null as T?) }
                    .toMutableList()
            for (entry in entries) {
                val rejectedPoints: MutableList<Point> = ArrayList()
                val selectedPoints: MutableList<Point> = ArrayList()
                val numDots = seatsFunc(entry)
                var i = 0
                while (i < numDots) {
                    val nextPoint = points.withIndex()
                            .filter { !rejectedPoints.contains(it.value.first) }
                            .filter { it.value.second == null }
                            .firstOrNull {
                                (selectedPoints.isEmpty() ||
                                        selectedPoints
                                        .any { point: Point -> pointsAreBesideEachOther(point, it.value.first, rows) })
                            }
                    if (nextPoint == null) {
                        rejectedPoints.addAll(selectedPoints)
                        selectedPoints.clear()
                        i--
                        i++
                        continue
                    }
                    points[nextPoint.index] = nextPoint.value.copy(second = entry)
                    selectedPoints.add(nextPoint.value.first)
                    i++
                }
            }
            val builder = HemicycleFrameBuilder()
            builder.frame.setRowsBinding(Binding.fixedBinding(rows))
            val dots: List<T> = points
                    .sortedWith(
                            Comparator.comparingInt { it: Pair<Point, T?> -> it.first.x }
                                    .thenComparing { it -> it.first.y })
                    .map { it.second!! }
                    .toList()
            builder.frame.setDotsBinding(Binding.listBinding(
                dots.map {
                    colorFunc(it).merge(borderFunc(it)) {
                        color, border -> HemicycleFrame.Dot(color = color, border = border)
                    }
                }
            ))
            return builder
        }

        private fun pointsAreBesideEachOther(a: Point, b: Point, rows: List<Int>): Boolean {
            if (a.x == b.x) {
                return abs(a.y - b.y) <= 1
            }
            if (abs(a.x - b.x) > 1) {
                return false
            }
            val aY: Double
            val bY: Double
            if (a.x > b.x) {
                aY = 1.0 * a.y
                bY = 1.0 * b.y / rows[b.x] * rows[a.x]
            } else {
                aY = 1.0 * a.y / rows[a.x] * rows[b.x]
                bY = 1.0 * b.y
            }
            return abs(aY - bY) <= 0.5
        }

        @JvmStatic fun <T> ofElectedLeading(
            rows: List<Int>,
            entries: List<T>,
            resultFunc: (T) -> Binding<Result?>,
            prevResultFunc: (T) -> Party,
            leftParty: Party,
            rightParty: Party,
            leftLabel: (Int, Int) -> String,
            rightLabel: (Int, Int) -> String,
            otherLabel: (Int, Int) -> String,
            showChange: (Int, Int) -> Boolean,
            changeLabel: (Int, Int) -> String,
            tiebreaker: Tiebreaker,
            header: Binding<String?>
        ): HemicycleFrame {
            return ofElectedLeading(
                    rows,
                    entries,
                    { 1 },
                    resultFunc,
                    prevResultFunc,
                    leftParty,
                    rightParty,
                    leftLabel,
                    rightLabel,
                    otherLabel,
                    showChange,
                    changeLabel,
                    tiebreaker,
                    header)
        }

        @Beta
        @JvmStatic fun <T> ofElectedLeading(
            rows: List<Int>,
            entries: List<T>,
            seatsFunc: (T) -> Int,
            resultFunc: (T) -> Binding<Result?>,
            prevResultFunc: (T) -> Party,
            leftParty: Party,
            rightParty: Party,
            leftLabel: (Int, Int) -> String,
            rightLabel: (Int, Int) -> String,
            otherLabel: (Int, Int) -> String,
            showChange: (Int, Int) -> Boolean,
            changeLabel: (Int, Int) -> String,
            tiebreaker: Tiebreaker,
            header: Binding<String?>
        ): HemicycleFrame {
            val results: Map<T, BindingReceiver<Result?>> = entries
                    .distinct()
                    .associateWith { BindingReceiver(resultFunc(it)) }
            val prev = entries.distinct().associateWith(prevResultFunc)
            val resultBindings = entries
                    .map {
                        BindingReceiver(
                                results[it]!!.getBinding { x: Result? -> Pair(x, seatsFunc(it)) })
                    }
                    .toList()
            val resultWithPrevBindings: List<BindingReceiver<Triple<Result?, Party, Int>>> = entries
                    .map {
                        BindingReceiver(
                                results[it]!!.getBinding { result: Result? -> Triple(result, prev[it]!!, seatsFunc(it)) })
                    }
                    .toList()
            val leftSeats = createSeatBarBinding(resultBindings) { it == leftParty }
            val leftList = leftSeats.getBinding { listOf(
                Pair(leftParty.color, it.first),
                Pair(ColorUtils.lighten(leftParty.color), it.second - it.first)) }
            val rightSeats = createSeatBarBinding(resultBindings) { it == rightParty }
            val rightList = rightSeats.getBinding { listOf(
                Pair(rightParty.color, it.first),
                Pair(ColorUtils.lighten(rightParty.color), it.second - it.first)) }
            val middleSeats = createSeatBarBinding(
                    resultBindings
            ) { party: Party? -> party != null && party != leftParty && party != rightParty }
            val middleList = middleSeats.getBinding { listOf(
                Pair(Party.OTHERS.color, it.first),
                Pair(ColorUtils.lighten(Party.OTHERS.color), it.second - it.first)) }
            val leftChange = createChangeBarBinding(
                    resultWithPrevBindings
            ) { it == leftParty }
            val leftChangeList = leftChange.getBinding {
                if (showChange(it.first, it.second)) {
                        listOf(
                            Pair(leftParty.color, it.first),
                            Pair(ColorUtils.lighten(leftParty.color), it.second - it.first))
                } else {
                    emptyList()
                }
            }
            val rightChange = createChangeBarBinding(
                    resultWithPrevBindings
            ) { it == rightParty }
            val rightChangeList = rightChange.getBinding {
                if (showChange(it.first, it.second)) {
                    listOf(
                        Pair(rightParty.color, it.first),
                        Pair(ColorUtils.lighten(rightParty.color), it.second - it.first))
                } else {
                    emptyList()
                }
            }
            val changeLabelFunc = { p: Pair<Int, Int> -> if (showChange(p.first, p.second)) changeLabel(p.first, p.second) else "" }
            val allPrevs: List<Pair<Party, Int>> = entries
                    .map { Pair(prev[it]!!, seatsFunc(it)) }
                    .toList()
            return ofClustered(
                    rows,
                    entries,
                    seatsFunc,
                    {
                        results[it]!!.getBinding { result: Result? ->
                                    when {
                                        result?.winner == null -> Color.WHITE
                                        result.hasWon -> result.winner.color
                                        else -> ColorUtils.lighten(result.winner.color)
                                    }
                                }
                    },
                    { Binding.fixedBinding(prevResultFunc(it).color) },
                    tiebreaker)
                    .withLeftSeatBars(
                            leftList, { it.first }, { it.second },
                            leftSeats.getBinding { leftLabel(it.first, it.second) })
                    .withRightSeatBars(
                            rightList, { it.first }, { it.second },
                            rightSeats.getBinding { rightLabel(it.first, it.second) })
                    .withMiddleSeatBars(
                            middleList, { it.first }, { it.second },
                            middleSeats.getBinding { otherLabel(it.first, it.second) })
                    .withLeftChangeBars(
                            leftChangeList, { it.first }, { it.second },
                            Binding.fixedBinding(calcPrevForParty(allPrevs, leftParty)),
                            leftChange.getBinding(changeLabelFunc))
                    .withRightChangeBars(
                            rightChangeList, { it.first }, { it.second },
                            Binding.fixedBinding(calcPrevForParty(allPrevs, rightParty)),
                            rightChange.getBinding(changeLabelFunc))
                    .withHeader(header)
                    .build()
        }

        private fun calcPrevForParty(prev: List<Pair<Party, Int>>, party: Party): Int {
            return prev.filter { party == it.first }.map { it.second }.sum()
        }

        private fun createSeatBarBinding(
            results: List<BindingReceiver<Pair<Result?, Int>>>,
            partyFilter: (Party?) -> Boolean
        ): BindingReceiver<Pair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                results.map { it.getBinding() }.toList(),
                Pair(0, 0),
                { p: Pair<Int, Int>, r: Pair<Result?, Int> ->
                    val result = r.first
                    if (result == null || !partyFilter(result.winner)) {
                        p
                    } else {
                        Pair(p.first + if (result.hasWon) r.second else 0, p.second + r.second)
                    }
                },
                { p: Pair<Int, Int>, r: Pair<Result?, Int> ->
                    val result = r.first
                    if (result == null || !partyFilter(result.winner)) {
                        p
                    } else {
                        Pair(p.first - if (result.hasWon) r.second else 0, p.second - r.second)
                    }
                })
            return BindingReceiver(binding)
        }

        private fun createChangeBarBinding(
            resultWithPrev: List<BindingReceiver<Triple<Result?, Party, Int>>>,
            partyFilter: (Party?) -> Boolean
        ): BindingReceiver<Pair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                resultWithPrev.map { it.getBinding() }.toList(),
                Pair(0, 0),
                { p: Pair<Int, Int>, r: Triple<Result?, Party, Int> ->
                    var ret = p
                    val result = r.first
                    if (result?.winner == null) {
                        ret
                    } else {
                        if (partyFilter(result.winner)) {
                            ret = Pair(ret.first + if (result.hasWon) r.third else 0, ret.second + r.third)
                        }
                        if (partyFilter(r.second)) {
                            ret = Pair(ret.first - if (result.hasWon) r.third else 0, ret.second - r.third)
                        }
                        ret
                    }
                },
                { p: Pair<Int, Int>, r: Triple<Result?, Party, Int> ->
                    var ret = p
                    val result = r.first
                    if (result?.winner == null) {
                        ret
                    } else {
                        if (partyFilter(result.winner)) {
                            ret = Pair(ret.first - if (result.hasWon) r.third else 0, ret.second - r.third)
                        }
                        if (partyFilter(r.second)) {
                            ret = Pair(ret.first + if (result.hasWon) r.third else 0, ret.second + r.third)
                        }
                        ret
                    }
                })
            return BindingReceiver(binding)
        }
    }
}
