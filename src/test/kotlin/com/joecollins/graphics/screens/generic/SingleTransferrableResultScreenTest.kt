package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import org.junit.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Shape

class SingleTransferrableResultScreenTest {

    @Test
    fun testDisplayQuotas() {
        val apni = Party("Alliance", "APNI", Color.ORANGE)
        val uup = Party("Ulster Unionist Party", "UUP", Color.BLUE)
        val dup = Party("Democratic Unionist Party", "DUP", Color.ORANGE.darker())
        val pup = Party("Progressive Unionist Party", "PUP", Color.BLUE.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val sf = Party("Sinn F\u00e9in", "SF", Color.GREEN.darker().darker())
        val tuv = Party("Traditional Unionist Voice", "TUV", Color.BLUE.darker().darker())
        val labalt = Party("Labour Alternative", "LAB-ALT", Color.RED.darker())
        val con = Party("NI Conservatives", "CON", Color.CYAN.darker())
        val sdlp = Party("Social Democratic and Labour Party", "SDLP", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val prevVotes = mapOf(
            dup to 5538 + 4230 + 3875,
            apni to 5428 + 2805 + 2372,
            uup to 3047 + 1095,
            grn to 2183,
            pup to 1772,
            ind to 1099,
            tuv to 887,
            sf to 946,
            Party.OTHERS to 631 + 78,
            labalt to 517,
            con to 477,
            sdlp to 141
        )
        val currQuota = Publisher(1)
        val prevQuota = 5311.0
        val currRound = Publisher(emptyMap<Party, Double>())
        val prevRound = Publisher(prevVotes.mapValues { it.value / prevQuota })
        val currVotes = Publisher(emptyMap<Party, Int>())
        val currWinner = Publisher<Party?>(null)
        val round = Publisher(0)

        val panel = BasicResultPanel.partyQuotas(
            currRound.merge(currQuota) { votes, quota -> votes.mapValues { it.value / quota } },
            5.asOneTimePublisher(),
            "PARTY SUMMARY".asOneTimePublisher(),
            round.map { if (it == 0) "WAITING..." else "COUNT $it" }
        ).withPrev(
            prevRound,
            round.map { if (it <= 1) "CHANGE SINCE 2016" else "CHANGE FROM COUNT ${it - 1}" }
        ).withSwing(
            currVotes,
            prevVotes.asOneTimePublisher(),
            Comparator.comparing { listOf(sf, apni, dup).indexOf(it) },
            "FIRST PREF SWING".asOneTimePublisher()
        ).withPartyMap(
            niShapesByConstituency().asOneTimePublisher(),
            9.asOneTimePublisher(),
            currWinner,
            listOf(9, 10, 12, 15).asOneTimePublisher(),
            "BELFAST".asOneTimePublisher()
        ).build(
            "BELFAST EAST".asOneTimePublisher()
        )
        panel.setSize(1024, 512)
        compareRendering("SingleTransferrableResultScreen", "Quotas-0", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, WAITING... (CHANGE SINCE 2016)
                
                FIRST PREF SWING: NOT AVAILABLE
            """.trimIndent()
        )

        val round1 = mapOf(
            apni to 7610 + 5059,
            uup to 5275,
            dup to 6007 + 4729 + 4431,
            pup to 2658,
            grn to 1447,
            sf to 1173,
            tuv to 917,
            labalt to 442,
            con to 275,
            sdlp to 250,
            ind to 84
        )
        currVotes.submit(round1)
        val quota = 6727.0
        currRound.submit(round1.mapValues { it.value / quota })
        round.submit(1)
        currWinner.submit(dup)
        compareRendering("SingleTransferrableResultScreen", "Quotas-1", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 1 (CHANGE SINCE 2016)
                DEMOCRATIC UNIONIST PARTY: 2.25 QUOTAS (-0.31)
                ALLIANCE: 1.88 QUOTAS (-0.11)
                ULSTER UNIONIST PARTY: 0.78 QUOTAS (+0.00)
                PROGRESSIVE UNIONIST PARTY: 0.40 QUOTAS (+0.06)
                GREEN: 0.22 QUOTAS (-0.20)
                SINN FÉIN: 0.17 QUOTAS (-0.00)
                TRADITIONAL UNIONIST VOICE: 0.14 QUOTAS (-0.03)
                LABOUR ALTERNATIVE: 0.07 QUOTAS (-0.03)
                NI CONSERVATIVES: 0.04 QUOTAS (-0.05)
                SOCIAL DEMOCRATIC AND LABOUR PARTY: 0.04 QUOTAS (+0.01)
                INDEPENDENT: 0.01 QUOTAS (-0.19)
                OTHERS: - (-0.13)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round2 = mapOf(
            apni to quota + 5760.03,
            uup to 5313.17,
            dup to 6016.02 + 4734.50 + 4435.18,
            pup to 2673.29,
            grn to 1474.06,
            sf to 1183.34,
            tuv to 918.65,
            labalt to 449.48,
            con to 276.32,
            sdlp to 260.45,
            ind to 85.43
        )
        currRound.submit(round2.mapValues { it.value / quota })
        prevRound.submit(round1.mapValues { it.value / quota })
        round.submit(2)
        compareRendering("SingleTransferrableResultScreen", "Quotas-2", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 2 (CHANGE FROM COUNT 1)
                DEMOCRATIC UNIONIST PARTY: 2.26 QUOTAS (+0.00)
                ALLIANCE: 1.86 QUOTAS (-0.03)
                ULSTER UNIONIST PARTY: 0.79 QUOTAS (+0.01)
                PROGRESSIVE UNIONIST PARTY: 0.40 QUOTAS (+0.00)
                GREEN: 0.22 QUOTAS (+0.00)
                SINN FÉIN: 0.18 QUOTAS (+0.00)
                TRADITIONAL UNIONIST VOICE: 0.14 QUOTAS (+0.00)
                LABOUR ALTERNATIVE: 0.07 QUOTAS (+0.00)
                NI CONSERVATIVES: 0.04 QUOTAS (+0.00)
                SOCIAL DEMOCRATIC AND LABOUR PARTY: 0.04 QUOTAS (+0.00)
                INDEPENDENT: 0.01 QUOTAS (+0.00)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round3 = mapOf(
            apni to quota + 5780.25,
            uup to 5323.39,
            dup to 6021.02 + 4736.50 + 4436.18,
            pup to 2678.51,
            grn to 1500.28,
            sf to 1185.45,
            tuv to 920.65,
            labalt to 456.48,
            con to 278.43,
            sdlp to 260.56
        )
        currRound.submit(round3.mapValues { it.value / quota })
        prevRound.submit(round2.mapValues { it.value / quota })
        round.submit(3)
        compareRendering("SingleTransferrableResultScreen", "Quotas-3", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 3 (CHANGE FROM COUNT 2)
                DEMOCRATIC UNIONIST PARTY: 2.26 QUOTAS (+0.00)
                ALLIANCE: 1.86 QUOTAS (+0.00)
                ULSTER UNIONIST PARTY: 0.79 QUOTAS (+0.00)
                PROGRESSIVE UNIONIST PARTY: 0.40 QUOTAS (+0.00)
                GREEN: 0.22 QUOTAS (+0.00)
                SINN FÉIN: 0.18 QUOTAS (+0.00)
                TRADITIONAL UNIONIST VOICE: 0.14 QUOTAS (+0.00)
                LABOUR ALTERNATIVE: 0.07 QUOTAS (+0.00)
                NI CONSERVATIVES: 0.04 QUOTAS (+0.00)
                SOCIAL DEMOCRATIC AND LABOUR PARTY: 0.04 QUOTAS (+0.00)
                INDEPENDENT: - (-0.01)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round4 = mapOf(
            apni to quota + 5885.86,
            uup to 5356.60,
            dup to 6026.13 + 4739.50 + 4440.18,
            pup to 2685.73,
            grn to 1539.49,
            sf to 1216.66,
            tuv to 924.76,
            labalt to 467.81,
            con to 280.43
        )
        currRound.submit(round4.mapValues { it.value / quota })
        prevRound.submit(round3.mapValues { it.value / quota })
        round.submit(4)
        compareRendering("SingleTransferrableResultScreen", "Quotas-4", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 4 (CHANGE FROM COUNT 3)
                DEMOCRATIC UNIONIST PARTY: 2.26 QUOTAS (+0.00)
                ALLIANCE: 1.87 QUOTAS (+0.02)
                ULSTER UNIONIST PARTY: 0.80 QUOTAS (+0.00)
                PROGRESSIVE UNIONIST PARTY: 0.40 QUOTAS (+0.00)
                GREEN: 0.23 QUOTAS (+0.01)
                SINN FÉIN: 0.18 QUOTAS (+0.00)
                TRADITIONAL UNIONIST VOICE: 0.14 QUOTAS (+0.00)
                LABOUR ALTERNATIVE: 0.07 QUOTAS (+0.00)
                NI CONSERVATIVES: 0.04 QUOTAS (+0.00)
                SOCIAL DEMOCRATIC AND LABOUR PARTY: - (-0.04)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round5 = mapOf(
            apni to quota + 6032.81,
            uup to 5498.59,
            dup to 6057.35 + 4774.50 + 4455.40,
            pup to 2751.95,
            grn to 1763.36,
            sf to 1232.88,
            tuv to 958.87
        )
        currRound.submit(round5.mapValues { it.value / quota })
        prevRound.submit(round4.mapValues { it.value / quota })
        round.submit(5)
        compareRendering("SingleTransferrableResultScreen", "Quotas-5", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 5 (CHANGE FROM COUNT 4)
                DEMOCRATIC UNIONIST PARTY: 2.27 QUOTAS (+0.01)
                ALLIANCE: 1.90 QUOTAS (+0.02)
                ULSTER UNIONIST PARTY: 0.82 QUOTAS (+0.02)
                PROGRESSIVE UNIONIST PARTY: 0.41 QUOTAS (+0.01)
                GREEN: 0.26 QUOTAS (+0.03)
                SINN FÉIN: 0.18 QUOTAS (+0.00)
                TRADITIONAL UNIONIST VOICE: 0.14 QUOTAS (+0.01)
                LABOUR ALTERNATIVE: - (-0.07)
                NI CONSERVATIVES: - (-0.04)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round6 = mapOf(
            apni to quota + 6072.14,
            uup to 5767.92,
            dup to 6217.46 + 4862.61 + 4568.40,
            pup to 2936.28,
            grn to 1801.58,
            sf to 1235.10
        )
        currRound.submit(round6.mapValues { it.value / quota })
        prevRound.submit(round5.mapValues { it.value / quota })
        round.submit(6)
        compareRendering("SingleTransferrableResultScreen", "Quotas-6", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 6 (CHANGE FROM COUNT 5)
                DEMOCRATIC UNIONIST PARTY: 2.33 QUOTAS (+0.05)
                ALLIANCE: 1.90 QUOTAS (+0.01)
                ULSTER UNIONIST PARTY: 0.86 QUOTAS (+0.04)
                PROGRESSIVE UNIONIST PARTY: 0.44 QUOTAS (+0.03)
                GREEN: 0.27 QUOTAS (+0.01)
                SINN FÉIN: 0.18 QUOTAS (+0.00)
                TRADITIONAL UNIONIST VOICE: - (-0.14)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round7 = mapOf(
            apni to quota + 6367.87,
            uup to 5783.14,
            dup to 6228.46 + 4865.61 + 4572.40,
            pup to 2950.28,
            grn to 2080.65
        )
        currRound.submit(round7.mapValues { it.value / quota })
        prevRound.submit(round6.mapValues { it.value / quota })
        round.submit(7)
        compareRendering("SingleTransferrableResultScreen", "Quotas-7", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 7 (CHANGE FROM COUNT 6)
                DEMOCRATIC UNIONIST PARTY: 2.33 QUOTAS (+0.00)
                ALLIANCE: 1.95 QUOTAS (+0.04)
                ULSTER UNIONIST PARTY: 0.86 QUOTAS (+0.00)
                PROGRESSIVE UNIONIST PARTY: 0.44 QUOTAS (+0.00)
                GREEN: 0.31 QUOTAS (+0.04)
                SINN FÉIN: - (-0.18)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round8 = mapOf(
            apni to quota + 7268.87,
            uup to 6048.62,
            dup to 6275.01 + 4890.72 + 4599.73,
            pup to 3148.79
        )
        currRound.submit(round8.mapValues { it.value / quota })
        prevRound.submit(round7.mapValues { it.value / quota })
        round.submit(8)
        compareRendering("SingleTransferrableResultScreen", "Quotas-8", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 8 (CHANGE FROM COUNT 7)
                DEMOCRATIC UNIONIST PARTY: 2.34 QUOTAS (+0.01)
                ALLIANCE: 2.08 QUOTAS (+0.13)
                ULSTER UNIONIST PARTY: 0.90 QUOTAS (+0.04)
                PROGRESSIVE UNIONIST PARTY: 0.47 QUOTAS (+0.03)
                GREEN: - (-0.31)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round9 = mapOf(
            apni to quota + quota,
            uup to 7257.62,
            dup to 6759.01 + 5268.26 + 4995.25
        )
        currRound.submit(round9.mapValues { it.value / quota })
        prevRound.submit(round8.mapValues { it.value / quota })
        round.submit(9)
        compareRendering("SingleTransferrableResultScreen", "Quotas-9", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 9 (CHANGE FROM COUNT 8)
                DEMOCRATIC UNIONIST PARTY: 2.53 QUOTAS (+0.19)
                ALLIANCE: 2.00 QUOTAS (-0.08)
                ULSTER UNIONIST PARTY: 1.08 QUOTAS (+0.18)
                PROGRESSIVE UNIONIST PARTY: - (-0.47)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round10 = mapOf(
            apni to quota + quota,
            uup to quota,
            dup to quota + 5333.26 + 5093.25
        )
        currRound.submit(round10.mapValues { it.value / quota })
        prevRound.submit(round9.mapValues { it.value / quota })
        round.submit(10)
        compareRendering("SingleTransferrableResultScreen", "Quotas-10", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 10 (CHANGE FROM COUNT 9)
                DEMOCRATIC UNIONIST PARTY: 2.55 QUOTAS (+0.02)
                ALLIANCE: 2.00 QUOTAS (+0.00)
                ULSTER UNIONIST PARTY: 1.00 QUOTAS (-0.08)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )

        val round11 = mapOf(
            apni to quota + quota,
            uup to quota,
            dup to quota + 5541.65 + 5410.84
        )
        currRound.submit(round11.mapValues { it.value / quota })
        prevRound.submit(round10.mapValues { it.value / quota })
        round.submit(11)
        compareRendering("SingleTransferrableResultScreen", "Quotas-11", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 11 (CHANGE FROM COUNT 10)
                DEMOCRATIC UNIONIST PARTY: 2.63 QUOTAS (+0.08)
                ALLIANCE: 2.00 QUOTAS (+0.00)
                ULSTER UNIONIST PARTY: 1.00 QUOTAS (+0.00)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent()
        )
    }

    @Test
    fun displayCandidates() {
        val apni = Party("Alliance", "APNI", Color.ORANGE)
        val uup = Party("Ulster Unionist Party", "UUP", Color.BLUE)
        val dup = Party("Democratic Unionist Party", "DUP", Color.ORANGE.darker())
        val pup = Party("Progressive Unionist Party", "PUP", Color.BLUE.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val sf = Party("Sinn F\u00e9in", "SF", Color.GREEN.darker().darker())
        val tuv = Party("Traditional Unionist Voice", "TUV", Color.BLUE.darker().darker())
        val labalt = Party("Labour Alternative", "LAB-ALT", Color.RED.darker())
        val con = Party("NI Conservatives", "CON", Color.CYAN.darker())
        val sdlp = Party("Social Democratic and Labour Party", "SDLP", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val allen = Candidate("Andy Allen", uup, true)
        val bodel = Candidate("Sheila Bodel", con)
        val bunting = Candidate("Joanne Bunting", dup, true)
        val defaoite = Candidate("S\u00e9amus de Faoite", sdlp)
        val douglas = Candidate("David Douglas", dup)
        val girvin = Candidate("Andrew Girvin", tuv)
        val kyle = Candidate("John Kyle", pup)
        val long = Candidate("Naomi Long", apni, true)
        val lyttle = Candidate("Chris Lyttle", apni, true)
        val mckeag = Candidate("Jordy McKeag", ind)
        val milne = Candidate("Georgina Milne", grn)
        val newton = Candidate("Robin Newton", dup, true)
        val odonnell = Candidate("Mair\u00e9ad O'Donnell", sf)
        val robinson = Candidate("Courtney Robinson", labalt)

        val candidateVotes = Publisher<Map<Candidate, Number?>>(
            mapOf(
                allen to null,
                bodel to null,
                bunting to null,
                defaoite to null,
                douglas to null,
                girvin to null,
                kyle to null,
                long to null,
                lyttle to null,
                mckeag to null,
                milne to null,
                newton to null,
                odonnell to null,
                robinson to null
            )
        )
        val quota = Publisher<Int?>(null)
        val elected = Publisher<List<Pair<Candidate, Int>>>(emptyList())
        val excluded = Publisher<List<Candidate>>(emptyList())
        val candidateHeader = Publisher("WAITING FOR RESULTS...")
        val candidateSubhead = Publisher("0 OF 5 SEATS ELECTED")
        val currWinner = Publisher<Party?>(null)

        val panel = SingleTransferrableResultScreen.withCandidates(
            candidateVotes,
            quota,
            elected,
            excluded,
            candidateHeader,
            candidateSubhead,
            "[MLA]"
        )
            .withPartyTotals(
                5.asOneTimePublisher(),
                "TOTAL QUOTAS BY PARTY".asOneTimePublisher()
            )
            .withPrevSeats(
                mapOf(dup to 3, apni to 2, uup to 1).asOneTimePublisher(),
                "SEATS IN 2016".asOneTimePublisher()
            )
            .withPartyMap(
                niShapesByConstituency().asOneTimePublisher(),
                9.asOneTimePublisher(),
                currWinner,
                listOf(9, 10, 12, 15).asOneTimePublisher(),
                "BELFAST".asOneTimePublisher()
            )
            .build("BELFAST EAST".asOneTimePublisher())
        panel.size = Dimension(1024, 512)
        compareRendering("SingleTransferrableResultScreen", "Candidates-0", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                WAITING FOR RESULTS..., 0 OF 5 SEATS ELECTED
                ANDY ALLEN [MLA] (UUP): WAITING...
                SHEILA BODEL (CON): WAITING...
                JOANNE BUNTING [MLA] (DUP): WAITING...
                SÉAMUS DE FAOITE (SDLP): WAITING...
                DAVID DOUGLAS (DUP): WAITING...
                ANDREW GIRVIN (TUV): WAITING...
                JOHN KYLE (PUP): WAITING...
                NAOMI LONG [MLA] (APNI): WAITING...
                CHRIS LYTTLE [MLA] (APNI): WAITING...
                JORDY MCKEAG (IND): WAITING...
                GEORGINA MILNE (GRN): WAITING...
                ROBIN NEWTON [MLA] (DUP): WAITING...
                MAIRÉAD O'DONNELL (SF): WAITING...
                COURTNEY ROBINSON (LAB-ALT): WAITING...
                
                TOTAL QUOTAS BY PARTY
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                long to 7610,
                lyttle to 5059,
                allen to 5275,
                bunting to 6007,
                newton to 4729,
                douglas to 4431,
                kyle to 2658,
                milne to 1447,
                odonnell to 1173,
                girvin to 917,
                robinson to 442,
                bodel to 275,
                defaoite to 250,
                mckeag to 84
            )
        )
        elected.submit(listOf(long to 1))
        candidateHeader.submit("COUNT 1")
        candidateSubhead.submit("1 OF 5 SEATS ELECTED")
        quota.submit(6727)
        currWinner.submit(dup)
        compareRendering("SingleTransferrableResultScreen", "Candidates-1", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 1, 1 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): 7,610 (1.13) ELECTED
                JOANNE BUNTING [MLA] (DUP): 6,007 (0.89)
                ANDY ALLEN [MLA] (UUP): 5,275 (0.78)
                CHRIS LYTTLE [MLA] (APNI): 5,059 (0.75)
                ROBIN NEWTON [MLA] (DUP): 4,729 (0.70)
                DAVID DOUGLAS (DUP): 4,431 (0.66)
                JOHN KYLE (PUP): 2,658 (0.40)
                GEORGINA MILNE (GRN): 1,447 (0.22)
                MAIRÉAD O'DONNELL (SF): 1,173 (0.17)
                ANDREW GIRVIN (TUV): 917 (0.14)
                COURTNEY ROBINSON (LAB-ALT): 442 (0.07)
                SHEILA BODEL (CON): 275 (0.04)
                SÉAMUS DE FAOITE (SDLP): 250 (0.04)
                JORDY MCKEAG (IND): 84 (0.01)
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.25
                ALLIANCE: 1.88
                ULSTER UNIONIST PARTY: 0.78
                PROGRESSIVE UNIONIST PARTY: 0.40
                GREEN: 0.22
                SINN FÉIN: 0.17
                TRADITIONAL UNIONIST VOICE: 0.14
                LABOUR ALTERNATIVE: 0.07
                NI CONSERVATIVES: 0.04
                SOCIAL DEMOCRATIC AND LABOUR PARTY: 0.04
                INDEPENDENT: 0.01
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                lyttle to 5760.03,
                allen to 5313.17,
                bunting to 6016.02,
                newton to 4734.50,
                douglas to 4435.18,
                kyle to 2673.29,
                milne to 1474.06,
                odonnell to 1183.34,
                girvin to 918.65,
                robinson to 449.48,
                bodel to 276.32,
                defaoite to 260.45,
                mckeag to 85.43
            )
        )
        excluded.submit(listOf(mckeag))
        candidateHeader.submit("COUNT 2")
        compareRendering("SingleTransferrableResultScreen", "Candidates-2", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 2, 1 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                JOANNE BUNTING [MLA] (DUP): 6,016.02 (0.89)
                CHRIS LYTTLE [MLA] (APNI): 5,760.03 (0.86)
                ANDY ALLEN [MLA] (UUP): 5,313.17 (0.79)
                ROBIN NEWTON [MLA] (DUP): 4,734.50 (0.70)
                DAVID DOUGLAS (DUP): 4,435.18 (0.66)
                JOHN KYLE (PUP): 2,673.29 (0.40)
                GEORGINA MILNE (GRN): 1,474.06 (0.22)
                MAIRÉAD O'DONNELL (SF): 1,183.34 (0.18)
                ANDREW GIRVIN (TUV): 918.65 (0.14)
                COURTNEY ROBINSON (LAB-ALT): 449.48 (0.07)
                SHEILA BODEL (CON): 276.32 (0.04)
                SÉAMUS DE FAOITE (SDLP): 260.45 (0.04)
                JORDY MCKEAG (IND): 85.43 (0.01) EXCLUDED
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.26
                ALLIANCE: 1.86
                ULSTER UNIONIST PARTY: 0.79
                PROGRESSIVE UNIONIST PARTY: 0.40
                GREEN: 0.22
                SINN FÉIN: 0.18
                TRADITIONAL UNIONIST VOICE: 0.14
                LABOUR ALTERNATIVE: 0.07
                NI CONSERVATIVES: 0.04
                SOCIAL DEMOCRATIC AND LABOUR PARTY: 0.04
                INDEPENDENT: 0.01
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                lyttle to 5780.25,
                allen to 5323.39,
                bunting to 6021.02,
                newton to 4736.50,
                douglas to 4436.18,
                kyle to 2678.51,
                milne to 1500.28,
                odonnell to 1185.45,
                girvin to 920.65,
                robinson to 456.48,
                bodel to 278.43,
                defaoite to 260.56
            )
        )
        excluded.submit(listOf(mckeag, defaoite))
        candidateHeader.submit("COUNT 3")
        compareRendering("SingleTransferrableResultScreen", "Candidates-3", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 3, 1 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                JOANNE BUNTING [MLA] (DUP): 6,021.02 (0.90)
                CHRIS LYTTLE [MLA] (APNI): 5,780.25 (0.86)
                ANDY ALLEN [MLA] (UUP): 5,323.39 (0.79)
                ROBIN NEWTON [MLA] (DUP): 4,736.50 (0.70)
                DAVID DOUGLAS (DUP): 4,436.18 (0.66)
                JOHN KYLE (PUP): 2,678.51 (0.40)
                GEORGINA MILNE (GRN): 1,500.28 (0.22)
                MAIRÉAD O'DONNELL (SF): 1,185.45 (0.18)
                ANDREW GIRVIN (TUV): 920.65 (0.14)
                COURTNEY ROBINSON (LAB-ALT): 456.48 (0.07)
                SHEILA BODEL (CON): 278.43 (0.04)
                SÉAMUS DE FAOITE (SDLP): 260.56 (0.04) EXCLUDED
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.26
                ALLIANCE: 1.86
                ULSTER UNIONIST PARTY: 0.79
                PROGRESSIVE UNIONIST PARTY: 0.40
                GREEN: 0.22
                SINN FÉIN: 0.18
                TRADITIONAL UNIONIST VOICE: 0.14
                LABOUR ALTERNATIVE: 0.07
                NI CONSERVATIVES: 0.04
                SOCIAL DEMOCRATIC AND LABOUR PARTY: 0.04
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                lyttle to 5885.86,
                allen to 5356.60,
                bunting to 6026.13,
                newton to 4739.50,
                douglas to 4440.18,
                kyle to 2685.73,
                milne to 1539.49,
                odonnell to 1216.66,
                girvin to 924.76,
                robinson to 467.81,
                bodel to 280.43
            )
        )
        excluded.submit(listOf(mckeag, defaoite, bodel, robinson))
        candidateHeader.submit("COUNT 4")
        compareRendering("SingleTransferrableResultScreen", "Candidates-4", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 4, 1 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                JOANNE BUNTING [MLA] (DUP): 6,026.13 (0.90)
                CHRIS LYTTLE [MLA] (APNI): 5,885.86 (0.87)
                ANDY ALLEN [MLA] (UUP): 5,356.60 (0.80)
                ROBIN NEWTON [MLA] (DUP): 4,739.50 (0.70)
                DAVID DOUGLAS (DUP): 4,440.18 (0.66)
                JOHN KYLE (PUP): 2,685.73 (0.40)
                GEORGINA MILNE (GRN): 1,539.49 (0.23)
                MAIRÉAD O'DONNELL (SF): 1,216.66 (0.18)
                ANDREW GIRVIN (TUV): 924.76 (0.14)
                COURTNEY ROBINSON (LAB-ALT): 467.81 (0.07) EXCLUDED
                SHEILA BODEL (CON): 280.43 (0.04) EXCLUDED
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.26
                ALLIANCE: 1.87
                ULSTER UNIONIST PARTY: 0.80
                PROGRESSIVE UNIONIST PARTY: 0.40
                GREEN: 0.23
                SINN FÉIN: 0.18
                TRADITIONAL UNIONIST VOICE: 0.14
                LABOUR ALTERNATIVE: 0.07
                NI CONSERVATIVES: 0.04
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                lyttle to 6032.81,
                allen to 5498.59,
                bunting to 6057.35,
                newton to 4774.50,
                douglas to 4455.40,
                kyle to 2751.95,
                milne to 1763.36,
                odonnell to 1232.88,
                girvin to 958.87
            )
        )
        excluded.submit(listOf(mckeag, defaoite, bodel, robinson, girvin))
        candidateHeader.submit("COUNT 5")
        compareRendering("SingleTransferrableResultScreen", "Candidates-5", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 5, 1 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                JOANNE BUNTING [MLA] (DUP): 6,057.35 (0.90)
                CHRIS LYTTLE [MLA] (APNI): 6,032.81 (0.90)
                ANDY ALLEN [MLA] (UUP): 5,498.59 (0.82)
                ROBIN NEWTON [MLA] (DUP): 4,774.50 (0.71)
                DAVID DOUGLAS (DUP): 4,455.40 (0.66)
                JOHN KYLE (PUP): 2,751.95 (0.41)
                GEORGINA MILNE (GRN): 1,763.36 (0.26)
                MAIRÉAD O'DONNELL (SF): 1,232.88 (0.18)
                ANDREW GIRVIN (TUV): 958.87 (0.14) EXCLUDED
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.27
                ALLIANCE: 1.90
                ULSTER UNIONIST PARTY: 0.82
                PROGRESSIVE UNIONIST PARTY: 0.41
                GREEN: 0.26
                SINN FÉIN: 0.18
                TRADITIONAL UNIONIST VOICE: 0.14
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                lyttle to 6072.14,
                allen to 5767.92,
                bunting to 6217.46,
                newton to 4862.61,
                douglas to 4568.40,
                kyle to 2936.28,
                milne to 1801.58,
                odonnell to 1235.10
            )
        )
        excluded.submit(listOf(mckeag, defaoite, bodel, robinson, girvin, odonnell))
        candidateHeader.submit("COUNT 6")
        compareRendering("SingleTransferrableResultScreen", "Candidates-6", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 6, 1 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                JOANNE BUNTING [MLA] (DUP): 6,217.46 (0.92)
                CHRIS LYTTLE [MLA] (APNI): 6,072.14 (0.90)
                ANDY ALLEN [MLA] (UUP): 5,767.92 (0.86)
                ROBIN NEWTON [MLA] (DUP): 4,862.61 (0.72)
                DAVID DOUGLAS (DUP): 4,568.40 (0.68)
                JOHN KYLE (PUP): 2,936.28 (0.44)
                GEORGINA MILNE (GRN): 1,801.58 (0.27)
                MAIRÉAD O'DONNELL (SF): 1,235.10 (0.18) EXCLUDED
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.33
                ALLIANCE: 1.90
                ULSTER UNIONIST PARTY: 0.86
                PROGRESSIVE UNIONIST PARTY: 0.44
                GREEN: 0.27
                SINN FÉIN: 0.18
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                lyttle to 6367.87,
                allen to 5783.14,
                bunting to 6228.46,
                newton to 4865.61,
                douglas to 4572.40,
                kyle to 2950.28,
                milne to 2080.65
            )
        )
        excluded.submit(listOf(mckeag, defaoite, bodel, robinson, girvin, odonnell, milne))
        candidateHeader.submit("COUNT 7")
        compareRendering("SingleTransferrableResultScreen", "Candidates-7", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 7, 1 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                CHRIS LYTTLE [MLA] (APNI): 6,367.87 (0.95)
                JOANNE BUNTING [MLA] (DUP): 6,228.46 (0.93)
                ANDY ALLEN [MLA] (UUP): 5,783.14 (0.86)
                ROBIN NEWTON [MLA] (DUP): 4,865.61 (0.72)
                DAVID DOUGLAS (DUP): 4,572.40 (0.68)
                JOHN KYLE (PUP): 2,950.28 (0.44)
                GEORGINA MILNE (GRN): 2,080.65 (0.31) EXCLUDED
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.33
                ALLIANCE: 1.95
                ULSTER UNIONIST PARTY: 0.86
                PROGRESSIVE UNIONIST PARTY: 0.44
                GREEN: 0.31
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                lyttle to 7268.87,
                allen to 6048.62,
                bunting to 6275.01,
                newton to 4890.72,
                douglas to 4599.73,
                kyle to 3148.79
            )
        )
        candidateSubhead.submit("2 OF 5 SEATS ELECTED")
        elected.submit(listOf(long to 1, lyttle to 8))
        excluded.submit(listOf(mckeag, defaoite, bodel, robinson, girvin, odonnell, milne, kyle))
        candidateHeader.submit("COUNT 8")
        compareRendering("SingleTransferrableResultScreen", "Candidates-8", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 8, 2 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                CHRIS LYTTLE [MLA] (APNI): 7,268.87 (1.08) ELECTED
                JOANNE BUNTING [MLA] (DUP): 6,275.01 (0.93)
                ANDY ALLEN [MLA] (UUP): 6,048.62 (0.90)
                ROBIN NEWTON [MLA] (DUP): 4,890.72 (0.73)
                DAVID DOUGLAS (DUP): 4,599.73 (0.68)
                JOHN KYLE (PUP): 3,148.79 (0.47) EXCLUDED
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.34
                ALLIANCE: 2.08
                ULSTER UNIONIST PARTY: 0.90
                PROGRESSIVE UNIONIST PARTY: 0.47
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                allen to 7257.62,
                bunting to 6759.01,
                newton to 5268.26,
                douglas to 4995.25
            )
        )
        candidateSubhead.submit("4 OF 5 SEATS ELECTED")
        elected.submit(listOf(long to 1, lyttle to 8, allen to 9, bunting to 9))
        excluded.submit(listOf(mckeag, defaoite, bodel, robinson, girvin, odonnell, milne, kyle))
        candidateHeader.submit("COUNT 9")
        compareRendering("SingleTransferrableResultScreen", "Candidates-9", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 9, 4 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                CHRIS LYTTLE [MLA] (APNI): ELECTED IN 8
                ANDY ALLEN [MLA] (UUP): 7,257.62 (1.08) ELECTED
                JOANNE BUNTING [MLA] (DUP): 6,759.01 (1.00) ELECTED
                ROBIN NEWTON [MLA] (DUP): 5,268.26 (0.78)
                DAVID DOUGLAS (DUP): 4,995.25 (0.74)
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.53
                ALLIANCE: 2.00
                ULSTER UNIONIST PARTY: 1.08
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                newton to 5333.26,
                douglas to 5093.25
            )
        )
        elected.submit(listOf(long to 1, lyttle to 8, allen to 9, bunting to 9))
        excluded.submit(listOf(mckeag, defaoite, bodel, robinson, girvin, odonnell, milne, kyle))
        candidateHeader.submit("COUNT 10")
        compareRendering("SingleTransferrableResultScreen", "Candidates-10", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 10, 4 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                CHRIS LYTTLE [MLA] (APNI): ELECTED IN 8
                ANDY ALLEN [MLA] (UUP): ELECTED IN 9
                JOANNE BUNTING [MLA] (DUP): ELECTED IN 9
                ROBIN NEWTON [MLA] (DUP): 5,333.26 (0.79)
                DAVID DOUGLAS (DUP): 5,093.25 (0.76)
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.55
                ALLIANCE: 2.00
                ULSTER UNIONIST PARTY: 1.00
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )

        candidateVotes.submit(
            mapOf(
                newton to 5541.65,
                douglas to 5410.84
            )
        )
        candidateSubhead.submit("5 OF 5 SEATS ELECTED")
        elected.submit(listOf(long to 1, lyttle to 8, allen to 9, bunting to 9, newton to 11))
        excluded.submit(listOf(mckeag, defaoite, bodel, robinson, girvin, odonnell, milne, kyle, douglas))
        candidateHeader.submit("COUNT 11")
        compareRendering("SingleTransferrableResultScreen", "Candidates-11", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                COUNT 11, 5 OF 5 SEATS ELECTED
                NAOMI LONG [MLA] (APNI): ELECTED IN 1
                CHRIS LYTTLE [MLA] (APNI): ELECTED IN 8
                ANDY ALLEN [MLA] (UUP): ELECTED IN 9
                JOANNE BUNTING [MLA] (DUP): ELECTED IN 9
                ROBIN NEWTON [MLA] (DUP): 5,541.65 (0.82) ELECTED
                DAVID DOUGLAS (DUP): 5,410.84 (0.80) EXCLUDED
                QUOTA: 6,727
                
                TOTAL QUOTAS BY PARTY
                DEMOCRATIC UNIONIST PARTY: 2.63
                ALLIANCE: 2.00
                ULSTER UNIONIST PARTY: 1.00
                
                SEATS IN 2016
                DUP: 3
                APNI: 2
                UUP: 1
            """.trimIndent()
        )
    }

    @Test
    fun testPartyOthers() {
        val lib = Party("Liberal", "LIB", Color.BLUE)
        val alp = Party("Labor", "ALP", Color.RED)
        val grn = Party("Greens", "GRN", Color.GREEN)
        val oth = Party.OTHERS

        val prevVotes = mapOf(
            lib to 346423,
            alp to 289942,
            grn to 62345,
            oth to 362455
        )
        val currVotes = mapOf(
            lib to 413957,
            alp to 332399,
            grn to 119470,
            oth to 228997
        )
        val currQuota = 156404.0
        val prevQuota = 151596.0
        val currRound = Publisher(currVotes.mapValues { it.value / currQuota })
        val prevRound = Publisher(prevVotes.mapValues { it.value / prevQuota })

        val panel = BasicResultPanel.partyQuotas(
            currRound,
            6.asOneTimePublisher(),
            "PARTY SUMMARY".asOneTimePublisher(),
            "FIRST PREFERENCES".asOneTimePublisher()
        ).withPrev(
            prevRound,
            "CHANGE SINCE 2016".asOneTimePublisher()
        ).build(
            "SOUTH AUSTRALIA".asOneTimePublisher()
        )
        panel.setSize(1024, 512)
        compareRendering("SingleTransferrableResultScreen", "Quotas-Oth", panel)
        assertPublishes(
            panel.altText,
            """
                SOUTH AUSTRALIA
                
                PARTY SUMMARY, FIRST PREFERENCES (CHANGE SINCE 2016)
                LIBERAL: 2.65 QUOTAS (+0.36)
                LABOR: 2.13 QUOTAS (+0.21)
                GREENS: 0.76 QUOTAS (+0.35)
                OTHERS: 1.46 QUOTAS (-0.93)
            """.trimIndent()
        )
    }

    private fun niShapesByConstituency(): Map<Int, Shape> {
        val niMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/ni-constituencies.shp")
        return ShapefileReader.readShapes(niMap, "OBJECTID", Int::class.java)
    }
}
