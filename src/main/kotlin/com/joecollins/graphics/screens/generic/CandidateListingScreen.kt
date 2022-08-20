package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.ListingFrameBuilder
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
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
    builder: Builder
) : JPanel() {

    internal val candidatesPanel = builder.candidatesPanel
    internal val prevPanel = builder.prevPanel
    internal val secondaryPrevPanel = builder.secondaryPrevPanel
    internal val mapPanel = builder.mapPanel

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
        fun of(candidates: Flow.Publisher<out List<Candidate>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?>): Builder {
            return Builder(candidates, header, subhead, null)
        }

        fun of(candidates: Flow.Publisher<out List<Candidate>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?>, incumbentMarker: String): Builder {
            return Builder(candidates, header, subhead, incumbentMarker)
        }
    }

    class Builder internal constructor(candidates: Flow.Publisher<out List<Candidate>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?> = "".asOneTimePublisher(), incumbentMarker: String?) {
        internal val candidatesPanel: JPanel
        internal var prevPanel: JPanel? = null
        internal var secondaryPrevPanel: JPanel? = null
        internal var mapPanel: JPanel? = null

        init {
            candidatesPanel = ListingFrameBuilder.of(
                candidates,
                { it.name.uppercase() + (if (!it.incumbent || incumbentMarker == null) "" else (" $incumbentMarker")) },
                { it.party.name.uppercase() },
                { it.party.color }
            )
                .withHeader(header)
                .withSubhead(subhead)
                .build()
        }

        fun withPrev(prevVotes: Flow.Publisher<out Map<Party, Int>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()): Builder {
            prevPanel = BarFrameBuilder.basic(prevVotes.map { v -> createBars(v) })
                .withMax((2.0 / 3).asOneTimePublisher())
                .withHeader(header)
                .withSubhead(subhead)
                .build()
            return this
        }

        fun withSecondaryPrev(prevVotes: Flow.Publisher<out Map<Party, Int>>, header: Flow.Publisher<out String?>, subhead: Flow.Publisher<out String?> = (null as String?).asOneTimePublisher()): Builder {
            secondaryPrevPanel = BarFrameBuilder.basic(prevVotes.map { v -> createBars(v) })
                .withMax((2.0 / 3).asOneTimePublisher())
                .withHeader(header)
                .withSubhead(subhead)
                .build()
            return this
        }

        private fun createBars(votes: Map<Party, Int>): List<BarFrameBuilder.BasicBar> {
            val total = votes.values.sum()
            return votes.asSequence()
                .sortedByDescending { if (it.key == Party.OTHERS) -1 else it.value }
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
            mapPanel = MapBuilder(shapes, selectedShape, (null as PartyResult?).asOneTimePublisher(), focus, header).createMapFrame()
            return this
        }

        fun <K> withMap(
            shapes: Flow.Publisher<out Map<K, Shape>>,
            selectedShape: Flow.Publisher<out K>,
            focus: Flow.Publisher<out List<K>?>,
            additionalHighlight: Flow.Publisher<out List<K>?>,
            header: Flow.Publisher<out String>
        ): Builder {
            mapPanel = MapBuilder(shapes, selectedShape, (null as PartyResult?).asOneTimePublisher(), focus, additionalHighlight, header).createMapFrame()
            return this
        }

        fun build(title: Flow.Publisher<out String>): CandidateListingScreen {
            return CandidateListingScreen(createHeaderLabel(title), this)
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
