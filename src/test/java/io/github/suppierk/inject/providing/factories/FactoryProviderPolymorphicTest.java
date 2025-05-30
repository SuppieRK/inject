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

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import io.github.suppierk.mocks.Polymorphic;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class FactoryProviderPolymorphicTest {
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
    Polymorphic firstDescendant() {
      return new FirstDescendant();
    }

    @Provides
    Polymorphic secondDescendant() {
      return new SecondDescendant();
    }
  }

  @Test
  void invokesFactoryMethodToProvideDependencies() {
    final var builder = Injector.injector();
    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(DependencyProvider.class),
        "Non distinguishable polymorphic dependencies are not supported");
  }
}
