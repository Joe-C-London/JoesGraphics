package com.joecollins.bindings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.Test;

public class IndexedBindingTest {

  @Test
  public void indexedBindingBasicTest() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    BindableList<Integer> list = new BindableList<>();
    list.add(7);
    IndexedBinding<Integer> binding = IndexedBinding.propertyBinding(list, i -> i);
    binding.bind(valuesByIndex::put);
    assertEquals(7, valuesByIndex.get(0).intValue());

    list.set(0, 42);
    assertEquals(42, valuesByIndex.get(0).intValue());

    binding.unbind();
    list.set(0, 7);
    assertEquals(42, valuesByIndex.get(0).intValue());
  }

  @Test
  public void indexedBindingListTest() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    MutableInt size = new MutableInt();
    BindableList<Integer> list = new BindableList<>();
    list.add(7);
    IndexedBinding<Integer> binding = IndexedBinding.propertyBinding(list, i -> i);
    binding.bind(valuesByIndex::put);
    Binding.sizeBinding(list).bind(size::setValue);
    assertEquals(7, valuesByIndex.get(0).intValue());
    assertEquals(1, size.getValue().intValue());

    list.add(0, 42);
    assertEquals(42, valuesByIndex.get(0).intValue());
    assertEquals(7, valuesByIndex.get(1).intValue());
    assertEquals(2, size.getValue().intValue());

    list.add(17);
    assertEquals(42, valuesByIndex.get(0).intValue());
    assertEquals(7, valuesByIndex.get(1).intValue());
    assertEquals(17, valuesByIndex.get(2).intValue());
    assertEquals(3, size.getValue().intValue());

    list.remove(0);
    assertEquals(7, valuesByIndex.get(0).intValue());
    assertEquals(17, valuesByIndex.get(1).intValue());
    assertEquals(2, size.getValue().intValue());

    list.addAll(List.of(20, 30));
    assertEquals(20, valuesByIndex.get(2).intValue());
    assertEquals(30, valuesByIndex.get(3).intValue());
    assertEquals(4, size.getValue().intValue());

    list.removeAll(List.of(7, 17));
    assertEquals(20, valuesByIndex.get(0).intValue());
    assertEquals(30, valuesByIndex.get(1).intValue());
    assertEquals(2, size.getValue().intValue());

    list.setAll(List.of(7, 17, 27));
    assertEquals(7, valuesByIndex.get(0).intValue());
    assertEquals(17, valuesByIndex.get(1).intValue());
    assertEquals(27, valuesByIndex.get(2).intValue());
    assertEquals(3, size.getValue().intValue());

    list.setAll(List.of(17, 27));
    assertEquals(17, valuesByIndex.get(0).intValue());
    assertEquals(27, valuesByIndex.get(1).intValue());
    assertEquals(2, size.getValue().intValue());
  }

  @Test
  public void indexedBindingBindableTest() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    BindableList<BindableInt> list = new BindableList<>();
    BindableInt bindable1 = new BindableInt(7);
    list.add(bindable1);
    IndexedBinding<Integer> binding =
        IndexedBinding.propertyBinding(list, BindableValue::getValue, BindableInt.Property.VALUE);
    binding.bind(valuesByIndex::put);
    assertEquals(7, valuesByIndex.get(0).intValue());

    bindable1.setValue(17);
    assertEquals(17, valuesByIndex.get(0).intValue());

    BindableInt bindable2 = new BindableInt(42);
    list.set(0, bindable2);
    assertEquals(42, valuesByIndex.get(0).intValue());

    bindable1.setValue(7);
    assertEquals(42, valuesByIndex.get(0).intValue());

    bindable2.setValue(7);
    assertEquals(7, valuesByIndex.get(0).intValue());

    binding.unbind();
    bindable2.setValue(17);
    assertEquals(7, valuesByIndex.get(0).intValue());

    list.set(0, new BindableInt(27));
    assertEquals(7, valuesByIndex.get(0).intValue());
  }

  @Test
  public void testEmptyBinding() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    IndexedBinding<Integer> binding = IndexedBinding.emptyBinding();
    binding.bind(valuesByIndex::put);
    assertTrue(valuesByIndex.isEmpty());
  }

  @Test
  public void testSingletonBinding() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    IndexedBinding<Integer> binding = IndexedBinding.singletonBinding(7);
    binding.bind(valuesByIndex::put);
    assertEquals(1, valuesByIndex.size());
    assertEquals(7, valuesByIndex.get(0).intValue());
  }

  @Test
  public void testListPropertyBinding() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    BindableToList<Integer> bindable = new BindableToList<>(1, 2, 3);
    IndexedBinding<Integer> binding =
        IndexedBinding.propertyBinding(
            bindable, BindableValue::getValue, BindableValue.Property.VALUE);
    binding.bind(valuesByIndex::put);
    assertEquals(1, valuesByIndex.get(0).intValue());
    assertEquals(2, valuesByIndex.get(1).intValue());
    assertEquals(3, valuesByIndex.get(2).intValue());

    bindable.setValue(4, 5, 6);
    assertEquals(4, valuesByIndex.get(0).intValue());
    assertEquals(5, valuesByIndex.get(1).intValue());
    assertEquals(6, valuesByIndex.get(2).intValue());

    binding.unbind();
    bindable.setValue(7, 8, 9);
    assertEquals(4, valuesByIndex.get(0).intValue());
    assertEquals(5, valuesByIndex.get(1).intValue());
    assertEquals(6, valuesByIndex.get(2).intValue());
  }

  @Test
  public void testListArrayBinding() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    IndexedBinding<Integer> binding = IndexedBinding.listBinding(1, 2, 3);
    binding.bind(valuesByIndex::put);
    assertEquals(1, valuesByIndex.get(0).intValue());
    assertEquals(2, valuesByIndex.get(1).intValue());
    assertEquals(3, valuesByIndex.get(2).intValue());
  }

  @Test
  public void testListListBinding() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    IndexedBinding<Integer> binding = IndexedBinding.listBinding(List.of(1, 2, 3));
    binding.bind(valuesByIndex::put);
    assertEquals(1, valuesByIndex.get(0).intValue());
    assertEquals(2, valuesByIndex.get(1).intValue());
    assertEquals(3, valuesByIndex.get(2).intValue());
  }

  @Test
  public void testListFuncBinding() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    List<BindableInt> bindables =
        List.of(new BindableInt(1), new BindableInt(2), new BindableInt(3));
    IndexedBinding<Integer> binding =
        IndexedBinding.listBinding(
            bindables,
            b -> Binding.propertyBinding(b, BindableInt::getValue, BindableInt.Property.VALUE));
    binding.bind(valuesByIndex::put);
    assertEquals(1, valuesByIndex.get(0).intValue());
    assertEquals(2, valuesByIndex.get(1).intValue());
    assertEquals(3, valuesByIndex.get(2).intValue());

    bindables.get(0).setValue(4);
    bindables.get(1).setValue(5);
    bindables.get(2).setValue(6);
    assertEquals(4, valuesByIndex.get(0).intValue());
    assertEquals(5, valuesByIndex.get(1).intValue());
    assertEquals(6, valuesByIndex.get(2).intValue());

    binding.unbind();
    bindables.get(0).setValue(1);
    bindables.get(1).setValue(2);
    bindables.get(2).setValue(3);
    assertEquals(4, valuesByIndex.get(0).intValue());
    assertEquals(5, valuesByIndex.get(1).intValue());
    assertEquals(6, valuesByIndex.get(2).intValue());
  }

  @Test
  public void testRangeBinding() {
    Map<Integer, Integer> valuesByIndex = new HashMap<>();
    IndexedBinding<Integer> binding = IndexedBinding.functionBinding(1, 5, i -> i * i);
    binding.bind(valuesByIndex::put);
    assertEquals(1, valuesByIndex.get(0).intValue());
    assertEquals(4, valuesByIndex.get(1).intValue());
    assertEquals(9, valuesByIndex.get(2).intValue());
    assertEquals(16, valuesByIndex.get(3).intValue());
    assertNull(valuesByIndex.get(4));
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
      onPropertyRefreshed(BindableValue.Property.VALUE);
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

  private static class BindableToList<T> extends BindableValue<List<T>> {
    public BindableToList(T... items) {
      setValue(items);
    }

    public void setValue(T... items) {
      setValue(new ArrayList<>(Arrays.asList(items)));
    }
  }
}
