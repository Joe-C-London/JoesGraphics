package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.io.IOException;
import org.junit.Test;

public class FiguresFrameTest {

  @Test
  public void testNamesAndDescriptions() {
    FiguresFrame frame = new FiguresFrame();
    frame.setNumEntriesBinding(Binding.fixedBinding(3));
    frame.setNameBinding(
        IndexedBinding.listBinding("Justin Trudeau", "Andrew Scheer", "Jagmeet Singh"));
    frame.setDescriptionBinding(
        IndexedBinding.listBinding("Liberal Leader", "Conservative Leader", "NDP Leader"));
    frame.setColorBinding(IndexedBinding.listBinding(Color.RED, Color.BLUE, Color.ORANGE));

    assertEquals(3, frame.getNumEntries());

    assertEquals("Justin Trudeau", frame.getName(0));
    assertEquals("Andrew Scheer", frame.getName(1));
    assertEquals("Jagmeet Singh", frame.getName(2));

    assertEquals("Liberal Leader", frame.getDescription(0));
    assertEquals("Conservative Leader", frame.getDescription(1));
    assertEquals("NDP Leader", frame.getDescription(2));

    assertEquals(Color.RED, frame.getColor(0));
    assertEquals(Color.BLUE, frame.getColor(1));
    assertEquals(Color.ORANGE, frame.getColor(2));
  }

  @Test
  public void testResults() {
    FiguresFrame frame = new FiguresFrame();
    frame.setNumEntriesBinding(Binding.fixedBinding(3));
    frame.setResultBinding(IndexedBinding.listBinding("LEADING", "ELECTED", "WAITING..."));
    frame.setResultColorBinding(
        IndexedBinding.listBinding(Color.RED, Color.BLUE, Color.LIGHT_GRAY));

    assertEquals(3, frame.getNumEntries());

    assertEquals("LEADING", frame.getResult(0));
    assertEquals("ELECTED", frame.getResult(1));
    assertEquals("WAITING...", frame.getResult(2));

    assertEquals(Color.RED, frame.getResultColor(0));
    assertEquals(Color.BLUE, frame.getResultColor(1));
    assertEquals(Color.LIGHT_GRAY, frame.getResultColor(2));
  }

  @Test
  public void testRenderEntries() throws IOException {
    FiguresFrame frame = new FiguresFrame();
    frame.setHeaderBinding(Binding.fixedBinding("PARTY LEADERS"));
    frame.setNumEntriesBinding(Binding.fixedBinding(3));
    frame.setNameBinding(
        IndexedBinding.listBinding("JUSTIN TRUDEAU", "ANDREW SCHEER", "JAGMEET SINGH"));
    frame.setDescriptionBinding(
        IndexedBinding.listBinding("Liberal Leader", "Conservative Leader", "NDP Leader"));
    frame.setColorBinding(IndexedBinding.listBinding(Color.RED, Color.BLUE, Color.ORANGE));
    frame.setResultBinding(IndexedBinding.listBinding("LEADING", "ELECTED", "WAITING..."));
    frame.setResultColorBinding(
        IndexedBinding.listBinding(Color.RED, Color.BLUE, Color.LIGHT_GRAY));
    frame.setSize(512, 256);

    compareRendering("FiguresFrame", "Entries", frame);
  }

