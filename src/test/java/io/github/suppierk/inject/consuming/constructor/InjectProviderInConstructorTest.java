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

package io.github.suppierk.inject.consuming.constructor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.mocks.NanoTimeValue;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class InjectProviderInConstructorTest {
  static class Consumer {
    private final NanoTimeValue value;

    @Inject
    Consumer(Provider<NanoTimeValue> value) {
      this.value = value.get();
    }
  }

  @Test
  void mustCorrectlyInjectDependency() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(NanoTimeValue.class, Consumer.class),
        "There must be no problem with providing dependencies");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with constructing injector");

    final var exemplar =
        assertDoesNotThrow(
            () -> injector.get(NanoTimeValue.class),
            "There must be no problem with retrieving value");
    final var target =
        assertDoesNotThrow(
            () -> injector.get(Consumer.class),
            "There must be no problem with retrieving consumer");
    assertNotEquals(
        exemplar,
        target.value,
        "Because factory provides non-singleton dependency, injected values must be different");
  }
}
