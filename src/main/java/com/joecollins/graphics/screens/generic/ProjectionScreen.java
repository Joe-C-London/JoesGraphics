package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.graphics.components.ProjectionFrame;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.Image;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class ProjectionScreen extends JPanel {

  public static ProjectionScreen createScreen(
      Binding<String> text, Binding<Color> color, Binding<Image> image) {
    return new ProjectionScreen(
        text, color, image, Binding.fixedBinding(ProjectionFrame.Alignment.BOTTOM));
  }

  public static ProjectionScreen createScreen(
      Binding<String> text,
      Binding<Color> color,
      Binding<Image> image,
      Binding<ProjectionFrame.Alignment> imageAlignment) {
    return new ProjectionScreen(text, color, image, imageAlignment);
  }

  private ProjectionScreen(
      Binding<String> text,
      Binding<Color> color,
      Binding<Image> image,
      Binding<ProjectionFrame.Alignment> imageAlignment) {
    setLayout(new GridLayout(1, 1));
    setBackground(Color.WHITE);
    setBorder(new EmptyBorder(5, 5, 5, 5));

    BindingReceiver<Color> colorReceiver = new BindingReceiver<>(color);
    ProjectionFrame frame = new ProjectionFrame();
    frame.setImageBinding(image);
    frame.setHeaderBinding(() -> "PROJECTION");
    frame.setBackColorBinding(colorReceiver.getBinding());
    frame.setBorderColorBinding(colorReceiver.getBinding());
    frame.setFooterTextBinding(text);
    frame.setImageAlignmentBinding(imageAlignment);
    add(frame);
  }
}
