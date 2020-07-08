package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.BindingReceiver;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.components.MapFrameBuilder;
import com.joecollins.graphics.utils.ColorUtils;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Shape;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class MapBuilder {

  private final BindingReceiver<List<Pair<Shape, Color>>> winners;
  private final BindingReceiver<List<Shape>> mapFocus;
  private final BindingReceiver<String> mapHeader;

  public <T> MapBuilder(
      Binding<Map<T, Shape>> shapes,
      Binding<Map<T, Result>> winners,
      Binding<List<T>> focus,
      Binding<String> headerBinding) {
    BindingReceiver<Map<T, Shape>> shapesReceiver = new BindingReceiver<>(shapes);
    Binding<List<ImmutablePair<Shape, Result>>> shapesToParties =
        shapesReceiver
            .getBinding()
            .merge(
                winners,
                (s, w) ->
                    s.entrySet().stream()
                        .map(
                            e -> {
                              Result winnerParty = w.get(e.getKey());
                              return ImmutablePair.of(e.getValue(), winnerParty);
                            })
                        .collect(Collectors.toList()));
    this.mapFocus =
        new BindingReceiver<>(shapesReceiver.getBinding().merge(focus, this::createFocusShapes));
    this.winners =
        new BindingReceiver<>(
            shapesToParties.merge(
                mapFocus.getBinding(),
                (r, f) ->
                    r.stream()
                        .map(p -> ImmutablePair.of(p.left, extractColor(f, p.left, p.right)))
                        .collect(Collectors.toList())));
    this.mapHeader = new BindingReceiver<>(headerBinding);
  }

  public <T> MapBuilder(
      Binding<Map<T, Shape>> shapes,
      Binding<T> selectedShape,
      Binding<Result> leadingParty,
      Binding<List<T>> focus,
      Binding<String> header) {
    this(shapes, selectedShape, leadingParty, focus, () -> null, header);
  }

  public <T> MapBuilder(
      Binding<Map<T, Shape>> shapes,
      Binding<T> selectedShape,
      Binding<Result> leadingParty,
      Binding<List<T>> focus,
      Binding<List<T>> additionalHighlight,
      Binding<String> header) {
    BindingReceiver<Map<T, Shape>> shapesReceiver = new BindingReceiver<>(shapes);
    Binding<ImmutablePair<T, Result>> leaderWithShape =
        selectedShape.merge(leadingParty, ImmutablePair::new);
    mapFocus =
        new BindingReceiver<>(shapesReceiver.getBinding().merge(focus, this::createFocusShapes));
    Binding<List<Shape>> additionalFocusShapes =
        shapesReceiver.getBinding().merge(additionalHighlight, this::createFocusShapes);
    mapHeader = new BindingReceiver<>(header);
    Binding<List<Pair<Shape, Color>>> shapeWinners =
        shapesReceiver
            .getBinding()
            .merge(
                leaderWithShape,
                (shp, ldr) ->
                    shp.entrySet().stream()
                        .map(
                            e -> {
                              Color color;
                              if (e.getKey().equals(ldr.left)) {
                                color =
                                    Optional.ofNullable(ldr.right)
                                        .map(Result::getColor)
                                        .orElse(Color.BLACK);
                              } else {
                                color = Color.LIGHT_GRAY;
                              }
                              return ImmutablePair.of(e.getValue(), color);
                            })
                        .collect(Collectors.toList()));
    Binding<List<Shape>> allFocusShapes =
        mapFocus
            .getBinding()
            .merge(
                additionalFocusShapes,
                (l1, l2) -> {
                  if (l1 == null) return l2;
                  if (l2 == null) return l1;
                  return Stream.concat(l1.stream(), l2.stream())
                      .distinct()
                      .collect(Collectors.toList());
                });
    Binding<List<Pair<Shape, Color>>> focusedShapeWinners =
        shapeWinners.merge(
            allFocusShapes,
            (sw, f) -> {
              if (f == null) {
                return sw;
              }
              return sw.stream()
                  .map(
                      e -> {
                        if (f.contains(e.getLeft())) {
                          return e;
                        }
                        return ImmutablePair.of(e.getLeft(), new Color(220, 220, 220));
                      })
                  .collect(Collectors.toList());
            });
    winners = new BindingReceiver<>(focusedShapeWinners);
  }

  private <T> List<Shape> createFocusShapes(Map<T, Shape> shapes, List<T> focus) {
    return focus == null
        ? null
        : focus.stream().filter(shapes::containsKey).map(shapes::get).collect(Collectors.toList());
  }

  public MapFrame createMapFrame() {
    if (mapHeader == null) {
      return null;
    }
    return MapFrameBuilder.from(winners.getBinding())
        .withFocus(mapFocus.getBinding())
        .withHeader(mapHeader.getBinding())
        .build();
  }

  private static Color extractColor(List<Shape> focus, Shape shape, Result winner) {
    if (winner != null) {
      return winner.getColor();
    } else if (focus == null || focus.isEmpty() || focus.contains(shape)) {
      return Color.LIGHT_GRAY;
    } else {
      return new Color(220, 220, 220);
    }
  }

  public static class Result {

    private final Party party;
    private final boolean elected;

    public Result(Party party, boolean elected) {
      this.party = party;
      this.elected = elected;
    }

    public static Result elected(Party party) {
      return new Result(party, true);
    }

    public static Result leading(Party party) {
      return new Result(party, false);
    }

    public Color getColor() {
      if (party == null) {
        return Color.BLACK;
      }
      if (elected) {
        return party.getColor();
      }
      return ColorUtils.lighten(party.getColor());
    }
  }
}
