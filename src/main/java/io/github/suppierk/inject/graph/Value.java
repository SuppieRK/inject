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
import java.util.function.Consumer;

/**
 * Defines dependency node with already instantiated value.
 *
 * @param <T> is the type of the instance this node refers to
 */
public final class Value<T> extends Node<T> {
  private final T instance;
  private final Consumer<T> onCloseConsumer;

  /**
   * Default constructor.
   *
   * @param injectorReference for dependency lookups
   * @param instance to set
   * @throws IllegalArgumentException if the value is {@code null}
   */
  public Value(InjectorReference injectorReference, T instance) {
    this(injectorReference, instance, instanceOnCloseConsumer(instance));
  }

  /**
   * Copy constructor.
   *
   * @param injectorReference for dependency lookups
   * @param instance to set
   * @param onCloseConsumer to clean yp resources
   * @throws IllegalArgumentException if the value is {@code null}
   */
  private Value(InjectorReference injectorReference, T instance, Consumer<T> onCloseConsumer) {
    super(injectorReference, Set.of());
    this.instance = instance;
    this.onCloseConsumer = onCloseConsumer;
  }

  /** {@inheritDoc} */
  @Override
  public T get() {
    return instance;
  }

  /** {@inheritDoc} */
  @Override
  public Node<T> copy(InjectorReference newInjector) {
    return new Value<>(newInjector, instance, onCloseConsumer);
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    onCloseConsumer.accept(instance);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Value)) return false;
    Value<?> value = (Value<?>) o;
    return Objects.equals(this.instance, value.instance);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hashCode(instance);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("%s(%s)", getClass().getSimpleName(), instance);
  }

  /** {@inheritDoc} */
  @Override
  public String toYamlString(int indentationLevel) {
    final var indent = ConsoleConstants.indent(indentationLevel);
    final var nestedIndent = ConsoleConstants.indent(indentationLevel + 1);

    return String.format(
        "%sinstance:%n%s",
        indent, String.format("%ssingleton: %s", nestedIndent, ConsoleConstants.blueBold("true")));
  }

  /**
   * Small shortcut to create {@link Consumer} to clean up resources for the given class instance.
   *
   * @param instance to create clean up {@link Consumer}
   * @return {@link Consumer} to clean up resources
   * @param <C> is the type of the instance
   * @throws IllegalArgumentException if the value is {@code null}
   */
  private static <C> Consumer<C> instanceOnCloseConsumer(C instance) {
    if (instance == null) {
      throw new IllegalArgumentException("Value is null");
    }

    @SuppressWarnings("unchecked")
    final Class<C> instanceClass = (Class<C>) instance.getClass();
    return createOnCloseConsumer(instanceClass);
  }
}
