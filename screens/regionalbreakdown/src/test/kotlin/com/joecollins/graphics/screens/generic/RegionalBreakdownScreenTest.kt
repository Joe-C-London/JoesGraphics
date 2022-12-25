package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Coalition
import com.joecollins.models.general.Party
import com.joecollins.models.general.PollsReporting
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension

class RegionalBreakdownScreenTest {
    @Test
    fun testSeats() {
        val peiSeats = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganSeats = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeSeats = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = Publisher<Map<Party, Int>>(emptyMap())
        val egmontSeats = Publisher<Map<Party, Int>>(emptyMap())
        val screen = RegionalBreakdownScreen.seats(
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
        val screen = RegionalBreakdownScreen.seatsWithDiff(
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
        val screen = RegionalBreakdownScreen.seatsWithPrev(
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
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        val screen = RegionalBreakdownScreen.seats(
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
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        val screen = RegionalBreakdownScreen.seatsWithPrev(
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
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val oth = Party.OTHERS

        val screen = RegionalBreakdownScreen.seatsWithDiff(
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

    @Test
    fun testSeatsWithLimitedColumns() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Quebecois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val federalSeats = Publisher(emptyMap<Party, Int>()) to Publisher(emptyMap<Party, Int>())
        val provincialSeats = listOf(
            "Newfoundland and Labrador" to 7,
            "Nova Scotia" to 11,
            "Prince Edward Island" to 4,
            "New Brunswick" to 10,
            "Quebec" to 78,
            "Ontario" to 121,
            "Manitoba" to 14,
            "Saskatchewan" to 14,
            "Alberta" to 34,
            "British Columbia" to 42,
            "Northern Canada" to 3
        ).associateWith { Publisher(emptyMap<Party, Int>()) to Publisher(emptyMap<Party, Int>()) }
        val builder = RegionalBreakdownScreen.seatsWithPrev(
            "CANADA".asOneTimePublisher(),
            federalSeats.first,
            federalSeats.second,
            338.asOneTimePublisher(),
            "SEATS BY PROVINCE".asOneTimePublisher(),
            maxColumnsPublisher = 4.asOneTimePublisher()
        ).withBlankRow()
        provincialSeats.forEach { (name, seats), (curr, prev) ->
            builder.withRegion(
                name.uppercase().asOneTimePublisher(),
                curr,
                prev,
                seats.asOneTimePublisher()
            )
        }
        val screen = builder.build("CANADA".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-1", screen)

        federalSeats.also {
            it.first.submit(mapOf(lib to 6, ndp to 1))
            it.second.submit(mapOf(lib to 7))
        }
        provincialSeats["Newfoundland and Labrador" to 7]!!.also {
            it.first.submit(mapOf(lib to 6, ndp to 1))
            it.second.submit(mapOf(lib to 7))
        }
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-2", screen)

        federalSeats.also {
            it.first.submit(mapOf(lib to 26, con to 4, ndp to 1, grn to 1))
            it.second.submit(mapOf(lib to 32))
        }
        provincialSeats["Nova Scotia" to 11]!!.also {
            it.first.submit(mapOf(lib to 10, con to 1))
            it.second.submit(mapOf(lib to 11))
        }
        provincialSeats["Prince Edward Island" to 4]!!.also {
            it.first.submit(mapOf(lib to 4))
            it.second.submit(mapOf(lib to 4))
        }
        provincialSeats["New Brunswick" to 10]!!.also {
            it.first.submit(mapOf(lib to 6, con to 3, grn to 1))
            it.second.submit(mapOf(lib to 10))
        }
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-3", screen)

        federalSeats.also {
            it.first.submit(mapOf(lib to 145, con to 104, ndp to 13, bq to 32, grn to 1))
            it.second.submit(mapOf(lib to 166, con to 89, ndp to 30, bq to 10))
        }
        provincialSeats["Quebec" to 78]!!.also {
            it.first.submit(mapOf(lib to 35, con to 10, ndp to 1, bq to 32))
            it.second.submit(mapOf(lib to 40, con to 12, ndp to 16, bq to 10))
        }
        provincialSeats["Ontario" to 121]!!.also {
            it.first.submit(mapOf(lib to 79, con to 36, ndp to 6))
            it.second.submit(mapOf(lib to 80, con to 33, ndp to 8))
        }
        provincialSeats["Manitoba" to 14]!!.also {
            it.first.submit(mapOf(lib to 4, con to 7, ndp to 3))
            it.second.submit(mapOf(lib to 7, con to 5, ndp to 2))
        }
        provincialSeats["Saskatchewan" to 14]!!.also {
            it.first.submit(mapOf(con to 14))
            it.second.submit(mapOf(lib to 1, con to 10, ndp to 3))
        }
        provincialSeats["Alberta" to 34]!!.also {
            it.first.submit(mapOf(con to 33, ndp to 1))
            it.second.submit(mapOf(lib to 4, con to 29, ndp to 1))
        }
        provincialSeats["Northern Canada" to 3]!!.also {
            it.first.submit(mapOf(lib to 1, ndp to 1))
            it.second.submit(mapOf(lib to 2))
        }
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-4", screen)

        federalSeats.also {
            it.first.submit(mapOf(lib to 157, con to 121, ndp to 24, bq to 32, grn to 3, ind to 1))
            it.second.submit(mapOf(lib to 184, con to 99, ndp to 44, bq to 10, grn to 1))
        }
        provincialSeats["British Columbia" to 42]!!.also {
            it.first.submit(mapOf(lib to 11, con to 17, ndp to 11, grn to 2, ind to 1))
            it.second.submit(mapOf(lib to 17, con to 10, ndp to 14, grn to 1))
        }
        provincialSeats["Northern Canada" to 3]!!.also {
            it.first.submit(mapOf(lib to 2, ndp to 1))
            it.second.submit(mapOf(lib to 3))
        }
        compareRendering("RegionalBreakdownScreen", "SeatsWithLimitedColumns-5", screen)
    }

    @Test
    fun testVotes() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPct = Publisher(0.0)
        val cardiganPct = Publisher(0.0)
        val malpequePct = Publisher(0.0)
        val charlottetownPct = Publisher(0.0)
        val egmontPct = Publisher(0.0)
        val screen = RegionalBreakdownScreen.votes(
            "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            peiVotes,
            peiPct,
            "VOTES BY REGION".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("CARDIGAN".asOneTimePublisher(), cardiganVotes, cardiganPct)
            .withRegion("MALPEQUE".asOneTimePublisher(), malpequeVotes, malpequePct)
            .withRegion("CHARLOTTETOWN".asOneTimePublisher(), charlottetownVotes, charlottetownPct)
            .withRegion("EGMONT".asOneTimePublisher(), egmontVotes, egmontPct)
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Votes-1", screen)
        peiVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        peiPct.submit(1.0 / 27)
        cardiganVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        cardiganPct.submit(1.0 / 7)
        compareRendering("RegionalBreakdownScreen", "Votes-2", screen)
        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        peiPct.submit(1.0)
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        cardiganPct.submit(1.0)
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        malpequePct.submit(1.0)
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        charlottetownPct.submit(1.0)
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        egmontPct.submit(1.0)
        compareRendering("RegionalBreakdownScreen", "Votes-3", screen)
    }

    @Test
    fun testVotesPollsReporting() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPct = Publisher(PollsReporting(0, 27))
        val cardiganPct = Publisher(PollsReporting(0, 7))
        val malpequePct = Publisher(PollsReporting(0, 7))
        val charlottetownPct = Publisher(PollsReporting(0, 6))
        val egmontPct = Publisher(PollsReporting(0, 7))
        val screen = RegionalBreakdownScreen.votesPollsReporting(
            "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            peiVotes,
            peiPct,
            "VOTES BY REGION".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("CARDIGAN".asOneTimePublisher(), cardiganVotes, cardiganPct)
            .withRegion("MALPEQUE".asOneTimePublisher(), malpequeVotes, malpequePct)
            .withRegion("CHARLOTTETOWN".asOneTimePublisher(), charlottetownVotes, charlottetownPct)
            .withRegion("EGMONT".asOneTimePublisher(), egmontVotes, egmontPct)
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesPollsReporting-1", screen)
        peiVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        peiPct.submit(PollsReporting(1, 27))
        cardiganVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        cardiganPct.submit(PollsReporting(1, 7))
        compareRendering("RegionalBreakdownScreen", "VotesPollsReporting-2", screen)
        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        peiPct.submit(PollsReporting(27, 27))
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        cardiganPct.submit(PollsReporting(7, 7))
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        malpequePct.submit(PollsReporting(7, 7))
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        charlottetownPct.submit(PollsReporting(6, 6))
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        egmontPct.submit(PollsReporting(7, 7))
        compareRendering("RegionalBreakdownScreen", "VotesPollsReporting-3", screen)
    }

    @Test
    fun testVotesWithPrev() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequePrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPct = Publisher(0.0)
        val cardiganPct = Publisher(0.0)
        val malpequePct = Publisher(0.0)
        val charlottetownPct = Publisher(0.0)
        val egmontPct = Publisher(0.0)
        val screen = RegionalBreakdownScreen.votesWithPrev(
            "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            peiVotes,
            peiPrevVotes,
            peiPct,
            "VOTES BY REGION".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("CARDIGAN".asOneTimePublisher(), cardiganVotes, cardiganPrevVotes, cardiganPct)
            .withRegion("MALPEQUE".asOneTimePublisher(), malpequeVotes, malpequePrevVotes, malpequePct)
            .withRegion("CHARLOTTETOWN".asOneTimePublisher(), charlottetownVotes, charlottetownPrevVotes, charlottetownPct)
            .withRegion("EGMONT".asOneTimePublisher(), egmontVotes, egmontPrevVotes, egmontPct)
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrev-1", screen)
        peiVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        peiPrevVotes.submit(mapOf(lib to 1173, pc to 1173, grn to 234, ndp to 258))
        peiPct.submit(1.0 / 27)
        cardiganVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        cardiganPrevVotes.submit(mapOf(lib to 1173, pc to 1173, grn to 234, ndp to 258))
        cardiganPct.submit(1.0 / 7)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrev-2", screen)
        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        peiPrevVotes.submit(mapOf(lib to 33481, pc to 30663, grn to 8857, ndp to 8997))
        peiPct.submit(1.0)
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        cardiganPrevVotes.submit(mapOf(lib to 8016, pc to 9444, grn to 1144, ndp to 2404))
        cardiganPct.submit(1.0)
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        malpequePrevVotes.submit(mapOf(lib to 7767, pc to 8169, grn to 4011, ndp to 1427))
        malpequePct.submit(1.0)
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        charlottetownPrevVotes.submit(mapOf(lib to 8383, pc to 6405, grn to 2557, ndp to 3261))
        charlottetownPct.submit(1.0)
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        egmontPrevVotes.submit(mapOf(lib to 9312, pc to 6646, grn to 1138, ndp to 1902))
        egmontPct.submit(1.0)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrev-3", screen)
    }

    @Test
    fun testVotesWithPrevPollsReporting() {
        val peiVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequeVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val cardiganPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val malpequePrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val charlottetownPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val egmontPrevVotes = Publisher<Map<Party, Int>>(emptyMap())
        val peiPct = Publisher(PollsReporting(0, 27))
        val cardiganPct = Publisher(PollsReporting(0, 7))
        val malpequePct = Publisher(PollsReporting(0, 7))
        val charlottetownPct = Publisher(PollsReporting(0, 6))
        val egmontPct = Publisher(PollsReporting(0, 7))
        val screen = RegionalBreakdownScreen.votesWithPrevPollsReporting(
            "PRINCE EDWARD ISLAND".asOneTimePublisher(),
            peiVotes,
            peiPrevVotes,
            peiPct,
            "VOTES BY REGION".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion("CARDIGAN".asOneTimePublisher(), cardiganVotes, cardiganPrevVotes, cardiganPct)
            .withRegion("MALPEQUE".asOneTimePublisher(), malpequeVotes, malpequePrevVotes, malpequePct)
            .withRegion("CHARLOTTETOWN".asOneTimePublisher(), charlottetownVotes, charlottetownPrevVotes, charlottetownPct)
            .withRegion("EGMONT".asOneTimePublisher(), egmontVotes, egmontPrevVotes, egmontPct)
            .build("PRINCE EDWARD ISLAND".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrevPollsReporting-1", screen)
        peiVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        peiPrevVotes.submit(mapOf(lib to 1173, pc to 1173, grn to 234, ndp to 258))
        peiPct.submit(PollsReporting(1, 27))
        cardiganVotes.submit(mapOf(lib to 902, pc to 934, grn to 1152, ndp to 38))
        cardiganPrevVotes.submit(mapOf(lib to 1173, pc to 1173, grn to 234, ndp to 258))
        cardiganPct.submit(PollsReporting(1, 7))
        compareRendering("RegionalBreakdownScreen", "VotesWithPrevPollsReporting-2", screen)
        peiVotes.submit(mapOf(lib to 24346, pc to 30415, grn to 25302, ndp to 2454, ind to 282))
        peiPrevVotes.submit(mapOf(lib to 33481, pc to 30663, grn to 8857, ndp to 8997))
        peiPct.submit(PollsReporting(27, 27))
        cardiganVotes.submit(mapOf(lib to 5265, pc to 9714, grn to 5779, ndp to 277))
        cardiganPrevVotes.submit(mapOf(lib to 8016, pc to 9444, grn to 1144, ndp to 2404))
        cardiganPct.submit(PollsReporting(7, 7))
        malpequeVotes.submit(mapOf(lib to 5548, pc to 9893, grn to 7378, ndp to 244, ind to 80))
        malpequePrevVotes.submit(mapOf(lib to 7767, pc to 8169, grn to 4011, ndp to 1427))
        malpequePct.submit(PollsReporting(7, 7))
        charlottetownVotes.submit(mapOf(lib to 6078, pc to 4932, grn to 6591, ndp to 674, ind to 202))
        charlottetownPrevVotes.submit(mapOf(lib to 8383, pc to 6405, grn to 2557, ndp to 3261))
        charlottetownPct.submit(PollsReporting(6, 6))
        egmontVotes.submit(mapOf(lib to 7455, pc to 5876, grn to 5554, ndp to 1259))
        egmontPrevVotes.submit(mapOf(lib to 9312, pc to 6646, grn to 1138, ndp to 1902))
        egmontPct.submit(PollsReporting(7, 7))
        compareRendering("RegionalBreakdownScreen", "VotesWithPrevPollsReporting-3", screen)
    }

    @Test
    fun testVotesCoalition() {
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val grn = Party("Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation", "ONP", Color.ORANGE.darker())
        val uap = Party("United Australia", "UAP", Color.YELLOW)
        val oth = Party.OTHERS

        val screen = RegionalBreakdownScreen.votes(
            "AUSTRALIA".asOneTimePublisher(),
            mapOf(
                alp to 4776030,
                coa to 5233334,
                grn to 1795985,
                onp to 727464,
                uap to 604536,
                oth to 1521693
            ).asOneTimePublisher(),
            0.8982.asOneTimePublisher(),
            "PRIMARY VOTE BY STATE".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion(
                "NEW SOUTH WALES".asOneTimePublisher(),
                mapOf(coa to 1699323, alp to 1552684, grn to 466069, onp to 224965, uap to 183174, oth to 524725).asOneTimePublisher(),
                0.9070.asOneTimePublisher()
            )
            .withRegion(
                "VICTORIA".asOneTimePublisher(),
                mapOf(coa to 1239280, alp to 1230842, grn to 514893, onp to 143558, uap to 177745, oth to 440115).asOneTimePublisher(),
                0.9059.asOneTimePublisher()
            )
            .withRegion(
                "QUEENSLAND".asOneTimePublisher(),
                mapOf(lnp to 1172515, alp to 811069, grn to 382900, onp to 221640, uap to 149255, oth to 220647).asOneTimePublisher(),
                0.8816.asOneTimePublisher(),
                mapOf(coa to lnp).asOneTimePublisher()
            )
            .withRegion(
                "WESTERN AUSTRALIA".asOneTimePublisher(),
                mapOf(coa to 512414, alp to 542667, grn to 184094, onp to 58226, uap to 33863, oth to 141961).asOneTimePublisher(),
                0.8799.asOneTimePublisher()
            )
            .withRegion(
                "SOUTH AUSTRALIA".asOneTimePublisher(),
                mapOf(coa to 390195, alp to 378329, grn to 140227, onp to 53057, uap to 42688, oth to 93290).asOneTimePublisher(),
                0.9107.asOneTimePublisher()
            )
            .withRegion(
                "TASMANIA".asOneTimePublisher(),
                mapOf(coa to 115184, alp to 95322, grn to 41972, onp to 13970, uap to 6437, oth to 76813).asOneTimePublisher(),
                0.9243.asOneTimePublisher()
            )
            .withRegion(
                "AUSTRALIAN CAPITAL TERRITORY".asOneTimePublisher(),
                mapOf(lib to 74759, alp to 126595, grn to 52648, onp to 6630, uap to 6864, oth to 14501).asOneTimePublisher(),
                0.9207.asOneTimePublisher(),
                mapOf(coa to lib).asOneTimePublisher()
            )
            .withRegion(
                "NORTHERN TERRITORY".asOneTimePublisher(),
                mapOf(clp to 29664, alp to 38522, grn to 13182, onp to 5418, uap to 4510, oth to 9641).asOneTimePublisher(),
                0.7308.asOneTimePublisher(),
                mapOf(coa to clp).asOneTimePublisher()
            )
            .build("AUSTRALIA".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Votes-C", screen)
    }

    @Test
    fun testVotesWithPrevCoalition() {
        val lib = Party("Liberal", "LIB", Color.BLUE.darker())
        val lnp = Party("Liberal National", "LNP", Color.BLUE)
        val clp = Party("Country Liberal", "CLP", Color.ORANGE)
        val coa = Coalition("Coalition", "L/NP", lib, lnp, clp)
        val alp = Party("Labor", "ALP", Color.RED)
        val grn = Party("Greens", "GRN", Color.GREEN.darker())
        val onp = Party("One Nation", "ONP", Color.ORANGE.darker())
        val uap = Party("United Australia", "UAP", Color.YELLOW)
        val oth = Party.OTHERS

        val screen = RegionalBreakdownScreen.votesWithPrev(
            "AUSTRALIA".asOneTimePublisher(),
            mapOf(
                alp to 4776030,
                coa to 5233334,
                grn to 1795985,
                onp to 727464,
                uap to 604536,
                oth to 1521693
            ).asOneTimePublisher(),
            mapOf(
                alp to 4752110,
                coa to 5906884,
                grn to 1482923,
                onp to 438587,
                uap to 488817,
                oth to 1184031
            ).asOneTimePublisher(),
            0.8982.asOneTimePublisher(),
            "PRIMARY VOTE BY STATE".asOneTimePublisher()
        )
            .withBlankRow()
            .withRegion(
                "NEW SOUTH WALES".asOneTimePublisher(),
                mapOf(coa to 1699323, alp to 1552684, grn to 466069, onp to 224965, uap to 183174, oth to 524725).asOneTimePublisher(),
                mapOf(coa to 1930426, alp to 1568173, grn to 395238, onp to 59464, uap to 153477, oth to 430508).asOneTimePublisher(),
                0.9070.asOneTimePublisher()
            )
            .withRegion(
                "VICTORIA".asOneTimePublisher(),
                mapOf(coa to 1239280, alp to 1230842, grn to 514893, onp to 143558, uap to 177745, oth to 440115).asOneTimePublisher(),
                mapOf(coa to 1425542, alp to 1361913, grn to 439169, onp to 35177, uap to 134581, oth to 298650).asOneTimePublisher(),
                0.9059.asOneTimePublisher()
            )
            .withRegion(
                "QUEENSLAND".asOneTimePublisher(),
                mapOf(lnp to 1172515, alp to 811069, grn to 382900, onp to 221640, uap to 149255, oth to 220647).asOneTimePublisher(),
                mapOf(lnp to 1236401, alp to 754792, grn to 292059, onp to 250779, uap to 99329, oth to 195658).asOneTimePublisher(),
                0.8816.asOneTimePublisher(),
                mapOf(coa to lnp).asOneTimePublisher()
            )
            .withRegion(
                "WESTERN AUSTRALIA".asOneTimePublisher(),
                mapOf(coa to 512414, alp to 542667, grn to 184094, onp to 58226, uap to 33863, oth to 141961).asOneTimePublisher(),
                mapOf(coa to 633930, alp to 417727, grn to 162876, onp to 74478, uap to 28488, oth to 84375).asOneTimePublisher(),
                0.8799.asOneTimePublisher()
            )
            .withRegion(
                "SOUTH AUSTRALIA".asOneTimePublisher(),
                mapOf(coa to 390195, alp to 378329, grn to 140227, onp to 53057, uap to 42688, oth to 93290).asOneTimePublisher(),
                mapOf(coa to 438022, alp to 379495, grn to 103036, onp to 8990, uap to 46007, oth to 97101).asOneTimePublisher(),
                0.9107.asOneTimePublisher()
            )
            .withRegion(
                "TASMANIA".asOneTimePublisher(),
                mapOf(coa to 115184, alp to 95322, grn to 41972, onp to 13970, uap to 6437, oth to 76813).asOneTimePublisher(),
                mapOf(coa to 120415, alp to 116955, grn to 35229, onp to 9699, uap to 16868, oth to 48826).asOneTimePublisher(),
                0.9243.asOneTimePublisher()
            )
            .withRegion(
                "AUSTRALIAN CAPITAL TERRITORY".asOneTimePublisher(),
                mapOf(lib to 74759, alp to 126595, grn to 52648, onp to 6630, uap to 6864, oth to 14501).asOneTimePublisher(),
                mapOf(lib to 83311, alp to 109300, grn to 44804, uap to 7117, oth to 21443).asOneTimePublisher(),
                0.9207.asOneTimePublisher(),
                mapOf(coa to lib).asOneTimePublisher()
            )
            .withRegion(
                "NORTHERN TERRITORY".asOneTimePublisher(),
                mapOf(clp to 29664, alp to 38522, grn to 13182, onp to 5418, uap to 4510, oth to 9641).asOneTimePublisher(),
                mapOf(clp to 38837, alp to 43755, grn to 10512, uap to 2950, oth to 7464).asOneTimePublisher(),
                0.7308.asOneTimePublisher(),
                mapOf(coa to clp).asOneTimePublisher()
            )
            .build("AUSTRALIA".asOneTimePublisher())
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesWithPrev-C", screen)
    }

    @Test
    fun testVotesWithLimitedColumns() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val bq = Party("Bloc Quebecois", "BQ", Color.CYAN.darker())
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ppc = Party("Peoples", "PPC", Color.MAGENTA.darker())
        val oth = Party.OTHERS

        val federalVotes = Triple(
            Publisher(emptyMap<Party, Int>()),
            Publisher(emptyMap<Party, Int>()),
            Publisher(0.0)
        )
        val provincialVotes = listOf(
            "Newfoundland and Labrador",
            "Nova Scotia",
            "Prince Edward Island",
            "New Brunswick",
            "Quebec",
            "Ontario",
            "Manitoba",
            "Saskatchewan",
            "Alberta",
            "British Columbia",
            "Northern Canada"
        ).associateWith { Triple(
            Publisher(emptyMap<Party, Int>()),
            Publisher(emptyMap<Party, Int>()),
            Publisher(0.0)
        ) }
        val builder = RegionalBreakdownScreen.votesWithPrev(
            "CANADA".asOneTimePublisher(),
            federalVotes.first,
            federalVotes.second,
            federalVotes.third,
            "POPULAR VOTE BY PROVINCE".asOneTimePublisher(),
            maxColumnsPublisher = 4.asOneTimePublisher()
        ).withBlankRow()
        provincialVotes.forEach { name, (curr, prev, pct) ->
            builder.withRegion(
                name.uppercase().asOneTimePublisher(),
                curr,
                prev,
                pct
            )
        }
        val screen = builder.build("CANADA".asOneTimePublisher())
        screen.size = Dimension(1024, 512)
        compareRendering("RegionalBreakdownScreen", "VotesWithLimitedColumns-1", screen)

        federalVotes.also {
            it.first.submit(mapOf(lib to 331, con to 343, ndp to 160, bq to 76, grn to 65, ppc to 16, oth to 2))
            it.second.submit(mapOf(lib to 395, con to 319, ndp to 197, bq to 47, grn to 34, oth to 2))
            it.third.submit(1.0)
        }
        provincialVotes["Newfoundland and Labrador"]!!.also {
            it.first.submit(mapOf(lib to 449, con to 279, ndp to 237, grn to 31, ppc to 1, oth to 2))
            it.second.submit(mapOf(lib to 645, con to 103, ndp to 210, grn to 11, oth to 29))
            it.third.submit(1.0)
        }
        provincialVotes["Nova Scotia"]!!.also {
            it.first.submit(mapOf(lib to 414, con to 257, ndp to 189, grn to 110, ppc to 12, oth to 18))
            it.second.submit(mapOf(lib to 619, con to 179, ndp to 164, grn to 34, oth to 3))
            it.third.submit(1.0)
        }
        provincialVotes["Prince Edward Island"]!!.also {
            it.first.submit(mapOf(lib to 437, con to 273, ndp to 76, grn to 209, oth to 5))
            it.second.submit(mapOf(lib to 583, con to 193, ndp to 160, grn to 60))
            it.third.submit(1.0)
        }
        provincialVotes["New Brunswick"]!!.also {
            it.first.submit(mapOf(lib to 375, con to 328, ndp to 94, grn to 172, ppc to 20, oth to 11))
            it.second.submit(mapOf(lib to 516, con to 253, ndp to 183, grn to 46, oth to 1))
            it.third.submit(1.0)
        }
        provincialVotes["Quebec"]!!.also {
            it.first.submit(mapOf(lib to 343, con to 160, ndp to 108, bq to 324, grn to 45, ppc to 15, oth to 1))
            it.second.submit(mapOf(lib to 357, con to 167, ndp to 254, bq to 193, grn to 23, oth to 1))
            it.third.submit(1.0)
        }
        provincialVotes["Ontario"]!!.also {
            it.first.submit(mapOf(lib to 416, con to 331, ndp to 168, grn to 62, ppc to 16, oth to 2))
            it.second.submit(mapOf(lib to 448, con to 350, ndp to 166, grn to 29, oth to 2))
            it.third.submit(1.0)
        }
        provincialVotes["Manitoba"]!!.also {
            it.first.submit(mapOf(lib to 265, con to 452, ndp to 208, grn to 51, ppc to 17, oth to 6))
            it.second.submit(mapOf(lib to 446, con to 373, ndp to 138, grn to 32, oth to 6))
            it.third.submit(1.0)
        }
        provincialVotes["Saskatchewan"]!!.also {
            it.first.submit(mapOf(lib to 117, con to 640, ndp to 196, grn to 26, ppc to 18, oth to 2))
            it.second.submit(mapOf(lib to 239, con to 485, ndp to 251, grn to 21, oth to 2))
            it.third.submit(1.0)
        }
        provincialVotes["Alberta"]!!.also {
            it.first.submit(mapOf(lib to 138, con to 690, ndp to 116, grn to 28, ppc to 22, oth to 5))
            it.second.submit(mapOf(lib to 246, con to 595, ndp to 116, grn to 25, oth to 8))
            it.third.submit(1.0)
        }
        provincialVotes["British Columbia"]!!.also {
            it.first.submit(mapOf(lib to 262, con to 340, ndp to 244, grn to 125, ppc to 17, oth to 13))
            it.second.submit(mapOf(lib to 352, con to 300, ndp to 259, grn to 82, oth to 1))
            it.third.submit(1.0)
        }
        provincialVotes["Northern Canada"]!!.also {
            it.first.submit(mapOf(lib to 347, con to 281, ndp to 284, grn to 78, ppc to 11))
            it.second.submit(mapOf(lib to 497, con to 223, ndp to 256, grn to 24))
            it.third.submit(1.0)
        }
        compareRendering("RegionalBreakdownScreen", "VotesWithLimitedColumns-2", screen)
    }

    companion object {
        private val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        private val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        private val lib = Party("Liberal", "LIB", Color.RED)
        private val grn = Party("Green", "GRN", Color.GREEN.darker())
        private val ind = Party("Independent", "IND", Color.GRAY)
    }
}