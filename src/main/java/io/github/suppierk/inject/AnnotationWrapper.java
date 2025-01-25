/*
 * MIT License
 *
 * Copyright 2025 Roman Khlebnov
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Objects;

/**
 * Wraps an annotation to be suitable for querying.
 *
 * <p>Typically {@link Annotation} is a proxy of package-private {@link
 * sun.reflect.annotation.AnnotationInvocationHandler} where we are interested to fetch its {@code
 * memberValues} field.
 */
public final class AnnotationWrapper {
  private static final String MEMBER_VALUES_FIELD_NAME = "memberValues";

  private final Annotation annotation;
  private final Map<String, Object> memberValues;
  private final String stringRepresentation;

  @SuppressWarnings("unchecked")
  public AnnotationWrapper(Annotation annotation) {
    try {
      this.annotation = annotation;

      final var invocationHandler = Proxy.getInvocationHandler(annotation);
      final var memberValuesField =
          invocationHandler.getClass().getDeclaredField(MEMBER_VALUES_FIELD_NAME);
      memberValuesField.setAccessible(true);
      this.memberValues =
          Map.copyOf((Map<String, Object>) memberValuesField.get(invocationHandler));

      this.stringRepresentation = annotation.toString();
    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to introspect an annotation " + annotation.toString(), e);
    }
  }

  public Annotation annotation() {
    return annotation;
  }

  public Map<String, Object> memberValues() {
    return memberValues;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AnnotationWrapper)) {
      return false;
    }

    AnnotationWrapper that = (AnnotationWrapper) o;
    return Objects.equals(annotation, that.annotation);
  }

  @Override
  public int hashCode() {
    return annotation.hashCode();
  }

  @Override
  public String toString() {
    return stringRepresentation;
  }
}
