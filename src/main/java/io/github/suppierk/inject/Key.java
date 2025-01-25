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

import io.github.suppierk.utils.ConsoleConstants;
import jakarta.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Defines a map key for the qualified object lookup.
 *
 * @param <T> is the type of the object
 */
public final class Key<T> {
  private final Class<T> type;
  private final Set<AnnotationWrapper> annotationWrappers;

  public Key(Class<T> type, Set<Annotation> annotations) {
    if (type == null) {
      throw new IllegalArgumentException("Type is null");
    } else {
      this.type = type;
    }

    if (annotations == null) {
      this.annotationWrappers = Set.of();
    } else {
      this.annotationWrappers =
          annotations.stream()
              .map(
                  annotation -> {
                    if (!annotation.annotationType().isAnnotationPresent(Qualifier.class)) {
                      throw new IllegalArgumentException(
                          "Annotation @"
                              + annotation.annotationType().getName()
                              + " is not a @Qualifier");
                    }

                    return new AnnotationWrapper(annotation);
                  })
              .collect(Collectors.toUnmodifiableSet());
    }
  }

  public Class<T> type() {
    return type;
  }

  public Set<AnnotationWrapper> annotationWrappers() {
    return annotationWrappers;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Key)) {
      return false;
    }

    Key<?> that = (Key<?>) o;
    return Objects.equals(type, that.type)
        && Objects.equals(annotationWrappers, that.annotationWrappers);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, annotationWrappers);
  }

  @Override
  public String toString() {
    return String.format(
        "%s(%s%s)",
        Key.class.getSimpleName(),
        type.getName(),
        annotationWrappers.isEmpty()
            ? ""
            : String.format(
                " as [%s]",
                annotationWrappers.stream()
                    .map(AnnotationWrapper::toString)
                    .collect(Collectors.joining(", "))));
  }

  /**
   * Renders particular {@link Key} as YAML for human-readable option to inspect dependencies.
   *
   * @param itemize denotes whether this key must represent YAML array element
   * @param indentationLevel for the generated YAML fragment
   * @return YAML string
   */
  public String toYamlString(boolean itemize, int indentationLevel) {
    final var actualIndent = itemize ? indentationLevel + 1 : indentationLevel;
    final var firstIndent =
        itemize
            ? ConsoleConstants.indent(indentationLevel) + ConsoleConstants.YAML_ITEM
            : ConsoleConstants.indent(actualIndent);
    final var nestedIndent = ConsoleConstants.indent(actualIndent);

    return String.format(
        "%stype: %s%n%sannotations:%s",
        firstIndent,
        ConsoleConstants.cyanBold(type.getName()),
        nestedIndent,
        annotationWrappers().isEmpty()
            ? ConsoleConstants.YAML_EMPTY_ARRAY
            : String.format(
                "%n%s",
                annotationWrappers().stream()
                    .map(
                        annotationWrapper ->
                            String.format(
                                "%s%s'%s'",
                                ConsoleConstants.indent(actualIndent + 1),
                                ConsoleConstants.YAML_ITEM,
                                annotationString(annotationWrapper.annotation())))
                    .collect(Collectors.joining(String.format("%n")))));
  }

  private String annotationString(Annotation annotation) {
    return annotation
        .toString()
        .replace(
            annotation.annotationType().getName(),
            ConsoleConstants.yellow(annotation.annotationType().getName()));
  }
}
