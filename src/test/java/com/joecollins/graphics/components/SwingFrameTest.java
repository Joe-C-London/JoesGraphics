package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Binding;
import java.awt.Color;
import java.io.IOException;
import org.junit.Test;

public class SwingFrameTest {

  @Test
  public void testSwingRange() {
    SwingFrame frame = new SwingFrame();
    frame.setRangeBinding(Binding.fixedBinding(10));
    assertEquals(10, frame.getRange());
  }

  @Test
  public void testSwingValue() {
    SwingFrame frame = new SwingFrame();
    frame.setValueBinding(Binding.fixedBinding(3));
    assertEquals(3, frame.getValue());
  }

  @Test
  public void testLeftRightColors() {
    SwingFrame frame = new SwingFrame();
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    assertEquals(Color.BLUE, frame.getLeftColor());
    assertEquals(Color.RED, frame.getRightColor());
  }

  @Test
  public void testBottomText() {
    SwingFrame frame = new SwingFrame();
    frame.setBottomTextBinding(Binding.fixedBinding("4.7% SWING LIB TO CON"));
    frame.setBottomColorBinding(Binding.fixedBinding(Color.BLUE));
    assertEquals("4.7% SWING LIB TO CON", frame.getBottomText());
    assertEquals(Color.BLUE, frame.getBottomColor());
  }

  @Test
  public void testRenderNoSwing() throws IOException {
    SwingFrame frame = new SwingFrame();
    frame.setHeaderBinding(Binding.fixedBinding("SWING SINCE 2015"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(0));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    frame.setBottomTextBinding(Binding.fixedBinding("WAITING FOR RESULTS..."));
    frame.setBottomColorBinding(Binding.fixedBinding(Color.BLACK));
    frame.setSize(256, 128);

    compareRendering("SwingFrame", "NoSwing", frame);
  }

  @Test
  public void testRenderSwingRight() throws IOException {
    SwingFrame frame = new SwingFrame();
    frame.setHeaderBinding(Binding.fixedBinding("SWING SINCE 2015"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(4.7));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    frame.setBottomTextBinding(Binding.fixedBinding("4.7% SWING LIB TO CON"));
    frame.setBottomColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setSize(256, 128);

    compareRendering("SwingFrame", "SwingRight", frame);
  }

  @Test
  public void testRenderSwingLeft() throws IOException {
    SwingFrame frame = new SwingFrame();
    frame.setHeaderBinding(Binding.fixedBinding("SWING SINCE 2015"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(-1.3));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.RED));
    frame.setRightColorBinding(Binding.fixedBinding(Color.ORANGE));
    frame.setBottomTextBinding(Binding.fixedBinding("1.3% SWING LIB TO NDP"));
    frame.setBottomColorBinding(Binding.fixedBinding(Color.ORANGE));
    frame.setSize(256, 128);

    compareRendering("SwingFrame", "SwingLeft", frame);
  }

  @Test
  public void testRenderMaxSwingRight() throws IOException {
    SwingFrame frame = new SwingFrame();
    frame.setHeaderBinding(Binding.fixedBinding("SWING SINCE 2015"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(19.9));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    frame.setBottomTextBinding(Binding.fixedBinding("19.9% SWING LIB TO CON"));
    frame.setBottomColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setSize(256, 128);

    compareRendering("SwingFrame", "MaxSwingRight", frame);
  }

  @Test
  public void testRenderMaxSwingLeft() throws IOException {
    SwingFrame frame = new SwingFrame();
    frame.setHeaderBinding(Binding.fixedBinding("SWING SINCE 2015"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(-21.6));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.RED));
    frame.setRightColorBinding(Binding.fixedBinding(Color.GREEN));
    frame.setBottomTextBinding(Binding.fixedBinding("21.6% SWING LIB TO GRN"));
    frame.setBottomColorBinding(Binding.fixedBinding(Color.GREEN));
    frame.setSize(256, 128);

    compareRendering("SwingFrame", "MaxSwingLeft", frame);
  }

  @Test
  public void testRenderAccents() throws IOException {
    SwingFrame frame = new SwingFrame();
    frame.setHeaderBinding(Binding.fixedBinding("CHANGES APR\u00c8S 2014"));
    frame.setRangeBinding(Binding.fixedBinding(10));
    frame.setValueBinding(Binding.fixedBinding(0));
    frame.setLeftColorBinding(Binding.fixedBinding(Color.BLUE));
    frame.setRightColorBinding(Binding.fixedBinding(Color.RED));
    frame.setBottomTextBinding(Binding.fixedBinding("VOIX PAS R\u00c9\u00c7US"));
    frame.setBottomColorBinding(Binding.fixedBinding(Color.BLACK));
    frame.setSize(256, 128);

    compareRendering("SwingFrame", "Accents", frame);
  }
}
