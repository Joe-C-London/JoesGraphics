package com.joecollins.bindings;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.junit.Test;

public class BindingTest {

  @Test
  public void testFixedBinding() {
    Mutable<Integer> boundValue = new MutableObject<>();
    Binding<Integer> binding = Binding.fixedBinding(42);
    binding.bind(boundValue::setValue);
    assertEquals(42, boundValue.getValue().intValue());
  }

  @Test
  public void testPropertyBinding() {
    Mutable<Integer> boundValue = new MutableObject<>();
    BindableInt bindable = new BindableInt(7);
    Binding<Integer> binding =
        Binding.propertyBinding(bindable, BindableInt::getValue, BindableInt.Property.VALUE);
    binding.bind(boundValue::setValue);
    assertEquals(7, boundValue.getValue().intValue());

    bindable.setValue(42);
    assertEquals(42, boundValue.getValue().intValue());

    binding.unbind();
    bindable.setValue(7);
    assertEquals(42, boundValue.getValue().intValue());
  }

  @Test
  public void testSizeBinding() {
    Mutable<Integer> boundValue = new MutableObject<>();
    BindableList<Integer> list = new BindableList<>();
    list.add(7);
    Binding<Integer> binding = Binding.sizeBinding(list);
    binding.bind(boundValue::setValue);
    assertEquals(1, boundValue.getValue().intValue());

    list.add(42);
    assertEquals(2, boundValue.getValue().intValue());

    list.remove(1);
    assertEquals(1, boundValue.getValue().intValue());

    list.add(0, 7);
    assertEquals(2, boundValue.getValue().intValue());

    list.clear();
    assertEquals(0, boundValue.getValue().intValue());

    list.add(7);
    assertEquals(1, boundValue.getValue().intValue());

    binding.unbind();
    list.add(42);
    assertEquals(1, boundValue.getValue().intValue());
  }

  @Test
  public void testMapBinding() {
    Mutable<String> boundValue = new MutableObject<>();
    BindableInt bindable = new BindableInt(7);
    Binding<String> binding =
        Binding.propertyBinding(bindable, BindableInt::getValue, BindableInt.Property.VALUE)
            .map(Object::toString);
    binding.bind(boundValue::setValue);
    assertEquals("7", boundValue.getValue());

    bindable.setValue(42);
    assertEquals("42", boundValue.getValue());

    binding.unbind();
    bindable.setValue(7);
    assertEquals("42", boundValue.getValue());
  }

  @Test
  public void testMergeBinding() {
    Mutable<Integer> boundValue = new MutableObject<>();
    BindableInt bindable1 = new BindableInt(7);
    BindableInt bindable2 = new BindableInt(42);
    Binding<Integer> binding1 =
        Binding.propertyBinding(bindable1, BindableInt::getValue, BindableInt.Property.VALUE);
    Binding<Integer> binding2 =
        Binding.propertyBinding(bindable2, BindableInt::getValue, BindableInt.Property.VALUE);
    Binding<Integer> binding = binding1.merge(binding2, Integer::sum);
    binding.bind(boundValue::setValue);
    assertEquals(49, boundValue.getValue().intValue());

    bindable1.setValue(42);
    assertEquals(84, boundValue.getValue().intValue());

    bindable2.setValue(7);
    assertEquals(49, boundValue.getValue().intValue());

    binding.unbind();
    bindable1.setValue(7);
    assertEquals(49, boundValue.getValue().intValue());
    bindable2.setValue(1);
    assertEquals(49, boundValue.getValue().intValue());
  }

  @Test
  public void testBindingReceiverBasic() {
    Mutable<Integer> boundValue1 = new MutableObject<>();
    Mutable<Integer> boundValue2 = new MutableObject<>();
    BindableInt bindable = new BindableInt(7);
    Binding<Integer> binding =
        Binding.propertyBinding(bindable, BindableInt::getValue, BindableInt.Property.VALUE);
    BindingReceiver<Integer> receiver = new BindingReceiver<>(binding);
    Binding<Integer> binding1 = receiver.getBinding();
    binding1.bind(boundValue1::setValue);
    Binding<Integer> binding2 = receiver.getBinding();
    binding2.bind(boundValue2::setValue);
    assertEquals(7, boundValue1.getValue().intValue());
    assertEquals(7, boundValue2.getValue().intValue());

    bindable.setValue(42);
    assertEquals(42, boundValue1.getValue().intValue());
    assertEquals(42, boundValue2.getValue().intValue());

    binding1.unbind();
    bindable.setValue(7);
    assertEquals(42, boundValue1.getValue().intValue());
    assertEquals(7, boundValue2.getValue().intValue());
  }

