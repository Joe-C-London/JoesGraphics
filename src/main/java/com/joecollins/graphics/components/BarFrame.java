package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class BarFrame extends GraphicsFrame {

  private final JPanel centralPanel;
  private final SubheadLabel subheadLabel = new SubheadLabel();
  private final List<Bar> bars = new ArrayList<>();
  private final List<Line> lines = new ArrayList<>();
  private Number min = 0.0, max = 0.0;
  private boolean usingDefaultMin = true, usingDefaultMax = true;

  private Binding<String> subheadTextBinding = () -> null;
  private Binding<Color> subheadColorBinding = () -> Color.BLACK;
  private Binding<Integer> numBarsBinding = () -> 0;
  private IndexedBinding<String> leftTextBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<String> rightTextBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Shape> leftIconBinding = IndexedBinding.emptyBinding();
  private Map<String, Pair<IndexedBinding<Color>, IndexedBinding<? extends Number>>>
      seriesBindings = new LinkedHashMap<>();
  private Binding<Integer> numLinesBinding = () -> 0;
  private IndexedBinding<? extends Number> lineLevelsBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<String> lineLabelsBinding = IndexedBinding.emptyBinding();
  private Binding<? extends Number> minBinding =
      () ->
          bars.stream()
              .mapToDouble(bar -> bar.getTotalNegative().doubleValue())
              .reduce(0, Math::min);
  private Binding<? extends Number> maxBinding =
      () ->
          bars.stream()
              .mapToDouble(bar -> bar.getTotalPositive().doubleValue())
              .reduce(0, Math::max);
  private static final int BAR_MARGIN = 2;

  public BarFrame() {
    centralPanel =
        new JPanel() {
          {
            setBackground(Color.WHITE);
            setLayout(new BarFrameLayout());
          }

          @Override
          protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            ((Graphics2D) g)
                .setRenderingHint(
                    RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            ((Graphics2D) g)
                .setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawLines(
                g,
                subheadLabel.isVisible() ? subheadLabel.getHeight() : BAR_MARGIN,
                getHeight() - BAR_MARGIN);
          }
        };
    add(centralPanel, BorderLayout.CENTER);

    centralPanel.add(subheadLabel);
  }

  private void drawLines(Graphics g, int top, int bottom) {
    g.setColor(Color.BLACK);
    for (var line : lines) {
      int level = (int) getPixelOfValue(line.level);
      g.drawLine(level, top, level, bottom);
    }
  }

  String getSubheadText() {
    return subheadLabel.isVisible() ? subheadLabel.getText() : null;
  }

  public void setSubheadTextBinding(Binding<String> subheadTextBinding) {
    this.subheadTextBinding.unbind();
    this.subheadTextBinding = subheadTextBinding;
    this.subheadTextBinding.bind(
        subheadText -> {
          subheadLabel.setVisible(subheadText != null);
          if (subheadText != null) {
            subheadLabel.setText(subheadText);
          }
        });
  }

  Color getSubheadColor() {
    return subheadLabel.getForeground();
  }

  public void setSubheadColorBinding(Binding<Color> subheadColorBinding) {
    this.subheadColorBinding.unbind();
    this.subheadColorBinding = subheadColorBinding;
    this.subheadColorBinding.bind(subheadLabel::setForeground);
  }

  int getNumBars() {
    return bars.size();
  }

  public void setNumBarsBinding(Binding<Integer> numBarsBinding) {
    this.numBarsBinding.unbind();
    this.numBarsBinding = numBarsBinding;
    this.numBarsBinding.bind(
        numBars -> {
          while (bars.size() < numBars) {
            Bar bar = new Bar();
            bars.add(bar);
            centralPanel.add(bar);
          }
          while (bars.size() > numBars) {
            Bar bar = bars.remove(numBars.intValue());
            centralPanel.remove(bar);
          }
          centralPanel.invalidate();
          centralPanel.revalidate();
          repaint();
        });
  }

  String getLeftText(int barNum) {
    return bars.get(barNum).leftText;
  }

  public void setLeftTextBinding(IndexedBinding<String> leftTextBinding) {
    this.leftTextBinding.unbind();
    this.leftTextBinding = leftTextBinding;
    this.leftTextBinding.bind((idx, leftText) -> bars.get(idx).setLeftText(leftText));
  }

  String getRightText(int barNum) {
    return bars.get(barNum).rightText;
  }

  public void setRightTextBinding(IndexedBinding<String> rightTextBinding) {
    this.rightTextBinding.unbind();
    this.rightTextBinding = rightTextBinding;
    this.rightTextBinding.bind((idx, rightText) -> bars.get(idx).setRightText(rightText));
  }

  List<Pair<Color, Number>> getSeries(int barNum) {
    return bars.get(barNum).series.stream()
        .map(e -> new ImmutablePair<>(e.getKey(), e.getValue()))
        .collect(Collectors.toUnmodifiableList());
  }

  public void addSeriesBinding(
      String seriesName,
      IndexedBinding<Color> colorBinding,
      IndexedBinding<? extends Number> valueBinding) {
    var oldSeries = seriesBindings.get(seriesName);
    if (oldSeries != null) {
      oldSeries.getLeft().unbind();
      oldSeries.getRight().unbind();
    }
    seriesBindings.put(seriesName, new ImmutablePair<>(colorBinding, valueBinding));
    int seriesNum = new ArrayList<>(seriesBindings.keySet()).indexOf(seriesName);
    colorBinding.bind((idx, color) -> bars.get(idx).setColor(seriesNum, color));
    valueBinding.bind(
        (idx, color) -> {
          bars.get(idx).setValue(seriesNum, color);
          if (usingDefaultMin) {
            Number newMin = minBinding.getValue();
            if (newMin.doubleValue() != min.doubleValue()) {
              min = newMin;
              repaint();
            }
          }
          if (usingDefaultMax) {
            Number newMax = maxBinding.getValue();
            if (newMax.doubleValue() != max.doubleValue()) {
              max = newMax;
              repaint();
            }
          }
        });
  }

  Shape getLeftIcon(int barNum) {
    return bars.get(barNum).leftIcon;
  }

  public void setLeftIconBinding(IndexedBinding<Shape> leftIconBinding) {
    this.leftIconBinding.unbind();
    this.leftIconBinding = leftIconBinding;
    this.leftIconBinding.bind((idx, shape) -> bars.get(idx).setLeftIcon(shape));
  }

  Number getMin() {
    return min;
  }

  public void setMinBinding(Binding<? extends Number> minBinding) {
    usingDefaultMin = false;
    this.minBinding.unbind();
    this.minBinding = minBinding;
    this.minBinding.bind(
        min -> {
          this.min = min;
          repaint();
        });
  }

  Number getMax() {
    return max;
  }

  public void setMaxBinding(Binding<? extends Number> maxBinding) {
    usingDefaultMax = false;
    this.maxBinding.unbind();
    this.maxBinding = maxBinding;
    this.maxBinding.bind(
        max -> {
          this.max = max;
          repaint();
        });
  }

  private double getPixelOfValue(Number value) {
    double range = max.doubleValue() - min.doubleValue();
    double progress = value.doubleValue() - min.doubleValue();
    return (int) ((centralPanel.getWidth() - 2 * BAR_MARGIN) * progress / range) + BAR_MARGIN;
  }

  private int getMaxLines() {
    return bars.stream().mapToInt(Bar::getNumLines).max().orElse(1);
  }

  int getNumLines() {
    return lines.size();
  }

  public void setNumLinesBinding(Binding<Integer> numLinesBinding) {
    this.numLinesBinding.unbind();
    this.numLinesBinding = numLinesBinding;
    this.numLinesBinding.bind(
        size -> {
          while (size > lines.size()) {
            Line line = new Line();
            lines.add(line);
            centralPanel.add(line.label);
          }
          while (size < lines.size()) {
            Line line = lines.remove(size.intValue());
            centralPanel.remove(line.label);
          }
        });
  }

  Number getLineLevel(int index) {
    return lines.get(index).level;
  }

  public void setLineLevelsBinding(IndexedBinding<? extends Number> lineLevelsBinding) {
    this.lineLevelsBinding.unbind();
    this.lineLevelsBinding = lineLevelsBinding;
    this.lineLevelsBinding.bind((idx, level) -> lines.get(idx).setLevel(level));
  }

  String getLineLabel(int index) {
    return lines.get(index).label.getText();
  }

  public void setLineLabelsBinding(IndexedBinding<String> lineLabelsBinding) {
    this.lineLabelsBinding.unbind();
    this.lineLabelsBinding = lineLabelsBinding;
    this.lineLabelsBinding.bind((idx, label) -> lines.get(idx).setLabel(label));
  }

  private class SubheadLabel extends JLabel {

    public SubheadLabel() {
      setForeground(Color.BLACK);
      setPreferredSize(new Dimension(1024, 30));
      setFont(StandardFont.readBoldFont(20));
      setVisible(false);
      setHorizontalAlignment(JLabel.CENTER);
      setVerticalAlignment(JLabel.BOTTOM);
      addComponentListener(
          new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
              setFont(StandardFont.readBoldFont(getHeight() * 2 / 3));
            }
          });
    }
  }

  private class Bar extends JPanel {
    private String leftText = "";
    private String rightText = "";
    private List<MutablePair<Color, Number>> series = new ArrayList<>();
    private Shape leftIcon = null;

    public Bar() {
      resetPreferredSize();
      setBackground(Color.WHITE);
    }

    private void resetPreferredSize() {
      setPreferredSize(new Dimension(1024, 30 * getNumLines()));
    }

    private void setLeftText(String leftText) {
      this.leftText = leftText;
      resetPreferredSize();
      repaint();
    }

    private void setRightText(String rightText) {
      this.rightText = rightText;
      resetPreferredSize();
      repaint();
    }

    private void setLeftIcon(Shape leftIcon) {
      this.leftIcon = leftIcon;
      repaint();
    }

    private void setColor(int idx, Color color) {
      while (series.size() <= idx) {
        series.add(new MutablePair<>(Color.BLACK, 0));
      }
      series.get(idx).setLeft(color);
      repaint();
    }

    private void setValue(int idx, Number value) {
      while (series.size() <= idx) {
        series.add(new MutablePair<>(Color.BLACK, 0));
      }
      series.get(idx).setRight(value);
      repaint();
    }

    private Number getTotalPositive() {
      return series.stream().mapToDouble(e -> e.getRight().doubleValue()).filter(v -> v > 0).sum();
    }

    private Number getTotalNegative() {
      return series.stream().mapToDouble(e -> e.getRight().doubleValue()).filter(v -> v < 0).sum();
    }

    private int getNumLines() {
      int leftLines = leftText.split("\n").length;
      int rightLines = rightText.split("\n").length;
      return Math.max(leftLines, rightLines);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      ((Graphics2D) g)
          .setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      drawLines(g, 0, getHeight());
      Font font = StandardFont.readBoldFont(getBarHeight() * 3 / 4 / getMaxLines());
      g.setFont(font);
      Color mainColor = series.isEmpty() ? Color.BLACK : series.get(0).left;
      g.setColor(mainColor);
      drawText(g, font);

      int zero = (int) getPixelOfValue(0.0);
      int posLeft = zero;
      int negRight = zero;
      for (var seriesItem : series) {
        g.setColor(seriesItem.left);
        int width = (int) getPixelOfValue(seriesItem.right) - zero;
        if (width > 0) {
          g.fillRect(posLeft, BAR_MARGIN, width, getBarHeight());
          posLeft += width;
        } else {
          negRight += width;
          g.fillRect(negRight, BAR_MARGIN, -width, getBarHeight());
        }
      }

      if (posLeft != zero && negRight != zero) {
        g.setColor(Color.WHITE);
        g.drawLine(zero, 0, zero, getHeight());
      }

      Shape oldClip = g.getClip();
      g.setClip(negRight, 0, posLeft - negRight, getHeight());
      g.setColor(Color.WHITE);
      drawText(g, font);
      g.setClip(oldClip);
    }

    private int getBarHeight() {
      return getHeight() - 2 * BAR_MARGIN;
    }

    private void drawText(Graphics g, Font font) {
      Map<Boolean, Double> sumsPosNeg =
          series.stream()
              .map(e -> e.getRight().doubleValue())
              .collect(Collectors.partitioningBy(e -> e > 0, Collectors.summingDouble(Math::abs)));
      boolean isNetPositive = sumsPosNeg.get(true) >= sumsPosNeg.get(false);
      double zero = getPixelOfValue(0.0);

      String[] leftText = this.leftText.split("\n");
      double leftMax = zero;
      for (int i = 0; i < leftText.length; i++) {
        int leftWidth = g.getFontMetrics(font).stringWidth(leftText[i]);
        int textHeight = font.getSize();
        int textBase = (i + 1) * (getBarHeight() + textHeight) / (leftText.length + 1);
        if (isNetPositive) {
          g.drawString(leftText[i], (int) zero, textBase);
          leftMax = Math.max(leftMax, leftWidth + zero);
        } else {
          g.drawString(leftText[i], (int) zero - leftWidth, textBase);
          leftMax = Math.min(leftMax, zero - leftWidth);
        }
      }

      String[] rightText = this.rightText.split("\n");
      for (int i = 0; i < rightText.length; i++) {
        int rightWidth = g.getFontMetrics(font).stringWidth(rightText[i]);
        int textHeight = font.getSize();
        int textBase = (i + 1) * (getBarHeight() + textHeight) / (rightText.length + 1);
        if (isNetPositive) {
          g.drawString(rightText[i], getWidth() - rightWidth - BAR_MARGIN, textBase);
        } else {
          g.drawString(rightText[i], BAR_MARGIN, textBase);
        }
      }

      if (leftIcon != null) {
        Rectangle leftIconBounds = leftIcon.getBounds();
        double leftIconScale = (getBarHeight() - 2 * BAR_MARGIN) / leftIconBounds.getHeight();
        AffineTransform transform = new AffineTransform();
        int spaceWidth = g.getFontMetrics(font).stringWidth(" ");
        if (isNetPositive) {
          transform.translate(leftMax + spaceWidth, 2 * BAR_MARGIN);
        } else {
          transform.translate(
              leftMax - spaceWidth - leftIconScale * leftIconBounds.getWidth(), 2 * BAR_MARGIN);
        }
        transform.scale(leftIconScale, leftIconScale);
        Shape scaledLeftIcon = transform.createTransformedShape(leftIcon);
        ((Graphics2D) g).fill(scaledLeftIcon);
      }
    }
  }

  private class Line {
    private Number level = 0;
    private JLabel label =
        new JLabel("") {
          {
            setForeground(Color.BLACK);
            setPreferredSize(new Dimension(1024, 15));
            setFont(StandardFont.readNormalFont(10));
            setVisible(true);
            setHorizontalAlignment(JLabel.LEFT);
            setVerticalAlignment(JLabel.BOTTOM);
            addComponentListener(
                new ComponentAdapter() {
                  @Override
                  public void componentResized(ComponentEvent e) {
                    setFont(StandardFont.readNormalFont(getHeight() * 2 / 3));
                  }
                });
          }
        };

    public void setLevel(Number level) {
      this.level = level;
    }

    public void setLabel(String label) {
      this.label.setText(label);
    }
  }

  private class BarFrameLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return getLayoutSize(JComponent::getPreferredSize);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return getLayoutSize(JComponent::getMinimumSize);
    }

    private Dimension getLayoutSize(Function<JComponent, Dimension> func) {
      int width = 0, height = 0;
      if (subheadLabel.isVisible()) {
        Dimension subheadSize = func.apply(subheadLabel);
        width = subheadSize.width;
        height = subheadSize.height;
      }
      int barHeight = 0;
      for (Bar bar : bars) {
        Dimension barSize = func.apply(bar);
        width = Math.max(width, barSize.width);
        barHeight = Math.max(barHeight, barSize.height);
      }
      height += bars.size() * barHeight;
      if (!lines.isEmpty()) {
        height += lines.get(0).label.getPreferredSize().height;
      }
      return new Dimension(width, height);
    }

    @Override
    public void layoutContainer(Container parent) {
      int preferredHeight = preferredLayoutSize(parent).height;
      int actualHeight = parent.getHeight();
      double factor = Math.min(1.0, 1.0 * actualHeight / preferredHeight);

      int width = parent.getWidth();
      int top = 0;
      if (subheadLabel.isVisible()) {
        int height = (int) (subheadLabel.getPreferredSize().height * factor);
        subheadLabel.setLocation(0, top);
        subheadLabel.setSize(width, height);
        top += height;
      }

      int barHeight =
          (int) (bars.stream().mapToInt(i -> i.getPreferredSize().height).max().orElse(0) * factor);
      for (Bar bar : bars) {
        bar.setLocation(0, top);
        bar.setSize(width, barHeight);
        top += barHeight;
      }

      for (Line line : lines) {
        int left = (int) getPixelOfValue(line.level) + BAR_MARGIN;
        int labelHeight = Math.min(line.label.getPreferredSize().height, actualHeight - top);
        line.label.setLocation(left, actualHeight - labelHeight);
        line.label.setSize(width - left, labelHeight);
      }
    }
  }
}
