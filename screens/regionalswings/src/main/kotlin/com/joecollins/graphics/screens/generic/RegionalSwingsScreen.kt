package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class RegionalSwingsScreen private constructor(
    panel: JPanel,
    title: Flow.Publisher<String>,
    altText: Flow.Publisher<String>,
) : GenericPanel(panel, title, altText) {

    class Builder<R, POC : PartyOrCoalition>(
        private val regions: List<R>,
        private val name: (R) -> Flow.Publisher<String>,
        private val currVotes: (R) -> Flow.Publisher<out Map<out POC, Int>>,
        private val prevVotes: (R) -> Flow.Publisher<out Map<out POC, Int>>,
        private val swingOrder: Comparator<POC>,
        private val numRows: Int,
    ) {
        private var progressLabel: (R) -> Flow.Publisher<out String?> = { null.asOneTimePublisher() }
        private var swingRange: Flow.Publisher<Double> = 0.10.asOneTimePublisher()
        private var partyFilter: Flow.Publisher<out Collection<POC>>? = null

        fun withProgressLabel(
            progress: (R) -> Flow.Publisher<out String?>,
        ): Builder<R, POC> {
            this.progressLabel = progress
            return this
        }

        fun withRange(
            swingRange: Flow.Publisher<Double>,
        ): Builder<R, POC> {
            this.swingRange = swingRange
            return this
        }

        fun withPartyFilter(
            partyFilter: Flow.Publisher<out Collection<POC>>,
        ): Builder<R, POC> {
            this.partyFilter = partyFilter
            return this
        }

        fun build(title: Flow.Publisher<String>): RegionalSwingsScreen {
            val swings = regions.map {
                it to SwingFrameBuilder.prevCurr(
                    prevVotes(it),
                    currVotes(it),
                    swingOrder,
                    partyFilter,
                )
                    .withHeader(name(it), progressPublisher = progressLabel(it))
                    .withRange(swingRange)
            }
            return RegionalSwingsScreen(
                JPanel().also { panel ->
                    panel.background = Color.WHITE
                    panel.layout = GridLayout(numRows, 0, 5, 5)
                    panel.border = EmptyBorder(5, 5, 5, 5)
                    swings.forEach { (_, swingFrame) ->
                        panel.add(swingFrame.build())
                    }
                },
                title,
                swings.map { (region, swingFrame) ->
                    name(region).merge(progressLabel(region)) { n, p -> if (p == null) n else "$n [$p]" }
                        .merge(swingFrame.buildBottomText()!!) { t, b -> "$t: $b" }
                }.combine().merge(title) { text, top -> "$top\n\n${text.joinToString("\n")}" },
            )
        }
    }

    companion object {
        fun <R, POC : PartyOrCoalition> of(
            regions: List<R>,
            name: (R) -> Flow.Publisher<String>,
            currVotes: (R) -> Flow.Publisher<out Map<out POC, Int>>,
            prevVotes: (R) -> Flow.Publisher<out Map<out POC, Int>>,
            swingOrder: Comparator<POC>,
            numRows: Int,
        ): Builder<R, POC> {
            return Builder(regions, name, currVotes, prevVotes, swingOrder, numRows)
        }
    }
}
