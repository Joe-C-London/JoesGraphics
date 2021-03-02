package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Test;

public class SwingFrameBuilderTest {

  private static class SwingProperties {
    private final Color leftColor;
    private final Color rightColor;
    private final Number value;
    private final String text;

    private SwingProperties(Color leftColor, Color rightColor, Number value, String text) {
      this.leftColor = leftColor;
      this.rightColor = rightColor;
      this.value = value;
      this.text = text;
    }
  }

  @Test
  public void basicTest() {
    BindableWrapper<SwingProperties> swingProps =
        new BindableWrapper<>(new SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"));
    SwingFrame frame =
        SwingFrameBuilder.basic(
                swingProps.getBinding(),
                p -> p.leftColor,
                p -> p.rightColor,
                p -> p.value,
                p -> p.text)
            .withRange(Binding.fixedBinding(0.10))
            .withHeader(Binding.fixedBinding("SWING"))
            .build();
    assertEquals(Color.RED, frame.getLeftColor());
    assertEquals(Color.BLUE, frame.getRightColor());
    assertEquals(Color.RED, frame.getBottomColor());
    assertEquals(0.02, frame.getValue().doubleValue(), 1e-6);
    assertEquals("2% SWING", frame.getBottomText());
    assertEquals(0.10, frame.getRange());
    assertEquals("SWING", frame.getHeader());

    swingProps.setValue(new SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING"));
    assertEquals(Color.GREEN, frame.getLeftColor());
    assertEquals(Color.ORANGE, frame.getRightColor());
    assertEquals(Color.ORANGE, frame.getBottomColor());
    assertEquals(-0.05, frame.getValue().doubleValue(), 1e-6);
    assertEquals("5% SWING", frame.getBottomText());
  }

  @Test
  public void testNeutralBottomColor() {
    BindableWrapper<SwingProperties> swingProps =
        new BindableWrapper<>(new SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"));
    BindableWrapper<Color> neutralColor = new BindableWrapper<>(Color.GRAY);
    SwingFrame frame =
        SwingFrameBuilder.basic(
                swingProps.getBinding(),
                p -> p.leftColor,
                p -> p.rightColor,
                p -> p.value,
                p -> p.text)
            .withRange(Binding.fixedBinding(0.10))
            .withNeutralColor(neutralColor.getBinding())
            .build();
    assertEquals(Color.RED, frame.getBottomColor());

    swingProps.setValue(new SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING"));
    assertEquals(Color.ORANGE, frame.getBottomColor());

    neutralColor.setValue(Color.LIGHT_GRAY);
    assertEquals(Color.ORANGE, frame.getBottomColor());

    swingProps.setValue(new SwingProperties(Color.GREEN, Color.ORANGE, 0.00, "NO SWING"));
    assertEquals(Color.LIGHT_GRAY, frame.getBottomColor());

    neutralColor.setValue(Color.BLACK);
    assertEquals(Color.BLACK, frame.getBottomColor());
  }

  @Test
  public void testSwingPrevCurrTwoMainPartiesSwingRight() {
    Party lib = new Party("LIBERAL", "LIB", Color.RED);
    Party con = new Party("CONSERVATIVE", "CON", Color.BLUE);
    Party ndp = new Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE);
    Binding<Map<Party, Integer>> prevBinding = () -> Map.of(lib, 25, con, 15, ndp, 10);
    Binding<Map<Party, Integer>> currBinding = () -> Map.of(lib, 16, con, 13, ndp, 11);
    // LIB: 50.00 -> 40.00 (-10.00)
    // CON: 30.00 -> 32.25 (+ 2.25)
    // NDP: 20.00 -> 27.75 (+ 7.75)
    List<Party> partyOrder = Arrays.asList(ndp, lib, con);
    SwingFrame swingFrame =
        SwingFrameBuilder.prevCurr(
                prevBinding, currBinding, Comparator.comparing(partyOrder::indexOf))
            .build();
    assertEquals(Color.BLUE, swingFrame.getLeftColor());
    assertEquals(Color.RED, swingFrame.getRightColor());
    assertEquals(Color.BLUE, swingFrame.getBottomColor());
    assertEquals(0.0625, swingFrame.getValue().doubleValue(), 1e-6);
    assertEquals(0.1, swingFrame.getRange().doubleValue(), 1e-6);
    assertEquals("6.2% SWING LIB TO CON", swingFrame.getBottomText());
  }

  @Test
  public void testSwingPrevCurrTwoMainPartiesSwingLeft() {
    Party lib = new Party("LIBERAL", "LIB", Color.RED);
    Party con = new Party("CONSERVATIVE", "CON", Color.BLUE);
    Party ndp = new Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE);
    Binding<Map<Party, Integer>> prevBinding = () -> Map.of(lib, 25, con, 15, ndp, 10);
    Binding<Map<Party, Integer>> currBinding = () -> Map.of(lib, 26, con, 10, ndp, 4);
    // LIB: 50.00 -> 65.00 (+15.00)
    // CON: 30.00 -> 25.00 (- 5.00)
    // NDP: 20.00 -> 10.00 (-10.00)
    List<Party> partyOrder = Arrays.asList(ndp, lib, con);
    SwingFrame swingFrame =
        SwingFrameBuilder.prevCurr(
                prevBinding, currBinding, Comparator.comparing(partyOrder::indexOf))
            .build();
    assertEquals(Color.BLUE, swingFrame.getLeftColor());
    assertEquals(Color.RED, swingFrame.getRightColor());
    assertEquals(Color.RED, swingFrame.getBottomColor());
    assertEquals(-0.1, swingFrame.getValue().doubleValue(), 1e-6);
    assertEquals(0.1, swingFrame.getRange().doubleValue(), 1e-6);
    assertEquals("10.0% SWING CON TO LIB", swingFrame.getBottomText());
  }

  @Test
  public void testSwingPrevCurrPartiesNotInComparator() {
    Party lib = new Party("LIBERAL", "LIB", Color.RED);
    Party con = new Party("CONSERVATIVE", "CON", Color.BLUE);
    Party ndp = new Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE);
    Binding<Map<Party, Integer>> prevBinding = () -> Map.of(lib, 25, con, 15, ndp, 10);
    Binding<Map<Party, Integer>> currBinding = () -> Map.of(lib, 26, con, 10, ndp, 4);
    // LIB: 50.00 -> 65.00 (+15.00)
    // CON: 30.00 -> 25.00 (- 5.00)
    // NDP: 20.00 -> 10.00 (-10.00)
    List<Party> partyOrder = Arrays.asList();
    SwingFrame swingFrame =
        SwingFrameBuilder.prevCurr(
                prevBinding, currBinding, Comparator.comparing(partyOrder::indexOf))
            .build();
    assertEquals(
        Set.of(Color.BLUE, Color.RED),
        Set.of(swingFrame.getLeftColor(), swingFrame.getRightColor()));
    //    assertEquals(Color.RED, swingFrame.getBottomColor());
    //    assertEquals(0.1 * (swingFrame.getLeftColor().equals(Color.BLUE) ? -1 : 1),
    // swingFrame.getValue().doubleValue(), 1e-6);
    assertEquals(0.1, swingFrame.getRange().doubleValue(), 1e-6);
    assertEquals("10.0% SWING CON TO LIB", swingFrame.getBottomText());
  }

  @Test
  public void testSwingPrevCurrPartiesSwingLeftFromRight() {
    Party lib = new Party("LIBERAL", "LIB", Color.RED);
    Party con = new Party("CONSERVATIVE", "CON", Color.BLUE);
    Party ndp = new Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE);
    Binding<Map<Party, Integer>> prevBinding = () -> Map.of(lib, 15, con, 25, ndp, 10);
    Binding<Map<Party, Integer>> currBinding = () -> Map.of(lib, 6, con, 10, ndp, 24);
    // LIB: 30.00 -> 15.00 (-15.00)
    // CON: 50.00 -> 25.00 (-25.00)
    // NDP: 20.00 -> 60.00 (+40.00)
    List<Party> partyOrder = Arrays.asList(ndp, lib, con);
    SwingFrame swingFrame =
        SwingFrameBuilder.prevCurr(
                prevBinding, currBinding, Comparator.comparing(partyOrder::indexOf))
            .build();
    assertEquals(Color.BLUE, swingFrame.getLeftColor());
    assertEquals(Color.ORANGE, swingFrame.getRightColor());
    assertEquals(Color.ORANGE, swingFrame.getBottomColor());
    assertEquals(-0.325, swingFrame.getValue().doubleValue(), 1e-6);
    assertEquals(0.1, swingFrame.getRange().doubleValue(), 1e-6);
    assertEquals("32.5% SWING CON TO NDP", swingFrame.getBottomText());
  }

