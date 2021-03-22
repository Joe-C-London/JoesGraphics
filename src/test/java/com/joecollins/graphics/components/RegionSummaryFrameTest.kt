package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class RegionSummaryFrameTest {
    @Test
    fun testEntriesDifferentColors() {
        val frame = RegionSummaryFrame()
        frame.setNumSectionsBinding(fixedBinding(2))
        frame.setSectionHeaderBinding(listBinding(listOf("ELECTORAL VOTES", "POPULAR VOTE")))
        frame.setSectionValueColorBinding(
                listBinding(
                        listOf(
                                listOf(
                                        Pair(Color.BLUE, "306"),
                                        Pair(Color.BLUE, "<< 74"),
                                        Pair(Color.RED, "232")),
                                listOf(
                                        Pair(Color.BLUE, "51.1%"),
                                        Pair(Color.BLUE, "<< 1.0%"),
                                        Pair(Color.RED, "47.2%")))))
        Assert.assertEquals(2, frame.getNumSections().toLong())
        Assert.assertEquals(Color.BLACK, frame.getSummaryColor())
        Assert.assertEquals("ELECTORAL VOTES", frame.getSectionHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getValueColor(1, 0))
        Assert.assertEquals("<< 1.0%", frame.getValue(1, 1))
    }

    @Test
    fun testEntriesSameColor() {
        val frame = RegionSummaryFrame()
        frame.setNumSectionsBinding(fixedBinding(2))
        frame.setSummaryColorBinding(fixedBinding(Color.BLUE))
        frame.setSectionHeaderBinding(listBinding(listOf("ELECTORAL VOTES", "POPULAR VOTE")))
        frame.setSectionValueBinding(listBinding(listOf(listOf("306", "+74"), listOf("51.1%", "+1.0%"))))
        Assert.assertEquals(2, frame.getNumSections().toLong())
        Assert.assertEquals(Color.BLUE, frame.getSummaryColor())
        Assert.assertEquals("ELECTORAL VOTES", frame.getSectionHeader(0))
        Assert.assertEquals(Color.BLUE, frame.getValueColor(1, 0))
        Assert.assertEquals("+1.0%", frame.getValue(1, 1))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderDifferentColors() {
        val frame = RegionSummaryFrame()
        frame.setHeaderBinding(fixedBinding("UNITED STATES"))
        frame.setNumSectionsBinding(fixedBinding(2))
        frame.setSectionHeaderBinding(listBinding(listOf("ELECTORAL VOTES", "POPULAR VOTE")))
        frame.setSectionValueColorBinding(
                listBinding(
                        listOf(
                                listOf(
                                        Pair(Color.BLUE, "306"),
                                        Pair(Color.BLUE, "<< 74"),
                                        Pair(Color.RED, "232")),
                                listOf(
                                        Pair(Color.BLUE, "51.1%"),
                                        Pair(Color.BLUE, "<< 1.0%"),
                                        Pair(Color.RED, "47.2%")))))
        frame.setSize(500, 500)
        compareRendering("RegionSummaryFrame", "DifferentColors", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderSameColor() {
        val frame = RegionSummaryFrame()
        frame.setHeaderBinding(fixedBinding("USA"))
        frame.setNumSectionsBinding(fixedBinding(2))
        frame.setBorderColorBinding(fixedBinding(Color.BLUE))
        frame.setSummaryColorBinding(fixedBinding(Color.BLUE))
        frame.setSectionHeaderBinding(listBinding(listOf("ELECTORAL VOTES", "POPULAR VOTE")))
        frame.setSectionValueBinding(listBinding(listOf(listOf("306", "+74"), listOf("51.1%", "+1.0%"))))
        frame.setSize(125, 125)
        compareRendering("RegionSummaryFrame", "SameColor", frame)
    }
}
