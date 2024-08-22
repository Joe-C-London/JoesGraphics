package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.models.general.CanOverrideSortOrder
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel

class CandidateListingScreen private constructor(
    header: Flow.Publisher<out String?>,
    candidatesPanel: JPanel,
    prevPanel: JPanel?,
    secondaryPrevPanel: JPanel?,
    mapPanel: JPanel?,
    altText: Flow.Publisher<String>,
) : GenericPanel(
    {
        layout = RightStackLayout()
        background = Color.WHITE
        add(candidatesPanel, RightStackLayout.WEST)
        if (prevPanel != null) {
            add(prevPanel, RightStackLayout.EAST)
        }
        if (secondaryPrevPanel != null) {
            add(secondaryPrevPanel, RightStackLayout.EAST)
        }
        if (mapPanel != null) {
            add(mapPanel, RightStackLayout.EAST)
        }
        if (setOf(prevPanel, secondaryPrevPanel, mapPanel).count { it != null } == 1) {
            add(JPanel().also { it.background = Color.WHITE }, RightStackLayout.EAST)
        }
    },
    header,
    altText,
) {

    companion object {

        fun <K> createMap(func: MapPanel<K>.() -> Unit) = MapPanel<K>().apply(func)

        fun of(
            candidates: CandidatesPanel<Candidate>.() -> Unit,
            prev: (PrevPanel<Party>.() -> Unit)? = null,
            secondaryPrev: (PrevPanel<Party>.() -> Unit)? = null,
            map: MapPanel<*>? = null,
            incumbentMarker: String? = null,
            showTwoColumns: Flow.Publisher<Boolean>? = null,
            title: Flow.Publisher<out String?>,
        ): CandidateListingScreen {
            val candidatesPanel = CandidatesPanel<Candidate>().apply(candidates)
            val prevPanel = prev?.let { PrevPanel<Party>().apply(it) }
            val secondaryPrevPanel = secondaryPrev?.let { PrevPanel<Party>().apply(it) }
            val combinedFunc: Candidate.() -> String = { name.uppercase() + (if (incumbentMarker != null && incumbent) " [$incumbentMarker]" else "") + " (${party.abbreviation})" }
            return CandidateListingScreen(
                title,
                createCandidatesPanel(
                    candidatesPanel,
                    showTwoColumns,
                    { name.uppercase() + (if (incumbentMarker != null && incumbent) " [$incumbentMarker]" else "") },
                    { party.name.uppercase() },
                    { party.color },
                    combinedFunc,
                ),
                prevPanel?.run {
                    createPrevPanel(
                        votes,
                        header,
                        subheadOrDefault,
                        { abbreviation },
                        { color },
                    )
                },
                secondaryPrevPanel?.run {
                    createPrevPanel(
                        votes,
                        header,
                        subheadOrDefault,
                        { abbreviation },
                        { color },
                    )
                },
                map?.createPanel(),
                createAltText(
                    title,
                    candidatesPanel,
                    prevPanel,
                    secondaryPrevPanel,
                    { abbreviation },
                    combinedFunc,
                ),
            )
        }

        fun ofNonPartisan(
            candidates: CandidatesPanel<NonPartisanCandidate>.() -> Unit,
            prev: (PrevPanel<NonPartisanCandidate>.() -> Unit)? = null,
            secondaryPrev: (PrevPanel<NonPartisanCandidate>.() -> Unit)? = null,
            map: MapPanel<*>? = null,
            showTwoColumns: Flow.Publisher<Boolean>? = null,
            title: Flow.Publisher<out String?>,
        ): CandidateListingScreen {
            val candidatesPanel = CandidatesPanel<NonPartisanCandidate>().apply(candidates)
            val prevPanel = prev?.let { PrevPanel<NonPartisanCandidate>().apply(it) }
            val secondaryPrevPanel = secondaryPrev?.let { PrevPanel<NonPartisanCandidate>().apply(it) }
            val combinedFunc: NonPartisanCandidate.() -> String = { fullName.uppercase() + (if (description == null) "" else " (${description!!.uppercase()})") }
            return CandidateListingScreen(
                title,
                createCandidatesPanel(
                    candidatesPanel,
                    showTwoColumns,
                    { fullName.uppercase() },
                    { description?.uppercase() ?: "" },
                    { color },
                    combinedFunc,
                ),
                prevPanel?.run {
                    createPrevPanel(
                        votes,
                        header,
                        subheadOrDefault,
                        { surname.uppercase() },
                        { color },
                    )
                },
                secondaryPrevPanel?.run {
                    createPrevPanel(
                        prevVotes = votes,
                        header = header,
                        subhead = subheadOrDefault,
                        prevLabel = { surname.uppercase() },
                        prevColor = { color },
                    )
                },
                map?.createPanel(),
                createAltText(
                    title,
                    candidatesPanel,
                    prevPanel,
                    secondaryPrevPanel,
                    { surname.uppercase() },
                    combinedFunc,
                ),
            )
        }

        private fun <CT> createCandidatesPanel(
            candidates: CandidatesPanel<CT>,
            showToColumns: Flow.Publisher<Boolean>?,
            leftLabel: CT.() -> String,
            rightLabel: CT.() -> String,
            color: CT.() -> Color,
            combinedLabel: CT.() -> String,
        ): BarFrame {
            return BarFrame(
                barsPublisher = if (showToColumns == null) {
                    candidates.list.mapElements {
                        BarFrame.Bar(
                            it.leftLabel(),
                            it.rightLabel(),
                            listOf(it.color() to 1.0),
                        )
                    }
                } else {
                    candidates.list.merge(showToColumns) { cList, show ->
                        if (show) {
                            val mid = cList.size / 2
                            val first = cList.take(mid)
                            val last = cList.drop(mid)
                            (0 until mid).map { idx ->
                                val left = first[idx]
                                val right = if (idx == last.size) null else last[idx]
                                val func = { c: CT -> c.combinedLabel() + " " }
                                BarFrame.Bar(
                                    func(left),
                                    right?.let(func) ?: "",
                                    listOf(
                                        left.color() to 0.49,
                                        Color.WHITE to 0.02,
                                        (right?.color() ?: Color.WHITE) to 0.49,
                                    ),
                                )
                            }
                        } else {
                            cList.map {
                                BarFrame.Bar(
                                    it.leftLabel(),
                                    it.rightLabel(),
                                    listOf(it.color() to 1.0),
                                )
                            }
                        }
                    }
                },
                headerPublisher = candidates.header,
                subheadTextPublisher = candidates.subhead,
                maxPublisher = 1.0.asOneTimePublisher(),
            )
        }

        private fun <PT : CanOverrideSortOrder> createPrevPanel(
            prevVotes: Flow.Publisher<out Map<PT, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            prevLabel: PT.() -> String,
            prevColor: PT.() -> Color,
        ): JPanel {
            return BarFrameBuilder.basic(
                barsPublisher = prevVotes.map { v -> createVoteBars(v, prevLabel, prevColor) },
                maxPublisher = (2.0 / 3).asOneTimePublisher(),
                headerPublisher = header,
                subheadPublisher = subhead,
            )
        }
        private fun <PT : CanOverrideSortOrder> createVoteBars(
            votes: Map<PT, Int>,
            prevLabel: PT.() -> String,
            prevColor: PT.() -> Color,
        ): List<BarFrameBuilder.BasicBar> {
            val total = votes.values.sum().toDouble().coerceAtLeast(1e-6)
            return votes.asSequence()
                .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                .map {
                    BarFrameBuilder.BasicBar(
                        it.key.prevLabel(),
                        it.key.prevColor(),
                        it.value / total,
                        if (votes.size == 1) "UNCONTESTED" else DecimalFormat("0.0%").format(it.value / total),
                    )
                }
                .toList()
        }

        private fun <CT, PT : CanOverrideSortOrder> createAltText(
            title: Flow.Publisher<out String>,
            candidates: CandidatesPanel<CT>,
            prev: PrevPanel<PT>?,
            secondaryPrev: PrevPanel<PT>?,
            prevLabel: PT.() -> String,
            combinedLabel: CT.() -> String,
        ): Flow.Publisher<String> {
            val candidatesTitle = candidates.header.merge(candidates.subhead) { h, s ->
                if (s.isNullOrBlank()) {
                    h
                } else {
                    "$h, $s"
                }
            }
            val candidatesText = candidates.list.map { c ->
                c.joinToString("\n") { it.combinedLabel() }
            }.merge(candidatesTitle) { cList, cTitle -> "$cTitle\n$cList" }
            var ret = title.merge(candidatesText) { t, c -> "$t\n\n$c" }
            if (prev != null) {
                val prevText = createPrevAltText(prev.votes, prev.header, prev.subheadOrDefault, prevLabel)
                ret = ret.merge(prevText) { r, p -> "$r\n\n$p" }
            }
            if (secondaryPrev != null) {
                val prevText = createPrevAltText(
                    secondaryPrev.votes,
                    secondaryPrev.header,
                    secondaryPrev.subheadOrDefault,
                    prevLabel,
                )
                ret = ret.merge(prevText) { r, p -> "$r\n\n$p" }
            }
            return ret
        }

        private fun <PT : CanOverrideSortOrder> createPrevAltText(
            prevVotes: Flow.Publisher<out Map<PT, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            prevLabel: PT.() -> String,
        ): Flow.Publisher<String> {
            val title = header.merge(subhead) { h, s ->
                if (s.isNullOrBlank()) {
                    h
                } else {
                    "$h, $s"
                }
            }
            return prevVotes.map { votes ->
                val total = votes.values.sum().toDouble()
                votes.entries.sortedByDescending { it.key.overrideSortOrder ?: it.value }
                    .joinToString("\n") { "${it.key.prevLabel()}: ${if (votes.size == 1) "UNCONTESTED" else DecimalFormat("0.0%").format(it.value / total)}" }
            }.merge(title) { v, t -> "$t\n$v" }
        }
    }

    class CandidatesPanel<CT> internal constructor() {
        lateinit var list: Flow.Publisher<out List<CT>>
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
    }

    class PrevPanel<PT> internal constructor() {
        lateinit var votes: Flow.Publisher<out Map<PT, Int>>
        lateinit var header: Flow.Publisher<out String?>
        var subhead: Flow.Publisher<out String?>? = null

        internal val subheadOrDefault by lazy { subhead ?: null.asOneTimePublisher() }
    }

    class MapPanel<K> internal constructor() {
        lateinit var shapes: Flow.Publisher<out Map<K, Shape>>
        lateinit var selectedShape: Flow.Publisher<out K>
        var focus: Flow.Publisher<out List<K>?>? = null
        var additionalHighlight: Flow.Publisher<out List<K>?>? = null
        lateinit var header: Flow.Publisher<out String>

        internal fun createPanel() = MapBuilder.singleResult(
            shapes = shapes,
            selectedShape = selectedShape,
            leadingParty = (null as PartyResult?).asOneTimePublisher(),
            focus = focus,
            additionalHighlight = additionalHighlight,
            header = header,
        )
    }
}
