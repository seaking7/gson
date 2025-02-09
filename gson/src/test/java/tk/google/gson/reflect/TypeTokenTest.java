/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.google.gson.reflect;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;
import java.util.Set;
import junit.framework.TestCase;

/**
 * @author Jesse Wilson
 */
public final class TypeTokenTest extends TestCase {
  // These fields are accessed using reflection by the tests below
  List<Integer> listOfInteger = null;
  List<Number> listOfNumber = null;
  List<String> listOfString = null;
  List<?> listOfUnknown = null;
  List<Set<String>> listOfSetOfString = null;
  List<Set<?>> listOfSetOfUnknown = null;

  @SuppressWarnings({"deprecation"})
  public void testIsAssignableFromRawTypes() {
    assertTrue(TypeToken.get(Object.class).isAssignableFrom(String.class));
    assertFalse(TypeToken.get(String.class).isAssignableFrom(Object.class));
    assertTrue(TypeToken.get(RandomAccess.class).isAssignableFrom(ArrayList.class));
    assertFalse(TypeToken.get(ArrayList.class).isAssignableFrom(RandomAccess.class));
  }

  @SuppressWarnings({"deprecation"})
  public void testIsAssignableFromWithTypeParameters() throws Exception {
    Type a = getClass().getDeclaredField("listOfInteger").getGenericType();
    Type b = getClass().getDeclaredField("listOfNumber").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfInteger = listOfNumber; // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    // listOfNumber = listOfInteger; // doesn't compile; must be false
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

  @SuppressWarnings({"deprecation"})
  public void testIsAssignableFromWithBasicWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfString = listOfUnknown  // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    listOfUnknown = listOfString; // compiles; must be true
    // The following assertion is too difficult to support reliably, so disabling
    // assertTrue(TypeToken.get(b).isAssignableFrom(a));
  }

  @SuppressWarnings({"deprecation"})
  public void testIsAssignableFromWithNestedWildcards() throws Exception {
    Type a = getClass().getDeclaredField("listOfSetOfString").getGenericType();
    Type b = getClass().getDeclaredField("listOfSetOfUnknown").getGenericType();
    assertTrue(TypeToken.get(a).isAssignableFrom(a));
    assertTrue(TypeToken.get(b).isAssignableFrom(b));

    // listOfSetOfString = listOfSetOfUnknown; // doesn't compile; must be false
    assertFalse(TypeToken.get(a).isAssignableFrom(b));
    // listOfSetOfUnknown = listOfSetOfString; // doesn't compile; must be false
    assertFalse(TypeToken.get(b).isAssignableFrom(a));
  }

  public void testArrayFactory() {
    TypeToken<?> expectedStringArray = new TypeToken<String[]>() {};
    assertEquals(expectedStringArray, TypeToken.getArray(String.class));

    TypeToken<?> expectedListOfStringArray = new TypeToken<List<String>[]>() {};
    Type listOfString = new TypeToken<List<String>>() {}.getType();
    assertEquals(expectedListOfStringArray, TypeToken.getArray(listOfString));
  }

  public void testParameterizedFactory() {
    TypeToken<?> expectedListOfString = new TypeToken<List<String>>() {};
    assertEquals(expectedListOfString, TypeToken.getParameterized(List.class, String.class));

    TypeToken<?> expectedMapOfStringToString = new TypeToken<Map<String, String>>() {};
    assertEquals(expectedMapOfStringToString, TypeToken.getParameterized(Map.class, String.class, String.class));

    TypeToken<?> expectedListOfListOfListOfString = new TypeToken<List<List<List<String>>>>() {};
    Type listOfString = TypeToken.getParameterized(List.class, String.class).getType();
    Type listOfListOfString = TypeToken.getParameterized(List.class, listOfString).getType();
    assertEquals(expectedListOfListOfListOfString, TypeToken.getParameterized(List.class, listOfListOfString));
  }

  private static class CustomTypeToken extends TypeToken<String> {
  }

  public void testTypeTokenNonAnonymousSubclass() {
    TypeToken<?> typeToken = new CustomTypeToken();
    assertEquals(String.class, typeToken.getRawType());
    assertEquals(String.class, typeToken.getType());
  }

  /**
   * User must only create direct subclasses of TypeToken, but not subclasses
   * of subclasses (...) of TypeToken.
   */
  public void testTypeTokenSubSubClass() {
    class SubTypeToken<T> extends TypeToken<String> {}
    class SubSubTypeToken1<T> extends SubTypeToken<T> {}
    class SubSubTypeToken2 extends SubTypeToken<Integer> {}

    try {
      new SubTypeToken<Integer>() {};
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Must only create direct subclasses of TypeToken", expected.getMessage());
    }

    try {
      new SubSubTypeToken1<Integer>();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Must only create direct subclasses of TypeToken", expected.getMessage());
    }

    try {
      new SubSubTypeToken2();
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("Must only create direct subclasses of TypeToken", expected.getMessage());
    }
  }

  @SuppressWarnings("rawtypes")
  public void testTypeTokenRaw() {
    try {
      new TypeToken() {};
      fail();
    } catch (IllegalStateException expected) {
      assertEquals("TypeToken must be created with a type argument: new TypeToken<...>() {}; "
          + "When using code shrinkers (ProGuard, R8, ...) make sure that generic signatures are preserved.",
          expected.getMessage());
    }
  }
}
