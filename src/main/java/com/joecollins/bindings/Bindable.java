package com.joecollins.bindings;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Bindable {

  @SuppressWarnings("rawtypes")
  private Map<Enum<?>, List<Consumer>> bindings = new ConcurrentHashMap<>();

  public void addBinding(Consumer<?> binding, Enum<?>... properties) {
    for (Enum<?> property : properties)
      bindings.computeIfAbsent(property, createNewList()).add(binding);
  }

  @SuppressWarnings("rawtypes")
  private Function<Enum<?>, List<Consumer>> createNewList() {
    return x -> new CopyOnWriteArrayList<>();
  }

  public void removeBinding(Consumer<?> binding, Enum<?>... properties) {
    for (Enum<?> property : properties)
      bindings.computeIfAbsent(property, createNewList()).remove(binding);
  }

  protected void onPropertyRefreshed(Enum<?> property) {
    bindings
        .getOrDefault(property, Collections.emptyList())
        .forEach(binding -> binding.accept(this));
  }
}
