package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class PartySummaryScreenTest {
    private var lib = Party("Liberal", "LIB", Color.RED)
    private var con = Party("Conservative", "CON", Color.BLUE)
    private var ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
    private var bq = Party("Bloc Qu\u00e9b\u00e9cois", "BQ", Color.CYAN.darker())
    private var grn = Party("Green", "GRN", Color.GREEN.darker())
    private var oth = Party.OTHERS

    @Test
    fun testBasicPartySummaryWithDiff() {
        val canada = Region("Canada")
        val bc = Region("British Columbia")
        val prairies = Region("Prairies")
        val ontario = Region("Ontario")
        val quebec = Region("Qu\u00e9bec")
        val atlantic = Region("Atlantic")
        val north = Region("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.ofDiff(
            canada,
            { r -> r.name.uppercase().asOneTimePublisher() },
            { r -> r.seatsPublisher },
            { r -> r.seatDiffPublisher },
            { r -> r.votePublisher },
            { r -> r.voteDiffPublisher },
            3,
        )
            .withRegion(bc)
            .withRegion(prairies)
            .withRegion(ontario)
            .withRegion(quebec)
            .withRegion(atlantic)
            .withRegion(north)
            .build(partySelected)
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "SingleParty-1", screen)

        atlantic.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        atlantic.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        atlantic.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        atlantic.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        canada.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        canada.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        canada.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        compareRendering("PartySummaryScreen", "SingleParty-2", screen)

        quebec.seats = mapOf(lib to 35, con to 10, ndp to 1, bq to 32)
        quebec.seatDiff = mapOf(lib to -5, con to -2, ndp to -15, bq to +22)
        quebec.votePct = mapOf(lib to 0.3428, con to 0.1598, ndp to 0.1084, grn to 0.0451, bq to 0.3237, oth to 0.0201)
        quebec.votePctDiff = mapOf(lib to -0.0146, con to -0.0074, ndp to -0.1451, grn to +0.0227, bq to +0.1301, oth to +0.0143)
        ontario.seats = mapOf(lib to 79, con to 36, ndp to 6)
        ontario.seatDiff = mapOf(lib to -1, con to +3, ndp to -2)
        ontario.votePct = mapOf(lib to 0.4155, con to 0.3305, ndp to 0.1681, grn to 0.0623, oth to 0.0235)
        ontario.votePctDiff = mapOf(lib to -0.0323, con to -0.0201, ndp to +0.0021, grn to +0.0339, oth to +0.0164)
        prairies.seats = mapOf(lib to 4, con to 54, ndp to 4)
        prairies.seatDiff = mapOf(lib to -8, con to +10, ndp to -2)
        prairies.votePct = mapOf(lib to 0.1574, con to 0.6381, ndp to 0.1470, grn to 0.0321, oth to 0.0253)
        prairies.votePctDiff = mapOf(lib to -0.1258, con to +0.1054, ndp to +0.0026, grn to +0.0064, oth to +0.0115)
        north.seats = mapOf(lib to 1, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3645, con to 0.2574, ndp to 0.2914, grn to 0.0752, oth to 0.0115)
        north.votePctDiff = mapOf(lib to -0.1141, con to +0.0491, ndp to 0.0016, grn to +0.0520, oth to +0.0115)
        canada.seats = mapOf(lib to 145, con to 104, ndp to 13, grn to 1, bq to 32)
        canada.seatDiff = mapOf(lib to -21, con to +15, ndp to -17, grn to +1, bq to +22)
        canada.votePct = mapOf(lib to 0.3418, con to 0.3439, ndp to 0.1469, grn to 0.0564, bq to 0.0880, oth to 0.0230)
        canada.votePctDiff = mapOf(lib to -0.0595, con to 0.0216, ndp to -0.0405, grn to 0.0296, bq to +0.0340, oth to +0.0148)
        compareRendering("PartySummaryScreen", "SingleParty-3", screen)

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "SingleParty-4", screen)

        north.seats = mapOf(lib to 2, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3511, con to 0.2889, ndp to 0.2591, grn to 0.0885, oth to 0.0124)
        north.votePctDiff = mapOf(lib to -0.1505, con to +0.0669, ndp to +0.0072, grn to +0.0640, oth to +0.0124)
        bc.seats = mapOf(lib to 11, con to 17, ndp to 11, grn to 2, oth to 1)
        bc.seatDiff = mapOf(lib to -6, con to +7, ndp to -3, grn to +1, oth to +1)
        bc.votePct = mapOf(lib to 0.2616, con to 0.3398, ndp to 0.2444, grn to 0.1248, oth to 0.0294)
        bc.votePctDiff = mapOf(lib to -0.0893, con to +0.0404, ndp to -0.0158, grn to +0.0424, oth to +0.0223)
        canada.seats = mapOf(lib to 157, con to 121, ndp to 24, grn to 3, bq to 32, oth to 1)
        canada.seatDiff = mapOf(lib to -27, con to +22, ndp to -20, grn to +2, bq to +22, oth to +1)
        canada.votePct = mapOf(lib to 0.3312, con to 0.3434, ndp to 0.1598, grn to 0.0655, bq to 0.0763, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.0634, con to +0.0243, ndp to -0.0374, grn to +0.0312, bq to +0.0297, oth to +0.0158)
        compareRendering("PartySummaryScreen", "SingleParty-5", screen)
    }

    @Test
    fun testBasicPartySummaryWithPrev() {
        val canada = Region("Canada")
        val bc = Region("British Columbia")
        val prairies = Region("Prairies")
        val ontario = Region("Ontario")
        val quebec = Region("Qu\u00e9bec")
        val atlantic = Region("Atlantic")
        val north = Region("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.ofPrev(
            canada,
            { r -> r.name.uppercase().asOneTimePublisher() },
            { r -> r.seatsPublisher },
            { r -> r.prevSeatPublisher },
            { r -> r.votePublisher },
            { r -> r.prevVotePublisher },
            3,
        )
            .withRegion(bc)
            .withRegion(prairies)
            .withRegion(ontario)
            .withRegion(quebec)
            .withRegion(atlantic)
            .withRegion(north)
            .build(partySelected)
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "SingleParty-1", screen)

        atlantic.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        atlantic.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        atlantic.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        atlantic.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        canada.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        canada.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        canada.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        compareRendering("PartySummaryScreen", "SingleParty-2", screen)

        quebec.seats = mapOf(lib to 35, con to 10, ndp to 1, bq to 32)
        quebec.seatDiff = mapOf(lib to -5, con to -2, ndp to -15, bq to +22)
        quebec.votePct = mapOf(lib to 0.3428, con to 0.1598, ndp to 0.1084, grn to 0.0451, bq to 0.3237, oth to 0.0201)
        quebec.votePctDiff = mapOf(lib to -0.0146, con to -0.0074, ndp to -0.1451, grn to +0.0227, bq to +0.1301, oth to +0.0143)
        ontario.seats = mapOf(lib to 79, con to 36, ndp to 6)
        ontario.seatDiff = mapOf(lib to -1, con to +3, ndp to -2)
        ontario.votePct = mapOf(lib to 0.4155, con to 0.3305, ndp to 0.1681, grn to 0.0623, oth to 0.0235)
        ontario.votePctDiff = mapOf(lib to -0.0323, con to -0.0201, ndp to +0.0021, grn to +0.0339, oth to +0.0164)
        prairies.seats = mapOf(lib to 4, con to 54, ndp to 4)
        prairies.seatDiff = mapOf(lib to -8, con to +10, ndp to -2)
        prairies.votePct = mapOf(lib to 0.1574, con to 0.6381, ndp to 0.1470, grn to 0.0321, oth to 0.0253)
        prairies.votePctDiff = mapOf(lib to -0.1258, con to +0.1054, ndp to +0.0026, grn to +0.0064, oth to +0.0115)
        north.seats = mapOf(lib to 1, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3645, con to 0.2574, ndp to 0.2914, grn to 0.0752, oth to 0.0115)
        north.votePctDiff = mapOf(lib to -0.1141, con to +0.0491, ndp to 0.0016, grn to +0.0520, oth to +0.0115)
        canada.seats = mapOf(lib to 145, con to 104, ndp to 13, grn to 1, bq to 32)
        canada.seatDiff = mapOf(lib to -21, con to +15, ndp to -17, grn to +1, bq to +22)
        canada.votePct = mapOf(lib to 0.3418, con to 0.3439, ndp to 0.1469, grn to 0.0564, bq to 0.0880, oth to 0.0230)
        canada.votePctDiff = mapOf(lib to -0.0595, con to 0.0216, ndp to -0.0405, grn to 0.0296, bq to +0.0340, oth to +0.0148)
        compareRendering("PartySummaryScreen", "SingleParty-3", screen)

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "SingleParty-4", screen)

        north.seats = mapOf(lib to 2, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3511, con to 0.2889, ndp to 0.2591, grn to 0.0885, oth to 0.0124)
        north.votePctDiff = mapOf(lib to -0.1505, con to +0.0669, ndp to +0.0072, grn to +0.0640, oth to +0.0124)
        bc.seats = mapOf(lib to 11, con to 17, ndp to 11, grn to 2, oth to 1)
        bc.seatDiff = mapOf(lib to -6, con to +7, ndp to -3, grn to +1, oth to +1)
        bc.votePct = mapOf(lib to 0.2616, con to 0.3398, ndp to 0.2444, grn to 0.1248, oth to 0.0294)
        bc.votePctDiff = mapOf(lib to -0.0893, con to +0.0404, ndp to -0.0158, grn to +0.0424, oth to +0.0223)
        canada.seats = mapOf(lib to 157, con to 121, ndp to 24, grn to 3, bq to 32, oth to 1)
        canada.seatDiff = mapOf(lib to -27, con to +22, ndp to -20, grn to +2, bq to +22, oth to +1)
        canada.votePct = mapOf(lib to 0.3312, con to 0.3434, ndp to 0.1598, grn to 0.0655, bq to 0.0763, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.0634, con to +0.0243, ndp to -0.0374, grn to +0.0312, bq to +0.0297, oth to +0.0158)
        compareRendering("PartySummaryScreen", "SingleParty-5", screen)
    }

    @Test
    fun testBasicPartySummarySeatsOnly() {
        val canada = Region("Canada")
        val bc = Region("British Columbia")
        val prairies = Region("Prairies")
        val ontario = Region("Ontario")
        val quebec = Region("Qu\u00e9bec")
        val atlantic = Region("Atlantic")
        val north = Region("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.of(
            canada,
            { r -> r.name.uppercase().asOneTimePublisher() },
            3,
        )
            .withSeatAndDiff(
                { r -> r.seatsPublisher },
                { r -> r.seatDiffPublisher },
                "SEAT PROJECTION",
            )
            .withRegion(bc)
            .withRegion(prairies)
            .withRegion(ontario)
            .withRegion(quebec)
            .withRegion(atlantic)
            .withRegion(north)
            .build(partySelected)
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-1", screen)

        atlantic.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        atlantic.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        atlantic.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        atlantic.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        canada.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        canada.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        canada.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-2", screen)

        quebec.seats = mapOf(lib to 35, con to 10, ndp to 1, bq to 32)
        quebec.seatDiff = mapOf(lib to -5, con to -2, ndp to -15, bq to +22)
        quebec.votePct = mapOf(lib to 0.3428, con to 0.1598, ndp to 0.1084, grn to 0.0451, bq to 0.3237, oth to 0.0201)
        quebec.votePctDiff = mapOf(lib to -0.0146, con to -0.0074, ndp to -0.1451, grn to +0.0227, bq to +0.1301, oth to +0.0143)
        ontario.seats = mapOf(lib to 79, con to 36, ndp to 6)
        ontario.seatDiff = mapOf(lib to -1, con to +3, ndp to -2)
        ontario.votePct = mapOf(lib to 0.4155, con to 0.3305, ndp to 0.1681, grn to 0.0623, oth to 0.0235)
        ontario.votePctDiff = mapOf(lib to -0.0323, con to -0.0201, ndp to +0.0021, grn to +0.0339, oth to +0.0164)
        prairies.seats = mapOf(lib to 4, con to 54, ndp to 4)
        prairies.seatDiff = mapOf(lib to -8, con to +10, ndp to -2)
        prairies.votePct = mapOf(lib to 0.1574, con to 0.6381, ndp to 0.1470, grn to 0.0321, oth to 0.0253)
        prairies.votePctDiff = mapOf(lib to -0.1258, con to +0.1054, ndp to +0.0026, grn to +0.0064, oth to +0.0115)
        north.seats = mapOf(lib to 1, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3645, con to 0.2574, ndp to 0.2914, grn to 0.0752, oth to 0.0115)
        north.votePctDiff = mapOf(lib to -0.1141, con to +0.0491, ndp to 0.0016, grn to +0.0520, oth to +0.0115)
        canada.seats = mapOf(lib to 145, con to 104, ndp to 13, grn to 1, bq to 32)
        canada.seatDiff = mapOf(lib to -21, con to +15, ndp to -17, grn to +1, bq to +22)
        canada.votePct = mapOf(lib to 0.3418, con to 0.3439, ndp to 0.1469, grn to 0.0564, bq to 0.0880, oth to 0.0230)
        canada.votePctDiff = mapOf(lib to -0.0595, con to 0.0216, ndp to -0.0405, grn to 0.0296, bq to +0.0340, oth to +0.0148)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-3", screen)

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-4", screen)

        north.seats = mapOf(lib to 2, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3511, con to 0.2889, ndp to 0.2591, grn to 0.0885, oth to 0.0124)
        north.votePctDiff = mapOf(lib to -0.1505, con to +0.0669, ndp to +0.0072, grn to +0.0640, oth to +0.0124)
        bc.seats = mapOf(lib to 11, con to 17, ndp to 11, grn to 2, oth to 1)
        bc.seatDiff = mapOf(lib to -6, con to +7, ndp to -3, grn to +1, oth to +1)
        bc.votePct = mapOf(lib to 0.2616, con to 0.3398, ndp to 0.2444, grn to 0.1248, oth to 0.0294)
        bc.votePctDiff = mapOf(lib to -0.0893, con to +0.0404, ndp to -0.0158, grn to +0.0424, oth to +0.0223)
        canada.seats = mapOf(lib to 157, con to 121, ndp to 24, grn to 3, bq to 32, oth to 1)
        canada.seatDiff = mapOf(lib to -27, con to +22, ndp to -20, grn to +2, bq to +22, oth to +1)
        canada.votePct = mapOf(lib to 0.3312, con to 0.3434, ndp to 0.1598, grn to 0.0655, bq to 0.0763, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.0634, con to +0.0243, ndp to -0.0374, grn to +0.0312, bq to +0.0297, oth to +0.0158)
        compareRendering("PartySummaryScreen", "SinglePartySeatsOnly-5", screen)
    }

    @Test
    fun testBasicPartySummaryVotesOnly() {
        val canada = Region("Canada")
        val bc = Region("British Columbia")
        val prairies = Region("Prairies")
        val ontario = Region("Ontario")
        val quebec = Region("Qu\u00e9bec")
        val atlantic = Region("Atlantic")
        val north = Region("North")
        val partySelected = Publisher(lib)
        val screen = PartySummaryScreen.of(
            canada,
            { r -> r.name.uppercase().asOneTimePublisher() },
            3,
        )
            .withVotePctAndDiff(
                { r -> r.votePublisher },
                { r -> r.voteDiffPublisher },
                "CENTRAL VOTE FORECAST",
            )
            .withRegion(bc)
            .withRegion(prairies)
            .withRegion(ontario)
            .withRegion(quebec)
            .withRegion(atlantic)
            .withRegion(north)
            .build(partySelected)
        screen.setSize(1024, 512)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-1", screen)

        atlantic.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        atlantic.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        atlantic.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        atlantic.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        canada.seats = mapOf(lib to 26, con to 4, ndp to 1, grn to 1)
        canada.seatDiff = mapOf(lib to -6, con to +4, ndp to +1, grn to +1)
        canada.votePct = mapOf(lib to 0.4089, con to 0.2863, ndp to 0.1583, grn to 0.1227, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.1784, con to +0.0960, ndp to -0.0209, grn to +0.0874, oth to +0.0159)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-2", screen)

        quebec.seats = mapOf(lib to 35, con to 10, ndp to 1, bq to 32)
        quebec.seatDiff = mapOf(lib to -5, con to -2, ndp to -15, bq to +22)
        quebec.votePct = mapOf(lib to 0.3428, con to 0.1598, ndp to 0.1084, grn to 0.0451, bq to 0.3237, oth to 0.0201)
        quebec.votePctDiff = mapOf(lib to -0.0146, con to -0.0074, ndp to -0.1451, grn to +0.0227, bq to +0.1301, oth to +0.0143)
        ontario.seats = mapOf(lib to 79, con to 36, ndp to 6)
        ontario.seatDiff = mapOf(lib to -1, con to +3, ndp to -2)
        ontario.votePct = mapOf(lib to 0.4155, con to 0.3305, ndp to 0.1681, grn to 0.0623, oth to 0.0235)
        ontario.votePctDiff = mapOf(lib to -0.0323, con to -0.0201, ndp to +0.0021, grn to +0.0339, oth to +0.0164)
        prairies.seats = mapOf(lib to 4, con to 54, ndp to 4)
        prairies.seatDiff = mapOf(lib to -8, con to +10, ndp to -2)
        prairies.votePct = mapOf(lib to 0.1574, con to 0.6381, ndp to 0.1470, grn to 0.0321, oth to 0.0253)
        prairies.votePctDiff = mapOf(lib to -0.1258, con to +0.1054, ndp to +0.0026, grn to +0.0064, oth to +0.0115)
        north.seats = mapOf(lib to 1, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3645, con to 0.2574, ndp to 0.2914, grn to 0.0752, oth to 0.0115)
        north.votePctDiff = mapOf(lib to -0.1141, con to +0.0491, ndp to 0.0016, grn to +0.0520, oth to +0.0115)
        canada.seats = mapOf(lib to 145, con to 104, ndp to 13, grn to 1, bq to 32)
        canada.seatDiff = mapOf(lib to -21, con to +15, ndp to -17, grn to +1, bq to +22)
        canada.votePct = mapOf(lib to 0.3418, con to 0.3439, ndp to 0.1469, grn to 0.0564, bq to 0.0880, oth to 0.0230)
        canada.votePctDiff = mapOf(lib to -0.0595, con to 0.0216, ndp to -0.0405, grn to 0.0296, bq to +0.0340, oth to +0.0148)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-3", screen)

        partySelected.submit(con)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-4", screen)

        north.seats = mapOf(lib to 2, ndp to 1)
        north.seatDiff = mapOf(lib to -1, ndp to +1)
        north.votePct = mapOf(lib to 0.3511, con to 0.2889, ndp to 0.2591, grn to 0.0885, oth to 0.0124)
        north.votePctDiff = mapOf(lib to -0.1505, con to +0.0669, ndp to +0.0072, grn to +0.0640, oth to +0.0124)
        bc.seats = mapOf(lib to 11, con to 17, ndp to 11, grn to 2, oth to 1)
        bc.seatDiff = mapOf(lib to -6, con to +7, ndp to -3, grn to +1, oth to +1)
        bc.votePct = mapOf(lib to 0.2616, con to 0.3398, ndp to 0.2444, grn to 0.1248, oth to 0.0294)
        bc.votePctDiff = mapOf(lib to -0.0893, con to +0.0404, ndp to -0.0158, grn to +0.0424, oth to +0.0223)
        canada.seats = mapOf(lib to 157, con to 121, ndp to 24, grn to 3, bq to 32, oth to 1)
        canada.seatDiff = mapOf(lib to -27, con to +22, ndp to -20, grn to +2, bq to +22, oth to +1)
        canada.votePct = mapOf(lib to 0.3312, con to 0.3434, ndp to 0.1598, grn to 0.0655, bq to 0.0763, oth to 0.0238)
        canada.votePctDiff = mapOf(lib to -0.0634, con to +0.0243, ndp to -0.0374, grn to +0.0312, bq to +0.0297, oth to +0.0158)
        compareRendering("PartySummaryScreen", "SinglePartyVotesOnly-5", screen)
    }

    private class Region constructor(val name: String) {
        var seats: Map<Party, Int> = emptyMap()
            set(value) {
                field = value
                seatsPublisher.submit(seats)
                prevSeatPublisher.submit(prevSeats)
            }
        val seatsPublisher = Publisher(seats)

        var seatDiff: Map<Party, Int> = emptyMap()
            set(value) {
                field = value
                seatDiffPublisher.submit(seatDiff)
                prevSeatPublisher.submit(prevSeats)
            }
        val seatDiffPublisher = Publisher(seatDiff)

        var votePct: Map<Party, Double> = emptyMap()
            set(value) {
                field = value
                votePublisher.submit(votePct)
                prevVotePublisher.submit(prevVotePct)
            }
        val votePublisher = Publisher(votePct)

        var votePctDiff: Map<Party, Double> = emptyMap()
            set(value) {
                field = value
                voteDiffPublisher.submit(votePctDiff)
                prevVotePublisher.submit(prevVotePct)
            }
        val voteDiffPublisher = Publisher(votePctDiff)

        val prevSeats: Map<Party, Int>
            get() = sequenceOf(seats.keys, seatDiff.keys)
                .flatten()
                .distinct()
                .associateWith { (seats[it] ?: 0) - (seatDiff[it] ?: 0) }
        val prevSeatPublisher = Publisher(prevSeats)

        val prevVotePct: Map<Party, Double>
            get() = sequenceOf(votePct.keys, votePctDiff.keys)
                .flatten()
                .distinct()
                .associateWith { (votePct[it] ?: 0.0) - (votePctDiff[it] ?: 0.0) }
        val prevVotePublisher = Publisher(prevVotePct)
    }
}
