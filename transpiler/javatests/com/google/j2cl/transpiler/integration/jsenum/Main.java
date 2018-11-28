/*
 * Copyright 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.j2cl.transpiler.integration.jsenum;

import java.io.Serializable;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;
import javaemul.internal.annotations.DoNotAutobox;
import javaemul.internal.annotations.UncheckedCast;
import jsinterop.annotations.JsEnum;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsOverlay;

public class Main {

  private static final Object OK_STRING = "Ok";
  private static final Object HELLO_STRING = "Hello";
  private static final Object ONE_DOUBLE = 1.0d;
  private static final Object FALSE_BOOLEAN = false;

  public static void main(String... args) {
    testNativeJsEnum();
    testStringNativeJsEnum();
    testCastOnNative();
    testJsEnum();
    testBooleanJsEnum();
    testStringJsEnum();
    testJsEnumClassInitialization();
    testNativeEnumClassInitialization();
    testDoNotAutoboxJsEnum();
    testUnckeckedCastJsEnum();
    testBoxUnboxWithTypeInference();
    testBoxingWithSpecialMethods();
    testParameterizedLambdaUnboxing();
  }

  @JsEnum(isNative = true, namespace = "test")
  enum NativeEnum {
    OK,
    CANCEL
  }

  private static void testNativeJsEnum() {
    NativeEnum v = NativeEnum.OK;
    switch (v) {
      case OK:
        break;
      case CANCEL:
        fail();
        break;
      default:
        fail();
        break;
    }

    assertThrows(
        NullPointerException.class,
        () -> {
          NativeEnum nullJsEnum = null;
          switch (nullJsEnum) {
          }
        });

    assertTrue(v == NativeEnum.OK);
    assertTrue(v != NativeEnum.CANCEL);
    // Native JsEnums are not boxed.
    assertTrue(v == OK_STRING);
    assertTrue(v == (Object) StringNativeEnum.OK);

    // No boxing
    Object o = NativeEnum.OK;
    assertTrue(o == NativeEnum.OK);

    // Object methods calls on a variable of JsEnum type.
    assertTrue(v.hashCode() == NativeEnum.OK.hashCode());
    assertTrue(v.hashCode() != NativeEnum.CANCEL.hashCode());
    assertTrue(v.hashCode() == StringNativeEnum.OK.hashCode());
    assertTrue(v.toString().equals(OK_STRING));
    assertTrue(v.equals(NativeEnum.OK));
    assertTrue(v.equals(OK_STRING));
    assertTrue(v.equals(StringNativeEnum.OK));

    // Object methods calls on a variable of Object type.
    assertTrue(o.hashCode() == NativeEnum.OK.hashCode());
    assertTrue(o.hashCode() != NativeEnum.CANCEL.hashCode());
    assertTrue(o.hashCode() == StringNativeEnum.OK.hashCode());
    assertTrue(o.toString().equals(OK_STRING));
    assertTrue(o.equals(NativeEnum.OK));
    assertTrue(o.equals(OK_STRING));
    assertTrue(v.equals(StringNativeEnum.OK));

    assertFalse(v instanceof Enum);
    assertTrue((Object) v instanceof String);
    assertTrue(v instanceof Comparable);
    assertTrue(v instanceof Serializable);
    assertFalse((Object) v instanceof PlainJsEnum);

    NativeEnum ne = (NativeEnum) o;
    StringNativeEnum sne = (StringNativeEnum) o;
    Comparable ce = (Comparable) o;
    Serializable s = (Serializable) o;
    assertThrowsClassCastException(() -> (Enum) o);
    assertThrowsClassCastException(() -> (Boolean) o);

    assertTrue(asSeenFromJs(NativeEnum.OK) == OK_STRING);
  }

  @JsMethod(name = "passThrough")
  private static native Object asSeenFromJs(NativeEnum s);

  @JsEnum(isNative = true, namespace = "test", name = "NativeEnum", hasCustomValue = true)
  enum StringNativeEnum {
    OK,
    CANCEL;

    private String value;

    @JsOverlay
    public String getValue() {
      return value;
    }
  }

  private static void testStringNativeJsEnum() {
    StringNativeEnum v = StringNativeEnum.OK;
    switch (v) {
      case OK:
        break;
      case CANCEL:
        fail();
        break;
      default:
        fail();
        break;
    }

    assertThrows(
        NullPointerException.class,
        () -> {
          StringNativeEnum nullJsEnum = null;
          switch (nullJsEnum) {
          }
        });

    assertTrue(v == StringNativeEnum.OK);
    assertTrue(v != StringNativeEnum.CANCEL);
    assertTrue((Object) v == OK_STRING);
    assertTrue(v == (Object) NativeEnum.OK);

    Object o = StringNativeEnum.OK;
    assertTrue(o == StringNativeEnum.OK);

    // Object methods calls on a variable of JsEnum type.
    assertTrue(v.hashCode() == StringNativeEnum.OK.hashCode());
    assertTrue(v.hashCode() != StringNativeEnum.CANCEL.hashCode());
    assertTrue(v.toString().equals(OK_STRING));
    assertTrue(v.equals(StringNativeEnum.OK));
    assertTrue(v.equals(NativeEnum.OK));
    assertTrue(v.equals(OK_STRING));

    // Object methods calls on a variable of Object type.
    assertTrue(o.hashCode() == StringNativeEnum.OK.hashCode());
    assertTrue(o.hashCode() != StringNativeEnum.CANCEL.hashCode());
    assertTrue(o.toString().equals(OK_STRING));
    assertTrue(o.equals(StringNativeEnum.OK));
    assertTrue(o.equals(NativeEnum.OK));
    assertTrue(o.equals(OK_STRING));

    assertTrue(v.getValue().equals(v.toString()));
    assertTrue(v.getValue().equals(OK_STRING));

    assertFalse(v instanceof Enum);
    assertTrue((Object) v instanceof String);
    assertTrue(v instanceof Comparable);
    assertTrue(v instanceof Serializable);
    assertFalse((Object) v instanceof PlainJsEnum);

    Serializable se = (Serializable) o;
    StringNativeEnum sne = (StringNativeEnum) o;
    NativeEnum ne = (NativeEnum) o;
    Comparable ce = (Comparable) o;
    Serializable s = (Serializable) o;
    assertThrowsClassCastException(() -> (Enum) o);
    assertThrowsClassCastException(() -> (Boolean) o);

    assertTrue(asSeenFromJs(StringNativeEnum.OK) == OK_STRING);
  }

  @JsEnum(isNative = true, namespace = "test", name = "NativeEnumOfNumber", hasCustomValue = true)
  enum NumberNativeEnum {
    ONE,
    TWO;

    short value;
  }

  public static void testCastOnNative() {
    castToNativeEnum(NativeEnum.OK);
    castToNativeEnum(StringNativeEnum.OK);
    castToNativeEnum(NumberNativeEnum.ONE);
    castToNativeEnum(PlainJsEnum.ONE);
    castToNativeEnum(OK_STRING);
    castToNativeEnum((Double) 2.0);
    castToNativeEnum((Integer) 1);

    castToStringNativeEnum(StringNativeEnum.OK);
    castToStringNativeEnum(NativeEnum.OK);
    castToStringNativeEnum(OK_STRING);
    assertThrowsClassCastException(() -> castToStringNativeEnum(NumberNativeEnum.ONE));
    assertThrowsClassCastException(() -> castToStringNativeEnum(PlainJsEnum.ONE));
    assertThrowsClassCastException(() -> castToStringNativeEnum((Integer) 1));
    assertThrowsClassCastException(() -> castToStringNativeEnum((Double) 2.0));

    castToNumberNativeEnum(NumberNativeEnum.ONE);
    castToNumberNativeEnum((Double) 2.0);
    assertThrowsClassCastException(() -> castToNumberNativeEnum(NativeEnum.OK));
    assertThrowsClassCastException(() -> castToNumberNativeEnum(StringNativeEnum.OK));
    assertThrowsClassCastException(() -> castToNumberNativeEnum(PlainJsEnum.ONE));
    assertThrowsClassCastException(() -> castToNumberNativeEnum((Integer) 1));
    assertThrowsClassCastException(() -> castToNumberNativeEnum(OK_STRING));
  }

  private static NativeEnum castToNativeEnum(Object o) {
    return (NativeEnum) o;
  }

  private static StringNativeEnum castToStringNativeEnum(Object o) {
    return (StringNativeEnum) o;
  }

  private static NumberNativeEnum castToNumberNativeEnum(Object o) {
    return (NumberNativeEnum) o;
  }

  @JsMethod(name = "passThrough")
  private static native Object asSeenFromJs(StringNativeEnum s);

  @JsEnum
  enum PlainJsEnum {
    ZERO,
    ONE,
    TWO,
    THREE,
    FOUR,
    FIVE,
    SIX,
    SEVEN,
    EIGHT,
    NINE,
    TEN;

    public int getValue() {
      return ordinal();
    }
  }

  @JsEnum
  enum OtherPlainJsEnum {
    NONE,
    UNIT
  }

  private static void testJsEnum() {
    PlainJsEnum v = PlainJsEnum.ONE;
    switch (v) {
      case ZERO:
        fail();
        break;
      case ONE:
        break;
      default:
        fail();
        break;
    }

    assertThrows(
        NullPointerException.class,
        () -> {
          PlainJsEnum nullJsEnum = null;
          switch (nullJsEnum) {
          }
        });

    assertTrue(v == PlainJsEnum.ONE);
    assertTrue(v != PlainJsEnum.ZERO);
    assertTrue((Object) v != ONE_DOUBLE);
    // Boxing preserves equality.
    Object o = PlainJsEnum.ONE;
    assertTrue(o == PlainJsEnum.ONE);

    // Object methods calls on a variable of JsEnum type.
    assertTrue(v.hashCode() == PlainJsEnum.ONE.hashCode());
    assertTrue(v.hashCode() != PlainJsEnum.ZERO.hashCode());
    assertTrue(v.toString().equals(String.valueOf(ONE_DOUBLE)));
    assertTrue(v.equals(PlainJsEnum.ONE));
    assertFalse(v.equals(ONE_DOUBLE));
    assertFalse(PlainJsEnum.ZERO.equals(OtherPlainJsEnum.NONE));

    // Object methods calls on a variable of Object type.
    assertTrue(o.hashCode() == PlainJsEnum.ONE.hashCode());
    assertTrue(o.hashCode() != PlainJsEnum.ZERO.hashCode());
    assertTrue(o.toString().equals(String.valueOf(ONE_DOUBLE)));
    assertTrue(o.equals(PlainJsEnum.ONE));
    assertFalse(o.equals(ONE_DOUBLE));

    assertTrue(v.getValue() == 1);
    assertTrue(v.ordinal() == 1);
    assertTrue(PlainJsEnum.ONE.compareTo(v) == 0);
    assertTrue(PlainJsEnum.ZERO.compareTo(v) < 0);
    assertThrows(
        ClassCastException.class,
        () -> {
          Comparable comparable = PlainJsEnum.ONE;
          comparable.compareTo(OtherPlainJsEnum.UNIT);
        });
    assertThrows(
        ClassCastException.class,
        () -> {
          Comparable comparable = PlainJsEnum.ONE;
          comparable.compareTo(ONE_DOUBLE);
        });
    assertThrows(
        ClassCastException.class,
        () -> {
          Comparable comparable = (Comparable) ONE_DOUBLE;
          comparable.compareTo(PlainJsEnum.ONE);
        });

    // Test that boxing of special method 'ordinal()' call is not broken by normalization.
    Integer i = v.ordinal();
    assertTrue(i.intValue() == 1);

    assertFalse(v instanceof Enum);
    assertTrue(v instanceof PlainJsEnum);
    assertFalse((Object) v instanceof Double);
    assertTrue(v instanceof Comparable);
    assertTrue(v instanceof Serializable);
    assertFalse((Object) v instanceof BooleanJsEnum);

    assertFalse(new Object() instanceof PlainJsEnum);
    assertFalse((Object) ONE_DOUBLE instanceof PlainJsEnum);

    PlainJsEnum pe = (PlainJsEnum) o;
    Comparable c = (Comparable) o;
    Serializable s = (Serializable) o;
    assertThrowsClassCastException(() -> (Enum) o);
    assertThrowsClassCastException(() -> (Double) o);

    assertTrue(asSeenFromJs(PlainJsEnum.ONE) == ONE_DOUBLE);

    // Comparable test.
    SortedSet<Comparable> sortedSet = new TreeSet<>(Comparable::compareTo);
    sortedSet.add(PlainJsEnum.ONE);
    sortedSet.add(PlainJsEnum.ZERO);
    assertTrue(sortedSet.iterator().next() == PlainJsEnum.ZERO);
    assertTrue(sortedSet.iterator().next() instanceof PlainJsEnum);
  }

  @JsMethod(name = "passThrough")
  private static native Object asSeenFromJs(PlainJsEnum d);

  @JsEnum(hasCustomValue = true)
  enum BooleanJsEnum {
    TRUE(true),
    FALSE(false);

    boolean value;

    BooleanJsEnum(boolean value) {
      this.value = value;
    }
  }

  private static void testBooleanJsEnum() {
    BooleanJsEnum v = BooleanJsEnum.FALSE;
    switch (v) {
      case TRUE:
        fail();
        break;
      case FALSE:
        break;
      default:
        fail();
        break;
    }

    assertThrows(
        NullPointerException.class,
        () -> {
          BooleanJsEnum nullJsEnum = null;
          switch (nullJsEnum) {
          }
        });

    assertTrue(v == BooleanJsEnum.FALSE);
    assertTrue(v != BooleanJsEnum.TRUE);
    assertTrue((Object) v != FALSE_BOOLEAN);
    // Boxing preserves equality.
    Object o = BooleanJsEnum.FALSE;
    assertTrue(o == BooleanJsEnum.FALSE);

    // Object methods calls on a variable of JsEnum type.
    assertTrue(v.hashCode() == BooleanJsEnum.FALSE.hashCode());
    assertTrue(v.hashCode() != BooleanJsEnum.TRUE.hashCode());
    assertTrue(v.toString().equals(String.valueOf(FALSE_BOOLEAN)));
    assertTrue(v.equals(BooleanJsEnum.FALSE));
    assertFalse(v.equals(FALSE_BOOLEAN));

    // Object methods calls on a variable of Object type.
    assertTrue(o.hashCode() == BooleanJsEnum.FALSE.hashCode());
    assertTrue(o.hashCode() != BooleanJsEnum.TRUE.hashCode());
    assertTrue(o.toString().equals(String.valueOf(FALSE_BOOLEAN)));
    assertTrue(o.equals(BooleanJsEnum.FALSE));
    assertFalse(o.equals(FALSE_BOOLEAN));

    assertTrue((Object) v.value == FALSE_BOOLEAN);
    // Test that boxing of special field 'value' call is not broken by normalization.
    Boolean b = v.value;
    assertTrue(b == FALSE_BOOLEAN);

    assertFalse(v instanceof Enum);
    assertTrue(v instanceof BooleanJsEnum);
    assertFalse((Object) v instanceof Boolean);
    assertFalse(v instanceof Comparable);
    assertTrue(v instanceof Serializable);
    assertFalse((Object) v instanceof PlainJsEnum);

    assertFalse(new Object() instanceof BooleanJsEnum);
    assertFalse((Object) FALSE_BOOLEAN instanceof BooleanJsEnum);

    BooleanJsEnum be = (BooleanJsEnum) o;
    Serializable s = (Serializable) o;

    assertThrowsClassCastException(() -> (Enum) o);
    assertThrowsClassCastException(() -> (Comparable) o);
    assertThrowsClassCastException(() -> (Boolean) o);

    assertTrue(asSeenFromJs(BooleanJsEnum.FALSE) == FALSE_BOOLEAN);
  }

  @JsMethod(name = "passThrough")
  private static native Object asSeenFromJs(BooleanJsEnum b);

  @JsEnum(hasCustomValue = true)
  enum StringJsEnum {
    HELLO("Hello"),
    GOODBYE("Good Bye");

    String value;

    StringJsEnum(String value) {
      this.value = value;
    }
  }

  private static void testStringJsEnum() {
    StringJsEnum v = StringJsEnum.HELLO;
    switch (v) {
      case GOODBYE:
        fail();
        break;
      case HELLO:
        break;
      default:
        fail();
        break;
    }

    assertThrows(
        NullPointerException.class,
        () -> {
          StringJsEnum nullJsEnum = null;
          switch (nullJsEnum) {
          }
        });

    assertTrue(v == StringJsEnum.HELLO);
    assertTrue(v != StringJsEnum.GOODBYE);
    assertTrue((Object) v != HELLO_STRING);
    // Boxing preserves equality.
    Object o = StringJsEnum.HELLO;
    assertTrue(o == StringJsEnum.HELLO);

    // Object methods calls on a variable of JsEnum type.
    assertTrue(v.hashCode() == StringJsEnum.HELLO.hashCode());
    assertTrue(v.hashCode() != StringJsEnum.GOODBYE.hashCode());
    assertTrue(v.toString().equals(HELLO_STRING));
    assertTrue(v.equals(StringJsEnum.HELLO));
    assertFalse(v.equals(HELLO_STRING));

    // Object methods calls on a variable of Object type.
    assertTrue(o.hashCode() == StringJsEnum.HELLO.hashCode());
    assertTrue(o.hashCode() != StringJsEnum.GOODBYE.hashCode());
    assertTrue(o.toString().equals(HELLO_STRING));
    assertTrue(o.equals(StringJsEnum.HELLO));
    assertFalse(o.equals(HELLO_STRING));

    assertTrue(v.value.equals(HELLO_STRING));

    assertFalse(v instanceof Enum);
    assertTrue(v instanceof StringJsEnum);
    assertFalse((Object) v instanceof String);
    assertFalse(v instanceof Comparable);
    assertTrue(v instanceof Serializable);
    assertFalse((Object) v instanceof PlainJsEnum);

    assertFalse(new Object() instanceof StringJsEnum);
    assertFalse((Object) HELLO_STRING instanceof StringJsEnum);

    StringJsEnum se = (StringJsEnum) o;
    Serializable s = (Serializable) o;
    assertThrowsClassCastException(() -> (Enum) o);
    assertThrowsClassCastException(() -> (Comparable) o);
    assertThrowsClassCastException(() -> (String) o);

    assertTrue(asSeenFromJs(StringJsEnum.HELLO) == HELLO_STRING);
  }

  @JsMethod(name = "passThrough")
  private static native Object asSeenFromJs(StringJsEnum b);

  private static boolean nonNativeClinitCalled = false;

  @JsEnum
  enum EnumWithClinit {
    A;

    static {
      nonNativeClinitCalled = true;
    }

    int getValue() {
      return ordinal();
    }
  }

  private static void testJsEnumClassInitialization() {
    assertFalse(nonNativeClinitCalled);
    // Access to an enum value does not trigger clinit.
    Object o = EnumWithClinit.A;
    assertFalse(nonNativeClinitCalled);

    // Cast and instanceof do not trigger clinit.
    if (o instanceof EnumWithClinit) {
      o = (EnumWithClinit) o;
    }
    assertFalse(nonNativeClinitCalled);

    // Access to ordinal() does not trigger clinit.
    int n = EnumWithClinit.A.ordinal();
    assertFalse(nonNativeClinitCalled);

    // Access to any devirtualized method triggers clinit.
    EnumWithClinit.A.getValue();
    assertTrue(nonNativeClinitCalled);
  }

  private static boolean nativeClinitCalled = false;

  @JsEnum(isNative = true, hasCustomValue = true, namespace = "test", name = "NativeEnum")
  enum NativeEnumWithClinit {
    OK;

    static {
      nativeClinitCalled = true;
    }

    String value;

    @JsOverlay
    String getValue() {
      return value;
    }
  }

  private static void testNativeEnumClassInitialization() {
    assertFalse(nativeClinitCalled);
    // Access to an enum value does not trigger clinit.
    Object o = NativeEnumWithClinit.OK;
    assertFalse(nativeClinitCalled);

    // Cast does not trigger clinit.
    o = (NativeEnumWithClinit) o;
    assertFalse(nativeClinitCalled);

    // Access to value does not trigger clinit.
    String s = NativeEnumWithClinit.OK.value;
    assertFalse(nativeClinitCalled);

    // Access to any devirtualized method triggers clinit.
    NativeEnumWithClinit.OK.getValue();
    assertTrue(nativeClinitCalled);
  }

  private static void testDoNotAutoboxJsEnum() {
    assert returnsObject(StringJsEnum.HELLO) == HELLO_STRING;
    assert returnsObject(0, StringJsEnum.HELLO) == HELLO_STRING;
  }

  private static Object returnsObject(@DoNotAutobox Object object) {
    return object;
  }

  private static Object returnsObject(int n, @DoNotAutobox Object... object) {
    return object[0];
  }

  private static void testUnckeckedCastJsEnum() {
    StringJsEnum s = uncheckedCast(HELLO_STRING);
    assert s == StringJsEnum.HELLO;
  }

  @UncheckedCast
  private static <T> T uncheckedCast(@DoNotAutobox Object object) {
    return (T) object;
  }

  private static void testBoxingWithSpecialMethods() {
    assertTrue(PlainJsEnum.ONE.equals(PlainJsEnum.ONE));
    assertTrue(PlainJsEnum.ONE.compareTo(PlainJsEnum.ONE) == 0);
    assertTrue(PlainJsEnum.ONE.compareTo(PlainJsEnum.ZERO) > 0);
    assertTrue(PlainJsEnum.TWO.compareTo(PlainJsEnum.TEN) < 0);
  }

  private static void testBoxUnboxWithTypeInference() {
    assertSameType(Double.class, PlainJsEnum.ONE);
    assertSameType(PlainJsEnum.class, boxingIdentity(PlainJsEnum.ONE));

    // Make sure the enum is boxed even when assigned to a field that is inferred to be JsEnum.
    TemplatedField<PlainJsEnum> templatedField = new TemplatedField<PlainJsEnum>(PlainJsEnum.ONE);
    PlainJsEnum unboxed = templatedField.getValue();
    assertSameType(Double.class, unboxed);
    // Boxing through specialized method parameter assignment.
    assertSameType(PlainJsEnum.class, boxingIdentity(unboxed));
    // Unboxing as a qualifier to ordinal.
    assertSameType(Double.class, templatedField.getValue().ordinal());

    // Boxing through specialized method parameter assignment.
    assertSameType(PlainJsEnum.class, boxingIdentity(templatedField.getValue()));
    // Checks what is actually returned by getValue().
    assertSameType(PlainJsEnum.class, ((TemplatedField) templatedField).getValue());

    unboxed = templatedField.value;
    assertSameType(Double.class, unboxed);

    templatedField.value = PlainJsEnum.ONE;
    // Boxing through specialized method parameter assignment.
    assertSameType(PlainJsEnum.class, boxingIdentity(templatedField.value));
    // Checks what is actually stored in value.
    assertSameType(PlainJsEnum.class, ((TemplatedField) templatedField).value);
    // Unboxing as a qualifier to ordinal.
    assertSameType(Double.class, templatedField.value.ordinal());

    // Boxing/unboxing in varargs.
    assertSameType(Double.class, Arrays.asList(PlainJsEnum.ONE).get(0));

    // TODO(b/118615488): Rewrite the following checks when JsEnum arrays are allowed.
    // In Java the varargs array will be of the inferred argument type. Since non native JsEnum
    // arrays are not allowed, the created array is of the declared type.
    assertSameType(Comparable[].class, varargsToComparableArray(PlainJsEnum.ONE));
    assertSameType(PlainJsEnum.class, varargsToComparableArray(PlainJsEnum.ONE)[0]);
    assertSameType(Object[].class, varargsToObjectArray(PlainJsEnum.ONE));
    assertSameType(PlainJsEnum.class, varargsToObjectArray(PlainJsEnum.ONE)[0]);
  }

  private static class TemplatedField<T> {
    T value;

    TemplatedField(T value) {
      this.value = value;
    }

    T getValue() {
      return this.value;
    }
  }

  private static <T> Object boxingIdentity(T o) {
    return o;
  }

  private static <T extends Comparable> Object[] varargsToComparableArray(T... elements) {
    return elements;
  }

  private static <T> Object[] varargsToObjectArray(T... elements) {
    return elements;
  }

  private static void testParameterizedLambdaUnboxing() {

    Function<Object> ordinalWithCast = e -> ((PlainJsEnum) e).ordinal();
    assertTrue(1 == ordinalWithCast.apply(PlainJsEnum.ONE));

    // TODO(b/120087079): uncomment when parameterizing lambda with non-native JsEnums is supported.
    // Since the lambda is not a class and cannot not have bridge methods, if the cast
    // check/conversions triggered by specialization is not present the following code would not
    // unbox "e" and would end up returning a boxed enum instead of the ordinal.
    // Function<PlainJsEnum> ordinal = e -> e.ordinal();
    // assertTrue(1 == ordinal.apply(PlainJsEnum.ONE));
  }

  interface Function<T> {
    int apply(T t);
  }

  @JsMethod
  // Pass through an enum value as if it were coming from and going to JavaScript.
  private static Object passThrough(Object o) {
    // Supported closure enums can only have number, boolean or string as their underlying type.
    // Make sure that boxed enums are not passing though here.
    assertTrue(o instanceof String || o instanceof Double || o instanceof Boolean);
    return o;
  }

  private static <T> void assertSameType(Class<?> expectedType, @DoNotAutobox T actual) {
    // Makes sure that even if the type variable T is inferred to be JsEnum, boxing still
    // happens.
    assertTrue(
        "Not true that actual type <"
            + actual.getClass().getCanonicalName()
            + "> is equals to expected type <"
            + expectedType.getCanonicalName()
            + ">.",
        expectedType == actual.getClass());
  }

  private static void assertThrowsClassCastException(Supplier<?> supplier) {
    assertThrows(
        ClassCastException.class,
        () -> {
          supplier.get();
        });
  }

  private static <T> void assertThrows(Class<?> exceptionClass, Runnable runnable) {
    try {
      runnable.run();
    } catch (Throwable e) {
      if (e.getClass() == exceptionClass) {
        return;
      }
    }
    fail("Should have thrown " + exceptionClass.getSimpleName());
  }

  private static void assertTrue(boolean condition) {
    assert condition;
  }

  private static void assertTrue(String message, boolean condition) {
    assert condition : message;
  }

  private static void assertFalse(boolean condition) {
    assertTrue(!condition);
  }

  private static void fail() {
    assert false;
  }

  private static void fail(String message) {
    assert false : message;
  }
}
