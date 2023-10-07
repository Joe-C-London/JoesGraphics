package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.mapReduce
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

object HeatMapFrameBuilder {
    class Squares<T> internal constructor() {
        lateinit var numRows: Flow.Publisher<out Int>
        lateinit var entries: List<T>
        var seats: T.() -> Int = { 1 }
        lateinit var fill: T.() -> Flow.Publisher<out Color>
        var border: T.() -> Flow.Publisher<out Color> = { fill() }
        var label: T.() -> Flow.Publisher<out String?> = { null.asOneTimePublisher() }

        val numRowsPublisher: Flow.Publisher<out Int> by lazy {
            numRows
        }
        val squaresPublisher: Flow.Publisher<out List<HeatMapFrame.Square>> by lazy {
            entries
                .flatMap { generateSequence { it }.take(it.seats()) }
                .map {
                    it.fill().merge(it.border()) { fill, border -> fill to border }
                        .merge(it.label()) { (fill, border), label ->
                            HeatMapFrame.Square(fillColor = fill, borderColor = border, label = label)
                        }
                }
                .combine()
        }
    }

    class SeatBars<T> internal constructor() {
        lateinit var bars: Flow.Publisher<out List<T>>
        lateinit var colorFunc: (T) -> Color
        lateinit var seatFunc: (T) -> Int
        lateinit var labelPublisher: Flow.Publisher<out String>

        val barsPublisher by lazy {
            bars.mapElements { HeatMapFrame.Bar(colorFunc(it), seatFunc(it)) }
        }
    }

    class ChangeBars<T> internal constructor() {
        lateinit var bars: Flow.Publisher<out List<T>>
        lateinit var colorFunc: (T) -> Color
        lateinit var seatFunc: (T) -> Int
        lateinit var startPublisher: Flow.Publisher<out Int>
        lateinit var labelPublisher: Flow.Publisher<out String>

        val barsPublisher by lazy {
            bars.mapElements { HeatMapFrame.Bar(colorFunc(it), seatFunc(it)) }
        }
    }

    fun <SQ, SB, CB> build(
        squares: Squares<SQ>.() -> Unit,
        seatBars: (SeatBars<SB>.() -> Unit)? = null,
        changeBars: (ChangeBars<CB>.() -> Unit)? = null,
        header: Flow.Publisher<out String?>? = null,
        borderColor: Flow.Publisher<out Color>? = null,
    ): HeatMapFrame {
        val squaresBuilder = Squares<SQ>().apply(squares)
        val seatBarsBuilder = seatBars?.let { SeatBars<SB>().apply(it) }
        val changeBarsBuilder = changeBars?.let { ChangeBars<CB>().apply(it) }
        return HeatMapFrame(
            headerPublisher = header ?: (null as String?).asOneTimePublisher(),
            borderColorPublisher = borderColor,
            numRowsPublisher = squaresBuilder.numRowsPublisher,
            squaresPublisher = squaresBuilder.squaresPublisher,
            seatBarsPublisher = seatBarsBuilder?.barsPublisher,
            seatBarLabelPublisher = seatBarsBuilder?.labelPublisher,
            changeBarsPublisher = changeBarsBuilder?.barsPublisher,
            changeBarStartPublisher = changeBarsBuilder?.startPublisher,
            changeBarLabelPublisher = changeBarsBuilder?.labelPublisher,
        )
    }

    data class ElectedLeading(val elected: Int, val total: Int)

