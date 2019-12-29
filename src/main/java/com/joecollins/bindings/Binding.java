package com.joecollins.bindings;

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

  static <T> Binding<T> fixedBinding(T t) {
    return () -> t;
  }

  static <T extends Bindable, U> Binding<U> propertyBinding(
      T object, Function<T, U> func, String... properties) {
    if (object == null || properties.length == 0) {
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
          throw new IllegalStateException("Binding is not currently used");
        }
        object.removeBinding(consumer, properties);
        consumer = null;
      }
    };
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
          throw new IllegalStateException("Binding is not currently used");
        }
        list.removeSizeBinding(consumer);
        consumer = null;
      }
    };
  }
}
