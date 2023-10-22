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
    {
        layout = BasicResultLayout()
        background = Color.WHITE
        add(seatFrame, BasicResultLayout.MAIN)
        if (secondarySeatFrame != null) add(secondarySeatFrame, BasicResultLayout.PREF)
        if (changeFrame != null) add(changeFrame, BasicResultLayout.DIFF)
        if (leftSupplementaryFrame != null) add(leftSupplementaryFrame, BasicResultLayout.SWING)
        if (rightSupplementaryFrame != null) add(rightSupplementaryFrame, BasicResultLayout.MAP)
    },
    label,
    altText,
) {

    companion object {
        fun <P : PartyOrCoalition> partyQuotas(
            curr: Curr<P>.() -> Unit,
            change: (Change<P>.() -> Unit)? = null,
            swing: (Swing<P>.() -> Unit)? = null,
            map: MapPanel<*, P>? = null,
            title: Flow.Publisher<out String>,
        ): PartyQuotasPanel {
            val currProps = Curr<P>().apply(curr)
            val changeProps = change?.let { Change<P>().apply(it) }
            val swingProps = swing?.let { Swing<P>().apply(it) }
            return PartyQuotasPanel(
                title,
                createFrame(currProps),
                null,
                createDiffFrame(currProps, changeProps),
                createSwingFrame(swingProps),
                map?.frame,
                createAltText(currProps, changeProps, swingProps, title),
            )
        }

        fun <T, P : PartyOrCoalition> createMap(map: MapPanel<T, P>.() -> Unit) = MapPanel<T, P>().apply(map)

        private fun <P : PartyOrCoalition> createFrame(curr: Curr<P>): BarFrame {
            return BarFrame(
                barsPublisher = curr.quotas.map { q ->
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
                headerPublisher = curr.header,
                subheadTextPublisher = curr.subhead,
                maxPublisher = curr.totalSeats,
                linesPublisher = curr.totalSeats.map { lines -> (1 until lines).map { BarFrame.Line(it, "$it QUOTA${if (it == 1) "" else "S"}") } },
                headerLabelsPublisher = curr.progressLabel?.map { mapOf(GraphicsFrame.HeaderLabelLocation.RIGHT to it) },
            )
        }

        private fun <P : PartyOrCoalition> createDiffFrame(curr: Curr<P>, change: Change<P>?): BarFrame? {
            if (change == null) return null
            return BarFrame(
                barsPublisher = curr.quotas.merge(change.prevQuotas) { currQuotas, prevQuotas ->
                    if (currQuotas.isEmpty()) {
                        emptyList()
                    } else {
                        sequenceOf(
                            currQuotas.asSequence().sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }.map { it.key },
                            prevQuotas.keys.asSequence().filter { !currQuotas.containsKey(it) }.sortedByDescending { it.overrideSortOrder ?: 0 },
                        )
                            .flatten()
                            .distinct()
                            .map { party ->
                                val diff = (currQuotas[party] ?: 0.0) - (prevQuotas[party] ?: 0.0)
                                BarFrame.Bar(
                                    leftText = party.abbreviation.uppercase(),
                                    rightText = DecimalFormat("+0.00;-0.00").format(diff),
                                    series = listOf(party.color to diff),
                                )
                            }
                            .toList()
                    }
                },
                headerPublisher = change.header,
                maxPublisher = 1.asOneTimePublisher(),
                minPublisher = (-1).asOneTimePublisher(),
            )
        }

        private fun <P : PartyOrCoalition> createSwingFrame(swing: Swing<P>?): SwingFrame? {
            return swing?.run {
                SwingFrameBuilder.prevCurr(
                    prev = prevVotes,
                    curr = currVotes,
                    partyOrder = order,
                    range = range,
                    header = header,
                )
            }
        }

        private fun <P : PartyOrCoalition> createAltText(
            curr: Curr<P>,
            change: Change<P>?,
            swing: Swing<P>?,
            title: Flow.Publisher<out String?>,
        ): Flow.Publisher<String> {
            val combineHeaderAndSubhead: (String?, String?) -> String? = { h, s ->
                if (h == null) {
                    s
                } else if (s == null) {
                    h
                } else {
                    "$h, $s"
                }
            }
            val mainHeader = curr.header.merge(curr.subhead, combineHeaderAndSubhead)
                .run {
                    if (change == null) {
                        this
                    } else {
                        merge(change.header) { h, c ->
                            if (c == null) {
                                h
                            } else {
                                "${h ?: ""} ($c)"
                            }
                        }
                    }
                }.run {
                    if (curr.progressLabel == null) {
                        this
                    } else {
                        merge(curr.progressLabel!!) { h, p ->
                            if (p == null) {
                                h
                            } else {
                                "${h ?: ""} [$p]"
                            }
                        }
                    }
                }
            val mainEntries = curr.quotas.merge(change?.prevQuotas ?: null.asOneTimePublisher()) { currQuotas, prevQuotas ->
                val entries = currQuotas.entries
                    .sortedByDescending { it.key.overrideSortOrder?.toDouble() ?: it.value }
                    .joinToString("") { e ->
                        "\n${e.key.name.uppercase()}: ${DecimalFormat("0.00").format(e.value)} QUOTAS" +
                            (if (prevQuotas == null) "" else " (${DecimalFormat("+0.00;-0.00").format(e.value - (prevQuotas[e.key] ?: 0.0))})")
                    }
                val others = prevQuotas?.filterKeys { currQuotas.isNotEmpty() && !currQuotas.containsKey(it) }
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
            val swingText = if (swing == null) {
                null
            } else {
                createSwingFrame(swing)?.altText
                    ?.merge(swing.header) { t, h ->
                        if (h == null) {
                            t
                        } else {
                            "$h: $t"
                        }
                    }
            }
            return title.merge(mainText) { h, m -> "$h\n\n$m" }
                .merge(swingText ?: null.asOneTimePublisher()) { h, s ->
                    if (s == null) {
                        h
                    } else {
                        "$h\n\n$s"
                    }
                }
        }
    }

    class Curr<P : PartyOrCoalition> internal constructor() {
        lateinit var quotas: Flow.Publisher<out Map<out P, Double>>
        lateinit var totalSeats: Flow.Publisher<out Int>
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        var progressLabel: Flow.Publisher<out String?>? = null
    }

    class Change<P : PartyOrCoalition> internal constructor() {
        lateinit var prevQuotas: Flow.Publisher<out Map<out P, Double>>
        lateinit var header: Flow.Publisher<out String?>
    }

    class Swing<P : PartyOrCoalition> internal constructor() {
        lateinit var currVotes: Flow.Publisher<out Map<out P, Int>>
        lateinit var prevVotes: Flow.Publisher<out Map<out P, Int>>
        lateinit var order: Comparator<P>
        lateinit var header: Flow.Publisher<out String?>
        var range: Flow.Publisher<Double>? = null
    }

    class MapPanel<T, P : PartyOrCoalition> internal constructor() {
        lateinit var shapes: Flow.Publisher<out Map<T, Shape>>
        lateinit var selectedShape: Flow.Publisher<out T>
        lateinit var leadingParty: Flow.Publisher<out P?>
        lateinit var focus: Flow.Publisher<out List<T>?>
        lateinit var header: Flow.Publisher<out String?>

        internal val frame by lazy {
            MapBuilder.singleResult(
                shapes = shapes,
                selectedShape = selectedShape,
                leadingParty = leadingParty.map { PartyResult.elected(it?.toParty()) },
                focus = focus,
                header = header,
            )
        }
    }
}
