package com.joecollins.graphics;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

public class ImageGenerator {

  private ImageGenerator() {}

  public static Shape createTickShape() {
    Area shape = new Area(new Rectangle(0, 0, 100, 100));
    shape.subtract(
        new Area(
            new Polygon(
                new int[] {10, 40, 90, 80, 40, 20}, new int[] {50, 80, 30, 20, 60, 40}, 6)));
    return shape;
  }

  public static Shape createHalfTickShape() {
    Area shape = new Area(createTickShape());
    shape.add(new Area(new Rectangle2D.Double(200, 200, 1e-6, 1e-6)));
    return shape;
  }

  public static Shape createMidTickShape() {
    AffineTransform transform = AffineTransform.getTranslateInstance(0, 50);
    Area shape = new Area(transform.createTransformedShape(createTickShape()));
    shape.add(new Area(new Rectangle2D.Double(200, 200, 1e-6, 1e-6)));
    shape.add(new Area(new Rectangle2D.Double(0, 0, 1e-6, 1e-6)));
    return shape;
  }

  public static Shape createCrossShape() {
    Area shape = new Area(new Rectangle(0, 0, 100, 100));
    shape.subtract(
        new Area(
            new Polygon(
                new int[] {15, 25, 50, 75, 85, 60, 85, 75, 50, 25, 15, 40},
                new int[] {25, 15, 40, 15, 25, 50, 75, 85, 60, 85, 75, 50},
                12)));
    return shape;
  }
}
