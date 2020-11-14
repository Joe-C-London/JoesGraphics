package com.joecollins.bindings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

@FunctionalInterface
public interface Binding<T> {

  T getValue();

  default void bind(Consumer<T> onUpdate) {
    onUpdate.accept(getValue());
  }

  default void unbind() {}

  default <R> Binding<R> map(Function<T, R> func) {
    Binding<T> me = this;
    return new Binding<>() {
      @Override
      public R getValue() {
        return func.apply(me.getValue());
      }

      @Override
      public void bind(Consumer<R> onUpdate) {
        me.bind(t -> onUpdate.accept(func.apply(t)));
      }

      @Override
      public void unbind() {
        me.unbind();
      }
    };
  }

  default <R> Binding<R> mapNonNull(Function<T, R> func) {
    return map(t -> t == null ? null : func.apply(t));
  }

  default <U, R> Binding<R> merge(Binding<U> other, BiFunction<T, U, R> func) {
    Binding<T> me = this;
    return new Binding<>() {
      private T val1;
      private U val2;
      private boolean bound = false;

      @Override
      public R getValue() {
        return func.apply(me.getValue(), other.getValue());
      }

      @Override
      public void bind(Consumer<R> onUpdate) {
        me.bind(
            t -> {
              val1 = t;
              if (bound) {
                onUpdate.accept(func.apply(t, val2));
              }
            });
        other.bind(
            u -> {
              val2 = u;
              onUpdate.accept(func.apply(val1, u));
            });
        bound = true;
      }

      @Override
      public void unbind() {
        me.unbind();
        other.unbind();
        bound = false;
      }
    };
  }

  static <T> Binding<T> fixedBinding(T t) {
    return () -> t;
  }

  static <T extends Bindable<E>, U, E extends Enum<E>> Binding<U> propertyBinding(
      T object, Function<T, U> func, E... properties) {
    if (object == null) {
      return () -> null;
    }
    return new Binding<>() {
      private Consumer<T> consumer = null;

      @Override
      @SuppressWarnings("unchecked")
      public U getValue() {
        return func.apply(object);
      }

      @Override
      public void bind(Consumer<U> onUpdate) {
        if (consumer != null) {
          throw new IllegalStateException("Binding is already used");
        }
        consumer = val -> onUpdate.accept(func.apply(val));
        object.addBinding(consumer, properties);
        onUpdate.accept(getValue());
      }

      @Override
      public void unbind() {
        if (consumer == null) {
          return;
        }
        object.removeBinding(consumer, properties);
        consumer = null;
      }
    };
  }

  static <T extends Bindable<E>, U, E extends Enum<E>> Function<T, Binding<U>> propertyBindingFunc(
      Function<T, U> func, E... properties) {
    return t -> propertyBinding(t, func, properties);
  }

  static Binding<Integer> sizeBinding(BindableList<?> list) {
    if (list == null) {
      return () -> 0;
    }
    return new Binding<>() {
      private IntConsumer consumer = null;

      @Override
      public Integer getValue() {
        return list.size();
      }

      @Override
      public void bind(Consumer<Integer> onUpdate) {
        if (consumer != null) {
          throw new IllegalStateException("Binding is already used");
        }
        consumer = onUpdate::accept;
        list.addSizeBinding(consumer);
        onUpdate.accept(getValue());
      }

      @Override
      public void unbind() {
        if (consumer == null) {
          return;
        }
        list.removeSizeBinding(consumer);
        consumer = null;
      }
    };
  }

  static <T, R> Binding<R> mapReduceBinding(
      List<Binding<T>> bindings,
      R identity,
      BiConsumer<R, T> onValueAdded,
      BiConsumer<R, T> onValueRemoved) {
    return mapReduceBinding(
        bindings,
        identity,
        (a, b) -> {
          onValueAdded.accept(a, b);
          return a;
        },
        (a, b) -> {
          onValueRemoved.accept(a, b);
          return a;
        });
  }

  static <T, R> Binding<R> mapReduceBinding(
      List<Binding<T>> bindings,
      R identity,
      BiFunction<R, T, R> onValueAdded,
      BiFunction<R, T, R> onValueRemoved) {
    return new Binding<>() {
      private boolean bound = false;

      @Override
      public R getValue() {
        return bindings.stream()
            .map(Binding::getValue)
            .reduce(
                identity,
                onValueAdded,
                (a, b) -> {
                  throw new IllegalStateException("Combiner should not be called");
                });
      }

      private R aggregate = identity;
      private List<T> values = new ArrayList<>();

      @Override
      public void bind(Consumer<R> onUpdate) {
        for (int i = 0; i < bindings.size(); i++) {
          values.add(null);
        }
        aggregate = identity;
        for (int i = 0; i < bindings.size(); i++) {
          int index = i;
          bindings
              .get(index)
              .bind(
                  newVal -> {
                    T oldVal = values.get(index);
                    if (!Objects.equals(oldVal, newVal)) {
                      if (oldVal != null) {
                        aggregate = onValueRemoved.apply(aggregate, oldVal);
                      }
                      values.set(index, newVal);
                      if (newVal != null) {
                        aggregate = onValueAdded.apply(aggregate, newVal);
                      }
                      if (bound) {
                        onUpdate.accept(aggregate);
                      }
                    }
                  });
        }
        bound = true;
        onUpdate.accept(aggregate);
      }

      @Override
      public void unbind() {
        bindings.forEach(Binding::unbind);
        values.clear();
      }
    };
  }
}
