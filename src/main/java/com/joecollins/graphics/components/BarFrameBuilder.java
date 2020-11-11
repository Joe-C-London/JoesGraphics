package com.joecollins.graphics.components;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import com.joecollins.graphics.utils.ColorUtils;
import java.awt.Color;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

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

  private static class RangeFinder extends Bindable {

    enum Property {
      MIN,
      MAX
    };

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

  public static <T> BarFrameBuilder basicList(
      Binding<? extends List<? extends T>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super T, ? extends Number> valueFunc,
      Function<? super T, String> valueLabelFunc) {
    return basicList(binding, labelFunc, colorFunc, valueFunc, valueLabelFunc, t -> null);
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

    public BasicBar(String label, Color color, Number value, String valueLabel, Shape shape) {
      this.label = label;
      this.color = color;
      this.value = value;
      this.valueLabel = valueLabel;
      this.shape = shape;
    }
  }

  public static BarFrameBuilder basic(Binding<? extends List<BasicBar>> binding) {
    return basicList(
        binding, b -> b.label, b -> b.color, b -> b.value, b -> b.valueLabel, b -> b.shape);
  }

  @Deprecated
  public static <T> BarFrameBuilder basicList(
      Binding<? extends List<? extends T>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super T, ? extends Number> valueFunc,
      Function<? super T, String> valueLabelFunc,
      Function<? super T, Shape> shapeFunc) {
    BarFrameBuilder builder = new BarFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    RangeFinder rangeFinder = builder.rangeFinder;

    class BarEntry {
      final String label;
      final Color color;
      final Number value;
      final String valueLabel;
      final Shape shape;

      BarEntry(String label, Color color, Number value, String valueLabel, Shape shape) {
        this.label = label;
        this.color = color;
        this.value = value;
        this.valueLabel = valueLabel;
        this.shape = shape;
      }
    }

    BindableList<BarEntry> entries = new BindableList<>();
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
            entries.setAll(
                map.stream()
                    .map(
                        e ->
                            new BarEntry(
                                labelFunc.apply(e),
                                colorFunc.apply(e),
                                valueFunc.apply(e),
                                valueLabelFunc.apply(e),
                                shapeFunc.apply(e)))
                    .collect(Collectors.toList()));
            rangeFinder.setHighest(
                map.stream().map(valueFunc).mapToDouble(Number::doubleValue).reduce(0, Math::max));
            rangeFinder.setLowest(
                map.stream().map(valueFunc).mapToDouble(Number::doubleValue).reduce(0, Math::min));
          }
        });
    return builder;
  }

  @Deprecated
  public static <T> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends Number>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc) {
    return basic(binding, labelFunc, colorFunc, Number::toString);
  }

  @Deprecated
  public static <T> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends Number>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super Number, String> valueLabelFunc) {
    return basic(binding, labelFunc, colorFunc, Function.identity(), valueLabelFunc);
  }

  @Deprecated
  public static <T, U> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Number> valueFunc,
      Function<? super U, String> valueLabelFunc) {
    return basic(
        binding, labelFunc, colorFunc, valueFunc, valueLabelFunc, (t, u) -> valueFunc.apply(u));
  }

  @Deprecated
  public static <T, U> BarFrameBuilder basic(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Number> valueFunc,
      Function<? super U, String> valueLabelFunc,
      BiFunction<? super T, ? super U, ? extends Number> sortFunc) {
    return basicWithShapes(
        binding, labelFunc, colorFunc, valueFunc, valueLabelFunc, (x, y) -> null, sortFunc);
  }

  @Deprecated
  public static <T, U extends Number> BarFrameBuilder basicWithShapes(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, String> valueLabelFunc,
      BiFunction<? super T, ? super U, ? extends Shape> shapeFunc) {
    return basicWithShapes(
        binding, labelFunc, colorFunc, Function.identity(), valueLabelFunc, shapeFunc);
  }

  @Deprecated
  public static <T, U> BarFrameBuilder basicWithShapes(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Number> valueFunc,
      Function<? super U, String> valueLabelFunc,
      BiFunction<? super T, ? super U, ? extends Shape> shapeFunc) {
    return basicWithShapes(
        binding,
        labelFunc,
        colorFunc,
        valueFunc,
        valueLabelFunc,
        shapeFunc,
        (t, u) -> valueFunc.apply(u));
  }

  @Deprecated
  public static <T, U> BarFrameBuilder basicWithShapes(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Number> valueFunc,
      Function<? super U, String> valueLabelFunc,
      BiFunction<? super T, ? super U, ? extends Shape> shapeFunc,
      BiFunction<? super T, ? super U, ? extends Number> sortFunc) {
    BarFrameBuilder builder = new BarFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    RangeFinder rangeFinder = builder.rangeFinder;

    class BarEntry {
      final String label;
      final Color color;
      final Number value;
      final String valueLabel;
      final Shape shape;

      BarEntry(String label, Color color, Number value, String valueLabel, Shape shape) {
        this.label = label;
        this.color = color;
        this.value = value;
        this.valueLabel = valueLabel;
        this.shape = shape;
      }
    }

    BindableList<BarEntry> entries = new BindableList<>();
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
            entries.setAll(
                map.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<? extends T, ? extends U>>comparingDouble(
                                e -> sortFunc.apply(e.getKey(), e.getValue()).doubleValue())
                            .reversed())
                    .map(
                        e ->
                            new BarEntry(
                                labelFunc.apply(e.getKey()),
                                colorFunc.apply(e.getKey()),
                                valueFunc.apply(e.getValue()),
                                valueLabelFunc.apply(e.getValue()),
                                shapeFunc.apply(e.getKey(), e.getValue())))
                    .collect(Collectors.toList()));
            rangeFinder.setHighest(
                map.values().stream()
                    .map(valueFunc)
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::max));
            rangeFinder.setLowest(
                map.values().stream()
                    .map(valueFunc)
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::min));
          }
        });
    return builder;
  }

  @Deprecated
  public static <T> BarFrameBuilder dual(
      Binding<? extends Map<? extends T, ? extends Pair<? extends Number, ? extends Number>>>
          binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super Pair<? extends Number, ? extends Number>, String> valueLabelFunc,
      Function<Pair<? extends Number, ? extends Number>, Number> sortFunc) {
    return dual(binding, labelFunc, colorFunc, Function.identity(), valueLabelFunc, sortFunc);
  }

  @Deprecated
  public static <T, U> BarFrameBuilder dual(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Pair<? extends Number, ? extends Number>> valueFunc,
      Function<? super U, String> valueLabelFunc,
      Function<? super U, ? extends Number> sortFunc) {
    return dual(
        binding, labelFunc, colorFunc, valueFunc, valueLabelFunc, (k, v) -> sortFunc.apply(v));
  }

  @Deprecated
  public static <T, U> BarFrameBuilder dual(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Pair<? extends Number, ? extends Number>> valueFunc,
      Function<? super U, String> valueLabelFunc,
      BiFunction<? super T, ? super U, ? extends Number> sortFunc) {
    return dualWithShapes(
        binding, labelFunc, colorFunc, valueFunc, valueLabelFunc, (k, v) -> null, sortFunc);
  }

  @Deprecated
  public static <T, U> BarFrameBuilder dualWithShapes(
      Binding<? extends Map<? extends T, ? extends U>> binding,
      Function<? super T, String> labelFunc,
      Function<? super T, Color> colorFunc,
      Function<? super U, ? extends Pair<? extends Number, ? extends Number>> valueFunc,
      Function<? super U, String> valueLabelFunc,
      BiFunction<? super T, ? super U, ? extends Shape> shapeFunc,
      BiFunction<? super T, ? super U, ? extends Number> sortFunc) {
    BarFrameBuilder builder = new BarFrameBuilder();
    BarFrame barFrame = builder.barFrame;
    RangeFinder rangeFinder = builder.rangeFinder;

    class BarEntry {
      final String label;
      final Color color;
      final Number value1;
      final Number value2;
      final String valueLabel;
      final Shape shape;

      BarEntry(
          String label, Color color, Number value1, Number value2, String valueLabel, Shape shape) {
        this.label = label;
        this.color = color;
        this.value1 = value1;
        this.value2 = value2;
        this.valueLabel = valueLabel;
        this.shape = shape;
      }

      boolean differentDirections() {
        return Math.signum(value1.doubleValue()) * Math.signum(value2.doubleValue()) == -1;
      }

      Number first() {
        if (differentDirections()
            || Math.abs(value1.doubleValue()) < Math.abs(value2.doubleValue())) {
          return value1;
        } else {
          return value2;
        }
      }

      Number second() {
        if (differentDirections()
            || Math.abs(value1.doubleValue()) < Math.abs(value2.doubleValue())) {
          return value2;
        } else {
          return value1;
        }
      }
    }

    BindableList<BarEntry> entries = new BindableList<>();
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
                  e.differentDirections() ? ColorUtils::lighten : UnaryOperator.identity();
              return cf.apply(e.color);
            }),
        IndexedBinding.propertyBinding(entries, BarEntry::first));
    barFrame.addSeriesBinding(
        "Second",
        IndexedBinding.propertyBinding(entries, e -> ColorUtils.lighten(e.color)),
        IndexedBinding.propertyBinding(
            entries,
            e ->
                e.second().doubleValue()
                    - (e.differentDirections() ? 0 : e.first().doubleValue())));
    barFrame.setLeftIconBinding(IndexedBinding.propertyBinding(entries, e -> e.shape));
    builder.bind(
        binding,
        map -> {
          if (map == null) {
            entries.clear();
            rangeFinder.setLowest(0);
            rangeFinder.setHighest(0);
          } else {
            entries.setAll(
                map.entrySet().stream()
                    .sorted(
                        Comparator.<Map.Entry<? extends T, ? extends U>>comparingDouble(
                                e -> sortFunc.apply(e.getKey(), e.getValue()).doubleValue())
                            .reversed())
                    .map(
                        e -> {
                          Pair<? extends Number, ? extends Number> values =
                              valueFunc.apply(e.getValue());
                          return new BarEntry(
                              labelFunc.apply(e.getKey()),
                              colorFunc.apply(e.getKey()),
                              values.getLeft(),
                              values.getRight(),
                              valueLabelFunc.apply(e.getValue()),
                              shapeFunc.apply(e.getKey(), e.getValue()));
                        })
                    .collect(Collectors.toList()));
            rangeFinder.setHighest(
                map.values().stream()
                    .map(valueFunc)
                    .flatMap(e -> Stream.of(e.getLeft(), e.getRight()))
                    .mapToDouble(Number::doubleValue)
                    .reduce(0, Math::max));
            rangeFinder.setLowest(
                map.values().stream()
                    .map(valueFunc)
                    .flatMap(e -> Stream.of(e.getLeft(), e.getRight()))
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
    binding.bind(onUpdate);
    bindings.add(binding);
  }

  public BarFrame build() {
    return barFrame;
  }
}
