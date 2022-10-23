package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.BarFrameBuilder.DualBar
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.CanOverrideSortOrder
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.toParty
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
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
import kotlin.math.roundToInt
import kotlin.math.sign

class BasicResultPanel private constructor(
    label: Flow.Publisher<out String?>,
    private val seatFrame: BarFrame,
    private val preferenceFrame: BarFrame?,
    private val changeFrame: BarFrame?,
    private val swingFrame: SwingFrame?,
    private val mapFrame: MapFrame?
) : GenericPanel({
    val panel = JPanel()
    panel.layout = BasicResultLayout()
    panel.background = Color.WHITE
    panel.add(seatFrame, BasicResultLayout.MAIN)
    if (preferenceFrame != null) panel.add(preferenceFrame, BasicResultLayout.PREF)
    if (changeFrame != null) panel.add(changeFrame, BasicResultLayout.DIFF)
    if (swingFrame != null) panel.add(swingFrame, BasicResultLayout.SWING)
    if (mapFrame != null) panel.add(mapFrame, BasicResultLayout.MAP)
    panel
}, label) {

    interface KeyTemplate<KT, KPT : PartyOrCoalition> {
        fun toParty(key: KT): KPT
        fun toMainBarHeader(key: KT, forceSingleLine: Boolean): String
        fun winnerShape(forceSingleLine: Boolean): Shape
        fun runoffShape(forceSingleLine: Boolean): Shape
    }

    private class PartyTemplate<P : PartyOrCoalition> : KeyTemplate<P, P> {
        override fun toParty(key: P): P {
            return key
        }

        override fun toMainBarHeader(key: P, forceSingleLine: Boolean): String {
            return key.name.uppercase()
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createRunoffShape()
        }
    }

    private class CandidateTemplate : KeyTemplate<Candidate, Party> {
        private val incumbentMarker: String

        constructor() {
            incumbentMarker = ""
        }

        constructor(incumbentMarker: String) {
            this.incumbentMarker = " $incumbentMarker"
        }

        override fun toParty(key: Candidate): Party {
            return key.party
        }

        override fun toMainBarHeader(key: Candidate, forceSingleLine: Boolean): String {
            return if (key === Candidate.OTHERS) {
                key.party.name.uppercase()
            } else {
                ("${key.name}${if (key.isIncumbent()) incumbentMarker else ""}${if (forceSingleLine) (" (" + key.party.abbreviation + ")") else ("\n" + key.party.name)}")
                    .uppercase()
            }
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createTickShape() else ImageGenerator.createHalfTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createRunoffShape() else ImageGenerator.createHalfRunoffShape()
        }
    }

    abstract class SeatScreenBuilder<KT, KPT : PartyOrCoalition, CT, PT> internal constructor(
        protected var current: Flow.Publisher<out Map<out KT, CT>>,
        protected var header: Flow.Publisher<out String?>,
        protected var subhead: Flow.Publisher<out String?>,
        protected val keyTemplate: KeyTemplate<KT, KPT>
    ) {
        protected var total: Flow.Publisher<out Int>? = null
        protected var showMajority: Flow.Publisher<out Boolean>? = null
        protected var majorityFunction: ((Int) -> String)? = null
        protected var winner: Flow.Publisher<out KT?>? = null
        protected var notes: Flow.Publisher<out String?>? = null
        protected var changeNotes: Flow.Publisher<out String?>? = null
        protected var prev: Flow.Publisher<out Map<out KPT, PT>>? = null
        protected var diff: Flow.Publisher<out Map<KPT, CurrDiff<CT>>>? = null
        protected var showPrevRaw: Flow.Publisher<Boolean>? = null
        protected var changeHeader: Flow.Publisher<out String?>? = null
        protected var changeSubhead: Flow.Publisher<out String?>? = null
        private var currVotes: Flow.Publisher<out Map<out KPT, Int>>? = null
        private var prevVotes: Flow.Publisher<out Map<out KPT, Int>>? = null
        private var swingHeader: Flow.Publisher<out String?>? = null
        private var swingComparator: Comparator<KPT>? = null
        protected var classificationFunc: ((KPT) -> KPT)? = null
        protected var classificationHeader: Flow.Publisher<out String?>? = null
        protected var progressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()
        private var mapBuilder: MapBuilder<*>? = null

        fun withTotal(totalSeats: Flow.Publisher<out Int>): SeatScreenBuilder<KT, KPT, CT, PT> {
            total = totalSeats
            return this
        }

        fun withMajorityLine(
            showMajority: Flow.Publisher<out Boolean>,
            majorityLabelFunc: (Int) -> String
        ): SeatScreenBuilder<KT, KPT, CT, PT> {
            this.showMajority = showMajority
            majorityFunction = majorityLabelFunc
            return this
        }

        fun withWinner(winner: Flow.Publisher<out KT?>): SeatScreenBuilder<KT, KPT, CT, PT> {
            this.winner = winner
            return this
        }

        @JvmOverloads
        fun withDiff(
            diff: Flow.Publisher<out Map<KPT, CT>>,
            changeHeader: Flow.Publisher<out String?>,
            changeSubhead: Flow.Publisher<out String?> = null.asOneTimePublisher()
        ): SeatScreenBuilder<KT, KPT, CT, PT> {
            this.diff =
                current
                    .merge(diff) { c, d ->
                        val ret = LinkedHashMap<KPT, CurrDiff<CT>>()
                        c.forEach { (k, v) -> ret[keyTemplate.toParty(k)] = createFromDiff(v, d[keyTemplate.toParty(k)]) }
                        d.forEach { (k, v) -> ret.putIfAbsent(k, createFromDiff(v)) }
                        ret
                    }
            this.changeHeader = changeHeader
            this.changeSubhead = changeSubhead
            return this
        }

        protected abstract fun createFromDiff(curr: CT, diff: CT?): CurrDiff<CT>
        protected abstract fun createFromDiff(diff: CT): CurrDiff<CT>

        @JvmOverloads
        fun withPrev(
            prev: Flow.Publisher<out Map<out KPT, PT>>,
            changeHeader: Flow.Publisher<out String?>,
            changeSubhead: Flow.Publisher<out String?> = null.asOneTimePublisher(),
            showPrevRaw: Flow.Publisher<Boolean> = false.asOneTimePublisher()
        ): SeatScreenBuilder<KT, KPT, CT, PT> {
            this.prev = prev
            this.diff =
                current
                    .merge(prev) { c, p ->
                        val ret = LinkedHashMap<KPT, CurrDiff<CT>>()
                        c.forEach { (k, v) -> ret[keyTemplate.toParty(k)] = createFromPrev(v, p[keyTemplate.toParty(k)]) }
                        p.forEach { (k, v) -> ret.putIfAbsent(k, createFromPrev(v)) }
                        ret
                    }
            this.showPrevRaw = showPrevRaw
            this.changeHeader = changeHeader
            this.changeSubhead = changeSubhead
            return this
        }

        protected abstract fun createFromPrev(curr: CT, prev: PT?): CurrDiff<CT>
        protected abstract fun createFromPrev(prev: PT): CurrDiff<CT>

        fun withSwing(
            currVotes: Flow.Publisher<out Map<out KPT, Int>>,
            prevVotes: Flow.Publisher<out Map<out KPT, Int>>,
            comparator: Comparator<KPT>,
            header: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, KPT, CT, PT> {
            swingHeader = header
            this.currVotes = currVotes
            this.prevVotes = prevVotes
            swingComparator = comparator
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyOrCoalition?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, KPT, CT, PT> {
            mapBuilder = MapBuilder(
                shapes,
                winners.map { m -> partyMapToResultMap(m) },
                focus,
                headerPublisher
            )
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, KPT, CT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            additionalHighlight: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, KPT, CT, PT> {
            mapBuilder = MapBuilder(shapes, winners, Pair(focus, additionalHighlight), headerPublisher)
            return this
        }

        fun withNotes(notes: Flow.Publisher<out String?>): SeatScreenBuilder<KT, KPT, CT, PT> {
            this.notes = notes
            return this
        }

        fun withChangeNotes(notes: Flow.Publisher<out String?>): SeatScreenBuilder<KT, KPT, CT, PT> {
            this.changeNotes = notes
            return this
        }

        fun withClassification(
            classificationFunc: (KPT) -> KPT,
            classificationHeader: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, KPT, CT, PT> {
            this.classificationFunc = classificationFunc
            this.classificationHeader = classificationHeader
            return this
        }

        fun withProgressLabel(progressLabel: Flow.Publisher<out String?>): SeatScreenBuilder<KT, KPT, CT, PT> {
            this.progressLabel = progressLabel
            return this
        }

        fun build(textHeader: Flow.Publisher<out String?>): BasicResultPanel {
            return BasicResultPanel(
                textHeader,
                createFrame(),
                createClassificationFrame(),
                createDiffFrame(),
                createSwingFrame(),
                createMapFrame()
            )
        }

        protected abstract fun createFrame(): BarFrame
        protected abstract fun createClassificationFrame(): BarFrame?
        protected abstract fun createDiffFrame(): BarFrame?

        private fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { header ->
                val prev = prevVotes!!
                val curr = currVotes!!
                val func = classificationFunc
                SwingFrameBuilder.prevCurr(
                    (if (func == null) prev else Aggregators.adjustKey(prev, func)),
                    (if (func == null) curr else Aggregators.adjustKey(curr, func)),
                    swingComparator!!
                )
                    .withHeader(header)
                    .build()
            }
        }

        private fun createMapFrame(): MapFrame? {
            return mapBuilder?.createMapFrame()
        }
    }

    private class BasicSeatScreenBuilder<KT : CanOverrideSortOrder, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<out KT, Int>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT, KPT>
    ) : SeatScreenBuilder<KT, KPT, Int, Int>(current, header, subhead, keyTemplate) {

        override fun createFromDiff(curr: Int, diff: Int?): CurrDiff<Int> {
            return CurrDiff(curr, diff ?: 0)
        }

        override fun createFromDiff(diff: Int): CurrDiff<Int> {
            return CurrDiff(0, diff)
        }

        override fun createFromPrev(curr: Int, prev: Int?): CurrDiff<Int> {
            return CurrDiff(curr, curr - (prev ?: 0))
        }

        override fun createFromPrev(prev: Int): CurrDiff<Int> {
            return CurrDiff(0, -prev)
        }

        private inner class Result {
            var seats: Map<out KT, Int> = emptyMap()
                set(value) {
                    field = value
                    updateBars()
                }

            var winner: KT? = null
                set(value) {
                    field = value
                    updateBars()
                }

            val barsPublisher = Publisher(calculateBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(calculateBars()) }
            private fun calculateBars(): List<BasicBar> {
                val seats = this.seats
                val winner = this.winner
                val numBars = seats.size
                return seats.entries.asSequence()
                    .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                    .map {
                        BasicBar(
                            keyTemplate.toMainBarHeader(
                                it.key,
                                numBars > doubleLineBarLimit()
                            ),
                            keyTemplate.toParty(it.key).color,
                            it.value,
                            DecimalFormat("#,##0").format(it.value),
                            if (it.key == winner) keyTemplate.winnerShape(numBars > doubleLineBarLimit()) else null
                        )
                    }
                    .toList()
            }
        }

        private fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createFrame(): BarFrame {
            val result = Result()
            current.subscribe(Subscriber { result.seats = it })
            winner?.subscribe(Subscriber { result.winner = it })
            val bars = result.barsPublisher
            var builder = BarFrameBuilder.basic(bars)
                .withHeader(header, rightLabelPublisher = progressLabel)
                .withSubhead(subhead)
                .withNotes(this.notes ?: (null as String?).asOneTimePublisher())
            val total = this.total
            if (total != null) {
                builder = builder.withMax(total.map { it * 2 / 3 })
            }
            val showMajority = showMajority
            if (showMajority != null) {
                applyMajorityLine(
                    builder,
                    showMajority,
                    this.total
                        ?: throw IllegalArgumentException("Cannot show majority line without total")
                )
            }
            return builder.build()
        }

        private fun applyMajorityLine(
            builder: BarFrameBuilder,
            showMajority: Flow.Publisher<out Boolean>?,
            total: Flow.Publisher<out Int>
        ) {
            if (showMajority != null) {
                val lines = showMajority.merge(total) {
                        show, tot ->
                    if (show) {
                        listOf(tot / 2 + 1)
                    } else {
                        emptyList()
                    }
                }
                builder.withLines(lines) { t -> majorityFunction!!(t) }
            }
        }

        override fun createClassificationFrame(): BarFrame? {
            return classificationHeader?.let { classificationHeader ->
                val bars: Flow.Publisher<out List<BasicBar>> = Aggregators.adjustKey(
                    current
                ) { classificationFunc!!(keyTemplate.toParty(it)) }
                    .map { seats ->
                        seats.entries.asSequence()
                            .sortedByDescending { it.value }
                            .map {
                                BasicBar(
                                    it.key.name.uppercase(),
                                    it.key.color,
                                    it.value
                                )
                            }
                            .toList()
                    }
                var builder = BarFrameBuilder.basic(bars).withHeader(classificationHeader)
                val total = this.total
                if (total != null) {
                    builder = builder.withMax(total.map { it * 2 / 3 })
                }
                applyMajorityLine(
                    builder,
                    showMajority,
                    this.total ?: throw IllegalArgumentException("Cannot show majority line without total")
                )
                return builder.build()
            }
        }

        override fun createDiffFrame(): BarFrame? {
            val diffBars = diff?.map { map ->
                map.entries.asSequence()
                    .sortedByDescending { it.key.overrideSortOrder ?: it.value.curr }
                    .map {
                        BasicBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            it.value.diff,
                            changeStr(it.value.diff)
                        )
                    }
                    .toList()
            }
            val prevBars = prev?.map { map ->
                map.entries.asSequence()
                    .sortedByDescending { it.value }
                    .map {
                        BasicBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            it.value
                        )
                    }
                    .toList()
            }
            val showPrevRaw = showPrevRaw ?: false.asOneTimePublisher()
            return changeHeader?.let { changeHeader ->
                val bars = showPrevRaw.compose { showRaw -> if (showRaw) prevBars!! else diffBars!! }
                var builder = BarFrameBuilder.basic(bars)
                    .withHeader(changeHeader)
                    .withSubhead(changeSubhead ?: (null as String?).asOneTimePublisher())
                val total = this.total
                if (total != null) {
                    builder = builder.withLimits(
                        total.merge(showPrevRaw) { totalSeats, showRaw ->
                            if (showRaw) {
                                BarFrameBuilder.Limit(max = totalSeats * 2 / 3)
                            } else {
                                BarFrameBuilder.Limit(wingspan = (totalSeats / 20).coerceAtLeast(1))
                            }
                        }
                    )
                }
                this.showMajority?.let { showMajority ->
                    applyMajorityLine(builder, showMajority.merge(showPrevRaw) { m, r -> m && r }, prev?.map { it.values.sum() } ?: 0.asOneTimePublisher())
                }
                val changeNotes = this.changeNotes
                if (changeNotes != null) {
                    builder = builder.withNotes(changeNotes)
                }
                return builder.build()
            }
        }

        companion object {
            private fun changeStr(seats: Int): String {
                return if (seats == 0) "\u00b10" else DecimalFormat("+0;-0").format(seats)
            }
        }
    }

    private class DualSeatScreenBuilder<KT : CanOverrideSortOrder, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<out KT, Pair<Int, Int>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT, KPT>,
        val focusLocation: FocusLocation
    ) : SeatScreenBuilder<KT, KPT, Pair<Int, Int>, Pair<Int, Int>>(current, header, subhead, keyTemplate) {

        enum class FocusLocation { FIRST, LAST }

        override fun createFromDiff(
            curr: Pair<Int, Int>,
            diff: Pair<Int, Int>?
        ): CurrDiff<Pair<Int, Int>> {
            return CurrDiff(curr, diff ?: Pair(0, 0))
        }

        override fun createFromDiff(diff: Pair<Int, Int>): CurrDiff<Pair<Int, Int>> {
            return CurrDiff(Pair(0, 0), diff)
        }

        override fun createFromPrev(
            curr: Pair<Int, Int>,
            prev: Pair<Int, Int>?
        ): CurrDiff<Pair<Int, Int>> {
            return CurrDiff(curr, Pair(curr.first - (prev?.first ?: 0), curr.second - (prev?.second ?: 0)))
        }

        override fun createFromPrev(prev: Pair<Int, Int>): CurrDiff<Pair<Int, Int>> {
            return CurrDiff(Pair(0, 0), Pair(-prev.first, -prev.second))
        }

        private inner class Result {
            var seats: Map<out KT, Pair<Int, Int>> = emptyMap()
                set(value) {
                    field = value
                    updateBars()
                }

            var winner: KT? = null
                set(value) {
                    field = value
                    updateBars()
                }

            val barsPublisher = Publisher(calculateBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(calculateBars()) }
            private fun calculateBars(): List<DualBar> {
                val seats = this.seats
                val winner = this.winner
                val count = seats.size
                return seats.entries.asSequence()
                    .sortedByDescending { it.key.overrideSortOrder ?: it.value.second }
                    .map {
                        DualBar(
                            keyTemplate.toMainBarHeader(
                                it.key,
                                count > doubleLineBarLimit()
                            ),
                            keyTemplate.toParty(it.key).color,
                            if (focusLocation == FocusLocation.FIRST) it.value.first else (it.value.second - it.value.first),
                            it.value.second,
                            DecimalFormat("#,##0").format(it.value.first) + "/" + DecimalFormat("#,##0").format(it.value.second),
                            if (it.key == winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else null
                        )
                    }
                    .toList()
            }
        }

        private fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createFrame(): BarFrame {
            val result = Result()
            current.subscribe(Subscriber { result.seats = it })
            winner?.subscribe(Subscriber { result.winner = it })
            val bars = result.barsPublisher
            var builder = if (focusLocation == FocusLocation.FIRST) {
                BarFrameBuilder.dual(bars)
            } else {
                BarFrameBuilder.dualReversed(bars)
            }
            builder = builder
                .withHeader(header, rightLabelPublisher = progressLabel)
                .withSubhead(subhead)
                .withNotes(this.notes ?: (null as String?).asOneTimePublisher())
            val total = this.total
            if (total != null) {
                builder = builder.withMax(total.map { it * 2 / 3 })
            }
            this.showMajority?.let { showMajority ->
                builder = applyMajorityLine(builder, showMajority, total ?: throw IllegalStateException("Cannot show majority line without total"))
            }
            return builder.build()
        }

        private fun applyMajorityLine(
            builder: BarFrameBuilder,
            showMajority: Flow.Publisher<out Boolean>,
            total: Flow.Publisher<out Int>
        ): BarFrameBuilder {
            val lines = showMajority.merge(total) { show, tot ->
                if (show) {
                    listOf(tot / 2 + 1)
                } else {
                    emptyList()
                }
            }
            return builder.withLines(lines) { t -> majorityFunction!!(t) }
        }

        override fun createClassificationFrame(): BarFrame? {
            if (classificationHeader == null) {
                return null
            }
            throw UnsupportedOperationException("Classification frame not supported on dual frame")
        }

        override fun createDiffFrame(): BarFrame? {
            val diffBars = diff?.map { map ->
                map.entries.asSequence()
                    .sortedByDescending { it.key.overrideSortOrder ?: it.value.curr.second }
                    .map {
                        DualBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            if (focusLocation == FocusLocation.FIRST ||
                                (it.value.diff.first != 0 && sign(it.value.diff.first.toDouble()) != sign(it.value.diff.second.toDouble())) ||
                                abs(it.value.diff.first.toDouble()) > abs(it.value.diff.second.toDouble())
                            ) {
                                it.value.diff.first
                            } else {
                                (it.value.diff.second - it.value.diff.first)
                            },
                            it.value.diff.second,
                            changeStr(it.value.diff.first) +
                                "/" +
                                changeStr(it.value.diff.second)
                        )
                    }
                    .toList()
            }
            val prevBars = prev?.map { map ->
                map.entries.asSequence()
                    .sortedByDescending { it.key.overrideSortOrder ?: it.value.second }
                    .map {
                        DualBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            if (focusLocation == FocusLocation.FIRST ||
                                (it.value.first != 0 && sign(it.value.first.toDouble()) != sign(it.value.second.toDouble())) ||
                                abs(it.value.first.toDouble()) > abs(it.value.second.toDouble())
                            ) {
                                it.value.first
                            } else {
                                (it.value.second - it.value.first)
                            },
                            it.value.second,
                            it.value.first.toString() +
                                "/" +
                                it.value.second.toString()
                        )
                    }
                    .toList()
            }
            val showPrevRaw = showPrevRaw ?: false.asOneTimePublisher()
            return changeHeader?.let { changeHeader ->
                val bars = showPrevRaw.compose { showRaw -> if (showRaw) prevBars!! else diffBars!! }
                var builder = if (focusLocation == FocusLocation.FIRST) {
                    BarFrameBuilder.dual(bars)
                } else {
                    BarFrameBuilder.dualReversed(bars)
                }
                builder = builder
                    .withHeader(changeHeader)
                    .withSubhead(changeSubhead ?: (null as String?).asOneTimePublisher())
                val total = this.total
                if (total != null) {
                    builder = builder.withLimits(
                        total.merge(showPrevRaw) { totalSeats, showRaw ->
                            if (showRaw) {
                                BarFrameBuilder.Limit(max = totalSeats * 2 / 3)
                            } else {
                                BarFrameBuilder.Limit(wingspan = (totalSeats / 20).coerceAtLeast(1))
                            }
                        }
                    )
                }
                this.showMajority?.let { showMajority ->
                    applyMajorityLine(builder, showMajority.merge(showPrevRaw) { m, r -> m && r }, prev?.map { it.values.sumOf { e -> e.second } } ?: 0.asOneTimePublisher())
                }
                val changeNotes = this.changeNotes
                if (changeNotes != null) {
                    builder = builder.withNotes(changeNotes)
                }
                return builder.build()
            }
        }

        companion object {
            private fun changeStr(seats: Int): String {
                return if (seats == 0) "\u00b10" else DecimalFormat("+0;-0").format(seats)
            }
        }
    }

    private class RangeSeatScreenBuilder<KT : CanOverrideSortOrder, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<KT, IntRange>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT, KPT>
    ) : SeatScreenBuilder<KT, KPT, IntRange, Int>(current, header, subhead, keyTemplate) {
        override fun createFromDiff(curr: IntRange, diff: IntRange?): CurrDiff<IntRange> {
            return CurrDiff(curr, diff ?: IntRange(0, 0))
        }

        override fun createFromDiff(diff: IntRange): CurrDiff<IntRange> {
            return CurrDiff(IntRange(0, 0), diff)
        }

        override fun createFromPrev(curr: IntRange, prev: Int?): CurrDiff<IntRange> {
            return CurrDiff(curr, IntRange(curr.first - (prev ?: 0), curr.last - (prev ?: 0)))
        }

        override fun createFromPrev(prev: Int): CurrDiff<IntRange> {
            return CurrDiff(IntRange(0, 0), IntRange(-prev, -prev))
        }

        private inner class Result {
            var seats: Map<out KT, IntRange> = emptyMap()
                set(value) {
                    field = value
                    updateBars()
                }

            var winner: KT? = null
                set(value) {
                    field = value
                    updateBars()
                }

            val barsPublisher = Publisher(createBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(createBars()) }
            private fun createBars(): List<DualBar> {
                val seats = this.seats
                val winner = this.winner
                val count = seats.size
                return seats.entries.asSequence()
                    .sortedByDescending { it.key.overrideSortOrder ?: (it.value.first + it.value.last) }
                    .map {
                        DualBar(
                            keyTemplate.toMainBarHeader(
                                it.key,
                                count > doubleLineBarLimit()
                            ),
                            keyTemplate.toParty(it.key).color,
                            it.value.first,
                            it.value.last,
                            it.value.first.toString() + "-" + it.value.last,
                            if (it.key == winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else null
                        )
                    }
                    .toList()
            }
        }

        private fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createFrame(): BarFrame {
            val result = Result()
            current.subscribe(Subscriber { result.seats = it })
            winner?.subscribe(Subscriber { result.winner = it })
            val bars = result.barsPublisher
            val notes = this.notes
            var builder = BarFrameBuilder.dual(bars)
                .withHeader(header, rightLabelPublisher = progressLabel)
                .withSubhead(subhead)
                .withNotes(notes ?: (null as String?).asOneTimePublisher())
            val total = this.total
            if (total != null) {
                builder = builder.withMax(total.map { it * 2 / 3 })
            }
            val showMajority = this.showMajority
            if (showMajority != null) {
                if (total == null) {
                    throw IllegalStateException("Cannot show majority without total")
                }
                builder = applyMajorityLine(builder, showMajority, total)
            }
            return builder.build()
        }

        private fun applyMajorityLine(
            builder: BarFrameBuilder,
            showMajority: Flow.Publisher<out Boolean>,
            total: Flow.Publisher<out Int>
        ): BarFrameBuilder {
            val lines = showMajority.merge(total) { show, tot ->
                if (show) {
                    listOf(tot / 2 + 1)
                } else {
                    emptyList()
                }
            }
            return builder.withLines(lines) { t -> majorityFunction!!(t) }
        }

        override fun createClassificationFrame(): BarFrame? {
            if (classificationHeader == null) {
                return null
            }
            throw UnsupportedOperationException("Classification frame not supported on range frame")
        }

        override fun createDiffFrame(): BarFrame? {
            val diffBars = diff?.map { map ->
                map.entries.asSequence()
                    .sortedByDescending {
                        it.key.overrideSortOrder ?: (it.value.curr.first + it.value.curr.last)
                    }
                    .map {
                        DualBar(
                            it.key.abbreviation.uppercase(),
                            it.key.color,
                            it.value.diff.first,
                            it.value.diff.last,
                            "(" +
                                changeStr(it.value.diff.first) +
                                ")-(" +
                                changeStr(it.value.diff.last) +
                                ")"
                        )
                    }
                    .toList()
            }
            val prevBars = prev?.map { map ->
                map.entries.asSequence()
                    .sortedByDescending { e ->
                        e.key.overrideSortOrder ?: e.value
                    }
                    .map { e ->
                        DualBar(
                            e.key.abbreviation.uppercase(),
                            e.key.color,
                            e.value,
                            e.value,
                            e.value.toString()
                        )
                    }
                    .toList()
            }
            val showPrevRaw = this.showPrevRaw ?: false.asOneTimePublisher()
            return changeHeader?.let { changeHeader ->
                val bars = showPrevRaw.compose { showRaw -> if (showRaw) prevBars!! else diffBars!! }
                var builder = BarFrameBuilder.dual(bars)
                    .withHeader(changeHeader)
                    .withSubhead(changeSubhead ?: (null as String?).asOneTimePublisher())
                val total = this.total
                if (total != null) {
                    builder = builder.withLimits(
                        total.merge(showPrevRaw) { totalSeats, showRaw ->
                            if (showRaw) {
                                BarFrameBuilder.Limit(max = totalSeats * 2 / 3)
                            } else {
                                BarFrameBuilder.Limit(wingspan = (totalSeats / 20).coerceAtLeast(1))
                            }
                        }
                    )
                }
                this.showMajority?.let { showMajority ->
                    applyMajorityLine(builder, showMajority.merge(showPrevRaw) { m, r -> m && r }, prev?.map { it.values.sum() } ?: 0.asOneTimePublisher())
                }
                val changeNotes = this.changeNotes
                if (changeNotes != null) {
                    builder = builder.withNotes(changeNotes)
                }
                return builder.build()
            }
        }

        companion object {
            private fun changeStr(seats: Int): String {
                return if (seats == 0) "\u00b10" else DecimalFormat("+0;-0").format(seats)
            }
        }
    }

    interface VoteTemplate {
        fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): String
    }

    private class PctOnlyTemplate : VoteTemplate {
        override fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): String {
            return PCT_FORMAT.format(pct)
        }
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
    }

    private class VotePctOnlyTemplate : VoteTemplate {
        override fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): String {
            return PCT_FORMAT.format(pct)
        }
    }

    abstract class VoteScreenBuilder<KT, KPT : PartyOrCoalition, CT, CPT, PT> internal constructor(
        protected var current: Flow.Publisher<out Map<out KT, CT>>,
        protected var header: Flow.Publisher<out String?>,
        protected var subhead: Flow.Publisher<out String?>,
        protected val keyTemplate: KeyTemplate<KT, KPT>,
        protected val voteTemplate: VoteTemplate,
        protected val others: KT
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
        protected var classificationFunc: ((KPT) -> KPT)? = null
        protected var classificationHeader: Flow.Publisher<out String?>? = null
        private var mapBuilder: MapBuilder<*>? = null
        private var runoffSubhead: Flow.Publisher<String>? = null
        private var winnerNotRunningAgain: Flow.Publisher<String>? = null
        protected var progressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()
        protected var preferenceProgressLabel: Flow.Publisher<out String?> = null.asOneTimePublisher()

        protected val filteredPrev: Flow.Publisher<out Map<out KPT, PT>>?
            get() {
                val prev = this.prev
                if (prev == null) return prev
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
                        val winner = p.entries.filter { it.value is Number }.maxByOrNull { (it.value as Number).toDouble() } ?: return@merge p
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
                        val winner = p.entries.filter { it.value is Number }.maxByOrNull { (it.value as Number).toDouble() } ?: return@merge true
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

        @JvmOverloads
        fun withPrev(
            prev: Flow.Publisher<out Map<out KPT, PT>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher(),
            showPrevRaw: Flow.Publisher<Boolean> = false.asOneTimePublisher()
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.prev = prev
            changeHeader = header
            changeSubhead = subhead
            this.showPrevRaw = showPrevRaw
            return this
        }

        fun withPreferences(
            preferences: Flow.Publisher<out Map<out KT, CT>>,
            preferenceHeader: Flow.Publisher<out String?>,
            preferenceSubhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            currPreferences = preferences
            this.preferenceHeader = preferenceHeader
            this.preferenceSubhead = preferenceSubhead
            return this
        }

        fun withPrevPreferences(
            prevPreferences: Flow.Publisher<out Map<out KPT, PT>>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.prevPreferences = prevPreferences
            return this
        }

        fun withWinner(winner: Flow.Publisher<out KT?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.winner = winner
            return this
        }

        fun withRunoff(runoff: Flow.Publisher<out Set<KT>?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.runoff = runoff
            return this
        }

        fun withPctReporting(pctReporting: Flow.Publisher<Double>): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.pctReporting = pctReporting
            return this
        }

        fun withPreferencePctReporting(
            preferencePctReporting: Flow.Publisher<out Double>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.preferencePctReporting = preferencePctReporting
            return this
        }

        fun withSwing(
            comparator: Comparator<KPT>?,
            header: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            swingComparator = comparator
            swingHeader = header
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyOrCoalition?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners.map { m -> partyMapToResultMap(m) }, focus, headerPublisher)
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out Party?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty.map { party -> PartyResult.elected(party) }, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Pair<Flow.Publisher<out List<T>?>, Flow.Publisher<out List<T>?>>,
            headerPublisher: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out PartyResult?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out PartyResult?>,
            focus: Flow.Publisher<out List<T>?>,
            additionalHighlight: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, additionalHighlight, header)
            return this
        }

        fun withMajorityLine(
            showMajority: Flow.Publisher<out Boolean>,
            majorityLabel: String
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.showMajority = showMajority
            this.majorityLabel = majorityLabel
            return this
        }

        fun withNotes(notes: Flow.Publisher<out String?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.notes = notes
            return this
        }

        fun withLimit(limit: Int, vararg mandatoryParties: KPT): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            require(limit > 0) { "Invalid limit: $limit" }
            this.limit = limit
            this.mandatoryParties = setOf(*mandatoryParties)
            return this
        }

        fun withClassification(
            classificationFunc: (KPT) -> KPT,
            classificationHeader: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.classificationFunc = classificationFunc
            this.classificationHeader = classificationHeader
            return this
        }

        fun inRunoffMode(changeSubhead: Flow.Publisher<String>): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.runoffSubhead = changeSubhead
            return this
        }

        fun whenWinnerNotRunningAgain(changeSubhead: Flow.Publisher<String>): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.winnerNotRunningAgain = changeSubhead
            return this
        }

        fun withProgressLabel(progressLabel: Flow.Publisher<out String?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.progressLabel = progressLabel
            return this
        }

        fun withPreferenceProgressLabel(progressLabel: Flow.Publisher<out String?>): VoteScreenBuilder<KT, KPT, CT, CPT, PT> {
            this.preferenceProgressLabel = progressLabel
            return this
        }

        fun build(textHeader: Flow.Publisher<out String?>): BasicResultPanel {
            return BasicResultPanel(
                textHeader,
                createFrame(),
                if (classificationHeader == null) createPreferenceFrame() else createClassificationFrame(),
                createDiffFrame(),
                createSwingFrame(),
                createMapFrame()
            )
        }

        protected abstract fun createFrame(): BarFrame
        protected abstract fun createPreferenceFrame(): BarFrame?
        protected abstract fun createClassificationFrame(): BarFrame?
        protected abstract fun createDiffFrame(): BarFrame?
        protected abstract fun createSwingFrame(): SwingFrame?

        private fun createMapFrame(): MapFrame? {
            return mapBuilder?.createMapFrame()
        }
    }

    private class BasicVoteScreenBuilder<KT, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<out KT, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT, KPT>,
        voteTemplate: VoteTemplate,
        others: KT
    ) : VoteScreenBuilder<KT, KPT, Int?, Double, Int>(current, header, subhead, keyTemplate, voteTemplate, others) {
        private inner class Result(private val isPreference: Boolean) {
            var votes: Map<out KT, Int?> = emptyMap()
                set(value) {
                    field = value
                    updateBars()
                }

            var winner: KT? = null
                set(value) {
                    field = value
                    updateBars()
                }

            var runoff: Set<KT>? = null
                set(value) {
                    field = value
                    updateBars()
                }

            val barsPublisher = Publisher(calculateBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(calculateBars()) }
            private fun calculateBars(): List<BasicBar> {
                val votes = this.votes
                val winner = this.winner
                val runoff = this.runoff
                val total = votes.values.filterNotNull().sum()

                @Suppress("UNCHECKED_CAST")
                val mandatory = sequenceOf(
                    votes.keys.asSequence()
                        .filter { mandatoryParties.contains(keyTemplate.toParty(it)) },
                    (runoff?.asSequence() ?: emptySequence()),
                    sequenceOf(winner)
                        .filter { obj -> obj != null }
                )
                    .flatten()
                    .filter { it != null }
                    .map { it as Any }
                    .toList().toTypedArray() as Array<KT>
                val aggregatedResult = Aggregators.topAndOthers(votes, limit, others, *mandatory)
                val count = aggregatedResult.size
                val partialDeclaration = votes.values.any { it == null }
                return aggregatedResult.entries.asSequence()
                    .sortedByDescending {
                        if (it.key === others) {
                            Int.MIN_VALUE
                        } else {
                            (it.value ?: -1)
                        }
                    }
                    .map {
                        val pct = it.value?.toDouble()?.div(total) ?: Double.NaN
                        val valueLabel: String = when {
                            count == 1 -> {
                                if (isPreference) "ELECTED" else "UNCONTESTED"
                            }
                            pct.isNaN() -> {
                                "WAITING..."
                            }
                            partialDeclaration -> {
                                THOUSANDS_FORMAT.format(it.value)
                            }
                            else -> {
                                voteTemplate.toBarString(
                                    it.value!!,
                                    pct,
                                    count > doubleLineBarLimit()
                                )
                            }
                        }
                        val shape: Shape? = if (it.key == winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else if ((runoff ?: emptySet()).contains(it.key)) keyTemplate.runoffShape(count > doubleLineBarLimit()) else null
                        BasicBar(
                            keyTemplate.toMainBarHeader(it.key, count > doubleLineBarLimit()),
                            keyTemplate.toParty(it.key).color,
                            if (pct.isNaN()) 0 else pct,
                            valueLabel,
                            shape
                        )
                    }
                    .toList()
            }
        }

        private fun doubleLineBarLimit(): Int {
            return if (currPreferences == null) 10 else 0
        }

        override fun createFrame(): BarFrame {
            val result = Result(false)
            current.subscribe(Subscriber { result.votes = it })
            winner?.subscribe(Subscriber { result.winner = it })
            runoff?.subscribe(Subscriber { result.runoff = it })
            val bars = result.barsPublisher
            val notes = this.notes
            val pctReporting = this.pctReporting
            val builder = BarFrameBuilder.basic(bars)
                .withHeader(header, rightLabelPublisher = progressLabel)
                .withSubhead(subhead)
                .withNotes(notes ?: (null as String?).asOneTimePublisher())
                .withMax(
                    pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher()
                )
            applyMajorityLine(builder)
            return builder.build()
        }

        private fun applyMajorityLine(builder: BarFrameBuilder) {
            val showMajority = this.showMajority
            val pctReporting = this.pctReporting
            if (showMajority != null) {
                val lines = showMajority.merge(
                    pctReporting ?: 1.0.asOneTimePublisher()
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

        private inner class Change {
            var currVotes: Map<out KT, Int?> = emptyMap()
                set(value) {
                    field = value
                    updateBars()
                }

            var prevVotes: Map<out KPT, Int> = emptyMap()
                set(value) {
                    field = value
                    updateBars()
                }

            var showPrevRaw: Boolean = false
                set(value) {
                    field = value
                    updateBars()
                }

            val barsPublisher = Publisher(createBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(createBars()) }
            private fun createBars(): List<BasicBar> {
                val cVotes = this.currVotes
                if (cVotes.values.any { it == null }) {
                    return emptyList()
                }
                val pVotes = this.prevVotes
                val prevWinner: KPT? = pVotes.entries
                    .maxByOrNull { it.value }
                    ?.key
                val prevHasOther = pVotes.containsKey(Party.OTHERS as PartyOrCoalition)
                val partiesToShow = sequenceOf(
                    sequenceOf(prevWinner),
                    cVotes.entries
                        .asSequence()
                        .filter { !prevHasOther || pVotes.containsKey(keyTemplate.toParty(it.key)) }
                        .sortedByDescending { it.value!! }
                        .map { keyTemplate.toParty(it.key) }
                ).flatten().filterNotNull().distinct().take(10).toSet()
                val currTotal = cVotes.values.filterNotNull().sum()
                val prevTotal = pVotes.values.sum()
                if (showPrevRaw) {
                    return if (prevTotal == 0) {
                        emptyList()
                    } else {
                        pVotes.entries.asSequence()
                            .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                            .map {
                                val pct = it.value.toDouble() / prevTotal
                                BasicBar(
                                    it.key.abbreviation.uppercase(),
                                    it.key.color,
                                    pct,
                                    DecimalFormat("0.0%").format(pct)
                                )
                            }
                            .toList()
                    }
                }
                if (currTotal == 0 || prevTotal == 0) {
                    return emptyList()
                }
                val partyTotal = Aggregators.topAndOthers(
                    consolidate(currTotalByParty(cVotes), partiesToShow),
                    limit,
                    Party.OTHERS as PartyOrCoalition,
                    mandatoryParties
                )
                val finalPartiesToShow = sequenceOf(
                    partyTotal.keys.asSequence(),
                    pVotes.entries.asSequence().filter { !partyTotal.containsKey(it.key) }.map { Party.OTHERS }
                ).flatten().toSet()
                val prevVotes: Map<PartyOrCoalition, Int> = pVotes.entries
                    .groupingBy { if (finalPartiesToShow.contains(it.key)) it.key else Party.OTHERS }
                    .fold(0) { a, e -> a + e.value }
                return finalPartiesToShow.asSequence()
                    .sortedByDescending { it.overrideSortOrder ?: (partyTotal[it] ?: 0) }
                    .map { e: PartyOrCoalition ->
                        val cpct = 1.0 * (partyTotal[e] ?: 0) / currTotal
                        val ppct = 1.0 * (prevVotes[e] ?: 0) / prevTotal
                        BasicBar(
                            e.abbreviation.uppercase(),
                            e.color,
                            cpct - ppct,
                            DecimalFormat("+0.0%;-0.0%").format(cpct - ppct)
                        )
                    }
                    .toList()
            }
        }

        override fun createPreferenceFrame(): BarFrame? {
            return currPreferences?.let { currPreferences ->
                val result = Result(true)
                currPreferences.subscribe(Subscriber { result.votes = it })
                winner?.subscribe(Subscriber { result.winner = it })
                val bars = result.barsPublisher
                val preferencePctReporting = this.preferencePctReporting
                return BarFrameBuilder.basic(bars)
                    .withHeader(preferenceHeader!!, rightLabelPublisher = preferenceProgressLabel)
                    .withSubhead(preferenceSubhead ?: (null as String?).asOneTimePublisher())
                    .withLines(
                        preferencePctReporting?.map { listOf(0.5 / it.coerceAtLeast(1e-6)) }
                            ?: listOf(0.5).asOneTimePublisher()
                    ) { "50%" }
                    .withMax(
                        preferencePctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) }
                            ?: (2.0 / 3).asOneTimePublisher()
                    )
                    .build()
            }
        }

        override fun createClassificationFrame(): BarFrame? {
            return classificationHeader?.let { classificationHeader ->
                val bars = Aggregators.adjustKey(
                    current.map { it.mapValues { e -> e.value ?: throw UnsupportedOperationException("Classifications not supported for partial declarations") } }
                ) { classificationFunc!!(keyTemplate.toParty(it)) }
                    .map { votes ->
                        val total = votes.values.sum()
                        votes.entries.asSequence()
                            .sortedByDescending { it.value }
                            .map {
                                BasicBar(
                                    it.key.name.uppercase(),
                                    it.key.color,
                                    1.0 * it.value / total,
                                    voteTemplate.toBarString(
                                        it.value,
                                        1.0 * it.value / total,
                                        true
                                    )
                                )
                            }
                            .toList()
                    }
                val pctReporting = this.pctReporting
                val builder = BarFrameBuilder.basic(bars)
                    .withHeader(classificationHeader)
                    .withMax(
                        pctReporting?.map { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: (2.0 / 3).asOneTimePublisher()
                    )
                applyMajorityLine(builder)
                return builder.build()
            }
        }

        override fun createDiffFrame(): BarFrame? {
            return filteredPrev?.let { prev ->
                val showPrevRaw = this.showPrevRaw ?: false.asOneTimePublisher()
                val change = Change()
                current.subscribe(Subscriber { change.currVotes = it })
                prev.subscribe(Subscriber { change.prevVotes = it })
                showPrevRaw.subscribe(Subscriber { change.showPrevRaw = it })
                val bars = change.barsPublisher
                val pctReporting = this.pctReporting
                return BarFrameBuilder.basic(bars)
                    .withLimits(
                        showPrevRaw.merge(pctReporting ?: 1.0.asOneTimePublisher()) { showRaw, pct ->
                            if (showRaw) {
                                BarFrameBuilder.Limit(max = 2.0 / 3 / pct.coerceAtLeast(1e-6))
                            } else {
                                BarFrameBuilder.Limit(wingspan = 0.1 / pct.coerceAtLeast(1e-6))
                            }
                        }
                    )
                    .withHeader(changeHeader!!)
                    .withSubhead(filteredChangeSubhead ?: (null as String?).asOneTimePublisher())
                    .build()
            }
        }

        override fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { swingHeader ->
                val curr: Flow.Publisher<out Map<out KPT, Int>>
                val prev: Flow.Publisher<out Map<out KPT, Int>>
                val currPreferences = this.currPreferences
                val prevPreferences = this.prevPreferences
                if (currPreferences != null && prevPreferences != null) {
                    curr = currPreferences.map { currTotalByParty(it) }
                    prev = prevPreferences
                        .merge(
                            currPreferences.map { currTotalByParty(it) }
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
                    swingComparator!!
                )
                    .withHeader(swingHeader)
                    .build()
            }
        }

        private fun currTotalByParty(curr: Map<out KT, Int?>): Map<KPT, Int> {
            if (curr.values.any { it == null }) {
                return emptyMap()
            }
            return curr.entries
                .groupingBy { keyTemplate.toParty(it.key) }
                .fold(0) { a, e -> a + (e.value ?: 0) }
        }

        private fun consolidate(votes: Map<KPT, Int>, parties: Set<KPT>): Map<PartyOrCoalition, Int> {
            return votes.entries.groupingBy { if (parties.contains(it.key)) it.key else Party.OTHERS }.fold(0) { a, e -> a + e.value }
        }
    }

    private class RangeVoteScreenBuilder<KT, KPT : PartyOrCoalition>(
        current: Flow.Publisher<out Map<KT, ClosedRange<Double>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT, KPT>,
        voteTemplate: VoteTemplate,
        others: KT
    ) : VoteScreenBuilder<KT, KPT, ClosedRange<Double>, Double, Int>(current, header, subhead, keyTemplate, voteTemplate, others) {
        override fun createFrame(): BarFrame {
            val bars = current.map { curr ->
                curr.entries.asSequence()
                    .sortedByDescending { if (it.key === others) Double.MIN_VALUE else (it.value.start + it.value.endInclusive) }
                    .map {
                        val valueLabel = (
                            DECIMAL_FORMAT.format(100 * it.value.start) +
                                "-" +
                                DecimalFormat("0.0").format(100 * it.value.endInclusive) +
                                "%"
                            )
                        DualBar(
                            keyTemplate.toMainBarHeader(it.key, false),
                            keyTemplate.toParty(it.key).color,
                            it.value.start,
                            it.value.endInclusive,
                            valueLabel
                        )
                    }
                    .toList()
            }
            val notes = notes
            var builder = BarFrameBuilder.dual(bars)
                .withHeader(header, rightLabelPublisher = progressLabel)
                .withSubhead(subhead)
                .withNotes(notes ?: (null as String?).asOneTimePublisher())
                .withMax((2.0 / 3).asOneTimePublisher())
            val showMajority = showMajority
            if (showMajority != null) {
                val lines = showMajority.map {
                    if (it) listOf(0.5) else emptyList()
                }
                builder = builder.withLines(lines) { majorityLabel!! }
            }
            return builder.build()
        }

        private inner class Change {
            var currVotes: Map<out KT, ClosedRange<Double>> = emptyMap()
                set(value) {
                    field = value
                    updateBars()
                }

            var prevVotes: Map<out KPT, Int> = emptyMap()
                set(value) {
                    field = value
                    updateBars()
                }

            var showPrevRaw: Boolean = false
                set(value) {
                    field = value
                    updateBars()
                }

            val barsPublisher = Publisher(createBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(createBars()) }
            private fun createBars(): List<DualBar> {
                val pVotes = this.prevVotes
                val prevTotal = pVotes.values.sum()
                if (prevTotal == 0) {
                    return emptyList()
                }
                if (showPrevRaw) {
                    return pVotes.entries.asSequence()
                        .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                        .map {
                            val pct = it.value.toDouble() / prevTotal
                            DualBar(
                                it.key.abbreviation.uppercase(),
                                it.key.color,
                                pct,
                                pct,
                                DecimalFormat("0.0%").format(pct)
                            )
                        }.toList()
                }
                val cVotes = this.currVotes
                val partyTotal = cVotes.entries.groupingBy { keyTemplate.toParty(it.key) }
                    .fold(0.0..0.0) { a, e -> (a.start + e.value.start)..(a.endInclusive + e.value.endInclusive) }
                val finalPartiesToShow = sequenceOf(
                    partyTotal.keys.asSequence(),
                    pVotes.entries.asSequence().filter { !partyTotal.containsKey(it.key) }.map { Party.OTHERS }
                ).flatten().toSet()
                val prevVotes = pVotes.entries
                    .groupingBy { if (partyTotal.containsKey(it.key)) it.key else Party.OTHERS }
                    .fold(0) { a, e -> a + e.value }
                return finalPartiesToShow.asSequence()
                    .sortedByDescending { e -> e.overrideSortOrder?.toDouble() ?: partyTotal[e]!!.let { it.start + it.endInclusive } }
                    .map {
                        val range = partyTotal[it] ?: (0.0..0.0)
                        val cpctMin = range.start
                        val cpctMax = range.endInclusive
                        val ppct = 1.0 * prevVotes.getOrDefault(it, 0) / prevTotal
                        DualBar(
                            it.abbreviation.uppercase(),
                            it.color,
                            cpctMin - ppct,
                            cpctMax - ppct,
                            "(" +
                                CHANGE_DECIMAL_FORMAT.format(100.0 * (cpctMin - ppct)) +
                                ")-(" +
                                CHANGE_DECIMAL_FORMAT.format(100.0 * (cpctMax - ppct)) +
                                ")%"
                        )
                    }
                    .toList()
            }
        }

        override fun createDiffFrame(): BarFrame? {
            return filteredPrev?.let { prev ->
                val showPrevRaw = this.showPrevRaw ?: false.asOneTimePublisher()
                val change = Change()
                current.subscribe(Subscriber { change.currVotes = it })
                prev.subscribe(Subscriber { change.prevVotes = it })
                showPrevRaw.subscribe(Subscriber { change.showPrevRaw = it })
                val bars = change.barsPublisher
                return BarFrameBuilder.dual(bars)
                    .withLimits(
                        showPrevRaw.merge(pctReporting ?: 1.0.asOneTimePublisher()) { showRaw, pct ->
                            if (showRaw) {
                                BarFrameBuilder.Limit(max = 2.0 / 3 / pct.coerceAtLeast(1e-6))
                            } else {
                                BarFrameBuilder.Limit(wingspan = 0.1 / pct.coerceAtLeast(1e-6))
                            }
                        }
                    )
                    .withHeader(changeHeader!!)
                    .withSubhead(filteredChangeSubhead ?: (null as String?).asOneTimePublisher())
                    .build()
            }
        }

        override fun createClassificationFrame(): BarFrame? {
            return classificationHeader?.let { throw UnsupportedOperationException("Classifications not supported on ranges") }
        }

        override fun createPreferenceFrame(): BarFrame? {
            return currPreferences?.let { currPreferences ->
                val bars = currPreferences.map { r ->
                    r.entries.asSequence()
                        .sortedByDescending { if (it.key === others) Double.MIN_VALUE else (it.value.start + it.value.endInclusive) }
                        .map {
                            val valueLabel = (
                                DECIMAL_FORMAT.format(100 * it.value.start) +
                                    "-" +
                                    DecimalFormat("0.0").format(100 * it.value.endInclusive) +
                                    "%"
                                )
                            DualBar(
                                keyTemplate.toMainBarHeader(it.key, false),
                                keyTemplate.toParty(it.key).color,
                                it.value.start,
                                it.value.endInclusive,
                                valueLabel
                            )
                        }
                        .toList()
                }
                var builder = BarFrameBuilder.dual(bars)
                    .withHeader(preferenceHeader!!, rightLabelPublisher = preferenceProgressLabel)
                    .withSubhead(preferenceSubhead ?: (null as String?).asOneTimePublisher())
                    .withMax((2.0 / 3).asOneTimePublisher())
                val lines = listOf(0.5).asOneTimePublisher()
                builder = builder.withLines(lines) { "50%" }
                return builder.build()
            }
        }

        override fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { swingHeader ->
                val curr = current
                    .map { m ->
                        m.entries.groupingBy { keyTemplate.toParty(it.key) }
                            .fold(0.0..0.0) { a, e -> (a.start + e.value.start)..(a.endInclusive + e.value.endInclusive) }
                    }
                    .map { m ->
                        m.mapValues { e -> e.value.let { r -> (1000000 * (r.start + r.endInclusive) / 2).roundToInt() } }
                    }
                return SwingFrameBuilder.prevCurr(filteredPrev!!, curr, swingComparator!!)
                    .withHeader(swingHeader)
                    .build()
            }
        }

        companion object {
            private val DECIMAL_FORMAT = DecimalFormat("0.0")
            private val CHANGE_DECIMAL_FORMAT = DecimalFormat("+0.0;-0.0")
        }
    }

    class PartyQuotaScreenBuilder<P : PartyOrCoalition>(
        private val quotas: Flow.Publisher<out Map<out P, Double>>,
        private val totalSeats: Flow.Publisher<out Int>,
        private val header: Flow.Publisher<out String?>,
        private val subhead: Flow.Publisher<out String?>
    ) {
        private var prevQuotas: Flow.Publisher<out Map<out P, Double>>? = null
        private var changeHeader: Flow.Publisher<out String>? = null
        private var progressLabel: Flow.Publisher<out String?>? = null

        private var swingCurrVotes: Flow.Publisher<out Map<out P, Int>>? = null
        private var swingPrevVotes: Flow.Publisher<out Map<out P, Int>>? = null
        private var swingComparator: Comparator<P>? = null
        private var swingHeader: Flow.Publisher<out String?>? = null

        private var mapBuilder: MapBuilder<*>? = null

        fun withPrev(
            prevQuotas: Flow.Publisher<out Map<out P, Double>>,
            changeHeader: Flow.Publisher<out String>
        ): PartyQuotaScreenBuilder<P> {
            this.prevQuotas = prevQuotas
            this.changeHeader = changeHeader
            return this
        }

        fun withSwing(
            currVotes: Flow.Publisher<out Map<out P, Int>>,
            prevVotes: Flow.Publisher<out Map<out P, Int>>,
            comparator: Comparator<P>,
            header: Flow.Publisher<out String?>
        ): PartyQuotaScreenBuilder<P> {
            this.swingCurrVotes = currVotes
            this.swingPrevVotes = prevVotes
            this.swingComparator = comparator
            this.swingHeader = header
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out P?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): PartyQuotaScreenBuilder<P> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty.map { PartyResult.elected(it?.toParty()) }, focus, header)
            return this
        }

        fun withProgressLabel(progressLabel: Flow.Publisher<out String?>): PartyQuotaScreenBuilder<P> {
            this.progressLabel = progressLabel
            return this
        }

        fun build(textHeader: Flow.Publisher<out String>): BasicResultPanel {
            return BasicResultPanel(
                textHeader,
                createFrame(),
                null,
                createDiffFrame(),
                createSwingFrame(),
                mapBuilder?.createMapFrame()
            )
        }

        private fun createFrame(): BarFrame {
            return BarFrame(
                barsPublisher = quotas.map { q ->
                    q.entries.asSequence()
                        .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }
                        .map {
                            BarFrame.Bar(
                                leftText = it.key.name.uppercase(),
                                rightText = DecimalFormat("0.00").format(it.value) + " QUOTAS",
                                series = listOf(it.key.color to it.value)
                            )
                        }
                        .toList()
                },
                headerPublisher = header,
                subheadTextPublisher = subhead,
                maxPublisher = totalSeats,
                linesPublisher = totalSeats.map { lines -> (1 until lines).map { BarFrame.Line(it, "$it QUOTA${if (it == 1) "" else "S"}") } },
                headerLabelsPublisher = progressLabel?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) }
            )
        }

        private fun createDiffFrame(): BarFrame? {
            if (prevQuotas == null) return null
            return BarFrame(
                barsPublisher = quotas.merge(prevQuotas!!) { curr, prev ->
                    if (curr.isEmpty()) {
                        emptyList()
                    } else {
                        sequenceOf(
                            curr.asSequence().sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }.map { it.key },
                            prev.keys.asSequence().filter { !curr.containsKey(it) }.sortedByDescending { it.overrideSortOrder ?: 0 }
                        )
                            .flatten()
                            .distinct()
                            .map { party ->
                                val diff = (curr[party] ?: 0.0) - (prev[party] ?: 0.0)
                                BarFrame.Bar(
                                    leftText = party.abbreviation.uppercase(),
                                    rightText = DecimalFormat("+0.00;-0.00").format(diff),
                                    series = listOf(party.color to diff)
                                )
                            }
                            .toList()
                    }
                },
                headerPublisher = changeHeader ?: (null as String?).asOneTimePublisher(),
                maxPublisher = 1.asOneTimePublisher(),
                minPublisher = (-1).asOneTimePublisher()
            )
        }

        private fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { header ->
                val prev = swingPrevVotes!!
                val curr = swingCurrVotes!!
                SwingFrameBuilder.prevCurr(
                    prev,
                    curr,
                    swingComparator!!
                )
                    .withHeader(header)
                    .build()
            }
        }
    }

    class CurrDiff<CT>(val curr: CT, val diff: CT)
    companion object {
        private val PCT_FORMAT = DecimalFormat("0.0%")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")
        private fun <T> partyMapToResultMap(m: Map<T, PartyOrCoalition?>): Map<T, PartyResult?> {
            return m.mapValues { e -> e.value?.let { PartyResult.elected(it.toParty()) } }
        }

        fun <P : PartyOrCoalition> partySeats(
            seats: Flow.Publisher<out Map<out P, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<P, P, Int, Int> {
            return BasicSeatScreenBuilder(
                seats,
                header,
                subhead,
                PartyTemplate()
            )
        }

        fun candidateSeats(
            seats: Flow.Publisher<out Map<Candidate, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Candidate, Party, Int, Int> {
            return BasicSeatScreenBuilder(
                seats,
                header,
                subhead,
                CandidateTemplate()
            )
        }

        fun <P : PartyOrCoalition> partyDualSeats(
            seats: Flow.Publisher<out Map<out P, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<P, P, Pair<Int, Int>, Pair<Int, Int>> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                PartyTemplate(),
                DualSeatScreenBuilder.FocusLocation.FIRST
            )
        }

        fun <P : PartyOrCoalition> partyDualSeatsReversed(
            seats: Flow.Publisher<out Map<P, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<P, P, Pair<Int, Int>, Pair<Int, Int>> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                PartyTemplate(),
                DualSeatScreenBuilder.FocusLocation.LAST
            )
        }

        fun candidateDualSeats(
            seats: Flow.Publisher<out Map<Candidate, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Candidate, Party, Pair<Int, Int>, Pair<Int, Int>> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                CandidateTemplate(),
                DualSeatScreenBuilder.FocusLocation.FIRST
            )
        }

        fun <P : PartyOrCoalition> partyRangeSeats(
            seats: Flow.Publisher<out Map<P, IntRange>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<P, P, IntRange, Int> {
            return RangeSeatScreenBuilder(
                seats,
                header,
                subhead,
                PartyTemplate()
            )
        }

        fun candidateRangeSeats(
            seats: Flow.Publisher<out Map<Candidate, IntRange>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Candidate, Party, IntRange, Int> {
            return RangeSeatScreenBuilder(
                seats,
                header,
                subhead,
                CandidateTemplate()
            )
        }

        fun <P : PartyOrCoalition> partyVotes(
            votes: Flow.Publisher<out Map<out P, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<P, P, Int?, Double, Int> {
            @Suppress("UNCHECKED_CAST")
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                PartyTemplate(),
                PctOnlyTemplate(),
                Party.OTHERS as P
            )
        }

        fun candidateVotes(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<Candidate, Party, Int?, Double, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                CandidateTemplate(),
                VotePctTemplate(),
                Candidate.OTHERS
            )
        }

        fun candidateVotesPctOnly(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<Candidate, Party, Int?, Double, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                CandidateTemplate(),
                VotePctOnlyTemplate(),
                Candidate.OTHERS
            )
        }

        fun candidateVotes(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            incumbentMarker: String
        ): VoteScreenBuilder<Candidate, Party, Int?, Double, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                CandidateTemplate(incumbentMarker),
                VotePctTemplate(),
                Candidate.OTHERS
            )
        }

        fun candidateVotesPctOnly(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            incumbentMarker: String
        ): VoteScreenBuilder<Candidate, Party, Int?, Double, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                CandidateTemplate(incumbentMarker),
                VotePctOnlyTemplate(),
                Candidate.OTHERS
            )
        }

        fun <P : PartyOrCoalition> partyRangeVotes(
            votes: Flow.Publisher<out Map<P, ClosedRange<Double>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<P, P, ClosedRange<Double>, Double, Int> {
            @Suppress("UNCHECKED_CAST")
            return RangeVoteScreenBuilder(
                votes,
                header,
                subhead,
                PartyTemplate(),
                PctOnlyTemplate(),
                Party.OTHERS as P
            )
        }

        fun <P : PartyOrCoalition> partyQuotas(
            quotas: Flow.Publisher<out Map<out P, Double>>,
            totalSeats: Flow.Publisher<out Int>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): PartyQuotaScreenBuilder<P> {
            return PartyQuotaScreenBuilder(
                quotas,
                totalSeats,
                header,
                subhead
            )
        }
    }
}
