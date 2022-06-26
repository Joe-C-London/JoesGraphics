package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.FiguresFrame
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.merge
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.util.LinkedList
import java.util.concurrent.Flow
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.border.EmptyBorder
import kotlin.collections.ArrayList

class FiguresScreen private constructor(headerLabel: JLabel, frames: Array<FiguresFrame>) : JPanel() {

    class Section(private val name: String) {
        private val entries: MutableList<Entry> = ArrayList()

        fun withCandidate(
            candidate: Candidate,
            description: String,
            leader: Flow.Publisher<out Party?>,
            status: Flow.Publisher<out String>
        ): Section {
            val entry = Entry(candidate, description)
            leader.subscribe(Subscriber { entry.leader = it })
            status.subscribe(Subscriber { entry.status = it })
            entries.add(entry)
            return this
        }

        fun createFrame(): FiguresFrame {
            val frame = FiguresFrame(
                headerPublisher = name.asOneTimePublisher(),
                entriesPublisher =
                entries.map { e ->
                    e.colorPublisher.merge(e.statusPublisher) { color, status ->
                        FiguresFrame.Entry(
                            name = e.candidate.name.uppercase(),
                            color = e.candidate.party.color,
                            description = e.description,
                            resultColor = color,
                            result = status
                        )
                    }
                }
                    .combine()
            )
            return frame
        }
    }

    private class Entry(val candidate: Candidate, val description: String) {
        private var _leader: Party? = null
        private var _status: String = ""

        var leader: Party?
            get() = _leader
            set(leader) {
                this._leader = leader
                colorPublisher.submit((leader ?: Party.OTHERS).color)
            }

        var status: String
            get() = _status
            set(status) {
                this._status = status
                statusPublisher.submit(status)
            }

        val colorPublisher = Publisher((leader ?: Party.OTHERS).color)
        val statusPublisher = Publisher(status)
    }

    class Builder {
        private val sections: MutableList<Section> = LinkedList()

        fun build(titlePublisher: Flow.Publisher<out String?>): FiguresScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titlePublisher.subscribe(Subscriber(eventQueueWrapper { headerLabel.text = it }))
            val frames: Array<FiguresFrame> = sections.map { it.createFrame() }.toTypedArray()
            return FiguresScreen(headerLabel, frames)
        }

        fun withSection(section: Section): Builder {
            sections.add(section)
            return this
        }
    }

    companion object {
        @JvmStatic fun of(): Builder {
            return Builder()
        }

        @JvmStatic fun section(sectionHeader: String): Section {
            return Section(sectionHeader)
        }
    }

    init {
        background = Color.WHITE
        layout = BorderLayout()
        add(headerLabel, BorderLayout.NORTH)
        val panel = JPanel()
        panel.background = Color.WHITE
        panel.layout = GridLayout(1, 0, 5, 5)
        panel.border = EmptyBorder(5, 5, 5, 5)
        add(panel, BorderLayout.CENTER)
        for (frame in frames) {
            panel.add(frame)
        }
    }
}
