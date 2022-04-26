package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.BattlefieldFrame
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class BattlefieldScreen private constructor(title: JLabel, frame: BattlefieldFrame) : JPanel() {

    companion object {
        fun <T> build(
            prevVotesPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
            resultsPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
            leftPartyPublisher: Flow.Publisher<Party>,
            rightPartyPublisher: Flow.Publisher<Party>,
            bottomPartyPublisher: Flow.Publisher<Party>,
            headerPublisher: Flow.Publisher<out String?>
        ) = Builder(prevVotesPublisher, resultsPublisher, leftPartyPublisher, rightPartyPublisher, bottomPartyPublisher, headerPublisher)
    }

    class Builder<T>(
        private val prevVotesPublisher: Flow.Publisher<out Map<T, Map<Party, Int>>>,
        private val resultsPublisher: Flow.Publisher<out Map<T, PartyResult?>>,
        private val leftPartyPublisher: Flow.Publisher<Party>,
        private val rightPartyPublisher: Flow.Publisher<Party>,
        private val bottomPartyPublisher: Flow.Publisher<Party>,
        private val headerPublisher: Flow.Publisher<out String?>
    ) {
        private data class Parties(val left: Party, val right: Party, val bottom: Party) {
            fun asSequence() = sequenceOf(left, right, bottom)
        }

        private val allParties = leftPartyPublisher.merge(rightPartyPublisher) { l, r -> l to r }
            .merge(bottomPartyPublisher) { (l, r), b -> Parties(l, r, b) }

        private var limitPublisher: Flow.Publisher<out Number> = 0.80.asOneTimePublisher()
        private var incrementPublisher: Flow.Publisher<out Number> = 0.10.asOneTimePublisher()
        private var partySwingsPublisher: Flow.Publisher<out Map<Party, Double>?> = null.asOneTimePublisher()
        private var linesPublisher: Flow.Publisher<Boolean> = false.asOneTimePublisher()

        fun withLimit(limitPublisher: Flow.Publisher<out Number>): Builder<T> {
            this.limitPublisher = limitPublisher
            return this
        }

        fun withIncrement(incrementPublisher: Flow.Publisher<out Number>): Builder<T> {
            this.incrementPublisher = incrementPublisher
            return this
        }

        fun withPartySwings(partySwingsPublisher: Flow.Publisher<out Map<Party, Double>?>): Builder<T> {
            this.partySwingsPublisher = partySwingsPublisher
            return this
        }

        fun withLines(linesPublisher: Flow.Publisher<Boolean>): Builder<T> {
            this.linesPublisher = linesPublisher
            return this
        }

        fun build(title: Flow.Publisher<out String?>): BattlefieldScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            title.subscribe(Subscriber(Subscriber.eventQueueWrapper { headerLabel.text = it }))
            val swingometer = createBattlefield()
            return BattlefieldScreen(headerLabel, swingometer)
        }

        private fun createBattlefield(): BattlefieldFrame {
            val dotsPublisher = prevVotesPublisher.merge(resultsPublisher) { prev, curr -> prev to curr }
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
                        ) to (curr[key]?.color ?: Color.BLACK)
                    }
                }

            val linesPublisher = prevVotesPublisher.merge(linesPublisher) { prev, show -> prev to show }
                .merge(allParties.merge(limitPublisher) { parties, limit -> parties to limit }) { (prev, show), (parties, limit) ->
                    if (!show) return@merge emptyList<Pair<BattlefieldFrame.Line, Color>>()

                    val prevPcts = prev.mapValues { e ->
                        val total = e.value.values.sum().toDouble()
                        e.value.mapValues { it.value / total }
                    }

                    parties.asSequence().mapNotNull { calculateLine(it, parties, prevPcts, limit.toDouble()) }.toList()
                }

            return BattlefieldFrame(
                headerPublisher = headerPublisher,
                limitPublisher = limitPublisher,
                incrementPublisher = incrementPublisher,
                dotsPublisher = dotsPublisher,
                linesPublisher = linesPublisher,
                swingPublisher = partySwingsPublisher.merge(allParties) { swings, parties ->
                    if (swings == null) null
                    else BattlefieldFrame.Dot(swings[parties.left] ?: 0, swings[parties.right] ?: 0, swings[parties.bottom] ?: 0)
                }
            )
        }

        private fun calculateLine(party: Party, allParties: Parties, prevPcts: Map<T, Map<Party, Double>>, limit: Double): Pair<BattlefieldFrame.Line, Color>? {
            val otherParties = when (party) {
                allParties.left -> allParties.right to allParties.bottom
                allParties.right -> allParties.left to allParties.bottom
                allParties.bottom -> allParties.left to allParties.right
                else -> return null
            }

            val incrementSize = 0.01
            val numIncrements = (limit / incrementSize).toInt()

            val swings = (-numIncrements..numIncrements).asSequence().map { incrementSize * it }
                .mapNotNull { otherSwing -> calculateSwing(party, otherParties, prevPcts, otherSwing)?.let { otherSwing to it } }
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

        private fun calculateSwing(party: Party, otherParties: Pair<Party, Party>, prevPcts: Map<T, Map<Party, Double>>, swing: Double): Double? {
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
    }

    init {
        background = Color.WHITE
        layout = BorderLayout()
        add(title, BorderLayout.NORTH)
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.border = EmptyBorder(5, 5, 5, 5)
        panel.layout = GridLayout(1, 1)
        panel.add(frame)
        add(panel, BorderLayout.CENTER)
    }
}
