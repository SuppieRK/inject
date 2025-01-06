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

package io.github.suppierk.inject.consuming.mixed;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

class InjectQualifiedClassAsSuperclassTest {
  @Named("testRunnable")
  static class TestRunnable implements Runnable {
    private final long id;

    TestRunnable() {
      this.id = System.nanoTime();
    }

    @Override
    public void run() {
      // Do nothing
    }
  }

  static class DependencyFactory {
    @Provides
    Runnable runnable() {
      return new TestRunnable();
    }
  }

  static class Consumer implements Runnable {
    private final Runnable runnable;

    @Inject
    Consumer(@Named("testRunnable") Runnable runnable) {
      this.runnable = runnable;
    }

    @Override
    public void run() {
      runnable.run();
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(DependencyFactory.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(Consumer.class), "There must be no problem with adding consumer");
    assertThrows(
        IllegalArgumentException.class,
        builder::build,
        "Injector must not be created, because there is no dependency that would fit the expected key in Consumer");
  }
}
