package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrameTest
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
import java.io.IOException

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

        val round9 = mapOf(
            apni to quota + quota,
            uup to 7257.62,
            dup to 6759.01 + 5268.26 + 4995.25
        )
        currRound.submit(round9.mapValues { it.value / quota })
        prevRound.submit(round8.mapValues { it.value / quota })
        round.submit(9)
        compareRendering("SingleTransferrableResultScreen", "Quotas-9", panel)

        val round10 = mapOf(
            apni to quota + quota,
            uup to quota,
            dup to quota + 5333.26 + 5093.25
        )
        currRound.submit(round10.mapValues { it.value / quota })
        prevRound.submit(round9.mapValues { it.value / quota })
        round.submit(10)
        compareRendering("SingleTransferrableResultScreen", "Quotas-10", panel)

        val round11 = mapOf(
            apni to quota + quota,
            uup to quota,
            dup to quota + 5541.65 + 5410.84
        )
        currRound.submit(round11.mapValues { it.value / quota })
        prevRound.submit(round10.mapValues { it.value / quota })
        round.submit(11)
        compareRendering("SingleTransferrableResultScreen", "Quotas-11", panel)
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
    }

    @Throws(IOException::class)
    private fun niShapesByConstituency(): Map<Int, Shape> {
        val niMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/ni-constituencies.shp")
        return ShapefileReader.readShapes(niMap, "OBJECTID", Int::class.java)
    }
}
