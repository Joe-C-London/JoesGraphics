package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.Test;

public class MultiSummaryFrameTest {

  @Test
  public void testEntries() {
    MultiSummaryFrame frame = new MultiSummaryFrame();
    frame.setNumRowsBinding(Binding.fixedBinding(5));
    frame.setRowHeaderBinding(
        IndexedBinding.listBinding(
            "ATLANTIC", "QU\u00c9BEC", "ONTARIO", "WESTERN CANADA", "THE NORTH"));
    frame.setValuesBinding(
        IndexedBinding.listBinding(
            List.of(
                ImmutablePair.of(Color.RED, "26"),
                ImmutablePair.of(Color.BLUE, "4"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "1"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "35"),
                ImmutablePair.of(Color.BLUE, "10"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "32"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "79"),
                ImmutablePair.of(Color.BLUE, "36"),
                ImmutablePair.of(Color.ORANGE, "6"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "15"),
                ImmutablePair.of(Color.BLUE, "71"),
                ImmutablePair.of(Color.ORANGE, "15"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "2"),
                ImmutablePair.of(Color.GRAY, "1")),
            List.of(
                ImmutablePair.of(Color.RED, "2"),
                ImmutablePair.of(Color.BLUE, "0"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0"))));

    assertEquals(5, frame.getNumRows());
    assertEquals("ATLANTIC", frame.getRowHeader(0));
    assertEquals("QU\u00c9BEC", frame.getRowHeader(1));
    assertEquals("ONTARIO", frame.getRowHeader(2));
    assertEquals("WESTERN CANADA", frame.getRowHeader(3));
    assertEquals("THE NORTH", frame.getRowHeader(4));

    assertEquals(6, frame.getNumValues(0));
    assertEquals(6, frame.getNumValues(1));
    assertEquals(6, frame.getNumValues(2));
    assertEquals(6, frame.getNumValues(3));
    assertEquals(6, frame.getNumValues(4));

    assertEquals(Color.RED, frame.getColor(0, 0));
    assertEquals(Color.BLUE, frame.getColor(1, 1));
    assertEquals(Color.ORANGE, frame.getColor(2, 2));
    assertEquals(Color.CYAN.darker(), frame.getColor(3, 3));
    assertEquals(Color.GREEN.darker(), frame.getColor(4, 4));

    assertEquals("26", frame.getValue(0, 0));
    assertEquals("10", frame.getValue(1, 1));
    assertEquals("6", frame.getValue(2, 2));
    assertEquals("0", frame.getValue(3, 3));
    assertEquals("0", frame.getValue(4, 4));
  }

  @Test
  public void testRenderBasicSummary() throws IOException {
    MultiSummaryFrame frame = new MultiSummaryFrame();
    frame.setHeaderBinding(Binding.fixedBinding("SEATS BY REGION"));
    frame.setNumRowsBinding(Binding.fixedBinding(5));
    frame.setRowHeaderBinding(
        IndexedBinding.listBinding(
            "ATLANTIC", "QU\u00c9BEC", "ONTARIO", "WESTERN CANADA", "THE NORTH"));
    frame.setValuesBinding(
        IndexedBinding.listBinding(
            List.of(
                ImmutablePair.of(Color.RED, "26"),
                ImmutablePair.of(Color.BLUE, "4"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "1"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "35"),
                ImmutablePair.of(Color.BLUE, "10"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "32"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "79"),
                ImmutablePair.of(Color.BLUE, "36"),
                ImmutablePair.of(Color.ORANGE, "6"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "15"),
                ImmutablePair.of(Color.BLUE, "71"),
                ImmutablePair.of(Color.ORANGE, "15"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "2"),
                ImmutablePair.of(Color.GRAY, "1")),
            List.of(
                ImmutablePair.of(Color.RED, "2"),
                ImmutablePair.of(Color.BLUE, "0"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0"))));
    frame.setSize(512, 256);

    compareRendering("MultiSummaryFrame", "Basic", frame);
  }

  @Test
  public void testRenderOverflowSummary() throws IOException {
    MultiSummaryFrame frame = new MultiSummaryFrame();
    frame.setHeaderBinding(Binding.fixedBinding("SEATS BY PROVINCE"));
    frame.setNumRowsBinding(Binding.fixedBinding(13));
    frame.setRowHeaderBinding(
        IndexedBinding.listBinding(
            "NEWFOUNDLAND & LABRADOR",
            "NOVA SCOTIA",
            "PRINCE EDWARD ISLAND",
            "NEW BRUNSWICK",
            "QU\u00c9BEC",
            "ONTARIO",
            "MANITOBA",
            "SASKATCHEWAN",
            "ALBERTA",
            "BRITISH COLUMBIA",
            "YUKON",
            "NORTHWEST TERRITORIES",
            "NUNAVUT"));
    frame.setValuesBinding(
        IndexedBinding.listBinding(
            List.of(
                ImmutablePair.of(Color.RED, "6"),
                ImmutablePair.of(Color.BLUE, "0"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "10"),
                ImmutablePair.of(Color.BLUE, "1"),
                ImmutablePair.of(Color.ORANGE, "0"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "4"),
                ImmutablePair.of(Color.BLUE, "0"),
                ImmutablePair.of(Color.ORANGE, "0"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "6"),
                ImmutablePair.of(Color.BLUE, "3"),
                ImmutablePair.of(Color.ORANGE, "0"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "1"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "35"),
                ImmutablePair.of(Color.BLUE, "10"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "32"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "79"),
                ImmutablePair.of(Color.BLUE, "36"),
                ImmutablePair.of(Color.ORANGE, "6"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "4"),
                ImmutablePair.of(Color.BLUE, "7"),
                ImmutablePair.of(Color.ORANGE, "3"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "0"),
                ImmutablePair.of(Color.BLUE, "14"),
                ImmutablePair.of(Color.ORANGE, "0"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "0"),
                ImmutablePair.of(Color.BLUE, "33"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "11"),
                ImmutablePair.of(Color.BLUE, "17"),
                ImmutablePair.of(Color.ORANGE, "11"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "2"),
                ImmutablePair.of(Color.GRAY, "1")),
            List.of(
                ImmutablePair.of(Color.RED, "1"),
                ImmutablePair.of(Color.BLUE, "0"),
                ImmutablePair.of(Color.ORANGE, "0"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "1"),
                ImmutablePair.of(Color.BLUE, "0"),
                ImmutablePair.of(Color.ORANGE, "0"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0")),
            List.of(
                ImmutablePair.of(Color.RED, "0"),
                ImmutablePair.of(Color.BLUE, "0"),
                ImmutablePair.of(Color.ORANGE, "1"),
                ImmutablePair.of(Color.CYAN.darker(), "0"),
                ImmutablePair.of(Color.GREEN.darker(), "0"),
                ImmutablePair.of(Color.GRAY, "0"))));
    frame.setSize(512, 256);

    compareRendering("MultiSummaryFrame", "Overflow", frame);
  }

  @Test
  public void testRenderDifferentColCounts() throws IOException {
    MultiSummaryFrame frame = new MultiSummaryFrame();
    frame.setHeaderBinding(Binding.fixedBinding("SENATE SEATS"));
    frame.setNumRowsBinding(Binding.fixedBinding(8));
    frame.setRowHeaderBinding(
        IndexedBinding.listBinding(
            "NEW SOUTH WALES",
            "VICTORIA",
            "QUEENSLAND",
            "WESTERN AUSTRALIA",
            "SOUTH AUSTRALIA",
            "TASMANIA",
            "ACT",
            "NORTHERN TERRITORY"));
    frame.setValuesBinding(
        IndexedBinding.listBinding(
            List.of(
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.GREEN.darker().darker(), "NAT"),
                ImmutablePair.of(Color.GREEN.darker(), "GRN")),
            List.of(
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.GREEN.darker(), "GRN"),
                ImmutablePair.of(Color.BLUE, "LIB")),
            List.of(
                ImmutablePair.of(Color.BLUE, "LNP"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.BLUE, "LNP"),
                ImmutablePair.of(Color.ORANGE, "ONP"),
                ImmutablePair.of(Color.BLUE, "LNP"),
                ImmutablePair.of(Color.GREEN.darker(), "GRN")),
            List.of(
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.GREEN.darker(), "GRN")),
            List.of(
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.GREEN.darker(), "GRN"),
                ImmutablePair.of(Color.BLUE, "LIB")),
            List.of(
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.BLUE, "LIB"),
                ImmutablePair.of(Color.GREEN.darker(), "GRN"),
                ImmutablePair.of(Color.RED, "LAB"),
                ImmutablePair.of(Color.YELLOW, "LAMB")),
            List.of(ImmutablePair.of(Color.RED, "LAB"), ImmutablePair.of(Color.BLUE, "LIB")),
            List.of(ImmutablePair.of(Color.RED, "LAB"), ImmutablePair.of(Color.ORANGE, "CLP"))));
    frame.setSize(512, 256);

    compareRendering("MultiSummaryFrame", "DiffColCounts", frame);
  }
}
