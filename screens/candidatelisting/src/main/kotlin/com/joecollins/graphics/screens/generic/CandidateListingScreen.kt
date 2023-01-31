package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CandidateListingScreen private constructor(
    header: JLabel,
    candidatesPanel: JPanel,
    prevPanel: JPanel?,
    secondaryPrevPanel: JPanel?,
    mapPanel: JPanel?,
    override val altText: Flow.Publisher<String>,
) : JPanel(), AltTextProvider {

    init {
        layout = BorderLayout()
        background = Color.WHITE
        add(header, BorderLayout.NORTH)
        val panel = JPanel()
        panel.layout = RightStackLayout()
        panel.background = Color.WHITE
        add(panel, BorderLayout.CENTER)
        panel.add(candidatesPanel, RightStackLayout.WEST)
        if (prevPanel != null) {
            panel.add(prevPanel, RightStackLayout.EAST)
        }
        if (secondaryPrevPanel != null) {
            panel.add(secondaryPrevPanel, RightStackLayout.EAST)
        }
        if (mapPanel != null) {
            panel.add(mapPanel, RightStackLayout.EAST)
        }
        if (setOf(prevPanel, secondaryPrevPanel, mapPanel).count { it != null } == 1) {
            panel.add(JPanel().also { it.background = Color.WHITE }, RightStackLayout.EAST)
        }
    }

    companion object {
        fun of(candidates: Flow.Publisher<out List<Candidate>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?>, incumbentMarker: String? = null): Builder {
            return Builder(candidates, header, subhead, incumbentMarker)
        }
    }

    class Builder internal constructor(
        private val candidates: Flow.Publisher<out List<Candidate>>,
        private val candidateHeader: Flow.Publisher<out String?>,
        private val candidateSubhead: Flow.Publisher<out String?> = "".asOneTimePublisher(),
        private val incumbentMarker: String?,
    ) {

        private var showTwoColumns: Flow.Publisher<Boolean>? = null

        private var prevVotes: Flow.Publisher<out Map<Party, Int>>? = null
        private var prevVoteHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var prevVoteSubhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()

        private var secondaryPrevVotes: Flow.Publisher<out Map<Party, Int>>? = null
        private var secondaryPrevVoteHeader: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()
        private var secondaryPrevVoteSubhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()

        private var mapPanel: JPanel? = null

        fun withPrev(prevVotes: Flow.Publisher<out Map<Party, Int>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()): Builder {
            this.prevVotes = prevVotes
            this.prevVoteHeader = header
            this.prevVoteSubhead = subhead
            return this
        }

        fun withSecondaryPrev(prevVotes: Flow.Publisher<out Map<Party, Int>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()): Builder {
            this.secondaryPrevVotes = prevVotes
            this.secondaryPrevVoteHeader = header
            this.secondaryPrevVoteSubhead = subhead
            return this
        }

        private fun createVoteBars(votes: Map<Party, Int>): List<BarFrameBuilder.BasicBar> {
            val total = votes.values.sum()
            return votes.asSequence()
                .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                .map {
                    BarFrameBuilder.BasicBar(
                        it.key.abbreviation.uppercase(),
                        it.key.color,
                        it.value.toDouble() / total,
                        DecimalFormat("0.0%").format(it.value.toDouble() / total),
                    )
                }
                .toList()
        }

        fun <K> withMap(
            shapes: Flow.Publisher<out Map<K, Shape>>,
            selectedShape: Flow.Publisher<out K>,
            focus: Flow.Publisher<out List<K>?>,
            header: Flow.Publisher<out String>,
        ): Builder {
            mapPanel = MapBuilder(
                shapes,
                selectedShape,
                (null as PartyResult?).asOneTimePublisher(),
                focus,
                header,
            ).createMapFrame()
            return this
        }

        fun <K> withMap(
            shapes: Flow.Publisher<out Map<K, Shape>>,
            selectedShape: Flow.Publisher<out K>,
            focus: Flow.Publisher<out List<K>?>,
            additionalHighlight: Flow.Publisher<out List<K>?>,
            header: Flow.Publisher<out String>,
        ): Builder {
            mapPanel = MapBuilder(
                shapes,
                selectedShape,
                (null as PartyResult?).asOneTimePublisher(),
                focus,
                additionalHighlight,
                header,
            ).createMapFrame()
            return this
        }

        fun withTwoColumns(showTwoColumns: Flow.Publisher<Boolean>): Builder {
            this.showTwoColumns = showTwoColumns
            return this
        }

        fun build(title: Flow.Publisher<out String>): CandidateListingScreen {
            return CandidateListingScreen(
                createHeaderLabel(title),
                createCandidatesPanel(),
                prevVotes?.let { createPrevPanel(it, prevVoteHeader, prevVoteSubhead) },
                secondaryPrevVotes?.let { createPrevPanel(it, secondaryPrevVoteHeader, secondaryPrevVoteSubhead) },
                this.mapPanel,
                createAltText(title),
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

        private fun createCandidatesPanel(): BarFrame {
            return BarFrame(
                barsPublisher = candidates.merge(showTwoColumns ?: false.asOneTimePublisher()) { cList, show ->
                    if (show) {
                        val mid = cList.size / 2
                        val first = cList.take(mid)
                        val last = cList.drop(mid)
                        (0 until mid).map { idx ->
                            val left = first[idx]
                            val right = if (idx == last.size) null else last[idx]
                            val func = { c: Candidate -> c.name.uppercase() + (if (!c.incumbent || incumbentMarker == null) "" else (" $incumbentMarker")) + " (${c.party}) " }
                            BarFrame.Bar(
                                func(left),
                                right?.let(func) ?: "",
                                listOf(
                                    left.party.color to 0.49,
                                    Color.WHITE to 0.02,
                                    (right?.party?.color ?: Color.WHITE) to 0.49,
                                ),
                            )
                        }
                    } else {
                        cList.map {
                            BarFrame.Bar(
                                it.name.uppercase() + (if (!it.incumbent || incumbentMarker == null) "" else (" $incumbentMarker")),
                                it.party.name.uppercase(),
                                listOf(it.party.color to 1.0),
                            )
                        }
                    }
                },
                headerPublisher = candidateHeader,
                subheadTextPublisher = candidateSubhead,
                maxPublisher = 1.0.asOneTimePublisher(),
            )
        }

        private fun createPrevPanel(prevVotes: Flow.Publisher<out Map<Party, Int>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?>): JPanel {
            return BarFrameBuilder.basic(prevVotes.map { v -> createVoteBars(v) })
                .withMax((2.0 / 3).asOneTimePublisher())
                .withHeader(header)
                .withSubhead(subhead)
                .build()
        }

        private fun createAltText(title: Flow.Publisher<out String>): Flow.Publisher<String> {
            val candidatesTitle = candidateHeader.merge(candidateSubhead) { h, s ->
                if (s.isNullOrBlank()) {
                    h
                } else {
                    "$h, $s"
                }
            }
            val candidates = this.candidates.map { candidates ->
                candidates.joinToString("\n") { "${it.name.uppercase()}${if (it.incumbent && incumbentMarker != null) " $incumbentMarker" else ""} (${it.party.abbreviation})" }
            }.merge(candidatesTitle) { cList, cTitle -> "$cTitle\n$cList" }
            var ret = title.merge(candidates) { t, c -> "$t\n\n$c" }
            if (prevVotes != null) {
                ret = ret.merge(createPrevAltText(prevVotes!!, prevVoteHeader, prevVoteSubhead)) { r, prev -> "$r\n\n$prev" }
            }
            if (secondaryPrevVotes != null) {
                ret = ret.merge(createPrevAltText(secondaryPrevVotes!!, secondaryPrevVoteHeader, secondaryPrevVoteSubhead)) { r, prev -> "$r\n\n$prev" }
            }
            return ret
        }

        private fun createPrevAltText(prevVotes: Flow.Publisher<out Map<Party, Int>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?>): Flow.Publisher<String> {
            val title = header.merge(subhead) { h, s ->
                if (s.isNullOrBlank()) {
                    h
                } else {
                    "$h, $s"
                }
            }
            return prevVotes.map { votes ->
                val total = votes.values.sum().toDouble()
                votes.entries.sortedByDescending { it.key.overrideSortOrder ?: it.value }
                    .joinToString("\n") { "${it.key.abbreviation}: ${DecimalFormat("0.0%").format(it.value / total)}" }
            }.merge(title) { v, t -> "$t\n$v" }
        }
    }
}
