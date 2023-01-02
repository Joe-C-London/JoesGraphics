package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Aggregators
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.models.general.PollsReporting
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

@Suppress("UNCHECKED_CAST")
class TooCloseToCallScreenTest {
    @Test
    fun testBasic() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val screen = TooCloseToCallScreen.of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )

        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-2", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217
            """.trimIndent(),
        )

        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-3", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 12: LIB: 478; GRN: 475; LEAD: 3
                DISTRICT 23: PC: 436; GRN: 428; LEAD: 8
                DISTRICT 8: LIB: 620; PC: 609; LEAD: 11
                DISTRICT 26: PC: 700; LIB: 686; LEAD: 14
                DISTRICT 5: PC: 533; LIB: 518; LEAD: 15
                DISTRICT 25: LIB: 454; NDP: 425; LEAD: 29
                DISTRICT 21: GRN: 617; PC: 577; LEAD: 40
                DISTRICT 11: GRN: 636; PC: 595; LEAD: 41
                DISTRICT 22: GRN: 602; LIB: 560; LEAD: 42
                DISTRICT 24: LIB: 330; GRN: 197; LEAD: 133
                DISTRICT 15: PC: 583; LIB: 425; LEAD: 158
                DISTRICT 13: LIB: 717; GRN: 542; LEAD: 175
                DISTRICT 6: PC: 725; LIB: 526; LEAD: 199
                DISTRICT 14: LIB: 699; PC: 492; LEAD: 207
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217
                DISTRICT 9: PC: 620; LIB: 395; LEAD: 225
                DISTRICT 27: LIB: 646; PC: 405; LEAD: 241
                DISTRICT 2: PC: 555; GRN: 308; LEAD: 247
                DISTRICT 10: LIB: 808; GRN: 516; LEAD: 292
                DISTRICT 16: LIB: 983; GRN: 542; LEAD: 441
            """.trimIndent(),
        )

        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-4", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 26: LIB: 919; PC: 890; LEAD: 29
                DISTRICT 13: LIB: 952; GRN: 840; LEAD: 112
                DISTRICT 8: PC: 948; LIB: 832; LEAD: 116
                DISTRICT 5: GRN: 871; PC: 743; LEAD: 128
                DISTRICT 12: GRN: 831; LIB: 639; LEAD: 192
                DISTRICT 24: LIB: 774; GRN: 582; LEAD: 192
                DISTRICT 14: LIB: 874; GRN: 660; LEAD: 214
                DISTRICT 15: PC: 909; LIB: 652; LEAD: 257
                DISTRICT 9: PC: 807; GRN: 533; LEAD: 274
                DISTRICT 6: PC: 995; LIB: 684; LEAD: 311
                DISTRICT 16: LIB: 1,286; GRN: 819; LEAD: 467
            """.trimIndent(),
        )

        setupFullResults(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )
    }

    @Test
    fun testPctReporting() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val pctReportingRaw: MutableMap<Int, Double> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val pctReporting = Publisher<Map<Int, Double>>(pctReportingRaw)
        val screen = TooCloseToCallScreen.of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
            .withPctReporting { pctReporting.map { p -> p[it] ?: 0.0 } }
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "PctReporting-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )

        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "PctReporting-2", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217; 10.0% IN
            """.trimIndent(),
        )

        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "PctReporting-3", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 12: LIB: 478; GRN: 475; LEAD: 3; 10.0% IN
                DISTRICT 23: PC: 436; GRN: 428; LEAD: 8; 10.0% IN
                DISTRICT 8: LIB: 620; PC: 609; LEAD: 11; 10.0% IN
                DISTRICT 26: PC: 700; LIB: 686; LEAD: 14; 10.0% IN
                DISTRICT 5: PC: 533; LIB: 518; LEAD: 15; 12.5% IN
                DISTRICT 25: LIB: 454; NDP: 425; LEAD: 29; 9.1% IN
                DISTRICT 21: GRN: 617; PC: 577; LEAD: 40; 10.0% IN
                DISTRICT 11: GRN: 636; PC: 595; LEAD: 41; 10.0% IN
                DISTRICT 22: GRN: 602; LIB: 560; LEAD: 42; 10.0% IN
                DISTRICT 24: LIB: 330; GRN: 197; LEAD: 133; 12.5% IN
                DISTRICT 15: PC: 583; LIB: 425; LEAD: 158; 10.0% IN
                DISTRICT 13: LIB: 717; GRN: 542; LEAD: 175; 10.0% IN
                DISTRICT 6: PC: 725; LIB: 526; LEAD: 199; 11.1% IN
                DISTRICT 14: LIB: 699; PC: 492; LEAD: 207; 10.0% IN
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217; 10.0% IN
                DISTRICT 9: PC: 620; LIB: 395; LEAD: 225; 9.1% IN
                DISTRICT 27: LIB: 646; PC: 405; LEAD: 241; 10.0% IN
                DISTRICT 2: PC: 555; GRN: 308; LEAD: 247; 10.0% IN
                (...)
            """.trimIndent(),
        )

        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "PctReporting-4", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 26: LIB: 919; PC: 890; LEAD: 29; 50.0% IN
                DISTRICT 13: LIB: 952; GRN: 840; LEAD: 112; 50.0% IN
                DISTRICT 8: PC: 948; LIB: 832; LEAD: 116; 50.0% IN
                DISTRICT 5: GRN: 871; PC: 743; LEAD: 128; 62.5% IN
                DISTRICT 12: GRN: 831; LIB: 639; LEAD: 192; 50.0% IN
                DISTRICT 24: LIB: 774; GRN: 582; LEAD: 192; 62.5% IN
                DISTRICT 14: LIB: 874; GRN: 660; LEAD: 214; 50.0% IN
                DISTRICT 15: PC: 909; LIB: 652; LEAD: 257; 50.0% IN
                DISTRICT 9: PC: 807; GRN: 533; LEAD: 274; 45.5% IN
                DISTRICT 6: PC: 995; LIB: 684; LEAD: 311; 55.6% IN
                DISTRICT 16: LIB: 1,286; GRN: 819; LEAD: 467; 50.0% IN
            """.trimIndent(),
        )

        setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "PctReporting-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )
    }

    @Test
    fun testPollsReporting() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val pollsReportingRaw: MutableMap<Int, PollsReporting> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val pollsReporting = Publisher<Map<Int, PollsReporting>>(pollsReportingRaw)
        val screen = TooCloseToCallScreen.of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
            .withPollsReporting { pollsReporting.map { p -> p[it] ?: PollsReporting(0, 0) } }
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "PollsReporting-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )

        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, HashMap(), pollsReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pollsReporting.submit(pollsReportingRaw)
        compareRendering("TooCloseToCallScreen", "PollsReporting-2", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217; 1/10
            """.trimIndent(),
        )

        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, HashMap(), pollsReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pollsReporting.submit(pollsReportingRaw)
        compareRendering("TooCloseToCallScreen", "PollsReporting-3", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 12: LIB: 478; GRN: 475; LEAD: 3; 1/10
                DISTRICT 23: PC: 436; GRN: 428; LEAD: 8; 1/10
                DISTRICT 8: LIB: 620; PC: 609; LEAD: 11; 1/10
                DISTRICT 26: PC: 700; LIB: 686; LEAD: 14; 1/10
                DISTRICT 5: PC: 533; LIB: 518; LEAD: 15; 1/8
                DISTRICT 25: LIB: 454; NDP: 425; LEAD: 29; 1/11
                DISTRICT 21: GRN: 617; PC: 577; LEAD: 40; 1/10
                DISTRICT 11: GRN: 636; PC: 595; LEAD: 41; 1/10
                DISTRICT 22: GRN: 602; LIB: 560; LEAD: 42; 1/10
                DISTRICT 24: LIB: 330; GRN: 197; LEAD: 133; 1/8
                DISTRICT 15: PC: 583; LIB: 425; LEAD: 158; 1/10
                DISTRICT 13: LIB: 717; GRN: 542; LEAD: 175; 1/10
                DISTRICT 6: PC: 725; LIB: 526; LEAD: 199; 1/9
                DISTRICT 14: LIB: 699; PC: 492; LEAD: 207; 1/10
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217; 1/10
                DISTRICT 9: PC: 620; LIB: 395; LEAD: 225; 1/11
                DISTRICT 27: LIB: 646; PC: 405; LEAD: 241; 1/10
                DISTRICT 2: PC: 555; GRN: 308; LEAD: 247; 1/10
                DISTRICT 10: LIB: 808; GRN: 516; LEAD: 292; 1/10
                DISTRICT 16: LIB: 983; GRN: 542; LEAD: 441; 1/10
            """.trimIndent(),
        )

        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, HashMap(), pollsReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pollsReporting.submit(pollsReportingRaw)
        compareRendering("TooCloseToCallScreen", "PollsReporting-4", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 26: LIB: 919; PC: 890; LEAD: 29; 5/10
                DISTRICT 13: LIB: 952; GRN: 840; LEAD: 112; 5/10
                DISTRICT 8: PC: 948; LIB: 832; LEAD: 116; 5/10
                DISTRICT 5: GRN: 871; PC: 743; LEAD: 128; 5/8
                DISTRICT 12: GRN: 831; LIB: 639; LEAD: 192; 5/10
                DISTRICT 24: LIB: 774; GRN: 582; LEAD: 192; 5/8
                DISTRICT 14: LIB: 874; GRN: 660; LEAD: 214; 5/10
                DISTRICT 15: PC: 909; LIB: 652; LEAD: 257; 5/10
                DISTRICT 9: PC: 807; GRN: 533; LEAD: 274; 5/11
                DISTRICT 6: PC: 995; LIB: 684; LEAD: 311; 5/9
                DISTRICT 16: LIB: 1,286; GRN: 819; LEAD: 467; 5/10
            """.trimIndent(),
        )

        setupFullResults(candidateVotesRaw, partyResultsRaw, HashMap(), pollsReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pollsReporting.submit(pollsReportingRaw)
        compareRendering("TooCloseToCallScreen", "PollsReporting-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )
    }

    @Test
    fun testLimitRows() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val pctReportingRaw: MutableMap<Int, Double> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val pctReporting = Publisher<Map<Int, Double>>(pctReportingRaw)
        val screen = TooCloseToCallScreen.of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
            .withPctReporting { pctReporting.map { p -> p[it] ?: 0.0 } }
            .withMaxRows(15.asOneTimePublisher())
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "LimitRows-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )

        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "LimitRows-2", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217; 10.0% IN
            """.trimIndent(),
        )

        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "LimitRows-3", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 12: LIB: 478; GRN: 475; LEAD: 3; 10.0% IN
                DISTRICT 23: PC: 436; GRN: 428; LEAD: 8; 10.0% IN
                DISTRICT 8: LIB: 620; PC: 609; LEAD: 11; 10.0% IN
                DISTRICT 26: PC: 700; LIB: 686; LEAD: 14; 10.0% IN
                DISTRICT 5: PC: 533; LIB: 518; LEAD: 15; 12.5% IN
                DISTRICT 25: LIB: 454; NDP: 425; LEAD: 29; 9.1% IN
                DISTRICT 21: GRN: 617; PC: 577; LEAD: 40; 10.0% IN
                DISTRICT 11: GRN: 636; PC: 595; LEAD: 41; 10.0% IN
                DISTRICT 22: GRN: 602; LIB: 560; LEAD: 42; 10.0% IN
                DISTRICT 24: LIB: 330; GRN: 197; LEAD: 133; 12.5% IN
                DISTRICT 15: PC: 583; LIB: 425; LEAD: 158; 10.0% IN
                DISTRICT 13: LIB: 717; GRN: 542; LEAD: 175; 10.0% IN
                DISTRICT 6: PC: 725; LIB: 526; LEAD: 199; 11.1% IN
                DISTRICT 14: LIB: 699; PC: 492; LEAD: 207; 10.0% IN
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217; 10.0% IN
            """.trimIndent(),
        )

        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "LimitRows-4", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 26: LIB: 919; PC: 890; LEAD: 29; 50.0% IN
                DISTRICT 13: LIB: 952; GRN: 840; LEAD: 112; 50.0% IN
                DISTRICT 8: PC: 948; LIB: 832; LEAD: 116; 50.0% IN
                DISTRICT 5: GRN: 871; PC: 743; LEAD: 128; 62.5% IN
                DISTRICT 12: GRN: 831; LIB: 639; LEAD: 192; 50.0% IN
                DISTRICT 24: LIB: 774; GRN: 582; LEAD: 192; 62.5% IN
                DISTRICT 14: LIB: 874; GRN: 660; LEAD: 214; 50.0% IN
                DISTRICT 15: PC: 909; LIB: 652; LEAD: 257; 50.0% IN
                DISTRICT 9: PC: 807; GRN: 533; LEAD: 274; 45.5% IN
                DISTRICT 6: PC: 995; LIB: 684; LEAD: 311; 55.6% IN
                DISTRICT 16: LIB: 1,286; GRN: 819; LEAD: 467; 50.0% IN
            """.trimIndent(),
        )

        setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "LimitRows-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )
    }

    @Test
    fun testNumberOfCandidates() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val pctReportingRaw: MutableMap<Int, Double> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val pctReporting = Publisher<Map<Int, Double>>(pctReportingRaw)
        val screen = TooCloseToCallScreen.of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
            .withPctReporting { pctReporting.map { p -> p[it] ?: 0.0 } }
            .withNumberOfCandidates(5.asOneTimePublisher())
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "NumCandidates-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )

        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "NumCandidates-2", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 1: PC: 684; LIB: 467; GRN: 365; LEAD: 217; 10.0% IN
            """.trimIndent(),
        )

        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "NumCandidates-3", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 12: LIB: 478; GRN: 475; PC: 352; NDP: 172; LEAD: 3; 10.0% IN
                DISTRICT 23: PC: 436; GRN: 428; LIB: 379; NDP: 34; LEAD: 8; 10.0% IN
                DISTRICT 8: LIB: 620; PC: 609; GRN: 285; NDP: 22; LEAD: 11; 10.0% IN
                DISTRICT 26: PC: 700; LIB: 686; GRN: 122; NDP: 47; LEAD: 14; 10.0% IN
                DISTRICT 5: PC: 533; LIB: 518; GRN: 482; NDP: 18; LEAD: 15; 12.5% IN
                DISTRICT 25: LIB: 454; NDP: 425; PC: 177; GRN: 62; LEAD: 29; 9.1% IN
                DISTRICT 21: GRN: 617; PC: 577; LIB: 563; NDP: 18; LEAD: 40; 10.0% IN
                DISTRICT 11: GRN: 636; PC: 595; LIB: 534; NDP: 36; LEAD: 41; 10.0% IN
                DISTRICT 22: GRN: 602; LIB: 560; PC: 335; NDP: 34; LEAD: 42; 10.0% IN
                DISTRICT 24: LIB: 330; GRN: 197; PC: 144; NDP: 14; LEAD: 133; 12.5% IN
                DISTRICT 15: PC: 583; LIB: 425; GRN: 287; NDP: 27; LEAD: 158; 10.0% IN
                DISTRICT 13: LIB: 717; GRN: 542; PC: 331; NDP: 75; LEAD: 175; 10.0% IN
                DISTRICT 6: PC: 725; LIB: 526; GRN: 348; NDP: 17; LEAD: 199; 11.1% IN
                (...)
            """.trimIndent(),
        )

        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "NumCandidates-4", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 26: LIB: 919; PC: 890; GRN: 198; NDP: 60; LEAD: 29; 50.0% IN
                DISTRICT 13: LIB: 952; GRN: 840; PC: 437; NDP: 92; LEAD: 112; 50.0% IN
                DISTRICT 8: PC: 948; LIB: 832; GRN: 490; NDP: 34; LEAD: 116; 50.0% IN
                DISTRICT 5: GRN: 871; PC: 743; LIB: 742; NDP: 31; LEAD: 128; 62.5% IN
                DISTRICT 12: GRN: 831; LIB: 639; PC: 479; NDP: 248; LEAD: 192; 50.0% IN
                DISTRICT 24: LIB: 774; GRN: 582; PC: 434; NDP: 27; LEAD: 192; 62.5% IN
                DISTRICT 14: LIB: 874; GRN: 660; PC: 624; IND: 171; NDP: 38; LEAD: 214; 50.0% IN
                DISTRICT 15: PC: 909; LIB: 652; GRN: 567; NDP: 45; LEAD: 257; 50.0% IN
                DISTRICT 9: PC: 807; GRN: 533; LIB: 492; NDP: 38; LEAD: 274; 45.5% IN
                DISTRICT 6: PC: 995; LIB: 684; GRN: 578; NDP: 25; LEAD: 311; 55.6% IN
                DISTRICT 16: LIB: 1,286; GRN: 819; NDP: 652; PC: 431; LEAD: 467; 50.0% IN
            """.trimIndent(),
        )

        setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "NumCandidates-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )
    }

    @Test
    fun testParty() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val screen = TooCloseToCallScreen.ofParty(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> Aggregators.adjustKey(v[it] ?: emptyMap()) { c -> c.party } } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )

        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-2", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217
            """.trimIndent(),
        )

        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-3", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 12: LIB: 478; GRN: 475; LEAD: 3
                DISTRICT 23: PC: 436; GRN: 428; LEAD: 8
                DISTRICT 8: LIB: 620; PC: 609; LEAD: 11
                DISTRICT 26: PC: 700; LIB: 686; LEAD: 14
                DISTRICT 5: PC: 533; LIB: 518; LEAD: 15
                DISTRICT 25: LIB: 454; NDP: 425; LEAD: 29
                DISTRICT 21: GRN: 617; PC: 577; LEAD: 40
                DISTRICT 11: GRN: 636; PC: 595; LEAD: 41
                DISTRICT 22: GRN: 602; LIB: 560; LEAD: 42
                DISTRICT 24: LIB: 330; GRN: 197; LEAD: 133
                DISTRICT 15: PC: 583; LIB: 425; LEAD: 158
                DISTRICT 13: LIB: 717; GRN: 542; LEAD: 175
                DISTRICT 6: PC: 725; LIB: 526; LEAD: 199
                DISTRICT 14: LIB: 699; PC: 492; LEAD: 207
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217
                DISTRICT 9: PC: 620; LIB: 395; LEAD: 225
                DISTRICT 27: LIB: 646; PC: 405; LEAD: 241
                DISTRICT 2: PC: 555; GRN: 308; LEAD: 247
                DISTRICT 10: LIB: 808; GRN: 516; LEAD: 292
                DISTRICT 16: LIB: 983; GRN: 542; LEAD: 441
            """.trimIndent(),
        )

        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-4", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 26: LIB: 919; PC: 890; LEAD: 29
                DISTRICT 13: LIB: 952; GRN: 840; LEAD: 112
                DISTRICT 8: PC: 948; LIB: 832; LEAD: 116
                DISTRICT 5: GRN: 871; PC: 743; LEAD: 128
                DISTRICT 12: GRN: 831; LIB: 639; LEAD: 192
                DISTRICT 24: LIB: 774; GRN: 582; LEAD: 192
                DISTRICT 14: LIB: 874; GRN: 660; LEAD: 214
                DISTRICT 15: PC: 909; LIB: 652; LEAD: 257
                DISTRICT 9: PC: 807; GRN: 533; LEAD: 274
                DISTRICT 6: PC: 995; LIB: 684; LEAD: 311
                DISTRICT 16: LIB: 1,286; GRN: 819; LEAD: 467
            """.trimIndent(),
        )

        setupFullResults(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )
    }

    @Test
    fun testNullResults() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> =
            (1..27).associateWith { emptyMap<Candidate, Int>() }.toMutableMap()
        val partyResultsRaw: MutableMap<Int, PartyResult?> = (1..27).associateWith { null }.toMutableMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult?>>(partyResultsRaw)
        val screen = TooCloseToCallScreen.ofParty(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> Aggregators.adjustKey(v[it] ?: emptyMap()) { c -> c.party } } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )

        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-2", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217
            """.trimIndent(),
        )

        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-3", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 12: LIB: 478; GRN: 475; LEAD: 3
                DISTRICT 23: PC: 436; GRN: 428; LEAD: 8
                DISTRICT 8: LIB: 620; PC: 609; LEAD: 11
                DISTRICT 26: PC: 700; LIB: 686; LEAD: 14
                DISTRICT 5: PC: 533; LIB: 518; LEAD: 15
                DISTRICT 25: LIB: 454; NDP: 425; LEAD: 29
                DISTRICT 21: GRN: 617; PC: 577; LEAD: 40
                DISTRICT 11: GRN: 636; PC: 595; LEAD: 41
                DISTRICT 22: GRN: 602; LIB: 560; LEAD: 42
                DISTRICT 24: LIB: 330; GRN: 197; LEAD: 133
                DISTRICT 15: PC: 583; LIB: 425; LEAD: 158
                DISTRICT 13: LIB: 717; GRN: 542; LEAD: 175
                DISTRICT 6: PC: 725; LIB: 526; LEAD: 199
                DISTRICT 14: LIB: 699; PC: 492; LEAD: 207
                DISTRICT 1: PC: 684; LIB: 467; LEAD: 217
                DISTRICT 9: PC: 620; LIB: 395; LEAD: 225
                DISTRICT 27: LIB: 646; PC: 405; LEAD: 241
                DISTRICT 2: PC: 555; GRN: 308; LEAD: 247
                DISTRICT 10: LIB: 808; GRN: 516; LEAD: 292
                DISTRICT 16: LIB: 983; GRN: 542; LEAD: 441
            """.trimIndent(),
        )

        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-4", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 26: LIB: 919; PC: 890; LEAD: 29
                DISTRICT 13: LIB: 952; GRN: 840; LEAD: 112
                DISTRICT 8: PC: 948; LIB: 832; LEAD: 116
                DISTRICT 5: GRN: 871; PC: 743; LEAD: 128
                DISTRICT 12: GRN: 831; LIB: 639; LEAD: 192
                DISTRICT 24: LIB: 774; GRN: 582; LEAD: 192
                DISTRICT 14: LIB: 874; GRN: 660; LEAD: 214
                DISTRICT 15: PC: 909; LIB: 652; LEAD: 257
                DISTRICT 9: PC: 807; GRN: 533; LEAD: 274
                DISTRICT 6: PC: 995; LIB: 684; LEAD: 311
                DISTRICT 16: LIB: 1,286; GRN: 819; LEAD: 467
            """.trimIndent(),
        )

        setupFullResults(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )
    }

    private fun <R : PartyResult?> setupFirstAdvancePoll(
        candidateVotes: MutableMap<Int, Map<Candidate, Int>>,
        partyResults: MutableMap<Int, R>,
        pctReporting: MutableMap<Int, Double>,
        pollsReporting: MutableMap<Int, PollsReporting>,
    ) {
        candidateVotes[1] = mapOf(
            Candidate("Tommy Kickham", lib) to 467,
            Candidate("Colin Lavie", pc) to 684,
            Candidate("Boyd Leard", grn) to 365,
        )
        partyResults[1] = leading(pc) as R
        pctReporting[1] = 1.0 / 10
        pollsReporting[1] = PollsReporting(1, 10)
    }

    private fun <R : PartyResult?> setupAllAdvancePolls(
        candidateVotes: MutableMap<Int, Map<Candidate, Int>>,
        partyResults: MutableMap<Int, R>,
        pctReporting: MutableMap<Int, Double>,
        pollsReporting: MutableMap<Int, PollsReporting>,
    ) {
        candidateVotes[1] = mapOf(
            Candidate("Tommy Kickham", lib) to 467,
            Candidate("Colin Lavie", pc) to 684,
            Candidate("Boyd Leard", grn) to 365,
        )
        partyResults[1] = leading(pc) as R
        pctReporting[1] = 1.0 / 10
        pollsReporting[1] = PollsReporting(1, 10)
        candidateVotes[2] = mapOf(
            Candidate("Kevin Doyle", lib) to 288,
            Candidate("Susan Hartley", grn) to 308,
            Candidate("Steven Myers", pc) to 555,
            Candidate("Edith Perry", ndp) to 23,
        )
        partyResults[2] = leading(pc) as R
        pctReporting[2] = 1.0 / 10
        pollsReporting[2] = PollsReporting(1, 10)
        candidateVotes[3] = mapOf(
            Candidate("Billy Cann", ndp) to 78,
            Candidate("Cory Deagle", pc) to 872,
            Candidate("Daphne Griffin", lib) to 451,
            Candidate("John Allen Maclean", grn) to 289,
        )
        partyResults[3] = elected(pc) as R
        pctReporting[3] = 1.0 / 9
        pollsReporting[3] = PollsReporting(1, 9)
        candidateVotes[4] = mapOf(
            Candidate("Darlene Compton", pc) to 588,
            Candidate("Ian MacPherson", lib) to 240,
            Candidate("James Sanders", grn) to 232,
        )
        partyResults[4] = elected(pc) as R
        pctReporting[4] = 1.0 / 10
        pollsReporting[4] = PollsReporting(1, 10)
        candidateVotes[5] = mapOf(
            Candidate("Michele Beaton", grn) to 482,
            Candidate("Randy Cooper", lib) to 518,
            Candidate("Mary Ellen McInnis", pc) to 533,
            Candidate("Lawrence Millar", ndp) to 18,
        )
        partyResults[5] = leading(pc) as R
        pctReporting[5] = 1.0 / 8
        pollsReporting[5] = PollsReporting(1, 8)
        candidateVotes[6] = mapOf(
            Candidate("James Aylward", pc) to 725,
            Candidate("David Dunphy", lib) to 526,
            Candidate("Devon Strang", grn) to 348,
            Candidate("Lynne Thiele", ndp) to 17,
        )
        partyResults[6] = leading(pc) as R
        pctReporting[6] = 1.0 / 9
        pollsReporting[6] = PollsReporting(1, 9)
        candidateVotes[7] = mapOf(
            Candidate("Margaret Andrade", ndp) to 12,
            Candidate("Kyle MacDonald", grn) to 184,
            Candidate("Sidney MacEwan", pc) to 610,
            Candidate("Susan Myers", lib) to 203,
        )
        partyResults[7] = elected(pc) as R
        pctReporting[7] = 1.0 / 11
        pollsReporting[7] = PollsReporting(1, 11)
        candidateVotes[8] = mapOf(
            Candidate("Sarah Donald", grn) to 285,
            Candidate("Wade MacLauchlan", lib) to 620,
            Candidate("Bloyce Thompson", pc) to 609,
            Candidate("Marian White", ndp) to 22,
        )
        partyResults[8] = leading(lib) as R
        pctReporting[8] = 1.0 / 10
        pollsReporting[8] = PollsReporting(1, 10)
        candidateVotes[9] = mapOf(
            Candidate("John Andrew", grn) to 363,
            Candidate("Gordon Gay", ndp) to 19,
            Candidate("Natalie Jameson", pc) to 620,
            Candidate("Karen Lavers", lib) to 395,
        )
        partyResults[9] = leading(pc) as R
        pctReporting[9] = 1.0 / 11
        pollsReporting[9] = PollsReporting(1, 11)
        candidateVotes[10] = mapOf(
            Candidate("Mike Gillis", pc) to 510,
            Candidate("Robert Mitchell", lib) to 808,
            Candidate("Amanda Morrison", grn) to 516,
            Candidate("Jesse Reddin Cousins", ndp) to 27,
        )
        partyResults[10] = leading(lib) as R
        pctReporting[10] = 1.0 / 10
        pollsReporting[10] = PollsReporting(1, 10)
        candidateVotes[11] = mapOf(
            Candidate("Hannah Bell", grn) to 636,
            Candidate("Ronnie Carragher", pc) to 595,
            Candidate("Roxanne Carter-Thompson", lib) to 534,
            Candidate("Trevor Leclerc", ndp) to 36,
        )
        partyResults[11] = leading(grn) as R
        pctReporting[11] = 1.0 / 10
        pollsReporting[11] = PollsReporting(1, 10)
        candidateVotes[12] = mapOf(
            Candidate("Karla Bernard", grn) to 475,
            Candidate("Richard Brown", lib) to 478,
            Candidate("Joe Byrne", ndp) to 172,
            Candidate("Tim Keizer", pc) to 352,
        )
        partyResults[12] = leading(lib) as R
        pctReporting[12] = 1.0 / 10
        pollsReporting[12] = PollsReporting(1, 10)
        candidateVotes[13] = mapOf(
            Candidate("Jordan Brown", lib) to 717,
            Candidate("Ole Hammarlund", grn) to 542,
            Candidate("Donna Hurry", pc) to 331,
            Candidate("Simone Webster", ndp) to 75,
        )
        partyResults[13] = leading(lib) as R
        pctReporting[13] = 1.0 / 10
        pollsReporting[13] = PollsReporting(1, 10)
        candidateVotes[14] = mapOf(
            Candidate("Angus Birt", pc) to 492,
            Candidate("Bush Dumville", ind) to 131,
            Candidate("Gavin Hall", grn) to 437,
            Candidate("Gord MacNeilly", lib) to 699,
            Candidate("Janis Newman", ndp) to 34,
        )
        partyResults[14] = leading(lib) as R
        pctReporting[14] = 1.0 / 10
        pollsReporting[14] = PollsReporting(1, 10)
        candidateVotes[15] = mapOf(
            Candidate("Greg Bradley", grn) to 287,
            Candidate("Leah-Jane Hayward", ndp) to 27,
            Candidate("Dennis King", pc) to 583,
            Candidate("Windsor Wight", lib) to 425,
        )
        partyResults[15] = leading(pc) as R
        pctReporting[15] = 1.0 / 10
        pollsReporting[15] = PollsReporting(1, 10)
        candidateVotes[16] = mapOf(
            Candidate("Elaine Barnes", pc) to 296,
            Candidate("Ellen Jones", grn) to 542,
            Candidate("Heath MacDonald", lib) to 983,
            Candidate("Craig Nash", ndp) to 425,
        )
        partyResults[16] = leading(lib) as R
        pctReporting[16] = 1.0 / 10
        pollsReporting[16] = PollsReporting(1, 10)
        candidateVotes[17] = mapOf(
            Candidate("Peter Bevan-Baker", grn) to 851,
            Candidate("Kris Currie", pc) to 512,
            Candidate("Judy MacNevin", lib) to 290,
            Candidate("Don Wills", ind) to 7,
        )
        partyResults[17] = elected(grn) as R
        pctReporting[17] = 1.0 / 10
        pollsReporting[17] = PollsReporting(1, 10)
        candidateVotes[18] = mapOf(
            Candidate("Sean Deagle", ndp) to 15,
            Candidate("Colin Jeffrey", grn) to 271,
            Candidate("Sandy MacKay", lib) to 196,
            Candidate("Brad Trivers", pc) to 710,
        )
        partyResults[18] = elected(pc) as R
        pctReporting[18] = 1.0 / 10
        pollsReporting[18] = PollsReporting(1, 10)
        candidateVotes[19] = mapOf(
            Candidate("Jamie Fox", pc) to 647,
            Candidate("Joan Gauvin", ndp) to 7,
            Candidate("Matthew MacFarlane", grn) to 311,
            Candidate("Fred McCardle", ind) to 18,
            Candidate("Jamie Stride", lib) to 167,
        )
        partyResults[19] = elected(pc) as R
        pctReporting[19] = 1.0 / 10
        pollsReporting[19] = PollsReporting(1, 10)
        candidateVotes[20] = mapOf(
            Candidate("Nancy Beth Guptill", lib) to 203,
            Candidate("Carole MacFarlane", ndp) to 21,
            Candidate("Matthew MacKay", pc) to 1166,
            Candidate("Matthew J. MacKay", grn) to 342,
        )
        partyResults[20] = elected(pc) as R
        pctReporting[20] = 1.0 / 10
        pollsReporting[20] = PollsReporting(1, 10)
        candidateVotes[21] = mapOf(
            Candidate("Tyler Desroches", pc) to 577,
            Candidate("Paulette Halupa", ndp) to 18,
            Candidate("Lynne Lund", grn) to 617,
            Candidate("Chris Palmer", lib) to 563,
        )
        partyResults[21] = leading(grn) as R
        pctReporting[21] = 1.0 / 10
        pollsReporting[21] = PollsReporting(1, 10)
        candidateVotes[22] = mapOf(
            Candidate("Steve Howard", grn) to 602,
            Candidate("Tina Mundy", lib) to 560,
            Candidate("Garth Oatway", ndp) to 34,
            Candidate("Paul Walsh", pc) to 335,
        )
        partyResults[22] = leading(grn) as R
        pctReporting[22] = 1.0 / 10
        pollsReporting[22] = PollsReporting(1, 10)
        candidateVotes[23] = mapOf(
            Candidate("Trish Altass", grn) to 428,
            Candidate("Paula Biggar", lib) to 379,
            Candidate("Robin John Robert Ednman", ndp) to 34,
            Candidate("Holton A MacLennan", pc) to 436,
        )
        partyResults[23] = leading(grn) as R
        pctReporting[23] = 1.0 / 10
        pollsReporting[23] = PollsReporting(1, 10)
        candidateVotes[24] = mapOf(
            Candidate("Nick Arsenault", grn) to 197,
            Candidate("Sonny Gallant", lib) to 330,
            Candidate("Grant Gallant", ndp) to 14,
            Candidate("Jaosn Woodbury", pc) to 144,
        )
        partyResults[24] = leading(lib) as R
        pctReporting[24] = 1.0 / 8
        pollsReporting[24] = PollsReporting(1, 8)
        candidateVotes[25] = mapOf(
            Candidate("Barb Broome", pc) to 177,
            Candidate("Jason Charette", grn) to 62,
            Candidate("Dr. Herb Dickieson", ndp) to 425,
            Candidate("Robert Henderson", lib) to 454,
        )
        partyResults[25] = leading(lib) as R
        pctReporting[25] = 1.0 / 11
        pollsReporting[25] = PollsReporting(1, 11)
        candidateVotes[26] = mapOf(
            Candidate("Michelle Arsenault", ndp) to 47,
            Candidate("Ernie Hudson", pc) to 700,
            Candidate("James McKenna", grn) to 122,
            Candidate("Pat Murphy", lib) to 686,
        )
        partyResults[26] = leading(pc) as R
        pctReporting[26] = 1.0 / 10
        pollsReporting[26] = PollsReporting(1, 10)
        candidateVotes[27] = mapOf(
            Candidate("Sean Doyle", grn) to 241,
            Candidate("Melissa Handrahan", pc) to 405,
            Candidate("Hal Perry", lib) to 646,
            Candidate("Dale Ryan", ndp) to 18,
        )
        partyResults[27] = leading(lib) as R
        pctReporting[27] = 1.0 / 10
        pollsReporting[27] = PollsReporting(1, 10)
    }

    private fun <R : PartyResult?> setupHalfOfPolls(
        candidateVotes: MutableMap<Int, Map<Candidate, Int>>,
        partyResults: MutableMap<Int, R>,
        pctReporting: MutableMap<Int, Double>,
        pollsReporting: MutableMap<Int, PollsReporting>,
    ) {
        candidateVotes[1] = mapOf(
            Candidate("Tommy Kickham", lib) to 619,
            Candidate("Colin Lavie", pc) to 982,
            Candidate("Boyd Leard", grn) to 577,
        )
        partyResults[1] = elected(pc) as R
        pctReporting[1] = 5.0 / 10
        pollsReporting[1] = PollsReporting(5, 10)
        candidateVotes[2] = mapOf(
            Candidate("Kevin Doyle", lib) to 438,
            Candidate("Susan Hartley", grn) to 571,
            Candidate("Steven Myers", pc) to 1164,
            Candidate("Edith Perry", ndp) to 34,
        )
        partyResults[2] = elected(pc) as R
        pctReporting[2] = 5.0 / 10
        pollsReporting[2] = PollsReporting(5, 10)
        candidateVotes[3] = mapOf(
            Candidate("Billy Cann", ndp) to 93,
            Candidate("Cory Deagle", pc) to 1115,
            Candidate("Daphne Griffin", lib) to 606,
            Candidate("John Allen Maclean", grn) to 468,
        )
        partyResults[3] = elected(pc) as R
        pctReporting[3] = 5.0 / 9
        pollsReporting[3] = PollsReporting(5, 9)
        candidateVotes[4] = mapOf(
            Candidate("Darlene Compton", pc) to 1028,
            Candidate("Ian MacPherson", lib) to 415,
            Candidate("James Sanders", grn) to 498,
        )
        partyResults[4] = elected(pc) as R
        pctReporting[4] = 5.0 / 10
        pollsReporting[4] = PollsReporting(5, 10)
        candidateVotes[5] = mapOf(
            Candidate("Michele Beaton", grn) to 871,
            Candidate("Randy Cooper", lib) to 742,
            Candidate("Mary Ellen McInnis", pc) to 743,
            Candidate("Lawrence Millar", ndp) to 31,
        )
        partyResults[5] = leading(grn) as R
        pctReporting[5] = 5.0 / 8
        pollsReporting[5] = PollsReporting(5, 8)
        candidateVotes[6] = mapOf(
            Candidate("James Aylward", pc) to 995,
            Candidate("David Dunphy", lib) to 684,
            Candidate("Devon Strang", grn) to 578,
            Candidate("Lynne Thiele", ndp) to 25,
        )
        partyResults[6] = leading(pc) as R
        pctReporting[6] = 5.0 / 9
        pollsReporting[6] = PollsReporting(5, 9)
        candidateVotes[7] = mapOf(
            Candidate("Margaret Andrade", ndp) to 22,
            Candidate("Kyle MacDonald", grn) to 369,
            Candidate("Sidney MacEwan", pc) to 1190,
            Candidate("Susan Myers", lib) to 359,
        )
        partyResults[7] = elected(pc) as R
        pctReporting[7] = 5.0 / 11
        pollsReporting[7] = PollsReporting(5, 11)
        candidateVotes[8] = mapOf(
            Candidate("Sarah Donald", grn) to 490,
            Candidate("Wade MacLauchlan", lib) to 832,
            Candidate("Bloyce Thompson", pc) to 948,
            Candidate("Marian White", ndp) to 34,
        )
        partyResults[8] = leading(pc) as R
        pctReporting[8] = 5.0 / 10
        pollsReporting[8] = PollsReporting(5, 10)
        candidateVotes[9] = mapOf(
            Candidate("John Andrew", grn) to 533,
            Candidate("Gordon Gay", ndp) to 38,
            Candidate("Natalie Jameson", pc) to 807,
            Candidate("Karen Lavers", lib) to 492,
        )
        partyResults[9] = leading(pc) as R
        pctReporting[9] = 5.0 / 11
        pollsReporting[9] = PollsReporting(5, 11)
        candidateVotes[10] = mapOf(
            Candidate("Mike Gillis", pc) to 614,
            Candidate("Robert Mitchell", lib) to 1098,
            Candidate("Amanda Morrison", grn) to 759,
            Candidate("Jesse Reddin Cousins", ndp) to 32,
        )
        partyResults[10] = elected(lib) as R
        pctReporting[10] = 5.0 / 10
        pollsReporting[10] = PollsReporting(5, 10)
        candidateVotes[11] = mapOf(
            Candidate("Hannah Bell", grn) to 922,
            Candidate("Ronnie Carragher", pc) to 769,
            Candidate("Roxanne Carter-Thompson", lib) to 678,
            Candidate("Trevor Leclerc", ndp) to 44,
        )
        partyResults[11] = elected(grn) as R
        pctReporting[11] = 5.0 / 10
        pollsReporting[11] = PollsReporting(5, 10)
        candidateVotes[12] = mapOf(
            Candidate("Karla Bernard", grn) to 831,
            Candidate("Richard Brown", lib) to 639,
            Candidate("Joe Byrne", ndp) to 248,
            Candidate("Tim Keizer", pc) to 479,
        )
        partyResults[12] = leading(grn) as R
        pctReporting[12] = 5.0 / 10
        pollsReporting[12] = PollsReporting(5, 10)
        candidateVotes[13] = mapOf(
            Candidate("Jordan Brown", lib) to 952,
            Candidate("Ole Hammarlund", grn) to 840,
            Candidate("Donna Hurry", pc) to 437,
            Candidate("Simone Webster", ndp) to 92,
        )
        partyResults[13] = leading(lib) as R
        pctReporting[13] = 5.0 / 10
        pollsReporting[13] = PollsReporting(5, 10)
        candidateVotes[14] = mapOf(
            Candidate("Angus Birt", pc) to 624,
            Candidate("Bush Dumville", ind) to 171,
            Candidate("Gavin Hall", grn) to 660,
            Candidate("Gord MacNeilly", lib) to 874,
            Candidate("Janis Newman", ndp) to 38,
        )
        partyResults[14] = leading(lib) as R
        pctReporting[14] = 5.0 / 10
        pollsReporting[14] = PollsReporting(5, 10)
        candidateVotes[15] = mapOf(
            Candidate("Greg Bradley", grn) to 567,
            Candidate("Leah-Jane Hayward", ndp) to 45,
            Candidate("Dennis King", pc) to 909,
            Candidate("Windsor Wight", lib) to 652,
        )
        partyResults[15] = leading(pc) as R
        pctReporting[15] = 5.0 / 10
        pollsReporting[15] = PollsReporting(5, 10)
        candidateVotes[16] = mapOf(
            Candidate("Elaine Barnes", pc) to 431,
            Candidate("Ellen Jones", grn) to 819,
            Candidate("Heath MacDonald", lib) to 1286,
            Candidate("Craig Nash", ndp) to 652,
        )
        partyResults[16] = leading(lib) as R
        pctReporting[16] = 5.0 / 10
        pollsReporting[16] = PollsReporting(5, 10)
        candidateVotes[17] = mapOf(
            Candidate("Peter Bevan-Baker", grn) to 1357,
            Candidate("Kris Currie", pc) to 799,
            Candidate("Judy MacNevin", lib) to 421,
            Candidate("Don Wills", ind) to 12,
        )
        partyResults[17] = elected(grn) as R
        pctReporting[17] = 5.0 / 10
        pollsReporting[17] = PollsReporting(5, 10)
        candidateVotes[18] = mapOf(
            Candidate("Sean Deagle", ndp) to 22,
            Candidate("Colin Jeffrey", grn) to 551,
            Candidate("Sandy MacKay", lib) to 330,
            Candidate("Brad Trivers", pc) to 1224,
        )
        partyResults[18] = elected(pc) as R
        pctReporting[18] = 5.0 / 10
        pollsReporting[18] = PollsReporting(5, 10)
        candidateVotes[19] = mapOf(
            Candidate("Jamie Fox", pc) to 1059,
            Candidate("Joan Gauvin", ndp) to 12,
            Candidate("Matthew MacFarlane", grn) to 684,
            Candidate("Fred McCardle", ind) to 26,
            Candidate("Jamie Stride", lib) to 280,
        )
        partyResults[19] = elected(pc) as R
        pctReporting[19] = 5.0 / 10
        pollsReporting[19] = PollsReporting(5, 10)
        candidateVotes[20] = mapOf(
            Candidate("Nancy Beth Guptill", lib) to 277,
            Candidate("Carole MacFarlane", ndp) to 26,
            Candidate("Matthew MacKay", pc) to 1584,
            Candidate("Matthew J. MacKay", grn) to 550,
        )
        partyResults[20] = elected(pc) as R
        pctReporting[20] = 5.0 / 10
        pollsReporting[20] = PollsReporting(5, 10)
        candidateVotes[21] = mapOf(
            Candidate("Tyler Desroches", pc) to 794,
            Candidate("Paulette Halupa", ndp) to 29,
            Candidate("Lynne Lund", grn) to 899,
            Candidate("Chris Palmer", lib) to 713,
        )
        partyResults[21] = elected(grn) as R
        pctReporting[21] = 5.0 / 10
        pollsReporting[21] = PollsReporting(5, 10)
        candidateVotes[22] = mapOf(
            Candidate("Steve Howard", grn) to 885,
            Candidate("Tina Mundy", lib) to 691,
            Candidate("Garth Oatway", ndp) to 46,
            Candidate("Paul Walsh", pc) to 456,
        )
        partyResults[22] = elected(grn) as R
        pctReporting[22] = 5.0 / 10
        pollsReporting[22] = PollsReporting(5, 10)
        candidateVotes[23] = mapOf(
            Candidate("Trish Altass", grn) to 737,
            Candidate("Paula Biggar", lib) to 549,
            Candidate("Robin John Robert Ednman", ndp) to 49,
            Candidate("Holton A MacLennan", pc) to 647,
        )
        partyResults[23] = elected(grn) as R
        pctReporting[23] = 5.0 / 10
        pollsReporting[23] = PollsReporting(5, 10)
        candidateVotes[24] = mapOf(
            Candidate("Nick Arsenault", grn) to 582,
            Candidate("Sonny Gallant", lib) to 774,
            Candidate("Grant Gallant", ndp) to 27,
            Candidate("Jaosn Woodbury", pc) to 434,
        )
        partyResults[24] = leading(lib) as R
        pctReporting[24] = 5.0 / 8
        pollsReporting[24] = PollsReporting(5, 8)
        candidateVotes[25] = mapOf(
            Candidate("Barb Broome", pc) to 329,
            Candidate("Jason Charette", grn) to 189,
            Candidate("Dr. Herb Dickieson", ndp) to 614,
            Candidate("Robert Henderson", lib) to 820,
        )
        partyResults[25] = elected(lib) as R
        pctReporting[25] = 5.0 / 11
        pollsReporting[25] = PollsReporting(5, 11)
        candidateVotes[26] = mapOf(
            Candidate("Michelle Arsenault", ndp) to 60,
            Candidate("Ernie Hudson", pc) to 890,
            Candidate("James McKenna", grn) to 198,
            Candidate("Pat Murphy", lib) to 919,
        )
        partyResults[26] = leading(lib) as R
        pctReporting[26] = 5.0 / 10
        pollsReporting[26] = PollsReporting(5, 10)
        candidateVotes[27] = mapOf(
            Candidate("Sean Doyle", grn) to 360,
            Candidate("Melissa Handrahan", pc) to 530,
            Candidate("Hal Perry", lib) to 913,
            Candidate("Dale Ryan", ndp) to 20,
        )
        partyResults[27] = elected(lib) as R
        pctReporting[27] = 5.0 / 10
        pollsReporting[27] = PollsReporting(5, 10)
    }

    private fun <R : PartyResult?> setupFullResults(
        candidateVotes: MutableMap<Int, Map<Candidate, Int>>,
        partyResults: MutableMap<Int, R>,
        pctReporting: MutableMap<Int, Double>,
        pollsReporting: MutableMap<Int, PollsReporting>,
    ) {
        candidateVotes[1] = mapOf(
            Candidate("Tommy Kickham", lib) to 861,
            Candidate("Colin Lavie", pc) to 1347,
            Candidate("Boyd Leard", grn) to 804,
        )
        partyResults[1] = elected(pc) as R
        pctReporting[1] = 10.0 / 10
        pollsReporting[1] = PollsReporting(10, 10)
        candidateVotes[2] = mapOf(
            Candidate("Kevin Doyle", lib) to 663,
            Candidate("Susan Hartley", grn) to 865,
            Candidate("Steven Myers", pc) to 1493,
            Candidate("Edith Perry", ndp) to 49,
        )
        partyResults[2] = elected(pc) as R
        pctReporting[2] = 10.0 / 10
        pollsReporting[2] = PollsReporting(10, 10)
        candidateVotes[3] = mapOf(
            Candidate("Billy Cann", ndp) to 124,
            Candidate("Cory Deagle", pc) to 1373,
            Candidate("Daphne Griffin", lib) to 785,
            Candidate("John Allen Maclean", grn) to 675,
        )
        partyResults[3] = elected(pc) as R
        pctReporting[3] = 9.0 / 9
        pollsReporting[3] = PollsReporting(9, 9)
        candidateVotes[4] = mapOf(
            Candidate("Darlene Compton", pc) to 1545,
            Candidate("Ian MacPherson", lib) to 615,
            Candidate("James Sanders", grn) to 781,
        )
        partyResults[4] = elected(pc) as R
        pctReporting[4] = 10.0 / 10
        pollsReporting[4] = PollsReporting(10, 10)
        candidateVotes[5] = mapOf(
            Candidate("Michele Beaton", grn) to 1152,
            Candidate("Randy Cooper", lib) to 902,
            Candidate("Mary Ellen McInnis", pc) to 943,
            Candidate("Lawrence Millar", ndp) to 38,
        )
        partyResults[5] = elected(grn) as R
        pctReporting[5] = 8.0 / 8
        pollsReporting[5] = PollsReporting(8, 8)
        candidateVotes[6] = mapOf(
            Candidate("James Aylward", pc) to 1270,
            Candidate("David Dunphy", lib) to 882,
            Candidate("Devon Strang", grn) to 805,
            Candidate("Lynne Thiele", ndp) to 31,
        )
        partyResults[6] = elected(pc) as R
        pctReporting[6] = 9.0 / 9
        pollsReporting[6] = PollsReporting(9, 9)
        candidateVotes[7] = mapOf(
            Candidate("Margaret Andrade", ndp) to 35,
            Candidate("Kyle MacDonald", grn) to 697,
            Candidate("Sidney MacEwan", pc) to 1752,
            Candidate("Susan Myers", lib) to 557,
        )
        partyResults[7] = elected(pc) as R
        pctReporting[7] = 11.0 / 11
        pollsReporting[7] = PollsReporting(11, 11)
        candidateVotes[8] = mapOf(
            Candidate("Sarah Donald", grn) to 747,
            Candidate("Wade MacLauchlan", lib) to 1196,
            Candidate("Bloyce Thompson", pc) to 1300,
            Candidate("Marian White", ndp) to 46,
        )
        partyResults[8] = elected(pc) as R
        pctReporting[8] = 10.0 / 10
        pollsReporting[8] = PollsReporting(10, 10)
        candidateVotes[9] = mapOf(
            Candidate("John Andrew", grn) to 709,
            Candidate("Gordon Gay", ndp) to 46,
            Candidate("Natalie Jameson", pc) to 1080,
            Candidate("Karen Lavers", lib) to 635,
        )
        partyResults[9] = elected(pc) as R
        pctReporting[9] = 11.0 / 11
        pollsReporting[9] = PollsReporting(11, 11)
        candidateVotes[10] = mapOf(
            Candidate("Mike Gillis", pc) to 865,
            Candidate("Robert Mitchell", lib) to 1420,
            Candidate("Amanda Morrison", grn) to 1058,
            Candidate("Jesse Reddin Cousins", ndp) to 41,
        )
        partyResults[10] = elected(lib) as R
        pctReporting[10] = 10.0 / 10
        pollsReporting[10] = PollsReporting(10, 10)
        candidateVotes[11] = mapOf(
            Candidate("Hannah Bell", grn) to 1286,
            Candidate("Ronnie Carragher", pc) to 998,
            Candidate("Roxanne Carter-Thompson", lib) to 846,
            Candidate("Trevor Leclerc", ndp) to 55,
        )
        partyResults[11] = elected(grn) as R
        pctReporting[11] = 10.0 / 10
        pollsReporting[11] = PollsReporting(10, 10)
        candidateVotes[12] = mapOf(
            Candidate("Karla Bernard", grn) to 1272,
            Candidate("Richard Brown", lib) to 875,
            Candidate("Joe Byrne", ndp) to 338,
            Candidate("Tim Keizer", pc) to 656,
        )
        partyResults[12] = elected(grn) as R
        pctReporting[12] = 10.0 / 10
        pollsReporting[12] = PollsReporting(10, 10)
        candidateVotes[13] = mapOf(
            Candidate("Jordan Brown", lib) to 1223,
            Candidate("Ole Hammarlund", grn) to 1301,
            Candidate("Donna Hurry", pc) to 567,
            Candidate("Simone Webster", ndp) to 138,
        )
        partyResults[13] = elected(grn) as R
        pctReporting[13] = 10.0 / 10
        pollsReporting[13] = PollsReporting(10, 10)
        candidateVotes[14] = mapOf(
            Candidate("Angus Birt", pc) to 766,
            Candidate("Bush Dumville", ind) to 202,
            Candidate("Gavin Hall", grn) to 966,
            Candidate("Gord MacNeilly", lib) to 1079,
            Candidate("Janis Newman", ndp) to 56,
        )
        partyResults[14] = elected(lib) as R
        pctReporting[14] = 10.0 / 10
        pollsReporting[14] = PollsReporting(10, 10)
        candidateVotes[15] = mapOf(
            Candidate("Greg Bradley", grn) to 879,
            Candidate("Leah-Jane Hayward", ndp) to 57,
            Candidate("Dennis King", pc) to 1315,
            Candidate("Windsor Wight", lib) to 899,
        )
        partyResults[15] = elected(pc) as R
        pctReporting[15] = 10.0 / 10
        pollsReporting[15] = PollsReporting(10, 10)
        candidateVotes[16] = mapOf(
            Candidate("Elaine Barnes", pc) to 602,
            Candidate("Ellen Jones", grn) to 1137,
            Candidate("Heath MacDonald", lib) to 1643,
            Candidate("Craig Nash", ndp) to 899,
        )
        partyResults[16] = elected(lib) as R
        pctReporting[16] = 10.0 / 10
        pollsReporting[16] = PollsReporting(10, 10)
        candidateVotes[17] = mapOf(
            Candidate("Peter Bevan-Baker", grn) to 1870,
            Candidate("Kris Currie", pc) to 1068,
            Candidate("Judy MacNevin", lib) to 515,
            Candidate("Don Wills", ind) to 26,
        )
        partyResults[17] = elected(grn) as R
        pctReporting[17] = 10.0 / 10
        pollsReporting[17] = PollsReporting(10, 10)
        candidateVotes[18] = mapOf(
            Candidate("Sean Deagle", ndp) to 30,
            Candidate("Colin Jeffrey", grn) to 899,
            Candidate("Sandy MacKay", lib) to 489,
            Candidate("Brad Trivers", pc) to 1920,
        )
        partyResults[18] = elected(pc) as R
        pctReporting[18] = 10.0 / 10
        pollsReporting[18] = PollsReporting(10, 10)
        candidateVotes[19] = mapOf(
            Candidate("Jamie Fox", pc) to 1680,
            Candidate("Joan Gauvin", ndp) to 32,
            Candidate("Matthew MacFarlane", grn) to 1041,
            Candidate("Fred McCardle", ind) to 54,
            Candidate("Jamie Stride", lib) to 417,
        )
        partyResults[19] = elected(pc) as R
        pctReporting[19] = 10.0 / 10
        pollsReporting[19] = PollsReporting(10, 10)
        candidateVotes[20] = mapOf(
            Candidate("Nancy Beth Guptill", lib) to 389,
            Candidate("Carole MacFarlane", ndp) to 31,
            Candidate("Matthew MacKay", pc) to 2008,
            Candidate("Matthew J. MacKay", grn) to 805,
        )
        partyResults[20] = elected(pc) as R
        pctReporting[20] = 10.0 / 10
        pollsReporting[20] = PollsReporting(10, 10)
        candidateVotes[21] = mapOf(
            Candidate("Tyler Desroches", pc) to 1037,
            Candidate("Paulette Halupa", ndp) to 39,
            Candidate("Lynne Lund", grn) to 1258,
            Candidate("Chris Palmer", lib) to 892,
        )
        partyResults[21] = elected(grn) as R
        pctReporting[21] = 10.0 / 10
        pollsReporting[21] = PollsReporting(10, 10)
        candidateVotes[22] = mapOf(
            Candidate("Steve Howard", grn) to 1302,
            Candidate("Tina Mundy", lib) to 938,
            Candidate("Garth Oatway", ndp) to 65,
            Candidate("Paul Walsh", pc) to 662,
        )
        partyResults[22] = elected(grn) as R
        pctReporting[22] = 10.0 / 10
        pollsReporting[22] = PollsReporting(10, 10)
        candidateVotes[23] = mapOf(
            Candidate("Trish Altass", grn) to 1101,
            Candidate("Paula Biggar", lib) to 882,
            Candidate("Robin John Robert Ednman", ndp) to 81,
            Candidate("Holton A MacLennan", pc) to 1026,
        )
        partyResults[23] = elected(grn) as R
        pctReporting[23] = 10.0 / 10
        pollsReporting[23] = PollsReporting(10, 10)
        candidateVotes[24] = mapOf(
            Candidate("Nick Arsenault", grn) to 761,
            Candidate("Sonny Gallant", lib) to 1100,
            Candidate("Grant Gallant", ndp) to 33,
            Candidate("Jaosn Woodbury", pc) to 575,
        )
        partyResults[24] = elected(lib) as R
        pctReporting[24] = 8.0 / 8
        pollsReporting[24] = PollsReporting(8, 8)
        candidateVotes[25] = mapOf(
            Candidate("Barb Broome", pc) to 462,
            Candidate("Jason Charette", grn) to 231,
            Candidate("Dr. Herb Dickieson", ndp) to 898,
            Candidate("Robert Henderson", lib) to 1102,
        )
        partyResults[25] = elected(lib) as R
        pctReporting[25] = 11.0 / 11
        pollsReporting[25] = PollsReporting(11, 11)
        candidateVotes[26] = mapOf(
            Candidate("Michelle Arsenault", ndp) to 99,
            Candidate("Ernie Hudson", pc) to 1312,
            Candidate("James McKenna", grn) to 317,
            Candidate("Pat Murphy", lib) to 1153,
        )
        partyResults[26] = elected(lib) as R
        pctReporting[26] = 10.0 / 10
        pollsReporting[26] = PollsReporting(10, 10)
        candidateVotes[27] = mapOf(
            Candidate("Sean Doyle", grn) to 584,
            Candidate("Melissa Handrahan", pc) to 802,
            Candidate("Hal Perry", lib) to 1388,
            Candidate("Dale Ryan", ndp) to 44,
        )
        partyResults[27] = elected(lib) as R
        pctReporting[27] = 10.0 / 10
        pollsReporting[27] = PollsReporting(10, 10)
    }

    @Test
    fun testPct() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val screen = TooCloseToCallScreen.of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher(),
        )
            .sortByPcts()
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "Pct-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )

        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Pct-2", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 1: PC: 45.1%; LIB: 30.8%; LEAD: 14.3%
            """.trimIndent(),
        )

        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Pct-3", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 12: LIB: 32.4%; GRN: 32.2%; LEAD: 0.2%
                DISTRICT 23: PC: 34.1%; GRN: 33.5%; LEAD: 0.6%
                DISTRICT 8: LIB: 40.4%; PC: 39.6%; LEAD: 0.7%
                DISTRICT 26: PC: 45.0%; LIB: 44.1%; LEAD: 0.9%
                DISTRICT 5: PC: 34.4%; LIB: 33.4%; LEAD: 1.0%
                DISTRICT 21: GRN: 34.8%; PC: 32.5%; LEAD: 2.3%
                DISTRICT 11: GRN: 35.3%; PC: 33.0%; LEAD: 2.3%
                DISTRICT 25: LIB: 40.6%; NDP: 38.0%; LEAD: 2.6%
                DISTRICT 22: GRN: 39.3%; LIB: 36.6%; LEAD: 2.7%
                DISTRICT 13: LIB: 43.1%; GRN: 32.6%; LEAD: 10.5%
                DISTRICT 14: LIB: 39.0%; PC: 27.4%; LEAD: 11.5%
                DISTRICT 15: PC: 44.1%; LIB: 32.1%; LEAD: 12.0%
                DISTRICT 6: PC: 44.9%; LIB: 32.5%; LEAD: 12.3%
                DISTRICT 1: PC: 45.1%; LIB: 30.8%; LEAD: 14.3%
                DISTRICT 10: LIB: 43.4%; GRN: 27.7%; LEAD: 15.7%
                DISTRICT 9: PC: 44.4%; LIB: 28.3%; LEAD: 16.1%
                DISTRICT 27: LIB: 49.3%; PC: 30.9%; LEAD: 18.4%
                DISTRICT 24: LIB: 48.2%; GRN: 28.8%; LEAD: 19.4%
                DISTRICT 16: LIB: 43.8%; GRN: 24.1%; LEAD: 19.6%
                (...)
            """.trimIndent(),
        )

        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Pct-4", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                DISTRICT 26: LIB: 44.5%; PC: 43.1%; LEAD: 1.4%
                DISTRICT 13: LIB: 41.0%; GRN: 36.2%; LEAD: 4.8%
                DISTRICT 8: PC: 41.1%; LIB: 36.1%; LEAD: 5.0%
                DISTRICT 5: GRN: 36.5%; PC: 31.1%; LEAD: 5.4%
                DISTRICT 12: GRN: 37.8%; LIB: 29.1%; LEAD: 8.7%
                DISTRICT 14: LIB: 36.9%; GRN: 27.9%; LEAD: 9.0%
                DISTRICT 24: LIB: 42.6%; GRN: 32.0%; LEAD: 10.6%
                DISTRICT 15: PC: 41.8%; LIB: 30.0%; LEAD: 11.8%
                DISTRICT 6: PC: 43.6%; LIB: 30.0%; LEAD: 13.6%
                DISTRICT 16: LIB: 40.3%; GRN: 25.7%; LEAD: 14.6%
                DISTRICT 9: PC: 43.2%; GRN: 28.5%; LEAD: 14.7%
            """.trimIndent(),
        )

        setupFullResults(candidateVotesRaw, partyResultsRaw, HashMap(), HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Pct-1", screen)
        assertPublishes(
            screen.altText,
            """
                PRINCE EDWARD ISLAND
                
                TOO CLOSE TO CALL
                (empty)
            """.trimIndent(),
        )
    }

    companion object {
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
    }
}
