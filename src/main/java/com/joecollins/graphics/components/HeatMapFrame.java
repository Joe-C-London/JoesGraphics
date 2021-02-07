package com.joecollins.graphics.components;

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
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntBinaryOperator;
import javax.swing.JPanel;

public class HeatMapFrame extends GraphicsFrame {

  private Binding<Integer> numRowsBinding = Binding.fixedBinding(1);
  private Binding<Integer> numSquaresBinding = Binding.fixedBinding(0);
  private IndexedBinding<Color> squareBordersBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> squareFillBinding = IndexedBinding.emptyBinding();

  private Binding<Integer> numSeatBarsBinding = Binding.fixedBinding(0);
  private IndexedBinding<Color> seatBarColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Integer> seatBarSizeBinding = IndexedBinding.emptyBinding();
  private Binding<String> seatBarLabelBinding = Binding.fixedBinding("");

  private Binding<Integer> numChangeBarsBinding = Binding.fixedBinding(0);
  private IndexedBinding<Color> changeBarColorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Integer> changeBarSizeBinding = IndexedBinding.emptyBinding();
  private Binding<String> changeBarLabelBinding = Binding.fixedBinding("");
  private Binding<Integer> changeBarStartBinding = Binding.fixedBinding(0);

  private final SeatBarPanel barsPanel = new SeatBarPanel();
  private final SquaresPanel squaresPanel = new SquaresPanel();

