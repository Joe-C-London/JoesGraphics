package com.joecollins.bindings;

import java.util.function.Consumer;
import java.util.function.Function;

public class BindingReceiver<T> extends Bindable<BindingReceiver.Property> {
  enum Property {
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

  public <U> Binding<U> getFlatBinding(Function<T, Binding<U>> func) {
    return new Binding<>() {
      private Binding<T> topBinding;
      private Binding<U> subBinding;
      private Consumer<U> consumer;

      @Override
      public U getValue() {
        return func.apply(value).getValue();
      }

      @Override
      public void bind(Consumer<U> onUpdate) {
        if (consumer != null) {
          throw new IllegalStateException("Binding already in use");
        }
        consumer = onUpdate;
        topBinding = getBinding();
        topBinding.bind(
            t -> {
              if (subBinding != null) {
                subBinding.unbind();
              }
              subBinding = func.apply(t);
              subBinding.bind(consumer);
            });
      }

      @Override
      public void unbind() {
        topBinding.unbind();
        if (subBinding != null) {
          subBinding.unbind();
        }
        consumer = null;
      }
    };
  }

  public T getValue() {
    return value;
  }
}
