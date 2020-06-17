package com.joecollins.graphics.components;

import static java.lang.Math.PI;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.swing.JPanel;

public class HemicycleFrame extends GraphicsFrame {

  private Binding<Integer> numRowsBinding = () -> 0;
  private IndexedBinding<Integer> rowCountBinding = IndexedBinding.emptyBinding();

  private Binding<Integer> numDotsBinding = () -> 0;
  private IndexedBinding<Color> dotColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> dotBorderBinding = IndexedBinding.emptyBinding();

  private Binding<Integer> leftSeatBarCountBinding = () -> 0;
  private IndexedBinding<Color> leftSeatBarColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Integer> leftSeatBarSizeBinding = IndexedBinding.emptyBinding();
  private Binding<String> leftSeatBarLabelBinding = () -> "";

  private Binding<Integer> rightSeatBarCountBinding = () -> 0;
  private IndexedBinding<Color> rightSeatBarColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Integer> rightSeatBarSizeBinding = IndexedBinding.emptyBinding();
  private Binding<String> rightSeatBarLabelBinding = () -> "";

  private Binding<Integer> middleSeatBarCountBinding = () -> 0;
  private IndexedBinding<Color> middleSeatBarColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Integer> middleSeatBarSizeBinding = IndexedBinding.emptyBinding();
  private Binding<String> middleSeatBarLabelBinding = () -> "";

  private Binding<Integer> leftChangeBarCountBinding = () -> 0;
  private IndexedBinding<Color> leftChangeBarColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Integer> leftChangeBarSizeBinding = IndexedBinding.emptyBinding();
  private Binding<Integer> leftChangeBarStartBinding = () -> 0;
  private Binding<String> leftChangeBarLabelBinding = () -> "";

  private Binding<Integer> rightChangeBarCountBinding = () -> 0;
  private IndexedBinding<Color> rightChangeBarColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Integer> rightChangeBarSizeBinding = IndexedBinding.emptyBinding();
  private Binding<Integer> rightChangeBarStartBinding = () -> 0;
  private Binding<String> rightChangeBarLabelBinding = () -> "";

  private BarPanel barsPanel = new BarPanel();
  private DotsPanel dotsPanel = new DotsPanel();

  public HemicycleFrame() {
    JPanel panel = new JPanel();
    add(panel, BorderLayout.CENTER);

    panel.setLayout(new Layout());
    panel.add(barsPanel);
    panel.add(dotsPanel);
  }

