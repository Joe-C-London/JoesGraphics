package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.models.general.PartyOrCoalition
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

    companion object {
        fun <R, POC : PartyOrCoalition> of(
            regions: List<R>,
            name: R.() -> Flow.Publisher<String>,
            currVotes: R.() -> Flow.Publisher<out Map<out POC, Int>>,
            prevVotes: R.() -> Flow.Publisher<out Map<out POC, Int>>,
            swingOrder: Comparator<POC>,
            numRows: Int,
            progressLabel: (R.() -> Flow.Publisher<out String?>)? = null,
            swingRange: Flow.Publisher<Double>? = null,
            partyFilter: Flow.Publisher<out Collection<POC>>? = null,
            title: Flow.Publisher<String>,
        ): RegionalSwingsScreen {
            val swings = regions.map {
                it to SwingFrameBuilder.prevCurr(
                    prev = it.prevVotes(),
                    curr = it.currVotes(),
                    partyOrder = swingOrder,
                    selectedParties = partyFilter,
                    range = swingRange,
                    header = it.name(),
                    progress = progressLabel?.invoke(it),
                )
            }
            return RegionalSwingsScreen(
                JPanel().also { panel ->
                    panel.background = Color.WHITE
                    panel.layout = GridLayout(numRows, 0, 5, 5)
                    panel.border = EmptyBorder(5, 5, 5, 5)
                    swings.forEach { (_, swingFrame) ->
                        panel.add(swingFrame)
                    }
                },
                title,
                swings.map { (region, swingFrame) ->
                    name(region).run {
                        if (progressLabel == null) {
                            this
                        } else {
                            merge(progressLabel(region)) { n, p -> if (p == null) n else "$n [$p]" }
                        }
                    }
                        .merge(swingFrame.altText) { t, b -> "$t: $b" }
                }.combine().merge(title) { text, top -> "$top\n\n${text.joinToString("\n")}" },
            )
        }
    }
}
