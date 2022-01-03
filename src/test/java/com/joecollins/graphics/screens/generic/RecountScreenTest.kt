package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import java.awt.Color
import java.util.TreeMap

class RecountScreenTest {

    @Test
    fun testRecountVotes() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val yp = Party("Yukon Party", "YP", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val ind = Party("Independent", "IND", Color.GRAY)

        val candidateVotesRaw: MutableMap<String, Map<Candidate, Int>> = TreeMap()
        val pctReportingRaw: MutableMap<String, Double> = TreeMap()
        val candidateVotes = Publisher<Map<String, Map<Candidate, Int>>>(candidateVotesRaw)
        val pctReporting = Publisher<Map<String, Double>>(pctReportingRaw)
        val screen = RecountScreen.of(
            candidateVotes,
            { it.uppercase() },
            10,
            "AUTOMATIC RECOUNTS".asOneTimePublisher()
        )
            .withPctReporting(pctReporting)
            .build("YUKON".asOneTimePublisher())
        screen.setSize(1024, 512)
        RenderTestUtils.compareRendering("RecountScreen", "RecountVotes-1", screen)

        candidateVotesRaw["Mountainview"] = mapOf(
            Candidate("Shaunagh Stikeman", ndp) to 62,
            Candidate("Jeanie Davis", lib) to 68,
            Candidate("Darrell Pasloski", yp) to 65
        )
        pctReportingRaw["Mountainview"] = 1.0 / 6

        candidateVotesRaw["Vuntut Gwitchin"] = mapOf(
            Candidate("Pauline Frost", lib) to 77,
            Candidate("Darius Elias", yp) to 70,
            Candidate("Skeeter Wright", ndp) to 3
        )
        pctReportingRaw["Vuntut Gwitchin"] = 3.0 / 3

        candidateVotesRaw["Watson Lake"] = mapOf(
            Candidate("Patti McLeod", yp) to 147,
            Candidate("Victor Kisoun", ind) to 7,
            Candidate("Erin Labonte", ndp) to 94,
            Candidate("Ernie Jamieson", lib) to 103
        )
        pctReportingRaw["Watson Lake"] = 2.0 / 6

        candidateVotes.submit(candidateVotesRaw)
        pctReporting.submit(pctReportingRaw)
        RenderTestUtils.compareRendering("RecountScreen", "RecountVotes-2", screen)

        candidateVotesRaw["Mountainview"] = mapOf(
            Candidate("Shaunagh Stikeman", ndp) to 432,
            Candidate("Jeanie Davis", lib) to 439,
            Candidate("Darrell Pasloski", yp) to 399
        )
        pctReportingRaw["Mountainview"] = 6.0 / 6

        candidateVotesRaw["Watson Lake"] = mapOf(
            Candidate("Patti McLeod", yp) to 299,
            Candidate("Victor Kisoun", ind) to 38,
            Candidate("Erin Labonte", ndp) to 219,
            Candidate("Ernie Jamieson", lib) to 212
        )
        pctReportingRaw["Watson Lake"] = 6.0 / 6

        candidateVotes.submit(candidateVotesRaw)
        pctReporting.submit(pctReportingRaw)
        RenderTestUtils.compareRendering("RecountScreen", "RecountVotes-3", screen)
    }

    @Test
    fun testRecountPct() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val con = Party("Conservative", "CON", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)

        val candidateVotesRaw: MutableMap<String, Map<Candidate, Int>> = TreeMap()
        val candidateVotes = Publisher<Map<String, Map<Candidate, Int>>>(candidateVotesRaw)
        val screen = RecountScreen.of(
            candidateVotes,
            { it.uppercase() },
            0.001,
            "AUTOMATIC RECOUNTS".asOneTimePublisher()
        )
            .build("CANADA".asOneTimePublisher())
        screen.setSize(1024, 512)
        RenderTestUtils.compareRendering("RecountScreen", "RecountPct-1", screen)

        candidateVotesRaw["Etobicoke Centre"] = mapOf(
            Candidate("Ted Opitz", con) to 21644,
            Candidate("Borys Wrzesnewskyj", lib) to 21618,
            Candidate("Ana Maria Rivero", ndp) to 7735,
            Candidate("Katarina Zoricic", grn) to 1377,
            Candidate("Sarah Thompson", ind) to 149
        )
        candidateVotesRaw["Montmagny\u2014L'Islet\u2014Kamouraska\u2014Rivi\u00e8re-du-Loup"] = mapOf(
            Candidate("Fran\u00e7ois Lapointe", ndp) to 17285,
            Candidate("Bernard G\u00e9n\u00e9reux", con) to 17276,
            Candidate("Nathalie Arsenault", bq) to 9550,
            Candidate("Andrew Caddell", lib) to 2743,
            Candidate("Lynette Tremblay", grn) to 691
        )
        candidateVotesRaw["Nipissing\u2014Timiskaming"] = mapOf(
            Candidate("Jay Aspin", con) to 15495,
            Candidate("Anthony Rota", lib) to 15477,
            Candidate("Rona Eckert", ndp) to 8781,
            Candidate("Scott Daley", grn) to 2518
        )
        candidateVotesRaw["Winnipeg North"] = mapOf(
            Candidate("Kevin Lamoureux", lib) to 9097,
            Candidate("Rebecca Blaikie", ndp) to 9053,
            Candidate("Ann Matejicka", con) to 6701,
            Candidate("John Harvie", grn) to 458,
            Candidate("Frank Komarniski", ind) to 118
        )

        candidateVotes.submit(candidateVotesRaw)
        RenderTestUtils.compareRendering("RecountScreen", "RecountPct-2", screen)
    }
}
