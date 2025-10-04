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

    companion object {
        fun <C, R> multiRegion(
            regions: Iterable<R>,
            prevWinners: Flow.Publisher<out Map<C, Party?>>,
            currWinners: Flow.Publisher<out Map<C, PartyResult?>>,
            constituencyName: C.() -> String,
            prevRegionLists: Flow.Publisher<out Map<R, List<Party>>>,
            currRegionLists: Flow.Publisher<out Map<R, List<PartyResult>>>,
            regionName: R.() -> String,
            regionConstituencies: R.() -> List<C>,
            regionHeader: R.() -> Flow.Publisher<String>,
            title: Flow.Publisher<String?>,
        ): MixedMemberAllSeatsScreen {
            val regionalPanels = regions.map { region ->
                RegionalPanel(
                    region.regionHeader(),
                    region.regionName(),
                    region.regionConstituencies(),
                    constituencyName,
                    prevWinners,
                    currWinners,
                    prevRegionLists.map { it[region] ?: emptyList() },
                    currRegionLists.map { it[region] ?: emptyList() },
                )
            }
            val allDeclared = currWinners.merge(currRegionLists) { c, r ->
                c.values.filterNotNull().all { it.elected } && r.values.flatten().all { it.elected }
            }
            return MixedMemberAllSeatsScreen(
                JPanel().also { panel ->
                    panel.background = Color.WHITE
                    panel.layout = GridLayout(1, 0, 5, 5)
                    panel.border = EmptyBorder(5, 5, 5, 5)
                    regionalPanels.forEach {
                        panel.add(
                            it.createPanel(
                                regionalPanels.map { it.constituencyRows }.combine().map { it.max() + 1 },
                                regionalPanels.map { it.totalRows }.combine().map { it.max() },
                            ),
                        )
                    }
                },
                title,
                regionalPanels.map { it.createAltText(allDeclared) }.combine().merge(title) { p, t ->
                    t + p.joinToString("") { "\n\n$it" }
                },
            )
        }

        private class RegionalPanel<C>(
            val header: Flow.Publisher<String>,
            val name: String,
            constituencies: List<C>,
            val constituencyName: C.() -> String,
            val prevWinners: Flow.Publisher<out Map<C, Party?>>,
            val currWinners: Flow.Publisher<out Map<C, PartyResult?>>,
            prevRegionList: Flow.Publisher<List<Party>>,
            currRegionList: Flow.Publisher<List<PartyResult>>,
        ) {
            val constituencyRows = constituencies.size.asOneTimePublisher()
            val listRows = prevRegionList.merge(currRegionList) { p, c ->
                maxOf(p.size, c.size)
            }
            val totalRows = constituencyRows.merge(listRows) { c, l -> c + l + 1 }

            val constituencyItems = constituencies.map { c ->
                currWinners.map { it[c] }.merge(prevWinners.map { it[c] }) { curr, prev ->
                    Triple(c, curr, prev)
                }
            }.combine()

            val listItems = currRegionList.merge(prevRegionList) { curr, prev ->
                val currLeft = curr.toMutableList()
                val prevLeft = prev.toMutableList()
                val items = LinkedList<Pair<PartyResult?, Party?>>()
                while (currLeft.isNotEmpty()) {
                    val c = currLeft.removeAt(0)
                    val p = when {
                        prevLeft.contains(c.leader) -> c.leader
                        else -> prevLeft.firstOrNull { p -> currLeft.count { it.leader == p } < prevLeft.count { it == p } }
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

            fun createPanel(maxConstituenciesPerRegion: Flow.Publisher<Int>, maxRowsPerRegion: Flow.Publisher<Int>): JPanel {
                val itemFunc: (Party?, PartyResult?, String) -> ResultListingFrame.Item = { prev, curr, name ->
                    ResultListingFrame.Item(
                        text = name,
                        border = prev?.color ?: Color.WHITE,
                        background = if (curr?.elected == true) curr.leader.color else Color.WHITE,
                        foreground = if (curr == null) {
                            Color.LIGHT_GRAY
                        } else if (!curr.elected) {
                            ColorUtils.contrastForBackground(curr.leader.color)
                        } else {
                            ColorUtils.foregroundToContrast(curr.leader.color)
                        },
                    )
                }
                val constituencyItems = constituencyItems.mapElements { (c, curr, prev) ->
                    itemFunc(prev, curr, c.constituencyName())
                }
                val blankItems = maxConstituenciesPerRegion
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
                    itemFunc(prev, curr, name)
                }
                return ResultListingFrame(
                    headerPublisher = header,
                    numRowsPublisher = maxRowsPerRegion,
                    itemsPublisher = constituencyItems.merge(blankItems) { c, b -> listOf(c, b).flatten() }
                        .merge(listItems) { c, l -> listOf(c, l).flatten() },
                )
            }

            fun createAltText(allDeclared: Flow.Publisher<Boolean>): Flow.Publisher<String> {
                val constituencyGroups = constituencyItems.map { c -> c.groupBy({ it.second?.leader to it.third }, { it.second }) }
                val listGroups = listItems.map { c -> c.groupBy({ it.first?.leader to it.second }, { it.first }) }
                val allGroups = constituencyGroups.merge(listGroups) { c, l -> c to l }.merge(allDeclared) { (cg, lg), all ->
                    sequenceOf(cg.keys, lg.keys)
                        .flatten()
                        .distinct()
                        .sortedWith(
                            Comparator.comparing<Pair<Party?, Party?>, Int> { -(cg[it]?.size ?: 0) }
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
    }
}
