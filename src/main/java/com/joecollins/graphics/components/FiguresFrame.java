package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
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

public class FiguresFrame extends GraphicsFrame {

  private Binding<Integer> numEntriesBinding = () -> 0;
  private IndexedBinding<Color> colorBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<String> nameBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<String> descriptionBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<String> resultBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<Color> resultColorBinding = IndexedBinding.emptyBinding();

  private JPanel centralPanel;
  private List<Entry> entries = new ArrayList<>();

  public FiguresFrame() {
    centralPanel =
        new JPanel() {
          {
            setBackground(Color.WHITE);
            setLayout(new FrameLayout());
          }
        };
    add(centralPanel, BorderLayout.CENTER);
  }

  public void setNumEntriesBinding(Binding<Integer> numEntriesBinding) {
    this.numEntriesBinding.unbind();
    this.numEntriesBinding = numEntriesBinding;
    this.numEntriesBinding.bindLegacy(
        size -> {
          while (entries.size() < size) {
            Entry entry = new Entry();
            centralPanel.add(entry);
            entries.add(entry);
          }
          while (entries.size() > size) {
            centralPanel.remove(entries.remove(size.intValue()));
          }
        });
  }

  int getNumEntries() {
    return entries.size();
  }

  public void setColorBinding(IndexedBinding<Color> colorBinding) {
    this.colorBinding.unbind();
    this.colorBinding = colorBinding;
    this.colorBinding.bind((idx, color) -> entries.get(idx).setForeground(color));
  }

  Color getColor(int index) {
    return entries.get(index).getForeground();
  }

  public void setNameBinding(IndexedBinding<String> nameBinding) {
    this.nameBinding.unbind();
    this.nameBinding = nameBinding;
    this.nameBinding.bind((idx, name) -> entries.get(idx).nameLabel.setText(name));
  }

  String getName(int index) {
    return entries.get(index).nameLabel.getText();
  }

  public void setDescriptionBinding(IndexedBinding<String> descriptionBinding) {
    this.descriptionBinding.unbind();
    this.descriptionBinding = descriptionBinding;
    this.descriptionBinding.bind((idx, desc) -> entries.get(idx).descriptionLabel.setText(desc));
  }

  String getDescription(int index) {
    return entries.get(index).descriptionLabel.getText();
  }

  public void setResultBinding(IndexedBinding<String> resultBinding) {
    this.resultBinding.unbind();
    this.resultBinding = resultBinding;
    this.resultBinding.bind((idx, result) -> entries.get(idx).resultLabel.setText(result));
  }

  String getResult(int index) {
    return entries.get(index).resultLabel.getText();
  }

  public void setResultColorBinding(IndexedBinding<Color> resultColorBinding) {
    this.resultColorBinding.unbind();
    this.resultColorBinding = resultColorBinding;
    this.resultColorBinding.bind((idx, color) -> entries.get(idx).resultPanel.setBackground(color));
  }

  Color getResultColor(int index) {
    return entries.get(index).resultPanel.getBackground();
  }

  private class Entry extends JPanel {

    private final FontSizeAdjustingLabel nameLabel;
    private final FontSizeAdjustingLabel descriptionLabel;
    private final JPanel resultPanel;
    private final FontSizeAdjustingLabel resultLabel;

    public Entry() {
      setForeground(Color.LIGHT_GRAY);
      setBackground(Color.WHITE);
      setLayout(new EntryLayout());

      nameLabel = new FontSizeAdjustingLabel();
      nameLabel.setFont(StandardFont.readBoldFont(15));
      nameLabel.setBorder(new EmptyBorder(2, 0, -2, 0));
      add(nameLabel);

      descriptionLabel = new FontSizeAdjustingLabel();
      descriptionLabel.setFont(StandardFont.readNormalFont(10));
      descriptionLabel.setBorder(new EmptyBorder(1, 0, -1, 0));
      add(descriptionLabel);

      resultPanel = new JPanel();
      resultPanel.setLayout(new GridLayout(1, 1));
      resultPanel.setBackground(Color.LIGHT_GRAY);
      add(resultPanel);

      resultLabel = new FontSizeAdjustingLabel();
      resultLabel.setFont(StandardFont.readBoldFont(20));
      resultLabel.setForeground(Color.WHITE);
      resultLabel.setBorder(new EmptyBorder(3, 0, -3, 0));
      resultLabel.setHorizontalAlignment(JLabel.CENTER);
      resultPanel.add(resultLabel);
    }

    @Override
    public void setForeground(Color fg) {
      super.setForeground(fg);
      setBorder(new MatteBorder(1, 1, 1, 1, fg));
      if (nameLabel != null) nameLabel.setForeground(fg);
      if (descriptionLabel != null) descriptionLabel.setForeground(fg);
    }

    private class EntryLayout implements LayoutManager {

      @Override
      public void addLayoutComponent(String name, Component comp) {}

      @Override
      public void removeLayoutComponent(Component comp) {}

      @Override
      public Dimension preferredLayoutSize(Container parent) {
        return new Dimension(1024, 30);
      }

      @Override
      public Dimension minimumLayoutSize(Container parent) {
        return new Dimension(50, 15);
      }

      @Override
      public void layoutContainer(Container parent) {
        int width = parent.getWidth();
        int height = parent.getHeight();
        nameLabel.setLocation(2, 0);
        nameLabel.setSize(width * 2 / 3 - 4, height * 3 / 5);
        nameLabel.setFont(StandardFont.readBoldFont(height / 2));
        descriptionLabel.setLocation(2, height * 3 / 5);
        descriptionLabel.setSize(width * 2 / 3 - 4, height * 2 / 5);
        descriptionLabel.setFont(StandardFont.readNormalFont(height / 3));
        resultPanel.setLocation(width * 2 / 3, 1);
        resultPanel.setSize(width / 3, height - 2);
        resultLabel.setFont(StandardFont.readBoldFont(height * 2 / 3));
      }
    }
  }

  private class FrameLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return new Dimension(1024, 30 * entries.size());
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return new Dimension(50, 15 * entries.size());
    }

    @Override
    public void layoutContainer(Container parent) {
      int entryHeight = Math.min(30, parent.getHeight() / Math.max(1, entries.size()));
      for (int i = 0; i < entries.size(); i++) {
        entries.get(i).setLocation(0, entryHeight * i);
        entries.get(i).setSize(parent.getWidth(), entryHeight);
      }
    }
  }
}
