package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.jupiter.api.Test
import java.awt.Color

class ParliamentVoteFrameTest {

    @Test
    fun ukHouseOfCommonsDivisionFrame() {
        val divisionResult = Publisher(intArrayOf())
        val partyVotes = Publisher(emptyArray<List<Pair<Party, Int>>>())

        val frame = ParliamentVoteFrame(
            "NATIONALITY AND BORDERS BILL".asOneTimePublisher(),
            "THIRD READING".asOneTimePublisher(),
            "House of Commons",
            Color(0x006030),
            arrayOf("AYES", "NOES"),
            divisionResult,
            3,
            partyVotes,
        )
        frame.setSize(500, 250)
        RenderTestUtils.compareRendering("ParliamentVoteFrame", "ukHouse-1", frame)

        divisionResult.submit(intArrayOf(297, 229))
        RenderTestUtils.compareRendering("ParliamentVoteFrame", "ukHouse-2", frame)

        val con = Party("Conservative", "CON", Color.BLUE)
        val lab = Party("Labour", "LAB", Color.RED)
        val snp = Party("Scottish National Party", "SNP", Color.YELLOW)
        val ld = Party("Liberal Democrats", "LD", Color.ORANGE)
        val dup = Party("Democratic Unionist Party", "DUP", Color.ORANGE.darker())
        val pc = Party("Plaid Cymru", "PC", Color.GREEN.darker().darker())
        val alba = Party("Alba Party", "ALBA", Color.BLUE.darker())
        val sdlp = Party("Social Democratic and Labour Party", "SDLP", Color.GREEN)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val apni = Party("Alliance Party of Northern Ireland", "APNI", Color.YELLOW)
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        partyVotes.submit(
            arrayOf(
                listOf(
                    con to 291,
                    lab to 0,
                    snp to 0,
                    ld to 0,
                    dup to 5,
                    pc to 0,
                    alba to 0,
                    sdlp to 0,
                    grn to 0,
                    apni to 0,
                    ind to 1,
                ),
                listOf(
                    con to 0,
                    lab to 166,
                    snp to 39,
                    ld to 11,
                    dup to 0,
                    pc to 3,
                    alba to 2,
                    sdlp to 2,
                    grn to 1,
                    apni to 1,
                    ind to 4,
                ),
            ),
        )
        RenderTestUtils.compareRendering("ParliamentVoteFrame", "ukHouse-3", frame)
    }

    @Test
    fun ukHouseOfCommonsVoiceVote() {
        val resultText: Publisher<String?> = Publisher(null)

        val frame = ParliamentVoteFrame(
            "EXITING THE EUROPEAN UNION (FINANCIAL SERVICES)".asOneTimePublisher(),
            "TO APPROVE DRAFT INSOLVENCY 2 (GROUP SUPERVISION) (AMENDMENT) REGULATIONS 2021".asOneTimePublisher(),
            "House of Commons",
            Color(0x006030),
            arrayOf("AYES", "NOES"),
            intArrayOf().asOneTimePublisher(),
            3,
            emptyArray<List<Pair<Party, Int>>>().asOneTimePublisher(),
            resultText,
        )
        frame.setSize(500, 250)
        RenderTestUtils.compareRendering("ParliamentVoteFrame", "ukHouseNoDivision-1", frame)

        resultText.submit("AYE (NO DIVISION)")
        RenderTestUtils.compareRendering("ParliamentVoteFrame", "ukHouseNoDivision-2", frame)
    }

    @Test
    fun usSenateDivisionFrame() {
        val divisionResult = Publisher(intArrayOf())
        val partyVotes = Publisher(emptyArray<List<Pair<Party, Int>>>())

        val frame = ParliamentVoteFrame(
            "IMPEACHMENT ARTICLE I".asOneTimePublisher(),
            "INCITEMENT OF INSURRECTION".asOneTimePublisher(),
            "US Senate",
            Color(0x900000),
            arrayOf("GUILTY", "NOT GUILTY"),
            divisionResult,
            1,
            partyVotes,
        )
        frame.setSize(500, 250)
        RenderTestUtils.compareRendering("ParliamentVoteFrame", "usSenate-1", frame)

        divisionResult.submit(intArrayOf(57, 43))

        val dem = Party("Democratic", "DEM", Color.BLUE)
        val gop = Party("Republican", "GOP", Color.RED)
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        partyVotes.submit(
            arrayOf(
                listOf(
                    dem to 48,
                    gop to 7,
                    ind to 2,
                ),
                listOf(
                    dem to 0,
                    gop to 43,
                    ind to 0,
                ),
            ),
        )
        RenderTestUtils.compareRendering("ParliamentVoteFrame", "usSenate-2", frame)
    }
}
