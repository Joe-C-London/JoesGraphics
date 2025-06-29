package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.SimpleVoteViewPanel.Companion.nonPartisanVotes
import com.joecollins.graphics.screens.generic.SimpleVoteViewPanelTest.Companion.peiShapesByDistrict
import com.joecollins.graphics.screens.generic.SingleNonPartisanResultMap.Companion.createSingleNonPartisanResultMap
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.NonPartisanCandidateResult
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class SimpleNonPartisanCandidateVoteView {

    @Test
    fun testNonPartisanVotes() {
        val ndp = NonPartisanCandidate("Billy Cann", color = Color.ORANGE)
        val pc = NonPartisanCandidate("Cory Deagle", color = Color.BLUE)
        val lib = NonPartisanCandidate("Daphne Griffin", color = Color.RED)
        val grn = NonPartisanCandidate("John Allen MacLean", color = Color.GREEN.darker())
        val currentVotes = Publisher(sequenceOf(ndp, pc, lib, grn).associateWith { 0 })
        val previousVotes = Publisher(
            mapOf(
                NonPartisanCandidate("Michael Redmond", color = Color.ORANGE) to 585,
                NonPartisanCandidate("Andrew Daggett", color = Color.BLUE) to 785,
                NonPartisanCandidate("Allen Roach", color = Color.RED) to 1060,
                NonPartisanCandidate("Jason Furness", color = Color.GREEN.darker()) to 106,
            ),
        )
        val pctReporting = Publisher(0.0)
        val title = Publisher("MONTAGUE-KILMUIR")
        val voteHeader = Publisher("2019 RESULT")
        val voteSubhead = Publisher("WAITING FOR RESULTS...")
        val progress = Publisher("0/9 POLLS")
        val changeHeader = Publisher("2015 RESULT")
        val mapHeader = Publisher("CARDIGAN")
        val leader = Publisher<NonPartisanCandidateResult?>(null)
        val winner = Publisher<NonPartisanCandidate?>(null)
        val shapesByDistrict = peiShapesByDistrict()
        val focus = Publisher(shapesByDistrict.keys.filter { it <= 7 })
        val selectedDistrict = Publisher(3)
        val panel = nonPartisanVotes(
            current = {
                votes = currentVotes
                header = voteHeader
                subhead = voteSubhead
                this.winner = winner
                this.pctReporting = pctReporting
                progressLabel = progress
            },
            prev = {
                votes = previousVotes
                header = changeHeader
            },
            map = createSingleNonPartisanResultMap {
                shapes = shapesByDistrict.asOneTimePublisher()
                selectedShape = selectedDistrict
                this.leader = leader
                this.focus = focus
                header = mapHeader
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "NonPartisan-1", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [0/9 POLLS], WAITING FOR RESULTS...
                BILLY CANN: WAITING...
                CORY DEAGLE: WAITING...
                DAPHNE GRIFFIN: WAITING...
                JOHN ALLEN MACLEAN: WAITING...
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 5,
                pc to 47,
                lib to 58,
                grn to 52,
            ),
        )
        progress.submit("1/9 POLLS")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(1.0 / 9)
        leader.submit(NonPartisanCandidateResult.leading(lib))
        compareRendering("SimpleVoteViewPanel", "NonPartisan-2", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [1/9 POLLS], PROJECTION: TOO EARLY TO CALL
                DAPHNE GRIFFIN: 58 (35.8%)
                JOHN ALLEN MACLEAN: 52 (32.1%)
                CORY DEAGLE: 47 (29.0%)
                BILLY CANN: 5 (3.1%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 8,
                pc to 91,
                lib to 100,
                grn to 106,
            ),
        )
        progress.submit("2/9 POLLS")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(2.0 / 9)
        leader.submit(NonPartisanCandidateResult.leading(grn))
        compareRendering("SimpleVoteViewPanel", "NonPartisan-3", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [2/9 POLLS], PROJECTION: TOO EARLY TO CALL
                JOHN ALLEN MACLEAN: 106 (34.8%)
                DAPHNE GRIFFIN: 100 (32.8%)
                CORY DEAGLE: 91 (29.8%)
                BILLY CANN: 8 (2.6%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 18,
                pc to 287,
                lib to 197,
                grn to 243,
            ),
        )
        progress.submit("5/9 POLLS")
        voteSubhead.submit("PROJECTION: TOO EARLY TO CALL")
        pctReporting.submit(5.0 / 9)
        leader.submit(NonPartisanCandidateResult.leading(pc))
        compareRendering("SimpleVoteViewPanel", "NonPartisan-4", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [5/9 POLLS], PROJECTION: TOO EARLY TO CALL
                CORY DEAGLE: 287 (38.5%)
                JOHN ALLEN MACLEAN: 243 (32.6%)
                DAPHNE GRIFFIN: 197 (26.4%)
                BILLY CANN: 18 (2.4%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
            """.trimIndent(),
        )

        currentVotes.submit(
            mapOf(
                ndp to 124,
                pc to 1373,
                lib to 785,
                grn to 675,
            ),
        )
        progress.submit("9/9 POLLS")
        voteSubhead.submit("PROJECTION: DEAGLE WIN")
        pctReporting.submit(9.0 / 9)
        leader.submit(NonPartisanCandidateResult.elected(pc))
        winner.submit(pc)
        compareRendering("SimpleVoteViewPanel", "NonPartisan-5", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [9/9 POLLS], PROJECTION: DEAGLE WIN
                CORY DEAGLE: 1,373 (46.4%) WINNER
                DAPHNE GRIFFIN: 785 (26.5%)
                JOHN ALLEN MACLEAN: 675 (22.8%)
                BILLY CANN: 124 (4.2%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
            """.trimIndent(),
        )

        winner.submit(null)
        compareRendering("SimpleVoteViewPanel", "NonPartisan-6", panel)
        assertPublishes(
            panel.altText,
            """
                MONTAGUE-KILMUIR
                
                2019 RESULT [9/9 POLLS], PROJECTION: DEAGLE WIN
                CORY DEAGLE: 1,373 (46.4%)
                DAPHNE GRIFFIN: 785 (26.5%)
                JOHN ALLEN MACLEAN: 675 (22.8%)
                BILLY CANN: 124 (4.2%)
                
                2015 RESULT
                ROACH: 41.8%
                DAGGETT: 31.0%
                REDMOND: 23.1%
                FURNESS: 4.2%
            """.trimIndent(),
        )
    }

    @Test
    fun testNonPartisanVoteMisc() {
        val title = Publisher("IQALUIT-TASILUK")
        val curr = Publisher(
            mapOf(
                NonPartisanCandidate("James T. Arreak") to 133,
                NonPartisanCandidate("George Hicks", description = "Incumbent MLA") to 265,
                NonPartisanCandidate("Jonathan Chul-Hee Min Park") to 41,
                NonPartisanCandidate("Michael Salomonie") to 81,
            ),
        )
        val prev = Publisher(
            mapOf(
                NonPartisanCandidate("George Hicks") to 449,
                NonPartisanCandidate("Jacopoosie Peter") to 121,
            ),
        )
        val panel = nonPartisanVotes(
            current = {
                votes = curr
                header = "2021 RESULT".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev
                header = "2017 RESULT".asOneTimePublisher()
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "NonPartisanMisc-1", panel)
        assertPublishes(
            panel.altText,
            """
                IQALUIT-TASILUK
                
                2021 RESULT
                GEORGE HICKS (INCUMBENT MLA): 265 (51.0%)
                JAMES T. ARREAK: 133 (25.6%)
                MICHAEL SALOMONIE: 81 (15.6%)
                JONATHAN CHUL-HEE MIN PARK: 41 (7.9%)
                
                2017 RESULT
                HICKS: 78.8%
                PETER: 21.2%
            """.trimIndent(),
        )

        title.submit("IQALUIT-NIAQUNNGUU")
        curr.submit(
            mapOf(
                NonPartisanCandidate("P.J. Akeeagok") to 404,
                NonPartisanCandidate("Noah Papatsie") to 54,
                NonPartisanCandidate("Dinos Tikivik") to 21,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Pat Angnakak") to 231,
                NonPartisanCandidate("Franco Buscemi") to 196,
                NonPartisanCandidate("Anne Crawford") to 134,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanMisc-2", panel)
        assertPublishes(
            panel.altText,
            """
                IQALUIT-NIAQUNNGUU

                2021 RESULT
                P.J. AKEEAGOK: 404 (84.3%)
                NOAH PAPATSIE: 54 (11.3%)
                DINOS TIKIVIK: 21 (4.4%)
                
                2017 RESULT
                ANGNAKAK: 41.2%
                BUSCEMI: 34.9%
                CRAWFORD: 23.9%
            """.trimIndent(),
        )

        title.submit("ARVIAT SOUTH")
        curr.submit(
            mapOf(
                NonPartisanCandidate("Joe Savikataaq", description = "Incumbent MLA") to 0,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Jason Gibbons") to 234,
                NonPartisanCandidate("Joe Savikataaq") to 280,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanMisc-3", panel)
        assertPublishes(
            panel.altText,
            """
                ARVIAT SOUTH

                2021 RESULT
                JOE SAVIKATAAQ (INCUMBENT MLA): UNCONTESTED
                
                2017 RESULT
                SAVIKATAAQ: 54.5%
                GIBBONS: 45.5%
            """.trimIndent(),
        )

        title.submit("KUGLUKTUK")
        curr.submit(
            mapOf(
                NonPartisanCandidate("Bobby Anavilok") to 170,
                NonPartisanCandidate("Angele Kuliktana") to 77,
                NonPartisanCandidate("Genevieve Nivingalok") to 51,
                NonPartisanCandidate("Calvin Aivgak Pedersen", description = "Incumbent MLA") to 140,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Mila Adjukak Kamingoak") to 0,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanMisc-4", panel)
        assertPublishes(
            panel.altText,
            """
                KUGLUKTUK

                2021 RESULT
                BOBBY ANAVILOK: 170 (38.8%)
                CALVIN AIVGAK PEDERSEN (INCUMBENT MLA): 140 (32.0%)
                ANGELE KULIKTANA: 77 (17.6%)
                GENEVIEVE NIVINGALOK: 51 (11.6%)
                
                2017 RESULT
                KAMINGOAK: UNCONTESTED
            """.trimIndent(),
        )
    }

    @Test
    fun testNonPartisanVoteIncumbentMarker() {
        val title = Publisher("IQALUIT-TASILUK")
        val curr = Publisher(
            mapOf(
                NonPartisanCandidate("James T. Arreak") to 133,
                NonPartisanCandidate("George Hicks", incumbent = true) to 265,
                NonPartisanCandidate("Jonathan Chul-Hee Min Park") to 41,
                NonPartisanCandidate("Michael Salomonie") to 81,
            ),
        )
        val prev = Publisher(
            mapOf(
                NonPartisanCandidate("George Hicks") to 449,
                NonPartisanCandidate("Jacopoosie Peter") to 121,
            ),
        )
        val panel = nonPartisanVotes(
            current = {
                votes = curr
                header = "2021 RESULT".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
                incumbentMarker = "MLA"
            },
            prev = {
                votes = prev
                header = "2017 RESULT".asOneTimePublisher()
            },
            title = title,
        )
        panel.setSize(1024, 512)
        compareRendering("SimpleVoteViewPanel", "NonPartisanIncumbent-1", panel)
        assertPublishes(
            panel.altText,
            """
                IQALUIT-TASILUK
                
                2021 RESULT
                GEORGE HICKS [MLA]: 265 (51.0%)
                JAMES T. ARREAK: 133 (25.6%)
                MICHAEL SALOMONIE: 81 (15.6%)
                JONATHAN CHUL-HEE MIN PARK: 41 (7.9%)
                
                2017 RESULT
                HICKS: 78.8%
                PETER: 21.2%
            """.trimIndent(),
        )

        title.submit("IQALUIT-NIAQUNNGUU")
        curr.submit(
            mapOf(
                NonPartisanCandidate("P.J. Akeeagok") to 404,
                NonPartisanCandidate("Noah Papatsie") to 54,
                NonPartisanCandidate("Dinos Tikivik") to 21,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Pat Angnakak") to 231,
                NonPartisanCandidate("Franco Buscemi") to 196,
                NonPartisanCandidate("Anne Crawford") to 134,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanIncumbent-2", panel)
        assertPublishes(
            panel.altText,
            """
                IQALUIT-NIAQUNNGUU

                2021 RESULT
                P.J. AKEEAGOK: 404 (84.3%)
                NOAH PAPATSIE: 54 (11.3%)
                DINOS TIKIVIK: 21 (4.4%)
                
                2017 RESULT
                ANGNAKAK: 41.2%
                BUSCEMI: 34.9%
                CRAWFORD: 23.9%
            """.trimIndent(),
        )

        title.submit("ARVIAT SOUTH")
        curr.submit(
            mapOf(
                NonPartisanCandidate("Joe Savikataaq", incumbent = true) to 0,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Jason Gibbons") to 234,
                NonPartisanCandidate("Joe Savikataaq") to 280,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanIncumbent-3", panel)
        assertPublishes(
            panel.altText,
            """
                ARVIAT SOUTH

                2021 RESULT
                JOE SAVIKATAAQ [MLA]: UNCONTESTED
                
                2017 RESULT
                SAVIKATAAQ: 54.5%
                GIBBONS: 45.5%
            """.trimIndent(),
        )

        title.submit("KUGLUKTUK")
        curr.submit(
            mapOf(
                NonPartisanCandidate("Bobby Anavilok") to 170,
                NonPartisanCandidate("Angele Kuliktana") to 77,
                NonPartisanCandidate("Genevieve Nivingalok") to 51,
                NonPartisanCandidate("Calvin Aivgak Pedersen", incumbent = true) to 140,
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Mila Adjukak Kamingoak") to 0,
            ),
        )
        compareRendering("SimpleVoteViewPanel", "NonPartisanIncumbent-4", panel)
        assertPublishes(
            panel.altText,
            """
                KUGLUKTUK

                2021 RESULT
                BOBBY ANAVILOK: 170 (38.8%)
                CALVIN AIVGAK PEDERSEN [MLA]: 140 (32.0%)
                ANGELE KULIKTANA: 77 (17.6%)
                GENEVIEVE NIVINGALOK: 51 (11.6%)
                
                2017 RESULT
                KAMINGOAK: UNCONTESTED
            """.trimIndent(),
        )
    }
}
