package com.joecollins.bindings;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

public interface IndexedBinding<T> {

  int size();

  T getValue(int index);

  default void bind(BiConsumer<Integer, T> onUpdate) {
    IntStream.range(0, size()).forEach(idx -> onUpdate.accept(idx, getValue(idx)));
  }

  default void unbind() {}

  static <T> IndexedBinding<T> emptyBinding() {
    return new IndexedBinding<T>() {
      @Override
      public int size() {
        return 0;
      }

      @Override
      public T getValue(int index) {
        return null;
      }
    };
  }

  static <T> IndexedBinding<T> singletonBinding(T item) {
    return new IndexedBinding<T>() {
      @Override
      public int size() {
        return 1;
      }

      @Override
      public T getValue(int index) {
        return item;
      }
    };
  }

  @SuppressWarnings("unchecked")
  static <T extends Bindable, U> IndexedBinding<U> propertyBinding(
      BindableList<T> list, Function<T, U> func, String... properties) {
    if (list == null || properties.length == 0) {
      return emptyBinding();
    }
    return new IndexedBinding<>() {
      private BiConsumer<Integer, T> consumer = null;

      @Override
      public U getValue(int idx) {
        if (idx < 0 || idx >= size()) {
          return null;
        }
        return func.apply(list.get(idx));
      }

      @Override
      public int size() {
        return list.size();
      }

      @Override
      public void bind(BiConsumer<Integer, U> onUpdate) {
        if (consumer != null) {
          throw new IllegalStateException("Binding is already used");
        }
        consumer = (idx, val) -> onUpdate.accept(idx, func.apply(val));
        list.addItemBinding(consumer, properties);
        IntStream.range(0, size()).forEach(idx -> onUpdate.accept(idx, getValue(idx)));
      }

      @Override
      public void unbind() {
        if (consumer == null) {
          throw new IllegalStateException("Binding is not currently used");
        }
        list.removeItemBinding(consumer, properties);
        consumer = null;
      }
    };
  }
}
