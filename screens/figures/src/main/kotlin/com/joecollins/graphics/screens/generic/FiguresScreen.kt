package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.FiguresFrame
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.util.LinkedList
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class FiguresScreen private constructor(headerLabel: Flow.Publisher<out String?>, frames: Array<FiguresFrame>) :
    GenericPanel(
        run {
            val panel = JPanel()
            panel.background = Color.WHITE
            panel.layout = GridLayout(1, 0, 5, 5)
            panel.border = EmptyBorder(5, 5, 5, 5)
            for (frame in frames) {
                panel.add(frame)
            }
            panel
        },
        headerLabel
    ) {

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
        var leader: Party? = null
            set(value) {
                field = value
                colorPublisher.submit((leader ?: Party.OTHERS).color)
            }

        var status: String = ""
            set(value) {
                field = value
                statusPublisher.submit(status)
            }

        val colorPublisher = Publisher((leader ?: Party.OTHERS).color)
        val statusPublisher = Publisher(status)
    }

    class Builder {
        private val sections: MutableList<Section> = LinkedList()

        fun build(titlePublisher: Flow.Publisher<out String?>): FiguresScreen {
            val frames: Array<FiguresFrame> = sections.map { it.createFrame() }.toTypedArray()
            return FiguresScreen(titlePublisher, frames)
        }

        fun withSection(section: Section): Builder {
            sections.add(section)
            return this
        }
    }

    companion object {
        fun of(): Builder {
            return Builder()
        }

        fun section(sectionHeader: String): Section {
            return Section(sectionHeader)
        }
    }
}
