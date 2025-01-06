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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

class InjectMixedTest {
  static class SimpleField {
    @Provides
    String simpleField() {
      return "simpleField";
    }
  }

  static class NamedField {
    @Provides
    @Named("namedField")
    String namedField() {
      return "namedField";
    }
  }

  static class NamedProviderField {
    @Provides
    @Named("namedProviderField")
    String namedProviderField() {
      return "namedProviderField";
    }
  }

  static class SimpleLongField {
    @Provides
    Long simpleLongField() {
      return 42L;
    }
  }

  static class NamedLongField {
    @Provides
    @Named("namedLongField")
    Long namedLongField() {
      return 43L;
    }
  }

  static class NamedProviderLongField {
    @Provides
    @Named("namedProviderLongField")
    Long namedProviderLongField() {
      return 45L;
    }
  }

  static class Consumer {
    @Inject private String simpleField;

    @Inject
    @Named("namedField")
    private String namedField;

    @Inject private Provider<String> simpleProviderField;

    @Inject
    @Named("namedProviderField")
    private Provider<String> namedProviderField;

    private final Long simpleLongField;
    private final Long namedLongField;
    private final Provider<Long> simpleProviderLongField;
    private final Provider<Long> namedProviderLongField;

    @Inject
    Consumer(
        Long simpleLongField,
        @Named("namedLongField") Long namedLongField,
        Provider<Long> simpleProviderLongField,
        @Named("namedProviderLongField") Provider<Long> namedProviderLongField) {
      this.simpleLongField = simpleLongField;
      this.namedLongField = namedLongField;
      this.simpleProviderLongField = simpleProviderLongField;
      this.namedProviderLongField = namedProviderLongField;
    }
  }

  @Test
  void allFieldsInjectedAsExpected() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(SimpleField.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(NamedField.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(NamedProviderField.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(SimpleLongField.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(NamedLongField.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(NamedProviderLongField.class),
        "There must be no problem with adding dependency factory");
    assertDoesNotThrow(
        () -> builder.add(Consumer.class), "There must be no problem with adding consumer");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    final var consumer = injector.get(Consumer.class);
    assertNotNull(consumer, "Consumer must be instantiated and not null");
    assertNotNull(consumer.simpleField, "Consumer's simpleField must be instantiated and not null");
    assertNotNull(consumer.namedField, "Consumer's namedField must be instantiated and not null");
    assertNotNull(
        consumer.simpleProviderField,
        "Consumer's simpleProviderField must be instantiated and not null");
    assertNotNull(
        consumer.namedProviderField,
        "Consumer's namedProviderField must be instantiated and not null");
    assertNotNull(
        consumer.simpleLongField, "Consumer's simpleLongField must be instantiated and not null");
    assertNotNull(
        consumer.namedLongField, "Consumer's namedLongField must be instantiated and not null");
    assertNotNull(
        consumer.simpleProviderLongField,
        "Consumer's simpleProviderLongField must be instantiated and not null");
    assertNotNull(
        consumer.namedProviderLongField,
        "Consumer's namedProviderLongField must be instantiated and not null");
  }
}
