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

package io.github.suppierk.inject.graph;

import io.github.suppierk.inject.InjectorReference;
import io.github.suppierk.utils.ConsoleConstants;
import java.util.Objects;
import java.util.Set;

/**
 * Defines dependency node with already instantiated value.
 *
 * @param <T> is the type of the instance this node refers to
 */
public final class Value<T> extends Node<T> {
  private final T instance;

  /**
   * Default constructor.
   *
   * @param injectorReference for dependency lookups
   * @param instance to set
   * @throws IllegalArgumentException if the value is {@code null}
   */
  public Value(InjectorReference injectorReference, T instance) {
    super(injectorReference, Set.of());

    if (instance == null) {
      throw new IllegalArgumentException("Value is null");
    }

    this.instance = instance;
  }

  @Override
  public T get() {
    return instance;
  }

  @Override
  public Node<T> copy(InjectorReference newInjector) {
    return new Value<>(newInjector, instance);
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Value)) return false;
    Value<?> value = (Value<?>) o;
    return Objects.equals(this.instance, value.instance);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(instance);
  }

  @Override
  public String toString() {
    return String.format("%s(%s)", getClass().getSimpleName(), instance);
  }

  public String toYamlString(int indentationLevel) {
    final var indent = ConsoleConstants.indent(indentationLevel);
    final var nestedIndent = ConsoleConstants.indent(indentationLevel + 1);

    return String.format(
        "%sinstance:%n%s",
        indent, String.format("%ssingleton: %s", nestedIndent, ConsoleConstants.blueBold("true")));
  }
}
