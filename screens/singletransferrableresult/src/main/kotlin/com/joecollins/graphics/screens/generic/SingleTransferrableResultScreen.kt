package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.ImageGenerator.combineHorizontal
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel

class SingleTransferrableResultScreen private constructor(
    label: Flow.Publisher<out String?>,
    private val candidateFrame: JPanel,
    private val partyFrame: JPanel?,
    private val prevFrame: JPanel?,
    private val mapFrame: JPanel?,
    altText: Flow.Publisher<String>,
) : GenericPanel(
    {
        layout = BasicResultLayout()
        background = Color.WHITE
        add(candidateFrame, BasicResultLayout.MAIN)
        if (partyFrame != null) add(partyFrame, BasicResultLayout.DIFF)
        if (prevFrame != null) add(prevFrame, BasicResultLayout.SWING)
        if (mapFrame != null) add(mapFrame, BasicResultLayout.MAP)
    },
    label,
    altText,
),
    AltTextProvider {

    companion object {

        fun of(
            candidateResults: CandidateResults.() -> Unit,
            partyTotals: (PartyTotals.() -> Unit)? = null,
            prevResults: (PrevResults.() -> Unit)? = null,
            map: AbstractMap<*>? = null,
            title: Flow.Publisher<out String>,
        ): SingleTransferrableResultScreen {
            val candidate = CandidateResults().apply(candidateResults)
            val party = partyTotals?.let { PartyTotals().apply(it) }
            val prev = prevResults?.let { PrevResults().apply(it) }
            return SingleTransferrableResultScreen(
                title,
                createCandidatesPanel(candidate),
                createPartiesPanel(candidate, party),
                createPrevPanel(prev),
                map?.mapFrame,
                createAltText(title, candidate, party, prev),
            )
        }

        private fun createCandidatesPanel(candidateResults: CandidateResults): JPanel {
            val votesQuota = candidateResults.votes.merge(candidateResults.quota) { v, q -> v to q }
            val inOut = candidateResults.elected.merge(candidateResults.excluded) { el, ex -> el to ex }
            val bars = votesQuota.merge(inOut) { (votes, quota), (elected, excluded) ->
                val electedCandidates = elected.map { it.first }
                val excludedCandidates = excluded.map { it.first }
                val alreadyElectedSequence = elected.asSequence()
                    .filter { !votes.containsKey(it.first) }
                    .map {
                        BarFrameBuilder.BasicBar.of(
                            label = it.first.name.uppercase() + " (${it.first.party.abbreviation.uppercase()})",
                            valueLabel = it.second,
                            shape = (
                                if (it.first.incumbent) {
                                    candidateResults.incumbentMarker?.let { inc -> ImageGenerator.createBoxedTextShape(inc) }
                                } else {
                                    null
                                }
                                ).combineHorizontal(ImageGenerator.createTickShape()),
                            value = 0,
                            color = it.first.party.color,
                        )
                    }
                val thisRoundSequence = votes.entries.asSequence()
                    .sortedByDescending { it.value?.toDouble() ?: -1.0 }
                    .map {
                        BarFrameBuilder.BasicBar.of(
                            label = it.key.name.uppercase() + " (${it.key.party.abbreviation.uppercase()})",
                            valueLabel = if (it.value == null) {
                                "WAITING..."
                            } else {
                                (formatString(it.value!!) + (if (quota == null) "" else (" (" + formatString(it.value!!.toDouble() / quota.toDouble()) + ")")))
                            },
                            color = it.key.party.color,
                            value = (it.value ?: 0),
                            shape = (
                                if (it.key.incumbent) {
                                    candidateResults.incumbentMarker?.let { inc -> ImageGenerator.createBoxedTextShape(inc) }
                                } else {
                                    null
                                }
                                ).combineHorizontal(
                                when {
                                    electedCandidates.contains(it.key) -> ImageGenerator.createTickShape()
                                    excludedCandidates.contains(it.key) -> ImageGenerator.createCrossShape()
                                    else -> null
                                },
                            ),
                        )
                    }
                val alreadyExcludedSequence = excluded.reversed().asSequence()
                    .filter { !votes.containsKey(it.first) }
                    .filter { candidateResults.showExcludedCandidates(it.first) }
                    .map {
                        BarFrameBuilder.BasicBar.of(
                            label = it.first.name.uppercase() + " (${it.first.party.abbreviation.uppercase()})",
                            valueLabel = it.second,
                            shape = (
                                if (it.first.incumbent) {
                                    candidateResults.incumbentMarker?.let { inc -> ImageGenerator.createBoxedTextShape(inc) }
                                } else {
                                    null
                                }
                                ).combineHorizontal(ImageGenerator.createCrossShape()),
                            value = 0,
                            color = it.first.party.color,
                        )
                    }
                sequenceOf(alreadyElectedSequence, thisRoundSequence, alreadyExcludedSequence)
                    .flatten()
                    .toList()
            }
            return BarFrameBuilder.basic(
                barsPublisher = bars,
                headerPublisher = candidateResults.header,
                rightHeaderLabelPublisher = candidateResults.progress,
                subheadPublisher = candidateResults.subhead,
                maxPublisher = candidateResults.quota.map { (it?.toDouble() ?: 1.0) * 2 },
                linesPublisher = BarFrameBuilder.Lines.of(candidateResults.quota.map { if (it == null) emptyList() else listOf(it) }) { "QUOTA: " + formatString(this) },
            )
        }

        private fun createPartiesPanel(
            candidateResults: CandidateResults,
            partyTotals: PartyTotals?,
        ): JPanel? {
            if (partyTotals == null) {
                return null
            }
            val quotaAndElected = candidateResults.quota.merge(candidateResults.elected.map { e -> e.map { it.first } }) { q, e -> q to e }
            val bars = candidateResults.votes.merge(quotaAndElected) { votes, (quota, elected) ->
                val electedEarlier = elected.filter { !votes.containsKey(it) }
                    .groupingBy { it.party }
                    .eachCount()
                val quotasThisRound = votes.entries
                    .filter { it.value != null }
                    .groupBy({ it.key.party }, { it.value })
                    .mapValues { e -> e.value.filterNotNull().sumOf { it.toDouble() } / (quota ?: 1.0).toDouble() }
                sequenceOf(electedEarlier.keys, quotasThisRound.keys)
                    .flatten()
                    .distinct()
                    .associateWith { (electedEarlier[it] ?: 0) + (quotasThisRound[it] ?: 0.0) }
                    .entries
                    .sortedByDescending { it.value }
                    .map {
                        BarFrameBuilder.BasicBar.of(
                            label = it.key.name.uppercase(),
                            color = it.key.color,
                            value = it.value,
                            valueLabel = formatString(it.value),
                        )
                    }
            }
            return BarFrameBuilder.basic(
                barsPublisher = bars,
                headerPublisher = partyTotals.header,
                maxPublisher = partyTotals.totalSeats,
                linesPublisher = BarFrameBuilder.Lines.of(partyTotals.totalSeats.map { (1 until it).toList() }) { "$this QUOTA${if (this == 1) "" else "S"}" },
            )
        }

        private fun createPrevPanel(prevResults: PrevResults?): JPanel? {
            if (prevResults == null) {
                return null
            }
            return BarFrameBuilder.basic(
                barsPublisher = prevResults.seats.map { prev ->
                    prev.entries
                        .sortedByDescending { it.value }
                        .map {
                            BarFrameBuilder.BasicBar.of(
                                label = it.key.abbreviation.uppercase(),
                                color = it.key.color,
                                value = it.value,
                            )
                        }
                },
                maxPublisher = prevResults.seats.map { prev -> prev.values.sum() / 2 },
                headerPublisher = prevResults.header,
            )
        }

        private fun formatString(value: Number): String = if (value is Int || value is Long) {
            DecimalFormat("#,##0").format(value)
        } else {
            DecimalFormat("#,##0.00").format(value)
        }

        private fun createAltText(
            title: Flow.Publisher<out String>,
            candidateResults: CandidateResults,
            partyTotals: PartyTotals?,
            prevResults: PrevResults?,
        ): Flow.Publisher<String> {
            val candidateTop = candidateResults.header.run {
                if (candidateResults.progress == null) {
                    this
                } else {
                    merge(candidateResults.progress!!) { h, p -> "$h [$p]" }
                }
            }.merge(candidateResults.subhead) { h, s ->
                if (h.isNullOrEmpty()) {
                    s
                } else if (s.isNullOrEmpty()) {
                    h
                } else {
                    "$h, $s"
                }
            }
            val status = candidateResults.elected.merge(candidateResults.excluded) { el, ex -> el to ex }
            val candidateEntries = candidateResults.votes.merge(candidateResults.quota) { v, q -> v to q }.merge(status) { (votes, q), (el, ex) ->
                val candidateFunc: (Candidate) -> String = { c -> "${c.name.uppercase()}${if (c.incumbent) " [${candidateResults.incumbentMarker}]" else ""} (${c.party.abbreviation})" }
                val prevElected = el.filter { !votes.containsKey(it.first) }
                    .joinToString("\n") { (c, r) -> "${candidateFunc(c)}: $r" }
                val currRound = votes.entries
                    .sortedByDescending { it.value?.toDouble() ?: 0.0 }
                    .joinToString("\n") { (c, v) ->
                        "${candidateFunc(c)}: ${
                            if (v == null) {
                                "WAITING..."
                            } else {
                                "${if (v is Int) {
                                    DecimalFormat("#,##0").format(v.toInt())
                                } else {
                                    DecimalFormat("#,##0.00").format(v.toDouble())
                                }}${if (q == null) {
                                    ""
                                } else {
                                    " (${
                                        DecimalFormat("0.00").format(v.toDouble() / q.toDouble())
                                    })"
                                }}"
                            }
                        }${if (el.any { it.first == c }) {
                            " ELECTED"
                        } else if (ex.any { it.first == c }) {
                            " EXCLUDED"
                        } else {
                            ""
                        }}"
                    }
                val quota = (q?.let { "QUOTA: ${if (it is Int) DecimalFormat("#,##0").format(it.toInt()) else DecimalFormat("#,##0.00").format(it.toDouble())}" } ?: "")
                val prevExcluded = ex.reversed().filter { !votes.containsKey(it.first) }.filter { candidateResults.showExcludedCandidates(it.first) }
                    .joinToString("\n") { (c, r) -> "${candidateFunc(c)}: $r" }
                listOf(prevElected, currRound, prevExcluded, quota)
                    .filter { it.isNotEmpty() }
                    .joinToString("\n")
            }
            val candidate = candidateTop.merge(candidateEntries) { t, e ->
                if (t == null) {
                    e
                } else {
                    "$t\n$e"
                }
            }

            val party = partyTotals?.header?.run {
                val partyEntries = candidateResults.votes.merge(candidateResults.quota) { v, q -> v to q }.merge(status) { (v, q), (el, _) ->
                    if (q == null) return@merge null
                    val electedByParty = el.filter { !v.containsKey(it.first) }
                        .groupingBy { it.first.party }
                        .eachCount()
                    val currentByParty = v.entries
                        .groupingBy { it.key.party }
                        .fold(0.0) { a, e -> a + (e.value?.toDouble() ?: 0.0) }
                        .mapValues { it.value / q.toDouble() }
                    val totalByParty = sequenceOf(electedByParty.keys, currentByParty.keys)
                        .flatten()
                        .distinct()
                        .associateWith { (electedByParty[it] ?: 0) + (currentByParty[it] ?: 0.0) }
                    totalByParty.entries
                        .sortedByDescending { it.value }
                        .joinToString("") { "\n${it.key.name.uppercase()}: ${DecimalFormat("0.00").format(it.value)}" }
                }
                merge(partyEntries) { h, e ->
                    if (e == null) {
                        h
                    } else {
                        h + e
                    }
                }
            }

            val prevSeats = prevResults?.header?.merge(prevResults.seats) { h, s ->
                h + s.entries
                    .sortedByDescending { it.value }
                    .joinToString("") { "\n${it.key.abbreviation}: ${it.value}" }
            }

            return title.merge(candidate) { t, c -> "$t\n\n$c" }
                .merge(party ?: (null as String?).asOneTimePublisher()) { t, p -> if (p == null) t else "$t\n\n$p" }
                .merge(prevSeats ?: (null as String?).asOneTimePublisher()) { t, p -> if (p == null) t else "$t\n\n$p" }
        }
    }

    class CandidateResults internal constructor() {
        lateinit var votes: Flow.Publisher<out Map<Candidate, Number?>>
        lateinit var quota: Flow.Publisher<out Number?>
        lateinit var elected: Flow.Publisher<out List<Pair<Candidate, String>>>
        lateinit var excluded: Flow.Publisher<out List<Pair<Candidate, String>>>
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        var progress: Flow.Publisher<String>? = null
        var incumbentMarker: String? = null
        var showExcludedCandidates: Candidate.() -> Boolean = { false }
    }

    class PartyTotals internal constructor() {
        lateinit var totalSeats: Flow.Publisher<out Int>
        lateinit var header: Flow.Publisher<out String?>
    }

    class PrevResults internal constructor() {
        lateinit var seats: Flow.Publisher<out Map<Party, Int>>
        lateinit var header: Flow.Publisher<out String?>
    }
}
