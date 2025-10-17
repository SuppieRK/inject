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

package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class InjectorCopyBuilderTest {

  static class Dependency {}

  @Singleton
  static class OriginalService implements AutoCloseable {
    final Injector injector;
    final Dependency dependency;
    boolean closed;

    @Inject
    OriginalService(Injector injector, Dependency dependency) {
      this.injector = injector;
      this.dependency = dependency;
    }

    @Override
    public void close() {
      closed = true;
    }
  }

  @Singleton
  static class ReplacementService extends OriginalService {
    @Inject
    ReplacementService(Injector injector, Dependency dependency) {
      super(injector, dependency);
    }
  }

  @Test
  void replaceOnCopyBuilderKeepsCloseOrder() {
    final Injector base = Injector.injector().add(Dependency.class, OriginalService.class).build();

    final OriginalService original = base.get(OriginalService.class);
    assertSame(base, original.injector, "Original service must capture injector reference");

    final Injector replaced =
        base.copy().replace(OriginalService.class, ReplacementService.class).build();

    final ReplacementService replacement = replaced.get(ReplacementService.class);
    assertSame(replaced, replacement.injector, "Replacement must capture new injector instance");

    replaced.close();

    assertTrue(
        replacement.closed, "Replacement singleton must be closed when injector copy is closed");
  }
}
