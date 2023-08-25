package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.BarFrame
import com.joecollins.graphics.components.GraphicsFrame
import com.joecollins.graphics.components.SwingFrame
import com.joecollins.graphics.components.SwingFrameBuilder
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.toParty
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.Shape
import java.text.DecimalFormat
import java.util.concurrent.Flow
import javax.swing.JPanel

class PartyQuotasPanel private constructor(
    label: Flow.Publisher<out String?>,
    private val seatFrame: JPanel,
    private val secondarySeatFrame: JPanel?,
    private val changeFrame: JPanel?,
    private val leftSupplementaryFrame: JPanel?,
    private val rightSupplementaryFrame: JPanel?,
    altText: Flow.Publisher<String>,
) : GenericPanel(
    run {
        val panel = JPanel()
        panel.layout = BasicResultLayout()
        panel.background = Color.WHITE
        panel.add(seatFrame, BasicResultLayout.MAIN)
        if (secondarySeatFrame != null) panel.add(secondarySeatFrame, BasicResultLayout.PREF)
        if (changeFrame != null) panel.add(changeFrame, BasicResultLayout.DIFF)
        if (leftSupplementaryFrame != null) panel.add(leftSupplementaryFrame, BasicResultLayout.SWING)
        if (rightSupplementaryFrame != null) panel.add(rightSupplementaryFrame, BasicResultLayout.MAP)
        panel
    },
    label,
    altText,
) {

    companion object {
        fun <P : PartyOrCoalition> partyQuotas(
            quotas: Flow.Publisher<out Map<out P, Double>>,
            totalSeats: Flow.Publisher<out Int>,
            header: Flow.Publisher<out String?>,
            subhead: Flow.Publisher<out String?>,
        ): PartyQuotasPanel.PartyQuotaScreenBuilder<P> {
            return PartyQuotasPanel.PartyQuotaScreenBuilder(
                quotas,
                totalSeats,
                header,
                subhead,
            )
        }
    }

    class PartyQuotaScreenBuilder<P : PartyOrCoalition>(
        private val quotas: Flow.Publisher<out Map<out P, Double>>,
        private val totalSeats: Flow.Publisher<out Int>,
        private val header: Flow.Publisher<out String?>,
        private val subhead: Flow.Publisher<out String?>,
    ) {
        private var prevQuotas: Flow.Publisher<out Map<out P, Double>>? = null
        private var changeHeader: Flow.Publisher<out String>? = null
        private var progressLabel: Flow.Publisher<out String?>? = null

        private var swingCurrVotes: Flow.Publisher<out Map<out P, Int>>? = null
        private var swingPrevVotes: Flow.Publisher<out Map<out P, Int>>? = null
        private var swingComparator: Comparator<P>? = null
        private var swingHeader: Flow.Publisher<out String?>? = null
        private var swingRange: Flow.Publisher<Double>? = null

        private var mapBuilder: MapBuilder<*>? = null

        fun withPrev(
            prevQuotas: Flow.Publisher<out Map<out P, Double>>,
            changeHeader: Flow.Publisher<out String>,
        ): PartyQuotaScreenBuilder<P> {
            this.prevQuotas = prevQuotas
            this.changeHeader = changeHeader
            return this
        }

        fun withSwing(
            currVotes: Flow.Publisher<out Map<out P, Int>>,
            prevVotes: Flow.Publisher<out Map<out P, Int>>,
            comparator: Comparator<P>,
            header: Flow.Publisher<out String?>,
            swingRange: Flow.Publisher<Double> = 0.1.asOneTimePublisher(),
        ): PartyQuotaScreenBuilder<P> {
            this.swingCurrVotes = currVotes
            this.swingPrevVotes = prevVotes
            this.swingComparator = comparator
            this.swingHeader = header
            this.swingRange = swingRange
            return this
        }

        fun <T> withPartyMap(
            shapes: Flow.Publisher<out Map<T, Shape>>,
            selectedShape: Flow.Publisher<out T>,
            leadingParty: Flow.Publisher<out P?>,
            focus: Flow.Publisher<out List<T>?>,
            header: Flow.Publisher<out String?>,
        ): PartyQuotaScreenBuilder<P> {
            mapBuilder = MapBuilder.singleResult(shapes, selectedShape, leadingParty.map { PartyResult.elected(it?.toParty()) }, focus, header)
            return this
        }

        fun withProgressLabel(progressLabel: Flow.Publisher<out String?>): PartyQuotaScreenBuilder<P> {
            this.progressLabel = progressLabel
            return this
        }

        fun build(textHeader: Flow.Publisher<out String>): PartyQuotasPanel {
            return PartyQuotasPanel(
                textHeader,
                createFrame(),
                null,
                createDiffFrame(),
                createSwingFrame(),
                mapBuilder?.createMapFrame(),
                createAltText(textHeader),
            )
        }

        private fun createFrame(): BarFrame {
            return BarFrame(
                barsPublisher = quotas.map { q ->
                    q.entries.asSequence()
                        .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }
                        .map {
                            BarFrame.Bar(
                                leftText = it.key.name.uppercase(),
                                rightText = DecimalFormat("0.00").format(it.value) + " QUOTAS",
                                series = listOf(it.key.color to it.value),
                            )
                        }
                        .toList()
                },
                headerPublisher = header,
                subheadTextPublisher = subhead,
                maxPublisher = totalSeats,
                linesPublisher = totalSeats.map { lines -> (1 until lines).map { BarFrame.Line(it, "$it QUOTA${if (it == 1) "" else "S"}") } },
                headerLabelsPublisher = progressLabel?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
            )
        }

        private fun createDiffFrame(): BarFrame? {
            if (prevQuotas == null) return null
            return BarFrame(
                barsPublisher = quotas.merge(prevQuotas!!) { curr, prev ->
                    if (curr.isEmpty()) {
                        emptyList()
                    } else {
                        sequenceOf(
                            curr.asSequence().sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }.map { it.key },
                            prev.keys.asSequence().filter { !curr.containsKey(it) }.sortedByDescending { it.overrideSortOrder ?: 0 },
                        )
                            .flatten()
                            .distinct()
                            .map { party ->
                                val diff = (curr[party] ?: 0.0) - (prev[party] ?: 0.0)
                                BarFrame.Bar(
                                    leftText = party.abbreviation.uppercase(),
                                    rightText = DecimalFormat("+0.00;-0.00").format(diff),
                                    series = listOf(party.color to diff),
                                )
                            }
                            .toList()
                    }
                },
                headerPublisher = changeHeader ?: (null as String?).asOneTimePublisher(),
                maxPublisher = 1.asOneTimePublisher(),
                minPublisher = (-1).asOneTimePublisher(),
            )
        }

        private fun createSwingFrame(): SwingFrame? {
            return swingHeader?.let { header ->
                val prev = swingPrevVotes!!
                val curr = swingCurrVotes!!
                SwingFrameBuilder.prevCurr(
                    prev,
                    curr,
                    swingComparator!!,
                )
                    .withHeader(header)
                    .withRange(swingRange!!)
                    .build()
            }
        }

        private fun createAltText(textHeader: Flow.Publisher<out String?>): Flow.Publisher<String> {
            val combineHeaderAndSubhead: (String?, String?) -> String? = { h, s ->
                if (h == null) {
                    s
                } else if (s == null) {
                    h
                } else {
                    "$h, $s"
                }
            }
            val mainHeader = header.merge(subhead, combineHeaderAndSubhead)
                .merge(changeHeader ?: null.asOneTimePublisher()) { h, c ->
                    if (c == null) {
                        h
                    } else {
                        "${h ?: ""} ($c)"
                    }
                }.merge(progressLabel ?: null.asOneTimePublisher()) { h, p ->
                    if (p == null) {
                        h
                    } else {
                        "${h ?: ""} [$p]"
                    }
                }
            val mainEntries = quotas.merge(prevQuotas ?: null.asOneTimePublisher()) { curr, prev ->
                val entries = curr.entries
                    .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }
                    .joinToString("") { e ->
                        "\n${e.key.name.uppercase()}: ${DecimalFormat("0.00").format(e.value)} QUOTAS" +
                            (if (prev == null) "" else " (${DecimalFormat("+0.00;-0.00").format(e.value - (prev[e.key] ?: 0.0))})")
                    }
                val others = prev?.filterKeys { curr.isNotEmpty() && !curr.containsKey(it) }
                    ?.entries
                    ?.sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }
                    ?.joinToString("") { "\n${it.key.name.uppercase()}: - (-${DecimalFormat("0.00").format(it.value)})" }
                    ?: ""
                entries + others
            }
            val mainText = mainHeader.merge(mainEntries) { h, e ->
                if (h == null) {
                    e
                } else {
                    ("$h$e")
                }
            }
            val swingText = if (swingPrevVotes == null || swingCurrVotes == null) {
                null
            } else {
                run {
                    val prev = swingPrevVotes!!
                    val curr = swingCurrVotes!!
                    SwingFrameBuilder.prevCurr(
                        prev,
                        curr,
                        swingComparator!!,
                    ).buildBottomText()
                }?.merge(swingHeader ?: null.asOneTimePublisher()) { t, h ->
                    if (h == null) {
                        t
                    } else {
                        "$h: $t"
                    }
                }
            }
            return textHeader.merge(mainText) { h, m -> "$h\n\n$m" }
                .merge(swingText ?: null.asOneTimePublisher()) { h, s ->
                    if (s == null) {
                        h
                    } else {
                        "$h\n\n$s"
                    }
                }
        }
    }
}
