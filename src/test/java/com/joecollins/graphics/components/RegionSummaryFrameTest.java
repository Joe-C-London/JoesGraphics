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

public class RegionSummaryFrameTest {

  @Test
  public void testEntriesDifferentColors() {
    RegionSummaryFrame frame = new RegionSummaryFrame();
    frame.setNumSectionsBinding(Binding.fixedBinding(2));
    frame.setSectionHeaderBinding(
        IndexedBinding.listBinding(List.of("ELECTORAL VOTES", "POPULAR VOTE")));
    frame.setSectionValueColorBinding(
        IndexedBinding.listBinding(
            List.of(
                List.of(
                    ImmutablePair.of(Color.BLUE, "306"),
                    ImmutablePair.of(Color.BLUE, "<< 74"),
                    ImmutablePair.of(Color.RED, "232")),
                List.of(
                    ImmutablePair.of(Color.BLUE, "51.1%"),
                    ImmutablePair.of(Color.BLUE, "<< 1.0%"),
                    ImmutablePair.of(Color.RED, "47.2%")))));

    assertEquals(2, frame.getNumSections());
    assertEquals(Color.BLACK, frame.getSummaryColor());
    assertEquals("ELECTORAL VOTES", frame.getSectionHeader(0));
    assertEquals(Color.BLUE, frame.getValueColor(1, 0));
    assertEquals("<< 1.0%", frame.getValue(1, 1));
  }

  @Test
  public void testEntriesSameColor() {
    RegionSummaryFrame frame = new RegionSummaryFrame();
    frame.setNumSectionsBinding(Binding.fixedBinding(2));
    frame.setSummaryColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setSectionHeaderBinding(
        IndexedBinding.listBinding(List.of("ELECTORAL VOTES", "POPULAR VOTE")));
    frame.setSectionValueBinding(
        IndexedBinding.listBinding(List.of(List.of("306", "+74"), List.of("51.1%", "+1.0%"))));

    assertEquals(2, frame.getNumSections());
    assertEquals(Color.BLUE, frame.getSummaryColor());
    assertEquals("ELECTORAL VOTES", frame.getSectionHeader(0));
    assertEquals(Color.BLUE, frame.getValueColor(1, 0));
    assertEquals("+1.0%", frame.getValue(1, 1));
  }

  @Test
  public void testRenderDifferentColors() throws IOException {
    RegionSummaryFrame frame = new RegionSummaryFrame();
    frame.setHeaderBinding(Binding.fixedBinding("UNITED STATES"));
    frame.setNumSectionsBinding(Binding.fixedBinding(2));
    frame.setSectionHeaderBinding(
        IndexedBinding.listBinding(List.of("ELECTORAL VOTES", "POPULAR VOTE")));
    frame.setSectionValueColorBinding(
        IndexedBinding.listBinding(
            List.of(
                List.of(
                    ImmutablePair.of(Color.BLUE, "306"),
                    ImmutablePair.of(Color.BLUE, "<< 74"),
                    ImmutablePair.of(Color.RED, "232")),
                List.of(
                    ImmutablePair.of(Color.BLUE, "51.1%"),
                    ImmutablePair.of(Color.BLUE, "<< 1.0%"),
                    ImmutablePair.of(Color.RED, "47.2%")))));
    frame.setSize(500, 500);
    compareRendering("RegionSummaryFrame", "DifferentColors", frame);
  }

  @Test
  public void testRenderSameColor() throws IOException {
    RegionSummaryFrame frame = new RegionSummaryFrame();
    frame.setHeaderBinding(Binding.fixedBinding("USA"));
    frame.setNumSectionsBinding(Binding.fixedBinding(2));
    frame.setBorderColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setSummaryColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setSectionHeaderBinding(
        IndexedBinding.listBinding(List.of("ELECTORAL VOTES", "POPULAR VOTE")));
    frame.setSectionValueBinding(
        IndexedBinding.listBinding(List.of(List.of("306", "+74"), List.of("51.1%", "+1.0%"))));
    frame.setSize(125, 125);
    compareRendering("RegionSummaryFrame", "SameColor", frame);
  }
}
