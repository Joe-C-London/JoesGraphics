package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
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
import java.util.concurrent.Flow
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
                height - 10
            )
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
            candidateVotes: Flow.Publisher<out Map<Candidate, Number?>>,
            quota: Flow.Publisher<out Number?>,
            elected: Flow.Publisher<out List<Pair<Candidate, Int>>>,
            excluded: Flow.Publisher<out List<Candidate>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            incumbentMarker: String = ""
        ): Builder {
            return Builder(
                candidateVotes,
                quota,
                elected,
                excluded,
                header,
                subhead,
                incumbentMarker
            )
        }
    }

    class Builder(
        private val candidateVotes: Flow.Publisher<out Map<Candidate, Number?>>,
        private val quota: Flow.Publisher<out Number?>,
        private val elected: Flow.Publisher<out List<Pair<Candidate, Int>>>,
        private val excluded: Flow.Publisher<out List<Candidate>>,
        private val candidateHeader: Flow.Publisher<out String?>,
        private val candidateSubhead: Flow.Publisher<out String?>,
        private val incumbentMarker: String
    ) {

        private var totalSeats: Flow.Publisher<out Int>? = null
        private var partyHeader: Flow.Publisher<out String?>? = null

        private var prevSeats: Flow.Publisher<out Map<Party, Int>>? = null
        private var prevHeader: Flow.Publisher<out String?>? = null

        private var mapBuilder: MapBuilder<*>? = null

        fun withPartyTotals(
            totalSeats: Flow.Publisher<out Int>,
            partyHeader: Flow.Publisher<out String?>
        ): Builder {
            this.totalSeats = totalSeats
            this.partyHeader = partyHeader
            return this
        }

        fun withPrevSeats(
            prevSeats: Flow.Publisher<out Map<Party, Int>>,
            prevHeader: Flow.Publisher<out String?>
        ): Builder {
            this.prevSeats = prevSeats
            this.prevHeader = prevHeader
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out Party?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): Builder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty.map { party: Party? -> PartyResult.elected(party) }, focus, header)
            return this
        }

        fun build(title: Flow.Publisher<out String>): SingleTransferrableResultScreen {
            return SingleTransferrableResultScreen(
                createHeaderLabel(title),
                createCandidatesPanel(),
                createPartiesPanel(),
                createPrevPanel(),
                mapBuilder?.createMapFrame()
            )
        }

        private fun createHeaderLabel(textPublisher: Flow.Publisher<out String>): JLabel {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textPublisher.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            return headerLabel
        }

        private fun createCandidatesPanel(): JPanel {
            val votesQuota = candidateVotes.merge(quota) { v, q -> v to q }
            val inOut = elected.merge(excluded) { el, ex -> el to ex }
            return BarFrameBuilder.basic(
                votesQuota.merge(inOut) { (votes, quota), (elected, excluded) ->
                    val electedCandidates = elected.map { it.first }
                    val alreadyElectedSequence = elected.asSequence()
                        .filter { !votes.containsKey(it.first) }
                        .map {
                            BarFrameBuilder.BasicBar(
                                label = it.first.name.uppercase() + (if (it.first.incumbent) " $incumbentMarker" else "") + " (${it.first.party.abbreviation.uppercase()})",
                                valueLabel = "ELECTED IN ${it.second}",
                                shape = ImageGenerator.createTickShape(),
                                value = 0,
                                color = it.first.party.color
                            )
                        }
                    val thisRoundSequence = votes.entries.asSequence()
                        .sortedByDescending { it.value?.toDouble() ?: -1.0 }
                        .map {
                            BarFrameBuilder.BasicBar(
                                label = it.key.name.uppercase() + (if (it.key.incumbent) " $incumbentMarker" else "") + " (${it.key.party.abbreviation.uppercase()})",
                                valueLabel = if (it.value == null) "WAITING..." else (formatString(it.value!!) + (if (quota == null) "" else (" (" + formatString(it.value!!.toDouble() / quota.toDouble()) + ")"))),
                                color = it.key.party.color,
                                value = (it.value ?: 0),
                                shape = when {
                                    electedCandidates.contains(it.key) -> ImageGenerator.createTickShape()
                                    excluded.contains(it.key) -> ImageGenerator.createCrossShape()
                                    else -> null
                                }
                            )
                        }
                    sequenceOf(alreadyElectedSequence, thisRoundSequence)
                        .flatten()
                        .toList()
                }
            )
                .withHeader(candidateHeader)
                .withSubhead(candidateSubhead)
                .withMax(quota.map { (it?.toDouble() ?: 1.0) * 2 })
                .withLines(quota.map { if (it == null) emptyList() else listOf(it) }) { "QUOTA: " + formatString(it) }
                .build()
        }

        private fun createPartiesPanel(): JPanel? {
            if (partyHeader == null) {
                return null
            }
            val quotaAndElected = quota.merge(elected.map { e -> e.map { it.first } }) { q, e -> q to e }
            return BarFrameBuilder.basic(
                candidateVotes.merge(quotaAndElected) { votes, (quota, elected) ->
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
                        .map {
                            BarFrameBuilder.BasicBar(
                                label = it.key.name.uppercase(),
                                color = it.key.color,
                                value = it.value,
                                valueLabel = formatString(it.value)
                            )
                        }
                }
            )
                .withHeader(partyHeader!!)
                .withMax(totalSeats!!)
                .withLines(totalSeats!!.map { (1 until it).toList() }) { i -> "$i QUOTA${if (i == 1) "" else "S"}" }
                .build()
        }

        private fun createPrevPanel(): JPanel? {
            if (prevHeader == null) {
                return null
            }
            return BarFrameBuilder.basic(
                prevSeats!!.map { prev ->
                    prev.entries
                        .sortedByDescending { it.value }
                        .map {
                            BarFrameBuilder.BasicBar(
                                label = it.key.abbreviation.uppercase(),
                                color = it.key.color,
                                value = it.value
                            )
                        }
                }
            )
                .withMax(prevSeats!!.map { prev -> prev.values.sum() / 2 })
                .withHeader(prevHeader!!)
                .build()
        }

        private fun formatString(value: Number): String {
            return if (value is Int || value is Long) DecimalFormat("#,##0").format(value)
            else DecimalFormat("#,##0.00").format(value)
        }
    }
}