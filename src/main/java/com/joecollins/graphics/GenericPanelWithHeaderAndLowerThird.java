package com.joecollins.graphics;

import com.joecollins.bindings.Binding;
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

  public GenericPanelWithHeaderAndLowerThird(T panel, String label) {
    this(panel, Binding.fixedBinding(label), null);
  }

  public GenericPanelWithHeaderAndLowerThird(T panel, LowerThird lowerThird) {
    this(panel, (Binding<String>) null, lowerThird);
  }

  public GenericPanelWithHeaderAndLowerThird(T panel, String label, LowerThird lowerThird) {
    this(panel, Binding.fixedBinding(label), lowerThird);
  }

  public GenericPanelWithHeaderAndLowerThird(
      T panel, Binding<String> label, LowerThird lowerThird) {
    this.panel = panel;
    this.lowerThird = lowerThird;
    this.label = new JLabel();
    if (label != null) {
      label.bind(this.label::setText);
      this.label.setHorizontalAlignment(JLabel.CENTER);
      this.label.setBorder(new EmptyBorder(5, 0, -5, 0));
      this.label.setFont(StandardFont.readBoldFont(32));
    }

    setBackground(Color.WHITE);
    setLayout(new BorderLayout());
    setPreferredSize(new Dimension(1024, 512));
    if (label != null) {
      add(this.label, BorderLayout.NORTH);
    }
    if (lowerThird != null) {
      add(this.lowerThird, BorderLayout.SOUTH);
    }
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
