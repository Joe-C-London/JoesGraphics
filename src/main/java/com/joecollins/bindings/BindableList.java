package com.joecollins.bindings;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;

public class BindableList<T> extends AbstractList<T> {

  private enum Empty {
    NULL
  }

  public BindableList() {}

  @SafeVarargs
  public BindableList(T... items) {
    setAll(List.of(items));
  }

  public BindableList(List<T> list) {
    setAll(list);
  }

  private ArrayList<T> underlying = new ArrayList<>();

  private List<IntConsumer> sizeBindings = new LinkedList<>();
  private List<BiConsumer<Integer, T>> itemBindings = new LinkedList<>();

  public void addSizeBinding(IntConsumer binding) {
    sizeBindings.add(binding);
  }

  public void removeSizeBinding(IntConsumer binding) {
    sizeBindings.remove(binding);
  }

  public void addItemBinding(BiConsumer<Integer, T> binding) {
    itemBindings.add(binding);
  }

  public void removeItemBinding(BiConsumer<Integer, ?> binding, Enum<?>... properties) {
    itemBindings.remove(binding);
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

  protected void addAllBindings(int index) {
    T item = get(index);
    for (BiConsumer<Integer, T> c : itemBindings) {
      c.accept(index, item);
    }
  }

  protected void removeAllBindings(int index) {}
}
