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

package io.github.suppierk.inject.providing.objects;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class SingletonClassTest {
  @Singleton
  static class Value {
    private final Long value;

    Value() {
      this.value = System.nanoTime();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Value)) return false;
      Value value1 = (Value) o;
      return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }
  }

  static class FirstConsumer {
    private final Value value;

    @Inject
    FirstConsumer(Value value) {
      this.value = value;
    }
  }

  static class SecondConsumer {
    private final Value value;

    @Inject
    SecondConsumer(Value value) {
      this.value = value;
    }
  }

  @Test
  void invokesClassConstructorToProvideDependencies() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(Value.class), "There must be no problem with adding value");
    assertDoesNotThrow(
        () -> builder.add(FirstConsumer.class),
        "There must be no problem with adding first consumer");
    assertDoesNotThrow(
        () -> builder.add(SecondConsumer.class),
        "There must be no problem with adding second consumer");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertEquals(
        injector.get(FirstConsumer.class).value,
        injector.get(SecondConsumer.class).value,
        "Because factory provides singleton dependency, injected values must be equal");
  }
}
