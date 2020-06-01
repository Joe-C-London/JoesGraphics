package com.joecollins.graphics.components;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.Pair;

public class SwingometerFrameBuilder {

  private static class Properties extends Bindable {

    enum Property {
      VALUE
    };

    private Number value = 0;
    private Number max = 1;
    private Number bucketSize = 1;
    private Number tickInterval = null;

    public void setValue(Number value) {
      this.value = value;
      onPropertyRefreshed(Properties.Property.VALUE);
    }

    public void setMax(Number max) {
      this.max = max;
      onPropertyRefreshed(Properties.Property.VALUE);
    }

    public void setBucketSize(Number bucketSize) {
      this.bucketSize = bucketSize;
      onPropertyRefreshed(Properties.Property.VALUE);
    }

    public void setTickInterval(Number tickInterval) {
      this.tickInterval = tickInterval;
      onPropertyRefreshed(Properties.Property.VALUE);
    }
  }

  private SwingometerFrame frame = new SwingometerFrame();
  private final Properties properties = new Properties();

  public static SwingometerFrameBuilder basic(
      Binding<? extends Pair<Color, Color>> colors, Binding<? extends Number> value) {
    BindingReceiver<? extends Pair<Color, Color>> colorsRec = new BindingReceiver<>(colors);
    SwingometerFrameBuilder builder = new SwingometerFrameBuilder();
    value.bind(builder.properties::setValue);
    builder.frame.setLeftColorBinding(colorsRec.getBinding(Pair::getLeft));
    builder.frame.setRightColorBinding(colorsRec.getBinding(Pair::getRight));
    builder.frame.setValueBinding(
        Binding.propertyBinding(
            builder.properties, props -> props.value, Properties.Property.VALUE));
    return builder;
  }

  public SwingometerFrameBuilder withRange(Binding<? extends Number> range) {
    range.bind(properties::setMax);
    frame.setRangeBinding(
        Binding.propertyBinding(properties, this::getMax, Properties.Property.VALUE));
    return this;
  }

  private double getMax(Properties props) {
    return Math.max(
        props.max.doubleValue(),
        props.bucketSize.doubleValue()
            * Math.ceil(Math.abs(props.value.doubleValue() / props.bucketSize.doubleValue())));
  }

  public SwingometerFrameBuilder withBucketSize(Binding<? extends Number> bucketSize) {
    bucketSize.bind(properties::setBucketSize);
    frame.setNumBucketsPerSideBinding(
        Binding.propertyBinding(
            properties,
            props -> (int) Math.round(getMax(props) / props.bucketSize.doubleValue()),
            Properties.Property.VALUE));
    return this;
  }

  private class Tick {
    private final double level;
    private final String text;

    public Tick(double level, String text) {
      this.level = level;
      this.text = text;
    }
  }

  public SwingometerFrameBuilder withTickInterval(
      Binding<? extends Number> tickInterval, Function<? super Number, String> tickStringFunc) {
    BindableList<Tick> ticks = new BindableList<>();
    tickInterval.bind(properties::setTickInterval);
    Binding.propertyBinding(
            properties, props -> getTicks(props, tickStringFunc), Properties.Property.VALUE)
        .bind(ticks::setAll);
    frame.setNumTicksBinding(Binding.sizeBinding(ticks));
    frame.setTickPositionBinding(IndexedBinding.propertyBinding(ticks, t -> t.level));
    frame.setTickTextBinding(IndexedBinding.propertyBinding(ticks, t -> t.text));
    return this;
  }

  private List<Tick> getTicks(Properties props, Function<? super Number, String> tickStringFunc) {
    ArrayList<Tick> ticks = new ArrayList<>();
    double max = getMax(props);
    ticks.add(new Tick(0, tickStringFunc.apply(0)));
    for (double i = props.tickInterval.doubleValue();
        i < max;
        i += props.tickInterval.doubleValue()) {
      ticks.add(new Tick(i, tickStringFunc.apply(i)));
      ticks.add(new Tick(-i, tickStringFunc.apply(i)));
    }
    return ticks;
  }

  public SwingometerFrameBuilder withLeftNeedingToWin(Binding<? extends Number> leftToWin) {
    frame.setLeftToWinBinding(leftToWin);
    return this;
  }

  public SwingometerFrameBuilder withRightNeedingToWin(Binding<? extends Number> rightToWin) {
    frame.setRightToWinBinding(rightToWin);
    return this;
  }

  public <T> SwingometerFrameBuilder withOuterLabels(
      BindableList<T> labels,
      Function<T, ? extends Number> positionFunc,
      Function<T, String> labelFunc,
      Function<T, Color> colorFunc) {
    frame.setNumOuterLabelsBinding(Binding.sizeBinding(labels));
    frame.setOuterLabelPositionBinding(IndexedBinding.propertyBinding(labels, positionFunc));
    frame.setOuterLabelTextBinding(IndexedBinding.propertyBinding(labels, labelFunc));
    frame.setOuterLabelColorBinding(IndexedBinding.propertyBinding(labels, colorFunc));
    return this;
  }

  public <T> SwingometerFrameBuilder withDots(
      BindableList<T> dots,
      Function<T, ? extends Number> positionFunc,
      Function<T, Color> colorFunc) {
    return withDots(dots, positionFunc, colorFunc, d -> "");
  }

  public <T> SwingometerFrameBuilder withDots(
      BindableList<T> dots,
      Function<T, ? extends Number> positionFunc,
      Function<T, Color> colorFunc,
      Function<T, String> labelFunc) {
    frame.setNumDotsBinding(Binding.sizeBinding(dots));
    frame.setDotsPositionBinding(IndexedBinding.propertyBinding(dots, positionFunc));
    frame.setDotsColorBinding(IndexedBinding.propertyBinding(dots, colorFunc));
    frame.setDotsLabelBinding(IndexedBinding.propertyBinding(dots, labelFunc));
    return this;
  }

  public SwingometerFrame build() {
    return frame;
  }
}
