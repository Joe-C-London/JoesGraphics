package com.joecollins.graphics.components

import com.joecollins.bindings.BindableList
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.graphics.components.ListingFrameBuilder.Companion.of
import com.joecollins.graphics.components.ListingFrameBuilder.Companion.ofFixedList
import com.joecollins.graphics.utils.BindableWrapper
import java.awt.Color
import java.util.ArrayList
import org.apache.commons.lang3.tuple.ImmutableTriple
import org.apache.commons.lang3.tuple.Triple
import org.junit.Assert
import org.junit.Test

class ListingFrameBuilderTest {
    @Test
    fun testBasicListingFrame() {
        val list = BindableList<Triple<String, Color, String>>()
        list.add(ImmutableTriple.of("JUSTIN TRUDEAU", Color.RED, "LIBERAL"))
        list.add(ImmutableTriple.of("ANDREW SCHEER", Color.BLUE, "CONSERVATIVE"))
        val frame = of(list, { it.left }, { it.right }) { it.middle }
                .withHeader(fixedBinding("HEADER"))
                .withSubhead(fixedBinding("SUBHEAD"))
                .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals(0.0, frame.min.toDouble(), 1e-6)
        Assert.assertEquals(1.0, frame.max.toDouble(), 1e-6)
        Assert.assertEquals("HEADER", frame.header)
        Assert.assertEquals("SUBHEAD", frame.subheadText)
        Assert.assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getRightText(0))
        Assert.assertEquals(1, frame.getSeries(0).size.toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].left)
        Assert.assertEquals(1.0, frame.getSeries(0)[0].right.toDouble(), 1e-6)
        Assert.assertEquals("ANDREW SCHEER", frame.getLeftText(1))
        Assert.assertEquals("CONSERVATIVE", frame.getRightText(1))
        Assert.assertEquals(1, frame.getSeries(1).size.toLong())
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].left)
        Assert.assertEquals(1.0, frame.getSeries(1)[0].right.toDouble(), 1e-6)
    }

    @Test
    fun testBasicListingFrameWithListBinding() {
        val list: MutableList<Triple<String, Color, String>> = ArrayList()
        list.add(ImmutableTriple.of("JUSTIN TRUDEAU", Color.RED, "LIBERAL"))
        list.add(ImmutableTriple.of("ANDREW SCHEER", Color.BLUE, "CONSERVATIVE"))
        val frame: BarFrame = of(fixedBinding(list), { it.left }, { it.right }) { it.middle }
                .withHeader(fixedBinding("HEADER"))
                .withSubhead(fixedBinding("SUBHEAD"))
                .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals(0.0, frame.min.toDouble(), 1e-6)
        Assert.assertEquals(1.0, frame.max.toDouble(), 1e-6)
        Assert.assertEquals("HEADER", frame.header)
        Assert.assertEquals("SUBHEAD", frame.subheadText)
        Assert.assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getRightText(0))
        Assert.assertEquals(1, frame.getSeries(0).size.toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].left)
        Assert.assertEquals(1.0, frame.getSeries(0)[0].right.toDouble(), 1e-6)
        Assert.assertEquals("ANDREW SCHEER", frame.getLeftText(1))
        Assert.assertEquals("CONSERVATIVE", frame.getRightText(1))
        Assert.assertEquals(1, frame.getSeries(1).size.toLong())
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].left)
        Assert.assertEquals(1.0, frame.getSeries(1)[0].right.toDouble(), 1e-6)
    }

    @Test
    fun testBasicFixedListFrame() {
        val list: MutableList<Triple<BindableWrapper<String>, BindableWrapper<Color>, BindableWrapper<String>>> = ArrayList()
        list.add(ImmutableTriple.of(
                        BindableWrapper("JUSTIN TRUDEAU"),
                        BindableWrapper(Color.RED),
                        BindableWrapper("LIBERAL")))
        list.add(ImmutableTriple.of(
                        BindableWrapper("ANDREW SCHEER"),
                        BindableWrapper(Color.BLUE),
                        BindableWrapper("CONSERVATIVE")))
        val frame = ofFixedList(list, { it.left.binding }, { it.right.binding }) { it.middle.binding }
                .withHeader(fixedBinding("HEADER"))
                .withSubhead(fixedBinding("SUBHEAD"))
                .build()
        Assert.assertEquals(0, frame.numLines.toLong())
        Assert.assertEquals(2, frame.numBars.toLong())
        Assert.assertEquals(0.0, frame.min.toDouble(), 1e-6)
        Assert.assertEquals(1.0, frame.max.toDouble(), 1e-6)
        Assert.assertEquals("HEADER", frame.header)
        Assert.assertEquals("SUBHEAD", frame.subheadText)
        Assert.assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0))
        Assert.assertEquals("LIBERAL", frame.getRightText(0))
        Assert.assertEquals(1, frame.getSeries(0).size.toLong())
        Assert.assertEquals(Color.RED, frame.getSeries(0)[0].left)
        Assert.assertEquals(1.0, frame.getSeries(0)[0].right.toDouble(), 1e-6)
        Assert.assertEquals("ANDREW SCHEER", frame.getLeftText(1))
        Assert.assertEquals("CONSERVATIVE", frame.getRightText(1))
        Assert.assertEquals(1, frame.getSeries(1).size.toLong())
        Assert.assertEquals(Color.BLUE, frame.getSeries(1)[0].left)
        Assert.assertEquals(1.0, frame.getSeries(1)[0].right.toDouble(), 1e-6)
    }
}
