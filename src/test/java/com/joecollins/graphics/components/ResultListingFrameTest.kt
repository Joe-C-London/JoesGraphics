package com.joecollins.graphics.components

import com.joecollins.bindings.Bindable
import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.mapElements
import com.joecollins.graphics.utils.BindableWrapper
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class ResultListingFrameTest {
    @Test
    fun testNumRows() {
        val frame = ResultListingFrame(
            headerBinding = fixedBinding(null),
            numRowsBinding = fixedBinding(20),
            itemsBinding = fixedBinding(emptyList())
        )
        Assert.assertEquals(20, frame.getNumRows().toLong())
    }

    @Test
    fun testItems() {
        val rawItems: MutableList<Item> = ArrayList()
        rawItems.add(Item("BELFAST-MURRAY RIVER", Color.WHITE, Color.BLACK, Color.BLUE))
        rawItems.add(Item("SOURIS-ELMIRA", Color.WHITE, Color.BLACK, Color.BLUE))
        rawItems.add(Item("GEORGETOWN-ST. PETERS", Color.WHITE, Color.BLACK, Color.BLUE))
        rawItems.add(Item("MORRELL-MERMAID", Color.WHITE, Color.BLACK, Color.BLUE))
        rawItems.add(Item("RUSTICO-EMERALD", Color.WHITE, Color.BLACK, Color.BLUE))
        rawItems.add(Item("BORDEN-KINKORA", Color.WHITE, Color.BLACK, Color.BLUE))
        rawItems.add(Item("STRATFORD-KINLOCK", Color.WHITE, Color.BLACK, Color.BLUE))
        rawItems.add(Item("KENSINGTON-MALPEQUE", Color.WHITE, Color.BLACK, Color.BLUE))
        val items: BindableWrapper<List<Item>> = BindableWrapper(rawItems)
        val frame = ResultListingFrame(
            headerBinding = fixedBinding(null),
            numRowsBinding = fixedBinding(20),
            itemsBinding = items.binding.mapElements {
                ResultListingFrame.Item(
                    text = it.text,
                    border = it.border,
                    background = it.background,
                    foreground = it.foreground
                )
            }
        )
        Assert.assertEquals(8, frame.getNumItems().toLong())
        Assert.assertEquals("BELFAST-MURRAY RIVER", frame.getText(0))
        Assert.assertEquals(Color.WHITE, frame.getBackground(1))
        Assert.assertEquals(Color.BLACK, frame.getForeground(2))
        Assert.assertEquals(Color.BLUE, frame.getBorder(3))

        rawItems[4].foreground = Color.WHITE
        rawItems[4].background = Color.BLUE
        items.value = rawItems
        Assert.assertEquals(Color.BLUE, frame.getBackground(4))
        Assert.assertEquals(Color.WHITE, frame.getForeground(4))
        Assert.assertEquals(Color.BLUE, frame.getBorder(4))
    }

    @Test
    @Throws(IOException::class)
    fun testSingleFullColumn() {
        val rawItems: MutableList<Item> = ArrayList()
        rawItems.add(Item("Mermaid-Stratford", Color.WHITE, Color.BLACK, Color.RED)) // 0.0
        rawItems.add(Item("Charlottetown-Brighton", Color.WHITE, Color.BLACK, Color.RED)) // 0.8
        rawItems.add(Item("Summerside-Wilmot", Color.WHITE, Color.BLACK, Color.RED)) // 1.0
        rawItems.add(Item("Brackley-Hunter River", Color.WHITE, Color.BLACK, Color.RED)) // 1.6
        rawItems.add(Item("Summerside-South Drive", Color.WHITE, Color.BLACK, Color.RED)) // 4.9
        rawItems.add(Item("Charlottetown-West Royalty", Color.WHITE, Color.BLACK, Color.RED)) // 7.3
        rawItems.add(Item("O'Leary-Inverness", Color.WHITE, Color.BLACK, Color.RED)) // 9.2
        rawItems.add(Item("Montague-Kilmuir", Color.WHITE, Color.BLACK, Color.RED)) // 10.8
        rawItems.add(Item("Charlottetown-Victoria Park", Color.WHITE, Color.BLACK, Color.RED)) // 11.9
        rawItems.add(Item("Cornwall-Meadowbank", Color.WHITE, Color.BLACK, Color.RED)) // 12.5
        val items: BindableWrapper<List<Item>> = BindableWrapper(rawItems)
        val frame = ResultListingFrame(
            headerBinding = fixedBinding("PC TARGETS"),
            borderColorBinding = fixedBinding(Color.BLUE),
            numRowsBinding = fixedBinding(10),
            itemsBinding = items.binding.mapElements {
                ResultListingFrame.Item(
                    text = it.text.toUpperCase(),
                    border = it.border,
                    background = it.background,
                    foreground = it.foreground
                )
            }
        )
        frame.setSize(512, 512)
        compareRendering("ResultListingFrame", "FullColumn-1", frame)

        for (i in 0..9) {
            when (i) {
                0, 1, 2, 4, 8 -> rawItems[i].background = Color.GREEN.darker()
                3, 7 -> rawItems[i].background = Color.BLUE
                5, 6, 9 -> rawItems[i].background = Color.RED
                else -> continue
            }
            rawItems[i].foreground = Color.WHITE
        }
        items.value = rawItems
        compareRendering("ResultListingFrame", "FullColumn-2", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testVaryingItems() {
        val rawItems: MutableList<Item> = ArrayList()
        val items: BindableWrapper<List<Item>> = BindableWrapper(rawItems)
        val frame = ResultListingFrame(
            headerBinding = fixedBinding("SEATS CHANGING"),
            numRowsBinding = fixedBinding(10),
            itemsBinding = items.binding.mapElements {
                ResultListingFrame.Item(
                    text = it.text.toUpperCase(),
                    border = it.border,
                    background = it.background,
                    foreground = it.foreground
                )
            }
        )
        frame.setSize(512, 256)
        compareRendering("ResultListingFrame", "Varying-1", frame)

        rawItems.add(Item("Montague-Kilmuir", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Brackley-Hunter River", Color.BLUE, Color.WHITE, Color.GRAY))
        rawItems.add(Item("Charlottetown-Victoria Park", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Summerside-South Drive", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.sortBy { it.text }
        items.value = rawItems
        compareRendering("ResultListingFrame", "Varying-2", frame)

        rawItems.add(Item("Mermaid-Stratford", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Charlottetown-Belvedere", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Stanhope-Marshfield", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Charlottetown-Brighton", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Alberton-Bloomfield", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Summerside-Wilmot", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Tyne Valley-Sherbrooke", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.sortBy { it.text }
        items.value = rawItems
        compareRendering("ResultListingFrame", "Varying-3", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testVaryingItemsInReverse() {
        val rawItems: MutableList<Item> = ArrayList()
        val items: BindableWrapper<List<Item>> = BindableWrapper(rawItems)
        val frame = ResultListingFrame(
            headerBinding = fixedBinding("SEATS CHANGING"),
            numRowsBinding = fixedBinding(10),
            reversedBinding = fixedBinding(true),
            itemsBinding = items.binding.mapElements {
                ResultListingFrame.Item(
                    text = it.text.uppercase(),
                    border = it.border,
                    background = it.background,
                    foreground = it.foreground
                )
            }
        )
        frame.setSize(512, 256)
        compareRendering("ResultListingFrame", "Reversed-1", frame)

        rawItems.add(Item("Montague-Kilmuir", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Brackley-Hunter River", Color.BLUE, Color.WHITE, Color.GRAY))
        rawItems.add(Item("Charlottetown-Victoria Park", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Summerside-South Drive", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.sortBy { it.text }
        items.value = rawItems
        compareRendering("ResultListingFrame", "Reversed-2", frame)

        rawItems.add(Item("Mermaid-Stratford", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Charlottetown-Belvedere", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Stanhope-Marshfield", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Charlottetown-Brighton", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Alberton-Bloomfield", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Summerside-Wilmot", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Tyne Valley-Sherbrooke", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.sortBy { it.text }
        items.value = rawItems
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
