package com.joecollins.graphics.components.lowerthird;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class SummaryWithoutLabels extends JPanel {

  private Binding<String> headlineBinding = () -> "RESULT";
  private Binding<Integer> numEntriesBinding = () -> 1;
  private IndexedBinding<Entry> entriesBinding =
      IndexedBinding.singletonBinding(new Entry(Color.WHITE, "WAITING..."));

  private HeadlinePanel headlinePanel = new HeadlinePanel();
  private List<EntryPanel> entryPanels = new ArrayList<>();

  public SummaryWithoutLabels() {
    setBackground(Color.WHITE);
    setLayout(new SummaryLayout());
    setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
    setNumEntriesBinding(numEntriesBinding);
    setEntriesBinding(entriesBinding);
    add(headlinePanel);
  }

  public static class Entry {
    private final Color color;
    private final String value;

    public Entry(Color color, String value) {
      this.color = color;
      this.value = value;
    }
  }

  public void setNumEntriesBinding(Binding<Integer> numEntriesBinding) {
    this.numEntriesBinding.unbind();
    this.numEntriesBinding = numEntriesBinding;
    this.numEntriesBinding.bindLegacy(
        size -> {
          while (entryPanels.size() < size) {
            EntryPanel newPanel = new EntryPanel();
            add(newPanel);
            entryPanels.add(newPanel);
          }
          while (entryPanels.size() > size) {
            remove(entryPanels.remove(size.intValue()));
          }
        });
  }

  String getHeadline() {
    return this.headlinePanel.topLabel.getText();
  }

  public void setHeadlineBinding(Binding<String> headlineBinding) {
    this.headlineBinding.unbind();
    this.headlineBinding = headlineBinding;
    this.headlineBinding.bindLegacy(this.headlinePanel.topLabel::setText);
  }

  int getNumEntries() {
    return this.entryPanels.size();
  }

  public void setEntriesBinding(IndexedBinding<Entry> entriesBinding) {
    this.entriesBinding.unbind();
    this.entriesBinding = entriesBinding;
    this.entriesBinding.bind(
        (idx, entry) -> {
          entryPanels.get(idx).bottomPanel.setBackground(entry.color);
          entryPanels
              .get(idx)
              .bottomLabel
              .setForeground(entry.color.equals(Color.WHITE) ? Color.BLACK : Color.WHITE);
          entryPanels.get(idx).bottomLabel.setText(entry.value);
        });
  }

  Color getEntryColor(int index) {
    return entryPanels.get(index).bottomPanel.getBackground();
  }

  //  String getEntryLabel(int index) {
  //    return entryPanels.get(index).topLabel.getText();
  //  }

  String getEntryValue(int index) {
    return entryPanels.get(index).bottomLabel.getText();
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

  private class EntryPanel extends JPanel {
    private final JPanel bottomPanel =
        new JPanel() {
          {
            setBackground(Color.WHITE);
            setLayout(new GridLayout(1, 1));
          }
        };
    private final JLabel bottomLabel =
        new JLabel() {
          {
            setFont(StandardFont.readBoldFont(24));
            setHorizontalAlignment(JLabel.CENTER);
            setForeground(Color.BLACK);
            setBorder(new EmptyBorder(4, 0, -4, 0));
          }
        };

    public EntryPanel() {
      add(bottomPanel);
      bottomPanel.add(bottomLabel);
      setLayout(new GridLayout(1, 1));
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
      for (int i = 0; i < entryPanels.size(); i++) {
        int left = width * i / entryPanels.size();
        int right = width * (i + 1) / entryPanels.size();
        entryPanels.get(i).setLocation(left + 1, mid + 1);
        entryPanels.get(i).setSize(right - left, height - mid);
      }
    }
  }
}
