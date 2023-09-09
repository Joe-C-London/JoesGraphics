package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.NonPartisanCandidateResult
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel
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
    run {
        val panel = JPanel()
        panel.layout = BasicResultLayout()
        panel.background = Color.WHITE
        panel.add(seatFrame, BasicResultLayout.MAIN)
        if (secondarySeatFrame != null) panel.add(secondarySeatFrame, BasicResultLayout.PREF)
        if (changeFrame != null) panel.add(changeFrame, BasicResultLayout.DIFF)
        if (leftSupplementaryFrame != null) panel.add(leftSupplementaryFrame, BasicResultLayout.SWING)
        if (rightSupplementaryFrame != null) panel.add(rightSupplementaryFrame, BasicResultLayout.MAP)
        panel
    },
    label,
    altText,
) {

    companion object {
        private val PCT_FORMAT = DecimalFormat("0.0%")
        private val PCT_DIFF_FORMAT = DecimalFormat("+0.0%;-0.0%")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")

        fun <P : PartyOrCoalition> partyVotes(
            votes: Flow.Publisher<out Map<out P, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<P, P, Int?, Double, Int, *> {
            @Suppress("UNCHECKED_CAST")
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                BasicResultPanel.PartyTemplate(),
                VotePctOnlyTemplate(),
                Party.OTHERS as P,
            )
        }

        fun candidateVotes(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<Candidate, Party, Int?, Double, Int, *> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                BasicResultPanel.CandidateTemplate(),
                VotePctTemplate(),
                Candidate.OTHERS,
            )
        }

        fun candidateVotesPctOnly(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<Candidate, Party, Int?, Double, Int, *> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                BasicResultPanel.CandidateTemplate(),
                VotePctOnlyTemplate(),
                Candidate.OTHERS,
            )
        }

        fun candidateVotes(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            incumbentMarker: String,
        ): VoteScreenBuilder<Candidate, Party, Int?, Double, Int, *> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                BasicResultPanel.CandidateTemplate(incumbentMarker),
                VotePctTemplate(),
                Candidate.OTHERS,
            )
        }

        fun candidateVotesPctOnly(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            incumbentMarker: String,
        ): VoteScreenBuilder<Candidate, Party, Int?, Double, Int, *> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                BasicResultPanel.CandidateTemplate(incumbentMarker),
                VotePctOnlyTemplate(),
                Candidate.OTHERS,
            )
        }

        fun <P : PartyOrCoalition> partyRangeVotes(
            votes: Flow.Publisher<out Map<P, ClosedRange<Double>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<P, P, ClosedRange<Double>, Double, Int, *> {
            @Suppress("UNCHECKED_CAST")
            return RangeVoteScreenBuilder(
                votes,
                header,
                subhead,
                BasicResultPanel.PartyTemplate(),
                VotePctOnlyTemplate(),
                Party.OTHERS as P,
            )
        }

        fun nonPartisanVotes(
            votes: Flow.Publisher<out Map<NonPartisanCandidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): NonPartisanVoteBuilder {
            return NonPartisanVoteBuilder(votes, header, subhead)
        }
    }

    interface ValueTemplate<V> {
        fun sortOrder(value: V): Double

        fun <K> combine(votes: Map<out K, V>, limit: Int, others: K, mandatory: Collection<K>): Map<out K, V>
    }

    private object VoteValueTemplate : ValueTemplate<Int?> {
        override fun sortOrder(value: Int?): Double = (value ?: 0).toDouble()

        override fun <K> combine(votes: Map<out K, Int?>, limit: Int, others: K, mandatory: Collection<K>) = Aggregators.topAndOthers(votes, limit, others, mandatory)
    }

    private object RangeValueTemplate : ValueTemplate<ClosedRange<Double>> {
        override fun sortOrder(value: ClosedRange<Double>): Double = value.start + value.endInclusive

        override fun <K> combine(
            votes: Map<out K, ClosedRange<Double>>,
            limit: Int,
            others: K,
            mandatory: Collection<K>,
        ) = votes
    }

    interface VoteTemplate {
        fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): String

        fun toAltTextString(votes: Int, pct: Double, diffPct: Double?, symbols: String): String
    }

    private class VotePctTemplate : VoteTemplate {
        override fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): String {
            return (
                THOUSANDS_FORMAT.format(votes.toLong()) +
                    (if (forceSingleLine) " (" else "\n") +
                    PCT_FORMAT.format(pct) +
                    (if (forceSingleLine) ")" else "")
                )
        }

        override fun toAltTextString(votes: Int, pct: Double, diffPct: Double?, symbols: String): String {
            return (
                THOUSANDS_FORMAT.format(votes.toLong()) +
                    " (" +
                    PCT_FORMAT.format(pct) +
                    (if (diffPct == null && symbols.isEmpty()) "" else ", ${if (diffPct == null) "" else PCT_DIFF_FORMAT.format(diffPct)}$symbols") +
                    ")"
                )
        }
    }

    private class VotePctOnlyTemplate : VoteTemplate {
        override fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): String {
            return PCT_FORMAT.format(pct)
        }

        override fun toAltTextString(votes: Int, pct: Double, diffPct: Double?, symbols: String): String {
            return PCT_FORMAT.format(pct) +
                (
                    if (diffPct == null) {
                        (if (symbols.isEmpty()) "" else " ($symbols)")
                    } else {
                        (" (" + PCT_DIFF_FORMAT.format(diffPct) + symbols + ")")
                    }
                    )
        }
    }

    abstract class VoteScreenBuilder<KT : Any, KPT : PartyOrCoalition, CT, CPT : Any, PT : Number, DT : Any> internal constructor(
        protected var current: Flow.Publisher<out Map<out KT, CT>>,
        protected var header: Flow.Publisher<out String?>,
        protected var subhead: Flow.Publisher<out String?>,
        protected val keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
        protected val voteTemplate: VoteTemplate,
        protected val valueTemplate: ValueTemplate<CT>,
        protected val others: KT,
    ) {
        protected var showMajority: Flow.Publisher<out Boolean>? = null
        protected var majorityLabel: String? = null
        protected var winner: Flow.Publisher<out KT?>? = null
        protected var runoff: Flow.Publisher<out Set<KT>?>? = null
        protected var pctReporting: Flow.Publisher<Double>? = null
        protected var notes: Flow.Publisher<out String?>? = null
        protected var limit = Int.MAX_VALUE
        protected var mandatoryParties: Set<KPT> = emptySet()
        protected var prev: Flow.Publisher<out Map<out KPT, PT>>? = null
        protected var prevRaw: Flow.Publisher<out Map<out KPT, PT>>? = null
        protected var showPrevRaw: Flow.Publisher<Boolean>? = null
        protected var changeHeader: Flow.Publisher<out String?>? = null
        protected var changeSubhead: Flow.Publisher<out String?>? = null
        protected var currPreferences: Flow.Publisher<out Map<out KT, CT>>? = null
        protected var prevPreferences: Flow.Publisher<out Map<out KPT, PT>>? = null
        protected var preferenceHeader: Flow.Publisher<out String?>? = null
        protected var preferenceSubhead: Flow.Publisher<out String?>? = null
        protected var preferencePctReporting: Flow.Publisher<out Double>? = null
        protected var swingHeader: Flow.Publisher<out String?>? = null
        protected var swingComparator: Comparator<KPT>? = null
        protected var swingRange: Flow.Publisher<Double>? = null
        protected var classificationFunc: ((KPT) -> KPT)? = null
        protected var classificationHeader: Flow.Publisher<out String?>? = null
        private var mapBuilder: MapBuilder<*>? = null
        private var secondMapBuilder: MapBuilder<*>? = null
        protected var runoffSubhead: Flow.Publisher<String>? = null
        protected var winnerNotRunningAgain: Flow.Publisher<String>? = null
        protected var progressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()
        protected var preferenceProgressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()

        protected abstract val diff: Flow.Publisher<out Map<out KPT, DT>>?

        protected val filteredPrev: Flow.Publisher<out Map<out KPT, PT>>?
            get() {
                val prev = this.prev ?: return null
                if (runoffSubhead != null) {
                    return current.merge(prev) { c, p ->
                        if (c.keys.map { keyTemplate.toParty(it) }.toSet() == p.keys) {
                            p
                        } else {
                            emptyMap()
                        }
                    }
                }
                if (winnerNotRunningAgain != null) {
                    return current.merge(prev) { c, p ->
                        val winner = p.entries.maxByOrNull { it.value.toDouble() } ?: return@merge p
                        if (c.keys.map { keyTemplate.toParty(it) }.contains(winner.key)) {
                            p
                        } else {
                            emptyMap()
                        }
                    }
                }
                return prev
            }
        protected val filteredChangeSubhead: Flow.Publisher<out String?>?
            get() {
                val runoffSubhead = this.runoffSubhead
                val winnerNotRunningAgain = this.winnerNotRunningAgain
                val changeSubhead = this.changeSubhead
                val prev = this.prev
                if (prev == null || changeSubhead == null) return changeSubhead
                if (runoffSubhead != null) {
                    return current.merge(prev) { c, p ->
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
                }
                if (winnerNotRunningAgain != null) {
                    return current.merge(prev) { c, p ->
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
                }
                return changeSubhead
            }

        fun withPrev(
            prev: Flow.Publisher<out Map<out KPT, PT>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher(),
            showPrevRaw: Flow.Publisher<Boolean> = false.asOneTimePublisher(),
            partyChanges: Flow.Publisher<Map<KPT, KPT>> = emptyMap<KPT, KPT>().asOneTimePublisher(),
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.prevRaw = prev
            this.prev = Aggregators.partyChanges(prev, partyChanges) { a, b -> prevCombine(a, b) }
            changeHeader = header
            changeSubhead = subhead
            this.showPrevRaw = showPrevRaw
            return this
        }

        protected abstract fun prevCombine(value1: PT, value2: PT): PT

        fun withPreferences(
            preferences: Flow.Publisher<out Map<out KT, CT>>,
            preferenceHeader: Flow.Publisher<out String?>,
            preferenceSubhead: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            currPreferences = preferences
            this.preferenceHeader = preferenceHeader
            this.preferenceSubhead = preferenceSubhead
            return this
        }

        fun withPrevPreferences(
            prevPreferences: Flow.Publisher<out Map<out KPT, PT>>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.prevPreferences = prevPreferences
            return this
        }

        fun withWinner(winner: Flow.Publisher<out KT?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.winner = winner
            return this
        }

        fun withRunoff(runoff: Flow.Publisher<out Set<KT>?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.runoff = runoff
            return this
        }

        fun withPctReporting(pctReporting: Flow.Publisher<Double>): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.pctReporting = pctReporting
            return this
        }

        fun withPreferencePctReporting(
            preferencePctReporting: Flow.Publisher<out Double>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.preferencePctReporting = preferencePctReporting
            return this
        }

        fun withSwing(
            comparator: Comparator<KPT>?,
            header: Flow.Publisher<out String?>,
            swingRange: Flow.Publisher<Double> = 0.1.asOneTimePublisher(),
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            swingComparator = comparator
            swingHeader = header
            this.swingRange = swingRange
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyOrCoalition?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            mapBuilder = MapBuilder.multiResult(shapes, winners.map { m -> BasicResultPanel.partyMapToResultMap(m) }, focus, headerPublisher)
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out Party?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            mapBuilder = MapBuilder.singleResult(shapes, selectedShape, leadingParty.map { party -> PartyResult.elected(party) }, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            mapBuilder = MapBuilder.multiResult(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Pair<Flow.Publisher<out List<T>?>, Flow.Publisher<out List<T>?>>,
            headerPublisher: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            mapBuilder = MapBuilder.multiResult(shapes, winners, focus.first, focus.second, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out PartyResult?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            mapBuilder = MapBuilder.singleResult(shapes, selectedShape, leadingParty, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out PartyResult?>,
            focus: Flow.Publisher<out List<T>?>,
            additionalHighlight: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            mapBuilder = MapBuilder.singleResult(shapes, selectedShape, leadingParty, focus, additionalHighlight, header)
            return this
        }

        fun <T> withSecondResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            secondMapBuilder = MapBuilder.multiResult(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withSecondResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Pair<Flow.Publisher<out List<T>?>, Flow.Publisher<out List<T>?>>,
            headerPublisher: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            secondMapBuilder = MapBuilder.multiResult(shapes, winners, focus.first, focus.second, headerPublisher)
            return this
        }

        fun withMajorityLine(
            showMajority: Flow.Publisher<out Boolean>,
            majorityLabel: String,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.showMajority = showMajority
            this.majorityLabel = majorityLabel
            return this
        }

        fun withNotes(notes: Flow.Publisher<out String?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.notes = notes
            return this
        }

        fun withLimit(limit: Int, vararg mandatoryParties: KPT): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            require(limit > 0) { "Invalid limit: $limit" }
            this.limit = limit
            this.mandatoryParties = setOf(*mandatoryParties)
            return this
        }

        fun withClassification(
            classificationFunc: (KPT) -> KPT,
            classificationHeader: Flow.Publisher<out String?>,
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.classificationFunc = classificationFunc
            this.classificationHeader = classificationHeader
            return this
        }

        fun inRunoffMode(changeSubhead: Flow.Publisher<String>): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.runoffSubhead = changeSubhead
            return this
        }

        fun whenWinnerNotRunningAgain(changeSubhead: Flow.Publisher<String>): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.winnerNotRunningAgain = changeSubhead
            return this
        }

        fun withProgressLabel(progressLabel: Flow.Publisher<out String?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.progressLabel = progressLabel
            return this
        }

        fun withPreferenceProgressLabel(progressLabel: Flow.Publisher<out String?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT, DT> {
            this.preferenceProgressLabel = progressLabel
            return this
        }

        fun build(textHeader: Flow.Publisher<out String?>): SimpleVoteViewPanel {
            return SimpleVoteViewPanel(
                textHeader,
                createFrame(),
                if (classificationHeader == null) createPreferenceFrame() else createClassificationFrame(),
                createDiffFrame(),
                if (secondMapBuilder == null) createSwingFrame() else createMapFrame(),
                if (secondMapBuilder == null) createMapFrame() else createSecondMapFrame(),
                createAltText(textHeader),
            )
        }

        private val results get() = (winner ?: null.asOneTimePublisher())
            .merge(runoff ?: null.asOneTimePublisher()) { w, r -> w to r }

        protected enum class Result {
            WINNER, RUNOFF
        }

        protected data class Entry<K, V>(val key: K, val value: V, val result: Result?)

        private val mandatoryKeys get() = current.merge(results) { c, r ->
            sequenceOf(
                c.keys.filter { mandatoryParties.contains(keyTemplate.toParty(it)) },
                r.first?.let { setOf(it) },
                r.second,
            ).filterNotNull().flatten().toSet()
        }

        protected val filteredCurr get() = current.merge(mandatoryKeys) { c, k -> valueTemplate.combine(c, limit, others, k) }

        protected val currEntries: Flow.Publisher<List<Entry<KT, CT>>>
            get() {
                return filteredCurr.merge(results) { c, r ->
                    c.entries
                        .sortedByDescending { (k, v) -> keyTemplate.toParty(k).overrideSortOrder?.toDouble() ?: valueTemplate.sortOrder(v) }
                        .map { (k, v) ->
                            val result = when {
                                r.first == k -> Result.WINNER
                                (r.second ?: emptySet()).contains(k) -> Result.RUNOFF
                                else -> null
                            }
                            Entry(k, v, result)
                        }
                }
            }

        abstract fun createResultFrameBuilder(): BarFrameBuilder

        private fun applyMajorityLine(builder: BarFrameBuilder) {
            val showMajority = this.showMajority
            val pctReporting = this.pctReporting
            if (showMajority != null) {
                val lines = showMajority.merge(
                    pctReporting ?: 1.0.asOneTimePublisher(),
                ) {
                        show, pct ->
                    if (show) {
                        listOf(0.5 / pct.coerceAtLeast(1e-6))
                    } else {
                        emptyList()
                    }
                }
                builder.withLines(lines) { majorityLabel!! }
            }
        }

        private fun createFrame(): BarFrame {
            val builder = createResultFrameBuilder()
                .withHeader(header, rightLabelPublisher = progressLabel)
                .withSubhead(subhead)
                .withNotes(this.notes ?: (null as String?).asOneTimePublisher())
                .withMax(
                    this.pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher(),
                )
            applyMajorityLine(builder)
            return builder.build()
        }

        protected val currPreferencesEntries get() = currPreferences?.merge(results) { c, r ->
            c.entries
                .sortedByDescending { (k, v) -> keyTemplate.toParty(k).overrideSortOrder?.toDouble() ?: valueTemplate.sortOrder(v) }
                .map { (k, v) ->
                    val result = when {
                        r.first == k -> Result.WINNER
                        (r.second ?: emptySet()).contains(k) -> Result.RUNOFF
                        else -> null
                    }
                    Entry(k, v, result)
                }
        }

        abstract fun createPreferenceResultFrameBuilder(): BarFrameBuilder?

        private fun createPreferenceFrame(): BarFrame? {
            val builder = (createPreferenceResultFrameBuilder() ?: return null)
                .withHeader(preferenceHeader!!, rightLabelPublisher = preferenceProgressLabel)
                .withSubhead(preferenceSubhead!!)
                .withMax(
                    this.preferencePctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher(),
                )
                .withLines(this.preferencePctReporting?.map { listOf(0.5 / it.coerceAtLeast(1e-6)) } ?: listOf(0.5).asOneTimePublisher()) { "50%" }
            return builder.build()
        }

        protected val currClassificationEntries get() = classificationFunc?.let { func ->
            current.map { c ->
                c.entries
                    .groupBy({ func(keyTemplate.toParty(it.key)) }, { it.value })
                    .mapValues { it.value.reduce { a, b -> combineValuesForClassification(a, b) } }
                    .entries
                    .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: valueTemplate.sortOrder(it.value) }
                    .map { Entry(it.key, it.value, null) }
            }
        }

        protected abstract fun createClassificationFrameBuilder(): BarFrameBuilder?

        protected abstract fun combineValuesForClassification(a: CT, b: CT): CT

        private fun createClassificationFrame(): BarFrame? {
            val builder = (createClassificationFrameBuilder() ?: return null)
                .withHeader(classificationHeader!!)
                .withMax(
                    pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher(),
                )
            applyMajorityLine(builder)
            return builder.build()
        }

        protected data class CurrDiffEntry<K, P, C, D>(val key: K?, val party: P, val curr: C?, val diff: D?, val result: Result?)

        protected val diffEntries get() = diff?.merge(currEntries) { diff, curr ->
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

        protected val prevEntries get() = prevRaw?.map { prev ->
            prev.entries
                .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value.toDouble() }
                .map { Entry(it.key, it.value, null) }
        }

        private fun createDiffFrame(): BarFrame? {
            return (createDiffFrameBuilder() ?: return null)
                .withLimits(
                    (showPrevRaw ?: false.asOneTimePublisher()).merge(pctReporting ?: 1.0.asOneTimePublisher()) { showRaw, pct ->
                        if (showRaw) {
                            BarFrameBuilder.Limit(max = 2.0 / 3)
                        } else {
                            BarFrameBuilder.Limit(wingspan = 0.1 / pct.coerceAtLeast(1e-6))
                        }
                    },
                )
                .withHeader(changeHeader!!)
                .withSubhead(filteredChangeSubhead ?: (null as String?).asOneTimePublisher())
                .build()
        }

        protected abstract fun createDiffFrameBuilder(): BarFrameBuilder?

        private fun createSwingFrame(): SwingFrame? {
            return (createSwingFrameBuilder() ?: return null)
                .withHeader(swingHeader!!)
                .withRange(swingRange!!)
                .build()
        }

        protected abstract fun createSwingFrameBuilder(): SwingFrameBuilder?

        private fun createMapFrame(): MapFrame? {
            return mapBuilder?.createMapFrame()
        }

        private fun createSecondMapFrame(): MapFrame? {
            return secondMapBuilder?.createMapFrame()
        }

        private fun createAltText(textHeader: Flow.Publisher<out String?>): Flow.Publisher<String> {
            return listOf(
                textHeader,
                createBarAltText(),
                createPrevAltText(),
                createClassificationAltText(),
                createPreferencesAltText(),
                createSwingAltText(),
            ).combine()
                .map { list -> list.filterNotNull().joinToString("\n\n") }
        }

        protected abstract fun createBarAltText(): Flow.Publisher<String?>

        protected abstract fun createClassificationAltText(): Flow.Publisher<String?>

        protected abstract fun createPreferencesAltText(): Flow.Publisher<String?>

        private fun createPrevAltText(): Flow.Publisher<String?> {
            return (showPrevRaw ?: return null.asOneTimePublisher()).compose { showPrevRaw ->
                if (!showPrevRaw) return@compose null.asOneTimePublisher()
                val title = createHeaderAltText(changeHeader, changeSubhead, null)
                val bars = prevEntries?.map { entries ->
                    createPrevBarAltTexts(entries).joinToString("")
                } ?: return@compose null.asOneTimePublisher()
                title.merge(bars) { t, b -> t + b }
            }
        }

        protected abstract fun createPrevBarAltTexts(entries: List<Entry<KPT, PT>>): List<String>

        private fun createSwingAltText(): Flow.Publisher<String?> {
            return createSwingFrameBuilder()?.buildBottomText()?.merge(swingHeader ?: null.asOneTimePublisher()) { bottom, header ->
                "${header?.let { "$it: " }}$bottom"
            } ?: null.asOneTimePublisher()
        }

        protected fun createHeaderAltText(header: Flow.Publisher<out String?>?, subhead: Flow.Publisher<out String?>?, progress: Flow.Publisher<out String?>?): Flow.Publisher<String?> {
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

    private class BasicVoteScreenBuilder<KT : Any, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<out KT, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
        voteTemplate: VoteTemplate,
        others: KT,
    ) : VoteScreenBuilder<KT, KPT, Int?, Double, Int, Double>(current, header, subhead, keyTemplate, voteTemplate, VoteValueTemplate, others) {

        override val diff: Flow.Publisher<out Map<out KPT, Double>>?
            get() = filteredPrev?.merge(filteredCurr) { prev, curr ->
                val prevHasOthers = prev.containsKey(keyTemplate.toParty(others))
                val currByParty = curr.entries
                    .groupBy({ keyTemplate.toParty(it.key).let { p -> if (prevHasOthers && !prev.containsKey(p)) keyTemplate.toParty(others) else p } }, { it.value })
                    .mapValues { list -> list.value.sumOf { it ?: return@merge emptyMap() } }
                val prevByParty = Aggregators.adjustKey(prev, { if (currByParty.containsKey(it)) it else keyTemplate.toParty(others) }, { a, b -> prevCombine(a, b) })
                val allParties = sequenceOf(currByParty.keys, prevByParty.keys).flatten().toSet()
                val currTotal = currByParty.values.sum()
                val prevTotal = prev.values.sum()
                if (currTotal == 0 || prevTotal == 0 || curr.any { it.value == null }) return@merge emptyMap()
                allParties.associateWith {
                    (currByParty[it] ?: 0).toDouble() / currTotal - (prevByParty[it] ?: 0).toDouble() / prevTotal
                }
            }

        private fun doubleLineBarLimit(): Int {
            return if (currPreferences == null) 10 else 0
        }

        override fun createResultFrameBuilder(): BarFrameBuilder {
            return currEntries.map { entries ->
                createBars(entries, "UNCONTESTED")
            }.let { BarFrameBuilder.basic(it) }
        }

        override fun createPreferenceResultFrameBuilder(): BarFrameBuilder? {
            return currPreferencesEntries?.map { entries ->
                createBars(entries, "ELECTED")
            }?.let { BarFrameBuilder.basic(it) }
        }

        override fun createClassificationFrameBuilder(): BarFrameBuilder? {
            return currClassificationEntries?.map { entries ->
                val total = entries.sumOf { it.value ?: 0 }
                entries.map {
                    BarFrameBuilder.BasicBar(
                        it.key.name.uppercase(),
                        it.key.color,
                        1.0 * (it.value ?: 0) / total,
                        if (it.value == null) {
                            "WAITING..."
                        } else voteTemplate.toBarString(
                            it.value,
                            1.0 * it.value / total,
                            true,
                        ),
                    )
                }
            }?.let { BarFrameBuilder.basic(it) }
        }

        private fun createBars(entries: List<Entry<KT, Int?>>, singleEntryLabel: String): List<BarFrameBuilder.BasicBar> {
            val count = entries.size
            val partialDeclaration = entries.any { it.value == null }
            val total = entries.sumOf { it.value ?: 0 }
            return entries.map { e ->
                val pct = e.value?.toDouble()?.div(total) ?: Double.NaN
                val valueLabel: String = when {
                    count == 1 -> {
                        singleEntryLabel
                    }

                    pct.isNaN() -> {
                        "WAITING..."
                    }

                    partialDeclaration -> {
                        THOUSANDS_FORMAT.format(e.value)
                    }

                    else -> {
                        voteTemplate.toBarString(
                            e.value!!,
                            pct,
                            count > doubleLineBarLimit(),
                        )
                    }
                }
                val shape: Shape? = when (e.result) {
                    VoteScreenBuilder.Result.WINNER -> keyTemplate.winnerShape(count > doubleLineBarLimit())
                    VoteScreenBuilder.Result.RUNOFF -> keyTemplate.runoffShape(count > doubleLineBarLimit())
                    null -> null
                }
                BarFrameBuilder.BasicBar(
                    keyTemplate.toMainBarHeader(e.key, count > doubleLineBarLimit()),
                    keyTemplate.toParty(e.key).color,
                    if (pct.isNaN()) 0 else pct,
                    valueLabel,
                    shape,
                )
            }
        }

        override fun combineValuesForClassification(a: Int?, b: Int?): Int? {
            return if (a == null || b == null) null else (a + b)
        }

        override fun prevCombine(value1: Int, value2: Int): Int {
            return value1 + value2
        }

        override fun createDiffFrameBuilder(): BarFrameBuilder? {
            val prevBars = prevEntries?.map { entries ->
                val total = entries.sumOf { it.value }
                entries.map {
                    val pct = it.value.toDouble() / total
                    BarFrameBuilder.BasicBar(
                        it.key.abbreviation.uppercase(),
                        it.key.color,
                        pct,
                        DecimalFormat("0.0%").format(pct),
                    )
                }
            }
            val diffBars = diffEntries?.map { entries ->
                entries.filter { it.diff != null }
                    .filter { it.diff != null }
                    .let { e ->
                        if (e.size <= 10) return@let e
                        val others = keyTemplate.toParty(others)
                        val take = e.filter { it.key != others }.take(9)
                        val rest = e.filter { !take.contains(it) }
                            .sumOf { it.diff!! }
                        take + listOf(CurrDiffEntry(null, others, null, rest, null))
                    }
                    .map {
                        BarFrameBuilder.BasicBar(
                            it.party.abbreviation.uppercase(),
                            it.party.color,
                            it.diff!!,
                            DecimalFormat("+0.0%;-0.0%").format(it.diff),
                        )
                    }
            }
            return (showPrevRaw ?: return null)
                .compose { if (it) prevBars!! else diffBars!! }
                .let { BarFrameBuilder.basic(it) }
        }

        override fun createSwingFrameBuilder(): SwingFrameBuilder? {
            if (swingComparator == null) return null
            val curr: Flow.Publisher<out Map<out KPT, Int>>
            val prev: Flow.Publisher<out Map<out KPT, Int>>
            val currPreferences = this.currPreferences
            val prevPreferences = this.prevPreferences
            if (currPreferences != null && prevPreferences != null) {
                curr = currPreferences.map { currTotalByParty(it) }
                prev = prevPreferences
                    .merge(
                        currPreferences.map { currTotalByParty(it) },
                    ) { p, c ->
                        if (c.keys != p.keys) {
                            emptyMap()
                        } else {
                            p
                        }
                    }
            } else {
                curr = current.map { currTotalByParty(it) }
                prev = this.filteredPrev!!
                    .merge(current) { p, c ->
                        val prevWinner: KPT? = p.entries
                            .maxByOrNull { it.value }
                            ?.key
                        if (prevWinner == null ||
                            c.keys.asSequence()
                                .map { keyTemplate.toParty(it) }
                                .none { it == prevWinner }
                        ) {
                            emptyMap()
                        } else {
                            p
                        }
                    }
            }
            val classificationFunc = classificationFunc
            return SwingFrameBuilder.prevCurr(
                (if (classificationFunc == null) prev else Aggregators.adjustKey(prev, classificationFunc)),
                (if (classificationFunc == null) curr else Aggregators.adjustKey(curr, classificationFunc)),
                swingComparator!!,
            )
        }

        private fun currTotalByParty(curr: Map<out KT, Int?>): Map<KPT, Int> {
            if (curr.values.any { it == null }) {
                return emptyMap()
            }
            return curr.entries
                .groupingBy { keyTemplate.toParty(it.key) }
                .fold(0) { a, e -> a + (e.value ?: 0) }
        }

        private fun consolidate(votes: Map<KPT, Int>, parties: Collection<KPT>): Map<PartyOrCoalition, Int> {
            return votes.entries.groupingBy { if (parties.contains(it.key)) it.key else Party.OTHERS }.fold(0) { a, e -> a + e.value }
        }

        override fun createBarAltText(): Flow.Publisher<String?> {
            val isRunoffSameParties = current.merge(prev ?: null.asOneTimePublisher()) { current, prev -> current to prev }
                .merge(runoffSubhead ?: null.asOneTimePublisher()) { (current, prev), runoffSubhead ->
                    if (runoffSubhead == null) {
                        true
                    } else {
                        current.keys.map { keyTemplate.toParty(it) }.toSet() == prev?.keys
                    }
                }

            val isWinnerRunningAgain = current.merge(prev ?: null.asOneTimePublisher()) { current, prev -> current to prev }
                .map { (current, prev) ->
                    val prevWinner = (prev ?: emptyMap()).entries.maxByOrNull { it.value }?.key
                    prevWinner != null && current.keys.any { keyTemplate.toParty(it) == prevWinner }
                }

            val mainText: Flow.Publisher<String?> = createHeaderAltText(header, subhead, progressLabel)

            val changeText: Flow.Publisher<String?> =
                createHeaderAltText(
                    listOf(
                        (changeHeader ?: null.asOneTimePublisher()),
                        isWinnerRunningAgain.merge(winnerNotRunningAgain ?: null.asOneTimePublisher()) { winnerRunningAgain, winnerNotRunningAgain ->
                            if (winnerRunningAgain) null else winnerNotRunningAgain?.takeIf { it.isNotEmpty() }
                        },
                        isRunoffSameParties.merge(runoffSubhead ?: null.asOneTimePublisher()) { runoffSameParties, runoffSubhead ->
                            if (runoffSameParties) null else runoffSubhead?.takeIf { it.isNotEmpty() }
                        },
                    ).combine().map { items -> items.filterNotNull().joinToString(" ").takeIf { it.isNotEmpty() } },
                    changeSubhead,
                    null,
                )
                    .merge(showPrevRaw ?: false.asOneTimePublisher()) { text, showPrevRaw -> if (showPrevRaw) null else text }

            val majorityText: Flow.Publisher<String?> =
                (showMajority ?: false.asOneTimePublisher()).map { showMajority ->
                    if (showMajority) {
                        majorityLabel
                    } else {
                        null
                    }
                }

            val title = mainText.merge(changeText) { main, change ->
                (main ?: "") + (change?.let { " ($it)" } ?: "")
            }

            val barsText = (showPrevRaw ?: false.asOneTimePublisher()).compose { prevRaw ->
                val diffEntries = diffEntries
                if (prevRaw || diffEntries == null) {
                    currEntries.map { entries ->
                        val total = entries.sumOf { it.value ?: 0 }.toDouble()
                        val partial = entries.any { it.value == null }
                        entries.joinToString("") { e ->
                            "\n" + keyTemplate.toMainBarHeader(e.key, true) + ": " +
                                (
                                    if (e.value == null || total == 0.0) {
                                        "WAITING..."
                                    } else if (partial) {
                                        THOUSANDS_FORMAT.format(e.value)
                                    } else
                                        voteTemplate.toAltTextString(
                                            e.value,
                                            e.value / total,
                                            null,
                                            "",
                                        )
                                    ) + (e.result?.let { " $it" } ?: "")
                        }
                    }
                } else {
                    diffEntries.map { entries ->
                        val total = entries.sumOf { it.curr ?: 0 }.toDouble()
                        val partial = entries.any { it.curr == null && it.diff == null }
                        val noDiffs = entries.all { it.diff == null }
                        val duplicateDiffs = entries.groupBy { it.party }
                            .filterValues { it.size > 1 }
                            .filterValues { it.any { e -> e.diff != null } }
                            .keys
                        var changeInAggregates = false
                        var changeInOthers = false
                        entries.joinToString("") { e ->
                            "\n" + (e.key?.let { keyTemplate.toMainBarHeader(it, true) } ?: e.party.name.uppercase()) + ": " +
                                (
                                    if (entries.size == 1) {
                                        "UNCONTESTED"
                                    } else if (e.curr == null && e.diff != null) {
                                        "- (${PCT_DIFF_FORMAT.format(e.diff)})"
                                    } else if (e.curr == null || total == 0.0) {
                                        "WAITING..."
                                    } else if (partial) {
                                        THOUSANDS_FORMAT.format(e.curr)
                                    } else
                                        voteTemplate.toAltTextString(
                                            e.curr,
                                            e.curr / total,
                                            e.diff,
                                            if (duplicateDiffs.contains(e.party)) {
                                                "^".also { changeInAggregates = true }
                                            } else if (e.diff != null || noDiffs) {
                                                ""
                                            } else
                                                "*".also { changeInOthers = true },
                                        )
                                    ) + (e.result?.let { " $it" } ?: "")
                        } +
                            (if (changeInAggregates) "\n^ AGGREGATED ACROSS CANDIDATES IN PARTY" else "") +
                            (if (changeInOthers) "\n* CHANGE INCLUDED IN OTHERS" else "")
                    }
                }
            }

            return listOf(
                title,
                barsText,
                majorityText,
            ).combine().map { (title, barsText, majorityText) ->
                title +
                    barsText +
                    (majorityText?.let { "\n$it" } ?: "")
            }
        }

        override fun createClassificationAltText(): Flow.Publisher<String?> {
            val bars = (currClassificationEntries ?: return null.asOneTimePublisher())
                .map { entries ->
                    val total = entries.sumOf { it.value ?: 0 }.toDouble()
                    entries.joinToString("") {
                        "\n${it.key.name.uppercase()}: ${PCT_FORMAT.format(it.value!! / total)}"
                    }
                }
            val title = (classificationHeader ?: null.asOneTimePublisher()).map { it ?: "" }
            return title.merge(bars) { t, b -> t + b }
        }

        override fun createPreferencesAltText(): Flow.Publisher<String?> {
            val barsText = (currPreferencesEntries ?: return null.asOneTimePublisher()).map { entries ->
                val total = entries.takeUnless { e -> e.any { it.value == null } }?.sumOf { it.value!! }?.toDouble()
                entries.joinToString("") {
                    "\n${keyTemplate.toMainBarHeader(it.key, true)}: ${
                        if (entries.size == 1) {
                            "ELECTED"
                        } else if (total == 0.0 || it.value == null) {
                            "WAITING..."
                        } else
                            THOUSANDS_FORMAT.format(it.value)
                    }${
                        if (total == 0.0 || total == null) "" else " (${PCT_FORMAT.format(it.value!! / total)})"
                    }${
                        (it.result?.takeIf { entries.size > 1 }?.let { r -> " $r" } ?: "")
                    }"
                }
            }
            val title = createHeaderAltText(preferenceHeader, preferenceSubhead, preferenceProgressLabel)
            return title.merge(barsText) { t, b -> t + b }
        }

        override fun createPrevBarAltTexts(entries: List<Entry<KPT, Int>>): List<String> {
            val total = entries.sumOf { it.value }.toDouble()
            return entries.map { (k, v) -> "\n${k.abbreviation}: ${PCT_FORMAT.format(v / total)}" }
        }
    }

    private class RangeVoteScreenBuilder<KT : Any, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<KT, ClosedRange<Double>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
        voteTemplate: VoteTemplate,
        others: KT,
    ) : VoteScreenBuilder<KT, KPT, ClosedRange<Double>, Double, Int, ClosedRange<Double>>(current, header, subhead, keyTemplate, voteTemplate, RangeValueTemplate, others) {

        override val diff: Flow.Publisher<out Map<out KPT, ClosedRange<Double>>>?
            get() = filteredPrev?.merge(filteredCurr) { prev, curr ->
                val prevTotal = prev.values.sum()
                if (prevTotal == 0) return@merge emptyMap()
                val currByParty = Aggregators.adjustKey(curr, { keyTemplate.toParty(it) }, { a, b -> (a.start + b.start)..(a.endInclusive + b.endInclusive) })
                val prevByParty = Aggregators.adjustKey(prev, { if (currByParty.containsKey(it)) it else keyTemplate.toParty(others) }, { a, b -> prevCombine(a, b) })
                val allParties = sequenceOf(prevByParty.keys, currByParty.keys).flatten().toSet()
                allParties.associateWith {
                    val prevPct = (prevByParty[it] ?: 0).toDouble() / prevTotal
                    (currByParty[it] ?: 0.0..0.0).let { curr -> (curr.start - prevPct)..(curr.endInclusive - prevPct) }
                }
            }

        override fun createResultFrameBuilder(): BarFrameBuilder {
            return currEntries.map { entries ->
                createBars(entries)
            }.let { BarFrameBuilder.dual(it) }
        }

        override fun createPreferenceResultFrameBuilder(): BarFrameBuilder? {
            return currPreferencesEntries?.map { entries ->
                createBars(entries)
            }?.let { BarFrameBuilder.dual(it) }
        }

        override fun createClassificationFrameBuilder(): BarFrameBuilder? {
            return classificationFunc?.let { throw UnsupportedOperationException("Classifications not supported on ranges") }
        }

        private fun createBars(entries: List<Entry<KT, ClosedRange<Double>>>) =
            entries.map {
                val valueLabel = (
                    DECIMAL_FORMAT.format(100 * it.value.start) +
                        "-" +
                        DecimalFormat("0.0").format(100 * it.value.endInclusive) +
                        "%"
                    )
                BarFrameBuilder.DualBar(
                    keyTemplate.toMainBarHeader(it.key, false),
                    keyTemplate.toParty(it.key).color,
                    it.value.start,
                    it.value.endInclusive,
                    valueLabel,
                )
            }
                .toList()

        override fun combineValuesForClassification(
            a: ClosedRange<Double>,
            b: ClosedRange<Double>,
        ): ClosedRange<Double> {
            throw UnsupportedOperationException("Classifications not supported on ranges")
        }

        override fun createDiffFrameBuilder(): BarFrameBuilder? {
            val prevBars = prevEntries?.map { entries ->
                val total = entries.sumOf { it.value }
                entries.map {
                    val pct = it.value.toDouble() / total
                    BarFrameBuilder.DualBar(
                        it.key.abbreviation.uppercase(),
                        it.key.color,
                        pct,
                        pct,
                        DecimalFormat("0.0%").format(pct),
                    )
                }
            }
            val diffBars = diffEntries?.map { entries ->
                entries.map {
                    BarFrameBuilder.DualBar(
                        it.party.abbreviation.uppercase(),
                        it.party.color,
                        it.diff!!.start,
                        it.diff.endInclusive,
                        "(" +
                            CHANGE_DECIMAL_FORMAT.format(100.0 * (it.diff.start)) +
                            ")-(" +
                            CHANGE_DECIMAL_FORMAT.format(100.0 * (it.diff.endInclusive)) +
                            ")%",
                    )
                }
            }
            return (this.showPrevRaw ?: return null)
                .compose { if (it) prevBars!! else diffBars!! }
                .let { BarFrameBuilder.dual(it) }
        }

        override fun prevCombine(value1: Int, value2: Int): Int {
            return value1 + value2
        }

        override fun createSwingFrameBuilder(): SwingFrameBuilder? {
            if (swingComparator == null) return null
            val curr = current
                .map { m ->
                    m.entries.groupingBy { keyTemplate.toParty(it.key) }
                        .fold(0.0..0.0) { a, e -> (a.start + e.value.start)..(a.endInclusive + e.value.endInclusive) }
                }
                .map { m ->
                    m.mapValues { e -> e.value.let { r -> (1000000 * (r.start + r.endInclusive) / 2).roundToInt() } }
                }
            return SwingFrameBuilder.prevCurr(filteredPrev!!, curr, swingComparator!!)
        }

        private val barEntryLine: (String, ClosedRange<Double>?, ClosedRange<Double>?) -> String = { h, p, d ->
            "$h: " + (if (p == null) "-" else "${DecimalFormat("0.0").format(100 * p.start)}-${PCT_FORMAT.format(p.endInclusive)}") +
                (d?.let { " ((${DecimalFormat("+0.0;-0.0").format(100 * it.start)})-(${DecimalFormat("+0.0;-0.0").format(100 * it.endInclusive)})%)" } ?: "")
        }

        override fun createBarAltText(): Flow.Publisher<String?> {
            val showPrevRaw: Flow.Publisher<Boolean> = showPrevRaw ?: false.asOneTimePublisher()
            val mainText = createHeaderAltText(header, subhead, progressLabel)
            val changeText = createHeaderAltText(changeHeader, changeSubhead, null)
                .merge(showPrevRaw) { text, raw -> if (raw) null else text }
            val title = mainText.merge(changeText) { main, change -> main + (if (change == null) "" else " ($change)") }
            val bars = showPrevRaw.compose { raw ->
                val diffEntries = diffEntries
                if (raw || diffEntries == null) {
                    currEntries.map { entries ->
                        entries.joinToString("") {
                            "\n" + barEntryLine(
                                keyTemplate.toMainBarHeader(it.key, true),
                                it.value,
                                null,
                            )
                        }
                    }
                } else {
                    diffEntries.map { entries ->
                        entries.joinToString("") {
                            "\n" + barEntryLine(
                                if (it.key == null) {
                                    it.party.abbreviation.uppercase()
                                } else
                                    keyTemplate.toMainBarHeader(it.key, true),
                                it.curr,
                                it.diff,
                            )
                        }
                    }
                }
            }
            return title.merge(bars) { head, text -> "$head$text" }
        }

        override fun createClassificationAltText(): Flow.Publisher<String?> {
            return null.asOneTimePublisher()
        }

        override fun createPreferencesAltText(): Flow.Publisher<String?> {
            val barsText = (currPreferencesEntries ?: return null.asOneTimePublisher()).map { entries ->
                entries.joinToString("") { "\n${barEntryLine(keyTemplate.toMainBarHeader(it.key, true), it.value, null)}" }
            }
            val title = createHeaderAltText(preferenceHeader, preferenceSubhead, preferenceProgressLabel)
            return title.merge(barsText) { t, b -> t + b }
        }

        override fun createPrevBarAltTexts(entries: List<Entry<KPT, Int>>): List<String> {
            val total = entries.sumOf { it.value }.toDouble()
            return entries.map { (k, v) -> "\n${k.abbreviation}: ${PCT_FORMAT.format(v / total)}" }
        }

        companion object {
            private val DECIMAL_FORMAT = DecimalFormat("0.0")
            private val CHANGE_DECIMAL_FORMAT = DecimalFormat("+0.0;-0.0")
        }
    }

    class NonPartisanVoteBuilder(
        private val votes: Flow.Publisher<out Map<NonPartisanCandidate, Int?>>,
        private val header: Flow.Publisher<out String?>,
        private val subhead: Flow.Publisher<out String?>,
    ) {
        private var prevVotes: Flow.Publisher<out Map<NonPartisanCandidate, Int>>? = null
        private var prevHeader: Flow.Publisher<out String?>? = null
        private var prevSubhead: Flow.Publisher<out String?>? = null
        private var pctReporting: Flow.Publisher<Double> = 1.0.asOneTimePublisher()
        private var progressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()
        private var winner: Flow.Publisher<out NonPartisanCandidate?> = null.asOneTimePublisher()

        private var mapBuilder: MapBuilder<*>? = null

        fun withPrev(
            prevVotes: Flow.Publisher<out Map<NonPartisanCandidate, Int>>,
            prevHeader: Flow.Publisher<out String?>,
            prevSubhead: Flow.Publisher<out String?> = null.asOneTimePublisher(),
        ): NonPartisanVoteBuilder {
            this.prevVotes = prevVotes
            this.prevHeader = prevHeader
            this.prevSubhead = prevSubhead
            return this
        }

        fun withWinner(
            winner: Flow.Publisher<out NonPartisanCandidate?>,
        ): NonPartisanVoteBuilder {
            this.winner = winner
            return this
        }

        fun withPctReporting(
            pctReporting: Flow.Publisher<Double>,
        ): NonPartisanVoteBuilder {
            this.pctReporting = pctReporting
            return this
        }

        fun withProgressLabel(progressLabel: Flow.Publisher<out String?>): NonPartisanVoteBuilder {
            this.progressLabel = progressLabel
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingCandidate: Flow.Publisher<out NonPartisanCandidateResult?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>,
        ): NonPartisanVoteBuilder {
            mapBuilder = MapBuilder.singleNonPartisanResult(shapes, selectedShape, leadingCandidate, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingCandidate: Flow.Publisher<out NonPartisanCandidateResult?>,
            focus: Flow.Publisher<out List<T>?>,
            additionalHighlight: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>,
        ): NonPartisanVoteBuilder {
            mapBuilder = MapBuilder.singleNonPartisanResult(shapes, selectedShape, leadingCandidate, focus, additionalHighlight, header)
            return this
        }

        fun build(title: Flow.Publisher<out String?>): SimpleVoteViewPanel {
            return SimpleVoteViewPanel(
                title,
                createResultFrame(),
                null,
                createPrevFrame(),
                null,
                mapBuilder?.createMapFrame(),
                createAltText(title),
            )
        }

        private fun createResultFrame(): JPanel {
            val bars = votes.merge(winner) { r, w ->
                val total = if (r.values.any { it == null }) {
                    null
                } else {
                    r.values.sumOf { it!! }.toDouble()
                }
                r.entries.sortedByDescending { it.key.overrideSortOrder ?: it.value ?: 0 }
                    .map { (c, v) ->
                        BarFrameBuilder.BasicBar(
                            "${c.fullName.uppercase()}\n${c.description?.uppercase() ?: ""}",
                            c.color,
                            v ?: 0,
                            when {
                                r.size == 1 -> "UNCONTESTED"
                                v == null || total == 0.0 -> "WAITING..."
                                total == null -> DecimalFormat("#,##0").format(v)
                                else -> "${DecimalFormat("#,##0").format(v)}\n${DecimalFormat("0.0%").format(v / total)}"
                            },
                            if (c == w) ImageGenerator.createHalfTickShape() else null,
                        )
                    }
            }
            return BarFrameBuilder.basic(bars)
                .withHeader(header, rightLabelPublisher = progressLabel)
                .withSubhead(subhead)
                .withMax(
                    votes.map { r -> r.values.sumOf { it ?: 0 } * 2 / 3 }
                        .merge(pctReporting) { v, p -> v / p.coerceAtLeast(1e-6) },
                )
                .build()
        }

        private fun createPrevFrame(): JPanel? {
            val prevVotes = this.prevVotes ?: return null
            val bars = prevVotes.map { r ->
                val total = r.values.sumOf { it }.toDouble()
                r.entries.sortedByDescending { it.key.overrideSortOrder ?: it.value }
                    .map { (c, v) ->
                        BarFrameBuilder.BasicBar(
                            c.surname.uppercase(),
                            c.color,
                            v,
                            if (r.size == 1) "UNCONTESTED" else DecimalFormat("0.0%").format(v / total),
                        )
                    }
            }
            return BarFrameBuilder.basic(bars)
                .withHeader(prevHeader!!)
                .withSubhead(prevSubhead!!)
                .withMax(prevVotes.map { r -> r.values.sumOf { it } * 2 / 3 })
                .build()
        }

        private fun createAltText(title: Flow.Publisher<out String?>): Flow.Publisher<String> {
            val votes = header.merge(progressLabel) { h, p -> if (p == null) h else listOfNotNull(h, "[$p]").joinToString(" ") }
                .merge(subhead) { h, s -> listOfNotNull(h, s).filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }?.joinToString(", ") }
                .merge(votes.merge(winner) { r, w -> r to w }) { h, (r, w) ->
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
            val prev = if (prevVotes == null) {
                null.asOneTimePublisher()
            } else {
                prevHeader!!.merge(prevSubhead!!) { h, s -> listOfNotNull(h, s).filter { it.isNotEmpty() }.takeIf { it.isNotEmpty() }?.joinToString(", ") }
                    .merge(prevVotes!!) { h, r ->
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
