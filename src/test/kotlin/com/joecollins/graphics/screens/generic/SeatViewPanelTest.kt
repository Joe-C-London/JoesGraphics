package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateDualSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateRangeSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.candidateSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyDualSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyDualSeatsReversed
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partyRangeSeats
import com.joecollins.graphics.screens.generic.BasicResultPanel.Companion.partySeats
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader.readShapes
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Shape
import java.util.IdentityHashMap

class SeatViewPanelTest {
    @Test
    fun testBasicCurrPrev() {
        val currentSeats = Publisher(emptyMap<Party, Int>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(650)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED KINGDOM")
        val seatHeader = Publisher("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val changeSubhead = Publisher<String?>("CON NEED +9 FOR MAJORITY")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withPrev(previousSeats, changeHeader, changeSubhead)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Basic-1", panel)

        currentSeats.submit(mapOf(con to 1))
        previousSeats.submit(mapOf(lab to 1))
        seatHeader.submit("1 OF 650 SEATS DECLARED")
        compareRendering("SeatViewPanel", "Basic-2", panel)

        currentSeats.submit(mapOf(con to 1, lab to 2))
        previousSeats.submit(mapOf(lab to 3))
        seatHeader.submit("3 OF 650 SEATS DECLARED")
        compareRendering("SeatViewPanel", "Basic-3", panel)

        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val oth = Party.OTHERS
        currentSeats.submit(
            mapOf(
                con to 365,
                lab to 202,
                ld to 11,
                snp to 48,
                grn to 1,
                pc to 4,
                oth to 19,
            )
        )
        previousSeats.submit(
            mapOf(
                lab to 262,
                con to 317,
                ld to 12,
                snp to 35,
                grn to 1,
                pc to 4,
                oth to 19,
            )
        )
        seatHeader.submit("650 OF 650 SEATS DECLARED")
        seatSubhead.submit("PROJECTION: CON MAJORITY")
        compareRendering("SeatViewPanel", "Basic-4", panel)
        header.submit("SCOTLAND")
        seatHeader.submit("59 OF 59 SEATS DECLARED")
        seatSubhead.submit("")
        changeSubhead.submit(null)
        totalSeats.submit(59)
        showMajority.submit(false)
        currentSeats.submit(mapOf(snp to 48, con to 6, ld to 4, lab to 1))
        previousSeats.submit(mapOf(snp to 35, con to 13, lab to 7, ld to 4))
        compareRendering("SeatViewPanel", "Basic-5", panel)
    }

    @Test
    fun testBasicCurrDiff() {
        val currentSeats = Publisher(emptyMap<Party, Int>())
        val seatDiff = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(650)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED KINGDOM")
        val seatHeader = Publisher("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val changeSubhead = Publisher<String?>("CON NEED +9 FOR MAJORITY")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withDiff(seatDiff, changeHeader, changeSubhead)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Basic-1", panel)

        currentSeats.submit(mapOf(con to 1))
        seatDiff.submit(mapOf(con to +1, lab to -1))
        seatHeader.submit("1 OF 650 SEATS DECLARED")
        compareRendering("SeatViewPanel", "Basic-2", panel)

        currentSeats.submit(mapOf(con to 1, lab to 2))
        seatHeader.submit("3 OF 650 SEATS DECLARED")
        compareRendering("SeatViewPanel", "Basic-3", panel)

        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val oth = Party.OTHERS
        currentSeats.submit(
            mapOf(
                con to 365,
                lab to 202,
                ld to 11,
                snp to 48,
                grn to 1,
                pc to 4,
                oth to 19,
            )
        )
        seatDiff.submit(
            mapOf(
                con to +48,
                lab to -60,
                ld to -1,
                snp to +13,
                grn to 0,
                pc to 0,
                oth to 0,
            )
        )
        seatHeader.submit("650 OF 650 SEATS DECLARED")
        seatSubhead.submit("PROJECTION: CON MAJORITY")
        compareRendering("SeatViewPanel", "Basic-4", panel)
        header.submit("SCOTLAND")
        seatHeader.submit("59 OF 59 SEATS DECLARED")
        seatSubhead.submit("")
        changeSubhead.submit(null)
        totalSeats.submit(59)
        showMajority.submit(false)
        currentSeats.submit(mapOf(snp to 48, con to 6, ld to 4, lab to 1))
        seatDiff.submit(mapOf(snp to +13, con to -7, ld to 0, lab to -6))
        compareRendering("SeatViewPanel", "Basic-5", panel)
    }

    @Test
    fun testSwing() {
        val currentSeats = Publisher(emptyMap<Party, Int>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val currentVotes = Publisher(emptyMap<Party, Int>())
        val previousVotes = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(650)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED KINGDOM")
        val seatHeader = Publisher("0 OF 650 CONSTITUENCIES DECLARED")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val swingHeader = Publisher("SWING SINCE 2017")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val oth = Party.OTHERS
        val partyOrder = listOf(snp, lab, pc, grn, ld, oth, con)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withSwing(currentVotes, previousVotes, compareBy { partyOrder.indexOf(it) }, swingHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Swing-1", panel)

        currentSeats.submit(mapOf(lab to 1))
        previousSeats.submit(mapOf(lab to 1))
        currentVotes.submit(
            mapOf(
                lab to 21568,
                con to 9290,
                ld to 2709,
                grn to 1365,
                oth to 2542,
            )
        )
        previousVotes.submit(
            mapOf(
                lab to 24071,
                con to 9134,
                ld to 1812,
                grn to 595,
                oth to 1482,
            )
        )
        seatHeader.submit("1 OF 650 SEATS DECLARED")
        compareRendering("SeatViewPanel", "Swing-2", panel)
    }

    @Test
    fun testMap() {
        val currentSeats = Publisher(emptyMap<Party, Int>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val currentVotes = Publisher(emptyMap<Party, Int>())
        val previousVotes = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(27)
        val showMajority = Publisher(true)
        val header = Publisher("PRINCE EDWARD ISLAND")
        val seatHeader = Publisher("0 OF 27 DISTRICTS DECLARED")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("PEI")
        val winnersByDistrict = Publisher<Map<Int, Party?>>(HashMap())
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher<List<Int>?>(null)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val oth = Party.OTHERS
        val partyOrder = listOf(ndp, grn, lib, oth, pc)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withSwing(currentVotes, previousVotes, compareBy { partyOrder.indexOf(it) }, swingHeader)
            .withPartyMap(shapesByDistrict.asOneTimePublisher(), winnersByDistrict, focus, mapHeader)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Map-1", panel)

        val winners = mutableMapOf<Int, Party?>()
        currentSeats.submit(mapOf(pc to 1))
        previousSeats.submit(mapOf(pc to 1))
        currentVotes.submit(mapOf(pc to 1347, lib to 861, grn to 804))
        previousVotes.submit(mapOf(pc to 1179, lib to 951, ndp to 528))
        winners[1] = pc
        winnersByDistrict.submit(winners)
        seatHeader.submit("1 OF 27 DISTRICTS DECLARED")
        compareRendering("SeatViewPanel", "Map-2", panel)

        focus.submit(shapesByDistrict.keys.filter { it <= 7 })
        header.submit("CARDIGAN")
        seatHeader.submit("1 OF 7 DISTRICTS DECLARED")
        seatSubhead.submit("")
        totalSeats.submit(7)
        showMajority.submit(false)
        mapHeader.submit("CARDIGAN")
        compareRendering("SeatViewPanel", "Map-3", panel)

        winners[8] = pc
        winnersByDistrict.submit(winners)
        compareRendering("SeatViewPanel", "Map-3", panel)

        currentSeats.submit(mapOf(pc to 2))
        previousSeats.submit(mapOf(pc to 1, lib to 1))
        currentVotes.submit(mapOf(pc to 2720, lib to 1646, grn to 1478, ndp to 124))
        previousVotes.submit(mapOf(pc to 1964, lib to 2011, ndp to 1113, grn to 106))
        winners[3] = pc
        winnersByDistrict.submit(winners)
        seatHeader.submit("2 OF 7 DISTRICTS DECLARED")
        compareRendering("SeatViewPanel", "Map-4", panel)

        focus.submit(null)
        header.submit("PRINCE EDWARD ISLAND")
        seatHeader.submit("3 OF 27 DISTRICTS DECLARED")
        seatSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        currentSeats.submit(mapOf(pc to 3))
        previousSeats.submit(mapOf(pc to 1, lib to 2))
        totalSeats.submit(27)
        showMajority.submit(true)
        mapHeader.submit("PEI")
        compareRendering("SeatViewPanel", "Map-5", panel)

        (1..27).forEach { winners.putIfAbsent(it, null) }
        winnersByDistrict.submit(winners)
        compareRendering("SeatViewPanel", "Map-5", panel)
    }

    @Test
    fun testDualCurrPrev() {
        val currentSeats = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val previousSeats = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(338)
        val showMajority = Publisher(true)
        val header = Publisher("CANADA")
        val seatHeader = Publisher("0 OF 338 RIDINGS REPORTING")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val panel = partyDualSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Dual-1", panel)

        currentSeats.submit(
            mapOf(
                lib to (0 to 6),
                ndp to (0 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (0 to 7),
            )
        )
        seatHeader.submit("7 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-2", panel)

        currentSeats.submit(
            mapOf(
                lib to (6 to 26),
                ndp to (1 to 1),
                con to (0 to 4),
                grn to (0 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (7 to 32),
            )
        )
        seatHeader.submit("32 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-3", panel)

        currentSeats.submit(
            mapOf(
                lib to (26 to 145),
                ndp to (1 to 13),
                con to (4 to 104),
                bq to (0 to 32),
                grn to (1 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (32 to 166),
                ndp to (0 to 30),
                con to (0 to 89),
                bq to (0 to 10),
            )
        )
        seatHeader.submit("295 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-4", panel)

        currentSeats.submit(
            mapOf(
                lib to (145 to 157),
                ndp to (13 to 24),
                con to (104 to 121),
                bq to (32 to 32),
                grn to (1 to 3),
                ind to (0 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (166 to 184),
                ndp to (30 to 44),
                con to (89 to 99),
                bq to (10 to 10),
                grn to (0 to 1),
            )
        )
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-5", panel)

        currentSeats.submit(
            mapOf(
                lib to (157 to 157),
                ndp to (24 to 24),
                con to (121 to 121),
                bq to (32 to 32),
                grn to (3 to 3),
                ind to (1 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (184 to 184),
                ndp to (44 to 44),
                con to (99 to 99),
                bq to (10 to 10),
                grn to (1 to 1),
            )
        )
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-6", panel)
    }

    @Test
    fun testDualReversedCurrPrev() {
        val currentSeats = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val previousSeats = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(338)
        val showMajority = Publisher(true)
        val header = Publisher("CANADA")
        val seatHeader = Publisher("0 OF 338 RIDINGS REPORTING")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val panel = partyDualSeatsReversed(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "DualReversed-1", panel)

        currentSeats.submit(
            mapOf(
                lib to (0 to 6),
                ndp to (0 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (0 to 7),
            )
        )
        seatHeader.submit("7 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-2", panel)

        currentSeats.submit(
            mapOf(
                lib to (6 to 26),
                ndp to (1 to 1),
                con to (0 to 4),
                grn to (0 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (7 to 32),
            )
        )
        seatHeader.submit("32 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-3", panel)

        currentSeats.submit(
            mapOf(
                lib to (26 to 145),
                ndp to (1 to 13),
                con to (4 to 104),
                bq to (0 to 32),
                grn to (1 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (32 to 166),
                ndp to (0 to 30),
                con to (0 to 89),
                bq to (0 to 10),
            )
        )
        seatHeader.submit("295 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-4", panel)

        currentSeats.submit(
            mapOf(
                lib to (145 to 157),
                ndp to (13 to 24),
                con to (104 to 121),
                bq to (32 to 32),
                grn to (1 to 3),
                ind to (0 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (166 to 184),
                ndp to (30 to 44),
                con to (89 to 99),
                bq to (10 to 10),
                grn to (0 to 1),
            )
        )
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-5", panel)

        currentSeats.submit(
            mapOf(
                lib to (157 to 157),
                ndp to (24 to 24),
                con to (121 to 121),
                bq to (32 to 32),
                grn to (3 to 3),
                ind to (1 to 1),
            )
        )
        previousSeats.submit(
            mapOf(
                lib to (184 to 184),
                ndp to (44 to 44),
                con to (99 to 99),
                bq to (10 to 10),
                grn to (1 to 1),
            )
        )
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "DualReversed-6", panel)
    }

    @Test
    fun testDualCurrDiff() {
        val currentSeats = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val seatDiff = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(338)
        val showMajority = Publisher(true)
        val header = Publisher("CANADA")
        val seatHeader = Publisher("0 OF 338 RIDINGS REPORTING")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Color.GRAY)
        val panel = partyDualSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withDiff(seatDiff, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Dual-1", panel)

        currentSeats.submit(
            mapOf(
                lib to (0 to 6),
                ndp to (0 to 1)
            )
        )
        seatDiff.submit(
            mapOf(
                lib to (0 to -1),
                ndp to (0 to +1)
            )
        )
        seatHeader.submit("7 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-2", panel)

        currentSeats.submit(
            mapOf(
                lib to (6 to 26),
                ndp to (1 to 1),
                con to (0 to 4),
                grn to (0 to 1)
            )
        )
        seatDiff.submit(
            mapOf(
                lib to (-1 to -6),
                ndp to (+1 to +1),
                con to (0 to +4),
                grn to (0 to +1)
            )
        )
        seatHeader.submit("32 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-3", panel)

        currentSeats.submit(
            mapOf(
                lib to (26 to 145),
                ndp to (1 to 13),
                con to (4 to 104),
                bq to (0 to 32),
                grn to (1 to 1)
            )
        )
        seatDiff.submit(
            mapOf(
                lib to (-6 to -21),
                ndp to (+1 to -17),
                con to (+4 to +15),
                bq to (0 to +22),
                grn to (+1 to +1)
            )
        )
        seatHeader.submit("295 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-4", panel)

        currentSeats.submit(
            mapOf(
                lib to (145 to 157),
                ndp to (13 to 24),
                con to (104 to 121),
                bq to (32 to 32),
                grn to (1 to 3),
                ind to (0 to 1)
            )
        )
        seatDiff.submit(
            mapOf(
                lib to (-21 to -27),
                ndp to (-17 to -20),
                con to (+15 to +22),
                bq to (+22 to +22),
                grn to (+1 to +2),
                ind to (0 to +1)
            )
        )
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-5", panel)

        currentSeats.submit(
            mapOf(
                lib to (157 to 157),
                ndp to (24 to 24),
                con to (121 to 121),
                bq to (32 to 32),
                grn to (3 to 3),
                ind to (1 to 1)
            )
        )
        seatDiff.submit(
            mapOf(
                lib to (-27 to -27),
                ndp to (-20 to -20),
                con to (+22 to +22),
                bq to (+22 to +22),
                grn to (+2 to +2),
                ind to (+1 to +1)
            )
        )
        seatHeader.submit("338 OF 338 RIDINGS REPORTING")
        compareRendering("SeatViewPanel", "Dual-6", panel)
    }

    @Test
    fun testRangeCurrPrev() {
        val currentSeats = Publisher(emptyMap<Party, IntRange>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(76)
        val showMajority = Publisher(true)
        val header = Publisher("AUSTRALIA")
        val seatHeader = Publisher("SENATE SEATS")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2013")
        val lnp = Party("Liberal/National Coalition", "L/NP", Color.BLUE)
        val alp = Party("Labor Party", "ALP", Color.RED)
        val grn = Party("The Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation Party", "ONP", Color.ORANGE)
        val nxt = Party("Nick Xenophon Team", "NXT", Color.ORANGE)
        val oth = Party.OTHERS
        val panel = partyRangeSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Range-1", panel)

        currentSeats.submit(
            mapOf(
                lnp to 4..5,
                alp to 4..4,
                grn to 0..1,
                onp to 0..1,
                oth to 0..2
            )
        )
        previousSeats.submit(
            mapOf(
                lnp to 6,
                alp to 4,
                grn to 1,
                oth to 1
            )
        )
        compareRendering("SeatViewPanel", "Range-2", panel)

        currentSeats.submit(
            mapOf(
                lnp to 8..10,
                alp to 7..8,
                grn to 0..2,
                onp to 1..2,
                nxt to 0..1,
                oth to 0..4
            )
        )
        previousSeats.submit(
            mapOf(
                lnp to 12,
                alp to 8,
                grn to 2,
                oth to 2
            )
        )
        compareRendering("SeatViewPanel", "Range-3", panel)

        currentSeats.submit(
            mapOf(
                lnp to 27..31,
                alp to 25..27,
                grn to 5..9,
                onp to 1..4,
                nxt to 3..3,
                oth to 1..8
            )
        )
        previousSeats.submit(
            mapOf(
                lnp to 33,
                alp to 25,
                grn to 10,
                nxt to 1,
                oth to 7
            )
        )
        compareRendering("SeatViewPanel", "Range-4", panel)
    }

    @Test
    fun testRangeCurrDiff() {
        val currentSeats = Publisher(emptyMap<Party, IntRange>())
        val seatDiff = Publisher(emptyMap<Party, IntRange>())
        val totalSeats = Publisher(76)
        val showMajority = Publisher(true)
        val header = Publisher("AUSTRALIA")
        val seatHeader = Publisher("SENATE SEATS")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2013")
        val lnp = Party("Liberal/National Coalition", "L/NP", Color.BLUE)
        val alp = Party("Labor Party", "ALP", Color.RED)
        val grn = Party("The Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation Party", "ONP", Color.ORANGE)
        val nxt = Party("Nick Xenophon Team", "NXT", Color.ORANGE)
        val oth = Party.OTHERS
        val panel = partyRangeSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withDiff(seatDiff, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Range-1", panel)

        currentSeats.submit(
            mapOf(
                lnp to 4..5,
                alp to 4..4,
                grn to 0..1,
                onp to 0..1,
                oth to 0..2
            )
        )
        seatDiff.submit(
            mapOf(
                lnp to -2..-1,
                alp to 0..0,
                grn to -1..0,
                onp to 0..+1,
                oth to -1..+1
            )
        )
        compareRendering("SeatViewPanel", "Range-2", panel)

        currentSeats.submit(
            mapOf(
                lnp to 8..10,
                alp to 7..8,
                grn to 0..2,
                onp to 1..2,
                nxt to 0..1,
                oth to 0..4
            )
        )
        seatDiff.submit(
            mapOf(
                lnp to -4..-2,
                alp to -1..0,
                grn to -2..0,
                onp to +1..+2,
                nxt to 0..+1,
                oth to -2..+2
            )
        )
        compareRendering("SeatViewPanel", "Range-3", panel)

        currentSeats.submit(
            mapOf(
                lnp to 27..31,
                alp to 25..27,
                grn to 5..9,
                onp to 1..4,
                nxt to 3..3,
                oth to 1..8
            )
        )
        seatDiff.submit(
            mapOf(
                lnp to -6..-2,
                alp to 0..+2,
                grn to -5..-1,
                onp to +1..+4,
                nxt to +2..+2,
                oth to -6..+1
            )
        )
        compareRendering("SeatViewPanel", "Range-4", panel)
    }

    @Test
    fun testCandidates() {
        val currentSeats = Publisher(emptyMap<Candidate, Int>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(538)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("PRESIDENT")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2012")
        val winner = Publisher<Candidate?>(null)
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val panel = candidateSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withWinner(winner)
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it ELECTORAL VOTES TO WIN" }
            .build(header)
        panel.setSize(1024, 512)
        currentSeats.submit(mapOf(clinton to 232, trump to 306))
        previousSeats.submit(mapOf(clinton.party to 332, trump.party to 206))
        winner.submit(trump)
        compareRendering("SeatViewPanel", "Candidate-1", panel)
        winner.submit(null)
        compareRendering("SeatViewPanel", "Candidate-WinnerRemoved", panel)
    }

    @Test
    fun testCandidatesDual() {
        val currentSeats = Publisher(emptyMap<Candidate, Pair<Int, Int>>())
        val previousSeats = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(538)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("PRESIDENT")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2012")
        val winner = Publisher<Candidate?>(null)
        val panel = candidateDualSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withWinner(winner)
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it ELECTORAL VOTES TO WIN" }
            .build(header)
        panel.setSize(1024, 512)
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        currentSeats.submit(mapOf(clinton to (218 to 232), trump to (276 to 306)))
        previousSeats.submit(mapOf(clinton.party to (302 to 332), trump.party to (192 to 206)))
        winner.submit(trump)
        compareRendering("SeatViewPanel", "Candidate-2", panel)
    }

    @Test
    fun testCandidatesRange() {
        val currentSeats = Publisher(emptyMap<Candidate, IntRange>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(538)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("PRESIDENT")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2012")
        val clinton = Candidate("Hillary Clinton", Party("Democrat", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        currentSeats.submit(mapOf(clinton to 238..368, trump to 170..300))
        previousSeats.submit(mapOf(clinton.party to 332, trump.party to 206))
        val panel = candidateRangeSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it ELECTORAL VOTES TO WIN" }
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "Candidate-3", panel)
    }

    @Test
    fun testPartySeatsTicked() {
        val currentSeats = Publisher(emptyMap<Party, Int>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(435)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("HOUSE OF REPRESENTATIVES")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val winner = Publisher<Party?>(null)
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withWinner(winner)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        val dem = Party("Democrat", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)
        currentSeats.submit(mapOf(dem to 235, gop to 200))
        previousSeats.submit(mapOf(dem to 194, gop to 241))
        winner.submit(dem)
        compareRendering("SeatViewPanel", "PartyTick-1", panel)
    }

    @Test
    fun testPartySeatsTickedDual() {
        val currentSeats = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val previousSeats = Publisher(emptyMap<Party, Pair<Int, Int>>())
        val totalSeats = Publisher(435)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED STATES")
        val seatHeader = Publisher("HOUSE OF REPRESENTATIVES")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2016")
        val winner = Publisher<Party?>(null)
        val panel = partyDualSeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withWinner(winner)
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .build(header)
        panel.setSize(1024, 512)
        val dem = Party("Democrat", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)
        currentSeats.submit(mapOf(dem to (224 to 235), gop to (192 to 200)))
        previousSeats.submit(mapOf(dem to (193 to 194), gop to (223 to 241)))
        winner.submit(dem)
        compareRendering("SeatViewPanel", "PartyTick-2", panel)
    }

    @Test
    fun testAdditionalHighlightMapWithNoResults() {
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val currentSeats = Publisher(mapOf<Party, Int>())
        val previousSeats = Publisher(mapOf<Party, Int>())
        val currentVotes = Publisher(mapOf<Party, Int>())
        val previousVotes = Publisher(mapOf<Party, Int>())
        val header = Publisher("CARDIGAN")
        val seatHeader = Publisher("0 OF 7 RIDINGS REPORTING")
        val seatSubhead = Publisher("")
        val changeHeader = Publisher("CHANGE SINCE 2015")
        val changeSubhead = Publisher(null)
        val swingHeader = Publisher("SWING SINCE 2015")
        val mapHeader = Publisher("CARDIGAN")
        val swingPartyOrder = listOf(ndp, grn, lib, pc)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val additionalHighlight = Publisher<List<Int>>(shapesByDistrict.keys.toList())
        val winnerByDistrict = Publisher(mapOf<Int, PartyResult>())
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader, changeSubhead)
            .withSwing(currentVotes, previousVotes, compareBy { swingPartyOrder.indexOf(it) }, swingHeader)
            .withResultMap(shapesByDistrict.asOneTimePublisher(), winnerByDistrict, focus, additionalHighlight, mapHeader)
            .withTotal(7.asOneTimePublisher())
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "AdditionalHighlightMap-1", panel)
    }

    @Test
    fun testPartyClassification() {
        val dup = Party("Democratic Unionist Party", "DUP", Color.ORANGE.darker())
        val sf = Party("Sinn F\u00e9in", "SF", Color.GREEN.darker().darker())
        val sdlp = Party("Social Democratic and Labour Party", "SDLP", Color.GREEN.darker())
        val uup = Party("Ulster Unionist Party", "UUP", Color.BLUE)
        val apni = Party("Alliance Party", "APNI", Color.YELLOW)
        val grn = Party("Green Party", "GRN", Color.GREEN)
        val tuv = Party("Traditional Unionist Voice", "TUV", Color.BLUE.darker())
        val pbp = Party("People Before Profit", "PBP", Color.MAGENTA)
        val pup = Party("Progressive Unionist Party", "PUP", Color.BLUE.darker())
        val con = Party("NI Conservatives", "CON", Color.BLUE)
        val lab = Party("Labour Alternative", "LAB", Color.RED)
        val ukip = Party("UK Independence Party", "UKIP", Color.MAGENTA.darker())
        val cista = Party("Cannabis is Safer than Alcohol", "CISTA", Color.GRAY)
        val wp = Party("Workers' Party", "WP", Color.RED)
        val indU = Party("Independent", "IND", Color.GRAY)
        val indN = Party("Independent", "IND", Color.GRAY)
        val indO = Party("Independent", "IND", Color.GRAY)
        val unionists = Party("Unionists", "Unionists", Color(0xff8200))
        val nationalists = Party("Nationalists", "Nationalists", Color(0x169b62))
        val others = Party.OTHERS
        val mapping = IdentityHashMap<Party, Party>()
        sequenceOf(dup, uup, tuv, con, pup, ukip, indU).forEach { mapping[it] = unionists }
        sequenceOf(sf, sdlp, wp, indN).forEach { mapping[it] = nationalists }
        sequenceOf(apni, grn, pbp, lab, indO).forEach { mapping[it] = others }
        val currentSeats = Publisher(emptyMap<Party, Int>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val currentVotes = Publisher(emptyMap<Party, Int>())
        val previousVotes = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(90)
        val header = Publisher("NORTHERN IRELAND")
        val seatHeader = Publisher("2017 RESULTS")
        val seatSubhead = Publisher<String?>(null)
        val changeHeader = Publisher("NOTIONAL CHANGE SINCE 2016")
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withClassification({ mapping.getOrDefault(it, others) }, "BY DESIGNATION".asOneTimePublisher())
            .withSwing(currentVotes, previousVotes, compareBy { listOf(nationalists, others, unionists).indexOf(it) }, "FIRST PREFERENCE SWING SINCE 2016".asOneTimePublisher())
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "PartyClassifications-1", panel)

        currentSeats.submit(
            mapOf(
                dup to 28,
                sf to 27,
                sdlp to 12,
                uup to 10,
                apni to 8,
                grn to 2,
                tuv to 1,
                pbp to 1,
                indU to 1,
            )
        )
        previousSeats.submit(
            mapOf(
                dup to 33,
                sf to 23,
                sdlp to 11,
                uup to 11,
                apni to 8,
                grn to 2,
                tuv to 1,
                pbp to 1,
            )
        )
        currentVotes.submit(
            mapOf(
                dup to 225413,
                sf to 224245,
                sdlp to 95958,
                uup to 103314,
                apni to 72717,
                grn to 18527,
                tuv to 20523,
                pbp to 14100,
                pup to 5590,
                con to 2399,
                lab to 2009,
                ukip to 1579,
                cista to 1273,
                wp to 1261,
                indU to 4918,
                indN to 1639,
                indO to 7850,
            )
        )
        previousVotes.submit(
            mapOf(
                dup to 202567,
                sf to 166785,
                uup to 87302,
                sdlp to 83368,
                apni to 48447,
                tuv to 23776,
                grn to 18718,
                pbp to 13761,
                ukip to 10109,
                pup to 5955,
                con to 2554,
                cista to 2510,
                lab to 1939 + 1577,
                wp to 1565,
                indU to 351 + 3270,
                indN to 0,
                indO to 224 + 124 + 32 + 19380,
            )
        )
        compareRendering("SeatViewPanel", "PartyClassifications-2", panel)
    }

    @Test
    fun testShowPrevSeats() {
        val ldp = Party("Liberal Democratic Party", "LDP", Color(60, 163, 36))
        val cdp = Party("Constitutional Democratic Party", "CDP", Color(24, 69, 137))
        val kibo = Party("Kib\u014d no T\u014d", "KIB\u014c", Color(24, 100, 57))
        val nippon = Party("Nippon Ishin no Kai", "NIPPON", Color(184, 206, 67))
        val komeito = Party("Komeito", "KOMEITO", Color(245, 88, 129))
        val jcp = Party("Japanese Communist Party", "JCP", Color(219, 0, 28))
        val dpp = Party("Democratic Party for the People", "DPP", Color(255, 215, 0))
        val reiwa = Party("Reiwa Shinsengumi", "REIWA", Color(237, 0, 140))
        val sdp = Party("Social Democratic Party", "SDP", Color(28, 169, 233))
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val curr = Publisher(
            mapOf(
                ldp to 259,
                cdp to 96,
                nippon to 41,
                komeito to 32,
                jcp to 10,
                dpp to 11,
                reiwa to 3,
                sdp to 1,
                ind to 12
            )
        )
        val prev = Publisher(
            mapOf(
                ldp to 284,
                cdp to 55,
                kibo to 50,
                komeito to 29,
                jcp to 12,
                nippon to 11,
                sdp to 2,
                ind to 22
            )
        )
        val seatsHeader = Publisher("HOUSE OF REPRESENTATIVES")
        val changeHeader = Publisher("2017 RESULT")
        val total = Publisher(465)
        val showMajority = Publisher(true)
        val showPrevRaw = Publisher(true)

        val panel = partySeats(curr, seatsHeader, "".asOneTimePublisher())
            .withPrev(prev, changeHeader, showPrevRaw = showPrevRaw)
            .withTotal(total)
            .withMajorityLine(showMajority) { "$it FOR MAJORITY" }
            .build("JAPAN".asOneTimePublisher())
        panel.size = Dimension(1024, 512)
        compareRendering("SeatViewPanel", "PrevSeats-1", panel)

        curr.submit(
            mapOf(
                ldp to 187,
                cdp to 57,
                nippon to 16,
                komeito to 9,
                jcp to 1,
                dpp to 6,
                sdp to 1
            )
        )
        prev.submit(
            mapOf(
                ldp to 218,
                cdp to 18,
                kibo to 18,
                komeito to 8,
                jcp to 1,
                nippon to 3,
                sdp to 1
            )
        )
        seatsHeader.submit("CONSTITUENCY SEATS")
        total.submit(289)
        showMajority.submit(false)
        compareRendering("SeatViewPanel", "PrevSeats-2", panel)

        showPrevRaw.submit(false)
        changeHeader.submit("CHANGE SINCE 2017")
        compareRendering("SeatViewPanel", "PrevSeats-3", panel)

        curr.submit(
            mapOf(
                ldp to 259,
                cdp to 96,
                nippon to 41,
                komeito to 32,
                jcp to 10,
                dpp to 11,
                reiwa to 3,
                sdp to 1,
                ind to 12
            )
        )
        prev.submit(
            mapOf(
                ldp to 284,
                cdp to 55,
                kibo to 50,
                komeito to 29,
                jcp to 12,
                nippon to 11,
                sdp to 2,
                ind to 22
            )
        )
        seatsHeader.submit("HOUSE OF REPRESENTATIVES")
        showMajority.submit(true)
        total.submit(465)
        compareRendering("SeatViewPanel", "PrevSeats-4", panel)

        showPrevRaw.submit(true)
        changeHeader.submit("2017 RESULT")
        compareRendering("SeatViewPanel", "PrevSeats-1", panel)
    }

    @Test
    fun testShowPrevDualSeats() {
        val ldp = Party("Liberal Democratic Party", "LDP", Color(60, 163, 36))
        val cdp = Party("Constitutional Democratic Party", "CDP", Color(24, 69, 137))
        val kibo = Party("Kib\u014d no T\u014d", "KIB\u014c", Color(24, 100, 57))
        val nippon = Party("Nippon Ishin no Kai", "NIPPON", Color(184, 206, 67))
        val komeito = Party("Komeito", "KOMEITO", Color(245, 88, 129))
        val jcp = Party("Japanese Communist Party", "JCP", Color(219, 0, 28))
        val dpp = Party("Democratic Party for the People", "DPP", Color(255, 215, 0))
        val reiwa = Party("Reiwa Shinsengumi", "REIWA", Color(237, 0, 140))
        val sdp = Party("Social Democratic Party", "SDP", Color(28, 169, 233))
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val curr = Publisher(
            mapOf(
                ldp to (187 to 259),
                cdp to (57 to 96),
                nippon to (16 to 41),
                komeito to (9 to 32),
                jcp to (1 to 10),
                dpp to (6 to 11),
                reiwa to (0 to 3),
                sdp to (1 to 1),
                ind to (12 to 12)
            )
        )
        val prev = Publisher(
            mapOf(
                ldp to (218 to 284),
                cdp to (18 to 55),
                kibo to (18 to 50),
                komeito to (8 to 29),
                jcp to (1 to 12),
                nippon to (3 to 11),
                sdp to (1 to 2),
                ind to (22 to 22)
            )
        )
        val seatsHeader = Publisher("HOUSE OF REPRESENTATIVES")
        val seatsSubhead = Publisher("CONSTITUENCIES / TOTAL SEATS")
        val changeHeader = Publisher("2017 RESULT")
        val total = Publisher(465)
        val showMajority = Publisher(true)
        val showPrevRaw = Publisher(true)

        val panel = partyDualSeats(curr, seatsHeader, seatsSubhead)
            .withPrev(prev, changeHeader, showPrevRaw = showPrevRaw)
            .withTotal(total)
            .withMajorityLine(showMajority) { "$it FOR MAJORITY" }
            .build("JAPAN".asOneTimePublisher())
        panel.size = Dimension(1024, 512)
        compareRendering("SeatViewPanel", "PrevDualSeats-1", panel)

        showPrevRaw.submit(false)
        changeHeader.submit("CHANGE SINCE 2017")
        compareRendering("SeatViewPanel", "PrevDualSeats-2", panel)
    }

    @Test
    fun showPrevRangeSeats() {
        val ldp = Party("Liberal Democratic Party", "LDP", Color(60, 163, 36))
        val cdp = Party("Constitutional Democratic Party", "CDP", Color(24, 69, 137))
        val kibo = Party("Kib\u014d no T\u014d", "KIB\u014c", Color(24, 100, 57))
        val nippon = Party("Nippon Ishin no Kai", "NIPPON", Color(184, 206, 67))
        val komeito = Party("Komeito", "KOMEITO", Color(245, 88, 129))
        val jcp = Party("Japanese Communist Party", "JCP", Color(219, 0, 28))
        val dpp = Party("Democratic Party for the People", "DPP", Color(255, 215, 0))
        val reiwa = Party("Reiwa Shinsengumi", "REIWA", Color(237, 0, 140))
        val sdp = Party("Social Democratic Party", "SDP", Color(28, 169, 233))
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val curr = Publisher(
            mapOf(
                ldp to 251..279,
                cdp to 94..120,
                nippon to 25..36,
                komeito to 25..37,
                jcp to 9..21,
                dpp to 5..12,
                reiwa to 3..3,
                sdp to 2..2,
                ind to 4..9
            )
        )
        val prev = Publisher(
            mapOf(
                ldp to 284,
                cdp to 55,
                kibo to 50,
                komeito to 29,
                jcp to 12,
                nippon to 11,
                sdp to 2,
                ind to 22
            )
        )
        val seatsHeader = Publisher("ASAHI SHIMBUN SEAT PROJECTION")
        val seatsSubhead = Publisher("FIELDWORK: 23-24 OCTOBER 2021")
        val changeHeader = Publisher("2017 RESULT")
        val total = Publisher(465)
        val showMajority = Publisher(true)
        val showPrevRaw = Publisher(true)

        val panel = partyRangeSeats(curr, seatsHeader, seatsSubhead)
            .withPrev(prev, changeHeader, showPrevRaw = showPrevRaw)
            .withTotal(total)
            .withMajorityLine(showMajority) { "$it FOR MAJORITY" }
            .build("JAPAN".asOneTimePublisher())
        panel.size = Dimension(1024, 512)
        compareRendering("SeatViewPanel", "PrevRangeSeats-1", panel)

        showPrevRaw.submit(false)
        changeHeader.submit("CHANGE SINCE 2017")
        compareRendering("SeatViewPanel", "PrevRangeSeats-2", panel)
    }

    @Test
    fun testProgressLabel() {
        val currentSeats = Publisher(emptyMap<Party, Int>())
        val previousSeats = Publisher(emptyMap<Party, Int>())
        val totalSeats = Publisher(650)
        val showMajority = Publisher(true)
        val header = Publisher("UNITED KINGDOM")
        val seatHeader = Publisher("SEATS DECLARED")
        val progressLabel = Publisher("0/650")
        val seatSubhead = Publisher("PROJECTION: TOO EARLY TO CALL")
        val changeHeader = Publisher("CHANGE SINCE 2017")
        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker())
        val grn = Party("Green", "GRN", Color.GREEN)
        val oth = Party.OTHERS
        val panel = partySeats(
            currentSeats, seatHeader, seatSubhead
        )
            .withPrev(previousSeats, changeHeader)
            .withTotal(totalSeats)
            .withMajorityLine(showMajority) { "$it SEATS FOR MAJORITY" }
            .withProgressLabel(progressLabel)
            .build(header)
        panel.setSize(1024, 512)
        compareRendering("SeatViewPanel", "ProgressLabel-1", panel)

        currentSeats.submit(mapOf(lab to 1))
        previousSeats.submit(mapOf(lab to 1))
        progressLabel.submit("1/650")
        compareRendering("SeatViewPanel", "ProgressLabel-2", panel)
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
