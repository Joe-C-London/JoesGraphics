package com.joecollins.graphics.screens.generic

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.toFixedBinding
import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.seats
import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.seatsWithDiff
import com.joecollins.graphics.screens.generic.RegionalBreakdownScreen.Companion.seatsWithPrev
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.models.general.Party
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.junit.Test

class RegionalBreakdownScreenTest {
    @Test
    @Throws(IOException::class)
    fun testSeats() {
        val peiSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val cardiganSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val malpequeSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val egmontSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val screen = seats(
                fixedBinding("PRINCE EDWARD ISLAND"),
                peiSeats.binding,
                fixedBinding(27),
                fixedBinding("SEATS BY REGION"))
                .withBlankRow()
                .withRegion(fixedBinding("CARDIGAN"), cardiganSeats.binding, fixedBinding(7))
                .withRegion(fixedBinding("MALPEQUE"), malpequeSeats.binding, fixedBinding(7))
                .withRegion(fixedBinding("CHARLOTTETOWN"), charlottetownSeats.binding, fixedBinding(6))
                .withRegion(fixedBinding("EGMONT"), egmontSeats.binding, fixedBinding(7))
                .build(fixedBinding("PRINCE EDWARD ISLAND"))
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "Seats-1", screen)
        peiSeats.value = mapOf(grn to 1)
        cardiganSeats.value = mapOf(grn to 1)
        compareRendering("RegionalBreakdownScreen", "Seats-2", screen)
        peiSeats.value = mapOf(pc to 13, grn to 8, lib to 6)
        cardiganSeats.value = mapOf(pc to 6, grn to 1)
        malpequeSeats.value = mapOf(pc to 5, grn to 1, lib to 1)
        charlottetownSeats.value = mapOf(grn to 3, lib to 2, pc to 1)
        egmontSeats.value = mapOf(grn to 3, lib to 3, pc to 1)
        compareRendering("RegionalBreakdownScreen", "Seats-3", screen)
    }

    @Test
    @Throws(IOException::class)
    fun testSeatsWithDiff() {
        val peiSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val cardiganSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val malpequeSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val egmontSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val peiDiff = BindableWrapper<Map<Party, Int>>(emptyMap())
        val cardiganDiff = BindableWrapper<Map<Party, Int>>(emptyMap())
        val malpequeDiff = BindableWrapper<Map<Party, Int>>(emptyMap())
        val charlottetownDiff = BindableWrapper<Map<Party, Int>>(emptyMap())
        val egmontDiff = BindableWrapper<Map<Party, Int>>(emptyMap())
        val screen = seatsWithDiff(
                fixedBinding("PRINCE EDWARD ISLAND"),
                peiSeats.binding,
                peiDiff.binding,
                fixedBinding(27),
                fixedBinding("SEATS BY REGION"))
                .withBlankRow()
                .withRegion(fixedBinding("CARDIGAN"), cardiganSeats.binding, cardiganDiff.binding, fixedBinding(7))
                .withRegion(fixedBinding("MALPEQUE"), malpequeSeats.binding, malpequeDiff.binding, fixedBinding(7))
                .withRegion(fixedBinding("CHARLOTTETOWN"), charlottetownSeats.binding, charlottetownDiff.binding, fixedBinding(6))
                .withRegion(fixedBinding("EGMONT"), egmontSeats.binding, egmontDiff.binding, fixedBinding(7))
                .build(fixedBinding("PRINCE EDWARD ISLAND"))
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-1", screen)
        peiSeats.value = mapOf(grn to 1)
        peiDiff.value = mapOf(grn to +1, lib to -1)
        cardiganSeats.value = mapOf(grn to 1)
        cardiganDiff.value = mapOf(grn to +1, lib to -1)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-2", screen)
        peiSeats.value = mapOf(pc to 13, grn to 8, lib to 6)
        peiDiff.value = mapOf(pc to +5, grn to +7, lib to -12)
        cardiganSeats.value = mapOf(pc to 6, grn to 1)
        cardiganDiff.value = mapOf(pc to +1, grn to +1, lib to -2)
        malpequeSeats.value = mapOf(pc to 5, grn to 1, lib to 1)
        malpequeDiff.value = mapOf(pc to +2, grn to 0, lib to -1)
        charlottetownSeats.value = mapOf(grn to 3, lib to 2, pc to 1)
        charlottetownDiff.value = mapOf(grn to +3, lib to -5, pc to +1)
        egmontSeats.value = mapOf(grn to 3, lib to 3, pc to 1)
        egmontDiff.value = mapOf(grn to +3, lib to -4, pc to +1)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-3", screen)
    }

    @Test
    @Throws(IOException::class)
    fun testSeatsWithPrev() {
        val peiSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val cardiganSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val malpequeSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val charlottetownSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val egmontSeats = BindableWrapper<Map<Party, Int>>(emptyMap())
        val peiPrev = BindableWrapper<Map<Party, Int>>(emptyMap())
        val cardiganPrev = BindableWrapper<Map<Party, Int>>(emptyMap())
        val malpequePrev = BindableWrapper<Map<Party, Int>>(emptyMap())
        val charlottetownPrev = BindableWrapper<Map<Party, Int>>(emptyMap())
        val egmontPrev = BindableWrapper<Map<Party, Int>>(emptyMap())
        val screen = seatsWithPrev(
                fixedBinding("PRINCE EDWARD ISLAND"),
                peiSeats.binding,
                peiPrev.binding,
                fixedBinding(27),
                fixedBinding("SEATS BY REGION"))
                .withBlankRow()
                .withRegion(fixedBinding("CARDIGAN"), cardiganSeats.binding, cardiganPrev.binding, fixedBinding(7))
                .withRegion(fixedBinding("MALPEQUE"), malpequeSeats.binding, malpequePrev.binding, fixedBinding(7))
                .withRegion(fixedBinding("CHARLOTTETOWN"), charlottetownSeats.binding, charlottetownPrev.binding, fixedBinding(6))
                .withRegion(fixedBinding("EGMONT"), egmontSeats.binding, egmontPrev.binding, fixedBinding(7))
                .build(fixedBinding("PRINCE EDWARD ISLAND"))
        screen.setSize(1024, 512)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-1", screen)
        peiSeats.value = mapOf(grn to 1)
        peiPrev.value = mapOf(lib to 1)
        cardiganSeats.value = mapOf(grn to 1)
        cardiganPrev.value = mapOf(lib to 1)
        compareRendering("RegionalBreakdownScreen", "SeatsWithDiff-2", screen)
        peiSeats.value = mapOf(pc to 13, grn to 8, lib to 6)
        peiPrev.value = mapOf(pc to 8, grn to 1, lib to 18)
        cardiganSeats.value = mapOf(pc to 6, grn to 1)
        cardiganPrev.value = mapOf(pc to 5, lib to 2)
        malpequeSeats.value = mapOf(pc to 5, grn to 1, lib to 1)
        malpequePrev.value = mapOf(pc to 3, grn to 1, lib to 2)
        charlottetownSeats.value = mapOf(grn to 3, lib to 2, pc to 1)
        charlottetownPrev.value = mapOf(lib to 7)
        egmontSeats.value = mapOf(grn to 3, lib to 3, pc to 1)
        egmontPrev.value = mapOf(lib to 7)
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
            fixedBinding("AUSTRALIA"),
            mapOf(alp to 68, coa to 77, oth to 6).toFixedBinding(),
            fixedBinding(151),
            fixedBinding("SEATS BY STATE"))
            .withBlankRow()
            .withRegion(fixedBinding("NEW SOUTH WALES"), mapOf(coa to 22, alp to 24, oth to 1).toFixedBinding(), fixedBinding(47))
            .withRegion(fixedBinding("VICTORIA"), mapOf(coa to 15, alp to 21, oth to 2).toFixedBinding(), fixedBinding(38))
            .withRegion(fixedBinding("QUEENSLAND"), mapOf(lnp to 23, alp to 6, oth to 1).toFixedBinding(), fixedBinding(30), mapOf(coa to lnp).toFixedBinding())
            .withRegion(fixedBinding("WESTERN AUSTRALIA"), mapOf(coa to 11, alp to 5).toFixedBinding(), fixedBinding(16))
            .withRegion(fixedBinding("SOUTH AUSTRALIA"), mapOf(coa to 4, alp to 5, oth to 1).toFixedBinding(), fixedBinding(10))
            .withRegion(fixedBinding("TASMANIA"), mapOf(coa to 2, alp to 2, oth to 1).toFixedBinding(), fixedBinding(5))
            .withRegion(fixedBinding("AUSTRALIAN CAPITAL TERRITORY"), mapOf(alp to 3).toFixedBinding(), fixedBinding(3), mapOf(coa to lib).toFixedBinding())
            .withRegion(fixedBinding("NORTHERN TERRITORY"), mapOf(alp to 2).toFixedBinding(), fixedBinding(2), mapOf(coa to clp).toFixedBinding())
            .build(fixedBinding("AUSTRALIA"))
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
            fixedBinding("AUSTRALIA"),
            mapOf(alp to 68, coa to 77, oth to 6).toFixedBinding(),
            mapOf(alp to 69, coa to 76, oth to 5).toFixedBinding(),
            fixedBinding(151),
            fixedBinding("SEATS BY STATE"))
            .withBlankRow()
            .withRegion(fixedBinding("NEW SOUTH WALES"), mapOf(coa to 22, alp to 24, oth to 1).toFixedBinding(), mapOf(coa to 23, alp to 24).toFixedBinding(), fixedBinding(47))
            .withRegion(fixedBinding("VICTORIA"), mapOf(coa to 15, alp to 21, oth to 2).toFixedBinding(), mapOf(coa to 17, alp to 18, oth to 2).toFixedBinding(), fixedBinding(38))
            .withRegion(fixedBinding("QUEENSLAND"), mapOf(lnp to 23, alp to 6, oth to 1).toFixedBinding(), mapOf(lnp to 21, alp to 8, oth to 1).toFixedBinding(), fixedBinding(30), mapOf(coa to lnp).toFixedBinding())
            .withRegion(fixedBinding("WESTERN AUSTRALIA"), mapOf(coa to 11, alp to 5).toFixedBinding(), mapOf(coa to 11, alp to 5).toFixedBinding(), fixedBinding(16))
            .withRegion(fixedBinding("SOUTH AUSTRALIA"), mapOf(coa to 4, alp to 5, oth to 1).toFixedBinding(), mapOf(coa to 4, alp to 6, oth to 1).toFixedBinding(), fixedBinding(10))
            .withRegion(fixedBinding("TASMANIA"), mapOf(coa to 2, alp to 2, oth to 1).toFixedBinding(), mapOf(alp to 4, oth to 1).toFixedBinding(), fixedBinding(5))
            .withRegion(fixedBinding("AUSTRALIAN CAPITAL TERRITORY"), mapOf(alp to 3).toFixedBinding(), mapOf(alp to 2).toFixedBinding(), fixedBinding(3), mapOf(coa to lib).toFixedBinding())
            .withRegion(fixedBinding("NORTHERN TERRITORY"), mapOf(alp to 2).toFixedBinding(), mapOf(alp to 2).toFixedBinding(), fixedBinding(2), mapOf(coa to clp).toFixedBinding())
            .build(fixedBinding("AUSTRALIA"))
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
            fixedBinding("AUSTRALIA"),
            mapOf(alp to 68, coa to 77, oth to 6).toFixedBinding(),
            mapOf(alp to -1, coa to +1, oth to +1).toFixedBinding(),
            fixedBinding(151),
            fixedBinding("SEATS BY STATE"))
            .withBlankRow()
            .withRegion(fixedBinding("NEW SOUTH WALES"), mapOf(coa to 22, alp to 24, oth to 1).toFixedBinding(), mapOf(coa to -1, oth to +1).toFixedBinding(), fixedBinding(47))
            .withRegion(fixedBinding("VICTORIA"), mapOf(coa to 15, alp to 21, oth to 2).toFixedBinding(), mapOf(coa to -2, alp to +3).toFixedBinding(), fixedBinding(38))
            .withRegion(fixedBinding("QUEENSLAND"), mapOf(lnp to 23, alp to 6, oth to 1).toFixedBinding(), mapOf(lnp to +2, alp to -2).toFixedBinding(), fixedBinding(30), mapOf(coa to lnp).toFixedBinding())
            .withRegion(fixedBinding("WESTERN AUSTRALIA"), mapOf(coa to 11, alp to 5).toFixedBinding(), mapOf<Party, Int>().toFixedBinding(), fixedBinding(16))
            .withRegion(fixedBinding("SOUTH AUSTRALIA"), mapOf(coa to 4, alp to 5, oth to 1).toFixedBinding(), mapOf(alp to -1).toFixedBinding(), fixedBinding(10))
            .withRegion(fixedBinding("TASMANIA"), mapOf(coa to 2, alp to 2, oth to 1).toFixedBinding(), mapOf(coa to +2, alp to -2).toFixedBinding(), fixedBinding(5))
            .withRegion(fixedBinding("AUSTRALIAN CAPITAL TERRITORY"), mapOf(alp to 3).toFixedBinding(), mapOf(alp to +1).toFixedBinding(), fixedBinding(3), mapOf(coa to lib).toFixedBinding())
            .withRegion(fixedBinding("NORTHERN TERRITORY"), mapOf(alp to 2).toFixedBinding(), mapOf(alp to 0).toFixedBinding(), fixedBinding(2), mapOf(coa to clp).toFixedBinding())
            .build(fixedBinding("AUSTRALIA"))
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