  private class Layout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return null;
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return null;
    }

    @Override
    public void layoutContainer(Container parent) {
      int mid = 0;
      if (barsPanel.hasSeats()) {
        mid = Math.min(25, parent.getHeight() / 10);
      }
      if (barsPanel.hasChange()) {
        mid = Math.min(50, parent.getHeight() / 5);
      }
      barsPanel.setLocation(0, 0);
      barsPanel.setSize(parent.getWidth(), mid);
      dotsPanel.setLocation(0, mid);
      dotsPanel.setSize(parent.getWidth(), parent.getHeight() - mid);
    }
  }

  int getNumRows() {
    return dotsPanel.rows.size();
  }

  public void setNumRowsBinding(Binding<Integer> numRowsBinding) {
    this.numRowsBinding.unbind();
    this.numRowsBinding = numRowsBinding;
    this.numRowsBinding.bind(
        numRows -> {
          setSize(dotsPanel.rows, numRows, () -> 0);
          dotsPanel.repaint();
        });
  }

  int getRowCount(int rowNum) {
    return dotsPanel.rows.get(rowNum);
  }

  public void setRowCountsBinding(IndexedBinding<Integer> rowCountBinding) {
    this.rowCountBinding.unbind();
    this.rowCountBinding = rowCountBinding;
    this.rowCountBinding.bind(
        (idx, count) -> {
          dotsPanel.rows.set(idx, count);
          dotsPanel.repaint();
        });
  }

  int getNumDots() {
    return dotsPanel.dots.size();
  }

  public void setNumDotsBinding(Binding<Integer> numDotsBinding) {
    this.numDotsBinding.unbind();
    this.numDotsBinding = numDotsBinding;
    this.numDotsBinding.bind(
        numDots -> {
          setSize(dotsPanel.dots, numDots, Dot::new);
          dotsPanel.repaint();
        });
  }

  Color getDotColor(int dotNum) {
    return dotsPanel.dots.get(dotNum).color;
  }

  public void setDotColorBinding(IndexedBinding<Color> dotColorBinding) {
    this.dotColorBinding.unbind();
    this.dotColorBinding = dotColorBinding;
    this.dotColorBinding.bind(
        (idx, color) -> {
          dotsPanel.dots.get(idx).color = color;
          dotsPanel.repaint();
        });
  }

  Color getDotBorder(int dotNum) {
    return dotsPanel.dots.get(dotNum).border;
  }

  public void setDotBorderBinding(IndexedBinding<Color> dotBorderBinding) {
    this.dotBorderBinding.unbind();
    this.dotBorderBinding = dotBorderBinding;
    this.dotBorderBinding.bind(
        (idx, border) -> {
          dotsPanel.dots.get(idx).border = border;
          dotsPanel.repaint();
        });
  }

  int getLeftSeatBarCount() {
    return barsPanel.leftSeatBars.size();
  }

  public void setLeftSeatBarCountBinding(Binding<Integer> leftSeatBarCountBinding) {
    this.leftSeatBarCountBinding.unbind();
    this.leftSeatBarCountBinding = leftSeatBarCountBinding;
    this.leftSeatBarCountBinding.bind(
        numBars -> {
          setSize(barsPanel.leftSeatBars, numBars, Bar::new);
          barsPanel.repaint();
        });
  }

  Color getLeftSeatBarColor(int idx) {
    return barsPanel.leftSeatBars.get(idx).color;
  }

  public void setLeftSeatBarColorBinding(IndexedBinding<Color> leftSeatBarColorBinding) {
    this.leftSeatBarColorBinding.unbind();
    this.leftSeatBarColorBinding = leftSeatBarColorBinding;
    this.leftSeatBarColorBinding.bind(
        (idx, color) -> {
          barsPanel.leftSeatBars.get(idx).color = color;
          barsPanel.repaint();
        });
  }

  int getLeftSeatBarSize(int idx) {
    return barsPanel.leftSeatBars.get(idx).size;
  }

  public void setLeftSeatBarSizeBinding(IndexedBinding<Integer> leftSeatBarSizeBinding) {
    this.leftSeatBarSizeBinding.unbind();
    this.leftSeatBarSizeBinding = leftSeatBarSizeBinding;
    this.leftSeatBarSizeBinding.bind(
        (idx, size) -> {
          barsPanel.leftSeatBars.get(idx).size = size;
          barsPanel.repaint();
        });
  }

  String getLeftSeatBarLabel() {
    return barsPanel.leftSeatLabel;
  }

  public void setLeftSeatBarLabelBinding(Binding<String> leftSeatBarLabelBinding) {
    this.leftSeatBarLabelBinding.unbind();
    this.leftSeatBarLabelBinding = leftSeatBarLabelBinding;
    this.leftSeatBarLabelBinding.bind(
        label -> {
          barsPanel.leftSeatLabel = label;
          barsPanel.repaint();
        });
  }

  int getRightSeatBarCount() {
    return barsPanel.rightSeatBars.size();
  }

  public void setRightSeatBarCountBinding(Binding<Integer> rightSeatBarCountBinding) {
    this.rightSeatBarCountBinding.unbind();
    this.rightSeatBarCountBinding = rightSeatBarCountBinding;
    this.rightSeatBarCountBinding.bind(
        numBars -> {
          setSize(barsPanel.rightSeatBars, numBars, Bar::new);
          barsPanel.repaint();
        });
  }

  Color getRightSeatBarColor(int idx) {
    return barsPanel.rightSeatBars.get(idx).color;
  }

  public void setRightSeatBarColorBinding(IndexedBinding<Color> rightSeatBarColorBinding) {
    this.rightSeatBarColorBinding.unbind();
    this.rightSeatBarColorBinding = rightSeatBarColorBinding;
    this.rightSeatBarColorBinding.bind(
        (idx, color) -> {
          barsPanel.rightSeatBars.get(idx).color = color;
          barsPanel.repaint();
        });
  }

  int getRightSeatBarSize(int idx) {
    return barsPanel.rightSeatBars.get(idx).size;
  }

  public void setRightSeatBarSizeBinding(IndexedBinding<Integer> rightSeatBarSizeBinding) {
    this.rightSeatBarSizeBinding.unbind();
    this.rightSeatBarSizeBinding = rightSeatBarSizeBinding;
    this.rightSeatBarSizeBinding.bind(
        (idx, size) -> {
          barsPanel.rightSeatBars.get(idx).size = size;
          barsPanel.repaint();
        });
  }

  String getRightSeatBarLabel() {
    return barsPanel.rightSeatLabel;
  }

  public void setRightSeatBarLabelBinding(Binding<String> rightSeatBarLabelBinding) {
    this.rightSeatBarLabelBinding.unbind();
    this.rightSeatBarLabelBinding = rightSeatBarLabelBinding;
    this.rightSeatBarLabelBinding.bind(
        label -> {
          barsPanel.rightSeatLabel = label;
          barsPanel.repaint();
        });
  }

  int getMiddleSeatBarCount() {
    return barsPanel.middleSeatBars.size();
  }

  public void setMiddleSeatBarCountBinding(Binding<Integer> middleSeatBarCountBinding) {
    this.middleSeatBarCountBinding.unbind();
    this.middleSeatBarCountBinding = middleSeatBarCountBinding;
    this.middleSeatBarCountBinding.bind(
        numBars -> {
          setSize(barsPanel.middleSeatBars, numBars, Bar::new);
          barsPanel.repaint();
        });
  }

  Color getMiddleSeatBarColor(int idx) {
    return barsPanel.middleSeatBars.get(idx).color;
  }

  public void setMiddleSeatBarColorBinding(IndexedBinding<Color> middleSeatBarColorBinding) {
    this.middleSeatBarColorBinding.unbind();
    this.middleSeatBarColorBinding = middleSeatBarColorBinding;
    this.middleSeatBarColorBinding.bind(
        (idx, color) -> {
          barsPanel.middleSeatBars.get(idx).color = color;
          barsPanel.repaint();
        });
  }

  int getMiddleSeatBarSize(int idx) {
    return barsPanel.middleSeatBars.get(idx).size;
  }

  public void setMiddleSeatBarSizeBinding(IndexedBinding<Integer> middleSeatBarSizeBinding) {
    this.middleSeatBarSizeBinding.unbind();
    this.middleSeatBarSizeBinding = middleSeatBarSizeBinding;
    this.middleSeatBarSizeBinding.bind(
        (idx, size) -> {
          barsPanel.middleSeatBars.get(idx).size = size;
          barsPanel.repaint();
        });
  }

  String getMiddleSeatBarLabel() {
    return barsPanel.middleSeatLabel;
  }

  public void setMiddleSeatBarLabelBinding(Binding<String> middleSeatBarLabelBinding) {
    this.middleSeatBarLabelBinding.unbind();
    this.middleSeatBarLabelBinding = middleSeatBarLabelBinding;
    this.middleSeatBarLabelBinding.bind(
        label -> {
          barsPanel.middleSeatLabel = label;
          barsPanel.repaint();
        });
  }

  int getLeftChangeBarCount() {
    return barsPanel.leftChangeBars.size();
  }

  public void setLeftChangeBarCountBinding(Binding<Integer> leftChangeBarCountBinding) {
    this.leftChangeBarCountBinding.unbind();
    this.leftChangeBarCountBinding = leftChangeBarCountBinding;
    this.leftChangeBarCountBinding.bind(
        numBars -> {
          setSize(barsPanel.leftChangeBars, numBars, Bar::new);
          barsPanel.repaint();
        });
  }

  Color getLeftChangeBarColor(int idx) {
    return barsPanel.leftChangeBars.get(idx).color;
  }

  public void setLeftChangeBarColorBinding(IndexedBinding<Color> leftChangeBarColorBinding) {
    this.leftChangeBarColorBinding.unbind();
    this.leftChangeBarColorBinding = leftChangeBarColorBinding;
    this.leftChangeBarColorBinding.bind(
        (idx, color) -> {
          barsPanel.leftChangeBars.get(idx).color = color;
          barsPanel.repaint();
        });
  }

  int getLeftChangeBarSize(int idx) {
    return barsPanel.leftChangeBars.get(idx).size;
  }

  public void setLeftChangeBarSizeBinding(IndexedBinding<Integer> leftChangeBarSizeBinding) {
    this.leftChangeBarSizeBinding.unbind();
    this.leftChangeBarSizeBinding = leftChangeBarSizeBinding;
    this.leftChangeBarSizeBinding.bind(
        (idx, size) -> {
          barsPanel.leftChangeBars.get(idx).size = size;
          barsPanel.repaint();
        });
  }

  int getLeftChangeBarStart() {
    return barsPanel.leftChangeStart;
  }

  public void setLeftChangeBarStartBinding(Binding<Integer> leftChangeBarStartBinding) {
    this.leftChangeBarStartBinding.unbind();
    this.leftChangeBarStartBinding = leftChangeBarStartBinding;
    this.leftChangeBarStartBinding.bind(
        start -> {
          barsPanel.leftChangeStart = start;
          barsPanel.repaint();
        });
  }

  String getLeftChangeBarLabel() {
    return barsPanel.leftChangeLabel;
  }

  public void setLeftChangeBarLabelBinding(Binding<String> leftChangeBarLabelBinding) {
    this.leftChangeBarLabelBinding.unbind();
    this.leftChangeBarLabelBinding = leftChangeBarLabelBinding;
    this.leftChangeBarLabelBinding.bind(
        label -> {
          barsPanel.leftChangeLabel = label;
          barsPanel.repaint();
        });
  }

  int getRightChangeBarCount() {
    return barsPanel.rightChangeBars.size();
  }

  public void setRightChangeBarCountBinding(Binding<Integer> rightChangeBarCountBinding) {
    this.rightChangeBarCountBinding.unbind();
    this.rightChangeBarCountBinding = rightChangeBarCountBinding;
    this.rightChangeBarCountBinding.bind(
        numBars -> {
          setSize(barsPanel.rightChangeBars, numBars, Bar::new);
          barsPanel.repaint();
        });
  }

  Color getRightChangeBarColor(int idx) {
    return barsPanel.rightChangeBars.get(idx).color;
  }

  public void setRightChangeBarColorBinding(IndexedBinding<Color> rightChangeBarColorBinding) {
    this.rightChangeBarColorBinding.unbind();
    this.rightChangeBarColorBinding = rightChangeBarColorBinding;
    this.rightChangeBarColorBinding.bind(
        (idx, color) -> {
          barsPanel.rightChangeBars.get(idx).color = color;
          barsPanel.repaint();
        });
  }

  int getRightChangeBarSize(int idx) {
    return barsPanel.rightChangeBars.get(idx).size;
  }

  public void setRightChangeBarSizeBinding(IndexedBinding<Integer> rightChangeBarSizeBinding) {
    this.rightChangeBarSizeBinding.unbind();
    this.rightChangeBarSizeBinding = rightChangeBarSizeBinding;
    this.rightChangeBarSizeBinding.bind(
        (idx, size) -> {
          barsPanel.rightChangeBars.get(idx).size = size;
          barsPanel.repaint();
        });
  }

  int getRightChangeBarStart() {
    return barsPanel.rightChangeStart;
  }

  public void setRightChangeBarStartBinding(Binding<Integer> rightChangeBarStartBinding) {
    this.rightChangeBarStartBinding.unbind();
    this.rightChangeBarStartBinding = rightChangeBarStartBinding;
    this.rightChangeBarStartBinding.bind(
        start -> {
          barsPanel.rightChangeStart = start;
          barsPanel.repaint();
        });
  }

  String getRightChangeBarLabel() {
    return barsPanel.rightChangeLabel;
  }

  public void setRightChangeBarLabelBinding(Binding<String> rightChangeBarLabelBinding) {
    this.rightChangeBarLabelBinding.unbind();
    this.rightChangeBarLabelBinding = rightChangeBarLabelBinding;
    this.rightChangeBarLabelBinding.bind(
        label -> {
          barsPanel.rightChangeLabel = label;
          barsPanel.repaint();
        });
  }

  private static <T> void setSize(List<T> list, int size, Supplier<T> defaultItem) {
    while (size > list.size()) {
      list.add(defaultItem.get());
    }
    while (size < list.size()) {
      list.remove(size);
    }
  }

  private class Bar {

    private Color color;
    private int size;
  }

  private class BarPanel extends JPanel {

    private List<Bar> leftSeatBars = new ArrayList<>();
    private String leftSeatLabel = "";
    private List<Bar> rightSeatBars = new ArrayList<>();
    private String rightSeatLabel = "";
    private List<Bar> middleSeatBars = new ArrayList<>();
    private String middleSeatLabel = "";

    private List<Bar> leftChangeBars = new ArrayList<>();
    private String leftChangeLabel = "";
    private int leftChangeStart = 0;
    private List<Bar> rightChangeBars = new ArrayList<>();
    private String rightChangeLabel = "";
    private int rightChangeStart = 0;

    public BarPanel() {
      setBackground(Color.WHITE);
    }

    boolean hasSeats() {
      return !leftSeatBars.isEmpty()
          || !middleSeatBars.isEmpty()
          || !rightSeatBars.isEmpty()
          || !leftSeatLabel.isEmpty()
          || !middleSeatLabel.isEmpty()
          || !rightSeatLabel.isEmpty();
    }

    boolean hasChange() {
      return !leftChangeBars.isEmpty()
          || !rightChangeBars.isEmpty()
          || !leftChangeLabel.isEmpty()
          || !rightChangeLabel.isEmpty();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      ((Graphics2D) g)
          .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      ((Graphics2D) g)
          .setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      g.setColor(Color.BLACK);
      g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());

      g.setFont(StandardFont.readBoldFont((getHeight() / (hasChange() ? 2 : 1)) * 4 / 5));

      if (hasSeats()) {
        paintSeatBars(g);
      }
      if (hasChange()) {
        paintChangeBars(g);
      }
    }

    private void paintSeatBars(Graphics g) {
      int height = getHeight() / (hasChange() ? 2 : 1);
      int seatBaseline = height * 4 / 5;

      g.setColor(leftSeatBars.isEmpty() ? Color.BLACK : leftSeatBars.get(0).color);
      g.drawString(leftSeatLabel, 5, seatBaseline);

      g.setColor(rightSeatBars.isEmpty() ? Color.BLACK : rightSeatBars.get(0).color);
      int rightWidth = g.getFontMetrics().stringWidth(rightSeatLabel);
      int rightLeft = getWidth() - rightWidth - 5;
      g.drawString(rightSeatLabel, rightLeft, seatBaseline);

      g.setColor(middleSeatBars.isEmpty() ? Color.BLACK : middleSeatBars.get(0).color);
      int middleWidth = g.getFontMetrics().stringWidth(middleSeatLabel);
      int middleLeft = getMiddleStartPosition(0) - middleWidth / 2;
      g.drawString(middleSeatLabel, middleLeft, seatBaseline);

      int seatBarTop = height / 10;
      int seatBarHeight = height * 4 / 5;

      int leftSoFar = 0;
      for (Bar bar : leftSeatBars) {
        int start = getLeftPosition(leftSoFar);
        int end = getLeftPosition(leftSoFar + bar.size);
        g.setColor(bar.color);
        g.fillRect(start, seatBarTop, end - start, seatBarHeight);
        leftSoFar += bar.size;
      }
      Shape leftClip = new Rectangle(0, seatBarTop, getLeftPosition(leftSoFar), seatBarHeight);

      int rightSoFar = 0;
      for (Bar bar : rightSeatBars) {
        int start = getRightPosition(rightSoFar + bar.size);
        int end = getRightPosition(rightSoFar);
        g.setColor(bar.color);
        g.fillRect(start, seatBarTop, end - start, seatBarHeight);
        rightSoFar += bar.size;
      }
      Shape rightClip =
          new Rectangle(
              getRightPosition(rightSoFar),
              seatBarTop,
              getWidth() - getRightPosition(rightSoFar),
              seatBarHeight);

      int middleSoFar = 0;
      for (Bar bar : middleSeatBars) {
        g.setColor(bar.color);
        int startL = getMiddleStartPosition(middleSoFar + bar.size);
        int endL = getMiddleStartPosition(middleSoFar);
        int startR = getMiddleEndPosition(middleSoFar);
        int endR = getMiddleEndPosition(middleSoFar + bar.size);
        g.fillRect(startL, seatBarTop, endL - startL, seatBarHeight);
        g.fillRect(startR, seatBarTop, endR - startR, seatBarHeight);
        middleSoFar += bar.size;
      }
      Shape middleClip =
          new Rectangle(
              getMiddleStartPosition(middleSoFar),
              seatBarTop,
              getMiddleEndPosition(middleSoFar) - getMiddleStartPosition(middleSoFar),
              seatBarHeight);

      Shape oldClip = g.getClip();
      Area newClip = new Area();
      newClip.add(new Area(leftClip));
      newClip.add(new Area(rightClip));
      newClip.add(new Area(middleClip));
      g.setClip(newClip);
      g.setColor(Color.WHITE);
      g.drawString(leftSeatLabel, 5, seatBaseline);
      g.drawString(rightSeatLabel, rightLeft, seatBaseline);
      g.drawString(middleSeatLabel, middleLeft, seatBaseline);

      g.setClip(oldClip);
    }

    private void paintChangeBars(Graphics g) {
      int height = getHeight() / (hasChange() ? 2 : 1);
      int seatBaseline = height * 4 / 5 + height;

      g.setColor(leftChangeBars.isEmpty() ? Color.BLACK : leftChangeBars.get(0).color);
      int leftLeft = getLeftPosition(leftChangeStart) + 5;
      g.drawString(leftChangeLabel, leftLeft, seatBaseline);

      g.setColor(rightChangeBars.isEmpty() ? Color.BLACK : rightChangeBars.get(0).color);
      int rightWidth = g.getFontMetrics().stringWidth(rightChangeLabel);
      int rightLeft = getRightPosition(rightChangeStart) - rightWidth - 5;
      g.drawString(rightChangeLabel, rightLeft, seatBaseline);

      int changeBarHeight = height * 4 / 5;
      int changeBarTop = height / 10 + height;
      int changeBarMid = changeBarTop + changeBarHeight / 2;
      int changeBarBottom = changeBarTop + changeBarHeight;

      int leftSoFar = leftChangeStart;
      int leftBase = getLeftPosition(leftChangeStart);
      for (Bar bar : leftChangeBars) {
        int start = getLeftPosition(leftSoFar);
        int end = getLeftPosition(leftSoFar + bar.size);
        int startSide = Math.max(leftBase, start - changeBarHeight / 2);
        int endSide = Math.max(leftBase, end - changeBarHeight / 2);
        g.setColor(bar.color);
        List<Point> points = new ArrayList<>();
        points.add(new Point(startSide, changeBarTop));
        points.add(new Point(start, changeBarMid));
        points.add(new Point(startSide, changeBarBottom));
        points.add(new Point(endSide, changeBarBottom));
        points.add(new Point(end, changeBarMid));
        points.add(new Point(endSide, changeBarTop));
        g.fillPolygon(
            points.stream().mapToInt(p -> (int) p.getX()).toArray(),
            points.stream().mapToInt(p -> (int) p.getY()).toArray(),
            points.size());
        leftSoFar += bar.size;
      }
      int leftTip = getLeftPosition(leftSoFar);
      int leftSize = Math.max(leftBase, leftTip - changeBarHeight / 2);
      Shape leftClip =
          new Polygon(
              new int[] {leftBase, leftBase, leftSize, leftTip, leftSize},
              new int[] {
                changeBarTop, changeBarBottom, changeBarBottom, changeBarMid, changeBarTop
              },
              5);

      int rightSoFar = rightChangeStart;
      int rightBase = getRightPosition(rightChangeStart);
      for (Bar bar : rightChangeBars) {
        int start = getRightPosition(rightSoFar);
        int end = getRightPosition(rightSoFar + bar.size);
        int startSide = Math.min(rightBase, start + changeBarHeight / 2);
        int endSide = Math.min(rightBase, end + changeBarHeight / 2);
        g.setColor(bar.color);
        List<Point> points = new ArrayList<>();
        points.add(new Point(startSide, changeBarTop));
        points.add(new Point(start, changeBarMid));
        points.add(new Point(startSide, changeBarBottom));
        points.add(new Point(endSide, changeBarBottom));
        points.add(new Point(end, changeBarMid));
        points.add(new Point(endSide, changeBarTop));
        g.fillPolygon(
            points.stream().mapToInt(p -> (int) p.getX()).toArray(),
            points.stream().mapToInt(p -> (int) p.getY()).toArray(),
            points.size());
        rightSoFar += bar.size;
      }
      int rightTip = getRightPosition(rightSoFar);
      int rightSize = Math.min(rightBase, rightTip + changeBarHeight / 2);
      Shape rightClip =
          new Polygon(
              new int[] {rightBase, rightBase, rightSize, rightTip, rightSize},
              new int[] {
                changeBarTop, changeBarBottom, changeBarBottom, changeBarMid, changeBarTop
              },
              5);

      Shape oldClip = g.getClip();
      Area newClip = new Area();
      newClip.add(new Area(leftClip));
      newClip.add(new Area(rightClip));
      g.setClip(newClip);
      g.setColor(Color.WHITE);
      g.drawString(leftChangeLabel, leftLeft, seatBaseline);
      g.drawString(rightChangeLabel, rightLeft, seatBaseline);

      g.setClip(oldClip);
    }

    private int getLeftPosition(int seats) {
      return getSize(seats);
    }

    private int getRightPosition(int seats) {
      return getWidth() - getSize(seats);
    }

    private int getMiddleStartPosition(int seats) {
      int midSize = getSize(middleSeatBars.stream().mapToInt(e -> e.size).sum());
      int leftSize = getSize(leftSeatBars.stream().mapToInt(e -> e.size).sum());
      int rightSize = getSize(rightSeatBars.stream().mapToInt(e -> e.size).sum());
      int midPoint;
      if (leftSize + midSize / 2 > getWidth() / 2) {
        midPoint = leftSize + midSize / 2;
      } else if (rightSize + midSize / 2 > getWidth() / 2) {
        midPoint = getWidth() - rightSize - midSize / 2;
      } else {
        midPoint = getWidth() / 2;
      }
      return midPoint - getSize(seats) / 2;
    }

    private int getMiddleEndPosition(int seats) {
      return getMiddleStartPosition(seats) + getSize(seats);
    }

    private int getSize(int seats) {
      return (int) Math.round(1.0 * getWidth() * seats / getNumDots());
    }
  }

  private class Dot {

    private Color color;
    private Color border;
  }

  private class DotsPanel extends JPanel {

    private List<Integer> rows = new ArrayList<>();
    private List<Dot> dots = new ArrayList<>();

    public DotsPanel() {
      setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      ((Graphics2D) g)
          .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      ((Graphics2D) g)
          .setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      double dFrac = 1.0 / (rows.size() - 0.5);
      for (int rowsFromInner = 0; rowsFromInner < rows.size(); rowsFromInner++) {
        int rowsFromOuter = rows.size() - rowsFromInner - 1;
        int dotsInRow = rows.get(rowsFromInner);
        double dForRow = (PI / (dotsInRow - 1)) / (1 + rowsFromOuter * PI / (dotsInRow - 1));
        dFrac = Math.min(dFrac, dForRow);
      }

      int arcLimit = Math.min(getHeight(), getWidth() / 2);
      double r = 1.0 * arcLimit / (1 + dFrac);
      double d = r * dFrac;
      int arcY = (int) Math.round(getHeight() - d / 2);

      g.setColor(Color.BLACK);
      g.drawLine(getWidth() / 2, 0, getWidth() / 2, (int) Math.round(getHeight() - d / 2));

      List<Integer> rowStartIndexes = new ArrayList<>(rows.size());
      for (int i = 0; i < rows.size(); i++) {
        if (i == 0) {
          rowStartIndexes.add(0);
        } else {
          rowStartIndexes.add(rowStartIndexes.get(i - 1) + rows.get(i - 1));
        }
      }

      AffineTransform originalTransform = ((Graphics2D) g).getTransform();
      for (int rowsFromInner = 0; rowsFromInner < rows.size(); rowsFromInner++) {
        int rowsFromOuter = rows.size() - rowsFromInner - 1;
        int dotsInRow = rows.get(rowsFromInner);
        int firstDot = rowStartIndexes.get(rowsFromInner);
        for (int dotNum = 0; dotNum < dotsInRow; dotNum++) {
          Dot dot = dots.get(firstDot + dotNum);
          ((Graphics2D) g)
              .setTransform(
                  createRotationTransform(1.0 * dotNum / (dotsInRow - 1), originalTransform, arcY));
          int x = getWidth() / 2;
          int y = getHeight() - (int) Math.round(r + (0.5 - rowsFromOuter) * d);
          int rad = (int) Math.round(d / 2 * 4 / 5);
          ((Graphics2D) g).setStroke(new BasicStroke(Math.max(1, rad / 5)));
          if (dot.color != null) {
            g.setColor(dot.color);
            g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad);
          }
          if (dot.border != null) {
            g.setColor(dot.border);
            g.drawOval(x - rad, y - rad, 2 * rad, 2 * rad);
          }
          ((Graphics2D) g).setTransform(originalTransform);
        }
      }
    }

    private AffineTransform createRotationTransform(
        double frac, AffineTransform originalTransform, int arcY) {
      double arcAngle = Math.PI * (frac - 0.5);
      AffineTransform newTransform = new AffineTransform(originalTransform);
      newTransform.rotate(arcAngle, getWidth() / 2, arcY);
      return newTransform;
    }
  }
}
