package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.Binding.Companion.sizeBinding
import com.joecollins.bindings.IndexedBinding.Companion.propertyBinding
import com.joecollins.bindings.NestedBindableList
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class ResultListingFrameTest {
    @Test
    fun testNumRows() {
        val frame = ResultListingFrame()
        frame.setNumRowsBinding(fixedBinding(20))
        Assert.assertEquals(20, frame.getNumRows().toLong())
    }

    @Test
    fun testItems() {
        val items: NestedBindableList<Item, Item.Property> = NestedBindableList()
        val frame = ResultListingFrame()
        frame.setNumItemsBinding(sizeBinding(items))
        frame.setTextBinding(propertyBinding(items, { it.text }, Item.Property.TEXT))
        frame.setForegroundBinding(propertyBinding(items, { it.foreground }, Item.Property.FOREGROUND))
        frame.setBackgroundBinding(propertyBinding(items, { it.background }, Item.Property.BACKGROUND))
        frame.setBorderBinding(propertyBinding(items, { it.border }, Item.Property.BORDER))
        items.add(Item("BELFAST-MURRAY RIVER", Color.WHITE, Color.BLACK, Color.BLUE))
        items.add(Item("SOURIS-ELMIRA", Color.WHITE, Color.BLACK, Color.BLUE))
        items.add(Item("GEORGETOWN-ST. PETERS", Color.WHITE, Color.BLACK, Color.BLUE))
        items.add(Item("MORRELL-MERMAID", Color.WHITE, Color.BLACK, Color.BLUE))
        items.add(Item("RUSTICO-EMERALD", Color.WHITE, Color.BLACK, Color.BLUE))
        items.add(Item("BORDEN-KINKORA", Color.WHITE, Color.BLACK, Color.BLUE))
        items.add(Item("STRATFORD-KINLOCK", Color.WHITE, Color.BLACK, Color.BLUE))
        items.add(Item("KENSINGTON-MALPEQUE", Color.WHITE, Color.BLACK, Color.BLUE))
        Assert.assertEquals(8, frame.getNumItems().toLong())
        Assert.assertEquals("BELFAST-MURRAY RIVER", frame.getText(0))
        Assert.assertEquals(Color.WHITE, frame.getBackground(1))
        Assert.assertEquals(Color.BLACK, frame.getForeground(2))
        Assert.assertEquals(Color.BLUE, frame.getBorder(3))
        items[4].foreground = Color.WHITE
        items[4].background = Color.BLUE
        Assert.assertEquals(Color.BLUE, frame.getBackground(4))
        Assert.assertEquals(Color.WHITE, frame.getForeground(4))
        Assert.assertEquals(Color.BLUE, frame.getBorder(4))
    }

    @Test
    @Throws(IOException::class)
    fun testSingleFullColumn() {
        val items: NestedBindableList<Item, Item.Property> = NestedBindableList()
        items.add(Item("Mermaid-Stratford", Color.WHITE, Color.BLACK, Color.RED)) // 0.0
        items.add(Item("Charlottetown-Brighton", Color.WHITE, Color.BLACK, Color.RED)) // 0.8
        items.add(Item("Summerside-Wilmot", Color.WHITE, Color.BLACK, Color.RED)) // 1.0
        items.add(Item("Brackley-Hunter River", Color.WHITE, Color.BLACK, Color.RED)) // 1.6
        items.add(Item("Summerside-South Drive", Color.WHITE, Color.BLACK, Color.RED)) // 4.9
        items.add(Item("Charlottetown-West Royalty", Color.WHITE, Color.BLACK, Color.RED)) // 7.3
        items.add(Item("O'Leary-Inverness", Color.WHITE, Color.BLACK, Color.RED)) // 9.2
        items.add(Item("Montague-Kilmuir", Color.WHITE, Color.BLACK, Color.RED)) // 10.8
        items.add(Item("Charlottetown-Victoria Park", Color.WHITE, Color.BLACK, Color.RED)) // 11.9
        items.add(Item("Cornwall-Meadowbank", Color.WHITE, Color.BLACK, Color.RED)) // 12.5
        val frame = ResultListingFrame()
        frame.setNumRowsBinding(fixedBinding(10))
        frame.setNumItemsBinding(sizeBinding(items))
        frame.setTextBinding(propertyBinding(items, { it.text.toUpperCase() }, Item.Property.TEXT))
        frame.setForegroundBinding(propertyBinding(items, { it.foreground }, Item.Property.FOREGROUND))
        frame.setBackgroundBinding(propertyBinding(items, { it.background }, Item.Property.BACKGROUND))
        frame.setBorderBinding(propertyBinding(items, { it.border }, Item.Property.BORDER))
        frame.setHeaderBinding(fixedBinding("PC TARGETS"))
        frame.setBorderColorBinding(fixedBinding(Color.BLUE))
        frame.setSize(512, 512)
        compareRendering("ResultListingFrame", "FullColumn-1", frame)

        for (i in 0..9) {
            when (i) {
                0, 1, 2, 4, 8 -> items[i].background = Color.GREEN.darker()
                3, 7 -> items[i].background = Color.BLUE
                5, 6, 9 -> items[i].background = Color.RED
                else -> continue
            }
            items[i].foreground = Color.WHITE
        }
        compareRendering("ResultListingFrame", "FullColumn-2", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testVaryingItems() {
        val items: NestedBindableList<Item, Item.Property> = NestedBindableList()
        val frame = ResultListingFrame()
        frame.setNumRowsBinding(fixedBinding(10))
        frame.setNumItemsBinding(sizeBinding(items))
        frame.setTextBinding(propertyBinding(items, { it.text.toUpperCase() }, Item.Property.TEXT))
        frame.setForegroundBinding(propertyBinding(items, { it.foreground }, Item.Property.FOREGROUND))
        frame.setBackgroundBinding(propertyBinding(items, { it.background }, Item.Property.BACKGROUND))
        frame.setBorderBinding(propertyBinding(items, { it.border }, Item.Property.BORDER))
        frame.setHeaderBinding(fixedBinding("SEATS CHANGING"))
        frame.setSize(512, 256)
        compareRendering("ResultListingFrame", "Varying-1", frame)

        items.add(Item("Montague-Kilmuir", Color.BLUE, Color.WHITE, Color.RED))
        items.add(Item("Brackley-Hunter River", Color.BLUE, Color.WHITE, Color.GRAY))
        items.add(Item("Charlottetown-Victoria Park", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Summerside-South Drive", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.sortBy { it.text }
        compareRendering("ResultListingFrame", "Varying-2", frame)

        items.add(Item("Mermaid-Stratford", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Charlottetown-Belvedere", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Stanhope-Marshfield", Color.BLUE, Color.WHITE, Color.RED))
        items.add(Item("Charlottetown-Brighton", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Alberton-Bloomfield", Color.BLUE, Color.WHITE, Color.RED))
        items.add(Item("Summerside-Wilmot", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Tyne Valley-Sherbrooke", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.sortBy { it.text }
        compareRendering("ResultListingFrame", "Varying-3", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testVaryingItemsInReverse() {
        val items: NestedBindableList<Item, Item.Property> = NestedBindableList()
        val frame = ResultListingFrame()
        frame.setNumRowsBinding(fixedBinding(10))
        frame.setReversedBinding(fixedBinding(true))
        frame.setNumItemsBinding(sizeBinding(items))
        frame.setTextBinding(propertyBinding(items, { it.text.toUpperCase() }, Item.Property.TEXT))
        frame.setForegroundBinding(propertyBinding(items, { it.foreground }, Item.Property.FOREGROUND))
        frame.setBackgroundBinding(propertyBinding(items, { it.background }, Item.Property.BACKGROUND))
        frame.setBorderBinding(propertyBinding(items, { it.border }, Item.Property.BORDER))
        frame.setHeaderBinding(fixedBinding("SEATS CHANGING"))
        frame.setSize(512, 256)
        compareRendering("ResultListingFrame", "Reversed-1", frame)

        items.add(Item("Montague-Kilmuir", Color.BLUE, Color.WHITE, Color.RED))
        items.add(Item("Brackley-Hunter River", Color.BLUE, Color.WHITE, Color.GRAY))
        items.add(Item("Charlottetown-Victoria Park", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Summerside-South Drive", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.sortBy { it.text }
        compareRendering("ResultListingFrame", "Reversed-2", frame)

        items.add(Item("Mermaid-Stratford", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Charlottetown-Belvedere", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Stanhope-Marshfield", Color.BLUE, Color.WHITE, Color.RED))
        items.add(Item("Charlottetown-Brighton", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Alberton-Bloomfield", Color.BLUE, Color.WHITE, Color.RED))
        items.add(Item("Summerside-Wilmot", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.add(Item("Tyne Valley-Sherbrooke", Color.GREEN.darker(), Color.WHITE, Color.RED))
        items.sortBy { it.text }
        compareRendering("ResultListingFrame", "Reversed-3", frame)
    }

    private class Item(private var _text: String, private var _background: Color, private var _foreground: Color, private var _border: Color) : Bindable<Item, Item.Property>() {
        enum class Property {
            TEXT, FOREGROUND, BACKGROUND, BORDER
        }

        var text: String
        get() = _text
        set(text) {
            this._text = text
            onPropertyRefreshed(Property.TEXT)
        }

        var foreground: Color
        get() = _foreground
        set(foreground) {
            this._foreground = foreground
            onPropertyRefreshed(Property.FOREGROUND)
        }

        var background: Color
        get() = _background
        set(background) {
            this._background = background
            onPropertyRefreshed(Property.BACKGROUND)
        }

        var border: Color
        get() = _border
        set(border) {
            this._border = border
            onPropertyRefreshed(Property.BORDER)
        }
    }
}
