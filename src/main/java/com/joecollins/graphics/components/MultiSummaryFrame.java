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
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import org.apache.commons.lang3.tuple.Pair;

public class MultiSummaryFrame extends GraphicsFrame {

  private final JPanel centralPanel;

  private Binding<Integer> numRowsBinding = () -> 0;
  private IndexedBinding<String> headerBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<List<Pair<Color, String>>> valuesBinding = IndexedBinding.emptyBinding();

  private List<EntryPanel> entries = new ArrayList<>();

  public MultiSummaryFrame() {
    centralPanel =
        new JPanel() {
          {
            setBackground(Color.WHITE);
            setLayout(new FrameLayout());
          }
        };
    add(centralPanel, BorderLayout.CENTER);
  }

  public void setNumRowsBinding(Binding<Integer> numRowsBinding) {
    this.numRowsBinding.unbind();
    this.numRowsBinding = numRowsBinding;
    this.numRowsBinding.bind(
        size -> {
          while (entries.size() < size) {
            EntryPanel entryPanel = new EntryPanel();
            centralPanel.add(entryPanel);
            entries.add(entryPanel);
          }
          while (entries.size() > size) {
            centralPanel.remove(entries.remove(size.intValue()));
          }
          repaint();
        });
  }

  int getNumRows() {
    return entries.size();
  }

  public void setRowHeaderBinding(IndexedBinding<String> headerBinding) {
    this.headerBinding.unbind();
    this.headerBinding = headerBinding;
    this.headerBinding.bind(
        (idx, header) -> {
          entries.get(idx).headerLabel.setText(header);
          repaint();
        });
  }

  String getRowHeader(int index) {
    return entries.get(index).headerLabel.getText();
  }

  public void setValuesBinding(IndexedBinding<List<Pair<Color, String>>> valuesBinding) {
    this.valuesBinding.unbind();
    this.valuesBinding = valuesBinding;
    this.valuesBinding.bind(
        (idx, values) -> {
          EntryPanel entry = entries.get(idx);
          while (entry.panels.size() < values.size()) {
            JPanel panel = new JPanel();
            JLabel label = new JLabel();
            label.setFont(entry.headerLabel.getFont());
            label.setBorder(entry.headerLabel.getBorder());
            label.setHorizontalAlignment(JLabel.CENTER);
            panel.setLayout(new GridLayout(1, 1));
            panel.add(label);
            entry.panels.add(panel);
            entry.labels.add(label);
            entry.add(panel);
          }
          while (entry.panels.size() > values.size()) {
            entry.remove(entry.panels.remove(values.size()));
            entry.labels.remove(values.size());
          }
          for (int i = 0; i < values.size(); i++) {
            entry.panels.get(i).setBackground(values.get(i).getLeft());
            entry
                .labels
                .get(i)
                .setForeground(
                    values.get(i).getLeft().equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
            entry.labels.get(i).setText(values.get(i).getRight());
          }
          entries.forEach(
              e -> {
                e.invalidate();
                e.revalidate();
              });
        });
  }

  int getNumValues(int index) {
    return entries.get(index).panels.size();
  }

  Color getColor(int row, int col) {
    return entries.get(row).panels.get(col).getBackground();
  }

  String getValue(int row, int col) {
    return entries.get(row).labels.get(col).getText();
  }

  private class FrameLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return new Dimension(1024, 24 * entries.size());
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return new Dimension(50, 10 * entries.size());
    }

    @Override
    public void layoutContainer(Container parent) {
      int entryHeight = Math.min(24, parent.getHeight() / Math.max(1, entries.size()));
      for (int i = 0; i < entries.size(); i++) {
        entries.get(i).setLocation(0, entryHeight * i);
        entries.get(i).setSize(parent.getWidth(), entryHeight);
      }
    }
  }

  private class EntryPanel extends JPanel {
    private FontSizeAdjustingLabel headerLabel;
    private List<JPanel> panels = new ArrayList<>();
    private List<JLabel> labels = new ArrayList<>();

    public EntryPanel() {
      setBackground(Color.WHITE);
      setLayout(new EntryLayout());
      setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
      headerLabel = new FontSizeAdjustingLabel();
      headerLabel.setFont(StandardFont.readBoldFont(16));
      headerLabel.setBorder(new EmptyBorder(4, 0, -4, 0));
      add(headerLabel);

      addComponentListener(
          new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
              int height = Math.min(24, getHeight());
              Font font = StandardFont.readBoldFont(height * 2 / 3);
              headerLabel.setFont(font);
              labels.forEach(label -> label.setFont(font));
              EmptyBorder border = new EmptyBorder(height / 6, 0, -height / 6, 0);
              headerLabel.setBorder(border);
              labels.forEach(label -> label.setBorder(border));
            }
          });
    }

    private class EntryLayout implements LayoutManager {

      @Override
      public void addLayoutComponent(String name, Component comp) {}

      @Override
      public void removeLayoutComponent(Component comp) {}

      @Override
      public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(1024, 24);
      }

      @Override
      public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(50, 10);
      }

      @Override
      public void layoutContainer(Container parent) {
        int width = parent.getWidth();
        int height = parent.getHeight();
        int numCells = entries.stream().mapToInt(e -> e.panels.size()).reduce(0, Integer::max);
        headerLabel.setLocation(0, 1);
        headerLabel.setSize(width * 3 / (3 + numCells), height - 3);
        for (int i = 0; i < panels.size(); i++) {
          JPanel panel = panels.get(i);
          panel.setLocation(width * (3 + i) / (3 + numCells) + 1, 1);
          panel.setSize(width / (3 + numCells) - 2, height - 3);
        }
      }
    }
  }
}
