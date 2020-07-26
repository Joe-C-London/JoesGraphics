package com.joecollins.graphics.components;

import static com.joecollins.graphics.utils.RenderTestUtils.compareRendering;
import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import com.joecollins.graphics.utils.BindableWrapper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;
import javax.swing.JPanel;
import org.junit.Test;

public class GraphicsFrameTest {

  private static class TestObject extends Bindable {

    private enum Properties {
      NUM_POLLS
    }

    int numPolls = 0;

    public void setNumPolls(int numPolls) {
      this.numPolls = numPolls;
      onPropertyRefreshed(Properties.NUM_POLLS);
    }
  }

  @Test
  public void testFixedHeader() {
    GraphicsFrame graphicsFrame = new GraphicsFrame();
    graphicsFrame.setHeaderBinding(Binding.fixedBinding("HEADER"));
    assertEquals("HEADER", graphicsFrame.getHeader());
  }

  @Test
  public void testDynamicHeader() {
    GraphicsFrame graphicsFrame = new GraphicsFrame();
    graphicsFrame.setHeaderBinding(
        Binding.propertyBinding(
            new TestObject(),
            o -> o.numPolls + " POLLS REPORTING",
            TestObject.Properties.NUM_POLLS));
    assertEquals("0 POLLS REPORTING", graphicsFrame.getHeader());
  }

  @Test
  public void testDynamicHeaderRefreshed() {
    GraphicsFrame graphicsFrame = new GraphicsFrame();
    TestObject object = new TestObject();
    graphicsFrame.setHeaderBinding(
        Binding.propertyBinding(
            object, o -> o.numPolls + " POLLS REPORTING", TestObject.Properties.NUM_POLLS));
    assertEquals("0 POLLS REPORTING", graphicsFrame.getHeader());
    object.setNumPolls(1);
    assertEquals("1 POLLS REPORTING", graphicsFrame.getHeader());

    graphicsFrame.dispose();
    object.setNumPolls(0);
    assertEquals("1 POLLS REPORTING", graphicsFrame.getHeader());
  }

  @Test
  public void testUnbind() {
    GraphicsFrame graphicsFrame = new GraphicsFrame();
    TestObject object = new TestObject();
    graphicsFrame.setHeaderBinding(
        Binding.propertyBinding(
            object, o -> o.numPolls + " POLLS REPORTING", TestObject.Properties.NUM_POLLS));
    assertEquals("0 POLLS REPORTING", graphicsFrame.getHeader());
    graphicsFrame.setHeaderBinding(Binding.fixedBinding("FIXED BINDING"));
    assertEquals("FIXED BINDING", graphicsFrame.getHeader());
    object.setNumPolls(1);
    assertEquals("FIXED BINDING", graphicsFrame.getHeader());
  }

  @Test
  public void testFixedNotes() {
    GraphicsFrame graphicsFrame = new GraphicsFrame();
    graphicsFrame.setNotesBinding(Binding.fixedBinding("SOURCE: BBC"));
    assertEquals("SOURCE: BBC", graphicsFrame.getNotes());
  }

  @Test
  public void testBorderColor() {
    GraphicsFrame graphicsFrame = new GraphicsFrame();
    graphicsFrame.setBorderColorBinding(Binding.fixedBinding(Color.BLUE));
    assertEquals(Color.BLUE, graphicsFrame.getBorderColor());
  }

  @Test
  public void testRenderingHeaderOnly() throws IOException {
    GraphicsFrame graphicsFrame =
        new GraphicsFrame() {
          {
            JPanel panel = new JPanel();
            panel.setBackground(Color.YELLOW);
            add(panel, BorderLayout.CENTER);
          }
        };
    graphicsFrame.setHeaderBinding(Binding.fixedBinding("HEADER"));
    graphicsFrame.setSize(256, 128);
    compareRendering("GraphicsFrame", "HeaderOnly", graphicsFrame);
  }

  @Test
  public void testRenderingHeaderAndNotes() throws IOException {
    GraphicsFrame graphicsFrame =
        new GraphicsFrame() {
          {
            JPanel panel = new JPanel();
            panel.setBackground(Color.YELLOW);
            add(panel, BorderLayout.CENTER);
          }
        };
    graphicsFrame.setHeaderBinding(Binding.fixedBinding("HEADER"));
    graphicsFrame.setNotesBinding(Binding.fixedBinding("SOURCE: BBC"));
    graphicsFrame.setSize(256, 128);
    compareRendering("GraphicsFrame", "HeaderAndNotes", graphicsFrame);
  }