  @Test
  public void testRenderOverflow() throws IOException {
    FiguresFrame frame = new FiguresFrame();
    frame.setHeaderBinding(Binding.fixedBinding("PARTY LEADERS"));
    frame.setNumEntriesBinding(Binding.fixedBinding(9));
    frame.setNameBinding(
        IndexedBinding.listBinding(
            "JUSTIN TRUDEAU",
            "ANDREW SCHEER",
            "JAGMEET SINGH",
            "YVES-FRAN\u00c7OIS BLANCHET",
            "ELIZABETH MAY",
            "MAXIME BERNIER",
            "ROD TAYLOR",
            "S\u00c9BASTIEN CORHINO",
            "TIM MOEN"));
    frame.setDescriptionBinding(
        IndexedBinding.listBinding(
            "Liberal Leader, Papineau",
            "Conservative Leader, Regina-Qu'Apelle",
            "NDP Leader, Burnaby South",
            "Bloc Qu\u00e9b\u00e9cois Leader, Beloeil-Chambly",
            "Green Leader, Saanich-Gulf Islands",
            "People's Party Leader, Beauce",
            "CHP Leader, Skeena-Bulkley Valley",
            "Rhinoceros Party Leader, Qu\u00e9bec",
            "Libertarian Leader, Fort McMurray-Athabasca"));
    frame.setColorBinding(
        IndexedBinding.listBinding(
            Color.RED,
            Color.BLUE,
            Color.ORANGE,
            Color.CYAN.darker(),
            Color.GREEN.darker(),
            Color.MAGENTA.darker(),
            Color.MAGENTA,
            Color.GRAY,
            Color.YELLOW.darker()));
    frame.setResultBinding(
        IndexedBinding.listBinding(
            "ELECTED",
            "ELECTED",
            "ELECTED",
            "ELECTED",
            "ELECTED",
            "DEFEATED",
            "DEFEATED",
            "DEFEATED",
            "DEFEATED"));
    frame.setResultColorBinding(
        IndexedBinding.listBinding(
            Color.RED,
            Color.BLUE,
            Color.ORANGE,
            Color.CYAN.darker(),
            Color.GREEN.darker(),
            Color.BLUE,
            Color.ORANGE,
            Color.RED,
            Color.BLUE));
    frame.setSize(512, 256);

    compareRendering("FiguresFrame", "Overflow", frame);
  }

  @Test
  public void testRenderLongStrings() throws IOException {
    FiguresFrame frame = new FiguresFrame();
    frame.setHeaderBinding(Binding.fixedBinding("PARTY LEADERS"));
    frame.setNumEntriesBinding(Binding.fixedBinding(9));
    frame.setNameBinding(
        IndexedBinding.listBinding(
            "JUSTIN TRUDEAU",
            "ANDREW SCHEER",
            "JAGMEET SINGH",
            "YVES-FRAN\u00c7OIS BLANCHET",
            "ELIZABETH MAY",
            "MAXIME BERNIER",
            "ROD TAYLOR",
            "S\u00c9BASTIEN CORHINO",
            "TIM MOEN"));
    frame.setDescriptionBinding(
        IndexedBinding.listBinding(
            "Liberal Leader, Papineau",
            "Conservative Leader, Regina-Qu'Apelle",
            "NDP Leader, Burnaby South",
            "Bloc Qu\u00e9b\u00e9cois Leader, Beloeil-Chambly",
            "Green Leader, Saanich-Gulf Islands",
            "People's Party Leader, Beauce",
            "CHP Leader, Skeena-Bulkley Valley",
            "Rhinoceros Party Leader, Qu\u00e9bec",
            "Libertarian Leader, Fort McMurray-Athabasca"));
    frame.setColorBinding(
        IndexedBinding.listBinding(
            Color.RED,
            Color.BLUE,
            Color.ORANGE,
            Color.CYAN.darker(),
            Color.GREEN.darker(),
            Color.MAGENTA.darker(),
            Color.MAGENTA,
            Color.GRAY,
            Color.YELLOW.darker()));
    frame.setResultBinding(
        IndexedBinding.listBinding(
            "ELECTED",
            "ELECTED",
            "ELECTED",
            "ELECTED",
            "ELECTED",
            "DEFEATED",
            "DEFEATED",
            "DEFEATED",
            "DEFEATED"));
    frame.setResultColorBinding(
        IndexedBinding.listBinding(
            Color.RED,
            Color.BLUE,
            Color.ORANGE,
            Color.CYAN.darker(),
            Color.GREEN.darker(),
            Color.BLUE,
            Color.ORANGE,
            Color.RED,
            Color.BLUE));
    frame.setSize(128, 256);

    compareRendering("FiguresFrame", "LongStrings", frame);
  }
}
