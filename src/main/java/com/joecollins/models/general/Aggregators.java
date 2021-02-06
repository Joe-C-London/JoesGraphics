package com.joecollins.models.general;

import com.joecollins.bindings.Binding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

public class Aggregators {

  private Aggregators() {}

  public static <T, K> Binding<Map<K, Integer>> combine(
      Collection<T> items, Function<T, Binding<Map<K, Integer>>> result) {
    return combine(items, result, new HashMap<>());
  }

  public static <T, K> Binding<Map<K, Integer>> combine(
      Collection<T> items, Function<T, Binding<Map<K, Integer>>> result, Map<K, Integer> identity) {
    Set<K> seededKeys = new HashSet<>(identity.keySet());
    return Binding.mapReduceBinding(
        items.stream().map(result).collect(Collectors.toList()),
        new LinkedHashMap<>(identity),
        (a, r) -> {
          if (r == null) {
            return new LinkedHashMap<>(a);
          }
          return Stream.concat(a.entrySet().stream(), r.entrySet().stream())
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, Map.Entry::getValue, Integer::sum, LinkedHashMap::new));
        },
        (a, r) -> {
          if (r == null) {
            return new LinkedHashMap<>(a);
          }
          return Stream.concat(
                  a.entrySet().stream(),
                  r.entrySet().stream().map(e -> ImmutablePair.of(e.getKey(), -e.getValue())))
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, Map.Entry::getValue, Integer::sum, LinkedHashMap::new))
              .entrySet()
              .stream()
              .filter(
                  e ->
                      seededKeys.contains(e.getKey())
                          || e.getValue() != 0
                          || !r.containsKey(e.getKey()))
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, Map.Entry::getValue, Integer::sum, LinkedHashMap::new));
        });
  }

  public static <T, K> Binding<Map<K, Pair<Integer, Integer>>> combineDual(
      Collection<T> items, Function<T, Binding<Map<K, Pair<Integer, Integer>>>> result) {
    return combineDual(items, result, new HashMap<>());
  }

  public static <T, K> Binding<Map<K, Pair<Integer, Integer>>> combineDual(
      Collection<T> items,
      Function<T, Binding<Map<K, Pair<Integer, Integer>>>> result,
      Map<K, Pair<Integer, Integer>> identity) {
    Set<K> seededKeys = new HashSet<>(identity.keySet());
    BinaryOperator<Pair<Integer, Integer>> sum =
        (v1, v2) -> ImmutablePair.of(v1.getLeft() + v2.getLeft(), v1.getRight() + v2.getRight());
    return Binding.mapReduceBinding(
        items.stream().map(result).collect(Collectors.toList()),
        new LinkedHashMap<>(identity),
        (a, r) -> {
          if (r == null) {
            return new LinkedHashMap<>(a);
          }
          return Stream.concat(a.entrySet().stream(), r.entrySet().stream())
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, Map.Entry::getValue, sum, LinkedHashMap::new));
        },
        (a, r) -> {
          if (r == null) {
            return new LinkedHashMap<>(a);
          }
          return Stream.concat(
                  a.entrySet().stream(),
                  r.entrySet().stream()
                      .map(
                          e ->
                              new ImmutablePair<>(
                                  e.getKey(),
                                  ImmutablePair.of(
                                      -e.getValue().getLeft(), -e.getValue().getRight()))))
              .collect(
                  Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, sum, LinkedHashMap::new))
              .entrySet()
              .stream()
              .filter(
                  e ->
                      seededKeys.contains(e.getKey())
                          || e.getValue().getLeft() != 0
                          || e.getValue().getRight() != 0
                          || !r.containsKey(e.getKey()))
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, Map.Entry::getValue, sum, LinkedHashMap::new));
        });
  }

  public static <T> Binding<Integer> sum(Collection<T> items, Function<T, Binding<Integer>> val) {
    return Binding.mapReduceBinding(
        items.stream().map(val).collect(Collectors.toList()), 0, (t, v) -> t + v, (t, v) -> t - v);
  }

  public static <K> Binding<Map<K, Integer>> adjustForPctReporting(
      Binding<Map<K, Integer>> result, Binding<Double> pctReporting) {
    return result.merge(
        pctReporting,
        (r, p) -> {
          LinkedHashMap<K, Integer> ret = new LinkedHashMap<>();
          r.forEach((k, v) -> ret.put(k, (int) (v * p)));
          return ret;
        });
  }

  public static <T> Binding<Double> combinePctReporting(
      Collection<T> items, Function<T, Binding<Double>> pctReportingFunc) {
    return combinePctReporting(items, pctReportingFunc, t -> 1);
  }

  public static <T> Binding<Double> combinePctReporting(
      Collection<T> items,
      Function<T, Binding<Double>> pctReportingFunc,
      ToDoubleFunction<T> weightFunc) {
    double totalWeight = items.stream().mapToDouble(weightFunc).sum();
    return Binding.mapReduceBinding(
        items.stream()
            .map(
                t -> {
                  double weight = weightFunc.applyAsDouble(t);
                  return pctReportingFunc.apply(t).map(x -> x * weight);
                })
            .collect(Collectors.toList()),
        0.0,
        (a, p) -> a + (p / totalWeight),
        (a, p) -> a - (p / totalWeight));
  }

  public static <K1, K2> Binding<Map<K2, Integer>> adjustKey(
      Binding<Map<K1, Integer>> result, Function<K1, K2> func) {
    return result.map(m -> adjustKey(m, func));
  }

  public static <K1, K2> Map<K2, Integer> adjustKey(Map<K1, Integer> map, Function<K1, K2> func) {
    LinkedHashMap<K2, Integer> ret = new LinkedHashMap<>();
    map.forEach((k, v) -> ret.merge(func.apply(k), v, Integer::sum));
    return ret;
  }

  public static <K> Binding<Map<K, Double>> toPct(Binding<Map<K, Integer>> mapBinding) {
    return mapBinding.map(Aggregators::toPct);
  }

  public static <K> Map<K, Double> toPct(Map<K, Integer> map) {
    int total = map.values().stream().mapToInt(i -> i).sum();
    return map.entrySet().stream()
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> total == 0 ? 0 : 1.0 * e.getValue() / total,
                Double::sum,
                LinkedHashMap::new));
  }

  public static <K> Binding<Map<K, Integer>> topAndOthers(
      Binding<? extends Map<K, Integer>> result, int limit, K others) {
    return topAndOthers(result, limit, others, Binding.fixedBinding((K[]) new Object[0]));
  }

  public static <K> Binding<Map<K, Integer>> topAndOthers(
      Binding<? extends Map<K, Integer>> result, int limit, K others, Binding<K[]> mustInclude) {
    return result.merge(mustInclude, (m, w) -> topAndOthers(m, limit, others, w));
  }

  @SafeVarargs
  public static <K> Map<K, Integer> topAndOthers(
      Map<K, Integer> map, int limit, K others, K... mustInclude) {
    if (map.size() <= limit) {
      return map;
    }
    Set<K> mustIncludeSet =
        Arrays.stream(mustInclude).filter(Objects::nonNull).collect(Collectors.toSet());
    Set<Map.Entry<K, Integer>> top =
        map.entrySet().stream()
            .filter(e -> !mustIncludeSet.contains(e.getKey()))
            .sorted(
                Map.Entry.<K, Integer>comparingByValue(
                        Comparator.comparingInt(i -> i == null ? -1 : i))
                    .reversed())
            .limit(Math.max(0, limit - 1 - mustIncludeSet.size()))
            .collect(Collectors.toSet());
    Predicate<Map.Entry<K, Integer>> topAndRequired =
        e -> top.contains(e) || mustIncludeSet.contains(e.getKey());
    LinkedHashMap<K, Integer> result = new LinkedHashMap<>();
    boolean needOthers = false;
    for (Map.Entry<K, Integer> e : map.entrySet()) {
      if (topAndRequired.test(e) || e.getValue() != null) {
        var key = topAndRequired.test(e) ? e.getKey() : others;
        if (result.containsKey(key)) {
          result.merge(key, e.getValue(), Integer::sum);
        } else {
          result.put(key, e.getValue());
        }
      } else {
        needOthers = true;
      }
    }
    if (needOthers) {
      result.put(others, null);
    }
    return result;
  }

  public static <K, V> Binding<Map<K, V>> toMap(
      Collection<K> keys, Function<K, Binding<V>> bindingFunc) {
    return toMap(keys, Function.identity(), bindingFunc);
  }

  public static <T, K, V> Binding<Map<K, V>> toMap(
      Collection<T> entries, final Function<T, K> keyFunc, Function<T, Binding<V>> bindingFunc) {
    return new Binding<>() {
      private List<Binding<V>> bindings = null;
      private Map<K, V> value = null;

      @Override
      public Map<K, V> getValue() {
        return entries.stream()
            .collect(Collectors.toMap(keyFunc, k -> bindingFunc.apply(k).getValue()));
      }

      @Override
      public void bind(@NotNull Function1<? super Map<K, V>, Unit> onUpdate) {
        bindLegacy((Consumer<Map<K, V>>) onUpdate::invoke);
      }

      @Override
      public void bindLegacy(Consumer<Map<K, V>> onUpdate) {
        if (bindings != null) {
          throw new IllegalStateException("Binding is already used");
        }
        value = new HashMap<>();
        Map<K, Binding<V>> bindingsMap = new HashMap<>();
        for (T entry : entries) {
          K key = keyFunc.apply(entry);
          Binding<V> binding = bindingFunc.apply(entry);
          bindingsMap.put(key, binding);
          binding.bindLegacy(
              val -> {
                value.put(key, val);
                if (bindings != null) {
                  onUpdate.accept(value);
                }
              });
        }
        onUpdate.accept(value);
        bindings = new ArrayList<>(bindingsMap.values());
      }

      @Override
      public void unbind() {
        if (bindings != null) {
          bindings.forEach(Binding::unbind);
          bindings = null;
          value = null;
        }
      }
    };
  }
}
