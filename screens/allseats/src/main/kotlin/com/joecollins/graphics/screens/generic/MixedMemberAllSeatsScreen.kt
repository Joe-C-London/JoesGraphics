package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.util.LinkedList
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class MixedMemberAllSeatsScreen private constructor(
    panel: JPanel,
    title: Flow.Publisher<String?>,
    altText: Flow.Publisher<String>,
) : GenericPanel(panel, title, altText) {

    class MultiRegionBuilder<C, R>(
        private val prevConstituencies: Flow.Publisher<out Map<C, Party?>>,
        private val currConstituencies: Flow.Publisher<out Map<C, PartyResult?>>,
        private val constituencyNames: (C) -> String,
        private val prevRegionLists: Flow.Publisher<out Map<R, List<Party>>>,
        private val currRegionLists: Flow.Publisher<out Map<R, List<PartyResult>>>,
        private val regionNames: (R) -> String,
        private val regionConstituencies: (R) -> List<C>,
    ) {
        private inner class RegionalPanel(val header: Flow.Publisher<String>, val region: R) {
            val constituencyRows = regionConstituencies(region).size.asOneTimePublisher()
            val listRows = prevRegionLists.merge(currRegionLists) { p, c ->
                maxOf(p[region]?.size ?: 0, c[region]?.size ?: 0)
            }
            val totalRows = constituencyRows.merge(listRows) { c, l -> c + l + 1 }

            val constituencyItems = regionConstituencies(region).map { c ->
                currConstituencies.map { it[c] }.merge(prevConstituencies.map { it[c] }) { curr, prev ->
                    Triple(c, curr, prev)
                }
            }.combine()

            val listItems = currRegionLists.map { it[region] ?: emptyList() }.merge(prevRegionLists.map { it[region] ?: emptyList() }) { curr, prev ->
                val currLeft = curr.toMutableList()
                val prevLeft = prev.toMutableList()
                val items = LinkedList<Pair<PartyResult?, Party?>>()
                while (currLeft.isNotEmpty()) {
                    val c = currLeft.removeAt(0)
                    val p = when {
                        prevLeft.contains(c.party) -> c.party
                        else -> prevLeft.firstOrNull { p -> currLeft.count { it.party == p } < prevLeft.count { it == p } }
                    }
                    if (p != null) prevLeft.removeAt(prevLeft.indexOf(p))
                    items.add(c to p)
                }
                while (prevLeft.isNotEmpty()) {
                    val p = prevLeft.removeAt(0)
                    items.add(null to p)
                }
                items
            }

            fun createPanel(): JPanel {
                val itemFunc: (Party?, PartyResult?, String) -> ResultListingFrame.Item = { prev, curr, name ->
                    ResultListingFrame.Item(
                        text = name,
                        border = prev?.color ?: Color.WHITE,
                        background = if (curr?.elected == true) curr.party.color else Color.WHITE,
                        foreground = if (curr == null) Color.LIGHT_GRAY else if (!curr.elected) ColorUtils.contrastForBackground(curr.party.color) else ColorUtils.foregroundToContrast(curr.party.color),
                    )
                }
                val constituencyItems = constituencyItems.mapElements { (c, curr, prev) ->
                    itemFunc(prev, curr, constituencyNames(c))
                }
                val blankItems = regionalPanels.map { it.constituencyRows }.combine().map { it.max() + 1 }
                    .merge(constituencyItems) { t, c ->
                        generateSequence {
                            ResultListingFrame.Item(
                                text = "",
                                border = Color.WHITE,
                                background = Color.WHITE,
                                foreground = Color.WHITE,
                            )
                        }.take(t - c.size).toList()
                    }
                val listItems = listItems.mapElements { (curr, prev) ->
                    itemFunc(prev, curr, regionNames(region))
                }
                return ResultListingFrame(
                    headerPublisher = header,
                    numRowsPublisher = regionalPanels.map { it.totalRows }.combine().map { it.max() },
                    itemsPublisher = constituencyItems.merge(blankItems) { c, b -> listOf(c, b).flatten() }
                        .merge(listItems) { c, l -> listOf(c, l).flatten() },
                )
            }

            fun createAltText(): Flow.Publisher<String> {
                val constituencyGroups = constituencyItems.map { c -> c.groupBy({ it.second?.party to it.third }, { it.second }) }
                val listGroups = listItems.map { c -> c.groupBy({ it.first?.party to it.second }, { it.first }) }
                val allGroups = constituencyGroups.merge(listGroups) { c, l -> c to l }.merge(allDeclared) { (cg, lg), all ->
                    sequenceOf(cg.keys, lg.keys)
                        .flatten()
                        .distinct()
                        .sortedWith(
                            Comparator.comparing<Pair<Party?, Party?>?, Int?> { -(cg[it]?.size ?: 0) }
                                .thenByDescending { lg[it]?.size ?: 0 },
                        )
                        .joinToString("\n") {
                            val curr = it.first
                            val prev = it.second
                            val head = when {
                                curr == null -> "PENDING ${prev?.abbreviation ?: "NEW"}"
                                prev == null -> "${curr.abbreviation} WIN (NEW)"
                                curr == prev -> "${curr.abbreviation} HOLD"
                                else -> "${curr.abbreviation} GAIN FROM ${prev.abbreviation}"
                            }
                            val seatCounter: (List<PartyResult?>) -> String =
                                { list -> (if (all || curr == null) "" else "${list.count { e -> e?.elected == true }}/") + "${list.size}" }
                            val tail = (cg[it] ?: emptyList()).let(seatCounter) + " + " + (lg[it] ?: emptyList()).let(seatCounter)
                            "$head: $tail"
                        }
                }
                return header.merge(allGroups) { h, a -> "$h\n$a" }
            }
        }
        private val regionalPanels = LinkedList<RegionalPanel>()

        private val constituencyRows = regionalPanels

        private val allDeclared = currConstituencies.merge(currRegionLists) { c, r ->
            c.values.filterNotNull().all { it.isElected } && r.values.flatten().all { it.isElected }
        }

        fun withRegion(header: Flow.Publisher<String>, region: R): MultiRegionBuilder<C, R> {
            regionalPanels.add(RegionalPanel(header, region))
            return this
        }

        fun build(title: Flow.Publisher<String?>): MixedMemberAllSeatsScreen {
            return MixedMemberAllSeatsScreen(
                JPanel().also { panel ->
                    panel.background = Color.WHITE
                    panel.layout = GridLayout(1, 0, 5, 5)
                    panel.border = EmptyBorder(5, 5, 5, 5)
                    regionalPanels.forEach { panel.add(it.createPanel()) }
                },
                title,
                regionalPanels.map { it.createAltText() }.combine().merge(title) { p, t ->
                    t + p.joinToString("") { "\n\n$it" }
                },
            )
        }
    }

    companion object {
        fun <C, R> multiRegion(
            prevConstituencies: Flow.Publisher<out Map<C, Party?>>,
            currConstituencies: Flow.Publisher<out Map<C, PartyResult?>>,
            constituencyNames: (C) -> String,
            prevRegionLists: Flow.Publisher<out Map<R, List<Party>>>,
            currRegionLists: Flow.Publisher<out Map<R, List<PartyResult>>>,
            regionNames: (R) -> String,
            regionConstituencies: (R) -> List<C>,
        ): MultiRegionBuilder<C, R> {
            return MultiRegionBuilder(
                prevConstituencies,
                currConstituencies,
                constituencyNames,
                prevRegionLists,
                currRegionLists,
                regionNames,
                regionConstituencies,
            )
        }
    }
}
