package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.TooCloseToCallScreen.Companion.of
import com.joecollins.graphics.screens.generic.TooCloseToCallScreen.Companion.ofParty
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Aggregators.adjustKey
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.models.general.PartyResult.Companion.leading
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.Test
import java.awt.Color

@Suppress("UNCHECKED_CAST")
class TooCloseToCallScreenTest {
    @Test
    fun testBasic() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val screen = of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher()
        )
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-2", screen)
        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-3", screen)
        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-4", screen)
        setupFullResults(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
    }

    @Test
    fun testPctReporting() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val pctReportingRaw: MutableMap<Int, Double> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val pctReporting = Publisher<Map<Int, Double>>(pctReportingRaw)
        val screen = of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher()
        )
            .withPctReporting { pctReporting.map { p -> p[it] ?: 0.0 } }
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "PctReporting-1", screen)
        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "PctReporting-2", screen)
        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "PctReporting-3", screen)
        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "PctReporting-4", screen)
        setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "PctReporting-1", screen)
    }

    @Test
    fun testLimitRows() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val pctReportingRaw: MutableMap<Int, Double> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val pctReporting = Publisher<Map<Int, Double>>(pctReportingRaw)
        val screen = of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher()
        )
            .withPctReporting { pctReporting.map { p -> p[it] ?: 0.0 } }
            .withMaxRows(15.asOneTimePublisher())
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "LimitRows-1", screen)
        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "LimitRows-2", screen)
        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "LimitRows-3", screen)
        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "LimitRows-4", screen)
        setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "LimitRows-1", screen)
    }

    @Test
    fun testNumberOfCandidates() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val pctReportingRaw: MutableMap<Int, Double> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val pctReporting = Publisher<Map<Int, Double>>(pctReportingRaw)
        val screen = of(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> v[it] ?: emptyMap() } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher()
        )
            .withPctReporting { pctReporting.map { p -> p[it] ?: 0.0 } }
            .withNumberOfCandidates(5.asOneTimePublisher())
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "NumCandidates-1", screen)
        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "NumCandidates-2", screen)
        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "NumCandidates-3", screen)
        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "NumCandidates-4", screen)
        setupFullResults(candidateVotesRaw, partyResultsRaw, pctReportingRaw)
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        pctReporting.submit(pctReportingRaw)
        compareRendering("TooCloseToCallScreen", "NumCandidates-1", screen)
    }

    @Test
    fun testParty() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = HashMap()
        val partyResultsRaw: MutableMap<Int, PartyResult> = HashMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult>>(partyResultsRaw)
        val screen = ofParty(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> adjustKey(v[it] ?: emptyMap()) { c -> c.party } } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher()
        )
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-2", screen)
        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-3", screen)
        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-4", screen)
        setupFullResults(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
    }

    @Test
    fun testNullResults() {
        val candidateVotesRaw: MutableMap<Int, Map<Candidate, Int>> = (1..27).associateWith { emptyMap<Candidate, Int>() }.toMutableMap()
        val partyResultsRaw: MutableMap<Int, PartyResult?> = (1..27).associateWith { null }.toMutableMap()
        val candidateVotes = Publisher<Map<Int, Map<Candidate, Int>>>(candidateVotesRaw)
        val partyResults = Publisher<Map<Int, PartyResult?>>(partyResultsRaw)
        val screen = ofParty(
            (1..27).toSet().asOneTimePublisher(),
            { candidateVotes.map { v -> adjustKey(v[it] ?: emptyMap()) { c -> c.party } } },
            { partyResults.map { v -> v[it] } },
            { "DISTRICT $it".asOneTimePublisher() },
            "TOO CLOSE TO CALL".asOneTimePublisher()
        )
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
        setupFirstAdvancePoll(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-2", screen)
        setupAllAdvancePolls(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-3", screen)
        setupHalfOfPolls(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-4", screen)
        setupFullResults(candidateVotesRaw, partyResultsRaw, HashMap())
        candidateVotes.submit(candidateVotesRaw)
        partyResults.submit(partyResultsRaw)
        compareRendering("TooCloseToCallScreen", "Basic-1", screen)
    }

    private fun <R : PartyResult?> setupFirstAdvancePoll(
        candidateVotes: MutableMap<Int, Map<Candidate, Int>>,
        partyResults: MutableMap<Int, R>,
        pctReporting: MutableMap<Int, Double>
    ) {
        candidateVotes[1] = mapOf(
            Candidate("Tommy Kickham", lib) to 467,
            Candidate("Colin Lavie", pc) to 684,
            Candidate("Boyd Leard", grn) to 365
        )
        partyResults[1] = leading(pc) as R
        pctReporting[1] = 1.0 / 10
    }

    private fun <R : PartyResult?> setupAllAdvancePolls(
        candidateVotes: MutableMap<Int, Map<Candidate, Int>>,
        partyResults: MutableMap<Int, R>,
        pctReporting: MutableMap<Int, Double>
    ) {
        candidateVotes[1] = mapOf(
            Candidate("Tommy Kickham", lib) to 467,
            Candidate("Colin Lavie", pc) to 684,
            Candidate("Boyd Leard", grn) to 365
        )
        partyResults[1] = leading(pc) as R
        pctReporting[1] = 1.0 / 10
        candidateVotes[2] = mapOf(
            Candidate("Kevin Doyle", lib) to 288,
            Candidate("Susan Hartley", grn) to 308,
            Candidate("Steven Myers", pc) to 555,
            Candidate("Edith Perry", ndp) to 23
        )
        partyResults[2] = leading(pc) as R
        pctReporting[2] = 1.0 / 10
        candidateVotes[3] = mapOf(
            Candidate("Billy Cann", ndp) to 78,
            Candidate("Cory Deagle", pc) to 872,
            Candidate("Daphne Griffin", lib) to 451,
            Candidate("John Allen Maclean", grn) to 289
        )
        partyResults[3] = elected(pc) as R
        pctReporting[3] = 1.0 / 9
        candidateVotes[4] = mapOf(
            Candidate("Darlene Compton", pc) to 588,
            Candidate("Ian MacPherson", lib) to 240,
            Candidate("James Sanders", grn) to 232
        )
        partyResults[4] = elected(pc) as R
        pctReporting[4] = 1.0 / 10
        candidateVotes[5] = mapOf(
            Candidate("Michele Beaton", grn) to 482,
            Candidate("Randy Cooper", lib) to 518,
            Candidate("Mary Ellen McInnis", pc) to 533,
            Candidate("Lawrence Millar", ndp) to 18
        )
        partyResults[5] = leading(pc) as R
        pctReporting[5] = 1.0 / 8
        candidateVotes[6] = mapOf(
            Candidate("James Aylward", pc) to 725,
            Candidate("David Dunphy", lib) to 526,
            Candidate("Devon Strang", grn) to 348,
            Candidate("Lynne Thiele", ndp) to 17
        )
        partyResults[6] = leading(pc) as R
        pctReporting[6] = 1.0 / 9
        candidateVotes[7] = mapOf(
            Candidate("Margaret Andrade", ndp) to 12,
            Candidate("Kyle MacDonald", grn) to 184,
            Candidate("Sidney MacEwan", pc) to 610,
            Candidate("Susan Myers", lib) to 203
        )
        partyResults[7] = elected(pc) as R
        pctReporting[7] = 1.0 / 11
        candidateVotes[8] = mapOf(
            Candidate("Sarah Donald", grn) to 285,
            Candidate("Wade MacLauchlan", lib) to 620,
            Candidate("Bloyce Thompson", pc) to 609,
            Candidate("Marian White", ndp) to 22
        )
        partyResults[8] = leading(lib) as R
        pctReporting[8] = 1.0 / 10
        candidateVotes[9] = mapOf(
            Candidate("John Andrew", grn) to 363,
            Candidate("Gordon Gay", ndp) to 19,
            Candidate("Natalie Jameson", pc) to 620,
            Candidate("Karen Lavers", lib) to 395
        )
        partyResults[9] = leading(pc) as R
        pctReporting[9] = 1.0 / 11
        candidateVotes[10] = mapOf(
            Candidate("Mike Gillis", pc) to 510,
            Candidate("Robert Mitchell", lib) to 808,
            Candidate("Amanda Morrison", grn) to 516,
            Candidate("Jesse Reddin Cousins", ndp) to 27
        )
        partyResults[10] = leading(lib) as R
        pctReporting[10] = 1.0 / 10
        candidateVotes[11] = mapOf(
            Candidate("Hannah Bell", grn) to 636,
            Candidate("Ronnie Carragher", pc) to 595,
            Candidate("Roxanne Carter-Thompson", lib) to 534,
            Candidate("Trevor Leclerc", ndp) to 36
        )
        partyResults[11] = leading(grn) as R
        pctReporting[11] = 1.0 / 10
        candidateVotes[12] = mapOf(
            Candidate("Karla Bernard", grn) to 475,
            Candidate("Richard Brown", lib) to 478,
            Candidate("Joe Byrne", ndp) to 172,
            Candidate("Tim Keizer", pc) to 352
        )
        partyResults[12] = leading(lib) as R
        pctReporting[12] = 1.0 / 10
        candidateVotes[13] = mapOf(
            Candidate("Jordan Brown", lib) to 717,
            Candidate("Ole Hammarlund", grn) to 542,
            Candidate("Donna Hurry", pc) to 331,
            Candidate("Simone Webster", ndp) to 75
        )
        partyResults[13] = leading(lib) as R
        pctReporting[13] = 1.0 / 10
        candidateVotes[14] = mapOf(
            Candidate("Angus Birt", pc) to 492,
            Candidate("Bush Dumville", ind) to 131,
            Candidate("Gavin Hall", grn) to 437,
            Candidate("Gord MacNeilly", lib) to 699,
            Candidate("Janis Newman", ndp) to 34
        )
        partyResults[14] = leading(lib) as R
        pctReporting[14] = 1.0 / 10
        candidateVotes[15] = mapOf(
            Candidate("Greg Bradley", grn) to 287,
            Candidate("Leah-Jane Hayward", ndp) to 27,
            Candidate("Dennis King", pc) to 583,
            Candidate("Windsor Wight", lib) to 425
        )
        partyResults[15] = leading(pc) as R
        pctReporting[15] = 1.0 / 10
        candidateVotes[16] = mapOf(
            Candidate("Elaine Barnes", pc) to 296,
            Candidate("Ellen Jones", grn) to 542,
            Candidate("Heath MacDonald", lib) to 983,
            Candidate("Craig Nash", ndp) to 425
        )
        partyResults[16] = leading(lib) as R
        pctReporting[16] = 1.0 / 10
        candidateVotes[17] = mapOf(
            Candidate("Peter Bevan-Baker", grn) to 851,
            Candidate("Kris Currie", pc) to 512,
            Candidate("Judy MacNevin", lib) to 290,
            Candidate("Don Wills", ind) to 7
        )
        partyResults[17] = elected(grn) as R
        pctReporting[17] = 1.0 / 10
        candidateVotes[18] = mapOf(
            Candidate("Sean Deagle", ndp) to 15,
            Candidate("Colin Jeffrey", grn) to 271,
            Candidate("Sandy MacKay", lib) to 196,
            Candidate("Brad Trivers", pc) to 710
        )
        partyResults[18] = elected(pc) as R
        pctReporting[18] = 1.0 / 10
        candidateVotes[19] = mapOf(
            Candidate("Jamie Fox", pc) to 647,
            Candidate("Joan Gauvin", ndp) to 7,
            Candidate("Matthew MacFarlane", grn) to 311,
            Candidate("Fred McCardle", ind) to 18,
            Candidate("Jamie Stride", lib) to 167
        )
        partyResults[19] = elected(pc) as R
        pctReporting[19] = 1.0 / 10
        candidateVotes[20] = mapOf(
            Candidate("Nancy Beth Guptill", lib) to 203,
            Candidate("Carole MacFarlane", ndp) to 21,
            Candidate("Matthew MacKay", pc) to 1166,
            Candidate("Matthew J. MacKay", grn) to 342
        )
        partyResults[20] = elected(pc) as R
        pctReporting[20] = 1.0 / 10
        candidateVotes[21] = mapOf(
            Candidate("Tyler Desroches", pc) to 577,
            Candidate("Paulette Halupa", ndp) to 18,
            Candidate("Lynne Lund", grn) to 617,
            Candidate("Chris Palmer", lib) to 563
        )
        partyResults[21] = leading(grn) as R
        pctReporting[21] = 1.0 / 10
        candidateVotes[22] = mapOf(
            Candidate("Steve Howard", grn) to 602,
            Candidate("Tina Mundy", lib) to 560,
            Candidate("Garth Oatway", ndp) to 34,
            Candidate("Paul Walsh", pc) to 335
        )
        partyResults[22] = leading(grn) as R
        pctReporting[22] = 1.0 / 10
        candidateVotes[23] = mapOf(
            Candidate("Trish Altass", grn) to 428,
            Candidate("Paula Biggar", lib) to 379,
            Candidate("Robin John Robert Ednman", ndp) to 34,
            Candidate("Holton A MacLennan", pc) to 436
        )
        partyResults[23] = leading(grn) as R
        pctReporting[23] = 1.0 / 10
        candidateVotes[24] = mapOf(
            Candidate("Nick Arsenault", grn) to 197,
            Candidate("Sonny Gallant", lib) to 330,
            Candidate("Grant Gallant", ndp) to 14,
            Candidate("Jaosn Woodbury", pc) to 144
        )
        partyResults[24] = leading(lib) as R
        pctReporting[24] = 1.0 / 8
        candidateVotes[25] = mapOf(
            Candidate("Barb Broome", pc) to 177,
            Candidate("Jason Charette", grn) to 62,
            Candidate("Dr. Herb Dickieson", ndp) to 425,
            Candidate("Robert Henderson", lib) to 454
        )
        partyResults[25] = leading(lib) as R
        pctReporting[25] = 1.0 / 11
        candidateVotes[26] = mapOf(
            Candidate("Michelle Arsenault", ndp) to 47,
            Candidate("Ernie Hudson", pc) to 700,
            Candidate("James McKenna", grn) to 122,
            Candidate("Pat Murphy", lib) to 686
        )
        partyResults[26] = leading(pc) as R
        pctReporting[26] = 1.0 / 10
        candidateVotes[27] = mapOf(
            Candidate("Sean Doyle", grn) to 241,
            Candidate("Melissa Handrahan", pc) to 405,
            Candidate("Hal Perry", lib) to 646,
            Candidate("Dale Ryan", ndp) to 18
        )
        partyResults[27] = leading(lib) as R
        pctReporting[27] = 1.0 / 10
    }

    private fun <R : PartyResult?> setupHalfOfPolls(
        candidateVotes: MutableMap<Int, Map<Candidate, Int>>,
        partyResults: MutableMap<Int, R>,
        pctReporting: MutableMap<Int, Double>
    ) {
        candidateVotes[1] = mapOf(
            Candidate("Tommy Kickham", lib) to 619,
            Candidate("Colin Lavie", pc) to 982,
            Candidate("Boyd Leard", grn) to 577
        )
        partyResults[1] = elected(pc) as R
        pctReporting[1] = 5.0 / 10
        candidateVotes[2] = mapOf(
            Candidate("Kevin Doyle", lib) to 438,
            Candidate("Susan Hartley", grn) to 571,
            Candidate("Steven Myers", pc) to 1164,
            Candidate("Edith Perry", ndp) to 34
        )
        partyResults[2] = elected(pc) as R
        pctReporting[2] = 5.0 / 10
        candidateVotes[3] = mapOf(
            Candidate("Billy Cann", ndp) to 93,
            Candidate("Cory Deagle", pc) to 1115,
            Candidate("Daphne Griffin", lib) to 606,
            Candidate("John Allen Maclean", grn) to 468
        )
        partyResults[3] = elected(pc) as R
        pctReporting[3] = 5.0 / 9
        candidateVotes[4] = mapOf(
            Candidate("Darlene Compton", pc) to 1028,
            Candidate("Ian MacPherson", lib) to 415,
            Candidate("James Sanders", grn) to 498
        )
        partyResults[4] = elected(pc) as R
        pctReporting[4] = 5.0 / 10
        candidateVotes[5] = mapOf(
            Candidate("Michele Beaton", grn) to 871,
            Candidate("Randy Cooper", lib) to 742,
            Candidate("Mary Ellen McInnis", pc) to 743,
            Candidate("Lawrence Millar", ndp) to 31
        )
        partyResults[5] = leading(grn) as R
        pctReporting[5] = 5.0 / 8
        candidateVotes[6] = mapOf(
            Candidate("James Aylward", pc) to 995,
            Candidate("David Dunphy", lib) to 684,
            Candidate("Devon Strang", grn) to 578,
            Candidate("Lynne Thiele", ndp) to 25
        )
        partyResults[6] = leading(pc) as R
        pctReporting[6] = 5.0 / 9
        candidateVotes[7] = mapOf(
            Candidate("Margaret Andrade", ndp) to 22,
            Candidate("Kyle MacDonald", grn) to 369,
            Candidate("Sidney MacEwan", pc) to 1190,
            Candidate("Susan Myers", lib) to 359
        )
        partyResults[7] = elected(pc) as R
        pctReporting[7] = 5.0 / 11
        candidateVotes[8] = mapOf(
            Candidate("Sarah Donald", grn) to 490,
            Candidate("Wade MacLauchlan", lib) to 832,
            Candidate("Bloyce Thompson", pc) to 948,
            Candidate("Marian White", ndp) to 34
        )
        partyResults[8] = leading(pc) as R
        pctReporting[8] = 5.0 / 10
        candidateVotes[9] = mapOf(
            Candidate("John Andrew", grn) to 533,
            Candidate("Gordon Gay", ndp) to 38,
            Candidate("Natalie Jameson", pc) to 807,
            Candidate("Karen Lavers", lib) to 492
        )
        partyResults[9] = leading(pc) as R
        pctReporting[9] = 5.0 / 11
        candidateVotes[10] = mapOf(
            Candidate("Mike Gillis", pc) to 614,
            Candidate("Robert Mitchell", lib) to 1098,
            Candidate("Amanda Morrison", grn) to 759,
            Candidate("Jesse Reddin Cousins", ndp) to 32
        )
        partyResults[10] = elected(lib) as R
        pctReporting[10] = 5.0 / 10
        candidateVotes[11] = mapOf(
            Candidate("Hannah Bell", grn) to 922,
            Candidate("Ronnie Carragher", pc) to 769,
            Candidate("Roxanne Carter-Thompson", lib) to 678,
            Candidate("Trevor Leclerc", ndp) to 44
        )
        partyResults[11] = elected(grn) as R
        pctReporting[11] = 5.0 / 10
        candidateVotes[12] = mapOf(
            Candidate("Karla Bernard", grn) to 831,
            Candidate("Richard Brown", lib) to 639,
            Candidate("Joe Byrne", ndp) to 248,
            Candidate("Tim Keizer", pc) to 479
        )
        partyResults[12] = leading(grn) as R
        pctReporting[12] = 5.0 / 10
        candidateVotes[13] = mapOf(
            Candidate("Jordan Brown", lib) to 952,
            Candidate("Ole Hammarlund", grn) to 840,
            Candidate("Donna Hurry", pc) to 437,
            Candidate("Simone Webster", ndp) to 92
        )
        partyResults[13] = leading(lib) as R
        pctReporting[13] = 5.0 / 10
        candidateVotes[14] = mapOf(
            Candidate("Angus Birt", pc) to 624,
            Candidate("Bush Dumville", ind) to 171,
            Candidate("Gavin Hall", grn) to 660,
            Candidate("Gord MacNeilly", lib) to 874,
            Candidate("Janis Newman", ndp) to 38
        )
        partyResults[14] = leading(lib) as R
        pctReporting[14] = 5.0 / 10
        candidateVotes[15] = mapOf(
            Candidate("Greg Bradley", grn) to 567,
            Candidate("Leah-Jane Hayward", ndp) to 45,
            Candidate("Dennis King", pc) to 909,
            Candidate("Windsor Wight", lib) to 652
        )
        partyResults[15] = leading(pc) as R
        pctReporting[15] = 5.0 / 10
        candidateVotes[16] = mapOf(
            Candidate("Elaine Barnes", pc) to 431,
            Candidate("Ellen Jones", grn) to 819,
            Candidate("Heath MacDonald", lib) to 1286,
            Candidate("Craig Nash", ndp) to 652
        )
        partyResults[16] = leading(lib) as R
        pctReporting[16] = 5.0 / 10
        candidateVotes[17] = mapOf(
            Candidate("Peter Bevan-Baker", grn) to 1357,
            Candidate("Kris Currie", pc) to 799,
            Candidate("Judy MacNevin", lib) to 421,
            Candidate("Don Wills", ind) to 12
        )
        partyResults[17] = elected(grn) as R
        pctReporting[17] = 5.0 / 10
        candidateVotes[18] = mapOf(
            Candidate("Sean Deagle", ndp) to 22,
            Candidate("Colin Jeffrey", grn) to 551,
            Candidate("Sandy MacKay", lib) to 330,
            Candidate("Brad Trivers", pc) to 1224
        )
        partyResults[18] = elected(pc) as R
        pctReporting[18] = 5.0 / 10
        candidateVotes[19] = mapOf(
            Candidate("Jamie Fox", pc) to 1059,
            Candidate("Joan Gauvin", ndp) to 12,
            Candidate("Matthew MacFarlane", grn) to 684,
            Candidate("Fred McCardle", ind) to 26,
            Candidate("Jamie Stride", lib) to 280
        )
        partyResults[19] = elected(pc) as R
        pctReporting[19] = 5.0 / 10
        candidateVotes[20] = mapOf(
            Candidate("Nancy Beth Guptill", lib) to 277,
            Candidate("Carole MacFarlane", ndp) to 26,
            Candidate("Matthew MacKay", pc) to 1584,
            Candidate("Matthew J. MacKay", grn) to 550
        )
        partyResults[20] = elected(pc) as R
        pctReporting[20] = 5.0 / 10
        candidateVotes[21] = mapOf(
            Candidate("Tyler Desroches", pc) to 794,
            Candidate("Paulette Halupa", ndp) to 29,
            Candidate("Lynne Lund", grn) to 899,
            Candidate("Chris Palmer", lib) to 713
        )
        partyResults[21] = elected(grn) as R
        pctReporting[21] = 5.0 / 10
        candidateVotes[22] = mapOf(
            Candidate("Steve Howard", grn) to 885,
            Candidate("Tina Mundy", lib) to 691,
            Candidate("Garth Oatway", ndp) to 46,
            Candidate("Paul Walsh", pc) to 456
        )
        partyResults[22] = elected(grn) as R
        pctReporting[22] = 5.0 / 10
        candidateVotes[23] = mapOf(
            Candidate("Trish Altass", grn) to 737,
            Candidate("Paula Biggar", lib) to 549,
            Candidate("Robin John Robert Ednman", ndp) to 49,
            Candidate("Holton A MacLennan", pc) to 647
        )
        partyResults[23] = elected(grn) as R
        pctReporting[23] = 5.0 / 10
        candidateVotes[24] = mapOf(
            Candidate("Nick Arsenault", grn) to 582,
            Candidate("Sonny Gallant", lib) to 774,
            Candidate("Grant Gallant", ndp) to 27,
            Candidate("Jaosn Woodbury", pc) to 434
        )
        partyResults[24] = leading(lib) as R
        pctReporting[24] = 5.0 / 8
        candidateVotes[25] = mapOf(
            Candidate("Barb Broome", pc) to 329,
            Candidate("Jason Charette", grn) to 189,
            Candidate("Dr. Herb Dickieson", ndp) to 614,
            Candidate("Robert Henderson", lib) to 820
        )
        partyResults[25] = elected(lib) as R
        pctReporting[25] = 5.0 / 11
        candidateVotes[26] = mapOf(
            Candidate("Michelle Arsenault", ndp) to 60,
            Candidate("Ernie Hudson", pc) to 890,
            Candidate("James McKenna", grn) to 198,
            Candidate("Pat Murphy", lib) to 919
        )
        partyResults[26] = leading(lib) as R
        pctReporting[26] = 5.0 / 10
        candidateVotes[27] = mapOf(
            Candidate("Sean Doyle", grn) to 360,
            Candidate("Melissa Handrahan", pc) to 530,
            Candidate("Hal Perry", lib) to 913,
            Candidate("Dale Ryan", ndp) to 20
        )
        partyResults[27] = elected(lib) as R
        pctReporting[27] = 5.0 / 10
    }

    private fun <R : PartyResult?> setupFullResults(
        candidateVotes: MutableMap<Int, Map<Candidate, Int>>,
        partyResults: MutableMap<Int, R>,
        pctReporting: MutableMap<Int, Double>
    ) {
        candidateVotes[1] = mapOf(
            Candidate("Tommy Kickham", lib) to 861,
            Candidate("Colin Lavie", pc) to 1347,
            Candidate("Boyd Leard", grn) to 804
        )
        partyResults[1] = elected(pc) as R
        pctReporting[1] = 10.0 / 10
        candidateVotes[2] = mapOf(
            Candidate("Kevin Doyle", lib) to 663,
            Candidate("Susan Hartley", grn) to 865,
            Candidate("Steven Myers", pc) to 1493,
            Candidate("Edith Perry", ndp) to 49
        )
        partyResults[2] = elected(pc) as R
        pctReporting[2] = 10.0 / 10
        candidateVotes[3] = mapOf(
            Candidate("Billy Cann", ndp) to 124,
            Candidate("Cory Deagle", pc) to 1373,
            Candidate("Daphne Griffin", lib) to 785,
            Candidate("John Allen Maclean", grn) to 675
        )
        partyResults[3] = elected(pc) as R
        pctReporting[3] = 9.0 / 9
        candidateVotes[4] = mapOf(
            Candidate("Darlene Compton", pc) to 1545,
            Candidate("Ian MacPherson", lib) to 615,
            Candidate("James Sanders", grn) to 781
        )
        partyResults[4] = elected(pc) as R
        pctReporting[4] = 10.0 / 10
        candidateVotes[5] = mapOf(
            Candidate("Michele Beaton", grn) to 1152,
            Candidate("Randy Cooper", lib) to 902,
            Candidate("Mary Ellen McInnis", pc) to 943,
            Candidate("Lawrence Millar", ndp) to 38
        )
        partyResults[5] = elected(grn) as R
        pctReporting[5] = 8.0 / 8
        candidateVotes[6] = mapOf(
            Candidate("James Aylward", pc) to 1270,
            Candidate("David Dunphy", lib) to 882,
            Candidate("Devon Strang", grn) to 805,
            Candidate("Lynne Thiele", ndp) to 31
        )
        partyResults[6] = elected(pc) as R
        pctReporting[6] = 9.0 / 9
        candidateVotes[7] = mapOf(
            Candidate("Margaret Andrade", ndp) to 35,
            Candidate("Kyle MacDonald", grn) to 697,
            Candidate("Sidney MacEwan", pc) to 1752,
            Candidate("Susan Myers", lib) to 557
        )
        partyResults[7] = elected(pc) as R
        pctReporting[7] = 11.0 / 11
        candidateVotes[8] = mapOf(
            Candidate("Sarah Donald", grn) to 747,
            Candidate("Wade MacLauchlan", lib) to 1196,
            Candidate("Bloyce Thompson", pc) to 1300,
            Candidate("Marian White", ndp) to 46
        )
        partyResults[8] = elected(pc) as R
        pctReporting[8] = 10.0 / 10
        candidateVotes[9] = mapOf(
            Candidate("John Andrew", grn) to 709,
            Candidate("Gordon Gay", ndp) to 46,
            Candidate("Natalie Jameson", pc) to 1080,
            Candidate("Karen Lavers", lib) to 635
        )
        partyResults[9] = elected(pc) as R
        pctReporting[9] = 11.0 / 11
        candidateVotes[10] = mapOf(
            Candidate("Mike Gillis", pc) to 865,
            Candidate("Robert Mitchell", lib) to 1420,
            Candidate("Amanda Morrison", grn) to 1058,
            Candidate("Jesse Reddin Cousins", ndp) to 41
        )
        partyResults[10] = elected(lib) as R
        pctReporting[10] = 10.0 / 10
        candidateVotes[11] = mapOf(
            Candidate("Hannah Bell", grn) to 1286,
            Candidate("Ronnie Carragher", pc) to 998,
            Candidate("Roxanne Carter-Thompson", lib) to 846,
            Candidate("Trevor Leclerc", ndp) to 55
        )
        partyResults[11] = elected(grn) as R
        pctReporting[11] = 10.0 / 10
        candidateVotes[12] = mapOf(
            Candidate("Karla Bernard", grn) to 1272,
            Candidate("Richard Brown", lib) to 875,
            Candidate("Joe Byrne", ndp) to 338,
            Candidate("Tim Keizer", pc) to 656
        )
        partyResults[12] = elected(grn) as R
        pctReporting[12] = 10.0 / 10
        candidateVotes[13] = mapOf(
            Candidate("Jordan Brown", lib) to 1223,
            Candidate("Ole Hammarlund", grn) to 1301,
            Candidate("Donna Hurry", pc) to 567,
            Candidate("Simone Webster", ndp) to 138
        )
        partyResults[13] = elected(grn) as R
        pctReporting[13] = 10.0 / 10
        candidateVotes[14] = mapOf(
            Candidate("Angus Birt", pc) to 766,
            Candidate("Bush Dumville", ind) to 202,
            Candidate("Gavin Hall", grn) to 966,
            Candidate("Gord MacNeilly", lib) to 1079,
            Candidate("Janis Newman", ndp) to 56
        )
        partyResults[14] = elected(lib) as R
        pctReporting[14] = 10.0 / 10
        candidateVotes[15] = mapOf(
            Candidate("Greg Bradley", grn) to 879,
            Candidate("Leah-Jane Hayward", ndp) to 57,
            Candidate("Dennis King", pc) to 1315,
            Candidate("Windsor Wight", lib) to 899
        )
        partyResults[15] = elected(pc) as R
        pctReporting[15] = 10.0 / 10
        candidateVotes[16] = mapOf(
            Candidate("Elaine Barnes", pc) to 602,
            Candidate("Ellen Jones", grn) to 1137,
            Candidate("Heath MacDonald", lib) to 1643,
            Candidate("Craig Nash", ndp) to 899
        )
        partyResults[16] = elected(lib) as R
        pctReporting[16] = 10.0 / 10
        candidateVotes[17] = mapOf(
            Candidate("Peter Bevan-Baker", grn) to 1870,
            Candidate("Kris Currie", pc) to 1068,
            Candidate("Judy MacNevin", lib) to 515,
            Candidate("Don Wills", ind) to 26
        )
        partyResults[17] = elected(grn) as R
        pctReporting[17] = 10.0 / 10
        candidateVotes[18] = mapOf(
            Candidate("Sean Deagle", ndp) to 30,
            Candidate("Colin Jeffrey", grn) to 899,
            Candidate("Sandy MacKay", lib) to 489,
            Candidate("Brad Trivers", pc) to 1920
        )
        partyResults[18] = elected(pc) as R
        pctReporting[18] = 10.0 / 10
        candidateVotes[19] = mapOf(
            Candidate("Jamie Fox", pc) to 1680,
            Candidate("Joan Gauvin", ndp) to 32,
            Candidate("Matthew MacFarlane", grn) to 1041,
            Candidate("Fred McCardle", ind) to 54,
            Candidate("Jamie Stride", lib) to 417
        )
        partyResults[19] = elected(pc) as R
        pctReporting[19] = 10.0 / 10
        candidateVotes[20] = mapOf(
            Candidate("Nancy Beth Guptill", lib) to 389,
            Candidate("Carole MacFarlane", ndp) to 31,
            Candidate("Matthew MacKay", pc) to 2008,
            Candidate("Matthew J. MacKay", grn) to 805
        )
        partyResults[20] = elected(pc) as R
        pctReporting[20] = 10.0 / 10
        candidateVotes[21] = mapOf(
            Candidate("Tyler Desroches", pc) to 1037,
            Candidate("Paulette Halupa", ndp) to 39,
            Candidate("Lynne Lund", grn) to 1258,
            Candidate("Chris Palmer", lib) to 892
        )
        partyResults[21] = elected(grn) as R
        pctReporting[21] = 10.0 / 10
        candidateVotes[22] = mapOf(
            Candidate("Steve Howard", grn) to 1302,
            Candidate("Tina Mundy", lib) to 938,
            Candidate("Garth Oatway", ndp) to 65,
            Candidate("Paul Walsh", pc) to 662
        )
        partyResults[22] = elected(grn) as R
        pctReporting[22] = 10.0 / 10
        candidateVotes[23] = mapOf(
            Candidate("Trish Altass", grn) to 1101,
            Candidate("Paula Biggar", lib) to 882,
            Candidate("Robin John Robert Ednman", ndp) to 81,
            Candidate("Holton A MacLennan", pc) to 1026
        )
        partyResults[23] = elected(grn) as R
        pctReporting[23] = 10.0 / 10
        candidateVotes[24] = mapOf(
            Candidate("Nick Arsenault", grn) to 761,
            Candidate("Sonny Gallant", lib) to 1100,
            Candidate("Grant Gallant", ndp) to 33,
            Candidate("Jaosn Woodbury", pc) to 575
        )
        partyResults[24] = elected(lib) as R
        pctReporting[24] = 8.0 / 8
        candidateVotes[25] = mapOf(
            Candidate("Barb Broome", pc) to 462,
            Candidate("Jason Charette", grn) to 231,
            Candidate("Dr. Herb Dickieson", ndp) to 898,
            Candidate("Robert Henderson", lib) to 1102
        )
        partyResults[25] = elected(lib) as R
        pctReporting[25] = 11.0 / 11
        candidateVotes[26] = mapOf(
            Candidate("Michelle Arsenault", ndp) to 99,
            Candidate("Ernie Hudson", pc) to 1312,
            Candidate("James McKenna", grn) to 317,
            Candidate("Pat Murphy", lib) to 1153
        )
        partyResults[26] = elected(lib) as R
        pctReporting[26] = 10.0 / 10
        candidateVotes[27] = mapOf(
            Candidate("Sean Doyle", grn) to 584,
            Candidate("Melissa Handrahan", pc) to 802,
            Candidate("Hal Perry", lib) to 1388,
            Candidate("Dale Ryan", ndp) to 44
        )
        partyResults[27] = elected(lib) as R
        pctReporting[27] = 10.0 / 10
    }

    companion object {
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
    }
}
