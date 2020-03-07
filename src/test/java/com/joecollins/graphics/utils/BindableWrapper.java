package com.joecollins.graphics.utils;

import com.joecollins.bindings.Bindable;
import com.joecollins.bindings.Binding;

public class BindableWrapper<T> extends Bindable {
  private enum BindableWrapperValue {
    VALUE
  }

  private T value;

  public BindableWrapper() {}

  public BindableWrapper(T value) {
    setValue(value);
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
    onPropertyRefreshed(BindableWrapperValue.VALUE);
  }

  public Binding<T> getBinding() {
    return Binding.propertyBinding(this, BindableWrapper::getValue, BindableWrapperValue.VALUE);
  }
}
