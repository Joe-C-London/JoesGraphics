package com.joecollins.graphics.components

import com.google.common.annotations.Beta
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.mapReduce
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Point
import java.util.ArrayList
import java.util.Comparator
import java.util.concurrent.Flow
import kotlin.math.abs

class HemicycleFrameBuilder {
    private var headerPublisher: Flow.Publisher<out String?>? = null
    private var leftSeatBarPublisher: Flow.Publisher<out List<HemicycleFrame.Bar>>? = null
    private var leftSeatBarLabelPublisher: Flow.Publisher<out String>? = null
    private var rightSeatBarPublisher: Flow.Publisher<out List<HemicycleFrame.Bar>>? = null
    private var rightSeatBarLabelPublisher: Flow.Publisher<out String>? = null
    private var middleSeatBarPublisher: Flow.Publisher<out List<HemicycleFrame.Bar>>? = null
    private var middleSeatBarLabelPublisher: Flow.Publisher<out String>? = null
    private var leftChangeBarPublisher: Flow.Publisher<out List<HemicycleFrame.Bar>>? = null
    private var leftChangeBarStartPublisher: Flow.Publisher<out Int>? = null
    private var leftChangeBarLabelPublisher: Flow.Publisher<out String>? = null
    private var rightChangeBarPublisher: Flow.Publisher<out List<HemicycleFrame.Bar>>? = null
    private var rightChangeBarStartPublisher: Flow.Publisher<out Int>? = null
    private var rightChangeBarLabelPublisher: Flow.Publisher<out String>? = null
    private var rowsPublisher: Flow.Publisher<out List<Int>>? = null
    private var dotsPublisher: Flow.Publisher<out List<HemicycleFrame.Dot>>? = null

    enum class Tiebreaker {
        FRONT_ROW_FROM_LEFT, FRONT_ROW_FROM_RIGHT
    }

    fun withHeader(headerPublisher: Flow.Publisher<out String?>): HemicycleFrameBuilder {
        this.headerPublisher = headerPublisher
        return this
    }

