package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.SwingometerScreen.Companion.calculateSwing
import com.joecollins.graphics.screens.generic.SwingometerScreen.Companion.convertToPartyOrCandidateForSwingometer
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Coalition
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.models.general.PartyOrCoalition
import com.joecollins.models.general.PartyResult
import com.joecollins.models.general.PartyResult.Companion.elected
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class SwingometerScreenTest {

    @Test
    fun testBasicTwoParties() {
        val prevResult = Publisher(nbPrevResult())
        val currResult = Publisher(nbCurrResult())
        val parties = Publisher(Pair(lib, pc))
        val swing = Publisher(
            mapOf(
                pc to +0.0745,
                lib to -0.0345,
                grn to +0.0336,
                pa to -0.0339,
                ndp to -0.0335,
                ind to -0.0062,
            ),
        )
        val panel = SwingometerScreen.of(
            prevVotes = prevResult.convertToPartyOrCandidateForSwingometer(),
            results = currResult,
            swing = swing,
            parties = parties,
            seatLabelIncrement = 3.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            title = "NEW BRUNSWICK".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-TwoParty-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            5.5% SWING LIB TO PCP
            PCP WOULD HAVE 26 ON UNIFORM SWING
            
            LIB NEEDS 1.8% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 1.7% SWING FROM LIB TO GAIN MAJORITY
            """.trimIndent(),
        )

        parties.submit(Pair(grn, pc))
        compareRendering("SwingometerScreen", "Basic-TwoParty-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            2.0% SWING GRN TO PCP
            PCP WOULD HAVE 22 ON UNIFORM SWING
            
            GRN NEEDS 25.7% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 21.3% SWING FROM GRN TO GAIN MAJORITY
            """.trimIndent(),
        )

        parties.submit(Pair(pa, pc))
        compareRendering("SwingometerScreen", "Basic-TwoParty-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            5.4% SWING PA TO PCP
            PCP WOULD HAVE 23 ON UNIFORM SWING
            
            PA NEEDS 26.6% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 16.3% SWING FROM PA TO GAIN MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testBasicSwingUpdates() {
        val prevResult = Publisher(nbPrevResult())
        val result = nbCurrResult().mapValues { null }
        val currResult = Publisher<Map<String, PartyResult?>>(result)
        val parties = Publisher(Pair(lib, pc))
        val swing = Publisher<Map<Party, Double>>(emptyMap())
        val panel = SwingometerScreen.of(
            prevVotes = prevResult.convertToPartyOrCandidateForSwingometer(),
            results = currResult,
            swing = swing,
            parties = parties,
            seatLabelIncrement = 3.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            title = "NEW BRUNSWICK".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-Updates-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            NO SWING BETWEEN LIB AND PCP
            PCP WOULD HAVE 22 ON UNIFORM SWING
            
            LIB NEEDS 1.8% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 1.7% SWING FROM LIB TO GAIN MAJORITY
            """.trimIndent(),
        )

        currResult.submit(nbCurrResult())
        swing.submit(mapOf(pc to +0.0745, lib to -0.0345, grn to +0.0336, pa to -0.0339, ndp to -0.0335, ind to -0.0062))
        compareRendering("SwingometerScreen", "Basic-Updates-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            5.5% SWING LIB TO PCP
            PCP WOULD HAVE 26 ON UNIFORM SWING
            
            LIB NEEDS 1.8% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 1.7% SWING FROM LIB TO GAIN MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testFilteredTwoParties() {
        val prevResult = Publisher(nbPrevResult())
        val currResult = Publisher(nbCurrResult())
        val parties = Publisher(Pair(lib, pc))
        val swing = Publisher(
            mapOf(
                pc to +0.0745,
                lib to -0.0345,
                grn to +0.0336,
                pa to -0.0339,
                ndp to -0.0335,
                ind to -0.0062,
            ),
        )
        val seatsFiltered = Publisher<Set<String>?>(
            setOf(
                "Oromocto-Lincoln-Fredericton",
                "Fredericton-Grand Lake",
                "New Maryland-Sunbury",
                "Fredericton South",
                "Fredericton North",
                "Fredericton-York",
                "Fredericton West-Hanwell",
                "Carleton-York",
            ),
        )
        val panel = SwingometerScreen.of(
            prevVotes = prevResult.convertToPartyOrCandidateForSwingometer(),
            results = currResult,
            swing = swing,
            parties = parties,
            seatLabelIncrement = 3.asOneTimePublisher(),
            seatFilter = seatsFiltered,
            header = "SWINGOMETER".asOneTimePublisher(),
            title = "NEW BRUNSWICK".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Filtered-TwoParty-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            5.5% SWING LIB TO PCP
            PCP WOULD HAVE 26 ON UNIFORM SWING
            
            LIB NEEDS 1.8% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 1.7% SWING FROM LIB TO GAIN MAJORITY
            """.trimIndent(),
        )

        seatsFiltered.submit(emptySet())
        compareRendering("SwingometerScreen", "Filtered-TwoParty-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            5.5% SWING LIB TO PCP
            PCP WOULD HAVE 26 ON UNIFORM SWING
            
            LIB NEEDS 1.8% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 1.7% SWING FROM LIB TO GAIN MAJORITY
            """.trimIndent(),
        )

        seatsFiltered.submit(null)
        compareRendering("SwingometerScreen", "Filtered-TwoParty-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            5.5% SWING LIB TO PCP
            PCP WOULD HAVE 26 ON UNIFORM SWING
            
            LIB NEEDS 1.8% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 1.7% SWING FROM LIB TO GAIN MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testBasicProgressLabel() {
        val prevResult = Publisher(nbPrevResult())
        val currResult = Publisher(nbCurrResult())
        val parties = Publisher(Pair(lib, pc))
        val swing = Publisher(
            mapOf(
                pc to +0.0745,
                lib to -0.0345,
                grn to +0.0336,
                pa to -0.0339,
                ndp to -0.0335,
                ind to -0.0062,
            ),
        )
        val panel = SwingometerScreen.of(
            prevVotes = prevResult.convertToPartyOrCandidateForSwingometer(),
            results = currResult,
            swing = swing,
            parties = parties,
            seatLabelIncrement = 3.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            progressLabel = "100% IN".asOneTimePublisher(),
            title = "NEW BRUNSWICK".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-ProgressLabel", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER [100% IN]

            5.5% SWING LIB TO PCP
            PCP WOULD HAVE 26 ON UNIFORM SWING
            
            LIB NEEDS 1.8% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 1.7% SWING FROM LIB TO GAIN MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testBasicTwoPartiesWithVotes() {
        val prevResult = Publisher(nbPrevResult())
        val currResult = Publisher(nbCurrResult())
        val parties = Publisher(Pair(lib, pc))
        val curr = Publisher(mapOf(pc to 147790, lib to 129025, grn to 57252, pa to 34526, ndp to 6220, ind to 824))
        val prev = Publisher(mapOf(pc to 121300, lib to 143791, grn to 45186, pa to 47860, ndp to 19039, ind to 3187))
        val panel = SwingometerScreen.of(
            prevVotes = prevResult.convertToPartyOrCandidateForSwingometer(),
            results = currResult,
            swing = calculateSwing(
                currTotal = curr,
                prevTotal = prev,
            ),
            parties = parties,
            seatLabelIncrement = 3.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            title = "NEW BRUNSWICK".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-TwoPartyVotes-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            5.5% SWING LIB TO PCP
            PCP WOULD HAVE 26 ON UNIFORM SWING
            
            LIB NEEDS 1.8% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 1.7% SWING FROM LIB TO GAIN MAJORITY
            """.trimIndent(),
        )

        parties.submit(Pair(grn, pc))
        compareRendering("SwingometerScreen", "Basic-TwoPartyVotes-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            2.0% SWING GRN TO PCP
            PCP WOULD HAVE 22 ON UNIFORM SWING
            
            GRN NEEDS 25.7% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 21.3% SWING FROM GRN TO GAIN MAJORITY
            """.trimIndent(),
        )

        parties.submit(Pair(pa, pc))
        compareRendering("SwingometerScreen", "Basic-TwoPartyVotes-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER

            5.4% SWING PA TO PCP
            PCP WOULD HAVE 23 ON UNIFORM SWING
            
            PA NEEDS 26.6% SWING FROM PCP TO GAIN MAJORITY
            PCP NEEDS 16.3% SWING FROM PA TO GAIN MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testIncludingCarryovers() {
        val dem = Party("Democrats", "DEM", Color.BLUE)
        val gop = Party("Republicans", "GOP", Color.RED)
        val oth = Party.OTHERS

        val prevResult = mapOf(
            "AL" to mapOf(gop to 640, dem to 358, oth to 2),
            "AK" to mapOf(gop to 445, dem to 116, oth to 439),
            "AZ" to mapOf(gop to 537, dem to 408, oth to 55),
            "AR" to mapOf(gop to 597, dem to 363, oth to 40),
            "CA" to mapOf(dem to 1000),
            "CO" to mapOf(dem to 500, gop to 443, oth to 57),
            "CT" to mapOf(dem to 632, gop to 346, oth to 22),
            "FL" to mapOf(gop to 520, dem to 443, oth to 37),
            "GA" to mapOf(gop to 548, dem to 410, oth to 42),
            "HI" to mapOf(dem to 736, gop to 222, oth to 42),
            "ID" to mapOf(gop to 661, dem to 278, oth to 61),
            "IL" to mapOf(dem to 549, gop to 398, oth to 53),
            "IN" to mapOf(gop to 521, dem to 424, oth to 55),
            "IA" to mapOf(gop to 601, dem to 357, oth to 27),
            "KS" to mapOf(gop to 621, dem to 322, oth to 57),
            "KY" to mapOf(gop to 573, dem to 427),
            "LA" to mapOf(gop to 606, dem to 394),
            "MD" to mapOf(dem to 609, gop to 357, oth to 34),
            "MO" to mapOf(gop to 493, dem to 462, oth to 45),
            "NV" to mapOf(dem to 471, gop to 447, oth to 82),
            "NH" to mapOf(dem to 480, gop to 479, oth to 41),
            "NY" to mapOf(dem to 704, gop to 274, oth to 22),
            "NC" to mapOf(gop to 511, dem to 453, oth to 36),
            "ND" to mapOf(gop to 784, dem to 170, oth to 46),
            "OH" to mapOf(gop to 580, dem to 371, oth to 49),
            "OK" to mapOf(gop to 677, dem to 245, oth to 78),
            "OR" to mapOf(dem to 561, gop to 334, oth to 105),
            "PA" to mapOf(gop to 489, dem to 472, oth to 39),
            "SC" to mapOf(gop to 605, dem to 370, oth to 25),
            "SD" to mapOf(gop to 718, dem to 282),
            "UT" to mapOf(gop to 681, dem to 271, oth to 48),
            "VT" to mapOf(dem to 631, gop to 330, oth to 39),
            "WA" to mapOf(dem to 588, gop to 409, oth to 3),
            "WI" to mapOf(gop to 502, dem to 468, oth to 30),
        ).asOneTimePublisher()
        val parties = Publisher(dem to gop)
        val panel = SwingometerScreen.of(
            prevVotes = prevResult.convertToPartyOrCandidateForSwingometer(),
            results = emptyMap<String, PartyResult?>().asOneTimePublisher(),
            swing = emptyMap<Party, Double>().asOneTimePublisher(),
            parties = parties,
            seatLabelIncrement = 5.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            carryovers = mapOf(dem to 36, gop to 30).asOneTimePublisher(),
            title = "US SENATE".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Carryovers-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            US SENATE
            SWINGOMETER

            NO SWING BETWEEN DEM AND GOP
            GOP WOULD HAVE 52 ON UNIFORM SWING
            
            DEM NEEDS 1.7% SWING FROM GOP TO GAIN MAJORITY
            GOP NEEDS TO AVOID 1.6% SWING TO DEM TO HOLD MAJORITY
            """.trimIndent(),
        )

        parties.submit(gop to dem)
        compareRendering("SwingometerScreen", "Carryovers-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            US SENATE
            SWINGOMETER

            NO SWING BETWEEN GOP AND DEM
            GOP WOULD HAVE 52 ON UNIFORM SWING
            
            GOP NEEDS TO AVOID 1.6% SWING TO DEM TO HOLD MAJORITY
            DEM NEEDS 1.7% SWING FROM GOP TO GAIN MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testIncludingCarryoversUncontested() {
        val dem = Party("Democrats", "DEM", Color.BLUE)
        val gop = Party("Republicans", "GOP", Color.RED)
        val oth = Party.OTHERS

        val prevResult = mapOf(
            "AL" to mapOf(gop to 640, dem to 358, oth to 2),
            "AK" to mapOf(gop to 445, dem to 116, oth to 439),
            "AZ" to mapOf(gop to 537, dem to 408, oth to 55),
            "AR" to mapOf(gop to 597, dem to 363, oth to 40),
            "CA" to mapOf(dem to 0),
            "CO" to mapOf(dem to 500, gop to 443, oth to 57),
            "CT" to mapOf(dem to 632, gop to 346, oth to 22),
            "FL" to mapOf(gop to 520, dem to 443, oth to 37),
            "GA" to mapOf(gop to 548, dem to 410, oth to 42),
            "HI" to mapOf(dem to 736, gop to 222, oth to 42),
            "ID" to mapOf(gop to 661, dem to 278, oth to 61),
            "IL" to mapOf(dem to 549, gop to 398, oth to 53),
            "IN" to mapOf(gop to 521, dem to 424, oth to 55),
            "IA" to mapOf(gop to 601, dem to 357, oth to 27),
            "KS" to mapOf(gop to 621, dem to 322, oth to 57),
            "KY" to mapOf(gop to 573, dem to 427),
            "LA" to mapOf(gop to 0),
            "MD" to mapOf(dem to 609, gop to 357, oth to 34),
            "MO" to mapOf(gop to 493, dem to 462, oth to 45),
            "NV" to mapOf(dem to 471, gop to 447, oth to 82),
            "NH" to mapOf(dem to 480, gop to 479, oth to 41),
            "NY" to mapOf(dem to 704, gop to 274, oth to 22),
            "NC" to mapOf(gop to 511, dem to 453, oth to 36),
            "ND" to mapOf(gop to 784, dem to 170, oth to 46),
            "OH" to mapOf(gop to 580, dem to 371, oth to 49),
            "OK" to mapOf(gop to 677, dem to 245, oth to 78),
            "OR" to mapOf(dem to 561, gop to 334, oth to 105),
            "PA" to mapOf(gop to 489, dem to 472, oth to 39),
            "SC" to mapOf(gop to 605, dem to 370, oth to 25),
            "SD" to mapOf(gop to 0),
            "UT" to mapOf(gop to 681, dem to 271, oth to 48),
            "VT" to mapOf(dem to 631, gop to 330, oth to 39),
            "WA" to mapOf(dem to 588, gop to 409, oth to 3),
            "WI" to mapOf(gop to 502, dem to 468, oth to 30),
        ).asOneTimePublisher()
        val parties = Publisher(dem to gop)
        val panel = SwingometerScreen.of(
            prevVotes = prevResult.convertToPartyOrCandidateForSwingometer(),
            results = emptyMap<String, PartyResult?>().asOneTimePublisher(),
            swing = emptyMap<Party, Double>().asOneTimePublisher(),
            parties = parties,
            seatLabelIncrement = 5.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            carryovers = mapOf(dem to 36, gop to 30).asOneTimePublisher(),
            title = "US SENATE".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Carryovers-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            US SENATE
            SWINGOMETER

            NO SWING BETWEEN DEM AND GOP
            GOP WOULD HAVE 52 ON UNIFORM SWING
            
            DEM NEEDS 1.7% SWING FROM GOP TO GAIN MAJORITY
            GOP NEEDS TO AVOID 1.6% SWING TO DEM TO HOLD MAJORITY
            """.trimIndent(),
        )

        parties.submit(gop to dem)
        compareRendering("SwingometerScreen", "Carryovers-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            US SENATE
            SWINGOMETER

            NO SWING BETWEEN GOP AND DEM
            GOP WOULD HAVE 52 ON UNIFORM SWING
            
            GOP NEEDS TO AVOID 1.6% SWING TO DEM TO HOLD MAJORITY
            DEM NEEDS 1.7% SWING FROM GOP TO GAIN MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testWeightedDots() {
        val dem = Party("Democrats", "DEM", Color.BLUE)
        val gop = Party("Republicans", "GOP", Color.RED)
        val oth = Party.OTHERS

        val prevResult = mapOf(
            ("AL" to 9) to mapOf(dem to 729547, gop to 1318255, oth to 75570),
            ("AK" to 3) to mapOf(dem to 116454, gop to 163387, oth to 38767),
            ("AZ" to 11) to mapOf(dem to 1161167, gop to 1252401, oth to 191089),
            ("AR" to 6) to mapOf(dem to 380494, gop to 684872, oth to 65310),
            ("CA" to 55) to mapOf(dem to 8753788, gop to 4483810, oth to 943997),
            ("CO" to 9) to mapOf(dem to 1338870, gop to 1202484, oth to 238893),
            ("CT" to 7) to mapOf(dem to 897572, gop to 673215, oth to 74133),
            ("DE" to 3) to mapOf(dem to 235603, gop to 185127, oth to 23084),
            ("DC" to 3) to mapOf(dem to 282830, gop to 12723, oth to 15715),
            ("FL" to 29) to mapOf(dem to 4504975, gop to 4617886, oth to 297178),
            ("GA" to 16) to mapOf(dem to 1877963, gop to 2089104, oth to 147665),
            ("HI" to 4) to mapOf(dem to 266891, gop to 128847, oth to 33199),
            ("ID" to 4) to mapOf(dem to 189765, gop to 409055, oth to 91435),
            ("IL" to 20) to mapOf(dem to 3090729, gop to 2146015, oth to 299680),
            ("IN" to 11) to mapOf(dem to 1033126, gop to 1557286, oth to 144546),
            ("IA" to 6) to mapOf(dem to 653669, gop to 800983, oth to 111379),
            ("KS" to 6) to mapOf(dem to 427005, gop to 671018, oth to 86379),
            ("KY" to 8) to mapOf(dem to 628854, gop to 1202971, oth to 92324),
            ("LA" to 8) to mapOf(dem to 780154, gop to 1178638, oth to 70240),
            ("ME" to 2) to mapOf(dem to 357735, gop to 335593, oth to 54599),
            ("ME-01" to 1) to mapOf(dem to 212774, gop to 154384, oth to 27171),
            ("ME-02" to 1) to mapOf(dem to 144817, gop to 181177, oth to 27422),
            ("MD" to 10) to mapOf(dem to 1677928, gop to 943169, oth to 160349),
            ("MA" to 11) to mapOf(dem to 1995196, gop to 1090893, oth to 238957),
            ("MI" to 16) to mapOf(dem to 2268839, gop to 2279543, oth to 250902),
            ("MN" to 10) to mapOf(dem to 1367716, gop to 1322951, oth to 254146),
            ("MS" to 6) to mapOf(dem to 485131, gop to 700714, oth to 23512),
            ("MO" to 10) to mapOf(dem to 1071068, gop to 1594511, oth to 143026),
            ("MT" to 3) to mapOf(dem to 177709, gop to 279240, oth to 40198),
            ("NE" to 2) to mapOf(dem to 284494, gop to 495961, oth to 63772),
            ("NE-01" to 1) to mapOf(dem to 100132, gop to 158642, oth to 23588),
            ("NE-02" to 1) to mapOf(dem to 131030, gop to 137564, oth to 23086),
            ("NE-03" to 1) to mapOf(dem to 53332, gop to 199755, oth to 17173),
            ("NV" to 6) to mapOf(dem to 539260, gop to 512058, oth to 74067),
            ("NH" to 4) to mapOf(dem to 348526, gop to 345790, oth to 49980),
            ("NJ" to 14) to mapOf(dem to 2148278, gop to 1601933, oth to 123835),
            ("NM" to 5) to mapOf(dem to 385234, gop to 319667, oth to 93418),
            ("NY" to 29) to mapOf(dem to 4556124, gop to 2819534, oth to 345795),
            ("NC" to 15) to mapOf(dem to 2189316, gop to 2362631, oth to 189617),
            ("ND" to 3) to mapOf(dem to 93758, gop to 216794, oth to 33808),
            ("OH" to 18) to mapOf(dem to 2394164, gop to 2841005, oth to 261318),
            ("OK" to 7) to mapOf(dem to 420375, gop to 949136, oth to 83481),
            ("OR" to 7) to mapOf(dem to 1002106, gop to 782403, oth to 216827),
            ("PA" to 20) to mapOf(dem to 2926441, gop to 2970733, oth to 268304),
            ("RI" to 4) to mapOf(dem to 252525, gop to 180543, oth to 31076),
            ("SC" to 9) to mapOf(dem to 855373, gop to 1155389, oth to 92265),
            ("SD" to 3) to mapOf(dem to 117458, gop to 227721, oth to 24914),
            ("TN" to 11) to mapOf(dem to 870695, gop to 1522925, oth to 114407),
            ("TX" to 38) to mapOf(dem to 3877868, gop to 4685047, oth to 406311),
            ("UT" to 6) to mapOf(dem to 310676, gop to 515231, oth to 305523),
            ("VT" to 3) to mapOf(dem to 178573, gop to 95369, oth to 41125),
            ("VA" to 13) to mapOf(dem to 1981473, gop to 1769443, oth to 233715),
            ("WA" to 12) to mapOf(dem to 1742718, gop to 1221747, oth to 352554),
            ("WV" to 5) to mapOf(dem to 188794, gop to 489371, oth to 36258),
            ("WI" to 10) to mapOf(dem to 1382536, gop to 1405284, oth to 188330),
            ("WY" to 3) to mapOf(dem to 55973, gop to 174419, oth to 25457),
        ).asOneTimePublisher()
        val parties = Publisher(dem to gop)
        val panel = SwingometerScreen.of(
            prevVotes = prevResult.convertToPartyOrCandidateForSwingometer(),
            results = emptyMap<Pair<String, Int>, PartyResult?>().asOneTimePublisher(),
            swing = emptyMap<Party, Double>().asOneTimePublisher(),
            parties = parties,
            weights = { second },
            seatLabelIncrement = 50.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            title = "US PRESIDENT".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Weights-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            US PRESIDENT
            SWINGOMETER

            NO SWING BETWEEN DEM AND GOP
            GOP WOULD HAVE 306 ON UNIFORM SWING
            
            DEM NEEDS 0.4% SWING FROM GOP TO GAIN MAJORITY
            GOP NEEDS TO AVOID 0.4% SWING TO DEM TO HOLD MAJORITY
            """.trimIndent(),
        )

        parties.submit(gop to dem)
        compareRendering("SwingometerScreen", "Weights-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            US PRESIDENT
            SWINGOMETER

            NO SWING BETWEEN GOP AND DEM
            GOP WOULD HAVE 306 ON UNIFORM SWING
            
            GOP NEEDS TO AVOID 0.4% SWING TO DEM TO HOLD MAJORITY
            DEM NEEDS 0.4% SWING FROM GOP TO GAIN MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testCoalitions() {
        val alp = Party("Labor", "ALP", Color.RED)
        val lib = Party("Liberal", "LIB", Color.BLUE)
        val nat = Party("National", "NAT", Color.GREEN.darker().darker())
        val coa = Coalition("Liberal/National Coalition", "L/NP", Color.BLUE, lib, nat)
        val grn = Party("Greens", "GRN", Color.GREEN)
        val sff = Party("Shooters, Fishers, Farmers", "SFF", Color.RED.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val prev2CPVotes = mapOf(
            "Albury" to mapOf(lib to 28258, alp to 14752),
            "Auburn" to mapOf(alp to 24419, lib to 16876),
            "Ballina" to mapOf(grn to 24645, nat to 19824),
            "Balmain" to mapOf(grn to 24074, alp to 16037),
            "Bankstown" to mapOf(alp to 25735, lib to 14590),
            "Barwon" to mapOf(sff to 19901, nat to 15258),
            "Bathurst" to mapOf(nat to 30130, alp to 14242),
            "Baulkham Hills" to mapOf(lib to 31658, alp to 14434),
            "Bega" to mapOf(lib to 26210, alp to 19830),
            "Blacktown" to mapOf(alp to 28020, lib to 13348),
            "Blue Mountains" to mapOf(alp to 28834, lib to 15620),
            "Cabramatta" to mapOf(alp to 25089, ind to 14818),
            "Camden" to mapOf(lib to 29556, alp to 21796),
            "Campbelltown" to mapOf(alp to 27026, lib to 13305),
            "Canterbury" to mapOf(alp to 28358, lib to 16634),
            "Castle Hill" to mapOf(lib to 37043, alp to 12561),
            "Cessnock" to mapOf(alp to 30229, nat to 13364),
            "Charlestown" to mapOf(alp to 28270, lib to 17069),
            "Clarence" to mapOf(nat to 25985, alp to 14322),
            "Coffs Harbour" to mapOf(nat to 22375, ind to 14730),
            "Coogee" to mapOf(alp to 21510, lib to 20141),
            "Cootamundra" to mapOf(nat to 32504, alp to 9673),
            "Cronulla" to mapOf(lib to 33349, alp to 14556),
            "Davidson" to mapOf(lib to 33440, grn to 11004),
            "Drummonye" to mapOf(lib to 28878, alp to 15552),
            "Dubbo" to mapOf(nat to 19920, ind to 18370),
            "East Hills" to mapOf(lib to 21646, alp to 21217),
            "Epping" to mapOf(lib to 28584, alp to 17238),
            "Fairfield" to mapOf(alp to 26848, lib to 12675),
            "Gosford" to mapOf(alp to 25048, lib to 18691),
            "Goulburn" to mapOf(lib to 22539, alp to 19398),
            "Granville" to mapOf(alp to 23629, lib to 17365),
            "Hawkesbury" to mapOf(lib to 26935, alp to 12982),
            "Heathcote" to mapOf(lib to 26174, alp to 21450),
            "Heffron" to mapOf(alp to 28874, lib to 15462),
            "Holsworthy" to mapOf(lib to 22861, alp to 20042),
            "Hornsby" to mapOf(lib to 28700, alp to 14585),
            "Keira" to mapOf(alp to 33744, lib to 14635),
            "Kiama" to mapOf(lib to 28016, alp to 17167),
            "Kogarah" to mapOf(alp to 21544, lib to 20073),
            "Ku-ring-gai" to mapOf(lib to 31027, alp to 12969),
            "Lake Macquarie" to mapOf(ind to 31164, alp to 12053),
            "Lakemba" to mapOf(alp to 29245, lib to 11136),
            "Lane Cove" to mapOf(lib to 29042, alp to 16092),
            "Lismore" to mapOf(alp to 21856, nat to 20710),
            "Liverpool" to mapOf(alp to 27951, lib to 13945),
            "Londonderry" to mapOf(alp to 27442, lib to 21163),
            "Macquarie Fields" to mapOf(alp to 29944, lib to 16301),
            "Maitland" to mapOf(alp to 27211, lib to 15835),
            "Manly" to mapOf(lib to 26628, grn to 15706),
            "Maroubra" to mapOf(alp to 24026, lib to 17069),
            "Miranda" to mapOf(lib to 28414, alp to 15593),
            "Monaro" to mapOf(nat to 27723, alp to 17276),
            "Mount Druitt" to mapOf(alp to 28505, lib to 14410),
            "Mulgoa" to mapOf(lib to 29910, alp to 18858),
            "Murray" to mapOf(sff to 20765, nat to 18020),
            "Myall Lakes" to mapOf(nat to 25990, alp to 17916),
            "Newcastle" to mapOf(alp to 29843, lib to 14236),
            "Newtown" to mapOf(grn to 24849, alp to 14078),
            "North Shore" to mapOf(lib to 23917, ind to 15209),
            "Northern Tablelands" to mapOf(nat to 37727, alp to 7814),
            "Oatley" to mapOf(lib to 27321, alp to 17802),
            "Orange" to mapOf(sff to 27746, nat to 14821),
            "Oxley" to mapOf(nat to 27111, alp to 14672),
            "Parramatta" to mapOf(lib to 27330, alp to 17733),
            "Penrith" to mapOf(lib to 21204, alp to 20096),
            "Pittwater" to mapOf(lib to 29696, grn to 12225),
            "Port Macquarie" to mapOf(nat to 34725, alp to 14690),
            "Port Stephens" to mapOf(alp to 25766, lib to 20448),
            "Prospect" to mapOf(alp to 26008, lib to 16867),
            "Riverstone" to mapOf(lib to 29337, alp to 22735),
            "Rockdale" to mapOf(alp to 25077, lib to 17037),
            "Ryde" to mapOf(lib to 26032, alp to 18123),
            "Seven Hills" to mapOf(lib to 24518, alp to 18988),
            "Shellharbour" to mapOf(alp to 34435, lib to 16005),
            "South Coast" to mapOf(lib to 27902, alp to 18178),
            "Strathfield" to mapOf(alp to 23519, lib to 19245),
            "Summer Hill" to mapOf(alp to 32023, lib to 12271),
            "Swansea" to mapOf(alp to 26792, lib to 17449),
            "Sydney" to mapOf(ind to 22841, lib to 14134),
            "Tamworth" to mapOf(nat to 30522, ind to 12403),
            "Terrigal" to mapOf(lib to 27802, alp to 16794),
            "The Entrance" to mapOf(alp to 23661, lib to 19189),
            "Tweed" to mapOf(nat to 23243, alp to 19040),
            "Upper Hunter" to mapOf(nat to 19341, alp to 17456),
            "Vaucluse" to mapOf(lib to 28260, grn to 12506),
            "Wagga Wagga" to mapOf(ind to 26869, nat to 14169),
            "Wakehurst" to mapOf(lib to 30182, alp to 12326),
            "Wallsend" to mapOf(alp to 35605, lib to 11591),
            "Willoughby" to mapOf(lib to 29142, alp to 11885),
            "Wollondilly" to mapOf(lib to 22925, alp to 12988),
            "Wollongong" to mapOf(alp to 31357, lib to 12580),
            "Wyong" to mapOf(alp to 27296, lib to 16415),
        )

        val panel = SwingometerScreen.of(
            prevVotes = prev2CPVotes.asOneTimePublisher().convertToPartyOrCandidateForSwingometer(),
            results = prev2CPVotes.mapValues { it.value.entries.maxBy { e -> e.value }.key.let { w -> elected(w) } }.asOneTimePublisher(),
            swing = emptyMap<PartyOrCoalition, Double>().asOneTimePublisher(),
            parties = (alp to coa).asOneTimePublisher(),
            seatLabelIncrement = 5.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            title = "NEW SOUTH WALES".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-Coalition-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW SOUTH WALES
            SWINGOMETER
            
            NO SWING BETWEEN ALP AND L/NP
            L/NP WOULD HAVE 48 ON UNIFORM SWING
            
            ALP NEEDS 7.6% SWING FROM L/NP TO GAIN MAJORITY
            L/NP NEEDS TO AVOID 1.3% SWING TO ALP TO HOLD MAJORITY
            """.trimIndent(),
        )
    }

    @Test
    fun testMultipleIndependents() {
        val lib = PartyOrCandidate(Party("Liberal", "LIB", Color.RED))
        val pc = PartyOrCandidate(Party("Progressive Conservative", "PC", Color.BLUE))
        val grn = PartyOrCandidate("Green")
        val pa = PartyOrCandidate("People's Alliance")
        val ndp = PartyOrCandidate(Party("New Democratic Party", "NDP", Color.ORANGE))
        val kiss = PartyOrCandidate("KISS")
        val ind = PartyOrCandidate("Independent")

        val prevVotes = mapOf(
            "Albert" to mapOf(pc to 5040, lib to 921, grn to 1056, pa to 977, ind to 90),
            "Bathurst East-Nepisiguit-Saint-Isidore" to mapOf(pc to 1568, lib to 4163, grn to 798),
            "Bathurst West-Beresford" to mapOf(pc to 1985, lib to 3730, grn to 965),
            "Campbellton-Dalhousie" to mapOf(pc to 1369, lib to 4540, grn to 1054),
            "Caraquet" to mapOf(pc to 985, lib to 5928, grn to 1290),
            "Carleton" to mapOf(pc to 3536, lib to 1239, grn to 581, pa to 1909, ndp to 80, kiss to 41),
            "Carleton-Victoria" to mapOf(pc to 3330, lib to 2939, grn to 372, pa to 610, ndp to 113),
            "Carleton-York" to mapOf(pc to 4750, lib to 940, grn to 890, pa to 1524, ndp to 110),
            "Dieppe" to mapOf(pc to 1680, lib to 4564, grn to 1142, ndp to 200),
            "Edmundston-Madawaska Centre" to mapOf(pc to 1380, lib to 5236, grn to 415),
            "Fredericton North" to mapOf(pc to 3227, lib to 1464, grn to 2464, pa to 591, ndp to 100),
            "Fredericton South" to mapOf(pc to 2342, lib to 895, grn to 4213, pa to 234, ndp to 117),
            "Fredericton West-Hanwell" to mapOf(pc to 4726, lib to 1510, grn to 1745, pa to 825, ndp to 131),
            "Fredericton-Grand Lake" to mapOf(pc to 2479, lib to 749, grn to 1005, pa to 3759, ndp to 87, kiss to 18),
            "Fredericton-York" to mapOf(pc to 3730, lib to 872, grn to 2110, pa to 1991, ndp to 68, kiss to 24),
            "Fundy-The Isles-Saint John West" to mapOf(pc to 4740, lib to 726, grn to 686, pa to 688, ndp to 291),
            "Gagetown-Petitcodiac" to mapOf(pc to 4773, lib to 867, grn to 1003, pa to 1303, ndp to 131),
            "Hampton" to mapOf(pc to 4351, lib to 1084, grn to 816, pa to 687, ndp to 251),
            "Kent North" to mapOf(pc to 1363, lib to 2933, grn to 4021, ind to 154),
            "Kent South" to mapOf(pc to 2817, lib to 5148, grn to 996, pa to 243, ndp to 118),
            "Kings Centre" to mapOf(pc to 4583, lib to 911, grn to 1006, pa to 693, ndp to 254),
            "Madawaska Les Lacs-Edmundston" to mapOf(pc to 1763, lib to 4583, grn to 542),
            "Memramcook-Tantramar" to mapOf(pc to 1678, lib to 2902, grn to 3425, pa to 192, ind to 34),
            "Miramichi" to mapOf(pc to 1508, lib to 2239, grn to 398, pa to 3527, ndp to 92, ind to 54),
            "Miramichi Bay-Neguac" to mapOf(pc to 2751, lib to 3561, grn to 825, pa to 898, ndp to 139),
            "Moncton Centre" to mapOf(pc to 1642, lib to 2448, grn to 1725, pa to 308, ndp to 168),
            "Moncton East" to mapOf(pc to 3525, lib to 2759, grn to 989, pa to 378, ndp to 153),
            "Moncton Northwest" to mapOf(pc to 4111, lib to 2448, grn to 702, pa to 493, ndp to 229),
            "Moncton South" to mapOf(pc to 2734, lib to 1966, grn to 1245, pa to 331, ndp to 220),
            "Moncton Southwest" to mapOf(pc to 3679, lib to 1561, grn to 927, pa to 667, ndp to 224),
            "New Maryland-Sunbury" to mapOf(pc to 5342, lib to 1048, grn to 1463, pa to 1254, ndp to 141),
            "Oromocto-Lincoln-Fredericton" to mapOf(pc to 3374, lib to 2072, grn to 1306, pa to 745, ndp to 127),
            "Portland-Simonds" to mapOf(pc to 3170, lib to 1654, grn to 483, pa to 282, ndp to 164),
            "Quispamsis" to mapOf(pc to 5697, lib to 1225, grn to 528, pa to 414, ndp to 501),
            "Restigouche West" to mapOf(pc to 1247, lib to 5022, grn to 1755, kiss to 56),
            "Restigouche-Chaleur" to mapOf(pc to 1149, lib to 3823, grn to 1896),
            "Riverview" to mapOf(pc to 4695, lib to 1281, grn to 800, pa to 778, ndp to 261),
            "Rothesay" to mapOf(pc to 4265, lib to 1463, grn to 719, pa to 413, ind to 100),
            "Saint Croix" to mapOf(pc to 3570, lib to 401, grn to 1238, pa to 2546, ndp to 147),
            "Saint John East" to mapOf(pc to 3507, lib to 1639, grn to 394, pa to 434, ndp to 248),
            "Saint John Harbour" to mapOf(pc to 2181, lib to 1207, grn to 1224, pa to 186, ndp to 309),
            "Saint John Lancaster" to mapOf(pc to 3560, lib to 1471, grn to 938, pa to 394, ndp to 201),
            "Shediac Bay-Dieppe" to mapOf(pc to 2971, lib to 5839, pa to 371, ndp to 528),
            "Shediac-Beaubassin-Cap-Pelé" to mapOf(pc to 1820, lib to 4949, grn to 2453),
            "Shippagan-Lamèque-Miscou" to mapOf(pc to 714, lib to 6834, grn to 609),
            "Southwest Miramichi-Bay du Vin" to mapOf(pc to 3887, lib to 1760, pa to 2268, ndp to 188),
            "Sussex-Fundy-St. Martins" to mapOf(pc to 4366, lib to 971, grn to 969, pc to 1321, ndp to 129),
            "Tracadie-Sheila" to mapOf(pc to 2059, lib to 6175, grn to 645),
            "Victoria-La Vallée" to mapOf(pc to 2071, lib to 4365, grn to 426, pa to 292, ind to 92),
        )

        val panel = SwingometerScreen.of(
            prevVotes = prevVotes.asOneTimePublisher(),
            results = prevVotes.mapValues { it.value.entries.maxBy { e -> e.value }.key.let { w -> elected(w.party) } }.asOneTimePublisher(),
            swing = emptyMap<PartyOrCoalition, Double>().asOneTimePublisher(),
            parties = (lib.party to pc.party).asOneTimePublisher(),
            seatLabelIncrement = 3.asOneTimePublisher(),
            range = 0.2.asOneTimePublisher(),
            header = "SWINGOMETER".asOneTimePublisher(),
            title = "NEW BRUNSWICK".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("SwingometerScreen", "Basic-MultipleIndependents-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            NEW BRUNSWICK
            SWINGOMETER
            
            NO SWING BETWEEN LIB AND PC
            PC WOULD HAVE 27 ON UNIFORM SWING
            
            LIB NEEDS 11.2% SWING FROM PC TO GAIN MAJORITY
            PC NEEDS TO AVOID 5.2% SWING TO LIB TO HOLD MAJORITY
            """.trimIndent(),
        )
    }

    companion object {
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val pc = Party("Progressive Conservative", "PCP", Color.BLUE)
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pa = Party("People's Alliance", "PA", Color.MAGENTA.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
        private fun nbPrevResult(): Map<String, Map<Party, Int>> = mapOf(
            "Restigouche West" to mapOf(lib to 4233, pc to 961, grn to 2540, ndp to 263, ind to 62),
            "Campbellton-Dalhousie" to mapOf(lib to 3720, pc to 1761, grn to 637, ndp to 721, pa to 558),
            "Restigouche-Chaleur" to mapOf(lib to 4430, pc to 826, grn to 831, ndp to 621),
            "Bathurst West-Beresford" to mapOf(lib to 4351, pc to 1082, grn to 503, ndp to 443, ind to 64),
            "Bathurst East-Nepisguit-Saint-Isidore" to mapOf(lib to 3550, pc to 858, grn to 421, ndp to 2026),
            "Caraquet" to mapOf(lib to 5420, pc to 1827, grn to 330, ndp to 548, ind to 373),
            "Shippagan-Lameque-Miscou" to mapOf(lib to 3949, pc to 4048, ndp to 578, ind to 178),
            "Tracadie-Sheila" to mapOf(lib to 4320, pc to 2390, grn to 390, ndp to 1213, ind to 544),
            "Miramichi Bay-Neguac" to mapOf(lib to 3512, pc to 1741, grn to 349, ndp to 718, pa to 2047),
            "Miramichi" to mapOf(lib to 2825, pc to 1154, grn to 189, ndp to 110, pa to 3788),
            "Southwest Miramichi-Bay du Vin" to mapOf(lib to 1909, pc to 2960, grn to 447, ndp to 97, pa to 2925, ind to 19),
            "Kent North" to mapOf(lib to 3301, pc to 1112, grn to 4056, ndp to 171, ind to 194),
            "Kent South" to mapOf(lib to 5595, pc to 1848, grn to 1304, ndp to 436),
            "Shediac Bay-Dieppe" to mapOf(lib to 6162, pc to 1353, grn to 906, ndp to 764),
            "Shediac-Beaubassin-Cap-Pele" to mapOf(lib to 5919, pc to 2081, grn to 888, ndp to 428),
            "Memramcook-Tantramar" to mapOf(lib to 3137, pc to 1518, grn to 3148, ndp to 410),
            "Dieppe" to mapOf(lib to 5173, pc to 998, ndp to 1057),
            "Moncton East" to mapOf(lib to 3626, pc to 2771, grn to 925, ndp to 424),
            "Moncton Centre" to mapOf(lib to 2698, pc to 982, grn to 771, ndp to 229, pa to 309, ind to 1200),
            "Moncton South" to mapOf(lib to 3099, pc to 2090, grn to 628, ndp to 249, pa to 466),
            "Moncton Northwest" to mapOf(lib to 2963, pc to 3186, grn to 437, ndp to 297, pa to 875),
            "Moncton Southwest" to mapOf(lib to 2667, pc to 2920, grn to 907, ndp to 503),
            "Riverview" to mapOf(lib to 2053, pc to 3701, grn to 542, ndp to 249, pa to 1005),
            "Albert" to mapOf(lib to 1775, pc to 3479, grn to 870, ndp to 375, pa to 1546, ind to 87),
            "Gagetown-Petitcodiac" to mapOf(lib to 1153, pc to 3674, grn to 1097, ndp to 165, pa to 1892, ind to 56),
            "Sussex-Fundy-St. Martins" to mapOf(lib to 1212, pc to 3816, grn to 505, ndp to 254, pa to 1874, ind to 54),
            "Hampton" to mapOf(lib to 1454, pc to 3702, grn to 743, ndp to 384, pa to 1246),
            "Quispamsis" to mapOf(lib to 2078, pc to 4691, grn to 445, ndp to 239, pa to 795),
            "Rothesay" to mapOf(lib to 2001, pc to 3542, grn to 571, ndp to 251, pa to 722),
            "Saint John East" to mapOf(lib to 1775, pc to 3017, grn to 373, ndp to 402, pa to 1047),
            "Portland-Simonds" to mapOf(lib to 1703, pc to 3168, grn to 435, ndp to 449, ind to 191),
            "Saint John Harbour" to mapOf(lib to 1865, pc to 1855, grn to 721, ndp to 836, pa to 393),
            "Saint John Lancaster" to mapOf(lib to 1727, pc to 3001, grn to 582, ndp to 414, pa to 922),
            "Kings Centre" to mapOf(lib to 1785, pc to 3267, grn to 731, ndp to 342, pa to 1454),
            "Fundy-The Isles-Saint John West" to mapOf(lib to 2422, pc to 3808, grn to 469, ndp to 203, pa to 1104),
            "Saint Croix" to mapOf(lib to 2436, pc to 3249, grn to 1047, ndp to 89, pa to 1466),
            "Oromocto-Lincoln-Fredericton" to mapOf(lib to 2306, pc to 2399, grn to 903, ndp to 159, pa to 1741),
            "Fredericton-Grand Lake" to mapOf(lib to 955, pc to 2433, grn to 472, ndp to 114, pa to 4799, ind to 19),
            "New Maryland-Sunbury" to mapOf(lib to 2210, pc to 3844, grn to 902, ndp to 143, pa to 2214, ind to 14),
            "Fredericton South" to mapOf(lib to 1525, pc to 1042, grn to 4273, ndp to 132, pa to 616),
            "Fredericton North" to mapOf(lib to 2443, pc to 2182, grn to 1313, ndp to 139, pa to 1651),
            "Fredericton-York" to mapOf(lib to 1652, pc to 2777, grn to 1393, ndp to 103, pa to 3033, ind to 34),
            "Fredericton West-Hanwell" to mapOf(lib to 2404, pc to 2739, grn to 1490, ndp to 171, pa to 1803),
            "Carleton-York" to mapOf(lib to 1556, pc to 3118, grn to 837, ndp to 255, pa to 2583, ind to 40),
            "Carleton" to mapOf(lib to 1197, pc to 2982, grn to 1247, ndp to 82, pa to 2026),
            "Carleton-Victoria" to mapOf(lib to 3116, pc to 2872, grn to 503, ndp to 114, pa to 960, ind to 58),
            "Victoria-La Vallee" to mapOf(lib to 3570, pc to 3212, grn to 468, ndp to 307),
            "Edmunston-Madawaska Centre" to mapOf(lib to 4668, pc to 1437, grn to 702, ndp to 206),
            "Madawaska Les Lacs-Edmunston" to mapOf(lib to 4191, pc to 1826, grn to 945, ndp to 156),
        )

        private fun nbCurrResult(): Map<String, PartyResult?> = mapOf(
            "Restigouche West" to elected(lib),
            "Campbellton-Dalhousie" to elected(lib),
            "Restigouche-Chaleur" to elected(lib),
            "Bathurst West-Beresford" to elected(lib),
            "Bathurst East-Nepisguit-Saint-Isidore" to elected(lib),
            "Caraquet" to elected(lib),
            "Shippagan-Lameque-Miscou" to elected(lib),
            "Tracadie-Sheila" to elected(lib),
            "Miramichi Bay-Neguac" to elected(lib),
            "Miramichi" to elected(pa),
            "Southwest Miramichi-Bay du Vin" to elected(pc),
            "Kent North" to elected(grn),
            "Kent South" to elected(lib),
            "Shediac Bay-Dieppe" to elected(lib),
            "Shediac-Beaubassin-Cap-Pele" to elected(lib),
            "Memramcook-Tantramar" to elected(grn),
            "Dieppe" to elected(lib),
            "Moncton East" to elected(pc),
            "Moncton Centre" to elected(lib),
            "Moncton South" to elected(pc),
            "Moncton Northwest" to elected(pc),
            "Moncton Southwest" to elected(pc),
            "Riverview" to elected(pc),
            "Albert" to elected(pc),
            "Gagetown-Petitcodiac" to elected(pc),
            "Sussex-Fundy-St. Martins" to elected(pc),
            "Hampton" to elected(pc),
            "Quispamsis" to elected(pc),
            "Rothesay" to elected(pc),
            "Saint John East" to elected(pc),
            "Portland-Simonds" to elected(pc),
            "Saint John Harbour" to elected(pc),
            "Saint John Lancaster" to elected(pc),
            "Kings Centre" to elected(pc),
            "Fundy-The Isles-Saint John West" to elected(pc),
            "Saint Croix" to elected(pc),
            "Oromocto-Lincoln-Fredericton" to elected(pc),
            "Fredericton-Grand Lake" to elected(pa),
            "New Maryland-Sunbury" to elected(pc),
            "Fredericton South" to elected(grn),
            "Fredericton North" to elected(pc),
            "Fredericton-York" to elected(pc),
            "Fredericton West-Hanwell" to elected(pc),
            "Carleton-York" to elected(pc),
            "Carleton" to elected(pc),
            "Carleton-Victoria" to elected(pc),
            "Victoria-La Vallee" to elected(lib),
            "Edmunston-Madawaska Centre" to elected(lib),
            "Madawaska Les Lacs-Edmunston" to elected(lib),
        )
    }
}
