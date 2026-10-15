package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.SankeyFrame
import com.joecollins.graphics.utils.PanelUtils.pad
import com.joecollins.models.general.Party
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.util.concurrent.Flow

class SankeyScreen private constructor(frame: SankeyFrame, title: Flow.Publisher<String>, altText: Flow.Publisher<out (Int) -> String?>) : GenericPanel(frame.pad(), title, altText) {

    companion object {
        fun <T> build(
            prevWinners: Flow.Publisher<Map<T, Party>>,
            currWinners: Flow.Publisher<Map<T, Party?>>,
            header: Flow.Publisher<String>,
            title: Flow.Publisher<String>,
            prevPartyOrder: Flow.Publisher<List<Party>>? = null,
            currPartyOrder: Flow.Publisher<List<Party>>? = null,
        ): SankeyScreen {
            val leftItems = prevWinners.map { it.values.distinct().sortedBy { it.abbreviation } }
                .run { if (prevPartyOrder == null) this else merge(prevPartyOrder) { p, o -> o + p.filterNot { it in o } } }
            val rightItems = currWinners.map { it.values.distinct().filterNotNull().sortedBy { it.abbreviation } }
                .run { if (currPartyOrder == null) this else merge(currPartyOrder) { p, o -> o + p.filterNot { it in o } } }
            val transfers = prevWinners.merge(currWinners) { prev, curr ->
                val items = prev.keys + curr.keys
                items.groupingBy { prev[it] to curr[it] }.eachCount()
            }
            val frame = SankeyFrame.create(
                leftItems = leftItems,
                rightItems = rightItems,
                transfers = transfers,
                header = header,
                leftLabel = { count -> "$abbreviation: $count" },
                leftColor = { color },
                rightLabel = { count -> "$abbreviation: $count" },
                rightColor = { color },
            )
            val altText = leftItems.merge(rightItems) { l, r -> l to r }.merge(transfers) { (left, right), t ->
                (left + listOf(null)).flatMap { l ->
                    (right + listOf(null)).mapNotNull { r ->
                        val count = t[l to r]
                        if (count == null) {
                            null
                        } else {
                            "${l?.abbreviation ?: "?"} TO ${r?.abbreviation ?: "?"}: $count"
                        }
                    }
                }.joinToString("\n")
            }.merge(title.merge(header) { t, h -> "$t\n$h" }) { b, h -> "$h\n\n$b" }
            return SankeyScreen(
                frame,
                title,
                altText.map { { _: Int -> it } },
            )
        }
    }
}
