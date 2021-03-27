package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.BarFrameBuilder.DualBar
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.Shape
import java.text.DecimalFormat
import java.util.Comparator
import java.util.HashMap
import java.util.LinkedHashMap
import java.util.Objects
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.roundToInt

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
                    height * (if (preferenceFrame == null) 3 else 2) / 3 - 10)
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
            return key.name.toUpperCase()
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
                key.party.name.toUpperCase()
            } else ("${key.name}${if (key.isIncumbent()) incumbentMarker else ""}${if (forceSingleLine) (" (" + key.party.abbreviation + ")") else ("\n" + key.party.name)}")
                    .toUpperCase()
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createTickShape() else ImageGenerator.createHalfTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createRunoffShape() else ImageGenerator.createHalfRunoffShape()
        }
    }

    abstract class SeatScreenBuilder<KT, CT, PT> internal constructor(
        protected var current: BindingReceiver<Map<KT, CT>>,
        protected var header: BindingReceiver<String?>,
        protected var subhead: BindingReceiver<String?>,
        protected val keyTemplate: KeyTemplate<KT>
    ) {
        protected var total: BindingReceiver<Int>? = null
        protected var showMajority: BindingReceiver<Boolean>? = null
        protected var majorityFunction: ((Int) -> String)? = null
        protected var winner: BindingReceiver<KT?>? = null
        protected var notes: BindingReceiver<String?>? = null
        protected var diff: BindingReceiver<Map<Party, CurrDiff<CT>>>? = null
        protected var changeHeader: BindingReceiver<String?>? = null
        protected var changeSubhead: BindingReceiver<String?>? = null
        protected var currVotes: BindingReceiver<Map<Party, Int>>? = null
        protected var prevVotes: BindingReceiver<Map<Party, Int>>? = null
        protected var swingHeader: BindingReceiver<String?>? = null
        protected var swingComparator: Comparator<Party>? = null
        protected var classificationFunc: ((Party) -> Party)? = null
        protected var classificationHeader: BindingReceiver<String?>? = null
        protected var mapBuilder: MapBuilder<*>? = null
        fun withTotal(totalSeats: Binding<Int>): SeatScreenBuilder<KT, CT, PT> {
            total = BindingReceiver(totalSeats)
            return this
        }

        fun withMajorityLine(
            showMajority: Binding<Boolean>,
            majorityLabelFunc: (Int) -> String
        ): SeatScreenBuilder<KT, CT, PT> {
            this.showMajority = BindingReceiver(showMajority)
            majorityFunction = majorityLabelFunc
            return this
        }

        fun withWinner(winner: Binding<KT?>): SeatScreenBuilder<KT, CT, PT> {
            this.winner = BindingReceiver(winner)
            return this
        }

        @JvmOverloads
        fun withDiff(
            diff: Binding<Map<Party, CT>>,
            changeHeader: Binding<String?>,
            changeSubhead: Binding<String?> = Binding.fixedBinding(null)
        ): SeatScreenBuilder<KT, CT, PT> {
            this.diff = BindingReceiver(
                    current
                            .getBinding()
                            .merge(diff) { c, d ->
                                val ret = LinkedHashMap<Party, CurrDiff<CT>>()
                                c.forEach { (k, v) -> ret[keyTemplate.toParty(k)] = createFromDiff(v, d[keyTemplate.toParty(k)]) }
                                d.forEach { (k, v) -> ret.putIfAbsent(k, createFromDiff(v)) }
                                ret
                            })
            this.changeHeader = BindingReceiver(changeHeader)
            this.changeSubhead = BindingReceiver(changeSubhead)
            return this
        }

        protected abstract fun createFromDiff(curr: CT, diff: CT?): CurrDiff<CT>
        protected abstract fun createFromDiff(diff: CT): CurrDiff<CT>

        @JvmOverloads
        fun withPrev(
            prev: Binding<Map<Party, PT>>,
            changeHeader: Binding<String?>,
            changeSubhead: Binding<String?> = Binding.fixedBinding(null)
        ): SeatScreenBuilder<KT, CT, PT> {
            diff = BindingReceiver(
                    current
                            .getBinding()
                            .merge(prev) { c, p ->
                                val ret = LinkedHashMap<Party, CurrDiff<CT>>()
                                c.forEach { (k, v) -> ret[keyTemplate.toParty(k)] = createFromPrev(v, p[keyTemplate.toParty(k)]) }
                                p.forEach { (k, v) -> ret.putIfAbsent(k, createFromPrev(v)) }
                                ret
                            })
            this.changeHeader = BindingReceiver(changeHeader)
            this.changeSubhead = BindingReceiver(changeSubhead)
            return this
        }

        protected abstract fun createFromPrev(curr: CT, prev: PT?): CurrDiff<CT>
        protected abstract fun createFromPrev(prev: PT): CurrDiff<CT>

        fun withSwing(
            currVotes: Binding<Map<Party, Int>>,
            prevVotes: Binding<Map<Party, Int>>,
            comparator: Comparator<Party>,
            header: Binding<String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            swingHeader = BindingReceiver(header)
            this.currVotes = BindingReceiver(currVotes)
            this.prevVotes = BindingReceiver(prevVotes)
            swingComparator = comparator
            return this
        }

        fun <T> withPartyMap(
            shapes: Binding<Map<T, Shape>>,
            winners: Binding<Map<T, Party?>>,
            focus: Binding<List<T>?>,
            headerBinding: Binding<String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            mapBuilder = MapBuilder(
                    shapes, winners.map { m: Map<T, Party?> -> partyMapToResultMap(m) }, focus, headerBinding)
            return this
        }

        fun <T> withResultMap(
            shapes: Binding<Map<T, Shape>>,
            winners: Binding<Map<T, PartyResult?>>,
            focus: Binding<List<T>?>,
            headerBinding: Binding<String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerBinding)
            return this
        }

        fun <T> withResultMap(
            shapes: Binding<Map<T, Shape>>,
            winners: Binding<Map<T, PartyResult?>>,
            focus: Binding<List<T>?>,
            additionalHighlight: Binding<List<T>?>,
            headerBinding: Binding<String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            mapBuilder = MapBuilder(shapes, winners, Pair(focus, additionalHighlight), headerBinding)
            return this
        }

        fun withNotes(notes: Binding<String?>): SeatScreenBuilder<KT, CT, PT> {
            this.notes = BindingReceiver(notes)
            return this
        }

        fun withClassification(
            classificationFunc: (Party) -> Party,
            classificationHeader: Binding<String?>
        ): SeatScreenBuilder<KT, CT, PT> {
            this.classificationFunc = classificationFunc
            this.classificationHeader = BindingReceiver(classificationHeader)
            return this
        }

        fun build(textHeader: Binding<String>): BasicResultPanel {
            return BasicResultPanel(
                    createHeaderLabel(textHeader),
                    createFrame(),
                    createClassificationFrame(),
                    createDiffFrame(),
                    createSwingFrame(),
                    createMapFrame())
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
                        if (func == null) prev.getBinding() else Aggregators.adjustKey(prev.getBinding(), func),
                        if (func == null) curr.getBinding() else Aggregators.adjustKey(curr.getBinding(), func),
                        swingComparator!!)
                        .withHeader(header.getBinding())
                        .build()
            }
        }

        private fun createMapFrame(): MapFrame? {
            return mapBuilder?.createMapFrame()
        }
    }

    private class BasicSeatScreenBuilder<KT>(
        current: BindingReceiver<Map<KT, Int>>,
        header: BindingReceiver<String?>,
        subhead: BindingReceiver<String?>,
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

        private class Result<KT> : Bindable<Result<KT>, Result.Property>() {
            enum class Property {
                SEATS, WINNER
            }

            private var _seats: Map<KT, Int> = HashMap()
            private var _winner: KT? = null

            var seats: Map<KT, Int>
            get() = _seats
            set(seats) {
                _seats = seats
                onPropertyRefreshed(Property.SEATS)
            }

            var winner: KT?
            get() = _winner
            set(winner) {
                _winner = winner
                onPropertyRefreshed(Property.WINNER)
            }
        }

        private fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createFrame(): BarFrame {
            val result = Result<KT>()
            current.getBinding().bind { result.seats = it }
            winner?.getBinding()?.bind { result.winner = it }
            val bars = Binding.propertyBinding(
                    result,
                    { r: Result<KT> ->
                        val numBars = r.seats.size
                        r.seats.entries.asSequence()
                                .sortedByDescending { e: Map.Entry<KT, Int> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value }
                                .map { e: Map.Entry<KT, Int> ->
                                    BasicBar(
                                            keyTemplate.toMainBarHeader(
                                                    e.key, numBars > doubleLineBarLimit()),
                                            keyTemplate.toParty(e.key).color,
                                            e.value, e.value.toString(),
                                            if (e.key == r.winner) keyTemplate.winnerShape(numBars > doubleLineBarLimit()) else null)
                                }
                                .toList()
                    },
                    Result.Property.SEATS,
                    Result.Property.WINNER)
            var builder = BarFrameBuilder.basic(bars)
                    .withHeader(header.getBinding())
                    .withSubhead(subhead.getBinding())
                    .withNotes(this.notes?.getBinding() ?: Binding.fixedBinding(null))
            val total = this.total
            if (total != null) {
                builder = builder.withMax(total.getBinding { it * 2 / 3 })
            }
            applyMajorityLine(builder)
            return builder.build()
        }

        private fun applyMajorityLine(builder: BarFrameBuilder) {
            val showMajority = this.showMajority
            if (showMajority != null) {
                val total = this.total ?: throw IllegalArgumentException("Cannot show majority line without total")
                val lines = BindableList<Int>()
                showMajority
                        .getBinding()
                        .bind { show ->
                            lines.clear()
                            if (show) {
                                lines.add(total.value / 2 + 1)
                            }
                        }
                total
                        .getBinding()
                        .bind { tot ->
                            if (!lines.isEmpty()) {
                                lines[0] = tot / 2 + 1
                            }
                        }
                builder.withLines(lines) { t -> majorityFunction!!(t) }
            }
        }

        override fun createClassificationFrame(): BarFrame? {
            return classificationHeader?.let { classificationHeader ->
                val bars: Binding<List<BasicBar>> = Aggregators.adjustKey(
                        current.getBinding()) { classificationFunc!!(keyTemplate.toParty(it)) }
                        .map { seats: Map<Party, Int> ->
                            seats.entries.asSequence()
                                    .sortedByDescending { it.value }
                                    .map { e: Map.Entry<Party, Int> ->
                                        BasicBar(
                                                e.key.name.toUpperCase(),
                                                e.key.color,
                                                e.value)
                                    }
                                    .toList()
                        }
                var builder = BarFrameBuilder.basic(bars).withHeader(classificationHeader.getBinding())
                val total = this.total
                if (total != null) {
                    builder = builder.withMax(total.getBinding { it * 2 / 3 })
                }
                applyMajorityLine(builder)
                return builder.build()
            }
        }

        override fun createDiffFrame(): BarFrame? {
            return changeHeader?.let { changeHeader ->
                val bars = diff!!.getBinding { map: Map<Party, CurrDiff<Int>> ->
                    map.entries.asSequence()
                            .sortedByDescending { e: Map.Entry<Party, CurrDiff<Int>> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value.curr }
                            .map { e: Map.Entry<Party, CurrDiff<Int>> ->
                                BasicBar(
                                        e.key.abbreviation.toUpperCase(),
                                        e.key.color,
                                        e.value.diff,
                                        changeStr(e.value.diff))
                            }
                            .toList()
                }
                var builder = BarFrameBuilder.basic(bars)
                        .withHeader(changeHeader.getBinding())
                        .withSubhead(changeSubhead?.getBinding() ?: Binding.fixedBinding(null))
                val total = this.total
                if (total != null) {
                    builder = builder.withWingspan(total.getBinding { (it / 20).coerceAtLeast(1) })
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
        current: BindingReceiver<Map<KT, Pair<Int, Int>>>,
        header: BindingReceiver<String?>,
        subhead: BindingReceiver<String?>,
        keyTemplate: KeyTemplate<KT>
    ) : SeatScreenBuilder<KT, Pair<Int, Int>, Pair<Int, Int>>(current, header, subhead, keyTemplate) {
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

        private class Result<KT> : Bindable<Result<KT>, Result.Property>() {
            enum class Property {
                SEATS, WINNER
            }

            private var _seats: Map<KT, Pair<Int, Int>> = HashMap()
            private var _winner: KT? = null

            var seats: Map<KT, Pair<Int, Int>>
            get() = _seats
            set(seats) {
                _seats = seats
                onPropertyRefreshed(Property.SEATS)
            }

            var winner: KT?
            get() = _winner
            set(winner) {
                _winner = winner
                onPropertyRefreshed(Property.WINNER)
            }
        }

        private fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createFrame(): BarFrame {
            val result = Result<KT>()
            current.getBinding().bind { result.seats = it }
            winner?.getBinding()?.bind { result.winner = it }
            val bars = Binding.propertyBinding(
                    result,
                    { r: Result<KT> ->
                        val count = r.seats.size
                        r.seats.entries.asSequence()
                                .sortedByDescending { e: Map.Entry<KT, Pair<Int, Int>> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value.second }
                                .map { e: Map.Entry<KT, Pair<Int, Int>> ->
                                    DualBar(
                                            keyTemplate.toMainBarHeader(
                                                    e.key, count > doubleLineBarLimit()),
                                            keyTemplate.toParty(e.key).color,
                                            e.value.first,
                                            e.value.second,
                                            e.value.first.toString() + "/" + e.value.second,
                                            if (e.key == r.winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else null)
                                }
                                .toList()
                    },
                    Result.Property.SEATS,
                    Result.Property.WINNER)
            var builder = BarFrameBuilder.dual(bars)
                    .withHeader(header.getBinding())
                    .withSubhead(subhead.getBinding())
                    .withNotes(this.notes?.getBinding() ?: Binding.fixedBinding(null))
            val total = this.total
            if (total != null) {
                builder = builder.withMax(total.getBinding { it * 2 / 3 })
            }
            val showMajority = this.showMajority
            if (showMajority != null) {
                if (total == null) {
                    throw IllegalStateException("Cannot show majority line without total")
                }
                val lines = BindableList<Int>()
                showMajority
                        .getBinding()
                        .bind { show ->
                            lines.clear()
                            if (show) {
                                lines.add(total.value / 2 + 1)
                            }
                        }
                total
                        .getBinding()
                        .bind { tot ->
                            if (!lines.isEmpty()) {
                                lines[0] = tot / 2 + 1
                            }
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
                val bars = diff!!.getBinding { map: Map<Party, CurrDiff<Pair<Int, Int>>> ->
                    map.entries.asSequence()
                            .sortedByDescending { e: Map.Entry<Party, CurrDiff<Pair<Int, Int>>> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value.curr.second }
                            .map { e: Map.Entry<Party, CurrDiff<Pair<Int, Int>>> ->
                                DualBar(
                                        e.key.abbreviation.toUpperCase(),
                                        e.key.color,
                                        e.value.diff.first,
                                        e.value.diff.second,
                                        changeStr(e.value.diff.first) +
                                                "/" +
                                                changeStr(e.value.diff.second))
                            }
                            .toList()
                }
                var builder = BarFrameBuilder.dual(bars)
                        .withHeader(changeHeader.getBinding())
                        .withSubhead(changeSubhead?.getBinding() ?: Binding.fixedBinding(null))
                val total = this.total
                if (total != null) {
                    builder = builder.withWingspan(total.getBinding { (it / 20).coerceAtLeast(1) })
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
        current: BindingReceiver<Map<KT, IntRange>>,
        header: BindingReceiver<String?>,
        subhead: BindingReceiver<String?>,
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

        private class Result<KT> : Bindable<Result<KT>, Result.Property>() {
            enum class Property {
                SEATS, WINNER
            }

            private var _seats: Map<KT, IntRange> = HashMap()
            private var _winner: KT? = null

            var seats: Map<KT, IntRange>
            get() = _seats
            set(seats) {
                _seats = seats
                onPropertyRefreshed(Property.SEATS)
            }

            var winner: KT?
            get() = _winner
            set(winner) {
                _winner = winner
                onPropertyRefreshed(Property.WINNER)
            }
        }

        private fun doubleLineBarLimit(): Int {
            return 10
        }

        override fun createFrame(): BarFrame {
            val result = Result<KT>()
            current.getBinding().bind { result.seats = it }
            winner?.getBinding()?.bind { result.winner = it }
            val bars = Binding.propertyBinding(
                    result,
                    { r: Result<KT> ->
                        val count = r.seats.size
                        r.seats.entries.asSequence()
                                .sortedByDescending { e: Map.Entry<KT, IntRange> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else (e.value.first + e.value.last) }
                                .map { e: Map.Entry<KT, IntRange> ->
                                    DualBar(
                                            keyTemplate.toMainBarHeader(
                                                    e.key, count > doubleLineBarLimit()),
                                            keyTemplate.toParty(e.key).color,
                                            e.value.first,
                                            e.value.last,
                                            e.value.first.toString() + "-" + e.value.last,
                                            if (e.key == r.winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else null)
                                }
                                .toList()
                    },
                    Result.Property.SEATS,
                    Result.Property.WINNER)
            val notes = this.notes
            var builder = BarFrameBuilder.dual(bars)
                    .withHeader(header.getBinding())
                    .withSubhead(subhead.getBinding())
                    .withNotes(notes?.getBinding() ?: Binding.fixedBinding(null))
            val total = this.total
            if (total != null) {
                builder = builder.withMax(total.getBinding { it * 2 / 3 })
            }
            val showMajority = this.showMajority
            if (showMajority != null) {
                if (total == null) {
                    throw IllegalStateException("Cannot show majority without total")
                }
                val lines = BindableList<Int>()
                showMajority
                        .getBinding()
                        .bind { show ->
                            lines.clear()
                            if (show) {
                                lines.add(total.value / 2 + 1)
                            }
                        }
                total
                        .getBinding()
                        .bind { tot ->
                            if (!lines.isEmpty()) {
                                lines[0] = tot / 2 + 1
                            }
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
                val bars = diff!!.getBinding { map: Map<Party, CurrDiff<IntRange>> ->
                    map.entries.asSequence()
                            .sortedByDescending { e: Map.Entry<Party, CurrDiff<IntRange>> ->
                                if (e.key === Party.OTHERS) Int.MIN_VALUE
                                else (e.value.curr.first + e.value.curr.last)
                            }
                            .map { e: Map.Entry<Party, CurrDiff<IntRange>> ->
                                DualBar(
                                        e.key.abbreviation.toUpperCase(),
                                        e.key.color,
                                        e.value.diff.first,
                                        e.value.diff.last,
                                        "(" +
                                                changeStr(e.value.diff.first) +
                                                ")-(" +
                                                changeStr(e.value.diff.last) +
                                                ")")
                            }
                            .toList()
                }
                var builder = BarFrameBuilder.dual(bars)
                        .withHeader(changeHeader.getBinding())
                        .withSubhead(changeSubhead?.getBinding() ?: Binding.fixedBinding(null))
                val total = this.total
                if (total != null) {
                    builder = builder.withWingspan(total.getBinding { (it / 20).coerceAtLeast(1) })
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
            return (THOUSANDS_FORMAT.format(votes.toLong()) +
                    (if (forceSingleLine) " (" else "\n") +
                    PCT_FORMAT.format(pct) +
                    (if (forceSingleLine) ")" else ""))
        }
    }

    private class VotePctOnlyTemplate : VoteTemplate {
        override fun toBarString(votes: Int, pct: Double, forceSingleLine: Boolean): String {
            return PCT_FORMAT.format(pct)
        }
    }

    abstract class VoteScreenBuilder<KT, CT, CPT, PT> internal constructor(
        protected var current: BindingReceiver<Map<KT, CT>>,
        protected var header: BindingReceiver<String?>,
        protected var subhead: BindingReceiver<String?>,
        protected val keyTemplate: KeyTemplate<KT>,
        protected val voteTemplate: VoteTemplate,
        protected val others: KT
    ) {
        protected var showMajority: BindingReceiver<Boolean>? = null
        protected var majorityLabel: BindingReceiver<String>? = null
        protected var winner: BindingReceiver<KT?>? = null
        protected var runoff: BindingReceiver<Set<KT>?>? = null
        protected var pctReporting: BindingReceiver<Double>? = null
        protected var notes: BindingReceiver<String?>? = null
        protected var limit = Int.MAX_VALUE
        protected var mandatoryParties: Set<Party> = emptySet()
        protected var prev: BindingReceiver<Map<Party, PT>>? = null
        protected var changeHeader: BindingReceiver<String?>? = null
        protected var changeSubhead: BindingReceiver<String?>? = null
        protected var currPreferences: BindingReceiver<Map<KT, CT>>? = null
        protected var prevPreferences: BindingReceiver<Map<Party, PT>>? = null
        protected var preferenceHeader: BindingReceiver<String?>? = null
        protected var preferenceSubhead: BindingReceiver<String?>? = null
        protected var preferencePctReporting: BindingReceiver<Double>? = null
        protected var swingHeader: BindingReceiver<String?>? = null
        protected var swingComparator: Comparator<Party>? = null
        protected var classificationFunc: ((Party) -> Party)? = null
        protected var classificationHeader: BindingReceiver<String?>? = null
        protected var mapBuilder: MapBuilder<*>? = null

        @JvmOverloads
        fun withPrev(
            prev: Binding<Map<Party, PT>>,
            header: Binding<String?>,
            subhead: Binding<String?> = Binding.fixedBinding(null)
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.prev = BindingReceiver(prev)
            changeHeader = BindingReceiver(header)
            changeSubhead = BindingReceiver(subhead)
            return this
        }

        fun withPreferences(
            preferences: Binding<Map<KT, CT>>,
            preferenceHeader: Binding<String?>,
            preferenceSubhead: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            currPreferences = BindingReceiver(preferences)
            this.preferenceHeader = BindingReceiver(preferenceHeader)
            this.preferenceSubhead = BindingReceiver(preferenceSubhead)
            return this
        }

        fun withPrevPreferences(
            prevPreferences: Binding<Map<Party, PT>>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.prevPreferences = BindingReceiver(prevPreferences)
            return this
        }

        fun withWinner(winner: Binding<KT?>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.winner = BindingReceiver(winner)
            return this
        }

        fun withRunoff(runoff: Binding<Set<KT>?>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.runoff = BindingReceiver(runoff)
            return this
        }

        fun withPctReporting(pctReporting: Binding<Double>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.pctReporting = BindingReceiver(pctReporting)
            return this
        }

        fun withPreferencePctReporting(
            preferencePctReporting: Binding<Double>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.preferencePctReporting = BindingReceiver(preferencePctReporting)
            return this
        }

        fun withSwing(
            comparator: Comparator<Party>?,
            header: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            swingComparator = comparator
            swingHeader = BindingReceiver(header)
            return this
        }

        fun <T> withPartyMap(
            shapes: Binding<Map<T, Shape>>,
            winners: Binding<Map<T, Party?>>,
            focus: Binding<List<T>?>,
            headerBinding: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners.map { m: Map<T, Party?> -> partyMapToResultMap(m) }, focus, headerBinding)
            return this
        }

        fun <T> withPartyMap(
            shapes: Binding<Map<T, Shape>>,
            selectedShape: Binding<T>,
            leadingParty: Binding<Party?>,
            focus: Binding<List<T>?>,
            header: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty.map { party: Party? -> PartyResult.elected(party) }, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Binding<Map<T, Shape>>,
            winners: Binding<Map<T, PartyResult?>>,
            focus: Binding<List<T>?>,
            headerBinding: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerBinding)
            return this
        }

        fun <T> withResultMap(
            shapes: Binding<Map<T, Shape>>,
            winners: Binding<Map<T, PartyResult?>>,
            focus: Pair<Binding<List<T>?>, Binding<List<T>?>>,
            headerBinding: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, winners, focus, headerBinding)
            return this
        }

        fun <T> withResultMap(
            shapes: Binding<Map<T, Shape>>,
            selectedShape: Binding<T>,
            leadingParty: Binding<PartyResult?>,
            focus: Binding<List<T>?>,
            header: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Binding<Map<T, Shape>>,
            selectedShape: Binding<T>,
            leadingParty: Binding<PartyResult?>,
            focus: Binding<List<T>?>,
            additionalHighlight: Binding<List<T>?>,
            header: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, additionalHighlight, header)
            return this
        }

        fun withMajorityLine(
            showMajority: Binding<Boolean>,
            majorityLabel: Binding<String>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.showMajority = BindingReceiver(showMajority)
            this.majorityLabel = BindingReceiver(majorityLabel)
            return this
        }

        fun withNotes(notes: Binding<String?>): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.notes = BindingReceiver(notes)
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
            classificationHeader: Binding<String?>
        ): VoteScreenBuilder<KT, CT, CPT, PT> {
            this.classificationFunc = classificationFunc
            this.classificationHeader = BindingReceiver(classificationHeader)
            return this
        }

        fun build(textHeader: Binding<String>): BasicResultPanel {
            return BasicResultPanel(
                    createHeaderLabel(textHeader),
                    createFrame(),
                    if (classificationHeader == null) createPreferenceFrame() else createClassificationFrame(),
                    createDiffFrame(),
                    createSwingFrame(),
                    createMapFrame())
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
        current: BindingReceiver<Map<KT, Int?>>,
        header: BindingReceiver<String?>,
        subhead: BindingReceiver<String?>,
        keyTemplate: KeyTemplate<KT>,
        voteTemplate: VoteTemplate,
        others: KT
    ) : VoteScreenBuilder<KT, Int?, Double?, Int>(current, header, subhead, keyTemplate, voteTemplate, others) {
        private class Result<KT> : Bindable<Result<KT>, Result.Property>() {
            enum class Property {
                VOTES, WINNER, RUNOFF
            }

            private var _votes: Map<KT, Int?> = HashMap()
            private var _winner: KT? = null
            private var _runoff: Set<KT>? = null

            var votes: Map<KT, Int?>
            get() = _votes
            set(votes) {
                _votes = votes
                onPropertyRefreshed(Property.VOTES)
            }

            var winner: KT?
            get() = _winner
            set(winner) {
                _winner = winner
                onPropertyRefreshed(Property.WINNER)
            }

            var runoff: Set<KT>?
            get() = _runoff
            set(runoff) {
                _runoff = runoff
                onPropertyRefreshed(Property.RUNOFF)
            }
        }

        private fun doubleLineBarLimit(): Int {
            return if (currPreferences == null) 10 else 6
        }

        override fun createFrame(): BarFrame {
            val result = Result<KT>()
            current.getBinding().bind { result.votes = it }
            winner?.getBinding()?.bind { result.winner = it }
            runoff?.getBinding()?.bind { result.runoff = it }
            val bars = Binding.propertyBinding(
                    result,
                    { r: Result<KT> ->
                        val total = r.votes.values.filterNotNull().sum()
                        @Suppress("UNCHECKED_CAST") val mandatory = sequenceOf(
                                r.votes.keys.asSequence()
                                        .filter { k: KT -> mandatoryParties.contains(keyTemplate.toParty(k)) },
                                (r.runoff?.asSequence() ?: emptySequence()), sequenceOf(r.winner)
                                        .filter { obj -> Objects.nonNull(obj) })
                                .flatten()
                                .filter { it != null }
                                .map { it as Any }
                                .toList().toTypedArray() as Array<KT>
                        val aggregatedResult = Aggregators.topAndOthers(r.votes, limit, others, *mandatory)
                        val count = aggregatedResult.size
                        val partialDeclaration = r.votes.values.any { it == null }
                        aggregatedResult.entries.asSequence()
                                .sortedByDescending { e: Map.Entry<KT, Int?> ->
                                    if (e.key === others) Int.MIN_VALUE
                                    else (e.value ?: -1)
                                }
                                .map { e ->
                                    val pct = e.value?.toDouble()?.div(total) ?: Double.NaN
                                    val valueLabel: String = when {
                                        count == 1 -> {
                                            "UNCONTESTED"
                                        }
                                        java.lang.Double.isNaN(pct) -> {
                                            "WAITING..."
                                        }
                                        partialDeclaration -> {
                                            THOUSANDS_FORMAT.format(e.value)
                                        }
                                        else -> {
                                            voteTemplate.toBarString(
                                                    e.value!!, pct, count > doubleLineBarLimit())
                                        }
                                    }
                                    val shape: Shape? = if (e.key == r.winner) keyTemplate.winnerShape(count > doubleLineBarLimit()) else if ((r.runoff ?: emptySet()).contains(e.key)) keyTemplate.runoffShape(count > doubleLineBarLimit()) else null
                                    BasicBar(
                                            keyTemplate.toMainBarHeader(e.key, count > doubleLineBarLimit()),
                                            keyTemplate.toParty(e.key).color,
                                            if (java.lang.Double.isNaN(pct)) 0 else pct,
                                            valueLabel,
                                            shape)
                                }
                                .toList()
                    },
                    Result.Property.VOTES,
                    Result.Property.WINNER,
                    Result.Property.RUNOFF)
            val notes = this.notes
            val pctReporting = this.pctReporting
            val builder = BarFrameBuilder.basic(bars)
                    .withHeader(header.getBinding())
                    .withSubhead(subhead.getBinding())
                    .withNotes(notes?.getBinding() ?: Binding.fixedBinding(null))
                    .withMax(
                            pctReporting?.getBinding { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: Binding.fixedBinding(2.0 / 3))
            applyMajorityLine(builder)
            return builder.build()
        }

        private fun applyMajorityLine(builder: BarFrameBuilder) {
            val showMajority = this.showMajority
            val pctReporting = this.pctReporting
            if (showMajority != null) {
                val lines = BindableList<Double>()
                showMajority
                        .getBinding()
                        .bind { show ->
                            lines.clear()
                            if (show) {
                                lines.add(
                                        if (pctReporting == null) 0.5 else 0.5 / pctReporting.value.coerceAtLeast(1e-6))
                            }
                        }
                pctReporting?.getBinding()?.bind { pct ->
                    if (!lines.isEmpty()) {
                        lines[0] = 0.5 / pct.coerceAtLeast(1e-6)
                    }
                }
                showMajority
                        .getBinding()
                        .bind {
                            if (!lines.isEmpty()) {
                                lines.setAll(lines)
                            }
                        }
                builder.withLines(lines) { majorityLabel!!.value }
            }
        }

        private class Change<KT> : Bindable<Change<KT>, Change.Property>() {
            enum class Property {
                CURR, PREV
            }

            private var _currVotes: Map<KT, Int?> = HashMap()
            private var _prevVotes: Map<Party, Int> = HashMap()

            var currVotes: Map<KT, Int?>
            get() = _currVotes
            set(currVotes) {
                _currVotes = currVotes
                onPropertyRefreshed(Property.CURR)
            }

            var prevVotes: Map<Party, Int>
            get() = _prevVotes
            set(prevVotes) {
                _prevVotes = prevVotes
                onPropertyRefreshed(Property.PREV)
            }
        }

        override fun createPreferenceFrame(): BarFrame? {
            return currPreferences?.let { currPreferences ->
                val result = Result<KT>()
                currPreferences.getBinding().bind { result.votes = it }
                winner?.getBinding()?.bind { result.winner = it }
                val bars = Binding.propertyBinding(
                        result,
                        { r: Result<KT> ->
                            val total = r.votes.values.filterNotNull().sum()
                            val partialDeclaration = r.votes.values.any { it == null }
                            val count = r.votes.size
                            r.votes.entries.asSequence()
                                    .sortedByDescending { e: Map.Entry<KT, Int?> -> if (e.key === others) Int.MIN_VALUE else (e.value ?: -1) }
                                    .map { e ->
                                        val pct = e.value?.toDouble()?.div(total) ?: Double.NaN
                                        val valueLabel: String = when {
                                            count == 1 -> {
                                                "ELECTED"
                                            }
                                            java.lang.Double.isNaN(pct) -> {
                                                "WAITING..."
                                            }
                                            partialDeclaration -> {
                                                THOUSANDS_FORMAT.format(e.value)
                                            }
                                            else -> {
                                                voteTemplate.toBarString(e.value!!, pct, true)
                                            }
                                        }
                                        val shape: Shape? = if (e.key == r.winner) keyTemplate.winnerShape(true) else if ((r.runoff ?: emptySet()).contains(e.key)) keyTemplate.runoffShape(true) else null
                                        BasicBar(
                                                keyTemplate.toMainBarHeader(e.key, true),
                                                keyTemplate.toParty(e.key).color,
                                                if (java.lang.Double.isNaN(pct)) 0 else pct,
                                                valueLabel,
                                                shape)
                                    }
                                    .toList()
                        },
                        Result.Property.VOTES,
                        Result.Property.WINNER,
                        Result.Property.RUNOFF)
                val preferencePctReporting = this.preferencePctReporting
                return BarFrameBuilder.basic(bars)
                        .withHeader(preferenceHeader!!.getBinding())
                        .withSubhead(preferenceSubhead?.getBinding() ?: Binding.fixedBinding(null))
                        .withLines(
                                preferencePctReporting?.getBinding { listOf(0.5 / it.coerceAtLeast(1e-6)) }
                                        ?: Binding.fixedBinding(listOf(0.5))
                        ) { "50%" }
                        .withMax(
                                preferencePctReporting?.getBinding { 2.0 / 3 / it.coerceAtLeast(1e-6) }
                                        ?: Binding.fixedBinding(2.0 / 3))
                        .build()
            }
        }

        override fun createClassificationFrame(): BarFrame? {
            return classificationHeader?.let { classificationHeader ->
                val bars: Binding<List<BasicBar>> = Aggregators.adjustKey(
                        current.getBinding().map { it.mapValues { e -> e.value ?: throw UnsupportedOperationException("Classifications not supported for partial declarations") } }) { classificationFunc!!(keyTemplate.toParty(it)) }
                        .map { m: Map<Party, Int> ->
                            val total = m.values.sum()
                            m.entries.asSequence()
                                    .sortedByDescending { it.value }
                                    .map { e: Map.Entry<Party, Int> ->
                                        BasicBar(
                                                e.key.name.toUpperCase(),
                                                e.key.color,
                                                1.0 * e.value / total,
                                                voteTemplate.toBarString(
                                                        e.value, 1.0 * e.value / total, true))
                                    }
                                    .toList()
                        }
                val pctReporting = this.pctReporting
                val builder = BarFrameBuilder.basic(bars)
                        .withHeader(classificationHeader.getBinding())
                        .withMax(
                                pctReporting?.getBinding { 2.0 / 3 / it.coerceAtLeast(1e-6) } ?: Binding.fixedBinding(2.0 / 3))
                applyMajorityLine(builder)
                return builder.build()
            }
        }

        override fun createDiffFrame(): BarFrame? {
            return prev?.let { prev ->
                val change = Change<KT>()
                current.getBinding().bind { change.currVotes = it }
                prev.getBinding().bind { change.prevVotes = it }
                val bars = Binding.propertyBinding(
                        change,
                        { r: Change<KT> ->
                            if (r.currVotes.values.any { it == null }) {
                                return@propertyBinding emptyList()
                            }
                            val prevWinner: Party? = r.prevVotes.entries
                                    .maxByOrNull { it.value }
                                    ?.key
                            if (prevWinner == null ||
                                    r.currVotes.keys
                                            .map { key: KT -> keyTemplate.toParty(key) }
                                            .none { it == prevWinner }) {
                                return@propertyBinding emptyList()
                            }
                            val currTotal = r.currVotes.values.filterNotNull().sum()
                            val prevTotal = r.prevVotes.values.sum()
                            if (currTotal == 0 || prevTotal == 0) {
                                return@propertyBinding emptyList()
                            }
                            val partyTotal = Aggregators.topAndOthers(
                                    currTotalByParty(r.currVotes),
                                    limit,
                                    Party.OTHERS,
                                    *mandatoryParties.toTypedArray()).toMutableMap()
                            val prevVotes: MutableMap<Party, Int> = HashMap(r.prevVotes)
                            r.prevVotes.entries.asSequence()
                                    .filter { e: Map.Entry<Party, Int> -> !partyTotal.containsKey(e.key) }
                                    .forEach { e: Map.Entry<Party, Int> ->
                                        partyTotal.putIfAbsent(Party.OTHERS, 0)
                                        prevVotes.merge(Party.OTHERS, e.value) { a: Int, b: Int -> Integer.sum(a, b) }
                                    }
                            partyTotal.entries.asSequence()
                                    .sortedByDescending { e: Map.Entry<Party, Int> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else e.value }
                                    .map { e: Map.Entry<Party, Int> ->
                                        val cpct = 1.0 * e.value / currTotal
                                        val ppct = 1.0 * prevVotes.getOrDefault(e.key, 0) / prevTotal
                                        BasicBar(
                                                e.key.abbreviation.toUpperCase(),
                                                e.key.color,
                                                cpct - ppct,
                                                DecimalFormat("+0.0%;-0.0%").format(cpct - ppct))
                                    }
                                    .toList()
                        },
                        Change.Property.CURR,
                        Change.Property.PREV)
                val pctReporting = this.pctReporting
                return BarFrameBuilder.basic(bars)
                        .withWingspan(
                                pctReporting?.getBinding { 0.1 / it.coerceAtLeast(1e-6) } ?: Binding.fixedBinding(0.1))
                        .withHeader(changeHeader!!.getBinding())
                        .withSubhead(changeSubhead?.getBinding() ?: Binding.fixedBinding(null))
                        .build()
            }
        }

        override fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { swingHeader ->
                val curr: Binding<Map<Party, Int>>
                val prev: Binding<Map<Party, Int>>
                val currPreferences = this.currPreferences
                val prevPreferences = this.prevPreferences
                if (currPreferences != null && prevPreferences != null) {
                    curr = currPreferences.getBinding { currTotalByParty(it) }
                    prev = prevPreferences
                            .getBinding()
                            .merge(
                                    currPreferences.getBinding { currTotalByParty(it) }
                            ) { p: Map<Party, Int>, c: Map<Party, Int> ->
                                if (c.keys != p.keys) {
                                    emptyMap()
                                } else {
                                    p
                                }
                            }
                } else {
                    curr = current.getBinding { currTotalByParty(it) }
                    prev = this.prev!!
                            .getBinding()
                            .merge(current.getBinding()) { p: Map<Party, Int>, c: Map<KT, Int?> ->
                                val prevWinner: Party? = p.entries
                                        .maxByOrNull { it.value }
                                        ?.key
                                if (prevWinner == null ||
                                        c.keys.asSequence()
                                                .map { key: KT -> keyTemplate.toParty(key) }
                                                .none { it == prevWinner }) {
                                    emptyMap()
                                } else {
                                    p
                                }
                            }
                }
                val classificationFunc = classificationFunc
                return SwingFrameBuilder.prevCurr(
                        if (classificationFunc == null) prev else Aggregators.adjustKey(prev, classificationFunc),
                        if (classificationFunc == null) curr else Aggregators.adjustKey(curr, classificationFunc),
                        swingComparator!!)
                        .withHeader(swingHeader.getBinding())
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
    }

    private class RangeVoteScreenBuilder<KT>(
        current: BindingReceiver<Map<KT, ClosedRange<Double>>>,
        header: BindingReceiver<String?>,
        subhead: BindingReceiver<String?>,
        keyTemplate: KeyTemplate<KT>,
        voteTemplate: VoteTemplate,
        others: KT
    ) : VoteScreenBuilder<KT, ClosedRange<Double>, Double, Int>(current, header, subhead, keyTemplate, voteTemplate, others) {
        override fun createFrame(): BarFrame {
            val bars = current.getBinding { r: Map<KT, ClosedRange<Double>> ->
                r.entries.asSequence()
                        .sortedByDescending { e: Map.Entry<KT, ClosedRange<Double>> -> if (e.key === others) Double.MIN_VALUE else (e.value.start + e.value.endInclusive) }
                        .map { e: Map.Entry<KT, ClosedRange<Double>> ->
                            val valueLabel = (DECIMAL_FORMAT.format(100 * e.value.start) +
                                    "-" +
                                    DecimalFormat("0.0").format(100 * e.value.endInclusive) +
                                    "%")
                            DualBar(
                                    keyTemplate.toMainBarHeader(e.key, false),
                                    keyTemplate.toParty(e.key).color,
                                    e.value.start,
                                    e.value.endInclusive,
                                    valueLabel)
                        }
                        .toList()
            }
            val notes = notes
            var builder = BarFrameBuilder.dual(bars)
                    .withHeader(header.getBinding())
                    .withSubhead(subhead.getBinding())
                    .withNotes(notes?.getBinding() ?: Binding.fixedBinding(null))
                    .withMax(Binding.fixedBinding(2.0 / 3))
            val showMajority = showMajority
            if (showMajority != null) {
                val lines = BindableList<Double>()
                showMajority
                        .getBinding()
                        .bind { show ->
                            lines.clear()
                            if (show) {
                                lines.add(0.5)
                            }
                        }
                showMajority
                        .getBinding()
                        .bind {
                            if (!lines.isEmpty()) {
                                lines.setAll(lines)
                            }
                        }
                builder = builder.withLines(lines) { majorityLabel!!.value }
            }
            return builder.build()
        }

        private class Change<KT> : Bindable<Change<KT>, Change.Property>() {
            enum class Property {
                CURR, PREV
            }

            private var _currVotes: Map<KT, ClosedRange<Double>> = HashMap()
            private var _prevVotes: Map<Party, Int> = HashMap()

            var currVotes: Map<KT, ClosedRange<Double>>
            get() = _currVotes
            set(currVotes) {
                _currVotes = currVotes
                onPropertyRefreshed(Property.CURR)
            }

            var prevVotes: Map<Party, Int>
            get() = _prevVotes
            set(prevVotes) {
                _prevVotes = prevVotes
                onPropertyRefreshed(Property.PREV)
            }
        }

        override fun createDiffFrame(): BarFrame? {
            return prev?.let { prev ->
                val change = Change<KT>()
                current.getBinding().bind { change.currVotes = it }
                prev.getBinding().bind { change.prevVotes = it }
                val bars = Binding.propertyBinding(
                        change,
                        { r: Change<KT> ->
                            val prevTotal = r.prevVotes.values.sum()
                            if (prevTotal == 0) {
                                return@propertyBinding emptyList()
                            }
                            val partyTotal = currTotalByParty(r.currVotes)
                            val prevVotes: MutableMap<Party, Int> = HashMap(r.prevVotes)
                            r.prevVotes.entries.asSequence()
                                    .filter { e: Map.Entry<Party, Int> -> !partyTotal.containsKey(e.key) }
                                    .forEach { e: Map.Entry<Party, Int> ->
                                        partyTotal.putIfAbsent(Party.OTHERS, (0.0).rangeTo(0.0))
                                        prevVotes.merge(Party.OTHERS, e.value) { a: Int, b: Int -> Integer.sum(a, b) }
                                    }
                            partyTotal.entries.asSequence()
                                    .sortedByDescending { e: Map.Entry<Party, ClosedRange<Double>> -> if (e.key === Party.OTHERS) Double.MIN_VALUE else (e.value.start + e.value.endInclusive) }
                                    .map { e: Map.Entry<Party, ClosedRange<Double>> ->
                                        val cpctMin = e.value.start
                                        val cpctMax = e.value.endInclusive
                                        val ppct = 1.0 * prevVotes.getOrDefault(e.key, 0) / prevTotal
                                        DualBar(
                                                e.key.abbreviation.toUpperCase(),
                                                e.key.color,
                                                cpctMin - ppct,
                                                cpctMax - ppct,
                                                "(" +
                                                        CHANGE_DECIMAL_FORMAT.format(100.0 * (cpctMin - ppct)) +
                                                        ")-(" +
                                                        CHANGE_DECIMAL_FORMAT.format(100.0 * (cpctMax - ppct)) +
                                                        ")%")
                                    }
                                    .toList()
                        },
                        Change.Property.CURR,
                        Change.Property.PREV)
                return BarFrameBuilder.dual(bars)
                        .withWingspan(Binding.fixedBinding(0.1))
                        .withHeader(changeHeader!!.getBinding())
                        .withSubhead(changeSubhead?.getBinding() ?: Binding.fixedBinding(null))
                        .build()
            }
        }

        override fun createClassificationFrame(): BarFrame? {
            return classificationHeader?.let { throw UnsupportedOperationException("Classifications not supported on ranges") }
        }

        override fun createPreferenceFrame(): BarFrame? {
            return currPreferences?.let { currPreferences ->
                val bars = currPreferences.getBinding { r: Map<KT, ClosedRange<Double>> ->
                    r.entries.asSequence()
                            .sortedByDescending { e: Map.Entry<KT, ClosedRange<Double>> -> if (e.key === others) Double.MIN_VALUE else (e.value.start + e.value.endInclusive) }
                            .map { e: Map.Entry<KT, ClosedRange<Double>> ->
                                val valueLabel = (DECIMAL_FORMAT.format(100 * e.value.start) +
                                        "-" +
                                        DecimalFormat("0.0").format(100 * e.value.endInclusive) +
                                        "%")
                                DualBar(
                                        keyTemplate.toMainBarHeader(e.key, false),
                                        keyTemplate.toParty(e.key).color,
                                        e.value.start,
                                        e.value.endInclusive,
                                        valueLabel)
                            }
                            .toList()
                }
                var builder = BarFrameBuilder.dual(bars)
                        .withHeader(preferenceHeader!!.getBinding())
                        .withSubhead(preferenceSubhead?.getBinding() ?: Binding.fixedBinding(null))
                        .withMax(Binding.fixedBinding(2.0 / 3))
                val lines = BindableList<Double>()
                lines.setAll(listOf(0.5))
                builder = builder.withLines(lines) { "50%" }
                return builder.build()
            }
        }

        override fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { swingHeader ->
                val curr = current
                        .getBinding<Map<Party, ClosedRange<Double>>> { currTotalByParty(it) }
                        .map { m: Map<Party, ClosedRange<Double>> ->
                            val ret: MutableMap<Party, Int> = LinkedHashMap()
                            m.forEach { (p: Party, r: ClosedRange<Double>) -> ret[p] = (1000000 * (r.start + r.endInclusive) / 2).roundToInt() }
                            ret
                        }
                return SwingFrameBuilder.prevCurr(prev!!.getBinding(), curr, swingComparator!!)
                        .withHeader(swingHeader.getBinding())
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

    class CurrDiff<CT>(val curr: CT, val diff: CT)
    companion object {
        private val PCT_FORMAT = DecimalFormat("0.0%")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")
        private fun <T> partyMapToResultMap(m: Map<T, Party?>): Map<T, PartyResult?> {
            val ret: MutableMap<T, PartyResult?> = LinkedHashMap()
            m.forEach { (k: T, v: Party?) -> ret[k] = if (v == null) null else PartyResult.elected(v) }
            return ret
        }

        private fun createHeaderLabel(textBinding: Binding<String>): JLabel {
            val headerLabel = JLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textBinding.bind { headerLabel.text = it }
            return headerLabel
        }

        @JvmStatic fun partySeats(
            seats: Binding<Map<Party, Int>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): SeatScreenBuilder<Party, Int, Int> {
            return BasicSeatScreenBuilder(
                    BindingReceiver(seats),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    PartyTemplate())
        }

        @JvmStatic fun candidateSeats(
            seats: Binding<Map<Candidate, Int>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): SeatScreenBuilder<Candidate, Int, Int> {
            return BasicSeatScreenBuilder(
                    BindingReceiver(seats),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    CandidateTemplate())
        }

        @JvmStatic fun partyDualSeats(
            seats: Binding<Map<Party, Pair<Int, Int>>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): SeatScreenBuilder<Party, Pair<Int, Int>, Pair<Int, Int>> {
            return DualSeatScreenBuilder(
                    BindingReceiver(seats),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    PartyTemplate())
        }

        @JvmStatic fun candidateDualSeats(
            seats: Binding<Map<Candidate, Pair<Int, Int>>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): SeatScreenBuilder<Candidate, Pair<Int, Int>, Pair<Int, Int>> {
            return DualSeatScreenBuilder(
                    BindingReceiver(seats),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    CandidateTemplate())
        }

        @JvmStatic fun partyRangeSeats(
            seats: Binding<Map<Party, IntRange>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): SeatScreenBuilder<Party, IntRange, Int> {
            return RangeSeatScreenBuilder(
                    BindingReceiver(seats),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    PartyTemplate())
        }

        @JvmStatic fun candidateRangeSeats(
            seats: Binding<Map<Candidate, IntRange>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): SeatScreenBuilder<Candidate, IntRange, Int> {
            return RangeSeatScreenBuilder(
                    BindingReceiver(seats),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    CandidateTemplate())
        }

        @JvmStatic fun partyVotes(
            votes: Binding<Map<Party, Int?>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): VoteScreenBuilder<Party, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                    BindingReceiver(votes),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    PartyTemplate(),
                    PctOnlyTemplate(),
                    Party.OTHERS)
        }

        @JvmStatic fun candidateVotes(
            votes: Binding<Map<Candidate, Int?>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): VoteScreenBuilder<Candidate, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                    BindingReceiver(votes),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    CandidateTemplate(),
                    VotePctTemplate(),
                    Candidate.OTHERS)
        }

        @JvmStatic fun candidateVotesPctOnly(
            votes: Binding<Map<Candidate, Int?>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): VoteScreenBuilder<Candidate, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                    BindingReceiver(votes),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    CandidateTemplate(),
                    VotePctOnlyTemplate(),
                    Candidate.OTHERS)
        }

        @JvmStatic fun candidateVotes(
            votes: Binding<Map<Candidate, Int?>>,
            header: Binding<String?>,
            subhead: Binding<String?>,
            incumbentMarker: String
        ): VoteScreenBuilder<Candidate, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                    BindingReceiver(votes),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    CandidateTemplate(incumbentMarker),
                    VotePctTemplate(),
                    Candidate.OTHERS)
        }

        @JvmStatic fun candidateVotesPctOnly(
            votes: Binding<Map<Candidate, Int?>>,
            header: Binding<String?>,
            subhead: Binding<String?>,
            incumbentMarker: String
        ): VoteScreenBuilder<Candidate, Int?, Double?, Int> {
            return BasicVoteScreenBuilder(
                    BindingReceiver(votes),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    CandidateTemplate(incumbentMarker),
                    VotePctOnlyTemplate(),
                    Candidate.OTHERS)
        }

        @JvmStatic fun partyRangeVotes(
            votes: Binding<Map<Party, ClosedRange<Double>>>,
            header: Binding<String?>,
            subhead: Binding<String?>
        ): VoteScreenBuilder<Party, ClosedRange<Double>, Double, Int> {
            return RangeVoteScreenBuilder(
                    BindingReceiver(votes),
                    BindingReceiver(header),
                    BindingReceiver(subhead),
                    PartyTemplate(),
                    PctOnlyTemplate(),
                    Party.OTHERS)
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
