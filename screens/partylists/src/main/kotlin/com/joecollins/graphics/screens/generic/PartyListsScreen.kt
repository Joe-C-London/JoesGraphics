package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.BorderFactory
import javax.swing.JPanel

class PartyListsScreen private constructor(title: Flow.Publisher<out String?>, frame: JPanel, altText: Flow.Publisher<(Int) -> String>) : GenericPanel(pad(frame), title, altText) {

    companion object {
        fun of(
            lists: Flow.Publisher<Map<PartyOrCandidate, List<Candidate>>>,
            showOrder: Flow.Publisher<List<PartyOrCandidate>>,
            numSeats: Flow.Publisher<Map<PartyOrCandidate, Int>>,
            skipCandidates: Flow.Publisher<out Collection<Candidate>>? = null,
            numRows: Flow.Publisher<Int>? = null,
            title: Flow.Publisher<String>,
        ): PartyListsScreen {
            val inputs = Inputs()
            lists.subscribe(Subscriber(inputs::setLists))
            showOrder.subscribe(Subscriber(inputs::setShowOrder))
            numSeats.subscribe(Subscriber(inputs::setNumSeats))
            skipCandidates?.subscribe(Subscriber(inputs::setSkipCandidates))

            val panel = JPanel().apply {
                background = Color.WHITE
                layout = GridLayout(1, 0, 5, 5)

                inputs.result.subscribe(
                    Subscriber(
                        eventQueueWrapper { result ->
                            while (components.size < result.size) {
                                val index = components.size
                                add(
                                    ResultListingFrame(
                                        headerPublisher = result[index].party.map { it.name.uppercase() },
                                        numRowsPublisher = numRows ?: 15.asOneTimePublisher(),
                                        itemsPublisher = result[index].items,
                                        borderColorPublisher = result[index].party.map { it.color },
                                        shrinkToFit = true,
                                    ),
                                )
                            }
                            while (components.size > result.size) {
                                remove(components.size - 1)
                            }
                        },
                    ),
                )
            }
            return PartyListsScreen(title, panel, inputs.altText.merge(title) { a, h -> { _: Int -> "$h\n\n$a" } })
        }
    }

    private class Inputs {
        private var lists: Map<PartyOrCandidate, List<Candidate>> = emptyMap()
        private var showOrder: List<PartyOrCandidate> = emptyList()
        private var numSeats: Map<PartyOrCandidate, Int> = emptyMap()
        private var skipCandidates: Collection<Candidate> = emptyList()

        fun setLists(lists: Map<PartyOrCandidate, List<Candidate>>) {
            synchronized(this) {
                this.lists = lists
                publishResult()
            }
        }

        fun setShowOrder(showOrder: List<PartyOrCandidate>) {
            synchronized(this) {
                this.showOrder = showOrder
                publishResult()
            }
        }

        fun setNumSeats(numSeats: Map<PartyOrCandidate, Int>) {
            synchronized(this) {
                this.numSeats = numSeats
                publishResult()
            }
        }

        fun setSkipCandidates(skipCandidates: Collection<Candidate>) {
            synchronized(this) {
                this.skipCandidates = skipCandidates
                publishResult()
            }
        }

        class FrameInput {
            val party = Publisher<PartyOrCandidate>()
            val items = Publisher<List<ResultListingFrame.Item>>()
        }

        private val frameInputs = ArrayList<FrameInput>()
        val result = Publisher<List<FrameInput>>(emptyList())
        val altText = Publisher("")
        private fun publishResult() {
            val anySeats = numSeats.values.sum() > 0
            while (frameInputs.size < showOrder.size) {
                frameInputs.add(FrameInput())
            }
            while (frameInputs.size > showOrder.size) {
                frameInputs.removeLast()
            }
            showOrder.forEachIndexed { index, poc ->
                val elected = (lists[poc] ?: emptyList())
                    .filterNot { skipCandidates.contains(it) }
                    .take(numSeats[poc] ?: 0)
                frameInputs[index].apply {
                    party.submit(poc)
                    items.submit(
                        (lists[poc] ?: emptyList()).map { c ->
                            ResultListingFrame.Item(
                                c.name.uppercase(),
                                if (elected.contains(c)) {
                                    ColorUtils.foregroundToContrast(c.party.color)
                                } else if (anySeats || skipCandidates.contains(c)) {
                                    ColorUtils.lighten(ColorUtils.lighten(Color.BLACK))
                                } else {
                                    Color.BLACK
                                },
                                if (elected.contains(c)) {
                                    c.party.color
                                } else {
                                    Color.WHITE
                                },
                                if ((anySeats && !elected.contains(c)) || skipCandidates.contains(c)) {
                                    ColorUtils.lighten(ColorUtils.lighten(c.party.color))
                                } else {
                                    c.party.color
                                },
                            )
                        },
                    )
                }
            }
            this.result.submit(frameInputs.toList())

            this.altText.submit(
                showOrder.joinToString("\n") { poc ->
                    val names = lists[poc] ?: emptyList()
                    val skipped = names.count { skipCandidates.contains(it) }
                    if (anySeats) {
                        val seats = numSeats[poc] ?: 0
                        "${poc.name.uppercase()}: $seats OF ${names.size} ELECTED"
                    } else {
                        "${poc.name.uppercase()}: ${names.size} NAME${if (names.size == 1) "" else "S"}"
                    } + if (skipped == 0) "" else ", $skipped SKIPPED"
                },
            )
        }
    }
}
