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
import io.github.suppierk.inject.ParameterInformation;
import io.github.suppierk.utils.ConsoleConstants;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a node which instantiates a new class instance by calling its constructor.
 *
 * @param <T> is the type of the instance this node refers to
 */
public class ConstructsNew<T> extends ReflectionNode<T> {
  protected final Constructor<T> constructor;

  /**
   * Default constructor.
   *
   * @param injectorReference for dependency lookups
   * @param constructor of the class to invoke to create a new instance
   * @param parametersInformation of the constructor to be invoked during dependency injection
   * @param fieldsInformation of the class to be set during dependency injection
   * @throws IllegalArgumentException if constructor is {@code null}
   */
  public ConstructsNew(
      InjectorReference injectorReference,
      Constructor<T> constructor,
      List<ParameterInformation> parametersInformation,
      List<FieldInformation> fieldsInformation) {
    super(injectorReference, parametersInformation, fieldsInformation);

    if (constructor == null) {
      throw new IllegalArgumentException("Constructor is null");
    }

    this.constructor = constructor;
  }

  @Override
  public T get() {
    try {
      if (constructor.trySetAccessible()) {
        final var args = createArguments();
        final var instance = constructor.newInstance(args);
        return injectFields(instance);
      } else {
        throw new IllegalStateException(
            String.format(
                "Cannot access %s constructor", constructor.getDeclaringClass().getName()));
      }
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          String.format("Cannot access %s constructor", constructor.getDeclaringClass().getName()),
          e);
    }
  }

  @Override
  public Node<T> copy(InjectorReference newInjector) {
    return new ConstructsNew<>(
        newInjector, constructor, parametersInformation(), fieldsInformation());
  }

  /** Constructor equality leverages parameters, which we already check in superclass. */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ConstructsNew)) return false;
    return super.equals(o);
  }

  /**
   * Constructor hash code leverages constant class name, not parameters - we use parameters in
   * superclass.
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return String.format("%s()", getClass().getSimpleName());
  }

  public String toYamlString(int indentationLevel) {
    return toYamlString(indentationLevel, false);
  }

  protected String toYamlString(int indentationLevel, boolean isSingleton) {
    final var indent = ConsoleConstants.indent(indentationLevel);
    final var nestedIndent = ConsoleConstants.indent(indentationLevel + 1);

    return String.format(
        "%sinstance:%n%s",
        indent,
        String.format(
            "%ssingleton: %s%n%sconstructor:%s%n%sfields:%s",
            nestedIndent,
            ConsoleConstants.blueBold(Boolean.toString(isSingleton)),
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