  @Test
  public void testNoSwingBetweenParties() {
    Party lib = new Party("LIBERAL", "LIB", Color.RED);
    Party con = new Party("CONSERVATIVE", "CON", Color.BLUE);
    Party ndp = new Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE);
    Binding<Map<Party, Integer>> prevBinding = () -> Map.of(lib, 15, con, 25, ndp, 10);
    Binding<Map<Party, Integer>> currBinding = () -> Map.of(lib, 15, con, 25, ndp, 10);
    List<Party> partyOrder = Arrays.asList(ndp, lib, con);
    SwingFrame swingFrame =
        SwingFrameBuilder.prevCurr(
                prevBinding, currBinding, Comparator.comparing(partyOrder::indexOf))
            .build();
    assertEquals(Color.BLUE, swingFrame.getLeftColor());
    assertEquals(Color.RED, swingFrame.getRightColor());
    assertEquals(Color.LIGHT_GRAY, swingFrame.getBottomColor());
    assertEquals(0.0, swingFrame.getValue().doubleValue(), 1e-6);
    assertEquals(0.1, swingFrame.getRange().doubleValue(), 1e-6);
    assertEquals("NO SWING", swingFrame.getBottomText());
  }

  @Test
  public void testNoSwingAvailable() {
    Party lib = new Party("LIBERAL", "LIB", Color.RED);
    Party con = new Party("CONSERVATIVE", "CON", Color.BLUE);
    Party ndp = new Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE);
    Binding<Map<Party, Integer>> prevBinding = () -> Map.of(lib, 15, con, 25, ndp, 10);
    Binding<Map<Party, Integer>> currBinding = Map::of;
    List<Party> partyOrder = Arrays.asList(ndp, lib, con);
    SwingFrame swingFrame =
        SwingFrameBuilder.prevCurr(
                prevBinding, currBinding, Comparator.comparing(partyOrder::indexOf))
            .build();
    assertEquals(Color.LIGHT_GRAY, swingFrame.getLeftColor());
    assertEquals(Color.LIGHT_GRAY, swingFrame.getRightColor());
    assertEquals(Color.LIGHT_GRAY, swingFrame.getBottomColor());
    assertEquals(0.0, swingFrame.getValue().doubleValue(), 1e-6);
    assertEquals(0.1, swingFrame.getRange().doubleValue(), 1e-6);
    assertEquals("NOT AVAILABLE", swingFrame.getBottomText());
  }

  @Test
  public void testSwingPrevCurrTwoMainPartiesNormalised() {
    Party lib = new Party("LIBERAL", "LIB", Color.RED);
    Party con = new Party("CONSERVATIVE", "CON", Color.BLUE);
    Party ndp = new Party("NEW DEMOCRATIC PARTY", "NDP", Color.ORANGE);
    Binding<Map<Party, Double>> prevBinding = () -> Map.of(lib, 0.40, con, 0.30, ndp, 0.20);
    Binding<Map<Party, Double>> currBinding = () -> Map.of(lib, 0.38, con, 0.35, ndp, 0.18);
    // LIB: 40.00 -> 38.00 (- 2.00)
    // CON: 30.00 -> 35.00 (+ 5.00)
    // NDP: 20.00 -> 18.00 (- 2.00)
    List<Party> partyOrder = Arrays.asList(ndp, lib, con);
    SwingFrame swingFrame =
        SwingFrameBuilder.prevCurrNormalised(
                prevBinding, currBinding, Comparator.comparing(partyOrder::indexOf))
            .build();
    assertEquals(Color.BLUE, swingFrame.getLeftColor());
    assertEquals(Color.RED, swingFrame.getRightColor());
    assertEquals(Color.BLUE, swingFrame.getBottomColor());
    assertEquals(0.035, swingFrame.getValue().doubleValue(), 1e-6);
    assertEquals(0.1, swingFrame.getRange().doubleValue(), 1e-6);
    assertEquals("3.5% SWING LIB TO CON", swingFrame.getBottomText());
  }
}
