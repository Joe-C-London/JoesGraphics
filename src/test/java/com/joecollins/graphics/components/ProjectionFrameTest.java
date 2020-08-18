package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import com.joecollins.graphics.components.lowerthird.LowerThird;
import java.awt.Color;
import java.awt.Image;
import java.io.IOException;
import org.junit.Test;

public class ProjectionFrameTest {

  @Test
  public void testImage() throws IOException {
    ProjectionFrame frame = new ProjectionFrame();
    Image image = peiLeg();
    frame.setImageBinding(Binding.fixedBinding(image));
    assertEquals(image, frame.getImage());
  }

  @Test
  public void testBackColor() {
    ProjectionFrame frame = new ProjectionFrame();
    frame.setBackColorBinding(Binding.fixedBinding(Color.GRAY));
    assertEquals(Color.GRAY, frame.getBackColor());
  }

  @Test
  public void testFooterText() {
    ProjectionFrame frame = new ProjectionFrame();
    frame.setFooterTextBinding(Binding.fixedBinding("MINORITY LEGISLATURE"));
    assertEquals("MINORITY LEGISLATURE", frame.getFooterText());
  }

  @Test
  public void testRendering() throws IOException {
    ProjectionFrame frame = new ProjectionFrame();
    frame.setImageBinding(Binding.fixedBinding(peiLeg()));
    frame.setBackColorBinding(Binding.fixedBinding(Color.GRAY));
    frame.setBorderColorBinding(Binding.fixedBinding(Color.GRAY));
    frame.setFooterTextBinding(Binding.fixedBinding("MINORITY LEGISLATURE"));
    frame.setHeaderBinding(Binding.fixedBinding("PROJECTION"));

    frame.setSize(1024, 512);
    compareRendering("ProjectionFrame", "Basic", frame);
  }

  @Test
  public void testLongRendering() throws IOException {
    ProjectionFrame frame = new ProjectionFrame();
    frame.setImageBinding(Binding.fixedBinding(peiLeg()));
    frame.setBackColorBinding(Binding.fixedBinding(Color.GRAY));
    frame.setBorderColorBinding(Binding.fixedBinding(Color.GRAY));
    frame.setFooterTextBinding(
        Binding.fixedBinding("WE ARE NOW PROJECTING A MINORITY LEGISLATURE"));
    frame.setHeaderBinding(Binding.fixedBinding("PROJECTION"));

    frame.setSize(1024, 512);
    compareRendering("ProjectionFrame", "Long", frame);
  }

  private Image peiLeg() throws IOException {
    return LowerThird.createImage(
        ProjectionFrameTest.class
            .getClassLoader()
            .getResource("com/joecollins/graphics/pei-leg.png"));
  }
}
