package com.joecollins.graphics.screens.generic

import com.joecollins.graphics.screens.generic.SingleNoResultMap.Companion.createSingleNoResultMap
import com.joecollins.graphics.utils.PublisherTestUtils.assertPublishes
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.NonPartisanCandidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.map
import org.junit.jupiter.api.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Shape

class CandidateListingScreenTest {

    @Test
    fun testCandidatesAlone() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val candidates = listOf(
            Candidate("Tommy Kickham", lib),
            Candidate("Colin LaVie", pc, true),
            Candidate("Boyd Leard", grn),
        )
        val screen = CandidateListingScreen.of(
            candidates = {
                list = candidates.asOneTimePublisher()
                header = "CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            incumbentMarker = "MLA",
            title = "SOURIS-ELMIRA".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "CandidatesOnly", screen)
        assertPublishes(
            screen.altText,
            """
            SOURIS-ELMIRA
            
            CANDIDATES
            TOMMY KICKHAM (LIB)
            COLIN LAVIE [MLA] (PC)
            BOYD LEARD (GRN)
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesWithPrev() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val candidates = listOf(
            Candidate("Tommy Kickham", lib),
            Candidate("Colin LaVie", pc, true),
            Candidate("Boyd Leard", grn),
        )
        val prev = mapOf(
            lib to 951,
            pc to 1179,
            ndp to 528,
        )
        val screen = CandidateListingScreen.of(
            candidates = {
                list = candidates.asOneTimePublisher()
                header = "CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev.asOneTimePublisher()
                header = "2015 RESULT".asOneTimePublisher()
                subhead = null.asOneTimePublisher()
            },
            incumbentMarker = "MLA",
            title = "SOURIS-ELMIRA".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "CandidatesWithPrev", screen)
        assertPublishes(
            screen.altText,
            """
            SOURIS-ELMIRA
            
            CANDIDATES
            TOMMY KICKHAM (LIB)
            COLIN LAVIE [MLA] (PC)
            BOYD LEARD (GRN)
            
            2015 RESULT
            PC: 44.4%
            LIB: 35.8%
            NDP: 19.9%
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesWithPrevAndMap() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val candidates = listOf(
            Candidate("Tommy Kickham", lib),
            Candidate("Colin LaVie", pc, true),
            Candidate("Boyd Leard", grn),
        )
        val prev = mapOf(
            lib to 951,
            pc to 1179,
            ndp to 528,
        )
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates = {
                list = candidates.asOneTimePublisher()
                header = "CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev.asOneTimePublisher()
                header = "2015 RESULT".asOneTimePublisher()
            },
            map = createSingleNoResultMap {
                this.shapes = shapes.asOneTimePublisher()
                selectedShape = 1.asOneTimePublisher()
                focus = listOf(1, 2, 3, 4, 5, 6, 7).asOneTimePublisher()
                additionalHighlight = null
                header = "CARDIGAN".asOneTimePublisher()
            },
            incumbentMarker = "MLA",
            title = "SOURIS-ELMIRA".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "CandidatesWithPrevAndMap", screen)
        assertPublishes(
            screen.altText,
            """
            SOURIS-ELMIRA
            
            CANDIDATES
            TOMMY KICKHAM (LIB)
            COLIN LAVIE [MLA] (PC)
            BOYD LEARD (GRN)
            
            2015 RESULT
            PC: 44.4%
            LIB: 35.8%
            NDP: 19.9%
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesWithPrevOthersAndMap() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val ind = Party("Independent", "IND", Party.OTHERS.color)
        val candidates = listOf(
            Candidate("Angus Birt", pc),
            Candidate("Bush Dumville", ind),
            Candidate("Gavin Hall", grn),
            Candidate("Gord McNeilly", lib),
            Candidate("Janis Newman", ndp),
        )
        val prev = mapOf(
            lib to 1040,
            ndp to 931,
            Party.OTHERS to 821 + 244,
        )
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates = {
                list = candidates.asOneTimePublisher()
                header = "CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev.asOneTimePublisher()
                header = "2015 RESULT".asOneTimePublisher()
            },
            map = createSingleNoResultMap {
                this.shapes = shapes.asOneTimePublisher()
                selectedShape = 14.asOneTimePublisher()
                focus = listOf(9, 10, 11, 12, 13, 14).asOneTimePublisher()
                header = "CHARLOTTETOWN".asOneTimePublisher()
            },
            incumbentMarker = "MLA",
            title = "CHARLOTTETOWN-WEST ROYALTY".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "CandidatesWithPrevOtherAndMap", screen)
        assertPublishes(
            screen.altText,
            """
            CHARLOTTETOWN-WEST ROYALTY
            
            CANDIDATES
            ANGUS BIRT (PC)
            BUSH DUMVILLE (IND)
            GAVIN HALL (GRN)
            GORD MCNEILLY (LIB)
            JANIS NEWMAN (NDP)
            
            2015 RESULT
            LIB: 34.3%
            NDP: 30.7%
            OTH: 35.1%
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesWithSecondaryPrevAndMap() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val candidates = listOf(
            Candidate("Tommy Kickham", lib),
            Candidate("Colin LaVie", pc, true),
            Candidate("Boyd Leard", grn),
        )
        val prev = mapOf(
            lib to 951,
            pc to 1179,
            ndp to 528,
        )
        val secondaryPrev = mapOf(
            lib to 8016,
            pc to 9444,
            grn to 1144,
            ndp to 2404,
        )
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates = {
                list = candidates.asOneTimePublisher()
                header = "CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev.asOneTimePublisher()
                header = "2015 RESULT".asOneTimePublisher()
            },
            secondaryPrev = {
                votes = secondaryPrev.asOneTimePublisher()
                header = "2015 REGIONAL RESULT".asOneTimePublisher()
            },
            map = createSingleNoResultMap {
                this.shapes = shapes.asOneTimePublisher()
                selectedShape = 1.asOneTimePublisher()
                focus = listOf(1, 2, 3, 4, 5, 6, 7).asOneTimePublisher()
                header = "CARDIGAN".asOneTimePublisher()
            },
            incumbentMarker = "MLA",
            title = "SOURIS-ELMIRA".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "CandidatesWithSecondaryPrevAndMap", screen)
        assertPublishes(
            screen.altText,
            """
            SOURIS-ELMIRA
            
            CANDIDATES
            TOMMY KICKHAM (LIB)
            COLIN LAVIE [MLA] (PC)
            BOYD LEARD (GRN)
            
            2015 RESULT
            PC: 44.4%
            LIB: 35.8%
            NDP: 19.9%
            
            2015 REGIONAL RESULT
            PC: 45.0%
            LIB: 38.2%
            NDP: 11.4%
            GRN: 5.4%
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesWithSecondaryPrev() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val candidates = listOf(
            Candidate("Tommy Kickham", lib),
            Candidate("Colin LaVie", pc, true),
            Candidate("Boyd Leard", grn),
        )
        val prev = mapOf(
            lib to 951,
            pc to 1179,
            ndp to 528,
        )
        val secondaryPrev = mapOf(
            lib to 8016,
            pc to 9444,
            grn to 1144,
            ndp to 2404,
        )
        val screen = CandidateListingScreen.of(
            candidates = {
                list = candidates.asOneTimePublisher()
                header = "CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev.asOneTimePublisher()
                header = "2015 RESULT".asOneTimePublisher()
            },
            secondaryPrev = {
                votes = secondaryPrev.asOneTimePublisher()
                header = "2015 REGIONAL RESULT".asOneTimePublisher()
            },
            incumbentMarker = "MLA",
            title = "SOURIS-ELMIRA".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "CandidatesWithSecondaryPrev", screen)
        assertPublishes(
            screen.altText,
            """
            SOURIS-ELMIRA
            
            CANDIDATES
            TOMMY KICKHAM (LIB)
            COLIN LAVIE [MLA] (PC)
            BOYD LEARD (GRN)
            
            2015 RESULT
            PC: 44.4%
            LIB: 35.8%
            NDP: 19.9%
            
            2015 REGIONAL RESULT
            PC: 45.0%
            LIB: 38.2%
            NDP: 11.4%
            GRN: 5.4%
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesScreenUpdating() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val candidates = Publisher(
            listOf(
                Candidate("Kevin Doyle", lib),
                Candidate("Susan Hartley", grn),
                Candidate("Edith Perry", ndp),
                Candidate("Steven Myers", pc, true),
            ),
        )
        val prev = Publisher(
            mapOf(
                lib to 1170,
                pc to 1448,
                grn to 145,
                ndp to 256,
            ),
        )
        val secondaryPrev = Publisher(
            mapOf(
                lib to 8016,
                pc to 9444,
                grn to 1144,
                ndp to 2404,
            ),
        )
        val region = Publisher("CARDIGAN")
        val districtName = Publisher("GEORGETOWN-POWNAL")
        val districtNum = Publisher(2)
        val focus = Publisher(listOf(1, 2, 3, 4, 5, 6, 7))
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates = {
                list = candidates
                header = "CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev
                header = "2015 RESULT".asOneTimePublisher()
            },
            secondaryPrev = {
                votes = secondaryPrev
                header = "2015 REGIONAL RESULT".asOneTimePublisher()
            },
            map = createSingleNoResultMap {
                this.shapes = shapes.asOneTimePublisher()
                selectedShape = districtNum
                this.focus = focus
                header = region
            },
            incumbentMarker = "MLA",
            title = districtName,
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "CandidatesUpdating-1", screen)
        assertPublishes(
            screen.altText,
            """
            GEORGETOWN-POWNAL
            
            CANDIDATES
            KEVIN DOYLE (LIB)
            SUSAN HARTLEY (GRN)
            EDITH PERRY (NDP)
            STEVEN MYERS [MLA] (PC)
            
            2015 RESULT
            PC: 48.0%
            LIB: 38.8%
            NDP: 8.5%
            GRN: 4.8%
            
            2015 REGIONAL RESULT
            PC: 45.0%
            LIB: 38.2%
            NDP: 11.4%
            GRN: 5.4%
            """.trimIndent(),
        )

        candidates.submit(
            listOf(
                Candidate("Sarah Donald", grn),
                Candidate("Wade MacLauchlan", lib, true),
                Candidate("Bloyce Thompson", pc),
                Candidate("Marian White", ndp),
            ),
        )
        prev.submit(mapOf(lib to 1938, pc to 1338, grn to 347, ndp to 442))
        secondaryPrev.submit(mapOf(lib to 7767, pc to 8169, grn to 4011, ndp to 1427))
        region.submit("MALPEQUE")
        districtName.submit("STANHOME-MARSFIELD")
        districtNum.submit(8)
        focus.submit(listOf(8, 15, 16, 17, 18, 19, 20))
        compareRendering("CandidateListingScreen", "CandidatesUpdating-2", screen)
        assertPublishes(
            screen.altText,
            """
            STANHOME-MARSFIELD
            
            CANDIDATES
            SARAH DONALD (GRN)
            WADE MACLAUCHLAN [MLA] (LIB)
            BLOYCE THOMPSON (PC)
            MARIAN WHITE (NDP)
            
            2015 RESULT
            LIB: 47.7%
            PC: 32.9%
            NDP: 10.9%
            GRN: 8.5%
            
            2015 REGIONAL RESULT
            PC: 38.2%
            LIB: 36.3%
            GRN: 18.8%
            NDP: 6.7%
            """.trimIndent(),
        )
    }

