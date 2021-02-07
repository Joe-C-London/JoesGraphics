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
import java.util.function.Predicate;
import org.apache.commons.lang3.tuple.Pair;

public class SwingometerFrameBuilder {

  private static class Properties extends Bindable<Properties.Property> {

    enum Property {
      VALUE
    }

    private Number value = 0;
    private Number max = 1;
    private Number bucketSize = 1;
    private Number tickInterval = null;

    public void setValue(Number value) {
      this.value = Double.isNaN(value.doubleValue()) ? 0.0 : value;
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
    value.bindLegacy(builder.properties::setValue);
    builder.frame.setLeftColorBinding(colorsRec.getBinding(Pair::getLeft));
    builder.frame.setRightColorBinding(colorsRec.getBinding(Pair::getRight));
    builder.frame.setValueBinding(
        Binding.propertyBinding(
            builder.properties, props -> props.value, Properties.Property.VALUE));
    return builder;
  }

  public SwingometerFrameBuilder withRange(Binding<? extends Number> range) {
    range.bindLegacy(properties::setMax);
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
    bucketSize.bindLegacy(properties::setBucketSize);
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
    tickInterval.bindLegacy(properties::setTickInterval);
    Binding.propertyBinding(
            properties, props -> getTicks(props, tickStringFunc), Properties.Property.VALUE)
        .bindLegacy(ticks::setAll);
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
    frame.setOuterLabelPositionBinding(IndexedBinding.propertyBinding(labels, positionFunc::apply));
    frame.setOuterLabelTextBinding(IndexedBinding.propertyBinding(labels, labelFunc::apply));
    frame.setOuterLabelColorBinding(IndexedBinding.propertyBinding(labels, colorFunc::apply));
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
    return withDots(dots, positionFunc, colorFunc, labelFunc, d -> true);
  }

  public <T> SwingometerFrameBuilder withDotsSolid(
      BindableList<T> dots,
      Function<T, ? extends Number> positionFunc,
      Function<T, Color> colorFunc,
      Predicate<T> solidFunc) {
    return withDots(dots, positionFunc, colorFunc, d -> "", solidFunc);
  }

  public <T> SwingometerFrameBuilder withDots(
      BindableList<T> dots,
      Function<T, ? extends Number> positionFunc,
      Function<T, Color> colorFunc,
      Function<T, String> labelFunc,
      Predicate<T> solidFunc) {
    frame.setNumDotsBinding(Binding.sizeBinding(dots));
    frame.setDotsPositionBinding(IndexedBinding.propertyBinding(dots, positionFunc::apply));
    frame.setDotsColorBinding(IndexedBinding.propertyBinding(dots, colorFunc::apply));
    frame.setDotsLabelBinding(IndexedBinding.propertyBinding(dots, labelFunc::apply));
    frame.setDotsSolidBinding(IndexedBinding.propertyBinding(dots, solidFunc::test));
    return this;
  }

  public <T> SwingometerFrameBuilder withFixedDots(
      List<T> dots,
      Function<T, ? extends Number> positionFunc,
      Function<T, Binding<Color>> colorFunc) {
    return withFixedDots(dots, positionFunc, colorFunc, d -> "");
  }

  public <T> SwingometerFrameBuilder withFixedDots(
      List<T> dots,
      Function<T, ? extends Number> positionFunc,
      Function<T, Binding<Color>> colorFunc,
      Function<T, String> labelFunc) {
    return withFixedDots(dots, positionFunc, colorFunc, labelFunc, d -> true);
  }

  public <T> SwingometerFrameBuilder withFixedDotsSolid(
      List<T> dots,
      Function<T, ? extends Number> positionFunc,
      Function<T, Binding<Color>> colorFunc,
      Predicate<T> solidFunc) {
    return withFixedDots(dots, positionFunc, colorFunc, d -> "", solidFunc);
  }

  public <T> SwingometerFrameBuilder withFixedDots(
      List<T> dots,
      Function<T, ? extends Number> positionFunc,
      Function<T, Binding<Color>> colorFunc,
      Function<T, String> labelFunc,
      Predicate<T> solidFunc) {
    frame.setNumDotsBinding(Binding.fixedBinding(dots.size()));
    frame.setDotsPositionBinding(
        IndexedBinding.listBinding(dots, d -> () -> positionFunc.apply(d)));
    frame.setDotsColorBinding(IndexedBinding.listBinding(dots, d -> colorFunc.apply(d)));
    frame.setDotsLabelBinding(IndexedBinding.listBinding(dots, d -> () -> labelFunc.apply(d)));
    frame.setDotsSolidBinding(IndexedBinding.listBinding(dots, d -> () -> solidFunc.test(d)));
    return this;
  }

  public SwingometerFrameBuilder withHeader(Binding<String> header) {
    frame.setHeaderBinding(header);
    return this;
  }

  public SwingometerFrame build() {
    return frame;
  }
}
