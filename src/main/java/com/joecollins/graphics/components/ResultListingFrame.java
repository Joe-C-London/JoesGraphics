package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;

public class ResultListingFrame extends GraphicsFrame {

  private final JPanel centralPanel = new JPanel();
  private final Layout layout = new Layout();
  private final List<Item> items = new ArrayList<>();

  private Binding<Integer> numRowsBinding = () -> 0;
  private Binding<Boolean> reversedBinding = () -> false;
  private Binding<Integer> numItemsBinding = () -> 0;
  private IndexedBinding<String> textBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> foregroundBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> backgroundBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> borderBinding = IndexedBinding.emptyBinding();

  public ResultListingFrame() {
    centralPanel.setBackground(Color.WHITE);
    centralPanel.setLayout(layout);
    add(centralPanel, BorderLayout.CENTER);
  }

  int getNumRows() {
    return layout.numRows;
  }

  public void setNumRowsBinding(Binding<Integer> numRowsBinding) {
    this.numRowsBinding.unbind();
    this.numRowsBinding = numRowsBinding;
    this.numRowsBinding.bind(layout::setNumRows);
  }

  boolean isReversed() {
    return layout.reversed;
  }

  public void setReversedBinding(Binding<Boolean> reversedBinding) {
    this.reversedBinding.unbind();
    this.reversedBinding = reversedBinding;
    this.reversedBinding.bind(layout::setReversed);
  }

  int getNumItems() {
    return items.size();
  }

  public void setNumItemsBinding(Binding<Integer> numItemsBinding) {
    this.numItemsBinding.unbind();
    this.numItemsBinding = numItemsBinding;
    this.numItemsBinding.bind(
        numItems -> {
          while (numItems > items.size()) {
            Item item = new Item();
            items.add(item);
            centralPanel.add(item);
          }
          while (numItems < items.size()) {
            centralPanel.remove(items.remove(numItems.intValue()));
          }
        });
  }

  String getText(int index) {
    return items.get(index).text;
  }

  public void setTextBinding(IndexedBinding<String> textBinding) {
    this.textBinding.unbind();
    this.textBinding = textBinding;
    this.textBinding.bind((idx, text) -> items.get(idx).setText(text));
  }

  Color getForeground(int index) {
    return items.get(index).foreground;
  }

  public void setForegroundBinding(IndexedBinding<Color> foregroundBinding) {
    this.foregroundBinding.unbind();
    this.foregroundBinding = foregroundBinding;
    this.foregroundBinding.bind((idx, color) -> items.get(idx).setForeground(color));
  }

  Color getBackground(int index) {
    return items.get(index).background;
  }

  public void setBackgroundBinding(IndexedBinding<Color> backgroundBinding) {
    this.backgroundBinding.unbind();
    this.backgroundBinding = backgroundBinding;
    this.backgroundBinding.bind((idx, color) -> items.get(idx).setBackground(color));
  }

  Color getBorder(int index) {
    return items.get(index).border;
  }

  public void setBorderBinding(IndexedBinding<Color> borderBinding) {
    this.borderBinding.unbind();
    this.borderBinding = borderBinding;
    this.borderBinding.bind((idx, color) -> items.get(idx).setBorder(color));
  }

  class Item extends JPanel {
    private String text = "";
    private Color foreground = Color.BLACK;
    private Color background = Color.WHITE;
    private Color border = Color.WHITE;

    public void setText(String text) {
      this.text = text;
      repaint();
    }

    public void setForeground(Color foreground) {
      this.foreground = foreground;
      super.setForeground(foreground);
      repaint();
    }

    public void setBackground(Color background) {
      this.background = background;
      super.setBackground(background);
      repaint();
    }

    public void setBorder(Color border) {
      this.border = border;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      ((Graphics2D) g)
          .setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setColor(border);
      g.fillRect(0, 0, getWidth(), getHeight());
      g.setColor(background);
      g.fillRect(3, 3, getWidth() - 6, getHeight() - 6);
      g.setColor(foreground);
      int fontSize = Math.min(24, getHeight() - 8);
      g.setFont(StandardFont.readBoldFont(fontSize));
      Shape oldClip = g.getClip();
      g.setClip(3, 3, getWidth() - 6, getHeight() - 6);
      g.drawString(text, 5, (getHeight() - 4 + fontSize) / 2);
      g.setClip(oldClip);
    }
  }

  class Layout implements LayoutManager {
    int numRows;
    boolean reversed;

    void setNumRows(int numRows) {
      this.numRows = numRows;
      layoutContainer(ResultListingFrame.this.centralPanel);
    }

    void setReversed(boolean reversed) {
      this.reversed = reversed;
      layoutContainer(ResultListingFrame.this.centralPanel);
    }

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
      int totalHeight = parent.getHeight();
      int totalWidth = parent.getWidth();
      int numRows = Math.max(1, this.numRows);
      int numCols = Math.max(1, (int) Math.ceil(1.0 * items.size() / numRows));
      int itemHeight = totalHeight / numRows;
      int itemWidth = totalWidth / numCols;
      for (int i = 0; i < items.size(); i++) {
        Item item = items.get(i);
        int row = i % numRows;
        int col = i / numRows;
        if (reversed) {
          col = numCols - col - 1;
        }
        item.setSize(itemWidth - 4, itemHeight - 4);
        item.setLocation(col * itemWidth + 2, row * itemHeight + 2);
      }
    }
  }
}
