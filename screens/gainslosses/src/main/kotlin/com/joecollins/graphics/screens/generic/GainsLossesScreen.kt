package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.math.max

class GainsLossesScreen private constructor(
    entries: Flow.Publisher<List<Entry>>,
    totalSeats: Flow.Publisher<Int>,
    progressLabel: Flow.Publisher<out String?>?,
    notes: Flow.Publisher<out String?>?,
    title: Flow.Publisher<out String?>,
) : GenericPanel(
    JPanel().apply {
        background = Color.WHITE
        layout = GridLayout(1, 2, 5, 5)
        border = EmptyBorder(5, 5, 5, 5)

        val limit = totalSeats.merge(entries) { t, e ->
            (listOf(t / 20) + e.map { it.totalSize }).max()
        }
        add(
            BarFrame(
                headerPublisher = "LOSSES".asOneTimePublisher(),
                barsPublisher = entries.mapElements { it.lossBar },
                minPublisher = limit.map { -it },
                maxPublisher = 0.asOneTimePublisher(),
                notesPublisher = notes?.map { it?.let { "" } },
            ),
        )
        add(
            BarFrame(
                headerPublisher = "GAINS".asOneTimePublisher(),
                barsPublisher = entries.mapElements { it.gainBar },
                minPublisher = 0.asOneTimePublisher(),
                maxPublisher = limit,
                notesPublisher = notes,
                headerLabelsPublisher = progressLabel?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
            ),
        )
    },
    title,
    title.run { if (progressLabel == null) map { t -> t to null } else merge(progressLabel) { t, p -> t to p } }
        .merge(entries) { (t, p), e ->
            "$t\n\nGAINS AND LOSSES${if (p == null) "" else " [$p]"}" +
                e.joinToString("") { "\n${it.altText}" }
        }.map { text -> { text } },
) {

    interface Entry {
        val lossBar: BarFrame.Bar
        val gainBar: BarFrame.Bar
        val altText: String
        val totalSize: Int
    }

    companion object {
        fun <T> of(
            prevWinner: Flow.Publisher<out Map<T, Party>>,
            currWinner: Flow.Publisher<out Map<T, Party?>>,
            seatFilter: Flow.Publisher<out Set<T>?>? = null,
            progressLabel: Flow.Publisher<out String?>? = null,
            partyChanges: Flow.Publisher<Map<Party, Party>>? = null,
            notes: Flow.Publisher<out String?>? = null,
            title: Flow.Publisher<out String?>,
        ): GainsLossesScreen {
            val entries: Flow.Publisher<List<Entry>> = extractParams(prevWinner, currWinner, seatFilter, partyChanges)
                .map { (curr, prev, filter) ->
                    val changes = extractChanges(prev, curr, { it }, filter)
                    val parties = extractParties(prev, curr, { it }, filter)
                    parties.map { party ->
                        val losses = changes.count { it.first == party }
                        val gains = changes.count { it.second == party }
                        object : Entry {
                            override val lossBar: BarFrame.Bar =
                                BarFrame.Bar.of(party.name.uppercase(), "$losses", listOf(party.color to -losses - 1e-12))
                            override val gainBar: BarFrame.Bar =
                                BarFrame.Bar.of(party.name.uppercase(), "$gains", listOf(party.color to gains + 1e-12))
                            override val altText: String = "${party.name.uppercase()}: $gains GAIN${if (gains == 1) "" else "S"}, $losses LOSS${if (losses == 1) "" else "ES"}"
                            override val totalSize: Int = max(gains, losses)
                        }
                    }
                }
            return GainsLossesScreen(entries, prevWinner.map { it.size }, progressLabel, notes, title)
        }

        fun <T> ofResult(
            prevWinner: Flow.Publisher<out Map<T, Party>>,
            currResult: Flow.Publisher<out Map<T, PartyResult?>>,
            seatFilter: Flow.Publisher<out Set<T>?>? = null,
            progressLabel: Flow.Publisher<out String?>? = null,
            partyChanges: Flow.Publisher<Map<Party, Party>>? = null,
            notes: Flow.Publisher<out String?>? = null,
            title: Flow.Publisher<out String?>,
        ): GainsLossesScreen {
            val entries: Flow.Publisher<List<Entry>> = extractParams(prevWinner, currResult, seatFilter, partyChanges)
                .map { (curr, prev, filter) ->
                    val changes = extractChanges(prev, curr, { it.leader }, filter)
                    val parties = extractParties(prev, curr, { it.leader }, filter)
                    parties.map { party ->
                        val losses = changes.filter { it.first == party }
                            .groupingBy { it.second.elected }
                            .eachCount()
                        val gains = changes.filter { it.second.leader == party }
                            .groupingBy { it.second.elected }
                            .eachCount()
                        object : Entry {
                            override val lossBar: BarFrame.Bar =
                                BarFrame.Bar.of(party.name.uppercase(), "${losses[true] ?: 0}/${losses.values.sum()}", listOf(party.color to -(losses[true] ?: 0) - 1e-12, ColorUtils.lighten(party.color) to -(losses[false] ?: 0)))
                            override val gainBar: BarFrame.Bar =
                                BarFrame.Bar.of(party.name.uppercase(), "${gains[true] ?: 0}/${gains.values.sum()}", listOf(party.color to (gains[true] ?: 0) + 1e-12, ColorUtils.lighten(party.color) to (gains[false] ?: 0)))
                            override val altText: String = "${party.name.uppercase()}: ${gains[true] ?: 0}/${gains.values.sum()} GAIN${if (gains.values.sum() == 1) "" else "S"}, ${losses[true] ?: 0}/${losses.values.sum()} LOSS${if (losses.values.sum() == 1) "" else "ES"}"
                            override val totalSize: Int = max(gains.values.sum(), losses.values.sum())
                        }
                    }
                }
            return GainsLossesScreen(entries, prevWinner.map { it.size }, progressLabel, notes, title)
        }

        private fun <T, R> extractParams(
            prevWinner: Flow.Publisher<out Map<T, Party>>,
            currWinner: Flow.Publisher<out Map<T, R?>>,
            seatFilter: Flow.Publisher<out Set<T>?>?,
            partyChanges: Flow.Publisher<Map<Party, Party>>?,
        ) = currWinner.merge(prevWinner) { curr, prev -> curr to prev }
            .run {
                if (seatFilter == null) {
                    map { (curr, prev) -> Triple(curr, prev, null) }
                } else {
                    merge(seatFilter) { (curr, prev), filter -> Triple(curr, prev, filter) }
                }
            }
            .run {
                if (partyChanges == null) {
                    this
                } else {
                    merge(partyChanges) { (curr, prev, filter), changes -> Triple(curr, prev.mapValues { changes[it.value] ?: it.value }, filter) }
                }
            }

        private fun <T, R> extractChanges(
            prev: Map<T, Party>,
            curr: Map<T, R?>,
            toParty: (R) -> Party,
            filter: Set<T>?,
        ) = prev.entries.mapNotNull { (k, p) ->
            val c = curr[k]
            when {
                filter != null && !filter.contains(k) -> null
                c == null -> null
                toParty(c) == p -> null
                else -> p to c
            }
        }

        private fun <T, R> extractParties(
            prev: Map<T, Party>,
            curr: Map<T, R?>,
            toParty: (R) -> Party,
            filter: Set<T>?,
        ): List<Party> {
            val currParties = curr
                .filterKeys { filter == null || filter.contains(it) }
                .values
                .filterNotNull()
                .groupingBy(toParty)
                .eachCount()
            val prevParties = prev
                .filterKeys { filter == null || filter.contains(it) }
                .filterKeys { curr[it] != null }
                .values
            val parties = (currParties.keys + prevParties)
                .associateWith { currParties[it] ?: 0 }
                .entries
                .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                .map { it.key }
            return parties
        }
    }
}