    @Test
    fun testCandidatesInTwoColumns() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val con = Party("Conservative", "CON", Color.BLUE)
        val ndp = Party("New Democratic Party", "NDP", Color.ORANGE)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val ppc = Party("People's Party", "PPC", Color.MAGENTA.darker())
        val rhino = Party("Rhinoceros", "RHINO", Color.PINK)
        val ind = Party("Independent", "IND", Party.OTHERS.color)

        val candidatesPublisher = Publisher(
            listOf(
                Candidate("Michael Ras", con),
                Candidate("Elizabeth Robertson", grn),
                Candidate("Vahid Seyfaie", ppc),
                Candidate("Sven Spengemann", lib, true),
                Candidate("Kayleigh Tahk", rhino),
                Candidate("Sarah Walji", ndp),
            ),
        )
        val headerPublisher = Publisher("2021 GENERAL ELECTION CANDIDATES")
        val screen = CandidateListingScreen.of(
            candidates = {
                list = candidatesPublisher
                header = headerPublisher
                subhead = "".asOneTimePublisher()
            },
            showTwoColumns = candidatesPublisher.map { it.size > 20 },
            title = "MISSISSAUGA—LAKESHORE".asOneTimePublisher(),
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "TwoColumns-1", screen)
        assertPublishes(
            screen.altText,
            """
            MISSISSAUGA—LAKESHORE
            
            2021 GENERAL ELECTION CANDIDATES
            MICHAEL RAS (CON)
            ELIZABETH ROBERTSON (GRN)
            VAHID SEYFAIE (PPC)
            SVEN SPENGEMANN (LIB)
            KAYLEIGH TAHK (RHINO)
            SARAH WALJI (NDP)
            """.trimIndent(),
        )

