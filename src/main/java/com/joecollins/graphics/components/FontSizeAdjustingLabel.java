package com.joecollins.graphics.components;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import javax.swing.JLabel;

public class FontSizeAdjustingLabel extends JLabel {

  @Override
  protected void paintComponent(Graphics g) {
    Font font = super.getFont();
    if (font != null) {
      Font newFont = font;
      for (int size = font.getSize(); size > 1; size--) {
        newFont = font.deriveFont((float) size);
        if (getStringWidth(newFont) <= getWidth() - 2) break;
      }
      g.setFont(newFont);
    }
    super.paintComponent(g);
  }

  private double getStringWidth(Font font) {
    AffineTransform affinetransform = new AffineTransform();
    FontRenderContext frc = new FontRenderContext(affinetransform, true, true);
    return font.getStringBounds(getText(), frc).getWidth();
  }
}
