package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.graphics.components.FiguresFrame
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.combine
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.util.LinkedList
import java.util.concurrent.Flow
import javax.swing.JPanel
import javax.swing.border.EmptyBorder

class FiguresScreen private constructor(headerLabel: Flow.Publisher<out String?>, frames: Array<JPanel>, altText: Flow.Publisher<(Int) -> String>) :
    GenericPanel(
        {
            background = Color.WHITE
            layout = GridLayout(1, 0, 5, 5)
            border = EmptyBorder(5, 5, 5, 5)
            for (frame in frames) {
                add(frame)
            }
        },
        headerLabel,
        altText,
    ) {

    class Sections internal constructor(private val title: Flow.Publisher<out String?>) {
        private val sections = LinkedList<AbstractSection>()

        fun addSection(name: String, candidates: Section.() -> Unit) {
            sections.add(Section(name).apply(candidates))
        }

        fun addBlankSection() {
            sections.add(BlankSection)
        }

        internal fun build(): FiguresScreen {
            val frames: Array<JPanel> = sections.map { it.createFrame() }.toTypedArray()
            val altText = sections.mapNotNull { it.createAltText() }.combine()
                .merge(title) { s, t -> t + s.joinToString("") { "\n\n$it" } }
            return FiguresScreen(title, frames, altText.map { text -> { text } })
        }
    }

    sealed class AbstractSection {
        internal abstract fun createFrame(): JPanel
        internal abstract fun createAltText(): Flow.Publisher<String>?
    }

    internal object BlankSection : AbstractSection() {
        override fun createFrame() = JPanel().apply {
            background = Color.WHITE
        }

        override fun createAltText(): Flow.Publisher<String>? = null
    }

    class Section internal constructor(private val name: String) : AbstractSection() {
        private val entries: MutableList<Entry> = ArrayList()

        fun addCandidate(
            candidate: Candidate,
            description: String,
            leader: Flow.Publisher<out Party?>,
            status: Flow.Publisher<out String>,
        ): Section {
            val entry = Entry(candidate, description)
            leader.subscribe(Subscriber { entry.leader = it })
            status.subscribe(Subscriber { entry.status = it })
            entries.add(entry)
            return this
        }

        override fun createFrame(): FiguresFrame {
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
                            result = status,
                        )
                    }
                }
                    .combine(),
            )
            return frame
        }

        override fun createAltText(): Flow.Publisher<String> = this.entries.map { e -> e.statusPublisher.map { it to e } }
            .combine()
            .map { e ->
                e.groupBy({ it.first }, { it.second })
                    .mapValues { it.value.joinToString { c -> c.candidate.name.uppercase() } }
                    .entries
                    .sortedBy { it.key }
                    .joinToString("\n") { "${it.key}: ${it.value}" }
            }
            .map { "$name\n$it" }
    }

    private class Entry(val candidate: Candidate, val description: String) {
        var leader: Party? = null
            set(value) {
                field = value
                colorPublisher.submit(leader?.color ?: Color.LIGHT_GRAY)
            }

        var status: String = ""
            set(value) {
                field = value
                statusPublisher.submit(status)
            }

        val colorPublisher = Publisher((leader ?: Party.OTHERS).color)
        val statusPublisher = Publisher(status)
    }

    companion object {
        fun create(title: Flow.Publisher<out String?>, sections: Sections.() -> Unit): FiguresScreen = Sections(title).apply(sections).build()
    }
}
