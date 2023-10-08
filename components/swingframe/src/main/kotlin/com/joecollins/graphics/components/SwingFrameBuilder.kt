package com.joecollins.graphics.components

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

    private class SelfPublishingPrevCurrPct<POC : PartyOrCoalition> {
        private var prevPct: Map<out POC, Double> = HashMap()
        private var currPct: Map<out POC, Double> = HashMap()
        var fromParty: POC? = null
        var toParty: POC? = null
        var swing = 0.0
        private var partyFilter: Collection<POC>? = null

        fun setPrevPct(prevPct: Map<out POC, Double>) {
            synchronized(this) {
                this.prevPct = prevPct
                setProperties()
            }
        }

        fun setCurrPct(currPct: Map<out POC, Double>) {
            synchronized(this) {
                this.currPct = currPct
                setProperties()
            }
        }

        fun setPartyFilter(partyFilter: Collection<POC>) {
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
    }

    fun <POC : PartyOrCoalition> prevCurr(
        prev: Flow.Publisher<out Map<out POC, Number>>,
        curr: Flow.Publisher<out Map<out POC, Number>>,
        partyOrder: Comparator<POC>,
        selectedParties: Flow.Publisher<out Collection<POC>>? = null,
        range: Flow.Publisher<out Number>? = null,
        header: Flow.Publisher<out String?>,
        progress: Flow.Publisher<out String?>? = null,
    ): SwingFrame {
        return prevCurr(prev, curr, partyOrder, false, selectedParties, range, header, progress)
    }

    private fun <POC : PartyOrCoalition, C : Map<out POC, Number>, P : Map<out POC, Number>> prevCurr(
        prev: Flow.Publisher<out P>,
        curr: Flow.Publisher<out C>,
        partyOrder: Comparator<POC>,
        normalised: Boolean,
        selectedParties: Flow.Publisher<out Collection<POC>>?,
        range: Flow.Publisher<out Number>? = null,
        header: Flow.Publisher<out String?>,
        progress: Flow.Publisher<out String?>? = null,
    ): SwingFrame {
        val prevCurr = SelfPublishingPrevCurrPct<POC>()
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
                if (this.fromParty == null || this.toParty == null) {
                    Color.LIGHT_GRAY
                } else {
                    ComparatorUtils.max(fromParty, toParty, partyOrder).color
                }
            },
            {
                val fromParty = fromParty
                val toParty = toParty
                if (fromParty == null || toParty == null) {
                    Color.LIGHT_GRAY
                } else {
                    (if (ComparatorUtils.max(fromParty, toParty, partyOrder) == fromParty) toParty else fromParty).color
                }
            },
            {
                if (fromParty == null || toParty == null) {
                    0
                } else {
                    if (ComparatorUtils.max(fromParty, toParty, partyOrder) == fromParty) {
                        -1 * swing
                    } else {
                        swing
                    }
                }
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

    fun <POC : PartyOrCoalition> prevCurrNormalised(
        prevPublisher: Flow.Publisher<out Map<out POC, Double>>,
        currPublisher: Flow.Publisher<out Map<out POC, Double>>,
        partyOrder: Comparator<POC>,
        range: Flow.Publisher<out Number>? = null,
        header: Flow.Publisher<out String?>,
        progress: Flow.Publisher<out String?>? = null,
    ): SwingFrame {
        return prevCurr(prevPublisher, currPublisher, partyOrder, true, null, range, header, progress)
    }

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
    ): SwingFrame {
        return SwingFrame(
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
}
