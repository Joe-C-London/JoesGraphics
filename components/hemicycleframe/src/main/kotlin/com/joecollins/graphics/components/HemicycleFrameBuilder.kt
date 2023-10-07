package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.ResultColorUtils.getColor
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.mapReduce
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Point
import java.util.concurrent.Flow
import kotlin.math.abs

object HemicycleFrameBuilder {
    class Dots<T> {
        lateinit var rows: List<Int>
        lateinit var entries: List<T>
        var seatsFunc: T.() -> Int = { 1 }
        lateinit var colorFunc: T.() -> Flow.Publisher<out Color>
        var borderFunc: T.() -> Flow.Publisher<out Color> = { colorFunc() }
        lateinit var tiebreaker: Tiebreaker

        val dots by lazy {
            val points = rows.indices
                .flatMap { row -> (0 until rows[row]).map { idx -> Point(row, idx) } }
                .sortedWith(
                    Comparator.comparingDouble { p: Point -> 180.0 * p.y / (rows[p.x] - 1) }
                        .thenComparingInt { p: Point -> (if (tiebreaker == Tiebreaker.FRONT_ROW_FROM_LEFT) 1 else -1) * p.x },
                )
                .map { Pair(it, null as T?) }
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
                                        .any { point -> pointsAreBesideEachOther(point, it.value.first, rows) }
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
            val dots: List<T> = points
                .sortedWith(
                    Comparator.comparingInt { p: Pair<Point, T?> -> p.first.x }
                        .thenComparing { p -> p.first.y },
                )
                .map { it.second!! }
                .toList()
            dots.map {
                colorFunc(it).merge(borderFunc(it)) {
                        color, border ->
                    HemicycleFrame.Dot(color = color, border = border)
                }
            }
                .combine()
        }
    }

    class SeatBar<T> {
        lateinit var bars: Flow.Publisher<out List<T>>
        lateinit var colorFunc: T.() -> Color
        lateinit var seatFunc: T.() -> Int
        lateinit var labelPublisher: Flow.Publisher<out String>

        val barsPublisher by lazy {
            bars.mapElements { HemicycleFrame.Bar(color = it.colorFunc(), size = it.seatFunc()) }
        }
    }

    class ChangeBar<T> {
        lateinit var bars: Flow.Publisher<out List<T>>
        lateinit var colorFunc: T.() -> Color
        lateinit var seatFunc: T.() -> Int
        lateinit var startPublisher: Flow.Publisher<out Int>
        lateinit var labelPublisher: Flow.Publisher<out String>

        val barsPublisher by lazy {
            bars.mapElements { HemicycleFrame.Bar(color = colorFunc(it), size = seatFunc(it)) }
        }
    }

    enum class Tiebreaker {
        FRONT_ROW_FROM_LEFT, FRONT_ROW_FROM_RIGHT
    }

