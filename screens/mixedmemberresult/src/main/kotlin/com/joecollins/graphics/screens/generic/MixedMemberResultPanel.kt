package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.MapFrame
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel

class MixedMemberResultPanel private constructor(
    label: Flow.Publisher<out String?>,
    private val candidateFrame: BarFrame,
    private val candidateChangeFrame: BarFrame?,
    private val partyFrame: BarFrame,
    private val partyChangeFrame: BarFrame?,
    private val mapFrame: MapFrame?,
) : GenericPanel(
    run {
        val panel = JPanel()
        panel.layout = ScreenLayout()
        panel.background = Color.WHITE
        panel.add(candidateFrame, ScreenLayout.CANDIDATE)
        candidateChangeFrame?.also { panel.add(it, ScreenLayout.CANDIDATE_DIFF) }
        panel.add(partyFrame, ScreenLayout.PARTY)
        partyChangeFrame?.also { panel.add(it, ScreenLayout.PARTY_DIFF) }
        mapFrame?.also { panel.add(it, ScreenLayout.MAP) }
        panel
    },
    label,
) {

    private class ScreenLayout : LayoutManager {

        companion object {
            val CANDIDATE = "CANDIDATE"
            val CANDIDATE_DIFF = "CANDIDATE_DIFF"
            val PARTY = "PARTY"
            val PARTY_DIFF = "PARTY_DIFF"
            val MAP = "MAP"
        }

        private val components = HashMap<String, Component>()

        override fun addLayoutComponent(name: String, comp: Component) {
            components[name] = comp
        }
        override fun removeLayoutComponent(comp: Component) {
            components.entries.firstOrNull { it.value == comp }?.let { components.remove(it.key) }
        }
        override fun preferredLayoutSize(parent: Container): Dimension {
            return Dimension(1024, 512)
        }

        override fun minimumLayoutSize(parent: Container): Dimension {
            return Dimension(0, 0)
        }

        override fun layoutContainer(parent: Container) {
            val width = parent.width
            val height = parent.height
            val candidateFrame = components[CANDIDATE]!!
            val candidateChangeFrame = components[CANDIDATE_DIFF]
            val partyFrame = components[PARTY]!!
            val partyChangeFrame = components[PARTY_DIFF]
            val mapFrame = components[MAP]
            candidateFrame.setLocation(5, 5)
            candidateFrame.setSize(
                width * 3 / 5 - 10,
                height / (if (candidateChangeFrame == null) 1 else 2) - 10,
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
        private var candidatePrev: Flow.Publisher<out Map<out PartyOrCoalition, Int>>? = null
        private var candidatePctReporting: Flow.Publisher<out Double>? = null
        private var candidateProgressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()
        private var winner: Flow.Publisher<out Candidate?> = (null as Candidate?).asOneTimePublisher()
        private var partyVotes: Flow.Publisher<out Map<out PartyOrCoalition, Int?>> = emptyMap<PartyOrCoalition, Int?>().asOneTimePublisher()
        private var partyPrev: Flow.Publisher<out Map<out PartyOrCoalition, Int>>? = null
        private var partyPctReporting: Flow.Publisher<out Double>? = null
        private var partyProgressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()
        private var incumbentMarker = ""
        private var candidateVoteHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var candidateVoteSubheader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var candidateChangeHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var partyVoteHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var partyChangeHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var mapBuilder: MapBuilder<*>? = null

        fun withCandidateVotes(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String>,
            subheader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher(),
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
            votes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            header: Flow.Publisher<out String>,
        ): Builder {
            candidatePrev = votes
            candidateChangeHeader = header
            return this
        }

        fun withPartyVotes(
            votes: Flow.Publisher<out Map<out PartyOrCoalition, Int?>>,
            header: Flow.Publisher<out String>,
        ): Builder {
            partyVotes = votes
            partyVoteHeader = header
            return this
        }

        fun withPrevPartyVotes(
            votes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>,
            header: Flow.Publisher<out String>,
        ): Builder {
            partyPrev = votes
            partyChangeHeader = header
            return this
        }

        fun withCandidatePctReporting(pctReporting: Flow.Publisher<out Double>): Builder {
            candidatePctReporting = pctReporting
            return this
        }

        fun withCandidateProgressLabel(progressLabel: Flow.Publisher<out String?>): Builder {
            candidateProgressLabel = progressLabel
            return this
        }

        fun withPartyPctReporting(pctReporting: Flow.Publisher<out Double>): Builder {
            partyPctReporting = pctReporting
            return this
        }

        fun withPartyProgressLabel(progressLabel: Flow.Publisher<out String?>): Builder {
            partyProgressLabel = progressLabel
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out PartyResult?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String>,
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
            header: Flow.Publisher<out String>,
        ): Builder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, additionalHighlight, header)
            return this
        }

        fun build(header: Flow.Publisher<out String>): MixedMemberResultPanel {
            return MixedMemberResultPanel(
                header,
                createCandidateVotes(),
                createCandidateChange(),
                createPartyVotes(),
                createPartyChange(),
                createMapFrame(),
            )
        }

        private class Result {
            var votes: Map<Candidate, Int?> = emptyMap()
                set(value) {
                    field = value
                    votesPublisher.submit(value)
                }

            var winner: Candidate? = null
                set(value) {
                    field = value
                    winnerPublisher.submit(value)
                }

            var votesPublisher = Publisher(votes)
            var winnerPublisher = Publisher(winner)
        }

        private fun createCandidateVotes(): BarFrame {
            class CandidateBarTemplate(
                val shape: Shape,
                val leftLabel: (Candidate) -> String,
                val rightLabel: (Int, Double) -> String,
            )
            val doubleLine = candidateVotes.map { it.size < 10 && candidatePrev == null }
            val namedCandidateTemplate = doubleLine.map {
                if (it) {
                    CandidateBarTemplate(
                        ImageGenerator.createHalfTickShape(),
                        { candidate -> "${candidate.name.uppercase()}${if (candidate.isIncumbent()) incumbentMarker else ""}\n${candidate.party.name.uppercase()}" },
                        { numVotes, pct -> "${THOUSANDS_FORMAT.format(numVotes.toLong())}\n${PCT_FORMAT.format(pct)}" },
                    )
                } else {
                    CandidateBarTemplate(
                        ImageGenerator.createTickShape(),
                        { candidate -> "${candidate.name.uppercase()}${if (candidate.isIncumbent()) incumbentMarker else ""} (${candidate.party.abbreviation.uppercase()})" },
                        { numVotes, pct -> "${THOUSANDS_FORMAT.format(numVotes.toLong())} (${PCT_FORMAT.format(pct)})" },
                    )
                }
            }
            val blankCandidateNameTemplate = CandidateBarTemplate(
                ImageGenerator.createTickShape(),
                { candidate -> candidate.party.name.uppercase() },
                { numVotes, pct -> "${THOUSANDS_FORMAT.format(numVotes.toLong())} (${PCT_FORMAT.format(pct)})" },
            )
            val result = Result()
            candidateVotes.subscribe(Subscriber { result.votes = it })
            winner.subscribe(Subscriber { result.winner = it })
            val bars = result.votesPublisher.merge(result.winnerPublisher) { votes, winner -> votes to winner }
                .merge(namedCandidateTemplate) { (votes, winner), template ->
                    val total = votes.values.filterNotNull().sum()
                    val partialDeclaration = votes.values.any { it == null }
                    votes.entries
                        .sortedByDescending {
                            it.key.overrideSortOrder ?: it.value ?: 0
                        }
                        .map {
                            val candidate = it.key
                            val numVotes = it.value ?: 0
                            val pct = it.value?.toDouble()?.div(total) ?: Double.NaN
                            val rowTemplate = if (candidate.name.isBlank()) blankCandidateNameTemplate else template
                            val leftLabel: String = rowTemplate.leftLabel(candidate)
                            val rightLabel: String = if (partialDeclaration) THOUSANDS_FORMAT.format(numVotes.toLong()) else rowTemplate.rightLabel(numVotes, pct)
                            BarFrameBuilder.BasicBar(
                                if (candidate === Candidate.OTHERS) "OTHERS" else leftLabel,
                                candidate.party.color,
                                if (pct.isNaN()) 0 else pct,
                                (if (pct.isNaN()) "WAITING..." else rightLabel),
                                if (candidate == winner) (if (candidate.name.isBlank()) ImageGenerator.createTickShape() else rowTemplate.shape) else null,
                            )
                        }
                        .toList()
                }
            return BarFrameBuilder.basic(bars)
                .withHeader(candidateVoteHeader, rightLabelPublisher = candidateProgressLabel)
                .withSubhead(candidateVoteSubheader)
                .withMax(
                    candidatePctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) }
                        ?: (2.0 / 3).asOneTimePublisher(),
                )
                .build()
        }

        private class Change<C> {
            var curr: Map<out C, Int?> = emptyMap()
                set(value) {
                    field = value
                    currPublisher.submit(value)
                }

            var prev: Map<out PartyOrCoalition, Int> = emptyMap()
                set(value) {
                    field = value
                    prevPublisher.submit(value)
                }

            val currPublisher = Publisher(curr)
            val prevPublisher = Publisher(prev)
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
                    .map {
                        val pct = (
                            1.0 * it.value / currTotal -
                                1.0 *
                                (prev[it.key.party] ?: 0) /
                                prevTotal
                            )
                        BarFrameBuilder.BasicBar(
                            it.key.party.abbreviation.uppercase(),
                            it.key.party.color,
                            pct,
                            PCT_DIFF_FORMAT.format(pct),
                        )
                    }
                val othersPct = (
                    curr.entries.asSequence()
                        .filter { it.key.party === Party.OTHERS }
                        .map { 1.0 * it.value / currTotal }
                        .sum() +
                        prev.entries
                            .filter { it.key === Party.OTHERS || !currParties.contains(it.key) }
                            .sumOf { -1.0 * it.value / prevTotal }
                    )
                val nonMatchingBars = if (othersPct == 0.0) {
                    emptySequence()
                } else {
                    sequenceOf(
                        BarFrameBuilder.BasicBar(
                            Party.OTHERS.abbreviation.uppercase(),
                            Party.OTHERS.color,
                            othersPct,
                            PCT_DIFF_FORMAT.format(othersPct),
                        ),
                    )
                }
                sequenceOf(matchingBars, nonMatchingBars).flatten().toList()
            }
            return BarFrameBuilder.basic(bars)
                .withHeader(candidateChangeHeader)
                .withWingspan(candidatePctReporting?.map { 0.05 / it.coerceAtLeast(1e-6) } ?: 0.05.asOneTimePublisher())
                .build()
        }

        private fun createPartyVotes(): BarFrame {
            return BarFrameBuilder.basic(
                partyVotes.map { votes ->
                    val total = votes.values.filterNotNull().sum()
                    val partialDeclaration = votes.values.any { it == null }
                    votes.entries
                        .sortedByDescending { it.key.overrideSortOrder ?: (it.value ?: 0) }
                        .map {
                            val value = it.value
                            val pct = if (value == null) Double.NaN else 1.0 * value / total
                            BarFrameBuilder.BasicBar(
                                it.key.name.uppercase(),
                                it.key.color,
                                if (pct.isNaN()) 0 else pct,
                                if (pct.isNaN()) {
                                    "WAITING..."
                                } else {
                                    THOUSANDS_FORMAT.format(it.value) +
                                        if (partialDeclaration) "" else " (" + PCT_FORMAT.format(pct) + ")"
                                },
                            )
                        }
                        .toList()
                },
            )
                .withHeader(partyVoteHeader, rightLabelPublisher = partyProgressLabel)
                .withMax(partyPctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher())
                .build()
        }

        private fun createPartyChange(): BarFrame? {
            if (partyPrev == null) {
                return null
            }
            val change = Change<PartyOrCoalition>()
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
                val curr: Map<out PartyOrCoalition, Int> = currRaw.mapValues { it.value!! }
                val prevTotal = prev.values.sum()
                val presentBars = curr.entries.asSequence()
                    .filter { it.key !== Party.OTHERS }
                    .sortedByDescending { it.value }
                    .map {
                        val pct = (
                            1.0 * it.value / currTotal -
                                1.0 * (prev[it.key] ?: 0) / prevTotal
                            )
                        BarFrameBuilder.BasicBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            pct,
                            PCT_DIFF_FORMAT.format(pct),
                        )
                    }
                val otherTotal = (
                    curr.entries
                        .filter { it.key === Party.OTHERS }.sumOf { 1.0 * it.value / currTotal } +
                        prev.entries
                            .filter { it.key === Party.OTHERS || !curr.containsKey(it.key) }
                            .sumOf { -1.0 * it.value / prevTotal }
                    )
                val absentBars = if (otherTotal == 0.0) {
                    emptySequence()
                } else {
                    sequenceOf(
                        BarFrameBuilder.BasicBar(
                            Party.OTHERS.abbreviation.uppercase(),
                            Party.OTHERS.color,
                            otherTotal,
                            PCT_DIFF_FORMAT.format(otherTotal),
                        ),
                    )
                }
                sequenceOf(presentBars, absentBars).flatten().toList()
            }
            return BarFrameBuilder.basic(bars)
                .withHeader(partyChangeHeader)
                .withWingspan(partyPctReporting?.map { 0.05 / it.coerceAtLeast(1e-6) } ?: 0.05.asOneTimePublisher())
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

        fun builder(): Builder {
            return Builder()
        }
    }
}