    fun <T> withLeftSeatBars(
        bars: Flow.Publisher<out List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelPublisher: Flow.Publisher<out String>
    ): HemicycleFrameBuilder {
        this.leftSeatBarPublisher = bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) }
        this.leftSeatBarLabelPublisher = labelPublisher
        return this
    }

    fun <T> withRightSeatBars(
        bars: Flow.Publisher<out List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelPublisher: Flow.Publisher<out String>
    ): HemicycleFrameBuilder {
        this.rightSeatBarPublisher = bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) }
        this.rightSeatBarLabelPublisher = labelPublisher
        return this
    }

    fun <T> withMiddleSeatBars(
        bars: Flow.Publisher<out List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelPublisher: Flow.Publisher<out String>
    ): HemicycleFrameBuilder {
        this.middleSeatBarPublisher = bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) }
        this.middleSeatBarLabelPublisher = labelPublisher
        return this
    }

    fun <T> withLeftChangeBars(
        bars: Flow.Publisher<out List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startPublisher: Flow.Publisher<out Int>,
        labelPublisher: Flow.Publisher<out String>
    ): HemicycleFrameBuilder {
        this.leftChangeBarPublisher = bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) }
        this.leftChangeBarStartPublisher = startPublisher
        this.leftChangeBarLabelPublisher = labelPublisher
        return this
    }

    fun <T> withRightChangeBars(
        bars: Flow.Publisher<out List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startPublisher: Flow.Publisher<out Int>,
        labelPublisher: Flow.Publisher<out String>
    ): HemicycleFrameBuilder {
        this.rightChangeBarPublisher = bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) }
        this.rightChangeBarStartPublisher = startPublisher
        this.rightChangeBarLabelPublisher = labelPublisher
        return this
    }

    fun build(): HemicycleFrame {
        return HemicycleFrame(
            headerPublisher = headerPublisher ?: (null as String?).asOneTimePublisher(),
            rowsPublisher = rowsPublisher,
            dotsPublisher = dotsPublisher ?: emptyList<HemicycleFrame.Dot>().asOneTimePublisher(),
            leftSeatBarPublisher = leftSeatBarPublisher,
            leftSeatBarLabelPublisher = leftSeatBarLabelPublisher,
            rightSeatBarPublisher = rightSeatBarPublisher,
            rightSeatBarLabelPublisher = rightSeatBarLabelPublisher,
            middleSeatBarPublisher = middleSeatBarPublisher,
            middleSeatBarLabelPublisher = middleSeatBarLabelPublisher,
            leftChangeBarPublisher = leftChangeBarPublisher,
            leftChangeBarStartPublisher = leftChangeBarStartPublisher,
            leftChangeBarLabelPublisher = leftChangeBarLabelPublisher,
            rightChangeBarPublisher = rightChangeBarPublisher,
            rightChangeBarStartPublisher = rightChangeBarStartPublisher,
            rightChangeBarLabelPublisher = rightChangeBarLabelPublisher
        )
    }

    class Result(val winner: Party?, val hasWon: Boolean)
    companion object {
        @JvmStatic fun <T> of(
            rows: List<Int>,
            entries: List<T>,
            colorFunc: (T) -> Flow.Publisher<out Color>,
            tiebreaker: Tiebreaker
        ): HemicycleFrameBuilder {
            return of(rows, entries, colorFunc, colorFunc, tiebreaker)
        }

        @JvmStatic fun <T> of(
            rows: List<Int>,
            entries: List<T>,
            colorFunc: (T) -> Flow.Publisher<out Color>,
            borderFunc: (T) -> Flow.Publisher<out Color>,
            tiebreaker: Tiebreaker
        ): HemicycleFrameBuilder {
            return ofClustered(rows, entries, { 1 }, colorFunc, borderFunc, tiebreaker)
        }

        @Beta
        @JvmStatic fun <T> ofClustered(
            rows: List<Int>,
            entries: List<T>,
            seatsFunc: (T) -> Int,
            colorFunc: (T) -> Flow.Publisher<out Color>,
            tiebreaker: Tiebreaker
        ): HemicycleFrameBuilder {
            return ofClustered(rows, entries, seatsFunc, colorFunc, colorFunc, tiebreaker)
        }

        @Beta
        @JvmStatic fun <T> ofClustered(
            rows: List<Int>,
            entries: List<T>,
            seatsFunc: (T) -> Int,
            colorFunc: (T) -> Flow.Publisher<out Color>,
            borderFunc: (T) -> Flow.Publisher<out Color>,
            tiebreaker: Tiebreaker
        ): HemicycleFrameBuilder {
            val points = rows.indices
                .flatMap { row -> (0 until rows[row]).map { idx: Int -> Point(row, idx) } }
                .sortedWith(
                    Comparator.comparingDouble { p: Point -> 180.0 * p.y / (rows[p.x] - 1) }
                        .thenComparingInt { p: Point -> (if (tiebreaker == Tiebreaker.FRONT_ROW_FROM_LEFT) 1 else -1) * p.x }
                )
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
                            (
                                selectedPoints.isEmpty() ||
                                    selectedPoints
                                        .any { point: Point -> pointsAreBesideEachOther(point, it.value.first, rows) }
                                )
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
            builder.rowsPublisher = rows.asOneTimePublisher()
            val dots: List<T> = points
                .sortedWith(
                    Comparator.comparingInt { it: Pair<Point, T?> -> it.first.x }
                        .thenComparing { it -> it.first.y }
                )
                .map { it.second!! }
                .toList()
            builder.dotsPublisher =
                dots.map {
                    colorFunc(it).merge(borderFunc(it)) {
                        color, border ->
                        HemicycleFrame.Dot(color = color, border = border)
                    }
                }
                    .combine()
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
            resultFunc: (T) -> Flow.Publisher<out Result?>,
            prevResultFunc: (T) -> Party,
            leftParty: Party,
            rightParty: Party,
            leftLabel: (Int, Int) -> String,
            rightLabel: (Int, Int) -> String,
            otherLabel: (Int, Int) -> String,
            showChange: (Int, Int) -> Boolean,
            changeLabel: (Int, Int) -> String,
            tiebreaker: Tiebreaker,
            header: Flow.Publisher<out String?>
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
                header
            )
        }

        @Beta
        @JvmStatic fun <T> ofElectedLeading(
            rows: List<Int>,
            entries: List<T>,
            seatsFunc: (T) -> Int,
            resultFunc: (T) -> Flow.Publisher<out Result?>,
            prevResultFunc: (T) -> Party,
            leftParty: Party,
            rightParty: Party,
            leftLabel: (Int, Int) -> String,
            rightLabel: (Int, Int) -> String,
            otherLabel: (Int, Int) -> String,
            showChange: (Int, Int) -> Boolean,
            changeLabel: (Int, Int) -> String,
            tiebreaker: Tiebreaker,
            header: Flow.Publisher<out String?>
        ): HemicycleFrame {
            val results: Map<T, Flow.Publisher<out Result?>> = entries
                .distinct()
                .associateWith { resultFunc(it) }
            val prev = entries.distinct().associateWith(prevResultFunc)
            val resultPublishers = entries
                .map {
                    results[it]!!.map { x: Result? -> Pair(x, seatsFunc(it)) }
                }
                .toList()
            val resultWithPrevPublishers: List<Flow.Publisher<Triple<Result?, Party, Int>>> = entries
                .map {
                    results[it]!!.map { result: Result? -> Triple(result, prev[it]!!, seatsFunc(it)) }
                }
                .toList()
            val leftSeats = createSeatBarPublisher(resultPublishers) { it == leftParty }
            val leftList = leftSeats.map {
                listOf(
                    Pair(leftParty.color, it.first),
                    Pair(ColorUtils.lighten(leftParty.color), it.second - it.first)
                )
            }
            val rightSeats = createSeatBarPublisher(resultPublishers) { it == rightParty }
            val rightList = rightSeats.map {
                listOf(
                    Pair(rightParty.color, it.first),
                    Pair(ColorUtils.lighten(rightParty.color), it.second - it.first)
                )
            }
            val middleSeats = createSeatBarPublisher(
                resultPublishers
            ) { party: Party? -> party != null && party != leftParty && party != rightParty }
            val middleList = middleSeats.map {
                listOf(
                    Pair(Party.OTHERS.color, it.first),
                    Pair(ColorUtils.lighten(Party.OTHERS.color), it.second - it.first)
                )
            }
            val leftChange = createChangeBarPublisher(
                resultWithPrevPublishers
            ) { it == leftParty }
            val leftChangeList = leftChange.map {
                if (showChange(it.first, it.second)) {
                    listOf(
                        Pair(leftParty.color, it.first),
                        Pair(ColorUtils.lighten(leftParty.color), it.second - it.first)
                    )
                } else {
                    emptyList()
                }
            }
            val rightChange = createChangeBarPublisher(
                resultWithPrevPublishers
            ) { it == rightParty }
            val rightChangeList = rightChange.map {
                if (showChange(it.first, it.second)) {
                    listOf(
                        Pair(rightParty.color, it.first),
                        Pair(ColorUtils.lighten(rightParty.color), it.second - it.first)
                    )
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
                    results[it]!!.map { result: Result? ->
                        when {
                            result?.winner == null -> Color.WHITE
                            result.hasWon -> result.winner.color
                            else -> ColorUtils.lighten(result.winner.color)
                        }
                    }
                },
                { prevResultFunc(it).color.asOneTimePublisher() },
                tiebreaker
            )
                .withLeftSeatBars(
                    leftList, { it.first }, { it.second },
                    leftSeats.map { leftLabel(it.first, it.second) }
                )
                .withRightSeatBars(
                    rightList, { it.first }, { it.second },
                    rightSeats.map { rightLabel(it.first, it.second) }
                )
                .withMiddleSeatBars(
                    middleList, { it.first }, { it.second },
                    middleSeats.map { otherLabel(it.first, it.second) }
                )
                .withLeftChangeBars(
                    leftChangeList, { it.first }, { it.second },
                    calcPrevForParty(allPrevs, leftParty).asOneTimePublisher(),
                    leftChange.map(changeLabelFunc)
                )
                .withRightChangeBars(
                    rightChangeList, { it.first }, { it.second },
                    calcPrevForParty(allPrevs, rightParty).asOneTimePublisher(),
                    rightChange.map(changeLabelFunc)
                )
                .withHeader(header)
                .build()
        }

        private fun calcPrevForParty(prev: List<Pair<Party, Int>>, party: Party): Int {
            return prev.filter { party == it.first }.map { it.second }.sum()
        }

        private fun createSeatBarPublisher(
            results: List<Flow.Publisher<Pair<Result?, Int>>>,
            partyFilter: (Party?) -> Boolean
        ): Flow.Publisher<Pair<Int, Int>> {
            return results.mapReduce(
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
                }
            )
        }

        private fun createChangeBarPublisher(
            resultWithPrev: List<Flow.Publisher<Triple<Result?, Party, Int>>>,
            partyFilter: (Party?) -> Boolean
        ): Flow.Publisher<Pair<Int, Int>> {
            return resultWithPrev.mapReduce(
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
                }
            )
        }
    }
}
