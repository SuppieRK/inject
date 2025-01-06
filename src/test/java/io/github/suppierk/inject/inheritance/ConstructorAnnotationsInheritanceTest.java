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

package io.github.suppierk.inject.inheritance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class ConstructorAnnotationsInheritanceTest {
  static class ParentConsumer {
    private final Long value;

    @Inject
    ParentConsumer(Long value) {
      this.value = value;
    }

    public Long getValue() {
      return value;
    }
  }

  static class ChildConsumer extends ParentConsumer {
    ChildConsumer(Long value) {
      super(value);
    }
  }

  @Test
  void mustThrowException() {
    final var value = System.nanoTime();

    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(value), "There must be no problem adding value");
    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(ChildConsumer.class),
        "Because child class must use super constructor in one of its constructors, child classes without constructors explicitly marked by @Inject must throw an exception");
  }
}
