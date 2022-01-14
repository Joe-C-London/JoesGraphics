package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.seats
import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.seatsWithDiff
import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.seatsWithPrev
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import java.awt.Color
import java.io.IOException
import kotlin.Throws

class RegionalBreakdownScreenTest {
    @Test
    @Throws(IOException::class)
    fun testSeats() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val screen = seats(
            "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            peiSeats,
            27.asOneTimePublisher(),
            "SEATS BY REGION".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("CARDIGAN".asOneTimePublisher(), cardiganSeats, 7.asOneTimePublisher())
            .withRegion("MALPEQUE".asOneTimePublisher(), malpequeSeats, 7.asOneTimePublisher())
            .withRegion("CHARLOTTETOWN".asOneTimePublisher(), charlottetownSeats, 6.asOneTimePublisher())
            .withRegion("EGMONT".asOneTimePublisher(), egmontSeats, 7.asOneTimePublisher())
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Seats-1", screen)
        peiSeats.submit(mapOf(grn to 1))
        cardiganSeats.submit(mapOf(grn to 1))
        compareRendering("RegionalBreakdownScreen", "Seats-2", screen)
        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        compareRendering("RegionalBreakdownScreen", "Seats-3", screen)
    }

    @Test
    @Throws(IOException::class)
    fun testSeatsWithDiff() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val peiDiff = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganDiff = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeDiff = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownDiff = Publisher<Map<Party, Int>>(emptyMap())
        val egmontDiff = Publisher<Map<Party, Int>>(emptyMap())
        val screen = seatsWithDiff(
            "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            peiSeats,
            peiDiff,
            27.asOneTimePublisher(),
            "SEATS BY REGION".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("CARDIGAN".asOneTimePublisher(), cardiganSeats, cardiganDiff, 7.asOneTimePublisher())
            .withRegion("MALPEQUE".asOneTimePublisher(), malpequeSeats, malpequeDiff, 7.asOneTimePublisher())
            .withRegion("CHARLOTTETOWN".asOneTimePublisher(), charlottetownSeats, charlottetownDiff, 6.asOneTimePublisher())
            .withRegion("EGMONT".asOneTimePublisher(), egmontSeats, egmontDiff, 7.asOneTimePublisher())
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-1", screen)
        peiSeats.submit(mapOf(grn to 1))
        peiDiff.submit(mapOf(grn to +1, lib to -1))
        cardiganSeats.submit(mapOf(grn to 1))
        cardiganDiff.submit(mapOf(grn to +1, lib to -1))
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-2", screen)
        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        peiDiff.submit(mapOf(pc to +5, grn to +7, lib to -12))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        cardiganDiff.submit(mapOf(pc to +1, grn to +1, lib to -2))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        malpequeDiff.submit(mapOf(pc to +2, grn to 0, lib to -1))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        charlottetownDiff.submit(mapOf(grn to +3, lib to -5, pc to +1))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        egmontDiff.submit(mapOf(grn to +3, lib to -4, pc to +1))
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-3", screen)
    }

    @Test
    @Throws(IOException::class)
    fun testSeatsWithPrev() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val peiPrev = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganPrev = Publisher<Map<Party, Int>>(emptyMap())
        val malpequePrev = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownPrev = Publisher<Map<Party, Int>>(emptyMap())
        val egmontPrev = Publisher<Map<Party, Int>>(emptyMap())
        val screen = seatsWithPrev(
            "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            peiSeats,
            peiPrev,
            27.asOneTimePublisher(),
            "SEATS BY REGION".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("CARDIGAN".asOneTimePublisher(), cardiganSeats, cardiganPrev, 7.asOneTimePublisher())
            .withRegion("MALPEQUE".asOneTimePublisher(), malpequeSeats, malpequePrev, 7.asOneTimePublisher())
            .withRegion("CHARLOTTETOWN".asOneTimePublisher(), charlottetownSeats, charlottetownPrev, 6.asOneTimePublisher())
            .withRegion("EGMONT".asOneTimePublisher(), egmontSeats, egmontPrev, 7.asOneTimePublisher())
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-1", screen)
        peiSeats.submit(mapOf(grn to 1))
        peiPrev.submit(mapOf(lib to 1))
        cardiganSeats.submit(mapOf(grn to 1))
        cardiganPrev.submit(mapOf(lib to 1))
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-2", screen)
        peiSeats.submit(mapOf(pc to 13, grn to 8, lib to 6))
        peiPrev.submit(mapOf(pc to 8, grn to 1, lib to 18))
        cardiganSeats.submit(mapOf(pc to 6, grn to 1))
        cardiganPrev.submit(mapOf(pc to 5, lib to 2))
        malpequeSeats.submit(mapOf(pc to 5, grn to 1, lib to 1))
        malpequePrev.submit(mapOf(pc to 3, grn to 1, lib to 2))
        charlottetownSeats.submit(mapOf(grn to 3, lib to 2, pc to 1))
        charlottetownPrev.submit(mapOf(lib to 7))
        egmontSeats.submit(mapOf(grn to 3, lib to 3, pc to 1))
        egmontPrev.submit(mapOf(lib to 7))
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-3", screen)
    }

