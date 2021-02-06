package com.joecollins.bindings;

import java.util.function.Consumer;
import java.util.function.Function;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import org.jetbrains.annotations.NotNull;

public class BindingReceiver<T> extends Bindable<BindingReceiver.Property> {
  enum Property {
    PROP
  }

  private T value;

  public BindingReceiver(Binding<? extends T> binding) {
    binding.bindLegacy(
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
      public void bind(@NotNull Function1<? super U, Unit> onUpdate) {
        bindLegacy(onUpdate::invoke);
      }

      @Override
      public void bindLegacy(Consumer<U> onUpdate) {
        if (consumer != null) {
          throw new IllegalStateException("Binding already in use");
        }
        consumer = onUpdate;
        topBinding = getBinding();
        topBinding.bindLegacy(
            t -> {
              if (subBinding != null) {
                subBinding.unbind();
              }
              subBinding = func.apply(t);
              subBinding.bindLegacy(consumer);
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
