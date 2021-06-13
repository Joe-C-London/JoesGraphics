package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding
import com.joecollins.bindings.BindingReceiver
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
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

class SingleTransferrableResultScreen private constructor(
    label: JLabel,
    private val candidateFrame: JPanel,
    private val partyFrame: JPanel?,
    private val prevFrame: JPanel?,
    private val mapFrame: JPanel?
) : JPanel() {

    init {
        layout = BorderLayout()
        background = Color.WHITE
        add(label, BorderLayout.NORTH)
        val panel = JPanel()
        panel.layout = ScreenLayout()
        panel.background = Color.WHITE
        add(panel, BorderLayout.CENTER)
        panel.add(candidateFrame)
        if (partyFrame != null) panel.add(partyFrame)
        if (prevFrame != null) panel.add(prevFrame)
        if (mapFrame != null) panel.add(mapFrame)
    }

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
            val seatFrameIsAlone = partyFrame == null // && swingFrame == null && mapFrame == null
            candidateFrame.setSize(
                width * (if (seatFrameIsAlone) 5 else 3) / 5 - 10,
                height - 10)
            partyFrame?.setLocation(width * 3 / 5 + 5, 5)
            partyFrame?.setSize(width * 2 / 5 - 10, height * 2 / 3 - 10)
            prevFrame?.setLocation(width * 3 / 5 + 5, height * 2 / 3 + 5)
            prevFrame?.setSize(width * (if (mapFrame == null) 2 else 1) / 5 - 10, height / 3 - 10)
            mapFrame?.setLocation(width * (if (prevFrame == null) 3 else 4) / 5 + 5, height * 2 / 3 + 5)
            mapFrame?.setSize(width * (if (prevFrame == null) 2 else 1) / 5 - 10, height / 3 - 10)
        }
    }

    companion object {

        fun withCandidates(
            candidateVotes: Binding<Map<Candidate, Number?>>,
            quota: Binding<Number?>,
            elected: Binding<List<Pair<Candidate, Int>>>,
            excluded: Binding<List<Candidate>>,
            header: Binding<String?>,
            subhead: Binding<String?>,
            incumbentMarker: String = ""
        ): Builder {
            return Builder(
                BindingReceiver(candidateVotes),
                BindingReceiver(quota),
                BindingReceiver(elected),
                BindingReceiver(excluded),
                BindingReceiver(header),
                BindingReceiver(subhead),
                incumbentMarker)
        }
    }

    class Builder(
        private val candidateVotes: BindingReceiver<Map<Candidate, Number?>>,
        private val quota: BindingReceiver<Number?>,
        private val elected: BindingReceiver<List<Pair<Candidate, Int>>>,
        private val excluded: BindingReceiver<List<Candidate>>,
        private val candidateHeader: BindingReceiver<String?>,
        private val candidateSubhead: BindingReceiver<String?>,
        private val incumbentMarker: String
    ) {

        private var totalSeats: BindingReceiver<Int>? = null
        private var partyHeader: BindingReceiver<String?>? = null

        private var prevSeats: BindingReceiver<Map<Party, Int>>? = null
        private var prevHeader: BindingReceiver<String?>? = null

        private var mapBuilder: MapBuilder<*>? = null

        fun withPartyTotals(
            totalSeats: Binding<Int>,
            partyHeader: Binding<String?>
        ): Builder {
            this.totalSeats = BindingReceiver(totalSeats)
            this.partyHeader = BindingReceiver(partyHeader)
            return this
        }

        fun withPrevSeats(
            prevSeats: Binding<Map<Party, Int>>,
            prevHeader: Binding<String?>
        ): Builder {
            this.prevSeats = BindingReceiver(prevSeats)
            this.prevHeader = BindingReceiver(prevHeader)
            return this
        }

        fun <T> withPartyMap(
            shapes: Binding<Map<T, Shape>>,
            selectedShape: Binding<T>,
            leadingParty: Binding<Party?>,
            focus: Binding<List<T>?>,
            header: Binding<String?>
        ): Builder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty.map { party: Party? -> PartyResult.elected(party) }, focus, header)
            return this
        }

        fun build(title: Binding<String>): SingleTransferrableResultScreen {
            return SingleTransferrableResultScreen(
                createHeaderLabel(title),
                createCandidatesPanel(),
                createPartiesPanel(),
                createPrevPanel(),
                mapBuilder?.createMapFrame()
            )
        }

        private fun createHeaderLabel(textBinding: Binding<String>): JLabel {
            val headerLabel = JLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textBinding.bind { headerLabel.text = it }
            return headerLabel
        }

        private fun createCandidatesPanel(): JPanel {
            val votesQuota = candidateVotes.getBinding().merge(quota.getBinding()) { v, q -> v to q }
            val inOut = elected.getBinding().merge(excluded.getBinding()) { el, ex -> el to ex }
            return BarFrame(
                barsBinding = votesQuota.merge(inOut) { (votes, quota), (elected, excluded) ->
                    val electedCandidates = elected.map { it.first }
                    val alreadyElectedSequence = elected.asSequence()
                        .filter { !votes.containsKey(it.first) }
                        .map {
                            BarFrame.Bar(
                                leftText = it.first.name.uppercase() + (if (it.first.incumbent) " $incumbentMarker" else "") + " (${it.first.party.abbreviation.uppercase()})",
                                rightText = "ELECTED IN ${it.second}",
                                leftIcon = ImageGenerator.createTickShape(),
                                series = listOf(it.first.party.color to 0)
                            )
                        }
                    val thisRoundSequence = votes.entries.asSequence()
                        .sortedByDescending { it.value?.toDouble() ?: -1.0 }
                        .map {
                            BarFrame.Bar(
                                leftText = it.key.name.uppercase() + (if (it.key.incumbent) " $incumbentMarker" else "") + " (${it.key.party.abbreviation.uppercase()})",
                                rightText = if (it.value == null) "WAITING..." else (formatString(it.value!!) + (if (quota == null) "" else (" (" + formatString(it.value!!.toDouble() / quota!!.toDouble()) + ")"))),
                                series = listOf(it.key.party.color to (it.value ?: 0)),
                                leftIcon = when {
                                    electedCandidates.contains(it.key) -> ImageGenerator.createTickShape()
                                    excluded.contains(it.key) -> ImageGenerator.createCrossShape()
                                    else -> null
                                }
                            )
                        }
                    sequenceOf(alreadyElectedSequence, thisRoundSequence)
                        .flatten()
                        .toList()
                },
                headerBinding = candidateHeader.getBinding(),
                subheadTextBinding = candidateSubhead.getBinding(),
                maxBinding = quota.getBinding { (it?.toDouble() ?: 1.0) * 2 },
                linesBinding = quota.getBinding { if (it == null) emptyList() else listOf(BarFrame.Line(it, "QUOTA: " + formatString(it))) }
            )
        }

        private fun createPartiesPanel(): JPanel? {
            if (partyHeader == null) {
                return null
            }
            val quotaAndElected = quota.getBinding().merge(elected.getBinding { e -> e.map { it.first } }) { q, e -> q to e }
            return BarFrame(
                headerBinding = partyHeader!!.getBinding(),
                maxBinding = totalSeats!!.getBinding(),
                linesBinding = totalSeats!!.getBinding { (1 until it).map { i -> BarFrame.Line(i, "$i QUOTA${if (i == 1) "" else "S"}") } },
                barsBinding = candidateVotes.getBinding().merge(quotaAndElected) { votes, (quota, elected) ->
                    val electedEarlier = elected.filter { !votes.containsKey(it) }
                        .groupingBy { it.party }
                        .eachCount()
                    val quotasThisRound = votes.entries
                        .filter { it.value != null }
                        .groupBy({ it.key.party }, { it.value })
                        .mapValues { e -> e.value.filterNotNull().sumOf { it.toDouble() } / (quota ?: 1.0).toDouble() }
                    sequenceOf(electedEarlier.keys, quotasThisRound.keys)
                        .flatten()
                        .distinct()
                        .associateWith { (electedEarlier[it] ?: 0) + (quotasThisRound[it] ?: 0.0) }
                        .entries
                        .sortedByDescending { it.value }
                        .map { BarFrame.Bar(
                            leftText = it.key.name.uppercase(),
                            rightText = formatString(it.value),
                            series = listOf(it.key.color to it.value)
                        ) }
                }
            )
        }

        private fun createPrevPanel(): JPanel? {
            if (prevHeader == null) {
                return null
            }
            return BarFrame(
                barsBinding = prevSeats!!.getBinding { prev ->
                    prev.entries
                        .sortedByDescending { it.value }
                        .map { BarFrame.Bar(
                            leftText = it.key.abbreviation.uppercase(),
                            rightText = "${it.value}",
                            series = listOf(it.key.color to it.value)
                        ) }
                },
                headerBinding = prevHeader!!.getBinding(),
                maxBinding = prevSeats!!.getBinding { prev -> prev.values.sum() / 2 }
            )
        }

        private fun formatString(value: Number): String {
            return if (value is Int || value is Long) DecimalFormat("#,##0").format(value)
            else DecimalFormat("#,##0.00").format(value)
        }
    }
}
