package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class SwingometerFrame extends GraphicsFrame {

  private Binding<? extends Number> rangeBinding = () -> 1;
  private Binding<? extends Number> valueBinding = () -> 0;
  private Binding<Color> leftColorBinding = () -> Color.BLACK;
  private Binding<Color> rightColorBinding = () -> Color.BLACK;
  private Binding<? extends Number> leftToWinBinding = () -> Double.POSITIVE_INFINITY;
  private Binding<? extends Number> rightToWinBinding = () -> Double.POSITIVE_INFINITY;

  private Binding<Integer> numTicksBinding = () -> 0;
  private IndexedBinding<? extends Number> tickPositionBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<String> tickTextBinding = IndexedBinding.emptyBinding();

  private Binding<Integer> numOuterLabelsBinding = () -> 0;
  private IndexedBinding<? extends Number> outerLabelPositionBinding =
      IndexedBinding.emptyBinding();
  private IndexedBinding<String> outerLabelTextBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> outerLabelColorBinding = IndexedBinding.emptyBinding();

  private Binding<Integer> numBucketsPerSideBinding = () -> 1;
  private Binding<Integer> numDotsBinding = () -> 0;
  private IndexedBinding<? extends Number> dotsPositionBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> dotsColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<String> dotsLabelBinding = IndexedBinding.emptyBinding();

  private SwingPanel swingPanel = new SwingPanel();

  public SwingometerFrame() {
    JPanel centerPanel = new JPanel();
    centerPanel.setBackground(Color.WHITE);
    centerPanel.setLayout(new BorderLayout());
    centerPanel.add(swingPanel, BorderLayout.CENTER);
    add(centerPanel, BorderLayout.CENTER);
  }

  Color getLeftColor() {
    return swingPanel.leftColor;
  }

  public void setLeftColorBinding(Binding<Color> leftColorBinding) {
    this.leftColorBinding.unbind();
    this.leftColorBinding = leftColorBinding;
    this.leftColorBinding.bind(swingPanel::setLeftColor);
  }

  Color getRightColor() {
    return swingPanel.rightColor;
  }

  public void setRightColorBinding(Binding<Color> rightColorBinding) {
    this.rightColorBinding.unbind();
    this.rightColorBinding = rightColorBinding;
    this.rightColorBinding.bind(swingPanel::setRightColor);
  }

  Number getValue() {
    return swingPanel.value;
  }

  public void setValueBinding(Binding<? extends Number> valueBinding) {
    this.valueBinding.unbind();
    this.valueBinding = valueBinding;
    this.valueBinding.bind(swingPanel::setValue);
  }

  public Number getRange() {
    return swingPanel.range;
  }

  public void setRangeBinding(Binding<? extends Number> rangeBinding) {
    this.rangeBinding.unbind();
    this.rangeBinding = rangeBinding;
    this.rangeBinding.bind(swingPanel::setRange);
  }

  int getNumTicks() {
    return swingPanel.ticks.size();
  }

  public void setNumTicksBinding(Binding<Integer> numTicksBinding) {
    this.numTicksBinding.unbind();
    this.numTicksBinding = numTicksBinding;
    this.numTicksBinding.bind(swingPanel::setNumTicks);
  }

  Number getTickPosition(int index) {
    return swingPanel.ticks.get(index).left;
  }

  public void setTickPositionBinding(IndexedBinding<? extends Number> tickPositionBinding) {
    this.tickPositionBinding.unbind();
    this.tickPositionBinding = tickPositionBinding;
    this.tickPositionBinding.bind(swingPanel::setTickPosition);
  }

  String getTickText(int index) {
    return swingPanel.ticks.get(index).right;
  }

  public void setTickTextBinding(IndexedBinding<String> tickTextBinding) {
    this.tickTextBinding.unbind();
    this.tickTextBinding = tickTextBinding;
    this.tickTextBinding.bind(swingPanel::setTickText);
  }

  Number getLeftToWin() {
    return leftToWinBinding.getValue();
  }

  public void setLeftToWinBinding(Binding<? extends Number> leftToWinBinding) {
    this.leftToWinBinding.unbind();
    this.leftToWinBinding = leftToWinBinding;
    this.leftToWinBinding.bind(swingPanel::setLeftToWin);
  }

  Number getRightToWin() {
    return rightToWinBinding.getValue();
  }

  public void setRightToWinBinding(Binding<? extends Number> rightToWinBinding) {
    this.rightToWinBinding.unbind();
    this.rightToWinBinding = rightToWinBinding;
    this.rightToWinBinding.bind(swingPanel::setRightToWin);
  }

  int getNumOuterLabels() {
    return swingPanel.outerLabels.size();
  }

  public void setNumOuterLabelsBinding(Binding<Integer> numOuterLabelsBinding) {
    this.numOuterLabelsBinding.unbind();
    this.numOuterLabelsBinding = numOuterLabelsBinding;
    this.numOuterLabelsBinding.bind(swingPanel::setNumOuterLabels);
  }

  Number getOuterLabelPosition(int index) {
    return swingPanel.outerLabels.get(index).left;
  }

  public void setOuterLabelPositionBinding(
      IndexedBinding<? extends Number> outerLabelPositionBinding) {
    this.outerLabelPositionBinding.unbind();
    this.outerLabelPositionBinding = outerLabelPositionBinding;
    this.outerLabelPositionBinding.bind(swingPanel::setOuterLabelPosition);
  }

  public String getOuterLabelText(int index) {
    return swingPanel.outerLabels.get(index).middle;
  }

  public void setOuterLabelTextBinding(IndexedBinding<String> outerLabelTextBinding) {
    this.outerLabelTextBinding.unbind();
    this.outerLabelTextBinding = outerLabelTextBinding;
    this.outerLabelTextBinding.bind(swingPanel::setOuterLabelText);
  }

  public Color getOuterLabelColor(int index) {
    return swingPanel.outerLabels.get(index).right;
  }

  public void setOuterLabelColorBinding(IndexedBinding<Color> outerLabelColorBinding) {
    this.outerLabelColorBinding.unbind();
    this.outerLabelColorBinding = outerLabelColorBinding;
    this.outerLabelColorBinding.bind(swingPanel::setOuterLabelColor);
  }

  int getNumBucketsPerSide() {
    return swingPanel.numBucketsPerSide;
  }

  public void setNumBucketsPerSideBinding(Binding<Integer> numBucketsPerSideBinding) {
    this.numBucketsPerSideBinding.unbind();
    this.numBucketsPerSideBinding = numBucketsPerSideBinding;
    this.numBucketsPerSideBinding.bind(swingPanel::setNumBucketsPerSide);
  }

  int getNumDots() {
    return swingPanel.dots.size();
  }

  public void setNumDotsBinding(Binding<Integer> numDotsBinding) {
    this.numDotsBinding.unbind();
    this.numDotsBinding = numDotsBinding;
    this.numDotsBinding.bind(swingPanel::setNumDots);
  }

  Number getDotPosition(int index) {
    return swingPanel.dots.get(index).getLeft();
  }

  public void setDotsPositionBinding(IndexedBinding<? extends Number> dotsPositionBinding) {
    this.dotsPositionBinding.unbind();
    this.dotsPositionBinding = dotsPositionBinding;
    this.dotsPositionBinding.bind(swingPanel::setDotPosition);
  }

  Color getDotColor(int index) {
    return swingPanel.dots.get(index).getMiddle();
  }

  public void setDotsColorBinding(IndexedBinding<Color> dotsColorBinding) {
    this.dotsColorBinding.unbind();
    this.dotsColorBinding = dotsColorBinding;
    this.dotsColorBinding.bind(swingPanel::setDotColor);
  }

  String getDotLabel(int index) {
    return swingPanel.dots.get(index).getRight();
  }

  public void setDotsLabelBinding(IndexedBinding<String> dotsLabelBinding) {
    this.dotsLabelBinding.unbind();
    this.dotsLabelBinding = dotsLabelBinding;
    this.dotsLabelBinding.bind(swingPanel::setDotLabel);
  }

  private class SwingPanel extends JPanel {
    private Color leftColor = leftColorBinding.getValue();
    private Color rightColor = rightColorBinding.getValue();
    private Number value = valueBinding.getValue();
    private Number range = rangeBinding.getValue();
    private Number leftToWin = leftToWinBinding.getValue();
    private Number rightToWin = rightToWinBinding.getValue();
    private List<MutablePair<Number, String>> ticks = new ArrayList<>();
    private List<MutableTriple<Number, String, Color>> outerLabels = new ArrayList<>();
    private int numBucketsPerSide = numBucketsPerSideBinding.getValue();
    private List<MutableTriple<Number, Color, String>> dots = new ArrayList<>();

    public SwingPanel() {
      setBackground(Color.WHITE);
    }

    public void setLeftColor(Color leftColor) {
      this.leftColor = leftColor;
      repaint();
    }

    public void setRightColor(Color rightColor) {
      this.rightColor = rightColor;
      repaint();
    }

    public void setValue(Number value) {
      this.value = value;
      repaint();
    }

    public void setRange(Number range) {
      this.range = range;
      repaint();
    }

    public void setLeftToWin(Number leftToWin) {
      this.leftToWin = leftToWin;
      repaint();
    }

    public void setRightToWin(Number rightToWin) {
      this.rightToWin = rightToWin;
      repaint();
    }

    public void setNumTicks(int numTicks) {
      while (numTicks < ticks.size()) {
        ticks.remove(numTicks);
      }
      while (numTicks > ticks.size()) {
        ticks.add(MutablePair.of(0.0, ""));
      }
      repaint();
    }

    public void setTickPosition(int index, Number position) {
      ticks.get(index).setLeft(position);
      repaint();
    }

    public void setTickText(int index, String text) {
      ticks.get(index).setRight(text);
      repaint();
    }

    public void setNumOuterLabels(int numOuterLabels) {
      while (numOuterLabels < outerLabels.size()) {
        outerLabels.remove(numOuterLabels);
      }
      while (numOuterLabels > outerLabels.size()) {
        outerLabels.add(MutableTriple.of(0.0, "", Color.BLACK));
      }
      repaint();
    }

    public void setOuterLabelPosition(int index, Number position) {
      outerLabels.get(index).setLeft(position);
      repaint();
    }

    public void setOuterLabelText(int index, String text) {
      outerLabels.get(index).setMiddle(text);
      repaint();
    }

    public void setOuterLabelColor(int index, Color color) {
      outerLabels.get(index).setRight(color);
      repaint();
    }

    public void setNumBucketsPerSide(int numBucketsPerSide) {
      this.numBucketsPerSide = numBucketsPerSide;
      repaint();
    }

    public void setNumDots(int numDots) {
      while (numDots < dots.size()) {
        dots.remove(numDots);
      }
      while (numDots > dots.size()) {
        dots.add(MutableTriple.of(0.0, Color.WHITE, ""));
      }
      repaint();
    }

    public void setDotPosition(int index, Number position) {
      dots.get(index).setLeft(position);
    }

    public void setDotColor(int index, Color color) {
      dots.get(index).setMiddle(color);
    }

    public void setDotLabel(int index, String label) {
      dots.get(index).setRight(label);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      ((Graphics2D) g)
          .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      ((Graphics2D) g)
          .setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      int margin = 2;
      int arcWidth = getWidth() / 2 - 2 * margin;
      int arcHeight = getHeight() - 2 * margin;
      int arcSize = Math.min(arcWidth, arcHeight);
      int arcY = (getHeight() - arcSize) / 2;

      int boundary = arcSize * 2;
      int outer = boundary - 2 * 25;
      int inner = outer - 2 * 15;
      g.setColor(leftColor);
      g.fillArc((getWidth() - outer) / 2, arcY - outer / 2, outer, outer, 180, 90);
      g.setColor(rightColor);
      g.fillArc((getWidth() - outer) / 2, arcY - outer / 2, outer, outer, 0, -90);
      g.setColor(getBackground());
      g.fillArc((getWidth() - inner) / 2, arcY - inner / 2, inner, inner, 0, -180);

      g.setFont(StandardFont.readBoldFont(12));
      AffineTransform originalTransform = ((Graphics2D) g).getTransform();
      for (Pair<Number, String> tick : ticks) {
        ((Graphics2D) g)
            .setTransform(createRotationTransform(tick.getLeft(), originalTransform, arcY));
        int textWidth = g.getFontMetrics().stringWidth(tick.getRight());
        g.drawString(tick.getRight(), (getWidth() - textWidth) / 2, arcY + outer / 2 - 3);
        ((Graphics2D) g).setTransform(originalTransform);
      }

      double bucketSize = range.doubleValue() / numBucketsPerSide;
      var bucketedDots =
          dots.stream()
              .filter(e -> Math.abs(e.getLeft().doubleValue()) <= range.doubleValue())
              .sorted(Comparator.comparing(e -> Math.abs(e.getLeft().doubleValue())))
              .collect(
                  Collectors.groupingBy(
                      e ->
                          (int) Math.signum(e.getLeft().doubleValue())
                              * (int) Math.ceil(Math.abs(e.getLeft().doubleValue() / bucketSize))));
      int maxBucketSize = bucketedDots.values().stream().mapToInt(List::size).max().orElse(0);
      double theta = Math.PI / 2 / numBucketsPerSide;
      int dotSize =
          (int)
              (1.0
                  * inner
                  / 2
                  / (0.5 / Math.sin(theta / 2) + 1.0 * maxBucketSize / Math.cos(theta / 2)));
      for (var entry : bucketedDots.entrySet()) {
        double val = (entry.getKey() - 0.5 * Math.signum(entry.getKey())) * bucketSize;
        ((Graphics2D) g).setTransform(createRotationTransform(val, originalTransform, arcY));
        for (int dotNum = 0; dotNum < entry.getValue().size(); dotNum++) {
          var dot = entry.getValue().get(dotNum);
          g.setColor(dot.middle);
          g.fillOval(
              (getWidth() - dotSize) / 2 + 2,
              inner / 2 - (dotNum + 1) * dotSize + 2,
              dotSize - 4,
              dotSize - 4);
          g.setColor(Color.WHITE);
          String[] text = dot.right.split("\n");
          int size = (dotSize - 8) / text.length;
          Font font = null;
          while (size > 1) {
            font = StandardFont.readNormalFont(size);
            int maxWidth =
                Arrays.stream(text).mapToInt(g.getFontMetrics(font)::stringWidth).max().orElse(0);
            if (maxWidth < dotSize - 8) {
              break;
            }
            size--;
          }
          g.setFont(font);
          for (int i = 0; i < text.length; i++) {
            int strWidth = g.getFontMetrics(font).stringWidth(text[i]);
            int totalHeight = size * text.length;
            g.drawString(
                text[i],
                (getWidth() - strWidth) / 2,
                inner / 2
                    - dotNum * dotSize
                    - (dotSize - totalHeight * 3 / 4) / 2
                    + (i - text.length + 1) * size);
          }
        }
        ((Graphics2D) g).setTransform(originalTransform);
      }

      g.setColor(Color.BLACK);
      ((Graphics2D) g)
          .setTransform(createRotationTransform(value.doubleValue(), originalTransform, arcY));
      g.drawLine(getWidth() / 2, arcY, getWidth() / 2, arcY + inner / 2);
      g.fillPolygon(
          new int[] {getWidth() / 2, getWidth() / 2 - 6, getWidth() / 2 + 6},
          new int[] {arcY + inner / 2, arcY + inner / 2 - 10, arcY + inner / 2 - 10},
          3);
      ((Graphics2D) g).setTransform(originalTransform);

      ((Graphics2D) g).setStroke(new BasicStroke(3));
      if (Math.abs(leftToWin.doubleValue()) < range.doubleValue()) {
        int arcAngle = (int) Math.round(-90 * leftToWin.doubleValue() / range.doubleValue());
        g.setColor(leftColor);
        g.drawArc(
            (getWidth() - boundary) / 2,
            arcY - boundary / 2,
            boundary,
            boundary,
            180,
            arcAngle + 90);
      }
      if (Math.abs(rightToWin.doubleValue()) < range.doubleValue()) {
        int arcAngle = (int) Math.round(90 * rightToWin.doubleValue() / range.doubleValue());
        g.setColor(rightColor);
        g.drawArc(
            (getWidth() - boundary) / 2, arcY - boundary / 2, boundary, boundary, 0, arcAngle - 90);
      }
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), arcY);

      g.setFont(StandardFont.readNormalFont(20));
      for (Triple<Number, String, Color> outerLabel : outerLabels) {
        if (Math.abs(outerLabel.getLeft().doubleValue()) <= range.doubleValue()) {
          g.setColor(outerLabel.getRight());
          ((Graphics2D) g)
              .setTransform(
                  createRotationTransform(
                      outerLabel.getLeft().doubleValue(), originalTransform, arcY));
          int textWidth = g.getFontMetrics().stringWidth(outerLabel.getMiddle());
          g.drawString(
              outerLabel.getMiddle(), (getWidth() - textWidth) / 2, arcY + boundary / 2 - 6);
          ((Graphics2D) g).setTransform(originalTransform);
        }
      }
    }

    private AffineTransform createRotationTransform(
        Number value, AffineTransform originalTransform, int arcY) {
      double arcAngle = -Math.PI / 2 * value.doubleValue() / range.doubleValue();
      AffineTransform newTransform = new AffineTransform(originalTransform);
      newTransform.rotate(arcAngle, getWidth() / 2, arcY);
      return newTransform;
    }
  }
}
