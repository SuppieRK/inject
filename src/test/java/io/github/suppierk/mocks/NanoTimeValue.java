/*
 * MIT License
 *
 * Copyright 2025 Roman Khlebnov
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

package io.github.suppierk.mocks;

import java.util.Objects;
import java.util.StringJoiner;

/**
 * Multiple tests need some form of a value class to verify functionality.
 *
 * <p>This class serves as the simplest form of a value class to supply to those tests, reducing
 * duplicates.
 */
public class NanoTimeValue {
  private final String value;

  public NanoTimeValue() {
    this("");
  }

  public NanoTimeValue(final String prefix) {
    this.value = prefix + System.nanoTime();
  }

  public String getValue() {
    return value;
  }

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof NanoTimeValue)) return false;
    NanoTimeValue value1 = (NanoTimeValue) o;
    return Objects.equals(value, value1.value);
  }

  @Override
  public final int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public final String toString() {
    return new StringJoiner(", ", NanoTimeValue.class.getSimpleName() + "[", "]")
        .add("value=" + value)
        .toString();
  }
}
