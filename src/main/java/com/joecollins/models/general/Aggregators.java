package com.joecollins.models.general;

import com.joecollins.bindings.Binding;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
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

  public static <K1, K2> Binding<Map<K2, Integer>> adjustKey(
      Binding<Map<K1, Integer>> result, Function<K1, K2> func) {
    return result.map(m -> adjustKey(m, func));
  }

  public static <K1, K2> Map<K2, Integer> adjustKey(Map<K1, Integer> map, Function<K1, K2> func) {
    LinkedHashMap<K2, Integer> ret = new LinkedHashMap<>();
    map.forEach((k, v) -> ret.merge(func.apply(k), v, Integer::sum));
    return ret;
  }
}
