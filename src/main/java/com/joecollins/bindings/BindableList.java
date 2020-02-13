package com.joecollins.bindings;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class BindableList<T> extends AbstractList<T> {

  private enum Empty {
    NULL
  }

  private ArrayList<T> underlying = new ArrayList<>();

  private List<IntConsumer> sizeBindings = new LinkedList<>();

  @SuppressWarnings("rawtypes")
  private Map<Enum<?>, List<BiConsumer>> itemBindings = new HashMap<>();

  private Map<BiConsumer, Map<Integer, Consumer>> storedBindings = new WeakHashMap<>();

  public void addSizeBinding(IntConsumer binding) {
    sizeBindings.add(binding);
  }

  public void removeSizeBinding(IntConsumer binding) {
    sizeBindings.remove(binding);
  }

  public void addItemBinding(BiConsumer<Integer, ?> binding, Enum<?>... properties) {
    for (Enum<?> property : properties)
      itemBindings.computeIfAbsent(property, x -> new LinkedList<>()).add(binding);
    itemBindings.computeIfAbsent(Empty.NULL, x -> new LinkedList<>()).add(binding);
    for (int index = 0; index < size(); index++) {
      T item = get(index);
      if (item instanceof Bindable) {
        ((Bindable) item).addBinding(getOrCreateBinding(binding, index), properties);
        for (Enum<?> property : properties) ((Bindable) item).onPropertyRefreshed(property);
      }
    }
  }

  public void removeItemBinding(BiConsumer<Integer, ?> binding, Enum<?>... properties) {
    for (Enum<?> property : properties)
      itemBindings.computeIfAbsent(property, x -> new LinkedList<>()).remove(binding);
    itemBindings.computeIfAbsent(Empty.NULL, x -> new LinkedList<>()).remove(binding);
    for (int index = 0; index < size(); index++) {
      T item = get(index);
      if (item instanceof Bindable) {
        ((Bindable) item).removeBinding(getOrCreateBinding(binding, index), properties);
      }
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

  public void setAll(Collection<T> elements) {
    List<T> elementsInOrder = new ArrayList<>(elements);
    for (int i = 0; i < elementsInOrder.size(); i++) {
      if (i < size()) {
        set(i, elementsInOrder.get(i));
      } else {
        add(elementsInOrder.get(i));
      }
    }
    while (size() > elements.size()) {
      remove(elements.size());
    }
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

  @SuppressWarnings({"rawtypes", "unchecked"})
  private void addAllBindings(int index) {
    T item = get(index);
    for (Map.Entry<Enum<?>, List<BiConsumer>> entry : itemBindings.entrySet()) {
      Enum<?> property = entry.getKey();
      List<BiConsumer> consumers = entry.getValue();
      for (BiConsumer consumer : consumers) {
        if (item instanceof Bindable) {
          ((Bindable) item).addBinding(getOrCreateBinding(consumer, index), property);
          ((Bindable) item).onPropertyRefreshed(property);
        } else {
          getOrCreateBinding(consumer, index).accept(item);
        }
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private void removeAllBindings(int index) {
    T item = get(index);
    for (Map.Entry<Enum<?>, List<BiConsumer>> entry : itemBindings.entrySet()) {
      Enum<?> property = entry.getKey();
      List<BiConsumer> consumers = entry.getValue();
      for (BiConsumer consumer : consumers) {
        if (item instanceof Bindable) {
          ((Bindable) item).removeBinding(getOrCreateBinding(consumer, index), property);
        }
      }
    }
  }
}
