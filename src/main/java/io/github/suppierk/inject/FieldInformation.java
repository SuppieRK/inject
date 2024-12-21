/*
 * MIT License
 *
 * Copyright 2024 Roman Khlebnov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.suppierk.inject;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.StringJoiner;

/** Defines base information about specific class field. */
public final class FieldInformation {
  private final Field field;
  private final Key<?> key;
  private final Class<?> wrapper;

  public FieldInformation(Field field, Key<?> key, Class<?> wrapper) {
    if (field == null) {
      throw new IllegalArgumentException("Field is null");
    }

    if (key == null) {
      throw new IllegalArgumentException("Qualifier key is null");
    }

    this.field = field;
    this.key = key;
    this.wrapper = wrapper;
  }

  public Field getField() {
    return field;
  }

  @SuppressWarnings("squid:S1452")
  public Key<?> getQualifierKey() {
    return key;
  }

  @SuppressWarnings("squid:S1452")
  public Class<?> getWrapper() {
    return wrapper;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof FieldInformation)) return false;
    FieldInformation that = (FieldInformation) o;
    return Objects.equals(field, that.field)
        && Objects.equals(key, that.key)
        && Objects.equals(wrapper, that.wrapper);
  }

  @Override
  public int hashCode() {
    return Objects.hash(field, key, wrapper);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", FieldInformation.class.getSimpleName() + "[", "]")
        .add("field=" + field)
        .add("qualifierKey=" + key)
        .add("wrapper=" + wrapper)
        .toString();
  }
}
