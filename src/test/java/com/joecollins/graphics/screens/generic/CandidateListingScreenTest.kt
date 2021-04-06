package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding
import com.joecollins.graphics.components.MapFrameTest
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils
import com.joecollins.graphics.utils.ShapefileReader
import com.joecollins.models.general.Candidate
import com.joecollins.models.general.Party
import java.awt.Color
import java.awt.Dimension
import java.awt.Shape
import java.io.IOException
import org.junit.Test

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
            Binding.fixedBinding(candidates),
            Binding.fixedBinding("CANDIDATES"),
            Binding.fixedBinding(""),
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
            Binding.fixedBinding(candidates),
            Binding.fixedBinding("CANDIDATES"),
            Binding.fixedBinding(""),
            "[MLA]"
        )
            .withPrev(
                Binding.fixedBinding(prev),
                Binding.fixedBinding("2015 RESULT"),
                Binding.fixedBinding(null)
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
            Binding.fixedBinding(candidates),
            Binding.fixedBinding("CANDIDATES"),
            Binding.fixedBinding(""),
            "[MLA]"
        )
            .withMap(
                Binding.fixedBinding(shapes),
                Binding.fixedBinding(1),
                Binding.fixedBinding(listOf(1, 2, 3, 4, 5, 6, 7)),
                Binding.fixedBinding("CARDIGAN")
        )
            .withPrev(
                Binding.fixedBinding(prev),
                Binding.fixedBinding("2015 RESULT"),
                Binding.fixedBinding(null)
            )
            .build(Binding.fixedBinding("SOURIS-ELMIRA"))
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesWithPrevAndMap", screen)
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
            Binding.fixedBinding(candidates),
            Binding.fixedBinding("CANDIDATES"),
            Binding.fixedBinding(""),
            "[MLA]"
        )
            .withMap(
                Binding.fixedBinding(shapes),
                Binding.fixedBinding(1),
                Binding.fixedBinding(listOf(1, 2, 3, 4, 5, 6, 7)),
                Binding.fixedBinding("CARDIGAN")
            )
            .withPrev(
                Binding.fixedBinding(prev),
                Binding.fixedBinding("2015 RESULT"),
                Binding.fixedBinding(null)
            )
            .withSecondaryPrev(
                Binding.fixedBinding(secondaryPrev),
                Binding.fixedBinding("2015 REGIONAL RESULT"),
                Binding.fixedBinding(null)
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
            Binding.fixedBinding(candidates),
            Binding.fixedBinding("CANDIDATES"),
            Binding.fixedBinding(""),
            "[MLA]"
        )
            .withPrev(
                Binding.fixedBinding(prev),
                Binding.fixedBinding("2015 RESULT"),
                Binding.fixedBinding(null)
            )
            .withSecondaryPrev(
                Binding.fixedBinding(secondaryPrev),
                Binding.fixedBinding("2015 REGIONAL RESULT"),
                Binding.fixedBinding(null)
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
        val candidates = BindableWrapper(listOf(
            Candidate("Kevin Doyle", lib),
            Candidate("Susan Hartley", grn),
            Candidate("Edith Perry", ndp),
            Candidate("Steven Myers", pc, true)
        ))
        val prev = BindableWrapper(mapOf(
            lib to 1170,
            pc to 1448,
            grn to 145,
            ndp to 256
        ))
        val secondaryPrev = BindableWrapper(mapOf(
            lib to 8016,
            pc to 9444,
            grn to 1144,
            ndp to 2404
        ))
        val region = BindableWrapper("CARDIGAN")
        val districtName = BindableWrapper("GEORGETOWN-POWNAL")
        val districtNum = BindableWrapper(2)
        val focus = BindableWrapper(listOf(1, 2, 3, 4, 5, 6, 7))
        val shapes = peiShapesByDistrict()
        val screen = CandidateListingScreen.of(
            candidates.binding,
            Binding.fixedBinding("CANDIDATES"),
            Binding.fixedBinding(""),
            "[MLA]"
        )
            .withMap(
                Binding.fixedBinding(shapes),
                districtNum.binding,
                focus.binding,
                region.binding
            )
            .withPrev(
                prev.binding,
                Binding.fixedBinding("2015 RESULT"),
                Binding.fixedBinding(null)
            )
            .withSecondaryPrev(
                secondaryPrev.binding,
                Binding.fixedBinding("2015 REGIONAL RESULT"),
                Binding.fixedBinding(null)
            )
            .build(districtName.binding)
        screen.size = Dimension(1024, 512)
        RenderTestUtils.compareRendering("CandidateListingScreen", "CandidatesUpdating-1", screen)

        candidates.value = listOf(
            Candidate("Sarah Donald", grn),
            Candidate("Wade MacLauchlan", lib, true),
            Candidate("Bloyce Thompson", pc),
            Candidate("Marian White", ndp)
        )
        prev.value = mapOf(lib to 1938, pc to 1338, grn to 347, ndp to 442)
        secondaryPrev.value = mapOf(lib to 7767, pc to 8169, grn to 4011, ndp to 1427)
        region.value = "MALPEQUE"
        districtName.value = "STANHOME-MARSFIELD"
        districtNum.value = 8
        focus.value = listOf(8, 15, 16, 17, 18, 19, 20)
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
