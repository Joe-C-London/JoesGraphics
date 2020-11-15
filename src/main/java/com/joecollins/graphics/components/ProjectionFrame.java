package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ProjectionFrame extends GraphicsFrame {

  public enum Alignment {
    BOTTOM,
    MIDDLE
  }

  private Binding<Image> imageBinding =
      Binding.fixedBinding(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
  private Binding<Color> colorBinding = Binding.fixedBinding(Color.WHITE);
  private Binding<String> textBinding = Binding.fixedBinding("");
  private Binding<Alignment> imageAlignmentBinding = Binding.fixedBinding(Alignment.BOTTOM);

  private ImagePanel imagePanel = new ImagePanel();
  private JPanel footerPanel = new JPanel();
  private FontSizeAdjustingLabel footerLabel = new FontSizeAdjustingLabel();

  public ProjectionFrame() {
    JPanel centre = new JPanel();
    add(centre, BorderLayout.CENTER);

    centre.setLayout(new BorderLayout());
    centre.add(imagePanel, BorderLayout.CENTER);
    centre.add(footerPanel, BorderLayout.SOUTH);

    footerPanel.setLayout(new GridLayout(1, 1));
    footerPanel.add(footerLabel);

    footerLabel.setFont(StandardFont.readBoldFont(72));
    footerLabel.setForeground(Color.WHITE);
    footerLabel.setHorizontalAlignment(JLabel.CENTER);
    footerLabel.setBorder(new EmptyBorder(15, 0, -15, 0));
  }

  Image getImage() {
    return imagePanel.image;
  }

  public void setImageBinding(Binding<Image> imageBinding) {
    this.imageBinding.unbind();
    this.imageBinding = imageBinding;
    this.imageBinding.bind(imagePanel::setImage);
  }

  Color getBackColor() {
    return footerPanel.getBackground();
  }

  public void setBackColorBinding(Binding<Color> colorBinding) {
    this.colorBinding.unbind();
    this.colorBinding = colorBinding;
    this.colorBinding.bind(footerPanel::setBackground);
  }

  String getFooterText() {
    return footerLabel.getText();
  }

  public void setFooterTextBinding(Binding<String> textBinding) {
    this.textBinding.unbind();
    this.textBinding = textBinding;
    this.textBinding.bind(footerLabel::setText);
  }

  Alignment getImageAlignment() {
    return imagePanel.alignment;
  }

  public void setImageAlignmentBinding(Binding<Alignment> imageAlignmentBinding) {
    this.imageAlignmentBinding.unbind();
    this.imageAlignmentBinding = imageAlignmentBinding;
    this.imageAlignmentBinding.bind(imagePanel::setAlignment);
  }

  private class ImagePanel extends JPanel {
    private Image image = imageBinding.getValue();
    private Alignment alignment = imageAlignmentBinding.getValue();

    public ImagePanel() {
      setBackground(Color.WHITE);
    }

    private void setImage(Image image) {
      this.image = image;
      repaint();
    }

    private void setAlignment(Alignment alignment) {
      this.alignment = alignment;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      double xRatio = 1.0 * getWidth() / image.getWidth(null);
      double yRatio = 1.0 * getHeight() / image.getHeight(null);
      double ratio = Math.min(1.0, Math.min(xRatio, yRatio));
      int newWidth = (int) (ratio * image.getWidth(null));
      int newHeight = (int) (ratio * image.getHeight(null));
      ((Graphics2D) g)
          .setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      Image scaledImage = image.getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT);
      g.drawImage(
          scaledImage,
          (getWidth() - newWidth) / 2,
          (getHeight() - newHeight) / (alignment == Alignment.BOTTOM ? 1 : 2),
          null);
    }
  }
}
