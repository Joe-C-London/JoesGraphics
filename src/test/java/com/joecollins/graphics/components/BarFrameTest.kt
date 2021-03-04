package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.sizeBinding
import com.joecollins.bindings.IndexedBinding.Companion.functionBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.bindings.IndexedBinding.Companion.propertyBinding
import com.joecollins.bindings.IndexedBinding.Companion.singletonBinding
import com.joecollins.bindings.NestedBindableList
import com.joecollins.graphics.ImageGenerator.createHalfTickShape
import com.joecollins.graphics.components.BarFrameTest.ElectionResult
import com.joecollins.graphics.components.BarFrameTest.RidingResult
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.awt.Polygon
import java.awt.Rectangle
import java.awt.Shape
import java.awt.geom.Area
import java.awt.geom.Ellipse2D
import java.io.IOException
import java.text.DecimalFormat
import kotlin.Throws
import kotlin.jvm.JvmOverloads
import kotlin.math.sign
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.apache.commons.lang3.tuple.Triple
import org.junit.Assert
import org.junit.Test

class BarFrameTest {
    @Test
    fun testNumBars() {
        val results = BindableList<ElectionResult>()
        results.add(ElectionResult("LIBERAL", Color.RED, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1))
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        Assert.assertEquals(6, frame.numBars.toLong())
    }

