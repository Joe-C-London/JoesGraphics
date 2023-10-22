package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.PartyQuotasPanel.Companion.createMap
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import com.joecollins.pubsub.merge
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Shape

class PartyQuotasPanelTest {

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
            sdlp to 141,
        )
        val currQuota = Publisher(1)
        val prevQuota = 5311.0
        val currRound = Publisher(emptyMap<Party, Double>())
        val prevRound = Publisher(prevVotes.mapValues { it.value / prevQuota })
        val currVotes = Publisher(emptyMap<Party, Int>())
        val currWinner = Publisher<Party?>(null)
        val round = Publisher(0)

        val panel = PartyQuotasPanel.partyQuotas<Party>(
            curr = {
                quotas = currRound.merge(currQuota) { votes, quota -> votes.mapValues { it.value / quota } }
                totalSeats = 5.asOneTimePublisher()
                header = "PARTY SUMMARY".asOneTimePublisher()
                subhead = round.map { if (it == 0) "WAITING..." else "COUNT $it" }
            },
            change = {
                prevQuotas = prevRound
                header = round.map { if (it <= 1) "CHANGE SINCE 2016" else "CHANGE FROM COUNT ${it - 1}" }
            },
            swing = {
                this.currVotes = currVotes
                this.prevVotes = prevVotes.asOneTimePublisher()
                order = Comparator.comparing { listOf(sf, apni, dup).indexOf(it) }
                header = "FIRST PREF SWING".asOneTimePublisher()
            },
            map = createMap {
                shapes = niShapesByConstituency().asOneTimePublisher()
                selectedShape = 9.asOneTimePublisher()
                leadingParty = currWinner
                focus = listOf(9, 10, 12, 15).asOneTimePublisher()
                header = "BELFAST".asOneTimePublisher()
            },
            title = "BELFAST EAST".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("PartyQuotasPanel", "Quotas-0", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, WAITING... (CHANGE SINCE 2016)
                
                FIRST PREF SWING: NOT AVAILABLE
            """.trimIndent(),
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
            ind to 84,
        )
        currVotes.submit(round1)
        val quota = 6727.0
        currRound.submit(round1.mapValues { it.value / quota })
        round.submit(1)
        currWinner.submit(dup)
        compareRendering("PartyQuotasPanel", "Quotas-1", panel)
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
            """.trimIndent(),
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
            ind to 85.43,
        )
        currRound.submit(round2.mapValues { it.value / quota })
        prevRound.submit(round1.mapValues { it.value / quota })
        round.submit(2)
        compareRendering("PartyQuotasPanel", "Quotas-2", panel)
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
            """.trimIndent(),
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
            sdlp to 260.56,
        )
        currRound.submit(round3.mapValues { it.value / quota })
        prevRound.submit(round2.mapValues { it.value / quota })
        round.submit(3)
        compareRendering("PartyQuotasPanel", "Quotas-3", panel)
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
            """.trimIndent(),
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
            con to 280.43,
        )
        currRound.submit(round4.mapValues { it.value / quota })
        prevRound.submit(round3.mapValues { it.value / quota })
        round.submit(4)
        compareRendering("PartyQuotasPanel", "Quotas-4", panel)
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
            """.trimIndent(),
        )

        val round5 = mapOf(
            apni to quota + 6032.81,
            uup to 5498.59,
            dup to 6057.35 + 4774.50 + 4455.40,
            pup to 2751.95,
            grn to 1763.36,
            sf to 1232.88,
            tuv to 958.87,
        )
        currRound.submit(round5.mapValues { it.value / quota })
        prevRound.submit(round4.mapValues { it.value / quota })
        round.submit(5)
        compareRendering("PartyQuotasPanel", "Quotas-5", panel)
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
            """.trimIndent(),
        )

        val round6 = mapOf(
            apni to quota + 6072.14,
            uup to 5767.92,
            dup to 6217.46 + 4862.61 + 4568.40,
            pup to 2936.28,
            grn to 1801.58,
            sf to 1235.10,
        )
        currRound.submit(round6.mapValues { it.value / quota })
        prevRound.submit(round5.mapValues { it.value / quota })
        round.submit(6)
        compareRendering("PartyQuotasPanel", "Quotas-6", panel)
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
            """.trimIndent(),
        )

        val round7 = mapOf(
            apni to quota + 6367.87,
            uup to 5783.14,
            dup to 6228.46 + 4865.61 + 4572.40,
            pup to 2950.28,
            grn to 2080.65,
        )
        currRound.submit(round7.mapValues { it.value / quota })
        prevRound.submit(round6.mapValues { it.value / quota })
        round.submit(7)
        compareRendering("PartyQuotasPanel", "Quotas-7", panel)
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
            """.trimIndent(),
        )

        val round8 = mapOf(
            apni to quota + 7268.87,
            uup to 6048.62,
            dup to 6275.01 + 4890.72 + 4599.73,
            pup to 3148.79,
        )
        currRound.submit(round8.mapValues { it.value / quota })
        prevRound.submit(round7.mapValues { it.value / quota })
        round.submit(8)
        compareRendering("PartyQuotasPanel", "Quotas-8", panel)
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
            """.trimIndent(),
        )

        val round9 = mapOf(
            apni to quota + quota,
            uup to 7257.62,
            dup to 6759.01 + 5268.26 + 4995.25,
        )
        currRound.submit(round9.mapValues { it.value / quota })
        prevRound.submit(round8.mapValues { it.value / quota })
        round.submit(9)
        compareRendering("PartyQuotasPanel", "Quotas-9", panel)
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
            """.trimIndent(),
        )

        val round10 = mapOf(
            apni to quota + quota,
            uup to quota,
            dup to quota + 5333.26 + 5093.25,
        )
        currRound.submit(round10.mapValues { it.value / quota })
        prevRound.submit(round9.mapValues { it.value / quota })
        round.submit(10)
        compareRendering("PartyQuotasPanel", "Quotas-10", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 10 (CHANGE FROM COUNT 9)
                DEMOCRATIC UNIONIST PARTY: 2.55 QUOTAS (+0.02)
                ALLIANCE: 2.00 QUOTAS (+0.00)
                ULSTER UNIONIST PARTY: 1.00 QUOTAS (-0.08)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent(),
        )

        val round11 = mapOf(
            apni to quota + quota,
            uup to quota,
            dup to quota + 5541.65 + 5410.84,
        )
        currRound.submit(round11.mapValues { it.value / quota })
        prevRound.submit(round10.mapValues { it.value / quota })
        round.submit(11)
        compareRendering("PartyQuotasPanel", "Quotas-11", panel)
        assertPublishes(
            panel.altText,
            """
                BELFAST EAST
                
                PARTY SUMMARY, COUNT 11 (CHANGE FROM COUNT 10)
                DEMOCRATIC UNIONIST PARTY: 2.63 QUOTAS (+0.08)
                ALLIANCE: 2.00 QUOTAS (+0.00)
                ULSTER UNIONIST PARTY: 1.00 QUOTAS (+0.00)
                
                FIRST PREF SWING: 1.0% SWING DUP TO APNI
            """.trimIndent(),
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
            oth to 362455,
        )
        val currVotes = mapOf(
            lib to 413957,
            alp to 332399,
            grn to 119470,
            oth to 228997,
        )
        val currQuota = 156404.0
        val prevQuota = 151596.0
        val currRound = Publisher(currVotes.mapValues { it.value / currQuota })
        val prevRound = Publisher(prevVotes.mapValues { it.value / prevQuota })

        val panel = PartyQuotasPanel.partyQuotas<Party>(
            curr = {
                quotas = currRound
                totalSeats = 6.asOneTimePublisher()
                header = "PARTY SUMMARY".asOneTimePublisher()
                subhead = "FIRST PREFERENCES".asOneTimePublisher()
            },
            change = {
                prevQuotas = prevRound
                header = "CHANGE SINCE 2016".asOneTimePublisher()
            },
            title = "SOUTH AUSTRALIA".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("PartyQuotasPanel", "Quotas-Oth", panel)
        assertPublishes(
            panel.altText,
            """
                SOUTH AUSTRALIA
                
                PARTY SUMMARY, FIRST PREFERENCES (CHANGE SINCE 2016)
                LIBERAL: 2.65 QUOTAS (+0.36)
                LABOR: 2.13 QUOTAS (+0.21)
                GREENS: 0.76 QUOTAS (+0.35)
                OTHERS: 1.46 QUOTAS (-0.93)
            """.trimIndent(),
        )
    }

    @Test
    fun testSwingRange() {
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
            sdlp to 141,
        )
        val currQuota = Publisher(1)
        val prevQuota = 5311.0
        val currRound = Publisher(emptyMap<Party, Double>())
        val prevRound = Publisher(prevVotes.mapValues { it.value / prevQuota })
        val currVotes = Publisher(emptyMap<Party, Int>())
        val currWinner = Publisher<Party?>(null)
        val round = Publisher(0)

        val panel = PartyQuotasPanel.partyQuotas<Party>(
            curr = {
                quotas = currRound.merge(currQuota) { votes, quota -> votes.mapValues { it.value / quota } }
                totalSeats = 5.asOneTimePublisher()
                header = "PARTY SUMMARY".asOneTimePublisher()
                subhead = round.map { if (it == 0) "WAITING..." else "COUNT $it" }
            },
            change = {
                prevQuotas = prevRound
                header = round.map { if (it <= 1) "CHANGE SINCE 2016" else "CHANGE FROM COUNT ${it - 1}" }
            },
            swing = {
                this.currVotes = currVotes
                this.prevVotes = prevVotes.asOneTimePublisher()
                order = Comparator.comparing { listOf(sf, apni, dup).indexOf(it) }
                header = "FIRST PREF SWING".asOneTimePublisher()
                range = 0.02.asOneTimePublisher()
            },
            map = createMap {
                shapes = niShapesByConstituency().asOneTimePublisher()
                selectedShape = 9.asOneTimePublisher()
                leadingParty = currWinner
                focus = listOf(9, 10, 12, 15).asOneTimePublisher()
                header = "BELFAST".asOneTimePublisher()
            },
            title = "BELFAST EAST".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)

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
            ind to 84,
        )
        currVotes.submit(round1)
        val quota = 6727.0
        currRound.submit(round1.mapValues { it.value / quota })
        round.submit(1)
        currWinner.submit(dup)
        compareRendering("PartyQuotasPanel", "SwingRange", panel)
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
            """.trimIndent(),
        )
    }

    private fun niShapesByConstituency(): Map<Int, Shape> {
        val niMap = PartyQuotasPanelTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/ni-constituencies.shp")
        return ShapefileReader.readShapes(niMap, "OBJECTID", Int::class.java)
    }
}
