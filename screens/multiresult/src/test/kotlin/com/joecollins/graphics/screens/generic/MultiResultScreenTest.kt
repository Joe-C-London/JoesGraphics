package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.MultiResultScreen.Companion.createMap
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Shape
import java.util.concurrent.Flow
class MultiResultScreenTest {

    @Test
    fun testSimplePanel() {
        val districts = listOf(
            District(
                8,
                "Stanhope-Marshfield",
                mapOf(
                    Candidate("Wade MacLauchlan", lib, true) to 1196,
                    Candidate("Bloyce Thompson", pc) to 1300,
                    Candidate("Sarah Donald", grn) to 747,
                    Candidate("Marian White", ndp) to 46,
                ),
                false,
                mapOf(
                    lib to 1938,
                    pc to 1338,
                    grn to 347,
                    ndp to 443,
                ),
            ),
            District(
                15,
                "Brackley-Hunter River",
                mapOf(
                    Candidate("Windsor Wight", lib) to 899,
                    Candidate("Dennis King", pc, true) to 1315,
                    Candidate("Greg Bradley", grn) to 879,
                    Candidate("Leah-Jane Hayward", ndp) to 57,
                ),
                true,
                mapOf(
                    lib to 1389,
                    pc to 1330,
                    grn to 462,
                    ndp to 516,
                ),
            ),
            District(
                17,
                "New Haven-Rocky Point",
                mapOf(
                    Candidate("Judy MacNevin", lib) to 515,
                    Candidate("Kris Currie", pc) to 1068,
                    Candidate("Peter Bevan-Baker", grn, true) to 1870,
                    Candidate("Don Wills", ind) to 26,
                ),
                true,
                mapOf(
                    lib to 1046,
                    pc to 609,
                    grn to 2077,
                    ndp to 58,
                ),
            ),
        )
        val shapesByDistrict = peiShapesByDistrict()
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc)
        val panel = MultiResultScreen.of(
            list = districts.asOneTimePublisher(),
            curr = {
                votes = { votes.asOneTimePublisher() }
                header = { ("DISTRICT $districtNum").asOneTimePublisher() }
                subhead = { name.uppercase().asOneTimePublisher() }
                incumbentMarker = "(MLA)"
                winner = { (if (leaderHasWon) votes.entries.maxByOrNull { e -> e.value }!!.key else null).asOneTimePublisher() }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2015".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            map = createMap {
                shapes = { shapesByDistrict }
                selectedShape = { districtNum }
                leadingParty = {
                    votes.entries.maxByOrNull { e -> e.value }?.key?.party?.let { PartyResult(it, leaderHasWon) }
                        .asOneTimePublisher()
                }
                focus = { if (districtNum < 10) listOf(1, 2, 3, 4, 5, 6, 7, 8) else listOf(15, 16, 17, 18, 19, 20) }
                header = { (if (districtNum < 10) "CARDIGAN" else "MALPEQUE").asOneTimePublisher() }
            },
            title = "PARTY LEADERS".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "Basic-1", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                DISTRICT 8, STANHOPE-MARSHFIELD
                BLOYCE THOMPSON (PCP): 1,300 (39.5%)
                WADE MACLAUCHLAN (MLA) (LIB): 1,196 (36.4%)
                SARAH DONALD (GRN): 747 (22.7%)
                MARIAN WHITE (NDP): 46 (1.4%)
                
                DISTRICT 15, BRACKLEY-HUNTER RIVER
                DENNIS KING (MLA) (PCP): 1,315 (41.7%) WINNER
                WINDSOR WIGHT (LIB): 899 (28.5%)
                GREG BRADLEY (GRN): 879 (27.9%)
                LEAH-JANE HAYWARD (NDP): 57 (1.8%)
                
                DISTRICT 17, NEW HAVEN-ROCKY POINT
                PETER BEVAN-BAKER (MLA) (GRN): 1,870 (53.8%) WINNER
                KRIS CURRIE (PCP): 1,068 (30.7%)
                JUDY MACNEVIN (LIB): 515 (14.8%)
                DON WILLS (IND): 26 (0.7%)
            """.trimIndent(),
        )
    }

    @Test
    fun testVariousUpdates() {
        val district8 = District(
            8,
            "Stanhope-Marshfield",
            mapOf(
                Candidate("Sarah Donald", grn) to 0,
                Candidate("Wade MacLauchlan", lib, true) to 0,
                Candidate("Bloyce Thompson", pc) to 0,
                Candidate("Marian White", ndp) to 0,
            ),
            false,
            mapOf(
                lib to 1938,
                pc to 1338,
                grn to 347,
                ndp to 443,
            ),
            "0 OF 10 POLLS REPORTING",
            0.0,
        )
        val district15 = District(
            15,
            "Brackley-Hunter River",
            mapOf(
                Candidate("Greg Bradley", grn) to 0,
                Candidate("Leah-Jane Hayward", ndp) to 0,
                Candidate("Dennis King", pc, true) to 0,
                Candidate("Windsor Wight", lib) to 0,
            ),
            false,
            mapOf(
                lib to 1389,
                pc to 1330,
                grn to 462,
                ndp to 516,
            ),
            "0 OF 10 POLLS REPORTING",
            0.0,
        )
        val district17 = District(
            17,
            "New Haven-Rocky Point",
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 0,
                Candidate("Kris Currie", pc) to 0,
                Candidate("Judy MacNevin", lib) to 0,
                Candidate("Don Wills", ind) to 0,
            ),
            false,
            mapOf(
                lib to 1046,
                pc to 609,
                grn to 2077,
                ndp to 58,
            ),
            "0 OF 10 POLLS REPORTING",
            0.0,
        )
        val districts = Publisher(listOf(district15, district17))
        val shapesByDistrict = peiShapesByDistrict()
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc)
        val title = Publisher("MAJOR PARTY LEADERS")
        val panel = MultiResultScreen.of(
            list = districts,
            curr = {
                votes = { getVotes() }
                header = { name.uppercase().asOneTimePublisher() }
                subhead = { getStatus() }
                incumbentMarker = "(MLA)"
                winner = { winner }
                pctReporting = { getPctReporting() }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2015".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            map = createMap {
                shapes = { shapesByDistrict }
                selectedShape = { districtNum }
                leadingParty = {
                    leader.map { e ->
                        if (e == null) {
                            null
                        } else {
                            PartyResult(e.first.party, e.second)
                        }
                    }
                }
                focus = { if (districtNum < 10) listOf(1, 2, 3, 4, 5, 6, 7, 8) else listOf(15, 16, 17, 18, 19, 20) }
                header = { (if (districtNum < 10) "CARDIGAN" else "MALPEQUE").asOneTimePublisher() }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "Update-1", panel)
        assertPublishes(
            panel.altText,
            """
                MAJOR PARTY LEADERS
                
                BRACKLEY-HUNTER RIVER, 0 OF 10 POLLS REPORTING
                GREG BRADLEY (GRN): WAITING...
                LEAH-JANE HAYWARD (NDP): WAITING...
                DENNIS KING (MLA) (PCP): WAITING...
                WINDSOR WIGHT (LIB): WAITING...
                
                NEW HAVEN-ROCKY POINT, 0 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): WAITING...
                KRIS CURRIE (PCP): WAITING...
                JUDY MACNEVIN (LIB): WAITING...
                DON WILLS (IND): WAITING...
            """.trimIndent(),
        )

        district17.update(
            "1 OF 10 POLLS REPORTING",
            0.1,
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 851,
                Candidate("Kris Currie", pc) to 512,
                Candidate("Judy MacNevin", lib) to 290,
                Candidate("Don Wills", ind) to 7,
            ),
        )
        compareRendering("MultiResultPanel", "Update-2", panel)
        assertPublishes(
            panel.altText,
            """
                MAJOR PARTY LEADERS
                
                BRACKLEY-HUNTER RIVER, 0 OF 10 POLLS REPORTING
                GREG BRADLEY (GRN): WAITING...
                LEAH-JANE HAYWARD (NDP): WAITING...
                DENNIS KING (MLA) (PCP): WAITING...
                WINDSOR WIGHT (LIB): WAITING...
                
