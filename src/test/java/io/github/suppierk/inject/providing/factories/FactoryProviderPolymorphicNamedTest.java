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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import io.github.suppierk.mocks.Polymorphic;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class FactoryProviderPolymorphicNamedTest {
  static class FirstDescendant implements Polymorphic {
    private final Long value;

    public FirstDescendant() {
      this.value = System.nanoTime();
    }

    @Override
    public String getValue() {
      return "first" + value;
    }
  }

  static class SecondDescendant implements Polymorphic {
    private final Long value;

    public SecondDescendant() {
      this.value = System.nanoTime();
    }

    @Override
    public String getValue() {
      return "second" + value;
    }
  }

  static class DependencyProvider {
    @Provides
    @Named("firstDescendant")
    Polymorphic firstDescendant() {
      return new FirstDescendant();
    }

    @Provides
    @Named("secondDescendant")
    Polymorphic secondDescendant() {
      return new SecondDescendant();
    }
  }

  static class FirstConsumer {
    private final Polymorphic polymorphic;

    @Inject
    FirstConsumer(@Named("firstDescendant") Polymorphic polymorphic) {
      this.polymorphic = polymorphic;
    }
  }

  static class SecondConsumer {
    private final Polymorphic polymorphic;

    @Inject
    SecondConsumer(@Named("secondDescendant") Polymorphic polymorphic) {
      this.polymorphic = polymorphic;
    }
  }

  @Test
  void invokesFactoryMethodToProvideDependencies() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(DependencyProvider.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(FirstConsumer.class),
        "There must be no problem with adding first consumer");
    assertDoesNotThrow(
        () -> builder.add(SecondConsumer.class),
        "There must be no problem with adding second consumer");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertTrue(
        injector.get(FirstConsumer.class).polymorphic.getValue().startsWith("first"),
        "First consumer should have first polymorphic dependency injected");
    assertTrue(
        injector.get(SecondConsumer.class).polymorphic.getValue().startsWith("second"),
        "Second consumer should have second polymorphic dependency injected");
  }
}
