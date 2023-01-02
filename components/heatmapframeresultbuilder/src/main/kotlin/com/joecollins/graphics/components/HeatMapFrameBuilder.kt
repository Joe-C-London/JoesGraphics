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

class HeatMapFrameBuilder {
    private var seatBarsPublisher: Flow.Publisher<out List<HeatMapFrame.Bar>>? = null
    private var seatBarLabelPublisher: Flow.Publisher<out String>? = null
    private var changeBarsPublisher: Flow.Publisher<out List<HeatMapFrame.Bar>>? = null
    private var changeBarStartPublisher: Flow.Publisher<out Int>? = null
    private var changeBarLabelPublisher: Flow.Publisher<out String>? = null
    private var headerPublisher: Flow.Publisher<out String?>? = null
    private var borderColorPublisher: Flow.Publisher<out Color>? = null
    private var numRowsPublisher: Flow.Publisher<out Int>? = null
    private var squaresPublisher: Flow.Publisher<out List<HeatMapFrame.Square>>? = null

    fun <T> withSeatBars(
        bars: Flow.Publisher<out List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        labelPublisher: Flow.Publisher<out String>,
    ): HeatMapFrameBuilder {
        this.seatBarsPublisher = bars.mapElements { HeatMapFrame.Bar(colorFunc(it), seatFunc(it)) }
        this.seatBarLabelPublisher = labelPublisher
        return this
    }

    fun <T> withChangeBars(
        bars: Flow.Publisher<out List<T>>,
        colorFunc: (T) -> Color,
        seatFunc: (T) -> Int,
        startPublisher: Flow.Publisher<out Int>,
        labelPublisher: Flow.Publisher<out String>,
    ): HeatMapFrameBuilder {
        this.changeBarsPublisher = bars.mapElements { HeatMapFrame.Bar(colorFunc(it), seatFunc(it)) }
        this.changeBarStartPublisher = startPublisher
        this.changeBarLabelPublisher = labelPublisher
        return this
    }

    fun withHeader(headerPublisher: Flow.Publisher<out String?>): HeatMapFrameBuilder {
        this.headerPublisher = headerPublisher
        return this
    }

    fun withBorder(colorPublisher: Flow.Publisher<out Color>): HeatMapFrameBuilder {
        this.borderColorPublisher = colorPublisher
        return this
    }

    fun build(): HeatMapFrame {
        return HeatMapFrame(
            headerPublisher = headerPublisher ?: (null as String?).asOneTimePublisher(),
            borderColorPublisher = borderColorPublisher,
            numRowsPublisher = numRowsPublisher ?: 1.asOneTimePublisher(),
            squaresPublisher = squaresPublisher ?: emptyList<HeatMapFrame.Square>().asOneTimePublisher(),
            seatBarsPublisher = seatBarsPublisher,
            seatBarLabelPublisher = seatBarLabelPublisher,
            changeBarsPublisher = changeBarsPublisher,
            changeBarStartPublisher = changeBarStartPublisher,
            changeBarLabelPublisher = changeBarLabelPublisher,
        )
    }

