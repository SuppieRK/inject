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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class FactorySingletonProviderNamedTest {
  static class DependencyFactory {
    @Provides
    @Singleton
    String nonNamed() {
      return "simple" + System.nanoTime();
    }

    @Provides
    @Singleton
    @Named("named")
    String named() {
      return "named" + System.nanoTime();
    }
  }

  static class NonNamedConsumer {
    private final String value;

    @Inject
    NonNamedConsumer(String value) {
      this.value = value;
    }
  }

  static class NamedConsumer {
    private final String value;

    @Inject
    NamedConsumer(@Named("named") String value) {
      this.value = value;
    }
  }

  @Test
  void invokesFactoryMethodToProvideDependencies() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(DependencyFactory.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(NonNamedConsumer.class),
        "There must be no problem with adding non named consumer");
    assertDoesNotThrow(
        () -> builder.add(NamedConsumer.class),
        "There must be no problem with adding named consumer");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertTrue(
        injector.get(NonNamedConsumer.class).value.startsWith("simple"),
        "Non named consumer should have non named dependency injected");
    assertTrue(
        injector.get(NamedConsumer.class).value.startsWith("named"),
        "Named consumer should have named dependency injected");
    assertEquals(
        injector.get(NonNamedConsumer.class).value,
        injector.get(NonNamedConsumer.class).value,
        "Because factory provides singleton dependency, injected non named values must be equal");
    assertEquals(
        injector.get(NamedConsumer.class).value,
        injector.get(NamedConsumer.class).value,
        "Because factory provides singleton dependency, injected named values must be equal");
  }
}
