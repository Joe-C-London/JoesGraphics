package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class SeatsVotesScreenTest {

    @Test
    fun testSingle() {
        val lab = Party("Labour", "LAB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ld = Party("Liberal Democrat", "LD", Color.ORANGE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ref = Party("Reform", "REF", Color.CYAN.darker())
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker().darker())
        val oth = Party.OTHERS

        val seats = Publisher<Map<Party, Int>>(emptyMap())
        val totalSeats = Publisher(650)
        val seatsProgress = Publisher("0 OF 650")
        val showMajority = Publisher(true)

        val votes = Publisher<Map<Party, Int>>(emptyMap())
        val votesReporting = Publisher(0.0)
        val votesProgress = Publisher("0/650")

        val title = Publisher("UNITED KINGDOM")

        val screen = SeatsVotesScreen.of(
            seats = {
                this.header = "SEATS".asOneTimePublisher()
                this.subhead = "".asOneTimePublisher()
                this.seats = seats
                this.total = totalSeats
                this.progressLabel = seatsProgress
                this.majorityLine = {
                    show = showMajority
                    display = { "$it SEATS FOR MAJORITY" }
                }
            },
            votes = {
                this.header = "VOTES".asOneTimePublisher()
                this.subhead = "".asOneTimePublisher()
                this.votes = votes
                this.pctReporting = votesReporting
                this.progressLabel = votesProgress
            },
            title = title,
        )
        screen.setSize(1024, 512)
        compareRendering("SeatsVotesScreen", "Single-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                UNITED KINGDOM
                
                SEATS [0 OF 650]
                326 SEATS FOR MAJORITY
                
                VOTES [0/650]
            """.trimIndent(),
        )

        seats.submit(mapOf(lab to 1))
        seatsProgress.submit("1 OF 650")
        votes.submit(mapOf(lab to 18837, ref to 11668, con to 5514, ld to 2290, grn to 1723))
        votesReporting.submit(1.0 / 650)
        votesProgress.submit("1/650")
        compareRendering("SeatsVotesScreen", "Single-2", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                UNITED KINGDOM

                SEATS [1 OF 650]
                LABOUR: 1
                326 SEATS FOR MAJORITY
                
                VOTES [1/650]
                LABOUR: 47.1%
                REFORM: 29.1%
                CONSERVATIVE: 13.8%
                LIBERAL DEMOCRAT: 5.7%
                GREEN: 4.3%
            """.trimIndent(),
        )

        seats.submit(mapOf(lab to 411, con to 121, ld to 72, snp to 9, ref to 5, grn to 4, pc to 4, oth to 24))
        seatsProgress.submit("650 OF 650")
        votes.submit(mapOf(lab to 9708716, con to 6828925, ld to 3519143, snp to 724758, ref to 4117610, grn to 1944501, pc to 194811, oth to 1319946))
        votesReporting.submit(1.0)
        votesProgress.submit("650/650")
        compareRendering("SeatsVotesScreen", "Single-3", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                UNITED KINGDOM

                SEATS [650 OF 650]
                LABOUR: 411
                CONSERVATIVE: 121
                LIBERAL DEMOCRAT: 72
                SCOTTISH NATIONAL PARTY: 9
                REFORM: 5
                GREEN: 4
                PLAID CYMRU: 4
                OTHERS: 24
                326 SEATS FOR MAJORITY
                
                VOTES [650/650]
                LABOUR: 34.2%
                CONSERVATIVE: 24.1%
                REFORM: 14.5%
                LIBERAL DEMOCRAT: 12.4%
                GREEN: 6.9%
                SCOTTISH NATIONAL PARTY: 2.6%
                PLAID CYMRU: 0.7%
                OTHERS: 4.7%
            """.trimIndent(),
        )

        title.submit("SCOTLAND")
        seats.submit(mapOf(lab to 37, snp to 9, con to 5, ld to 6))
        totalSeats.submit(57)
        seatsProgress.submit("57 OF 57")
        showMajority.submit(false)
        votes.submit(mapOf(lab to 851897, snp to 724758, con to 307344, ld to 234228, ref to 167979, grn to 92685, oth to 35919))
        votesProgress.submit("57/57")
        compareRendering("SeatsVotesScreen", "Single-4", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                SCOTLAND

                SEATS [57 OF 57]
                LABOUR: 37
                SCOTTISH NATIONAL PARTY: 9
                LIBERAL DEMOCRAT: 6
                CONSERVATIVE: 5
                
                VOTES [57/57]
                LABOUR: 35.3%
                SCOTTISH NATIONAL PARTY: 30.0%
                CONSERVATIVE: 12.7%
                LIBERAL DEMOCRAT: 9.7%
                REFORM: 7.0%
                GREEN: 3.8%
                OTHERS: 1.5%
            """.trimIndent(),
        )
    }

    @Test
    fun testDual() {
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val lib = Party("Liberal", "LIB", Color.RED)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val oth = Party.OTHERS

        val seats = Publisher<Map<Party, Pair<Int, Int>>>(emptyMap())
        val totalSeats = Publisher(27)
        val seatsProgress = Publisher("0 OF 27")
        val showMajority = Publisher(true)

        val votes = Publisher<Map<Party, Int>>(emptyMap())
        val votesReporting = Publisher(0.0)
        val votesProgress = Publisher("0/267")

        val title = Publisher("PRINCE EDWARD ISLAND")

        val screen = SeatsVotesScreen.ofElectedLeading(
            seats = {
                this.header = "SEATS".asOneTimePublisher()
                this.subhead = "".asOneTimePublisher()
                this.seats = seats
                this.total = totalSeats
                this.progressLabel = seatsProgress
                this.majorityLine = {
                    show = showMajority
                    display = { "$it SEATS FOR MAJORITY" }
                }
            },
            votes = {
                this.header = "VOTES".asOneTimePublisher()
                this.subhead = "".asOneTimePublisher()
                this.votes = votes
                this.pctReporting = votesReporting
                this.progressLabel = votesProgress
            },
            title = title,
        )
        screen.setSize(1024, 512)
        compareRendering("SeatsVotesScreen", "Dual-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                PRINCE EDWARD ISLAND

                SEATS [0 OF 27]
                14 SEATS FOR MAJORITY
                
                VOTES [0/267]
            """.trimIndent(),
        )

        seats.submit(mapOf(pc to (1 to 1)))
        seatsProgress.submit("1 OF 27")
        votes.submit(mapOf(pc to 1593, lib to 481, ndp to 29, oth to 16, grn to 757))
        votesReporting.submit(10.0 / 267)
        votesProgress.submit("10/267")
        compareRendering("SeatsVotesScreen", "Dual-2", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                PRINCE EDWARD ISLAND

                SEATS [1 OF 27]
                PROGRESSIVE CONSERVATIVE: 1/1
                14 SEATS FOR MAJORITY
                
                VOTES [10/267]
                PROGRESSIVE CONSERVATIVE: 55.4%
                GREEN: 26.3%
                LIBERAL: 16.7%
                NEW DEMOCRATIC PARTY: 1.0%
                OTHERS: 0.6%
            """.trimIndent(),
        )

        seats.submit(mapOf(pc to (13 to 13), grn to (4 to 8), lib to (4 to 6)))
        seatsProgress.submit("27 OF 27")
        votes.submit(mapOf(pc to 41828, grn to 16134, lib to 12876, ndp to 3359, oth to 595))
        votesReporting.submit(1.0)
        votesProgress.submit("267/267")
        compareRendering("SeatsVotesScreen", "Dual-3", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                PRINCE EDWARD ISLAND

                SEATS [27 OF 27]
                PROGRESSIVE CONSERVATIVE: 13/13
                GREEN: 4/8
                LIBERAL: 4/6
                14 SEATS FOR MAJORITY
                
                VOTES [267/267]
                PROGRESSIVE CONSERVATIVE: 55.9%
                GREEN: 21.6%
                LIBERAL: 17.2%
                NEW DEMOCRATIC PARTY: 4.5%
                OTHERS: 0.8%
            """.trimIndent(),
        )

        seats.submit(mapOf(pc to (7 to 7), grn to (0 to 1)))
        totalSeats.submit(8)
        seatsProgress.submit("8 OF 8")
        showMajority.submit(false)
        votes.submit(mapOf(pc to 13743, grn to 4311, lib to 2619, ndp to 1026, oth to 234))
        votesProgress.submit("77/77")
        title.submit("CARDIGAN")
        compareRendering("SeatsVotesScreen", "Dual-4", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                CARDIGAN

                SEATS [8 OF 8]
                PROGRESSIVE CONSERVATIVE: 7/7
                GREEN: 0/1
                
                VOTES [77/77]
                PROGRESSIVE CONSERVATIVE: 62.7%
                GREEN: 19.7%
                LIBERAL: 11.9%
                NEW DEMOCRATIC PARTY: 4.7%
                OTHERS: 1.1%
            """.trimIndent(),
        )
    }

    @Test
    fun testRange() {
        val alp = Party("Labor", "ALP", Color.RED)
        val coa = Party("Coalition", "L/NP", Color.BLUE)
        val grn = Party("Greens", "GRN", Color.GREEN)
        val oth = Party.OTHERS

        val seats = Publisher(mapOf(alp to 59..71, coa to 64..78, grn to 3..5, oth to 7..13))
        val totalSeats = Publisher(150)
        val showMajority = Publisher(true)

        val votes = Publisher(mapOf(alp to 0.27..0.33, coa to 0.35..0.41, grn to 0.11..0.14, oth to 0.16..0.23))

        val title = Publisher("AUSTRALIA")

        val screen = SeatsVotesScreen.ofRange(
            seats = {
                this.header = "SEAT PROJECTION".asOneTimePublisher()
                this.subhead = "HOUSE OF REPRESENTATIVES".asOneTimePublisher()
                this.seats = seats
                this.total = totalSeats
                this.notes = "SOURCE: Accent Research, 6 December 2024".asOneTimePublisher()
                this.majorityLine = {
                    show = showMajority
                    display = { "$it SEATS FOR MAJORITY" }
                }
            },
            votes = {
                this.header = "FIRST PREFERENCE VOTES".asOneTimePublisher()
                this.subhead = "HOUSE OF REPRESENTATIVES".asOneTimePublisher()
                this.votes = votes
                this.notes = "SOURCE: Blend of polls from December 2024".asOneTimePublisher()
            },
            title = title,
        )
        screen.setSize(1024, 512)
        compareRendering("SeatsVotesScreen", "Range-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                AUSTRALIA

                SEAT PROJECTION, HOUSE OF REPRESENTATIVES
                COALITION: 64-78
                LABOR: 59-71
                GREENS: 3-5
                OTHERS: 7-13
                76 SEATS FOR MAJORITY
                
                FIRST PREFERENCE VOTES, HOUSE OF REPRESENTATIVES
                COALITION: 35.0-41.0%
                LABOR: 27.0-33.0%
                GREENS: 11.0-14.0%
                OTHERS: 16.0-23.0%
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesSingle() {
        val harris = Candidate("Kamala Harris", Party("Democratic", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val oth = Candidate.OTHERS

        val seats = Publisher(mapOf(harris to 226, trump to 312))
        val totalSeats = Publisher(538)
        val showMajority = Publisher(true)

        val votes = Publisher(mapOf(harris to 75015834, trump to 77302169, oth to 3101635))

        val title = Publisher("PRESIDENT")

        val screen = SeatsVotesScreen.ofCandidates(
            seats = {
                this.header = "ELECTORAL VOTES".asOneTimePublisher()
                this.subhead = "".asOneTimePublisher()
                this.seats = seats
                this.total = totalSeats
                this.majorityLine = {
                    show = showMajority
                    display = { "$it TO WIN" }
                }
            },
            votes = {
                this.header = "POPULAR VOTE".asOneTimePublisher()
                this.subhead = "".asOneTimePublisher()
                this.votes = votes
            },
            title = title,
        )
        screen.setSize(1024, 512)
        compareRendering("SeatsVotesScreen", "Candidates-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                PRESIDENT

                ELECTORAL VOTES
                DONALD TRUMP (GOP): 312
                KAMALA HARRIS (DEM): 226
                270 TO WIN
                
                POPULAR VOTE
                DONALD TRUMP (GOP): 77,302,169 (49.7%)
                KAMALA HARRIS (DEM): 75,015,834 (48.3%)
                OTHERS: 3,101,635 (2.0%)
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesDual() {
        val harris = Candidate("Kamala Harris", Party("Democratic", "DEM", Color.BLUE))
        val trump = Candidate("Donald Trump", Party("Republican", "GOP", Color.RED))
        val oth = Candidate.OTHERS

        val seats = Publisher(mapOf(harris to (177 to 226), trump to (219 to 312)))
        val totalSeats = Publisher(538)
        val showMajority = Publisher(true)

        val votes = Publisher(mapOf(harris to 75015834, trump to 77302169, oth to 3101635))

        val title = Publisher("PRESIDENT")

        val screen = SeatsVotesScreen.ofCandidatesElectedLeading(
            seats = {
                this.header = "ELECTORAL VOTES".asOneTimePublisher()
                this.subhead = "".asOneTimePublisher()
                this.seats = seats
                this.total = totalSeats
                this.majorityLine = {
                    show = showMajority
                    display = { "$it TO WIN" }
                }
            },
            votes = {
                this.header = "POPULAR VOTE".asOneTimePublisher()
                this.subhead = "".asOneTimePublisher()
                this.votes = votes
            },
            title = title,
        )
        screen.setSize(1024, 512)
        compareRendering("SeatsVotesScreen", "CandidatesDual-1", screen)
        assertPublishes(
            screen.altText.map { it(1000) },
            """
                PRESIDENT

                ELECTORAL VOTES
                DONALD TRUMP (GOP): 219/312
                KAMALA HARRIS (DEM): 177/226
                270 TO WIN
                
                POPULAR VOTE
                DONALD TRUMP (GOP): 77,302,169 (49.7%)
                KAMALA HARRIS (DEM): 75,015,834 (48.3%)
                OTHERS: 3,101,635 (2.0%)
            """.trimIndent(),
        )
    }
}
