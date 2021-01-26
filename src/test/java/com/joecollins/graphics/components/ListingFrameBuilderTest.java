package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

public class ListingFrameBuilderTest {

  @Test
  public void testBasicListingFrame() {
    BindableList<Triple<String, Color, String>> list = new BindableList<>();
    list.add(ImmutableTriple.of("JUSTIN TRUDEAU", Color.RED, "LIBERAL"));
    list.add(ImmutableTriple.of("ANDREW SCHEER", Color.BLUE, "CONSERVATIVE"));

    BarFrame frame =
        ListingFrameBuilder.of(list, Triple::getLeft, Triple::getRight, Triple::getMiddle)
            .withHeader(Binding.fixedBinding("HEADER"))
            .withSubhead(Binding.fixedBinding("SUBHEAD"))
            .build();
    assertEquals(0, frame.getNumLines());
    assertEquals(2, frame.getNumBars());
    assertEquals(0, frame.getMin().doubleValue(), 1e-6);
    assertEquals(1, frame.getMax().doubleValue(), 1e-6);
    assertEquals("HEADER", frame.getHeader());
    assertEquals("SUBHEAD", frame.getSubheadText());

    assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0));
    assertEquals("LIBERAL", frame.getRightText(0));
    assertEquals(1, frame.getSeries(0).size());
    assertEquals(Color.RED, frame.getSeries(0).get(0).getLeft());
    assertEquals(1.0, frame.getSeries(0).get(0).getRight().doubleValue(), 1e-6);

    assertEquals("ANDREW SCHEER", frame.getLeftText(1));
    assertEquals("CONSERVATIVE", frame.getRightText(1));
    assertEquals(1, frame.getSeries(1).size());
    assertEquals(Color.BLUE, frame.getSeries(1).get(0).getLeft());
    assertEquals(1.0, frame.getSeries(1).get(0).getRight().doubleValue(), 1e-6);
  }

  @Test
  public void testBasicListingFrameWithListBinding() {
    List<Triple<String, Color, String>> list = new ArrayList<>();
    list.add(ImmutableTriple.of("JUSTIN TRUDEAU", Color.RED, "LIBERAL"));
    list.add(ImmutableTriple.of("ANDREW SCHEER", Color.BLUE, "CONSERVATIVE"));

    BarFrame frame =
        ListingFrameBuilder.of(() -> list, Triple::getLeft, Triple::getRight, Triple::getMiddle)
            .withHeader(Binding.fixedBinding("HEADER"))
            .withSubhead(Binding.fixedBinding("SUBHEAD"))
            .build();
    assertEquals(0, frame.getNumLines());
    assertEquals(2, frame.getNumBars());
    assertEquals(0, frame.getMin().doubleValue(), 1e-6);
    assertEquals(1, frame.getMax().doubleValue(), 1e-6);
    assertEquals("HEADER", frame.getHeader());
    assertEquals("SUBHEAD", frame.getSubheadText());

    assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0));
    assertEquals("LIBERAL", frame.getRightText(0));
    assertEquals(1, frame.getSeries(0).size());
    assertEquals(Color.RED, frame.getSeries(0).get(0).getLeft());
    assertEquals(1.0, frame.getSeries(0).get(0).getRight().doubleValue(), 1e-6);

    assertEquals("ANDREW SCHEER", frame.getLeftText(1));
    assertEquals("CONSERVATIVE", frame.getRightText(1));
    assertEquals(1, frame.getSeries(1).size());
    assertEquals(Color.BLUE, frame.getSeries(1).get(0).getLeft());
    assertEquals(1.0, frame.getSeries(1).get(0).getRight().doubleValue(), 1e-6);
  }

  @Test
  public void testBasicFixedListFrame() {
    List<Triple<BindableWrapper<String>, BindableWrapper<Color>, BindableWrapper<String>>> list =
        new ArrayList<>();
    list.add(
        ImmutableTriple.of(
            new BindableWrapper<>("JUSTIN TRUDEAU"),
            new BindableWrapper<>(Color.RED),
            new BindableWrapper<>("LIBERAL")));
    list.add(
        ImmutableTriple.of(
            new BindableWrapper<>("ANDREW SCHEER"),
            new BindableWrapper<>(Color.BLUE),
            new BindableWrapper<>("CONSERVATIVE")));

    BarFrame frame =
        ListingFrameBuilder.ofFixedList(
                list,
                e -> e.getLeft().getBinding(),
                e -> e.getRight().getBinding(),
                e -> e.getMiddle().getBinding())
            .withHeader(Binding.fixedBinding("HEADER"))
            .withSubhead(Binding.fixedBinding("SUBHEAD"))
            .build();
    assertEquals(0, frame.getNumLines());
    assertEquals(2, frame.getNumBars());
    assertEquals(0, frame.getMin().doubleValue(), 1e-6);
    assertEquals(1, frame.getMax().doubleValue(), 1e-6);
    assertEquals("HEADER", frame.getHeader());
    assertEquals("SUBHEAD", frame.getSubheadText());

    assertEquals("JUSTIN TRUDEAU", frame.getLeftText(0));
    assertEquals("LIBERAL", frame.getRightText(0));
    assertEquals(1, frame.getSeries(0).size());
    assertEquals(Color.RED, frame.getSeries(0).get(0).getLeft());
    assertEquals(1.0, frame.getSeries(0).get(0).getRight().doubleValue(), 1e-6);

    assertEquals("ANDREW SCHEER", frame.getLeftText(1));
    assertEquals("CONSERVATIVE", frame.getRightText(1));
    assertEquals(1, frame.getSeries(1).size());
    assertEquals(Color.BLUE, frame.getSeries(1).get(0).getLeft());
    assertEquals(1.0, frame.getSeries(1).get(0).getRight().doubleValue(), 1e-6);
  }
}
