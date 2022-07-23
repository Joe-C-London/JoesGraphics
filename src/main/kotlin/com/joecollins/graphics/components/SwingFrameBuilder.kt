package com.joecollins.graphics.components

import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import org.apache.commons.collections4.ComparatorUtils
import java.awt.Color
import java.text.DecimalFormat
import java.util.Comparator
import java.util.concurrent.Flow
import kotlin.math.sign

class SwingFrameBuilder {
    private class SwingProperties {
        var leftColor: Color = Color.BLACK
            set(value) {
                field = value
                leftColorPublisher.submit(leftColor)
            }
        val leftColorPublisher = Publisher(leftColor)

        var rightColor: Color = Color.BLACK
            set(value) {
                field = value
                rightColorPublisher.submit(rightColor)
            }
        val rightColorPublisher = Publisher(rightColor)

        var value: Number = 0
            set(value) {
                field = value
                valuePublisher.submit(value)
            }
        val valuePublisher = Publisher(value)

        var text: String = ""
            set(value) {
                field = value
                textPublisher.submit(text)
            }
        val textPublisher = Publisher(text)

        var bottomColor: Color = Color.BLACK
            set(value) {
                field = value
                bottomColorPublisher.submit(bottomColor)
            }
        val bottomColorPublisher = Publisher(bottomColor)
    }

    private var rangePublisher: Flow.Publisher<out Number>? = null
    private var headerPublisher: Flow.Publisher<out String?>? = null
    private var leftColorPublisher: Flow.Publisher<out Color>? = null
    private var rightColorPublisher: Flow.Publisher<out Color>? = null
    private var valuePublisher: Flow.Publisher<out Number>? = null
    private var bottomColorPublisher: Flow.Publisher<out Color>? = null
    private var bottomTextPublisher: Flow.Publisher<out String>? = null

    private val props = SwingProperties()
    private var neutralColor = Color.BLACK

    fun withRange(rangePublisher: Flow.Publisher<out Number>): SwingFrameBuilder {
        this.rangePublisher = rangePublisher
        return this
    }

    fun withNeutralColor(neutralColorPublisher: Flow.Publisher<out Color>): SwingFrameBuilder {
        neutralColorPublisher.subscribe(
            Subscriber {
                neutralColor = it
                if (props.value.toDouble() == 0.0) {
                    props.bottomColor = it
                }
            }
        )
        return this
    }

    fun withHeader(headerPublisher: Flow.Publisher<out String?>): SwingFrameBuilder {
        this.headerPublisher = headerPublisher
        return this
    }

    fun build(): SwingFrame {
        return SwingFrame(
            headerPublisher = headerPublisher ?: (null as String?).asOneTimePublisher(),
            rangePublisher = rangePublisher ?: 1.asOneTimePublisher(),
            valuePublisher = valuePublisher ?: 0.asOneTimePublisher(),
            leftColorPublisher = leftColorPublisher ?: Color.BLACK.asOneTimePublisher(),
            rightColorPublisher = rightColorPublisher ?: Color.BLACK.asOneTimePublisher(),
            bottomColorPublisher = bottomColorPublisher ?: Color.BLACK.asOneTimePublisher(),
            bottomTextPublisher = bottomTextPublisher ?: null.asOneTimePublisher()
        )
    }

    private class SelfPublishingPrevCurrPct {
        private var prevPct: Map<Party, Double> = HashMap()
        private var currPct: Map<Party, Double> = HashMap()
        var fromParty: Party? = null
        var toParty: Party? = null
        var swing = 0.0

        fun setPrevPct(prevPct: Map<Party, Double>) {
            this.prevPct = prevPct
            setProperties()
        }

        fun setCurrPct(currPct: Map<Party, Double>) {
            this.currPct = currPct
            setProperties()
        }

        val publisher = Publisher(this)

