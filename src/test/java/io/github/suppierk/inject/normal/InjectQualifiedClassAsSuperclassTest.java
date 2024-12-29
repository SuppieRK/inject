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

package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class InjectQualifiedClassAsSuperclassTest {
  @Singleton
  @Named("testA")
  static class A implements Runnable {
    private final long id;

    A() {
      this.id = System.nanoTime();
    }

    @Override
    public void run() {
      // Do nothing
    }
  }

  static class C implements Runnable {
    private final Runnable r;

    @Inject
    C(@Named("testA") Runnable r) {
      this.r = r;
    }

    @Override
    public void run() {
      r.run();
    }
  }

  @Test
  void must_fail_to_inject_dependency() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(C.class));
    assertThrows(IllegalArgumentException.class, builder::build);
  }
}
