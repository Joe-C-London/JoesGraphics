package com.joecollins.graphics.components

import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import com.joecollins.pubsub.asOneTimePublisher
import org.awaitility.Awaitility
import org.hamcrest.core.IsEqual
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.awt.Color
import java.util.concurrent.TimeUnit

class FiguresFrameTest {
    @Test
    fun testNamesAndDescriptions() {
        val frame = FiguresFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            entriesPublisher =
            listOf(
                FiguresFrame.Entry(Color.RED, "Justin Trudeau", "Liberal Leader", "", Color.BLACK),
                FiguresFrame.Entry(Color.BLUE, "Andrew Scheer", "Conservative Leader", "", Color.BLACK),
                FiguresFrame.Entry(Color.ORANGE, "Jagmeet Singh", "NDP Leader", "", Color.BLACK)
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numEntries }, IsEqual(3))
        Assertions.assertEquals("Justin Trudeau", frame.getName(0))
        Assertions.assertEquals("Andrew Scheer", frame.getName(1))
        Assertions.assertEquals("Jagmeet Singh", frame.getName(2))
        Assertions.assertEquals("Liberal Leader", frame.getDescription(0))
        Assertions.assertEquals("Conservative Leader", frame.getDescription(1))
        Assertions.assertEquals("NDP Leader", frame.getDescription(2))
        Assertions.assertEquals(Color.RED, frame.getColor(0))
        Assertions.assertEquals(Color.BLUE, frame.getColor(1))
        Assertions.assertEquals(Color.ORANGE, frame.getColor(2))
    }

    @Test
    fun testResults() {
        val frame = FiguresFrame(
            headerPublisher = (null as String?).asOneTimePublisher(),
            entriesPublisher =
            listOf(
                FiguresFrame.Entry(Color.RED, "Justin Trudeau", "Liberal Leader", "LEADING", Color.RED),
                FiguresFrame.Entry(Color.BLUE, "Andrew Scheer", "Conservative Leader", "ELECTED", Color.BLUE),
                FiguresFrame.Entry(Color.ORANGE, "Jagmeet Singh", "NDP Leader", "WAITING...", Color.LIGHT_GRAY)
            )
                .asOneTimePublisher()
        )
        Awaitility.await().atMost(500, TimeUnit.MILLISECONDS)
            .until({ frame.numEntries }, IsEqual(3))
        Assertions.assertEquals("LEADING", frame.getResult(0))
        Assertions.assertEquals("ELECTED", frame.getResult(1))
        Assertions.assertEquals("WAITING...", frame.getResult(2))
        Assertions.assertEquals(Color.RED, frame.getResultColor(0))
        Assertions.assertEquals(Color.BLUE, frame.getResultColor(1))
        Assertions.assertEquals(Color.LIGHT_GRAY, frame.getResultColor(2))
    }

    @Test
    fun testRenderEntries() {
        val frame = FiguresFrame(
            headerPublisher = "PARTY LEADERS".asOneTimePublisher(),
            entriesPublisher =
            listOf(
                FiguresFrame.Entry(Color.RED, "JUSTIN TRUDEAU", "Liberal Leader", "LEADING", Color.RED),
                FiguresFrame.Entry(Color.BLUE, "ANDREW SCHEER", "Conservative Leader", "ELECTED", Color.BLUE),
                FiguresFrame.Entry(Color.ORANGE, "JAGMEET SINGH", "NDP Leader", "WAITING...", Color.LIGHT_GRAY)
            )
                .asOneTimePublisher()
        )
        frame.setSize(512, 256)
        compareRendering("FiguresFrame", "Entries", frame)
    }

    @Test
    fun testRenderOverflow() {
        val frame = FiguresFrame(
            headerPublisher = "PARTY LEADERS".asOneTimePublisher(),
            entriesPublisher =
            listOf(
                FiguresFrame.Entry(Color.RED, "JUSTIN TRUDEAU", "Liberal Leader, Papineau", "ELECTED", Color.RED),
                FiguresFrame.Entry(Color.BLUE, "ANDREW SCHEER", "Conservative Leader, Regina-Qu'Apelle", "ELECTED", Color.BLUE),
                FiguresFrame.Entry(Color.ORANGE, "JAGMEET SINGH", "NDP Leader, Burnaby South", "ELECTED", Color.ORANGE),
                FiguresFrame.Entry(Color.CYAN.darker(), "YVES-FRAN\u00c7OIS BLANCHET", "Bloc Qu\u00e9b\u00e9cois Leader, Beloeil-Chambly", "ELECTED", Color.CYAN.darker()),
                FiguresFrame.Entry(Color.GREEN.darker(), "ELIZABETH MAY", "Green Leader, Saanich-Gulf Islands", "ELECTED", Color.GREEN.darker()),
                FiguresFrame.Entry(Color.MAGENTA.darker(), "MAXIME BERNIER", "People's Party Leader, Beauce", "DEFEATED", Color.BLUE),
                FiguresFrame.Entry(Color.MAGENTA, "ROD TAYLOR", "CHP Leader, Skeena-Bulkley Valley", "DEFEATED", Color.ORANGE),
                FiguresFrame.Entry(Color.GRAY, "S\u00c9BASTIEN CORHINO", "Rhinoceros Party Leader, Qu\u00e9bec", "DEFEATED", Color.RED),
                FiguresFrame.Entry(Color.YELLOW.darker(), "TIM MOEN", "Libertarian Leader, Fort McMurray-Athabasca", "DEFEATED", Color.BLUE)
            )
                .asOneTimePublisher()
        )
        frame.setSize(512, 256)
        compareRendering("FiguresFrame", "Overflow", frame)
    }

    @Test
    fun testRenderLongStrings() {
        val frame = FiguresFrame(
            headerPublisher = "PARTY LEADERS".asOneTimePublisher(),
            entriesPublisher =
            listOf(
                FiguresFrame.Entry(Color.RED, "JUSTIN TRUDEAU", "Liberal Leader, Papineau", "ELECTED", Color.RED),
                FiguresFrame.Entry(Color.BLUE, "ANDREW SCHEER", "Conservative Leader, Regina-Qu'Apelle", "ELECTED", Color.BLUE),
                FiguresFrame.Entry(Color.ORANGE, "JAGMEET SINGH", "NDP Leader, Burnaby South", "ELECTED", Color.ORANGE),
                FiguresFrame.Entry(Color.CYAN.darker(), "YVES-FRAN\u00c7OIS BLANCHET", "Bloc Qu\u00e9b\u00e9cois Leader, Beloeil-Chambly", "ELECTED", Color.CYAN.darker() ),
                FiguresFrame.Entry(Color.GREEN.darker(), "ELIZABETH MAY", "Green Leader, Saanich-Gulf Islands", "ELECTED", Color.GREEN.darker()),
                FiguresFrame.Entry(Color.MAGENTA.darker(), "MAXIME BERNIER", "People's Party Leader, Beauce", "DEFEATED", Color.BLUE),
                FiguresFrame.Entry(Color.MAGENTA, "ROD TAYLOR", "CHP Leader, Skeena-Bulkley Valley", "DEFEATED", Color.ORANGE),
                FiguresFrame.Entry(Color.GRAY, "S\u00c9BASTIEN CORHINO", "Rhinoceros Party Leader, Qu\u00e9bec", "DEFEATED", Color.RED),
                FiguresFrame.Entry(Color.YELLOW.darker(), "TIM MOEN", "Libertarian Leader, Fort McMurray-Athabasca", "DEFEATED", Color.BLUE)
            )
                .asOneTimePublisher()
        )
        frame.setSize(128, 256)
        compareRendering("FiguresFrame", "LongStrings", frame)
    }
}