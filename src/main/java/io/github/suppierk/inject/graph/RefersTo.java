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
import io.github.suppierk.inject.Key;
import io.github.suppierk.utils.ConsoleConstants;
import java.util.Set;

/**
 * Defines a node used for performing replacement operations.
 *
 * <p>During replacement, because we copy all previous nodes instead of going through a painful
 * process of recreation for all keys, we simply replace the old node with an instance of this node
 * pointing to the new key.
 *
 * <p>Any call to this key will be resolved following references to the new key.
 *
 * @param <T> is the type of the instance this node refers to
 */
public final class RefersTo<T> extends Node<T> {
  /**
   * Default constructor.
   *
   * @param injectorReference for dependency lookups
   * @param parentKey for integrity and cycle check lookups
   */
  public RefersTo(InjectorReference injectorReference, Key<?> parentKey) {
    super(injectorReference, singleton(parentKey));
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public T get() {
    Node<T> current = this;

    while (RefersTo.class.isAssignableFrom(current.getClass())) {
      current = (Node<T>) injectorReference().getNode(current.parentKeys().iterator().next());
    }

    return current.get();
  }

  /** {@inheritDoc} */
  @Override
  public Node<T> copy(InjectorReference newInjector) {
    return new RefersTo<>(newInjector, parentKeys().iterator().next());
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    // Reference to another node cannot be closed
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof RefersTo)) return false;
    return super.equals(o);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("%s(%s)", getClass().getSimpleName(), parentKeys().iterator().next());
  }

  /** {@inheritDoc} */
  @Override
  public String toYamlString(int indentationLevel) {
    return String.format(
        "%sreferences:%n%s",
        ConsoleConstants.indent(indentationLevel),
        parentKeys().iterator().next().toYamlString(false, indentationLevel + 1));
  }

  /**
   * Null checked singleton set creator.
   *
   * @param key to add to set
   * @return a set consisting of a single element
   */
  @SuppressWarnings("squid:S1452")
  private static Set<Key<?>> singleton(Key<?> key) {
    if (key == null) {
      throw new IllegalArgumentException("Key is null");
    }

    return Set.of(key);
  }
}
