package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.ResultListingFrame
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import org.apache.commons.lang3.StringUtils
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.LayoutManager
import java.awt.Point
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.ceil
import kotlin.math.roundToInt

class AllSeatsScreen private constructor(title: Flow.Publisher<out String?>, frame: JPanel, altText: Flow.Publisher<String>) : GenericPanel(pad(frame), title, altText) {

    private class Input<T>(private val nameFunc: T.() -> String) {
        private var prevWinners: List<Pair<T, Party?>> = emptyList()
        private var currResults: Map<T, PartyResult?> = emptyMap()
        private var seatFilter: Set<T>? = null
        private var partyChanges: Map<Party, Party> = emptyMap()

        fun changedParty(party: Party): Party = partyChanges[party] ?: party

        fun setPrevWinners(prevWinners: Map<T, Party?>) {
            this.prevWinners = prevWinners.entries
                .asSequence()
                .sortedBy { e -> StringUtils.stripAccents(nameFunc(e.key)).uppercase() }
                .map { e ->
                    Pair(
                        e.key,
                        e.value,
                    )
                }
                .toList()
            publishResults()
        }

        fun setCurrResults(currResults: Map<T, PartyResult?>) {
            this.currResults = currResults
            publishResults()
        }

        fun setSeatFilter(seatFilter: Set<T>?) {
            this.seatFilter = seatFilter
            publishResults()
        }

        fun setPartyChanges(changes: Map<Party, Party>) {
            this.partyChanges = changes
            publishResults()
        }

        private fun publishResults() {
            (resultPublisher as Publisher<List<Entry<T>>>).submit(toEntries())
        }

        val resultPublisher: Flow.Publisher<List<Entry<T>>> = Publisher(toEntries())

        private fun toEntries() = this.prevWinners
            .asSequence()
            .filter { this.seatFilter?.contains(it.first) ?: true }
            .map {
                Triple(
                    it.first,
                    it.second,
                    this.currResults[it.first],
                )
            }
            .map {
                val result = it.third
                Entry(
                    it.first,
                    it.second,
                    result,
                )
            }
            .toList()
    }

    private class Entry<T>(val key: T, val prevWinner: Party?, val currResult: PartyResult?) {

        val prevColor: Color = prevWinner?.color ?: Color.WHITE

        val resultColor: Color = currResult?.leader?.color ?: Color.LIGHT_GRAY
        val fill = currResult?.elected ?: false
    }

