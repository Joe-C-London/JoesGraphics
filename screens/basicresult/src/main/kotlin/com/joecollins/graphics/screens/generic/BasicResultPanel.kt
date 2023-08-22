package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.ImageGenerator
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.toParty
import com.joecollins.pubsub.Publisher
import java.awt.Shape
import java.util.concurrent.Flow

object BasicResultPanel {

    interface KeyTemplate<KT, KPT : PartyOrCoalition> {
        fun toParty(key: KT): KPT
        fun toMainBarHeader(key: KT, forceSingleLine: Boolean): String
        fun winnerShape(forceSingleLine: Boolean): Shape
        fun runoffShape(forceSingleLine: Boolean): Shape
    }

    private class PartyTemplate<P : PartyOrCoalition> : KeyTemplate<P, P> {
        override fun toParty(key: P): P {
            return key
        }

        override fun toMainBarHeader(key: P, forceSingleLine: Boolean): String {
            return key.name.uppercase()
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return ImageGenerator.createRunoffShape()
        }
    }

    private class CandidateTemplate : KeyTemplate<Candidate, Party> {
        private val incumbentMarker: String

        constructor() {
            incumbentMarker = ""
        }

        constructor(incumbentMarker: String) {
            this.incumbentMarker = " $incumbentMarker"
        }

        override fun toParty(key: Candidate): Party {
            return key.party
        }

        override fun toMainBarHeader(key: Candidate, forceSingleLine: Boolean): String {
            return if (key === Candidate.OTHERS) {
                key.party.name.uppercase()
            } else {
                ("${key.name}${if (key.isIncumbent()) incumbentMarker else ""}${if (forceSingleLine) (" (" + key.party.abbreviation + ")") else ("\n" + key.party.name)}")
                    .uppercase()
            }
        }

        override fun winnerShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createTickShape() else ImageGenerator.createHalfTickShape()
        }

