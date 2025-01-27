package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.ImageGenerator.combineHorizontal
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.MapFrame
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyOrCoalition
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

class MixedMemberResultPanel private constructor(
    label: Flow.Publisher<out String?>,
    private val candidateFrame: BarFrame,
    private val candidateChangeFrame: BarFrame?,
    private val partyFrame: BarFrame,
    private val partyChangeFrame: BarFrame?,
    private val mapFrame: MapFrame?,
    altText: Flow.Publisher<out String>,
) : GenericPanel(
    {
        layout = ScreenLayout()
        background = Color.WHITE
        add(candidateFrame, ScreenLayout.CANDIDATE)
        candidateChangeFrame?.also { add(it, ScreenLayout.CANDIDATE_DIFF) }
        add(partyFrame, ScreenLayout.PARTY)
        partyChangeFrame?.also { add(it, ScreenLayout.PARTY_DIFF) }
        mapFrame?.also { add(it, ScreenLayout.MAP) }
    },
    label,
    altText,
) {

    private class ScreenLayout : LayoutManager {

        companion object {
            const val CANDIDATE = "CANDIDATE"
            const val CANDIDATE_DIFF = "CANDIDATE_DIFF"
            const val PARTY = "PARTY"
            const val PARTY_DIFF = "PARTY_DIFF"
            const val MAP = "MAP"
        }

        private val components = HashMap<String, Component>()

        override fun addLayoutComponent(name: String, comp: Component) {
            components[name] = comp
        }
        override fun removeLayoutComponent(comp: Component) {
            components.entries.firstOrNull { it.value == comp }?.let { components.remove(it.key) }
        }
        override fun preferredLayoutSize(parent: Container): Dimension = DEFAULT_SIZE

        override fun minimumLayoutSize(parent: Container): Dimension = Dimension(0, 0)

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

    class CandidateVotes internal constructor() {
        lateinit var votes: Flow.Publisher<out Map<Candidate, Int?>>
        lateinit var header: Flow.Publisher<out String?>
        var subhead: Flow.Publisher<out String?>? = null
        var incumbentMarker: String? = null
        var winner: Flow.Publisher<Candidate?>? = null
        var pctReporting: Flow.Publisher<Double>? = null
        var progressLabel: Flow.Publisher<out String?>? = null

        internal val incumbentMarkerPadded by lazy { if (incumbentMarker == null) "" else " [$incumbentMarker]" }
    }

    class CandidateChange internal constructor() {
        lateinit var prevVotes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>
        lateinit var header: Flow.Publisher<out String?>
    }

    class PartyVotes internal constructor() {
        lateinit var votes: Flow.Publisher<out Map<out PartyOrCandidate, Int?>>
        lateinit var header: Flow.Publisher<out String?>
        var pctReporting: Flow.Publisher<Double>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
    }

    class PartyChange internal constructor() {
        lateinit var prevVotes: Flow.Publisher<out Map<out PartyOrCoalition, Int>>
        lateinit var header: Flow.Publisher<out String?>
    }

    companion object {
        private val PCT_FORMAT = DecimalFormat("0.0%")
        private val PCT_DIFF_FORMAT = DecimalFormat("+0.0%;-0.0%")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")

        fun Flow.Publisher<out Map<out Party, Int?>>.convertToPartyOrCandidateForMixedMember() = map { v -> v.mapKeys { PartyOrCandidate(it.key) } }

        fun of(
            candidateVotes: CandidateVotes.() -> Unit,
            candidateChange: (CandidateChange.() -> Unit)? = null,
            partyVotes: PartyVotes.() -> Unit,
            partyChange: (PartyChange.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): MixedMemberResultPanel {
            val cv = CandidateVotes().apply(candidateVotes)
            val cc = candidateChange?.let { CandidateChange().apply(it) }
            val pv = PartyVotes().apply(partyVotes)
            val pc = partyChange?.let { PartyChange().apply(it) }
            return MixedMemberResultPanel(
                title,
                createCandidateVotes(cv, cc),
                createCandidateChange(cv, cc),
                createPartyVotes(pv),
                createPartyChange(pv, pc),
                map?.mapFrame,
                createAltText(cv, cc, pv, pc, title),
            )
        }

        private fun createCandidateVotes(
            candidateVotes: CandidateVotes,
            candidateChange: CandidateChange?,
        ): BarFrame {
            class CandidateBarTemplate(
                val shape: Shape,
                val leftLabel: (Candidate) -> String,
                val rightLabel: (Int, Double) -> String,
                val incumbentLabel: (Candidate) -> Shape?,
            )
            val doubleLine = candidateVotes.votes.map { it.size < 10 && candidateChange == null }
            val namedCandidateTemplate = doubleLine.map {
                if (it) {
                    CandidateBarTemplate(
                        ImageGenerator.createHalfTickShape(),
                        { candidate -> "${candidate.name.uppercase()}\n${candidate.party.name.uppercase()}" },
                        { numVotes, pct -> "${THOUSANDS_FORMAT.format(numVotes.toLong())}\n${PCT_FORMAT.format(pct)}" },
                        { candidate -> candidateVotes.incumbentMarker.takeIf { candidate.incumbent }?.let { ImageGenerator.createHalfBoxedTextShape(it) } },
                    )
                } else {
                    CandidateBarTemplate(
                        ImageGenerator.createTickShape(),
                        { candidate -> "${candidate.name.uppercase()} (${candidate.party.abbreviation.uppercase()})" },
                        { numVotes, pct -> "${THOUSANDS_FORMAT.format(numVotes.toLong())} (${PCT_FORMAT.format(pct)})" },
                        { candidate -> candidateVotes.incumbentMarker.takeIf { candidate.incumbent }?.let { ImageGenerator.createBoxedTextShape(it) } },
                    )
                }
            }
            val blankCandidateNameTemplate = CandidateBarTemplate(
                ImageGenerator.createTickShape(),
                { candidate -> candidate.party.name.uppercase() },
                { numVotes, pct -> "${THOUSANDS_FORMAT.format(numVotes.toLong())} (${PCT_FORMAT.format(pct)})" },
                { null },
            )
            val result = Result()
            candidateVotes.votes.subscribe(Subscriber { result.votes = it })
            candidateVotes.winner?.subscribe(Subscriber { result.winner = it })
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
                                if (candidate.name.isBlank()) candidate.party.name.uppercase() else leftLabel,
                                candidate.party.color,
                                if (pct.isNaN()) 0 else pct,
                                (if (pct.isNaN()) "WAITING..." else rightLabel),
                                rowTemplate.incumbentLabel(candidate).combineHorizontal(if (candidate == winner) (if (candidate.name.isBlank()) ImageGenerator.createTickShape() else rowTemplate.shape) else null),
                            )
                        }
                        .toList()
                }
            return BarFrameBuilder.basic(
                barsPublisher = bars,
                headerPublisher = candidateVotes.header,
                rightHeaderLabelPublisher = candidateVotes.progressLabel,
                subheadPublisher = candidateVotes.subhead,
                maxPublisher = candidateVotes.pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) }
                    ?: (2.0 / 3).asOneTimePublisher(),
            )
        }

        private fun createCandidateChange(
            candidateVotes: CandidateVotes,
            candidateChange: CandidateChange?,
        ): BarFrame? {
            if (candidateChange == null) {
                return null
            }
            val change = Change<Candidate>()
            candidateVotes.votes.subscribe(Subscriber { change.curr = it })
            candidateChange.prevVotes.subscribe(Subscriber { change.prev = it })
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
                        .filter { it.key.party == Party.OTHERS }
                        .map { 1.0 * it.value / currTotal }
                        .sum() +
                        prev.entries
                            .filter { it.key == Party.OTHERS || !currParties.contains(it.key) }
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
            return BarFrameBuilder.basic(
                barsPublisher = bars,
                headerPublisher = candidateChange.header,
                wingspanPublisher = candidateVotes.pctReporting?.map { 0.05 / it.coerceAtLeast(1e-6) } ?: 0.05.asOneTimePublisher(),
            )
        }

        private fun createPartyVotes(partyVotes: PartyVotes): BarFrame {
            val bars = partyVotes.votes.map { votes ->
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
            }
            return BarFrameBuilder.basic(
                barsPublisher = bars,
                headerPublisher = partyVotes.header,
                rightHeaderLabelPublisher = partyVotes.progressLabel,
                maxPublisher = partyVotes.pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher(),
            )
        }

        private fun createPartyChange(partyVotes: PartyVotes, partyChange: PartyChange?): BarFrame? {
            if (partyChange == null) {
                return null
            }
            val change = Change<PartyOrCandidate>()
            partyVotes.votes.subscribe(Subscriber { change.curr = it })
            partyChange.prevVotes.subscribe(Subscriber { change.prev = it })
            val bars = change.currPublisher.merge(change.prevPublisher) { currRaw, prev ->
                val currTotal = currRaw.values.filterNotNull().sum()
                if (currTotal == 0) {
                    return@merge listOf()
                }
                if (currRaw.values.any { it == null }) {
                    return@merge listOf()
                }
                val curr: Map<out PartyOrCandidate, Int> = currRaw.mapValues { it.value!! }
                val prevTotal = prev.values.sum()
                val presentBars = curr.entries.asSequence()
                    .filter { it.key.party !== Party.OTHERS }
                    .sortedByDescending { it.value }
                    .map {
                        val pct = (
                            1.0 * it.value / currTotal -
                                1.0 * (prev[it.key.party] ?: 0) / prevTotal
                            )
                        BarFrameBuilder.BasicBar(
                            it.key.party.abbreviation.uppercase(),
                            it.key.color,
                            pct,
                            PCT_DIFF_FORMAT.format(pct),
                        )
                    }
                val otherTotal = (
                    curr.entries
                        .filter { it.key.party == Party.OTHERS }.sumOf { 1.0 * it.value / currTotal } +
                        prev.entries
                            .filter { it.key == Party.OTHERS || !curr.keys.any { k -> k.party == it.key } }
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
            return BarFrameBuilder.basic(
                barsPublisher = bars,
                headerPublisher = partyChange.header,
                wingspanPublisher = partyVotes.pctReporting?.map { 0.05 / it.coerceAtLeast(1e-6) } ?: 0.05.asOneTimePublisher(),
            )
        }

        private fun createAltText(
            candidateVotes: CandidateVotes,
            candidateChange: CandidateChange?,
            partyVotes: PartyVotes,
            partyChange: PartyChange?,
            header: Flow.Publisher<out String?>,
        ): Flow.Publisher<String> {
            val candidatesEntries = candidateVotes.votes
                .run { if (candidateVotes.winner == null) map { votes -> votes to null } else merge(candidateVotes.winner!!) { votes, winner -> votes to winner } }
                .merge(candidateChange?.prevVotes ?: null.asOneTimePublisher()) { (curr, winner), prev ->
                    val partialDeclaration = curr.values.any { it == null }
                    val currTotal = curr.values.sumOf { it ?: 0 }.toDouble()
                    val prevTotal = prev?.values?.sum()?.toDouble()
                    val others = run {
                        val currParties = curr.keys.map { it.party }
                        prev?.filterKeys { !currParties.contains(it) }?.values?.sum() ?: 0
                    }
                    val entries = curr.entries
                        .sortedByDescending { it.key.overrideSortOrder ?: it.value ?: 0 }
                        .joinToString("\n") {
                            val incumbentMarker = if (it.key.incumbent && candidateVotes.incumbentMarker != null && it.key.name.isNotBlank()) {
                                candidateVotes.incumbentMarkerPadded
                            } else {
                                ""
                            }
                            val partyLabel = if (it.key.name.isBlank()) {
                                ""
                            } else {
                                " (${it.key.party.abbreviation})"
                            }
                            val candidateLabel =
                                it.key.name.ifBlank { it.key.party.name }.uppercase() +
                                    incumbentMarker +
                                    partyLabel

                            val votesLabel = if (it.value == null || currTotal == 0.0) {
                                "WAITING..."
                            } else {
                                DecimalFormat("#,##0").format(it.value) + (
                                    if (partialDeclaration) {
                                        ""
                                    } else {
                                        val currPct = (it.value ?: 0) / currTotal
                                        val diffLabel = if (prev == null) {
                                            ""
                                        } else {
                                            val prevVotes = (prev[it.key.party] ?: 0) + (if (it.key == Candidate.OTHERS) others else 0)
                                            val prevPct = prevVotes / (prevTotal ?: 0.0)
                                            val diff = currPct - prevPct
                                            ", ${DecimalFormat("+0.0%;-0.0%").format(diff)}"
                                        }
                                        val currLabel = DecimalFormat("0.0%").format(currPct)
                                        " ($currLabel$diffLabel)"
                                    }
                                    )
                            }
                            val winnerLabel = if (winner == it.key) " WINNER" else ""
                            "$candidateLabel: $votesLabel$winnerLabel"
                        }
                    entries + (if (others > 0 && !partialDeclaration && !curr.containsKey(Candidate.OTHERS)) "\nOTHERS: - (-${DecimalFormat("0.0%").format(others / (prevTotal ?: 0.0))})" else "")
                }
            val candidates = candidateVotes.header
                .run { if (candidateVotes.subhead == null) this else merge(candidateVotes.subhead!!) { head, subhead -> head + (if (subhead.isNullOrEmpty()) "" else ", $subhead") } }
                .run { if (candidateChange == null) this else merge(candidateChange.header) { vote, change -> vote + (if (change.isNullOrEmpty()) "" else " ($change)") } }
                .run { if (candidateVotes.progressLabel == null) this else merge(candidateVotes.progressLabel!!) { head, prog -> head + (if (prog.isNullOrEmpty()) "" else " [$prog]") } }
                .merge(candidatesEntries) { head, entries -> "$head\n$entries" }

            val partyEntries = partyVotes.votes.merge(partyChange?.prevVotes ?: null.asOneTimePublisher()) { curr, prev ->
                val partialDeclaration = curr.values.any { it == null }
                val currTotal = curr.values.sumOf { it ?: 0 }.toDouble()
                val prevTotal = prev?.values?.sum()?.toDouble()
                val others = run {
                    val currParties = curr.keys.map { it.party }
                    prev?.filterKeys { !currParties.contains(it) }?.values?.sum() ?: 0
                }
                curr.entries
                    .sortedByDescending { it.key.overrideSortOrder ?: it.value ?: 0 }
                    .joinToString("\n") {
                        val votesLabel = if (it.value == null || currTotal == 0.0) {
                            "WAITING..."
                        } else {
                            DecimalFormat("#,##0").format(it.value) + (
                                if (partialDeclaration) {
                                    ""
                                } else {
                                    val currPct = (it.value ?: 0) / currTotal
                                    val diffLabel = if (prev == null) {
                                        ""
                                    } else {
                                        val prevVotes = (prev[it.key.party] ?: 0) + (if (it.key.party == Party.OTHERS) others else 0)
                                        val prevPct = prevVotes / (prevTotal ?: 0.0)
                                        ", ${DecimalFormat("+0.0%;-0.0%").format(currPct - prevPct)}"
                                    }
                                    val currLabel = DecimalFormat("0.0%").format(currPct)
                                    " ($currLabel$diffLabel)"
                                }
                                )
                        }
                        "${it.key.name.uppercase()}: " + votesLabel
                    }
            }
            val parties = partyVotes.header
                .run { if (partyChange == null) this else merge(partyChange.header) { vote, change -> vote + (if (change.isNullOrEmpty()) "" else " ($change)") } }
                .run { if (partyVotes.progressLabel == null) this else merge(partyVotes.progressLabel!!) { head, prog -> head + (if (prog.isNullOrEmpty()) "" else " [$prog]") } }
                .merge(partyEntries) { head, entries -> "$head\n$entries" }

            return header.merge(candidates) { h, c -> "$h\n\n$c" }.merge(parties) { h, p -> "$h\n\n$p" }
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
    }
}
