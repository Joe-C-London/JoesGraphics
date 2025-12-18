package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.GenericPanel
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import java.awt.Color
import java.awt.GridLayout
import java.util.concurrent.Flow
import javax.swing.JPanel

class SeatsVotesScreen private constructor(seats: SeatViewPanel, votes: SimpleVoteViewPanel, title: Flow.Publisher<out String?>) :
    GenericPanel(
        JPanel().apply {
            background = Color.WHITE
            layout = GridLayout(1, 2)

            add(seats)
            add(votes)
        },
        title,
        seats.altText.merge(votes.altText) { sf, vf ->
            { maxLength: Int ->
                "${sf(maxLength / 2)}\n\n${vf(maxLength / 2)}"
            }
        }.merge(title) { f, h ->
            { maxLength: Int ->
                "$h\n\n${f(maxLength - h.length - 2)}"
            }
        },
    ) {

    class Seats internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        lateinit var seats: Flow.Publisher<out Map<Party, Int>>
        var total: Flow.Publisher<Int>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
        var majorityLine: (MajorityLine.() -> Unit)? = null
        var notes: Flow.Publisher<out String?>? = null

        private val majorityLineProps: MajorityLine? by lazy { majorityLine?.let { MajorityLine().apply(it) } }
        internal fun buildFrame(): SeatViewPanel {
            val me = this
            return SeatViewPanel.partySeats(
                current = {
                    this.seats = me.seats
                    this.header = me.header
                    this.subhead = me.subhead
                    this.totalSeats = me.total
                    this.progressLabel = me.progressLabel
                    this.notes = me.notes
                },
                majorityLine = me.majorityLineProps?.let {
                    {
                        show = it.show
                        display = it.display
                    }
                },
                title = null.asOneTimePublisher(),
            )
        }
    }

    class DualSeats internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        lateinit var seats: Flow.Publisher<out Map<Party, Pair<Int, Int>>>
        var total: Flow.Publisher<Int>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
        var majorityLine: (MajorityLine.() -> Unit)? = null
        var notes: Flow.Publisher<out String?>? = null

        private val majorityLineProps: MajorityLine? by lazy { majorityLine?.let { MajorityLine().apply(it) } }
        internal fun buildFrame(): SeatViewPanel {
            val me = this
            return SeatViewPanel.partyDualSeats(
                current = {
                    this.seats = me.seats
                    this.header = me.header
                    this.subhead = me.subhead
                    this.totalSeats = me.total
                    this.progressLabel = me.progressLabel
                    this.notes = me.notes
                },
                majorityLine = me.majorityLineProps?.let {
                    {
                        show = it.show
                        display = it.display
                    }
                },
                title = null.asOneTimePublisher(),
            )
        }
    }

    class SeatRange internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        lateinit var seats: Flow.Publisher<out Map<Party, IntRange>>
        var total: Flow.Publisher<Int>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
        var majorityLine: (MajorityLine.() -> Unit)? = null
        var notes: Flow.Publisher<out String?>? = null

        private val majorityLineProps: MajorityLine? by lazy { majorityLine?.let { MajorityLine().apply(it) } }
        internal fun buildFrame(): SeatViewPanel {
            val me = this
            return SeatViewPanel.partyRangeSeats(
                current = {
                    this.seats = me.seats
                    this.header = me.header
                    this.subhead = me.subhead
                    this.totalSeats = me.total
                    this.progressLabel = me.progressLabel
                    this.notes = me.notes
                },
                majorityLine = me.majorityLineProps?.let {
                    {
                        show = it.show
                        display = it.display
                    }
                },
                title = null.asOneTimePublisher(),
            )
        }
    }

    class CandidateSeats internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        lateinit var seats: Flow.Publisher<out Map<Candidate, Int>>
        var total: Flow.Publisher<Int>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
        var majorityLine: (MajorityLine.() -> Unit)? = null
        var notes: Flow.Publisher<out String?>? = null

        private val majorityLineProps: MajorityLine? by lazy { majorityLine?.let { MajorityLine().apply(it) } }
        internal fun buildFrame(): SeatViewPanel {
            val me = this
            return SeatViewPanel.candidateSeats(
                current = {
                    this.seats = me.seats
                    this.header = me.header
                    this.subhead = me.subhead
                    this.totalSeats = me.total
                    this.progressLabel = me.progressLabel
                    this.notes = me.notes
                },
                majorityLine = me.majorityLineProps?.let {
                    {
                        show = it.show
                        display = it.display
                    }
                },
                title = null.asOneTimePublisher(),
            )
        }
    }

    class CandidateDualSeats internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        lateinit var seats: Flow.Publisher<out Map<Candidate, Pair<Int, Int>>>
        var total: Flow.Publisher<Int>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
        var majorityLine: (MajorityLine.() -> Unit)? = null
        var notes: Flow.Publisher<out String?>? = null

        private val majorityLineProps: MajorityLine? by lazy { majorityLine?.let { MajorityLine().apply(it) } }
        internal fun buildFrame(): SeatViewPanel {
            val me = this
            return SeatViewPanel.candidateDualSeats(
                current = {
                    this.seats = me.seats
                    this.header = me.header
                    this.subhead = me.subhead
                    this.totalSeats = me.total
                    this.progressLabel = me.progressLabel
                    this.notes = me.notes
                },
                majorityLine = me.majorityLineProps?.let {
                    {
                        show = it.show
                        display = it.display
                    }
                },
                title = null.asOneTimePublisher(),
            )
        }
    }

    class Votes internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        lateinit var votes: Flow.Publisher<out Map<Party, Int>>
        var pctReporting: Flow.Publisher<Double>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
        var notes: Flow.Publisher<out String?>? = null

        internal fun buildFrame(): SimpleVoteViewPanel {
            val me = this
            return SimpleVoteViewPanel.partyVotes(
                current = {
                    this.votes = me.votes
                    this.header = me.header
                    this.subhead = me.subhead
                    this.pctReporting = me.pctReporting
                    this.progressLabel = me.progressLabel
                    this.notes = me.notes
                },
                title = null.asOneTimePublisher(),
            )
        }
    }

    class VotesRange internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        lateinit var votes: Flow.Publisher<out Map<Party, ClosedRange<Double>>>
        var progressLabel: Flow.Publisher<out String?>? = null
        var notes: Flow.Publisher<out String?>? = null

        internal fun buildFrame(): SimpleVoteViewPanel {
            val me = this
            return SimpleVoteViewPanel.partyRangeVotes(
                current = {
                    this.votes = me.votes
                    this.header = me.header
                    this.subhead = me.subhead
                    this.progressLabel = me.progressLabel
                    this.notes = me.notes
                },
                title = null.asOneTimePublisher(),
            )
        }
    }

    class CandidateVotes internal constructor() {
        lateinit var header: Flow.Publisher<out String?>
        lateinit var subhead: Flow.Publisher<out String?>
        lateinit var votes: Flow.Publisher<out Map<Candidate, Int>>
        var pctReporting: Flow.Publisher<Double>? = null
        var progressLabel: Flow.Publisher<out String?>? = null
        var notes: Flow.Publisher<out String?>? = null

        internal fun buildFrame(): SimpleVoteViewPanel {
            val me = this
            return SimpleVoteViewPanel.candidateVotes(
                current = {
                    this.votes = me.votes
                    this.header = me.header
                    this.subhead = me.subhead
                    this.pctReporting = me.pctReporting
                    this.progressLabel = me.progressLabel
                    this.notes = me.notes
                },
                title = null.asOneTimePublisher(),
            )
        }
    }

    class MajorityLine internal constructor() {
        var show: Flow.Publisher<out Boolean>? = null
        lateinit var display: (Int) -> String
    }

    companion object {
        fun of(
            seats: Seats.() -> Unit,
            votes: Votes.() -> Unit,
            title: Flow.Publisher<out String?>,
        ): SeatsVotesScreen = SeatsVotesScreen(
            Seats().apply(seats).buildFrame(),
            Votes().apply(votes).buildFrame(),
            title,
        )

        fun ofElectedLeading(
            seats: DualSeats.() -> Unit,
            votes: Votes.() -> Unit,
            title: Flow.Publisher<out String?>,
        ): SeatsVotesScreen = SeatsVotesScreen(
            DualSeats().apply(seats).buildFrame(),
            Votes().apply(votes).buildFrame(),
            title,
        )

        fun ofRange(
            seats: SeatRange.() -> Unit,
            votes: VotesRange.() -> Unit,
            title: Flow.Publisher<out String?>,
        ): SeatsVotesScreen = SeatsVotesScreen(
            SeatRange().apply(seats).buildFrame(),
            VotesRange().apply(votes).buildFrame(),
            title,
        )

        fun ofCandidates(
            seats: CandidateSeats.() -> Unit,
            votes: CandidateVotes.() -> Unit,
            title: Flow.Publisher<out String?>,
        ): SeatsVotesScreen = SeatsVotesScreen(
            CandidateSeats().apply(seats).buildFrame(),
            CandidateVotes().apply(votes).buildFrame(),
            title,
        )

        fun ofCandidatesElectedLeading(
            seats: CandidateDualSeats.() -> Unit,
            votes: CandidateVotes.() -> Unit,
            title: Flow.Publisher<out String?>,
        ): SeatsVotesScreen = SeatsVotesScreen(
            CandidateDualSeats().apply(seats).buildFrame(),
            CandidateVotes().apply(votes).buildFrame(),
            title,
        )
    }
}
