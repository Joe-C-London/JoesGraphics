package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.HeatMapFrameBuilder
import com.joecollins.models.general.HitMissBalance
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel

class PartyHeatMapScreen private constructor(panel: JPanel, title: Flow.Publisher<String>, altText: Flow.Publisher<String>) : GenericPanel(panel, title, altText) {

    companion object {
        fun <T> sortByPrevResult(prevResult: (T) -> Map<Party, Int>): (Party, T) -> Flow.Publisher<Double> = { party, riding ->
            val result = prevResult(riding)
            val me = result[party] ?: 0
            val oth = result.filter { it.key != party }.maxOf { it.value }
            val total = result.values.sum().toDouble()
            ((oth - me) / total).asOneTimePublisher()
        }

        fun <T, C : Comparable<C>> ofElected(
            items: Flow.Publisher<out Collection<T>>,
            parties: Flow.Publisher<out List<Party>>,
            prevResult: T.() -> Party,
            currResult: T.() -> Flow.Publisher<Party?>,
            sortOrder: (Party, T) -> Flow.Publisher<C>,
            numRows: Flow.Publisher<Int>,
            filter: (Flow.Publisher<T.() -> Boolean>) = { _: T -> true }.asOneTimePublisher(),
            partyChanges: Flow.Publisher<Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher(),
            title: Flow.Publisher<String>,
        ): PartyHeatMapScreen = build(
            items,
            parties,
            prevResult,
            { currResult().map { PartyResult.elected(it) } },
            sortOrder,
            false,
            numRows,
            filter,
            partyChanges,
            title,
        )

        fun <T, C : Comparable<C>> ofElectedLeading(
            items: Flow.Publisher<out Collection<T>>,
            parties: Flow.Publisher<out List<Party>>,
            prevResult: T.() -> Party,
            currResult: T.() -> Flow.Publisher<out PartyResult?>,
            sortOrder: (Party, T) -> Flow.Publisher<C>,
            numRows: Flow.Publisher<Int>,
            filter: (Flow.Publisher<T.() -> Boolean>) = { _: T -> true }.asOneTimePublisher(),
            partyChanges: Flow.Publisher<Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher(),
            title: Flow.Publisher<String>,
        ): PartyHeatMapScreen = build(
            items,
            parties,
            prevResult,
            currResult,
            sortOrder,
            true,
            numRows,
            filter,
            partyChanges,
            title,
        )

        private fun <T, C : Comparable<C>> build(
            itemsPublisher: Flow.Publisher<out Collection<T>>,
            partiesPublisher: Flow.Publisher<out List<Party>>,
            prevResult: T.() -> Party,
            currResult: T.() -> Flow.Publisher<out PartyResult?>,
            sortOrder: (Party, T) -> Flow.Publisher<C>,
            withLeading: Boolean,
            numRows: Flow.Publisher<Int>,
            filter: (Flow.Publisher<T.() -> Boolean>),
            partyChanges: Flow.Publisher<Map<Party, Party>>,
            title: Flow.Publisher<String>,
        ): PartyHeatMapScreen {
            val panel = JPanel()
            panel.background = Color.WHITE
            panel.layout = GridLayout(0, 1, 5, 5)

            val changeLabel: (Int) -> String = { if (it == 0) "\u00b10" else DecimalFormat("+0;-0").format(it) }
            itemsPublisher.merge(partiesPublisher) { items, parties ->
                parties.map { party ->
                    HeatMapFrameBuilder.buildElectedLeading(
                        rows = numRows,
                        entries = createOrderedList(items, party, sortOrder),
                        result = currResult,
                        prevResult = prevResult,
                        party = party,
                        seatLabel = { if (withLeading) "$elected/$total" else elected.toString() },
                        showChange = { true },
                        changeLabel = {
                            if (withLeading) {
                                "${changeLabel(elected)}/${changeLabel(total)}"
                            } else {
                                changeLabel(elected)
                            }
                        },
                        header = party.name.uppercase().asOneTimePublisher(),
                        label = { toString().asOneTimePublisher() },
                        filter = filter,
                        partyChanges = partyChanges,
                    )
                }
            }.subscribe(
                Subscriber(
                    eventQueueWrapper { panels ->
                        panel.removeAll()
                        panels.forEach { panel.add(it) }
                    },
                ),
            )

            val altText = run {
                val diffFormat: (Int) -> String = { if (it == 0) "±0" else DecimalFormat("+0;-0").format(it) }
                val seatsText: (Map<Boolean, Int>) -> String = { curr ->
                    if (withLeading) {
                        "${curr[true] ?: 0}/${curr.values.sum()}"
                    } else {
                        "${curr[true] ?: 0}"
                    }
                }
                val diffText: (Map<Boolean, Int>, Map<Boolean, Int>) -> String = { curr, prev ->
                    if (withLeading) {
                        "${diffFormat((curr[true] ?: 0) - (prev[true] ?: 0))}/${diffFormat(curr.values.sum() - prev.values.sum())}"
                    } else {
                        diffFormat((curr[true] ?: 0) - (prev[true] ?: 0))
                    }
                }
                val partyTexts = itemsPublisher.merge(partiesPublisher.merge(partyChanges) { p, c -> p to c }) { items, (parties, changes) ->
                    val numForMajority = items.size / 2 + 1
                    parties.map { party ->
                        val prevTotal = items.map(prevResult).count { (changes[it] ?: it) == party }
                        val entries = createOrderedList(items, party, sortOrder)
                        val seatsAndPrev = entries.mapElements { e ->
                            e.currResult().map { r ->
                                if (r == null) {
                                    null
                                } else {
                                    r to e.prevResult()
                                }
                            }
                        }.compose { it.combine() }.map { list -> list.filterNotNull() }.map { list ->
                            val curr = list.filter { it.first.leader == party }.groupingBy { it.first.elected }.eachCount()
                            val prev = list.filter { it.second.let { p -> changes[p] ?: p } == party }.groupingBy { it.first.elected }.eachCount()
                            curr to prev
                        }
                        val balance = entries.mapElements(currResult).compose { it.combine() }.map { list ->
                            HitMissBalance.calculateBalance(list.map { it?.leader }) {
                                if (it == null) null else (it == party)
                            }
                        }
                        seatsAndPrev.merge(balance) { (seats, prev), bal ->
                            """
                                ${party.name.uppercase()}
                                ${seatsText(seats)} ($numForMajority FOR MAJORITY)
                                ${diffText(seats, prev)} (${diffFormat(numForMajority - prevTotal)} FOR MAJORITY)
                                BALANCE: ${bal.first}-${bal.last}
                            """.trimIndent()
                        }
                    }.combine().map { it.joinToString("\n\n") }
                }.compose { it }
                title.merge(partyTexts) { t, p -> "$t\n\n$p" }
            }

            return PartyHeatMapScreen(pad(panel), title, altText)
        }

        private fun <T, C : Comparable<C>> createOrderedList(items: Collection<T>, party: Party, sortOrder: (Party, T) -> Flow.Publisher<C>): Flow.Publisher<List<T>> = items
            .map { e -> sortOrder(party, e).map { so -> e to so } }
            .combine()
            .map { list -> list.sortedBy { it.second }.map { it.first } }
    }
}
