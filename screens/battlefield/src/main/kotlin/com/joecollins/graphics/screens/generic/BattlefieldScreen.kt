package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.BattlefieldFrame
import com.joecollins.graphics.utils.ColorUtils
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.ResultColorUtils.getColor
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow

class BattlefieldScreen private constructor(header: Flow.Publisher<out String?>, frame: BattlefieldFrame, altText: Flow.Publisher<String>) : GenericPanel(pad(frame), header, altText) {

    companion object {
        private const val DEFAULT_LIMIT = 0.80

        fun <T> build(
            prevVotes: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            results: Flow.Publisher<out Map<T, PartyResult?>>,
            leftParty: Flow.Publisher<Party>,
            rightParty: Flow.Publisher<Party>,
            bottomParty: Flow.Publisher<Party>,
            header: Flow.Publisher<out String?>,
            limit: Flow.Publisher<out Number>? = null,
            increment: Flow.Publisher<out Number>? = null,
            partySwings: Flow.Publisher<out Map<Party, Double>?>? = null,
            majorityLines: Flow.Publisher<Boolean>? = null,
            title: Flow.Publisher<out String?>,
        ): BattlefieldScreen {
            val allParties = leftParty.merge(rightParty) { l, r -> l to r }
                .merge(bottomParty) { (l, r), b -> Parties(l, r, b) }
            val frame = createBattlefield(
                prevVotes,
                results,
                allParties,
                majorityLines,
                header,
                limit,
                increment,
                partySwings,
            )
            val altText = createAltText(title, header, partySwings, allParties, prevVotes)
            return BattlefieldScreen(title, frame, altText)
        }

        private data class Parties(val left: Party, val right: Party, val bottom: Party) {
            fun asSequence() = sequenceOf(left, right, bottom)
        }

        private fun <T> createBattlefield(
            prevVotes: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            results: Flow.Publisher<out Map<T, PartyResult?>>,
            allParties: Flow.Publisher<Parties>,
            majorityLines: Flow.Publisher<Boolean>?,
            header: Flow.Publisher<out String?>,
            limit: Flow.Publisher<out Number>?,
            increment: Flow.Publisher<out Number>?,
            partySwings: Flow.Publisher<out Map<Party, Double>?>?,
        ): BattlefieldFrame {
            val dotsPublisher = prevVotes.merge(results) { prev, curr -> prev to curr }
                .merge(allParties) { (prev, curr), parties ->
                    prev.entries.mapNotNull { (key, votes) ->
                        val winner = votes.entries.maxByOrNull { it.value }?.key
                        if (!parties.asSequence().contains(winner)) return@mapNotNull null
                        val totalVotes = votes.values.sum().toDouble()
                        val pctByParty = parties.asSequence().associateWith { (votes[it] ?: 0) / totalVotes }
                        BattlefieldFrame.Dot(
                            -(pctByParty[parties.left] ?: 0.0),
                            -(pctByParty[parties.right] ?: 0.0),
                            -(pctByParty[parties.bottom] ?: 0.0),
                        ) to (curr[key].getColor(default = Color.LIGHT_GRAY))
                    }
                }

            val linesPublisher = if (majorityLines == null) {
                null
            } else
                prevVotes.merge(majorityLines) { prev, show -> prev to show }
                    .merge(
                        allParties.run {
                            if (limit == null) {
                                map { parties -> parties to DEFAULT_LIMIT }
                            } else {
                                merge(limit) { parties, limit -> parties to limit }
                            }
                        },
                    ) { (prev, show), (parties, limit) ->
                        if (!show) return@merge emptyList<Pair<BattlefieldFrame.Line, Color>>()

                        val prevPcts = prev.mapValues { e ->
                            val total = e.value.values.sum().toDouble()
                            e.value.mapValues { it.value / total }
                        }

                        parties.asSequence().mapNotNull { calculateLine(it, parties, prevPcts, limit.toDouble()) }
                            .toList()
                    }

            return BattlefieldFrame(
                headerPublisher = header,
                limitPublisher = limit ?: DEFAULT_LIMIT.asOneTimePublisher(),
                incrementPublisher = increment ?: 0.10.asOneTimePublisher(),
                dotsPublisher = dotsPublisher,
                linesPublisher = linesPublisher
                    ?: emptyList<Pair<BattlefieldFrame.Line, Color>>().asOneTimePublisher(),
                swingPublisher = partySwings?.merge(allParties) { swings, parties ->
                    if (swings == null) {
                        null
                    } else {
                        BattlefieldFrame.Dot(
                            swings[parties.left] ?: 0,
                            swings[parties.right] ?: 0,
                            swings[parties.bottom] ?: 0,
                        )
                    }
                } ?: null.asOneTimePublisher(),
                territoriesPublisher = allParties.map { parties ->
                    BattlefieldFrame.Territories(
                        ColorUtils.lighten(ColorUtils.lighten(ColorUtils.lighten(parties.left.color))),
                        ColorUtils.lighten(ColorUtils.lighten(ColorUtils.lighten(parties.right.color))),
                        ColorUtils.lighten(ColorUtils.lighten(ColorUtils.lighten(parties.bottom.color))),
                    )
                },
            )
        }

        private fun <T> calculateLine(
            party: Party,
            allParties: Parties,
            prevPcts: Map<T, Map<Party, Double>>,
            limit: Double,
        ): Pair<BattlefieldFrame.Line, Color>? {
            val otherParties = when (party) {
                allParties.left -> allParties.right to allParties.bottom
                allParties.right -> allParties.left to allParties.bottom
                allParties.bottom -> allParties.left to allParties.right
                else -> return null
            }

            val incrementSize = 0.01
            val numIncrements = (limit / incrementSize).toInt()

            val swings = (-numIncrements..numIncrements).asSequence().map { incrementSize * it }
                .map { otherSwing ->
                    calculateSwing(
                        party,
                        otherParties,
                        prevPcts,
                        otherSwing,
                    ).let { otherSwing to it }
                }
                .map { (otherSwing, partySwing) ->
                    when (party) {
                        allParties.left -> BattlefieldFrame.Dot(partySwing, -otherSwing, otherSwing)
                        allParties.right -> BattlefieldFrame.Dot(-otherSwing, partySwing, otherSwing)
                        allParties.bottom -> BattlefieldFrame.Dot(-otherSwing, otherSwing, partySwing)
                        else -> throw IllegalStateException("We shouldn't ever be here")
                    }
                }
                .toList()

            return BattlefieldFrame.Line(swings) to party.color
        }

        private fun <T> calculateSwing(
            party: Party,
            otherParties: Pair<Party, Party>,
            prevPcts: Map<T, Map<Party, Double>>,
            swing: Double,
        ): Double {
            val majority = prevPcts.size / 2 + 1
            val stage1Pcts = prevPcts.mapValues { (_, pcts) ->
                pcts.mapValues {
                    it.value + when (it.key) {
                        otherParties.first -> -swing
                        otherParties.second -> swing
                        else -> 0.0
                    }
                }
            }
            val stage1Wins = stage1Pcts.values.count { pct -> pct.entries.maxByOrNull { it.value }?.key == party }
            val stage2Needed = majority - stage1Wins - (if (stage1Wins < majority) 0 else 1)

            val pctNeeded = if (stage2Needed > 0) {
                stage1Pcts.values.mapNotNull { pct ->
                    val winner = pct.entries.maxByOrNull { it.value }!!
                    if (winner.key == party) return@mapNotNull null
                    val partyPct = (pct[party] ?: 0.0)
                    val requiresAdjustment = winner.key != otherParties.first && winner.key != otherParties.second
                    (winner.value - partyPct) * (if (requiresAdjustment) 1.5 else 1.0)
                }.sorted()[stage2Needed - 1]
            } else {
                stage1Pcts.values.mapNotNull { pct ->
                    val winner = pct.entries.maxByOrNull { it.value }!!
                    if (winner.key != party) return@mapNotNull null
                    val second = pct.entries.filter { it.key != party }.maxByOrNull { it.value }!!
                    val requiresAdjustment = second.key != otherParties.first && second.key != otherParties.second
                    (winner.value - second.value) * (if (requiresAdjustment) 1.5 else 1.0)
                }.sorted()[-stage2Needed - 1] * -1
            }
            return pctNeeded
        }

        private fun <T> createAltText(
            title: Flow.Publisher<out String?>,
            header: Flow.Publisher<out String?>,
            partySwings: Flow.Publisher<out Map<Party, Double>?>?,
            allParties: Flow.Publisher<Parties>,
            prevVotes: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        ): Flow.Publisher<String> {
            val topText = title.merge(header) { t, h -> sequenceOf(t, h).filterNotNull().joinToString("\n") }
            val swingsText = partySwings?.merge(allParties) { swings, parties ->
                if (swings == null) return@merge null
                val relevantChanges = sequenceOf(parties.left, parties.right, parties.bottom)
                    .associateWith { party -> swings[party] ?: 0.0 }
                    .entries
                    .sortedBy { it.value }
                sequenceOf(
                    relevantChanges[0] to relevantChanges[2],
                    relevantChanges[0] to relevantChanges[1],
                    relevantChanges[1] to relevantChanges[2],
                )
                    .map { (from, to) -> Triple(from.key, to.key, (to.value - from.value) / 2) }
                    .sortedByDescending { (_, _, swing) -> swing }
                    .joinToString("\n") { (from, to, swing) ->
                        "${to.abbreviation} ADVANCES ${DecimalFormat("0.0%").format(swing)} INTO ${from.abbreviation} TERRITORY"
                    }
            }
            val majorityText = (partySwings ?: null.asOneTimePublisher()).merge(prevVotes) { swings, prev ->
                val adjSeats = prev.values.groupingBy { votes ->
                    val total = votes.values.sum().toDouble()
                    val pcts = votes.mapValues { it.value / total + (swings?.get(it.key) ?: 0.0) }
                    pcts.maxBy { it.value }.key
                }.eachCount()
                val majority = prev.size / 2 + 1
                val winner = adjSeats.filterValues { it >= majority }.keys.firstOrNull()
                (winner?.abbreviation ?: "NO PARTY") +
                    (if (swings == null) " CURRENTLY HAS MAJORITY" else " WOULD HAVE MAJORITY ON UNIFORM ADVANCES")
            }
            return topText.run {
                if (swingsText == null) {
                    this
                } else {
                    merge(swingsText) { t, s -> sequenceOf(t, s).filterNotNull().joinToString("\n\n") }
                }
            }
                .merge(majorityText) { t, m -> "$t\n\n$m" }
        }
    }
}
