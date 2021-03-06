package com.joecollins.graphics.components

import com.joecollins.bindings.Binding.Companion.fixedBinding
import com.joecollins.bindings.IndexedBinding.Companion.listBinding
import com.joecollins.graphics.utils.RenderTestUtils.compareRendering
import java.awt.Color
import java.io.IOException
import kotlin.Throws
import org.junit.Assert
import org.junit.Test

class FiguresFrameTest {
    @Test
    fun testNamesAndDescriptions() {
        val frame = FiguresFrame()
        frame.setNumEntriesBinding(fixedBinding(3))
        frame.setNameBinding(
                listBinding("Justin Trudeau", "Andrew Scheer", "Jagmeet Singh"))
        frame.setDescriptionBinding(
                listBinding("Liberal Leader", "Conservative Leader", "NDP Leader"))
        frame.setColorBinding(listBinding(Color.RED, Color.BLUE, Color.ORANGE))
        Assert.assertEquals(3, frame.numEntries.toLong())
        Assert.assertEquals("Justin Trudeau", frame.getName(0))
        Assert.assertEquals("Andrew Scheer", frame.getName(1))
        Assert.assertEquals("Jagmeet Singh", frame.getName(2))
        Assert.assertEquals("Liberal Leader", frame.getDescription(0))
        Assert.assertEquals("Conservative Leader", frame.getDescription(1))
        Assert.assertEquals("NDP Leader", frame.getDescription(2))
        Assert.assertEquals(Color.RED, frame.getColor(0))
        Assert.assertEquals(Color.BLUE, frame.getColor(1))
        Assert.assertEquals(Color.ORANGE, frame.getColor(2))
    }

    @Test
    fun testResults() {
        val frame = FiguresFrame()
        frame.setNumEntriesBinding(fixedBinding(3))
        frame.setResultBinding(listBinding("LEADING", "ELECTED", "WAITING..."))
        frame.setResultColorBinding(
                listBinding(Color.RED, Color.BLUE, Color.LIGHT_GRAY))
        Assert.assertEquals(3, frame.numEntries.toLong())
        Assert.assertEquals("LEADING", frame.getResult(0))
        Assert.assertEquals("ELECTED", frame.getResult(1))
        Assert.assertEquals("WAITING...", frame.getResult(2))
        Assert.assertEquals(Color.RED, frame.getResultColor(0))
        Assert.assertEquals(Color.BLUE, frame.getResultColor(1))
        Assert.assertEquals(Color.LIGHT_GRAY, frame.getResultColor(2))
    }

