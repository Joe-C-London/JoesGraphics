package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
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
  public void testMapPropertyBinding() {
    class ConstituencyPair {
      private final Shape shape;
      private final Color color;

      ConstituencyPair(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
      }
    }
    List<ConstituencyPair> shapes = new ArrayList<>();
    shapes.add(new ConstituencyPair(new Ellipse2D.Double(2, 2, 1, 1), Color.RED));
    shapes.add(new ConstituencyPair(new Rectangle2D.Double(5, 5, 2, 2), Color.BLUE));

    MapFrame frame =
        MapFrameBuilder.from(() -> shapes, cp -> cp.shape, cp -> Binding.fixedBinding(cp.color))
            .withHeader(Binding.fixedBinding("MAP"))
            .build();
    assertEquals(2, frame.getNumShapes());
    assertEquals(Ellipse2D.Double.class, frame.getShape(0).getClass());
    assertEquals(Color.RED, frame.getColor(0));
    assertEquals(Rectangle2D.Double.class, frame.getShape(1).getClass());
    assertEquals(Color.BLUE, frame.getColor(1));
    assertEquals("MAP", frame.getHeader());
    assertEquals(new Rectangle2D.Double(2, 2, 5, 5), frame.getFocusBox());
  }

  @Test
  public void testMapItemPropertyBinding() {
    List<Pair<Shape, BindableWrapper<Color>>> shapes = new ArrayList<>();
    shapes.add(
        ImmutablePair.of(new Ellipse2D.Double(2, 2, 1, 1), new BindableWrapper<>(Color.RED)));
    shapes.add(
        ImmutablePair.of(new Rectangle2D.Double(5, 5, 2, 2), new BindableWrapper<>(Color.BLUE)));

    MapFrame frame =
        MapFrameBuilder.from(() -> shapes, Pair::getLeft, cp -> cp.getRight().getBinding())
            .withHeader(Binding.fixedBinding("MAP"))
            .build();
    assertEquals(2, frame.getNumShapes());
    assertEquals(Ellipse2D.Double.class, frame.getShape(0).getClass());
    assertEquals(Color.RED, frame.getColor(0));
    assertEquals(Rectangle2D.Double.class, frame.getShape(1).getClass());
    assertEquals(Color.BLUE, frame.getColor(1));

    shapes.get(0).getRight().setValue(Color.GREEN);
    assertEquals(Color.GREEN, frame.getColor(0));
    assertEquals(Color.BLUE, frame.getColor(1));

    shapes.get(1).getRight().setValue(Color.ORANGE);
    assertEquals(Color.GREEN, frame.getColor(0));
    assertEquals(Color.ORANGE, frame.getColor(1));
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
