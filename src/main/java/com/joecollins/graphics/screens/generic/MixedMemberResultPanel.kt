package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.BarFrameBuilder.BasicBar
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.MapFrame
import com.joecollins.graphics.utils.StandardFont
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
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class MixedMemberResultPanel private constructor(
    label: JLabel,
    private val candidateFrame: BarFrame,
    private val candidateChangeFrame: BarFrame?,
    private val partyFrame: BarFrame,
    private val partyChangeFrame: BarFrame?,
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
            candidateFrame.setLocation(5, 5)
            candidateFrame.setSize(
                width * 3 / 5 - 10, height / (if (candidateChangeFrame == null) 1 else 2) - 10
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
        private var candidateVotes: BindingReceiver<Map<Candidate, Int?>> = BindingReceiver(Binding.fixedBinding(emptyMap()))
        private var candidatePrev: BindingReceiver<Map<Party, Int>>? = null
        private var candidatePctReporting: BindingReceiver<Double>? = null
        private var winner: BindingReceiver<Candidate?> = BindingReceiver(Binding.fixedBinding(null))
        private var partyVotes: BindingReceiver<Map<Party, Int?>> = BindingReceiver(Binding.fixedBinding(emptyMap()))
        private var partyPrev: BindingReceiver<Map<Party, Int>>? = null
        private var partyPctReporting: BindingReceiver<Double>? = null
        private var incumbentMarker = ""
        private var candidateVoteHeader: BindingReceiver<String?> = BindingReceiver(Binding.fixedBinding(null))
        private var candidateVoteSubheader: BindingReceiver<String?> = BindingReceiver(Binding.fixedBinding(null))
        private var candidateChangeHeader: BindingReceiver<String?> = BindingReceiver(Binding.fixedBinding(null))
        private var partyVoteHeader: BindingReceiver<String?> = BindingReceiver(Binding.fixedBinding(null))
        private var partyChangeHeader: BindingReceiver<String?> = BindingReceiver(Binding.fixedBinding(null))
        private var mapBuilder: MapBuilder<*>? = null

        @JvmOverloads
        fun withCandidateVotes(
            votes: Binding<Map<Candidate, Int?>>,
            header: Binding<String>,
            subheader: Binding<String?> = Binding.fixedBinding(null)
        ): Builder {
            candidateVotes = BindingReceiver(votes)
            candidateVoteHeader = BindingReceiver(header)
            candidateVoteSubheader = BindingReceiver(subheader)
            return this
        }

        fun withIncumbentMarker(incumbentMarker: String): Builder {
            this.incumbentMarker = " $incumbentMarker"
            return this
        }

        fun withWinner(winner: Binding<Candidate?>): Builder {
            this.winner = BindingReceiver(winner)
            return this
        }

        fun withPrevCandidateVotes(
            votes: Binding<Map<Party, Int>>,
            header: Binding<String>
        ): Builder {
            candidatePrev = BindingReceiver(votes)
            candidateChangeHeader = BindingReceiver(header)
            return this
        }

        fun withPartyVotes(
            votes: Binding<Map<Party, Int?>>,
            header: Binding<String>
        ): Builder {
            partyVotes = BindingReceiver(votes)
            partyVoteHeader = BindingReceiver(header)
            return this
        }

        fun withPrevPartyVotes(
            votes: Binding<Map<Party, Int>>,
            header: Binding<String>
        ): Builder {
            partyPrev = BindingReceiver(votes)
            partyChangeHeader = BindingReceiver(header)
            return this
        }

        fun withCandidatePctReporting(pctReporting: Binding<Double>): Builder {
            candidatePctReporting = BindingReceiver(pctReporting)
            return this
        }

        fun withPartyPctReporting(pctReporting: Binding<Double>): Builder {
            partyPctReporting = BindingReceiver(pctReporting)
            return this
        }

        fun <T> withResultMap(
            shapes: Binding<Map<T, Shape>>,
            selectedShape: Binding<T>,
            leadingParty: Binding<PartyResult?>,
            focus: Binding<List<T>?>,
            header: Binding<String>
        ): Builder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, header)
            return this
        }

        fun <T> withResultMap(
            shapes: Binding<Map<T, Shape>>,
            selectedShape: Binding<T>,
            leadingParty: Binding<PartyResult?>,
            focus: Binding<List<T>?>,
            additionalHighlight: Binding<List<T>?>,
            header: Binding<String>
        ): Builder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty, focus, additionalHighlight, header)
            return this
        }

        fun build(header: Binding<String>): MixedMemberResultPanel {
            return MixedMemberResultPanel(
                createHeaderLabel(header),
                createCandidateVotes(),
                createCandidateChange(),
                createPartyVotes(),
                createPartyChange(),
                createMapFrame()
            )
        }

        private class Result : Bindable<Result, Result.Property>() {
            enum class Property {
                VOTES, WINNER
            }

            private var _votes: Map<Candidate, Int?> = HashMap()
            private var _winner: Candidate? = null

            var votes: Map<Candidate, Int?>
                get() = _votes
                set(votes) {
                    _votes = votes
                    onPropertyRefreshed(Property.VOTES)
                }

            var winner: Candidate?
                get() = _winner
                set(winner) {
                    _winner = winner
                    onPropertyRefreshed(Property.WINNER)
                }
        }

        private fun createCandidateVotes(): BarFrame {
            val winnerBinding: Binding<Candidate?> = winner.getBinding()
            val showBothLines = candidatePrev == null
            val shape = if (showBothLines) ImageGenerator.createHalfTickShape() else ImageGenerator.createTickShape()
            val result = Result()
            candidateVotes.getBinding().bind { result.votes = it }
            winnerBinding.bind { result.winner = it }
            val bars = Binding.propertyBinding(
                result,
                { r: Result ->
                    val total = r.votes.values.filterNotNull().sum()
                    val partialDeclaration = r.votes.values.any { it == null }
                    r.votes.entries
                        .sortedByDescending { e: Map.Entry<Candidate, Int?> ->
                            val value = e.value
                            if (e.key === Candidate.OTHERS || value == null) Int.MIN_VALUE else value
                        }
                        .map { e: Map.Entry<Candidate, Int?> ->
                            val candidate = e.key
                            val votes = e.value ?: 0
                            val pct = e.value?.toDouble()?.div(total) ?: Double.NaN
                            val leftLabel: String
                            var rightLabel: String
                            if (candidate.name.isBlank()) {
                                leftLabel = candidate.party.name.uppercase()
                                rightLabel = ("${THOUSANDS_FORMAT.format(votes.toLong())} (${PCT_FORMAT.format(pct)})")
                            } else if (showBothLines) {
                                leftLabel = ("${candidate.name.uppercase()}${if (candidate.isIncumbent()) incumbentMarker else ""}\n${candidate.party.name.uppercase()}")
                                rightLabel = "${THOUSANDS_FORMAT.format(votes.toLong())}\n${PCT_FORMAT.format(pct)}"
                            } else {
                                leftLabel = ("${candidate.name.uppercase()}${if (candidate.isIncumbent()) incumbentMarker else ""} (${candidate.party.abbreviation.uppercase()})")
                                rightLabel = ("${THOUSANDS_FORMAT.format(votes.toLong())} (${PCT_FORMAT.format(pct)})")
                            }
                            if (partialDeclaration) {
                                rightLabel = THOUSANDS_FORMAT.format(votes.toLong())
                            }
                            BasicBar(
                                if (candidate === Candidate.OTHERS) "OTHERS" else leftLabel,
                                candidate.party.color,
                                if (java.lang.Double.isNaN(pct)) 0 else pct,
                                (if (java.lang.Double.isNaN(pct)) "WAITING..." else rightLabel),
                                if (candidate === r.winner) (if (candidate.name.isBlank()) ImageGenerator.createTickShape() else shape) else null
                            )
                        }
                        .toList()
                },
                Result.Property.VOTES,
                Result.Property.WINNER
            )
            return BarFrameBuilder.basic(bars)
                .withHeader(candidateVoteHeader.getBinding())
                .withSubhead(candidateVoteSubheader.getBinding())
                .withMax(candidatePctReporting?.getBinding { x: Double -> 2.0 / 3 / x.coerceAtLeast(1e-6) } ?: Binding.fixedBinding(2.0 / 3))
                .build()
        }

        private class Change<C> : Bindable<Change<C>, Change.Property>() {
            enum class Property {
                CURR, PREV
            }

            private var _curr: Map<C, Int?> = HashMap()
            private var _prev: Map<Party, Int> = HashMap()

            var curr: Map<C, Int?>
                get() = _curr
                set(curr) {
                    _curr = curr
                    onPropertyRefreshed(Property.CURR)
                }

            var prev: Map<Party, Int>
                get() = _prev
                set(prev) {
                    _prev = prev
                    onPropertyRefreshed(Property.PREV)
                }
        }

        private fun createCandidateChange(): BarFrame? {
            if (candidatePrev == null) {
                return null
            }
            val change = Change<Candidate>()
            candidateVotes.getBinding().bind { change.curr = it }
            candidatePrev!!.getBinding().bind { change.prev = it }
            val bars = Binding.propertyBinding(
                change,
                { r: Change<Candidate> ->
                    val currTotal = r.curr.values.filterNotNull().sum()
                    if (currTotal == 0) {
                        return@propertyBinding listOf()
                    }
                    if (r.curr.values.any { it == null }) {
                        return@propertyBinding listOf()
                    }
                    val curr: Map<Candidate, Int> = r.curr.mapValues { it.value!! }
                    val prev: Map<Party, Int> = r.prev
                    val prevTotal = prev.values.sum()
                    val currParties = curr.keys.map(Candidate::party).toSet()
                    val matchingBars = curr.entries.asSequence()
                        .filter { it.key.party !== Party.OTHERS }
                        .sortedByDescending { it.value }
                        .map { e: Map.Entry<Candidate, Int> ->
                            val pct = (
                                1.0 * e.value / currTotal -
                                    1.0 *
                                    (r.prev[e.key.party] ?: 0) /
                                    prevTotal
                                )
                            BasicBar(
                                e.key.party.abbreviation.uppercase(),
                                e.key.party.color,
                                pct,
                                PCT_DIFF_FORMAT.format(pct)
                            )
                        }
                    val othersPct = (
                        curr.entries.asSequence()
                            .filter { it.key.party === Party.OTHERS }
                            .map { 1.0 * it.value / currTotal }
                            .sum() +
                            r.prev.entries
                                .filter { it.key === Party.OTHERS || !currParties.contains(it.key) }
                                .map { -1.0 * it.value / prevTotal }
                                .sum()
                        )
                    val nonMatchingBars = if (othersPct == 0.0) emptySequence() else sequenceOf(
                        BasicBar(
                            Party.OTHERS.abbreviation.uppercase(),
                            Party.OTHERS.color,
                            othersPct,
                            PCT_DIFF_FORMAT.format(othersPct)
                        )
                    )
                    sequenceOf(matchingBars, nonMatchingBars).flatten().toList()
                },
                Change.Property.CURR,
                Change.Property.PREV
            )
            return BarFrameBuilder.basic(bars)
                .withHeader(candidateChangeHeader.getBinding())
                .withWingspan(candidatePctReporting?.getBinding { x: Double -> 0.05 / x.coerceAtLeast(1e-6) } ?: Binding.fixedBinding(0.05))
                .build()
        }

        private fun createPartyVotes(): BarFrame {
            return BarFrameBuilder.basic(
                partyVotes.getBinding { v: Map<Party, Int?> ->
                    val total = v.values.filterNotNull().sum()
                    val partialDeclaration = v.values.any { it == null }
                    v.entries
                        .sortedByDescending { e: Map.Entry<Party, Int?> -> if (e.key === Party.OTHERS) Int.MIN_VALUE else (e.value ?: -1) }
                        .map { e: Map.Entry<Party, Int?> ->
                            val value = e.value
                            val pct = if (value == null) Double.NaN else 1.0 * value / total
                            BasicBar(
                                e.key.name.uppercase(),
                                e.key.color,
                                if (java.lang.Double.isNaN(pct)) 0 else pct,
                                if (java.lang.Double.isNaN(pct)) "WAITING..." else THOUSANDS_FORMAT.format(e.value) +
                                    if (partialDeclaration) "" else " (" + PCT_FORMAT.format(pct) + ")"
                            )
                        }
                        .toList()
                }
            )
                .withHeader(partyVoteHeader.getBinding())
                .withMax(partyPctReporting?.getBinding { x: Double -> 2.0 / 3 / x.coerceAtLeast(1e-6) } ?: Binding.fixedBinding(2.0 / 3))
                .build()
        }

        private fun createPartyChange(): BarFrame? {
            if (partyPrev == null) {
                return null
            }
            val change = Change<Party>()
            partyVotes.getBinding().bind { change.curr = it }
            partyPrev!!.getBinding().bind { change.prev = it }
            val bars = Binding.propertyBinding(
                change,
                { r: Change<Party> ->
                    val currTotal = r.curr.values.filterNotNull().sum()
                    if (currTotal == 0) {
                        return@propertyBinding listOf()
                    }
                    if (r.curr.values.any { it == null }) {
                        return@propertyBinding listOf()
                    }
                    val curr: Map<Party, Int> = r.curr.mapValues { it.value!! }
                    val prev: Map<Party, Int> = r.prev
                    val prevTotal = prev.values.sum()
                    val presentBars = curr.entries.asSequence()
                        .filter { it.key !== Party.OTHERS }
                        .sortedByDescending { it.value }
                        .map { e: Map.Entry<Party, Int> ->
                            val pct = (
                                1.0 * e.value / currTotal -
                                    1.0 * (r.prev[e.key] ?: 0) / prevTotal
                                )
                            BasicBar(
                                e.key.abbreviation.uppercase(),
                                e.key.color,
                                pct,
                                PCT_DIFF_FORMAT.format(pct)
                            )
                        }
                    val otherTotal = (
                        curr.entries
                            .filter { it.key === Party.OTHERS }
                            .map { 1.0 * it.value / currTotal }
                            .sum() +
                            r.prev.entries
                                .filter { it.key === Party.OTHERS || !r.curr.containsKey(it.key) }
                                .map { -1.0 * it.value / prevTotal }
                                .sum()
                        )
                    val absentBars = if (otherTotal == 0.0) emptySequence() else sequenceOf(
                        BasicBar(
                            Party.OTHERS.abbreviation.uppercase(),
                            Party.OTHERS.color,
                            otherTotal,
                            PCT_DIFF_FORMAT.format(otherTotal)
                        )
                    )
                    sequenceOf(presentBars, absentBars).flatten().toList()
                },
                Change.Property.CURR,
                Change.Property.PREV
            )
            return BarFrameBuilder.basic(bars)
                .withHeader(partyChangeHeader.getBinding())
                .withWingspan(partyPctReporting?.getBinding { x: Double -> 0.05 / x.coerceAtLeast(1e-6) } ?: Binding.fixedBinding(0.05))
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

        @JvmStatic fun builder(): Builder {
            return Builder()
        }

        private fun createHeaderLabel(textBinding: Binding<String>): JLabel {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textBinding.bind { headerLabel.text = it }
            return headerLabel
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
        panel.add(candidateFrame)
        candidateChangeFrame?.also { panel.add(it) }
        panel.add(partyFrame)
        partyChangeFrame?.also { panel.add(it) }
        mapFrame?.also { panel.add(it) }
    }
}