    @Test
    fun testAddRemoveBars() {
        val results = BindableList<ElectionResult>()
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        Assert.assertEquals(0, frame.numBars.toLong())
        results.add(ElectionResult("LIBERAL", Color.RED, 1))
        Assert.assertEquals(1, frame.numBars.toLong())
        results.addAll(listOf(
                        ElectionResult("CONSERVATIVE", Color.BLUE, 1),
                        ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1)))
        Assert.assertEquals(3, frame.numBars.toLong())
        results.removeAt(2)
        Assert.assertEquals(2, frame.numBars.toLong())
        results.removeIf { it.getPartyName() != "LIBERAL" }
        Assert.assertEquals(1, frame.numBars.toLong())
        results.clear()
        Assert.assertEquals(0, frame.numBars.toLong())
    }

    @Test
    fun testLeftTextBinding() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1))
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(1))
        Assert.assertEquals("BLOC QUEBECOIS", frame.getLeftText(2))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3))
        Assert.assertEquals("GREEN", frame.getLeftText(4))
        Assert.assertEquals("INDEPENDENT", frame.getLeftText(5))
    }

    @Test
    fun testRightTextBinding() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1))
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.setRightTextBinding(propertyBinding(results, { it.getNumSeats().toString() }, ElectionResult.Properties.NUM_SEATS))
        Assert.assertEquals("157", frame.getRightText(0))
        Assert.assertEquals("121", frame.getRightText(1))
        Assert.assertEquals("32", frame.getRightText(2))
        Assert.assertEquals("24", frame.getRightText(3))
        Assert.assertEquals("3", frame.getRightText(4))
        Assert.assertEquals("1", frame.getRightText(5))
    }

    @Test
    fun testSeriesBinding() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 2, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 1, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 0, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 0, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 0, 1))
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        frame.addSeriesBinding(
                "Estimate",
                propertyBinding(results, { lighten(it.getPartyColor()) }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getSeatEstimate() - it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS, ElectionResult.Properties.SEAT_ESTIMATE))
        val lightRed = Color(255, 127, 127)
        var libSeries = frame.getSeries(0)
        Assert.assertEquals(Color.RED, libSeries[0].left)
        Assert.assertEquals(2, libSeries[0].right.toInt().toLong())
        Assert.assertEquals(lightRed, libSeries[1].left)
        Assert.assertEquals(155, libSeries[1].right.toInt().toLong())
        results[0].setSeatEstimate(158)
        libSeries = frame.getSeries(0)
        Assert.assertEquals(Color.RED, libSeries[0].left)
        Assert.assertEquals(2, libSeries[0].right.toInt().toLong())
        Assert.assertEquals(lightRed, libSeries[1].left)
        Assert.assertEquals(156, libSeries[1].right.toInt().toLong())
        results[0].setNumSeats(3)
        libSeries = frame.getSeries(0)
        Assert.assertEquals(Color.RED, libSeries[0].left)
        Assert.assertEquals(3, libSeries[0].right.toInt().toLong())
        Assert.assertEquals(lightRed, libSeries[1].left)
        Assert.assertEquals(155, libSeries[1].right.toInt().toLong())
    }

    @Test
    fun testLeftIconBinding() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1))
        val shape: Shape = Ellipse2D.Double()
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.setLeftIconBinding(propertyBinding(results, { if (it.getNumSeats() > 150) shape else null }, ElectionResult.Properties.NUM_SEATS))
        Assert.assertEquals(shape, frame.getLeftIcon(0))
        Assert.assertNull(frame.getLeftIcon(1))
        Assert.assertNull(frame.getLeftIcon(2))
        Assert.assertNull(frame.getLeftIcon(3))
        Assert.assertNull(frame.getLeftIcon(4))
        Assert.assertNull(frame.getLeftIcon(5))
    }

    @Test
    fun testMinMax() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1))
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        Assert.assertEquals(157, frame.max.toInt().toLong())
        Assert.assertEquals(0, frame.min.toInt().toLong())
        results[0].setNumSeats(-27)
        results[1].setNumSeats(22)
        results[2].setNumSeats(22)
        results[3].setNumSeats(-20)
        results[4].setNumSeats(2)
        results[5].setNumSeats(1)
        Assert.assertEquals(22, frame.max.toInt().toLong())
        Assert.assertEquals(-27, frame.min.toInt().toLong())
        frame.setMinBinding(fixedBinding(-30))
        frame.setMaxBinding(fixedBinding(30))
        Assert.assertEquals(30, frame.max.toInt().toLong())
        Assert.assertEquals(-30, frame.min.toInt().toLong())
    }

    @Test
    fun testSubheadText() {
        val frame = BarFrame()
        frame.setSubheadTextBinding(fixedBinding<String?>("PROJECTION: LIB MINORITY"))
        Assert.assertEquals("PROJECTION: LIB MINORITY", frame.subheadText)
    }

    @Test
    fun testDefaultSubheadColor() {
        val frame = BarFrame()
        Assert.assertEquals(Color.BLACK, frame.subheadColor)
    }

    @Test
    fun testSubheadColor() {
        val frame = BarFrame()
        frame.setSubheadColorBinding(fixedBinding(Color.RED))
        Assert.assertEquals(Color.RED, frame.subheadColor)
    }

    @Test
    fun testLines() {
        val frame = BarFrame()
        frame.setNumLinesBinding(fixedBinding(1))
        frame.setLineLevelsBinding(singletonBinding(170))
        frame.setLineLabelsBinding(singletonBinding("170 SEATS FOR MAJORITY"))
        Assert.assertEquals(1, frame.numLines.toLong())
        Assert.assertEquals(170, frame.getLineLevel(0))
        Assert.assertEquals("170 SEATS FOR MAJORITY", frame.getLineLabel(0))
    }

    @Test
    fun testUnbind() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        val con = ElectionResult("CONSERVATIVE", Color.BLUE, 1)
        val lib = ElectionResult("LIBERAL", Color.RED, 1)
        val ndp = ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1)
        results.addAll(listOf(con, lib, ndp))
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getLeftText(1))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2))
        val differentResults: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        differentResults.add(ElectionResult("GREEN", Color.GREEN, 1))
        frame.setNumBarsBinding(sizeBinding(differentResults))
        frame.setLeftTextBinding(propertyBinding(differentResults, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        Assert.assertEquals(1, frame.numBars.toLong())
        Assert.assertEquals("GREEN", frame.getLeftText(0))
        results.removeAt(0)
        Assert.assertEquals(1, frame.numBars.toLong())
        Assert.assertEquals("GREEN", frame.getLeftText(0))
        results[0].setPartyName("HAHA")
        Assert.assertEquals(1, frame.numBars.toLong())
        Assert.assertEquals("GREEN", frame.getLeftText(0))
        differentResults[0].setPartyName("GREENS")
        Assert.assertEquals(1, frame.numBars.toLong())
        Assert.assertEquals("GREENS", frame.getLeftText(0))
        differentResults.add(0, ElectionResult("LIBERAL", Color.RED, 1))
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("GREENS", frame.getLeftText(1))
        differentResults[1].setPartyName("GREEN")
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("GREEN", frame.getLeftText(1))
    }

    @Test
    fun testLeftTextBindingOnAdd() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        Assert.assertEquals(0, frame.numBars.toLong())
        results.add(ElectionResult("LIBERAL", Color.RED, 1))
        Assert.assertEquals(1, frame.numBars.toLong())
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1))
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(1))
        results.add(0, ElectionResult("CONSERVATIVE", Color.BLUE, 1))
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getLeftText(1))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2))
        results[0].setPartyName("CONSERVATIVES")
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVES", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getLeftText(1))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2))
    }

    @Test
    fun testLeftTextBindingOnRemove() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        val con = ElectionResult("CONSERVATIVE", Color.BLUE, 1)
        val lib = ElectionResult("LIBERAL", Color.RED, 1)
        val ndp = ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1)
        results.addAll(listOf(con, lib, ndp))
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getLeftText(1))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2))
        results.remove(lib)
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(0))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(1))
        lib.setPartyName("LIBERALS")
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(0))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(1))
        ndp.setPartyName("NDP")
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(0))
        Assert.assertEquals("NDP", frame.getLeftText(1))
        results.removeAt(1)
        Assert.assertEquals(1, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(0))
    }

    @Test
    fun testLeftTextBindingOnSet() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        val con = ElectionResult("CONSERVATIVE", Color.BLUE, 2)
        val lib = ElectionResult("LIBERAL", Color.RED, 3)
        val ndp = ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1)
        results.addAll(listOf(con, lib, ndp))
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getLeftText(1))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2))
        results.sortByDescending { it.getNumSeats() }
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(1))
        Assert.assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2))
        con.setPartyName("CONSERVATIVES")
        lib.setPartyName("LIBERALS")
        ndp.setPartyName("NDP")
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("LIBERALS", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVES", frame.getLeftText(1))
        Assert.assertEquals("NDP", frame.getLeftText(2))
    }

    @Test
    fun testTestNonBindableElements() {
        val results = BindableList<Triple<String, Color, Int>>()
        val frame = BarFrame()
        frame.setNumBarsBinding(sizeBinding(results))
        frame.setLeftTextBinding(propertyBinding(results) { it.left })
        Assert.assertEquals(0, frame.numBars.toLong())
        results.add(ImmutableTriple("NDP", Color.ORANGE, 1))
        Assert.assertEquals(1, frame.numBars.toLong())
        Assert.assertEquals("NDP", frame.getLeftText(0))
        results.setAll(listOf(
                ImmutableTriple("LIBERALS", Color.RED, 3),
                ImmutableTriple("CONSERVATIVES", Color.BLUE, 2),
                ImmutableTriple("NDP", Color.ORANGE, 1)))
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("LIBERALS", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVES", frame.getLeftText(1))
        Assert.assertEquals("NDP", frame.getLeftText(2))
        results.setAll(listOf(
                ImmutableTriple("LIBERALS", Color.RED, 3),
                ImmutableTriple("CONSERVATIVES", Color.BLUE, 3)))
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals("LIBERALS", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVES", frame.getLeftText(1))
    }

    @Test
    fun testOtherBindings() {
        val frame = BarFrame()
        frame.setNumBarsBinding(fixedBinding(3))
        frame.setLeftTextBinding(listBinding("LIBERAL", "CONSERVATIVE", "NDP"))
        frame.setRightTextBinding(functionBinding(3, 6) { it.toString() })
        Assert.assertEquals(3, frame.numBars.toLong())
        Assert.assertEquals("LIBERAL", frame.getLeftText(0))
        Assert.assertEquals("CONSERVATIVE", frame.getLeftText(1))
        Assert.assertEquals("NDP", frame.getLeftText(2))
        Assert.assertEquals("3", frame.getRightText(0))
        Assert.assertEquals("4", frame.getRightText(1))
        Assert.assertEquals("5", frame.getRightText(2))
        val result = ElectionResult("", Color.WHITE, 3)
        frame.setNumLinesBinding(Binding.propertyBinding(result, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        frame.setLineLevelsBinding(propertyBinding(result, { (1..it.getNumSeats()).toList() }, ElectionResult.Properties.NUM_SEATS))
        Assert.assertEquals(3, frame.numLines.toLong())
        Assert.assertEquals(1, frame.getLineLevel(0))
        Assert.assertEquals(2, frame.getLineLevel(1))
        Assert.assertEquals(3, frame.getLineLevel(2))
        result.setNumSeats(2)
        Assert.assertEquals(2, frame.numLines.toLong())
        Assert.assertEquals(1, frame.getLineLevel(0))
        Assert.assertEquals(2, frame.getLineLevel(1))
        frame.setNumLinesBinding(fixedBinding(4))
        frame.setLineLevelsBinding(listBinding(3, 4, 5, 6))
        Assert.assertEquals(4, frame.numLines.toLong())
        Assert.assertEquals(3, frame.getLineLevel(0))
        Assert.assertEquals(4, frame.getLineLevel(1))
        Assert.assertEquals(5, frame.getLineLevel(2))
        Assert.assertEquals(6, frame.getLineLevel(3))
        result.setNumSeats(1)
        Assert.assertEquals(4, frame.numLines.toLong())
        Assert.assertEquals(3, frame.getLineLevel(0))
        Assert.assertEquals(4, frame.getLineLevel(1))
        Assert.assertEquals(5, frame.getLineLevel(2))
        Assert.assertEquals(6, frame.getLineLevel(3))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSingleSeriesAllPositive() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("2019 CANADIAN ELECTION RESULT"))
        barFrame.setMaxBinding(fixedBinding(160))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { it.getNumSeats().toString() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesAllPositive", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSingleSeriesWithSubhead() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("2019 CANADIAN ELECTION RESULT"))
        barFrame.setMaxBinding(fixedBinding(160))
        barFrame.setSubheadTextBinding(fixedBinding<String?>("PROJECTION: LIB MINORITY"))
        barFrame.setSubheadColorBinding(fixedBinding(Color.RED))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { it.getNumSeats().toString() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesWithSubhead", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSingleSeriesShrinkToFit() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 177))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 95))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 39))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 10))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 8))
        results.add(ElectionResult("GREEN", Color.GREEN, 2))
        results.add(ElectionResult("CO-OPERATIVE COMMONWEALTH FEDERATION", Color.ORANGE.darker(), 1))
        results.add(ElectionResult("PEOPLE'S PARTY", Color.MAGENTA.darker(), 1))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("SEATS AT DISSOLUTION"))
        barFrame.setSubheadTextBinding(fixedBinding<String?>("170 FOR MAJORITY"))
        barFrame.setSubheadColorBinding(fixedBinding(Color.RED))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { it.getNumSeats().toString() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesShrinkToFit", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMultiSeriesAllPositive() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 34, 157))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 21, 121))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 2, 32))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 4, 24))
        results.add(ElectionResult("GREEN", Color.GREEN, 1, 3))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 0, 1))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("2019 CANADIAN ELECTION RESULT"))
        barFrame.setMaxBinding(fixedBinding(160))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { it.getNumSeats().toString() + "/" + it.getSeatEstimate() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Estimate",
                propertyBinding(results, { lighten(it.getPartyColor()) }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getSeatEstimate() - it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS, ElectionResult.Properties.SEAT_ESTIMATE))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "MultiSeriesAllPositive", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSingleSeriesBothDirections() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIB", Color.RED, -27))
        results.add(ElectionResult("CON", Color.BLUE, +22))
        results.add(ElectionResult("BQ", Color.CYAN, +22))
        results.add(ElectionResult("NDP", Color.ORANGE, -20))
        results.add(ElectionResult("GRN", Color.GREEN, +2))
        results.add(ElectionResult("IND", Color.LIGHT_GRAY, +1))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("RESULT CHANGE SINCE 2015"))
        barFrame.setMaxBinding(fixedBinding(28))
        barFrame.setMinBinding(fixedBinding(-28))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { DecimalFormat("+0;-0").format(it.getNumSeats().toLong()) }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "SingleSeriesBothDirections", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMultiSeriesBothDirections() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIB", Color.RED, -7, -27))
        results.add(ElectionResult("CON", Color.BLUE, +4, +22))
        results.add(ElectionResult("BQ", Color.CYAN, +0, +22))
        results.add(ElectionResult("NDP", Color.ORANGE, +2, -20))
        results.add(ElectionResult("GRN", Color.GREEN, +1, +2))
        results.add(ElectionResult("IND", Color.LIGHT_GRAY, +0, +1))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("RESULT CHANGE SINCE 2015"))
        barFrame.setMaxBinding(fixedBinding(28))
        barFrame.setMinBinding(fixedBinding(-28))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { ("${CHANGE_FORMAT.format(it.getNumSeats().toLong())}/${CHANGE_FORMAT.format(it.getSeatEstimate().toLong())}") }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Estimate",
                propertyBinding(results, { lighten(it.getPartyColor()) }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { (it.getSeatEstimate() - if (sign(it.getSeatEstimate().toFloat()) == sign(it.getNumSeats().toFloat())) it.getNumSeats() else 0) }, ElectionResult.Properties.NUM_SEATS, ElectionResult.Properties.SEAT_ESTIMATE))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "MultiSeriesBothDirections", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderTwoLinedBars() {
        val results: NestedBindableList<RidingResult, RidingResult.Properties> = NestedBindableList()
        results.add(RidingResult("BARDISH CHAGGER", "LIBERAL", Color.RED, 31085, 0.4879))
        results.add(RidingResult("JERRY ZHANG", "CONSERVATIVE", Color.BLUE, 15615, 0.2451))
        results.add(RidingResult("LORI CAMPBELL", "NEW DEMOCRATIC PARTY", Color.ORANGE, 9710, 0.1524))
        results.add(RidingResult("KIRSTEN WRIGHT", "GREEN", Color.GREEN, 6184, 0.0971))
        results.add(RidingResult("ERIKA TRAUB", "PEOPLE'S PARTY", Color.MAGENTA.darker(), 1112, 0.0175))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("WATERLOO"))
        barFrame.setMaxBinding(fixedBinding(results.map { it.getNumVotes() }.sum() / 2))
        barFrame.setSubheadTextBinding(fixedBinding<String?>("LIB HOLD"))
        barFrame.setSubheadColorBinding(fixedBinding(Color.RED))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { "${it.getCandidateName()}\n${it.getPartyName()}" }, RidingResult.Properties.CANDIDATE_NAME, RidingResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { "${THOUSANDS_FORMAT.format(it.getNumVotes().toLong())}\n${PERCENT_FORMAT.format(it.getVotePct())}" }, RidingResult.Properties.NUM_VOTES, RidingResult.Properties.VOTE_PCT))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, RidingResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumVotes() }, RidingResult.Properties.NUM_VOTES))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "TwoLinedBars", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderTwoLinedBarWithIcon() {
        val results: NestedBindableList<RidingResult, RidingResult.Properties> = NestedBindableList()
        results.add(RidingResult("BARDISH CHAGGER", "LIBERAL", Color.RED, 31085, 0.4879, true))
        results.add(RidingResult("JERRY ZHANG", "CONSERVATIVE", Color.BLUE, 15615, 0.2451))
        results.add(RidingResult("LORI CAMPBELL", "NEW DEMOCRATIC PARTY", Color.ORANGE, 9710, 0.1524))
        results.add(RidingResult("KIRSTEN WRIGHT", "GREEN", Color.GREEN, 6184, 0.0971))
        results.add(RidingResult("ERIKA TRAUB", "PEOPLE'S PARTY", Color.MAGENTA.darker(), 1112, 0.0175))
        val shape = createTickShape()
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("WATERLOO"))
        barFrame.setMaxBinding(fixedBinding(results.map { it.getNumVotes() }.sum() / 2))
        barFrame.setSubheadTextBinding(fixedBinding<String?>("LIB HOLD"))
        barFrame.setSubheadColorBinding(fixedBinding(Color.RED))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { "${it.getCandidateName()}\n${it.getPartyName()}" }, RidingResult.Properties.CANDIDATE_NAME, RidingResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { "${THOUSANDS_FORMAT.format(it.getNumVotes().toLong())}\n${PERCENT_FORMAT.format(it.getVotePct())}" }, RidingResult.Properties.NUM_VOTES, RidingResult.Properties.VOTE_PCT))
        barFrame.setLeftIconBinding(propertyBinding(results, { if (it.isElected()) shape else null }, RidingResult.Properties.ELECTED))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, RidingResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumVotes() }, RidingResult.Properties.NUM_VOTES))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "TwoLinedBarWithIcon", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderTwoLinedBarWithNegativeIcon() {
        val results: NestedBindableList<RidingResult, RidingResult.Properties> = NestedBindableList()
        results.add(RidingResult("BARDISH CHAGGER", "LIB", Color.RED, 31085, -0.010, true))
        results.add(RidingResult("JERRY ZHANG", "CON", Color.BLUE, 15615, -0.077))
        results.add(RidingResult("LORI CAMPBELL", "NDP", Color.ORANGE, 9710, +0.003))
        results.add(RidingResult("KIRSTEN WRIGHT", "GRN", Color.GREEN, 6184, +0.068))
        results.add(RidingResult("ERIKA TRAUB", "PPC", Color.MAGENTA.darker(), 1112, +0.017))
        val shape = createTickShape()
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("WATERLOO"))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, RidingResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { PERCENT_FORMAT.format(it.getVotePct()) }, RidingResult.Properties.VOTE_PCT))
        barFrame.setLeftIconBinding(propertyBinding(results, { if (it.isElected()) shape else null }, RidingResult.Properties.ELECTED))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, RidingResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getVotePct() }, RidingResult.Properties.VOTE_PCT))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "TwoLinedBarWithNegativeIcon", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderVerticalLine() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("LIBERAL", Color.RED, 177))
        results.add(ElectionResult("CONSERVATIVE", Color.BLUE, 95))
        results.add(ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 39))
        results.add(ElectionResult("BLOC QUEBECOIS", Color.CYAN, 10))
        results.add(ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 8))
        results.add(ElectionResult("GREEN", Color.GREEN, 2))
        results.add(ElectionResult("CO-OPERATIVE COMMONWEALTH FEDERATION", Color.ORANGE.darker(), 1))
        results.add(ElectionResult("PEOPLE'S PARTY", Color.MAGENTA.darker(), 1))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("SEATS AT DISSOLUTION"))
        barFrame.setSubheadTextBinding(fixedBinding<String?>("170 FOR MAJORITY"))
        barFrame.setSubheadColorBinding(fixedBinding(Color.RED))
        barFrame.setMaxBinding(fixedBinding(225))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { it.getNumSeats().toString() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.setNumLinesBinding(fixedBinding(1))
        barFrame.setLineLevelsBinding(singletonBinding(170))
        barFrame.setLineLabelsBinding(singletonBinding("MAJORITY"))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "VerticalLine", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderAccents() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("COALITION AVENIR QU\u00c9BEC: FRAN\u00c7OIS LEGAULT", Color.BLUE, 74))
        results.add(ElectionResult("LIB\u00c9RAL: PHILIPPE COUILLARD", Color.RED, 31))
        results.add(ElectionResult("PARTI QU\u00c9BECOIS: JEAN-FRAN\u00c7OIS LIS\u00c9E", Color.CYAN, 10))
        results.add(ElectionResult("QU\u00c9BEC SOLIDAIRE: MANON MASS\u00c9", Color.ORANGE, 10))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("\u00c9LECTION 2018"))
        barFrame.setSubheadTextBinding(fixedBinding<String?>("MAJORIT\u00c9: 63"))
        barFrame.setMaxBinding(fixedBinding(83))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { it.getNumSeats().toString() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.setNumLinesBinding(fixedBinding(1))
        barFrame.setLineLevelsBinding(singletonBinding(63))
        barFrame.setLineLabelsBinding(singletonBinding("MAJORIT\u00c9"))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "Accents", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderMultiLineAccents() {
        val results: NestedBindableList<ElectionResult, ElectionResult.Properties> = NestedBindableList()
        results.add(ElectionResult("COALITION AVENIR QU\u00c9BEC\nFRAN\u00c7OIS LEGAULT", Color.BLUE, 74))
        results.add(ElectionResult("LIB\u00c9RAL\nPHILIPPE COUILLARD", Color.RED, 31))
        results.add(ElectionResult("PARTI QU\u00c9BECOIS\nJEAN-FRAN\u00c7OIS LIS\u00c9E", Color.CYAN, 10))
        results.add(ElectionResult("QU\u00c9BEC SOLIDAIRE\nMANON MASS\u00c9", Color.ORANGE, 10))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("\u00c9LECTION 2018"))
        barFrame.setSubheadTextBinding(fixedBinding<String?>("MAJORIT\u00c9: 63"))
        barFrame.setMaxBinding(fixedBinding(83))
        barFrame.setNumBarsBinding(sizeBinding(results))
        barFrame.setLeftTextBinding(propertyBinding(results, { it.getPartyName() }, ElectionResult.Properties.PARTY_NAME))
        barFrame.setRightTextBinding(propertyBinding(results, { it.getNumSeats().toString() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.addSeriesBinding(
                "Seats",
                propertyBinding(results, { it.getPartyColor() }, ElectionResult.Properties.PARTY_COLOR),
                propertyBinding(results, { it.getNumSeats() }, ElectionResult.Properties.NUM_SEATS))
        barFrame.setNumLinesBinding(fixedBinding(1))
        barFrame.setLineLevelsBinding(singletonBinding(63))
        barFrame.setLineLabelsBinding(singletonBinding("MAJORIT\u00c9"))
        barFrame.setSize(512, 256)
        compareRendering("BarFrame", "MultiLineAccents", barFrame)
    }

    @Test
    @Throws(IOException::class)
    fun testBarFrameOverlaps() {
        val lines = BindableList<Triple<String, String, Boolean>>()
        lines.add(ImmutableTriple.of("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "RIGHT\nSIDE", false))
        val barFrame = BarFrame()
        barFrame.setHeaderBinding(fixedBinding<String?>("BAR FRAME"))
        barFrame.setSubheadTextBinding(fixedBinding<String?>(""))
        barFrame.setMaxBinding(fixedBinding(1))
        barFrame.setNumBarsBinding(sizeBinding(lines))
        barFrame.setLeftTextBinding(propertyBinding(lines) { it.left })
        barFrame.setRightTextBinding(propertyBinding(lines) { it.middle })
        barFrame.setLeftIconBinding(propertyBinding(lines) { if (it.right) createHalfTickShape() else null })
        barFrame.addSeriesBinding(
                "Value",
                propertyBinding(lines) { Color.RED },
                propertyBinding(lines) { 1 })
        barFrame.setNumLinesBinding(fixedBinding(1))
        barFrame.setLineLevelsBinding(listBinding(0.5))
        barFrame.setSize(256, 128)
        compareRendering("BarFrame", "FrameOverlap-1", barFrame)
        lines[0] = ImmutableTriple.of("LEFT\nSIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", false)
        compareRendering("BarFrame", "FrameOverlap-2", barFrame)
        lines[0] = ImmutableTriple.of("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", false)
        compareRendering("BarFrame", "FrameOverlap-3", barFrame)
        lines[0] = ImmutableTriple.of("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "RIGHT\nSIDE", true)
        compareRendering("BarFrame", "FrameOverlap-4", barFrame)
        lines[0] = ImmutableTriple.of("LEFT\nSIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", true)
        compareRendering("BarFrame", "FrameOverlap-5", barFrame)
        lines[0] = ImmutableTriple.of("THIS IS A VERY VERY LONG\nLEFT HAND SIDE", "THIS IS A VERY VERY LONG\nRIGHT HAND SIDE", true)
        compareRendering("BarFrame", "FrameOverlap-6", barFrame)
    }

    private fun createTickShape(): Shape {
        val shape = Area(Rectangle(0, 0, 100, 100))
        shape.subtract(Area(Polygon(intArrayOf(10, 40, 90, 80, 40, 20), intArrayOf(50, 80, 30, 20, 60, 40), 6)))
        return shape
    }

    private class ElectionResult @JvmOverloads constructor(private var partyName: String, private var partyColor: Color, private var numSeats: Int, private var seatEstimate: Int = numSeats) : Bindable<ElectionResult, ElectionResult.Properties>() {
        enum class Properties {
            PARTY_NAME, PARTY_COLOR, NUM_SEATS, SEAT_ESTIMATE
        }

        fun getPartyName(): String {
            return partyName
        }

        fun setPartyName(partyName: String) {
            this.partyName = partyName
            onPropertyRefreshed(Properties.PARTY_NAME)
        }

        fun getPartyColor(): Color {
            return partyColor
        }

        fun setPartyColor(partyColor: Color) {
            this.partyColor = partyColor
            onPropertyRefreshed(Properties.PARTY_COLOR)
        }

        fun getNumSeats(): Int {
            return numSeats
        }

        fun setNumSeats(numSeats: Int) {
            this.numSeats = numSeats
            onPropertyRefreshed(Properties.NUM_SEATS)
        }

        fun getSeatEstimate(): Int {
            return seatEstimate
        }

        fun setSeatEstimate(seatEstimate: Int) {
            this.seatEstimate = seatEstimate
            onPropertyRefreshed(Properties.SEAT_ESTIMATE)
        }
    }

    private class RidingResult @JvmOverloads constructor(
        private var candidateName: String,
        private var partyName: String,
        private var partyColor: Color,
        private var numVotes: Int,
        private var votePct: Double,
        private var elected: Boolean = false
    ) : Bindable<RidingResult, RidingResult.Properties>() {
        enum class Properties {
            CANDIDATE_NAME, PARTY_NAME, PARTY_COLOR, NUM_VOTES, VOTE_PCT, ELECTED
        }

        fun getCandidateName(): String {
            return candidateName
        }

        fun setCandidateName(candidateName: String) {
            this.candidateName = candidateName
            onPropertyRefreshed(Properties.CANDIDATE_NAME)
        }

        fun getPartyName(): String {
            return partyName
        }

        fun setPartyName(partyName: String) {
            this.partyName = partyName
            onPropertyRefreshed(Properties.PARTY_NAME)
        }

        fun getPartyColor(): Color {
            return partyColor
        }

        fun setPartyColor(partyColor: Color) {
            this.partyColor = partyColor
            onPropertyRefreshed(Properties.PARTY_COLOR)
        }

        fun getNumVotes(): Int {
            return numVotes
        }

        fun setNumVotes(numVotes: Int) {
            this.numVotes = numVotes
            onPropertyRefreshed(Properties.NUM_VOTES)
        }

        fun getVotePct(): Double {
            return votePct
        }

        fun setVotePct(votePct: Double) {
            this.votePct = votePct
            onPropertyRefreshed(Properties.VOTE_PCT)
        }

        fun isElected(): Boolean {
            return elected
        }

        fun setElected(elected: Boolean) {
            this.elected = elected
            onPropertyRefreshed(Properties.ELECTED)
        }
    }

    companion object {
        private val CHANGE_FORMAT = DecimalFormat("+0;-0")
        private val THOUSANDS_FORMAT = DecimalFormat("#,##0")
        private val PERCENT_FORMAT = DecimalFormat("0.0%")
        private fun lighten(color: Color?): Color {
            return Color(
                    (color!!.red + 255) / 2, (color.green + 255) / 2, (color.blue + 255) / 2)
        }
    }
}
