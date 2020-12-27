package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;
import static java.awt.Color.RED;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.BindableWrapper;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class HeatMapFrameTest {

  private static final Color DARK_GREEN = GREEN.darker();

  @Test
  public void testDots() {
    List<Color> borderColors =
        Stream.concat(
                Stream.concat(
                    Stream.generate(() -> BLUE).limit(8), Stream.generate(() -> RED).limit(18)),
                Stream.of(DARK_GREEN))
            .collect(Collectors.toList());
    List<Color> fillColors =
        List.of(
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            BLUE,
            DARK_GREEN,
            DARK_GREEN,
            DARK_GREEN,
            BLUE,
            DARK_GREEN,
            RED,
            RED,
            BLUE,
            DARK_GREEN,
            RED,
            DARK_GREEN,
            RED,
            BLUE,
            BLUE,
            DARK_GREEN,
            BLUE,
            RED,
            RED,
            DARK_GREEN);

    HeatMapFrame frame = new HeatMapFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(3));
    frame.setNumSquaresBinding(Binding.fixedBinding(27));
    frame.setSquareBordersBinding(IndexedBinding.listBinding(borderColors));
    frame.setSquareFillBinding(IndexedBinding.listBinding(fillColors));

    assertEquals(3, frame.getNumRows());
    assertEquals(27, frame.getNumSquares());

    assertEquals(BLUE, frame.getSquareBorder(0));
    assertEquals(RED, frame.getSquareBorder(12));
    assertEquals(DARK_GREEN, frame.getSquareBorder(26));

    assertEquals(BLUE, frame.getSquareFill(0));
    assertEquals(DARK_GREEN, frame.getSquareFill(12));
    assertEquals(DARK_GREEN, frame.getSquareFill(26));
  }

  @Test
  public void testSeatBar() {
    HeatMapFrame frame = new HeatMapFrame();
    frame.setNumSeatBarsBinding(Binding.fixedBinding(2));
    frame.setSeatBarColorBinding(IndexedBinding.listBinding(BLUE, new Color(128, 128, 255)));
    frame.setSeatBarSizeBinding(IndexedBinding.listBinding(8, 5));
    frame.setSeatBarLabelBinding(Binding.fixedBinding("8/13"));

    assertEquals(2, frame.getSeatBarCount());
    assertEquals(BLUE, frame.getSeatBarColor(0));
    assertEquals(5, frame.getSeatBarSize(1));
    assertEquals("8/13", frame.getSeatBarLabel());
  }

  @Test
  public void testChangeBar() {
    HeatMapFrame frame = new HeatMapFrame();
    frame.setNumChangeBarsBinding(Binding.fixedBinding(2));
    frame.setChangeBarColorBinding(IndexedBinding.listBinding(BLUE, new Color(128, 128, 255)));
    frame.setChangeBarSizeBinding(IndexedBinding.listBinding(3, 2));
    frame.setChangeBarLabelBinding(Binding.fixedBinding("+3/+5"));
    frame.setChangeBarStartBinding(Binding.fixedBinding(5));

    assertEquals(2, frame.getChangeBarCount());
    assertEquals(BLUE, frame.getChangeBarColor(0));
    assertEquals(2, frame.getChangeBarSize(1));
    assertEquals("+3/+5", frame.getChangeBarLabel());
    assertEquals(5, frame.getChangeBarStart());
  }

  @Test
  public void testRenderEvenWide() throws IOException {
    Map<Integer, Pair<Color, Color>> results = getPeiResults();

    BindableList<Pair<Color, Color>> squares = new BindableList<>();
    squares.setAll(
        Stream.of(
                20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                8, 27, 24, 17)
            .map(results::get)
            .collect(Collectors.toList()));

    BindableList<Pair<Color, Integer>> seatBars = new BindableList<>();
    seatBars.setAll(
        List.of(ImmutablePair.of(BLUE, 8), ImmutablePair.of(new Color(128, 128, 255), 5)));
    BindableWrapper<String> seatLabel = new BindableWrapper<>("8/13");

    BindableList<Pair<Color, Integer>> changeBars = new BindableList<>();
    changeBars.setAll(
        List.of(ImmutablePair.of(BLUE, 3), ImmutablePair.of(new Color(128, 128, 255), 2)));
    BindableWrapper<String> changeLabel = new BindableWrapper<>("+3/+5");
    BindableWrapper<Integer> changeStart = new BindableWrapper<>(8);

    BindableWrapper<Color> borderColor = new BindableWrapper<>(BLUE);
    BindableWrapper<String> header = new BindableWrapper<>("PROGRESSIVE CONSERVATIVE HEAT MAP");

    HeatMapFrame frame = new HeatMapFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(3));
    frame.setNumSquaresBinding(Binding.sizeBinding(squares));
    frame.setSquareBordersBinding(IndexedBinding.propertyBinding(squares, Pair::getLeft));
    frame.setSquareFillBinding(IndexedBinding.propertyBinding(squares, Pair::getRight));
    frame.setNumSeatBarsBinding(Binding.sizeBinding(seatBars));
    frame.setSeatBarColorBinding(IndexedBinding.propertyBinding(seatBars, Pair::getLeft));
    frame.setSeatBarSizeBinding(IndexedBinding.propertyBinding(seatBars, Pair::getRight));
    frame.setSeatBarLabelBinding(seatLabel.getBinding());
    frame.setNumChangeBarsBinding(Binding.sizeBinding(changeBars));
    frame.setChangeBarColorBinding(IndexedBinding.propertyBinding(changeBars, Pair::getLeft));
    frame.setChangeBarSizeBinding(IndexedBinding.propertyBinding(changeBars, Pair::getRight));
    frame.setChangeBarLabelBinding(changeLabel.getBinding());
    frame.setChangeBarStartBinding(changeStart.getBinding());
    frame.setBorderColorBinding(borderColor.getBinding());
    frame.setHeaderBinding(header.getBinding());

    frame.setSize(1024, 512);
    compareRendering("HeatMapFrame", "EvenWide-1", frame);

    squares.setAll(
        Stream.of(
                24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                19, 6, 20, 17)
            .map(results::get)
            .collect(Collectors.toList()));
    seatBars.setAll(
        List.of(ImmutablePair.of(RED, 2), ImmutablePair.of(new Color(255, 128, 128), 4)));
    seatLabel.setValue("2/6");
    changeBars.setAll(
        List.of(ImmutablePair.of(RED, -4), ImmutablePair.of(new Color(255, 128, 128), -8)));
    changeLabel.setValue("-4/-12");
    changeStart.setValue(18);
    borderColor.setValue(RED);
    header.setValue("LIBERAL HEAT MAP");
    compareRendering("HeatMapFrame", "EvenWide-2", frame);
  }

  @Test
  public void testRenderEvenHigh() throws IOException {
    Map<Integer, Pair<Color, Color>> results = getPeiResults();

    BindableList<Pair<Color, Color>> squares = new BindableList<>();
    squares.setAll(
        Stream.of(
                20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                8, 27, 24, 17)
            .map(results::get)
            .collect(Collectors.toList()));

    BindableList<Pair<Color, Integer>> seatBars = new BindableList<>();
    seatBars.setAll(
        List.of(ImmutablePair.of(BLUE, 8), ImmutablePair.of(new Color(128, 128, 255), 5)));
    BindableWrapper<String> seatLabel = new BindableWrapper<>("8/13");

    BindableList<Pair<Color, Integer>> changeBars = new BindableList<>();
    changeBars.setAll(
        List.of(ImmutablePair.of(BLUE, 3), ImmutablePair.of(new Color(128, 128, 255), 2)));
    BindableWrapper<String> changeLabel = new BindableWrapper<>("+3/+5");
    BindableWrapper<Integer> changeStart = new BindableWrapper<>(8);

    BindableWrapper<Color> borderColor = new BindableWrapper<>(BLUE);
    BindableWrapper<String> header = new BindableWrapper<>("PROGRESSIVE CONSERVATIVE HEAT MAP");

    HeatMapFrame frame = new HeatMapFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(9));
    frame.setNumSquaresBinding(Binding.sizeBinding(squares));
    frame.setSquareBordersBinding(IndexedBinding.propertyBinding(squares, Pair::getLeft));
    frame.setSquareFillBinding(IndexedBinding.propertyBinding(squares, Pair::getRight));
    frame.setNumSeatBarsBinding(Binding.sizeBinding(seatBars));
    frame.setSeatBarColorBinding(IndexedBinding.propertyBinding(seatBars, Pair::getLeft));
    frame.setSeatBarSizeBinding(IndexedBinding.propertyBinding(seatBars, Pair::getRight));
    frame.setSeatBarLabelBinding(seatLabel.getBinding());
    frame.setNumChangeBarsBinding(Binding.sizeBinding(changeBars));
    frame.setChangeBarColorBinding(IndexedBinding.propertyBinding(changeBars, Pair::getLeft));
    frame.setChangeBarSizeBinding(IndexedBinding.propertyBinding(changeBars, Pair::getRight));
    frame.setChangeBarLabelBinding(changeLabel.getBinding());
    frame.setChangeBarStartBinding(changeStart.getBinding());
    frame.setBorderColorBinding(borderColor.getBinding());
    frame.setHeaderBinding(header.getBinding());

    frame.setSize(1024, 512);
    compareRendering("HeatMapFrame", "EvenHigh-1", frame);

    squares.setAll(
        Stream.of(
                24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                19, 6, 20, 17)
            .map(results::get)
            .collect(Collectors.toList()));
    seatBars.setAll(
        List.of(ImmutablePair.of(RED, 2), ImmutablePair.of(new Color(255, 128, 128), 4)));
    seatLabel.setValue("2/6");
    changeBars.setAll(
        List.of(ImmutablePair.of(RED, -4), ImmutablePair.of(new Color(255, 128, 128), -8)));
    changeLabel.setValue("-4/-12");
    changeStart.setValue(18);
    borderColor.setValue(RED);
    header.setValue("LIBERAL HEAT MAP");
    compareRendering("HeatMapFrame", "EvenHigh-2", frame);
  }

  @Test
  public void testRenderUneven() throws IOException {
    Map<Integer, Pair<Color, Color>> results = getPeiResults();

    BindableList<Pair<Color, Color>> squares = new BindableList<>();
    squares.setAll(
        Stream.of(
                20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                8, 27, 24, 17)
            .map(results::get)
            .collect(Collectors.toList()));

    BindableList<Pair<Color, Integer>> seatBars = new BindableList<>();
    seatBars.setAll(
        List.of(ImmutablePair.of(BLUE, 8), ImmutablePair.of(new Color(128, 128, 255), 5)));
    BindableWrapper<String> seatLabel = new BindableWrapper<>("8/13");

    BindableList<Pair<Color, Integer>> changeBars = new BindableList<>();
    changeBars.setAll(
        List.of(ImmutablePair.of(BLUE, 3), ImmutablePair.of(new Color(128, 128, 255), 2)));
    BindableWrapper<String> changeLabel = new BindableWrapper<>("+3/+5");
    BindableWrapper<Integer> changeStart = new BindableWrapper<>(8);

    BindableWrapper<Color> borderColor = new BindableWrapper<>(BLUE);
    BindableWrapper<String> header = new BindableWrapper<>("PROGRESSIVE CONSERVATIVE HEAT MAP");

    HeatMapFrame frame = new HeatMapFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(5));
    frame.setNumSquaresBinding(Binding.sizeBinding(squares));
    frame.setSquareBordersBinding(IndexedBinding.propertyBinding(squares, Pair::getLeft));
    frame.setSquareFillBinding(IndexedBinding.propertyBinding(squares, Pair::getRight));
    frame.setNumSeatBarsBinding(Binding.sizeBinding(seatBars));
    frame.setSeatBarColorBinding(IndexedBinding.propertyBinding(seatBars, Pair::getLeft));
    frame.setSeatBarSizeBinding(IndexedBinding.propertyBinding(seatBars, Pair::getRight));
    frame.setSeatBarLabelBinding(seatLabel.getBinding());
    frame.setNumChangeBarsBinding(Binding.sizeBinding(changeBars));
    frame.setChangeBarColorBinding(IndexedBinding.propertyBinding(changeBars, Pair::getLeft));
    frame.setChangeBarSizeBinding(IndexedBinding.propertyBinding(changeBars, Pair::getRight));
    frame.setChangeBarLabelBinding(changeLabel.getBinding());
    frame.setChangeBarStartBinding(changeStart.getBinding());
    frame.setBorderColorBinding(borderColor.getBinding());
    frame.setHeaderBinding(header.getBinding());

    frame.setSize(1024, 512);
    compareRendering("HeatMapFrame", "Uneven-1", frame);

    squares.setAll(
        Stream.of(
                24, 27, 8, 11, 9, 26, 10, 23, 16, 12, 3, 25, 14, 22, 15, 21, 13, 5, 4, 2, 1, 7, 18,
                19, 6, 20, 17)
            .map(results::get)
            .collect(Collectors.toList()));
    seatBars.setAll(
        List.of(ImmutablePair.of(RED, 2), ImmutablePair.of(new Color(255, 128, 128), 4)));
    seatLabel.setValue("2/6");
    changeBars.setAll(
        List.of(ImmutablePair.of(RED, -4), ImmutablePair.of(new Color(255, 128, 128), -8)));
    changeLabel.setValue("-4/-12");
    changeStart.setValue(18);
    borderColor.setValue(RED);
    header.setValue("LIBERAL HEAT MAP");
    compareRendering("HeatMapFrame", "Uneven-2", frame);
  }

  @Test
  public void testRenderChangeReversals() throws IOException {
    Map<Integer, Pair<Color, Color>> results = getPeiResults();

    BindableList<Pair<Color, Color>> squares = new BindableList<>();
    squares.setAll(
        Stream.of(
                20, 6, 19, 18, 7, 1, 2, 4, 5, 13, 21, 15, 22, 14, 25, 3, 12, 16, 23, 10, 26, 9, 11,
                8, 27, 24, 17)
            .map(results::get)
            .collect(Collectors.toList()));

    BindableList<Pair<Color, Integer>> seatBars = new BindableList<>();
    seatBars.setAll(
        List.of(ImmutablePair.of(BLUE, 2), ImmutablePair.of(new Color(128, 128, 255), 2)));
    BindableWrapper<String> seatLabel = new BindableWrapper<>("2/4");

    BindableList<Pair<Color, Integer>> changeBars = new BindableList<>();
    changeBars.setAll(
        List.of(ImmutablePair.of(BLUE, 3), ImmutablePair.of(new Color(128, 128, 255), 2)));
    BindableWrapper<String> changeLabel = new BindableWrapper<>("+3/+5");
    BindableWrapper<Integer> changeStart = new BindableWrapper<>(8);

    BindableWrapper<Color> borderColor = new BindableWrapper<>(BLUE);
    BindableWrapper<String> header = new BindableWrapper<>("PROGRESSIVE CONSERVATIVE HEAT MAP");

    HeatMapFrame frame = new HeatMapFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(5));
    frame.setNumSquaresBinding(Binding.sizeBinding(squares));
    frame.setSquareBordersBinding(IndexedBinding.propertyBinding(squares, Pair::getLeft));
    frame.setSquareFillBinding(IndexedBinding.propertyBinding(squares, Pair::getRight));
    frame.setNumSeatBarsBinding(Binding.sizeBinding(seatBars));
    frame.setSeatBarColorBinding(IndexedBinding.propertyBinding(seatBars, Pair::getLeft));
    frame.setSeatBarSizeBinding(IndexedBinding.propertyBinding(seatBars, Pair::getRight));
    frame.setSeatBarLabelBinding(seatLabel.getBinding());
    frame.setNumChangeBarsBinding(Binding.sizeBinding(changeBars));
    frame.setChangeBarColorBinding(IndexedBinding.propertyBinding(changeBars, Pair::getLeft));
    frame.setChangeBarSizeBinding(IndexedBinding.propertyBinding(changeBars, Pair::getRight));
    frame.setChangeBarLabelBinding(changeLabel.getBinding());
    frame.setChangeBarStartBinding(changeStart.getBinding());
    frame.setBorderColorBinding(borderColor.getBinding());
    frame.setHeaderBinding(header.getBinding());
    frame.setSize(1024, 512);

    changeBars.setAll(
        List.of(ImmutablePair.of(BLUE, 2), ImmutablePair.of(new Color(128, 128, 255), -1)));
    changeLabel.setValue("+2/+1");
    compareRendering("HeatMapFrame", "ChangeReversals-1", frame);

    changeBars.setAll(
        List.of(ImmutablePair.of(BLUE, -2), ImmutablePair.of(new Color(128, 128, 255), 1)));
    changeLabel.setValue("-2/-1");
    compareRendering("HeatMapFrame", "ChangeReversals-2", frame);

    changeBars.setAll(
        List.of(ImmutablePair.of(BLUE, 1), ImmutablePair.of(new Color(128, 128, 255), -2)));
    changeLabel.setValue("+1/-1");
    compareRendering("HeatMapFrame", "ChangeReversals-3", frame);

    changeBars.setAll(
        List.of(ImmutablePair.of(BLUE, -1), ImmutablePair.of(new Color(128, 128, 255), 2)));
    changeLabel.setValue("-1/+1");
    compareRendering("HeatMapFrame", "ChangeReversals-4", frame);
  }

  private Map<Integer, Pair<Color, Color>> getPeiResults() {
    return Map.ofEntries(
        ImmutablePair.of(1, ImmutablePair.of(BLUE, BLUE)),
        ImmutablePair.of(2, ImmutablePair.of(BLUE, BLUE)),
        ImmutablePair.of(3, ImmutablePair.of(RED, BLUE)),
        ImmutablePair.of(4, ImmutablePair.of(BLUE, BLUE)),
        ImmutablePair.of(5, ImmutablePair.of(RED, DARK_GREEN)),
        ImmutablePair.of(6, ImmutablePair.of(BLUE, BLUE)),
        ImmutablePair.of(7, ImmutablePair.of(BLUE, BLUE)),
        ImmutablePair.of(8, ImmutablePair.of(RED, BLUE)),
        ImmutablePair.of(9, ImmutablePair.of(RED, BLUE)),
        ImmutablePair.of(10, ImmutablePair.of(RED, RED)),
        ImmutablePair.of(11, ImmutablePair.of(RED, DARK_GREEN)),
        ImmutablePair.of(12, ImmutablePair.of(RED, DARK_GREEN)),
        ImmutablePair.of(13, ImmutablePair.of(RED, DARK_GREEN)),
        ImmutablePair.of(14, ImmutablePair.of(RED, RED)),
        ImmutablePair.of(15, ImmutablePair.of(RED, BLUE)),
        ImmutablePair.of(16, ImmutablePair.of(RED, RED)),
        ImmutablePair.of(17, ImmutablePair.of(DARK_GREEN, DARK_GREEN)),
        ImmutablePair.of(18, ImmutablePair.of(BLUE, BLUE)),
        ImmutablePair.of(19, ImmutablePair.of(BLUE, BLUE)),
        ImmutablePair.of(20, ImmutablePair.of(BLUE, BLUE)),
        ImmutablePair.of(21, ImmutablePair.of(RED, DARK_GREEN)),
        ImmutablePair.of(22, ImmutablePair.of(RED, DARK_GREEN)),
        ImmutablePair.of(23, ImmutablePair.of(RED, DARK_GREEN)),
        ImmutablePair.of(24, ImmutablePair.of(RED, RED)),
        ImmutablePair.of(25, ImmutablePair.of(RED, RED)),
        ImmutablePair.of(26, ImmutablePair.of(RED, BLUE)),
        ImmutablePair.of(27, ImmutablePair.of(RED, RED)));
  }
}
