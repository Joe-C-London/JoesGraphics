package com.joecollins.graphics.components.lowerthird;

import com.joecollins.graphics.components.FontSizeAdjustingLabel;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class HeadlinePanel extends JPanel {

  private JLabel headlineLabel;

  private JLabel subheadLabel;

  public HeadlinePanel() {
    setLayout(new HeadlinePanelLayout());

    headlineLabel = new FontSizeAdjustingLabel();
    headlineLabel.setText("");
    headlineLabel.setHorizontalAlignment(JLabel.LEFT);
    headlineLabel.setVerticalAlignment(JLabel.CENTER);
    headlineLabel.setFont(StandardFont.readNormalFont(24));
    headlineLabel.setForeground(Color.BLACK);
    headlineLabel.setBorder(new EmptyBorder(6, 5, -6, 5));
    add(headlineLabel);

    subheadLabel = new FontSizeAdjustingLabel();
    subheadLabel.setText("");
    subheadLabel.setVisible(false);
    subheadLabel.setHorizontalAlignment(JLabel.LEFT);
    subheadLabel.setVerticalAlignment(JLabel.CENTER);
    subheadLabel.setFont(StandardFont.readNormalFont(16));
    subheadLabel.setForeground(Color.BLACK);
    subheadLabel.setBorder(new EmptyBorder(2, 5, -2, 5));
    add(subheadLabel);
  }

  public String getHeadline() {
    return headlineLabel.isVisible() ? headlineLabel.getText() : null;
  }

  public void setHeadline(String headline) {
    headlineLabel.setVisible(headline != null);
    if (headline != null) {
      headlineLabel.setText(headline);
    }
  }

  public String getSubhead() {
    return subheadLabel.isVisible() ? subheadLabel.getText() : null;
  }

  public void setSubhead(String subhead) {
    subheadLabel.setVisible(subhead != null);
    if (subhead != null) {
      subheadLabel.setText(subhead);
    } else {
    }
  }

  private class HeadlinePanelLayout implements LayoutManager {

    @Override
    public void addLayoutComponent(String name, Component comp) {}

    @Override
    public void removeLayoutComponent(Component comp) {}

    @Override
    public Dimension preferredLayoutSize(Container parent) {
      return new Dimension(1024, 50);
    }

    @Override
    public Dimension minimumLayoutSize(Container parent) {
      return new Dimension(50, 50);
    }

    @Override
    public void layoutContainer(Container parent) {
      int width = parent.getWidth();
      int height = parent.getHeight();
      int headlineHeight = height * 11 / 20;
      int subheadHeight = height * 9 / 20;
      headlineLabel.setLocation(0, 0);
      headlineLabel.setSize(width, subheadLabel.isVisible() ? headlineHeight : height);
      subheadLabel.setLocation(0, headlineHeight);
      subheadLabel.setSize(width, subheadHeight);
    }
  }
}
