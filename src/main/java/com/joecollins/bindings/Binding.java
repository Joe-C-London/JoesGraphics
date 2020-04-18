package com.joecollins.bindings;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

@FunctionalInterface
public interface Binding<T> {

  class BindingReceiver<T> extends Bindable {
    private enum Property {
      PROP
    }

    private T value;

    public BindingReceiver(Binding<? extends T> binding) {
      binding.bind(
          v -> {
            value = v;
            onPropertyRefreshed(Property.PROP);
          });
    }

    public Binding<T> getBinding() {
      return getBinding(Function.identity());
    }

    public <U> Binding<U> getBinding(Function<T, U> func) {
      return Binding.propertyBinding(this, t -> func.apply(t.value), Property.PROP);
    }

    public T getValue() {
      return value;
    }
  }

  T getValue();

  default void bind(Consumer<T> onUpdate) {
    onUpdate.accept(getValue());
  }

  default void unbind() {}

  default <U, R> Binding<R> merge(Binding<U> other, BiFunction<T, U, R> func) {
    Binding<T> me = this;
    return new Binding<R>() {
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

  static <T, U> Binding<U> propertyBinding(T object, Function<T, U> func, Enum<?>... properties) {
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
        if (object instanceof Bindable) {
          ((Bindable) object).addBinding(consumer, properties);
        }
        onUpdate.accept(getValue());
      }

      @Override
      public void unbind() {
        if (consumer == null) {
          return;
        }
        if (object instanceof Bindable) {
          ((Bindable) object).removeBinding(consumer, properties);
        }
        consumer = null;
      }
    };
  }

  static <T, U> Function<T, Binding<U>> propertyBindingFunc(
      Function<T, U> func, Enum<?>... properties) {
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
}