        candidatesPublisher.submit(
            listOf(
                Candidate("Khaled Al-Sudani", ppc),
                Candidate("Mélodie Anderson", ind),
                Candidate("Myriam Beaulieu", ind),
                Candidate("Line Bélanger", ind),
                Candidate("Mylène Bonneau", ind),
                Candidate("Jean-Denis Parent Boudreault", ind),
                Candidate("Jevin David Carroll", ind),
                Candidate("Sean Carson", ind),
                Candidate("Ron Chhinzer", con),
                Candidate("Sébastien CoRhino", rhino),
                Candidate("Charles Currie", ind),
                Candidate("Stephen Davis", ind),
                Candidate("Mark Dejewski", ind),
                Candidate("Ysack Dupont", ind),
                Candidate("Donovan Eckstrom", ind),
                Candidate("Alexandra Engering", ind),
                Candidate("Daniel Gagnon", ind),
                Candidate("Donald Gagnon", ind),
                Candidate("Kerri Hildebrandt", ind),
                Candidate("Peter House", ind),
                Candidate("Martin Acetaria Caesar Jubinville", ind),
                Candidate("Samuel Jubinville", ind),
                Candidate("Mary Kidnew", grn),
                Candidate("Julia Kole", ndp),
                Candidate("Alain Lamontagne", ind),
                Candidate("Marie-Hélène LeBel", ind),
                Candidate("Conrad Lukawski", ind),
                Candidate("Spencer Rocchi", ind),
                Candidate("Eliana Rosenblum", ind),
                Candidate("Julian Selody", ind),
                Candidate("Roger Sherwood", ind),
                Candidate("Adam Smith", ind),
                Candidate("Charles Sousa", lib),
                Candidate("Julie St-Amand", ind),
                Candidate("Pascal St-Amand", ind),
                Candidate("Patrick Strzalkowski", ind),
                Candidate("Tomas Szuchewycz", ind),
                Candidate("Ben Teichman", ind),
                Candidate("John The Engineer Turmel", ind),
                Candidate("Darcy Justin Vanderwater", ind),
            ),
        )
        headerPublisher.submit("2022 BY-ELECTION CANDIDATES")
        compareRendering("CandidateListingScreen", "TwoColumns-2", screen)
        assertPublishes(
            screen.altText,
            """
            MISSISSAUGA—LAKESHORE
            
            2022 BY-ELECTION CANDIDATES
            KHALED AL-SUDANI (PPC)
            MÉLODIE ANDERSON (IND)
            MYRIAM BEAULIEU (IND)
            LINE BÉLANGER (IND)
            MYLÈNE BONNEAU (IND)
            JEAN-DENIS PARENT BOUDREAULT (IND)
            JEVIN DAVID CARROLL (IND)
            SEAN CARSON (IND)
            RON CHHINZER (CON)
            SÉBASTIEN CORHINO (RHINO)
            CHARLES CURRIE (IND)
            STEPHEN DAVIS (IND)
            MARK DEJEWSKI (IND)
            YSACK DUPONT (IND)
            DONOVAN ECKSTROM (IND)
            ALEXANDRA ENGERING (IND)
            DANIEL GAGNON (IND)
            DONALD GAGNON (IND)
            KERRI HILDEBRANDT (IND)
            PETER HOUSE (IND)
            MARTIN ACETARIA CAESAR JUBINVILLE (IND)
            SAMUEL JUBINVILLE (IND)
            MARY KIDNEW (GRN)
            JULIA KOLE (NDP)
            ALAIN LAMONTAGNE (IND)
            MARIE-HÉLÈNE LEBEL (IND)
            CONRAD LUKAWSKI (IND)
            SPENCER ROCCHI (IND)
            ELIANA ROSENBLUM (IND)
            JULIAN SELODY (IND)
            ROGER SHERWOOD (IND)
            ADAM SMITH (IND)
            CHARLES SOUSA (LIB)
            JULIE ST-AMAND (IND)
            PASCAL ST-AMAND (IND)
            PATRICK STRZALKOWSKI (IND)
            TOMAS SZUCHEWYCZ (IND)
            BEN TEICHMAN (IND)
            JOHN THE ENGINEER TURMEL (IND)
            DARCY JUSTIN VANDERWATER (IND)
            """.trimIndent(),
        )
    }

    @Test
    fun testNonPartisanCandidatesList() {
        val title = Publisher("IQALUIT-TASILUK")
        val candidates = Publisher(
            listOf(
                NonPartisanCandidate("James T. Arreak"),
                NonPartisanCandidate("George Hicks", description = "Incumbent MLA"),
                NonPartisanCandidate("Jonathan Chul-Hee Min Park"),
                NonPartisanCandidate("Michael Salomonie"),
            ),
        )
        val prev = Publisher(
            mapOf(
                NonPartisanCandidate("George Hicks") to 449,
                NonPartisanCandidate("Jacopoosie Peter") to 121,
            ),
        )
        val screen = CandidateListingScreen.ofNonPartisan(
            candidates = {
                list = candidates
                header = "2021 CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev
                header = "2017 RESULT".asOneTimePublisher()
            },
            title = title,
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "NonPartisan-1", screen)
        assertPublishes(
            screen.altText,
            """
            IQALUIT-TASILUK

            2021 CANDIDATES
            JAMES T. ARREAK
            GEORGE HICKS (INCUMBENT MLA)
            JONATHAN CHUL-HEE MIN PARK
            MICHAEL SALOMONIE
            
            2017 RESULT
            HICKS: 78.8%
            PETER: 21.2%
            """.trimIndent(),
        )

        title.submit("KUGLUKTUK")
        candidates.submit(
            listOf(
                NonPartisanCandidate("Bobby Anavilok"),
                NonPartisanCandidate("Angele Kuliktana"),
                NonPartisanCandidate("Genevieve Nivingalok"),
                NonPartisanCandidate("Calvin Aivgak Pedersen", description = "Incumbent MLA"),
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Mila Adjukak Kamingoak") to 0,
            ),
        )
        compareRendering("CandidateListingScreen", "NonPartisan-2", screen)
        assertPublishes(
            screen.altText,
            """
            KUGLUKTUK

            2021 CANDIDATES
            BOBBY ANAVILOK
            ANGELE KULIKTANA
            GENEVIEVE NIVINGALOK
            CALVIN AIVGAK PEDERSEN (INCUMBENT MLA)
            
            2017 RESULT
            KAMINGOAK: UNCONTESTED
            """.trimIndent(),
        )
    }

    @Test
    fun testNonPartisanCandidatesListIncumbents() {
        val title = Publisher("IQALUIT-TASILUK")
        val candidates = Publisher(
            listOf(
                NonPartisanCandidate("James T. Arreak"),
                NonPartisanCandidate("George Hicks", incumbent = true),
                NonPartisanCandidate("Jonathan Chul-Hee Min Park"),
                NonPartisanCandidate("Michael Salomonie"),
            ),
        )
        val prev = Publisher(
            mapOf(
                NonPartisanCandidate("George Hicks") to 449,
                NonPartisanCandidate("Jacopoosie Peter") to 121,
            ),
        )
        val screen = CandidateListingScreen.ofNonPartisan(
            candidates = {
                list = candidates
                header = "2021 CANDIDATES".asOneTimePublisher()
                subhead = "".asOneTimePublisher()
            },
            prev = {
                votes = prev
                header = "2017 RESULT".asOneTimePublisher()
            },
            incumbentMarker = "MLA",
            title = title,
        )
        screen.size = Dimension(1024, 512)
        compareRendering("CandidateListingScreen", "NonPartisanIncumbent-1", screen)
        assertPublishes(
            screen.altText,
            """
            IQALUIT-TASILUK

            2021 CANDIDATES
            JAMES T. ARREAK
            GEORGE HICKS [MLA]
            JONATHAN CHUL-HEE MIN PARK
            MICHAEL SALOMONIE
            
            2017 RESULT
            HICKS: 78.8%
            PETER: 21.2%
            """.trimIndent(),
        )

        title.submit("KUGLUKTUK")
        candidates.submit(
            listOf(
                NonPartisanCandidate("Bobby Anavilok"),
                NonPartisanCandidate("Angele Kuliktana"),
                NonPartisanCandidate("Genevieve Nivingalok"),
                NonPartisanCandidate("Calvin Aivgak Pedersen", incumbent = true),
            ),
        )
        prev.submit(
            mapOf(
                NonPartisanCandidate("Mila Adjukak Kamingoak") to 0,
            ),
        )
        compareRendering("CandidateListingScreen", "NonPartisanIncumbent-2", screen)
        assertPublishes(
            screen.altText,
            """
            KUGLUKTUK

            2021 CANDIDATES
            BOBBY ANAVILOK
            ANGELE KULIKTANA
            GENEVIEVE NIVINGALOK
            CALVIN AIVGAK PEDERSEN [MLA]
            
            2017 RESULT
            KAMINGOAK: UNCONTESTED
            """.trimIndent(),
        )
    }

    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = CandidateListingScreenTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return ShapefileReader.readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
