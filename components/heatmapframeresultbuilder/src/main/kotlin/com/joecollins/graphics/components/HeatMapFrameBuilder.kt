package com.joecollins.graphics.components

import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

object HeatMapFrameBuilder {
    class Squares<T> internal constructor() {
        lateinit var numRows: Flow.Publisher<out Int>
        lateinit var entries: Flow.Publisher<List<T>>
        var seats: T.() -> Int = { 1 }
        lateinit var fill: T.() -> Flow.Publisher<out Color>
        var border: T.() -> Flow.Publisher<out Color> = { fill() }
        var label: T.() -> Flow.Publisher<out String?> = { null.asOneTimePublisher() }

        val numRowsPublisher: Flow.Publisher<out Int> by lazy {
            numRows
        }
        val squaresPublisher: Flow.Publisher<out List<HeatMapFrame.Square>> by lazy {
            entries.compose { e ->
                e.flatMap { generateSequence { it }.take(it.seats()) }
                    .map {
                        it.fill().merge(it.border()) { fill, border -> fill to border }
                            .merge(it.label()) { (fill, border), label ->
                                HeatMapFrame.Square(fillColor = fill, borderColor = border, label = label)
                            }
                    }
                    .combine()
            }
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

    fun <SQ> squares(squares: Squares<SQ>.() -> Unit) = Squares<SQ>().apply(squares)

    fun <SB> seatBars(seatBars: SeatBars<SB>.() -> Unit) = SeatBars<SB>().apply(seatBars)

    fun <CB> changeBars(changeBars: ChangeBars<CB>.() -> Unit) = ChangeBars<CB>().apply(changeBars)

    fun build(
        squares: Squares<*>,
        seatBars: SeatBars<*>? = null,
        changeBars: ChangeBars<*>? = null,
        header: Flow.Publisher<out String?>? = null,
        borderColor: Flow.Publisher<out Color>? = null,
    ): HeatMapFrame = HeatMapFrame(
        headerPublisher = header ?: (null as String?).asOneTimePublisher(),
        borderColorPublisher = borderColor,
        numRowsPublisher = squares.numRowsPublisher,
        squaresPublisher = squares.squaresPublisher,
        seatBarsPublisher = seatBars?.barsPublisher,
        seatBarLabelPublisher = seatBars?.labelPublisher,
        changeBarsPublisher = changeBars?.barsPublisher,
        changeBarStartPublisher = changeBars?.startPublisher,
        changeBarLabelPublisher = changeBars?.labelPublisher,
    )

    data class ElectedLeading(val elected: Int, val total: Int)

    fun <T> buildElectedLeading(
        rows: Flow.Publisher<out Int>,
        entries: Flow.Publisher<List<T>>,
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
        val results: Flow.Publisher<out Map<T, PartyResult?>> =
            entries.compose { e -> Aggregators.toMap(e) { it.result() } }
        val prev = entries.map { it.associateWith(prevResult) }
            .merge(partyChanges) { pr, pc -> pr.mapValues { (_, p) -> pc[p] ?: p } }
        val resultPublishers = entries
            .mapElements { t ->
                results.map { it[t] to t.seats() }
            }.compose { it.combine() }
        val resultWithPrevPublishers: Flow.Publisher<List<Triple<PartyResult?, Party, Int>>> =
            entries.mapElements { t ->
                results.merge(prev) { res, prev -> Triple(res[t], prev[t]!!, t.seats()) }
            }.compose { it.combine() }
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
        val allPrevs = entries.merge(prev) { list, p ->
            list.map { e -> Pair(p[e]!!, e.seats()) }
        }
        return build(
            squares = squares<T> {
                numRows = rows
                this.entries = entries
                this.seats = seats
                fill = {
                    results.merge(filter) { result, filter ->
                        val thisResult = result[this]
                        when {
                            !filter() -> Color.WHITE
                            thisResult?.leader == null -> Color.WHITE
                            thisResult.elected -> thisResult.leader.color
                            else -> ColorUtils.lighten(thisResult.leader.color)
                        }
                    }
                }
                border = { filter.map { filter -> if (filter()) prevResult().color else Color.WHITE } }
                this.label = label
            },
            seatBars = seatBars<Pair<Color, Int>> {
                bars = seatList
                colorFunc = { it.first }
                seatFunc = { it.second }
                labelPublisher = seatsPublisher.map { it.seatLabel() }
            },
            changeBars = changeBars<Pair<Color, Int>> {
                bars = changeList
                colorFunc = { it.first }
                seatFunc = { it.second }
                startPublisher = partyChanges.compose { calcPrevForParty(allPrevs, party, it) }
                labelPublisher = change.map(changeLabelFunc)
            },
            header = header,
            borderColor = party.color.asOneTimePublisher(),
        )
    }

    private fun calcPrevForParty(prev: Flow.Publisher<List<Pair<Party, Int>>>, party: Party, changes: Map<Party, Party>): Flow.Publisher<Int> = prev.map { p -> p.filter { party == it.first || party == changes[it.first] }.sumOf { it.second } }

    private fun createSeatBarPublisher(
        results: Flow.Publisher<List<Pair<PartyResult?, Int>>>,
        partyFilter: (Party?) -> Boolean,
    ): Flow.Publisher<ElectedLeading> = results
        .map { res ->
            res.asSequence().filter { (pr, _) -> pr != null && partyFilter(pr.leader) }
                .map { (pr, seats) ->
                    ElectedLeading(if (pr!!.elected) seats else 0, seats)
                }
                .fold(ElectedLeading(0, 0)) { a, e -> ElectedLeading(a.elected + e.elected, a.total + e.total) }
        }

    private fun createChangeBarPublisher(
        resultWithPrev: Flow.Publisher<List<Triple<PartyResult?, Party, Int>>>,
        partyFilter: (Party?) -> Boolean,
    ): Flow.Publisher<ElectedLeading> = resultWithPrev
        .map { res ->
            res.asSequence().filter { (pr, _) -> pr?.leader != null }
                .mapNotNull { (pr, p, seats) ->
                    val partyIsLeading = partyFilter(pr!!.leader)
                    val partyWonPreviously = partyFilter(p)
                    if (partyIsLeading == partyWonPreviously) {
                        null
                    } else if (partyIsLeading) {
                        ElectedLeading(if (pr.elected) seats else 0, seats)
                    } else {
                        ElectedLeading(if (pr.elected) -seats else 0, -seats)
                    }
                }
                .fold(ElectedLeading(0, 0)) { a, e -> ElectedLeading(a.elected + e.elected, a.total + e.total) }
        }
}
