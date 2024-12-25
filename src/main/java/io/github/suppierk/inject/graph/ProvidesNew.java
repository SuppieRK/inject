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
import io.github.suppierk.utils.ConsoleConstants;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Defines a node which instantiates a new class instance by calling a method of the existing
 * instance retrieved from {@link ConstructsNew}.
 *
 * @param <T> is the type of the instance this node refers to
 */
public class ProvidesNew<T> extends ReflectionNode<T> {
  protected final Key<?> classKey;
  protected final Method method;
  protected final Class<T> methodReturnClass;

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
  public ProvidesNew(
      InjectorReference injectorReference,
      Key<?> classKey,
      Method method,
      Class<T> methodReturnClass,
      List<ParameterInformation> parametersInformation,
      List<FieldInformation> fieldsInformation) {
    super(injectorReference, parametersInformation, fieldsInformation, classKey);

    if (method == null) {
      throw new IllegalArgumentException("Method is null");
    }

    if (methodReturnClass == null) {
      throw new IllegalArgumentException("Method return class is null");
    }

    this.classKey = classKey;
    this.method = method;
    this.methodReturnClass = methodReturnClass;
  }

  @SuppressWarnings("squid:S1452")
  public Key<?> getClassKey() {
    return classKey;
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public T get() {
    final var objectInstance = injectorReference().getNode(classKey).get();

    try {
      if (method.trySetAccessible()) {
        return injectFields((T) method.invoke(objectInstance, createArguments()));
      } else {
        throw new IllegalAccessException(
            String.format(
                "Cannot set accessible flag for %s in %s",
                method.getName(), classKey.type().getName()));
      }
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          String.format("Cannot access %s in %s", method.getName(), classKey.type().getName()), e);
    }
  }

  /** {@inheritDoc} */
  @Override
  public Node<T> copy(InjectorReference newInjector) {
    return new ProvidesNew<>(
        newInjector,
        classKey,
        method,
        methodReturnClass,
        parametersInformation(),
        fieldsInformation());
  }

  /** {@inheritDoc} */
  @Override
  public void close() {
    // Factory node cannot close created instances
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProvidesNew)) return false;
    if (!super.equals(o)) return false;
    ProvidesNew<?> that = (ProvidesNew<?>) o;
    return Objects.equals(classKey, that.classKey)
        && Objects.equals(method, that.method)
        && Objects.equals(methodReturnClass, that.methodReturnClass);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), classKey, method, methodReturnClass);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format(
        "%s(from %s ## %s)",
        getClass().getSimpleName(), classKey.type().getName(), method.getName());
  }

  /** {@inheritDoc} */
  @Override
  public String toYamlString(int indentationLevel) {
    return toYamlString(indentationLevel, false);
  }

  /**
   * Shared logic to create YAML structure for the current {@link Node}.
   *
   * @param indentationLevel for the generated YAML fragment
   * @param isSingleton to set the field in the YAML string
   * @return YAML string
   */
  protected String toYamlString(int indentationLevel, boolean isSingleton) {
    final var indent = ConsoleConstants.indent(indentationLevel);
    final var nestedIndent = ConsoleConstants.indent(indentationLevel + 1);

    return String.format(
        "%sinstance:%n%s",
        indent,
        String.format(
            "%ssingleton: %s%n%smethod: %s%n%sparameters:%s%n%sfields:%s",
            nestedIndent,
            ConsoleConstants.blueBold(Boolean.toString(isSingleton)),
            nestedIndent,
            String.format(
                "%s.%s",
                ConsoleConstants.cyanBold(method.getDeclaringClass().getName()), method.getName()),
            nestedIndent,
            parametersInformation().isEmpty()
                ? ConsoleConstants.YAML_EMPTY_ARRAY
                : String.format(
                    "%n%s",
                    parametersInformation().stream()
                        .map(
                            info -> info.getQualifierKey().toYamlString(true, indentationLevel + 2))
                        .collect(Collectors.joining(String.format("%n")))),
            nestedIndent,
            fieldsInformation().isEmpty()
                ? ConsoleConstants.YAML_EMPTY_ARRAY
                : String.format(
                    "%n%s",
                    fieldsInformation().stream()
                        .map(
                            info -> info.getQualifierKey().toYamlString(true, indentationLevel + 2))
                        .collect(Collectors.joining(String.format("%n"))))));
  }
}
