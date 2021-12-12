package com.joecollins.graphics.components

import com.joecollins.bindings.Binding
import com.joecollins.bindings.toFixedBinding
import com.joecollins.graphics.utils.ColorUtils.foregroundToContrast
import com.joecollins.graphics.utils.StandardFont
import com.joecollins.models.general.Party
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.GridLayout
import java.awt.RenderingHints
import java.util.LinkedList
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class ParliamentVoteFrame(
    titleBinding: Binding<String>,
    subtitleBinding: Binding<String>,
    bodyName: String,
    private val bodyColor: Color,
    sides: Array<String>,
    votes: Binding<IntArray>,
    private val partyRows: Int,
    partyVotes: Binding<Array<List<Pair<Party, Int>>>>,
    resultText: Binding<String?> = Binding.fixedBinding(null)
) : GraphicsFrame(
    headerBinding = titleBinding,
    notesBinding = "SOURCE: $bodyName".toFixedBinding(),
    borderColorBinding = bodyColor.toFixedBinding()
) {

    init {
        val outerPanel = JPanel()
        outerPanel.background = Color.WHITE
        outerPanel.layout = BorderLayout()
        add(outerPanel, BorderLayout.CENTER)

        val subtitleLabel = FontSizeAdjustingLabel()
        subtitleLabel.font = StandardFont.readNormalFont(16)
        subtitleLabel.horizontalAlignment = JLabel.CENTER
        subtitleLabel.border = EmptyBorder(4, 0, -4, 0)
        subtitleLabel.foreground = bodyColor
        subtitleBinding.bind { subtitleLabel.text = it }
        outerPanel.add(subtitleLabel, BorderLayout.NORTH)

        val innerPanel = JPanel()
        innerPanel.background = Color.WHITE
        val innerPanelLayout = CardLayout()
        innerPanel.layout = innerPanelLayout
        outerPanel.add(innerPanel, BorderLayout.CENTER)

        val divisionResultPanel = JPanel()
        divisionResultPanel.background = Color.WHITE
        divisionResultPanel.layout = GridLayout(0, 1, 5, 5)
        innerPanel.add(divisionResultPanel, "DIV")

        val resultLinePanels = LinkedList<ResultLinePanel>()
        sides.forEach { s ->
            val newPanel = ResultLinePanel()
            newPanel.header = s
            divisionResultPanel.add(newPanel)
            resultLinePanels.add(newPanel)
        }

        votes.bind { v ->
            resultLinePanels.forEachIndexed { index, panel ->
                panel.votes = if (index < v.size) v[index] else null
            }
        }

        partyVotes.bind { v ->
            resultLinePanels.forEachIndexed { index, panel ->
                panel.partyVotes = if (index < v.size) v[index] else emptyList()
            }
        }

        val noDivisionLabel = FontSizeAdjustingLabel()
        noDivisionLabel.font = StandardFont.readBoldFont(30)
        noDivisionLabel.foreground = bodyColor
        noDivisionLabel.horizontalAlignment = JLabel.CENTER
        innerPanel.add(noDivisionLabel, "NODIV")

        resultText.bind {
            innerPanelLayout.show(innerPanel, if (it == null) "DIV" else "NODIV")
            noDivisionLabel.text = it ?: ""
        }
    }

    private inner class ResultLinePanel : JPanel() {
        private val headerLabel: JLabel
        private val votesLabel: JLabel
        private val partyVotePanel: PartyVotePanel

        init {
            layout = GridLayout(1, 2)
            background = Color.WHITE

            val left = JPanel()
            left.layout = GridLayout(1, 2)
            left.background = Color.WHITE
            add(left)

            headerLabel = FontSizeAdjustingLabel()
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.font = StandardFont.readBoldFont(12)
            headerLabel.foreground = bodyColor
            left.add(headerLabel)

            votesLabel = FontSizeAdjustingLabel()
            votesLabel.horizontalAlignment = JLabel.CENTER
            votesLabel.font = StandardFont.readBoldFont(30)
            votesLabel.foreground = bodyColor
            left.add(votesLabel)

            partyVotePanel = PartyVotePanel()
            add(partyVotePanel)
        }

        var header: String
            get() = headerLabel.text
            set(value) {
                headerLabel.text = value
                repaint()
            }

        var votes: Int?
            get() = votesLabel.text.takeIf { it.isNotBlank() }?.toInt()
            set(value) {
                votesLabel.text = value?.toString() ?: ""
                repaint()
            }

        var partyVotes: List<Pair<Party, Int>>
            get() = partyVotePanel.votesByParty
            set(value) {
                partyVotePanel.votesByParty = value
                repaint()
            }
    }

    private inner class PartyVotePanel : JPanel() {
        var votesByParty: List<Pair<Party, Int>> = emptyList()
            set(value) {
                field = value
                removeAll()
                value.forEach { (party, votes) ->
                    val panel = object : JPanel() {
                        override fun paintComponent(g: Graphics) {
                            super.paintComponent(g)
                            (g as Graphics2D)
                                .setRenderingHint(
                                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
                                )

                            g.color = party.color
                            g.fillRect(0, 0, width, height)

                            val labelFontSize = (height / 4).coerceAtMost(12)
                            val voteFontSize = (height * 2 / 3).coerceAtMost(30)
                            val padding = height - labelFontSize - voteFontSize

                            g.color = foregroundToContrast(party.color)
                            g.font = StandardFont.readNormalFont(labelFontSize)
                            val labelWidth = g.fontMetrics.stringWidth(party.abbreviation)
                            g.drawString(party.abbreviation, (width - labelWidth) / 2, padding / 2 + labelFontSize)

                            g.font = StandardFont.readBoldFont(voteFontSize)
                            val voteWidth = g.fontMetrics.stringWidth(votes.toString())
                            g.drawString(
                                votes.toString(),
                                (width - voteWidth) / 2,
                                padding / 2 + labelFontSize + voteFontSize
                            )
                        }
                    }
                    add(panel)
                }
            }

        init {
            layout = GridLayout(partyRows, 0)
            background = Color.WHITE
        }
    }
}
