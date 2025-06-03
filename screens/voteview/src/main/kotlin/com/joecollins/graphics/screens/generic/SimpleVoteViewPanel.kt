package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.ImageGenerator.combineHorizontal
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.CanOverrideSortOrder
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.text.DecimalFormat
import java.util.LinkedList
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.roundToInt

class SimpleVoteViewPanel private constructor(
    label: Flow.Publisher<out String?>,
    private val seatFrame: JPanel,
    private val secondarySeatFrame: JPanel?,
    private val changeFrame: JPanel?,
    private val leftSupplementaryFrame: JPanel?,
    private val rightSupplementaryFrame: JPanel?,
    altText: Flow.Publisher<String>,
) : GenericPanel(
    {
        layout = BasicResultLayout()
        background = Color.WHITE
        add(seatFrame, BasicResultLayout.MAIN)
        if (secondarySeatFrame != null) add(secondarySeatFrame, BasicResultLayout.PREF)
        if (changeFrame != null) add(changeFrame, BasicResultLayout.DIFF)
        if (leftSupplementaryFrame != null) add(leftSupplementaryFrame, BasicResultLayout.SWING)
        if (rightSupplementaryFrame != null) add(rightSupplementaryFrame, BasicResultLayout.MAP)
    },
    label,
    altText,
) {

    enum class Display(internal val template: VoteTemplate) {
        VOTES_AND_PCT(VotePctTemplate),
        PCT_ONLY(VotePctOnlyTemplate),
    }

    companion object {
        private val PCT_FORMAT = DecimalFormat("0.0%")
        private val PCT_DIFF_FORMAT = DecimalFormat("+0.0%;-0.0%")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")

        fun partyVotes(
            current: CurrentVotes<PartyOrCoalition, Int?>.() -> Unit,
            prev: (PrevVotes<PartyOrCoalition>.() -> Unit)? = null,
            winningLine: (VoteBasedWinningLine.() -> Unit)? = null,
            displayLimit: (DisplayLimit<PartyOrCoalition>.() -> Unit)? = null,
            partyClassification: (PartyClassification<PartyOrCoalition>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel = VoteScreenBuilder<PartyOrCoalition, PartyOrCoalition, Int?, Double, Double, BarFrameBuilder.BasicBar>(
            current = CurrentVotes<PartyOrCoalition, Int?>().apply(current),
            prev = prev?.let { PrevVotes<PartyOrCoalition>().apply(it) },
            winningLine = winningLine?.let { VoteBasedWinningLine().apply(it) },
            displayLimit = displayLimit?.let { DisplayLimit<PartyOrCoalition>().apply(it) },
            partyClassification = partyClassification?.let { PartyClassification<PartyOrCoalition>().apply(it) },
            preferences = null,
            map = map?.mapFrame,
            secondMap = null,
            keyTemplate = BasicResultPanel.PartyTemplate(),
            voteTemplate = VotePctOnlyTemplate,
            valueTemplate = VoteValueTemplate(VotePctOnlyTemplate),
            others = Party.OTHERS,
            title = title,
            createBarFrame = {
                BarFrameBuilder.basic(
                    barsPublisher = bars,
                    headerPublisher = header,
                    rightHeaderLabelPublisher = progress,
                    subheadPublisher = subhead,
                    notesPublisher = notes,
                    limitsPublisher = limits,
                    linesPublisher = lines,
                )
            },
        ).build()

        fun partyVotes(
            current: CurrentVotes<PartyOrCoalition, Int?>.() -> Unit,
            prev: (PrevVotesNoSwing<PartyOrCoalition>.() -> Unit)? = null,
            winningLine: (VoteBasedWinningLine.() -> Unit)? = null,
            displayLimit: (DisplayLimit<PartyOrCoalition>.() -> Unit)? = null,
            partyClassification: (PartyClassification<PartyOrCoalition>.() -> Unit)? = null,
            map: AbstractMap<*>,
            secondMap: AbstractMap<*>,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel = VoteScreenBuilder<PartyOrCoalition, PartyOrCoalition, Int?, Double, Double, BarFrameBuilder.BasicBar>(
            current = CurrentVotes<PartyOrCoalition, Int?>().apply(current),
            prev = prev?.let { PrevVotes<PartyOrCoalition>().apply(it) },
            winningLine = winningLine?.let { VoteBasedWinningLine().apply(it) },
            displayLimit = displayLimit?.let { DisplayLimit<PartyOrCoalition>().apply(it) },
            partyClassification = partyClassification?.let { PartyClassification<PartyOrCoalition>().apply(it) },
            preferences = null,
            map = map.mapFrame,
            secondMap = secondMap.mapFrame,
            keyTemplate = BasicResultPanel.PartyTemplate(),
            voteTemplate = VotePctOnlyTemplate,
            valueTemplate = VoteValueTemplate(VotePctOnlyTemplate),
            others = Party.OTHERS,
            title = title,
            createBarFrame = {
                BarFrameBuilder.basic(
                    barsPublisher = bars,
                    headerPublisher = header,
                    rightHeaderLabelPublisher = progress,
                    subheadPublisher = subhead,
                    notesPublisher = notes,
                    limitsPublisher = limits,
                    linesPublisher = lines,
                )
            },
        ).build()

        fun partyVotes(
            current: CurrentVotes<PartyOrCoalition, Int?>.() -> Unit,
            prev: (PrevVotesNoSwing<PartyOrCoalition>.() -> Unit)? = null,
            winningLine: (VoteBasedWinningLine.() -> Unit)? = null,
            displayLimit: (DisplayLimit<PartyOrCoalition>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            preferences: (Preferences<PartyOrCoalition, PartyOrCoalition, Int?, Int>.() -> Unit),
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel = VoteScreenBuilder<PartyOrCoalition, PartyOrCoalition, Int?, Double, Double, BarFrameBuilder.BasicBar>(
            current = CurrentVotes<PartyOrCoalition, Int?>().apply(current),
            prev = prev?.let { PrevVotes<PartyOrCoalition>().apply(it) },
            winningLine = winningLine?.let { VoteBasedWinningLine().apply(it) },
            displayLimit = displayLimit?.let { DisplayLimit<PartyOrCoalition>().apply(it) },
            partyClassification = null,
            preferences = Preferences<PartyOrCoalition, PartyOrCoalition, Int?, Int>().apply(preferences),
            map = map?.mapFrame,
            secondMap = null,
            keyTemplate = BasicResultPanel.PartyTemplate(),
            voteTemplate = VotePctOnlyTemplate,
            valueTemplate = VoteValueTemplate(VotePctOnlyTemplate),
            others = Party.OTHERS,
            title = title,
            createBarFrame = {
                BarFrameBuilder.basic(
                    barsPublisher = bars,
                    headerPublisher = header,
                    rightHeaderLabelPublisher = progress,
                    subheadPublisher = subhead,
                    notesPublisher = notes,
                    limitsPublisher = limits,
                    linesPublisher = lines,
                )
            },
        ).build()

        fun partyPct(
            current: CurrentVotes<PartyOrCoalition, Double>.() -> Unit,
            prev: (PrevVotes<PartyOrCoalition>.() -> Unit)? = null,
            winningLine: (PctBasedWinningLine.() -> Unit)? = null,
            displayLimit: (DisplayLimit<PartyOrCoalition>.() -> Unit)? = null,
            partyClassification: (PartyClassification<PartyOrCoalition>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel = VoteScreenBuilder<PartyOrCoalition, PartyOrCoalition, Double, Double, Double, BarFrameBuilder.BasicBar>(
            current = CurrentVotes<PartyOrCoalition, Double>().apply(current),
            prev = prev?.let { PrevVotes<PartyOrCoalition>().apply(it) },
            winningLine = winningLine?.let { PctBasedWinningLine().apply(it) },
            displayLimit = displayLimit?.let { DisplayLimit<PartyOrCoalition>().apply(it) },
            partyClassification = partyClassification?.let { PartyClassification<PartyOrCoalition>().apply(it) },
            preferences = null,
            map = map?.mapFrame,
            secondMap = null,
            keyTemplate = BasicResultPanel.PartyTemplate(),
            voteTemplate = VotePctOnlyTemplate,
            valueTemplate = PctTemplate,
            others = Party.OTHERS,
            title = title,
            createBarFrame = {
                BarFrameBuilder.basic(
                    barsPublisher = bars,
                    headerPublisher = header,
                    rightHeaderLabelPublisher = progress,
                    subheadPublisher = subhead,
                    notesPublisher = notes,
                    limitsPublisher = limits,
                    linesPublisher = lines,
                )
            },
        ).build()

        fun candidateVotes(
            current: CandidateCurrentVotes<Int?>.() -> Unit,
            prev: (PrevVotesNoSwing<Party>.() -> Unit)? = null,
            winningLine: (VoteBasedWinningLine.() -> Unit)? = null,
            displayLimit: (DisplayLimit<Party>.() -> Unit)? = null,
            preferences: (Preferences<Candidate, Party, Int?, Int>.() -> Unit),
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel {
            val currentVotes = CandidateCurrentVotes<Int?>().apply(current)
            return VoteScreenBuilder<Candidate, Party, Int?, Double, Double, BarFrameBuilder.BasicBar>(
                current = currentVotes,
                prev = prev?.let { PrevVotes<Party>().apply(it) },
                winningLine = winningLine?.let { VoteBasedWinningLine().apply(it) },
                displayLimit = displayLimit?.let { DisplayLimit<Party>().apply(it) },
                partyClassification = null,
                preferences = Preferences<Candidate, Party, Int?, Int>().apply(preferences),
                map = map?.mapFrame,
                secondMap = null,
                keyTemplate = BasicResultPanel.CandidateTemplate(currentVotes.incumbentMarker),
                voteTemplate = currentVotes.display.template,
                valueTemplate = VoteValueTemplate(currentVotes.display.template),
                others = Candidate.OTHERS,
                title = title,
                createBarFrame = {
                    BarFrameBuilder.basic(
                        barsPublisher = bars,
                        headerPublisher = header,
                        rightHeaderLabelPublisher = progress,
                        subheadPublisher = subhead,
                        notesPublisher = notes,
                        limitsPublisher = limits,
                        linesPublisher = lines,
                    )
                },
            ).build()
        }

        fun candidateVotes(
            current: CandidateCurrentVotes<Int?>.() -> Unit,
            prev: (PrevVotes<Party>.() -> Unit)? = null,
            winningLine: (VoteBasedWinningLine.() -> Unit)? = null,
            displayLimit: (DisplayLimit<Party>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel {
            val currentVotes = CandidateCurrentVotes<Int?>().apply(current)
            return VoteScreenBuilder<Candidate, Party, Int?, Double, Double, BarFrameBuilder.BasicBar>(
                current = currentVotes,
                prev = prev?.let { PrevVotes<Party>().apply(it) },
                winningLine = winningLine?.let { VoteBasedWinningLine().apply(it) },
                displayLimit = displayLimit?.let { DisplayLimit<Party>().apply(it) },
                partyClassification = null,
                preferences = null,
                map = map?.mapFrame,
                secondMap = null,
                keyTemplate = BasicResultPanel.CandidateTemplate(currentVotes.incumbentMarker),
                valueTemplate = VoteValueTemplate(currentVotes.display.template),
                voteTemplate = currentVotes.display.template,
                others = Candidate.OTHERS,
                title = title,
                createBarFrame = {
                    BarFrameBuilder.basic(
                        barsPublisher = bars,
                        headerPublisher = header,
                        rightHeaderLabelPublisher = progress,
                        subheadPublisher = subhead,
                        notesPublisher = notes,
                        limitsPublisher = limits,
                        linesPublisher = lines,
                    )
                },
            ).build()
        }

        fun partyOrCandidateVotes(
            current: CurrentVotes<PartyOrCandidate, Int?>.() -> Unit,
            prev: (PrevVotes<Party>.() -> Unit)? = null,
            winningLine: (VoteBasedWinningLine.() -> Unit)? = null,
            displayLimit: (DisplayLimit<Party>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel {
            val currentVotes = CurrentVotes<PartyOrCandidate, Int?>().apply(current)
            return VoteScreenBuilder<PartyOrCandidate, Party, Int?, Double, Double, BarFrameBuilder.BasicBar>(
                current = currentVotes,
                prev = prev?.let { PrevVotes<Party>().apply(it) },
                winningLine = winningLine?.let { VoteBasedWinningLine().apply(it) },
                displayLimit = displayLimit?.let { DisplayLimit<Party>().apply(it) },
                partyClassification = null,
                preferences = null,
                map = map?.mapFrame,
                secondMap = null,
                keyTemplate = BasicResultPanel.PartyOrCandidateTemplate(),
                voteTemplate = VotePctTemplate,
                valueTemplate = VoteValueTemplate(VotePctTemplate),
                others = PartyOrCandidate(Party.OTHERS),
                title = title,
                createBarFrame = {
                    BarFrameBuilder.basic(
                        barsPublisher = bars,
                        headerPublisher = header,
                        rightHeaderLabelPublisher = progress,
                        subheadPublisher = subhead,
                        notesPublisher = notes,
                        limitsPublisher = limits,
                        linesPublisher = lines,
                    )
                },
            ).build()
        }

        fun partyRangeVotes(
            current: CurrentVotes<PartyOrCoalition, ClosedRange<Double>>.() -> Unit,
            prev: (PrevVotes<PartyOrCoalition>.() -> Unit)? = null,
            winningLine: (PctBasedWinningLine.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel = VoteScreenBuilder<PartyOrCoalition, PartyOrCoalition, ClosedRange<Double>, ClosedRange<Double>, ClosedRange<Double>, BarFrameBuilder.DualBar>(
            current = CurrentVotes<PartyOrCoalition, ClosedRange<Double>>().apply(current),
            prev = prev?.let { PrevVotes<PartyOrCoalition>().apply(it) },
            winningLine = winningLine?.let { PctBasedWinningLine().apply(it) },
            displayLimit = null,
            partyClassification = null,
            preferences = null,
            map = map?.mapFrame,
            secondMap = null,
            keyTemplate = BasicResultPanel.PartyTemplate(),
            voteTemplate = VotePctOnlyTemplate,
            valueTemplate = RangeValueTemplate,
            others = Party.OTHERS,
            title = title,
            createBarFrame = {
                BarFrameBuilder.dual(
                    barsPublisher = bars,
                    headerPublisher = header,
                    rightHeaderLabelPublisher = progress,
                    subheadPublisher = subhead,
                    notesPublisher = notes,
                    limitsPublisher = limits,
                    linesPublisher = lines,
                )
            },
        ).build()

        fun partyRangeVotes(
            current: CurrentVotes<PartyOrCoalition, ClosedRange<Double>>.() -> Unit,
            prev: (PrevVotesNoSwing<PartyOrCoalition>.() -> Unit)? = null,
            winningLine: (PctBasedWinningLine.() -> Unit)? = null,
            map: AbstractMap<*>,
            secondMap: AbstractMap<*>,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel = VoteScreenBuilder<PartyOrCoalition, PartyOrCoalition, ClosedRange<Double>, ClosedRange<Double>, ClosedRange<Double>, BarFrameBuilder.DualBar>(
            current = CurrentVotes<PartyOrCoalition, ClosedRange<Double>>().apply(current),
            prev = prev?.let { PrevVotes<PartyOrCoalition>().apply(it) },
            winningLine = winningLine?.let { PctBasedWinningLine().apply(it) },
            displayLimit = null,
            partyClassification = null,
            preferences = null,
            map = map.mapFrame,
            secondMap = secondMap.mapFrame,
            keyTemplate = BasicResultPanel.PartyTemplate(),
            voteTemplate = VotePctOnlyTemplate,
            valueTemplate = RangeValueTemplate,
            others = Party.OTHERS,
            title = title,
            createBarFrame = {
                BarFrameBuilder.dual(
                    barsPublisher = bars,
                    headerPublisher = header,
                    rightHeaderLabelPublisher = progress,
                    subheadPublisher = subhead,
                    notesPublisher = notes,
                    limitsPublisher = limits,
                    linesPublisher = lines,
                )
            },
        ).build()

        fun partyRangeVotes(
            current: CurrentVotes<PartyOrCoalition, ClosedRange<Double>>.() -> Unit,
            prev: (PrevVotesNoSwing<PartyOrCoalition>.() -> Unit)? = null,
            winningLine: (PctBasedWinningLine.() -> Unit)? = null,
            preferences: (Preferences<PartyOrCoalition, PartyOrCoalition, ClosedRange<Double>, Int>.() -> Unit),
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel = VoteScreenBuilder<PartyOrCoalition, PartyOrCoalition, ClosedRange<Double>, ClosedRange<Double>, ClosedRange<Double>, BarFrameBuilder.DualBar>(
            current = CurrentVotes<PartyOrCoalition, ClosedRange<Double>>().apply(current),
            prev = prev?.let { PrevVotes<PartyOrCoalition>().apply(it) },
            winningLine = winningLine?.let { PctBasedWinningLine().apply(it) },
            displayLimit = null,
            partyClassification = null,
            preferences = Preferences<PartyOrCoalition, PartyOrCoalition, ClosedRange<Double>, Int>().apply(preferences),
            map = map?.mapFrame,
            secondMap = null,
            keyTemplate = BasicResultPanel.PartyTemplate(),
            voteTemplate = VotePctOnlyTemplate,
            valueTemplate = RangeValueTemplate,
            others = Party.OTHERS,
            title = title,
            createBarFrame = {
                BarFrameBuilder.dual(
                    barsPublisher = bars,
                    headerPublisher = header,
                    rightHeaderLabelPublisher = progress,
                    subheadPublisher = subhead,
                    notesPublisher = notes,
                    limitsPublisher = limits,
                    linesPublisher = lines,
                )
            },
        ).build()

        fun nonPartisanVotes(
            current: CurrentVotes<NonPartisanCandidate, Int?>.() -> Unit,
            prev: (NonPartisanPrevVotes.() -> Unit)? = null,
            map: SingleNonPartisanResultMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SimpleVoteViewPanel = NonPartisanVoteBuilder(
            CurrentVotes<NonPartisanCandidate, Int?>().apply(current),
            prev?.let { NonPartisanPrevVotes().apply(it) },
            map?.mapFrame,
            title,
        ).build()
    }

    interface ValueTemplate<CT, DT, BAR> {
        fun sortOrder(value: CT): Double

        fun <KT, KPT> toPct(votes: Map<out KT, CT>, party: (KT) -> KPT): Map<KPT, DT>

        fun <KT, KPT> toVotesForSwing(votes: Map<out KT, CT>, party: (KT) -> KPT): Map<KPT, Int>

        fun toDiff(curr: DT, prev: Double): DT

        fun voteCombine(a: CT, b: CT): CT

        fun pctCombine(a: DT, b: DT): DT

        val zero: CT

        val zeroPct: DT

        fun createBar(keyLabel: List<String>, baseColor: Color, value: CT, total: CT, forcedTotal: CT, numBars: Int, forceSingleLine: Boolean, shape: Shape?): BAR
        fun createPreferencesBar(keyLabel: List<String>, baseColor: Color, value: CT, total: CT, forcedTotal: CT, numBars: Int, shape: Shape?): BAR
        fun createDiffBar(keyLabel: String, baseColor: Color, value: DT): BAR
        fun createPrevBar(keyLabel: String, baseColor: Color, value: Double): BAR

        fun createAltTextBar(keyLabel: String, value: CT?, total: CT, diff: DT?, numBars: Int, symbol: String?, result: String?): String

        fun createPreferenceAltTextBar(keyLabel: String, value: CT?, total: CT, diff: DT?, numBars: Int, symbol: String?, result: String?): String
    }

    private class VoteValueTemplate(val voteTemplate: VoteTemplate) : ValueTemplate<Int?, Double, BarFrameBuilder.BasicBar> {
        override fun sortOrder(value: Int?): Double = (value ?: 0).toDouble()

        override fun <KT, KPT> toPct(votes: Map<out KT, Int?>, party: (KT) -> KPT): Map<KPT, Double> = toVotesForSwing(votes, party).run {
            val total = values.sum().toDouble()
            if (total == 0.0) {
                emptyMap()
            } else {
                mapValues { it.value / total }
            }
        }

        override fun <KT, KPT> toVotesForSwing(votes: Map<out KT, Int?>, party: (KT) -> KPT): Map<KPT, Int> = if (votes.values.any { it == null }) {
            emptyMap()
        } else {
            Aggregators.adjustKey(votes.mapValues { it.value!! }, party)
        }

        override fun toDiff(curr: Double, prev: Double): Double = curr - prev

        override fun voteCombine(a: Int?, b: Int?): Int? = if (a == null || b == null) {
            null
        } else {
            a + b
        }

        override fun pctCombine(a: Double, b: Double): Double = a + b

        override val zero: Int = 0

        override val zeroPct: Double = 0.0

        override fun createBar(
            keyLabel: List<String>,
            baseColor: Color,
            value: Int?,
            total: Int?,
            forcedTotal: Int?,
            numBars: Int,
            forceSingleLine: Boolean,
            shape: Shape?,
        ): BarFrameBuilder.BasicBar = createBar(
            keyLabel,
            baseColor,
            value,
            forcedTotal,
            numBars,
            total,
            forceSingleLine,
            shape,
            "UNCONTESTED",
        )

        override fun createPreferencesBar(
            keyLabel: List<String>,
            baseColor: Color,
            value: Int?,
            total: Int?,
            forcedTotal: Int?,
            numBars: Int,
            shape: Shape?,
        ): BarFrameBuilder.BasicBar = createBar(
            keyLabel,
            baseColor,
            value,
            forcedTotal,
            numBars,
            total,
            true,
            shape,
            "ELECTED",
        )

        private fun createBar(
            keyLabel: List<String>,
            baseColor: Color,
            value: Int?,
            forcedTotal: Int?,
            numBars: Int,
            total: Int?,
            forceSingleLine: Boolean,
            shape: Shape?,
            singleBarLabel: String,
        ): BarFrameBuilder.BasicBar = BarFrameBuilder.BasicBar.of(
            label = keyLabel.mapIndexed { index, s -> s to (if (index == 0) shape else null) },
            color = baseColor,
            value = ((value ?: 0).toDouble() / (forcedTotal ?: 0)).takeUnless { it.isNaN() } ?: 0.0,
            valueLabel = when {
                numBars == 1 -> listOf(singleBarLabel)
                value == null -> listOf("WAITING...")
                forcedTotal == 0 -> listOf("WAITING...")
                total == null || total == 0 -> listOf(DecimalFormat("#,##0").format(value))
                else -> voteTemplate.toBarString(
                    votes = value,
                    pct = value.toDouble() / total,
                    forceSingleLine = forceSingleLine,
                )
            },
        )

        override fun createDiffBar(keyLabel: String, baseColor: Color, value: Double): BarFrameBuilder.BasicBar = BarFrameBuilder.BasicBar.of(
            keyLabel,
            baseColor,
            value,
            DecimalFormat("+0.0%;-0.0%").format(value),
        )

        override fun createPrevBar(keyLabel: String, baseColor: Color, value: Double): BarFrameBuilder.BasicBar = BarFrameBuilder.BasicBar.of(
            keyLabel,
            baseColor,
            value,
            DecimalFormat("0.0%").format(value),
        )

        override fun createAltTextBar(
            keyLabel: String,
            value: Int?,
            total: Int?,
            diff: Double?,
            numBars: Int,
            symbol: String?,
            result: String?,
        ): String = "$keyLabel: " + when {
            numBars == 1 -> "UNCONTESTED"
            value == null && diff != null -> "- (" + DecimalFormat("0.0%").format(diff) + ")"
            value == null -> "WAITING..."
            value == 0 && total == 0 -> "WAITING..."
            total == null || total == 0 -> DecimalFormat("#,##0").format(value)
            else -> voteTemplate.toAltTextString(
                votes = value,
                pct = value.toDouble() / total,
                diffPct = diff,
                symbols = symbol ?: "",
            )
        } + (if (result == null) "" else " $result")

        override fun createPreferenceAltTextBar(
            keyLabel: String,
            value: Int?,
            total: Int?,
            diff: Double?,
            numBars: Int,
            symbol: String?,
            result: String?,
        ): String = "$keyLabel: " + when {
            numBars == 1 -> "ELECTED"
            value == null && diff != null -> "- (" + DecimalFormat("0.0%").format(diff) + ")"
            value == null -> "WAITING..."
            value == 0 && total == 0 -> "WAITING..."
            total == null || total == 0 -> DecimalFormat("#,##0").format(value)
            else -> voteTemplate.toAltTextString(
                votes = value,
                pct = value.toDouble() / total,
                diffPct = diff,
                symbols = symbol ?: "",
            )
        } + (if (result == null || numBars == 1) "" else " $result")
    }

    private object RangeValueTemplate : ValueTemplate<ClosedRange<Double>, ClosedRange<Double>, BarFrameBuilder.DualBar> {
        override fun sortOrder(value: ClosedRange<Double>): Double = value.start + value.endInclusive

        override fun <KT, KPT> toPct(votes: Map<out KT, ClosedRange<Double>>, party: (KT) -> KPT): Map<KPT, ClosedRange<Double>> = Aggregators.adjustKey(votes, party, this::pctCombine)

        override fun <KT, KPT> toVotesForSwing(
            votes: Map<out KT, ClosedRange<Double>>,
            party: (KT) -> KPT,
        ): Map<KPT, Int> = toPct(votes, party).mapValues { (1_000_000 * (it.value.start + it.value.endInclusive) / 2).roundToInt() }

        override fun toDiff(curr: ClosedRange<Double>, prev: Double): ClosedRange<Double> = (curr.start - prev)..(curr.endInclusive - prev)

        override fun voteCombine(a: ClosedRange<Double>, b: ClosedRange<Double>): ClosedRange<Double> = pctCombine(a, b)

        override fun pctCombine(a: ClosedRange<Double>, b: ClosedRange<Double>): ClosedRange<Double> = (a.start + b.start)..(a.endInclusive + b.endInclusive)

        override val zero: ClosedRange<Double> = 0.0..0.0

        override val zeroPct: ClosedRange<Double> = zero

        override fun createBar(
            keyLabel: List<String>,
            baseColor: Color,
            value: ClosedRange<Double>,
            total: ClosedRange<Double>,
            forcedTotal: ClosedRange<Double>,
            numBars: Int,
            forceSingleLine: Boolean,
            shape: Shape?,
        ): BarFrameBuilder.DualBar = BarFrameBuilder.DualBar.of(
            keyLabel.mapIndexed { index, s -> s to (if (index == 0) shape else null) },
            baseColor,
            value.start,
            value.endInclusive,
            listOf(
                DecimalFormat("0.0").format(100 * value.start) +
                    "-" +
                    DecimalFormat("0.0").format(100 * value.endInclusive) +
                    "%",
            ),
        )

        override fun createPreferencesBar(
            keyLabel: List<String>,
            baseColor: Color,
            value: ClosedRange<Double>,
            total: ClosedRange<Double>,
            forcedTotal: ClosedRange<Double>,
            numBars: Int,
            shape: Shape?,
        ): BarFrameBuilder.DualBar = createBar(
            keyLabel,
            baseColor,
            value,
            total,
            forcedTotal,
            numBars,
            true,
            shape,
        )

        override fun createDiffBar(
            keyLabel: String,
            baseColor: Color,
            value: ClosedRange<Double>,
        ): BarFrameBuilder.DualBar = BarFrameBuilder.DualBar.of(
            keyLabel,
            baseColor,
            value.start,
            value.endInclusive,
            "(" +
                DecimalFormat("+0.0;-0.0").format(100.0 * (value.start)) +
                ")-(" +
                DecimalFormat("+0.0;-0.0").format(100.0 * (value.endInclusive)) +
                ")%",
        )

        override fun createPrevBar(keyLabel: String, baseColor: Color, value: Double): BarFrameBuilder.DualBar = BarFrameBuilder.DualBar.of(
            keyLabel,
            baseColor,
            value,
            value,
            DecimalFormat("0.0%").format(value),
        )

        override fun createAltTextBar(
            keyLabel: String,
            value: ClosedRange<Double>?,
            total: ClosedRange<Double>,
            diff: ClosedRange<Double>?,
            numBars: Int,
            symbol: String?,
            result: String?,
        ): String = "$keyLabel: " + when (value) {
            null -> "WAITING..."
            else -> DecimalFormat("0.0").format(value.start * 100) + "-" + DecimalFormat("0.0%").format(value.endInclusive)
        } + when (diff) {
            null -> symbol?.let { " ($it)" } ?: ""
            else -> " ((" + DecimalFormat("+0.0;-0.0").format(diff.start * 100) + ")-(" + DecimalFormat("+0.0;-0.0").format(diff.endInclusive * 100) + ")%)"
        } + (result ?: "")

        override fun createPreferenceAltTextBar(
            keyLabel: String,
            value: ClosedRange<Double>?,
            total: ClosedRange<Double>,
            diff: ClosedRange<Double>?,
            numBars: Int,
            symbol: String?,
            result: String?,
        ): String = createAltTextBar(
            keyLabel,
            value,
            total,
            diff,
            numBars,
            symbol,
            result,
        )
    }

    interface VoteTemplate {
        fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): List<String>

        fun toAltTextString(votes: Int, pct: Double, diffPct: Double?, symbols: String): String
    }

    private object VotePctTemplate : VoteTemplate {
        override fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): List<String> = if (forceSingleLine) {
            listOf("${THOUSANDS_FORMAT.format(votes.toLong())} (${PCT_FORMAT.format(pct)})")
        } else {
            listOf(THOUSANDS_FORMAT.format(votes.toLong()), PCT_FORMAT.format(pct))
        }

        override fun toAltTextString(votes: Int, pct: Double, diffPct: Double?, symbols: String): String = (
            THOUSANDS_FORMAT.format(votes.toLong()) +
                " (" +
                PCT_FORMAT.format(pct) +
                (if (diffPct == null && symbols.isEmpty()) "" else ", ${if (diffPct == null) "" else PCT_DIFF_FORMAT.format(diffPct)}$symbols") +
                ")"
            )
    }

    private object VotePctOnlyTemplate : VoteTemplate {
        override fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): List<String> = listOf(PCT_FORMAT.format(pct))

        override fun toAltTextString(votes: Int, pct: Double, diffPct: Double?, symbols: String): String = PCT_FORMAT.format(pct) +
            (
                if (diffPct == null) {
                    (if (symbols.isEmpty()) "" else " ($symbols)")
                } else {
                    (" (" + PCT_DIFF_FORMAT.format(diffPct) + symbols + ")")
                }
                )
    }

    private object PctTemplate : ValueTemplate<Double, Double, BarFrameBuilder.BasicBar> {
        override fun sortOrder(value: Double): Double = value

        override fun <KT, KPT> toPct(votes: Map<out KT, Double>, party: (KT) -> KPT): Map<KPT, Double> = votes.entries.groupingBy { party(it.key) }.fold(0.0) { a, e -> a + e.value }

        override fun <KT, KPT> toVotesForSwing(votes: Map<out KT, Double>, party: (KT) -> KPT): Map<KPT, Int> = votes.entries.groupingBy { party(it.key) }.fold(0) { a, e -> a + (e.value * 1000).roundToInt() }

        override fun toDiff(curr: Double, prev: Double): Double = curr - prev

        override fun voteCombine(a: Double, b: Double): Double = a + b

        override fun pctCombine(a: Double, b: Double): Double = a + b

        override val zero: Double = 0.0

        override val zeroPct: Double = 0.0

        override fun createBar(
            keyLabel: List<String>,
            baseColor: Color,
            value: Double,
            total: Double,
            forcedTotal: Double,
            numBars: Int,
            forceSingleLine: Boolean,
            shape: Shape?,
        ): BarFrameBuilder.BasicBar = createBar(
            keyLabel,
            baseColor,
            value,
            numBars,
            forceSingleLine,
            shape,
            "UNCONTESTED",
        )

        override fun createPreferencesBar(
            keyLabel: List<String>,
            baseColor: Color,
            value: Double,
            total: Double,
            forcedTotal: Double,
            numBars: Int,
            shape: Shape?,
        ): BarFrameBuilder.BasicBar = createBar(
            keyLabel,
            baseColor,
            value,
            numBars,
            true,
            shape,
            "ELECTED",
        )

        private fun createBar(
            keyLabel: List<String>,
            baseColor: Color,
            value: Double,
            numBars: Int,
            forceSingleLine: Boolean,
            shape: Shape?,
            singleBarLabel: String,
        ): BarFrameBuilder.BasicBar = BarFrameBuilder.BasicBar.of(
            label = keyLabel.mapIndexed { index, s -> s to (if (index == 0) shape else null) },
            color = baseColor,
            value = value.takeUnless { it.isNaN() } ?: 0.0,
            valueLabel = when {
                numBars == 1 -> listOf(singleBarLabel)
                else -> VotePctOnlyTemplate.toBarString(
                    votes = 0,
                    pct = value,
                    forceSingleLine = forceSingleLine,
                )
            },
        )

        override fun createDiffBar(keyLabel: String, baseColor: Color, value: Double): BarFrameBuilder.BasicBar = BarFrameBuilder.BasicBar.of(
            keyLabel,
            baseColor,
            value,
            DecimalFormat("+0.0%;-0.0%").format(value),
        )

        override fun createPrevBar(keyLabel: String, baseColor: Color, value: Double): BarFrameBuilder.BasicBar = BarFrameBuilder.BasicBar.of(
            keyLabel,
            baseColor,
            value,
            DecimalFormat("0.0%").format(value),
        )

        override fun createAltTextBar(
            keyLabel: String,
            value: Double?,
            total: Double,
            diff: Double?,
            numBars: Int,
            symbol: String?,
            result: String?,
        ): String = "$keyLabel: " + when {
            numBars == 1 -> "UNCONTESTED"
            value == null && diff != null -> "- (" + DecimalFormat("0.0%").format(diff) + ")"
            value == null -> "WAITING..."
            else -> VotePctOnlyTemplate.toAltTextString(
                votes = 0,
                pct = value,
                diffPct = diff,
                symbols = symbol ?: "",
            )
        } + (if (result == null) "" else " $result")

        override fun createPreferenceAltTextBar(
            keyLabel: String,
            value: Double?,
            total: Double,
            diff: Double?,
            numBars: Int,
            symbol: String?,
            result: String?,
        ): String = "$keyLabel: " + when {
            numBars == 1 -> "ELECTED"
            value == null && diff != null -> "- (" + DecimalFormat("0.0%").format(diff) + ")"
            value == null -> "WAITING..."
            else -> VotePctOnlyTemplate.toAltTextString(
                votes = 0,
                pct = value,
                diffPct = diff,
                symbols = symbol ?: "",
            )
        } + (if (result == null || numBars == 1) "" else " $result")
    }

    sealed class AbstractCurrentVotes<KT : Any, CT> {
        lateinit var votes: Flow.Publisher<out Map<out KT, CT>>
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        open var winner: Flow.Publisher<out KT?>? = null
        var pctReporting: Flow.Publisher<Double>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
        var notes: Flow.Publisher<out String?>? = null

        internal open val result: Flow.Publisher<ElectionResult<KT>>
            get() {
                return if (winner == null) {
                    ElectionResult<KT>().asOneTimePublisher()
                } else {
                    winner!!.map { ElectionResult(winners = if (it == null) emptySet() else setOf(it)) }
                }
            }

        internal fun <KPT : PartyOrCoalition, PCT : Any> diff(
            prevVotes: Flow.Publisher<out Map<out KPT, Int>>,
            partyChanges: Flow.Publisher<out Map<out KPT, KPT>>?,
            toPct: (Map<out KT, CT>) -> Map<KPT, PCT>,
            toDiff: (PCT, Double) -> PCT,
            pctCombine: (PCT, PCT) -> PCT,
            zero: PCT,
            isRunoff: Boolean,
            maskIfWinnerNotRunningAgain: Boolean,
            displayLimit: DisplayLimit<KPT>?,
        ): Flow.Publisher<Map<KPT, PCT>> {
            @Suppress("UNCHECKED_CAST")
            val others = Party.OTHERS as KPT
            return votes.merge(
                if (partyChanges == null) prevVotes else Aggregators.partyChanges(prevVotes, partyChanges),
            ) { curr, prev ->
                val currPct = toPct(curr).run {
                    if (displayLimit == null) {
                        this
                    } else {
                        Aggregators.topAndOthers(
                            result = this,
                            limit = displayLimit.limit,
                            others = others,
                            mustInclude = displayLimit.mandatoryParties,
                            sortOrder = { it: PCT -> it as Double },
                            sum = pctCombine,
                        )
                    }
                }
                if (currPct.isEmpty()) return@merge emptyMap<KPT, PCT>()
                val prevPct = Aggregators.toPct(prev)
                if (isRunoff && currPct.keys != prevPct.keys) {
                    return@merge emptyMap()
                }
                if (maskIfWinnerNotRunningAgain) {
                    val prevWinner = prev.maxByOrNull { it.value }?.key
                    if (prevWinner == null || !currPct.containsKey(prevWinner)) {
                        return@merge emptyMap()
                    }
                }
                val prevContainsOthers = prev.containsKey(others)
                val ret = currPct
                    .filterKeys { !prevContainsOthers || prevPct.containsKey(it) }
                    .mapValues { toDiff(it.value, prevPct[it.key] ?: 0.0) }.toMutableMap()
                val missingPrevKeys = prevPct.filterKeys { !ret.containsKey(it) }
                if (missingPrevKeys.isNotEmpty()) {
                    ret.merge(
                        others,
                        toDiff(zero, missingPrevKeys.values.sum()),
                        pctCombine,
                    )
                }
                val missingCurrKeys = currPct.filterKeys { !ret.containsKey(it) }
                if (missingCurrKeys.isNotEmpty()) {
                    ret.merge(
                        others,
                        missingCurrKeys.values.reduce(pctCombine),
                        pctCombine,
                    )
                }
                ret.toMap()
            }
        }
    }

    class CurrentVotes<KT : Any, CT> internal constructor() : AbstractCurrentVotes<KT, CT>()

    class CandidateCurrentVotes<CT> internal constructor() : AbstractCurrentVotes<Candidate, CT>() {
        var incumbentMarker: String? = null
        var display: Display = Display.VOTES_AND_PCT
        var winners: Flow.Publisher<out Set<Candidate>?>? = null
        var runoff: Flow.Publisher<out Set<Candidate>?>? = null

        override var winner: Flow.Publisher<out Candidate?>?
            get() {
                throw IllegalStateException("This property is write-only")
            }
            set(value) {
                winners = value?.map { w -> if (w == null) null else setOf(w) }
            }

        override val result: Flow.Publisher<ElectionResult<Candidate>>
            get() {
                return if (runoff == null) {
                    winners?.map { ElectionResult(winners = it) } ?: ElectionResult<Candidate>().asOneTimePublisher()
                } else if (winners == null) {
                    runoff!!.map { ElectionResult(runoff = it) }
                } else {
                    winners!!.merge(runoff!!) { w, r -> ElectionResult(winners = w, runoff = r) }
                }
            }
    }

    internal data class ElectionResult<KT>(val winners: Set<KT>? = null, val runoff: Set<KT>? = null)

    sealed class PrevVotesNoSwing<KPT : PartyOrCoalition> {
        lateinit var votes: Flow.Publisher<out Map<out KPT, Int>>
        lateinit var header: Flow.Publisher<out String?>
        var subhead: Flow.Publisher<out String?>? = null
        var showRaw: Flow.Publisher<Boolean>? = null
        var partyChanges: Flow.Publisher<out Map<out KPT, KPT>>? = null

        var runoff: (AlternativeChangeSubhead.() -> Unit)? = null
        var winnerNotRunningAgain: (AlternativeChangeSubhead.() -> Unit)? = null

        internal fun prevAdjusted(): Flow.Publisher<out Map<out KPT, Int>> = if (partyChanges == null) {
            votes
        } else {
            Aggregators.partyChanges(votes, partyChanges!!, Int::plus)
        }

        internal val runoffProps by lazy { runoff?.let { AlternativeChangeSubhead().apply(it) } }
        internal val winnerNotRunningAgainProps by lazy { winnerNotRunningAgain?.let { AlternativeChangeSubhead().apply(it) } }
    }

    class PrevVotes<KPT : PartyOrCoalition> internal constructor() : PrevVotesNoSwing<KPT>() {
        var swing: (Swing<KPT>.() -> Unit)? = null
        var notes: Flow.Publisher<out String?>? = null

        internal val swingProps by lazy { swing?.let { Swing<KPT>().apply(it) } }
    }

    class Swing<KPT : PartyOrCoalition> internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var partyOrder: List<KPT>
        var range: Flow.Publisher<Double>? = null
    }

    sealed class WinningLine {
        private var show: Flow.Publisher<out Boolean>? = null
        private val lines = LinkedList<Line>()

        data class LineLevel internal constructor(val pct: Double?, val votes: Int?)

        inner class Line internal constructor(val pct: Flow.Publisher<Double>?, val votes: Flow.Publisher<Int>?) {
            lateinit var display: Flow.Publisher<String>

            operator fun invoke(display: LineLevel.() -> String) {
                this.display = when {
                    pct != null && votes != null -> pct.merge(votes) { p, v -> display(LineLevel(p, v)) }
                    pct != null -> pct.map { display(LineLevel(it, null)) }
                    votes != null -> votes.map { display(LineLevel(null, it)) }
                    else -> display(LineLevel(null, null)).asOneTimePublisher()
                }
                lines.add(this)
            }

            internal fun line(pctReporting: Flow.Publisher<Double>?, totalVotes: Flow.Publisher<Int?>?): Flow.Publisher<Pair<Double, String>> {
                val votesPct = if (totalVotes == null) {
                    0.5.asOneTimePublisher()
                } else {
                    this.votes?.merge(totalVotes) { v, t -> if (t == null || t == 0) 0.5 else v.toDouble() / t } ?: 0.5.asOneTimePublisher()
                }
                val pct = when {
                    this.pct != null && this.votes != null -> pct.merge(votesPct) { a, b -> max(a, b) }
                    this.pct != null -> pct
                    this.votes != null -> votesPct
                    else -> 0.5.asOneTimePublisher()
                }
                return (pctReporting?.merge(pct) { reporting, level -> level / reporting.coerceAtLeast(1e-6) } ?: pct)
                    .merge(display) { l, d -> l to d }
            }

            infix fun and(other: Line): Line = Line(
                when {
                    this.pct == null -> other.pct
                    other.pct == null -> this.pct
                    else -> this.pct.merge(other.pct) { a, b -> max(a, b) }
                },
                when {
                    this.votes == null -> other.votes
                    other.votes == null -> this.votes
                    else -> this.votes.merge(other.votes) { a, b -> max(a, b) }
                },
            )
        }

        fun show(show: Flow.Publisher<out Boolean>?) {
            this.show = show
        }

        val majority: Line get() {
            return percentage(0.5.asOneTimePublisher())
        }

        fun majority(display: LineLevel.() -> String) {
            majority.invoke(display)
        }

        fun percentage(pct: Flow.Publisher<Double>): Line = Line(pct, null)

        fun percentage(pct: Flow.Publisher<Double>, display: LineLevel.() -> String) {
            percentage(pct).invoke(display)
        }

        internal fun lines(pctReporting: Flow.Publisher<Double>?, totalVotes: Flow.Publisher<Int?>?): BarFrameBuilder.Lines<*> = BarFrameBuilder.Lines.of(linesRaw(pctReporting, totalVotes), { second }, { first })

        internal fun maxPct(pctReporting: Flow.Publisher<Double>?, totalVotes: Flow.Publisher<Int?>?): Flow.Publisher<Double?> = linesRaw(pctReporting, totalVotes).map { l -> l.maxOfOrNull { it.first } }

        private fun linesRaw(
            pctReporting: Flow.Publisher<Double>?,
            totalVotes: Flow.Publisher<Int?>?,
        ) = this.lines.map { it.line(pctReporting, totalVotes) }.combine()
            .run {
                if (show == null) {
                    this
                } else {
                    merge(show!!) { l, s -> l.filter { s } }
                }
            }

        internal fun altText(): Flow.Publisher<out String?> {
            val display = lines.map { it.display }.combine().map { it.joinToString("\n") }
            return if (show == null) {
                display
            } else {
                show!!.merge(display) { maj, disp ->
                    if (maj) {
                        disp
                    } else {
                        null
                    }
                }
            }
        }
    }

    class PctBasedWinningLine internal constructor() : WinningLine()

    class VoteBasedWinningLine internal constructor() : WinningLine() {
        fun votes(votes: Flow.Publisher<Int>): Line = Line(null, votes)

        fun votes(votes: Flow.Publisher<Int>, display: LineLevel.() -> String) {
            votes(votes).invoke(display)
        }
    }

    class DisplayLimit<KPT> internal constructor() {
        var limit: Int = Int.MAX_VALUE
        var mandatoryParties: Collection<KPT> = emptySet()
    }

    class PartyClassification<KPT : PartyOrCoalition> internal constructor() {
        lateinit var classification: (KPT) -> KPT
        lateinit var header: Flow.Publisher<out String?>
    }

    class AlternativeChangeSubhead internal constructor() {
        lateinit var subhead: Flow.Publisher<out String?>
    }

    class Preferences<KT : Any, KPT : PartyOrCoalition, CT, PT : Any> internal constructor() {
        class Current<KT : Any, CT> internal constructor() {
            lateinit var votes: Flow.Publisher<out Map<out KT, CT>>
            lateinit var header: Flow.Publisher<out String?>
            lateinit var subhead: Flow.Publisher<out String?>
            var pctReporting: Flow.Publisher<Double>? = null
            var progressLabel: Flow.Publisher<out String?>? = null
        }
        class Prev<KPT : PartyOrCoalition, PT : Any> internal constructor() {
            lateinit var votes: Flow.Publisher<out Map<out KPT, PT>>
            lateinit var swing: (Swing<KPT>.() -> Unit)

            internal val swingProps by lazy { swing.let { Swing<KPT>().apply(it) } }
        }

        lateinit var current: Current<KT, CT>.() -> Unit
        var prev: (Prev<KPT, PT>.() -> Unit)? = null

        internal val currentProps by lazy { Current<KT, CT>().apply(current) }
        internal val prevProps by lazy { prev?.let { Prev<KPT, PT>().apply(it) } }
    }

    private class VoteScreenBuilder<KT : CanOverrideSortOrder, KPT : PartyOrCoalition, CT, CPT : Any, DT : Any, BAR>(
        private val current: AbstractCurrentVotes<out KT, CT>,
        private val prev: PrevVotes<KPT>?,
        private val winningLine: WinningLine?,
        private val displayLimit: DisplayLimit<KPT>?,
        private val partyClassification: PartyClassification<KPT>?,
        private val preferences: Preferences<KT, KPT, CT, Int>?,
        private val map: MapFrame?,
        private val secondMap: MapFrame?,
        private val keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
        private val voteTemplate: VoteTemplate,
        private val valueTemplate: ValueTemplate<CT, DT, BAR>,
        private val others: KT,
        private val createBarFrame: BarFrameArgs<BAR>.() -> BarFrame,
        private val title: Flow.Publisher<out String?>,
    ) {

        private val filteredPrev: Flow.Publisher<out Map<out KPT, Int>>? =
            prev?.let { _ ->
                val prev = this.prev.prevAdjusted()
                if (this.prev.runoffProps != null) {
                    current.votes.merge(prev) { c, p ->
                        if (c.keys.map { keyTemplate.toParty(it) }.toSet() == p.keys) {
                            p
                        } else {
                            emptyMap()
                        }
                    }
                } else if (this.prev.winnerNotRunningAgainProps != null) {
                    current.votes.merge(prev) { c, p ->
                        val winner = p.entries.maxByOrNull { it.value.toDouble() } ?: return@merge p
                        if (c.keys.map { keyTemplate.toParty(it) }.contains(winner.key)) {
                            p
                        } else {
                            emptyMap()
                        }
                    }
                } else {
                    prev
                }
            }

        private val filteredChangeSubhead: Flow.Publisher<out String?>? =
            prev?.let { _ ->
                val runoffSubhead = this.prev.runoffProps?.subhead
                val winnerNotRunningAgain = this.prev.winnerNotRunningAgainProps?.subhead
                val changeSubhead = this.prev.subhead ?: null.asOneTimePublisher()
                val prev = this.prev.prevAdjusted()
                if (runoffSubhead != null) {
                    current.votes.merge(prev) { c, p ->
                        c.keys.map { keyTemplate.toParty(it) }.toSet() == p.keys
                    }.merge(runoffSubhead) { sameParties, subhead -> if (sameParties) null else subhead }
                        .let { subhead ->
                            changeSubhead.merge(subhead) { c, s ->
                                if (c == null) {
                                    s
                                } else if (s == null) {
                                    c
                                } else {
                                    "$c / $s"
                                }
                            }
                        }
                } else if (winnerNotRunningAgain != null) {
                    current.votes.merge(prev) { c, p ->
                        val winner = p.entries.maxByOrNull { it.value.toDouble() } ?: return@merge true
                        c.keys.map { keyTemplate.toParty(it) }.contains(winner.key)
                    }.merge(winnerNotRunningAgain) { winnerRunningAgain, subhead -> if (winnerRunningAgain) null else subhead }
                        .let { subhead ->
                            changeSubhead.merge(subhead) { c, s ->
                                if (c == null) {
                                    s
                                } else if (s == null) {
                                    c
                                } else {
                                    "$c / $s"
                                }
                            }
                        }
                } else {
                    changeSubhead
                }
            }

        fun build(): SimpleVoteViewPanel = SimpleVoteViewPanel(
            title,
            createFrame(),
            if (partyClassification == null) createPreferenceFrame() else createClassificationFrame(),
            createDiffFrame(),
            if (secondMap == null) createSwingFrame() else map,
            secondMap ?: map,
            createAltText(),
        )

        private val results: Flow.Publisher<out ElectionResult<out KT>> = current.result

        private enum class CandidateResult {
            WINNER,
            RUNOFF,
        }

        private data class Entry<K, V>(val key: K, val value: V, val result: CandidateResult?)

        private val mandatoryKeys: Flow.Publisher<Set<KT>> =
            current.votes.merge(results) { c, r ->
                sequenceOf(
                    c.keys.filter { displayLimit?.mandatoryParties?.contains(keyTemplate.toParty(it)) ?: true },
                    r.winners,
                    r.runoff,
                ).filterNotNull().flatten().toSet()
            }

        private val filteredCurr: Flow.Publisher<Map<out KT, CT>> =
            current.votes.merge(mandatoryKeys) { c, k ->
                if (displayLimit == null) {
                    c
                } else {
                    Aggregators.topAndOthers(c, displayLimit.limit, others, k, sortOrder = valueTemplate::sortOrder, sum = valueTemplate::voteCombine)
                }
            }

        private val currEntries: Flow.Publisher<List<Entry<KT, CT>>> =
            filteredCurr.merge(results) { c, r ->
                c.entries
                    .sortedByDescending { (k, v) -> k.overrideSortOrder?.toDouble() ?: valueTemplate.sortOrder(v) }
                    .map { (k, v) ->
                        val result = when {
                            (r.winners ?: emptySet()).contains(k) -> CandidateResult.WINNER
                            (r.runoff ?: emptySet()).contains(k) -> CandidateResult.RUNOFF
                            else -> null
                        }
                        Entry(k, v, result)
                    }
            }

        private val diff: Flow.Publisher<out Map<out KPT, DT>>? =
            if (prev == null) {
                null
            } else {
                this.current.diff(
                    prev.votes,
                    partyChanges = prev.partyChanges,
                    toPct = { votes -> valueTemplate.toPct(votes, keyTemplate::toParty) },
                    toDiff = valueTemplate::toDiff,
                    pctCombine = valueTemplate::pctCombine,
                    zero = valueTemplate.zeroPct,
                    isRunoff = prev.runoff != null,
                    maskIfWinnerNotRunningAgain = prev.winnerNotRunningAgain != null,
                    displayLimit = displayLimit,
                )
            }

        private fun createResultFrame(
            header: Flow.Publisher<out String?>,
            progress: Flow.Publisher<out String?>?,
            subhead: Flow.Publisher<out String?>,
            notes: Flow.Publisher<out String?>?,
            max: Flow.Publisher<out Double?>,
            lines: BarFrameBuilder.Lines<*>?,
        ): BarFrame {
            val doubleLineBarLimit = if (preferences == null) 10 else 0
            return createBarFrame(
                BarFrameArgs(
                    bars = currEntries.map {
                        val total = it.map { (_, votes) -> votes }.reduceOrNull(valueTemplate::voteCombine) ?: valueTemplate.zero
                        val forcedTotal = it.map { (_, votes) -> votes ?: valueTemplate.zero }.reduceOrNull(valueTemplate::voteCombine) ?: valueTemplate.zero
                        val forceSingleLine = it.size > doubleLineBarLimit
                        it.map { e ->
                            valueTemplate.createBar(
                                keyTemplate.toMainBarHeader(e.key, forceSingleLine),
                                keyTemplate.toParty(e.key).color,
                                e.value,
                                total,
                                forcedTotal,
                                it.size,
                                forceSingleLine,
                                keyTemplate.incumbentShape(e.key, forceSingleLine).combineHorizontal(
                                    when (e.result) {
                                        CandidateResult.WINNER -> keyTemplate.winnerShape(it.size > doubleLineBarLimit)
                                        CandidateResult.RUNOFF -> keyTemplate.runoffShape(it.size > doubleLineBarLimit)
                                        null -> null
                                    },
                                ),
                            )
                        }
                    },
                    header = header,
                    progress = progress,
                    subhead = subhead,
                    notes = notes,
                    limits = max.map { BarFrameBuilder.Limit(max = it) },
                    lines = lines,
                ),
            )
        }

        private fun createFrame(): BarFrame {
            val totalVotes: Flow.Publisher<Int?> = current.votes.map { v ->
                v.values.fold(valueTemplate.zero, valueTemplate::voteCombine).let { if (it is Int) it else null }
            }
            val max: Flow.Publisher<out Double> =
                current.pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher()
            return createResultFrame(
                header = current.header,
                progress = current.progressLabel,
                subhead = current.subhead,
                notes = current.notes,
                max = max.let { m ->
                    if (winningLine == null) {
                        m
                    } else {
                        m.merge(winningLine.maxPct(current.pctReporting, totalVotes)) { a, b -> if (b == null) a else max(a, b * 1.2) }
                    }
                },
                lines = winningLine?.lines(current.pctReporting, totalVotes),
            )
        }

        private val currPreferencesEntries: Flow.Publisher<List<Entry<KT, CT>>>? =
            preferences?.currentProps?.votes?.merge(results) { c, r ->
                c.entries
                    .sortedByDescending { (k, v) -> k.overrideSortOrder?.toDouble() ?: valueTemplate.sortOrder(v) }
                    .map { (k, v) ->
                        val result = when {
                            (r.winners ?: emptySet()).contains(k) -> CandidateResult.WINNER
                            (r.runoff ?: emptySet()).contains(k) -> CandidateResult.RUNOFF
                            else -> null
                        }
                        Entry(k, v, result)
                    }
            }

        private fun createPreferenceFrame(): BarFrame? {
            return createBarFrame(
                BarFrameArgs(
                    bars = (currPreferencesEntries ?: return null).map {
                        val total = it.map { (_, votes) -> votes }.reduceOrNull(valueTemplate::voteCombine) ?: valueTemplate.zero
                        val forcedTotal = it.map { (_, votes) -> votes ?: valueTemplate.zero }.reduceOrNull(valueTemplate::voteCombine) ?: valueTemplate.zero
                        it.map { e ->
                            valueTemplate.createPreferencesBar(
                                keyTemplate.toMainBarHeader(e.key, true),
                                keyTemplate.toParty(e.key).color,
                                e.value,
                                total,
                                forcedTotal,
                                it.size,
                                keyTemplate.incumbentShape(e.key, true).combineHorizontal(
                                    when (e.result) {
                                        CandidateResult.WINNER -> keyTemplate.winnerShape(true)
                                        CandidateResult.RUNOFF -> keyTemplate.runoffShape(true)
                                        null -> null
                                    },
                                ),
                            )
                        }
                    },
                    header = preferences!!.currentProps.header,
                    progress = preferences.currentProps.progressLabel,
                    subhead = preferences.currentProps.subhead,
                    limits = (preferences.currentProps.pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher()).map { BarFrameBuilder.Limit(max = it) },
                    lines = BarFrameBuilder.Lines.of(preferences.currentProps.pctReporting?.map { listOf(0.5 / it.coerceAtLeast(1e-6)) } ?: listOf(0.5).asOneTimePublisher()) { "50%" },
                ),
            )
        }

        private val currClassificationEntries: Flow.Publisher<List<Entry<KPT, CT>>>? =
            partyClassification?.run {
                current.votes.map { c ->
                    c.entries
                        .groupBy({ classification(keyTemplate.toParty(it.key)) }, { it.value })
                        .mapValues { it.value.reduce { a, b -> valueTemplate.voteCombine(a, b) } }
                        .entries
                        .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: valueTemplate.sortOrder(it.value) }
                        .map { Entry(it.key, it.value, null) }
                }
            }

        private fun createClassificationFrame(): BarFrame? {
            val bars = (currClassificationEntries ?: return null).map { entries ->
                val total = entries.map { it.value }.reduceOrNull(valueTemplate::voteCombine) ?: valueTemplate.zero
                val forcedTotal = entries.map { it.value ?: valueTemplate.zero }.reduceOrNull(valueTemplate::voteCombine) ?: valueTemplate.zero
                entries.map {
                    valueTemplate.createBar(
                        listOf(it.key.name.uppercase()),
                        it.key.color,
                        it.value,
                        total,
                        forcedTotal,
                        entries.size,
                        true,
                        null,
                    )
                }
            }
            return createBarFrame(
                BarFrameArgs(
                    bars = bars,
                    header = partyClassification!!.header,
                    limits = (current.pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher()).map { BarFrameBuilder.Limit(max = it) },
                    lines = winningLine?.lines(current.pctReporting, null),
                ),
            )
        }

        private data class CurrDiffEntry<K, P, C, D>(val key: K?, val party: P, val curr: C?, val diff: D?, val result: CandidateResult?)

        private val diffEntries: Flow.Publisher<List<CurrDiffEntry<out KT, KPT, out CT, DT>>>? =
            diff?.merge(currEntries) { diff, curr ->
                val partiesSeen = HashSet<KPT>()
                val entries1 = curr.map { e ->
                    val party = keyTemplate.toParty(e.key)
                    CurrDiffEntry(
                        e.key,
                        party,
                        e.value,
                        if (partiesSeen.add(party)) diff[party] else null,
                        e.result,
                    )
                }
                val entries2 = diff.entries
                    .filter { e -> !partiesSeen.contains(e.key) }
                    .map { e ->
                        CurrDiffEntry(
                            null,
                            e.key,
                            null,
                            e.value,
                            null,
                        )
                    }
                entries1 + entries2
            }

        private val prevEntries: Flow.Publisher<List<Entry<KPT, Int>>>? =
            prev?.votes?.map { prev ->
                prev.entries
                    .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value.toDouble() }
                    .map { Entry(it.key, it.value, null) }
            }

        private fun createDiffFrame(): BarFrame? {
            if (prev == null) return null
            val limits = when {
                prev.showRaw != null && current.pctReporting != null -> prev.showRaw!!.merge(current.pctReporting!!) { showRaw, pct ->
                    if (showRaw) {
                        BarFrameBuilder.Limit(max = 2.0 / 3)
                    } else {
                        BarFrameBuilder.Limit(wingspan = 0.1 / pct.coerceAtLeast(1e-6))
                    }
                }
                prev.showRaw != null -> prev.showRaw!!.map { showRaw ->
                    if (showRaw) {
                        BarFrameBuilder.Limit(max = 2.0 / 3)
                    } else {
                        BarFrameBuilder.Limit(wingspan = 0.1)
                    }
                }
                current.pctReporting != null -> current.pctReporting!!.map { pct -> BarFrameBuilder.Limit(wingspan = 0.1 / pct.coerceAtLeast(1e-6)) }
                else -> BarFrameBuilder.Limit(wingspan = 0.1).asOneTimePublisher()
            }
            return createDiffFrameBuilder(
                prev.header,
                filteredChangeSubhead,
                limits,
            )
        }

        private fun createDiffFrameBuilder(
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>?,
            limits: Flow.Publisher<BarFrameBuilder.Limit>,
        ): BarFrame? {
            val prevBars = prevEntries?.map { entries ->
                val total = entries.sumOf { it.value }
                entries.map {
                    val pct = it.value.toDouble() / total
                    valueTemplate.createPrevBar(
                        it.key.abbreviation.uppercase(),
                        it.key.color,
                        pct,
                    )
                }
            }
            val diffBars = diffEntries?.map { entries ->
                entries.filter { it.diff != null }
                    .let { e ->
                        if (e.size <= 10) return@let e
                        val others = keyTemplate.toParty(others)
                        val take = e.filter { it.key != others }.take(9)
                        val rest = e.filter { !take.contains(it) }
                            .map { it.diff!! }
                            .reduce(valueTemplate::pctCombine)
                        take + listOf(CurrDiffEntry(null, others, null, rest, null))
                    }
                    .map {
                        valueTemplate.createDiffBar(
                            it.party.abbreviation.uppercase(),
                            it.party.color,
                            it.diff!!,
                        )
                    }
            }
            val bars = (prev ?: return null).showRaw?.compose { if (it) prevBars!! else diffBars!! } ?: diffBars!!
            return createBarFrame(
                BarFrameArgs(
                    bars = bars,
                    header = header,
                    subhead = subhead,
                    limits = limits,
                    notes = prev.notes,
                ),
            )
        }

        private fun createSwingFrame(): SwingFrame? {
            val currVotes: Flow.Publisher<out Map<out KPT, Int>>
            val prevVotes: Flow.Publisher<out Map<out KPT, Int>>
            val swing: Swing<KPT>
            if (preferences?.prevProps?.swingProps != null) {
                currVotes = preferences.currentProps.votes.map { valueTemplate.toVotesForSwing(it, keyTemplate::toParty) }
                prevVotes = preferences.prevProps!!.votes.merge(currVotes) { p, c ->
                    if (p.keys == c.keys) {
                        p
                    } else {
                        emptyMap()
                    }
                }
                swing = preferences.prevProps!!.swingProps
            } else if (prev?.swingProps != null) {
                currVotes = current.votes.map { valueTemplate.toVotesForSwing(it, keyTemplate::toParty) }
                prevVotes = filteredPrev!!
                swing = prev.swingProps!!
            } else {
                return null
            }

            return SwingFrameBuilder.prevCurr(
                prev = if (partyClassification == null) prevVotes else Aggregators.adjustKey(prevVotes, partyClassification.classification),
                curr = if (partyClassification == null) currVotes else Aggregators.adjustKey(currVotes, partyClassification.classification),
                partyOrder = swing.partyOrder,
                header = swing.header,
                range = swing.range,
            )
        }

        class BarFrameArgs<BAR>(
            val bars: Flow.Publisher<List<BAR>>,
            val header: Flow.Publisher<out String?>,
            val progress: Flow.Publisher<out String?>? = null,
            val subhead: Flow.Publisher<out String?>? = null,
            val notes: Flow.Publisher<out String?>? = null,
            val limits: Flow.Publisher<BarFrameBuilder.Limit>? = null,
            val lines: BarFrameBuilder.Lines<*>? = null,
        )

        private fun createAltText(): Flow.Publisher<String> = listOf(
            title,
            createBarAltText(),
            createPrevAltText(),
            createClassificationAltText(),
            createPreferencesAltText(),
            createSwingAltText(),
        ).combine()
            .map { list -> list.filterNotNull().joinToString("\n\n") }

        private fun createBarAltText(): Flow.Publisher<out String?> {
            val combineHeadAndSub = { h: String, s: String? -> listOfNotNull(h, s).filter { it.isNotBlank() }.joinToString(", ") }
            val header = current.run {
                header
                    .run {
                        if (progressLabel == null) {
                            this
                        } else {
                            merge(progressLabel!!) { h, p -> "$h [$p]" }
                        }
                    }
                    .merge(subhead, combineHeadAndSub)
            }
                .run {
                    if (prev == null) {
                        this
                    } else {
                        merge(
                            prev.run {
                                if (filteredChangeSubhead == null) {
                                    header
                                } else {
                                    val sub = subhead?.merge(filteredChangeSubhead) { s, f ->
                                        if (f == null) {
                                            null
                                        } else if (s.isNullOrEmpty()) {
                                            " $f"
                                        } else {
                                            ", $f"
                                        }
                                    } ?: filteredChangeSubhead.map { if (it == null) null else " $it" }
                                    header.merge(sub) { h, s -> listOfNotNull(h, s).joinToString("") }
                                }
                            }
                                .run {
                                    if (prev.showRaw == null) {
                                        this
                                    } else {
                                        merge(prev.showRaw!!) { h, r -> if (r) null else h }
                                    }
                                },
                        ) { h, c -> if (c == null) h else "$h ($c)" }
                    }
                }
            val barEntries = (diffEntries ?: currEntries.mapElements { CurrDiffEntry(it.key, keyTemplate.toParty(it.key), it.value, null, it.result) })
                .run {
                    if (prev?.showRaw == null) {
                        map { it to false }
                    } else {
                        merge(prev.showRaw!!) { e, r -> e to r }
                    }
                }
                .map { (entries, raw) ->
                    var partyDiffsAggregated = false
                    var partyDiffWithOthers = false
                    val total = entries.map {
                        it.curr ?: it.diff?.let { valueTemplate.zero }
                    }.reduceOrNull { a, b ->
                        if (a == null || b == null) {
                            null
                        } else {
                            valueTemplate.voteCombine(a, b)
                        }
                    } ?: valueTemplate.zero
                    val countByParty = entries.mapNotNull { it.key }.groupingBy { keyTemplate.toParty(it) }.eachCount()
                    entries.takeUnless { it.isEmpty() }?.joinToString("\n") { entry ->
                        val keyHead = entry.run { if (key == null) party.name.uppercase() else keyTemplate.toMainAltTextHeader(key) }
                        valueTemplate.createAltTextBar(
                            keyHead,
                            entry.curr,
                            total,
                            if (raw) null else entry.diff,
                            entries.size,
                            when {
                                entry.key != null && (countByParty[entry.party] ?: 0) > 1 && !raw && entries.any { it.party == entry.party && it.diff != null } -> {
                                    partyDiffsAggregated = true
                                    "^"
                                }
                                entry.key != null && entry.diff == null && !raw && entries.any { it.party == Party.OTHERS && it.diff != null } -> {
                                    partyDiffWithOthers = true
                                    "*"
                                }
                                else -> null
                            },
                            when (entry.result) {
                                null -> null
                                CandidateResult.WINNER -> "WINNER"
                                CandidateResult.RUNOFF -> "RUNOFF"
                            },
                        )
                    }?.plus(
                        if (partyDiffsAggregated) {
                            "\n^ AGGREGATED ACROSS CANDIDATES IN PARTY"
                        } else {
                            ""
                        },
                    )?.plus(
                        if (partyDiffWithOthers) {
                            "\n* CHANGE INCLUDED IN OTHERS"
                        } else {
                            ""
                        },
                    )
                }.run {
                    if (winningLine == null) {
                        this
                    } else {
                        merge(winningLine.altText()) { h, t -> listOfNotNull(h, t).joinToString("\n") }
                    }
                }
            return listOf(header, barEntries).combine().map { it.filterNotNull().joinToString("\n") }
        }

        private fun createClassificationAltText(): Flow.Publisher<String?> {
            return (currClassificationEntries ?: return null.asOneTimePublisher()).merge(partyClassification!!.header) { entries, header ->
                val total = entries.map { it.value }.reduceOrNull(valueTemplate::voteCombine) ?: valueTemplate.zero
                header + entries.joinToString("") {
                    "\n" + valueTemplate.createAltTextBar(
                        it.key.name.uppercase(),
                        it.value,
                        total,
                        null,
                        entries.size,
                        null,
                        null,
                    )
                }
            }
        }

        private fun createPreferencesAltText(): Flow.Publisher<String?> {
            return (currPreferencesEntries ?: return null.asOneTimePublisher()).merge(
                preferences!!.currentProps.run {
                    header
                        .run { if (progressLabel == null) this else merge(progressLabel!!) { h, p -> if (p == null) h else "$h [$p]" } }
                        .merge(subhead) { h, s -> listOfNotNull(h, s).filter { it.isNotEmpty() }.joinToString(", ") }
                },
            ) { entries, head ->
                val total = entries.map { (_, votes) -> votes }.reduceOrNull(valueTemplate::voteCombine) ?: valueTemplate.zero
                head + entries.joinToString("") {
                    "\n" + valueTemplate.createPreferenceAltTextBar(
                        keyTemplate.toMainAltTextHeader(it.key),
                        it.value,
                        total,
                        null,
                        entries.size,
                        null,
                        when (it.result) {
                            null -> null
                            CandidateResult.WINNER -> "WINNER"
                            CandidateResult.RUNOFF -> "RUNOFF"
                        },
                    )
                }
            }
        }

        private fun createPrevAltText(): Flow.Publisher<String?> {
            return (prev?.showRaw ?: return null.asOneTimePublisher()).compose { showPrevRaw ->
                if (!showPrevRaw) return@compose null.asOneTimePublisher()
                val title = createHeaderAltText(prev.header, prev.subhead, null)
                val bars = prevEntries?.map { entries ->
                    createPrevBarAltTexts(entries).joinToString("")
                } ?: return@compose null.asOneTimePublisher()
                title.merge(bars) { t, b -> t + b }
            }
        }

        private fun createPrevBarAltTexts(entries: List<Entry<KPT, Int>>): List<String> {
            val total = entries.sumOf { it.value }.toDouble()
            return entries.map { (k, v) -> "\n${k.abbreviation}: ${PCT_FORMAT.format(v / total)}" }
        }

        private fun createSwingAltText(): Flow.Publisher<String?> = createSwingFrame()?.altText?.merge((preferences?.prevProps?.swingProps ?: prev?.swingProps)?.header ?: null.asOneTimePublisher()) { bottom, header ->
            "${header?.let { "$it: " }}$bottom"
        } ?: null.asOneTimePublisher()

        private fun createHeaderAltText(header: Flow.Publisher<out String?>?, subhead: Flow.Publisher<out String?>?, progress: Flow.Publisher<out String?>?): Flow.Publisher<String?> {
            return listOf(
                header ?: null.asOneTimePublisher(),
                subhead ?: null.asOneTimePublisher(),
                progress ?: null.asOneTimePublisher(),
            ).combine().map { (head, sub, prog) ->
                if (head == null && sub == null && prog == null) return@map null
                (
                    (head ?: "") +
                        (prog?.takeIf { it.isNotEmpty() }?.let { " [$it]" } ?: "") +
                        (sub?.takeIf { it.isNotEmpty() }?.let { ", $it" } ?: "")
                    ).trim()
            }
        }
    }

    class NonPartisanPrevVotes internal constructor() {
        lateinit var votes: Flow.Publisher<out Map<out NonPartisanCandidate, Int>>
        lateinit var header: Flow.Publisher<out String?>
        var subhead: Flow.Publisher<out String?>? = null
    }

    internal class NonPartisanVoteBuilder(
        private val current: CurrentVotes<NonPartisanCandidate, Int?>,
        private val prev: NonPartisanPrevVotes?,
        private val map: MapFrame?,
        private val title: Flow.Publisher<out String?>,
    ) {
        fun build(): SimpleVoteViewPanel = SimpleVoteViewPanel(
            title,
            createResultFrame(),
            null,
            createPrevFrame(),
            null,
            map,
            createAltText(),
        )

        private fun createResultFrame(): JPanel {
            val bars = current.votes.merge(current.winner ?: null.asOneTimePublisher()) { r, w ->
                val total = if (r.values.any { it == null }) {
                    null
                } else {
                    r.values.sumOf { it!! }.toDouble()
                }
                r.entries.sortedByDescending { it.key.overrideSortOrder ?: it.value ?: 0 }
                    .map { (c, v) ->
                        BarFrameBuilder.BasicBar.of(
                            listOf(
                                c.fullName.uppercase() to (if (c == w) ImageGenerator.createTickShape() else null),
                                (c.description?.uppercase() ?: "") to null,
                            ),
                            c.color,
                            v ?: 0,
                            when {
                                r.size == 1 -> listOf("UNCONTESTED")
                                v == null || total == 0.0 -> listOf("WAITING...")
                                total == null -> listOf(DecimalFormat("#,##0").format(v))
                                else -> listOf(DecimalFormat("#,##0").format(v), DecimalFormat("0.0%").format(v / total))
                            },
                        )
                    }
            }
            return BarFrameBuilder.basic(
                barsPublisher = bars,
                headerPublisher = current.header,
                rightHeaderLabelPublisher = current.progressLabel,
                subheadPublisher = current.subhead,
                maxPublisher = current.votes.map { r -> r.values.sumOf { it ?: 0 } * 2 / 3 }
                    .merge(current.pctReporting ?: 1.0.asOneTimePublisher()) { v, p -> v / p.coerceAtLeast(1e-6) },
                notesPublisher = current.notes,
            )
        }

        private fun createPrevFrame(): JPanel? {
            val prevVotes = (this.prev ?: return null).votes
            val bars = prevVotes.map { r ->
                val total = r.values.sumOf { it }.toDouble()
                r.entries.sortedByDescending { it.key.overrideSortOrder ?: it.value }
                    .map { (c, v) ->
                        BarFrameBuilder.BasicBar.of(
                            c.surname.uppercase(),
                            c.color,
                            v,
                            if (r.size == 1) "UNCONTESTED" else DecimalFormat("0.0%").format(v / total),
                        )
                    }
            }
            return BarFrameBuilder.basic(
                barsPublisher = bars,
                headerPublisher = prev.header,
                subheadPublisher = prev.subhead,
                maxPublisher = prevVotes.map { r -> r.values.sumOf { it } * 2 / 3 },
            )
        }

        private fun createAltText(): Flow.Publisher<String> {
            val votes = current.header.merge(current.progressLabel ?: null.asOneTimePublisher()) { h, p -> if (p == null) h else listOfNotNull(h, "[$p]").joinToString(" ") }
                .merge(current.subhead) { h, s -> listOfNotNull(h, s).filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }?.joinToString(", ") }
                .merge(current.votes.merge(current.winner ?: null.asOneTimePublisher()) { r, w -> r to w }) { h, (r, w) ->
                    val total = if (r.values.any { it == null }) null else r.values.sumOf { it!! }.toDouble()
                    (if (h == null) "" else "$h\n") + r.entries.sortedByDescending { it.key.overrideSortOrder ?: it.value ?: 0 }
                        .joinToString("\n") { (c, v) ->
                            c.fullName.uppercase() +
                                (if (c.description.isNullOrEmpty()) "" else " (${c.description!!.uppercase()})") + ": " +
                                when {
                                    r.size == 1 -> "UNCONTESTED"
                                    v == null || total == 0.0 -> "WAITING..."
                                    total == null -> DecimalFormat("#,##0").format(v)
                                    else -> "${DecimalFormat("#,##0").format(v)} (${DecimalFormat("0.0%").format(v / total)})"
                                } +
                                if (c == w) " WINNER" else ""
                        }
                }
            val prev = if (prev == null) {
                null.asOneTimePublisher()
            } else {
                prev.header.merge(prev.subhead ?: null.asOneTimePublisher()) { h, s -> listOfNotNull(h, s).filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }?.joinToString(", ") }
                    .merge(prev.votes) { h, r ->
                        val total = r.values.sum().toDouble()
                        (if (h == null) "" else "$h\n") + r.entries.sortedByDescending { it.key.overrideSortOrder ?: it.value }
                            .joinToString("\n") { (c, v) ->
                                c.surname.uppercase() + ": " +
                                    (if (r.size == 1) "UNCONTESTED" else DecimalFormat("0.0%").format(v / total))
                            }
                    }
            }
            return listOf<Flow.Publisher<out String?>>(title, votes, prev).combine().map { it.filterNotNull().joinToString("\n\n") }
        }
    }
}