        fun setProperties() {
            synchronized(this) {
                fromParty = prevPct.entries
                    .filter { e -> !e.value.isNaN() }
                    .maxByOrNull { it.value }
                    ?.key
                toParty = currPct.entries
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
    }

    companion object {
        fun prevCurr(
            prevPublisher: Flow.Publisher<out Map<Party, Number>>,
            currPublisher: Flow.Publisher<out Map<Party, Number>>,
            partyOrder: Comparator<Party>
        ): SwingFrameBuilder {
            return prevCurr(prevPublisher, currPublisher, partyOrder, false)
        }

        private fun <C : Map<Party, Number>, P : Map<Party, Number>> prevCurr(
            prevPublisher: Flow.Publisher<out P>,
            currPublisher: Flow.Publisher<out C>,
            partyOrder: Comparator<Party>,
            normalised: Boolean
        ): SwingFrameBuilder {
            val prevCurr = SelfPublishingPrevCurrPct()
            val toPctFunc = { votes: Map<Party, Number> ->
                val total: Double = if (normalised) 1.0 else votes.values.sumOf { it.toDouble() }
                votes.mapValues { e -> e.value.toDouble() / total }
            }
            prevPublisher.subscribe(Subscriber { prevCurr.setPrevPct(toPctFunc(it)) })
            currPublisher.subscribe(Subscriber { prevCurr.setCurrPct(toPctFunc(it)) })
            val ret = basic(
                prevCurr.publisher,
                {
                    val fromParty = it.fromParty
                    val toParty = it.toParty
                    if (fromParty == null || toParty == null) {
                        Color.LIGHT_GRAY
                    } else {
                        ComparatorUtils.max(fromParty, toParty, partyOrder).color
                    }
                },
                {
                    val fromParty = it.fromParty
                    val toParty = it.toParty
                    if (fromParty == null || toParty == null) {
                        Color.LIGHT_GRAY
                    } else {
                        (if (ComparatorUtils.max(fromParty, toParty, partyOrder) == fromParty) toParty else fromParty).color
                    }
                },
                {
                    val fromParty = it.fromParty
                    val toParty = it.toParty
                    if (fromParty == null || toParty == null) {
                        0
                    } else {
                        (
                            it.swing
                                * sign(
                                    if (ComparatorUtils.max(fromParty, toParty, partyOrder)
                                        == fromParty
                                    ) -1.0 else 1.0
                                )
                            )
                    }
                },
                {
                    val fromParty = it.fromParty
                    val toParty = it.toParty
                    val swing = it.swing
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
                }
            )
                .withRange(0.1.asOneTimePublisher())
                .withNeutralColor(Color.LIGHT_GRAY.asOneTimePublisher())
            return ret
        }

        fun prevCurrNormalised(
            prevPublisher: Flow.Publisher<out Map<Party, Double>>,
            currPublisher: Flow.Publisher<out Map<Party, Double>>,
            partyOrder: Comparator<Party>
        ): SwingFrameBuilder {
            return prevCurr(prevPublisher, currPublisher, partyOrder, true)
        }

        fun <T> basic(
            items: Flow.Publisher<out T>,
            leftColorFunc: (T) -> Color,
            rightColorFunc: (T) -> Color,
            valueFunc: (T) -> Number,
            textFunc: (T) -> String
        ): SwingFrameBuilder {
            val builder = SwingFrameBuilder()
            val props = builder.props
            builder.leftColorPublisher = props.leftColorPublisher
            builder.rightColorPublisher = props.rightColorPublisher
            builder.valuePublisher = props.valuePublisher
            builder.bottomColorPublisher = props.bottomColorPublisher
            builder.bottomTextPublisher = props.textPublisher
            items.subscribe(
                Subscriber {
                    props.leftColor = leftColorFunc(it)
                    props.rightColor = rightColorFunc(it)
                    props.value = valueFunc(it)
                    props.text = textFunc(it)
                    props.bottomColor = when {
                        props.value.toDouble() > 0 -> {
                            leftColorFunc(it)
                        }
                        props.value.toDouble() < 0 -> {
                            rightColorFunc(it)
                        }
                        else -> {
                            builder.neutralColor
                        }
                    }
                }
            )
            return builder
        }
    }
}
