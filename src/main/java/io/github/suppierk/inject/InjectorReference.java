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

import io.github.suppierk.inject.graph.Node;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Value class which holds a reference to a particular instance of {@link Injector}.
 *
 * <p>The intent here is to have ratchet-like functionality for {@link Node} to access other nodes
 * registered in {@link Injector}.
 *
 * <p>However, due to builders {@link Injector} gets created after {@link Node}s, so this class also
 * helps to break this cycle.
 */
@SuppressWarnings({"javaarchitecture:S7091", "javaarchitecture:S7027"})
public final class InjectorReference {
  private final AtomicReference<Injector> reference;

  /** Default constructor. */
  public InjectorReference() {
    this.reference = new AtomicReference<>(null);
  }

  /**
   * Sets the reference to the injector using atomic CAS.
   *
   * @param injector to set
   * @throws IllegalArgumentException if {@link Injector} instance to set is {@code null}
   */
  public void set(Injector injector) {
    if (injector == null) {
      throw new IllegalStateException("Injector to set is null");
    }

    reference.compareAndSet(null, injector);
  }

  /**
   * Retrieves dependency graph node by delegating a call to {@link Injector#getNode(Key)}.
   *
   * @param key of the dependency to fetch
   * @return a respective dependency graph node which instantiates this particular dependency
   * @param <T> is the type of the dependency
   * @throws IllegalStateException because by the time this is called we expect {@link
   *     #set(Injector)} to be called and reference to be set
   */
  public <T> Node<T> getNode(Key<T> key) {
    if (reference.get() == null) {
      throw new IllegalStateException("No Injector available");
    }

    return reference.get().getNode(key);
  }
}