    fun <T> buildElectedLeading(
        rows: Flow.Publisher<out Int>,
        entries: List<T>,
        result: T.() -> Flow.Publisher<out PartyResult?>,
        prevResult: T.() -> Party,
        party: Party,
        seatLabel: ElectedLeading.() -> String,
        showChange: ElectedLeading.() -> Boolean,
        changeLabel: ElectedLeading.() -> String,
        header: Flow.Publisher<out String?>,
        label: T.() -> Flow.Publisher<out String?> = { (null as String?).asOneTimePublisher() },
        seats: T.() -> Int = { 1 },
        filter: Flow.Publisher<T.() -> Boolean> = { _: T -> true }.asOneTimePublisher(),
        partyChanges: Flow.Publisher<Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher(),
    ): HeatMapFrame {
        val results: Map<T, Flow.Publisher<out PartyResult?>> = entries
            .distinct()
            .associateWith { it.result() }
        val prev = entries.distinct().associateWith(prevResult)
        val resultPublishers = entries
            .map { t ->
                results[t]!!.map { Pair(it, t.seats()) }
            }
            .toList()
        val resultWithPrevPublishers: List<Flow.Publisher<Triple<PartyResult?, Party, Int>>> = entries
            .map { t ->
                results[t]!!.merge(partyChanges) { res, changes -> Triple(res, prev[t]!!.let { changes[it] ?: it }, t.seats()) }
            }
            .toList()
        val seatsPublisher = createSeatBarPublisher(resultPublishers) { party == it }
        val seatList = seatsPublisher.map {
            listOf(
                Pair(party.color, it.elected),
                Pair(ColorUtils.lighten(party.color), it.total - it.elected),
            )
        }

        val change = createChangeBarPublisher(resultWithPrevPublishers) { party == it }
        val changeLabelFunc = { p: ElectedLeading -> if (p.showChange()) p.changeLabel() else "" }
        val changeList = change
            .map {
                if (it.showChange()) {
                    listOf(
                        Pair(party.color, it.elected),
                        Pair(ColorUtils.lighten(party.color), it.total - it.elected),
                    )
                } else {
                    emptyList()
                }
            }
        val allPrevs = entries
            .map { Pair(prev[it]!!, it.seats()) }
            .toList()
        return build<T, Pair<Color, Int>, Pair<Color, Int>>(
            squares = {
                numRows = rows
                this.entries = entries
                this.seats = seats
                fill = {
                    results[this]!!.merge(filter) { result, filter ->
                        when {
                            !filter() -> Color.WHITE
                            result?.party == null -> Color.WHITE
                            result.isElected -> result.party.color
                            else -> ColorUtils.lighten(result.party.color)
                        }
                    }
                }
                border = { filter.map { filter -> if (filter()) prevResult().color else Color.WHITE } }
                this.label = label
            },
            seatBars = {
                bars = seatList
                colorFunc = { it.first }
                seatFunc = { it.second }
                labelPublisher = seatsPublisher.map { it.seatLabel() }
            },
            changeBars = {
                bars = changeList
                colorFunc = { it.first }
                seatFunc = { it.second }
                startPublisher = partyChanges.map { calcPrevForParty(allPrevs, party, it) }
                labelPublisher = change.map(changeLabelFunc)
            },
            header = header,
            borderColor = party.color.asOneTimePublisher(),
        )
    }

    private fun calcPrevForParty(prev: List<Pair<Party, Int>>, party: Party, changes: Map<Party, Party>): Int {
        return prev.filter { party == it.first || party == changes[it.first] }.sumOf { it.second }
    }

    private fun createSeatBarPublisher(
        results: List<Flow.Publisher<Pair<PartyResult?, Int>>>,
        partyFilter: (Party?) -> Boolean,
    ): Flow.Publisher<ElectedLeading> {
        return results.mapReduce(
            ElectedLeading(0, 0),
            { p, r ->
                val left = r.first
                if (left == null || !partyFilter(left.party)) {
                    p
                } else {
                    ElectedLeading(p.elected + if (left.isElected) r.second else 0, p.total + r.second)
                }
            },
            { p, r ->
                val left = r.first
                if (left == null || !partyFilter(left.party)) {
                    p
                } else {
                    ElectedLeading(p.elected - if (left.isElected) r.second else 0, p.total - r.second)
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
                val left = r.first
                if (left?.party == null) {
                    p
                } else {
                    var ret = p
                    if (partyFilter(left.party)) {
                        ret = ElectedLeading(
                            ret.elected + if (left.isElected) r.third else 0,
                            ret.total + r.third,
                        )
                    }
                    if (partyFilter(r.second)) {
                        ret = ElectedLeading(
                            ret.elected - if (left.isElected) r.third else 0,
                            ret.total - r.third,
                        )
                    }
                    ret
                }
            },
            { p, r ->
                val left = r.first
                if (left?.party == null) {
                    p
                } else {
                    var ret = p
                    if (partyFilter(left.party)) {
                        ret = ElectedLeading(
                            ret.elected - if (left.isElected) r.third else 0,
                            ret.total - r.third,
                        )
                    }
                    if (partyFilter(r.second)) {
                        ret = ElectedLeading(
                            ret.elected + if (left.isElected) r.third else 0,
                            ret.total + r.third,
                        )
                    }
                    ret
                }
            },
        )
    }
}