  @Test
  public void testBindingReceiverMap() {
    Mutable<Integer> boundValue1 = new MutableObject<>();
    Mutable<Integer> boundValue2 = new MutableObject<>();
    BindableInt bindable = new BindableInt(7);
    Binding<Integer> binding =
        Binding.propertyBinding(bindable, BindableInt::getValue, BindableInt.Property.VALUE);
    BindingReceiver<Integer> receiver = new BindingReceiver<>(binding);
    Binding<Integer> binding1 = receiver.getBinding(i -> i * 2);
    binding1.bind(boundValue1::setValue);
    Binding<Integer> binding2 = receiver.getBinding(i -> i * i);
    binding2.bind(boundValue2::setValue);
    assertEquals(14, boundValue1.getValue().intValue());
    assertEquals(49, boundValue2.getValue().intValue());

    bindable.setValue(42);
    assertEquals(84, boundValue1.getValue().intValue());
    assertEquals(1764, boundValue2.getValue().intValue());

    binding1.unbind();
    bindable.setValue(7);
    assertEquals(84, boundValue1.getValue().intValue());
    assertEquals(49, boundValue2.getValue().intValue());
  }

  @Test
  public void testBindingReceiverFlatMap() {
    Mutable<Integer> boundValue = new MutableObject<>();
    BindableInt bindable1 = new BindableInt(7);
    NestedBindable<Integer> nested = new NestedBindable<>(bindable1);
    Binding<BindableValue<Integer>> nestedBinding =
        Binding.propertyBinding(nested, NestedBindable::getValue, BindableInt.Property.VALUE);
    BindingReceiver<BindableValue<Integer>> receiver = new BindingReceiver<>(nestedBinding);
    Binding<Integer> binding =
        receiver.getFlatBinding(
            v -> Binding.propertyBinding(v, BindableValue::getValue, BindableInt.Property.VALUE));
    binding.bind(boundValue::setValue);
    assertEquals(7, boundValue.getValue().intValue());

    bindable1.setValue(42);
    assertEquals(42, boundValue.getValue().intValue());

    BindableInt bindable2 = new BindableInt(12);
    nested.setValue(bindable2);
    assertEquals(12, boundValue.getValue().intValue());

    bindable1.setValue(1);
    assertEquals(12, boundValue.getValue().intValue());

    binding.unbind();
    bindable2.setValue(10);
    assertEquals(12, boundValue.getValue().intValue());

    nested.setValue(new BindableInt(27));
    assertEquals(12, boundValue.getValue().intValue());
  }

  @Test
  public void testMapReduceBinding() {
    Mutable<Integer> boundValue = new MutableObject<>();
    List<BindableInt> list =
        Arrays.asList(new BindableInt(1), new BindableInt(2), new BindableInt(3));
    List<Binding<Integer>> bindings =
        list.stream()
            .map(
                b ->
                    Binding.propertyBinding(
                        b, BindableValue::getValue, BindableValue.Property.VALUE))
            .collect(Collectors.toList());
    Binding<Integer> binding =
        Binding.mapReduceBinding(bindings, 0, (a, v) -> a + v, (a, v) -> a - v);
    binding.bind(boundValue::setValue);
    assertEquals(6, boundValue.getValue().intValue());

    list.get(0).setValue(4);
    list.get(1).setValue(5);
    list.get(2).setValue(6);
    assertEquals(15, boundValue.getValue().intValue());

    binding.unbind();

    list.get(0).setValue(1);
    list.get(1).setValue(2);
    list.get(2).setValue(3);
    assertEquals(15, boundValue.getValue().intValue());
  }

  private static class BindableValue<T> extends Bindable {
    enum Property {
      VALUE
    }

    private T value;

    public BindableValue() {}

    public BindableValue(T value) {
      setValue(value);
    }

    public T getValue() {
      return value;
    }

    public void setValue(T value) {
      this.value = value;
      onPropertyRefreshed(Property.VALUE);
    }
  }

  private static class BindableInt extends BindableValue<Integer> {
    public BindableInt() {
      super();
    }

    public BindableInt(int value) {
      super(value);
    }
  }

  private static class NestedBindable<T> extends BindableValue<BindableValue<T>> {
    public NestedBindable() {
      super();
    }

    public NestedBindable(BindableValue<T> value) {
      super(value);
    }
  }
}
