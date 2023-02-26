package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.Publisher
import com.joecollins.pubsub.asOneTimePublisher
import com.joecollins.pubsub.mapElements
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.awt.Color

class ResultListingFrameTest {
    @Test
    fun testNumRows() {
        val frame = ResultListingFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            numRowsPublisher = 20.asOneTimePublisher(),
            itemsPublisher = emptyList<ResultListingFrame.Item>().asOneTimePublisher(),
        )
        assertEquals(20, frame.getNumRows())
    }

    @Test
    fun testItems() {
        val rawItems = listOf(
            Item("BELFAST-MURRAY RIVER", Color.WHITE, Color.BLACK, Color.BLUE),
            Item("SOURIS-ELMIRA", Color.WHITE, Color.BLACK, Color.BLUE),
            Item("GEORGETOWN-ST. PETERS", Color.WHITE, Color.BLACK, Color.BLUE),
            Item("MORRELL-MERMAID", Color.WHITE, Color.BLACK, Color.BLUE),
            Item("RUSTICO-EMERALD", Color.WHITE, Color.BLACK, Color.BLUE),
            Item("BORDEN-KINKORA", Color.WHITE, Color.BLACK, Color.BLUE),
            Item("STRATFORD-KINLOCK", Color.WHITE, Color.BLACK, Color.BLUE),
            Item("KENSINGTON-MALPEQUE", Color.WHITE, Color.BLACK, Color.BLUE),
        )
        val items: Publisher<List<Item>> = Publisher(rawItems)
        val frame = ResultListingFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            numRowsPublisher = 20.asOneTimePublisher(),
            itemsPublisher = items.mapElements {
                ResultListingFrame.Item(
                    text = it.text,
                    border = it.border,
                    background = it.background,
                    foreground = it.foreground,
                )
            },
        )
        assertEquals(8, frame.getNumItems())
        assertEquals("BELFAST-MURRAY RIVER", frame.getText(0))
        assertEquals(Color.WHITE, frame.getBackground(1))
        assertEquals(Color.BLACK, frame.getForeground(2))
        assertEquals(Color.BLUE, frame.getBorder(3))

        rawItems[4].foreground = Color.WHITE
        rawItems[4].background = Color.BLUE
        items.submit(rawItems)
        assertEquals(Color.BLUE, frame.getBackground(4))
        assertEquals(Color.WHITE, frame.getForeground(4))
        assertEquals(Color.BLUE, frame.getBorder(4))
    }

    @Test
    fun testSingleFullColumn() {
        val rawItems = listOf(
            Item("Mermaid-Stratford", Color.WHITE, Color.BLACK, Color.RED), // 0.0
            Item("Charlottetown-Brighton", Color.WHITE, Color.BLACK, Color.RED), // 0.8
            Item("Summerside-Wilmot", Color.WHITE, Color.BLACK, Color.RED), // 1.0
            Item("Brackley-Hunter River", Color.WHITE, Color.BLACK, Color.RED), // 1.6
            Item("Summerside-South Drive", Color.WHITE, Color.BLACK, Color.RED), // 4.9
            Item("Charlottetown-West Royalty", Color.WHITE, Color.BLACK, Color.RED), // 7.3
            Item("O'Leary-Inverness", Color.WHITE, Color.BLACK, Color.RED), // 9.2
            Item("Montague-Kilmuir", Color.WHITE, Color.BLACK, Color.RED), // 10.8
            Item("Charlottetown-Victoria Park", Color.WHITE, Color.BLACK, Color.RED), // 11.9
            Item("Cornwall-Meadowbank", Color.WHITE, Color.BLACK, Color.RED), // 12.5
        )
        val items: Publisher<List<Item>> = Publisher(rawItems)
        val frame = ResultListingFrame(
            headerPublisher = "PC TARGETS".asOneTimePublisher(),
            borderColorPublisher = Color.BLUE.asOneTimePublisher(),
            numRowsPublisher = 10.asOneTimePublisher(),
            itemsPublisher = items.mapElements {
                ResultListingFrame.Item(
                    text = it.text.uppercase(),
                    border = it.border,
                    background = it.background,
                    foreground = it.foreground,
                )
            },
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
        items.submit(rawItems)
        compareRendering("ResultListingFrame", "FullColumn-2", frame)
    }

    @Test
    fun testVaryingItems() {
        val rawItems: MutableList<Item> = mutableListOf()
        val items: Publisher<List<Item>> = Publisher(rawItems)
        val frame = ResultListingFrame(
            headerPublisher = "SEATS CHANGING".asOneTimePublisher(),
            numRowsPublisher = 10.asOneTimePublisher(),
            itemsPublisher = items.mapElements {
                ResultListingFrame.Item(
                    text = it.text.uppercase(),
                    border = it.border,
                    background = it.background,
                    foreground = it.foreground,
                )
            },
        )
        frame.setSize(512, 256)
        compareRendering("ResultListingFrame", "Varying-1", frame)

        rawItems.add(Item("Montague-Kilmuir", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Brackley-Hunter River", Color.BLUE, Color.WHITE, Color.GRAY))
        rawItems.add(Item("Charlottetown-Victoria Park", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Summerside-South Drive", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.sortBy { it.text }
        items.submit(rawItems)
        compareRendering("ResultListingFrame", "Varying-2", frame)

        rawItems.add(Item("Mermaid-Stratford", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Charlottetown-Belvedere", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Stanhope-Marshfield", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Charlottetown-Brighton", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Alberton-Bloomfield", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Summerside-Wilmot", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Tyne Valley-Sherbrooke", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.sortBy { it.text }
        items.submit(rawItems)
        compareRendering("ResultListingFrame", "Varying-3", frame)
    }

    @Test
    fun testVaryingItemsInReverse() {
        val rawItems: MutableList<Item> = mutableListOf()
        val items: Publisher<List<Item>> = Publisher(rawItems)
        val frame = ResultListingFrame(
            headerPublisher = "SEATS CHANGING".asOneTimePublisher(),
            numRowsPublisher = 10.asOneTimePublisher(),
            reversedPublisher = true.asOneTimePublisher(),
            itemsPublisher = items.mapElements {
                ResultListingFrame.Item(
                    text = it.text.uppercase(),
                    border = it.border,
                    background = it.background,
                    foreground = it.foreground,
                )
            },
        )
        frame.setSize(512, 256)
        compareRendering("ResultListingFrame", "Reversed-1", frame)

        rawItems.add(Item("Montague-Kilmuir", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Brackley-Hunter River", Color.BLUE, Color.WHITE, Color.GRAY))
        rawItems.add(Item("Charlottetown-Victoria Park", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Summerside-South Drive", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.sortBy { it.text }
        items.submit(rawItems)
        compareRendering("ResultListingFrame", "Reversed-2", frame)

        rawItems.add(Item("Mermaid-Stratford", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Charlottetown-Belvedere", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Stanhope-Marshfield", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Charlottetown-Brighton", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Alberton-Bloomfield", Color.BLUE, Color.WHITE, Color.RED))
        rawItems.add(Item("Summerside-Wilmot", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.add(Item("Tyne Valley-Sherbrooke", Color.GREEN.darker(), Color.WHITE, Color.RED))
        rawItems.sortBy { it.text }
        items.submit(rawItems)
        compareRendering("ResultListingFrame", "Reversed-3", frame)
    }

    private class Item(var text: String, var background: Color, var foreground: Color, var border: Color)
}
