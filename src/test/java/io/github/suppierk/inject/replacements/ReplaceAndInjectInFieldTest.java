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
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import io.github.suppierk.mocks.NanoTimeValue;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class ReplaceAndInjectInFieldTest {
  static class OriginalDependencyFactory {
    @Provides
    NanoTimeValue nanoTimeValue() {
      return new NanoTimeValue("original");
    }
  }

  static class ReplacementDependencyFactory extends OriginalDependencyFactory {
    @Override
    NanoTimeValue nanoTimeValue() {
      return new NanoTimeValue("replacement");
    }
  }

  static class Consumer {
    @Inject private NanoTimeValue value;
  }

  @Test
  void mustReplaceDependency() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(OriginalDependencyFactory.class),
        "There must be no problem adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(Consumer.class), "There must be no problem adding consumer");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem creating injector");

    final var consumer =
        assertDoesNotThrow(
            () -> injector.get(Consumer.class), "There must be no problem retrieving consumer");
    assertTrue(
        consumer.value.getValue().startsWith("original"),
        "Original value must start with expected prefix");

    final var copyBuilder = injector.copy();
    assertDoesNotThrow(
        () ->
            copyBuilder.replace(
                OriginalDependencyFactory.class, ReplacementDependencyFactory.class),
        "There must be no problem replacing dependency factory");
    final var copyInjector =
        assertDoesNotThrow(copyBuilder::build, "There must be no problem creating injector");

    final var replacedFirstConsumer =
        assertDoesNotThrow(
            () -> copyInjector.get(Consumer.class), "There must be no problem retrieving consumer");
    assertTrue(
        replacedFirstConsumer.value.getValue().startsWith("replacement"),
        "Replacement value must start with expected prefix");
  }
}
