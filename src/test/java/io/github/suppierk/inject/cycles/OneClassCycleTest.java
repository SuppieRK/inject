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

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class OneClassCycleTest {
  static class SelfCycle {
    final SelfCycle selfCycle;

    @Inject
    SelfCycle(SelfCycle selfCycle) {
      this.selfCycle = selfCycle;
    }
  }

  @Test
  void throwsExceptionForCycle() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(SelfCycle.class),
        "Self cycle element must throw an exception");
  }
}