    @Test
    @Throws(IOException::class)
    fun testRenderEntries() {
        val frame = FiguresFrame()
        frame.setHeaderBinding(fixedBinding<String?>("PARTY LEADERS"))
        frame.setNumEntriesBinding(fixedBinding(3))
        frame.setNameBinding(
                listBinding("JUSTIN TRUDEAU", "ANDREW SCHEER", "JAGMEET SINGH"))
        frame.setDescriptionBinding(
                listBinding("Liberal Leader", "Conservative Leader", "NDP Leader"))
        frame.setColorBinding(listBinding(Color.RED, Color.BLUE, Color.ORANGE))
        frame.setResultBinding(listBinding("LEADING", "ELECTED", "WAITING..."))
        frame.setResultColorBinding(
                listBinding(Color.RED, Color.BLUE, Color.LIGHT_GRAY))
        frame.setSize(512, 256)
        compareRendering("FiguresFrame", "Entries", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderOverflow() {
        val frame = FiguresFrame()
        frame.setHeaderBinding(fixedBinding<String?>("PARTY LEADERS"))
        frame.setNumEntriesBinding(fixedBinding(9))
        frame.setNameBinding(
                listBinding(
                        "JUSTIN TRUDEAU",
                        "ANDREW SCHEER",
                        "JAGMEET SINGH",
                        "YVES-FRAN\u00c7OIS BLANCHET",
                        "ELIZABETH MAY",
                        "MAXIME BERNIER",
                        "ROD TAYLOR",
                        "S\u00c9BASTIEN CORHINO",
                        "TIM MOEN"))
        frame.setDescriptionBinding(
                listBinding(
                        "Liberal Leader, Papineau",
                        "Conservative Leader, Regina-Qu'Apelle",
                        "NDP Leader, Burnaby South",
                        "Bloc Qu\u00e9b\u00e9cois Leader, Beloeil-Chambly",
                        "Green Leader, Saanich-Gulf Islands",
                        "People's Party Leader, Beauce",
                        "CHP Leader, Skeena-Bulkley Valley",
                        "Rhinoceros Party Leader, Qu\u00e9bec",
                        "Libertarian Leader, Fort McMurray-Athabasca"))
        frame.setColorBinding(
                listBinding(
                        Color.RED,
                        Color.BLUE,
                        Color.ORANGE,
                        Color.CYAN.darker(),
                        Color.GREEN.darker(),
                        Color.MAGENTA.darker(),
                        Color.MAGENTA,
                        Color.GRAY,
                        Color.YELLOW.darker()))
        frame.setResultBinding(
                listBinding(
                        "ELECTED",
                        "ELECTED",
                        "ELECTED",
                        "ELECTED",
                        "ELECTED",
                        "DEFEATED",
                        "DEFEATED",
                        "DEFEATED",
                        "DEFEATED"))
        frame.setResultColorBinding(
                listBinding(
                        Color.RED,
                        Color.BLUE,
                        Color.ORANGE,
                        Color.CYAN.darker(),
                        Color.GREEN.darker(),
                        Color.BLUE,
                        Color.ORANGE,
                        Color.RED,
                        Color.BLUE))
        frame.setSize(512, 256)
        compareRendering("FiguresFrame", "Overflow", frame)
    }

    @Test
    @Throws(IOException::class)
    fun testRenderLongStrings() {
        val frame = FiguresFrame()
        frame.setHeaderBinding(fixedBinding<String?>("PARTY LEADERS"))
        frame.setNumEntriesBinding(fixedBinding(9))
        frame.setNameBinding(
                listBinding(
                        "JUSTIN TRUDEAU",
                        "ANDREW SCHEER",
                        "JAGMEET SINGH",
                        "YVES-FRAN\u00c7OIS BLANCHET",
                        "ELIZABETH MAY",
                        "MAXIME BERNIER",
                        "ROD TAYLOR",
                        "S\u00c9BASTIEN CORHINO",
                        "TIM MOEN"))
        frame.setDescriptionBinding(
                listBinding(
                        "Liberal Leader, Papineau",
                        "Conservative Leader, Regina-Qu'Apelle",
                        "NDP Leader, Burnaby South",
                        "Bloc Qu\u00e9b\u00e9cois Leader, Beloeil-Chambly",
                        "Green Leader, Saanich-Gulf Islands",
                        "People's Party Leader, Beauce",
                        "CHP Leader, Skeena-Bulkley Valley",
                        "Rhinoceros Party Leader, Qu\u00e9bec",
                        "Libertarian Leader, Fort McMurray-Athabasca"))
        frame.setColorBinding(
                listBinding(
                        Color.RED,
                        Color.BLUE,
                        Color.ORANGE,
                        Color.CYAN.darker(),
                        Color.GREEN.darker(),
                        Color.MAGENTA.darker(),
                        Color.MAGENTA,
                        Color.GRAY,
                        Color.YELLOW.darker()))
        frame.setResultBinding(
                listBinding(
                        "ELECTED",
                        "ELECTED",
                        "ELECTED",
                        "ELECTED",
                        "ELECTED",
                        "DEFEATED",
                        "DEFEATED",
                        "DEFEATED",
                        "DEFEATED"))
        frame.setResultColorBinding(
                listBinding(
                        Color.RED,
                        Color.BLUE,
                        Color.ORANGE,
                        Color.CYAN.darker(),
                        Color.GREEN.darker(),
                        Color.BLUE,
                        Color.ORANGE,
                        Color.RED,
                        Color.BLUE))
        frame.setSize(128, 256)
        compareRendering("FiguresFrame", "LongStrings", frame)
    }
}
