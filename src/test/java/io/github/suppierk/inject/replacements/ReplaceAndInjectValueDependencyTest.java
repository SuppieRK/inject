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

package io.github.suppierk.inject.replacements;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.mocks.NanoTimeValue;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class ReplaceAndInjectValueDependencyTest {
  static class OriginalNanoTimeValue extends NanoTimeValue {
    public OriginalNanoTimeValue() {
      this("original");
    }

    public OriginalNanoTimeValue(String prefix) {
      super(prefix);
    }
  }

  static class ReplacementNanoTimeValue extends OriginalNanoTimeValue {
    public ReplacementNanoTimeValue() {
      super("replacement");
    }
  }

  static class Consumer {
    private final OriginalNanoTimeValue value;

    @Inject
    Consumer(OriginalNanoTimeValue value) {
      this.value = value;
    }
  }

  @Test
  void mustInjectReplacedDependency() {
    final var original = new OriginalNanoTimeValue();
    final var replacement = new ReplacementNanoTimeValue();

    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(original), "There must be no problem adding value");
    assertDoesNotThrow(
        () -> builder.add(Consumer.class), "There must be no problem adding consumer");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem creating injector");

    final var originalConsumer =
        assertDoesNotThrow(
            () -> injector.get(Consumer.class), "There must be no problem retrieving consumer");
    assertEquals(original, originalConsumer.value, "The injected value must be original value");

    final var copyBuilder = injector.copy();
    assertDoesNotThrow(
        () -> copyBuilder.replace(original, replacement),
        "There must be no problem replacing value");
    final var copyInjector =
        assertDoesNotThrow(copyBuilder::build, "There must be no problem creating injector");

    final var replacedConsumer =
        assertDoesNotThrow(
            () -> copyInjector.get(Consumer.class), "There must be no problem retrieving consumer");
    assertEquals(
        replacement, replacedConsumer.value, "The injected value must be replacement value");
  }
}
