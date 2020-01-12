package com.joecollins.graphics.components.lowerthird;

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

  private JLabel headlineLabel =
      new JLabel("") {
        {
          setHorizontalAlignment(JLabel.LEFT);
          setVerticalAlignment(JLabel.CENTER);
          setFont(StandardFont.readNormalFont(24));
          setForeground(Color.BLACK);
          setBorder(new EmptyBorder(10, 5, 0, 5));
        }
      };
  private JLabel subheadLabel =
      new JLabel("") {
        {
          setVisible(false);
          setHorizontalAlignment(JLabel.LEFT);
          setVerticalAlignment(JLabel.CENTER);
          setFont(StandardFont.readNormalFont(16));
          setForeground(Color.BLACK);
          setBorder(new EmptyBorder(0, 5, 0, 5));
        }
      };

  public HeadlinePanel() {
    setLayout(new HeadlinePanelLayout());
    add(headlineLabel);
    add(subheadLabel);
  }

  String getHeadline() {
    return headlineLabel.isVisible() ? headlineLabel.getText() : null;
  }

  void setHeadline(String headline) {
    headlineLabel.setVisible(headline != null);
    if (headline != null) {
      headlineLabel.setText(headline);
    }
  }

  String getSubhead() {
    return subheadLabel.isVisible() ? subheadLabel.getText() : null;
  }

  void setSubhead(String subhead) {
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
      int headlineHeight = height * 3 / 5;
      int subheadHeight = height * 2 / 5;
      headlineLabel.setLocation(0, 0);
      headlineLabel.setSize(width, subheadLabel.isVisible() ? headlineHeight : height);
      subheadLabel.setLocation(0, headlineHeight);
      subheadLabel.setSize(width, subheadHeight);
    }
  }
}
