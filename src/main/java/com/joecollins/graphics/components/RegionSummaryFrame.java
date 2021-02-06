package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.JPanel;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class RegionSummaryFrame extends GraphicsFrame {

  private JPanel centralPanel;

  private Binding<Color> summaryColorBinding = Binding.fixedBinding(Color.BLACK);
  private Binding<Integer> numSectionsBinding = Binding.fixedBinding(0);
  private IndexedBinding<String> sectionHeaderBinding = IndexedBinding.emptyBinding();
  private IndexedBinding<List<Pair<Color, String>>> sectionValueColorBinding =
      IndexedBinding.emptyBinding();

  private Color summaryColor = Color.BLACK;
  private List<SectionPanel> sections = new ArrayList<>();

  public RegionSummaryFrame() {
    centralPanel =
        new JPanel() {
          {
            setBackground(Color.WHITE);
            setLayout(new GridLayout(0, 1));
          }
        };
    add(centralPanel, BorderLayout.CENTER);
  }

  int getNumSections() {
    return sections.size();
  }

  public void setNumSectionsBinding(Binding<Integer> numSectionsBinding) {
    this.numSectionsBinding.unbind();
    this.numSectionsBinding = numSectionsBinding;
    this.numSectionsBinding.bindLegacy(
        numSections -> {
          while (sections.size() < numSections) {
            SectionPanel newPanel = new SectionPanel();
            centralPanel.add(newPanel);
            sections.add(newPanel);
          }
          while (sections.size() > numSections) {
            centralPanel.remove(sections.remove(numSections.intValue()));
          }
        });
  }

  Color getSummaryColor() {
    return summaryColor;
  }

  public void setSummaryColorBinding(Binding<Color> summaryColorBinding) {
    this.summaryColorBinding.unbind();
    this.summaryColorBinding = summaryColorBinding;
    this.summaryColorBinding.bindLegacy(
        color -> {
          summaryColor = color;
          sections.forEach(
              s ->
                  s.setValues(
                      s.values.stream()
                          .map(v -> ImmutablePair.of(color, v.getRight()))
                          .collect(Collectors.toList())));
        });
  }

  String getSectionHeader(int idx) {
    return sections.get(idx).header;
  }

  public void setSectionHeaderBinding(IndexedBinding<String> sectionHeaderBinding) {
    this.sectionHeaderBinding.unbind();
    this.sectionHeaderBinding = sectionHeaderBinding;
    this.sectionHeaderBinding.bind((idx, header) -> sections.get(idx).setHeader(header));
  }

  Color getValueColor(int sectionIdx, int valueIdx) {
    return sections.get(sectionIdx).values.get(valueIdx).getLeft();
  }

  String getValue(int sectionIdx, int valueIdx) {
    return sections.get(sectionIdx).values.get(valueIdx).getRight();
  }

  public void setSectionValueColorBinding(
      IndexedBinding<List<Pair<Color, String>>> sectionValueColorBinding) {
    this.sectionValueColorBinding.unbind();
    this.sectionValueColorBinding = sectionValueColorBinding;
    this.sectionValueColorBinding.bind((idx, values) -> sections.get(idx).setValues(values));
  }

  public void setSectionValueBinding(IndexedBinding<List<String>> sectionValueBinding) {
    setSectionValueColorBinding(
        sectionValueBinding.map(
            v ->
                v.stream()
                    .map(s -> ImmutablePair.of(summaryColor, s))
                    .collect(Collectors.toList())));
  }

  private class SectionPanel extends JPanel {
    private String header = "";
    private List<Pair<Color, String>> values = List.of();

    public SectionPanel() {
      setBackground(Color.WHITE);
    }

    void setHeader(String header) {
      this.header = header;
      repaint();
    }

    public void setValues(List<Pair<Color, String>> values) {
      this.values = values;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      ((Graphics2D) g)
          .setRenderingHint(
              RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      g.setColor(summaryColor);
      var headerFont = StandardFont.readBoldFont(Math.min(30, getHeight() / 3 - 5));
      g.setFont(headerFont);
      var headerWidth = g.getFontMetrics(headerFont).stringWidth(header);
      g.drawString(header, (getWidth() - headerWidth) / 2, getHeight() / 3 - 5);

      Font valueFont;
      var startFontSize = Math.min(61, getHeight() * 2 / 3 - 9);
      for (int i = 0; i < values.size(); i++) {
        g.setColor(values.get(i).getLeft());
        String value = values.get(i).getRight();
        int valueWidth;
        int fontSize = startFontSize;
        do {
          fontSize--;
          valueFont = StandardFont.readBoldFont(fontSize);
          valueWidth = g.getFontMetrics(valueFont).stringWidth(value);
        } while (valueWidth > getWidth() / values.size() - 20);
        g.setFont(valueFont);
        g.drawString(
            value,
            (getWidth() / values.size() - valueWidth) / 2 + (getWidth() / values.size() * i),
            getHeight() / 3 + (startFontSize + fontSize) / 2);
      }
    }
  }
}