  @Test
  public void testRenderingNoHeader() throws IOException {
    GraphicsFrame graphicsFrame =
        new GraphicsFrame() {
          {
            JPanel panel = new JPanel();
            panel.setBackground(Color.YELLOW);
            add(panel, BorderLayout.CENTER);
          }
        };
    graphicsFrame.setSize(256, 128);
    compareRendering("GraphicsFrame", "NoHeader", graphicsFrame);
  }

  @Test
  public void testRenderingBorderColor() throws IOException {
    GraphicsFrame graphicsFrame =
        new GraphicsFrame() {
          {
            JPanel panel = new JPanel();
            panel.setBackground(Color.YELLOW);
            add(panel, BorderLayout.CENTER);
          }
        };
    graphicsFrame.setHeaderBinding(Binding.fixedBinding("HEADER"));
    graphicsFrame.setNotesBinding(Binding.fixedBinding("SOURCE: BBC"));
    graphicsFrame.setBorderColorBinding(Binding.fixedBinding(Color.RED));
    graphicsFrame.setSize(256, 128);
    compareRendering("GraphicsFrame", "BorderColor", graphicsFrame);
  }

  @Test
  public void testRenderingAccents() throws IOException {
    GraphicsFrame graphicsFrame =
        new GraphicsFrame() {
          {
            JPanel panel = new JPanel();
            panel.setBackground(Color.YELLOW);
            add(panel, BorderLayout.CENTER);
          }
        };
    graphicsFrame.setHeaderBinding(Binding.fixedBinding("\u00c7A C'EST GR\u00c2VE"));
    graphicsFrame.setNotesBinding(Binding.fixedBinding("JOYEUX NO\u00cbL, GAR\u00c7ON!"));
    graphicsFrame.setSize(256, 128);
    compareRendering("GraphicsFrame", "Accents", graphicsFrame);
  }

  @Test
  public void testHeaderFontSize() throws IOException {
    GraphicsFrame graphicsFrame =
        new GraphicsFrame() {
          {
            JPanel panel = new JPanel();
            panel.setBackground(Color.YELLOW);
            add(panel, BorderLayout.CENTER);
          }
        };
    BindableWrapper<String> headerWrapper = new BindableWrapper<>("THIS IS A HEADER");
    graphicsFrame.setHeaderBinding(headerWrapper.getBinding());
    graphicsFrame.setSize(256, 128);
    compareRendering("GraphicsFrame", "HeaderFontSize-1", graphicsFrame);

    headerWrapper.setValue("THIS IS A VERY MUCH LONGER HEADER");
    compareRendering("GraphicsFrame", "HeaderFontSize-2", graphicsFrame);

    graphicsFrame.setSize(512, 128);
    compareRendering("GraphicsFrame", "HeaderFontSize-3", graphicsFrame);
  }

  @Test
  public void testRenderHeaderAlignment() throws IOException {
    BindableWrapper<GraphicsFrame.Alignment> alignment =
        new BindableWrapper<>(GraphicsFrame.Alignment.CENTER);
    GraphicsFrame graphicsFrame =
        new GraphicsFrame() {
          {
            JPanel panel = new JPanel();
            panel.setBackground(Color.YELLOW);
            add(panel, BorderLayout.CENTER);
          }
        };
    graphicsFrame.setHeaderBinding(Binding.fixedBinding("HEADER"));
    graphicsFrame.setHeaderAlignmentBinding(alignment.getBinding());
    graphicsFrame.setSize(256, 128);
    compareRendering("GraphicsFrame", "HeaderAlignment-1", graphicsFrame);

    alignment.setValue(GraphicsFrame.Alignment.LEFT);
    compareRendering("GraphicsFrame", "HeaderAlignment-2", graphicsFrame);

    alignment.setValue(GraphicsFrame.Alignment.RIGHT);
    compareRendering("GraphicsFrame", "HeaderAlignment-3", graphicsFrame);
  }
}
