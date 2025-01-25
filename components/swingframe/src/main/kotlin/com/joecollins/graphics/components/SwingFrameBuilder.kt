package com.joecollins.graphics.components

import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import org.apache.commons.collections4.ComparatorUtils
import java.awt.Color
import java.text.DecimalFormat
import java.util.concurrent.Flow

object SwingFrameBuilder {

    private class SelfPublishingPrevCurrPct {
        private var prevPct: Map<out PartyOrCoalition, Double> = HashMap()
        private var currPct: Map<out PartyOrCoalition, Double> = HashMap()
        var fromParty: PartyOrCoalition? = null
        var toParty: PartyOrCoalition? = null
        var swing = 0.0
        private var partyFilter: Collection<PartyOrCoalition>? = null

        fun leftParty(comparator: List<PartyOrCoalition>): PartyOrCoalition? = when (rightParty(comparator)) {
            null -> null
            fromParty -> toParty
            else -> fromParty
        }

        fun rightParty(comparator: List<PartyOrCoalition>): PartyOrCoalition? {
            val from = fromParty
            val to = toParty
            if (from == null || to == null) return null
            return ComparatorUtils.max(from, to, comparator.comparator())
        }

        fun setPrevPct(prevPct: Map<out PartyOrCoalition, Double>) {
            synchronized(this) {
                this.prevPct = prevPct
                setProperties()
            }
        }

        fun setCurrPct(currPct: Map<out PartyOrCoalition, Double>) {
            synchronized(this) {
                this.currPct = currPct
                setProperties()
            }
        }

        fun setPartyFilter(partyFilter: Collection<PartyOrCoalition>) {
            synchronized(this) {
                this.partyFilter = partyFilter
                setProperties()
            }
        }

        val publisher = Publisher(this)

        fun setProperties() {
            fromParty = prevPct.entries
                .filter { e -> partyFilter?.contains(e.key) ?: true }
                .filter { e -> !e.value.isNaN() }
                .maxByOrNull { it.value }
                ?.key
            toParty = currPct.entries
                .filter { e -> partyFilter?.contains(e.key) ?: true }
                .filter { e -> e.key != fromParty }
                .filter { e -> !e.value.isNaN() }
                .maxByOrNull { it.value }
                ?.key
            if (fromParty != null && toParty != null) {
                val fromSwing = (currPct[fromParty] ?: 0.0) - (prevPct[fromParty] ?: 0.0)
                val toSwing = (currPct[toParty] ?: 0.0) - (prevPct[toParty] ?: 0.0)
                swing = (toSwing - fromSwing) / 2
            }
            if (swing < 0) {
                swing *= -1.0
                val temp = fromParty
                fromParty = toParty
                toParty = temp
            }
            publisher.submit(this)
        }

        private fun List<PartyOrCoalition>.comparator(): Comparator<PartyOrCoalition> = Comparator.comparing { party -> indexOf(party).takeUnless { it == -1 } ?: indexOf(Party.OTHERS) }
    }

    fun prevCurr(
        prev: Flow.Publisher<out Map<out PartyOrCoalition, Number>>,
        curr: Flow.Publisher<out Map<out PartyOrCoalition, Number>>,
        partyOrder: List<PartyOrCoalition>,
        selectedParties: Flow.Publisher<out Collection<PartyOrCoalition>>? = null,
        range: Flow.Publisher<out Number>? = null,
        header: Flow.Publisher<out String?>,
        progress: Flow.Publisher<out String?>? = null,
    ): SwingFrame = prevCurr(prev, curr, partyOrder, false, selectedParties, range, header, progress)

    private fun <POC : PartyOrCoalition, C : Map<out POC, Number>, P : Map<out POC, Number>> prevCurr(
        prev: Flow.Publisher<out P>,
        curr: Flow.Publisher<out C>,
        partyOrder: List<POC>,
        normalised: Boolean,
        selectedParties: Flow.Publisher<out Collection<POC>>?,
        range: Flow.Publisher<out Number>? = null,
        header: Flow.Publisher<out String?>,
        progress: Flow.Publisher<out String?>? = null,
    ): SwingFrame {
        val prevCurr = SelfPublishingPrevCurrPct()
        val toPctFunc = { votes: Map<out POC, Number> ->
            val total: Double = if (normalised) 1.0 else votes.values.sumOf { it.toDouble() }
            votes.mapValues { e -> e.value.toDouble() / total }
        }
        prev.subscribe(Subscriber { prevCurr.setPrevPct(toPctFunc(it)) })
        curr.subscribe(Subscriber { prevCurr.setCurrPct(toPctFunc(it)) })
        selectedParties?.subscribe(Subscriber { prevCurr.setPartyFilter(it) })
        val ret = basic(
            prevCurr.publisher,
            {
                rightParty(partyOrder)?.color ?: Color.LIGHT_GRAY
            },
            {
                leftParty(partyOrder)?.color ?: Color.LIGHT_GRAY
            },
            {
                when (rightParty(partyOrder)) {
                    null -> 0
                    fromParty -> -1
                    else -> 1
                } * swing
            },
            {
                val fromParty = fromParty
                val toParty = toParty
                val swing = swing
                if (fromParty == null || toParty == null) {
                    "NOT AVAILABLE"
                } else if (swing == 0.0) {
                    "NO SWING"
                } else {
                    (
                        DecimalFormat("0.0%").format(swing) +
                            " SWING " +
                            fromParty.abbreviation.uppercase() +
                            " TO " +
                            toParty.abbreviation.uppercase()
                        )
                }
            },
            neutralColor = Color.LIGHT_GRAY.asOneTimePublisher(),
            range = range ?: 0.1.asOneTimePublisher(),
            header = header,
            progress = progress,
        )
        return ret
    }

    fun prevCurrNormalised(
        prevPublisher: Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
        currPublisher: Flow.Publisher<out Map<out PartyOrCoalition, Double>>,
        partyOrder: List<PartyOrCoalition>,
        range: Flow.Publisher<out Number>? = null,
        header: Flow.Publisher<out String?>,
        progress: Flow.Publisher<out String?>? = null,
    ): SwingFrame = prevCurr(prevPublisher, currPublisher, partyOrder, true, null, range, header, progress)

    fun <T> basic(
        item: Flow.Publisher<out T>,
        leftColor: T.() -> Color,
        rightColor: T.() -> Color,
        value: T.() -> Number,
        text: T.() -> String,
        neutralColor: Flow.Publisher<out Color> = Color.BLACK.asOneTimePublisher(),
        range: Flow.Publisher<out Number>? = null,
        header: Flow.Publisher<out String?>,
        progress: Flow.Publisher<out String?>? = null,
    ): SwingFrame = SwingFrame(
        leftColorPublisher = item.map(leftColor),
        rightColorPublisher = item.map(rightColor),
        valuePublisher = item.map(value),
        bottomTextPublisher = item.map(text),
        bottomColorPublisher = item.merge(neutralColor) { it, neutral ->
            val v = it.value()
            when {
                v.toDouble() > 0 -> it.leftColor()
                v.toDouble() < 0 -> it.rightColor()
                else -> neutral
            }
        },
        rangePublisher = range ?: 1.asOneTimePublisher(),
        headerPublisher = header,
        progressPublisher = progress,
    )
}
