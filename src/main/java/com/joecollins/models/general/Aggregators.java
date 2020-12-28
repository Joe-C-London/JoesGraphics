package com.joecollins.models.general;

import com.joecollins.bindings.Binding;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

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
          if (r != null) r.forEach((k, v) -> a.compute(k, (k1, v1) -> (v1 == null ? 0 : v1) + v));
        },
        (a, r) -> {
          if (r != null)
            r.forEach(
                (k, v) ->
                    a.compute(
                        k,
                        (k1, v1) -> {
                          int ret = (v1 == null ? 0 : v1) - v;
                          return ret == 0 && !seededKeys.contains(k1) ? null : ret;
                        }));
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
    return Binding.mapReduceBinding(
        items.stream().map(result).collect(Collectors.toList()),
        new LinkedHashMap<>(identity),
        (a, r) -> {
          if (r != null)
            r.forEach(
                (k, v) ->
                    a.compute(
                        k,
                        (k1, v1) -> {
                          Pair<Integer, Integer> v2 = v1 == null ? ImmutablePair.of(0, 0) : v1;
                          return ImmutablePair.of(
                              v2.getLeft() + v.getLeft(), v2.getRight() + v.getRight());
                        }));
        },
        (a, r) -> {
          if (r != null)
            r.forEach(
                (k, v) ->
                    a.compute(
                        k,
                        (k1, v1) -> {
                          Pair<Integer, Integer> v2 = v1 == null ? ImmutablePair.of(0, 0) : v1;
                          ImmutablePair<Integer, Integer> ret =
                              ImmutablePair.of(
                                  v2.getLeft() - v.getLeft(), v2.getRight() - v.getRight());
                          return ret.getLeft() == 0
                                  && ret.getRight() == 0
                                  && !seededKeys.contains(k1)
                              ? null
                              : ret;
                        }));
        });
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
                e -> 1.0 * e.getValue() / total,
                Double::sum,
                LinkedHashMap::new));
  }

  @SafeVarargs
  public static <K> Binding<Map<K, Integer>> topAndOthers(
      Binding<Map<K, Integer>> result, int limit, K others, Binding<K>... mustInclude) {
    Binding<Set<K>> mustIncludeSet = Binding.fixedBinding(Set.of());
    for (Binding<K> mustIncludeEntry : mustInclude) {
      mustIncludeSet =
          mustIncludeSet.merge(
              mustIncludeEntry,
              (s, e) ->
                  Stream.concat(s.stream(), e == null ? Stream.empty() : Stream.of(e))
                      .collect(Collectors.toSet()));
    }
    return result.merge(
        mustIncludeSet, (m, w) -> topAndOthers(m, limit, others, (K[]) w.toArray()));
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
            .sorted(Map.Entry.<K, Integer>comparingByValue().reversed())
            .limit(Math.max(0, limit - 1 - mustIncludeSet.size()))
            .collect(Collectors.toSet());
    return map.entrySet().stream()
        .collect(
            Collectors.toMap(
                e -> top.contains(e) || mustIncludeSet.contains(e.getKey()) ? e.getKey() : others,
                Map.Entry::getValue,
                Integer::sum,
                LinkedHashMap::new));
  }

  public static <K, V> Binding<Map<K, V>> toMap(Set<K> keys, Function<K, Binding<V>> bindingFunc) {
    return new Binding<Map<K, V>>() {
      private List<Binding<V>> bindings = null;
      private Map<K, V> value = null;

      @Override
      public Map<K, V> getValue() {
        return keys.stream()
            .collect(Collectors.toMap(Function.identity(), k -> bindingFunc.apply(k).getValue()));
      }

      @Override
      public void bind(Consumer<Map<K, V>> onUpdate) {
        if (bindings != null) {
          throw new IllegalStateException("Binding is already used");
        }
        value = new HashMap<>();
        Map<K, Binding<V>> bindingsMap = new HashMap<>();
        for (K key : keys) {
          Binding<V> binding = bindingFunc.apply(key);
          bindingsMap.put(key, binding);
          binding.bind(
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
