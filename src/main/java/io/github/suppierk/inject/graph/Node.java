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

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.InjectorReference;
import io.github.suppierk.inject.Key;
import jakarta.inject.Provider;
import java.io.Closeable;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Defines a baseline structure for the dependency graph node.
 *
 * @param <T> is the type of the instance this node refers to
 */
public abstract class Node<T> implements Provider<T>, Supplier<T>, Closeable {
  private static final Consumer<?> EMPTY_CONSUMER = t -> {};

  private final InjectorReference injectorReference;
  private final Set<Key<?>> parentKeys;

  /**
   * Default constructor.
   *
   * @param injectorReference for dependency lookups
   * @param parentKeys for integrity and cycle check lookups
   */
  protected Node(InjectorReference injectorReference, Set<Key<?>> parentKeys) {
    if (injectorReference == null) {
      throw new IllegalArgumentException("Injector reference is null");
    }

    if (parentKeys == null) {
      throw new IllegalArgumentException("Parent qualifiers is null");
    }

    this.injectorReference = injectorReference;
    this.parentKeys = Set.copyOf(parentKeys);
  }

  protected InjectorReference injectorReference() {
    return injectorReference;
  }

  @SuppressWarnings("squid:S1452")
  public Set<Key<?>> parentKeys() {
    return parentKeys;
  }

  @SuppressWarnings("squid:S1452")
  public Set<Key<?>> requiredParentKeys() {
    return parentKeys();
  }

  /**
   * Allows creating a copy of the current node for the new {@link Injector} instance.
   *
   * @param newInjector reference to be used by the copied node
   * @return a new instance of the node retaining all properties from its source
   */
  public abstract Node<T> copy(InjectorReference newInjector);

  /**
   * Renders particular {@link Node} as YAML for human-readable option to inspect dependencies.
   *
   * @param indentationLevel for the generated YAML fragment
   * @return YAML string
   */
  public abstract String toYamlString(int indentationLevel);

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Node)) return false;
    Node<?> that = (Node<?>) o;
    return Objects.equals(parentKeys, that.parentKeys);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(parentKeys);
  }

  /**
   * Similar to {@link Optional#empty()} but for {@link Consumer}.
   *
   * @return empty {@link Consumer}
   * @param <C> is the type of the consumed object
   */
  protected static <C> Consumer<C> emptyConsumer() {
    @SuppressWarnings("unchecked")
    Consumer<C> emptyConsumer = (Consumer<C>) EMPTY_CONSUMER;
    return emptyConsumer;
  }

  /**
   * Identify default behavior to clean up resources for the given class.
   *
   * <p><b>NOTE</b>: {@link Closeable} extends {@link AutoCloseable}.
   *
   * @param clazz to identify behavior for
   * @return {@link Consumer} to handle resource clean up
   * @param <T> is the type of the potentially {@link AutoCloseable} resource
   */
  protected static <T> Consumer<T> createOnCloseConsumer(Class<T> clazz) {
    if (AutoCloseable.class.isAssignableFrom(clazz)) {
      return resource -> {
        try {
          ((AutoCloseable) resource).close();
        } catch (Exception e) {
          throw new IllegalStateException("Could not close resource", e);
        }
      };
    }

    return emptyConsumer();
  }
}