        override fun runoffShape(forceSingleLine: Boolean): Shape {
            return if (forceSingleLine) ImageGenerator.createRunoffShape() else ImageGenerator.createHalfRunoffShape()
        }
    }

    class CurrDiff<CT>(val curr: CT, val diff: CT)

    internal fun <T> partyMapToResultMap(m: Map<T, PartyOrCoalition?>): Map<T, PartyResult?> {
        return m.mapValues { e -> e.value?.let { PartyResult.elected(it.toParty()) } }
    }

    fun <P : PartyOrCoalition> partySeats(
        seats: Flow.Publisher<out Map<out P, Int>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SeatViewPanel.SeatScreenBuilder<P, P, Int, Int, *> {
        return SeatViewPanel.BasicSeatScreenBuilder(
            seats,
            header,
            subhead,
            PartyTemplate(),
        )
    }

    fun candidateSeats(
        seats: Flow.Publisher<out Map<Candidate, Int>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SeatViewPanel.SeatScreenBuilder<Candidate, Party, Int, Int, *> {
        return SeatViewPanel.BasicSeatScreenBuilder(
            seats,
            header,
            subhead,
            CandidateTemplate(),
        )
    }

    fun <P : PartyOrCoalition> partyDualSeats(
        seats: Flow.Publisher<out Map<out P, Pair<Int, Int>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SeatViewPanel.SeatScreenBuilder<P, P, Pair<Int, Int>, Pair<Int, Int>, *> {
        return SeatViewPanel.DualSeatScreenBuilder(
            seats,
            header,
            subhead,
            PartyTemplate(),
            SeatViewPanel.DualSeatScreenBuilder.FocusLocation.FIRST,
        )
    }

    fun <P : PartyOrCoalition> partyDualSeatsReversed(
        seats: Flow.Publisher<out Map<P, Pair<Int, Int>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SeatViewPanel.SeatScreenBuilder<P, P, Pair<Int, Int>, Pair<Int, Int>, *> {
        return SeatViewPanel.DualSeatScreenBuilder(
            seats,
            header,
            subhead,
            PartyTemplate(),
            SeatViewPanel.DualSeatScreenBuilder.FocusLocation.LAST,
        )
    }

    fun candidateDualSeats(
        seats: Flow.Publisher<out Map<Candidate, Pair<Int, Int>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SeatViewPanel.SeatScreenBuilder<Candidate, Party, Pair<Int, Int>, Pair<Int, Int>, *> {
        return SeatViewPanel.DualSeatScreenBuilder(
            seats,
            header,
            subhead,
            CandidateTemplate(),
            SeatViewPanel.DualSeatScreenBuilder.FocusLocation.FIRST,
        )
    }

    fun <P : PartyOrCoalition> partyRangeSeats(
        seats: Flow.Publisher<out Map<P, IntRange>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SeatViewPanel.SeatScreenBuilder<P, P, IntRange, Int, *> {
        return SeatViewPanel.RangeSeatScreenBuilder(
            seats,
            header,
            subhead,
            PartyTemplate(),
        )
    }

    fun candidateRangeSeats(
        seats: Flow.Publisher<out Map<Candidate, IntRange>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SeatViewPanel.SeatScreenBuilder<Candidate, Party, IntRange, Int, *> {
        return SeatViewPanel.RangeSeatScreenBuilder(
            seats,
            header,
            subhead,
            CandidateTemplate(),
        )
    }

    fun <P : PartyOrCoalition> partyVotes(
        votes: Flow.Publisher<out Map<out P, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SimpleVoteViewPanel.VoteScreenBuilder<P, P, Int?, Double, Int> {
        @Suppress("UNCHECKED_CAST")
        return SimpleVoteViewPanel.BasicVoteScreenBuilder(
            votes,
            header,
            subhead,
            PartyTemplate(),
            SimpleVoteViewPanel.VotePctOnlyTemplate(),
            Party.OTHERS as P,
        )
    }

    fun candidateVotes(
        votes: Flow.Publisher<out Map<Candidate, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SimpleVoteViewPanel.VoteScreenBuilder<Candidate, Party, Int?, Double, Int> {
        return SimpleVoteViewPanel.BasicVoteScreenBuilder(
            votes,
            header,
            subhead,
            CandidateTemplate(),
            SimpleVoteViewPanel.VotePctTemplate(),
            Candidate.OTHERS,
        )
    }

    fun candidateVotesPctOnly(
        votes: Flow.Publisher<out Map<Candidate, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SimpleVoteViewPanel.VoteScreenBuilder<Candidate, Party, Int?, Double, Int> {
        return SimpleVoteViewPanel.BasicVoteScreenBuilder(
            votes,
            header,
            subhead,
            CandidateTemplate(),
            SimpleVoteViewPanel.VotePctOnlyTemplate(),
            Candidate.OTHERS,
        )
    }

    fun candidateVotes(
        votes: Flow.Publisher<out Map<Candidate, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        incumbentMarker: String,
    ): SimpleVoteViewPanel.VoteScreenBuilder<Candidate, Party, Int?, Double, Int> {
        return SimpleVoteViewPanel.BasicVoteScreenBuilder(
            votes,
            header,
            subhead,
            CandidateTemplate(incumbentMarker),
            SimpleVoteViewPanel.VotePctTemplate(),
            Candidate.OTHERS,
        )
    }

    fun candidateVotesPctOnly(
        votes: Flow.Publisher<out Map<Candidate, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
        incumbentMarker: String,
    ): SimpleVoteViewPanel.VoteScreenBuilder<Candidate, Party, Int?, Double, Int> {
        return SimpleVoteViewPanel.BasicVoteScreenBuilder(
            votes,
            header,
            subhead,
            CandidateTemplate(incumbentMarker),
            SimpleVoteViewPanel.VotePctOnlyTemplate(),
            Candidate.OTHERS,
        )
    }

    fun <P : PartyOrCoalition> partyRangeVotes(
        votes: Flow.Publisher<out Map<P, ClosedRange<Double>>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SimpleVoteViewPanel.VoteScreenBuilder<P, P, ClosedRange<Double>, Double, Int> {
        @Suppress("UNCHECKED_CAST")
        return SimpleVoteViewPanel.RangeVoteScreenBuilder(
            votes,
            header,
            subhead,
            PartyTemplate(),
            SimpleVoteViewPanel.VotePctOnlyTemplate(),
            Party.OTHERS as P,
        )
    }

    fun nonPartisanVotes(
        votes: Flow.Publisher<out Map<NonPartisanCandidate, Int?>>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): SimpleVoteViewPanel.NonPartisanVoteBuilder {
        return SimpleVoteViewPanel.NonPartisanVoteBuilder(votes, header, subhead)
    }

    fun <P : PartyOrCoalition> partyQuotas(
        quotas: Flow.Publisher<out Map<out P, Double>>,
        totalSeats: Flow.Publisher<out Int>,
        header: Flow.Publisher<out String?>,
        subhead: Flow.Publisher<out String?>,
    ): PartyQuotasPanel.PartyQuotaScreenBuilder<P> {
        return PartyQuotasPanel.PartyQuotaScreenBuilder(
            quotas,
            totalSeats,
            header,
            subhead,
        )
    }
}
