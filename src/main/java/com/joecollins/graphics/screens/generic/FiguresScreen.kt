package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.FiguresFrame
import com.joecollins.graphics.components.FontSizeAdjustingLabel
import com.joecollins.graphics.utils.StandardFont.readBoldFont
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import java.awt.BorderLayout
import java.awt.Color
import java.awt.GridLayout
import java.util.LinkedList
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
            leader: Binding<Party?>,
            status: Binding<String>
        ): Section {
            val entry = Entry(candidate, description)
            leader.bind { entry.leader = it }
            status.bind { entry.status = it }
            entries.add(entry)
            return this
        }

        fun createFrame(): FiguresFrame {
            val frame = FiguresFrame(
                headerPublisher = name.asOneTimePublisher(),
                entriesPublisher = Binding.listBinding(
                    entries.map { e ->
                        val colorBinding = Binding.propertyBinding(
                            e,
                            { x: Entry -> (x.leader ?: Party.OTHERS).color },
                            Entry.Property.LEADER
                        )
                        val statusBinding = Binding.propertyBinding(e, { x: Entry -> x.status }, Entry.Property.STATUS)
                        colorBinding.merge(statusBinding) { color, status ->
                            FiguresFrame.Entry(
                                name = e.candidate.name.uppercase(),
                                color = e.candidate.party.color,
                                description = e.description,
                                resultColor = color,
                                result = status
                            )
                        }
                    }
                ).toPublisher()
            )
            return frame
        }
    }

    private class Entry(val candidate: Candidate, val description: String) : Bindable<Entry, Entry.Property>() {
        enum class Property {
            LEADER, STATUS
        }

        private var _leader: Party? = null
        private var _status: String = ""

        var leader: Party?
            get() = _leader
            set(leader) {
                this._leader = leader
                onPropertyRefreshed(Property.LEADER)
            }

        var status: String
            get() = _status
            set(status) {
                this._status = status
                onPropertyRefreshed(Property.STATUS)
            }
    }

    class Builder {
        private val sections: MutableList<Section> = LinkedList()

        fun build(titleBinding: Binding<String?>): FiguresScreen {
            val headerLabel = FontSizeAdjustingLabel()
            headerLabel.font = readBoldFont(32)
            headerLabel.horizontalAlignment = JLabel.CENTER
            headerLabel.border = EmptyBorder(5, 0, -5, 0)
            titleBinding.bind { headerLabel.text = it }
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
