package com.joecollins.bindings;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
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

  static <T> IndexedBinding<T> functionBinding(
      int startInclusive, int endExclusive, IntFunction<T> itemFunc) {
    return new IndexedBinding<T>() {
      @Override
      public int size() {
        return endExclusive - startInclusive;
      }

      @Override
      public T getValue(int index) {
        return itemFunc.apply(startInclusive + index);
      }
    };
  }

  static <T> IndexedBinding<T> listBinding(T... items) {
    return new IndexedBinding<T>() {
      @Override
      public int size() {
        return items.length;
      }

      @Override
      public T getValue(int index) {
        return items[index];
      }
    };
  }

  static <T> IndexedBinding<T> listBinding(List<T> items) {
    return new IndexedBinding<T>() {
      @Override
      public int size() {
        return items.size();
      }

      @Override
      public T getValue(int index) {
        return items.get(index);
      }
    };
  }

  static <T extends Bindable, U> IndexedBinding<U> propertyBinding(
      T item, Function<T, List<? extends U>> func, Enum<?>... properties) {
    if (item == null) {
      return emptyBinding();
    }
    return new IndexedBinding<>() {
      private Consumer<T> consumer = null;

      @Override
      public U getValue(int idx) {
        if (idx < 0 || idx >= size()) {
          return null;
        }
        return func.apply(item).get(idx);
      }

      @Override
      public int size() {
        return func.apply(item).size();
      }

      @Override
      public void bind(BiConsumer<Integer, U> onUpdate) {
        if (consumer != null) {
          throw new IllegalStateException("Binding is already used");
        }
        consumer =
            val -> {
              List<? extends U> vals = func.apply(val);
              for (int i = 0; i < vals.size(); i++) {
                onUpdate.accept(i, vals.get(i));
              }
            };
        item.addBinding(consumer, properties);
        consumer.accept(item);
      }

      @Override
      public void unbind() {
        if (consumer == null) {
          throw new IllegalStateException("Binding is not currently used");
        }
        item.removeBinding(consumer, properties);
        consumer = null;
      }
    };
  }

  @SuppressWarnings("unchecked")
  static <T, U> IndexedBinding<U> propertyBinding(
      BindableList<T> list, Function<T, U> func, Enum<?>... properties) {
    if (list == null) {
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