    fun <DOT, SB, CB> build(
        dots: Dots<DOT>.() -> Unit,
        leftSeats: (SeatBar<SB>.() -> Unit)? = null,
        rightSeats: (SeatBar<SB>.() -> Unit)? = null,
        middleSeats: (SeatBar<SB>.() -> Unit)? = null,
        leftChange: (ChangeBar<CB>.() -> Unit)? = null,
        rightChange: (ChangeBar<CB>.() -> Unit)? = null,
        header: Flow.Publisher<out String?>,
    ): HemicycleFrame {
        val dotsBuilder = Dots<DOT>().apply(dots)
        val leftSeatBar = leftSeats?.let { SeatBar<SB>().apply(it) }
        val rightSeatBar = rightSeats?.let { SeatBar<SB>().apply(it) }
        val middleSeatBar = middleSeats?.let { SeatBar<SB>().apply(it) }
        val leftChangeBar = leftChange?.let { ChangeBar<CB>().apply(it) }
        val rightChangeBar = rightChange?.let { ChangeBar<CB>().apply(it) }
        return HemicycleFrame(
            headerPublisher = header,
            rowsPublisher = dotsBuilder.rows.asOneTimePublisher(),
            dotsPublisher = dotsBuilder.dots,
            leftSeatBarPublisher = leftSeatBar?.barsPublisher,
            leftSeatBarLabelPublisher = leftSeatBar?.labelPublisher,
            rightSeatBarPublisher = rightSeatBar?.barsPublisher,
            rightSeatBarLabelPublisher = rightSeatBar?.labelPublisher,
            middleSeatBarPublisher = middleSeatBar?.barsPublisher,
            middleSeatBarLabelPublisher = middleSeatBar?.labelPublisher,
            leftChangeBarPublisher = leftChangeBar?.barsPublisher,
            leftChangeBarStartPublisher = leftChangeBar?.startPublisher,
            leftChangeBarLabelPublisher = leftChangeBar?.labelPublisher,
            rightChangeBarPublisher = rightChangeBar?.barsPublisher,
            rightChangeBarStartPublisher = rightChangeBar?.startPublisher,
            rightChangeBarLabelPublisher = rightChangeBar?.labelPublisher,
        )
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

    data class ElectedLeading(val elected: Int, val total: Int)

    fun <T> buildElectedLeading(
        rows: List<Int>,
        entries: List<T>,
        seatsFunc: T.() -> Int = { 1 },
        resultFunc: T.() -> Flow.Publisher<out PartyResult?>,
        prevResultFunc: T.() -> Party,
        leftParty: Party,
        rightParty: Party,
        leftLabel: ElectedLeading.() -> String,
        rightLabel: ElectedLeading.() -> String,
        otherLabel: ElectedLeading.() -> String,
        showChange: ElectedLeading.() -> Boolean,
        changeLabel: ElectedLeading.() -> String,
        tiebreaker: Tiebreaker,
        header: Flow.Publisher<out String?>,
    ): HemicycleFrame {
        if (entries.sumOf(seatsFunc) != rows.sum()) {
            throw IllegalArgumentException("Hemicycle Mismatch: ${entries.sumOf(seatsFunc)}/${rows.sum()}")
        }
        val results: Map<T, Flow.Publisher<out PartyResult?>> = entries
            .distinct()
            .associateWith { resultFunc(it) }
        val prev = entries.distinct().associateWith(prevResultFunc)
        val resultPublishers = entries
            .map { t ->
                results[t]!!.map { Pair(it, seatsFunc(t)) }
            }
            .toList()
        val resultWithPrevPublishers: List<Flow.Publisher<Triple<PartyResult?, Party, Int>>> = entries
            .map {
                results[it]!!.map { result -> Triple(result, prev[it]!!, seatsFunc(it)) }
            }
            .toList()
        val leftSeats = createSeatBarPublisher(resultPublishers) { it == leftParty }
        val leftList = leftSeats.map {
            listOf(
                Pair(leftParty.color, it.elected),
                Pair(ColorUtils.lighten(leftParty.color), it.total - it.elected),
            )
        }
        val rightSeats = createSeatBarPublisher(resultPublishers) { it == rightParty }
        val rightList = rightSeats.map {
            listOf(
                Pair(rightParty.color, it.elected),
                Pair(ColorUtils.lighten(rightParty.color), it.total - it.elected),
            )
        }
        val middleSeats = createSeatBarPublisher(
            resultPublishers,
        ) { party -> party != null && party != leftParty && party != rightParty }
        val middleList = middleSeats.map {
            listOf(
                Pair(Party.OTHERS.color, it.elected),
                Pair(ColorUtils.lighten(Party.OTHERS.color), it.total - it.elected),
            )
        }
        val leftChange = createChangeBarPublisher(
            resultWithPrevPublishers,
        ) { it == leftParty }
        val leftChangeList = leftChange.map {
            if (it.showChange()) {
                listOf(
                    Pair(leftParty.color, it.elected),
                    Pair(ColorUtils.lighten(leftParty.color), it.total - it.elected),
                )
            } else {
                emptyList()
            }
        }
        val rightChange = createChangeBarPublisher(
            resultWithPrevPublishers,
        ) { it == rightParty }
        val rightChangeList = rightChange.map {
            if (it.showChange()) {
                listOf(
                    Pair(rightParty.color, it.elected),
                    Pair(ColorUtils.lighten(rightParty.color), it.total - it.elected),
                )
            } else {
                emptyList()
            }
        }
        val changeLabelFunc = { p: ElectedLeading -> if (p.showChange()) p.changeLabel() else "" }
        val allPrevs: List<Pair<Party, Int>> = entries
            .map { Pair(prev[it]!!, seatsFunc(it)) }
            .toList()
        return build<T, Pair<Color, Int>, Pair<Color, Int>>(
            dots = {
                this.rows = rows
                this.entries = entries
                this.seatsFunc = seatsFunc
                colorFunc = {
                    results[this]!!.map { result ->
                        result?.getColor(default = Color.LIGHT_GRAY) ?: Color.WHITE
                    }
                }
                borderFunc = { prevResultFunc().color.asOneTimePublisher() }
                this.tiebreaker = tiebreaker
            },
            leftSeats = {
                bars = leftList
                colorFunc = { first }
                seatFunc = { second }
                labelPublisher = leftSeats.map { it.leftLabel() }
            },
            rightSeats = {
                bars = rightList
                colorFunc = { first }
                seatFunc = { second }
                labelPublisher = rightSeats.map { it.rightLabel() }
            },
            middleSeats = {
                bars = middleList
                colorFunc = { first }
                seatFunc = { second }
                labelPublisher = middleSeats.map { it.otherLabel() }
            },
            leftChange = {
                bars = leftChangeList
                colorFunc = { first }
                seatFunc = { second }
                startPublisher = calcPrevForParty(allPrevs, leftParty).asOneTimePublisher()
                labelPublisher = leftChange.map(changeLabelFunc)
            },
            rightChange = {
                bars = rightChangeList
                colorFunc = { first }
                seatFunc = { second }
                startPublisher = calcPrevForParty(allPrevs, rightParty).asOneTimePublisher()
                labelPublisher = rightChange.map(changeLabelFunc)
            },
            header = header,
        )
    }

    private fun calcPrevForParty(prev: List<Pair<Party, Int>>, party: Party): Int {
        return prev.filter { party == it.first }.sumOf { it.second }
    }

    private fun createSeatBarPublisher(
        results: List<Flow.Publisher<Pair<PartyResult?, Int>>>,
        partyFilter: (Party?) -> Boolean,
    ): Flow.Publisher<ElectedLeading> {
        return results.mapReduce(
            ElectedLeading(0, 0),
            { p, r ->
                val result = r.first
                if (result == null || !partyFilter(result.party)) {
                    p
                } else {
                    ElectedLeading(p.elected + if (result.elected) r.second else 0, p.total + r.second)
                }
            },
            { p, r ->
                val result = r.first
                if (result == null || !partyFilter(result.party)) {
                    p
                } else {
                    ElectedLeading(p.elected - if (result.elected) r.second else 0, p.total - r.second)
                }
            },
        )
    }

    private fun createChangeBarPublisher(
        resultWithPrev: List<Flow.Publisher<Triple<PartyResult?, Party, Int>>>,
        partyFilter: (Party?) -> Boolean,
    ): Flow.Publisher<ElectedLeading> {
        return resultWithPrev.mapReduce(
            ElectedLeading(0, 0),
            { p, r ->
                var ret = p
                val result = r.first
                if (result == null) {
                    ret
                } else {
                    if (partyFilter(result.party)) {
                        ret = ElectedLeading(ret.elected + if (result.elected) r.third else 0, ret.total + r.third)
                    }
                    if (partyFilter(r.second)) {
                        ret = ElectedLeading(ret.elected - if (result.elected) r.third else 0, ret.total - r.third)
                    }
                    ret
                }
            },
            { p, r ->
                var ret = p
                val result = r.first
                if (result == null) {
                    ret
                } else {
                    if (partyFilter(result.party)) {
                        ret = ElectedLeading(ret.elected - if (result.elected) r.third else 0, ret.total - r.third)
                    }
                    if (partyFilter(r.second)) {
                        ret = ElectedLeading(ret.elected + if (result.elected) r.third else 0, ret.total + r.third)
                    }
                    ret
                }
            },
        )
    }
}
