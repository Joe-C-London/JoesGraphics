package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.models.general.PartyOrCandidate
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color

class PartyListsScreenTest {

    @Test
    fun testPartyLists() {
        val awp = PartyOrCandidate(Party("Animal Welfare Party", "AWP", Color.PINK.darker()))
        val brex = PartyOrCandidate(Party("Brexit", "BREX", Color.CYAN.darker()))
        val change = PartyOrCandidate(Party("Change UK", "CUK", Color.BLACK))
        val con = PartyOrCandidate(Party("Conservative", "CON", Color.BLUE))
        val green = PartyOrCandidate(Party("Green", "GREEN", Color.GREEN.darker()))
        val lab = PartyOrCandidate(Party("Labour", "LAB", Color.RED))
        val libdem = PartyOrCandidate(Party("Liberal Democrats", "LIBDEM", Color.ORANGE))
        val ukeu = PartyOrCandidate(Party("UK European Union", "UKEU", Color.BLUE.darker()))
        val ukip = PartyOrCandidate(Party("UK Independence Party", "UKIP", Color.MAGENTA.darker()))
        val wep = PartyOrCandidate(Party("Women's Equality Party", "WEP", Color.MAGENTA.darker().darker()))
        val mcdowell = PartyOrCandidate("Claudia Mcdowell")
        val aghaji = PartyOrCandidate("Daze Aghaji")
        val hallam = PartyOrCandidate("Roger Hallam")
        val klu = PartyOrCandidate("Koffi Klu")
        val venzon = PartyOrCandidate("Andrea Venzon")
        val shad = PartyOrCandidate("Mike Shad")
        val lafferty = PartyOrCandidate("Zoe Lafferty")
        val medhurst = PartyOrCandidate("Andrew Medhurst")
        val kirkby = PartyOrCandidate("Alan Kirkby")
        val sowden = PartyOrCandidate("Ian Sowden")
        val muss = PartyOrCandidate("Henry Muss")

        val lists = mapOf(
            awp to listOf("Vanessa Hudson", "Jane Smith", "Sam Morland", "Ranjan Joshi", "Mina Da Rui", "Jonathan Horrian", "Simon Gouldman"),
            brex to listOf("Benyamin Habib", "Lance Graham Forman", "Graham Shore", "Alka Cuthbert", "Jimi Ogunnusi", "Simon Marcus", "Mehrtash A'zami", "Aileen Quinton"),
            con to listOf("Syed Kamall", "Charles Tannock", "Joy Morrisey", "Tim Barnes", "Scott Pattenden", "Attick Rahman", "Kirsty Finlayson", "Luke Parker"),
            change to listOf("Gavin Esler", "Jacek Rostowski", "Carole Tongue", "Annabel Mullin", "Karen Newman", "Nora Mulready", "Jessica Simor", "Haseeb Ur-Rehman"),
            green to listOf("Scott Ainslie", "Gulnar Hasnain", "Shahrar Ali", "Rachel Collinson", "Eleanor Margolies", "Remco von der Stoep", "Kirsten De Keyser", "Peter Underwood"),
            lab to listOf("Claude Moraes", "Seb Dance", "Katy Clark", "Laura Parker", "Murad Qureshi", "Taranjit Kaur Chana", "James Beckles", "Sanchia Alasia"),
            libdem to listOf("Irina Von Wiese", "Dinesh Dhamija", "Luisa Manon Porritt", "Jonathan Fryer", "Hussain Khan", "Helen Cross", "Graham Colley", "Rabina Khan"),
            ukeu to listOf("Pierre Kirk", "Richard Stevens", "Saleyha Ahsan", "Anna Novikova", "Angela Antetomaso", "Richard Boardman"),
            ukip to listOf("Gerard Batten", "Richard Braine", "Peter Muswell", "Fredda Vachha", "Robert Stephenson", "Peter McIlvenna", "John Poynton", "Ronie Johnson"),
            wep to listOf("Catherine Mayer", "Bea Gare", "Nanci Hogan", "Aliyah Dunbar-Hussain", "Hannah Barham-Brown", "Alison Marshall", "Olivia Vincenti", "Leyla Mohan"),
        ).mapValues { (p, c) -> c.map { Candidate(it, p.party) } } +
            listOf(mcdowell, aghaji, hallam, klu, venzon, shad, lafferty, medhurst, kirkby, sowden, muss).associateWith { listOf(Candidate(it.name, it.party)) }

        val showOrder = Publisher(listOf(brex, con, change, green, lab, libdem))

        val numSeats = Publisher(emptyMap<PartyOrCandidate, Int>())

        val panel = PartyListsScreen.of(
            lists = lists.asOneTimePublisher(),
            showOrder = showOrder,
            numSeats = numSeats,
            title = "LONDON".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("PartyListsScreen", "Basic-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            LONDON
            
            BREXIT: 8 NAMES
            CONSERVATIVE: 8 NAMES
            CHANGE UK: 8 NAMES
            GREEN: 8 NAMES
            LABOUR: 8 NAMES
            LIBERAL DEMOCRATS: 8 NAMES
            """.trimIndent(),
        )

        numSeats.submit(mapOf(libdem to 3, lab to 2, brex to 2, green to 1))
        showOrder.submit(listOf(libdem, lab, brex, green))
        compareRendering("PartyListsScreen", "Basic-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            LONDON
            
            LIBERAL DEMOCRATS: 3 OF 8 ELECTED
            LABOUR: 2 OF 8 ELECTED
            BREXIT: 2 OF 8 ELECTED
            GREEN: 1 OF 8 ELECTED
            """.trimIndent(),
        )
    }

    @Test
    fun testPartyListsWithSkips() {
        val con = PartyOrCandidate(Party("Conservative", "CON", Color.BLUE))
        val green = PartyOrCandidate(Party("Green", "GREEN", Color.GREEN.darker()))
        val lab = PartyOrCandidate(Party("Labour", "LAB", Color.RED))
        val libdem = PartyOrCandidate(Party("Liberal Democrats", "LD", Color.ORANGE))
        val snp = PartyOrCandidate(Party("Scottish National Party", "SNP", Color.YELLOW))

        val lists = mapOf(
            con to listOf("Miles Briggs", "Sue Webber", "Jeremy Balfour", "Rebecca Fraser", "Malcolm Offord", "Scott Douglas", "Gordon Lindhurst", "Marie-Clair Munro", "Graham Hutchison", "Iain Whyte", "Callum Laidlaw", "Charles Kennedy", "Damian Timson"),
            green to listOf("Alison Johnstone", "Lorna Slater", "Kate Nevens", "Chas Booth", "Steve Burgess", "Alys Mumford", "Emily Frood", "Ben Parker", "Elaine Taylor", "Bill Wilson", "Evelyn Weston", "Alex Staniforth"),
            lab to listOf("Daniel Johnson", "Sarah Boyack", "Foysol Choudhury", "Maddy Kirkman", "Nick Ward", "Kirsteen Sullivan", "Frederick Hessler", "Stephen Curran"),
            libdem to listOf("Alex Cole-Hamilton", "Fred Mackintosh", "Jill Reilly", "Rebecca Bell", "Sally Pattie", "Fraser Graham", "Caron Lindsay", "Bruce Wilson", "Charles Dundas"),
            snp to listOf("Graham Campbell", "Angus Robertson", "Fiona Hyslop", "Ben Macpherson", "Catriona MacDonald", "Sarah Masson", "Greg McCarra", "Alison Dickie", "Alex Orr", "Douglas Thomson", "Andrew Ewen", "Rob Connell"),
        ).mapValues { (p, c) -> c.map { Candidate(it, p.party) } }
        val showOrder = Publisher(listOf(con, green, lab, libdem, snp))

        val numSeats = Publisher(emptyMap<PartyOrCandidate, Int>())

        val skipCandidates = Publisher(emptyList<Candidate>())

        val panel = PartyListsScreen.of(
            lists = lists.asOneTimePublisher(),
            showOrder = showOrder,
            numSeats = numSeats,
            skipCandidates = skipCandidates,
            title = "LOTHIAN".asOneTimePublisher(),
        )
        panel.setSize(1024, 512)
        compareRendering("PartyListsScreen", "BasicWithSkips-1", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            LOTHIAN

            CONSERVATIVE: 13 NAMES
            GREEN: 12 NAMES
            LABOUR: 8 NAMES
            LIBERAL DEMOCRATS: 9 NAMES
            SCOTTISH NATIONAL PARTY: 12 NAMES
            """.trimIndent(),
        )

        skipCandidates.submit(
            listOf(
                Candidate("Angela Constance", snp.party),
                Candidate("Angus Robertson", snp.party),
                Candidate("Ash Denham", snp.party),
                Candidate("Ben Macpherson", snp.party),
                Candidate("Gordon MacDonald", snp.party),
                Candidate("Daniel Johnson", lab.party),
                Candidate("Alex Cole-Hamilton", libdem.party),
                Candidate("Fiona Hyslop", snp.party),
                Candidate("Colin Beattie", snp.party),
            ),
        )
        compareRendering("PartyListsScreen", "BasicWithSkips-2", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            LOTHIAN

            CONSERVATIVE: 13 NAMES
            GREEN: 12 NAMES
            LABOUR: 8 NAMES, 1 SKIPPED
            LIBERAL DEMOCRATS: 9 NAMES, 1 SKIPPED
            SCOTTISH NATIONAL PARTY: 12 NAMES, 3 SKIPPED
            """.trimIndent(),
        )

        numSeats.submit(mapOf(con to 3, lab to 2, green to 2))
        showOrder.submit(listOf(snp, con, lab, green, libdem))
        compareRendering("PartyListsScreen", "BasicWithSkips-3", panel)
        assertPublishes(
            panel.altText.map { it(1000) },
            """
            LOTHIAN

            SCOTTISH NATIONAL PARTY: 0 OF 12 ELECTED, 3 SKIPPED
            CONSERVATIVE: 3 OF 13 ELECTED
            LABOUR: 2 OF 8 ELECTED, 1 SKIPPED
            GREEN: 2 OF 12 ELECTED
            LIBERAL DEMOCRATS: 0 OF 9 ELECTED, 1 SKIPPED
            """.trimIndent(),
        )
    }
}
