package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import org.junit.Test
import java.awt.Color
import java.awt.Dimension
import java.awt.Shape
import java.io.IOException

class CandidateListingScreenTest {

    @Test
    fun testCandidatesAlone() {
        val lib = Party("Liberal", "LIB", Color.RED)
        val grn = Party("Green", "GRN", Color.GREEN.darker())
        val pc = Party("Progressive Conservative", "PC", Color.BLUE)
        val candidates = listOf(
            Candidate("Tommy Kickham", lib),
            Candidate("Colin LaVie", pc, true),
            Candidate("Boyd Leard", grn)
        )
        val screen = CandidateListingScreen.of(
            candidates.asOneTimePublisher(),
            "CANDIDATES".asOneTimePublisher(),
            "".asOneTimePublisher(),
            "[MLA]"
        )
            .build(Binding.fixedBinding("SOURIS-ELMIRA"))
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesOnly", screen)
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
            Candidate("Boyd Leard", grn)
        )
        val prev = mapOf(
            lib to 951,
            pc to 1179,
            ndp to 528
        )
        val screen = CandidateListingScreen.of(
            candidates.asOneTimePublisher(),
            "CANDIDATES".asOneTimePublisher(),
            "".asOneTimePublisher(),
            "[MLA]"
        )
            .withPrev(
                prev.asOneTimePublisher(),
                "2015 RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .build(Binding.fixedBinding("SOURIS-ELMIRA"))
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesWithPrev", screen)
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
            Candidate("Boyd Leard", grn)
        )
        val prev = mapOf(
            lib to 951,
            pc to 1179,
            ndp to 528
        )
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates.asOneTimePublisher(),
            "CANDIDATES".asOneTimePublisher(),
            "".asOneTimePublisher(),
            "[MLA]"
        )
            .withMap(
                shapes.asOneTimePublisher(),
                1.asOneTimePublisher(),
                listOf(1, 2, 3, 4, 5, 6, 7).asOneTimePublisher(),
                "CARDIGAN".asOneTimePublisher()
            )
            .withPrev(
                prev.asOneTimePublisher(),
                "2015 RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .build(Binding.fixedBinding("SOURIS-ELMIRA"))
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesWithPrevAndMap", screen)
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
            Candidate("Janis Newman", ndp)
        )
        val prev = mapOf(
            lib to 1040,
            ndp to 931,
            Party.OTHERS to 821 + 244
        )
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates.asOneTimePublisher(),
            "CANDIDATES".asOneTimePublisher(),
            "".asOneTimePublisher(),
            "[MLA]"
        )
            .withMap(
                shapes.asOneTimePublisher(),
                14.asOneTimePublisher(),
                listOf(9, 10, 11, 12, 13, 14).asOneTimePublisher(),
                "CHARLOTTETOWN".asOneTimePublisher()
            )
            .withPrev(
                prev.asOneTimePublisher(),
                "2015 RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .build(Binding.fixedBinding("CHARLOTTETOWN-WEST ROYALTY"))
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesWithPrevOtherAndMap", screen)
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
            Candidate("Boyd Leard", grn)
        )
        val prev = mapOf(
            lib to 951,
            pc to 1179,
            ndp to 528
        )
        val secondaryPrev = mapOf(
            lib to 8016,
            pc to 9444,
            grn to 1144,
            ndp to 2404
        )
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates.asOneTimePublisher(),
            "CANDIDATES".asOneTimePublisher(),
            "".asOneTimePublisher(),
            "[MLA]"
        )
            .withMap(
                shapes.asOneTimePublisher(),
                1.asOneTimePublisher(),
                listOf(1, 2, 3, 4, 5, 6, 7).asOneTimePublisher(),
                "CARDIGAN".asOneTimePublisher()
            )
            .withPrev(
                prev.asOneTimePublisher(),
                "2015 RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .withSecondaryPrev(
                secondaryPrev.asOneTimePublisher(),
                "2015 REGIONAL RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .build(Binding.fixedBinding("SOURIS-ELMIRA"))
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesWithSecondaryPrevAndMap", screen)
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
            Candidate("Boyd Leard", grn)
        )
        val prev = mapOf(
            lib to 951,
            pc to 1179,
            ndp to 528
        )
        val secondaryPrev = mapOf(
            lib to 8016,
            pc to 9444,
            grn to 1144,
            ndp to 2404
        )
        val screen = CandidateListingScreen.of(
            candidates.asOneTimePublisher(),
            "CANDIDATES".asOneTimePublisher(),
            "".asOneTimePublisher(),
            "[MLA]"
        )
            .withPrev(
                prev.asOneTimePublisher(),
                "2015 RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .withSecondaryPrev(
                secondaryPrev.asOneTimePublisher(),
                "2015 REGIONAL RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .build(Binding.fixedBinding("SOURIS-ELMIRA"))
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesWithSecondaryPrev", screen)
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
                Candidate("Steven Myers", pc, true)
            )
        )
        val prev = Publisher(
            mapOf(
                lib to 1170,
                pc to 1448,
                grn to 145,
                ndp to 256
            )
        )
        val secondaryPrev = Publisher(
            mapOf(
                lib to 8016,
                pc to 9444,
                grn to 1144,
                ndp to 2404
            )
        )
        val region = Publisher("CARDIGAN")
        val districtName = BindableWrapper("GEORGETOWN-POWNAL")
        val districtNum = Publisher(2)
        val focus = Publisher(listOf(1, 2, 3, 4, 5, 6, 7))
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates,
            "CANDIDATES".asOneTimePublisher(),
            "".asOneTimePublisher(),
            "[MLA]"
        )
            .withMap(
                shapes.asOneTimePublisher(),
                districtNum,
                focus,
                region
            )
            .withPrev(
                prev,
                "2015 RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .withSecondaryPrev(
                secondaryPrev,
                "2015 REGIONAL RESULT".asOneTimePublisher(),
                null.asOneTimePublisher()
            )
            .build(districtName.binding)
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesUpdating-1", screen)

        candidates.submit(
            listOf(
                Candidate("Sarah Donald", grn),
                Candidate("Wade MacLauchlan", lib, true),
                Candidate("Bloyce Thompson", pc),
                Candidate("Marian White", ndp)
            )
        )
        prev.submit(mapOf(lib to 1938, pc to 1338, grn to 347, ndp to 442))
        secondaryPrev.submit(mapOf(lib to 7767, pc to 8169, grn to 4011, ndp to 1427))
        region.submit("MALPEQUE")
        districtName.value = "STANHOME-MARSFIELD"
        districtNum.submit(8)
        focus.submit(listOf(8, 15, 16, 17, 18, 19, 20))
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesUpdating-2", screen)
    }

    @Throws(IOException::class)
    private fun peiShapesByDistrict(): Map<Int, Shape> {
        val peiMap = MapFrameTest::class.java
            .classLoader
            .getResource("com/joecollins/graphics/shapefiles/pei-districts.shp")
        return ShapefileReader.readShapes(peiMap, "DIST_NO", Int::class.java)
    }
}
