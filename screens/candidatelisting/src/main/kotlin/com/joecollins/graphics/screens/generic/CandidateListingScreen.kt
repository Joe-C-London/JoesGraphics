package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.ImageGenerator
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.BarFrame.Bar.Companion.withIcon
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.CanOverrideSortOrder
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.mapElements
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CandidateListingScreen private constructor(
    header: Flow.Publisher<out String?>,
    candidatesPanel: JPanel,
    prevPanel: JPanel?,
    secondaryPrevPanel: JPanel?,
    mapPanel: JPanel?,
    altText: Flow.Publisher<(Int) -> String>,
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

        fun of(
            candidates: CandidatesPanel<Candidate>.() -> Unit,
            prev: (PrevPanel<Party>.() -> Unit)? = null,
            secondaryPrev: (PrevPanel<Party>.() -> Unit)? = null,
            map: SingleNoResultMap<*>? = null,
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
                    { name.uppercase() },
                    { party.name.uppercase() },
                    { party.color },
                    combinedFunc,
                    { if (incumbentMarker != null && incumbent) ImageGenerator.createFilledBoxedTextShape(incumbentMarker) else null },
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
                map?.mapFrame,
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
            incumbentMarker: String? = null,
            map: SingleNoResultMap<*>? = null,
            showTwoColumns: Flow.Publisher<Boolean>? = null,
            title: Flow.Publisher<out String?>,
        ): CandidateListingScreen {
            val candidatesPanel = CandidatesPanel<NonPartisanCandidate>().apply(candidates)
            val prevPanel = prev?.let { PrevPanel<NonPartisanCandidate>().apply(it) }
            val secondaryPrevPanel = secondaryPrev?.let { PrevPanel<NonPartisanCandidate>().apply(it) }
            val combinedFunc: NonPartisanCandidate.() -> String = { fullName.uppercase() + (if (description == null) "" else " (${description!!.uppercase()})") + (if (incumbent && incumbentMarker != null) " [$incumbentMarker]" else "") }
            return CandidateListingScreen(
                title,
                createCandidatesPanel(
                    candidatesPanel,
                    showTwoColumns,
                    { fullName.uppercase() },
                    { description?.uppercase() ?: "" },
                    { color },
                    combinedFunc,
                    { if (incumbentMarker != null && incumbent) ImageGenerator.createFilledBoxedTextShape(incumbentMarker) else null },
                ),
                prevPanel?.run {
                    createPrevPanel(
                        votes,
                        header,
                        subheadOrDefault,
                        { shortDisplayName.uppercase() },
                        { color },
                    )
                },
                secondaryPrevPanel?.run {
                    createPrevPanel(
                        prevVotes = votes,
                        header = header,
                        subhead = subheadOrDefault,
                        prevLabel = { shortDisplayName.uppercase() },
                        prevColor = { color },
                    )
                },
                map?.mapFrame,
                createAltText(
                    title,
                    candidatesPanel,
                    prevPanel,
                    secondaryPrevPanel,
                    { shortDisplayName.uppercase() },
                    combinedFunc,
                ),
            )
        }

        private fun <CT> createCandidatesPanel(
            candidates: CandidatesPanel<CT>,
            showTwoColumns: Flow.Publisher<Boolean>?,
            leftLabel: CT.() -> String,
            rightLabel: CT.() -> String,
            color: CT.() -> Color,
            combinedLabel: CT.() -> String,
            shape: CT.() -> Shape?,
        ): JPanel {
            if (showTwoColumns == null) {
                return BarFrame(
                    barsPublisher = candidates.list.mapElements {
                        BarFrame.Bar.of(
                            it.leftLabel().withIcon(it.shape()),
                            it.rightLabel(),
                            listOf(it.color() to 1.0),
                        )
                    },
                    headerPublisher = candidates.header,
                    subheadTextPublisher = candidates.subhead,
                    maxPublisher = 1.0.asOneTimePublisher(),
                )
            }
            val panel = JPanel().apply {
                layout = BorderLayout()
                background = Color.WHITE
                val subheadLabel = FontSizeAdjustingLabel("").also { label ->
                    label.font = StandardFont.readBoldFont(16)
                    candidates.subhead.subscribe(Subscriber(eventQueueWrapper { label.text = if (it.isNullOrBlank()) " " else it }))
                }
                add(subheadLabel, BorderLayout.NORTH)
                showTwoColumns.subscribe(
                    Subscriber(
                        eventQueueWrapper {
                            if (subheadLabel.isVisible != it) {
                                if (it) add(subheadLabel, BorderLayout.NORTH) else remove(subheadLabel)
                            }
                            subheadLabel.isVisible = it
                        },
                    ),
                )
                add(
                    JPanel().apply {
                        layout = GridLayout(1, 0, 5, 5)
                        background = Color.WHITE
                        border = EmptyBorder(-1, -1, -1, -1)
                        val left = BarFrame(
                            headerPublisher = null.asOneTimePublisher(),
                            subheadTextPublisher = candidates.subhead.merge(showTwoColumns) { sub, two -> if (two) null else sub },
                            borderColorPublisher = Color.WHITE.asOneTimePublisher(),
                            barsPublisher = candidates.list.merge(showTwoColumns) { cList, two ->
                                val list = if (two) {
                                    val mid = cList.size / 2
                                    cList.take(mid)
                                } else {
                                    cList
                                }
                                list.map {
                                    BarFrame.Bar.of(
                                        it.leftLabel().withIcon(it.shape()),
                                        it.rightLabel(),
                                        listOf(it.color() to 1.0),
                                    )
                                }
                            },
                        )
                        val right = BarFrame(
                            headerPublisher = null.asOneTimePublisher(),
                            subheadTextPublisher = candidates.subhead.merge(showTwoColumns) { sub, two -> if (two) null else sub },
                            borderColorPublisher = Color.WHITE.asOneTimePublisher(),
                            barsPublisher = candidates.list.merge(showTwoColumns) { cList, two ->
                                val list = if (two) {
                                    val mid = cList.size / 2
                                    cList.drop(mid)
                                } else {
                                    emptyList()
                                }
                                list.map {
                                    BarFrame.Bar.of(
                                        it.leftLabel().withIcon(it.shape()),
                                        it.rightLabel(),
                                        listOf(it.color() to 1.0),
                                    )
                                }
                            },
                        )
                        add(left)
                        add(right)
                        showTwoColumns.subscribe(
                            Subscriber(
                                eventQueueWrapper {
                                    if (right.isVisible != it) {
                                        if (it) add(right) else remove(right)
                                    }
                                    right.isVisible = it
                                },
                            ),
                        )
                    },
                    BorderLayout.CENTER,
                )
            }
            return object : GraphicsFrame(
                headerPublisher = candidates.header,
            ) {
                init {
                    addCenter(panel)
                }
            }
        }

        private fun <PT : CanOverrideSortOrder> createPrevPanel(
            prevVotes: Flow.Publisher<out Map<PT, Int>>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
            prevLabel: PT.() -> String,
            prevColor: PT.() -> Color,
        ): JPanel = BarFrameBuilder.basic(
            barsPublisher = prevVotes.map { v -> createVoteBars(v, prevLabel, prevColor) },
            maxPublisher = (2.0 / 3).asOneTimePublisher(),
            headerPublisher = header,
            subheadPublisher = subhead,
        )
        private fun <PT : CanOverrideSortOrder> createVoteBars(
            votes: Map<PT, Int>,
            prevLabel: PT.() -> String,
            prevColor: PT.() -> Color,
        ): List<BarFrameBuilder.BasicBar> {
            val total = votes.values.sum().toDouble().coerceAtLeast(1e-6)
            return votes.asSequence()
                .sortedByDescending { it.key.overrideSortOrder ?: it.value }
                .map {
                    BarFrameBuilder.BasicBar.of(
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
        ): Flow.Publisher<(Int) -> String> {
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
            return ret.map { text -> { text } }
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
}
