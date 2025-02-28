package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.MultiSummaryFrame
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.util.concurrent.Flow

class TopThreePlacesScreen private constructor(
    places: Flow.Publisher<Map<Party, Places>>,
    header: Flow.Publisher<out String?>,
    progressLabel: Flow.Publisher<out String?>? = null,
    notes: Flow.Publisher<out String?>? = null,
    title: Flow.Publisher<out String?>,
) : GenericPanel(
    MultiSummaryFrame(
        headerPublisher = header,
        progressLabel = progressLabel,
        notesPublisher = notes,
        rowsPublisher = places.map { p ->
            listOf(MultiSummaryFrame.Row("", listOf("FIRST", "SECOND", "THIRD").map { Color.WHITE to it })) +
                p.entries.sortedByDescending { it.value }
                    .map { (party, seats) ->
                        MultiSummaryFrame.Row(
                            party.name.uppercase(),
                            listOf(seats.first, seats.second, seats.third).map { party.color to it.toString() },
                        )
                    }
        },
    ).let { pad(it) },
    title,
    header.run { if (progressLabel == null) this else merge(progressLabel) { h, p -> if (p == null) h else "$h [$p]" } }
        .merge(title) { h, t -> if (t == null) h else "$t\n\n$h" }
        .merge(places) { h, p ->
            h + p.entries.sortedByDescending { it.value }
                .joinToString("") { (party, places) -> "\n${party.name.uppercase()}: FIRST ${places.first}, SECOND ${places.second}, THIRD ${places.third}" }
        },
) {
    data class Places(val first: Int = 0, val second: Int = 0, val third: Int = 0) : Comparable<Places> {

        override fun compareTo(other: Places): Int {
            if (first != other.first) return first.compareTo(other.first)
            if (second != other.second) return second.compareTo(other.second)
            return third.compareTo(other.third)
        }

        constructor() : this(0, 0, 0)
        fun merge(other: Places): Places = Places(first + other.first, second + other.second, third + other.third)
    }

    companion object {

        fun of(
            votes: Flow.Publisher<out Collection<Map<Party, Int>>>,
            header: Flow.Publisher<out String?>,
            progressLabel: Flow.Publisher<out String?>? = null,
            notes: Flow.Publisher<out String?>? = null,
            title: Flow.Publisher<out String?>,
        ): TopThreePlacesScreen = TopThreePlacesScreen(
            convertToPlaces(votes) { it },
            header,
            progressLabel,
            notes,
            title,
        )

        fun ofCandidates(
            votes: Flow.Publisher<out Collection<Map<Candidate, Int>>>,
            header: Flow.Publisher<out String?>,
            progressLabel: Flow.Publisher<out String?>? = null,
            notes: Flow.Publisher<out String?>? = null,
            title: Flow.Publisher<out String?>,
        ): TopThreePlacesScreen = TopThreePlacesScreen(
            convertToPlaces(votes) { it.party },
            header,
            progressLabel,
            notes,
            title,
        )

        private fun <K> convertToPlaces(votes: Flow.Publisher<out Collection<Map<K, Int>>>, toParty: (K) -> Party) = votes.map { list ->
            list.flatMap { v ->
                v.entries.asSequence()
                    .filter { it.value > 0 }
                    .sortedByDescending { it.value }
                    .mapIndexed { index, entry -> index to entry }
                    .take(3)
                    .map { (place, entry) ->
                        toParty(entry.key) to when (place) {
                            0 -> Places(first = 1)
                            1 -> Places(second = 1)
                            2 -> Places(third = 1)
                            else -> Places()
                        }
                    }
                    .toList()
            }.groupingBy { it.first }
                .fold(Places()) { a, e -> a.merge(e.second) }
        }
    }
}