    companion object {
        fun <T> of(
            numRows: Flow.Publisher<out Int>,
            entries: List<T>,
            colorFunc: (T) -> Flow.Publisher<out Color>,
            labelFunc: (T) -> Flow.Publisher<out String?> = { (null as String?).asOneTimePublisher() },
        ): HeatMapFrameBuilder {
            return of(numRows, entries, colorFunc, colorFunc, labelFunc)
        }

        fun <T> of(
            numRows: Flow.Publisher<out Int>,
            entries: List<T>,
            fillFunc: (T) -> Flow.Publisher<out Color>,
            borderFunc: (T) -> Flow.Publisher<out Color>,
            labelFunc: (T) -> Flow.Publisher<out String?> = { (null as String?).asOneTimePublisher() },
        ): HeatMapFrameBuilder {
            val builder = HeatMapFrameBuilder()
            builder.numRowsPublisher = numRows
            builder.squaresPublisher =
                entries.map {
                    fillFunc(it).merge(borderFunc(it)) {
                            fill, border ->
                        fill to border
                    }.merge(labelFunc(it)) {
                            (fill, border), label ->
                        HeatMapFrame.Square(fillColor = fill, borderColor = border, label = label)
                    }
                }
                    .combine()
            return builder
        }

        fun <T> ofClustered(
            numRows: Flow.Publisher<out Int>,
            entries: List<T>,
            seatFunc: (T) -> Int,
            fillFunc: (T) -> Flow.Publisher<out Color>,
            borderFunc: (T) -> Flow.Publisher<out Color>,
            labelFunc: (T) -> Flow.Publisher<out String?> = { (null as String?).asOneTimePublisher() },
        ): HeatMapFrameBuilder {
            val allEntries = entries
                .flatMap { generateSequence { it }.take(seatFunc(it)) }
                .toList()
            return of(numRows, allEntries, fillFunc, borderFunc, labelFunc)
        }

        fun <T> ofElectedLeading(
            rows: Flow.Publisher<out Int>,
            entries: List<T>,
            resultFunc: (T) -> Flow.Publisher<out PartyResult?>,
            prevResultFunc: (T) -> Party,
            party: Party,
            seatLabel: (Int, Int) -> String,
            showChange: (Int, Int) -> Boolean,
            changeLabel: (Int, Int) -> String,
            header: Flow.Publisher<out String?>,
            labelFunc: (T) -> Flow.Publisher<out String?> = { (null as String?).asOneTimePublisher() },
            seatsFunc: (T) -> Int = { 1 },
            filterFunc: Flow.Publisher<(T) -> Boolean> = { _: T -> true }.asOneTimePublisher(),
            partyChanges: Flow.Publisher<Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher(),
        ): HeatMapFrame {
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
                .map { t ->
                    results[t]!!.merge(partyChanges) { res, changes -> Triple(res, prev[t]!!.let { changes[it] ?: it }, seatsFunc(t)) }
                }
                .toList()
            val seats = createSeatBarPublisher(resultPublishers) { party == it }
            val seatList = seats.map {
                listOf(
                    Pair(party.color, it.first),
                    Pair(ColorUtils.lighten(party.color), it.second - it.first),
                )
            }

            val change = createChangeBarPublisher(resultWithPrevPublishers) { party == it }
            val changeLabelFunc = { p: Pair<Int, Int> -> if (showChange(p.first, p.second)) changeLabel(p.first, p.second) else "" }
            val changeList = change
                .map {
                    if (showChange(it.first, it.second)) {
                        listOf(
                            Pair(party.color, it.first),
                            Pair(ColorUtils.lighten(party.color), it.second - it.first),
                        )
                    } else {
                        emptyList()
                    }
                }
            val allPrevs = entries
                .map { Pair(prev[it]!!, seatsFunc(it)) }
                .toList()
            return ofClustered(
                rows,
                entries,
                seatsFunc,
                { t ->
                    results[t]!!.merge(filterFunc) { result, filter ->
                        when {
                            !filter(t) -> Color.WHITE
                            result?.party == null -> Color.WHITE
                            result.isElected -> result.party.color
                            else -> ColorUtils.lighten(result.party.color)
                        }
                    }
                },
                { t -> filterFunc.map { filter -> if (filter(t)) prevResultFunc(t).color else Color.WHITE } },
                labelFunc,
            )
                .withSeatBars(
                    seatList,
                    { it.first },
                    { it.second },
                    seats.map { seatLabel(it.first, it.second) },
                )
                .withChangeBars(
                    changeList,
                    { it.first },
                    { it.second },
                    partyChanges.map { calcPrevForParty(allPrevs, party, it) },
                    change.map(changeLabelFunc),
                )
                .withHeader(header)
                .withBorder(party.color.asOneTimePublisher())
                .build()
        }

        private fun calcPrevForParty(prev: List<Pair<Party, Int>>, party: Party, changes: Map<Party, Party>): Int {
            return prev.filter { party == it.first || party == changes[it.first] }.sumOf { it.second }
        }

        private fun createSeatBarPublisher(
            results: List<Flow.Publisher<Pair<PartyResult?, Int>>>,
            partyFilter: (Party?) -> Boolean,
        ): Flow.Publisher<Pair<Int, Int>> {
            return results.mapReduce(
                Pair(0, 0),
                { p, r ->
                    val left = r.first
                    if (left == null || !partyFilter(left.party)) {
                        p
                    } else {
                        Pair(p.first + if (left.isElected) r.second else 0, p.second + r.second)
                    }
                },
                { p, r ->
                    val left = r.first
                    if (left == null || !partyFilter(left.party)) {
                        p
                    } else {
                        Pair(p.first - if (left.isElected) r.second else 0, p.second - r.second)
                    }
                },
            )
        }

        private fun createChangeBarPublisher(
            resultWithPrev: List<Flow.Publisher<Triple<PartyResult?, Party, Int>>>,
            partyFilter: (Party?) -> Boolean,
        ): Flow.Publisher<Pair<Int, Int>> {
            return resultWithPrev.mapReduce(
                Pair(0, 0),
                { p, r ->
                    val left = r.first
                    if (left?.party == null) {
                        p
                    } else {
                        var ret = p
                        if (partyFilter(left.party)) {
                            ret = Pair(
                                ret.first + if (left.isElected) r.third else 0,
                                ret.second + r.third,
                            )
                        }
                        if (partyFilter(r.second)) {
                            ret = Pair(
                                ret.first - if (left.isElected) r.third else 0,
                                ret.second - r.third,
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
                            ret = Pair(
                                ret.first - if (left.isElected) r.third else 0,
                                ret.second - r.third,
                            )
                        }
                        if (partyFilter(r.second)) {
                            ret = Pair(
                                ret.first + if (left.isElected) r.third else 0,
                                ret.second + r.third,
                            )
                        }
                        ret
                    }
                },
            )
        }
    }
}
