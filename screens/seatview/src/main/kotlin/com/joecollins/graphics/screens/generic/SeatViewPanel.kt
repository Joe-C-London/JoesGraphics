package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.CanOverrideSortOrder
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.compose
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel
import kotlin.math.abs
import kotlin.math.sign

class SeatViewPanel private constructor(
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
        fun <P : PartyOrCoalition> partySeats(
            seats: Flow.Publisher<out Map<out P, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<P, P, Int, Int, *> {
            return BasicSeatScreenBuilder(
                seats,
                header,
                subhead,
                BasicResultPanel.PartyTemplate(),
            )
        }

        fun candidateSeats(
            seats: Flow.Publisher<out Map<Candidate, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<Candidate, Party, Int, Int, *> {
            return BasicSeatScreenBuilder(
                seats,
                header,
                subhead,
                BasicResultPanel.CandidateTemplate(),
            )
        }

        fun <P : PartyOrCoalition> partyDualSeats(
            seats: Flow.Publisher<out Map<out P, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<P, P, Pair<Int, Int>, Pair<Int, Int>, *> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                BasicResultPanel.PartyTemplate(),
                DualSeatScreenBuilder.FocusLocation.FIRST,
            )
        }

        fun <P : PartyOrCoalition> partyDualSeatsReversed(
            seats: Flow.Publisher<out Map<P, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<P, P, Pair<Int, Int>, Pair<Int, Int>, *> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                BasicResultPanel.PartyTemplate(),
                DualSeatScreenBuilder.FocusLocation.LAST,
            )
        }

        fun candidateDualSeats(
            seats: Flow.Publisher<out Map<Candidate, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<Candidate, Party, Pair<Int, Int>, Pair<Int, Int>, *> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                BasicResultPanel.CandidateTemplate(),
                DualSeatScreenBuilder.FocusLocation.FIRST,
            )
        }

        fun <P : PartyOrCoalition> partyRangeSeats(
            seats: Flow.Publisher<out Map<P, IntRange>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<P, P, IntRange, Int, *> {
            return RangeSeatScreenBuilder(
                seats,
                header,
                subhead,
                BasicResultPanel.PartyTemplate(),
            )
        }

        fun candidateRangeSeats(
            seats: Flow.Publisher<out Map<Candidate, IntRange>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<Candidate, Party, IntRange, Int, *> {
            return RangeSeatScreenBuilder(
                seats,
                header,
                subhead,
                BasicResultPanel.CandidateTemplate(),
            )
        }
    }

    interface SeatTemplate<CT, PT, BAR> {
        fun sortOrder(value: CT?): Int?

        fun prevSortOrder(value: PT?): Int?

        val default: CT
        fun labelText(value: CT): String

        fun diffLabelText(value: CT): String

        fun prevLabelText(value: PT): String

        fun combine(value1: CT, value2: CT): CT

        fun prevCombine(value1: PT, value2: PT): PT

        fun createBar(keyLabel: String, baseColor: Color, value: CT, shape: Shape?): BAR

        fun createPrevBar(keyLabel: String, baseColor: Color, value: PT): BAR

        fun createDiffBar(keyLabel: String, baseColor: Color, diff: CT): BAR
    }

    private class SingleSeatTemplate : SeatTemplate<Int, Int, BarFrameBuilder.BasicBar> {
        override fun sortOrder(value: Int?): Int? = value

        override fun prevSortOrder(value: Int?): Int? = value

        override val default: Int = 0

        override fun labelText(value: Int): String = value.toString()

        override fun diffLabelText(value: Int): String {
            return if (value == 0) {
                "±0"
            } else {
                DecimalFormat("+0;-0").format(value)
            }
        }

        override fun prevLabelText(value: Int): String = value.toString()

        override fun combine(value1: Int, value2: Int): Int {
            return value1 + value2
        }

        override fun prevCombine(value1: Int, value2: Int): Int {
            return value1 + value2
        }

        override fun createBar(keyLabel: String, baseColor: Color, value: Int, shape: Shape?): BarFrameBuilder.BasicBar {
            return BarFrameBuilder.BasicBar(
                keyLabel,
                baseColor,
                value,
                labelText(value),
                shape,
            )
        }

        override fun createPrevBar(keyLabel: String, baseColor: Color, value: Int): BarFrameBuilder.BasicBar {
            return BarFrameBuilder.BasicBar(
                keyLabel,
                baseColor,
                value,
                prevLabelText(value),
            )
        }

        override fun createDiffBar(keyLabel: String, baseColor: Color, diff: Int): BarFrameBuilder.BasicBar {
            return BarFrameBuilder.BasicBar(
                keyLabel,
                baseColor,
                diff,
                diffLabelText(diff),
            )
        }
    }

    private class DualSeatTemplate(val focusLocation: DualSeatScreenBuilder.FocusLocation) :
        SeatTemplate<Pair<Int, Int>, Pair<Int, Int>, BarFrameBuilder.DualBar> {
        override fun sortOrder(value: Pair<Int, Int>?): Int? = value?.second
        override fun prevSortOrder(value: Pair<Int, Int>?): Int? = sortOrder(value)

        override val default = 0 to 0

        override fun labelText(value: Pair<Int, Int>): String = "${value.first}/${value.second}"

        override fun diffLabelText(value: Pair<Int, Int>): String {
            return sequenceOf(value.first, value.second).map {
                if (it == 0) "±0" else DecimalFormat("+0;-0").format(it)
            }.joinToString("/")
        }

        override fun prevLabelText(value: Pair<Int, Int>): String = labelText(value)

        override fun combine(value1: Pair<Int, Int>, value2: Pair<Int, Int>): Pair<Int, Int> {
            return (value1.first + value2.first) to (value1.second + value2.second)
        }

        override fun prevCombine(value1: Pair<Int, Int>, value2: Pair<Int, Int>): Pair<Int, Int> {
            return (value1.first + value2.first) to (value1.second + value2.second)
        }

        override fun createBar(keyLabel: String, baseColor: Color, value: Pair<Int, Int>, shape: Shape?): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                if (focusLocation == DualSeatScreenBuilder.FocusLocation.FIRST) value.first else (value.second - value.first),
                value.second,
                labelText(value),
                shape,
            )
        }

        override fun createPrevBar(keyLabel: String, baseColor: Color, value: Pair<Int, Int>): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                if (focusLocation == DualSeatScreenBuilder.FocusLocation.FIRST) value.first else (value.second - value.first),
                value.second,
                prevLabelText(value),
            )
        }

        override fun createDiffBar(keyLabel: String, baseColor: Color, diff: Pair<Int, Int>): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                if (
                    focusLocation == DualSeatScreenBuilder.FocusLocation.FIRST ||
                    (diff.first != 0 && sign(diff.first.toDouble()) != sign(diff.second.toDouble())) ||
                    abs(diff.first.toDouble()) > abs(diff.second.toDouble())
                ) {
                    diff.first
                } else {
                    (diff.second - diff.first)
                },
                diff.second,
                diffLabelText(diff),
            )
        }
    }

    private class RangeSeatTemplate : SeatTemplate<IntRange, Int, BarFrameBuilder.DualBar> {
        override fun sortOrder(value: IntRange?): Int? = value?.let { it.first + it.last }

        override fun prevSortOrder(value: Int?): Int? = value

        override val default = 0..0

        override fun labelText(value: IntRange): String = "${value.first}-${value.last}"

        override fun diffLabelText(value: IntRange): String {
            return sequenceOf(value.first, value.last).map {
                "(" + (if (it == 0) "±0" else DecimalFormat("+0;-0").format(it)) + ")"
            }.joinToString("-")
        }

        override fun prevLabelText(value: Int): String = value.toString()

        override fun combine(value1: IntRange, value2: IntRange): IntRange {
            return (value1.first + value2.first)..(value1.last + value2.last)
        }

        override fun prevCombine(value1: Int, value2: Int): Int {
            return value1 + value2
        }

        override fun createBar(keyLabel: String, baseColor: Color, value: IntRange, shape: Shape?): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                value.first,
                value.last,
                labelText(value),
                shape,
            )
        }

        override fun createPrevBar(keyLabel: String, baseColor: Color, value: Int): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                value,
                value,
                prevLabelText(value),
            )
        }

        override fun createDiffBar(keyLabel: String, baseColor: Color, diff: IntRange): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                diff.first,
                diff.last,
                diffLabelText(diff),
            )
        }
    }

    abstract class SeatScreenBuilder<KT : Any, KPT : PartyOrCoalition, CT : Any, PT : Any, BAR> internal constructor(
        private var current: Flow.Publisher<out Map<out KT, CT>>,
        private var header: Flow.Publisher<out String?>,
        private var subhead: Flow.Publisher<out String?>,
        private val keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
        private val seatTemplate: SeatTemplate<CT, PT, BAR>,
    ) {
        private var total: Flow.Publisher<out Int>? = null
        private var showMajority: Flow.Publisher<out Boolean>? = null
        private var majorityFunction: ((Int) -> String)? = null
        private var winner: Flow.Publisher<out KT?>? = null
        private var notes: Flow.Publisher<out String?>? = null
        private var changeNotes: Flow.Publisher<out String?>? = null
        private var prev: Flow.Publisher<out Map<out KPT, PT>>? = null
        private var diff: Flow.Publisher<out Map<KPT, BasicResultPanel.CurrDiff<CT>>>? = null
        private var showPrevRaw: Flow.Publisher<Boolean>? = null
        private var changeHeader: Flow.Publisher<out String?>? = null
        private var changeSubhead: Flow.Publisher<out String?>? = null
        private var currVotes: Flow.Publisher<out Map<out KPT, Int>>? = null
        private var prevVotes: Flow.Publisher<out Map<out KPT, Int>>? = null
        private var swingHeader: Flow.Publisher<out String?>? = null
        private var swingComparator: Comparator<KPT>? = null
        private var swingRange: Flow.Publisher<Double>? = null
        private var classificationFunc: ((KPT) -> KPT)? = null
        private var classificationHeader: Flow.Publisher<out String?>? = null
        private var progressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()
        private var mapBuilder: MapBuilder<*>? = null
        private var secondMapBuilder: MapBuilder<*>? = null

        fun withTotal(totalSeats: Flow.Publisher<out Int>): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            total = totalSeats
            return this
        }

        fun withMajorityLine(
            showMajority: Flow.Publisher<out Boolean>,
            majorityLabelFunc: (Int) -> String,
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            this.showMajority = showMajority
            majorityFunction = majorityLabelFunc
            return this
        }

        fun withWinner(winner: Flow.Publisher<out KT?>): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            this.winner = winner
            return this
        }

        fun withDiff(
            diff: Flow.Publisher<out Map<KPT, CT>>,
            changeHeader: Flow.Publisher<out String?>,
            changeSubhead: Flow.Publisher<out String?> = null.asOneTimePublisher(),
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            this.diff =
                current
                    .merge(diff) { c, d ->
                        val ret = LinkedHashMap<KPT, BasicResultPanel.CurrDiff<CT>>()
                        c.forEach { (k, v) -> ret[keyTemplate.toParty(k)] = createFromDiff(v, d[keyTemplate.toParty(k)]) }
                        d.forEach { (k, v) -> ret.putIfAbsent(k, createFromDiff(v)) }
                        ret
                    }
            this.changeHeader = changeHeader
            this.changeSubhead = changeSubhead
            return this
        }

        protected abstract fun createFromDiff(curr: CT, diff: CT?): BasicResultPanel.CurrDiff<CT>
        protected abstract fun createFromDiff(diff: CT): BasicResultPanel.CurrDiff<CT>

        fun withPrev(
            prev: Flow.Publisher<out Map<out KPT, PT>>,
            changeHeader: Flow.Publisher<out String?>,
            changeSubhead: Flow.Publisher<out String?> = null.asOneTimePublisher(),
            showPrevRaw: Flow.Publisher<Boolean> = false.asOneTimePublisher(),
            partyChanges: Flow.Publisher<Map<KPT, KPT>> = emptyMap<KPT, KPT>().asOneTimePublisher(),
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            this.prev = prev
            this.diff =
                current
                    .merge(Aggregators.partyChanges(prev, partyChanges) { a, b -> seatTemplate.prevCombine(a, b) }) { c, p ->
                        val ret = LinkedHashMap<KPT, BasicResultPanel.CurrDiff<CT>>()
                        c.forEach { (k, v) -> ret[keyTemplate.toParty(k)] = createFromPrev(v, p[keyTemplate.toParty(k)]) }
                        p.forEach { (k, v) -> ret.putIfAbsent(k, createFromPrev(v)) }
                        ret
                    }
            this.showPrevRaw = showPrevRaw
            this.changeHeader = changeHeader
            this.changeSubhead = changeSubhead
            return this
        }

        protected abstract fun createFromPrev(curr: CT, prev: PT?): BasicResultPanel.CurrDiff<CT>
        protected abstract fun createFromPrev(prev: PT): BasicResultPanel.CurrDiff<CT>

        fun withSwing(
            currVotes: Flow.Publisher<out Map<out KPT, Int>>,
            prevVotes: Flow.Publisher<out Map<out KPT, Int>>,
            comparator: Comparator<KPT>,
            header: Flow.Publisher<out String?>,
            partyChanges: Flow.Publisher<Map<KPT, KPT>> = emptyMap<KPT, KPT>().asOneTimePublisher(),
            swingRange: Flow.Publisher<Double> = 0.1.asOneTimePublisher(),
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            swingHeader = header
            this.currVotes = currVotes
            this.prevVotes = Aggregators.partyChanges(prevVotes, partyChanges)
            swingComparator = comparator
            this.swingRange = swingRange
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyOrCoalition?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            mapBuilder = MapBuilder.multiResult(
                shapes,
                winners.map { m -> BasicResultPanel.partyMapToResultMap(m) },
                focus,
                headerPublisher,
            )
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            mapBuilder = MapBuilder.multiResult(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            additionalHighlight: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            mapBuilder = MapBuilder.multiResult(shapes, winners, focus, additionalHighlight, headerPublisher)
            return this
        }

        fun <T> withSecondResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            secondMapBuilder = MapBuilder.multiResult(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withSecondResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            additionalHighlight: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            secondMapBuilder = MapBuilder.multiResult(shapes, winners, focus, additionalHighlight, headerPublisher)
            return this
        }

        fun withNotes(notes: Flow.Publisher<out String?>): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            this.notes = notes
            return this
        }

        fun withChangeNotes(notes: Flow.Publisher<out String?>): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            this.changeNotes = notes
            return this
        }

        fun withClassification(
            classificationFunc: (KPT) -> KPT,
            classificationHeader: Flow.Publisher<out String?>,
        ): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            this.classificationFunc = classificationFunc
            this.classificationHeader = classificationHeader
            return this
        }

        fun withProgressLabel(progressLabel: Flow.Publisher<out String?>): SeatScreenBuilder<KT, KPT, CT, PT, BAR> {
            this.progressLabel = progressLabel
            return this
        }

        fun build(textHeader: Flow.Publisher<out String?>): SeatViewPanel {
            return SeatViewPanel(
                textHeader,
                createFrame(),
                createClassificationFrame(),
                createDiffFrame(),
                if (secondMapBuilder == null) createSwingFrame() else createMapFrame(),
                if (secondMapBuilder == null) createMapFrame() else createSecondMapFrame(),
                createAltText(textHeader),
            )
        }

        private fun createFrame(): BarFrame {
            val forceSingleLine = current.map { it.size > doubleLineBarLimit() }
            val shapes = (winner ?: null.asOneTimePublisher()).merge(forceSingleLine) { win, sl -> if (win == null) emptyMap() else mapOf(win to keyTemplate.winnerShape(sl)) }
            val bars = current.merge(forceSingleLine) { c, sl -> c to sl }.merge(shapes) { (c, sl), sh ->
                c.keys
                    .sortedByDescending { keyTemplate.toParty(it).overrideSortOrder ?: seatTemplate.sortOrder(c[it]) ?: 0 }
                    .map { seatTemplate.createBar(keyTemplate.toMainBarHeader(it, sl), keyTemplate.toParty(it).color, c[it] ?: seatTemplate.default, sh[it]) }
            }
            return createBarFrameBuilder(bars)
                .withHeader(header, rightLabelPublisher = progressLabel)
                .withSubhead(subhead)
                .withNotes(notes ?: (null as String?).asOneTimePublisher())
                .also { b -> total?.let { t -> b.withMax(t.map { it * 2 / 3 }) } }
                .also { b ->
                    showMajority?.let { s ->
                        val t = total ?: throw IllegalArgumentException("Cannot show majority line without total")
                        val lines = s.merge(t) { show, total ->
                            if (show) {
                                listOf(total / 2 + 1)
                            } else {
                                emptyList()
                            }
                        }
                        b.withLines(lines) { majorityFunction!!(it) }
                    }
                }
                .build()
        }

        private fun createClassificationFrame(): BarFrame? {
            return classificationHeader?.let { classificationHeader ->
                val adjustedCurr = Aggregators.adjustKey(current, { classificationFunc!!(keyTemplate.toParty(it)) }, { v1, v2 -> seatTemplate.combine(v1, v2) })
                val bars = adjustedCurr.map { c ->
                    c.keys
                        .sortedByDescending { it.overrideSortOrder ?: seatTemplate.sortOrder(c[it]) ?: 0 }
                        .map { seatTemplate.createBar(it.name.uppercase(), it.color, c[it] ?: seatTemplate.default, null) }
                }
                return createBarFrameBuilder(bars)
                    .withHeader(classificationHeader)
                    .also { b -> total?.let { t -> b.withMax(t.map { it * 2 / 3 }) } }
                    .also { b ->
                        showMajority?.let { s ->
                            val t = total ?: throw IllegalArgumentException("Cannot show majority line without total")
                            val lines = s.merge(t) { show, total ->
                                if (show) {
                                    listOf(total / 2 + 1)
                                } else {
                                    emptyList()
                                }
                            }
                            b.withLines(lines) { majorityFunction!!(it) }
                        }
                    }
                    .build()
            }
        }
        private fun createDiffFrame(): BarFrame? {
            val diffBars = diff?.map { map ->
                val topParties = map.entries
                    .sortedByDescending { it.key.overrideSortOrder ?: seatTemplate.sortOrder(it.value.curr) }
                    .map { it.key }
                    .let {
                        if (it.size <= 10) {
                            it
                        } else {
                            it.take(9)
                        }
                    }
                Aggregators.adjustKey(
                    map,
                    { if (topParties.contains(it)) it else Party.OTHERS },
                    { a, b ->
                        BasicResultPanel.CurrDiff(
                            seatTemplate.combine(a.curr, b.curr),
                            seatTemplate.combine(a.diff, b.diff),
                        )
                    },
                ).entries.asSequence()
                    .sortedByDescending { it.key.overrideSortOrder ?: seatTemplate.sortOrder(it.value.curr) }
                    .map {
                        seatTemplate.createDiffBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            it.value.diff,
                        )
                    }
                    .toList()
            }

            val prevBars = prev?.map { map ->
                map.entries.asSequence()
                    .sortedByDescending { it.key.overrideSortOrder ?: seatTemplate.prevSortOrder(it.value) }
                    .map {
                        seatTemplate.createPrevBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            it.value,
                        )
                    }
                    .toList()
            }
            val showPrevRaw = showPrevRaw ?: false.asOneTimePublisher()
            return changeHeader?.let { changeHeader ->
                val bars = showPrevRaw.compose { showRaw -> if (showRaw) prevBars!! else diffBars!! }
                return createBarFrameBuilder(bars)
                    .withHeader(changeHeader)
                    .withSubhead(changeSubhead ?: (null as String?).asOneTimePublisher())
                    .also { b ->
                        total?.let { t ->
                            b.withLimits(
                                t.merge(showPrevRaw) { total, raw ->
                                    if (raw) {
                                        BarFrameBuilder.Limit(max = total * 2 / 3)
                                    } else {
                                        BarFrameBuilder.Limit(wingspan = (total / 20).coerceAtLeast(1))
                                    }
                                },
                            )
                        }
                    }
                    .also { b ->
                        showMajority?.merge(showPrevRaw) { sm, sr -> sm && sr }?.let { s ->
                            val t = prev?.map { it.values.sumOf { v -> seatTemplate.prevSortOrder(v) ?: 0 } } ?: 0.asOneTimePublisher()
                            val lines = s.merge(t) { show, total ->
                                if (show) {
                                    listOf(total / 2 + 1)
                                } else {
                                    emptyList()
                                }
                            }
                            b.withLines(lines) { majorityFunction!!(it) }
                        }
                    }
                    .also { b -> changeNotes?.let { cn -> b.withNotes(cn) } }
                    .build()
            }
        }

        private fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { header ->
                val prev = prevVotes!!
                val curr = currVotes!!
                val func = classificationFunc
                SwingFrameBuilder.prevCurr(
                    (if (func == null) prev else Aggregators.adjustKey(prev, func)),
                    (if (func == null) curr else Aggregators.adjustKey(curr, func)),
                    swingComparator!!,
                )
                    .withHeader(header)
                    .withRange(swingRange!!)
                    .build()
            }
        }

        private fun createMapFrame(): MapFrame? {
            return mapBuilder?.createMapFrame()
        }

        private fun createSecondMapFrame(): MapFrame? {
            return secondMapBuilder?.createMapFrame()
        }

        protected abstract fun doubleLineBarLimit(): Int

        protected abstract fun createBarFrameBuilder(bars: Flow.Publisher<List<BAR>>): BarFrameBuilder

        private fun createAltText(textHeader: Flow.Publisher<out String?>): Flow.Publisher<String> {
            val combineHeadAndSub: (String?, String?) -> String? = { h, s ->
                if (h.isNullOrEmpty()) {
                    s
                } else if (s.isNullOrEmpty()) {
                    h
                } else {
                    ("$h, $s")
                }
            }
            val barEntryLine: (String, CT, CT?) -> String = { h, c, d ->
                "$h: ${seatTemplate.labelText(c)}" + (d?.let { seatTemplate.diffLabelText(it) }?.let { " ($it)" } ?: "")
            }
            val mainText = header.merge(progressLabel) { h, p -> h + (p?.let { " [$it]" } ?: "") }.merge(subhead, combineHeadAndSub)
            val changeText = (changeHeader ?: null.asOneTimePublisher()).merge((changeSubhead ?: null.asOneTimePublisher()), combineHeadAndSub)
                .merge(showPrevRaw ?: false.asOneTimePublisher()) { text, raw -> if (raw) null else text }
            val prevRaw = (changeHeader ?: null.asOneTimePublisher()).merge((changeSubhead ?: null.asOneTimePublisher()), combineHeadAndSub)
                .merge(showPrevRaw ?: false.asOneTimePublisher()) { text, raw -> if (raw) text else null }
            val shapes = (winner ?: null.asOneTimePublisher()).map { if (it == null) emptyMap() else mapOf(it to "WINNER") }
            val barsText = current.merge(diff?.merge(showPrevRaw ?: false.asOneTimePublisher()) { d, raw -> if (raw) emptyMap() else d } ?: emptyMap<KPT, BasicResultPanel.CurrDiff<CT>>().asOneTimePublisher()) { c, d -> c to d }.merge(shapes) { (c, d), s ->
                val currText = c.keys
                    .sortedByDescending { keyTemplate.toParty(it).overrideSortOrder ?: seatTemplate.sortOrder(c[it]) ?: 0 }
                    .joinToString("") { "\n${barEntryLine(keyTemplate.toMainBarHeader(it, true), c[it] ?: seatTemplate.default, d[keyTemplate.toParty(it)]?.diff)}" + (s[it]?.let { c -> " $c" } ?: "") }
                val currPrevKeys = c.keys.map { keyTemplate.toParty(it) }
                val prevText = d.entries
                    .filter { !currPrevKeys.contains(it.key) }
                    .joinToString("") { "\n${barEntryLine(it.key.name.uppercase(), seatTemplate.default, it.value.diff)}" }
                currText + prevText
            }
            val majorityText: Flow.Publisher<String?> = showMajority
                ?.compose { maj ->
                    val majorityFunction = this.majorityFunction
                    val total = this.total
                    if (maj && majorityFunction != null && total != null) {
                        total.map { majorityFunction(it / 2 + 1) }
                    } else {
                        null.asOneTimePublisher()
                    }
                }
                ?: null.asOneTimePublisher()
            val swingText: Flow.Publisher<out String?> =
                if (currVotes == null || prevVotes == null) {
                    null.asOneTimePublisher()
                } else {
                    val classificationFunc = this.classificationFunc
                    SwingFrameBuilder.prevCurr(
                        (if (classificationFunc == null) prevVotes!! else Aggregators.adjustKey(prevVotes!!, classificationFunc)),
                        (if (classificationFunc == null) currVotes!! else Aggregators.adjustKey(currVotes!!, classificationFunc)),
                        swingComparator!!,
                    ).buildBottomText() ?: null.asOneTimePublisher()
                }.merge(this.swingHeader ?: null.asOneTimePublisher()) { text, head ->
                    if (text == null && head == null) {
                        null
                    } else if (text == null) {
                        head
                    } else if (head == null) {
                        text
                    } else {
                        "$head: $text"
                    }
                }
            val classificationText: Flow.Publisher<out String?> = classificationHeader?.merge(current) { h, c ->
                val cg: Map<KPT, CT> = Aggregators.adjustKey(c, { classificationFunc!!(keyTemplate.toParty(it)) }, { v1, v2 -> seatTemplate.combine(v1, v2) })
                (h ?: "") + cg.entries
                    .sortedByDescending { it.key.overrideSortOrder ?: seatTemplate.sortOrder(it.value) }
                    .joinToString("") { "\n${it.key.name.uppercase()}: ${it.value}" }
            } ?: null.asOneTimePublisher()
            val prevRawText: Flow.Publisher<out String?> = prev?.merge(showPrevRaw ?: false.asOneTimePublisher()) { p, raw -> if (raw) p else null }
                ?.merge(showMajority ?: false.asOneTimePublisher()) { p, m -> p to m }
                ?.merge(prevRaw) { (p, m), t ->
                    if (p == null) {
                        null
                    } else {
                        t + p.entries
                            .sortedByDescending { it.key.overrideSortOrder ?: seatTemplate.prevSortOrder(it.value) }
                            .joinToString("") { "\n${it.key.abbreviation}: ${seatTemplate.prevLabelText(it.value)}" } +
                            if (m && majorityFunction != null) ("\n${majorityFunction!!(p.values.sumOf { seatTemplate.prevSortOrder(it)!! } / 2 + 1)}") else ""
                    }
                } ?: null.asOneTimePublisher()
            return mainText.merge(changeText) { main, change -> main + (if (change == null) "" else " ($change)") }
                .merge(textHeader) { second, head -> if (head == null) second else "$head\n\n$second" }
                .merge(barsText) { first, next -> first + next }
                .merge(majorityText) { text, maj -> text + (maj?.let { "\n$it" } ?: "") }
                .merge(classificationText) { text, cl -> text + (cl?.let { "\n\n$it" } ?: "") }
                .merge(prevRawText) { text, cl -> text + (cl?.let { "\n\n$it" } ?: "") }
                .merge(swingText) { text, swing -> text + (swing?.let { "\n\n$it" } ?: "") }
        }
    }

    private class BasicSeatScreenBuilder<KT : CanOverrideSortOrder, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<out KT, Int>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
    ) : SeatScreenBuilder<KT, KPT, Int, Int, BarFrameBuilder.BasicBar>(
        current,
        header,
        subhead,
        keyTemplate,
        SingleSeatTemplate(),
    ) {

        override fun createFromDiff(curr: Int, diff: Int?): BasicResultPanel.CurrDiff<Int> {
            return BasicResultPanel.CurrDiff(curr, diff ?: 0)
        }

        override fun createFromDiff(diff: Int): BasicResultPanel.CurrDiff<Int> {
            return BasicResultPanel.CurrDiff(0, diff)
        }

        override fun createFromPrev(curr: Int, prev: Int?): BasicResultPanel.CurrDiff<Int> {
            return BasicResultPanel.CurrDiff(curr, curr - (prev ?: 0))
        }

        override fun createFromPrev(prev: Int): BasicResultPanel.CurrDiff<Int> {
            return BasicResultPanel.CurrDiff(0, -prev)
        }

        override fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createBarFrameBuilder(bars: Flow.Publisher<List<BarFrameBuilder.BasicBar>>): BarFrameBuilder {
            return BarFrameBuilder.basic(bars)
        }
    }

    private class DualSeatScreenBuilder<KT : CanOverrideSortOrder, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<out KT, Pair<Int, Int>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
        val focusLocation: FocusLocation,
    ) : SeatScreenBuilder<KT, KPT, Pair<Int, Int>, Pair<Int, Int>, BarFrameBuilder.DualBar>(
        current,
        header,
        subhead,
        keyTemplate,
        DualSeatTemplate(focusLocation),
    ) {

        enum class FocusLocation { FIRST, LAST }

        override fun createFromDiff(
            curr: Pair<Int, Int>,
            diff: Pair<Int, Int>?,
        ): BasicResultPanel.CurrDiff<Pair<Int, Int>> {
            return BasicResultPanel.CurrDiff(curr, diff ?: Pair(0, 0))
        }

        override fun createFromDiff(diff: Pair<Int, Int>): BasicResultPanel.CurrDiff<Pair<Int, Int>> {
            return BasicResultPanel.CurrDiff(Pair(0, 0), diff)
        }

        override fun createFromPrev(
            curr: Pair<Int, Int>,
            prev: Pair<Int, Int>?,
        ): BasicResultPanel.CurrDiff<Pair<Int, Int>> {
            return BasicResultPanel.CurrDiff(
                curr,
                Pair(curr.first - (prev?.first ?: 0), curr.second - (prev?.second ?: 0)),
            )
        }

        override fun createFromPrev(prev: Pair<Int, Int>): BasicResultPanel.CurrDiff<Pair<Int, Int>> {
            return BasicResultPanel.CurrDiff(Pair(0, 0), Pair(-prev.first, -prev.second))
        }

        override fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createBarFrameBuilder(bars: Flow.Publisher<List<BarFrameBuilder.DualBar>>): BarFrameBuilder {
            return if (focusLocation == FocusLocation.FIRST) {
                BarFrameBuilder.dual(bars)
            } else {
                BarFrameBuilder.dualReversed(bars)
            }
        }
    }

    private class RangeSeatScreenBuilder<KT : CanOverrideSortOrder, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<KT, IntRange>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
    ) : SeatScreenBuilder<KT, KPT, IntRange, Int, BarFrameBuilder.DualBar>(
        current,
        header,
        subhead,
        keyTemplate,
        RangeSeatTemplate(),
    ) {
        override fun createFromDiff(curr: IntRange, diff: IntRange?): BasicResultPanel.CurrDiff<IntRange> {
            return BasicResultPanel.CurrDiff(curr, diff ?: IntRange(0, 0))
        }

        override fun createFromDiff(diff: IntRange): BasicResultPanel.CurrDiff<IntRange> {
            return BasicResultPanel.CurrDiff(IntRange(0, 0), diff)
        }

        override fun createFromPrev(curr: IntRange, prev: Int?): BasicResultPanel.CurrDiff<IntRange> {
            return BasicResultPanel.CurrDiff(curr, IntRange(curr.first - (prev ?: 0), curr.last - (prev ?: 0)))
        }

        override fun createFromPrev(prev: Int): BasicResultPanel.CurrDiff<IntRange> {
            return BasicResultPanel.CurrDiff(IntRange(0, 0), IntRange(-prev, -prev))
        }

        override fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createBarFrameBuilder(bars: Flow.Publisher<List<BarFrameBuilder.DualBar>>): BarFrameBuilder {
            return BarFrameBuilder.dual(bars)
        }
    }
}