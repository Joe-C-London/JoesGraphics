package com.joecollins.graphics.components

import com.google.common.annotations.Beta
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.bindings.IndexedBinding
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import java.awt.Color
import java.awt.Point
import java.util.ArrayList
import java.util.Comparator
import kotlin.math.abs
import org.apache.commons.lang3.tuple.MutablePair

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
        bars: BindableList<T>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setLeftSeatBarCountBinding(Binding.sizeBinding(bars))
        frame.setLeftSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc))
        frame.setLeftSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc))
        frame.setLeftSeatBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withRightSeatBars(
        bars: BindableList<T>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setRightSeatBarCountBinding(Binding.sizeBinding(bars))
        frame.setRightSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc))
        frame.setRightSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc))
        frame.setRightSeatBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withMiddleSeatBars(
        bars: BindableList<T>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setMiddleSeatBarCountBinding(Binding.sizeBinding(bars))
        frame.setMiddleSeatBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc))
        frame.setMiddleSeatBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc))
        frame.setMiddleSeatBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withLeftChangeBars(
        bars: BindableList<T>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startBinding: Binding<Int>,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setLeftChangeBarCountBinding(Binding.sizeBinding(bars))
        frame.setLeftChangeBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc))
        frame.setLeftChangeBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc))
        frame.setLeftChangeBarStartBinding(startBinding)
        frame.setLeftChangeBarLabelBinding(labelBinding)
        return this
    }

    fun <T> withRightChangeBars(
        bars: BindableList<T>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startBinding: Binding<Int>,
        labelBinding: Binding<String>
    ): HemicycleFrameBuilder {
        frame.setRightChangeBarCountBinding(Binding.sizeBinding(bars))
        frame.setRightChangeBarColorBinding(IndexedBinding.propertyBinding(bars, colorFunc))
        frame.setRightChangeBarSizeBinding(IndexedBinding.propertyBinding(bars, seatFunc))
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
                    .map { point: Point -> MutablePair(point, null as T?) }
                    .toList()
            for (entry in entries) {
                val rejectedPoints: MutableList<Point> = ArrayList()
                val selectedPoints: MutableList<Point> = ArrayList()
                val numDots = seatsFunc(entry)
                var i = 0
                while (i < numDots) {
                    val nextPoint = points
                            .filter { !rejectedPoints.contains(it.left) }
                            .filter { it.right == null }
                            .firstOrNull {
                                (selectedPoints.isEmpty() ||
                                        selectedPoints
                                        .any { point: Point -> pointsAreBesideEachOther(point, it.left, rows) })
                            }
                    if (nextPoint == null) {
                        rejectedPoints.addAll(selectedPoints)
                        selectedPoints.clear()
                        i--
                        i++
                        continue
                    }
                    nextPoint.setRight(entry)
                    selectedPoints.add(nextPoint.left)
                    i++
                }
            }
            val builder = HemicycleFrameBuilder()
            builder.frame.setNumRowsBinding(Binding.fixedBinding(rows.size))
            builder.frame.setRowCountsBinding(IndexedBinding.listBinding(rows))
            val dots: List<T> = points
                    .sortedWith(
                            Comparator.comparingInt { it: MutablePair<Point, T?> -> it.left.x }
                                    .thenComparing { it -> it.left.y })
                    .map { it.getRight()!! }
                    .toList()
            builder.frame.setNumDotsBinding(Binding.fixedBinding(dots.size))
            builder.frame.setDotColorBinding(IndexedBinding.listBinding(dots, colorFunc))
            builder.frame.setDotBorderBinding(IndexedBinding.listBinding(dots, borderFunc))
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
            val leftList = BindableList<Pair<Color, Int>>()
            val leftSeats = createSeatBarBinding(resultBindings, leftList, { it == leftParty }, leftParty.color)
            val rightList = BindableList<Pair<Color, Int>>()
            val rightSeats = createSeatBarBinding(resultBindings, rightList, { it == rightParty }, rightParty.color)
            val middleList = BindableList<Pair<Color, Int>>()
            val middleSeats = createSeatBarBinding(
                    resultBindings,
                    middleList,
                    { party: Party? -> party != null && party != leftParty && party != rightParty },
                    Party.OTHERS.color)
            val leftChangeList = BindableList<Pair<Color, Int>>()
            val leftChange = createChangeBarBinding(
                    resultWithPrevBindings,
                    leftChangeList, { it == leftParty },
                    leftParty.color,
                    showChange)
            val rightChangeList = BindableList<Pair<Color, Int>>()
            val rightChange = createChangeBarBinding(
                    resultWithPrevBindings,
                    rightChangeList, { it == rightParty },
                    rightParty.color,
                    showChange)
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
            list: BindableList<Pair<Color, Int>>,
            partyFilter: (Party?) -> Boolean,
            color: Color
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
            val seats = BindingReceiver(binding)
            seats.getBinding()
                    .bind {
                        list.setAll(
                                listOf(
                                        Pair(color, it.first),
                                        Pair(ColorUtils.lighten(color), it.second - it.first)))
                    }
            return seats
        }

        private fun createChangeBarBinding(
            resultWithPrev: List<BindingReceiver<Triple<Result?, Party, Int>>>,
            list: BindableList<Pair<Color, Int>>,
            partyFilter: (Party?) -> Boolean,
            color: Color,
            showChangeBars: (Int, Int) -> Boolean
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
            val seats = BindingReceiver(binding)
            seats.getBinding()
                    .bind {
                        if (showChangeBars(it.first, it.second)) {
                            list.setAll(
                                    listOf(
                                            Pair(color, it.first),
                                            Pair(ColorUtils.lighten(color), it.second - it.first)))
                        } else {
                            list.clear()
                        }
                    }
            return seats
        }
    }
}
