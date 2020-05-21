package com.joecollins.graphics.screens.generic;

import com.joecollins.bindings.Binding;
import com.joecollins.bindings.Binding.BindingReceiver;
import com.joecollins.graphics.components.MapFrame;
import com.joecollins.graphics.components.MapFrameBuilder;
import com.joecollins.models.general.Party;
import java.awt.Color;
import java.awt.Shape;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class MapBuilder {
  private final BindingReceiver<List<Pair<Shape, Color>>> winners;
  private final BindingReceiver<List<Shape>> mapFocus;
  private final BindingReceiver<String> mapHeader;

  public <T> MapBuilder(
      Binding<Map<T, Shape>> shapes,
      Binding<Map<T, Result>> winners,
      Binding<List<Shape>> focus,
      Binding<String> headerBinding) {
    Binding<List<ImmutablePair<Shape, Result>>> shapesToParties =
        shapes.merge(
            winners,
            (s, w) ->
                s.entrySet().stream()
                    .map(
                        e -> {
                          Result winnerParty = w.get(e.getKey());
                          return ImmutablePair.of(e.getValue(), winnerParty);
                        })
                    .collect(Collectors.toList()));
    this.mapFocus = new BindingReceiver<>(focus);
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
      Binding<List<Shape>> focus,
      Binding<String> header) {
    Binding<ImmutablePair<T, Result>> leaderWithShape =
        selectedShape.merge(leadingParty, ImmutablePair::new);
    mapFocus = new BindingReceiver<>(focus);
    mapHeader = new BindingReceiver<>(header);
    Binding<List<Pair<Shape, Color>>> shapeWinners =
        shapes.merge(
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
    Binding<List<Pair<Shape, Color>>> focusedShapeWinners =
        shapeWinners.merge(
            mapFocus.getBinding(),
            (sw, f) -> {
              if (f == null) return sw;
              return sw.stream()
                  .map(
                      e -> {
                        if (f.contains(e.getLeft())) return e;
                        return ImmutablePair.of(e.getLeft(), new Color(220, 220, 220));
                      })
                  .collect(Collectors.toList());
            });
    winners = new BindingReceiver<>(focusedShapeWinners);
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
    Color color;
    if (winner != null) {
      color = winner.getColor();
    } else {
      if (focus == null || focus.isEmpty() || focus.contains(shape)) {
        color = Color.LIGHT_GRAY;
      } else {
        color = new Color(220, 220, 220);
      }
    }
    return color;
  }

  public static class Result {
    private final Party party;
    private final boolean elected;

    private Result(Party party, boolean elected) {
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
      return brighter(party.getColor());
    }

    private static Color brighter(Color color) {
      return new Color(
          (3 * color.getRed() + 255) / 4,
          (3 * color.getGreen() + 255) / 4,
          (3 * color.getBlue() + 255) / 4,
          color.getAlpha());
    }
  }
}
