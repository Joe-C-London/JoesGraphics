package com.joecollins.graphics.components;

import com.joecollins.bindings.BindableList;
import com.joecollins.bindings.Binding;
import com.joecollins.bindings.IndexedBinding;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.tuple.Pair;

public class MultiSummaryFrameBuilder {

  private MultiSummaryFrame frame = new MultiSummaryFrame();

  public static <T> MultiSummaryFrameBuilder tooClose(
      List<T> items,
      Function<T, Binding<Boolean>> display,
      Function<T, Binding<? extends Number>> sortFunc,
      Function<T, Binding<String>> rowHeaderFunc,
      Function<T, Binding<List<Pair<Color, String>>>> rowLabelsFunc,
      int limit) {

    class Row {
      boolean display;
      double sort;
      String rowHeader;
      List<Pair<Color, String>> rowLabels;
    }

    List<Row> allRows = new ArrayList<>();
    BindableList<Row> displayedRows = new BindableList<>();

    MutableBoolean isReady = new MutableBoolean(false);
    Runnable update =
        () -> {
          if (isReady.booleanValue())
            displayedRows.setAll(
                allRows.stream()
                    .filter(r -> r.display)
                    .sorted(Comparator.comparing(r -> r.sort))
                    .limit(limit)
                    .collect(Collectors.toList()));
        };

    for (T item : items) {
      Row row = new Row();
      display
          .apply(item)
          .bind(
              d -> {
                row.display = d;
                update.run();
              });
      sortFunc
          .apply(item)
          .bind(
              s -> {
                row.sort = s.doubleValue();
                update.run();
              });
      rowHeaderFunc
          .apply(item)
          .bind(
              h -> {
                row.rowHeader = h;
                update.run();
              });
      rowLabelsFunc
          .apply(item)
          .bind(
              l -> {
                row.rowLabels = l;
                update.run();
              });
      allRows.add(row);
    }

    isReady.setTrue();
    update.run();
    MultiSummaryFrameBuilder builder = new MultiSummaryFrameBuilder();
    builder.frame.setNumRowsBinding(Binding.sizeBinding(displayedRows));
    builder.frame.setRowHeaderBinding(
        IndexedBinding.propertyBinding(displayedRows, t -> t.rowHeader));
    builder.frame.setValuesBinding(IndexedBinding.propertyBinding(displayedRows, t -> t.rowLabels));
    return builder;
  }

  public MultiSummaryFrameBuilder withHeader(Binding<String> header) {
    frame.setHeaderBinding(header);
    return this;
  }

  public MultiSummaryFrame build() {
    return frame;
  }
}
