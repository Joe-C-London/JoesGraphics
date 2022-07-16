package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.BarFrameBuilder.DualBar
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Aggregators
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
import java.util.Objects
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sign

class BasicResultPanel private constructor(
    label: JLabel,
    private val seatFrame: BarFrame,
    private val preferenceFrame: BarFrame?,
    private val changeFrame: BarFrame?,
    private val swingFrame: SwingFrame?,
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
            seatFrame.setLocation(5, 5)
            val seatFrameIsAlone = changeFrame == null && swingFrame == null && mapFrame == null
            seatFrame.setSize(
                width * (if (seatFrameIsAlone) 5 else 3) / 5 - 10,
                height * (if (preferenceFrame == null) 3 else 2) / 3 - 10
            )
            preferenceFrame?.setLocation(5, height * 2 / 3 + 5)
            preferenceFrame?.setSize(width * (if (seatFrameIsAlone) 5 else 3) / 5 - 10, height / 3 - 10)
            changeFrame?.setLocation(width * 3 / 5 + 5, 5)
            changeFrame?.setSize(width * 2 / 5 - 10, height * 2 / 3 - 10)
            swingFrame?.setLocation(width * 3 / 5 + 5, height * 2 / 3 + 5)
            swingFrame?.setSize(width * (if (mapFrame == null) 2 else 1) / 5 - 10, height / 3 - 10)
            mapFrame?.setLocation(width * (if (swingFrame == null) 3 else 4) / 5 + 5, height * 2 / 3 + 5)
            mapFrame?.setSize(width * (if (swingFrame == null) 2 else 1) / 5 - 10, height / 3 - 10)
        }
    }

    interface KeyTemplate<KT> {
        fun toParty(key: KT): Party
        fun toMainBarHeader(key: KT, forceSingleLine: Boolean): String
        fun winnerShape(forceSingleLine: Boolean): Shape
        fun runoffShape(forceSingleLine: Boolean): Shape
    }

    private class PartyTemplate : KeyTemplate<Party> {
        override fun toParty(key: Party): Party {
            return key
        }

        override fun toMainBarHeader(key: Party, forceSingleLine: Boolean): String {
            return key.name.uppercase()
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createRunoffShape()
        }
    }

    private class CandidateTemplate : KeyTemplate<Candidate> {
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
            } else ("${key.name}${if (key.isIncumbent()) incumbentMarker else ""}${if (forceSingleLine) (" (" + key.party.abbreviation + ")") else ("\n" + key.party.name)}")
                .uppercase()
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createTickShape() else ImageGenerator.createHalfTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createRunoffShape() else ImageGenerator.createHalfRunoffShape()
        }
    }

    abstract class SeatScreenBuilder<KT, CT, PT> internal constructor(
        protected var current: Flow.Publisher<out Map<KT, CT>>,
        protected var header: Flow.Publisher<out String?>,
        protected var subhead: Flow.Publisher<out String?>,
        protected val keyTemplate: KeyTemplate<KT>
    ) {
        protected var total: Flow.Publisher<out Int>? = null
        protected var showMajority: Flow.Publisher<out Boolean>? = null
        protected var majorityFunction: ((Int) -> String)? = null
        protected var winner: Flow.Publisher<out KT?>? = null
        protected var notes: Flow.Publisher<out String?>? = null
        protected var changeNotes: Flow.Publisher<out String?>? = null
        protected var diff: Flow.Publisher<out Map<Party, CurrDiff<CT>>>? = null
        protected var changeHeader: Flow.Publisher<out String?>? = null
        protected var changeSubhead: Flow.Publisher<out String?>? = null
        private var currVotes: Flow.Publisher<out Map<Party, Int>>? = null
        private var prevVotes: Flow.Publisher<out Map<Party, Int>>? = null
        private var swingHeader: Flow.Publisher<out String?>? = null
        private var swingComparator: Comparator<Party>? = null
        protected var classificationFunc: ((Party) -> Party)? = null
        protected var classificationHeader: Flow.Publisher<out String?>? = null
        private var mapBuilder: MapBuilder<*>? = null

        fun withTotal(totalSeats: Flow.Publisher<out Int>): SeatScreenBuilder<KT, CT, PT> {
            total = totalSeats
            return this
        }

        fun withMajorityLine(
            showMajority: Flow.Publisher<out Boolean>,
            majorityLabelFunc: (Int) -> String
        ): SeatScreenBuilder<KT, CT, PT> {
            this.showMajority = showMajority
            majorityFunction = majorityLabelFunc
            return this
        }

        fun withWinner(winner: Flow.Publisher<out KT?>): SeatScreenBuilder<KT, CT, PT> {
            this.winner = winner
            return this
        }

        @JvmOverloads
        fun withDiff(
            diff: Flow.Publisher<out Map<Party, CT>>,
            changeHeader: Flow.Publisher<out String?>,
            changeSubhead: Flow.Publisher<out String?> = null.asOneTimePublisher()
        ): SeatScreenBuilder<KT, CT, PT> {
            this.diff =
                current
                    .merge(diff) { c, d ->
                        val ret = LinkedHashMap<Party, CurrDiff<CT>>()
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
            prev: Flow.Publisher<out Map<Party, PT>>,
            changeHeader: Flow.Publisher<out String?>,
            changeSubhead: Flow.Publisher<out String?> = null.asOneTimePublisher()
        ): SeatScreenBuilder<KT, CT, PT> {
            diff =
                current
                    .merge(prev) { c, p ->
                        val ret = LinkedHashMap<Party, CurrDiff<CT>>()
                        c.forEach { (k, v) -> ret[keyTemplate.toParty(k)] = createFromPrev(v, p[keyTemplate.toParty(k)]) }
                        p.forEach { (k, v) -> ret.putIfAbsent(k, createFromPrev(v)) }
                        ret
                    }
            this.changeHeader = changeHeader
            this.changeSubhead = changeSubhead
            return this
        }

        protected abstract fun createFromPrev(curr: CT, prev: PT?): CurrDiff<CT>
        protected abstract fun createFromPrev(prev: PT): CurrDiff<CT>

        fun withSwing(
            currVotes: Flow.Publisher<out Map<Party, Int>>,
            prevVotes: Flow.Publisher<out Map<Party, Int>>,
            comparator: Comparator<Party>,
            header: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            swingHeader = header
            this.currVotes = currVotes
            this.prevVotes = prevVotes
            swingComparator = comparator
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, Party?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            mapBuilder = MapBuilder(
                shapes, winners.map { m: Map<T, Party?> -> partyMapToResultMap(m) }, focus, headerPublisher
            )
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            additionalHighlight: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            mapBuilder = MapBuilder(shapes, winners, Pair(focus, additionalHighlight), headerPublisher)
            return this
        }

        fun withNotes(notes: Flow.Publisher<out String?>): SeatScreenBuilder<KT, CT, PT> {
            this.notes = notes
            return this
        }

        fun withChangeNotes(notes: Flow.Publisher<out String?>): SeatScreenBuilder<KT, CT, PT> {
            this.changeNotes = notes
            return this
        }

        fun withClassification(
            classificationFunc: (Party) -> Party,
            classificationHeader: Flow.Publisher<out String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            this.classificationFunc = classificationFunc
            this.classificationHeader = classificationHeader
            return this
        }

        fun build(textHeader: Flow.Publisher<out String>): BasicResultPanel {
            return BasicResultPanel(
                createHeaderLabel(textHeader),
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

    private class BasicSeatScreenBuilder<KT>(
        current: Flow.Publisher<out Map<KT, Int>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT>
    ) : SeatScreenBuilder<KT, Int, Int>(current, header, subhead, keyTemplate) {

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
            private var _seats: Map<KT, Int> = HashMap()
            private var _winner: KT? = null

            var seats: Map<KT, Int>
                get() = _seats
                set(seats) {
                    _seats = seats
                    updateBars()
                }

            var winner: KT?
                get() = _winner
                set(winner) {
                    _winner = winner
                    updateBars()
                }

            val barsPublisher = Publisher(calculateBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(calculateBars()) }
            private fun calculateBars(): List<BasicBar> {
                val seats = this.seats
                val winner = this.winner
                val numBars = seats.size
                return seats.entries.asSequence()
                    .sortedByDescending { e: Map.Entry<KT, Int> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value }
                    .map { e: Map.Entry<KT, Int> ->
                        BasicBar(
                            keyTemplate.toMainBarHeader(
                                e.key, numBars > doubleLineBarLimit()
                            ),
                            keyTemplate.toParty(e.key).color,
                            e.value, e.value.toString(),
                            if (e.key == winner) keyTemplate.winnerShape(numBars > doubleLineBarLimit()) else null
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
                .withHeader(header)
                .withSubhead(subhead)
                .withNotes(this.notes ?: (null as String?).asOneTimePublisher())
            val total = this.total
            if (total != null) {
                builder = builder.withMax(total.map { it * 2 / 3 })
            }
            applyMajorityLine(builder)
            return builder.build()
        }

        private fun applyMajorityLine(builder: BarFrameBuilder) {
            val showMajority = this.showMajority
            if (showMajority != null) {
                val total = this.total ?: throw IllegalArgumentException("Cannot show majority line without total")
                val lines = showMajority.merge(total) {
                        show, tot ->
                    if (show) listOf(tot / 2 + 1)
                    else emptyList()
                }
                builder.withLines(lines) { t -> majorityFunction!!(t) }
            }
        }

        override fun createClassificationFrame(): BarFrame? {
            return classificationHeader?.let { classificationHeader ->
                val bars: Flow.Publisher<out List<BasicBar>> = Aggregators.adjustKey(
                    current
                ) { classificationFunc!!(keyTemplate.toParty(it)) }
                    .map { seats: Map<Party, Int> ->
                        seats.entries.asSequence()
                            .sortedByDescending { it.value }
                            .map { e: Map.Entry<Party, Int> ->
                                BasicBar(
                                    e.key.name.uppercase(),
                                    e.key.color,
                                    e.value
                                )
                            }
                            .toList()
                    }
                var builder = BarFrameBuilder.basic(bars).withHeader(classificationHeader)
                val total = this.total
                if (total != null) {
                    builder = builder.withMax(total.map { it * 2 / 3 })
                }
                applyMajorityLine(builder)
                return builder.build()
            }
        }

        override fun createDiffFrame(): BarFrame? {
            return changeHeader?.let { changeHeader ->
                val bars = diff!!.map { map: Map<Party, CurrDiff<Int>> ->
                    map.entries.asSequence()
                        .sortedByDescending { e: Map.Entry<Party, CurrDiff<Int>> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value.curr }
                        .map { e: Map.Entry<Party, CurrDiff<Int>> ->
                            BasicBar(
                                e.key.abbreviation.uppercase(),
                                e.key.color,
                                e.value.diff,
                                changeStr(e.value.diff)
                            )
                        }
                        .toList()
                }
                var builder = BarFrameBuilder.basic(bars)
                    .withHeader(changeHeader)
                    .withSubhead(changeSubhead ?: (null as String?).asOneTimePublisher())
                val total = this.total
                if (total != null) {
                    builder = builder.withWingspan(total.map { (it / 20).coerceAtLeast(1) })
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

    private class DualSeatScreenBuilder<KT>(
        current: Flow.Publisher<out Map<KT, Pair<Int, Int>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT>,
        val focusLocation: FocusLocation
    ) : SeatScreenBuilder<KT, Pair<Int, Int>, Pair<Int, Int>>(current, header, subhead, keyTemplate) {

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
            private var _seats: Map<KT, Pair<Int, Int>> = HashMap()
            private var _winner: KT? = null

            var seats: Map<KT, Pair<Int, Int>>
                get() = _seats
                set(seats) {
                    _seats = seats
                    updateBars()
                }

            var winner: KT?
                get() = _winner
                set(winner) {
                    _winner = winner
                    updateBars()
                }

            val barsPublisher = Publisher(calculateBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(calculateBars()) }
            private fun calculateBars(): List<DualBar> {
                val seats = this.seats
                val winner = this.winner
                val count = seats.size
                return seats.entries.asSequence()
                    .sortedByDescending { e: Map.Entry<KT, Pair<Int, Int>> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value.second }
                    .map { e: Map.Entry<KT, Pair<Int, Int>> ->
                        DualBar(
                            keyTemplate.toMainBarHeader(
                                e.key, count > doubleLineBarLimit()
                            ),
                            keyTemplate.toParty(e.key).color,
                            if (focusLocation == FocusLocation.FIRST) e.value.first else (e.value.second - e.value.first),
                            e.value.second,
                            e.value.first.toString() + "/" + e.value.second,
                            if (e.key == winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else null
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
                .withHeader(header)
                .withSubhead(subhead)
                .withNotes(this.notes ?: (null as String?).asOneTimePublisher())
            val total = this.total
            if (total != null) {
                builder = builder.withMax(total.map { it * 2 / 3 })
            }
            val showMajority = this.showMajority
            if (showMajority != null) {
                if (total == null) {
                    throw IllegalStateException("Cannot show majority line without total")
                }
                val lines = showMajority.merge(total) {
                        show, tot ->
                    if (show) listOf(tot / 2 + 1)
                    else emptyList()
                }
                builder = builder.withLines(lines) { t -> majorityFunction!!(t) }
            }
            return builder.build()
        }

        override fun createClassificationFrame(): BarFrame? {
            if (classificationHeader == null) {
                return null
            }
            throw UnsupportedOperationException("Classification frame not supported on dual frame")
        }

        override fun createDiffFrame(): BarFrame? {
            return changeHeader?.let { changeHeader ->
                val bars = diff!!.map { map: Map<Party, CurrDiff<Pair<Int, Int>>> ->
                    map.entries.asSequence()
                        .sortedByDescending { e: Map.Entry<Party, CurrDiff<Pair<Int, Int>>> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value.curr.second }
                        .map { e: Map.Entry<Party, CurrDiff<Pair<Int, Int>>> ->
                            DualBar(
                                e.key.abbreviation.uppercase(),
                                e.key.color,
                                if (focusLocation == FocusLocation.FIRST ||
                                    (e.value.diff.first != 0 && sign(e.value.diff.first.toDouble()) != sign(e.value.diff.second.toDouble())) ||
                                    abs(e.value.diff.first.toDouble()) > abs(e.value.diff.second.toDouble())
                                ) e.value.diff.first else (e.value.diff.second - e.value.diff.first),
                                e.value.diff.second,
                                changeStr(e.value.diff.first) +
                                    "/" +
                                    changeStr(e.value.diff.second)
                            )
                        }
                        .toList()
                }
                var builder = if (focusLocation == FocusLocation.FIRST)
                    BarFrameBuilder.dual(bars)
                else
                    BarFrameBuilder.dualReversed(bars)
                builder = builder
                    .withHeader(changeHeader)
                    .withSubhead(changeSubhead ?: (null as String?).asOneTimePublisher())
                val total = this.total
                if (total != null) {
                    builder = builder.withWingspan(total.map { (it / 20).coerceAtLeast(1) })
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

    private class RangeSeatScreenBuilder<KT>(
        current: Flow.Publisher<out Map<KT, IntRange>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT>
    ) : SeatScreenBuilder<KT, IntRange, Int>(current, header, subhead, keyTemplate) {
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
            private var _seats: Map<KT, IntRange> = HashMap()
            private var _winner: KT? = null

            var seats: Map<KT, IntRange>
                get() = _seats
                set(seats) {
                    _seats = seats
                    updateBars()
                }

            var winner: KT?
                get() = _winner
                set(winner) {
                    _winner = winner
                    updateBars()
                }

            val barsPublisher = Publisher(createBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(createBars()) }
            private fun createBars(): List<DualBar> {
                val seats = this.seats
                val winner = this.winner
                val count = seats.size
                return seats.entries.asSequence()
                    .sortedByDescending { e: Map.Entry<KT, IntRange> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else (e.value.first + e.value.last) }
                    .map { e: Map.Entry<KT, IntRange> ->
                        DualBar(
                            keyTemplate.toMainBarHeader(
                                e.key, count > doubleLineBarLimit()
                            ),
                            keyTemplate.toParty(e.key).color,
                            e.value.first,
                            e.value.last,
                            e.value.first.toString() + "-" + e.value.last,
                            if (e.key == winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else null
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
                .withHeader(header)
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
                val lines = showMajority.merge(total) {
                        show, tot ->
                    if (show) listOf(tot / 2 + 1)
                    else emptyList()
                }
                builder = builder.withLines(lines) { t -> majorityFunction!!(t) }
            }
            return builder.build()
        }

        override fun createClassificationFrame(): BarFrame? {
            if (classificationHeader == null) {
                return null
            }
            throw UnsupportedOperationException("Classification frame not supported on range frame")
        }

        override fun createDiffFrame(): BarFrame? {
            return changeHeader?.let { changeHeader ->
                val bars = diff!!.map { map: Map<Party, CurrDiff<IntRange>> ->
                    map.entries.asSequence()
                        .sortedByDescending { e: Map.Entry<Party, CurrDiff<IntRange>> ->
                            if (e.key === Party.OTHERS) Int.MIN_VALUE
                            else (e.value.curr.first + e.value.curr.last)
                        }
                        .map { e: Map.Entry<Party, CurrDiff<IntRange>> ->
                            DualBar(
                                e.key.abbreviation.uppercase(),
                                e.key.color,
                                e.value.diff.first,
                                e.value.diff.last,
                                "(" +
                                    changeStr(e.value.diff.first) +
                                    ")-(" +
                                    changeStr(e.value.diff.last) +
                                    ")"
                            )
                        }
                        .toList()
                }
                var builder = BarFrameBuilder.dual(bars)
                    .withHeader(changeHeader)
                    .withSubhead(changeSubhead ?: (null as String?).asOneTimePublisher())
                val total = this.total
                if (total != null) {
                    builder = builder.withWingspan(total.map { (it / 20).coerceAtLeast(1) })
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

    abstract class VoteScreenBuilder<KT, CT, CPT, PT> internal constructor(
        protected var current: Flow.Publisher<out Map<KT, CT>>,
        protected var header: Flow.Publisher<out String?>,
        protected var subhead: Flow.Publisher<out String?>,
        protected val keyTemplate: KeyTemplate<KT>,
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
        protected var mandatoryParties: Set<Party> = emptySet()
        protected var prev: Flow.Publisher<out Map<Party, PT>>? = null
        protected var changeHeader: Flow.Publisher<out String?>? = null
        protected var changeSubhead: Flow.Publisher<out String?>? = null
        protected var currPreferences: Flow.Publisher<out Map<KT, CT>>? = null
        protected var prevPreferences: Flow.Publisher<out Map<Party, PT>>? = null
        protected var preferenceHeader: Flow.Publisher<out String?>? = null
        protected var preferenceSubhead: Flow.Publisher<out String?>? = null
        protected var preferencePctReporting: Flow.Publisher<out Double>? = null
        protected var swingHeader: Flow.Publisher<out String?>? = null
        protected var swingComparator: Comparator<Party>? = null
        protected var classificationFunc: ((Party) -> Party)? = null
        protected var classificationHeader: Flow.Publisher<out String?>? = null
        private var mapBuilder: MapBuilder<*>? = null
        private var runoffSubhead: Flow.Publisher<String>? = null
        private var winnerNotRunningAgain: Flow.Publisher<String>? = null

        protected val filteredPrev: Flow.Publisher<out Map<Party, PT>>?
            get() {
                val prev = this.prev
                if (prev == null) return prev
                if (runoffSubhead != null)
                    return current.merge(prev) { c, p ->
                        if (c.keys.map { keyTemplate.toParty(it) }.toSet() == p.keys)
                            p
                        else
                            emptyMap()
                    }
                if (winnerNotRunningAgain != null)
                    return current.merge(prev) { c, p ->
                        val winner = p.entries.filter { it.value is Number }.maxByOrNull { (it.value as Number).toDouble() } ?: return@merge p
                        if (c.keys.map { keyTemplate.toParty(it) }.contains(winner.key))
                            p
                        else
                            emptyMap()
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
                                if (c == null) s
                                else if (s == null) c
                                else "$c / $s"
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
                                if (c == null) s
                                else if (s == null) c
                                else "$c / $s"
                            }
                        }
                }
                return changeSubhead
            }

        @JvmOverloads
        fun withPrev(
            prev: Flow.Publisher<out Map<Party, PT>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.prev = prev
            changeHeader = header
            changeSubhead = subhead
            return this
        }

        fun withPreferences(
            preferences: Flow.Publisher<out Map<KT, CT>>,
            preferenceHeader: Flow.Publisher<out String?>,
            preferenceSubhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            currPreferences = preferences
            this.preferenceHeader = preferenceHeader
            this.preferenceSubhead = preferenceSubhead
            return this
        }

        fun withPrevPreferences(
            prevPreferences: Flow.Publisher<out Map<Party, PT>>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.prevPreferences = prevPreferences
            return this
        }

        fun withWinner(winner: Flow.Publisher<out KT?>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.winner = winner
            return this
        }

        fun withRunoff(runoff: Flow.Publisher<out Set<KT>?>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.runoff = runoff
            return this
        }

        fun withPctReporting(pctReporting: Flow.Publisher<Double>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.pctReporting = pctReporting
            return this
        }

        fun withPreferencePctReporting(
            preferencePctReporting: Flow.Publisher<out Double>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.preferencePctReporting = preferencePctReporting
            return this
        }

        fun withSwing(
            comparator: Comparator<Party>?,
            header: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            swingComparator = comparator
            swingHeader = header
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, Party?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners.map { m -> partyMapToResultMap(m) }, focus, headerPublisher)
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out Party?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty.map { party: Party? -> PartyResult.elected(party) }, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Flow.Publisher<out List<T>?>,
            headerPublisher: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            winners: Flow.Publisher<out Map<T, PartyResult?>>,
            focus: Pair<Flow.Publisher<out List<T>?>, Flow.Publisher<out List<T>?>>,
            headerPublisher: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerPublisher)
            return this
        }

        fun <T> withResultMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out PartyResult?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
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
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, additionalHighlight, header)
            return this
        }

        fun withMajorityLine(
            showMajority: Flow.Publisher<out Boolean>,
            majorityLabel: String
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.showMajority = showMajority
            this.majorityLabel = majorityLabel
            return this
        }

        fun withNotes(notes: Flow.Publisher<out String?>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.notes = notes
            return this
        }

        fun withLimit(limit: Int, vararg mandatoryParties: Party): VoteScreenBuilder<KT, CT, CPT, PT> {
            require(limit > 0) { "Invalid limit: $limit" }
            this.limit = limit
            this.mandatoryParties = setOf(*mandatoryParties)
            return this
        }

        fun withClassification(
            classificationFunc: (Party) -> Party,
            classificationHeader: Flow.Publisher<out String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.classificationFunc = classificationFunc
            this.classificationHeader = classificationHeader
            return this
        }

        fun inRunoffMode(changeSubhead: Flow.Publisher<String>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.runoffSubhead = changeSubhead
            return this
        }

        fun whenWinnerNotRunningAgain(changeSubhead: Flow.Publisher<String>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.winnerNotRunningAgain = changeSubhead
            return this
        }

        fun build(textHeader: Flow.Publisher<out String>): BasicResultPanel {
            return BasicResultPanel(
                createHeaderLabel(textHeader),
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

    private class BasicVoteScreenBuilder<KT>(
        current: Flow.Publisher<out Map<KT, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT>,
        voteTemplate: VoteTemplate,
        others: KT
    ) : VoteScreenBuilder<KT, Int?, Double?, Int>(current, header, subhead, keyTemplate, voteTemplate, others) {
        private inner class Result(private val isPreference: Boolean) {
            private var _votes: Map<KT, Int?> = HashMap()
            private var _winner: KT? = null
            private var _runoff: Set<KT>? = null

            var votes: Map<KT, Int?>
                get() = _votes
                set(votes) {
                    _votes = votes
                    updateBars()
                }

            var winner: KT?
                get() = _winner
                set(winner) {
                    _winner = winner
                    updateBars()
                }

            var runoff: Set<KT>?
                get() = _runoff
                set(runoff) {
                    _runoff = runoff
                    updateBars()
                }

            val barsPublisher = Publisher(calculateBars())
            private fun updateBars() = synchronized(this) { barsPublisher.submit(calculateBars()) }
            private fun calculateBars(): List<BasicBar> {
                val votes = this.votes
                val winner = this.winner
                val runoff = this.runoff
                val total = votes.values.filterNotNull().sum()
                @Suppress("UNCHECKED_CAST") val mandatory = sequenceOf(
                    votes.keys.asSequence()
                        .filter { k: KT -> mandatoryParties.contains(keyTemplate.toParty(k)) },
                    (runoff?.asSequence() ?: emptySequence()),
                    sequenceOf(winner)
                        .filter { obj -> Objects.nonNull(obj) }
                )
                    .flatten()
                    .filter { it != null }
                    .map { it as Any }
                    .toList().toTypedArray() as Array<KT>
                val aggregatedResult = Aggregators.topAndOthers(votes, limit, others, *mandatory)
                val count = aggregatedResult.size
                val partialDeclaration = votes.values.any { it == null }
                return aggregatedResult.entries.asSequence()
                    .sortedByDescending { e: Map.Entry<KT, Int?> ->
                        if (e.key === others) Int.MIN_VALUE
                        else (e.value ?: -1)
                    }
                    .map { e ->
                        val pct = e.value?.toDouble()?.div(total) ?: Double.NaN
                        val valueLabel: String = when {
                            count == 1 -> {
                                if (isPreference) "ELECTED" else "UNCONTESTED"
                            }
                            java.lang.Double.isNaN(pct) -> {
                                "WAITING..."
                            }
                            partialDeclaration -> {
                                THOUSANDS_FORMAT.format(e.value)
                            }
                            else -> {
                                voteTemplate.toBarString(
                                    e.value!!, pct, count > doubleLineBarLimit()
                                )
                            }
                        }
                        val shape: Shape? = if (e.key == winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else if ((runoff ?: emptySet()).contains(e.key)) keyTemplate.runoffShape(count > doubleLineBarLimit()) else null
                        BasicBar(
                            keyTemplate.toMainBarHeader(e.key, count > doubleLineBarLimit()),
                            keyTemplate.toParty(e.key).color,
                            if (java.lang.Double.isNaN(pct)) 0 else pct,
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
                .withHeader(header)
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
                    if (show) listOf(0.5 / pct.coerceAtLeast(1e-6))
                    else emptyList()
                }
                builder.withLines(lines) { majorityLabel!! }
            }
        }

        private inner class Change {
            private var _currVotes: Map<KT, Int?> = HashMap()
            private var _prevVotes: Map<Party, Int> = HashMap()

            var currVotes: Map<KT, Int?>
                get() = _currVotes
                set(currVotes) {
                    _currVotes = currVotes
                    updateBars()
                }

            var prevVotes: Map<Party, Int>
                get() = _prevVotes
                set(prevVotes) {
                    _prevVotes = prevVotes
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
                val prevWinner: Party? = pVotes.entries
                    .maxByOrNull { it.value }
                    ?.key
                val prevHasOther = pVotes.containsKey(Party.OTHERS)
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
                if (currTotal == 0 || prevTotal == 0) {
                    return emptyList()
                }
                val partyTotal = Aggregators.topAndOthers(
                    consolidate(currTotalByParty(cVotes), partiesToShow),
                    limit,
                    Party.OTHERS,
                    *mandatoryParties.toTypedArray()
                )
                    .toMutableMap()
                val prevVotes: MutableMap<Party, Int> = HashMap(pVotes)
                pVotes.entries.asSequence()
                    .filter { e: Map.Entry<Party, Int> -> !partyTotal.containsKey(e.key) }
                    .forEach { e: Map.Entry<Party, Int> ->
                        partyTotal.putIfAbsent(Party.OTHERS, 0)
                        if (e.key != Party.OTHERS) {
                            prevVotes.merge(Party.OTHERS, e.value) { a: Int, b: Int -> Integer.sum(a, b) }
                        }
                    }
                return partyTotal.entries.asSequence()
                    .sortedByDescending { e: Map.Entry<Party, Int> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value }
                    .map { e: Map.Entry<Party, Int> ->
                        val cpct = 1.0 * e.value / currTotal
                        val ppct = 1.0 * prevVotes.getOrDefault(e.key, 0) / prevTotal
                        BasicBar(
                            e.key.abbreviation.uppercase(),
                            e.key.color,
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
                    .withHeader(preferenceHeader!!)
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
                    .map { m: Map<Party, Int> ->
                        val total = m.values.sum()
                        m.entries.asSequence()
                            .sortedByDescending { it.value }
                            .map { e: Map.Entry<Party, Int> ->
                                BasicBar(
                                    e.key.name.uppercase(),
                                    e.key.color,
                                    1.0 * e.value / total,
                                    voteTemplate.toBarString(
                                        e.value, 1.0 * e.value / total, true
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
                val change = Change()
                current.subscribe(Subscriber { change.currVotes = it })
                prev.subscribe(Subscriber { change.prevVotes = it })
                val bars = change.barsPublisher
                val pctReporting = this.pctReporting
                return BarFrameBuilder.basic(bars)
                    .withWingspan(
                        pctReporting?.map { 0.1 / it.coerceAtLeast(1e-6) } ?: 0.1.asOneTimePublisher()
                    )
                    .withHeader(changeHeader!!)
                    .withSubhead(filteredChangeSubhead ?: (null as String?).asOneTimePublisher())
                    .build()
            }
        }

        override fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { swingHeader ->
                val curr: Flow.Publisher<out Map<Party, Int>>
                val prev: Flow.Publisher<out Map<Party, Int>>
                val currPreferences = this.currPreferences
                val prevPreferences = this.prevPreferences
                if (currPreferences != null && prevPreferences != null) {
                    curr = currPreferences.map { currTotalByParty(it) }
                    prev = prevPreferences
                        .merge(
                            currPreferences.map { currTotalByParty(it) }
                        ) { p: Map<Party, Int>, c: Map<Party, Int> ->
                            if (c.keys != p.keys) {
                                emptyMap()
                            } else {
                                p
                            }
                        }
                } else {
                    curr = current.map { currTotalByParty(it) }
                    prev = this.filteredPrev!!
                        .merge(current) { p: Map<Party, Int>, c: Map<KT, Int?> ->
                            val prevWinner: Party? = p.entries
                                .maxByOrNull { it.value }
                                ?.key
                            if (prevWinner == null ||
                                c.keys.asSequence()
                                    .map { key: KT -> keyTemplate.toParty(key) }
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

        private fun currTotalByParty(curr: Map<KT, Int?>): Map<Party, Int> {
            if (curr.values.any { it == null }) {
                return emptyMap()
            }
            val ret: MutableMap<Party, Int> = LinkedHashMap()
            curr.forEach { (k, v) -> ret.merge(keyTemplate.toParty(k), v ?: 0) { a: Int, b: Int -> Integer.sum(a, b) } }
            return ret
        }

        private fun consolidate(votes: Map<Party, Int>, parties: Set<Party>): Map<Party, Int> {
            return votes.entries.groupingBy { if (parties.contains(it.key)) it.key else Party.OTHERS }.fold(0) { a, e -> a + e.value }
        }
    }

    private class RangeVoteScreenBuilder<KT>(
        current: Flow.Publisher<out Map<KT, ClosedRange<Double>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        keyTemplate: KeyTemplate<KT>,
        voteTemplate: VoteTemplate,
        others: KT
    ) : VoteScreenBuilder<KT, ClosedRange<Double>, Double, Int>(current, header, subhead, keyTemplate, voteTemplate, others) {
        override fun createFrame(): BarFrame {
            val bars = current.map { r: Map<KT, ClosedRange<Double>> ->
                r.entries.asSequence()
                    .sortedByDescending { e: Map.Entry<KT, ClosedRange<Double>> -> if (e.key === others) Double.MIN_VALUE else (e.value.start + e.value.endInclusive) }
                    .map { e: Map.Entry<KT, ClosedRange<Double>> ->
                        val valueLabel = (
                            DECIMAL_FORMAT.format(100 * e.value.start) +
                                "-" +
                                DecimalFormat("0.0").format(100 * e.value.endInclusive) +
                                "%"
                            )
                        DualBar(
                            keyTemplate.toMainBarHeader(e.key, false),
                            keyTemplate.toParty(e.key).color,
                            e.value.start,
                            e.value.endInclusive,
                            valueLabel
                        )
                    }
                    .toList()
            }
            val notes = notes
            var builder = BarFrameBuilder.dual(bars)
                .withHeader(header)
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
            private var _currVotes: Map<KT, ClosedRange<Double>> = HashMap()
            private var _prevVotes: Map<Party, Int> = HashMap()

            var currVotes: Map<KT, ClosedRange<Double>>
                get() = _currVotes
                set(currVotes) {
                    _currVotes = currVotes
                    updateBars()
                }

            var prevVotes: Map<Party, Int>
                get() = _prevVotes
                set(prevVotes) {
                    _prevVotes = prevVotes
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
                val cVotes = this.currVotes
                val partyTotal = currTotalByParty(cVotes)
                val prevVotes: MutableMap<Party, Int> = HashMap(pVotes)
                pVotes.entries.asSequence()
                    .filter { e: Map.Entry<Party, Int> -> !partyTotal.containsKey(e.key) }
                    .forEach { e: Map.Entry<Party, Int> ->
                        partyTotal.putIfAbsent(Party.OTHERS, (0.0).rangeTo(0.0))
                        prevVotes.merge(Party.OTHERS, e.value) { a: Int, b: Int -> Integer.sum(a, b) }
                    }
                return partyTotal.entries.asSequence()
                    .sortedByDescending { e: Map.Entry<Party, ClosedRange<Double>> -> if (e.key === Party.OTHERS) Double.MIN_VALUE else (e.value.start + e.value.endInclusive) }
                    .map { e: Map.Entry<Party, ClosedRange<Double>> ->
                        val cpctMin = e.value.start
                        val cpctMax = e.value.endInclusive
                        val ppct = 1.0 * prevVotes.getOrDefault(e.key, 0) / prevTotal
                        DualBar(
                            e.key.abbreviation.uppercase(),
                            e.key.color,
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
                val change = Change()
                current.subscribe(Subscriber { change.currVotes = it })
                prev.subscribe(Subscriber { change.prevVotes = it })
                val bars = change.barsPublisher
                return BarFrameBuilder.dual(bars)
                    .withWingspan(0.1.asOneTimePublisher())
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
                val bars = currPreferences.map { r: Map<KT, ClosedRange<Double>> ->
                    r.entries.asSequence()
                        .sortedByDescending { e: Map.Entry<KT, ClosedRange<Double>> -> if (e.key === others) Double.MIN_VALUE else (e.value.start + e.value.endInclusive) }
                        .map { e: Map.Entry<KT, ClosedRange<Double>> ->
                            val valueLabel = (
                                DECIMAL_FORMAT.format(100 * e.value.start) +
                                    "-" +
                                    DecimalFormat("0.0").format(100 * e.value.endInclusive) +
                                    "%"
                                )
                            DualBar(
                                keyTemplate.toMainBarHeader(e.key, false),
                                keyTemplate.toParty(e.key).color,
                                e.value.start,
                                e.value.endInclusive,
                                valueLabel
                            )
                        }
                        .toList()
                }
                var builder = BarFrameBuilder.dual(bars)
                    .withHeader(preferenceHeader!!)
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
                    .map { currTotalByParty(it) }
                    .map { m: Map<Party, ClosedRange<Double>> ->
                        val ret: MutableMap<Party, Int> = LinkedHashMap()
                        m.forEach { (p: Party, r: ClosedRange<Double>) -> ret[p] = (1000000 * (r.start + r.endInclusive) / 2).roundToInt() }
                        ret
                    }
                return SwingFrameBuilder.prevCurr(filteredPrev!!, curr, swingComparator!!)
                    .withHeader(swingHeader)
                    .build()
            }
        }

        private fun currTotalByParty(curr: Map<KT, ClosedRange<Double>>): MutableMap<Party, ClosedRange<Double>> {
            val ret: MutableMap<Party, ClosedRange<Double>> = LinkedHashMap()
            curr.forEach { (k: KT, v: ClosedRange<Double>) ->
                ret.merge(keyTemplate.toParty(k), v) { a, b ->
                    (a.start + b.start).rangeTo(a.endInclusive + b.endInclusive)
                }
            }
            return ret
        }

        companion object {
            private val DECIMAL_FORMAT = DecimalFormat("0.0")
            private val CHANGE_DECIMAL_FORMAT = DecimalFormat("+0.0;-0.0")
        }
    }

    class PartyQuotaScreenBuilder(
        private val quotas: Flow.Publisher<out Map<Party, Double>>,
        private val totalSeats: Flow.Publisher<out Int>,
        private val header: Flow.Publisher<out String?>,
        private val subhead: Flow.Publisher<out String?>
    ) {
        private var prevQuotas: Flow.Publisher<out Map<Party, Double>>? = null
        private var changeHeader: Flow.Publisher<out String>? = null

        private var swingCurrVotes: Flow.Publisher<out Map<Party, Int>>? = null
        private var swingPrevVotes: Flow.Publisher<out Map<Party, Int>>? = null
        private var swingComparator: Comparator<Party>? = null
        private var swingHeader: Flow.Publisher<out String?>? = null

        private var mapBuilder: MapBuilder<*>? = null

        fun withPrev(
            prevQuotas: Flow.Publisher<out Map<Party, Double>>,
            changeHeader: Flow.Publisher<out String>
        ): PartyQuotaScreenBuilder {
            this.prevQuotas = prevQuotas
            this.changeHeader = changeHeader
            return this
        }

        fun withSwing(
            currVotes: Flow.Publisher<out Map<Party, Int>>,
            prevVotes: Flow.Publisher<out Map<Party, Int>>,
            comparator: Comparator<Party>,
            header: Flow.Publisher<out String?>
        ): PartyQuotaScreenBuilder {
            this.swingCurrVotes = currVotes
            this.swingPrevVotes = prevVotes
            this.swingComparator = comparator
            this.swingHeader = header
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out Party?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): PartyQuotaScreenBuilder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty.map { party: Party? -> PartyResult.elected(party) }, focus, header)
            return this
        }

        fun build(textHeader: Flow.Publisher<out String>): BasicResultPanel {
            return BasicResultPanel(
                createHeaderLabel(textHeader),
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
                        .sortedByDescending { if (it.key == Party.OTHERS) -1.0 else it.value }
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
                linesPublisher = totalSeats.map { lines -> (1 until lines).map { BarFrame.Line(it, "$it QUOTA${if (it == 1) "" else "S"}") } }
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
                            curr.asSequence().sortedByDescending { if (it.key == Party.OTHERS) -1.0 else it.value }.map { it.key },
                            prev.keys.asSequence().filter { !curr.containsKey(it) }.sortedByDescending { if (it == Party.OTHERS) -1 else 0 }
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
        private fun <T> partyMapToResultMap(m: Map<T, Party?>): Map<T, PartyResult?> {
            val ret: MutableMap<T, PartyResult?> = LinkedHashMap()
            m.forEach { (k: T, v: Party?) -> ret[k] = if (v == null) null else PartyResult.elected(v) }
            return ret
        }

        private fun createHeaderLabel(textPublisher: Flow.Publisher<out String>): JLabel {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textPublisher.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            return headerLabel
        }

        @JvmStatic fun partySeats(
            seats: Flow.Publisher<out Map<Party, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Party, Int, Int> {
            return BasicSeatScreenBuilder(
                seats,
                header,
                subhead,
                PartyTemplate()
            )
        }

        @JvmStatic fun candidateSeats(
            seats: Flow.Publisher<out Map<Candidate, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Candidate, Int, Int> {
            return BasicSeatScreenBuilder(
                seats,
                header,
                subhead,
                CandidateTemplate()
            )
        }

        @JvmStatic fun partyDualSeats(
            seats: Flow.Publisher<out Map<Party, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Party, Pair<Int, Int>, Pair<Int, Int>> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                PartyTemplate(),
                DualSeatScreenBuilder.FocusLocation.FIRST
            )
        }

        @JvmStatic fun partyDualSeatsReversed(
            seats: Flow.Publisher<out Map<Party, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Party, Pair<Int, Int>, Pair<Int, Int>> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                PartyTemplate(),
                DualSeatScreenBuilder.FocusLocation.LAST
            )
        }

        @JvmStatic fun candidateDualSeats(
            seats: Flow.Publisher<out Map<Candidate, Pair<Int, Int>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Candidate, Pair<Int, Int>, Pair<Int, Int>> {
            return DualSeatScreenBuilder(
                seats,
                header,
                subhead,
                CandidateTemplate(),
                DualSeatScreenBuilder.FocusLocation.FIRST
            )
        }

        @JvmStatic fun partyRangeSeats(
            seats: Flow.Publisher<out Map<Party, IntRange>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Party, IntRange, Int> {
            return RangeSeatScreenBuilder(
                seats,
                header,
                subhead,
                PartyTemplate()
            )
        }

        @JvmStatic fun candidateRangeSeats(
            seats: Flow.Publisher<out Map<Candidate, IntRange>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): SeatScreenBuilder<Candidate, IntRange, Int> {
            return RangeSeatScreenBuilder(
                seats,
                header,
                subhead,
                CandidateTemplate()
            )
        }

        @JvmStatic fun partyVotes(
            votes: Flow.Publisher<out Map<Party, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<Party, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                PartyTemplate(),
                PctOnlyTemplate(),
                Party.OTHERS
            )
        }

        @JvmStatic fun candidateVotes(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<Candidate, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                CandidateTemplate(),
                VotePctTemplate(),
                Candidate.OTHERS
            )
        }

        @JvmStatic fun candidateVotesPctOnly(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<Candidate, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                CandidateTemplate(),
                VotePctOnlyTemplate(),
                Candidate.OTHERS
            )
        }

        @JvmStatic fun candidateVotes(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            incumbentMarker: String
        ): VoteScreenBuilder<Candidate, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                CandidateTemplate(incumbentMarker),
                VotePctTemplate(),
                Candidate.OTHERS
            )
        }

        @JvmStatic fun candidateVotesPctOnly(
            votes: Flow.Publisher<out Map<Candidate, Int?>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            incumbentMarker: String
        ): VoteScreenBuilder<Candidate, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                votes,
                header,
                subhead,
                CandidateTemplate(incumbentMarker),
                VotePctOnlyTemplate(),
                Candidate.OTHERS
            )
        }

        @JvmStatic fun partyRangeVotes(
            votes: Flow.Publisher<out Map<Party, ClosedRange<Double>>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): VoteScreenBuilder<Party, ClosedRange<Double>, Double, Int> {
            return RangeVoteScreenBuilder(
                votes,
                header,
                subhead,
                PartyTemplate(),
                PctOnlyTemplate(),
                Party.OTHERS
            )
        }

        @JvmStatic fun partyQuotas(
            quotas: Flow.Publisher<out Map<Party, Double>>,
            totalSeats: Flow.Publisher<out Int>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>
        ): PartyQuotaScreenBuilder {
            return PartyQuotaScreenBuilder(
                quotas,
                totalSeats,
                header,
                subhead
            )
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
        panel.add(seatFrame)
        if (preferenceFrame != null) panel.add(preferenceFrame)
        if (changeFrame != null) panel.add(changeFrame)
        if (swingFrame != null) panel.add(swingFrame)
        if (mapFrame != null) panel.add(mapFrame)
    }
}
