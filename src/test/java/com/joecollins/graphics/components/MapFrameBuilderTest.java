package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class MapFrameBuilderTest {

  @Test
  public void testBasicMapFrame() {
    BindableList<Pair<Shape, Color>> shapes = new BindableList<>();
    shapes.add(ImmutablePair.of(new Ellipse2D.Double(2, 2, 1, 1), Color.RED));
    shapes.add(ImmutablePair.of(new Rectangle2D.Double(5, 5, 2, 2), Color.BLUE));

    MapFrame frame = MapFrameBuilder.from(shapes).withHeader(Binding.fixedBinding("MAP")).build();
    assertEquals(2, frame.getNumShapes());
    assertEquals(Ellipse2D.Double.class, frame.getShape(0).getClass());
    assertEquals(Color.RED, frame.getColor(0));
    assertEquals(Rectangle2D.Double.class, frame.getShape(1).getClass());
    assertEquals(Color.BLUE, frame.getColor(1));
    assertEquals("MAP", frame.getHeader());
    assertEquals(new Rectangle2D.Double(2, 2, 5, 5), frame.getFocusBox());
  }

  @Test
  public void testBasicMapFrameWithListBinding() {
    List<Pair<Shape, Color>> shapes = new ArrayList<>();
    shapes.add(ImmutablePair.of(new Ellipse2D.Double(2, 2, 1, 1), Color.RED));
    shapes.add(ImmutablePair.of(new Rectangle2D.Double(5, 5, 2, 2), Color.BLUE));

    MapFrame frame =
        MapFrameBuilder.from(() -> shapes).withHeader(Binding.fixedBinding("MAP")).build();
    assertEquals(2, frame.getNumShapes());
    assertEquals(Ellipse2D.Double.class, frame.getShape(0).getClass());
    assertEquals(Color.RED, frame.getColor(0));
    assertEquals(Rectangle2D.Double.class, frame.getShape(1).getClass());
    assertEquals(Color.BLUE, frame.getColor(1));
    assertEquals("MAP", frame.getHeader());
    assertEquals(new Rectangle2D.Double(2, 2, 5, 5), frame.getFocusBox());
  }

  @Test
  public void testFocusBox() {
    BindableList<Pair<Shape, Color>> shapes = new BindableList<>();
    shapes.add(ImmutablePair.of(new Ellipse2D.Double(2, 2, 1, 1), Color.RED));
    shapes.add(ImmutablePair.of(new Rectangle2D.Double(5, 5, 2, 2), Color.BLUE));

    List<Shape> binding = Collections.singletonList(shapes.get(0).getLeft());

    MapFrame frame =
        MapFrameBuilder.from(shapes)
            .withHeader(Binding.fixedBinding("MAP"))
            .withFocus(() -> binding)
            .build();
    assertEquals(new Rectangle2D.Double(2, 2, 1, 1), frame.getFocusBox());
  }

  @Test
  public void testMultiFocusBox() {
    BindableList<Pair<Shape, Color>> shapes = new BindableList<>();
    shapes.add(ImmutablePair.of(new Ellipse2D.Double(2, 2, 1, 1), Color.RED));
    shapes.add(ImmutablePair.of(new Rectangle2D.Double(5, 5, 2, 2), Color.BLUE));

    List<Shape> binding = shapes.stream().map(Pair::getLeft).collect(Collectors.toList());

    MapFrame frame =
        MapFrameBuilder.from(shapes)
            .withHeader(Binding.fixedBinding("MAP"))
            .withFocus(() -> binding)
            .build();
    assertEquals(new Rectangle2D.Double(2, 2, 5, 5), frame.getFocusBox());
  }
}
