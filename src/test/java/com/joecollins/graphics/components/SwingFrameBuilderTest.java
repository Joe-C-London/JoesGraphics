package com.joecollins.graphics.components;

import static org.junit.Assert.assertEquals;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;
import java.awt.Color;
import org.junit.Test;

public class SwingFrameBuilderTest {

  private enum BindableWrapperValue {
    VALUE
  }

  private static class BindableWrapper<T> extends Bindable {
    private T value;

    T getValue() {
      return value;
    }

    void setValue(T value) {
      this.value = value;
      onPropertyRefreshed(BindableWrapperValue.VALUE);
    }

    Binding<T> getBinding() {
      return Binding.propertyBinding(this, BindableWrapper::getValue, BindableWrapperValue.VALUE);
    }
  }

  private static class SwingProperties {
    private final Color leftColor;
    private final Color rightColor;
    private final Number value;
    private final String text;

    private SwingProperties(Color leftColor, Color rightColor, Number value, String text) {
      this.leftColor = leftColor;
      this.rightColor = rightColor;
      this.value = value;
      this.text = text;
    }
  }

  @Test
  public void basicTest() {
    BindableWrapper<SwingProperties> swingProps = new BindableWrapper<>();
    swingProps.setValue(new SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"));
    SwingFrame frame =
        SwingFrameBuilder.basic(
                swingProps.getBinding(),
                p -> p.leftColor,
                p -> p.rightColor,
                p -> p.value,
                p -> p.text)
            .withRange(Binding.fixedBinding(0.10))
            .withHeader(Binding.fixedBinding("SWING"))
            .build();
    assertEquals(Color.RED, frame.getLeftColor());
    assertEquals(Color.BLUE, frame.getRightColor());
    assertEquals(Color.RED, frame.getBottomColor());
    assertEquals(0.02, frame.getValue().doubleValue(), 1e-6);
    assertEquals("2% SWING", frame.getBottomText());
    assertEquals(0.10, frame.getRange());
    assertEquals("SWING", frame.getHeader());

    swingProps.setValue(new SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING"));
    assertEquals(Color.GREEN, frame.getLeftColor());
    assertEquals(Color.ORANGE, frame.getRightColor());
    assertEquals(Color.ORANGE, frame.getBottomColor());
    assertEquals(-0.05, frame.getValue().doubleValue(), 1e-6);
    assertEquals("5% SWING", frame.getBottomText());
  }

  @Test
  public void testNeutralBottomColor() {
    BindableWrapper<SwingProperties> swingProps = new BindableWrapper<>();
    swingProps.setValue(new SwingProperties(Color.RED, Color.BLUE, 0.02, "2% SWING"));
    BindableWrapper<Color> neutralColor = new BindableWrapper<>();
    neutralColor.setValue(Color.GRAY);
    SwingFrame frame =
        SwingFrameBuilder.basic(
                swingProps.getBinding(),
                p -> p.leftColor,
                p -> p.rightColor,
                p -> p.value,
                p -> p.text)
            .withRange(Binding.fixedBinding(0.10))
            .withNeutralColor(neutralColor.getBinding())
            .build();
    assertEquals(Color.RED, frame.getBottomColor());

    swingProps.setValue(new SwingProperties(Color.GREEN, Color.ORANGE, -0.05, "5% SWING"));
    assertEquals(Color.ORANGE, frame.getBottomColor());

    neutralColor.setValue(Color.LIGHT_GRAY);
    assertEquals(Color.ORANGE, frame.getBottomColor());

    swingProps.setValue(new SwingProperties(Color.GREEN, Color.ORANGE, 0.00, "NO SWING"));
    assertEquals(Color.LIGHT_GRAY, frame.getBottomColor());

    neutralColor.setValue(Color.BLACK);
    assertEquals(Color.BLACK, frame.getBottomColor());
  }
}
