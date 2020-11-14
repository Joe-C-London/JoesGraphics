package com.joecollins.bindings;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Bindable<E extends Enum<E>> {

  @SuppressWarnings("rawtypes")
  private Map<E, List<Consumer>> bindings = new ConcurrentHashMap<>();

  public void addBinding(Consumer<?> binding, E... properties) {
    for (E property : properties) bindings.computeIfAbsent(property, createNewList()).add(binding);
  }

  @SuppressWarnings("rawtypes")
  private Function<E, List<Consumer>> createNewList() {
    return x -> new CopyOnWriteArrayList<>();
  }

  public void removeBinding(Consumer<?> binding, E... properties) {
    for (E property : properties)
      bindings.computeIfAbsent(property, createNewList()).remove(binding);
  }

  protected void onPropertyRefreshed(E property) {
    bindings
        .getOrDefault(property, Collections.emptyList())
        .forEach(binding -> binding.accept(this));
  }
}
