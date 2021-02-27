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
import org.apache.commons.lang3.tuple.ImmutablePair
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.apache.commons.lang3.tuple.MutablePair
import org.apache.commons.lang3.tuple.Pair
import org.apache.commons.lang3.tuple.Triple

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
                                        .any { point: Point -> pointsAreBesideEachOther<Any>(point, it.left, rows) })
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

        private fun <T> pointsAreBesideEachOther(a: Point, b: Point, rows: List<Int>): Boolean {
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
                        BindingReceiver<Pair<Result, Int>>(
                                results[it]!!.getBinding { x: Result? -> ImmutablePair.of(x, seatsFunc(it)) })
                    }
                    .toList()
            val resultWithPrevBindings: List<BindingReceiver<Triple<Result, Party, Int>>> = entries
                    .map {
                        BindingReceiver<Triple<Result, Party, Int>>(
                                results[it]!!.getBinding { result: Result? -> ImmutableTriple.of(result, prev[it], seatsFunc(it)) })
                    }
                    .toList()
            val leftList = BindableList<ImmutablePair<Color, Int>>()
            val leftSeats = createSeatBarBinding<Any>(resultBindings, leftList, { it == leftParty }, leftParty.color)
            val rightList = BindableList<ImmutablePair<Color, Int>>()
            val rightSeats = createSeatBarBinding<Any>(resultBindings, rightList, { it == rightParty }, rightParty.color)
            val middleList = BindableList<ImmutablePair<Color, Int>>()
            val middleSeats = createSeatBarBinding<Any>(
                    resultBindings,
                    middleList,
                    { party: Party? -> party != null && party != leftParty && party != rightParty },
                    Party.OTHERS.color)
            val leftChangeList = BindableList<ImmutablePair<Color, Int>>()
            val leftChange = createChangeBarBinding<Any>(
                    resultWithPrevBindings,
                    leftChangeList, { it == leftParty },
                    leftParty.color,
                    showChange)
            val rightChangeList = BindableList<ImmutablePair<Color, Int>>()
            val rightChange = createChangeBarBinding<Any>(
                    resultWithPrevBindings,
                    rightChangeList, { it == rightParty },
                    rightParty.color,
                    showChange)
            val changeLabelFunc = { p: ImmutablePair<Int, Int> -> if (showChange(p.left, p.right)) changeLabel(p.left, p.right) else "" }
            val allPrevs: List<Pair<Party, Int>> = entries
                    .map { ImmutablePair.of(prev[it]!!, seatsFunc(it)) }
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
                            leftList, { it.getLeft() }, { it.getRight() },
                            leftSeats.getBinding { leftLabel(it.left, it.right) })
                    .withRightSeatBars(
                            rightList, { it.getLeft() }, { it.getRight() },
                            rightSeats.getBinding { rightLabel(it.left, it.right) })
                    .withMiddleSeatBars(
                            middleList, { it.getLeft() }, { it.getRight() },
                            middleSeats.getBinding { otherLabel(it.left, it.right) })
                    .withLeftChangeBars(
                            leftChangeList, { it.getLeft() }, { it.getRight() },
                            Binding.fixedBinding(calcPrevForParty<Any>(allPrevs, leftParty)),
                            leftChange.getBinding(changeLabelFunc))
                    .withRightChangeBars(
                            rightChangeList, { it.getLeft() }, { it.getRight() },
                            Binding.fixedBinding(calcPrevForParty<Any>(allPrevs, rightParty)),
                            rightChange.getBinding(changeLabelFunc))
                    .withHeader(header)
                    .build()
        }

        private fun <T> calcPrevForParty(prev: List<Pair<Party, Int>>, party: Party): Int {
            return prev.filter { party == it.left }.map { it.right }.sum()
        }

        private fun <T> createSeatBarBinding(
            results: List<BindingReceiver<Pair<Result, Int>>>,
            list: BindableList<ImmutablePair<Color, Int>>,
            partyFilter: (Party?) -> Boolean,
            color: Color
        ): BindingReceiver<ImmutablePair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                    results.map { it.getBinding() }.toList(),
                    ImmutablePair.of(0, 0),
                    { p: ImmutablePair<Int, Int>, r: Pair<Result, Int> ->
                        if (r.left == null || !partyFilter(r.left.winner)) {
                            p
                        } else {
                            ImmutablePair.of(
                                    p.left + if (r.left.hasWon) r.right else 0, p.right + r.right)
                        }
                    },
                    { p: ImmutablePair<Int, Int>, r: Pair<Result, Int> ->
                        if (r.left == null || !partyFilter(r.left.winner)) {
                            p
                        } else {
                            ImmutablePair.of(
                                    p.left - if (r.left.hasWon) r.right else 0, p.right - r.right)
                        }
                    })
            val seats = BindingReceiver(binding)
            seats.getBinding()
                    .bind {
                        list.setAll(
                                listOf(
                                        ImmutablePair.of(color, it.left),
                                        ImmutablePair.of(ColorUtils.lighten(color), it.right - it.left)))
                    }
            return seats
        }

        private fun <T> createChangeBarBinding(
            resultWithPrev: List<BindingReceiver<Triple<Result, Party, Int>>>,
            list: BindableList<ImmutablePair<Color, Int>>,
            partyFilter: (Party?) -> Boolean,
            color: Color,
            showChangeBars: (Int, Int) -> Boolean
        ): BindingReceiver<ImmutablePair<Int, Int>> {
            val binding = Binding.mapReduceBinding(
                    resultWithPrev.map { it.getBinding() }.toList(),
                    ImmutablePair.of(0, 0),
                    { p: ImmutablePair<Int, Int>, r: Triple<Result, Party, Int> ->
                        var ret = p
                        if (r.left == null || r.left.winner == null) {
                            ret
                        } else {
                            if (partyFilter(r.left.winner)) {
                                ret = ImmutablePair.of(
                                        ret.left + if (r.left.hasWon) r.right else 0, ret.right + r.right)
                            }
                            if (partyFilter(r.middle)) {
                                ret = ImmutablePair.of(
                                        ret.left - if (r.left.hasWon) r.right else 0, ret.right - r.right)
                            }
                            ret
                        }
                    },
                    { p: ImmutablePair<Int, Int>, r: Triple<Result, Party, Int> ->
                        var ret = p
                        if (r.left == null || r.left.winner == null) {
                            ret
                        } else {
                            if (partyFilter(r.left.winner)) {
                                ret = ImmutablePair.of(
                                        ret.left - if (r.left.hasWon) r.right else 0, ret.right - r.right)
                            }
                            if (partyFilter(r.middle)) {
                                ret = ImmutablePair.of(
                                        ret.left + if (r.left.hasWon) r.right else 0, ret.right + r.right)
                            }
                            ret
                        }
                    })
            val seats = BindingReceiver(binding)
            seats.getBinding()
                    .bind {
                        if (showChangeBars(it.left, it.right)) {
                            list.setAll(
                                    listOf(
                                            ImmutablePair.of(color, it.left),
                                            ImmutablePair.of(ColorUtils.lighten(color), it.right - it.left)))
                        } else {
                            list.clear()
                        }
                    }
            return seats
        }
    }
}
