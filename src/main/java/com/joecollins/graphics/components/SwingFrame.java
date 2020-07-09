package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class SwingFrame extends GraphicsFrame {

  private Binding<? extends Number> rangeBinding = () -> 1;
  private Binding<? extends Number> valueBinding = () -> 0;
  private Binding<Color> leftColorBinding = () -> Color.BLACK;
  private Binding<Color> rightColorBinding = () -> Color.BLACK;
  private Binding<String> bottomTextBinding = () -> null;
  private Binding<Color> bottomColorBinding = () -> Color.BLACK;

  private SwingPanel swingPanel = new SwingPanel();
  private JLabel bottomLabel =
      new FontSizeAdjustingLabel() {
        {
          setHorizontalAlignment(JLabel.CENTER);
          setFont(StandardFont.readBoldFont(15));
          setBorder(new EmptyBorder(2, 0, -2, 0));
        }
      };

  public SwingFrame() {
    JPanel centerPanel = new JPanel();
    centerPanel.setBackground(Color.WHITE);
    centerPanel.setLayout(new BorderLayout());
    centerPanel.add(swingPanel, BorderLayout.CENTER);
    centerPanel.add(bottomLabel, BorderLayout.SOUTH);
    add(centerPanel, BorderLayout.CENTER);
  }

  Number getRange() {
    return swingPanel.range;
  }

  public void setRangeBinding(Binding<? extends Number> rangeBinding) {
    this.rangeBinding.unbind();
    this.rangeBinding = rangeBinding;
    this.rangeBinding.bind(swingPanel::setRange);
  }

  Number getValue() {
    return swingPanel.value;
  }

  public void setValueBinding(Binding<? extends Number> valueBinding) {
    this.valueBinding.unbind();
    this.valueBinding = valueBinding;
    this.valueBinding.bind(swingPanel::setValue);
  }

  Color getLeftColor() {
    return swingPanel.leftColor;
  }

  public void setLeftColorBinding(Binding<Color> leftColorBinding) {
    this.leftColorBinding.unbind();
    this.leftColorBinding = leftColorBinding;
    this.leftColorBinding.bind(swingPanel::setLeftColor);
  }

  Color getRightColor() {
    return swingPanel.rightColor;
  }

  public void setRightColorBinding(Binding<Color> rightColorBinding) {
    this.rightColorBinding.unbind();
    this.rightColorBinding = rightColorBinding;
    this.rightColorBinding.bind(swingPanel::setRightColor);
  }

  String getBottomText() {
    return bottomLabel.isVisible() ? bottomLabel.getText() : null;
  }

  public void setBottomTextBinding(Binding<String> bottomTextBinding) {
    this.bottomTextBinding.unbind();
    this.bottomTextBinding = bottomTextBinding;
    this.bottomTextBinding.bind(
        bottomText -> {
          bottomLabel.setVisible(bottomText != null);
          if (bottomText != null) {
            bottomLabel.setText(bottomText);
          }
        });
  }

  Color getBottomColor() {
    return bottomLabel.getForeground();
  }

  public void setBottomColorBinding(Binding<Color> bottomColorBinding) {
    this.bottomColorBinding.unbind();
    this.bottomColorBinding = bottomColorBinding;
    this.bottomColorBinding.bind(bottomLabel::setForeground);
  }

  private class SwingPanel extends JPanel {
    private Number range = rangeBinding.getValue();
    private Number value = valueBinding.getValue();
    private Color leftColor = leftColorBinding.getValue();
    private Color rightColor = rightColorBinding.getValue();

    private SwingPanel() {
      setBackground(Color.WHITE);
    }

    private void setRange(Number range) {
      this.range = range;
      repaint();
    }

    private void setValue(Number value) {
      this.value = value;
      repaint();
    }

    public void setLeftColor(Color leftColor) {
      this.leftColor = leftColor;
      repaint();
    }

    public void setRightColor(Color rightColor) {
      this.rightColor = rightColor;
      repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      ((Graphics2D) g)
          .setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      int margin = 2;
      int arcWidth = getWidth() / 2 - 2 * margin;
      int arcHeight = getHeight() - 2 * margin;
      int arcSize = Math.min(arcWidth, arcHeight);
      int arcX = getWidth() / 2 - arcSize;
      int arcY = (getHeight() - arcSize) / 2;
      int arcAngle = (int) (90 * value.doubleValue() / range.doubleValue());
      int maxAngle = 85;
      arcAngle = Math.max(-maxAngle, Math.min(arcAngle, maxAngle));
      g.setColor(leftColor);
      g.fillArc(arcX, arcY - arcSize, arcSize * 2, arcSize * 2, 180, arcAngle + 90);
      g.setColor(rightColor);
      g.fillArc(arcX, arcY - arcSize, arcSize * 2, arcSize * 2, 0, arcAngle - 90);
      g.setColor(getBackground());
      g.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
    }
  }
}
