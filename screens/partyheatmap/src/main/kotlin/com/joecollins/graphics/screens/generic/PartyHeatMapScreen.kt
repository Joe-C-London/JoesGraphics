package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.HeatMapFrameBuilder
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel

class PartyHeatMapScreen private constructor(panel: JPanel, title: Flow.Publisher<String>, altText: Flow.Publisher<String>) : GenericPanel(panel, title, altText) {

    companion object {
        fun <T> ofElected(
            items: Flow.Publisher<out Collection<T>>,
            parties: Flow.Publisher<out List<Party>>,
            prevResult: (T) -> Party,
            currResult: (T) -> Flow.Publisher<Party?>,
            sortOrder: (T, Party) -> Number,
        ): Builder<T> {
            return Builder(
                items,
                parties,
                prevResult,
                { t ->
                    currResult(t).map { p -> p?.let { PartyResult.elected(it) } }
                },
                sortOrder,
                false,
            )
        }

        fun <T> ofElectedLeading(
            items: Flow.Publisher<out Collection<T>>,
            parties: Flow.Publisher<out List<Party>>,
            prevResult: (T) -> Party,
            currResult: (T) -> Flow.Publisher<out PartyResult?>,
            sortOrder: (T, Party) -> Number,
        ): Builder<T> {
            return Builder(items, parties, prevResult, currResult, sortOrder, true)
        }
    }

    class Builder<T> internal constructor(
        private val items: Flow.Publisher<out Collection<T>>,
        private val parties: Flow.Publisher<out List<Party>>,
        private val prevResult: (T) -> Party,
        private val currResult: (T) -> Flow.Publisher<out PartyResult?>,
        private val sortOrder: (T, Party) -> Number,
        private val withLeading: Boolean,
    ) {
        private var numRows = 5.asOneTimePublisher()
        private var filter: Flow.Publisher<(T) -> Boolean> = { _: T -> true }.asOneTimePublisher()
        private var partyChanges: Flow.Publisher<Map<Party, Party>> = emptyMap<Party, Party>().asOneTimePublisher()

        fun withNumRows(numRows: Flow.Publisher<Int>): Builder<T> {
            this.numRows = numRows
            return this
        }

        fun withFilter(filter: Flow.Publisher<(T) -> Boolean>): Builder<T> {
            this.filter = filter
            return this
        }

        fun withPartyChanges(changes: Flow.Publisher<Map<Party, Party>>): Builder<T> {
            this.partyChanges = changes
            return this
        }

        fun build(title: Flow.Publisher<String>): PartyHeatMapScreen {
            val panel = JPanel()
            panel.background = Color.WHITE
            panel.layout = GridLayout(0, 1, 5, 5)

            val changeLabel: (Int) -> String = { if (it == 0) "\u00b10" else DecimalFormat("+0;-0").format(it) }
            items.merge(parties) { items, parties ->
                parties.map { party ->
                    HeatMapFrameBuilder.ofElectedLeading(
                        rows = numRows,
                        entries = createOrderedList(items, party),
                        resultFunc = currResult,
                        prevResultFunc = prevResult,
                        party = party,
                        seatLabel = { e, l -> if (withLeading) "$e/$l" else e.toString() },
                        showChange = { _, _ -> true },
                        changeLabel = { e, l ->
                            if (withLeading) {
                                "${changeLabel(e)}/${changeLabel(l)}"
                            } else {
                                changeLabel(e)
                            }
                        },
                        header = party.name.uppercase().asOneTimePublisher(),
                        labelFunc = { it.toString().asOneTimePublisher() },
                        filterFunc = filter,
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
                val partyTexts = items.merge(parties.merge(partyChanges) { p, c -> p to c }) { items, (parties, changes) ->
                    val numForMajority = items.size / 2 + 1
                    parties.map { party ->
                        val prevTotal = items.map(prevResult).count { (changes[it] ?: it) == party }
                        val entries = createOrderedList(items, party)
                        val seatsAndPrev = entries.map { e ->
                            currResult(e).map { r ->
                                if (r == null) {
                                    null
                                } else {
                                    r to prevResult(e)
                                }
                            }
                        }.combine().map { list -> list.filterNotNull() }.map { list ->
                            val curr = list.filter { it.first.party == party }.groupingBy { it.first.isElected }.eachCount()
                            val prev = list.filter { it.second.let { p -> changes[p] ?: p } == party }.groupingBy { it.first.isElected }.eachCount()
                            curr to prev
                        }
                        val balance = entries.map(currResult).combine().map { list ->
                            val indexedEntries = list.mapIndexed { index, partyResult -> index to partyResult?.party }
                            val hits = indexedEntries.asSequence()
                                .filter { it.second == party }
                                .map { it.first + 1 }
                                .let { sequenceOf(it.sortedDescending(), generateSequence { 0 }).flatten() }
                            val misses = indexedEntries.asSequence()
                                .filter { it.second != party && it.second != null }
                                .map { it.first + 1 }
                                .let { sequenceOf(it.sorted(), generateSequence { entries.size + 1 }).flatten() }
                            hits.zip(misses).first { (hit, miss) -> hit < miss }.let { (it.first + it.second) / 2 }
                        }
                        seatsAndPrev.merge(balance) { (seats, prev), bal ->
                            """
                                ${party.name.uppercase()}
                                ${seatsText(seats)} ($numForMajority FOR MAJORITY)
                                ${diffText(seats, prev)} (${diffFormat(numForMajority - prevTotal)} FOR MAJORITY)
                                BALANCE: $bal
                            """.trimIndent()
                        }
                    }.combine().map { it.joinToString("\n\n") }
                }.compose { it }
                title.merge(partyTexts) { t, p -> "$t\n\n$p" }
            }

            return PartyHeatMapScreen(pad(panel), title, altText)
        }

        private fun createOrderedList(items: Collection<T>, party: Party): List<T> {
            return items.asSequence()
                .map { it to sortOrder(it, party) }
                .sortedBy { it.second.toDouble() }
                .map { it.first }
                .toList()
        }
    }
}
