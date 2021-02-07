package com.joecollins.bindings;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Bindable<T extends Bindable<T, E>, E extends Enum<E>> {

  private Map<E, List<Consumer<T>>> bindings = new ConcurrentHashMap<>();

  public void addBinding(Consumer<T> binding, E... properties) {
    for (E property : properties) bindings.computeIfAbsent(property, createNewList()).add(binding);
  }

  private Function<E, List<Consumer<T>>> createNewList() {
    return x -> new CopyOnWriteArrayList<>();
  }

  public void removeBinding(Consumer<T> binding, E... properties) {
    for (E property : properties)
      bindings.computeIfAbsent(property, createNewList()).remove(binding);
  }

  @SuppressWarnings("unchecked")
  protected void onPropertyRefreshed(E property) {
    bindings
        .getOrDefault(property, Collections.emptyList())
        .forEach(binding -> binding.accept((T) this));
  }
}
