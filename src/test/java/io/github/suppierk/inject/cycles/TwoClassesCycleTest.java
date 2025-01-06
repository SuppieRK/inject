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

package io.github.suppierk.inject.cycles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class TwoClassesCycleTest {
  static class First {
    final Second second;

    @Inject
    First(Second second) {
      this.second = second;
    }
  }

  static class Second {
    final First first;

    @Inject
    Second(First first) {
      this.first = first;
    }
  }

  @Test
  void throwsExceptionForCycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(
        () -> builder.add(First.class), "First cycle element must not cause problems");
    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(Second.class),
        "Second cycle element must throw an exception");
  }

  @Test
  void throwsExceptionForCycleRegardlessOfTheDeclarationOrder() {
    final var builder = Injector.injector();

    assertDoesNotThrow(
        () -> builder.add(Second.class), "First cycle element must not cause problems");
    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(First.class),
        "Second cycle element must throw an exception");
  }
}
