package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
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

    companion object {
        fun partySeats(
            current: CurrentSeats<PartyOrCoalition, Int>.() -> Unit,
            diff: (SeatDiff<PartyOrCoalition, Int>.() -> Unit)? = null,
            prev: (PrevSeats<PartyOrCoalition, Int>.() -> Unit)? = null,
            swing: (Swing<PartyOrCoalition>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out PartyOrCoalition, PartyOrCoalition>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            partyClassification: (PartyClassification<PartyOrCoalition>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<PartyOrCoalition, Int>().apply(current),
                diff = diff?.let { SeatDiff<PartyOrCoalition, Int>().apply(it) },
                prev = prev?.let { PrevSeats<PartyOrCoalition, Int>(partyChanges).apply(it) },
                swing = swing?.let { Swing<PartyOrCoalition>(partyChanges).apply(it) },
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = partyClassification?.let { PartyClassification<PartyOrCoalition>().apply(it) },
                map = map,
                secondMap = null,
                keyTemplate = BasicResultPanel.PartyTemplate(),
                seatTemplate = SingleSeatTemplate,
                currDiffFactory = SingleCurrDiffFactory,
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
                textHeader = title,
            ).build()
        }

        fun partySeats(
            current: CurrentSeats<PartyOrCoalition, Int>.() -> Unit,
            diff: (SeatDiff<PartyOrCoalition, Int>.() -> Unit)? = null,
            prev: (PrevSeats<PartyOrCoalition, Int>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out PartyOrCoalition, PartyOrCoalition>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            partyClassification: (PartyClassification<PartyOrCoalition>.() -> Unit)? = null,
            map: AbstractMap<*>,
            secondMap: AbstractMap<*>,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<PartyOrCoalition, Int>().apply(current),
                diff = diff?.let { SeatDiff<PartyOrCoalition, Int>().apply(it) },
                prev = prev?.let { PrevSeats<PartyOrCoalition, Int>(partyChanges).apply(it) },
                swing = null,
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = partyClassification?.let { PartyClassification<PartyOrCoalition>().apply(it) },
                map = map,
                secondMap = secondMap,
                keyTemplate = BasicResultPanel.PartyTemplate(),
                seatTemplate = SingleSeatTemplate,
                currDiffFactory = SingleCurrDiffFactory,
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
                textHeader = title,
            ).build()
        }

        fun candidateSeats(
            current: CurrentSeats<Candidate, Int>.() -> Unit,
            diff: (SeatDiff<Party, Int>.() -> Unit)? = null,
            prev: (PrevSeats<Party, Int>.() -> Unit)? = null,
            swing: (Swing<Party>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out Party, Party>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            partyClassification: (PartyClassification<Party>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<Candidate, Int>().apply(current),
                diff = diff?.let { SeatDiff<Party, Int>().apply(it) },
                prev = prev?.let { PrevSeats<Party, Int>(partyChanges).apply(it) },
                swing = swing?.let { Swing<Party>(partyChanges).apply(it) },
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = partyClassification?.let { PartyClassification<Party>().apply(it) },
                map = map,
                secondMap = null,
                keyTemplate = BasicResultPanel.CandidateTemplate(),
                seatTemplate = SingleSeatTemplate,
                currDiffFactory = SingleCurrDiffFactory,
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
                textHeader = title,
            ).build()
        }

        fun partyDualSeats(
            current: CurrentSeats<PartyOrCoalition, Pair<Int, Int>>.() -> Unit,
            diff: (SeatDiff<PartyOrCoalition, Pair<Int, Int>>.() -> Unit)? = null,
            prev: (PrevSeats<PartyOrCoalition, Pair<Int, Int>>.() -> Unit)? = null,
            swing: (Swing<PartyOrCoalition>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out PartyOrCoalition, PartyOrCoalition>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            partyClassification: (PartyClassification<PartyOrCoalition>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<PartyOrCoalition, Pair<Int, Int>>().apply(current),
                diff = diff?.let { SeatDiff<PartyOrCoalition, Pair<Int, Int>>().apply(it) },
                prev = prev?.let { PrevSeats<PartyOrCoalition, Pair<Int, Int>>(partyChanges).apply(it) },
                swing = swing?.let { Swing<PartyOrCoalition>(partyChanges).apply(it) },
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = partyClassification?.let { PartyClassification<PartyOrCoalition>().apply(it) },
                map = map,
                secondMap = null,
                keyTemplate = BasicResultPanel.PartyTemplate(),
                seatTemplate = DualSeatTemplate,
                currDiffFactory = DualCurrDiffFactory,
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
                textHeader = title,
            ).build()
        }

        fun partyDualSeatsReversed(
            current: CurrentSeats<PartyOrCoalition, Pair<Int, Int>>.() -> Unit,
            diff: (SeatDiff<PartyOrCoalition, Pair<Int, Int>>.() -> Unit)? = null,
            prev: (PrevSeats<PartyOrCoalition, Pair<Int, Int>>.() -> Unit)? = null,
            swing: (Swing<PartyOrCoalition>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out PartyOrCoalition, PartyOrCoalition>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            partyClassification: (PartyClassification<PartyOrCoalition>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<PartyOrCoalition, Pair<Int, Int>>().apply(current),
                diff = diff?.let { SeatDiff<PartyOrCoalition, Pair<Int, Int>>().apply(it) },
                prev = prev?.let { PrevSeats<PartyOrCoalition, Pair<Int, Int>>(partyChanges).apply(it) },
                swing = swing?.let { Swing<PartyOrCoalition>(partyChanges).apply(it) },
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = partyClassification?.let { PartyClassification<PartyOrCoalition>().apply(it) },
                map = map,
                secondMap = null,
                keyTemplate = BasicResultPanel.PartyTemplate(),
                seatTemplate = ReversedDualSeatTemplate,
                currDiffFactory = DualCurrDiffFactory,
                createBarFrame = {
                    BarFrameBuilder.dualReversed(
                        barsPublisher = bars,
                        headerPublisher = header,
                        rightHeaderLabelPublisher = progress,
                        subheadPublisher = subhead,
                        notesPublisher = notes,
                        limitsPublisher = limits,
                        linesPublisher = lines,
                    )
                },
                textHeader = title,
            ).build()
        }

        fun candidateDualSeats(
            current: CurrentSeats<Candidate, Pair<Int, Int>>.() -> Unit,
            diff: (SeatDiff<Party, Pair<Int, Int>>.() -> Unit)? = null,
            prev: (PrevSeats<Party, Pair<Int, Int>>.() -> Unit)? = null,
            swing: (Swing<Party>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out Party, Party>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            partyClassification: (PartyClassification<Party>.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<Candidate, Pair<Int, Int>>().apply(current),
                diff = diff?.let { SeatDiff<Party, Pair<Int, Int>>().apply(it) },
                prev = prev?.let { PrevSeats<Party, Pair<Int, Int>>(partyChanges).apply(it) },
                swing = swing?.let { Swing<Party>(partyChanges).apply(it) },
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = partyClassification?.let { PartyClassification<Party>().apply(it) },
                map = map,
                secondMap = null,
                keyTemplate = BasicResultPanel.CandidateTemplate(),
                seatTemplate = DualSeatTemplate,
                currDiffFactory = DualCurrDiffFactory,
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
                textHeader = title,
            ).build()
        }

        fun partyRangeSeats(
            current: CurrentSeats<PartyOrCoalition, IntRange>.() -> Unit,
            diff: (SeatDiff<PartyOrCoalition, IntRange>.() -> Unit)? = null,
            prev: (PrevSeats<PartyOrCoalition, Int>.() -> Unit)? = null,
            swing: (Swing<PartyOrCoalition>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out PartyOrCoalition, PartyOrCoalition>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<PartyOrCoalition, IntRange>().apply(current),
                diff = diff?.let { SeatDiff<PartyOrCoalition, IntRange>().apply(it) },
                prev = prev?.let { PrevSeats<PartyOrCoalition, Int>(partyChanges).apply(it) },
                swing = swing?.let { Swing<PartyOrCoalition>(partyChanges).apply(it) },
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = null,
                map = map,
                secondMap = null,
                keyTemplate = BasicResultPanel.PartyTemplate(),
                seatTemplate = RangeSeatTemplate,
                currDiffFactory = RangeCurrDiffFactory,
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
                textHeader = title,
            ).build()
        }

        fun partyRangeSeats(
            current: CurrentSeats<PartyOrCoalition, IntRange>.() -> Unit,
            diff: (SeatDiff<PartyOrCoalition, IntRange>.() -> Unit)? = null,
            prev: (PrevSeats<PartyOrCoalition, Int>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out PartyOrCoalition, PartyOrCoalition>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            map: AbstractMap<*>,
            secondMap: AbstractMap<*>,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<PartyOrCoalition, IntRange>().apply(current),
                diff = diff?.let { SeatDiff<PartyOrCoalition, IntRange>().apply(it) },
                prev = prev?.let { PrevSeats<PartyOrCoalition, Int>(partyChanges).apply(it) },
                swing = null,
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = null,
                map = map,
                secondMap = secondMap,
                keyTemplate = BasicResultPanel.PartyTemplate(),
                seatTemplate = RangeSeatTemplate,
                currDiffFactory = RangeCurrDiffFactory,
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
                textHeader = title,
            ).build()
        }

        fun candidateRangeSeats(
            current: CurrentSeats<Candidate, IntRange>.() -> Unit,
            diff: (SeatDiff<Party, IntRange>.() -> Unit)? = null,
            prev: (PrevSeats<Party, Int>.() -> Unit)? = null,
            swing: (Swing<Party>.() -> Unit)? = null,
            partyChanges: Flow.Publisher<out Map<out Party, Party>>? = null,
            majorityLine: (MajorityLine.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String?>,
        ): SeatViewPanel {
            return SeatScreenBuilder(
                current = CurrentSeats<Candidate, IntRange>().apply(current),
                diff = diff?.let { SeatDiff<Party, IntRange>().apply(it) },
                prev = prev?.let { PrevSeats<Party, Int>(partyChanges).apply(it) },
                swing = swing?.let { Swing<Party>(partyChanges).apply(it) },
                majority = majorityLine?.let { MajorityLine().apply(it) },
                partyClassification = null,
                map = map,
                secondMap = null,
                keyTemplate = BasicResultPanel.CandidateTemplate(),
                seatTemplate = RangeSeatTemplate,
                currDiffFactory = RangeCurrDiffFactory,
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
                textHeader = title,
            ).build()
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

    private object SingleSeatTemplate : SeatTemplate<Int, Int, BarFrameBuilder.BasicBar> {
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

    private object DualSeatTemplate :
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
                value.first,
                value.second,
                labelText(value),
                shape,
            )
        }

        override fun createPrevBar(keyLabel: String, baseColor: Color, value: Pair<Int, Int>): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                value.first,
                value.second,
                prevLabelText(value),
            )
        }

        override fun createDiffBar(keyLabel: String, baseColor: Color, diff: Pair<Int, Int>): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                diff.first,
                diff.second,
                diffLabelText(diff),
            )
        }
    }

    private object ReversedDualSeatTemplate :
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
                value.second - value.first,
                value.second,
                labelText(value),
                shape,
            )
        }

        override fun createPrevBar(keyLabel: String, baseColor: Color, value: Pair<Int, Int>): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                value.second - value.first,
                value.second,
                prevLabelText(value),
            )
        }

        override fun createDiffBar(keyLabel: String, baseColor: Color, diff: Pair<Int, Int>): BarFrameBuilder.DualBar {
            return BarFrameBuilder.DualBar(
                keyLabel,
                baseColor,
                if (
                    (diff.first != 0 && sign(diff.first.toDouble()) != sign(diff.second.toDouble())) || abs(diff.first.toDouble()) > abs(diff.second.toDouble())
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

    private object RangeSeatTemplate : SeatTemplate<IntRange, Int, BarFrameBuilder.DualBar> {
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

    class CurrentSeats<KT : Any, CT : Any> internal constructor() {
        lateinit var seats: Flow.Publisher<out Map<out KT, CT>>
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        var totalSeats: Flow.Publisher<Int>? = null
        var winner: Flow.Publisher<out KT?>? = null
        var notes: Flow.Publisher<out String?>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
    }

    sealed interface Change {
        val header: Flow.Publisher<out String?>
        val subhead: Flow.Publisher<out String?>?
        val notes: Flow.Publisher<out String?>?
    }

    class SeatDiff<KPT : PartyOrCoalition, CT : Any> internal constructor() : Change {
        lateinit var seats: Flow.Publisher<out Map<out KPT, CT>>
        override lateinit var header: Flow.Publisher<out String?>
        override var subhead: Flow.Publisher<out String?>? = null
        override var notes: Flow.Publisher<out String?>? = null

        internal fun <KT : Any> currDiff(
            current: Flow.Publisher<out Map<out KT, CT>>,
            keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
            createFromDiff: (CT?, CT?) -> BasicResultPanel.CurrDiff<CT>,
            mergeFunc: (CT, CT) -> CT,
        ): Flow.Publisher<Map<KPT, BasicResultPanel.CurrDiff<CT>>> {
            return Aggregators.adjustKey(current, keyTemplate::toParty, mergeFunc).merge(seats) { c, d ->
                val ret = LinkedHashMap<KPT, BasicResultPanel.CurrDiff<CT>>()
                c.forEach { (k, v) -> ret[k] = createFromDiff(v, d[k]) }
                d.forEach { (k, v) -> ret.putIfAbsent(k, createFromDiff(null, v)) }
                ret
            }
        }
    }

    class PrevSeats<KPT : PartyOrCoalition, PT : Any> internal constructor(private val partyChanges: Flow.Publisher<out Map<out KPT, KPT>>?) : Change {
        lateinit var seats: Flow.Publisher<out Map<out KPT, PT>>
        override lateinit var header: Flow.Publisher<out String?>
        override var subhead: Flow.Publisher<out String?>? = null
        override var notes: Flow.Publisher<out String?>? = null
        var showRaw: Flow.Publisher<Boolean>? = null

        internal fun <KT : Any, CT : Any> currDiff(
            current: Flow.Publisher<out Map<out KT, CT>>,
            keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
            seatTemplate: SeatTemplate<CT, PT, *>,
            createFromPrev: (CT?, PT?) -> BasicResultPanel.CurrDiff<CT>,
            mergeFunc: (CT, CT) -> CT,
        ): Flow.Publisher<Map<KPT, BasicResultPanel.CurrDiff<CT>>> {
            val prev = if (partyChanges == null) seats else Aggregators.partyChanges(seats, partyChanges) { a, b -> seatTemplate.prevCombine(a, b) }
            return Aggregators.adjustKey(current, keyTemplate::toParty, mergeFunc).merge(prev) { c, p ->
                val ret = LinkedHashMap<KPT, BasicResultPanel.CurrDiff<CT>>()
                c.forEach { (k, v) -> ret[k] = createFromPrev(v, p[k]) }
                p.forEach { (k, v) -> ret.putIfAbsent(k, createFromPrev(null, v)) }
                ret
            }
        }
    }

    class Swing<KPT : PartyOrCoalition> internal constructor(private val partyChanges: Flow.Publisher<out Map<out KPT, KPT>>?) {
        lateinit var currVotes: Flow.Publisher<out Map<out KPT, Int>>
        lateinit var prevVotes: Flow.Publisher<out Map<out KPT, Int>>
        lateinit var header: Flow.Publisher<out String?>
        lateinit var partyOrder: List<KPT>
        var range: Flow.Publisher<Double>? = null

        internal val prevVotesWithChanges by lazy {
            if (partyChanges == null) {
                prevVotes
            } else {
                Aggregators.partyChanges(prevVotes, partyChanges)
            }
        }
        internal val rangeOrDefault by lazy { range ?: 0.1.asOneTimePublisher() }
    }

    class MajorityLine internal constructor() {
        var show: Flow.Publisher<out Boolean>? = null
        lateinit var display: (Int) -> String

        internal fun lines(total: Flow.Publisher<Int>): BarFrameBuilder.Lines<Int> {
            val lines = if (show == null) {
                total.map { t -> listOf(t / 2 + 1) }
            } else {
                show!!.merge(total) { s, t ->
                    if (s) {
                        listOf(t / 2 + 1)
                    } else {
                        emptyList()
                    }
                }
            }
            return BarFrameBuilder.Lines.of(lines, display)
        }

        internal fun altText(total: Flow.Publisher<Int>): Flow.Publisher<String?> {
            return if (show == null) {
                total.map { display(it / 2 + 1) }
            } else {
                show!!.compose { maj ->
                    if (maj) {
                        total.map { display(it / 2 + 1) }
                    } else {
                        null.asOneTimePublisher()
                    }
                }
            }
        }
    }

    class PartyClassification<KPT : PartyOrCoalition> internal constructor() {
        lateinit var classification: (KPT) -> KPT
        lateinit var header: Flow.Publisher<out String?>
    }

    private data class CurrDiffEntry<K, P, C>(val key: K?, val party: P, val curr: C?, val diff: C?, val result: SeatScreenBuilder.Result?)

    private sealed interface CurrDiffFactory<CT, PT> {
        fun createFromDiff(curr: CT?, diff: CT?): BasicResultPanel.CurrDiff<CT>
        fun createFromPrev(curr: CT?, prev: PT?): BasicResultPanel.CurrDiff<CT>
    }

    private object SingleCurrDiffFactory : CurrDiffFactory<Int, Int> {
        override fun createFromDiff(curr: Int?, diff: Int?): BasicResultPanel.CurrDiff<Int> {
            return BasicResultPanel.CurrDiff(curr ?: 0, diff ?: 0)
        }

        override fun createFromPrev(curr: Int?, prev: Int?): BasicResultPanel.CurrDiff<Int> {
            return BasicResultPanel.CurrDiff(curr ?: 0, (curr ?: 0) - (prev ?: 0))
        }
    }

    private object DualCurrDiffFactory : CurrDiffFactory<Pair<Int, Int>, Pair<Int, Int>> {
        override fun createFromDiff(
            curr: Pair<Int, Int>?,
            diff: Pair<Int, Int>?,
        ): BasicResultPanel.CurrDiff<Pair<Int, Int>> {
            return BasicResultPanel.CurrDiff((curr ?: Pair(0, 0)), diff ?: Pair(0, 0))
        }

        override fun createFromPrev(
            curr: Pair<Int, Int>?,
            prev: Pair<Int, Int>?,
        ): BasicResultPanel.CurrDiff<Pair<Int, Int>> {
            return BasicResultPanel.CurrDiff(
                curr ?: Pair(0, 0),
                Pair((curr?.first ?: 0) - (prev?.first ?: 0), (curr?.second ?: 0) - (prev?.second ?: 0)),
            )
        }
    }

    private object RangeCurrDiffFactory : CurrDiffFactory<IntRange, Int> {
        override fun createFromDiff(curr: IntRange?, diff: IntRange?): BasicResultPanel.CurrDiff<IntRange> {
            return BasicResultPanel.CurrDiff(curr ?: IntRange(0, 0), diff ?: IntRange(0, 0))
        }

        override fun createFromPrev(curr: IntRange?, prev: Int?): BasicResultPanel.CurrDiff<IntRange> {
            return BasicResultPanel.CurrDiff(curr ?: IntRange(0, 0), IntRange((curr?.first ?: 0) - (prev ?: 0), (curr?.last ?: 0) - (prev ?: 0)))
        }
    }

    private class SeatScreenBuilder<KT : Any, KPT : PartyOrCoalition, CT : Any, PT : Any, BAR> constructor(
        private val current: CurrentSeats<KT, CT>,
        private val diff: SeatDiff<KPT, CT>?,
        private val prev: PrevSeats<KPT, PT>?,
        private val swing: Swing<KPT>?,
        private val majority: MajorityLine?,
        private val partyClassification: PartyClassification<KPT>?,
        private val map: AbstractMap<*>?,
        private val secondMap: AbstractMap<*>?,
        private val keyTemplate: BasicResultPanel.KeyTemplate<KT, KPT>,
        private val seatTemplate: SeatTemplate<CT, PT, BAR>,
        private val currDiffFactory: CurrDiffFactory<CT, PT>,
        private val createBarFrame: BarFrameArgs<BAR>.() -> BarFrame,
        private val textHeader: Flow.Publisher<out String?>,
    ) {
        private val currDiff: Flow.Publisher<Map<KPT, BasicResultPanel.CurrDiff<CT>>>? =
            diff?.currDiff(
                current.seats,
                keyTemplate,
                currDiffFactory::createFromDiff,
                seatTemplate::combine,
            )
                ?: prev?.currDiff(
                    current.seats,
                    keyTemplate,
                    seatTemplate,
                    currDiffFactory::createFromPrev,
                    seatTemplate::combine,
                )

        private val change: Change? = diff ?: prev

        fun build(): SeatViewPanel {
            return SeatViewPanel(
                textHeader,
                createFrame(),
                createClassificationFrame(),
                createDiffFrame(),
                if (secondMap == null) createSwingFrame() else map?.mapFrame,
                secondMap?.mapFrame ?: map?.mapFrame,
                createAltText(),
            )
        }

        enum class Result {
            WINNER,
        }

        private data class Entry<K, V>(val key: K, val value: V, val result: Result?)

        private val currEntries: Flow.Publisher<List<Entry<KT, CT>>> =
            current.seats.merge(current.winner ?: null.asOneTimePublisher()) { c, w ->
                c.entries
                    .sortedByDescending { keyTemplate.toParty(it.key).overrideSortOrder ?: seatTemplate.sortOrder(it.value) ?: 0 }
                    .map { Entry(it.key, it.value, if (it.key == w) Result.WINNER else null) }
            }

        private val diffEntries: Flow.Publisher<List<CurrDiffEntry<KT, KPT, CT>>>? =
            currDiff?.merge(currEntries) { diff, curr ->
                val partiesSeen = HashSet<KPT>()
                val entries1 = curr.map { e ->
                    val party = keyTemplate.toParty(e.key)
                    CurrDiffEntry<KT, KPT, CT>(
                        e.key,
                        party,
                        e.value,
                        if (partiesSeen.add(party)) diff[party]?.diff else null,
                        e.result,
                    )
                }
                val entries2 = diff.entries
                    .filter { e -> !partiesSeen.contains(e.key) }
                    .map { e ->
                        CurrDiffEntry<KT, KPT, CT>(null, e.key, null, e.value.diff, null)
                    }
                entries1 + entries2
            }

        private val classificationEntries: Flow.Publisher<List<Entry<KPT, CT>>>? =
            partyClassification?.classification?.let { classificationFunc ->
                Aggregators.adjustKey(current.seats, { classificationFunc(keyTemplate.toParty(it)) }, { v1, v2 -> seatTemplate.combine(v1, v2) })
                    .map { c ->
                        c.entries
                            .sortedByDescending { it.key.overrideSortOrder ?: seatTemplate.sortOrder(it.value) ?: 0 }
                            .map { Entry(it.key, it.value, null) }
                    }
            }

        private val prevEntries: Flow.Publisher<List<Entry<KPT, PT>>>? =
            prev?.seats?.map { prev ->
                prev.entries
                    .sortedByDescending { it.key.overrideSortOrder ?: seatTemplate.prevSortOrder(it.value) ?: 0 }
                    .map { Entry(it.key, it.value, null) }
            }

        private val prevTotal: Flow.Publisher<Int> = prev?.seats?.map { it.values.sumOf { v -> seatTemplate.prevSortOrder(v) ?: 0 } } ?: 0.asOneTimePublisher()

        private fun createFrame(): BarFrame {
            val forceSingleLine = current.seats.map { it.size > 10 }
            val bars = currEntries.merge(forceSingleLine) { c, sl ->
                c.map { seatTemplate.createBar(keyTemplate.toMainBarHeader(it.key, sl), keyTemplate.toParty(it.key).color, it.value, if (it.result == Result.WINNER) keyTemplate.winnerShape(sl) else null) }
            }
            val max = current.totalSeats?.let { t -> t.map { it * 2 / 3 } }
            return createBarFrame(
                BarFrameArgs(
                    bars = bars,
                    header = current.header,
                    progress = current.progressLabel,
                    subhead = current.subhead,
                    notes = current.notes,
                    limits = max?.map { BarFrameBuilder.Limit(max = it) },
                    lines = majority?.lines(current.totalSeats!!),
                ),
            )
        }

        private fun createClassificationFrame(): BarFrame? {
            return partyClassification?.header?.let { classificationHeader ->
                val bars = (classificationEntries ?: return@let null).map { c ->
                    c.map { seatTemplate.createBar(it.key.name.uppercase(), it.key.color, it.value, null) }
                }
                val max = current.totalSeats?.let { t -> t.map { it * 2 / 3 } }
                return createBarFrame(
                    BarFrameArgs(
                        bars = bars,
                        header = classificationHeader,
                        limits = max?.map { BarFrameBuilder.Limit(max = it) },
                        lines = majority?.lines(current.totalSeats!!),
                    ),
                )
            }
        }
        private fun createDiffFrame(): BarFrame? {
            val diffBars = diffEntries?.map { entries -> entries.filter { it.diff != null } }?.map { entries ->
                val topParties = entries
                    .map { it.key }
                    .let {
                        if (it.size <= 10) {
                            it
                        } else {
                            it.take(9)
                        }
                    }
                val topEntries = entries.filter { topParties.contains(it.key) }
                val othEntries = entries.filter { !topParties.contains(it.key) }
                    .takeUnless { it.isEmpty() }
                    ?.map { it.diff!! }
                    ?.reduce { a, b -> seatTemplate.combine(a, b) }
                    ?.let { listOf(CurrDiffEntry<KT, PartyOrCoalition, CT>(null, Party.OTHERS, null, it, null)) }
                    ?: emptyList()
                (topEntries + othEntries).asSequence()
                    .map {
                        seatTemplate.createDiffBar(
                            it.party.abbreviation.uppercase(),
                            it.party.color,
                            it.diff!!,
                        )
                    }
                    .toList()
            }

            val prevBars = prevEntries?.map { entries ->
                entries.asSequence()
                    .map {
                        seatTemplate.createPrevBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            it.value,
                        )
                    }
                    .toList()
            }
            val showPrevRaw = prev?.showRaw ?: false.asOneTimePublisher()
            val limit = current.totalSeats?.let { t ->
                t.merge(showPrevRaw) { total, raw ->
                    if (raw) {
                        BarFrameBuilder.Limit(max = total * 2 / 3)
                    } else {
                        BarFrameBuilder.Limit(wingspan = (total / 20).coerceAtLeast(1))
                    }
                }
            }
            val prevMajority = majority?.let { maj ->
                MajorityLine().apply {
                    show = if (maj.show == null) showPrevRaw else maj.show!!.merge(showPrevRaw) { sm, sr -> sm && sr }
                    display = maj.display
                }
            }
            return change?.let { change ->
                val bars = showPrevRaw.compose { showRaw -> if (showRaw) prevBars!! else diffBars!! }
                return createBarFrame(
                    BarFrameArgs(
                        bars = bars,
                        header = change.header,
                        subhead = change.subhead,
                        notes = change.notes,
                        limits = limit,
                        lines = prevMajority?.lines(prevTotal),
                    ),
                )
            }
        }

        private fun createSwingFrame(): SwingFrame? {
            if (swing == null) return null
            val prev = swing.prevVotesWithChanges
            val curr = swing.currVotes
            val func = partyClassification?.classification
            return SwingFrameBuilder.prevCurr(
                prev = (if (func == null) prev else Aggregators.adjustKey(prev, func)),
                curr = (if (func == null) curr else Aggregators.adjustKey(curr, func)),
                partyOrder = swing.partyOrder,
                range = swing.rangeOrDefault,
                header = swing.header,
            )
        }

        class BarFrameArgs<BAR>(
            val bars: Flow.Publisher<List<BAR>>,
            val header: Flow.Publisher<out String?>,
            val progress: Flow.Publisher<out String?>? = null,
            val subhead: Flow.Publisher<out String?>? = null,
            val notes: Flow.Publisher<out String?>? = null,
            val limits: Flow.Publisher<BarFrameBuilder.Limit>?,
            val lines: BarFrameBuilder.Lines<*>?,
        )

        private fun createAltText(): Flow.Publisher<String> {
            val combineHeadAndSub: (String?, String?) -> String? = { h, s ->
                if (h.isNullOrEmpty()) {
                    s
                } else if (s.isNullOrEmpty()) {
                    h
                } else {
                    ("$h, $s")
                }
            }
            val barEntryLine: (String, CT, CT?, String?) -> String = { head, curr, diff, diffSymbol ->
                "$head: ${seatTemplate.labelText(curr)}" + when {
                    diff != null && diffSymbol != null -> " (${seatTemplate.diffLabelText(diff)}$diffSymbol)"
                    diff != null -> " (${seatTemplate.diffLabelText(diff)})"
                    diffSymbol != null -> " ($diffSymbol)"
                    else -> ""
                }
            }
            val mainText = current.header.run {
                if (current.progressLabel == null) this else merge(current.progressLabel!!) { h, p -> h + (p?.let { " [$it]" } ?: "") }
            }.merge(current.subhead, combineHeadAndSub)
            val changeText = (change?.header ?: null.asOneTimePublisher()).merge((change?.subhead ?: null.asOneTimePublisher()), combineHeadAndSub)
                .merge(prev?.showRaw ?: false.asOneTimePublisher()) { text, raw -> if (raw) null else text }
            val prevRawHeader = (change?.header ?: null.asOneTimePublisher()).merge((change?.subhead ?: null.asOneTimePublisher()), combineHeadAndSub)
                .merge(prev?.showRaw ?: false.asOneTimePublisher()) { text, raw -> if (raw) text else null }
            val barsText = (prev?.showRaw ?: false.asOneTimePublisher()).compose { prevRaw ->
                if (prevRaw || diffEntries == null) {
                    currEntries.map { entries ->
                        entries.joinToString("") {
                            "\n${
                                barEntryLine(
                                    keyTemplate.toMainBarHeader(it.key, true),
                                    it.value,
                                    null,
                                    null,
                                )
                            }" + (it.result?.let { c -> " $c" } ?: "")
                        }
                    }
                } else {
                    diffEntries.map { entries ->
                        val countByParty = entries.groupingBy { it.key?.let(keyTemplate::toParty) ?: it.party }.eachCount()
                        entries.joinToString("") {
                            "\n${
                                barEntryLine(
                                    if (it.key == null) it.party.name.uppercase() else keyTemplate.toMainBarHeader(it.key, true),
                                    it.curr ?: seatTemplate.default,
                                    it.diff,
                                    if ((countByParty[it.key?.let(keyTemplate::toParty) ?: it.party] ?: 0) > 1) "^" else null,
                                )
                            }" + (it.result?.let { c -> " $c" } ?: "")
                        } +
                            if (countByParty.values.any { it > 1 }) "\n^ AGGREGATED ACROSS CANDIDATES IN PARTY" else ""
                    }
                }
            }

            val majorityText: Flow.Publisher<String?> = majority?.altText(current.totalSeats!!)
                ?: null.asOneTimePublisher()
            val swingText: Flow.Publisher<out String?> =
                createSwingFrame()?.altText?.merge(this.swing!!.header) { text, head ->
                    if (head == null) {
                        text
                    } else {
                        "$head: $text"
                    }
                } ?: null.asOneTimePublisher()
            val classificationText: Flow.Publisher<out String?> = classificationEntries?.merge(partyClassification!!.header) { e, h ->
                (h ?: "") + e.joinToString("") { "\n${it.key.name.uppercase()}: ${it.value}" }
            } ?: null.asOneTimePublisher()
            val prevRawText: Flow.Publisher<out String?> = if (prev?.showRaw == null) {
                null.asOneTimePublisher()
            } else {
                prev.showRaw!!.compose { showPrevRaw ->
                    if (!showPrevRaw) return@compose null.asOneTimePublisher()
                    val prevLines = prevEntries!!.map { entries ->
                        entries.joinToString("") { "\n${it.key.abbreviation}: ${seatTemplate.prevLabelText(it.value)}" }
                    }
                    if (majority == null) return@compose prevLines
                    val majorityLines = majority.altText(prevTotal)
                    prevLines.merge(majorityLines) { p, m -> p + (if (m == null) "" else "\n$m") }
                }.merge(prevRawHeader) { text, head -> if (text == null) null else (head + text) }
            }
            return mainText.merge(changeText) { main, change -> main + (if (change == null) "" else " ($change)") }
                .merge(textHeader) { second, head -> if (head == null) second else "$head\n\n$second" }
                .merge(barsText) { first, next -> first + next }
                .merge(majorityText) { text, maj -> text + (maj?.let { "\n$it" } ?: "") }
                .merge(classificationText) { text, cl -> text + (cl?.let { "\n\n$it" } ?: "") }
                .merge(prevRawText) { text, cl -> text + (cl?.let { "\n\n$it" } ?: "") }
                .merge(swingText) { text, swing -> text + (swing?.let { "\n\n$it" } ?: "") }
        }
    }
}
