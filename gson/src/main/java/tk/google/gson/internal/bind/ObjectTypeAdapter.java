/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tk.google.gson.internal.bind;

import tk.google.gson.Gson;
import tk.google.gson.ToNumberStrategy;
import tk.google.gson.ToNumberPolicy;
import tk.google.gson.TypeAdapter;
import tk.google.gson.TypeAdapterFactory;
import tk.google.gson.internal.LinkedTreeMap;
import tk.google.gson.reflect.TypeToken;
import tk.google.gson.stream.JsonReader;
import tk.google.gson.stream.JsonToken;
import tk.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapts types whose static type is only 'Object'. Uses getClass() on
 * serialization and a primitive/Map/List on deserialization.
 */
public final class ObjectTypeAdapter extends TypeAdapter<Object> {
  /**
   * Gson default factory using {@link ToNumberPolicy#DOUBLE}.
   */
  private static final TypeAdapterFactory DOUBLE_FACTORY = newFactory(ToNumberPolicy.DOUBLE);

  private final Gson gson;
  private final ToNumberStrategy toNumberStrategy;

  private ObjectTypeAdapter(Gson gson, ToNumberStrategy toNumberStrategy) {
    this.gson = gson;
    this.toNumberStrategy = toNumberStrategy;
  }

  private static TypeAdapterFactory newFactory(final ToNumberStrategy toNumberStrategy) {
    return new TypeAdapterFactory() {
      @SuppressWarnings("unchecked")
      @Override public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
        if (type.getRawType() == Object.class) {
          return (TypeAdapter<T>) new ObjectTypeAdapter(gson, toNumberStrategy);
        }
        return null;
      }
    };
  }

  public static TypeAdapterFactory getFactory(ToNumberStrategy toNumberStrategy) {
    if (toNumberStrategy == ToNumberPolicy.DOUBLE) {
      return DOUBLE_FACTORY;
    } else {
      return newFactory(toNumberStrategy);
    }
  }

  @Override public Object read(JsonReader in) throws IOException {
    JsonToken token = in.peek();
    switch (token) {
    case BEGIN_ARRAY:
      List<Object> list = new ArrayList<>();
      in.beginArray();
      while (in.hasNext()) {
        list.add(read(in));
      }
      in.endArray();
      return list;

    case BEGIN_OBJECT:
      Map<String, Object> map = new LinkedTreeMap<>();
      in.beginObject();
      while (in.hasNext()) {
        map.put(in.nextName(), read(in));
      }
      in.endObject();
      return map;

    case STRING:
      return in.nextString();

    case NUMBER:
      return toNumberStrategy.readNumber(in);

    case BOOLEAN:
      return in.nextBoolean();

    case NULL:
      in.nextNull();
      return null;

    default:
      throw new IllegalStateException();
    }
  }

  @SuppressWarnings("unchecked")
  @Override public void write(JsonWriter out, Object value) throws IOException {
    if (value == null) {
      out.nullValue();
      return;
    }

    TypeAdapter<Object> typeAdapter = (TypeAdapter<Object>) gson.getAdapter(value.getClass());
    if (typeAdapter instanceof ObjectTypeAdapter) {
      out.beginObject();
      out.endObject();
      return;
    }

    typeAdapter.write(out, value);
  }
}
