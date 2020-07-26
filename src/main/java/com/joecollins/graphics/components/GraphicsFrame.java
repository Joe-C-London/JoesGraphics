package com.joecollins.graphics.components;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.StandardFont;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.lang.reflect.Field;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

public class GraphicsFrame extends JPanel {

  public enum Alignment {
    LEFT(JLabel.LEFT),
    CENTER(JLabel.CENTER),
    RIGHT(JLabel.RIGHT);

    final int jlabelAlignment;

    Alignment(int jlabelAlignment) {
      this.jlabelAlignment = jlabelAlignment;
    }
  }

  private final Font headerFont;
  private final JPanel headerPanel;
  private final JLabel headerLabel;
  private final JLabel notesLabel;

  private Binding<String> headerTextBinding = () -> null;
  private Binding<String> notesTextBinding = () -> null;
  private Binding<Color> borderColorBinding = () -> Color.BLACK;
  private Binding<Alignment> headerAlignmentBinding = () -> Alignment.CENTER;

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
        new FontSizeAdjustingLabel() {
          {
            setForeground(Color.WHITE);
            setHorizontalAlignment(JLabel.CENTER);
            setFont(headerFont);
            setBorder(new EmptyBorder(3, 0, -3, 0));
          }
        };
    headerPanel.add(headerLabel);

    notesLabel =
        new JLabel() {
          {
            setForeground(Color.BLACK);
            setHorizontalAlignment(JLabel.RIGHT);
            setFont(StandardFont.readNormalFont(12));
            setBorder(new EmptyBorder(2, 0, -2, 0));
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

  Alignment getHeaderAlignment() {
    return Arrays.stream(Alignment.values())
        .filter(a -> a.jlabelAlignment == headerLabel.getHorizontalAlignment())
        .findFirst()
        .orElseThrow();
  }

  public void setHeaderAlignmentBinding(Binding<Alignment> headerAlignmentBinding) {
    this.headerAlignmentBinding.unbind();
    this.headerAlignmentBinding = headerAlignmentBinding;
    this.headerAlignmentBinding.bind(
        headerAlignment -> headerLabel.setHorizontalAlignment(headerAlignment.jlabelAlignment));
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

  public void dispose() {
    try {
      Class<?> clazz = this.getClass();
      do {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
          if (Binding.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            ((Binding<?>) field.get(this)).unbind();
          } else if (IndexedBinding.class.isAssignableFrom(field.getType())) {
            field.setAccessible(true);
            ((IndexedBinding<?>) field.get(this)).unbind();
          }
        }
        clazz = clazz.getSuperclass();
      } while (clazz != GraphicsFrame.class.getSuperclass());
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    }
  }
}
