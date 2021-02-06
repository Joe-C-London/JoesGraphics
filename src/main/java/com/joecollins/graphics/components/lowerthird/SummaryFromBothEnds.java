package com.joecollins.graphics.components.lowerthird;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Shape;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class SummaryFromBothEnds extends JPanel {

  private Binding<String> headlineBinding = () -> "RESULT";
  private Binding<Entry> leftBinding = () -> null;
  private Binding<Entry> rightBinding = () -> null;
  private Binding<Entry> middleBinding = () -> null;
  private Binding<Integer> totalBinding = () -> 1;

  private HeadlinePanel headlinePanel = new HeadlinePanel();
  private EntryPanel entryPanel = new EntryPanel();

  public SummaryFromBothEnds() {
    setBackground(Color.WHITE);
    setLayout(new SummaryLayout());
    setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
    add(headlinePanel);
    add(entryPanel);
  }

  String getHeadline() {
    return this.headlinePanel.topLabel.getText();
  }

  public void setHeadlineBinding(Binding<String> headlineBinding) {
    this.headlineBinding.unbind();
    this.headlineBinding = headlineBinding;
    this.headlineBinding.bindLegacy(this.headlinePanel.topLabel::setText);
  }

  public int getTotal() {
    return entryPanel.total;
  }

  public void setTotalBinding(Binding<Integer> totalBinding) {
    this.totalBinding.unbind();
    this.totalBinding = totalBinding;
    this.totalBinding.bindLegacy(entryPanel::setTotal);
  }

  public Entry getLeft() {
    return entryPanel.left;
  }

  public void setLeftBinding(Binding<Entry> leftBinding) {
    this.leftBinding.unbind();
    this.leftBinding = leftBinding;
    this.leftBinding.bindLegacy(entryPanel::setLeft);
  }

  public Entry getRight() {
    return entryPanel.right;
  }

  public void setRightBinding(Binding<Entry> rightBinding) {
    this.rightBinding.unbind();
    this.rightBinding = rightBinding;
    this.rightBinding.bindLegacy(entryPanel::setRight);
  }

  public Entry getMiddle() {
    return entryPanel.middle;
  }

  public void setMiddleBinding(Binding<Entry> middleBinding) {
    this.middleBinding.unbind();
    this.middleBinding = middleBinding;
    this.middleBinding.bindLegacy(entryPanel::setMiddle);
  }

  private class HeadlinePanel extends JPanel {
    private final JPanel topPanel =
        new JPanel() {
          {
            setBackground(Color.BLACK);
            setLayout(new GridLayout(1, 1));
          }
        };
    private final JLabel topLabel =
        new JLabel() {
          {
            setFont(StandardFont.readNormalFont(16));
            setHorizontalAlignment(JLabel.CENTER);
            setForeground(Color.WHITE);
            setBorder(new EmptyBorder(3, 0, -3, 0));
          }
        };

    public HeadlinePanel() {
      add(topPanel);
      topPanel.add(topLabel);
      setLayout(new GridLayout(1, 1));
    }
  }

  public static class Entry {
    private final Color color;
    private final String label;
    private final int value;

    public Entry(Color color, String label, int value) {
      this.color = color;
      this.label = label;
      this.value = value;
    }

    public Color getColor() {
      return color;
    }

    public String getLabel() {
      return label;
    }

    public int getValue() {
      return value;
    }
  }

  private class EntryPanel extends JPanel {
    int total;
    Entry left, right, middle;

    public EntryPanel() {
      setBackground(Color.WHITE);
    }

    void setTotal(int total) {
      this.total = total;
      repaint();
    }

    void setLeft(Entry left) {
      this.left = left;
      repaint();
    }

    void setRight(Entry right) {
      this.right = right;
      repaint();
    }

    void setMiddle(Entry middle) {
      this.middle = middle;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      ((Graphics2D) g)
          .setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      Font labelFont = StandardFont.readNormalFont(12);
      Font valueFont = StandardFont.readBoldFont(20);

      int leftWidth = (int) Math.round(1.0 * getWidth() * (left == null ? 0 : left.value) / total);
      int rightWidth =
          (int) Math.round(1.0 * getWidth() * (right == null ? 0 : right.value) / total);
      int midWidth =
          (int) Math.round(1.0 * getWidth() * (middle == null ? 0 : middle.value) / total);
      int midCentre = getWidth() / 2;

      if (midCentre - midWidth / 2 < leftWidth) {
        midCentre = leftWidth + midWidth / 2;
      }
      if (midCentre + midWidth / 2 > getWidth() - rightWidth) {
        midCentre = getWidth() - rightWidth - midWidth / 2;
      }

      if (left != null) {
        g.setColor(left.color);
        g.fillRect(0, 0, leftWidth, getHeight());
      }

      if (right != null) {
        g.setColor(right.color);
        g.fillRect(getWidth() - rightWidth, 0, rightWidth, getHeight());
      }

      if (middle != null) {
        g.setColor(middle.color);
        g.fillRect(midCentre - midWidth / 2, 0, midWidth, getHeight());
      }

      if (left != null) {
        g.setColor(left.color);
        drawLeftLabels(g, labelFont, valueFont);
        Shape oldClip = g.getClip();
        g.setClip(0, 0, leftWidth, getHeight());
        g.setColor(Color.WHITE);
        drawLeftLabels(g, labelFont, valueFont);
        g.setClip(oldClip);
      }

      if (right != null) {
        g.setColor(right.color);
        drawRightLabels(g, labelFont, valueFont);
        Shape oldClip = g.getClip();
        g.setClip(getWidth() - rightWidth, 0, rightWidth, getHeight());
        g.setColor(Color.WHITE);
        drawRightLabels(g, labelFont, valueFont);
        g.setClip(oldClip);
      }

      if (middle != null) {
        g.setColor(middle.color);
        drawMidLabels(g, labelFont, valueFont, midCentre);
        Shape oldClip = g.getClip();
        g.setClip(midCentre - midWidth / 2, 0, midWidth, getHeight());
        g.setColor(Color.WHITE);
        drawMidLabels(g, labelFont, valueFont, midCentre);
        g.setClip(oldClip);
      }

      g.setColor(Color.BLACK);
      g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
    }

    private void drawMidLabels(Graphics g, Font labelFont, Font valueFont, int midCentre) {
      g.setFont(labelFont);
      g.drawString(middle.label, midCentre - g.getFontMetrics().stringWidth(middle.label) / 2, 10);
      g.setFont(valueFont);
      String rightValue = String.valueOf(middle.value);
      g.drawString(rightValue, midCentre - g.getFontMetrics().stringWidth(rightValue) / 2, 28);
    }

    private void drawRightLabels(Graphics g, Font labelFont, Font valueFont) {
      g.setFont(labelFont);
      g.drawString(right.label, getWidth() - g.getFontMetrics().stringWidth(right.label), 10);
      g.setFont(valueFont);
      String rightValue = String.valueOf(right.value);
      g.drawString(rightValue, getWidth() - g.getFontMetrics().stringWidth(rightValue), 28);
    }

    private void drawLeftLabels(Graphics g, Font labelFont, Font valueFont) {
      g.setFont(labelFont);
      g.drawString(left.label, 0, 10);
      g.setFont(valueFont);
      g.drawString(String.valueOf(left.value), 0, 28);
    }
  }

  private class SummaryLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return new Dimension(512, 50);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return new Dimension(50, 50);
    }

    @Override
    public void layoutContainer(Container parent) {
      int width = parent.getWidth() - 2;
      int height = parent.getHeight() - 2;
      int mid = height * 2 / 5;
      headlinePanel.setLocation(1, 1);
      headlinePanel.setSize(width, mid);
      entryPanel.setLocation(1, mid + 1);
      entryPanel.setSize(width, height - mid);
    }
  }
}
