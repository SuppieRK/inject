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
import jakarta.inject.Provider;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Defines a baseline structure for the dependency graph nodes which can produce new object
 * instances.
 *
 * @param <T> is the type of the instance this node refers to
 */
public abstract class ReflectionNode<T> extends Node<T> {
  private final Set<Key<?>> requiredParentKeys;
  private final List<ParameterInformation> parametersInformation;
  private final List<FieldInformation> fieldsInformation;

  /**
   * Default constructor.
   *
   * @param injectorReference for dependency lookups
   * @param parametersInformation of the method to be invoked during dependency injection
   * @param fieldsInformation of the class to be set during dependency injection
   * @param extraParentKeys this node depends upon
   */
  protected ReflectionNode(
      InjectorReference injectorReference,
      List<ParameterInformation> parametersInformation,
      List<FieldInformation> fieldsInformation,
      Key<?>... extraParentKeys) {
    super(
        injectorReference,
        gatherParentKeys(parametersInformation, fieldsInformation, extraParentKeys));
    this.requiredParentKeys =
        gatherRequiredParentKeys(parametersInformation, fieldsInformation, extraParentKeys);
    this.parametersInformation = List.copyOf(parametersInformation);
    this.fieldsInformation = List.copyOf(fieldsInformation);
  }

  public List<ParameterInformation> parametersInformation() {
    return parametersInformation;
  }

  public List<FieldInformation> fieldsInformation() {
    return fieldsInformation;
  }

  @Override
  @SuppressWarnings("squid:S1452")
  public Set<Key<?>> requiredParentKeys() {
    return requiredParentKeys;
  }

  /**
   * Create an array of arguments for instantiation method invocation.
   *
   * @return an array of objects to serve as method arguments
   */
  protected Object[] createArguments() {
    final Object[] args = new Object[parametersInformation().size()];

    for (int i = 0; i < parametersInformation().size(); i++) {
      final var currentParameter = parametersInformation().get(i);
      final var currentParameterNode =
          injectorReference().getNode(currentParameter.getQualifierKey());

      if (currentParameter.getWrapper() == null) {
        args[i] = currentParameterNode.get();
      } else {
        args[i] = currentParameterNode;
      }
    }

    return args;
  }

  /**
   * Perform in-place field injection into an object instance.
   *
   * @param instance to inject fields into
   * @return an original object instance with injected fields
   * @throws IllegalAccessException if we cannot access the field to inject the value into
   */
  @SuppressWarnings("squid:S3011")
  protected T injectFields(T instance) throws IllegalAccessException {
    for (FieldInformation fieldInformation : fieldsInformation()) {
      if (fieldInformation.getField().trySetAccessible()) {
        final var currentFieldNode =
            injectorReference().getNode(fieldInformation.getQualifierKey());

        Object value;
        if (fieldInformation.getWrapper() == null) {
          value = currentFieldNode.get();
        } else {
          value = currentFieldNode;
        }

        fieldInformation.getField().set(instance, value);
      } else {
        throw new IllegalArgumentException("Unable to access field " + fieldInformation.getField());
      }
    }

    return instance;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ReflectionNode)) return false;
    if (!super.equals(o)) return false;
    ReflectionNode<?> that = (ReflectionNode<?>) o;
    return Objects.equals(requiredParentKeys, that.requiredParentKeys)
        && Objects.equals(parametersInformation, that.parametersInformation)
        && Objects.equals(fieldsInformation, that.fieldsInformation);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(), requiredParentKeys, parametersInformation, fieldsInformation);
  }

  /**
   * Shortcut to concat multiple collections together into a single immutable {@link Set}.
   *
   * @param parametersInformation to add to the concatenation
   * @param fieldsInformation to add to the concatenation
   * @param extraParentKeys to add to the concatenation
   * @return an immutable set, truncated to the number of its elements
   */
  private static Set<Key<?>> gatherParentKeys(
      List<ParameterInformation> parametersInformation,
      List<FieldInformation> fieldsInformation,
      Key<?>... extraParentKeys) {
    if (parametersInformation == null) {
      throw new IllegalArgumentException("parametersInformation is null");
    }

    if (fieldsInformation == null) {
      throw new IllegalArgumentException("fieldsInformation is null");
    }

    final var result = new HashSet<Key<?>>(parametersInformation.size() + fieldsInformation.size());

    for (ParameterInformation parameterInformation : parametersInformation) {
      result.add(parameterInformation.getQualifierKey());
    }

    for (FieldInformation fieldInformation : fieldsInformation) {
      result.add(fieldInformation.getQualifierKey());
    }

    if (extraParentKeys != null) {
      for (Key<?> key : extraParentKeys) {
        if (key == null) {
          throw new IllegalArgumentException("Extra parent key is null");
        } else {
          result.add(key);
        }
      }
    }

    return Set.copyOf(result);
  }

  /**
   * Shortcut similar to {@link #gatherParentKeys(List, List, Key[])}, but filtering out supported
   * wrappers intended to break cyclic dependencies - namely {@link Provider} and {@link Supplier}.
   *
   * @param parametersInformation to add to the concatenation
   * @param fieldsInformation to add to the concatenation
   * @param extraParentKeys to add to the concatenation
   * @return an immutable set, truncated to the number of its elements
   */
  private static Set<Key<?>> gatherRequiredParentKeys(
      List<ParameterInformation> parametersInformation,
      List<FieldInformation> fieldsInformation,
      Key<?>... extraParentKeys) {
    final var result = new HashSet<Key<?>>(parametersInformation.size() + fieldsInformation.size());

    for (ParameterInformation parameterInformation : parametersInformation) {
      if (isNotSupportedWrapperClass(parameterInformation.getWrapper())) {
        result.add(parameterInformation.getQualifierKey());
      }
    }

    for (FieldInformation fieldInformation : fieldsInformation) {
      if (isNotSupportedWrapperClass(fieldInformation.getWrapper())) {
        result.add(fieldInformation.getQualifierKey());
      }
    }

    if (extraParentKeys != null) {
      Collections.addAll(result, extraParentKeys);
    }

    return Set.copyOf(result);
  }

  /**
   * Checks if current {@link Class} is either {@link Provider} or {@link Supplier}.
   *
   * @param clazz to check
   * @return {@code true}, if the {@link Class} is either {@link Provider} or {@link Supplier}
   */
  public static boolean isNotSupportedWrapperClass(Class<?> clazz) {
    return !Provider.class.equals(clazz) && !Supplier.class.equals(clazz);
  }
}
