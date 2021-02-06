package com.joecollins.bindings;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public interface IndexedBinding<T> {

  int size();

  T getValue(int index);

  default void bind(BiConsumer<Integer, T> onUpdate) {
    IntStream.range(0, size()).forEach(idx -> onUpdate.accept(idx, getValue(idx)));
  }

  default void unbind() {}

  default <R> IndexedBinding<R> map(Function<T, R> func) {
    IndexedBinding<T> me = this;
    return new IndexedBinding<R>() {
      @Override
      public int size() {
        return me.size();
      }

      @Override
      public R getValue(int index) {
        return func.apply(me.getValue(index));
      }

      @Override
      public void bind(BiConsumer<Integer, R> onUpdate) {
        me.bind((idx, val) -> onUpdate.accept(idx, func.apply(val)));
      }

      @Override
      public void unbind() {
        me.unbind();
      }
    };
  }

  static <T> IndexedBinding<T> emptyBinding() {
    return new IndexedBinding<>() {
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
    return new IndexedBinding<>() {
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
    return new IndexedBinding<>() {
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
    return new IndexedBinding<>() {
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
    return new IndexedBinding<>() {
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

  static <E extends Enum<E>, T extends Bindable<E>, U> IndexedBinding<U> propertyBinding(
      T item, Function<T, List<? extends U>> func, E... properties) {
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

  static <T> IndexedBinding<T> propertyBinding(BindableList<T> list) {
    return propertyBinding(list, Function.identity());
  }

  static <T, U> IndexedBinding<U> propertyBinding(BindableList<T> list, Function<T, U> func) {
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
        list.addItemBinding(consumer);
        IntStream.range(0, size()).forEach(idx -> onUpdate.accept(idx, getValue(idx)));
      }

      @Override
      public void unbind() {
        if (consumer == null) {
          throw new IllegalStateException("Binding is not currently used");
        }
        list.removeItemBinding(consumer);
        consumer = null;
      }
    };
  }

  static <T extends Bindable<E>, U, E extends Enum<E>> IndexedBinding<U> propertyBinding(
      NestedBindableList<T, E> list, Function<T, U> func, E... properties) {
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
        list.addNestedItemBinding(consumer, properties);
        IntStream.range(0, size()).forEach(idx -> onUpdate.accept(idx, getValue(idx)));
      }

      @Override
      public void unbind() {
        if (consumer == null) {
          throw new IllegalStateException("Binding is not currently used");
        }
        list.removeNestedItemBinding(consumer, properties);
        consumer = null;
      }
    };
  }

  static <T, U> IndexedBinding<U> listBinding(List<T> list, Function<T, Binding<U>> bindingFunc) {
    if (list == null) {
      return emptyBinding();
    }
    List<Binding<U>> bindings = list.stream().map(bindingFunc).collect(Collectors.toList());
    return new IndexedBinding<>() {
      @Override
      public int size() {
        return bindings.size();
      }

      @Override
      public U getValue(int index) {
        return bindings.get(index).getValue();
      }

      @Override
      public void bind(BiConsumer<Integer, U> onUpdate) {
        for (int i = 0; i < bindings.size(); i++) {
          int index = i;
          bindings.get(index).bindLegacy(val -> onUpdate.accept(index, val));
        }
      }

      @Override
      public void unbind() {
        bindings.forEach(Binding::unbind);
      }
    };
  }
}