    companion object {
        private const val DEFAULT_ROWS = 20

        fun <T> of(
            prevWinner: Flow.Publisher<out Map<T, Party?>>,
            currResult: Flow.Publisher<out Map<T, PartyResult?>>,
            nameFunc: T.() -> String,
            header: Flow.Publisher<out String?>,
            numRows: Flow.Publisher<out Int>? = null,
            seatFilter: Flow.Publisher<out Set<T>?>? = null,
            partyChanges: Flow.Publisher<Map<Party, Party>>? = null,
            title: Flow.Publisher<out String?>,
        ): AllSeatsScreen {
            val inputs = Input(nameFunc)
            prevWinner.subscribe(Subscriber { inputs.setPrevWinners(it) })
            currResult.subscribe(Subscriber { inputs.setCurrResults(it) })
            seatFilter?.subscribe(Subscriber { inputs.setSeatFilter(it) })
            partyChanges?.subscribe(Subscriber { inputs.setPartyChanges(it) })
            val frame = ResultListingFrame(
                headerPublisher = header,
                numRowsPublisher = numRows ?: DEFAULT_ROWS.asOneTimePublisher(),
                itemsPublisher = inputs.resultPublisher.mapElements {
                    ResultListingFrame.Item(
                        text = nameFunc(it.key),
                        border = it.prevColor,
                        background = if (it.fill) it.resultColor else Color.WHITE,
                        foreground = if (!it.fill) ColorUtils.contrastForBackground(it.resultColor) else ColorUtils.foregroundToContrast(it.resultColor),
                    )
                },
            )
            val altText = run {
                val head = title.merge(header) { t, h -> listOfNotNull(t, h).joinToString("\n\n") }
                val entries = inputs.resultPublisher.map { results ->
                    if (results.isEmpty()) return@map "(empty)"
                    val allElected = results.all { it.currResult?.elected ?: true }
                    results
                        .groupBy { it.prevWinner to it.currResult?.leader }
                        .entries
                        .sortedByDescending { group -> group.value.size }
                        .joinToString("\n") { group ->
                            val label = group.key.let { (from, to) ->
                                when {
                                    to == null -> "PENDING ${from?.abbreviation ?: "NEW"}"
                                    to == from -> "${to.abbreviation} HOLD"
                                    from == null -> "${to.abbreviation} WIN (NEW)"
                                    to == inputs.changedParty(from) -> "${to.abbreviation} HOLD (${from.abbreviation})"
                                    else -> "${to.abbreviation} GAIN FROM ${from.abbreviation}"
                                }
                            }
                            val count = group.value.size
                            val value = if (allElected || group.key.second == null) {
                                "$count"
                            } else {
                                val elected = group.value.count { it.currResult?.elected ?: false }
                                "$elected/$count"
                            }
                            "$label: $value"
                        }
                }
                head.merge(entries) { h, e -> "$h\n$e" }
            }
            return AllSeatsScreen(title, frame, altText)
        }

        data class Group<T> internal constructor(val name: Flow.Publisher<String>, val predicate: T.() -> Boolean)

        fun <T> group(name: Flow.Publisher<String>, predicate: T.() -> Boolean) = Group(name, predicate)

        fun <T> ofGrouped(
            prevWinner: Flow.Publisher<out Map<T, Party?>>,
            currResult: Flow.Publisher<out Map<T, PartyResult?>>,
            nameFunc: T.() -> String,
            numRows: Flow.Publisher<out Int>? = null,
            seatFilter: Flow.Publisher<out Set<T>?>? = null,
            partyChanges: Flow.Publisher<Map<Party, Party>>? = null,
            groups: List<Group<T>>,
            title: Flow.Publisher<out String?>,
        ): AllSeatsScreen {
            val outerPanel = JPanel()
            outerPanel.background = Color.WHITE
            outerPanel.border = EmptyBorder(-5, -5, -5, -5)
            val groupWinners = groups.map { (name, predicate) ->
                name to prevWinner.map { w -> w.filterKeys(predicate) }
            }
            val innerPanels = groupWinners.map { (name, groupPrevWinners) ->
                of(
                    prevWinner = groupPrevWinners,
                    currResult = currResult,
                    nameFunc = nameFunc,
                    header = name,
                    numRows = numRows,
                    seatFilter = seatFilter,
                    partyChanges = partyChanges,
                    title = null.asOneTimePublisher(),
                )
            }

            val columnsPerPanel = groupWinners.map { (_, groupPrevWinners) ->
                groupPrevWinners.run {
                    if (seatFilter == null) {
                        this
                    } else {
                        merge(seatFilter) { w, f ->
                            w.filterKeys { f == null || f.contains(it) }
                        }
                    }
                }.run {
                    if (numRows == null) {
                        map { w -> ceil(w.size.toDouble() / DEFAULT_ROWS).toInt() }
                    } else {
                        merge(numRows) { w, r ->
                            ceil(w.size.toDouble() / r).toInt()
                        }
                    }
                }
            }.combine()
            val layout = object : LayoutManager {
                var columnWidths = groups.map { 0 }

                override fun addLayoutComponent(name: String, comp: Component) {
                }

                override fun removeLayoutComponent(comp: Component) {
                }

                override fun preferredLayoutSize(parent: Container): Dimension {
                    return DEFAULT_SIZE
                }

                override fun minimumLayoutSize(parent: Container): Dimension {
                    return DEFAULT_SIZE
                }

                override fun layoutContainer(parent: Container) {
                    val totalColumns = columnWidths.sum().coerceAtLeast(1)
                    var columnsSoFar = 0
                    val width = parent.width.toDouble() + 10
                    val height = parent.height + 10
                    innerPanels.forEachIndexed { index, panel ->
                        val columns = columnWidths[index]
                        val left = (width * columnsSoFar / totalColumns).roundToInt()
                        val right = (width * (columnsSoFar + columns) / totalColumns).roundToInt()
                        panel.location = Point(left - 5, -5)
                        panel.size = Dimension(right - left, height)
                        columnsSoFar += columns
                    }
                }
            }
            outerPanel.layout = layout
            columnsPerPanel.subscribe(
                Subscriber(
                    eventQueueWrapper {
                        layout.columnWidths = it
                        EventQueue.invokeLater {
                            outerPanel.invalidate()
                            outerPanel.revalidate()
                            outerPanel.repaint()
                        }
                    },
                ),
            )

            innerPanels.forEach { outerPanel.add(it) }
            val altText = innerPanels.zip(groupWinners).map { (panel, group) ->
                group.second.run {
                    if (seatFilter == null) {
                        this
                    } else {
                        merge(seatFilter) { w, f -> w.filterKeys { f == null || f.contains(it) } }
                    }
                }.merge(panel.altText) { w, t -> t.takeIf { w.isNotEmpty() } }
            }
                .combine()
                .merge(title) { p, t -> listOfNotNull(t, p.filterNotNull().joinToString("\n\n")).joinToString("\n\n") }
            return AllSeatsScreen(title, outerPanel, altText)
        }
    }
}
