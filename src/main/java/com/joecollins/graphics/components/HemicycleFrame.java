package com.joecollins.graphics.components;

import static java.lang.Math.PI;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class HemicycleFrame extends GraphicsFrame {

  private Binding<Integer> numRowsBinding = () -> 0;
  private IndexedBinding<Integer> rowCountBinding = IndexedBinding.emptyBinding();

  private Binding<Integer> numDotsBinding = () -> 0;
  private IndexedBinding<Color> dotColorBinding = IndexedBinding.emptyBinding();

  private Panel panel = new Panel();

  public HemicycleFrame() {
    add(panel, BorderLayout.CENTER);
  }

  int getNumRows() {
    return panel.rows.size();
  }

  public void setNumRowsBinding(Binding<Integer> numRowsBinding) {
    this.numRowsBinding.unbind();
    this.numRowsBinding = numRowsBinding;
    this.numRowsBinding.bind(
        numRows -> {
          while (numRows > panel.rows.size()) {
            panel.rows.add(0);
          }
          while (numRows < panel.rows.size()) {
            panel.rows.remove(numRows.intValue());
          }
          panel.repaint();
        });
  }

  int getRowCount(int rowNum) {
    return panel.rows.get(rowNum);
  }

  public void setRowCountsBinding(IndexedBinding<Integer> rowCountBinding) {
    this.rowCountBinding.unbind();
    this.rowCountBinding = rowCountBinding;
    this.rowCountBinding.bind(
        (idx, count) -> {
          panel.rows.set(idx, count);
          panel.repaint();
        });
  }

  int getNumDots() {
    return panel.dots.size();
  }

  public void setNumDotsBinding(Binding<Integer> numDotsBinding) {
    this.numDotsBinding.unbind();
    this.numDotsBinding = numDotsBinding;
    this.numDotsBinding.bind(
        numDots -> {
          while (numDots > panel.dots.size()) {
            panel.dots.add(null);
          }
          while (numDots < panel.dots.size()) {
            panel.dots.remove(numDots.intValue());
          }
          panel.repaint();
        });
  }

  Color getDotColor(int dotNum) {
    return panel.dots.get(dotNum);
  }

  public void setDotColorBinding(IndexedBinding<Color> dotColorBinding) {
    this.dotColorBinding.unbind();
    this.dotColorBinding = dotColorBinding;
    this.dotColorBinding.bind(
        (idx, color) -> {
          panel.dots.set(idx, color);
          panel.repaint();
        });
  }

  class Panel extends JPanel {
    private List<Integer> rows = new ArrayList<>();
    private List<Color> dots = new ArrayList<>();

    public Panel() {
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
        for (int dot = 0; dot < dotsInRow; dot++) {
          Color color = dots.get(firstDot + dot);
          ((Graphics2D) g)
              .setTransform(
                  createRotationTransform(1.0 * dot / (dotsInRow - 1), originalTransform, arcY));
          int x = getWidth() / 2;
          int y = getHeight() - (int) Math.round(r + (0.5 - rowsFromOuter) * d);
          int rad = (int) Math.round(d / 2) * 19 / 20;
          g.setColor(color);
          g.fillOval(x - rad, y - rad, 2 * rad, 2 * rad);
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
