package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class MixedMemberResultPanel private constructor(
    label: JLabel,
    private val candidateFrame: BarFrame,
    private val candidateChangeFrame: BarFrame?,
    private val partyFrame: BarFrame,
    private val partyChangeFrame: BarFrame?,
    private val mapFrame: MapFrame?
) : JPanel() {

    private inner class ScreenLayout : LayoutManager {
        override fun addLayoutComponent(name: String, comp: Component) {}
        override fun removeLayoutComponent(comp: Component) {}
        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(1024, 512)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(0, 0)
        }

        override fun layoutContainer(parent: Container) {
            val width = parent.width
            val height = parent.height
            candidateFrame.setLocation(5, 5)
            candidateFrame.setSize(
                width * 3 / 5 - 10, height / (if (candidateChangeFrame == null) 1 else 2) - 10
            )
            candidateChangeFrame?.setLocation(5, height / 2 + 5)
            candidateChangeFrame?.setSize(width * 3 / 5 - 10, height / 2 - 10)
            partyFrame.setLocation(width * 3 / 5 + 5, 5)
            partyFrame.setSize(width * 2 / 5 - 10, height / (if (partyChangeFrame == null) 2 else 3) - 10)
            partyChangeFrame?.setLocation(width * 3 / 5 + 5, height / 3 + 5)
            partyChangeFrame?.setSize(width * 2 / 5 - 10, height / 3 - 10)
            mapFrame?.setLocation(width * 3 / 5 + 5, height * 2 / (if (partyChangeFrame == null) 4 else 3) + 5)
            mapFrame?.setSize(width * 2 / 5 - 10, height / (if (partyChangeFrame == null) 2 else 3) - 10)
        }
    }

    class Builder {
        private var candidateVotes: Flow.Publisher<out Map<Candidate, Int?>> = emptyMap<Candidate, Int?>().asOneTimePublisher()
        private var candidatePrev: Flow.Publisher<out Map<Party, Int>>? = null
        private var candidatePctReporting: Flow.Publisher<out Double>? = null
        private var winner: Flow.Publisher<out Candidate?> = (null as Candidate?).asOneTimePublisher()
        private var partyVotes: Flow.Publisher<out Map<Party, Int?>> = emptyMap<Party, Int?>().asOneTimePublisher()
        private var partyPrev: Flow.Publisher<out Map<Party, Int>>? = null
        private var partyPctReporting: Flow.Publisher<out Double>? = null
        private var incumbentMarker = ""
        private var candidateVoteHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var candidateVoteSubheader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var candidateChangeHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var partyVoteHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var partyChangeHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var mapBuilder: MapBuilder<*>? = null

        @JvmOverloads
        fun withCandidateVotes(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String>,
            subheader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        ): Builder {
            candidateVotes = votes
            candidateVoteHeader = header
            candidateVoteSubheader = subheader
            return this
        }

        fun withIncumbentMarker(incumbentMarker: String): Builder {
            this.incumbentMarker = " $incumbentMarker"
            return this
        }

        fun withWinner(winner: Flow.Publisher<out Candidate?>): Builder {
            this.winner = winner
            return this
        }

        fun withPrevCandidateVotes(
            votes: Flow.Publisher<out Map<Party, Int>>,
            header: Flow.Publisher<out String>
        ): Builder {
            candidatePrev = votes
            candidateChangeHeader = header
            return this
        }

        fun withPartyVotes(
            votes: Flow.Publisher<out Map<Party, Int?>>,
            header: Flow.Publisher<out String>
        ): Builder {
            partyVotes = votes
            partyVoteHeader = header
            return this
        }

        fun withPrevPartyVotes(
            votes: Flow.Publisher<out Map<Party, Int>>,
            header: Flow.Publisher<out String>
        ): Builder {
            partyPrev = votes
            partyChangeHeader = header
            return this
        }

        fun withCandidatePctReporting(pctReporting: Flow.Publisher<out Double>): Builder {
            candidatePctReporting = pctReporting
            return this
        }

        fun withPartyPctReporting(pctReporting: Flow.Publisher<out Double>): Builder {
            partyPctReporting = pctReporting
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out PartyResult?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String>
        ): Builder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out PartyResult?>,
            focus: Flow.Publisher<out List<T>?>,
            additionalHighlight: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String>
        ): Builder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, additionalHighlight, header)
            return this
        }

        fun build(header: Flow.Publisher<out String>): MixedMemberResultPanel {
            return MixedMemberResultPanel(
                createHeaderLabel(header),
                createCandidateVotes(),
                createCandidateChange(),
                createPartyVotes(),
                createPartyChange(),
                createMapFrame()
            )
        }

        private class Result {
            enum class Property {
                VOTES, WINNER
            }

            private var _votes: Map<Candidate, Int?> = HashMap()
            private var _winner: Candidate? = null

            var votes: Map<Candidate, Int?>
                get() = _votes
                set(votes) {
                    _votes = votes
                    votesPublisher.submit(votes)
                }

            var winner: Candidate?
                get() = _winner
                set(winner) {
                    _winner = winner
                    winnerPublisher.submit(winner)
                }

            var votesPublisher = Publisher(_votes)
            var winnerPublisher = Publisher(_winner)
        }

        private fun createCandidateVotes(): BarFrame {
            val showBothLines = candidatePrev == null
            val shape = if (showBothLines) ImageGenerator.createHalfTickShape() else ImageGenerator.createTickShape()
            val result = Result()
            candidateVotes.subscribe(Subscriber { result.votes = it })
            winner.subscribe(Subscriber { result.winner = it })
            val bars = result.votesPublisher.merge(result.winnerPublisher) { votes, winner ->
                val total = votes.values.filterNotNull().sum()
                val partialDeclaration = votes.values.any { it == null }
                votes.entries
                    .sortedByDescending { e: Map.Entry<Candidate, Int?> ->
                        val value = e.value
                        if (e.key === Candidate.OTHERS || value == null) Int.MIN_VALUE else value
                    }
                    .map { e: Map.Entry<Candidate, Int?> ->
                        val candidate = e.key
                        val votes = e.value ?: 0
                        val pct = e.value?.toDouble()?.div(total) ?: Double.NaN
                        val leftLabel: String
                        var rightLabel: String
                        if (candidate.name.isBlank()) {
                            leftLabel = candidate.party.name.uppercase()
                            rightLabel = ("${THOUSANDS_FORMAT.format(votes.toLong())} (${PCT_FORMAT.format(pct)})")
                        } else if (showBothLines) {
                            leftLabel =
                                ("${candidate.name.uppercase()}${if (candidate.isIncumbent()) incumbentMarker else ""}\n${candidate.party.name.uppercase()}")
                            rightLabel = "${THOUSANDS_FORMAT.format(votes.toLong())}\n${PCT_FORMAT.format(pct)}"
                        } else {
                            leftLabel =
                                ("${candidate.name.uppercase()}${if (candidate.isIncumbent()) incumbentMarker else ""} (${candidate.party.abbreviation.uppercase()})")
                            rightLabel = ("${THOUSANDS_FORMAT.format(votes.toLong())} (${PCT_FORMAT.format(pct)})")
                        }
                        if (partialDeclaration) {
                            rightLabel = THOUSANDS_FORMAT.format(votes.toLong())
                        }
                        BasicBar(
                            if (candidate === Candidate.OTHERS) "OTHERS" else leftLabel,
                            candidate.party.color,
                            if (java.lang.Double.isNaN(pct)) 0 else pct,
                            (if (java.lang.Double.isNaN(pct)) "WAITING..." else rightLabel),
                            if (candidate == winner) (if (candidate.name.isBlank()) ImageGenerator.createTickShape() else shape) else null
                        )
                    }
                    .toList()
            }
            return BarFrameBuilder.basic(bars)
                .withHeader(candidateVoteHeader)
                .withSubhead(candidateVoteSubheader)
                .withMax(
                    candidatePctReporting?.map { x: Double -> 2.0 / 3 / x.coerceAtLeast(1e-6) }
                        ?: (2.0 / 3).asOneTimePublisher()
                )
                .build()
        }

        private class Change<C> {
            private var _curr: Map<C, Int?> = HashMap()
            private var _prev: Map<Party, Int> = HashMap()

            var curr: Map<C, Int?>
                get() = _curr
                set(curr) {
                    _curr = curr
                    currPublisher.submit(curr)
                }

            var prev: Map<Party, Int>
                get() = _prev
                set(prev) {
                    _prev = prev
                    prevPublisher.submit(prev)
                }

            val currPublisher = Publisher(_curr)
            val prevPublisher = Publisher(_prev)
        }

        private fun createCandidateChange(): BarFrame? {
            if (candidatePrev == null) {
                return null
            }
            val change = Change<Candidate>()
            candidateVotes.subscribe(Subscriber { change.curr = it })
            candidatePrev!!.subscribe(Subscriber { change.prev = it })
            val bars = change.currPublisher.merge(change.prevPublisher) { currRaw, prev ->
                val currTotal = currRaw.values.filterNotNull().sum()
                if (currTotal == 0) {
                    return@merge listOf()
                }
                if (currRaw.values.any { it == null }) {
                    return@merge listOf()
                }
                val curr: Map<Candidate, Int> = currRaw.mapValues { it.value!! }
                val prevTotal = prev.values.sum()
                val currParties = curr.keys.map(Candidate::party).toSet()
                val matchingBars = curr.entries.asSequence()
                    .filter { it.key.party !== Party.OTHERS }
                    .sortedByDescending { it.value }
                    .map { e: Map.Entry<Candidate, Int> ->
                        val pct = (
                            1.0 * e.value / currTotal -
                                1.0 *
                                (prev[e.key.party] ?: 0) /
                                prevTotal
                            )
                        BasicBar(
                            e.key.party.abbreviation.uppercase(),
                            e.key.party.color,
                            pct,
                            PCT_DIFF_FORMAT.format(pct)
                        )
                    }
                val othersPct = (
                    curr.entries.asSequence()
                        .filter { it.key.party === Party.OTHERS }
                        .map { 1.0 * it.value / currTotal }
                        .sum() +
                        prev.entries
                            .filter { it.key === Party.OTHERS || !currParties.contains(it.key) }
                            .map { -1.0 * it.value / prevTotal }
                            .sum()
                    )
                val nonMatchingBars = if (othersPct == 0.0) emptySequence() else sequenceOf(
                    BasicBar(
                        Party.OTHERS.abbreviation.uppercase(),
                        Party.OTHERS.color,
                        othersPct,
                        PCT_DIFF_FORMAT.format(othersPct)
                    )
                )
                sequenceOf(matchingBars, nonMatchingBars).flatten().toList()
            }
            return BarFrameBuilder.basic(bars)
                .withHeader(candidateChangeHeader)
                .withWingspan(candidatePctReporting?.map { x: Double -> 0.05 / x.coerceAtLeast(1e-6) } ?: 0.05.asOneTimePublisher())
                .build()
        }

        private fun createPartyVotes(): BarFrame {
            return BarFrameBuilder.basic(
                partyVotes.map { v: Map<Party, Int?> ->
                    val total = v.values.filterNotNull().sum()
                    val partialDeclaration = v.values.any { it == null }
                    v.entries
                        .sortedByDescending { e: Map.Entry<Party, Int?> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else (e.value ?: -1) }
                        .map { e: Map.Entry<Party, Int?> ->
                            val value = e.value
                            val pct = if (value == null) Double.NaN else 1.0 * value / total
                            BasicBar(
                                e.key.name.uppercase(),
                                e.key.color,
                                if (java.lang.Double.isNaN(pct)) 0 else pct,
                                if (java.lang.Double.isNaN(pct)) "WAITING..." else THOUSANDS_FORMAT.format(e.value) +
                                    if (partialDeclaration) "" else " (" + PCT_FORMAT.format(pct) + ")"
                            )
                        }
                        .toList()
                }
            )
                .withHeader(partyVoteHeader)
                .withMax(partyPctReporting?.map { x: Double -> 2.0 / 3 / x.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher())
                .build()
        }

        private fun createPartyChange(): BarFrame? {
            if (partyPrev == null) {
                return null
            }
            val change = Change<Party>()
            partyVotes.subscribe(Subscriber { change.curr = it })
            partyPrev!!.subscribe(Subscriber { change.prev = it })
            val bars = change.currPublisher.merge(change.prevPublisher) { currRaw, prev ->
                val currTotal = currRaw.values.filterNotNull().sum()
                if (currTotal == 0) {
                    return@merge listOf()
                }
                if (currRaw.values.any { it == null }) {
                    return@merge listOf()
                }
                val curr: Map<Party, Int> = currRaw.mapValues { it.value!! }
                val prevTotal = prev.values.sum()
                val presentBars = curr.entries.asSequence()
                    .filter { it.key !== Party.OTHERS }
                    .sortedByDescending { it.value }
                    .map { e: Map.Entry<Party, Int> ->
                        val pct = (
                            1.0 * e.value / currTotal -
                                1.0 * (prev[e.key] ?: 0) / prevTotal
                            )
                        BasicBar(
                            e.key.abbreviation.uppercase(),
                            e.key.color,
                            pct,
                            PCT_DIFF_FORMAT.format(pct)
                        )
                    }
                val otherTotal = (
                    curr.entries
                        .filter { it.key === Party.OTHERS }
                        .map { 1.0 * it.value / currTotal }
                        .sum() +
                        prev.entries
                            .filter { it.key === Party.OTHERS || !curr.containsKey(it.key) }
                            .map { -1.0 * it.value / prevTotal }
                            .sum()
                    )
                val absentBars = if (otherTotal == 0.0) emptySequence() else sequenceOf(
                    BasicBar(
                        Party.OTHERS.abbreviation.uppercase(),
                        Party.OTHERS.color,
                        otherTotal,
                        PCT_DIFF_FORMAT.format(otherTotal)
                    )
                )
                sequenceOf(presentBars, absentBars).flatten().toList()
            }
            return BarFrameBuilder.basic(bars)
                .withHeader(partyChangeHeader)
                .withWingspan(partyPctReporting?.map { x: Double -> 0.05 / x.coerceAtLeast(1e-6) } ?: 0.05.asOneTimePublisher())
                .build()
        }

        private fun createMapFrame(): MapFrame? {
            return mapBuilder?.createMapFrame()
        }
    }

    companion object {
        private val PCT_FORMAT = DecimalFormat("0.0%")
        private val PCT_DIFF_FORMAT = DecimalFormat("+0.0%;-0.0%")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")

        @JvmStatic fun builder(): Builder {
            return Builder()
        }

        private fun createHeaderLabel(textPublisher: Flow.Publisher<out String>): JLabel {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textPublisher.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            return headerLabel
        }
    }

    init {
        layout = BorderLayout()
        background = Color.WHITE
        add(label, BorderLayout.NORTH)
        val panel = JPanel()
        panel.layout = ScreenLayout()
        panel.background = Color.WHITE
        add(panel, BorderLayout.CENTER)
        panel.add(candidateFrame)
        candidateChangeFrame?.also { panel.add(it) }
        panel.add(partyFrame)
        partyChangeFrame?.also { panel.add(it) }
        mapFrame?.also { panel.add(it) }
    }
}