package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.RegionSummaryFrame
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.ceil

class PartySummaryScreen private constructor(
    partyPublisher: Flow.Publisher<out PartyOrCoalition>,
    mainFrame: RegionSummaryFrame,
    otherFrames: List<RegionSummaryFrame>,
    numRows: Int
) : GenericPanel(
    run {
        val center = JPanel()
        center.background = Color.WHITE
        center.layout = Layout(numRows)
        center.add(mainFrame, "main")
        otherFrames.forEach { center.add(it, "other") }
        center
    },
    partyPublisher.map { it.name.uppercase() + " SUMMARY" }
) {

    init {
        partyPublisher.subscribe(Subscriber(eventQueueWrapper { super.label.foreground = it.color }))
    }

    private class Layout constructor(private val numRows: Int) : LayoutManager {
        private var main: Component = JPanel()
        private val others: MutableList<Component> = ArrayList()

        override fun addLayoutComponent(name: String, comp: Component) {
            if ("main" == name) main = comp else others.add(comp)
        }

        override fun removeLayoutComponent(comp: Component) {
            if (main === comp) main = JPanel()
            others.remove(comp)
        }

        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(1024, 512)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(128, 64)
        }

        override fun layoutContainer(parent: Container) {
            val numOtherCols = ceil(1.0 * others.size / numRows).toInt()
            val numTotalCols = numRows + numOtherCols
            val widthPerCol = 1.0 * parent.width / numTotalCols
            val heightPerRow = 1.0 * parent.height / numRows
            main.setLocation(5, 5)
            main.setSize((numRows * widthPerCol - 10).toInt(), (numRows * heightPerRow - 10).toInt())
            for (i in others.indices) {
                val other = others[i]
                val row = i / numOtherCols
                val col = i % numOtherCols + numRows
                other.setLocation((col * widthPerCol + 5).toInt(), (row * heightPerRow + 5).toInt())
                other.setSize((widthPerCol - 10).toInt(), (heightPerRow - 10).toInt())
            }
        }
    }

    class Builder<T>(
        private val mainRegion: T,
        private val titleFunc: (T) -> Flow.Publisher<out String>,
        private val numRows: Int
    ) {
        private var seatFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>)? = null
        private var seatDiffFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>)? = null
        private var seatsHeader = "SEATS"
        private var votePctFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>)? = null
        private var votePctDiffFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>)? = null
        private var voteHeader = "POPULAR VOTE"

        private val regions: MutableList<T> = ArrayList()

        fun withSeatAndDiff(
            seatFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>),
            seatDiffFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>),
            seatsHeader: String = "SEATS"
        ): Builder<T> {
            this.seatFunc = seatFunc
            this.seatDiffFunc = seatDiffFunc
            this.seatsHeader = seatsHeader
            return this
        }

        fun withSeatAndPrev(
            seatFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>),
            seatPrevFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>),
            seatsHeader: String = "SEATS"
        ): Builder<T> {
            val seatDiffFunc = { t: T ->
                val curr = seatFunc(t)
                val prev = seatPrevFunc(t)
                curr.merge(prev) { c, p ->
                    sequenceOf(c.keys.asSequence(), p.keys.asSequence()).flatten()
                        .distinct()
                        .associateWith { (c[it] ?: 0) - (p[it] ?: 0) }
                }
            }
            return withSeatAndDiff(seatFunc, seatDiffFunc, seatsHeader)
        }

        fun withVotePctAndDiff(
            votePctFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>),
            votePctDiffFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>),
            voteHeader: String = "POPULAR VOTE"
        ): Builder<T> {
            this.votePctFunc = votePctFunc
            this.votePctDiffFunc = votePctDiffFunc
            this.voteHeader = voteHeader
            return this
        }

        fun withVotePctAndPrev(
            votePctFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>),
            votePctPrevFunc: ((T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>),
            voteHeader: String = "POPULAR VOTE"
        ): Builder<T> {
            val votePctDiffFunc = { t: T ->
                val curr = votePctFunc(t)
                val prev = votePctPrevFunc(t)
                curr.merge(prev) { c, p ->
                    sequenceOf(c.keys.asSequence(), p.keys.asSequence()).flatten()
                        .distinct()
                        .associateWith { (c[it] ?: 0.0) - (p[it] ?: 0.0) }
                }
            }
            return withVotePctAndDiff(votePctFunc, votePctDiffFunc, voteHeader)
        }

        fun withRegion(region: T): Builder<T> {
            regions.add(region)
            return this
        }

        fun build(partyPublisher: Flow.Publisher<out PartyOrCoalition>): PartySummaryScreen {
            return PartySummaryScreen(
                partyPublisher,
                createFrame(mainRegion, partyPublisher),
                regions.map { createFrame(it, partyPublisher) },
                numRows
            )
        }

        private fun createFrame(region: T, party: Flow.Publisher<out PartyOrCoalition>): RegionSummaryFrame {
            val input = SinglePartyInput()
            seatFunc?.invoke(region)?.subscribe(Subscriber { input.seats = it })
            seatDiffFunc?.invoke(region)?.subscribe(Subscriber { input.seatDiff = it })
            votePctFunc?.invoke(region)?.subscribe(Subscriber { input.votePct = it })
            votePctDiffFunc?.invoke(region)?.subscribe(Subscriber { input.votePctDiff = it })
            party.subscribe(Subscriber { input.party = it })
            val seatPublisher = input.seatPublisher
            val votePublisher = input.votePublisher
            val (headers, values) = when {
                seatFunc == null -> listOf(voteHeader) to votePublisher.map { listOf(it) }
                votePctFunc == null -> listOf(seatsHeader) to seatPublisher.map { listOf(it) }
                else -> listOf(seatsHeader, voteHeader) to seatPublisher.merge(votePublisher) { s, v -> listOf(s, v) }
            }
            return RegionSummaryFrame(
                headerPublisher = titleFunc(region),
                summaryColorPublisher = party.map { it.color },
                sectionsPublisher = values.map { value ->
                    value.zip(headers) { v, h -> RegionSummaryFrame.SectionWithoutColor(h, v) }
                }
            )
        }
    }

    private class SinglePartyInput {
        var seats: Map<out PartyOrCoalition, Int> = emptyMap()
            set(value) {
                field = value
                updateSeats()
            }

        var seatDiff: Map<out PartyOrCoalition, Int> = emptyMap()
            set(value) {
                field = value
                updateSeats()
            }

        var votePct: Map<out PartyOrCoalition, Double> = emptyMap()
            set(value) {
                field = value
                updateVotes()
            }

        var votePctDiff: Map<out PartyOrCoalition, Double> = emptyMap()
            set(value) {
                field = value
                updateVotes()
            }

        var party: PartyOrCoalition? = null
            set(value) {
                field = value
                updateSeats()
                updateVotes()
            }

        val seatPublisher = Publisher(calculateSeats())
        private fun updateSeats() = synchronized(this) { seatPublisher.submit(calculateSeats()) }
        private fun calculateSeats(): List<String> {
            val seats = this.seats[this.party] ?: 0
            val diff = this.seatDiff[this.party] ?: 0
            return listOf(
                seats.toString(),
                if (diff == 0) "\u00b10" else DecimalFormat("+0;-0").format(diff)
            )
        }

        val votePublisher = Publisher(calculateVotes())
        private fun updateVotes() = synchronized(this) { votePublisher.submit(calculateVotes()) }
        private fun calculateVotes(): List<String> {
            val vote = this.votePct[this.party] ?: 0.0
            val diff = this.votePctDiff[this.party] ?: 0.0
            return listOf(
                DecimalFormat("0.0%").format(vote),
                if (diff == 0.0) "\u00b10.0%" else DecimalFormat("+0.0%;-0.0%").format(diff)
            )
        }
    }

    companion object {
        fun <T> of(
            mainRegion: T,
            titleFunc: (T) -> Flow.Publisher<out String>,
            numRows: Int
        ): Builder<T> {
            return Builder(mainRegion, titleFunc, numRows)
        }

        fun <T> ofDiff(
            mainRegion: T,
            titleFunc: (T) -> Flow.Publisher<out String>,
            seatFunc: (T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            seatDiffFunc: (T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            votePctFunc: (T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
            votePctDiffFunc: (T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
            numRows: Int
        ): Builder<T> {
            return Builder(mainRegion, titleFunc, numRows)
                .withSeatAndDiff(seatFunc, seatDiffFunc)
                .withVotePctAndDiff(votePctFunc, votePctDiffFunc)
        }

        fun <T> ofPrev(
            mainRegion: T,
            titleFunc: (T) -> Flow.Publisher<out String>,
            seatFunc: (T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            seatPrevFunc: (T) -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            votePctFunc: (T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
            votePctPrevFunc: (T) -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
            numRows: Int
        ): Builder<T> {
            return Builder(mainRegion, titleFunc, numRows)
                .withSeatAndPrev(seatFunc, seatPrevFunc)
                .withVotePctAndPrev(votePctFunc, votePctPrevFunc)
        }
    }
}
