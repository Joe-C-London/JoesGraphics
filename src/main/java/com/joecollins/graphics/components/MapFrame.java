package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JPanel;
import org.apache.commons.lang3.tuple.MutablePair;

public class MapFrame extends GraphicsFrame {

  private Binding<Integer> numShapesBinding = () -> 0;
  private IndexedBinding<Shape> shapeBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> colorBinding = IndexedBinding.emptyBinding();

  private List<MutablePair<Shape, Color>> shapesToDraw = new ArrayList<>();

  public MapFrame() {
    JPanel panel =
        new JPanel() {
          {
            setBackground(Color.WHITE);
          }

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Optional<Rectangle2D> bounds =
                shapesToDraw.stream()
                    .map(e -> e.left.getBounds2D())
                    .reduce(Rectangle2D::createUnion);
            if (bounds.isEmpty()) return;
            AffineTransform transform = new AffineTransform();
            double boundsWidth = bounds.get().getMaxX() - bounds.get().getMinX();
            double boundsHeight = bounds.get().getMaxY() - bounds.get().getMinY();
            double xScale = (getWidth() - 4) / boundsWidth;
            double yScale = (getHeight() - 4) / boundsHeight;
            double scale = Math.min(xScale, yScale);
            double x = (getWidth() - scale * boundsWidth) / 2;
            double y = (getHeight() - scale * boundsHeight) / 2;
            transform.translate(x, y);
            transform.scale(scale, scale);
            transform.translate(-bounds.get().getMinX(), -bounds.get().getMinY());
            shapesToDraw.forEach(
                pair -> {
                  g2d.setColor(pair.right);
                  g2d.fill(transform.createTransformedShape(pair.left));
                });
          }
        };
    add(panel, BorderLayout.CENTER);
  }

  int getNumShapes() {
    return shapesToDraw.size();
  }

  public void setNumShapesBinding(Binding<Integer> numShapesBinding) {
    this.numShapesBinding.unbind();
    this.numShapesBinding = numShapesBinding;
    this.numShapesBinding.bind(
        size -> {
          while (size > shapesToDraw.size()) {
            shapesToDraw.add(new MutablePair<>(new Area(), Color.BLACK));
          }
          while (size < shapesToDraw.size()) {
            shapesToDraw.remove(size.intValue());
          }
          repaint();
        });
  }

  Shape getShape(int idx) {
    return shapesToDraw.get(idx).left;
  }

  public void setShapeBinding(IndexedBinding<Shape> shapeBinding) {
    this.shapeBinding.unbind();
    this.shapeBinding = shapeBinding;
    this.shapeBinding.bind(
        (idx, shape) -> {
          shapesToDraw.get(idx).left = shape;
          repaint();
        });
  }

  Color getColor(int idx) {
    return shapesToDraw.get(idx).right;
  }

  public void setColorBinding(IndexedBinding<Color> colorBinding) {
    this.colorBinding.unbind();
    this.colorBinding = colorBinding;
    this.colorBinding.bind(
        (idx, color) -> {
          shapesToDraw.get(idx).right = color;
          repaint();
        });
  }
}
