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

package io.github.suppierk.inject.features;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import io.github.suppierk.mocks.CustomQualifier;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

class CustomQualifierTest {
  static class DependencyFactory {
    @Provides
    @Named("named")
    String named() {
      return "named" + System.nanoTime();
    }

    @Provides
    @CustomQualifier
    String custom() {
      return "custom" + System.nanoTime();
    }
  }

  static class NamedConsumer {
    private final String value;

    @Inject
    NamedConsumer(@Named("named") String value) {
      this.value = value;
    }
  }

  static class CustomConsumer {
    private final String value;

    @Inject
    CustomConsumer(@CustomQualifier String value) {
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
        () -> builder.add(NamedConsumer.class),
        "There must be no problem with adding named consumer");
    assertDoesNotThrow(
        () -> builder.add(CustomConsumer.class),
        "There must be no problem with adding custom consumer");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertTrue(
        injector.get(NamedConsumer.class).value.startsWith("named"),
        "Named consumer should have named dependency injected");
    assertTrue(
        injector.get(CustomConsumer.class).value.startsWith("custom"),
        "Custom consumer should have dependency marked with custom qualifier injected");
  }
}
