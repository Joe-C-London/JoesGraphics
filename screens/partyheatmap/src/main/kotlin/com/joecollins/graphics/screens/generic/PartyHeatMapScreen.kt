package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.HeatMapFrameBuilder
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel

class PartyHeatMapScreen private constructor(panel: JPanel, title: Flow.Publisher<String>) : GenericPanel(panel, title) {

    companion object {
        fun <T> ofElected(
            items: Flow.Publisher<out Collection<T>>,
            parties: Flow.Publisher<out List<Party>>,
            prevResult: (T) -> Party,
            currResult: (T) -> Flow.Publisher<Party?>,
            sortOrder: (T, Party) -> Number
        ): Builder<T> {
            return Builder(
                items,
                parties,
                prevResult,
                { t ->
                    currResult(t).map { p -> p?.let { PartyResult.elected(it) } }
                },
                sortOrder,
                false
            )
        }

        fun <T> ofElectedLeading(
            items: Flow.Publisher<out Collection<T>>,
            parties: Flow.Publisher<out List<Party>>,
            prevResult: (T) -> Party,
            currResult: (T) -> Flow.Publisher<out PartyResult?>,
            sortOrder: (T, Party) -> Number
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
        private val withLeading: Boolean
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
                            if (withLeading)
                                "${changeLabel(e)}/${changeLabel(l)}"
                            else
                                changeLabel(e)
                        },
                        header = party.name.uppercase().asOneTimePublisher(),
                        labelFunc = { it.toString().asOneTimePublisher() },
                        filterFunc = filter,
                        partyChanges = partyChanges
                    )
                }
            }.subscribe(
                Subscriber(
                    eventQueueWrapper { panels ->
                        panel.removeAll()
                        panels.forEach { panel.add(it) }
                    }
                )
            )

            return PartyHeatMapScreen(pad(panel), title)
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