    @Test
    fun testSeatsCoalition() {
        val coa = Party("Coalition", "L/NP", Color.BLUE.darker())
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        val screen = seats(
            "AUSTRALIA".asOneTimePublisher(),
            mapOf(alp to 68, coa to 77, oth to 6).asOneTimePublisher(),
            151.asOneTimePublisher(),
            "SEATS BY STATE".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("NEW SOUTH WALES".asOneTimePublisher(), mapOf(coa to 22, alp to 24, oth to 1).asOneTimePublisher(), 47.asOneTimePublisher())
            .withRegion("VICTORIA".asOneTimePublisher(), mapOf(coa to 15, alp to 21, oth to 2).asOneTimePublisher(), 38.asOneTimePublisher())
            .withRegion("QUEENSLAND".asOneTimePublisher(), mapOf(lnp to 23, alp to 6, oth to 1).asOneTimePublisher(), 30.asOneTimePublisher(), mapOf(coa to lnp).asOneTimePublisher())
            .withRegion("WESTERN AUSTRALIA".asOneTimePublisher(), mapOf(coa to 11, alp to 5).asOneTimePublisher(), 16.asOneTimePublisher())
            .withRegion("SOUTH AUSTRALIA".asOneTimePublisher(), mapOf(coa to 4, alp to 5, oth to 1).asOneTimePublisher(), 10.asOneTimePublisher())
            .withRegion("TASMANIA".asOneTimePublisher(), mapOf(coa to 2, alp to 2, oth to 1).asOneTimePublisher(), 5.asOneTimePublisher())
            .withRegion("AUSTRALIAN CAPITAL TERRITORY".asOneTimePublisher(), mapOf(alp to 3).asOneTimePublisher(), 3.asOneTimePublisher(), mapOf(coa to lib).asOneTimePublisher())
            .withRegion("NORTHERN TERRITORY".asOneTimePublisher(), mapOf(alp to 2).asOneTimePublisher(), 2.asOneTimePublisher(), mapOf(coa to clp).asOneTimePublisher())
            .build("AUSTRALIA".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Seats-C", screen)
    }

    @Test
    fun testSeatsWithPrevCoalition() {
        val coa = Party("Coalition", "L/NP", Color.BLUE.darker())
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        val screen = seatsWithPrev(
            "AUSTRALIA".asOneTimePublisher(),
            mapOf(alp to 68, coa to 77, oth to 6).asOneTimePublisher(),
            mapOf(alp to 69, coa to 76, oth to 5).asOneTimePublisher(),
            151.asOneTimePublisher(),
            "SEATS BY STATE".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("NEW SOUTH WALES".asOneTimePublisher(), mapOf(coa to 22, alp to 24, oth to 1).asOneTimePublisher(), mapOf(coa to 23, alp to 24).asOneTimePublisher(), 47.asOneTimePublisher())
            .withRegion("VICTORIA".asOneTimePublisher(), mapOf(coa to 15, alp to 21, oth to 2).asOneTimePublisher(), mapOf(coa to 17, alp to 18, oth to 2).asOneTimePublisher(), 38.asOneTimePublisher())
            .withRegion("QUEENSLAND".asOneTimePublisher(), mapOf(lnp to 23, alp to 6, oth to 1).asOneTimePublisher(), mapOf(lnp to 21, alp to 8, oth to 1).asOneTimePublisher(), 30.asOneTimePublisher(), mapOf(coa to lnp).asOneTimePublisher())
            .withRegion("WESTERN AUSTRALIA".asOneTimePublisher(), mapOf(coa to 11, alp to 5).asOneTimePublisher(), mapOf(coa to 11, alp to 5).asOneTimePublisher(), 16.asOneTimePublisher())
            .withRegion("SOUTH AUSTRALIA".asOneTimePublisher(), mapOf(coa to 4, alp to 5, oth to 1).asOneTimePublisher(), mapOf(coa to 4, alp to 6, oth to 1).asOneTimePublisher(), 10.asOneTimePublisher())
            .withRegion("TASMANIA".asOneTimePublisher(), mapOf(coa to 2, alp to 2, oth to 1).asOneTimePublisher(), mapOf(alp to 4, oth to 1).asOneTimePublisher(), 5.asOneTimePublisher())
            .withRegion("AUSTRALIAN CAPITAL TERRITORY".asOneTimePublisher(), mapOf(alp to 3).asOneTimePublisher(), mapOf(alp to 2).asOneTimePublisher(), 3.asOneTimePublisher(), mapOf(coa to lib).asOneTimePublisher())
            .withRegion("NORTHERN TERRITORY".asOneTimePublisher(), mapOf(alp to 2).asOneTimePublisher(), mapOf(alp to 2).asOneTimePublisher(), 2.asOneTimePublisher(), mapOf(coa to clp).asOneTimePublisher())
            .build("AUSTRALIA".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithPrev-C", screen)
    }

    @Test
    fun testSeatsWithDiffCoalition() {
        val coa = Party("Coalition", "L/NP", Color.BLUE.darker())
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        val screen = seatsWithDiff(
            "AUSTRALIA".asOneTimePublisher(),
            mapOf(alp to 68, coa to 77, oth to 6).asOneTimePublisher(),
            mapOf(alp to -1, coa to +1, oth to +1).asOneTimePublisher(),
            151.asOneTimePublisher(),
            "SEATS BY STATE".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("NEW SOUTH WALES".asOneTimePublisher(), mapOf(coa to 22, alp to 24, oth to 1).asOneTimePublisher(), mapOf(coa to -1, oth to +1).asOneTimePublisher(), 47.asOneTimePublisher())
            .withRegion("VICTORIA".asOneTimePublisher(), mapOf(coa to 15, alp to 21, oth to 2).asOneTimePublisher(), mapOf(coa to -2, alp to +3).asOneTimePublisher(), 38.asOneTimePublisher())
            .withRegion("QUEENSLAND".asOneTimePublisher(), mapOf(lnp to 23, alp to 6, oth to 1).asOneTimePublisher(), mapOf(lnp to +2, alp to -2).asOneTimePublisher(), 30.asOneTimePublisher(), mapOf(coa to lnp).asOneTimePublisher())
            .withRegion("WESTERN AUSTRALIA".asOneTimePublisher(), mapOf(coa to 11, alp to 5).asOneTimePublisher(), mapOf<Party, Int>().asOneTimePublisher(), 16.asOneTimePublisher())
            .withRegion("SOUTH AUSTRALIA".asOneTimePublisher(), mapOf(coa to 4, alp to 5, oth to 1).asOneTimePublisher(), mapOf(alp to -1).asOneTimePublisher(), 10.asOneTimePublisher())
            .withRegion("TASMANIA".asOneTimePublisher(), mapOf(coa to 2, alp to 2, oth to 1).asOneTimePublisher(), mapOf(coa to +2, alp to -2).asOneTimePublisher(), 5.asOneTimePublisher())
            .withRegion("AUSTRALIAN CAPITAL TERRITORY".asOneTimePublisher(), mapOf(alp to 3).asOneTimePublisher(), mapOf(alp to +1).asOneTimePublisher(), 3.asOneTimePublisher(), mapOf(coa to lib).asOneTimePublisher())
            .withRegion("NORTHERN TERRITORY".asOneTimePublisher(), mapOf(alp to 2).asOneTimePublisher(), mapOf(alp to 0).asOneTimePublisher(), 2.asOneTimePublisher(), mapOf(coa to clp).asOneTimePublisher())
            .build("AUSTRALIA".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithPrev-C", screen)
    }

    companion object {
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
    }
}