                NEW HAVEN-ROCKY POINT, 1 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        districts.submit(listOf(district8, district15, district17))
        title.submit("PARTY LEADERS")
        compareRendering("MultiResultPanel", "Update-3", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                STANHOPE-MARSHFIELD, 0 OF 10 POLLS REPORTING
                SARAH DONALD (GRN): WAITING...
                WADE MACLAUCHLAN (MLA) (LIB): WAITING...
                BLOYCE THOMPSON (PCP): WAITING...
                MARIAN WHITE (NDP): WAITING...
                
                BRACKLEY-HUNTER RIVER, 0 OF 10 POLLS REPORTING
                GREG BRADLEY (GRN): WAITING...
                LEAH-JANE HAYWARD (NDP): WAITING...
                DENNIS KING (MLA) (PCP): WAITING...
                WINDSOR WIGHT (LIB): WAITING...
                
                NEW HAVEN-ROCKY POINT, 1 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district15.update(
            "1 OF 10 POLLS REPORTING",
            0.1,
            mapOf(
                Candidate("Greg Bradley", grn) to 287,
                Candidate("Leah-Jane Hayward", ndp) to 27,
                Candidate("Dennis King", pc, true) to 583,
                Candidate("Windsor Wight", lib) to 425,
            ),
        )
        compareRendering("MultiResultPanel", "Update-4", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                STANHOPE-MARSHFIELD, 0 OF 10 POLLS REPORTING
                SARAH DONALD (GRN): WAITING...
                WADE MACLAUCHLAN (MLA) (LIB): WAITING...
                BLOYCE THOMPSON (PCP): WAITING...
                MARIAN WHITE (NDP): WAITING...
                
                BRACKLEY-HUNTER RIVER, 1 OF 10 POLLS REPORTING
                DENNIS KING (MLA) (PCP): 583 (44.1%)
                WINDSOR WIGHT (LIB): 425 (32.1%)
                GREG BRADLEY (GRN): 287 (21.7%)
                LEAH-JANE HAYWARD (NDP): 27 (2.0%)
                
                NEW HAVEN-ROCKY POINT, 1 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district8.update(
            "1 OF 10 POLLS REPORTING",
            0.1,
            mapOf(
                Candidate("Sarah Donald", grn) to 285,
                Candidate("Wade MacLauchlan", lib, true) to 620,
                Candidate("Bloyce Thompson", pc) to 609,
                Candidate("Marian White", ndp) to 22,
            ),
        )
        compareRendering("MultiResultPanel", "Update-5", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                STANHOPE-MARSHFIELD, 1 OF 10 POLLS REPORTING
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                BRACKLEY-HUNTER RIVER, 1 OF 10 POLLS REPORTING
                DENNIS KING (MLA) (PCP): 583 (44.1%)
                WINDSOR WIGHT (LIB): 425 (32.1%)
                GREG BRADLEY (GRN): 287 (21.7%)
                LEAH-JANE HAYWARD (NDP): 27 (2.0%)
                
                NEW HAVEN-ROCKY POINT, 1 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district15.update(
            "5 OF 10 POLLS REPORTING",
            0.5,
            mapOf(
                Candidate("Greg Bradley", grn) to 287 + 72 + 91 + 79 + 38,
                Candidate("Leah-Jane Hayward", ndp) to 27 + 7 + 7 + 1 + 3,
                Candidate("Dennis King", pc, true) to 583 + 87 + 109 + 76 + 54,
                Candidate("Windsor Wight", lib) to 425 + 73 + 66 + 58 + 30,
            ),
            true,
        )
        compareRendering("MultiResultPanel", "Update-6", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                STANHOPE-MARSHFIELD, 1 OF 10 POLLS REPORTING
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                BRACKLEY-HUNTER RIVER, 5 OF 10 POLLS REPORTING
                DENNIS KING (MLA) (PCP): 909 (41.8%) WINNER
                WINDSOR WIGHT (LIB): 652 (30.0%)
                GREG BRADLEY (GRN): 567 (26.1%)
                LEAH-JANE HAYWARD (NDP): 45 (2.1%)
                
                NEW HAVEN-ROCKY POINT, 1 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        districts.submit(listOf(district8, district17))
        title.submit("PARTY LEADERS IN DOUBT")
        compareRendering("MultiResultPanel", "Update-7", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS IN DOUBT
                
                STANHOPE-MARSHFIELD, 1 OF 10 POLLS REPORTING
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                NEW HAVEN-ROCKY POINT, 1 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district15.update(
            "10 OF 10 POLLS REPORTING",
            1.0,
            mapOf(
                Candidate("Greg Bradley", grn) to 879,
                Candidate("Leah-Jane Hayward", ndp) to 57,
                Candidate("Dennis King", pc, true) to 1315,
                Candidate("Windsor Wight", lib) to 899,
            ),
        )
        // intentionally same as before, as this district is no longer displayed
        compareRendering("MultiResultPanel", "Update-7", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS IN DOUBT
                
                STANHOPE-MARSHFIELD, 1 OF 10 POLLS REPORTING
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                NEW HAVEN-ROCKY POINT, 1 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district17.update(
            "2 OF 10 POLLS REPORTING",
            0.2,
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 851 + 117,
                Candidate("Kris Currie", pc) to 512 + 90,
                Candidate("Judy MacNevin", lib) to 290 + 28,
                Candidate("Don Wills", ind) to 7 + 4,
            ),
        )
        compareRendering("MultiResultPanel", "Update-8", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS IN DOUBT
                
                STANHOPE-MARSHFIELD, 1 OF 10 POLLS REPORTING
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                NEW HAVEN-ROCKY POINT, 2 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 968 (51.0%)
                KRIS CURRIE (PCP): 602 (31.7%)
                JUDY MACNEVIN (LIB): 318 (16.7%)
                DON WILLS (IND): 11 (0.6%)
            """.trimIndent(),
        )

        district8.update(
            "2 OF 10 POLLS REPORTING",
            0.2,
            mapOf(
                Candidate("Sarah Donald", grn) to 285 + 50,
                Candidate("Wade MacLauchlan", lib, true) to 620 + 68,
                Candidate("Bloyce Thompson", pc) to 609 + 112,
                Candidate("Marian White", ndp) to 22 + 7,
            ),
        )
        compareRendering("MultiResultPanel", "Update-9", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS IN DOUBT
                
                STANHOPE-MARSHFIELD, 2 OF 10 POLLS REPORTING
                BLOYCE THOMPSON (PCP): 721 (40.7%)
                WADE MACLAUCHLAN (MLA) (LIB): 688 (38.8%)
                SARAH DONALD (GRN): 335 (18.9%)
                MARIAN WHITE (NDP): 29 (1.6%)
                
                NEW HAVEN-ROCKY POINT, 2 OF 10 POLLS REPORTING
                PETER BEVAN-BAKER (MLA) (GRN): 968 (51.0%)
                KRIS CURRIE (PCP): 602 (31.7%)
                JUDY MACNEVIN (LIB): 318 (16.7%)
                DON WILLS (IND): 11 (0.6%)
            """.trimIndent(),
        )
    }

    @Test
    fun testProgressLabels() {
        val district8 = District(
            8,
            "Stanhope-Marshfield",
            mapOf(
                Candidate("Sarah Donald", grn) to 0,
                Candidate("Wade MacLauchlan", lib, true) to 0,
                Candidate("Bloyce Thompson", pc) to 0,
                Candidate("Marian White", ndp) to 0,
            ),
            false,
            mapOf(
                lib to 1938,
                pc to 1338,
                grn to 347,
                ndp to 443,
            ),
            "0/10",
            0.0,
        )
        val district15 = District(
            15,
            "Brackley-Hunter River",
            mapOf(
                Candidate("Greg Bradley", grn) to 0,
                Candidate("Leah-Jane Hayward", ndp) to 0,
                Candidate("Dennis King", pc, true) to 0,
                Candidate("Windsor Wight", lib) to 0,
            ),
            false,
            mapOf(
                lib to 1389,
                pc to 1330,
                grn to 462,
                ndp to 516,
            ),
            "0/10",
            0.0,
        )
        val district17 = District(
            17,
            "New Haven-Rocky Point",
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 0,
                Candidate("Kris Currie", pc) to 0,
                Candidate("Judy MacNevin", lib) to 0,
                Candidate("Don Wills", ind) to 0,
            ),
            false,
            mapOf(
                lib to 1046,
                pc to 609,
                grn to 2077,
                ndp to 58,
            ),
            "0/10",
            0.0,
        )
        val districts = Publisher(listOf(district15, district17))
        val shapesByDistrict = peiShapesByDistrict()
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc)
        val title = Publisher("MAJOR PARTY LEADERS")
        val panel = MultiResultScreen.of(
            list = districts,
            curr = {
                votes = { getVotes() }
                header = { name.uppercase().asOneTimePublisher() }
                subhead = { null.asOneTimePublisher() }
                incumbentMarker = "(MLA)"
                winner = { winner }
                pctReporting = { getPctReporting() }
                progressLabel = { getStatus() }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2015".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            map = createMap {
                shapes = { shapesByDistrict }
                selectedShape = { districtNum }
                leadingParty = {
                    leader.map { e ->
                        if (e == null) {
                            null
                        } else {
                            PartyResult(e.first.party, e.second)
                        }
                    }
                }
                focus = { if (districtNum < 10) listOf(1, 2, 3, 4, 5, 6, 7, 8) else listOf(15, 16, 17, 18, 19, 20) }
                header = { (if (districtNum < 10) "CARDIGAN" else "MALPEQUE").asOneTimePublisher() }
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "ProgressLabels-1", panel)
        assertPublishes(
            panel.altText,
            """
                MAJOR PARTY LEADERS
                
                BRACKLEY-HUNTER RIVER [0/10]
                GREG BRADLEY (GRN): WAITING...
                LEAH-JANE HAYWARD (NDP): WAITING...
                DENNIS KING (MLA) (PCP): WAITING...
                WINDSOR WIGHT (LIB): WAITING...
                
                NEW HAVEN-ROCKY POINT [0/10]
                PETER BEVAN-BAKER (MLA) (GRN): WAITING...
                KRIS CURRIE (PCP): WAITING...
                JUDY MACNEVIN (LIB): WAITING...
                DON WILLS (IND): WAITING...
            """.trimIndent(),
        )

        district17.update(
            "1/10",
            0.1,
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 851,
                Candidate("Kris Currie", pc) to 512,
                Candidate("Judy MacNevin", lib) to 290,
                Candidate("Don Wills", ind) to 7,
            ),
        )
        compareRendering("MultiResultPanel", "ProgressLabels-2", panel)
        assertPublishes(
            panel.altText,
            """
                MAJOR PARTY LEADERS
                
                BRACKLEY-HUNTER RIVER [0/10]
                GREG BRADLEY (GRN): WAITING...
                LEAH-JANE HAYWARD (NDP): WAITING...
                DENNIS KING (MLA) (PCP): WAITING...
                WINDSOR WIGHT (LIB): WAITING...
                
                NEW HAVEN-ROCKY POINT [1/10]
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        districts.submit(listOf(district8, district15, district17))
        title.submit("PARTY LEADERS")
        compareRendering("MultiResultPanel", "ProgressLabels-3", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                STANHOPE-MARSHFIELD [0/10]
                SARAH DONALD (GRN): WAITING...
                WADE MACLAUCHLAN (MLA) (LIB): WAITING...
                BLOYCE THOMPSON (PCP): WAITING...
                MARIAN WHITE (NDP): WAITING...
                
                BRACKLEY-HUNTER RIVER [0/10]
                GREG BRADLEY (GRN): WAITING...
                LEAH-JANE HAYWARD (NDP): WAITING...
                DENNIS KING (MLA) (PCP): WAITING...
                WINDSOR WIGHT (LIB): WAITING...
                
                NEW HAVEN-ROCKY POINT [1/10]
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district15.update(
            "1/10",
            0.1,
            mapOf(
                Candidate("Greg Bradley", grn) to 287,
                Candidate("Leah-Jane Hayward", ndp) to 27,
                Candidate("Dennis King", pc, true) to 583,
                Candidate("Windsor Wight", lib) to 425,
            ),
        )
        compareRendering("MultiResultPanel", "ProgressLabels-4", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                STANHOPE-MARSHFIELD [0/10]
                SARAH DONALD (GRN): WAITING...
                WADE MACLAUCHLAN (MLA) (LIB): WAITING...
                BLOYCE THOMPSON (PCP): WAITING...
                MARIAN WHITE (NDP): WAITING...
                
                BRACKLEY-HUNTER RIVER [1/10]
                DENNIS KING (MLA) (PCP): 583 (44.1%)
                WINDSOR WIGHT (LIB): 425 (32.1%)
                GREG BRADLEY (GRN): 287 (21.7%)
                LEAH-JANE HAYWARD (NDP): 27 (2.0%)
                
                NEW HAVEN-ROCKY POINT [1/10]
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district8.update(
            "1/10",
            0.1,
            mapOf(
                Candidate("Sarah Donald", grn) to 285,
                Candidate("Wade MacLauchlan", lib, true) to 620,
                Candidate("Bloyce Thompson", pc) to 609,
                Candidate("Marian White", ndp) to 22,
            ),
        )
        compareRendering("MultiResultPanel", "ProgressLabels-5", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                STANHOPE-MARSHFIELD [1/10]
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                BRACKLEY-HUNTER RIVER [1/10]
                DENNIS KING (MLA) (PCP): 583 (44.1%)
                WINDSOR WIGHT (LIB): 425 (32.1%)
                GREG BRADLEY (GRN): 287 (21.7%)
                LEAH-JANE HAYWARD (NDP): 27 (2.0%)
                
                NEW HAVEN-ROCKY POINT [1/10]
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district15.update(
            "5/10",
            0.5,
            mapOf(
                Candidate("Greg Bradley", grn) to 287 + 72 + 91 + 79 + 38,
                Candidate("Leah-Jane Hayward", ndp) to 27 + 7 + 7 + 1 + 3,
                Candidate("Dennis King", pc, true) to 583 + 87 + 109 + 76 + 54,
                Candidate("Windsor Wight", lib) to 425 + 73 + 66 + 58 + 30,
            ),
            true,
        )
        compareRendering("MultiResultPanel", "ProgressLabels-6", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS
                
                STANHOPE-MARSHFIELD [1/10]
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                BRACKLEY-HUNTER RIVER [5/10]
                DENNIS KING (MLA) (PCP): 909 (41.8%) WINNER
                WINDSOR WIGHT (LIB): 652 (30.0%)
                GREG BRADLEY (GRN): 567 (26.1%)
                LEAH-JANE HAYWARD (NDP): 45 (2.1%)
                
                NEW HAVEN-ROCKY POINT [1/10]
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        districts.submit(listOf(district8, district17))
        title.submit("PARTY LEADERS IN DOUBT")
        compareRendering("MultiResultPanel", "ProgressLabels-7", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS IN DOUBT
                
                STANHOPE-MARSHFIELD [1/10]
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                NEW HAVEN-ROCKY POINT [1/10]
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district15.update(
            "10/10",
            1.0,
            mapOf(
                Candidate("Greg Bradley", grn) to 879,
                Candidate("Leah-Jane Hayward", ndp) to 57,
                Candidate("Dennis King", pc, true) to 1315,
                Candidate("Windsor Wight", lib) to 899,
            ),
        )
        // intentionally same as before, as this district is no longer displayed
        compareRendering("MultiResultPanel", "ProgressLabels-7", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS IN DOUBT
                
                STANHOPE-MARSHFIELD [1/10]
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                NEW HAVEN-ROCKY POINT [1/10]
                PETER BEVAN-BAKER (MLA) (GRN): 851 (51.3%)
                KRIS CURRIE (PCP): 512 (30.8%)
                JUDY MACNEVIN (LIB): 290 (17.5%)
                DON WILLS (IND): 7 (0.4%)
            """.trimIndent(),
        )

        district17.update(
            "2/10",
            0.2,
            mapOf(
                Candidate("Peter Bevan-Baker", grn, true) to 851 + 117,
                Candidate("Kris Currie", pc) to 512 + 90,
                Candidate("Judy MacNevin", lib) to 290 + 28,
                Candidate("Don Wills", ind) to 7 + 4,
            ),
        )
        compareRendering("MultiResultPanel", "ProgressLabels-8", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS IN DOUBT
                
                STANHOPE-MARSHFIELD [1/10]
                WADE MACLAUCHLAN (MLA) (LIB): 620 (40.4%)
                BLOYCE THOMPSON (PCP): 609 (39.6%)
                SARAH DONALD (GRN): 285 (18.6%)
                MARIAN WHITE (NDP): 22 (1.4%)
                
                NEW HAVEN-ROCKY POINT [2/10]
                PETER BEVAN-BAKER (MLA) (GRN): 968 (51.0%)
                KRIS CURRIE (PCP): 602 (31.7%)
                JUDY MACNEVIN (LIB): 318 (16.7%)
                DON WILLS (IND): 11 (0.6%)
            """.trimIndent(),
        )

        district8.update(
            "2/10",
            0.2,
            mapOf(
                Candidate("Sarah Donald", grn) to 285 + 50,
                Candidate("Wade MacLauchlan", lib, true) to 620 + 68,
                Candidate("Bloyce Thompson", pc) to 609 + 112,
                Candidate("Marian White", ndp) to 22 + 7,
            ),
        )
        compareRendering("MultiResultPanel", "ProgressLabels-9", panel)
        assertPublishes(
            panel.altText,
            """
                PARTY LEADERS IN DOUBT
                
                STANHOPE-MARSHFIELD [2/10]
                BLOYCE THOMPSON (PCP): 721 (40.7%)
                WADE MACLAUCHLAN (MLA) (LIB): 688 (38.8%)
                SARAH DONALD (GRN): 335 (18.9%)
                MARIAN WHITE (NDP): 29 (1.6%)
                
                NEW HAVEN-ROCKY POINT [2/10]
                PETER BEVAN-BAKER (MLA) (GRN): 968 (51.0%)
                KRIS CURRIE (PCP): 602 (31.7%)
                JUDY MACNEVIN (LIB): 318 (16.7%)
                DON WILLS (IND): 11 (0.6%)
            """.trimIndent(),
        )
    }

    @Test
    fun testOthersPanel() {
        val districts = listOf(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248,
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047,
                ),
            ),
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Tony Gunn", pa) to 186,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Mike Cyr", ind) to 47,
                    Candidate("Arty Watson", ind) to 114,
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393,
                ),
            ),
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201,
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922,
                ),
            ),
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val panel = MultiResultScreen.of(
            list = districts.asOneTimePublisher(),
            curr = {
                votes = { votes.asOneTimePublisher() }
                header = { ("DISTRICT $districtNum").asOneTimePublisher() }
                subhead = { name.uppercase().asOneTimePublisher() }
                incumbentMarker = "(MLA)"
                winner = {
                    (
                        if (leaderHasWon) {
                            votes.entries.maxByOrNull { it.value }!!.key
                        } else {
                            null
                        }
                        ).asOneTimePublisher()
                }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2018".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            title = "SAINT JOHN".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "Others-1", panel)
        assertPublishes(
            panel.altText,
            """
                SAINT JOHN
                
                DISTRICT 30, SAINT JOHN EAST
                GLEN SAVOIE (MLA) (PCP): 3,507 (56.4%) WINNER
                PHIL COMEAU (LIB): 1,639 (26.3%)
                PATRICK KEMP (PA): 434 (7.0%)
                GERALD IRISH (GRN): 394 (6.3%)
                JOSH FLOYD (NDP): 248 (4.0%)
                
                DISTRICT 32, SAINT JOHN HARBOUR
                ARLENE DUNN (PCP): 2,181 (41.4%)
                BRENT HARRIS (GRN): 1,224 (23.2%)
                ALICE MCKIM (LIB): 1,207 (22.9%)
                COURTNEY PYRKE (NDP): 309 (5.9%)
                OTHERS: 347 (6.6%)
                
                DISTRICT 33, SAINT JOHN LANCASTER
                DOROTHY SHEPHARD (MLA) (PCP): 3,560 (54.2%) WINNER
                SHARON TEARE (LIB): 1,471 (22.4%)
                JOANNA KILLEN (GRN): 938 (14.3%)
                PAUL SEELYE (PA): 394 (6.0%)
                DON DURANT (NDP): 201 (3.1%)
            """.trimIndent(),
        )
    }

    @Test
    fun testOthersPartyPanel() {
        val districts = listOf(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248,
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047,
                ),
            ),
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Others", Party.OTHERS) to 47 + 114 + 186,
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393,
                ),
            ),
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201,
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922,
                ),
            ),
            District(
                38,
                "Fredericton-Grand Lake",
                mapOf(
                    Candidate("Mary E. Wilson", pc, true) to 3374,
                    Candidate("Steven Burns", lib) to 2072,
                    Candidate("Gail Costello", grn) to 1306,
                    Candidate("Craig Rector", pa) to 745,
                    Candidate("Natasha M Akhtar", ndp) to 127,
                ),
                true,
                mapOf(
                    lib to 955,
                    pc to 2433,
                    grn to 472,
                    ndp to 114,
                    pa to 4799,
                ),
            ),
            District(
                40,
                "Fredericton South",
                mapOf(
                    Candidate("Brian MacKinnon", pc) to 2342,
                    Candidate("Nicole Picot", lib) to 895,
                    Candidate("David Coon", grn, true) to 4213,
                    Candidate("Wendell Betts", pa) to 234,
                    Candidate("Geoffrey Noseworthy", ndp) to 117,
                ),
                true,
                mapOf(
                    lib to 1525,
                    pc to 1042,
                    grn to 4273,
                    ndp to 132,
                    pa to 616,
                ),
            ),
            District(
                41,
                "Fredericton North",
                mapOf(
                    Candidate("Jill Green", pc) to 3227,
                    Candidate("Stephen Horsman", lib, true) to 1464,
                    Candidate("Luke Randall", grn) to 2464,
                    Candidate("Allen Price", pa) to 591,
                    Candidate("Mackenzie Thomason", ndp) to 100,
                ),
                true,
                mapOf(
                    lib to 2443,
                    pc to 2182,
                    grn to 1313,
                    ndp to 139,
                    pa to 1651,
                ),
            ),
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val panel = MultiResultScreen.ofParties(
            list = districts.asOneTimePublisher(),
            curr = {
                votes = { votes.mapKeys { e -> e.key.party }.asOneTimePublisher() }
                header = { ("DISTRICT $districtNum").asOneTimePublisher() }
                subhead = { name.uppercase().asOneTimePublisher() }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2018".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            title = "SAINT JOHN".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "OthersParty-1", panel)
        assertPublishes(
            panel.altText,
            """
                SAINT JOHN
                
                DISTRICT 30, SAINT JOHN EAST
                PROGRESSIVE CONSERVATIVE: 56.4%
                LIBERAL: 26.3%
                PEOPLE'S ALLIANCE: 7.0%
                GREEN: 6.3%
                NEW DEMOCRATIC PARTY: 4.0%
                
                DISTRICT 32, SAINT JOHN HARBOUR
                PROGRESSIVE CONSERVATIVE: 41.4%
                GREEN: 23.2%
                LIBERAL: 22.9%
                NEW DEMOCRATIC PARTY: 5.9%
                OTHERS: 6.6%
                
                DISTRICT 33, SAINT JOHN LANCASTER
                PROGRESSIVE CONSERVATIVE: 54.2%
                LIBERAL: 22.4%
                GREEN: 14.3%
                PEOPLE'S ALLIANCE: 6.0%
                NEW DEMOCRATIC PARTY: 3.1%
                
                DISTRICT 38, FREDERICTON-GRAND LAKE
                PROGRESSIVE CONSERVATIVE: 44.3%
                LIBERAL: 27.2%
                GREEN: 17.1%
                PEOPLE'S ALLIANCE: 9.8%
                NEW DEMOCRATIC PARTY: 1.7%
                
                DISTRICT 40, FREDERICTON SOUTH
                GREEN: 54.0%
                PROGRESSIVE CONSERVATIVE: 30.0%
                LIBERAL: 11.5%
                PEOPLE'S ALLIANCE: 3.0%
                NEW DEMOCRATIC PARTY: 1.5%
                
                DISTRICT 41, FREDERICTON NORTH
                PROGRESSIVE CONSERVATIVE: 41.1%
                GREEN: 31.4%
                LIBERAL: 18.7%
                PEOPLE'S ALLIANCE: 7.5%
                NEW DEMOCRATIC PARTY: 1.3%
            """.trimIndent(),
        )
    }

    @Test
    fun testMultipleRowsPanel() {
        val districts = mutableListOf(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248,
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047,
                ),
            ),
            District(
                31,
                "Portland-Simonds",
                mapOf(
                    Candidate("Trevor Holder", pc, true) to 3170,
                    Candidate("Tim Jones", lib) to 1654,
                    Candidate("Stefan Warner", grn) to 483,
                    Candidate("Darella Jackson", pa) to 282,
                    Candidate("Erik Heinze-Milne", ndp) to 164,
                ),
                true,
                mapOf(
                    lib to 1703,
                    pc to 3168,
                    grn to 435,
                    ndp to 449,
                    ind to 191,
                ),
            ),
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Tony Gunn", pa) to 186,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Mike Cyr", ind) to 47,
                    Candidate("Arty Watson", ind) to 114,
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393,
                ),
            ),
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201,
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922,
                ),
            ),
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val districtsPublisher = Publisher(districts)
        val panel = MultiResultScreen.of(
            list = districtsPublisher,
            curr = {
                votes = { votes.asOneTimePublisher() }
                header = { ("DISTRICT $districtNum").asOneTimePublisher() }
                subhead = { name.uppercase().asOneTimePublisher() }
                incumbentMarker = "(MLA)"
                winner = {
                    (
                        if (leaderHasWon) {
                            votes.entries.maxByOrNull { it.value }!!.key
                        } else {
                            null
                        }
                        ).asOneTimePublisher()
                }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2018".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            title = "ELECTION 2020: NEW BRUNSWICK DECIDES".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "MultipleRows-1", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 30, SAINT JOHN EAST
                GLEN SAVOIE (MLA) (PCP): 3,507 (56.4%) WINNER
                PHIL COMEAU (LIB): 1,639 (26.3%)
                PATRICK KEMP (PA): 434 (7.0%)
                GERALD IRISH (GRN): 394 (6.3%)
                JOSH FLOYD (NDP): 248 (4.0%)
                
                DISTRICT 31, PORTLAND-SIMONDS
                TREVOR HOLDER (MLA) (PCP): 3,170 (55.1%) WINNER
                TIM JONES (LIB): 1,654 (28.8%)
                STEFAN WARNER (GRN): 483 (8.4%)
                DARELLA JACKSON (PA): 282 (4.9%)
                ERIK HEINZE-MILNE (NDP): 164 (2.9%)
                
                DISTRICT 32, SAINT JOHN HARBOUR
                ARLENE DUNN (PCP): 2,181 (41.4%)
                BRENT HARRIS (GRN): 1,224 (23.2%)
                ALICE MCKIM (LIB): 1,207 (22.9%)
                COURTNEY PYRKE (NDP): 309 (5.9%)
                OTHERS: 347 (6.6%)
                
                DISTRICT 33, SAINT JOHN LANCASTER
                DOROTHY SHEPHARD (MLA) (PCP): 3,560 (54.2%) WINNER
                SHARON TEARE (LIB): 1,471 (22.4%)
                JOANNA KILLEN (GRN): 938 (14.3%)
                PAUL SEELYE (PA): 394 (6.0%)
                DON DURANT (NDP): 201 (3.1%)
            """.trimIndent(),
        )

        districts.add(
            District(
                34,
                "Kings Centre",
                mapOf(
                    Candidate("Bill Oliver", pc, true) to 4583,
                    Candidate("Bruce Bryer", grn) to 1006,
                    Candidate("Paul Adams", lib) to 911,
                    Candidate("William Edgett", pa) to 693,
                    Candidate("Margaret Anderson Kilfoil", ndp) to 254,
                ),
                true,
                mapOf(
                    lib to 1785,
                    pc to 3267,
                    grn to 731,
                    ndp to 342,
                    pa to 1454,
                ),
            ),
        )
        districts.add(
            District(
                35,
                "Fundy-The Isles-Saint John West",
                mapOf(
                    Candidate("Andrea Anderson-Mason", pc, true) to 4740,
                    Candidate("Tony Mann", lib) to 726,
                    Candidate("Vincent Edgett", pa) to 688,
                    Candidate("Lois Mitchell", grn) to 686,
                    Candidate("Sharon Greenlaw", ndp) to 291,
                ),
                true,
                mapOf(
                    lib to 2422,
                    pc to 3808,
                    grn to 469,
                    ndp to 203,
                    pa to 1104,
                ),
            ),
        )
        districtsPublisher.submit(districts)
        compareRendering("MultiResultPanel", "MultipleRows-2", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 30, SAINT JOHN EAST
                GLEN SAVOIE (MLA) (PCP): 3,507 (56.4%) WINNER
                PHIL COMEAU (LIB): 1,639 (26.3%)
                PATRICK KEMP (PA): 434 (7.0%)
                OTHERS: 642 (10.3%)
                
                DISTRICT 31, PORTLAND-SIMONDS
                TREVOR HOLDER (MLA) (PCP): 3,170 (55.1%) WINNER
                TIM JONES (LIB): 1,654 (28.8%)
                STEFAN WARNER (GRN): 483 (8.4%)
                OTHERS: 446 (7.8%)
                
                DISTRICT 32, SAINT JOHN HARBOUR
                ARLENE DUNN (PCP): 2,181 (41.4%)
                BRENT HARRIS (GRN): 1,224 (23.2%)
                ALICE MCKIM (LIB): 1,207 (22.9%)
                OTHERS: 656 (12.5%)
                
                DISTRICT 33, SAINT JOHN LANCASTER
                DOROTHY SHEPHARD (MLA) (PCP): 3,560 (54.2%) WINNER
                SHARON TEARE (LIB): 1,471 (22.4%)
                JOANNA KILLEN (GRN): 938 (14.3%)
                OTHERS: 595 (9.1%)
                
                DISTRICT 34, KINGS CENTRE
                BILL OLIVER (MLA) (PCP): 4,583 (61.5%) WINNER
                BRUCE BRYER (GRN): 1,006 (13.5%)
                PAUL ADAMS (LIB): 911 (12.2%)
                OTHERS: 947 (12.7%)
                
                DISTRICT 35, FUNDY-THE ISLES-SAINT JOHN WEST
                ANDREA ANDERSON-MASON (MLA) (PCP): 4,740 (66.5%) WINNER
                TONY MANN (LIB): 726 (10.2%)
                VINCENT EDGETT (PA): 688 (9.6%)
                OTHERS: 977 (13.7%)
            """.trimIndent(),
        )

        districts.removeAt(3)
        districts.removeAt(2)
        districts.removeAt(1)
        districts.removeAt(0)
        districtsPublisher.submit(districts)
        compareRendering("MultiResultPanel", "MultipleRows-3", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 34, KINGS CENTRE
                BILL OLIVER (MLA) (PCP): 4,583 (61.5%) WINNER
                BRUCE BRYER (GRN): 1,006 (13.5%)
                PAUL ADAMS (LIB): 911 (12.2%)
                WILLIAM EDGETT (PA): 693 (9.3%)
                MARGARET ANDERSON KILFOIL (NDP): 254 (3.4%)
                
                DISTRICT 35, FUNDY-THE ISLES-SAINT JOHN WEST
                ANDREA ANDERSON-MASON (MLA) (PCP): 4,740 (66.5%) WINNER
                TONY MANN (LIB): 726 (10.2%)
                VINCENT EDGETT (PA): 688 (9.6%)
                LOIS MITCHELL (GRN): 686 (9.6%)
                SHARON GREENLAW (NDP): 291 (4.1%)
            """.trimIndent(),
        )
    }

    @Test
    fun testMultipleRowsPanelWithProgressLabels() {
        val districts = mutableListOf(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248,
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047,
                ),
            ),
            District(
                31,
                "Portland-Simonds",
                mapOf(
                    Candidate("Trevor Holder", pc, true) to 3170,
                    Candidate("Tim Jones", lib) to 1654,
                    Candidate("Stefan Warner", grn) to 483,
                    Candidate("Darella Jackson", pa) to 282,
                    Candidate("Erik Heinze-Milne", ndp) to 164,
                ),
                true,
                mapOf(
                    lib to 1703,
                    pc to 3168,
                    grn to 435,
                    ndp to 449,
                    ind to 191,
                ),
            ),
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Tony Gunn", pa) to 186,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Mike Cyr", ind) to 47,
                    Candidate("Arty Watson", ind) to 114,
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393,
                ),
            ),
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201,
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922,
                ),
            ),
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val districtsPublisher = Publisher(districts)
        val panel = MultiResultScreen.of(
            list = districtsPublisher,
            curr = {
                votes = { votes.asOneTimePublisher() }
                header = { ("DISTRICT $districtNum").asOneTimePublisher() }
                subhead = { name.uppercase().asOneTimePublisher() }
                incumbentMarker = "(MLA)"
                winner = {
                    (
                        if (leaderHasWon) {
                            votes.entries.maxByOrNull { it.value }!!.key
                        } else {
                            null
                        }
                        ).asOneTimePublisher()
                }
                progressLabel = { "100% IN".asOneTimePublisher() }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2018".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            title = "ELECTION 2020: NEW BRUNSWICK DECIDES".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "MultipleRowsProgressLabels-1", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 30, SAINT JOHN EAST [100% IN]
                GLEN SAVOIE (MLA) (PCP): 3,507 (56.4%) WINNER
                PHIL COMEAU (LIB): 1,639 (26.3%)
                PATRICK KEMP (PA): 434 (7.0%)
                GERALD IRISH (GRN): 394 (6.3%)
                JOSH FLOYD (NDP): 248 (4.0%)
                
                DISTRICT 31, PORTLAND-SIMONDS [100% IN]
                TREVOR HOLDER (MLA) (PCP): 3,170 (55.1%) WINNER
                TIM JONES (LIB): 1,654 (28.8%)
                STEFAN WARNER (GRN): 483 (8.4%)
                DARELLA JACKSON (PA): 282 (4.9%)
                ERIK HEINZE-MILNE (NDP): 164 (2.9%)
                
                DISTRICT 32, SAINT JOHN HARBOUR [100% IN]
                ARLENE DUNN (PCP): 2,181 (41.4%)
                BRENT HARRIS (GRN): 1,224 (23.2%)
                ALICE MCKIM (LIB): 1,207 (22.9%)
                COURTNEY PYRKE (NDP): 309 (5.9%)
                OTHERS: 347 (6.6%)
                
                DISTRICT 33, SAINT JOHN LANCASTER [100% IN]
                DOROTHY SHEPHARD (MLA) (PCP): 3,560 (54.2%) WINNER
                SHARON TEARE (LIB): 1,471 (22.4%)
                JOANNA KILLEN (GRN): 938 (14.3%)
                PAUL SEELYE (PA): 394 (6.0%)
                DON DURANT (NDP): 201 (3.1%)
            """.trimIndent(),
        )

        districts.add(
            District(
                34,
                "Kings Centre",
                mapOf(
                    Candidate("Bill Oliver", pc, true) to 4583,
                    Candidate("Bruce Bryer", grn) to 1006,
                    Candidate("Paul Adams", lib) to 911,
                    Candidate("William Edgett", pa) to 693,
                    Candidate("Margaret Anderson Kilfoil", ndp) to 254,
                ),
                true,
                mapOf(
                    lib to 1785,
                    pc to 3267,
                    grn to 731,
                    ndp to 342,
                    pa to 1454,
                ),
            ),
        )
        districts.add(
            District(
                35,
                "Fundy-The Isles-Saint John West",
                mapOf(
                    Candidate("Andrea Anderson-Mason", pc, true) to 4740,
                    Candidate("Tony Mann", lib) to 726,
                    Candidate("Vincent Edgett", pa) to 688,
                    Candidate("Lois Mitchell", grn) to 686,
                    Candidate("Sharon Greenlaw", ndp) to 291,
                ),
                true,
                mapOf(
                    lib to 2422,
                    pc to 3808,
                    grn to 469,
                    ndp to 203,
                    pa to 1104,
                ),
            ),
        )
        districtsPublisher.submit(districts)
        compareRendering("MultiResultPanel", "MultipleRowsProgressLabels-2", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 30, SAINT JOHN EAST [100% IN]
                GLEN SAVOIE (MLA) (PCP): 3,507 (56.4%) WINNER
                PHIL COMEAU (LIB): 1,639 (26.3%)
                PATRICK KEMP (PA): 434 (7.0%)
                OTHERS: 642 (10.3%)
                
                DISTRICT 31, PORTLAND-SIMONDS [100% IN]
                TREVOR HOLDER (MLA) (PCP): 3,170 (55.1%) WINNER
                TIM JONES (LIB): 1,654 (28.8%)
                STEFAN WARNER (GRN): 483 (8.4%)
                OTHERS: 446 (7.8%)
                
                DISTRICT 32, SAINT JOHN HARBOUR [100% IN]
                ARLENE DUNN (PCP): 2,181 (41.4%)
                BRENT HARRIS (GRN): 1,224 (23.2%)
                ALICE MCKIM (LIB): 1,207 (22.9%)
                OTHERS: 656 (12.5%)
                
                DISTRICT 33, SAINT JOHN LANCASTER [100% IN]
                DOROTHY SHEPHARD (MLA) (PCP): 3,560 (54.2%) WINNER
                SHARON TEARE (LIB): 1,471 (22.4%)
                JOANNA KILLEN (GRN): 938 (14.3%)
                OTHERS: 595 (9.1%)
                
                DISTRICT 34, KINGS CENTRE [100% IN]
                BILL OLIVER (MLA) (PCP): 4,583 (61.5%) WINNER
                BRUCE BRYER (GRN): 1,006 (13.5%)
                PAUL ADAMS (LIB): 911 (12.2%)
                OTHERS: 947 (12.7%)
                
                DISTRICT 35, FUNDY-THE ISLES-SAINT JOHN WEST [100% IN]
                ANDREA ANDERSON-MASON (MLA) (PCP): 4,740 (66.5%) WINNER
                TONY MANN (LIB): 726 (10.2%)
                VINCENT EDGETT (PA): 688 (9.6%)
                OTHERS: 977 (13.7%)
            """.trimIndent(),
        )

        districts.removeAt(3)
        districts.removeAt(2)
        districts.removeAt(1)
        districts.removeAt(0)
        districtsPublisher.submit(districts)
        compareRendering("MultiResultPanel", "MultipleRowsProgressLabels-3", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 34, KINGS CENTRE [100% IN]
                BILL OLIVER (MLA) (PCP): 4,583 (61.5%) WINNER
                BRUCE BRYER (GRN): 1,006 (13.5%)
                PAUL ADAMS (LIB): 911 (12.2%)
                WILLIAM EDGETT (PA): 693 (9.3%)
                MARGARET ANDERSON KILFOIL (NDP): 254 (3.4%)
                
                DISTRICT 35, FUNDY-THE ISLES-SAINT JOHN WEST [100% IN]
                ANDREA ANDERSON-MASON (MLA) (PCP): 4,740 (66.5%) WINNER
                TONY MANN (LIB): 726 (10.2%)
                VINCENT EDGETT (PA): 688 (9.6%)
                LOIS MITCHELL (GRN): 686 (9.6%)
                SHARON GREENLAW (NDP): 291 (4.1%)
            """.trimIndent(),
        )
    }

    @Test
    fun testRunoffs() {
        val districts = listOf(
            District(
                2,
                "Regular Election",
                mapOf(
                    Candidate("David Perdue", gop) to 2458453,
                    Candidate("Jon Ossoff", dem) to 2371921,
                    Candidate("Shane Hazel", ind) to 114873,
                ),
                false,
                emptyMap(),
            ),
            District(
                3,
                "Special Election",
                mapOf(
                    Candidate("Raphael Warnock", dem) to 1615550,
                    Candidate("Kelly Loeffler", gop) to 1271320,
                    Candidate("Doug Collins", gop) to 979052,
                    Candidate("Deborah Jackson", dem) to 323833,
                    Candidate.OTHERS to 718808,
                ),
                false,
                emptyMap(),
            ),
        )
        val panel = MultiResultScreen.of(
            list = districts.asOneTimePublisher(),
            curr = {
                votes = { votes.asOneTimePublisher() }
                header = { name.uppercase().asOneTimePublisher() }
                subhead = { ("CLASS $districtNum").asOneTimePublisher() }
                runoff = { runoff }
            },
            title = "GEORGIA SENATE".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "Runoff-1", panel)
        assertPublishes(
            panel.altText,
            """
                GEORGIA SENATE
                
                REGULAR ELECTION, CLASS 2
                DAVID PERDUE (GOP): 2,458,453 (49.7%)
                JON OSSOFF (DEM): 2,371,921 (48.0%)
                SHANE HAZEL (IND): 114,873 (2.3%)
                
                SPECIAL ELECTION, CLASS 3
                RAPHAEL WARNOCK (DEM): 1,615,550 (32.9%)
                KELLY LOEFFLER (GOP): 1,271,320 (25.9%)
                DOUG COLLINS (GOP): 979,052 (19.9%)
                DEBORAH JACKSON (DEM): 323,833 (6.6%)
                OTHERS: 718,808 (14.6%)
            """.trimIndent(),
        )

        districts.forEach { it.declareRunoff() }
        compareRendering("MultiResultPanel", "Runoff-2", panel)
        assertPublishes(
            panel.altText,
            """
                GEORGIA SENATE
                
                REGULAR ELECTION, CLASS 2
                DAVID PERDUE (GOP): 2,458,453 (49.7%) RUNOFF
                JON OSSOFF (DEM): 2,371,921 (48.0%) RUNOFF
                SHANE HAZEL (IND): 114,873 (2.3%)
                
                SPECIAL ELECTION, CLASS 3
                RAPHAEL WARNOCK (DEM): 1,615,550 (32.9%) RUNOFF
                KELLY LOEFFLER (GOP): 1,271,320 (25.9%) RUNOFF
                DOUG COLLINS (GOP): 979,052 (19.9%)
                DEBORAH JACKSON (DEM): 323,833 (6.6%)
                OTHERS: 718,808 (14.6%)
            """.trimIndent(),
        )
    }

    @Test
    fun testMapAdditionalHighlights() {
        val districts = listOf(
            District(
                13,
                "Charlottetown-Brighton",
                mapOf(
                    Candidate("Jordan Brown", lib, true) to 1223,
                    Candidate("Donna Hurry", pc) to 567,
                    Candidate("Ole Hammarlund", grn) to 1301,
                    Candidate("Simone Webster", ndp) to 138,
                ),
                false,
                mapOf(
                    lib to 1054,
                    pc to 1032,
                    grn to 352,
                    ndp to 265,
                ),
            ),
            District(
                12,
                "Charlottetown-Victoria Park",
                mapOf(
                    Candidate("Richard Brown", lib, true) to 875,
                    Candidate("Tim Keizer", pc) to 656,
                    Candidate("Karla Bernard", grn) to 1272,
                    Candidate("Joe Byrne", ndp) to 338,
                ),
                false,
                mapOf(
                    lib to 955,
                    pc to 666,
                    grn to 456,
                    ndp to 348,
                ),
            ),
            District(
                10,
                "Charlottetown-Winsloe",
                mapOf(
                    Candidate("Robert Mitchell", lib, true) to 1420,
                    Candidate("Mike Gillis", pc) to 865,
                    Candidate("Amanda Morrison", grn) to 1057,
                    Candidate("Jesse Reddin Cousins", ndp) to 41,
                ),
                false,
                mapOf(
                    lib to 1425,
                    pc to 1031,
                    grn to 295,
                    ndp to 360,
                ),
            ),
        )
        val shapesByDistrict = peiShapesByDistrict()
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc)
        val panel = MultiResultScreen.of(
            list = districts.asOneTimePublisher(),
            curr = {
                votes = { votes.asOneTimePublisher() }
                header = { ("DISTRICT $districtNum").asOneTimePublisher() }
                subhead = { name.uppercase().asOneTimePublisher() }
                incumbentMarker = "(MLA)"
                winner = {
                    (
                        if (leaderHasWon) {
                            votes.entries.maxByOrNull { it.value }!!.key
                        } else {
                            null
                        }
                        ).asOneTimePublisher()
                }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2015".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            map = createMap {
                shapes = { shapesByDistrict }
                selectedShape = { districtNum }
                leadingParty = {
                    votes.entries.maxByOrNull { it.value }?.key?.party?.let { PartyResult(it, leaderHasWon) }.asOneTimePublisher()
                }
                focus = { listOf(10, 11, 12, 13, 14) }
                additionalHighlights = { listOf(9, 10, 11, 12, 13, 14) }
                header = { "CHARLOTTETOWN".asOneTimePublisher() }
            },
            title = "CABINET MEMBERS IN CHARLOTTETOWN".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "MapAdditionalHighlights-1", panel)
        assertPublishes(
            panel.altText,
            """
                CABINET MEMBERS IN CHARLOTTETOWN
                
                DISTRICT 13, CHARLOTTETOWN-BRIGHTON
                OLE HAMMARLUND (GRN): 1,301 (40.3%)
                JORDAN BROWN (MLA) (LIB): 1,223 (37.9%)
                DONNA HURRY (PCP): 567 (17.6%)
                SIMONE WEBSTER (NDP): 138 (4.3%)
                
                DISTRICT 12, CHARLOTTETOWN-VICTORIA PARK
                KARLA BERNARD (GRN): 1,272 (40.5%)
                RICHARD BROWN (MLA) (LIB): 875 (27.9%)
                TIM KEIZER (PCP): 656 (20.9%)
                JOE BYRNE (NDP): 338 (10.8%)
                
                DISTRICT 10, CHARLOTTETOWN-WINSLOE
                ROBERT MITCHELL (MLA) (LIB): 1,420 (42.0%)
                AMANDA MORRISON (GRN): 1,057 (31.2%)
                MIKE GILLIS (PCP): 865 (25.6%)
                JESSE REDDIN COUSINS (NDP): 41 (1.2%)
            """.trimIndent(),
        )
    }

    @Test
    fun testPartiesOnly() {
        val districts = mutableListOf(
            District(
                30,
                "Saint John East",
                mapOf(
                    Candidate("Glen Savoie", pc, true) to 3507,
                    Candidate("Phil Comeau", lib) to 1639,
                    Candidate("Gerald Irish", grn) to 394,
                    Candidate("Patrick Kemp", pa) to 434,
                    Candidate("Josh Floyd", ndp) to 248,
                ),
                true,
                mapOf(
                    lib to 1775,
                    pc to 3017,
                    grn to 373,
                    ndp to 402,
                    pa to 1047,
                ),
            ),
            District(
                31,
                "Portland-Simonds",
                mapOf(
                    Candidate("Trevor Holder", pc, true) to 3170,
                    Candidate("Tim Jones", lib) to 1654,
                    Candidate("Stefan Warner", grn) to 483,
                    Candidate("Darella Jackson", pa) to 282,
                    Candidate("Erik Heinze-Milne", ndp) to 164,
                ),
                true,
                mapOf(
                    lib to 1703,
                    pc to 3168,
                    grn to 435,
                    ndp to 449,
                    ind to 191,
                ),
            ),
            District(
                32,
                "Saint John Harbour",
                mapOf(
                    Candidate("Arlene Dunn", pc) to 2181,
                    Candidate("Alice McKim", lib) to 1207,
                    Candidate("Brent Harris", grn) to 1224,
                    Candidate("Tony Gunn", pa) to 186,
                    Candidate("Courtney Pyrke", ndp) to 309,
                    Candidate("Mike Cyr", ind) to 47,
                    Candidate("Arty Watson", ind) to 114,
                ),
                false,
                mapOf(
                    lib to 1865,
                    pc to 1855,
                    grn to 721,
                    ndp to 836,
                    pa to 393,
                ),
            ),
            District(
                33,
                "Saint John Lancaster",
                mapOf(
                    Candidate("Dorothy Shephard", pc, true) to 3560,
                    Candidate("Sharon Teare", lib) to 1471,
                    Candidate("Joanna Killen", grn) to 938,
                    Candidate("Paul Seelye", pa) to 394,
                    Candidate("Don Durant", ndp) to 201,
                ),
                true,
                mapOf(
                    lib to 1727,
                    pc to 3001,
                    grn to 582,
                    ndp to 414,
                    pa to 922,
                ),
            ),
        )
        val swingometerOrder = listOf(ndp, grn, lib, ind, pc, pa)
        val districtPublisher = Publisher(districts)
        val panel = MultiResultScreen.ofParties(
            list = districtPublisher,
            curr = {
                votes = { partyVotes }
                header = { ("DISTRICT $districtNum").asOneTimePublisher() }
                subhead = { name.uppercase().asOneTimePublisher() }
            },
            prev = {
                votes = { prevVotes.asOneTimePublisher() }
                swing = {
                    header = { "SWING SINCE 2018".asOneTimePublisher() }
                    partyOrder = swingometerOrder
                }
            },
            title = "ELECTION 2020: NEW BRUNSWICK DECIDES".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("MultiResultPanel", "PartiesOnly-1", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 30, SAINT JOHN EAST
                PROGRESSIVE CONSERVATIVE: 56.4%
                LIBERAL: 26.3%
                PEOPLE'S ALLIANCE: 7.0%
                GREEN: 6.3%
                NEW DEMOCRATIC PARTY: 4.0%
                
                DISTRICT 31, PORTLAND-SIMONDS
                PROGRESSIVE CONSERVATIVE: 55.1%
                LIBERAL: 28.8%
                GREEN: 8.4%
                PEOPLE'S ALLIANCE: 4.9%
                NEW DEMOCRATIC PARTY: 2.9%
                
                DISTRICT 32, SAINT JOHN HARBOUR
                PROGRESSIVE CONSERVATIVE: 41.4%
                GREEN: 23.2%
                LIBERAL: 22.9%
                NEW DEMOCRATIC PARTY: 5.9%
                PEOPLE'S ALLIANCE: 3.5%
                INDEPENDENT: 3.1%
                
                DISTRICT 33, SAINT JOHN LANCASTER
                PROGRESSIVE CONSERVATIVE: 54.2%
                LIBERAL: 22.4%
                GREEN: 14.3%
                PEOPLE'S ALLIANCE: 6.0%
                NEW DEMOCRATIC PARTY: 3.1%
            """.trimIndent(),
        )

        districts.add(
            District(
                34,
                "Kings Centre",
                mapOf(
                    Candidate("Bill Oliver", pc, true) to 4583,
                    Candidate("Bruce Bryer", grn) to 1006,
                    Candidate("Paul Adams", lib) to 911,
                    Candidate("William Edgett", pa) to 693,
                    Candidate("Margaret Anderson Kilfoil", ndp) to 254,
                ),
                true,
                mapOf(
                    lib to 1785,
                    pc to 3267,
                    grn to 731,
                    ndp to 342,
                    pa to 1454,
                ),
            ),
        )
        districts.add(
            District(
                35,
                "Fundy-The Isles-Saint John West",
                mapOf(
                    Candidate("Andrea Anderson-Mason", pc, true) to 4740,
                    Candidate("Tony Mann", lib) to 726,
                    Candidate("Vincent Edgett", pa) to 688,
                    Candidate("Lois Mitchell", grn) to 686,
                    Candidate("Sharon Greenlaw", ndp) to 291,
                ),
                true,
                mapOf(
                    lib to 2422,
                    pc to 3808,
                    grn to 469,
                    ndp to 203,
                    pa to 1104,
                ),
            ),
        )
        districtPublisher.submit(districts)
        compareRendering("MultiResultPanel", "PartiesOnly-2", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 30, SAINT JOHN EAST
                PROGRESSIVE CONSERVATIVE: 56.4%
                LIBERAL: 26.3%
                PEOPLE'S ALLIANCE: 7.0%
                GREEN: 6.3%
                NEW DEMOCRATIC PARTY: 4.0%
                
                DISTRICT 31, PORTLAND-SIMONDS
                PROGRESSIVE CONSERVATIVE: 55.1%
                LIBERAL: 28.8%
                GREEN: 8.4%
                PEOPLE'S ALLIANCE: 4.9%
                NEW DEMOCRATIC PARTY: 2.9%
                
                DISTRICT 32, SAINT JOHN HARBOUR
                PROGRESSIVE CONSERVATIVE: 41.4%
                GREEN: 23.2%
                LIBERAL: 22.9%
                NEW DEMOCRATIC PARTY: 5.9%
                PEOPLE'S ALLIANCE: 3.5%
                INDEPENDENT: 3.1%
                
                DISTRICT 33, SAINT JOHN LANCASTER
                PROGRESSIVE CONSERVATIVE: 54.2%
                LIBERAL: 22.4%
                GREEN: 14.3%
                PEOPLE'S ALLIANCE: 6.0%
                NEW DEMOCRATIC PARTY: 3.1%
                
                DISTRICT 34, KINGS CENTRE
                PROGRESSIVE CONSERVATIVE: 61.5%
                GREEN: 13.5%
                LIBERAL: 12.2%
                PEOPLE'S ALLIANCE: 9.3%
                NEW DEMOCRATIC PARTY: 3.4%
                
                DISTRICT 35, FUNDY-THE ISLES-SAINT JOHN WEST
                PROGRESSIVE CONSERVATIVE: 66.5%
                LIBERAL: 10.2%
                PEOPLE'S ALLIANCE: 9.6%
                GREEN: 9.6%
                NEW DEMOCRATIC PARTY: 4.1%
            """.trimIndent(),
        )

        districts.removeAt(3)
        districts.removeAt(2)
        districts.removeAt(1)
        districts.removeAt(0)
        districtPublisher.submit(districts)
        compareRendering("MultiResultPanel", "PartiesOnly-3", panel)
        assertPublishes(
            panel.altText,
            """
                ELECTION 2020: NEW BRUNSWICK DECIDES
                
                DISTRICT 34, KINGS CENTRE
                PROGRESSIVE CONSERVATIVE: 61.5%
                GREEN: 13.5%
                LIBERAL: 12.2%
                PEOPLE'S ALLIANCE: 9.3%
                NEW DEMOCRATIC PARTY: 3.4%
                
                DISTRICT 35, FUNDY-THE ISLES-SAINT JOHN WEST
                PROGRESSIVE CONSERVATIVE: 66.5%
                LIBERAL: 10.2%
                PEOPLE'S ALLIANCE: 9.6%
                GREEN: 9.6%
                NEW DEMOCRATIC PARTY: 4.1%
            """.trimIndent(),
        )
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MultiResultScreenTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return ShapefileReader.readShapes(peiMap, "DIST_NO", Int::class.java)
    }

    private class District(
        val districtNum: Int,
        val name: String,
        var votes: Map<Candidate, Int>,
        var leaderHasWon: Boolean,
        val prevVotes: Map<Party, Int>,
        private var status: String = "100% REPORTING",
        private var pctReporting: Double = 1.0,
    ) {
        private var topTwoToRunoff = false

        private val statusPublisher = Publisher(status)
        fun getStatus(): Flow.Publisher<String> {
            return statusPublisher
        }

        private val pctReportingPublisher = Publisher(pctReporting)
        fun getPctReporting(): Flow.Publisher<Double> {
            return pctReportingPublisher
        }

        private val votesPublisher = Publisher(votes)
        fun getVotes(): Flow.Publisher<Map<Candidate, Int>> {
            return votesPublisher
        }

        val partyVotes = Publisher(calculatePartyVotes())
        private fun calculatePartyVotes() =
            votes.entries
                .groupBy { it.key.party }
                .mapValues { e -> e.value.sumOf { it.value } }

        private val leaderHasWonPublisher = Publisher(leaderHasWon)

        val winner = Publisher(calculateWinner())
        private fun calculateWinner() = if (leaderHasWon) votes.entries.maxByOrNull { it.value }!!.key else null

        val runoff = Publisher(calculateRunoff())
        private fun calculateRunoff() =
            if (!topTwoToRunoff) {
                null
            } else {
                votes.entries.asSequence().sortedByDescending { it.value }
                    .take(2)
                    .map { it.key }
                    .toSet()
            }

        val leader = Publisher(calculateLeader())
        private fun calculateLeader() = if (votes.values.all { it == 0 }) {
            null
        } else {
            Pair(
                votes.entries.asSequence()
                    .filter { it.value > 0 }
                    .maxByOrNull { it.value }!!.key,
                leaderHasWon,
            )
        }

        fun update(status: String, pctReporting: Double, votes: Map<Candidate, Int>) {
            this.status = status
            this.pctReporting = pctReporting
            this.votes = votes
            update()
        }

        fun update(
            status: String,
            pctReporting: Double,
            votes: Map<Candidate, Int>,
            leaderHasWon: Boolean,
        ) {
            this.status = status
            this.pctReporting = pctReporting
            this.votes = votes
            this.leaderHasWon = leaderHasWon
            update()
        }

        fun declareRunoff() {
            topTwoToRunoff = true
            update()
        }

        private fun update() {
            statusPublisher.submit(status)
            pctReportingPublisher.submit(pctReporting)
            votesPublisher.submit(votes)
            partyVotes.submit(calculatePartyVotes())
            leaderHasWonPublisher.submit(leaderHasWon)
            winner.submit(calculateWinner())
            runoff.submit(calculateRunoff())
            leader.submit(calculateLeader())
        }
    }

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val pc = Party("Progressive Conservative", "PCP", Color.BLUE)
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pa = Party("People's Alliance", "PA", Color.MAGENTA.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
        private val dem = Party("Democratic", "DEM", Color.BLUE)
        private val gop = Party("Republican", "GOP", Color.RED)
    }
}
