package com.joecollins.graphics.components;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.ColorUtils;
import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public class BarFrameBuilder {

  private final BarFrame barFrame =
      new BarFrame() {
        @Override
        public void dispose() {
          super.dispose();
          bindings.forEach(Binding::unbind);
        }
      };
  private final RangeFinder rangeFinder = new RangeFinder();

  private final List<Binding<?>> bindings = new ArrayList<>();

  private static class RangeFinder extends Bindable<RangeFinder.Property> {

    enum Property {
      MIN,
      MAX
    }

    private Number highest = 0, lowest = 0;
    private Function<RangeFinder, Number> minFunction = rf -> rf.lowest;
    private Function<RangeFinder, Number> maxFunction = rf -> rf.highest;

    public void setHighest(Number highest) {
      this.highest = highest;
      onPropertyRefreshed(Property.MIN);
      onPropertyRefreshed(Property.MAX);
    }

    public void setLowest(Number lowest) {
      this.lowest = lowest;
      onPropertyRefreshed(Property.MIN);
      onPropertyRefreshed(Property.MAX);
    }

    public void setMinFunction(Function<RangeFinder, Number> minFunction) {
      this.minFunction = minFunction;
      onPropertyRefreshed(Property.MIN);
    }

    public void setMaxFunction(Function<RangeFinder, Number> maxFunction) {
      this.maxFunction = maxFunction;
      onPropertyRefreshed(Property.MAX);
    }
  }

  public static class BasicBar {
    private final String label;
    private final Color color;
    private final Number value;
    private final String valueLabel;
    private final Shape shape;

    public BasicBar(String label, Color color, Number value) {
      this(label, color, value, value.toString());
    }

    public BasicBar(String label, Color color, Number value, String valueLabel) {
      this(label, color, value, valueLabel, null);
    }

    public BasicBar(String label, Color color, Number value, Shape shape) {
      this(label, color, value, value.toString(), shape);
    }

    public BasicBar(String label, Color color, Number value, String valueLabel, Shape shape) {
      this.label = label;
      this.color = color;
      this.value = value;
      this.valueLabel = valueLabel;
      this.shape = shape;
    }
  }

  public static BarFrameBuilder basic(Binding<? extends List<BasicBar>> binding) {
    BarFrameBuilder builder = new BarFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    RangeFinder rangeFinder = builder.rangeFinder;

    BindableList<BasicBar> entries = new BindableList<>();
    barFrame.setNumBarsBinding(Binding.sizeBinding(entries));
    barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(entries, e -> e.label));
    barFrame.setRightTextBinding(IndexedBinding.propertyBinding(entries, e -> e.valueLabel));
    barFrame.setLeftIconBinding(IndexedBinding.propertyBinding(entries, e -> e.shape));
    barFrame.setMinBinding(
        Binding.propertyBinding(
            rangeFinder, rf -> rf.minFunction.apply(rf), RangeFinder.Property.MIN));
    barFrame.setMaxBinding(
        Binding.propertyBinding(
            rangeFinder, rf -> rf.maxFunction.apply(rf), RangeFinder.Property.MAX));
    barFrame.addSeriesBinding(
        "Main",
        IndexedBinding.propertyBinding(entries, e -> e.color),
        IndexedBinding.propertyBinding(entries, e -> e.value));
    builder.bind(
        binding,
        map -> {
          if (map == null) {
            entries.clear();
            rangeFinder.setLowest(0);
            rangeFinder.setHighest(0);
          } else {
            entries.setAll(map);
            rangeFinder.setHighest(
                map.stream()
                    .map(e -> e.value)
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::max));
            rangeFinder.setLowest(
                map.stream()
                    .map(e -> e.value)
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::min));
          }
        });
    return builder;
  }

  public static class DualBar {
    private final String label;
    private final Color color;
    private final Number value1;
    private final Number value2;
    private final String valueLabel;
    private final Shape shape;

    public DualBar(String label, Color color, Number value1, Number value2, String valueLabel) {
      this(label, color, value1, value2, valueLabel, null);
    }

    public DualBar(
        String label, Color color, Number value1, Number value2, String valueLabel, Shape shape) {
      this.label = label;
      this.color = color;
      this.value1 = value1;
      this.value2 = value2;
      this.valueLabel = valueLabel;
      this.shape = shape;
    }
  }

  public static BarFrameBuilder dual(Binding<? extends List<DualBar>> bars) {
    BarFrameBuilder builder = new BarFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    RangeFinder rangeFinder = builder.rangeFinder;

    Predicate<DualBar> differentDirections =
        bar -> Math.signum(bar.value1.doubleValue()) * Math.signum(bar.value2.doubleValue()) == -1;
    Predicate<DualBar> reverse =
        bar ->
            differentDirections.test(bar)
                || Math.abs(bar.value1.doubleValue()) < Math.abs(bar.value2.doubleValue());
    Function<DualBar, Number> first = bar -> reverse.test(bar) ? bar.value1 : bar.value2;
    Function<DualBar, Number> second = bar -> reverse.test(bar) ? bar.value2 : bar.value1;

    BindableList<DualBar> entries = new BindableList<>();
    barFrame.setNumBarsBinding(Binding.sizeBinding(entries));
    barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(entries, e -> e.label));
    barFrame.setRightTextBinding(IndexedBinding.propertyBinding(entries, e -> e.valueLabel));
    barFrame.setMinBinding(
        Binding.propertyBinding(
            rangeFinder, rf -> rf.minFunction.apply(rf), RangeFinder.Property.MIN));
    barFrame.setMaxBinding(
        Binding.propertyBinding(
            rangeFinder, rf -> rf.maxFunction.apply(rf), RangeFinder.Property.MAX));
    barFrame.addSeriesBinding(
        "Placeholder",
        IndexedBinding.propertyBinding(entries, e -> e.color),
        IndexedBinding.propertyBinding(entries, e -> 0));
    barFrame.addSeriesBinding(
        "First",
        IndexedBinding.propertyBinding(
            entries,
            e -> {
              UnaryOperator<Color> cf =
                  differentDirections.test(e) ? ColorUtils::lighten : UnaryOperator.identity();
              return cf.apply(e.color);
            }),
        IndexedBinding.propertyBinding(entries, first));
    barFrame.addSeriesBinding(
        "Second",
        IndexedBinding.propertyBinding(entries, e -> ColorUtils.lighten(e.color)),
        IndexedBinding.propertyBinding(
            entries,
            e ->
                second.apply(e).doubleValue()
                    - (differentDirections.test(e) ? 0 : first.apply(e).doubleValue())));
    barFrame.setLeftIconBinding(IndexedBinding.propertyBinding(entries, e -> e.shape));
    builder.bind(
        bars,
        map -> {
          if (map == null) {
            entries.clear();
            rangeFinder.setLowest(0);
            rangeFinder.setHighest(0);
          } else {
            entries.setAll(map);
            rangeFinder.setHighest(
                map.stream()
                    .flatMap(e -> Stream.of(e.value1, e.value2))
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::max));
            rangeFinder.setLowest(
                map.stream()
                    .flatMap(e -> Stream.of(e.value1, e.value2))
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::min));
          }
        });
    return builder;
  }

  public static BarFrameBuilder dualReversed(Binding<? extends List<DualBar>> bars) {
    BarFrameBuilder builder = new BarFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    RangeFinder rangeFinder = builder.rangeFinder;

    Predicate<DualBar> differentDirections =
        bar -> Math.signum(bar.value1.doubleValue()) * Math.signum(bar.value2.doubleValue()) == -1;
    Predicate<DualBar> reverse =
        bar ->
            differentDirections.test(bar)
                || Math.abs(bar.value1.doubleValue()) < Math.abs(bar.value2.doubleValue());
    Function<DualBar, Number> first = bar -> reverse.test(bar) ? bar.value1 : bar.value2;
    Function<DualBar, Number> second = bar -> reverse.test(bar) ? bar.value2 : bar.value1;

    BindableList<DualBar> entries = new BindableList<>();
    barFrame.setNumBarsBinding(Binding.sizeBinding(entries));
    barFrame.setLeftTextBinding(IndexedBinding.propertyBinding(entries, e -> e.label));
    barFrame.setRightTextBinding(IndexedBinding.propertyBinding(entries, e -> e.valueLabel));
    barFrame.setMinBinding(
        Binding.propertyBinding(
            rangeFinder, rf -> rf.minFunction.apply(rf), RangeFinder.Property.MIN));
    barFrame.setMaxBinding(
        Binding.propertyBinding(
            rangeFinder, rf -> rf.maxFunction.apply(rf), RangeFinder.Property.MAX));
    barFrame.addSeriesBinding(
        "Placeholder",
        IndexedBinding.propertyBinding(entries, e -> e.color),
        IndexedBinding.propertyBinding(entries, e -> 0));
    barFrame.addSeriesBinding(
        "First",
        IndexedBinding.propertyBinding(entries, e -> ColorUtils.lighten(e.color)),
        IndexedBinding.propertyBinding(entries, first));
    barFrame.addSeriesBinding(
        "Second",
        IndexedBinding.propertyBinding(
            entries,
            e -> {
              UnaryOperator<Color> cf =
                  differentDirections.test(e) ? ColorUtils::lighten : UnaryOperator.identity();
              return cf.apply(e.color);
            }),
        IndexedBinding.propertyBinding(
            entries,
            e ->
                second.apply(e).doubleValue()
                    - (differentDirections.test(e) ? 0 : first.apply(e).doubleValue())));
    barFrame.setLeftIconBinding(IndexedBinding.propertyBinding(entries, e -> e.shape));
    builder.bind(
        bars,
        map -> {
          if (map == null) {
            entries.clear();
            rangeFinder.setLowest(0);
            rangeFinder.setHighest(0);
          } else {
            entries.setAll(map);
            rangeFinder.setHighest(
                map.stream()
                    .flatMap(e -> Stream.of(e.value1, e.value2))
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::max));
            rangeFinder.setLowest(
                map.stream()
                    .flatMap(e -> Stream.of(e.value1, e.value2))
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::min));
          }
        });
    return builder;
  }

  public BarFrameBuilder withHeader(Binding<String> headerBinding) {
    barFrame.setHeaderBinding(headerBinding);
    return this;
  }

  public BarFrameBuilder withSubhead(Binding<String> subheadBinding) {
    barFrame.setSubheadTextBinding(subheadBinding);
    return this;
  }

  public BarFrameBuilder withNotes(Binding<String> notesBinding) {
    barFrame.setNotesBinding(notesBinding);
    return this;
  }

  public BarFrameBuilder withBorder(Binding<Color> borderColorBinding) {
    barFrame.setBorderColorBinding(borderColorBinding);
    return this;
  }

  public BarFrameBuilder withSubheadColor(Binding<Color> subheadColorBinding) {
    barFrame.setSubheadColorBinding(subheadColorBinding);
    return this;
  }

  public BarFrameBuilder withMax(Binding<? extends Number> maxBinding) {
    rangeFinder.setMinFunction(rf -> 0);
    bind(
        maxBinding,
        max ->
            rangeFinder.setMaxFunction(
                rf -> Math.max(max.doubleValue(), rf.highest.doubleValue())));
    return this;
  }

  public BarFrameBuilder withWingspan(Binding<? extends Number> wingspanBinding) {
    bind(
        wingspanBinding,
        range -> {
          Function<RangeFinder, Double> f =
              rf ->
                  Math.max(
                      range.doubleValue(),
                      Math.max(
                          Math.abs(rf.lowest.doubleValue()), Math.abs(rf.highest.doubleValue())));
          rangeFinder.setMinFunction(rf -> -f.apply(rf));
          rangeFinder.setMaxFunction(rf -> +f.apply(rf));
        });
    return this;
  }

  public <T extends Number> BarFrameBuilder withTarget(
      Binding<T> targetBinding, Function<? super T, String> labelFunc) {
    bind(
        targetBinding,
        target -> {
          barFrame.setNumLinesBinding(Binding.fixedBinding(1));
          barFrame.setLineLevelsBinding(IndexedBinding.listBinding(target));
          barFrame.setLineLabelsBinding(IndexedBinding.listBinding(labelFunc.apply(target)));
        });
    return this;
  }

  public <T extends Number> BarFrameBuilder withLines(
      BindableList<T> lines, Function<? super T, String> labelFunc) {
    barFrame.setNumLinesBinding(Binding.sizeBinding(lines));
    barFrame.setLineLevelsBinding(IndexedBinding.propertyBinding(lines, Function.identity()));
    barFrame.setLineLabelsBinding(IndexedBinding.propertyBinding(lines, labelFunc::apply));
    return this;
  }

  public <T> BarFrameBuilder withLines(
      BindableList<T> lines,
      Function<? super T, String> labelFunc,
      Function<? super T, ? extends Number> valueFunc) {
    barFrame.setNumLinesBinding(Binding.sizeBinding(lines));
    barFrame.setLineLevelsBinding(IndexedBinding.propertyBinding(lines, valueFunc::apply));
    barFrame.setLineLabelsBinding(IndexedBinding.propertyBinding(lines, labelFunc::apply));
    return this;
  }

  public <T extends Number> BarFrameBuilder withLines(
      Binding<List<T>> linesBinding, Function<? super T, String> labelFunc) {
    bind(
        linesBinding,
        lines -> {
          barFrame.setNumLinesBinding(Binding.fixedBinding(lines.size()));
          barFrame.setLineLevelsBinding(IndexedBinding.listBinding(lines.toArray(new Number[0])));
          barFrame.setLineLabelsBinding(
              IndexedBinding.listBinding(lines.stream().map(labelFunc).toArray(String[]::new)));
        });
    return this;
  }

  private <T> void bind(Binding<T> binding, Consumer<T> onUpdate) {
    binding.bindLegacy(onUpdate);
    bindings.add(binding);
  }

  public BarFrame build() {
    return barFrame;
  }
}
