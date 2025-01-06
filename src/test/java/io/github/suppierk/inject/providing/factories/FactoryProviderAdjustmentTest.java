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

package io.github.suppierk.inject.providing.factories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class FactoryProviderAdjustmentTest {
  static class DependencyProvider {
    @Provides
    @Named("original")
    String original() {
      return "original";
    }

    @Provides
    @Named("adjusted")
    String adjusted(@Named("original") String original) {
      return original + "Adjusted";
    }
  }

  static class Consumer {
    private final String result;

    @Inject
    Consumer(@Named("adjusted") String adjusted) {
      this.result = adjusted;
    }
  }

  @Test
  void invokesFactoryMethodToProvideDependencies() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(DependencyProvider.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(Consumer.class), "There must be no problem with adding consumer");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertEquals(
        "originalAdjusted",
        injector.get(Consumer.class).result,
        "Factory methods within the same class must be able to be invoked independently to provide the opportunity to chain and adjust dependencies as needed");
  }
}
