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

import java.lang.reflect.Parameter;
import java.util.Objects;
import java.util.StringJoiner;

/** Defines base information about specific class method parameter. */
public final class ParameterInformation {
  private final Parameter parameter;
  private final Key<?> key;
  private final Class<?> wrapper;

  public ParameterInformation(Parameter parameter, Key<?> key, Class<?> wrapper) {
    if (parameter == null) {
      throw new IllegalArgumentException("Parameter is null");
    }

    if (key == null) {
      throw new IllegalArgumentException("Qualifier key is null");
    }

    this.parameter = parameter;
    this.key = key;
    this.wrapper = wrapper;
  }

  public Parameter getParameter() {
    return parameter;
  }

  @SuppressWarnings("squid:S1452")
  public Key<?> getQualifierKey() {
    return key;
  }

  @SuppressWarnings("squid:S1452")
  public Class<?> getWrapper() {
    return wrapper;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ParameterInformation)) return false;
    ParameterInformation that = (ParameterInformation) o;
    return Objects.equals(parameter, that.parameter)
        && Objects.equals(key, that.key)
        && Objects.equals(wrapper, that.wrapper);
  }

  @Override
  public int hashCode() {
    return Objects.hash(parameter, key, wrapper);
  }

  @Override
  public String toString() {
    return new StringJoiner(", ", ParameterInformation.class.getSimpleName() + "[", "]")
        .add("parameter=" + parameter)
        .add("qualifierKey=" + key)
        .add("wrapper=" + wrapper)
        .toString();
  }
}
