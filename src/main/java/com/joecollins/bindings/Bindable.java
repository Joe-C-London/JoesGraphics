package com.joecollins.bindings;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Bindable {

  @SuppressWarnings("rawtypes")
  private Map<Enum<?>, List<Consumer>> bindings = new HashMap<>();

  public void addBinding(Consumer<?> binding, Enum<?>... properties) {
    for (Enum<?> property : properties)
      bindings.computeIfAbsent(property, x -> new LinkedList<>()).add(binding);
  }

  public void removeBinding(Consumer<?> binding, Enum<?>... properties) {
    for (Enum<?> property : properties)
      bindings.computeIfAbsent(property, x -> new LinkedList<>()).remove(binding);
  }

  protected void onPropertyRefreshed(Enum<?> property) {
    bindings
        .getOrDefault(property, Collections.emptyList())
        .forEach(binding -> binding.accept(this));
  }
}
