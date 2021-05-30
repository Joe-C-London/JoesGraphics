package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.BarFrameBuilder
import com.joecollins.graphics.components.ListingFrameBuilder
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import java.awt.Point
import java.awt.Shape
import java.text.DecimalFormat
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class CandidateListingScreen private constructor(
    header: JLabel,
    builder: Builder
) : JPanel() {

    internal val candidatesPanel = builder.candidatesPanel
    internal val prevPanel = builder.prevPanel
    internal val secondaryPrevPanel = builder.secondaryPrevPanel
    internal val mapPanel = builder.mapPanel

    init {
        layout = BorderLayout()
        background = Color.WHITE
        add(header, BorderLayout.NORTH)
        val panel = JPanel()
        panel.layout = ScreenLayout()
        panel.background = Color.WHITE
        add(panel, BorderLayout.CENTER)
        panel.add(candidatesPanel)
        if (prevPanel != null) {
            panel.add(prevPanel)
        }
        if (secondaryPrevPanel != null) {
            panel.add(secondaryPrevPanel)
        }
        if (mapPanel != null) {
            panel.add(mapPanel)
        }
    }

    companion object {
        fun of(candidates: Binding<List<Candidate>>, header: Binding<String?>, subhead: Binding<String?>): Builder {
            return Builder(candidates, header, subhead, null)
        }

        fun of(candidates: Binding<List<Candidate>>, header: Binding<String?>, subhead: Binding<String?>, incumbentMarker: String): Builder {
            return Builder(candidates, header, subhead, incumbentMarker)
        }
    }

    class Builder internal constructor (candidates: Binding<List<Candidate>>, header: Binding<String?>, subhead: Binding<String?> = Binding.fixedBinding(""), incumbentMarker: String?) {
        internal val candidatesPanel: JPanel
        internal var prevPanel: JPanel? = null
        internal var secondaryPrevPanel: JPanel? = null
        internal var mapPanel: JPanel? = null

        init {
            candidatesPanel = ListingFrameBuilder.of(
                candidates,
                { it.name.uppercase() + (if (!it.incumbent || incumbentMarker == null) "" else (" $incumbentMarker")) },
                { it.party.name.uppercase() },
                { it.party.color }
            )
                .withHeader(header)
                .withSubhead(subhead)
                .build()
        }

        fun withPrev(prevVotes: Binding<Map<Party, Int>>, header: Binding<String?>, subhead: Binding<String?> = Binding.fixedBinding(null)): Builder {
            prevPanel = BarFrameBuilder.basic(prevVotes.map { v -> createBars(v) })
                .withMax(Binding.fixedBinding(2.0 / 3))
                .withHeader(header)
                .withSubhead(subhead)
                .build()
            return this
        }

        fun withSecondaryPrev(prevVotes: Binding<Map<Party, Int>>, header: Binding<String?>, subhead: Binding<String?> = Binding.fixedBinding(null)): Builder {
            secondaryPrevPanel = BarFrameBuilder.basic(prevVotes.map { v -> createBars(v) })
                .withMax(Binding.fixedBinding(2.0 / 3))
                .withHeader(header)
                .withSubhead(subhead)
                .build()
            return this
        }

        private fun createBars(votes: Map<Party, Int>): List<BarFrameBuilder.BasicBar> {
            val total = votes.values.sum()
            return votes.asSequence()
                .sortedByDescending { it.value }
                .map {
                    BarFrameBuilder.BasicBar(
                        it.key.abbreviation.uppercase(),
                        it.key.color,
                        it.value.toDouble() / total,
                        DecimalFormat("0.0%").format(it.value.toDouble() / total)
                    )
                }
                .toList()
        }

        fun <K> withMap(
            shapes: Binding<Map<K, Shape>>,
            selectedShape: Binding<K>,
            focus: Binding<List<K>?>,
            header: Binding<String>
        ): Builder {
            mapPanel = MapBuilder(shapes, selectedShape, Binding.fixedBinding(null), focus, header).createMapFrame()
            return this
        }

        fun <K> withMap(
            shapes: Binding<Map<K, Shape>>,
            selectedShape: Binding<K>,
            focus: Binding<List<K>?>,
            additionalHighlight: Binding<List<K>?>,
            header: Binding<String>
        ): Builder {
            mapPanel = MapBuilder(shapes, selectedShape, Binding.fixedBinding(null), focus, additionalHighlight, header).createMapFrame()
            return this
        }

        fun build(title: Binding<String>): CandidateListingScreen {
            return CandidateListingScreen(createHeaderLabel(title), this)
        }

        private fun createHeaderLabel(textBinding: Binding<String>): JLabel {
            val headerLabel = JLabel()
            headerLabel.font = StandardFont.readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            textBinding.bind { headerLabel.text = it }
            return headerLabel
        }
    }

    private inner class ScreenLayout : LayoutManager {
        override fun addLayoutComponent(name: String?, comp: Component?) {
        }

        override fun removeLayoutComponent(comp: Component?) {
        }

        override fun preferredLayoutSize(parent: Container?): Dimension {
            return Dimension(1024, 512)
        }

        override fun minimumLayoutSize(parent: Container?): Dimension {
            return Dimension(100, 50)
        }

        override fun layoutContainer(parent: Container) {
            val width = parent.width
            val height = parent.height
            val numPanels = listOf(prevPanel, secondaryPrevPanel, mapPanel).count { it != null }
            candidatesPanel.location = Point(5, 5)
            val rightColStart = if (numPanels == 0) width else (width - width / (numPanels + 1))
            candidatesPanel.size = Dimension(rightColStart - 10, height - 10)
            if (prevPanel != null) {
                prevPanel.location = Point(rightColStart + 5, 5)
                prevPanel.size = Dimension(width / (numPanels + 1) - 10, (height / numPanels) - 10)
            }
            if (secondaryPrevPanel != null) {
                secondaryPrevPanel.location = Point(rightColStart + 5, (if (prevPanel == null) 0 else 1) * height / numPanels + 5)
                secondaryPrevPanel.size = Dimension(width / (numPanels + 1) - 10, (height / numPanels) - 10)
            }
            if (mapPanel != null) {
                mapPanel.location = Point(rightColStart + 5, height - (height / (numPanels)) + 5)
                mapPanel.size = Dimension(width / (numPanels + 1) - 10, (height / numPanels) - 10)
            }
        }
    }
}
