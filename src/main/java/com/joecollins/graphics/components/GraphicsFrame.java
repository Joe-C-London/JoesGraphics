package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.MatteBorder;

public class GraphicsFrame extends JPanel {

  private final Font headerFont;
  private final JPanel headerPanel;
  private final JLabel headerLabel;
  private final JLabel notesLabel;

  private Binding<String> headerTextBinding = () -> null;
  private Binding<String> notesTextBinding = () -> null;
  private Binding<Color> borderColorBinding = () -> Color.BLACK;

  public GraphicsFrame() {
    setLayout(new BorderLayout());
    setBackground(Color.WHITE);
    setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
    setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
    setPreferredSize(new Dimension(1024, 1024));
    setMinimumSize(new Dimension(1, 1));

    headerPanel =
        new JPanel() {
          {
            setLayout(new GridLayout(1, 0));
            setBackground(Color.BLACK);
          }
        };
    add(headerPanel, BorderLayout.NORTH);

    headerFont = StandardFont.readNormalFont(24);
    headerLabel =
        new JLabel() {
          {
            setForeground(Color.WHITE);
            setHorizontalAlignment(JLabel.CENTER);
            setFont(headerFont);
          }
        };
    headerPanel.add(headerLabel);

    notesLabel =
        new JLabel() {
          {
            setForeground(Color.BLACK);
            setHorizontalAlignment(JLabel.RIGHT);
            setFont(StandardFont.readNormalFont(12));
          }
        };
    add(notesLabel, BorderLayout.SOUTH);
  }

  String getHeader() {
    return this.headerPanel.isVisible() ? this.headerLabel.getText().trim() : null;
  }

  public void setHeaderBinding(Binding<String> headerTextBinding) {
    this.headerTextBinding.unbind();
    this.headerTextBinding = headerTextBinding;
    this.headerTextBinding.bind(
        headerText -> {
          headerPanel.setVisible(headerText != null);
          if (headerText != null) {
            headerLabel.setText(headerText);
          }
        });
  }

  String getNotes() {
    return this.notesLabel.isVisible() ? this.notesLabel.getText().trim() : null;
  }

  public void setNotesBinding(Binding<String> notesTextBinding) {
    this.notesTextBinding.unbind();
    this.notesTextBinding = notesTextBinding;
    this.notesTextBinding.bind(
        notesText -> {
          notesLabel.setVisible(notesText != null);
          if (notesText != null) {
            notesLabel.setText(notesText + " ");
          }
        });
  }

  Color getBorderColor() {
    return this.headerPanel.getBackground();
  }

  public void setBorderColorBinding(Binding<Color> borderColorBinding) {
    this.borderColorBinding.unbind();
    this.borderColorBinding = borderColorBinding;
    this.borderColorBinding.bind(
        borderColor -> {
          setBorder(new MatteBorder(1, 1, 1, 1, borderColor));
          headerPanel.setBackground(borderColor);
          notesLabel.setForeground(borderColor);
        });
  }
}