  public HeatMapFrame() {
    JPanel panel = new JPanel();
    add(panel, BorderLayout.CENTER);

    panel.setLayout(new Layout());
    panel.add(barsPanel);
    panel.add(squaresPanel);
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
      squaresPanel.setLocation(0, mid);
      squaresPanel.setSize(parent.getWidth(), parent.getHeight() - mid);
    }
  }

  int getNumRows() {
    return squaresPanel.numRows;
  }

  public void setNumRowsBinding(Binding<Integer> numRowsBinding) {
    this.numRowsBinding.unbind();
    this.numRowsBinding = numRowsBinding;
    this.numRowsBinding.bindLegacy(squaresPanel::setNumRows);
  }

  int getNumSquares() {
    return squaresPanel.squares.size();
  }

  public void setNumSquaresBinding(Binding<Integer> numSquaresBinding) {
    this.numSquaresBinding.unbind();
    this.numSquaresBinding = numSquaresBinding;
    this.numSquaresBinding.bindLegacy(squaresPanel::setNumSquares);
  }

  Color getSquareBorder(int index) {
    return squaresPanel.squares.get(index).borderColor;
  }

  public void setSquareBordersBinding(IndexedBinding<Color> squareBordersBinding) {
    this.squareBordersBinding.unbind();
    this.squareBordersBinding = squareBordersBinding;
    this.squareBordersBinding.bindLegacy(squaresPanel::setSquareBorder);
  }

  Color getSquareFill(int index) {
    return squaresPanel.squares.get(index).fillColor;
  }

  public void setSquareFillBinding(IndexedBinding<Color> squareFillBinding) {
    this.squareFillBinding.unbind();
    this.squareFillBinding = squareFillBinding;
    this.squareFillBinding.bindLegacy(squaresPanel::setSquareFill);
  }

  int getSeatBarCount() {
    return barsPanel.seatBars.size();
  }

  public void setNumSeatBarsBinding(Binding<Integer> numSeatBarsBinding) {
    this.numSeatBarsBinding.unbind();
    this.numSeatBarsBinding = numSeatBarsBinding;
    this.numSeatBarsBinding.bindLegacy(barsPanel::setNumSeatBars);
  }

  Color getSeatBarColor(int index) {
    return barsPanel.seatBars.get(index).color;
  }

  public void setSeatBarColorBinding(IndexedBinding<Color> seatBarColorBinding) {
    this.seatBarColorBinding.unbind();
    this.seatBarColorBinding = seatBarColorBinding;
    this.seatBarColorBinding.bindLegacy(barsPanel::setSeatBarColor);
  }

  int getSeatBarSize(int index) {
    return barsPanel.seatBars.get(index).size;
  }

  public void setSeatBarSizeBinding(IndexedBinding<Integer> seatBarSizeBinding) {
    this.seatBarSizeBinding.unbind();
    this.seatBarSizeBinding = seatBarSizeBinding;
    this.seatBarSizeBinding.bindLegacy(barsPanel::setSeatBarSize);
  }

  String getSeatBarLabel() {
    return barsPanel.seatBarLabel;
  }

  public void setSeatBarLabelBinding(Binding<String> seatBarLabelBinding) {
    this.seatBarLabelBinding.unbind();
    this.seatBarLabelBinding = seatBarLabelBinding;
    this.seatBarLabelBinding.bindLegacy(barsPanel::setSeatBarLabel);
  }

  int getChangeBarCount() {
    return barsPanel.changeBars.size();
  }

  public void setNumChangeBarsBinding(Binding<Integer> numChangeBarsBinding) {
    this.numChangeBarsBinding.unbind();
    this.numChangeBarsBinding = numChangeBarsBinding;
    this.numChangeBarsBinding.bindLegacy(barsPanel::setNumChangeBars);
  }

  Color getChangeBarColor(int index) {
    return barsPanel.changeBars.get(index).color;
  }

  public void setChangeBarColorBinding(IndexedBinding<Color> changeBarColorBinding) {
    this.changeBarColorBinding.unbind();
    this.changeBarColorBinding = changeBarColorBinding;
    this.changeBarColorBinding.bindLegacy(barsPanel::setChangeBarColor);
  }

  int getChangeBarSize(int index) {
    return barsPanel.changeBars.get(index).size;
  }

  public void setChangeBarSizeBinding(IndexedBinding<Integer> changeBarSizeBinding) {
    this.changeBarSizeBinding.unbind();
    this.changeBarSizeBinding = changeBarSizeBinding;
    this.changeBarSizeBinding.bindLegacy(barsPanel::setChangeBarSize);
  }

  String getChangeBarLabel() {
    return barsPanel.changeBarLabel;
  }

  public void setChangeBarLabelBinding(Binding<String> changeBarLabelBinding) {
    this.changeBarLabelBinding.unbind();
    this.changeBarLabelBinding = changeBarLabelBinding;
    this.changeBarLabelBinding.bindLegacy(barsPanel::setChangeBarLabel);
  }

  int getChangeBarStart() {
    return barsPanel.changeBarStart;
  }

  public void setChangeBarStartBinding(Binding<Integer> changeBarStartBinding) {
    this.changeBarStartBinding.unbind();
    this.changeBarStartBinding = changeBarStartBinding;
    this.changeBarStartBinding.bindLegacy(barsPanel::setChangeBarStart);
  }

  private class SeatBarPanel extends JPanel {
    private List<Bar> seatBars = new ArrayList<>();
    private String seatBarLabel = "";

    private List<Bar> changeBars = new ArrayList<>();
    private String changeBarLabel = "";
    private int changeBarStart = 0;

    public SeatBarPanel() {
      setBackground(Color.WHITE);
    }

    private void setNumSeatBars(int numSeatBars) {
      while (seatBars.size() < numSeatBars) {
        seatBars.add(new Bar());
      }
      while (seatBars.size() > numSeatBars) {
        seatBars.remove(numSeatBars);
      }
      repaint();
    }

    private void setSeatBarColor(int index, Color color) {
      seatBars.get(index).color = color;
      repaint();
    }

    private void setSeatBarSize(int index, int size) {
      seatBars.get(index).size = size;
      repaint();
    }

    private void setSeatBarLabel(String label) {
      seatBarLabel = label;
      repaint();
    }

    private void setNumChangeBars(int numChangeBars) {
      while (changeBars.size() < numChangeBars) {
        changeBars.add(new Bar());
      }
      while (changeBars.size() > numChangeBars) {
        changeBars.remove(numChangeBars);
      }
      repaint();
    }

    private void setChangeBarColor(int index, Color color) {
      changeBars.get(index).color = color;
      repaint();
    }

    private void setChangeBarSize(int index, int size) {
      changeBars.get(index).size = size;
      repaint();
    }

    private void setChangeBarLabel(String label) {
      changeBarLabel = label;
      repaint();
    }

    private void setChangeBarStart(int start) {
      changeBarStart = start;
      repaint();
    }

    public boolean hasSeats() {
      return !seatBars.isEmpty();
    }

    public boolean hasChange() {
      return !changeBars.isEmpty();
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

      g.setColor(seatBars.isEmpty() ? Color.BLACK : seatBars.get(0).color);
      g.drawString(seatBarLabel, 5, seatBaseline);

      int seatBarTop = height / 10;
      int seatBarHeight = height * 4 / 5;

      int leftSoFar = 0;
      for (Bar bar : seatBars) {
        int start = getLeftPosition(leftSoFar);
        int end = getLeftPosition(leftSoFar + bar.size);
        g.setColor(bar.color);
        g.fillRect(start, seatBarTop, end - start, seatBarHeight);
        leftSoFar += bar.size;
      }
      Shape leftClip = new Rectangle(0, seatBarTop, getLeftPosition(leftSoFar), seatBarHeight);

      Shape oldClip = g.getClip();
      Area newClip = new Area();
      newClip.add(new Area(leftClip));
      g.setClip(newClip);
      g.setColor(Color.WHITE);
      g.drawString(seatBarLabel, 5, seatBaseline);

      g.setClip(oldClip);
    }

    private void paintChangeBars(Graphics g) {
      int height = getHeight() / (hasChange() ? 2 : 1);
      int seatBaseline = height * 4 / 5 + height;

      g.setColor(changeBars.isEmpty() ? Color.BLACK : changeBars.get(0).color);
      int leftLeft = getLeftPosition(changeBarStart) + 5;
      if (changeBars.stream().mapToInt(i -> i.size).sum() < 0) {
        leftLeft -= (g.getFontMetrics().stringWidth(changeBarLabel) + 10);
      }
      g.drawString(changeBarLabel, leftLeft, seatBaseline);

      int changeBarHeight = height * 4 / 5;
      int changeBarTop = height / 10 + height;
      int changeBarMid = changeBarTop + changeBarHeight / 2;
      int changeBarBottom = changeBarTop + changeBarHeight;

      IntBinaryOperator sideFunc =
          (zero, point) -> {
            if (point > zero) {
              return Math.max(zero, point - changeBarHeight / 2);
            }
            return Math.min(zero, point + changeBarHeight / 2);
          };

      int leftSoFar = changeBarStart;
      int leftBase = getLeftPosition(changeBarStart);
      Area newClip = new Area();
      for (Bar bar : changeBars) {
        int start = getLeftPosition(leftSoFar);
        int end = getLeftPosition(leftSoFar + bar.size);
        int startSide = sideFunc.applyAsInt(leftBase, start);
        int endSide = sideFunc.applyAsInt(leftBase, end);
        g.setColor(bar.color);
        List<Point> points = new ArrayList<>();
        points.add(new Point(startSide, changeBarTop));
        points.add(new Point(start, changeBarMid));
        points.add(new Point(startSide, changeBarBottom));
        points.add(new Point(endSide, changeBarBottom));
        points.add(new Point(end, changeBarMid));
        points.add(new Point(endSide, changeBarTop));
        var polygon =
            new Polygon(
                points.stream().mapToInt(p -> (int) p.getX()).toArray(),
                points.stream().mapToInt(p -> (int) p.getY()).toArray(),
                points.size());
        g.fillPolygon(polygon);
        newClip.add(new Area(polygon));
        leftSoFar += bar.size;
      }

      Shape oldClip = g.getClip();
      g.setClip(newClip);
      g.setColor(Color.WHITE);
      g.drawString(changeBarLabel, leftLeft, seatBaseline);

      g.setClip(oldClip);
    }

    private int getLeftPosition(int seats) {
      return getSize(seats);
    }

    private int getSize(int seats) {
      return (int) Math.round(1.0 * getWidth() * seats / getNumSquares());
    }
  }

  private class Bar {
    private Color color;
    private int size;
  }

  private class SquaresPanel extends JPanel {
    private int numRows = 1;
    private List<Square> squares = new ArrayList<>();

    public SquaresPanel() {
      setBackground(Color.WHITE);
    }

    private void setNumRows(int numRows) {
      this.numRows = numRows;
      repaint();
    }

    private void setNumSquares(int numSquares) {
      while (squares.size() < numSquares) {
        squares.add(new Square());
      }
      while (squares.size() > numSquares) {
        squares.remove(numSquares);
      }
      repaint();
    }

    private void setSquareBorder(int index, Color color) {
      squares.get(index).borderColor = color;
      repaint();
    }

    private void setSquareFill(int index, Color color) {
      squares.get(index).fillColor = color;
      repaint();
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
      if (squares.isEmpty()) return;

      int width = getWidth();
      int height = getHeight();
      int numCols = (int) Math.ceil(1.0 * squares.size() / numRows);
      int squareSize = Math.min((width - 10) / numCols, (height - 10) / numRows);
      int farLeft = (width - squareSize * numCols) / 2;
      int farTop = (height - squareSize * numRows) / 2;

      int padding;
      if (squares.size() % numRows != 0) {
        padding = (int) Math.ceil(0.5 * (numRows - (squares.size() % numRows)));
      } else {
        padding = 0;
      }
      for (int i = 0; i < squares.size(); i++) {
        Square square = squares.get(i);
        int index = i + padding;
        int row = index % numRows;
        int col = index / numRows;
        int left = farLeft + col * squareSize;
        int top = farTop + row * squareSize;
        if (square.fillColor != null) {
          g.setColor(square.fillColor);
          g.fillRect(left, top, squareSize, squareSize);
        }
        int borderSize = Math.max(2, squareSize / 10);
        ((Graphics2D) g).setStroke(new BasicStroke(borderSize));
        if (square.borderColor != null) {
          g.setColor(square.borderColor);
          g.drawRect(
              left + borderSize / 2,
              top + borderSize / 2,
              squareSize - borderSize,
              squareSize - borderSize);
        }
        ((Graphics2D) g).setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);
        g.drawRect(left, top, squareSize, squareSize);
      }
    }
  }

  private class Square {
    private Color borderColor;
    private Color fillColor;
  }
}
