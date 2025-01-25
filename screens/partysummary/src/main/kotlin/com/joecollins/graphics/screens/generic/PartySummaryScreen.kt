package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.GraphicsFrame.Companion.equaliseHeaderFonts
import com.joecollins.graphics.components.RegionSummaryFrame
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
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
    numRows: Int,
    altText: Flow.Publisher<out String?>,
) : GenericPanel(
    {
        background = Color.WHITE
        layout = Layout(numRows)
        add(mainFrame, "main")
        otherFrames.forEach { add(it, "other") }
        otherFrames.equaliseHeaderFonts()
    },
    partyPublisher.map { it.name.uppercase() + " SUMMARY" },
    altText,
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

        override fun preferredLayoutSize(parent: Container): Dimension = DEFAULT_SIZE

        override fun minimumLayoutSize(parent: Container): Dimension = Dimension(128, 64)

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

    class Seats<T> internal constructor(private val partyChanges: Flow.Publisher<out Map<out PartyOrCoalition, PartyOrCoalition>>?) {
        lateinit var curr: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>)
        var diff: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>)? = null
        var prev: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>>)? = null
        var header: String = "SEATS"

        val calculatedDiff: T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Int>> by lazy {
            diff ?: {
                curr().merge(if (partyChanges == null) prev!!() else Aggregators.partyChanges(prev!!(), partyChanges, Int::plus)) { c, p ->
                    sequenceOf(c.keys.asSequence(), p.keys.asSequence()).flatten()
                        .distinct()
                        .associateWith { (c[it] ?: 0) - (p[it] ?: 0) }
                }
            }
        }
    }

    class Votes<T> internal constructor(private val partyChanges: Flow.Publisher<Map<Party, Party>>?) {
        lateinit var currPct: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>)
        var diffPct: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>)? = null
        var prevPct: (T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Double>>)? = null
        var header: String = "POPULAR VOTE"

        val calculatedDiff: T.() -> Flow.Publisher<out Map<out PartyOrCoalition, Double>> by lazy {
            diffPct ?: {
                currPct().merge(if (partyChanges == null) prevPct!!() else Aggregators.partyChanges(prevPct!!(), partyChanges, Double::plus)) { c, p ->
                    sequenceOf(c.keys.asSequence(), p.keys.asSequence()).flatten()
                        .distinct()
                        .associateWith { (c[it] ?: 0.0) - (p[it] ?: 0.0) }
                }
            }
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
                if (diff == 0) "\u00b10" else DecimalFormat("+0;-0").format(diff),
            )
        }

        val votePublisher = Publisher(calculateVotes())
        private fun updateVotes() = synchronized(this) { votePublisher.submit(calculateVotes()) }
        private fun calculateVotes(): List<String> {
            val vote = this.votePct[this.party] ?: 0.0
            val diff = this.votePctDiff[this.party] ?: 0.0
            return listOf(
                DecimalFormat("0.0%").format(vote),
                if (diff == 0.0) "\u00b10.0%" else DecimalFormat("+0.0%;-0.0%").format(diff),
            )
        }
    }

    companion object {
        fun <T> of(
            mainRegion: T,
            header: T.() -> Flow.Publisher<out String>,
            numRows: Int,
            seats: (Seats<T>.() -> Unit)? = null,
            votes: (Votes<T>.() -> Unit)? = null,
            regions: List<T>,
            party: Flow.Publisher<out PartyOrCoalition>,
            partyChanges: Flow.Publisher<Map<Party, Party>>? = null,
        ): PartySummaryScreen = build(
            mainRegion,
            header,
            numRows,
            seats?.let { Seats<T>(partyChanges).apply(it) },
            votes?.let { Votes<T>(partyChanges).apply(it) },
            regions,
            party,
        )

        private fun <T> build(
            mainRegion: T,
            header: T.() -> Flow.Publisher<out String>,
            numRows: Int,
            seats: Seats<T>?,
            votes: Votes<T>?,
            regions: List<T>,
            party: Flow.Publisher<out PartyOrCoalition>,
        ): PartySummaryScreen = PartySummaryScreen(
            party,
            createFrame(mainRegion, header, party, seats, votes),
            regions.map { createFrame(it, header, party, seats, votes) },
            numRows,
            createAltText(party, header, mainRegion, regions, seats, votes),
        )

        private fun <T> createFrame(
            region: T,
            header: T.() -> Flow.Publisher<out String>,
            party: Flow.Publisher<out PartyOrCoalition>,
            seats: Seats<T>?,
            votes: Votes<T>?,
        ): RegionSummaryFrame {
            val input = createInput(region, party, seats, votes)
            val seatPublisher = input.seatPublisher
            val votePublisher = input.votePublisher
            val (headers, values) = when {
                seats != null && votes != null -> listOf(seats.header, votes.header) to seatPublisher.merge(votePublisher) { s, v -> listOf(s, v) }
                votes != null -> listOf(votes.header) to votePublisher.map { listOf(it) }
                seats != null -> listOf(seats.header) to seatPublisher.map { listOf(it) }
                else -> listOf<String>() to listOf<List<String>>().asOneTimePublisher()
            }
            return RegionSummaryFrame(
                headerPublisher = region.header(),
                summaryColorPublisher = party.map { it.color },
                sectionsPublisher = values.map { value ->
                    value.zip(headers) { v, h -> RegionSummaryFrame.SectionWithoutColor(h, v) }
                },
            )
        }

        private fun <T> createInput(
            region: T,
            party: Flow.Publisher<out PartyOrCoalition>,
            seats: Seats<T>?,
            votes: Votes<T>?,
        ): SinglePartyInput {
            val input = SinglePartyInput()
            seats?.apply {
                region.curr().subscribe(Subscriber { input.seats = it })
                region.calculatedDiff().subscribe(Subscriber { input.seatDiff = it })
            }
            votes?.apply {
                region.currPct().subscribe(Subscriber { input.votePct = it })
                region.calculatedDiff().subscribe(Subscriber { input.votePctDiff = it })
            }
            party.subscribe(Subscriber { input.party = it })
            return input
        }

        private fun <T> createAltText(
            partyPublisher: Flow.Publisher<out PartyOrCoalition>,
            header: T.() -> Flow.Publisher<out String>,
            mainRegion: T,
            regions: List<T>,
            seats: Seats<T>?,
            votes: Votes<T>?,
        ): Flow.Publisher<String> {
            val entries = regions.map { createAltTextLine(it, header, partyPublisher, seats, votes) }
                .combine()
                .map { it.joinToString("\n") }
            return partyPublisher.map { "${it.name.uppercase()} SUMMARY" }
                .merge(createAltTextLine(mainRegion, header, partyPublisher, seats, votes)) { h, m -> "$h\n\n$m" }
                .merge(entries) { h, e -> "$h\n\n$e" }
        }

        private fun <T> createAltTextLine(
            region: T,
            header: T.() -> Flow.Publisher<out String>,
            party: Flow.Publisher<out PartyOrCoalition>,
            seats: Seats<T>?,
            votes: Votes<T>?,
        ): Flow.Publisher<String> {
            val input = createInput(region, party, seats, votes)
            val seatPublisher = input.seatPublisher
            val votePublisher = input.votePublisher
            val seatsText = seatPublisher.map { (curr, diff) -> if (seats == null) null else "${seats.header}: $curr ($diff)" }
            val votesText = votePublisher.map { (curr, diff) -> if (votes == null) null else "${votes.header}: $curr ($diff)" }
            return seatsText.merge(votesText) { seatText, voteText -> sequenceOf(seatText, voteText).filterNotNull().joinToString("; ") }
                .merge(header(region)) { text, title -> "$title: $text" }
        }
    }
}
