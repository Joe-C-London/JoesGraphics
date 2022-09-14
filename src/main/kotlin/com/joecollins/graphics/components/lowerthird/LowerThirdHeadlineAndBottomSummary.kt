package com.joecollins.graphics.components.lowerthird

import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.pubsub.Subscriber
import com.joecollins.pubsub.Subscriber.Companion.eventQueueWrapper
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Image
import java.text.DecimalFormat
import java.time.Clock
import java.time.ZoneId
import java.util.concurrent.Flow

class LowerThirdHeadlineAndBottomSummary internal constructor(
    leftImagePublisher: Flow.Publisher<out Image>,
    placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
    private val headlinePublisher: Flow.Publisher<out String?>,
    private val subheadPublisher: Flow.Publisher<out String?>,
    summaryHeaderPublisher: Flow.Publisher<out String>,
    summaryFooterPublisher: Flow.Publisher<out String>,
    summaryEntriesPublisher: Flow.Publisher<out List<BottomSummary.Entry>>,
    clock: Clock,
    showTimeZone: Boolean = false
) : LowerThird(leftImagePublisher, placePublisher, clock, showTimeZone) {

    constructor(
        leftImagePublisher: Flow.Publisher<out Image>,
        placePublisher: Flow.Publisher<out Pair<String, ZoneId>>,
        headlinePublisher: Flow.Publisher<out String?>,
        subheadPublisher: Flow.Publisher<out String?>,
        summaryHeaderPublisher: Flow.Publisher<out String>,
        summaryFooterPublisher: Flow.Publisher<out String>,
        summaryEntriesPublisher: Flow.Publisher<out List<BottomSummary.Entry>>,
        showTimeZone: Boolean = false
    ) : this(
        leftImagePublisher,
        placePublisher,
        headlinePublisher,
        subheadPublisher,
        summaryHeaderPublisher,
        summaryFooterPublisher,
        summaryEntriesPublisher,
        Clock.systemDefaultZone(),
        showTimeZone
    )

    private val headlinePanel = HeadlinePanel()
    private val partySummary = BottomSummary(summaryHeaderPublisher, summaryFooterPublisher, summaryEntriesPublisher)

    internal val headline: String?
        get() = headlinePanel.headline

    internal val subhead: String?
        get() = headlinePanel.subhead

    internal val summaryHeader: String
        get() = partySummary.header

    internal val summaryFooter: String
        get() = partySummary.footer

    internal val numSummaryEntries: Int
        get() = partySummary.numEntries

    internal fun getEntryColor(index: Int): Color {
        return partySummary.getEntryColor(index)
    }

    internal fun getEntryLabel(index: Int): String {
        return partySummary.getEntryLabel(index)
    }

    internal fun getEntryValue(index: Int): String {
        return partySummary.getEntryValue(index)
    }

    init {
        preferredSize = Dimension(1024, 70)
        add(headlinePanel, BorderLayout.CENTER)
        add(partySummary, BorderLayout.SOUTH)
        this.headlinePublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.headline = it }))
        this.subheadPublisher.subscribe(Subscriber(eventQueueWrapper { headlinePanel.subhead = it }))
    }

    companion object {
        fun createSeatEntries(seats: Map<out PartyOrCoalition, Int>, totalSeats: Int = 0, partiesToShow: Set<out PartyOrCoalition> = emptySet()): List<BottomSummary.Entry> {
            val ret = sequenceOf(
                partiesToShow.asSequence(),
                seats.entries.asSequence().filter { it.value > 0 }.map { it.key }
            )
                .flatten()
                .distinct()
                .map { it to (seats[it] ?: 0) }
                .sortedByDescending { if (it.first == Party.OTHERS) -1 else it.second }
                .map { BottomSummary.Entry(it.first.color, it.first.abbreviation, it.second.toString()) }
                .toMutableList()
            val inDoubt = totalSeats - seats.values.sum()
            if (inDoubt > 0) {
                ret.add(BottomSummary.Entry(Color.WHITE, "?", inDoubt.toString()))
            }
            return ret
        }

        fun createVoteEntries(votes: Map<Party, Int>): List<SummaryWithHeaderAndLabels.Entry> {
            val total = votes.values.sum().toDouble()
            val ret = votes.entries
                .filter { it.value > 0 }
                .sortedByDescending { if (it.key == Party.OTHERS) -1 else it.value }
                .map {
                    SummaryWithHeaderAndLabels.Entry(
                        it.key.color,
                        it.key.abbreviation,
                        DecimalFormat("0.0%").format(it.value / total)
                    )
                }
                .toMutableList()
            if (ret.isEmpty()) {
                ret.add(SummaryWithHeaderAndLabels.Entry(Color.WHITE, "", "WAITING..."))
            }
            return ret
        }

        fun createVoteEntries(votes: Map<Candidate, Int>, labelFunc: (Candidate) -> String): List<SummaryWithHeaderAndLabels.Entry> {
            val total = votes.values.sum().toDouble()
            val ret = votes.entries
                .filter { it.value > 0 }
                .sortedByDescending { if (it.key == Candidate.OTHERS) -1 else it.value }
                .map {
                    SummaryWithHeaderAndLabels.Entry(
                        it.key.party.color,
                        if (it.key == Candidate.OTHERS) Candidate.OTHERS.party.abbreviation else labelFunc(it.key),
                        DecimalFormat("0.0%").format(it.value / total)
                    )
                }
                .toMutableList()
            if (ret.isEmpty()) {
                ret.add(SummaryWithHeaderAndLabels.Entry(Color.WHITE, "", "WAITING..."))
            }
            return ret
        }
    }
}
