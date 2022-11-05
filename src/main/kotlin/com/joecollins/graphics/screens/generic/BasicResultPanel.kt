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

private const val TICK = "\u2611"
private const val ARROW = "\u2348"

class BasicResultPanel private constructor(
    label: Flow.Publisher<out String?>,
    private val seatFrame: BarFrame,
    private val preferenceFrame: BarFrame?,
    private val changeFrame: BarFrame?,
    private val swingFrame: SwingFrame?,
    private val mapFrame: MapFrame?,
    altText: Flow.Publisher<String?>
) : GenericPanel(
    run {
        val panel = JPanel()
        panel.layout = BasicResultLayout()
        panel.background = Color.WHITE
        panel.add(seatFrame, BasicResultLayout.MAIN)
        if (preferenceFrame != null) panel.add(preferenceFrame, BasicResultLayout.PREF)
        if (changeFrame != null) panel.add(changeFrame, BasicResultLayout.DIFF)
        if (swingFrame != null) panel.add(swingFrame, BasicResultLayout.SWING)
        if (mapFrame != null) panel.add(mapFrame, BasicResultLayout.MAP)
        panel
    },
    label,
    altText
) {

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

    interface SeatTemplate<CT, PT> {
        fun sortOrder(value: CT?): Int?

        fun prevSortOrder(value: PT?): Int?

        val default: CT
        fun labelText(value: CT): String

        fun diffLabelText(value: CT): String

        fun prevLabelText(value: PT): String
    }

    private class SingleSeatTemplate : SeatTemplate<Int, Int> {
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
    }

    private class DualSeatTemplate : SeatTemplate<Pair<Int, Int>, Pair<Int, Int>> {
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
    }

    private class RangeSeatTemplate : SeatTemplate<IntRange, Int> {
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
    }

    abstract class SeatScreenBuilder<KT, KPT : PartyOrCoalition, CT, PT> internal constructor(
        protected var current: Flow.Publisher<out Map<out KT, CT>>,
        protected var header: Flow.Publisher<out String?>,
        protected var subhead: Flow.Publisher<out String?>,
        protected val keyTemplate: KeyTemplate<KT, KPT>,
        private val seatTemplate: SeatTemplate<CT, PT>
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
                createMapFrame(),
                createAltText(textHeader)
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

        private fun createAltText(textHeader: Flow.Publisher<out String?>): Flow.Publisher<String?> {
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
            val shapes = (winner ?: null.asOneTimePublisher()).map { if (it == null) emptyMap() else mapOf(it to TICK) }
            val barsText = current.merge(diff?.merge(showPrevRaw ?: false.asOneTimePublisher()) { d, raw -> if (raw) emptyMap() else d } ?: emptyMap<KPT, CurrDiff<CT>>().asOneTimePublisher()) { c, d -> c to d }.merge(shapes) { (c, d), s ->
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
                        swingComparator!!
                    ).bottomText ?: null.asOneTimePublisher()
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
                val cv: Map<KT, Int> = c.mapValues { seatTemplate.sortOrder(it.value)!! }
                val cg: Map<KPT, Int> = Aggregators.adjustKey(cv) { classificationFunc!!(keyTemplate.toParty(it)) }
                (h ?: "") + cg.entries
                    .sortedByDescending { it.key.overrideSortOrder ?: it.value }
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
                .merge(textHeader) { second, head -> "$head\n\n$second" }
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
        keyTemplate: KeyTemplate<KT, KPT>
    ) : SeatScreenBuilder<KT, KPT, Int, Int>(current, header, subhead, keyTemplate, SingleSeatTemplate()) {

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
    ) : SeatScreenBuilder<KT, KPT, Pair<Int, Int>, Pair<Int, Int>>(current, header, subhead, keyTemplate, DualSeatTemplate()) {

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
    ) : SeatScreenBuilder<KT, KPT, IntRange, Int>(current, header, subhead, keyTemplate, RangeSeatTemplate()) {
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
        protected var runoffSubhead: Flow.Publisher<String>? = null
        protected var winnerNotRunningAgain: Flow.Publisher<String>? = null
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
                createMapFrame(),
                createAltText(textHeader)
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

        protected abstract fun createAltText(textHeader: Flow.Publisher<out String?>): Flow.Publisher<String?>
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

        override fun createAltText(textHeader: Flow.Publisher<out String?>): Flow.Publisher<String?> {
            class Inputs {

                var textHeader: String? = null
                    set(value) { field = value; updateResult() }

                var current: Map<out KT, Int?> = emptyMap()
                    set(value) { field = value; updateResult() }

                var prev: Map<out KPT, Int>? = null
                    set(value) { field = value; updateResult() }

                var showPrevRaw: Boolean = false
                    set(value) { field = value; updateResult() }

                var winner: KT? = null
                    set(value) { field = value; updateResult() }

                var runoff: Set<KT>? = null
                    set(value) { field = value; updateResult() }

                val isWinnerRunningAgain: Boolean
                    get() {
                        val prevWinner = (prev ?: emptyMap()).entries.maxByOrNull { it.value }?.key
                        return prevWinner != null && current.keys.any { keyTemplate.toParty(it) == prevWinner }
                    }

                var winnerNotRunningAgain: String? = null
                    set(value) { field = value; updateResult() }

                var runoffSubhead: String? = null
                    set(value) { field = value; updateResult() }

                val isRunoffSameParties: Boolean
                    get() {
                        if (runoffSubhead == null) {
                            return true
                        }
                        return current.keys.map { keyTemplate.toParty(it) }.toSet() == prev?.keys
                    }

                val usePrev: Boolean
                    get() = this.isWinnerRunningAgain && this.isRunoffSameParties

                var header: String? = null
                    set(value) { field = value; updateResult() }

                var subhead: String? = null
                    set(value) { field = value; updateResult() }

                val mainText: String?
                    get() {
                        return combineHeadAndSub(header + (progressLabel?.let { " [$it]" } ?: ""), this.subhead)
                    }

                var changeHeader: String? = null
                    set(value) { field = value; updateResult() }

                var changeSubhead: String? = null
                    set(value) { field = value; updateResult() }

                val changeText: String?
                    get() {
                        if (showPrevRaw) {
                            return null
                        }
                        val head = sequenceOf(
                            this.changeHeader,
                            if (isWinnerRunningAgain) null else this.winnerNotRunningAgain?.takeIf { it.isNotEmpty() },
                            if (isRunoffSameParties) null else this.runoffSubhead?.takeIf { it.isNotEmpty() }
                        ).filterNotNull().joinToString(" ").takeIf { it.isNotEmpty() }
                        return combineHeadAndSub(head, this.changeSubhead)
                    }

                fun combineHeadAndSub(head: String?, sub: String?): String? {
                    return if (head.isNullOrEmpty()) {
                        sub
                    } else if (sub.isNullOrEmpty()) {
                        head
                    } else {
                        ("$head, $sub")
                    }
                }

                var progressLabel: String? = null
                    set(value) { field = value; updateResult() }

                val barsText: String
                    get() {
                        @Suppress("UNCHECKED_CAST")
                        val mandatory = sequenceOf(
                            current.keys.asSequence()
                                .filter { mandatoryParties.contains(keyTemplate.toParty(it)) },
                            (runoff?.asSequence() ?: emptySequence()),
                            sequenceOf(winner)
                                .filter { obj -> obj != null }
                        )
                            .flatten()
                            .filter { it != null }
                            .map { it as Any }
                            .toList().toTypedArray() as Array<KT>

                        val filteredCurr = Aggregators.topAndOthers(current, limit, others, *mandatory)
                        val filteredPrev = (if (this.usePrev && !this.showPrevRaw) this.prev else null) ?: emptyMap<KPT, Int>()

                        val shapes: Map<KT, String> = if (this.winner != null) {
                            mapOf(this.winner!! to TICK)
                        } else if (this.runoff != null) {
                            this.runoff!!.associateWith { ARROW }
                        } else {
                            emptyMap()
                        }
                        val currTotal = (filteredCurr.values.sumOf { it ?: 0 }).toDouble()
                        val allDeclared = filteredCurr.values.all { it != null }
                        val currGroupedByParty: Map<PartyOrCoalition, List<KT>> =
                            filteredCurr.entries.groupBy { keyTemplate.toParty(it.key) }
                                .mapValues { v -> v.value.sortedByDescending { it.value ?: 0 }.map { it.key } }
                        val currTotalByParty: Map<PartyOrCoalition, Int> = Aggregators.adjustKey(
                            filteredCurr.filterValues { it != null }
                                .mapValues { it.value!! }
                        ) { keyTemplate.toParty(it) }.let { cVotes ->
                            val pVotes = this.prev ?: emptyMap()
                            val prevWinner: KPT? = pVotes.entries
                                .maxByOrNull { it.value }
                                ?.key
                            val prevHasOther = pVotes.containsKey(Party.OTHERS as PartyOrCoalition)
                            val partiesToShow = sequenceOf(
                                sequenceOf(prevWinner),
                                cVotes.entries
                                    .asSequence()
                                    .filter { !prevHasOther || pVotes.containsKey(it.key) }
                                    .sortedByDescending { it.value }
                                    .map { it.key }
                            ).flatten().filterNotNull().distinct().toSet()
                            Aggregators.adjustKey(cVotes) { if (partiesToShow.contains(it)) it else Party.OTHERS }
                        }
                        val prevTotalByParty: Map<PartyOrCoalition, Int> = Aggregators.adjustKey(filteredPrev) {
                            if (currTotalByParty.containsKey(it)) {
                                it
                            } else {
                                Party.OTHERS
                            }
                        }
                        var somePartyAggregated = false
                        var someOtherAggregated = false
                        val prevTotal = filteredPrev.values.sum().toDouble()
                        val currText = if (filteredCurr.keys.size == 1) {
                            filteredCurr.keys.joinToString("") { candidate ->
                                "\n" + keyTemplate.toMainBarHeader(
                                    candidate,
                                    true
                                ) + ": UNCONTESTED"
                            }
                        } else {
                            filteredCurr.keys
                                .sortedByDescending { keyTemplate.toParty(it).overrideSortOrder ?: filteredCurr[it] ?: 0 }
                                .joinToString("") { candidate ->
                                    val party = keyTemplate.toParty(candidate)
                                    val partyAggregated = currTotal > 0 && prevTotal > 0 && (currGroupedByParty[party]?.size ?: 0) > 1 && currTotalByParty.containsKey(party)
                                    val otherAggregated = currTotal > 0 && !showPrevRaw && allDeclared && !currTotalByParty.containsKey(party)
                                    somePartyAggregated = somePartyAggregated || partyAggregated
                                    someOtherAggregated = someOtherAggregated || otherAggregated
                                    val line = barEntryLine(
                                        keyTemplate.toMainBarHeader(candidate, true),
                                        filteredCurr[candidate]?.takeUnless { currTotal == 0.0 },
                                        (if (filteredCurr[candidate] == null || currTotal == 0.0 || !allDeclared) null else (filteredCurr[candidate]!! / currTotal)),
                                        (
                                            if (
                                                currTotalByParty[party] == null ||
                                                !allDeclared ||
                                                currTotal == 0.0 ||
                                                prevTotal == 0.0 ||
                                                currGroupedByParty[party]?.get(0) != candidate
                                            ) {
                                                null
                                            } else {
                                                (currTotalByParty[party]!! / currTotal) - (prevTotalByParty[party] ?: 0) / prevTotal
                                            }
                                            ),
                                        (if (partyAggregated) "^" else "") +
                                            (if (otherAggregated) "*" else "")
                                    )
                                    val shape = shapes[candidate]
                                    "\n$line" + (shape?.let { c -> " $c" } ?: "")
                                }
                        }
                        val othersText = if (prevTotal > 0 && currTotal > 0 && filteredCurr.keys.size > 1 && filteredCurr.values.all { it != null } && !filteredCurr.any { keyTemplate.toParty(it.key) == Party.OTHERS } && prevTotalByParty.containsKey(Party.OTHERS)) {
                            "\nOTHERS: - (${PCT_DIFF_FORMAT.format(((currTotalByParty[Party.OTHERS] ?: 0) / currTotal) - prevTotalByParty[Party.OTHERS]!! / prevTotal)})"
                        } else {
                            ""
                        }
                        val legendText = (
                            (if (somePartyAggregated) "\n^ AGGREGATED ACROSS CANDIDATES IN PARTY" else "")
                            ) + (
                            (if (someOtherAggregated) "\n* CHANGE INCLUDED IN OTHERS" else "")
                            )
                        return currText + othersText + legendText
                    }

                private fun barEntryLine(header: String, curr: Int?, pct: Double?, diff: Double?, shapes: String): String {
                    return "$header: ${
                    if (curr == null) {
                        "WAITING..."
                    } else if (pct == null) {
                        DecimalFormat("#,##0").format(curr)
                    } else {
                        voteTemplate.toAltTextString(curr, pct, diff, shapes)
                    }
                    }"
                }

                var showMajority: Boolean = false
                    set(value) { field = value; updateResult() }

                val majorityText: String?
                    get() {
                        return if (showMajority) {
                            majorityLabel
                        } else {
                            null
                        }
                    }

                var classificationHeader: String? = null
                    set(value) { field = value; updateResult() }

                val classificationText: String?
                    get() {
                        if (classificationFunc == null) {
                            return null
                        }
                        val current: Map<KT, Int> = if (this.current.any { it.value == null }) emptyMap() else this.current.mapValues { it.value!! }
                        val total = current.values.sum().toDouble()
                        val grouped: Map<KPT, Int> =
                            Aggregators.adjustKey(current) { classificationFunc!!(keyTemplate.toParty(it)) }
                        return (classificationHeader ?: "") + grouped.entries
                            .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                            .joinToString("") { "\n${it.key.name.uppercase()}: ${PCT_FORMAT.format(it.value / total)}" }
                    }

                var swingHeader: String? = null
                    set(value) { field = value; updateResult() }

                var swingBottom: String? = null
                    set(value) { field = value; updateResult() }

                val prevText: String?
                    get() {
                        if (!this.showPrevRaw) return null
                        val prev = this.prev ?: emptyMap()
                        val total = prev.values.sum().toDouble()
                        return (changeHeader ?: "") +
                            prev.entries
                                .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                                .joinToString("") { "\n${it.key.abbreviation}: ${PCT_FORMAT.format(it.value / total)}" }
                    }

                val swingText: String?
                    get() {
                        val swingBottom = if (this.usePrev || this.swingBottom == null) swingBottom else "NOT AVAILABLE"
                        return if (swingBottom == null && swingHeader == null) {
                            null
                        } else if (swingBottom == null) {
                            swingHeader
                        } else if (swingHeader == null) {
                            swingBottom
                        } else {
                            "$swingHeader: $swingBottom"
                        }
                    }

                var preferencesHeader: String? = null
                    set(value) { field = value; updateResult() }

                var preferencesSubhead: String? = null
                    set(value) { field = value; updateResult() }

                var preferencesProgress: String? = null
                    set(value) { field = value; updateResult() }

                var currPreferences: Map<out KT, Int?> = emptyMap()
                    set(value) { field = value; updateResult() }

                val preferences: String?
                    get() {
                        if (this.preferencesHeader == null) return null
                        val total = this.currPreferences.takeUnless { v -> v.values.any { it == null } }?.values?.sumOf { it!! }?.toDouble()
                        return this.preferencesHeader!! +
                            (this.preferencesSubhead.takeUnless { it.isNullOrEmpty() }?.let { ", $it" } ?: "") +
                            (this.preferencesProgress?.let { " [$it]" } ?: "") +
                            this.currPreferences.entries.sortedByDescending { (if (it.key is CanOverrideSortOrder) (it.key as CanOverrideSortOrder).overrideSortOrder else null) ?: it.value ?: 0 }
                                .joinToString("") {
                                    "\n${keyTemplate.toMainBarHeader(it.key, true)}: ${
                                    if (this.currPreferences.size == 1) "ELECTED" else if (total == 0.0 || it.value == null) "WAITING..." else THOUSANDS_FORMAT.format(it.value)
                                    }${
                                    if (total == 0.0 || total == null) "" else " (${PCT_FORMAT.format(it.value!! / total)})"
                                    }${
                                    if (winner == it.key) " $TICK" else ""
                                    }"
                                }
                    }

                val result: String
                    get() {
                        return (if (this.textHeader == null) "" else this.textHeader + "\n\n") +
                            (this.mainText ?: "") + (if (this.changeText == null) "" else " (${this.changeText})") +
                            this.barsText +
                            (this.majorityText?.let { "\n$it" } ?: "") +
                            (this.prevText?.let { "\n\n$it" } ?: "") +
                            (this.classificationText?.let { "\n\n$it" } ?: "") +
                            (this.preferences?.let { "\n\n$it" } ?: "") +
                            (this.swingText?.let { "\n\n$it" } ?: "")
                    }

                fun updateResult() {
                    synchronized(this) {
                        this.resultPublisher.submit(this.result)
                    }
                }

                val resultPublisher = Publisher<String?>()
            }
            val inputs = Inputs()
            textHeader.subscribe(Subscriber { inputs.textHeader = it })
            this.current.subscribe(Subscriber { inputs.current = it })
            this.prev?.subscribe(Subscriber { inputs.prev = it })
            this.showPrevRaw?.subscribe(Subscriber { inputs.showPrevRaw = it })
            this.header.subscribe(Subscriber { inputs.header = it })
            this.subhead.subscribe(Subscriber { inputs.subhead = it })
            this.changeHeader?.subscribe(Subscriber { inputs.changeHeader = it })
            this.changeSubhead?.subscribe(Subscriber { inputs.changeSubhead = it })
            this.progressLabel.subscribe(Subscriber { inputs.progressLabel = it })
            this.winner?.subscribe(Subscriber { inputs.winner = it })
            this.runoff?.subscribe(Subscriber { inputs.runoff = it })
            this.winnerNotRunningAgain?.subscribe(Subscriber { inputs.winnerNotRunningAgain = it })
            this.runoffSubhead?.subscribe(Subscriber { inputs.runoffSubhead = it })
            this.showMajority?.subscribe(Subscriber { inputs.showMajority = it })
            this.classificationHeader?.subscribe(Subscriber { inputs.classificationHeader = it })
            this.preferenceHeader?.subscribe(Subscriber { inputs.preferencesHeader = it })
            this.preferenceSubhead?.subscribe(Subscriber { inputs.preferencesSubhead = it })
            this.preferenceProgressLabel.subscribe(Subscriber { inputs.preferencesProgress = it })
            this.currPreferences?.subscribe(Subscriber { inputs.currPreferences = it })
            this.swingHeader?.subscribe(Subscriber { inputs.swingHeader = it })
            if (prev != null && swingComparator != null) {
                if (currPreferences != null && prevPreferences != null) {
                    val currentNoNulls = this.currPreferences!!.map { v -> if (v.values.any { it == null }) emptyMap() else v.mapValues { it.value!! } }
                    val curr = Aggregators.adjustKey(currentNoNulls) { keyTemplate.toParty(it) }
                    val prev = prevPreferences!!.merge(curr) { p, c ->
                        if (p.keys == c.keys) p else emptyMap()
                    }
                    SwingFrameBuilder.prevCurr(
                        prev,
                        curr,
                        swingComparator!!
                    ).bottomText?.subscribe(Subscriber { inputs.swingBottom = it })
                } else {
                    val currentNoNulls = this.current.map { v -> if (v.values.any { it == null }) emptyMap() else v.mapValues { it.value!! } }
                    val currentByParty: Flow.Publisher<Map<KPT, Int>> = Aggregators.adjustKey(currentNoNulls) { keyTemplate.toParty(it) }
                    SwingFrameBuilder.prevCurr(
                        (if (classificationFunc == null) prev!! else Aggregators.adjustKey(prev!!, classificationFunc!!)),
                        (if (classificationFunc == null) currentByParty else Aggregators.adjustKey(currentByParty, classificationFunc!!)),
                        swingComparator!!
                    ).bottomText?.subscribe(Subscriber { inputs.swingBottom = it })
                }
            }

            return inputs.resultPublisher
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

        override fun createAltText(textHeader: Flow.Publisher<out String?>): Flow.Publisher<String?> {
            val combineHeadAndSub: (String?, String?) -> String? = { h, s ->
                if (h.isNullOrEmpty()) {
                    s
                } else if (s.isNullOrEmpty()) {
                    h
                } else {
                    ("$h, $s")
                }
            }
            val mainText = header.merge(progressLabel) { h, p -> h + (p?.let { " [$it]" } ?: "") }.merge(subhead, combineHeadAndSub)
            val changeTitle = (changeHeader ?: null.asOneTimePublisher())
                .merge((changeSubhead ?: null.asOneTimePublisher()), combineHeadAndSub)
            val showPrevRaw: Flow.Publisher<Boolean> = showPrevRaw ?: false.asOneTimePublisher()
            val changeText = changeTitle
                .merge(showPrevRaw) { text, raw -> if (raw) null else text }
            val barEntryLine: (String, ClosedRange<Double>, ClosedRange<Double>?) -> String = { h, p, d ->
                "$h: ${DecimalFormat("0.0").format(100 * p.start)}-${PCT_FORMAT.format(p.endInclusive)}" +
                    (d?.let { " ((${DecimalFormat("+0.0;-0.0").format(100 * it.start)})-(${DecimalFormat("+0.0;-0.0").format(100 * it.endInclusive)})%)" } ?: "")
            }
            val filteredPrev: Flow.Publisher<Map<PartyOrCoalition, Int>>? = prev?.merge(current) { p, c ->
                val parties = c.keys.map { keyTemplate.toParty(it) }
                Aggregators.adjustKey(p) { if (parties.contains(it)) it else Party.OTHERS }
            }
            val barsText = current.merge(filteredPrev?.merge(showPrevRaw) { p, b -> if (b) emptyMap() else p } ?: emptyMap<KPT, Int>().asOneTimePublisher()) { c, p ->
                val prevTotal = p.values.sum().toDouble()
                c.keys
                    .sortedByDescending { keyTemplate.toParty(it).overrideSortOrder?.toDouble() ?: c[it]?.let { it.start + it.endInclusive } ?: 0.0 }
                    .joinToString("") { candidate ->
                        val party = keyTemplate.toParty(candidate)
                        val prevPct = (p[party] ?: 0) / prevTotal
                        val line = barEntryLine(
                            keyTemplate.toMainBarHeader(candidate, true),
                            c[candidate]!!,
                            (if (c[candidate] == null || prevTotal == 0.0) null else c[candidate]!!.let { (it.start - prevPct)..(it.endInclusive - prevPct) })
                        )
                        "\n$line"
                    }
            }
            val prevText: Flow.Publisher<out String?> =
                (prev ?: emptyMap<KPT, Int>().asOneTimePublisher())
                    .merge(changeTitle) { p, t ->
                        val total = p.values.sum().toDouble()
                        t + p.entries
                            .sortedByDescending { it.value }
                            .joinToString("") { "\n${it.key.abbreviation}: ${PCT_FORMAT.format(it.value / total)}" }
                    }.merge(showPrevRaw) { t, b -> if (b) t else null }
            val preferenceText = preferenceHeader?.merge(preferenceSubhead ?: null.asOneTimePublisher(), combineHeadAndSub)
                ?.merge(currPreferences ?: emptyMap<KT, ClosedRange<Double>>().asOneTimePublisher()) { h, c ->
                    h + c.entries.sortedByDescending { (if (it.key is CanOverrideSortOrder) (it.key as CanOverrideSortOrder).overrideSortOrder?.toDouble() else null) ?: it.value.let { v -> v.start + v.endInclusive } }
                        .joinToString("") { "\n${barEntryLine(keyTemplate.toMainBarHeader(it.key, true), it.value, null)}" }
                } ?: null.asOneTimePublisher()
            val swingText: Flow.Publisher<out String?> =
                if (prev == null || swingComparator == null) {
                    null.asOneTimePublisher()
                } else {
                    val classificationFunc = this.classificationFunc
                    val current: Flow.Publisher<Map<out KT, Int>> = this.current.map { v ->
                        v.mapValues { it.value.let { r -> (r.start + r.endInclusive) * 10000 }.roundToInt() }
                    }
                    val c: Flow.Publisher<Map<KPT, Int>> = Aggregators.adjustKey(current) { keyTemplate.toParty(it) }
                    val p: Flow.Publisher<out Map<out KPT, Int>> = prev!!
                    SwingFrameBuilder.prevCurr(
                        (if (classificationFunc == null) p else Aggregators.adjustKey(p, classificationFunc)),
                        (if (classificationFunc == null) c else Aggregators.adjustKey(c, classificationFunc)),
                        swingComparator!!
                    ).bottomText ?: null.asOneTimePublisher()
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
            return mainText.merge(changeText) { main, change -> main + (if (change == null) "" else " ($change)") }
                .merge(textHeader) { second, head -> "$head\n\n$second" }
                .merge(barsText) { first, next -> first + next }
                .merge(prevText) { text, prev -> text + (prev?.let { "\n\n$it" } ?: "") }
                .merge(preferenceText) { text, pref -> text + (pref?.let { "\n\n$it" } ?: "") }
                .merge(swingText) { text, swing -> text + (swing?.let { "\n\n$it" } ?: "") }
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
                mapBuilder?.createMapFrame(),
                createAltText(textHeader)
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

        private fun createAltText(textHeader: Flow.Publisher<out String?>): Flow.Publisher<String?> {
            val combineHeaderAndSubhead: (String?, String?) -> String? = { h, s ->
                if (h == null) {
                    s
                } else if (s == null) {
                    h
                } else {
                    "$h, $s"
                }
            }
            val mainHeader = header.merge(subhead, combineHeaderAndSubhead)
                .merge(changeHeader ?: null.asOneTimePublisher()) { h, c ->
                    if (c == null) {
                        h
                    } else {
                        "${h ?: ""} ($c)"
                    }
                }.merge(progressLabel ?: null.asOneTimePublisher()) { h, p ->
                    if (p == null) {
                        h
                    } else {
                        "${h ?: ""} [$p]"
                    }
                }
            val mainEntries = quotas.merge(prevQuotas ?: null.asOneTimePublisher()) { curr, prev ->
                val entries = curr.entries
                    .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }
                    .joinToString("") { e ->
                        "\n${e.key.name.uppercase()}: ${DecimalFormat("0.00").format(e.value)} QUOTAS" +
                            (if (prev == null) "" else " (${DecimalFormat("+0.00;-0.00").format(e.value - (prev[e.key] ?: 0.0))})")
                    }
                val others = prev?.filterKeys { curr.isNotEmpty() && !curr.containsKey(it) }
                    ?.entries
                    ?.sortedByDescending { it.value }
                    ?.joinToString("") { "\n${it.key.name.uppercase()}: - (-${DecimalFormat("0.00").format(it.value)})" }
                    ?: ""
                entries + others
            }
            val mainText = mainHeader.merge(mainEntries) { h, e ->
                if (h == null) {
                    e
                } else {
                    ("$h$e")
                }
            }
            val swingText = if (swingPrevVotes == null || swingCurrVotes == null) {
                null
            } else {
                run {
                    val prev = swingPrevVotes!!
                    val curr = swingCurrVotes!!
                    SwingFrameBuilder.prevCurr(
                        prev,
                        curr,
                        swingComparator!!
                    ).bottomText
                }?.merge(swingHeader ?: null.asOneTimePublisher()) { t, h ->
                    if (h == null) {
                        t
                    } else {
                        "$h: $t"
                    }
                }
            }
            return textHeader.merge(mainText) { h, m -> "$h\n\n$m" }
                .merge(swingText ?: null.asOneTimePublisher()) { h, s ->
                    if (s == null) {
                        h
                    } else {
                        "$h\n\n$s"
                    }
                }
        }
    }

    class CurrDiff<CT>(val curr: CT, val diff: CT)
    companion object {
        private val PCT_FORMAT = DecimalFormat("0.0%")
        private val PCT_DIFF_FORMAT = DecimalFormat("+0.0%;-0.0%")
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
                VotePctOnlyTemplate(),
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
                VotePctOnlyTemplate(),
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
