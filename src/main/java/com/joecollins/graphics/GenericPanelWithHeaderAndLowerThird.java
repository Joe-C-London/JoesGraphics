package com.joecollins.graphics;

import com.joecollins.graphics.components.lowerthird.LowerThird;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class GenericPanelWithHeaderAndLowerThird<T extends JPanel> extends JPanel {

  private final T panel;
  private final JLabel label;
  private final LowerThird lowerThird;

  public GenericPanelWithHeaderAndLowerThird(T panel, String label, LowerThird lowerThird) {
    this.panel = panel;
    this.label = new JLabel(label);
    this.label.setHorizontalAlignment(JLabel.CENTER);
    this.label.setBorder(new EmptyBorder(5, 0, -5, 0));
    this.label.setFont(StandardFont.readBoldFont(32));
    this.lowerThird = lowerThird;

    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(1024, 512));
    add(this.label, BorderLayout.NORTH);
    add(this.lowerThird, BorderLayout.SOUTH);
    add(this.panel, BorderLayout.CENTER);
  }

  public T getPanel() {
    return panel;
  }

  public JLabel getLabel() {
    return label;
  }

  public LowerThird getLowerThird() {
    return lowerThird;
  }
}
