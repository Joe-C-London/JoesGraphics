package com.joecollins.graphics.screens.generic

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
    mapPanel: JPanel?
) : JPanel() {

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
    }

    companion object {
        fun of(candidates: Flow.Publisher<out List<Candidate>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?>, incumbentMarker: String? = null): Builder {
            return Builder(candidates, header, subhead, incumbentMarker)
        }
    }

    class Builder internal constructor(candidates: Flow.Publisher<out List<Candidate>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?> = "".asOneTimePublisher(), incumbentMarker: String?) {
        private val candidatesPanel: () -> JPanel
        private var prevPanel: JPanel? = null
        private var secondaryPrevPanel: JPanel? = null
        private var mapPanel: JPanel? = null

        private var showTwoColumns: Flow.Publisher<Boolean>? = null

        init {
            candidatesPanel = {
                BarFrame(
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
                                        (right?.party?.color ?: Color.WHITE) to 0.49
                                    )
                                )
                            }
                        } else {
                            cList.map {
                                BarFrame.Bar(
                                    it.name.uppercase() + (if (!it.incumbent || incumbentMarker == null) "" else (" $incumbentMarker")),
                                    it.party.name.uppercase(),
                                    listOf(it.party.color to 1.0)
                                )
                            }
                        }
                    },
                    headerPublisher = header,
                    subheadTextPublisher = subhead,
                    maxPublisher = 1.0.asOneTimePublisher()
                )
            }
        }

        fun withPrev(prevVotes: Flow.Publisher<out Map<Party, Int>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()): Builder {
            prevPanel = BarFrameBuilder.basic(prevVotes.map { v -> createVoteBars(v) })
                .withMax((2.0 / 3).asOneTimePublisher())
                .withHeader(header)
                .withSubhead(subhead)
                .build()
            return this
        }

        fun withSecondaryPrev(prevVotes: Flow.Publisher<out Map<Party, Int>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()): Builder {
            secondaryPrevPanel = BarFrameBuilder.basic(prevVotes.map { v -> createVoteBars(v) })
                .withMax((2.0 / 3).asOneTimePublisher())
                .withHeader(header)
                .withSubhead(subhead)
                .build()
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
                        DecimalFormat("0.0%").format(it.value.toDouble() / total)
                    )
                }
                .toList()
        }

        fun <K> withMap(
            shapes: Flow.Publisher<out Map<K, Shape>>,
            selectedShape: Flow.Publisher<out K>,
            focus: Flow.Publisher<out List<K>?>,
            header: Flow.Publisher<out String>
        ): Builder {
            mapPanel = MapBuilder(
                shapes,
                selectedShape,
                (null as PartyResult?).asOneTimePublisher(),
                focus,
                header
            ).createMapFrame()
            return this
        }

        fun <K> withMap(
            shapes: Flow.Publisher<out Map<K, Shape>>,
            selectedShape: Flow.Publisher<out K>,
            focus: Flow.Publisher<out List<K>?>,
            additionalHighlight: Flow.Publisher<out List<K>?>,
            header: Flow.Publisher<out String>
        ): Builder {
            mapPanel = MapBuilder(
                shapes,
                selectedShape,
                (null as PartyResult?).asOneTimePublisher(),
                focus,
                additionalHighlight,
                header
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
                this.candidatesPanel(),
                this.prevPanel,
                this.secondaryPrevPanel,
                this.mapPanel
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
    }
}