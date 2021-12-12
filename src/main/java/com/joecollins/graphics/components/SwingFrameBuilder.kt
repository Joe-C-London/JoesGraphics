package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.SwingFrameBuilder.SwingProperties.SwingProperty
import com.joecollins.models.general.Party
import org.apache.commons.collections4.ComparatorUtils
import java.awt.Color
import java.text.DecimalFormat
import java.util.Comparator
import java.util.HashMap
import kotlin.math.sign

class SwingFrameBuilder {
    private class SwingProperties : Bindable<SwingProperties, SwingProperty>() {
        enum class SwingProperty {
            LEFT_COLOR, RIGHT_COLOR, VALUE, TEXT, BOTTOM_COLOR
        }

        private var _leftColor = Color.BLACK
        private var _rightColor = Color.BLACK
        private var _value: Number = 0
        private var _text = ""
        private var _bottomColor = Color.BLACK

        var leftColor: Color
            get() { return _leftColor }
            set(leftColor) {
                _leftColor = leftColor
                onPropertyRefreshed(SwingProperty.LEFT_COLOR)
            }

        var rightColor: Color
            get() { return _rightColor }
            set(rightColor) {
                this._rightColor = rightColor
                onPropertyRefreshed(SwingProperty.RIGHT_COLOR)
            }

        var value: Number
            get() { return _value }
            set(value) {
                this._value = value
                onPropertyRefreshed(SwingProperty.VALUE)
            }

        var text: String
            get() { return _text }
            set(text) {
                this._text = text
                onPropertyRefreshed(SwingProperty.TEXT)
            }

        var bottomColor: Color
            get() { return _bottomColor }
            set(bottomColor) {
                this._bottomColor = bottomColor
                onPropertyRefreshed(SwingProperty.BOTTOM_COLOR)
            }
    }

    private var rangeBinding: Binding<Number>? = null
    private var headerBinding: Binding<String?>? = null
    private var leftColorBinding: Binding<Color>? = null
    private var rightColorBinding: Binding<Color>? = null
    private var valueBinding: Binding<Number>? = null
    private var bottomColorBinding: Binding<Color>? = null
    private var bottomTextBinding: Binding<String>? = null

    private val props = SwingProperties()
    private var neutralColor = Color.BLACK

    private enum class SingletonProperty {
        ALL
    }

    fun withRange(rangeBinding: Binding<Number>): SwingFrameBuilder {
        this.rangeBinding = rangeBinding
        return this
    }

    fun withNeutralColor(neutralColorBinding: Binding<Color>): SwingFrameBuilder {
        neutralColorBinding.bind {
            neutralColor = it
            if (props.value.toDouble() == 0.0) {
                props.bottomColor = it
            }
        }
        return this
    }

    fun withHeader(headerBinding: Binding<String?>): SwingFrameBuilder {
        this.headerBinding = headerBinding
        return this
    }

    fun build(): SwingFrame {
        return SwingFrame(
            headerBinding = headerBinding ?: Binding.fixedBinding(null),
            rangeBinding = rangeBinding ?: Binding.fixedBinding(1),
            valueBinding = valueBinding ?: Binding.fixedBinding(0),
            leftColorBinding = leftColorBinding ?: Binding.fixedBinding(Color.BLACK),
            rightColorBinding = rightColorBinding ?: Binding.fixedBinding(Color.BLACK),
            bottomColorBinding = bottomColorBinding ?: Binding.fixedBinding(Color.BLACK),
            bottomTextBinding = bottomTextBinding ?: Binding.fixedBinding(null)
        )
    }

    private class BindablePrevCurrPct : Bindable<BindablePrevCurrPct, SingletonProperty>() {
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

        fun setProperties() {
            fromParty = prevPct.entries
                .filter { e -> !java.lang.Double.isNaN(e.value) }
                .maxByOrNull { it.value }
                ?.key
            toParty = currPct.entries
                .filter { e -> e.key != fromParty }
                .filter { e -> !java.lang.Double.isNaN(e.value) }
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
            onPropertyRefreshed(SingletonProperty.ALL)
        }
    }

    companion object {
        @JvmStatic fun prevCurr(
            prevBinding: Binding<Map<Party, Number>>,
            currBinding: Binding<Map<Party, Number>>,
            partyOrder: Comparator<Party>
        ): SwingFrameBuilder {
            return prevCurr(prevBinding, currBinding, partyOrder, false)
        }

        private fun <C : Map<Party, Number>, P : Map<Party, Number>> prevCurr(
            prevBinding: Binding<P>,
            currBinding: Binding<C>,
            partyOrder: Comparator<Party>,
            normalised: Boolean
        ): SwingFrameBuilder {
            val prevCurr = BindablePrevCurrPct()
            val toPctFunc = { votes: Map<Party, Number> ->
                val total: Double = if (normalised) 1.0 else votes.values.map { it.toDouble() }.sum()
                votes.mapValues { e -> e.value.toDouble() / total }
            }
            prevBinding.bind { prevCurr.setPrevPct(toPctFunc(it)) }
            currBinding.bind { prevCurr.setCurrPct(toPctFunc(it)) }
            val ret = basic(
                Binding.propertyBinding(prevCurr, { it }, SingletonProperty.ALL),
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
                .withRange(Binding.fixedBinding(0.1))
                .withNeutralColor(Binding.fixedBinding(Color.LIGHT_GRAY))
            return ret
        }

        @JvmStatic fun prevCurrNormalised(
            prevBinding: Binding<Map<Party, Double>>,
            currBinding: Binding<Map<Party, Double>>,
            partyOrder: Comparator<Party>
        ): SwingFrameBuilder {
            return prevCurr(prevBinding, currBinding, partyOrder, true)
        }

        @JvmStatic fun <T> basic(
            binding: Binding<T>,
            leftColorFunc: (T) -> Color,
            rightColorFunc: (T) -> Color,
            valueFunc: (T) -> Number,
            textFunc: (T) -> String
        ): SwingFrameBuilder {
            val builder = SwingFrameBuilder()
            val props = builder.props
            builder.leftColorBinding =
                Binding.propertyBinding(props, { p: SwingProperties -> p.leftColor }, SwingProperty.LEFT_COLOR)
            builder.rightColorBinding = Binding.propertyBinding(
                props, { p: SwingProperties -> p.rightColor }, SwingProperty.RIGHT_COLOR
            )
            builder.valueBinding = Binding.propertyBinding(props, { p: SwingProperties -> p.value }, SwingProperty.VALUE)
            builder.bottomColorBinding = Binding.propertyBinding(
                props, { p: SwingProperties -> p.bottomColor }, SwingProperty.BOTTOM_COLOR
            )
            builder.bottomTextBinding = Binding.propertyBinding(props, { p: SwingProperties -> p.text }, SwingProperty.TEXT)
            binding.bind {
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
            return builder
        }
    }
}
