package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.AltTextProvider
import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel

private const val TICK = "\u2611"
private const val CROSS = "\u2612"

class SingleTransferrableResultScreen private constructor(
    label: Flow.Publisher<out String?>,
    private val candidateFrame: JPanel,
    private val partyFrame: JPanel?,
    private val prevFrame: JPanel?,
    private val mapFrame: JPanel?,
    override val altText: Flow.Publisher<String?>
) : GenericPanel(
    run {
        val panel = JPanel()
        panel.layout = BasicResultLayout()
        panel.background = Color.WHITE
        panel.add(candidateFrame, BasicResultLayout.MAIN)
        if (partyFrame != null) panel.add(partyFrame, BasicResultLayout.DIFF)
        if (prevFrame != null) panel.add(prevFrame, BasicResultLayout.SWING)
        if (mapFrame != null) panel.add(mapFrame, BasicResultLayout.MAP)
        panel
    },
    label
),
    AltTextProvider {

    companion object {

        fun withCandidates(
            candidateVotes: Flow.Publisher<out Map<Candidate, Number?>>,
            quota: Flow.Publisher<out Number?>,
            elected: Flow.Publisher<out List<Pair<Candidate, Int>>>,
            excluded: Flow.Publisher<out List<Candidate>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            incumbentMarker: String = ""
        ): Builder {
            return Builder(
                candidateVotes,
                quota,
                elected,
                excluded,
                header,
                subhead,
                incumbentMarker
            )
        }
    }

    class Builder(
        private val candidateVotes: Flow.Publisher<out Map<Candidate, Number?>>,
        private val quota: Flow.Publisher<out Number?>,
        private val elected: Flow.Publisher<out List<Pair<Candidate, Int>>>,
        private val excluded: Flow.Publisher<out List<Candidate>>,
        private val candidateHeader: Flow.Publisher<out String?>,
        private val candidateSubhead: Flow.Publisher<out String?>,
        private val incumbentMarker: String
    ) {

        private var totalSeats: Flow.Publisher<out Int>? = null
        private var partyHeader: Flow.Publisher<out String?>? = null

        private var prevSeats: Flow.Publisher<out Map<Party, Int>>? = null
        private var prevHeader: Flow.Publisher<out String?>? = null

        private var mapBuilder: MapBuilder<*>? = null

        fun withPartyTotals(
            totalSeats: Flow.Publisher<out Int>,
            partyHeader: Flow.Publisher<out String?>
        ): Builder {
            this.totalSeats = totalSeats
            this.partyHeader = partyHeader
            return this
        }

        fun withPrevSeats(
            prevSeats: Flow.Publisher<out Map<Party, Int>>,
            prevHeader: Flow.Publisher<out String?>
        ): Builder {
            this.prevSeats = prevSeats
            this.prevHeader = prevHeader
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out Party?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>
        ): Builder {
            mapBuilder = MapBuilder(shapes, selectedShape, leadingParty.map { PartyResult.elected(it) }, focus, header)
            return this
        }

        fun build(title: Flow.Publisher<out String>): SingleTransferrableResultScreen {
            return SingleTransferrableResultScreen(
                title,
                createCandidatesPanel(),
                createPartiesPanel(),
                createPrevPanel(),
                mapBuilder?.createMapFrame(),
                createAltText(title)
            )
        }

        private fun createAltText(title: Flow.Publisher<out String>): Flow.Publisher<String?> {
            val candidateTop = candidateHeader.merge(candidateSubhead) { h, s ->
                if (h.isNullOrEmpty()) {
                    s
                } else if (s.isNullOrEmpty()) {
                    h
                } else {
                    "$h, $s"
                }
            }
            val status = elected.merge(excluded) { el, ex -> el to ex }
            val candidateEntries = candidateVotes.merge(quota) { v, q -> v to q }.merge(status) { (votes, q), (el, ex) ->
                val candidateFunc: (Candidate) -> String = { c -> "${c.name.uppercase()}${if (c.incumbent) " $incumbentMarker" else ""} (${c.party.abbreviation})" }
                val prevElected = el.filter { !votes.containsKey(it.first) }
                    .joinToString("\n") { (c, r) -> "${candidateFunc(c)}: ELECTED IN $r $TICK" }
                val currRound = votes.entries
                    .sortedByDescending { it.value?.toDouble() ?: 0.0 }
                    .joinToString("\n") { (c, v) ->
                        "${candidateFunc(c)}: ${
                        if (v == null) {
                            "WAITING..."
                        } else {
                            "${if (v is Int) DecimalFormat("#,##0").format(v.toInt()) else DecimalFormat("#,##0.00").format(v.toDouble())}${if (q == null) "" else " (${DecimalFormat("0.00").format(v.toDouble() / q.toDouble())})"}"
                        }
                        }${if (el.any { it.first == c }) " $TICK" else if (ex.contains(c)) " $CROSS" else ""}"
                    } +
                    (q?.let { "\nQUOTA: ${if (it is Int) DecimalFormat("#,##0").format(it.toInt()) else DecimalFormat("#,##0.00").format(it.toDouble())}" } ?: "")
                if (prevElected.isEmpty()) {
                    currRound
                } else if (currRound.isEmpty()) {
                    prevElected
                } else {
                    "$prevElected\n$currRound"
                }
            }
            val candidate = candidateTop.merge(candidateEntries) { t, e ->
                if (t == null) e
                else "$t\n$e"
            }

            val partyEntries = candidateVotes.merge(quota) { v, q -> v to q }.merge(status) { (v, q), (el, _) ->
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
            val party = partyHeader?.merge(partyEntries) { h, e ->
                if (e == null) {
                    h
                } else {
                    h + e
                }
            }

            val prevSeats = prevHeader?.merge(prevSeats ?: emptyMap<Party, Int>().asOneTimePublisher()) { h, s ->
                h + s.entries
                    .sortedByDescending { it.value }
                    .joinToString("") { "\n${it.key.abbreviation}: ${it.value}" }
            }

            return title.merge(candidate) { t, c -> "$t\n\n$c" }
                .merge(party ?: (null as String?).asOneTimePublisher()) { t, p -> if (p == null) t else "$t\n\n$p" }
                .merge(prevSeats ?: (null as String?).asOneTimePublisher()) { t, p -> if (p == null) t else "$t\n\n$p" }
        }

        private fun createCandidatesPanel(): JPanel {
            val votesQuota = candidateVotes.merge(quota) { v, q -> v to q }
            val inOut = elected.merge(excluded) { el, ex -> el to ex }
            return BarFrameBuilder.basic(
                votesQuota.merge(inOut) { (votes, quota), (elected, excluded) ->
                    val electedCandidates = elected.map { it.first }
                    val alreadyElectedSequence = elected.asSequence()
                        .filter { !votes.containsKey(it.first) }
                        .map {
                            BarFrameBuilder.BasicBar(
                                label = it.first.name.uppercase() + (if (it.first.incumbent) " $incumbentMarker" else "") + " (${it.first.party.abbreviation.uppercase()})",
                                valueLabel = "ELECTED IN ${it.second}",
                                shape = ImageGenerator.createTickShape(),
                                value = 0,
                                color = it.first.party.color
                            )
                        }
                    val thisRoundSequence = votes.entries.asSequence()
                        .sortedByDescending { it.value?.toDouble() ?: -1.0 }
                        .map {
                            BarFrameBuilder.BasicBar(
                                label = it.key.name.uppercase() + (if (it.key.incumbent) " $incumbentMarker" else "") + " (${it.key.party.abbreviation.uppercase()})",
                                valueLabel = if (it.value == null) "WAITING..." else (formatString(it.value!!) + (if (quota == null) "" else (" (" + formatString(it.value!!.toDouble() / quota.toDouble()) + ")"))),
                                color = it.key.party.color,
                                value = (it.value ?: 0),
                                shape = when {
                                    electedCandidates.contains(it.key) -> ImageGenerator.createTickShape()
                                    excluded.contains(it.key) -> ImageGenerator.createCrossShape()
                                    else -> null
                                }
                            )
                        }
                    sequenceOf(alreadyElectedSequence, thisRoundSequence)
                        .flatten()
                        .toList()
                }
            )
                .withHeader(candidateHeader)
                .withSubhead(candidateSubhead)
                .withMax(quota.map { (it?.toDouble() ?: 1.0) * 2 })
                .withLines(quota.map { if (it == null) emptyList() else listOf(it) }) { "QUOTA: " + formatString(it) }
                .build()
        }

        private fun createPartiesPanel(): JPanel? {
            if (partyHeader == null) {
                return null
            }
            val quotaAndElected = quota.merge(elected.map { e -> e.map { it.first } }) { q, e -> q to e }
            return BarFrameBuilder.basic(
                candidateVotes.merge(quotaAndElected) { votes, (quota, elected) ->
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
                            BarFrameBuilder.BasicBar(
                                label = it.key.name.uppercase(),
                                color = it.key.color,
                                value = it.value,
                                valueLabel = formatString(it.value)
                            )
                        }
                }
            )
                .withHeader(partyHeader!!)
                .withMax(totalSeats!!)
                .withLines(totalSeats!!.map { (1 until it).toList() }) { i -> "$i QUOTA${if (i == 1) "" else "S"}" }
                .build()
        }

        private fun createPrevPanel(): JPanel? {
            if (prevHeader == null) {
                return null
            }
            return BarFrameBuilder.basic(
                prevSeats!!.map { prev ->
                    prev.entries
                        .sortedByDescending { it.value }
                        .map {
                            BarFrameBuilder.BasicBar(
                                label = it.key.abbreviation.uppercase(),
                                color = it.key.color,
                                value = it.value
                            )
                        }
                }
            )
                .withMax(prevSeats!!.map { prev -> prev.values.sum() / 2 })
                .withHeader(prevHeader!!)
                .build()
        }

        private fun formatString(value: Number): String {
            return if (value is Int || value is Long) {
                DecimalFormat("#,##0").format(value)
            } else {
                DecimalFormat("#,##0.00").format(value)
            }
        }
    }
}
