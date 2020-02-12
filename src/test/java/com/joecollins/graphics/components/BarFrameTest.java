package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

public class BarFrameTest {

  private static final DecimalFormat CHANGE_FORMAT = new DecimalFormat("+0;-0");
  private static final DecimalFormat THOUSANDS_FORMAT = new DecimalFormat("#,##0");
  private static final DecimalFormat PERCENT_FORMAT = new DecimalFormat("0.0%");

  @Test
  public void testNumBars() {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1));

    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    assertEquals(6, frame.getNumBars());
  }

  @Test
  public void testAddRemoveBars() {
    BindableList<ElectionResult> results = new BindableList<>();
    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    assertEquals(0, frame.getNumBars());

    results.add(new ElectionResult("LIBERAL", Color.RED, 1));
    assertEquals(1, frame.getNumBars());

    results.addAll(
        Arrays.asList(
            new ElectionResult("CONSERVATIVE", Color.BLUE, 1),
            new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1)));
    assertEquals(3, frame.getNumBars());

    results.remove(2);
    assertEquals(2, frame.getNumBars());

    results.removeIf(er -> !er.getPartyName().equals("LIBERAL"));
    assertEquals(1, frame.getNumBars());

    results.clear();
    assertEquals(0, frame.getNumBars());
  }

  @Test
  public void testLeftTextBinding() {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1));

    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    assertEquals("LIBERAL", frame.getLeftText(0));
    assertEquals("CONSERVATIVE", frame.getLeftText(1));
    assertEquals("BLOC QUEBECOIS", frame.getLeftText(2));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(3));
    assertEquals("GREEN", frame.getLeftText(4));
    assertEquals("INDEPENDENT", frame.getLeftText(5));
  }

  @Test
  public void testRightTextBinding() {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1));

    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.setRightTextBinding(
        IndexedBinding.propertyBinding(results, r -> String.valueOf(r.getNumSeats()), "NumSeats"));
    assertEquals("157", frame.getRightText(0));
    assertEquals("121", frame.getRightText(1));
    assertEquals("32", frame.getRightText(2));
    assertEquals("24", frame.getRightText(3));
    assertEquals("3", frame.getRightText(4));
    assertEquals("1", frame.getRightText(5));
  }

  @Test
  public void testSeriesBinding() {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 2, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 1, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 0, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 0, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 0, 1));

    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    frame.addSeriesBinding(
        "Estimate",
        IndexedBinding.propertyBinding(results, r -> lighten(r.getPartyColor()), "PartyColor"),
        IndexedBinding.propertyBinding(
            results, r -> r.getSeatEstimate() - r.getNumSeats(), "NumSeats", "SeatEstimate"));

    Color lightRed = new Color(255, 127, 127);
    List<Pair<Color, Number>> libSeries = frame.getSeries(0);
    assertEquals(Color.RED, libSeries.get(0).getLeft());
    assertEquals(2, libSeries.get(0).getRight().intValue());
    assertEquals(lightRed, libSeries.get(1).getLeft());
    assertEquals(155, libSeries.get(1).getRight().intValue());

    results.get(0).setSeatEstimate(158);
    libSeries = frame.getSeries(0);
    assertEquals(Color.RED, libSeries.get(0).getLeft());
    assertEquals(2, libSeries.get(0).getRight().intValue());
    assertEquals(lightRed, libSeries.get(1).getLeft());
    assertEquals(156, libSeries.get(1).getRight().intValue());

    results.get(0).setNumSeats(3);
    libSeries = frame.getSeries(0);
    assertEquals(Color.RED, libSeries.get(0).getLeft());
    assertEquals(3, libSeries.get(0).getRight().intValue());
    assertEquals(lightRed, libSeries.get(1).getLeft());
    assertEquals(155, libSeries.get(1).getRight().intValue());
  }

  @Test
  public void testLeftIconBinding() {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1));

    Shape shape = new Ellipse2D.Double();
    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.setLeftIconBinding(
        IndexedBinding.propertyBinding(
            results, r -> r.getNumSeats() > 150 ? shape : null, "NumSeats"));
    assertEquals(shape, frame.getLeftIcon(0));
    assertNull(frame.getLeftIcon(1));
    assertNull(frame.getLeftIcon(2));
    assertNull(frame.getLeftIcon(3));
    assertNull(frame.getLeftIcon(4));
    assertNull(frame.getLeftIcon(5));
  }

  @Test
  public void testMinMax() {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1));

    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));

    assertEquals(157, frame.getMax().intValue());
    assertEquals(0, frame.getMin().intValue());

    results.get(0).setNumSeats(-27);
    results.get(1).setNumSeats(22);
    results.get(2).setNumSeats(22);
    results.get(3).setNumSeats(-20);
    results.get(4).setNumSeats(2);
    results.get(5).setNumSeats(1);

    assertEquals(22, frame.getMax().intValue());
    assertEquals(-27, frame.getMin().intValue());

    frame.setMinBinding(Binding.fixedBinding(-30));
    frame.setMaxBinding(Binding.fixedBinding(30));
    assertEquals(30, frame.getMax().intValue());
    assertEquals(-30, frame.getMin().intValue());
  }

  @Test
  public void testSubheadText() {
    BarFrame frame = new BarFrame();
    frame.setSubheadTextBinding(Binding.fixedBinding("PROJECTION: LIB MINORITY"));
    assertEquals("PROJECTION: LIB MINORITY", frame.getSubheadText());
  }

  @Test
  public void testDefaultSubheadColor() {
    BarFrame frame = new BarFrame();
    assertEquals(Color.BLACK, frame.getSubheadColor());
  }

  @Test
  public void testSubheadColor() {
    BarFrame frame = new BarFrame();
    frame.setSubheadColorBinding(Binding.fixedBinding(Color.RED));
    assertEquals(Color.RED, frame.getSubheadColor());
  }

  @Test
  public void testLines() {
    BarFrame frame = new BarFrame();
    frame.setNumLinesBinding(Binding.fixedBinding(1));
    frame.setLineLevelsBinding(IndexedBinding.singletonBinding(170));
    frame.setLineLabelsBinding(IndexedBinding.singletonBinding("170 SEATS FOR MAJORITY"));

    assertEquals(1, frame.getNumLines());
    assertEquals(170, frame.getLineLevel(0));
    assertEquals("170 SEATS FOR MAJORITY", frame.getLineLabel(0));
  }

  @Test
  public void testUnbind() {
    BindableList<ElectionResult> results = new BindableList<>();
    ElectionResult con = new ElectionResult("CONSERVATIVE", Color.BLUE, 1);
    ElectionResult lib = new ElectionResult("LIBERAL", Color.RED, 1);
    ElectionResult ndp = new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1);
    results.addAll(Arrays.asList(con, lib, ndp));
    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    assertEquals(3, frame.getNumBars());
    assertEquals("CONSERVATIVE", frame.getLeftText(0));
    assertEquals("LIBERAL", frame.getLeftText(1));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2));

    BindableList<ElectionResult> differentResults = new BindableList<>();
    differentResults.add(new ElectionResult("GREEN", Color.GREEN, 1));
    frame.setNumBarsBinding(Binding.sizeBinding(differentResults));
    frame.setLeftTextBinding(
        IndexedBinding.propertyBinding(
            differentResults, ElectionResult::getPartyName, "PartyName"));
    assertEquals(1, frame.getNumBars());
    assertEquals("GREEN", frame.getLeftText(0));

    results.remove(0);
    assertEquals(1, frame.getNumBars());
    assertEquals("GREEN", frame.getLeftText(0));

    results.get(0).setPartyName("HAHA");
    assertEquals(1, frame.getNumBars());
    assertEquals("GREEN", frame.getLeftText(0));

    differentResults.get(0).setPartyName("GREENS");
    assertEquals(1, frame.getNumBars());
    assertEquals("GREENS", frame.getLeftText(0));

    differentResults.add(0, new ElectionResult("LIBERAL", Color.RED, 1));
    assertEquals(2, frame.getNumBars());
    assertEquals("LIBERAL", frame.getLeftText(0));
    assertEquals("GREENS", frame.getLeftText(1));

    differentResults.get(1).setPartyName("GREEN");
    assertEquals(2, frame.getNumBars());
    assertEquals("LIBERAL", frame.getLeftText(0));
    assertEquals("GREEN", frame.getLeftText(1));
  }

  @Test
  public void testLeftTextBindingOnAdd() {
    BindableList<ElectionResult> results = new BindableList<>();
    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    assertEquals(0, frame.getNumBars());

    results.add(new ElectionResult("LIBERAL", Color.RED, 1));
    assertEquals(1, frame.getNumBars());
    assertEquals("LIBERAL", frame.getLeftText(0));

    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1));
    assertEquals(2, frame.getNumBars());
    assertEquals("LIBERAL", frame.getLeftText(0));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(1));

    results.add(0, new ElectionResult("CONSERVATIVE", Color.BLUE, 1));
    assertEquals(3, frame.getNumBars());
    assertEquals("CONSERVATIVE", frame.getLeftText(0));
    assertEquals("LIBERAL", frame.getLeftText(1));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2));

    results.get(0).setPartyName("CONSERVATIVES");
    assertEquals(3, frame.getNumBars());
    assertEquals("CONSERVATIVES", frame.getLeftText(0));
    assertEquals("LIBERAL", frame.getLeftText(1));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2));
  }

  @Test
  public void testLeftTextBindingOnRemove() {
    BindableList<ElectionResult> results = new BindableList<>();
    ElectionResult con = new ElectionResult("CONSERVATIVE", Color.BLUE, 1);
    ElectionResult lib = new ElectionResult("LIBERAL", Color.RED, 1);
    ElectionResult ndp = new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1);
    results.addAll(Arrays.asList(con, lib, ndp));
    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    assertEquals(3, frame.getNumBars());
    assertEquals("CONSERVATIVE", frame.getLeftText(0));
    assertEquals("LIBERAL", frame.getLeftText(1));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2));

    results.remove(lib);
    assertEquals(2, frame.getNumBars());
    assertEquals("CONSERVATIVE", frame.getLeftText(0));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(1));

    lib.setPartyName("LIBERALS");
    assertEquals(2, frame.getNumBars());
    assertEquals("CONSERVATIVE", frame.getLeftText(0));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(1));

    ndp.setPartyName("NDP");
    assertEquals(2, frame.getNumBars());
    assertEquals("CONSERVATIVE", frame.getLeftText(0));
    assertEquals("NDP", frame.getLeftText(1));

    results.remove(1);
    assertEquals(1, frame.getNumBars());
    assertEquals("CONSERVATIVE", frame.getLeftText(0));
  }

  @Test
  public void testLeftTextBindingOnSet() {
    BindableList<ElectionResult> results = new BindableList<>();
    ElectionResult con = new ElectionResult("CONSERVATIVE", Color.BLUE, 2);
    ElectionResult lib = new ElectionResult("LIBERAL", Color.RED, 3);
    ElectionResult ndp = new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 1);
    results.addAll(Arrays.asList(con, lib, ndp));
    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    assertEquals(3, frame.getNumBars());
    assertEquals("CONSERVATIVE", frame.getLeftText(0));
    assertEquals("LIBERAL", frame.getLeftText(1));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2));

    results.sort(Comparator.comparingInt(ElectionResult::getNumSeats).reversed());
    assertEquals(3, frame.getNumBars());
    assertEquals("LIBERAL", frame.getLeftText(0));
    assertEquals("CONSERVATIVE", frame.getLeftText(1));
    assertEquals("NEW DEMOCRATIC PARTY", frame.getLeftText(2));

    con.setPartyName("CONSERVATIVES");
    lib.setPartyName("LIBERALS");
    ndp.setPartyName("NDP");
    assertEquals(3, frame.getNumBars());
    assertEquals("LIBERALS", frame.getLeftText(0));
    assertEquals("CONSERVATIVES", frame.getLeftText(1));
    assertEquals("NDP", frame.getLeftText(2));
  }

  @Test
  public void testTestNonBindableElements() {
    BindableList<Triple<String, Color, Integer>> results = new BindableList<>();
    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.sizeBinding(results));
    frame.setLeftTextBinding(IndexedBinding.propertyBinding(results, Triple::getLeft));
    assertEquals(0, frame.getNumBars());

    results.add(new ImmutableTriple<>("NDP", Color.ORANGE, 1));
    assertEquals(1, frame.getNumBars());
    assertEquals("NDP", frame.getLeftText(0));

    results.setAll(
        Arrays.asList(
            new ImmutableTriple<>("LIBERALS", Color.RED, 3),
            new ImmutableTriple<>("CONSERVATIVES", Color.BLUE, 2),
            new ImmutableTriple<>("NDP", Color.ORANGE, 1)));
    assertEquals(3, frame.getNumBars());
    assertEquals("LIBERALS", frame.getLeftText(0));
    assertEquals("CONSERVATIVES", frame.getLeftText(1));
    assertEquals("NDP", frame.getLeftText(2));

    results.setAll(
        Arrays.asList(
            new ImmutableTriple<>("LIBERALS", Color.RED, 3),
            new ImmutableTriple<>("CONSERVATIVES", Color.BLUE, 3)));
    assertEquals(2, frame.getNumBars());
    assertEquals("LIBERALS", frame.getLeftText(0));
    assertEquals("CONSERVATIVES", frame.getLeftText(1));
  }

  @Test
  public void testOtherBindings() {
    BarFrame frame = new BarFrame();
    frame.setNumBarsBinding(Binding.fixedBinding(3));
    frame.setLeftTextBinding(IndexedBinding.listBinding("LIBERAL", "CONSERVATIVE", "NDP"));
    frame.setRightTextBinding(IndexedBinding.functionBinding(3, 6, String::valueOf));

    assertEquals(3, frame.getNumBars());
    assertEquals("LIBERAL", frame.getLeftText(0));
    assertEquals("CONSERVATIVE", frame.getLeftText(1));
    assertEquals("NDP", frame.getLeftText(2));
    assertEquals("3", frame.getRightText(0));
    assertEquals("4", frame.getRightText(1));
    assertEquals("5", frame.getRightText(2));

    ElectionResult result = new ElectionResult(null, null, 3);
    frame.setNumLinesBinding(
        Binding.propertyBinding(result, ElectionResult::getNumSeats, "NumSeats"));
    frame.setLineLevelsBinding(
        IndexedBinding.propertyBinding(
            result,
            r -> IntStream.rangeClosed(1, r.numSeats).boxed().collect(Collectors.toList()),
            "NumSeats"));
    assertEquals(3, frame.getNumLines());
    assertEquals(1, frame.getLineLevel(0));
    assertEquals(2, frame.getLineLevel(1));
    assertEquals(3, frame.getLineLevel(2));

    result.setNumSeats(2);
    assertEquals(2, frame.getNumLines());
    assertEquals(1, frame.getLineLevel(0));
    assertEquals(2, frame.getLineLevel(1));

    frame.setNumLinesBinding(Binding.fixedBinding(4));
    frame.setLineLevelsBinding(IndexedBinding.listBinding(3, 4, 5, 6));
    assertEquals(4, frame.getNumLines());
    assertEquals(3, frame.getLineLevel(0));
    assertEquals(4, frame.getLineLevel(1));
    assertEquals(5, frame.getLineLevel(2));
    assertEquals(6, frame.getLineLevel(3));

    result.setNumSeats(1);
    assertEquals(4, frame.getNumLines());
    assertEquals(3, frame.getLineLevel(0));
    assertEquals(4, frame.getLineLevel(1));
    assertEquals(5, frame.getLineLevel(2));
    assertEquals(6, frame.getLineLevel(3));
  }

  private static Color lighten(Color color) {
    return new Color(
        (color.getRed() + 255) / 2, (color.getGreen() + 255) / 2, (color.getBlue() + 255) / 2);
  }

  @Test
  public void testRenderSingleSeriesAllPositive() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("2019 CANADIAN ELECTION RESULT"));
    barFrame.setMaxBinding(Binding.fixedBinding(160));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(results, r -> String.valueOf(r.getNumSeats()), "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "SingleSeriesAllPositive", barFrame);
  }

  @Test
  public void testRenderSingleSeriesWithSubhead() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 1));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("2019 CANADIAN ELECTION RESULT"));
    barFrame.setMaxBinding(Binding.fixedBinding(160));
    barFrame.setSubheadTextBinding(Binding.fixedBinding("PROJECTION: LIB MINORITY"));
    barFrame.setSubheadColorBinding(Binding.fixedBinding(Color.RED));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(results, r -> String.valueOf(r.getNumSeats()), "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "SingleSeriesWithSubhead", barFrame);
  }

  @Test
  public void testRenderSingleSeriesShrinkToFit() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 177));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 95));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 39));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 10));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 8));
    results.add(new ElectionResult("GREEN", Color.GREEN, 2));
    results.add(
        new ElectionResult("CO-OPERATIVE COMMONWEALTH FEDERATION", Color.ORANGE.darker(), 1));
    results.add(new ElectionResult("PEOPLE'S PARTY", Color.MAGENTA.darker(), 1));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("SEATS AT DISSOLUTION"));
    barFrame.setSubheadTextBinding(Binding.fixedBinding("170 FOR MAJORITY"));
    barFrame.setSubheadColorBinding(Binding.fixedBinding(Color.RED));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(results, r -> String.valueOf(r.getNumSeats()), "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "SingleSeriesShrinkToFit", barFrame);
  }

  @Test
  public void testRenderMultiSeriesAllPositive() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 34, 157));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 21, 121));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 2, 32));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 4, 24));
    results.add(new ElectionResult("GREEN", Color.GREEN, 1, 3));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 0, 1));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("2019 CANADIAN ELECTION RESULT"));
    barFrame.setMaxBinding(Binding.fixedBinding(160));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(
            results, r -> r.getNumSeats() + "/" + r.getSeatEstimate(), "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.addSeriesBinding(
        "Estimate",
        IndexedBinding.propertyBinding(results, r -> lighten(r.getPartyColor()), "PartyColor"),
        IndexedBinding.propertyBinding(
            results, r -> r.getSeatEstimate() - r.getNumSeats(), "NumSeats", "SeatEstimate"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "MultiSeriesAllPositive", barFrame);
  }

  @Test
  public void testRenderSingleSeriesBothDirections() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIB", Color.RED, -27));
    results.add(new ElectionResult("CON", Color.BLUE, +22));
    results.add(new ElectionResult("BQ", Color.CYAN, +22));
    results.add(new ElectionResult("NDP", Color.ORANGE, -20));
    results.add(new ElectionResult("GRN", Color.GREEN, +2));
    results.add(new ElectionResult("IND", Color.LIGHT_GRAY, +1));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("RESULT CHANGE SINCE 2015"));
    barFrame.setMaxBinding(Binding.fixedBinding(28));
    barFrame.setMinBinding(Binding.fixedBinding(-28));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(
            results, r -> new DecimalFormat("+0;-0").format(r.getNumSeats()), "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "SingleSeriesBothDirections", barFrame);
  }

  @Test
  public void testRenderMultiSeriesBothDirections() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIB", Color.RED, -7, -27));
    results.add(new ElectionResult("CON", Color.BLUE, +4, +22));
    results.add(new ElectionResult("BQ", Color.CYAN, +0, +22));
    results.add(new ElectionResult("NDP", Color.ORANGE, +2, -20));
    results.add(new ElectionResult("GRN", Color.GREEN, +1, +2));
    results.add(new ElectionResult("IND", Color.LIGHT_GRAY, +0, +1));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("RESULT CHANGE SINCE 2015"));
    barFrame.setMaxBinding(Binding.fixedBinding(28));
    barFrame.setMinBinding(Binding.fixedBinding(-28));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(
            results,
            r ->
                CHANGE_FORMAT.format(r.getNumSeats())
                    + "/"
                    + CHANGE_FORMAT.format(r.getSeatEstimate()),
            "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.addSeriesBinding(
        "Estimate",
        IndexedBinding.propertyBinding(results, r -> lighten(r.getPartyColor()), "PartyColor"),
        IndexedBinding.propertyBinding(
            results,
            r ->
                r.getSeatEstimate()
                    - (Math.signum(r.getSeatEstimate()) == Math.signum(r.getNumSeats())
                        ? r.getNumSeats()
                        : 0),
            "NumSeats",
            "SeatEstimate"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "MultiSeriesBothDirections", barFrame);
  }

  @Test
  public void testRenderTwoLinedBars() throws IOException {
    BindableList<RidingResult> results = new BindableList<>();
    results.add(new RidingResult("BARDISH CHAGGER", "LIBERAL", Color.RED, 31085, 0.4879));
    results.add(new RidingResult("JERRY ZHANG", "CONSERVATIVE", Color.BLUE, 15615, 0.2451));
    results.add(
        new RidingResult("LORI CAMPBELL", "NEW DEMOCRATIC PARTY", Color.ORANGE, 9710, 0.1524));
    results.add(new RidingResult("KIRSTEN WRIGHT", "GREEN", Color.GREEN, 6184, 0.0971));
    results.add(
        new RidingResult("ERIKA TRAUB", "PEOPLE'S PARTY", Color.MAGENTA.darker(), 1112, 0.0175));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("WATERLOO"));
    barFrame.setMaxBinding(
        Binding.fixedBinding(results.stream().mapToInt(RidingResult::getNumVotes).sum() / 2));
    barFrame.setSubheadTextBinding(Binding.fixedBinding("LIB HOLD"));
    barFrame.setSubheadColorBinding(Binding.fixedBinding(Color.RED));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(
            results,
            r -> r.getCandidateName() + "\n" + r.getPartyName(),
            "CandidateName",
            "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(
            results,
            r ->
                THOUSANDS_FORMAT.format(r.getNumVotes())
                    + "\n"
                    + PERCENT_FORMAT.format(r.getVotePct()),
            "NumVotes",
            "VotePct"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, RidingResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, RidingResult::getNumVotes, "NumSeats"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "TwoLinedBars", barFrame);
  }

  @Test
  public void testRenderTwoLinedBarWithIcon() throws IOException {
    BindableList<RidingResult> results = new BindableList<>();
    results.add(new RidingResult("BARDISH CHAGGER", "LIBERAL", Color.RED, 31085, 0.4879, true));
    results.add(new RidingResult("JERRY ZHANG", "CONSERVATIVE", Color.BLUE, 15615, 0.2451));
    results.add(
        new RidingResult("LORI CAMPBELL", "NEW DEMOCRATIC PARTY", Color.ORANGE, 9710, 0.1524));
    results.add(new RidingResult("KIRSTEN WRIGHT", "GREEN", Color.GREEN, 6184, 0.0971));
    results.add(
        new RidingResult("ERIKA TRAUB", "PEOPLE'S PARTY", Color.MAGENTA.darker(), 1112, 0.0175));

    Shape shape = createTickShape();

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("WATERLOO"));
    barFrame.setMaxBinding(
        Binding.fixedBinding(results.stream().mapToInt(RidingResult::getNumVotes).sum() / 2));
    barFrame.setSubheadTextBinding(Binding.fixedBinding("LIB HOLD"));
    barFrame.setSubheadColorBinding(Binding.fixedBinding(Color.RED));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(
            results,
            r -> r.getCandidateName() + "\n" + r.getPartyName(),
            "CandidateName",
            "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(
            results,
            r ->
                THOUSANDS_FORMAT.format(r.getNumVotes())
                    + "\n"
                    + PERCENT_FORMAT.format(r.getVotePct()),
            "NumVotes",
            "VotePct"));
    barFrame.setLeftIconBinding(
        IndexedBinding.propertyBinding(results, r -> r.isElected() ? shape : null, "Elected"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, RidingResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, RidingResult::getNumVotes, "NumSeats"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "TwoLinedBarWithIcon", barFrame);
  }

  @Test
  public void testRenderTwoLinedBarWithNegativeIcon() throws IOException {
    BindableList<RidingResult> results = new BindableList<>();
    results.add(new RidingResult("BARDISH CHAGGER", "LIB", Color.RED, 31085, -0.010, true));
    results.add(new RidingResult("JERRY ZHANG", "CON", Color.BLUE, 15615, -0.077));
    results.add(new RidingResult("LORI CAMPBELL", "NDP", Color.ORANGE, 9710, +0.003));
    results.add(new RidingResult("KIRSTEN WRIGHT", "GRN", Color.GREEN, 6184, +0.068));
    results.add(new RidingResult("ERIKA TRAUB", "PPC", Color.MAGENTA.darker(), 1112, +0.017));

    Shape shape = createTickShape();

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("WATERLOO"));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, RidingResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(
            results, r -> PERCENT_FORMAT.format(r.getVotePct()), "VotePct"));
    barFrame.setLeftIconBinding(
        IndexedBinding.propertyBinding(results, r -> r.isElected() ? shape : null, "Elected"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, RidingResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, RidingResult::getVotePct, "VotePct"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "TwoLinedBarWithNegativeIcon", barFrame);
  }

  @Test
  public void testRenderVerticalLine() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(new ElectionResult("LIBERAL", Color.RED, 177));
    results.add(new ElectionResult("CONSERVATIVE", Color.BLUE, 95));
    results.add(new ElectionResult("NEW DEMOCRATIC PARTY", Color.ORANGE, 39));
    results.add(new ElectionResult("BLOC QUEBECOIS", Color.CYAN, 10));
    results.add(new ElectionResult("INDEPENDENT", Color.LIGHT_GRAY, 8));
    results.add(new ElectionResult("GREEN", Color.GREEN, 2));
    results.add(
        new ElectionResult("CO-OPERATIVE COMMONWEALTH FEDERATION", Color.ORANGE.darker(), 1));
    results.add(new ElectionResult("PEOPLE'S PARTY", Color.MAGENTA.darker(), 1));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("SEATS AT DISSOLUTION"));
    barFrame.setSubheadTextBinding(Binding.fixedBinding("170 FOR MAJORITY"));
    barFrame.setSubheadColorBinding(Binding.fixedBinding(Color.RED));
    barFrame.setMaxBinding(Binding.fixedBinding(225));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(results, r -> String.valueOf(r.getNumSeats()), "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.setNumLinesBinding(Binding.fixedBinding(1));
    barFrame.setLineLevelsBinding(IndexedBinding.singletonBinding(170));
    barFrame.setLineLabelsBinding(IndexedBinding.singletonBinding("MAJORITY"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "VerticalLine", barFrame);
  }

  @Test
  public void testRenderAccents() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(
        new ElectionResult("COALITION AVENIR QU\u00c9BEC: FRAN\u00c7OIS LEGAULT", Color.BLUE, 74));
    results.add(new ElectionResult("LIB\u00c9RAL: PHILIPPE COUILLARD", Color.RED, 31));
    results.add(
        new ElectionResult("PARTI QU\u00c9BECOIS: JEAN-FRAN\u00c7OIS LIS\u00c9E", Color.CYAN, 10));
    results.add(new ElectionResult("QU\u00c9BEC SOLIDAIRE: MANON MASS\u00c9", Color.ORANGE, 10));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("\u00c9LECTION 2018"));
    barFrame.setSubheadTextBinding(Binding.fixedBinding("MAJORIT\u00c9: 63"));
    barFrame.setMaxBinding(Binding.fixedBinding(83));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(results, r -> String.valueOf(r.getNumSeats()), "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.setNumLinesBinding(Binding.fixedBinding(1));
    barFrame.setLineLevelsBinding(IndexedBinding.singletonBinding(63));
    barFrame.setLineLabelsBinding(IndexedBinding.singletonBinding("MAJORIT\u00c9"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "Accents", barFrame);
  }

  @Test
  public void testRenderMultiLineAccents() throws IOException {
    BindableList<ElectionResult> results = new BindableList<>();
    results.add(
        new ElectionResult("COALITION AVENIR QU\u00c9BEC\nFRAN\u00c7OIS LEGAULT", Color.BLUE, 74));
    results.add(new ElectionResult("LIB\u00c9RAL\nPHILIPPE COUILLARD", Color.RED, 31));
    results.add(
        new ElectionResult("PARTI QU\u00c9BECOIS\nJEAN-FRAN\u00c7OIS LIS\u00c9E", Color.CYAN, 10));
    results.add(new ElectionResult("QU\u00c9BEC SOLIDAIRE\nMANON MASS\u00c9", Color.ORANGE, 10));

    BarFrame barFrame = new BarFrame();
    barFrame.setHeaderBinding(Binding.fixedBinding("\u00c9LECTION 2018"));
    barFrame.setSubheadTextBinding(Binding.fixedBinding("MAJORIT\u00c9: 63"));
    barFrame.setMaxBinding(Binding.fixedBinding(83));
    barFrame.setNumBarsBinding(Binding.sizeBinding(results));
    barFrame.setLeftTextBinding(
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyName, "PartyName"));
    barFrame.setRightTextBinding(
        IndexedBinding.propertyBinding(results, r -> String.valueOf(r.getNumSeats()), "NumSeats"));
    barFrame.addSeriesBinding(
        "Seats",
        IndexedBinding.propertyBinding(results, ElectionResult::getPartyColor, "PartyColor"),
        IndexedBinding.propertyBinding(results, ElectionResult::getNumSeats, "NumSeats"));
    barFrame.setNumLinesBinding(Binding.fixedBinding(1));
    barFrame.setLineLevelsBinding(IndexedBinding.singletonBinding(63));
    barFrame.setLineLabelsBinding(IndexedBinding.singletonBinding("MAJORIT\u00c9"));
    barFrame.setSize(512, 256);

    compareRendering("BarFrame", "MultiLineAccents", barFrame);
  }

  private Shape createTickShape() {
    Area shape = new Area(new Rectangle(0, 0, 100, 100));
    shape.subtract(
        new Area(
            new Polygon(
                new int[] {10, 40, 90, 80, 40, 20}, new int[] {50, 80, 30, 20, 60, 40}, 6)));
    return shape;
  }

  private static class ElectionResult extends Bindable {
    private String partyName;
    private Color partyColor;
    private int numSeats;
    private int seatEstimate;

    public ElectionResult(String partyName, Color partyColor, int numSeats) {
      this(partyName, partyColor, numSeats, numSeats);
    }

    public ElectionResult(String partyName, Color partyColor, int numSeats, int seatEstimate) {
      this.partyName = partyName;
      this.partyColor = partyColor;
      this.numSeats = numSeats;
      this.seatEstimate = seatEstimate;
    }

    public String getPartyName() {
      return partyName;
    }

    public void setPartyName(String partyName) {
      this.partyName = partyName;
      onPropertyRefreshed("PartyName");
    }

    public Color getPartyColor() {
      return partyColor;
    }

    public void setPartyColor(Color partyColor) {
      this.partyColor = partyColor;
      onPropertyRefreshed("PartyColor");
    }

    public int getNumSeats() {
      return numSeats;
    }

    public void setNumSeats(int numSeats) {
      this.numSeats = numSeats;
      onPropertyRefreshed("NumSeats");
    }

    public int getSeatEstimate() {
      return seatEstimate;
    }

    public void setSeatEstimate(int seatEstimate) {
      this.seatEstimate = seatEstimate;
      onPropertyRefreshed("SeatEstimate");
    }
  }

  private static class RidingResult extends Bindable {
    private String candidateName;
    private String partyName;
    private Color partyColor;
    private int numVotes;
    private double votePct;
    private boolean elected;

    public RidingResult(
        String candidateName, String partyName, Color partyColor, int numVotes, double votePct) {
      this(candidateName, partyName, partyColor, numVotes, votePct, false);
    }

    public RidingResult(
        String candidateName,
        String partyName,
        Color partyColor,
        int numVotes,
        double votePct,
        boolean elected) {
      this.candidateName = candidateName;
      this.partyName = partyName;
      this.partyColor = partyColor;
      this.numVotes = numVotes;
      this.votePct = votePct;
      this.elected = elected;
    }

    public String getCandidateName() {
      return candidateName;
    }

    public void setCandidateName(String candidateName) {
      this.candidateName = candidateName;
      onPropertyRefreshed("CandidateName");
    }

    public String getPartyName() {
      return partyName;
    }

    public void setPartyName(String partyName) {
      this.partyName = partyName;
      onPropertyRefreshed("PartyName");
    }

    public Color getPartyColor() {
      return partyColor;
    }

    public void setPartyColor(Color partyColor) {
      this.partyColor = partyColor;
      onPropertyRefreshed("PartyColor");
    }

    public int getNumVotes() {
      return numVotes;
    }

    public void setNumVotes(int numVotes) {
      this.numVotes = numVotes;
      onPropertyRefreshed("NumVotes");
    }

    public double getVotePct() {
      return votePct;
    }

    public void setVotePct(double votePct) {
      this.votePct = votePct;
      onPropertyRefreshed("VotePct");
    }

    public boolean isElected() {
      return elected;
    }

    public void setElected(boolean elected) {
      this.elected = elected;
      onPropertyRefreshed("Elected");
    }
  }
}
