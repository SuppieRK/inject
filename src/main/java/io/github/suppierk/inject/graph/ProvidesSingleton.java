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

import io.github.suppierk.inject.FieldInformation;
import io.github.suppierk.inject.InjectorReference;
import io.github.suppierk.inject.Key;
import io.github.suppierk.inject.ParameterInformation;
import io.github.suppierk.utils.Memoized;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

/**
 * Defines a node which calls {@link ProvidesNew} logic to instantiate the value and stores
 * instantiated value inside {@link Memoized} for later access.
 *
 * <p>Memoization is reset on copy.
 *
 * @param <T> is the type of the instance this node refers to
 */
public final class ProvidesSingleton<T> extends ProvidesNew<T> {
  private final Memoized<T> memoized;
  private final Consumer<T> onCloseConsumer;

  /**
   * Default constructor.
   *
   * @param injectorReference for dependency lookups
   * @param classKey of the {@link ConstructsNew} to obtain class instance
   * @param method to call on the class instance
   * @param methodReturnClass to call on the class instance
   * @param parametersInformation of the method to be invoked during dependency injection
   * @param fieldsInformation of the class to be set during dependency injection
   * @throws IllegalArgumentException if class key or method are {@code null}
   */
  public ProvidesSingleton(
      InjectorReference injectorReference,
      Key<?> classKey,
      Method method,
      Class<T> methodReturnClass,
      List<ParameterInformation> parametersInformation,
      List<FieldInformation> fieldsInformation) {
    this(
        injectorReference,
        classKey,
        method,
        methodReturnClass,
        createOnCloseConsumer(methodReturnClass),
        parametersInformation,
        fieldsInformation);
  }

  /**
   * Copy constructor.
   *
   * @param injectorReference for dependency lookups
   * @param classKey of the {@link ConstructsNew} to obtain class instance
   * @param method to call on the class instance
   * @param parametersInformation of the method to be invoked during dependency injection
   * @param fieldsInformation of the class to be set during dependency injection
   * @throws IllegalArgumentException if class key or method are {@code null}
   */
  private ProvidesSingleton(
      InjectorReference injectorReference,
      Key<?> classKey,
      Method method,
      Class<T> methodReturnClass,
      Consumer<T> onCloseConsumer,
      List<ParameterInformation> parametersInformation,
      List<FieldInformation> fieldsInformation) {
    super(
        injectorReference,
        classKey,
        method,
        methodReturnClass,
        parametersInformation,
        fieldsInformation);
    this.memoized = Memoized.memoizedProvider(super::get);
    this.onCloseConsumer = onCloseConsumer;
  }

  /** {@inheritDoc} */
  @Override
  public T get() {
    return memoized.get();
  }

  /** {@inheritDoc} */
  @Override
  public Node<T> copy(InjectorReference newInjector) {
    return new ProvidesSingleton<>(
        newInjector,
        classKey,
        method,
        methodReturnClass,
        onCloseConsumer,
        parametersInformation(),
        fieldsInformation());
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    memoized.ifPresent(onCloseConsumer);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProvidesSingleton)) return false;
    return super.equals(o);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  /** {@inheritDoc} */
  @Override
  public String toYamlString(int indentationLevel) {
    return toYamlString(indentationLevel, true);
  }
}
