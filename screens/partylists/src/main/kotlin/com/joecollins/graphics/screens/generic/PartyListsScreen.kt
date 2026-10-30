package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
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
        ): PartyListsScreen = create(lists, showOrder, numSeats, skipCandidates, numRows, title, PartyOrCandidate::name, PartyOrCandidate::color)

        @JvmName("ofParty")
        fun of(
            lists: Flow.Publisher<Map<Party, List<Candidate>>>,
            showOrder: Flow.Publisher<List<Party>>,
            numSeats: Flow.Publisher<Map<Party, Int>>,
            skipCandidates: Flow.Publisher<out Collection<Candidate>>? = null,
            numRows: Flow.Publisher<Int>? = null,
            title: Flow.Publisher<String>,
        ): PartyListsScreen = create(lists, showOrder, numSeats, skipCandidates, numRows, title, Party::name, Party::color)

        private fun <P> create(
            lists: Flow.Publisher<Map<P, List<Candidate>>>,
            showOrder: Flow.Publisher<List<P>>,
            numSeats: Flow.Publisher<Map<P, Int>>,
            skipCandidates: Flow.Publisher<out Collection<Candidate>>?,
            numRows: Flow.Publisher<Int>?,
            title: Flow.Publisher<String>,
            name: P.() -> String,
            color: P.() -> Color,
        ): PartyListsScreen {
            val inputs = Inputs(name)
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
                                        headerPublisher = result[index].party.map { name(it).uppercase() },
                                        numRowsPublisher = numRows ?: 15.asOneTimePublisher(),
                                        itemsPublisher = result[index].items,
                                        borderColorPublisher = result[index].party.map { color(it) },
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

    private class Inputs<P>(private val name: P.() -> String) {
        private var lists: Map<P, List<Candidate>> = emptyMap()
        private var showOrder: List<P> = emptyList()
        private var numSeats: Map<P, Int> = emptyMap()
        private var skipCandidates: Collection<Candidate> = emptyList()

        fun setLists(lists: Map<P, List<Candidate>>) {
            synchronized(this) {
                this.lists = lists
                publishResult()
            }
        }

        fun setShowOrder(showOrder: List<P>) {
            synchronized(this) {
                this.showOrder = showOrder
                publishResult()
            }
        }

        fun setNumSeats(numSeats: Map<P, Int>) {
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

        class FrameInput<P> {
            val party = Publisher<P>()
            val items = Publisher<List<ResultListingFrame.Item>>()
        }

        private val frameInputs = ArrayList<FrameInput<P>>()
        val result = Publisher<List<FrameInput<P>>>(emptyList())
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
                        "${name(poc).uppercase()}: $seats OF ${names.size} ELECTED"
                    } else {
                        "${name(poc).uppercase()}: ${names.size} NAME${if (names.size == 1) "" else "S"}"
                    } + if (skipped == 0) "" else ", $skipped SKIPPED"
                },
            )
        }
    }
}
