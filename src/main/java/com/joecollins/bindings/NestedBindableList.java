package com.joecollins.bindings;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NestedBindableList<T extends Bindable<E>, E extends Enum<E>> extends BindableList<T> {
  private Map<E, List<BiConsumer<Integer, T>>> itemBindings = new HashMap<>();
  private Map<BiConsumer<Integer, T>, Map<Integer, Consumer<T>>> storedBindings =
      new WeakHashMap<>();

  public void addNestedItemBinding(BiConsumer<Integer, T> binding, E... properties) {
    addItemBinding(binding);
    for (E property : properties)
      itemBindings.computeIfAbsent(property, x -> new LinkedList<>()).add(binding);
    for (int index = 0; index < size(); index++) {
      T item = get(index);
      if (item != null) {
        item.addBinding(getOrCreateBinding(binding, index), properties);
        for (E property : properties) item.onPropertyRefreshed(property);
      }
    }
  }

  public void removeNestedItemBinding(BiConsumer<Integer, T> binding, E... properties) {
    removeItemBinding(binding);
    for (E property : properties)
      itemBindings.computeIfAbsent(property, x -> new LinkedList<>()).remove(binding);
    for (int index = 0; index < size(); index++) {
      T item = get(index);
      if (item != null) {
        item.removeBinding(getOrCreateBinding(binding, index), properties);
      }
    }
  }

  @Override
  protected void addAllBindings(int index) {
    super.addAllBindings(index);
    T item = get(index);
    for (Map.Entry<E, List<BiConsumer<Integer, T>>> entry : itemBindings.entrySet()) {
      E property = entry.getKey();
      List<BiConsumer<Integer, T>> consumers = entry.getValue();
      for (BiConsumer<Integer, T> consumer : consumers) {
        if (item != null) {
          item.addBinding(getOrCreateBinding(consumer, index), property);
        }
      }
    }
  }

  @Override
  protected void removeAllBindings(int index) {
    super.removeAllBindings(index);
    T item = get(index);
    for (Map.Entry<E, List<BiConsumer<Integer, T>>> entry : itemBindings.entrySet()) {
      E property = entry.getKey();
      List<BiConsumer<Integer, T>> consumers = entry.getValue();
      for (BiConsumer<Integer, T> consumer : consumers) {
        if (item != null) {
          item.removeBinding(getOrCreateBinding(consumer, index), property);
        }
      }
    }
  }

  private Consumer<T> getOrCreateBinding(BiConsumer<Integer, T> consumer, int index) {
    return storedBindings
        .computeIfAbsent(consumer, x -> new HashMap<>())
        .computeIfAbsent(index, i -> (val -> consumer.accept(i, val)));
  }
}
