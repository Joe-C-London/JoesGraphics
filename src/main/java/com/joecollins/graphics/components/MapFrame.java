package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import javax.swing.JPanel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;

public class MapFrame extends GraphicsFrame {

  private Binding<Integer> numShapesBinding = () -> 0;
  private IndexedBinding<Shape> shapeBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> colorBinding = IndexedBinding.emptyBinding();
  private Binding<Rectangle2D> focusBinding = () -> null;
  private Binding<Integer> numOutlineShapesBinding = () -> 0;
  private IndexedBinding<Shape> outlineShapesBinding = IndexedBinding.emptyBinding();

  private List<MutablePair<Shape, Color>> shapesToDraw = new ArrayList<>();
  private Rectangle2D focus = null;
  private List<Shape> outlineShapes = new ArrayList<>();
  private Map<Shape, Shape> transformedShapesCache = new WeakHashMap<>();
  private final double distanceThreshold = 0.5;

  public MapFrame() {
    JPanel panel =
        new JPanel() {
          {
            setBackground(Color.WHITE);
            addComponentListener(
                new ComponentAdapter() {
                  @Override
                  public void componentResized(ComponentEvent e) {
                    transformedShapesCache.clear();
                    repaint();
                  }
                });
          }

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (shapesToDraw.isEmpty()) {
              return;
            }
            Rectangle2D bounds = getFocusBox();
            AffineTransform transform = new AffineTransform();
            double boundsWidth = bounds.getMaxX() - bounds.getMinX();
            double boundsHeight = bounds.getMaxY() - bounds.getMinY();
            double xScale = (getWidth() - 4) / boundsWidth;
            double yScale = (getHeight() - 4) / boundsHeight;
            double scale = Math.min(xScale, yScale);
            double x = (getWidth() - scale * boundsWidth) / 2;
            double y = (getHeight() - scale * boundsHeight) / 2;
            transform.translate(x, y);
            transform.scale(scale, scale);
            transform.translate(-bounds.getMinX(), -bounds.getMinY());
            final Predicate<Shape> inScope;
            try {
              AffineTransform inverted = transform.createInverse();
              Shape drawArea =
                  inverted.createTransformedShape(
                      new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
              inScope = s -> drawArea.intersects(s.getBounds());
            } catch (NoninvertibleTransformException e) {
              throw new RuntimeException(e);
            }
            shapesToDraw.stream()
                .filter(e -> inScope.test(e.left))
                .map(
                    pair ->
                        ImmutablePair.of(
                            transformedShapesCache.computeIfAbsent(
                                pair.left, shape -> createTransformedShape(transform, shape)),
                            pair.right))
                .forEach(
                    pair -> {
                      g2d.setColor(pair.right);
                      g2d.fill(pair.left);
                    });
            outlineShapes.stream()
                .filter(e -> inScope.test(e))
                .map(
                    shape ->
                        transformedShapesCache.computeIfAbsent(
                            shape, s -> createTransformedShape(transform, s)))
                .forEach(
                    shape -> {
                      g2d.setColor(Color.WHITE);
                      g2d.setStroke(new BasicStroke((float) Math.sqrt(0.5)));
                      g2d.draw(shape);
                    });
          }
        };
    add(panel, BorderLayout.CENTER);
  }

  private Shape createTransformedShape(AffineTransform transform, Shape shape) {
    PathIterator pathIterator = transform.createTransformedShape(shape).getPathIterator(null);
    GeneralPath currentPath = new GeneralPath();
    double[] c = new double[6];
    Point2D.Double lastPoint = null;
    while (!pathIterator.isDone()) {
      int type = pathIterator.currentSegment(c);
      Point2D.Double nextPoint;
      switch (type) {
        case PathIterator.SEG_MOVETO:
          lastPoint = new Point2D.Double(c[0], c[1]);
          currentPath.moveTo(c[0], c[1]);
          break;
        case PathIterator.SEG_LINETO:
          nextPoint = new Point2D.Double(c[0], c[1]);
          if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold) {
            currentPath.lineTo(c[0], c[1]);
            lastPoint = nextPoint;
          }
          break;
        case PathIterator.SEG_QUADTO:
          nextPoint = new Point2D.Double(c[2], c[3]);
          if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold) {
            currentPath.quadTo(c[0], c[1], c[2], c[3]);
            lastPoint = nextPoint;
          }
          break;
        case PathIterator.SEG_CUBICTO:
          nextPoint = new Point2D.Double(c[4], c[5]);
          if (lastPoint == null || lastPoint.distance(nextPoint) > distanceThreshold) {
            currentPath.curveTo(c[0], c[1], c[2], c[3], c[4], c[5]);
            lastPoint = nextPoint;
          }
          break;
        case PathIterator.SEG_CLOSE:
          lastPoint = null;
          currentPath.closePath();
          break;
        default:
          throw new IllegalStateException("Unrecognised segment type " + type);
      }
      pathIterator.next();
    }
    return currentPath;
  }

  int getNumShapes() {
    return shapesToDraw.size();
  }

  public void setNumShapesBinding(Binding<Integer> numShapesBinding) {
    this.numShapesBinding.unbind();
    this.numShapesBinding = numShapesBinding;
    this.numShapesBinding.bindLegacy(
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

  Rectangle2D getFocusBox() {
    if (focus == null) {
      Rectangle2D bounds = null;
      for (MutablePair<Shape, Color> entry : shapesToDraw) {
        if (bounds == null) {
          bounds = entry.left.getBounds2D();
        } else {
          bounds.add(entry.left.getBounds2D());
        }
      }
      return bounds;
    }
    return focus;
  }

  public void setFocusBoxBinding(Binding<Rectangle2D> focusBinding) {
    this.focusBinding.unbind();
    this.focusBinding = focusBinding;
    this.focusBinding.bindLegacy(
        focus -> {
          this.focus = focus;
          transformedShapesCache.clear();
          repaint();
        });
  }

  int getNumOutlineShapes() {
    return outlineShapes.size();
  }

  public void setNumOutlineShapesBinding(Binding<Integer> numOutlineShapesBinding) {
    this.numOutlineShapesBinding.unbind();
    this.numOutlineShapesBinding = numOutlineShapesBinding;
    this.numOutlineShapesBinding.bindLegacy(
        size -> {
          while (size > outlineShapes.size()) {
            outlineShapes.add(new Area());
          }
          while (size < outlineShapes.size()) {
            outlineShapes.remove(size.intValue());
          }
          repaint();
        });
  }

  Shape getOutlineShape(int idx) {
    return outlineShapes.get(idx);
  }

  public void setOutlineShapesBinding(IndexedBinding<Shape> outlineShapesBinding) {
    this.outlineShapesBinding.unbind();
    this.outlineShapesBinding = outlineShapesBinding;
    this.outlineShapesBinding.bind(
        (idx, outline) -> {
          this.outlineShapes.set(idx, outline);
          repaint();
        });
  }
}
