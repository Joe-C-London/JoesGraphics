package com.joecollins.bindings;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class BindableList<T extends Bindable> extends AbstractList<T> {

  private ArrayList<T> underlying = new ArrayList<>();

  private List<IntConsumer> sizeBindings = new LinkedList<>();

  @SuppressWarnings("rawtypes")
  private Map<String, List<BiConsumer>> itemBindings = new HashMap<>();

  private Map<BiConsumer, Map<Integer, Consumer>> storedBindings = new WeakHashMap<>();

  public void addSizeBinding(IntConsumer binding) {
    sizeBindings.add(binding);
  }

  public void removeSizeBinding(IntConsumer binding) {
    sizeBindings.remove(binding);
  }

  public void addItemBinding(BiConsumer<Integer, ?> binding, String... properties) {
    for (String property : properties)
      itemBindings.computeIfAbsent(property, x -> new LinkedList<>()).add(binding);
    for (int index = 0; index < size(); index++) {
      get(index).addBinding(getOrCreateBinding(binding, index), properties);
      for (String property : properties) get(index).onPropertyRefreshed(property);
    }
  }

  public void removeItemBinding(BiConsumer<Integer, ?> binding, String... properties) {
    for (String property : properties)
      itemBindings.computeIfAbsent(property, x -> new LinkedList<>()).remove(binding);
    for (int index = 0; index < size(); index++) {
      get(index).removeBinding(getOrCreateBinding(binding, index), properties);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  private Consumer getOrCreateBinding(BiConsumer consumer, int index) {
    return storedBindings
        .computeIfAbsent(consumer, x -> new HashMap<>())
        .computeIfAbsent(index, i -> (val -> consumer.accept(i, val)));
  }

  private void onSizeUpdated() {
    this.sizeBindings.forEach(binding -> binding.accept(size()));
  }

  @Override
  public T get(int index) {
    return underlying.get(index);
  }

  @Override
  public int size() {
    return underlying.size();
  }

  @Override
  public T set(int index, T element) {
    removeAllBindings(index);
    T ret = underlying.set(index, element);
    addAllBindings(index);
    return ret;
  }

  @Override
  public void add(int index, T element) {
    for (int i = index; i < size(); i++) {
      removeAllBindings(i);
    }
    underlying.add(index, element);
    onSizeUpdated();
    for (int i = index; i < size(); i++) {
      addAllBindings(i);
    }
  }

  @Override
  public T remove(int index) {
    for (int i = index; i < size(); i++) {
      removeAllBindings(i);
    }
    T ret = underlying.remove(index);
    onSizeUpdated();
    for (int i = index; i < size(); i++) {
      addAllBindings(i);
    }
    return ret;
  }

  @SuppressWarnings("rawtypes")
  private void addAllBindings(int index) {
    for (Map.Entry<String, List<BiConsumer>> entry : itemBindings.entrySet()) {
      String property = entry.getKey();
      List<BiConsumer> consumers = entry.getValue();
      for (BiConsumer consumer : consumers) {
        get(index).addBinding(getOrCreateBinding(consumer, index), property);
        get(index).onPropertyRefreshed(property);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private void removeAllBindings(int index) {
    for (Map.Entry<String, List<BiConsumer>> entry : itemBindings.entrySet()) {
      String property = entry.getKey();
      List<BiConsumer> consumers = entry.getValue();
      for (BiConsumer consumer : consumers) {
        get(index).removeBinding(getOrCreateBinding(consumer, index), property);
      }
    }
  }
}
