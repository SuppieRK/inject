package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

class ReplaceAndInjectProvidesTest {
  static class A {
    @Provides
    String getValue() {
      return "original" + System.nanoTime();
    }
  }

  static class B extends A {
    @Override
    String getValue() {
      return "replacement" + System.nanoTime();
    }
  }

  static class FirstConsumer {
    private final String value;

    @Inject
    FirstConsumer(String value) {
      this.value = value;
    }
  }

  static class SecondConsumer {
    private final String value;

    @Inject
    SecondConsumer(String value) {
      this.value = value;
    }
  }

  @Test
  void must_correctly_inject_replaced_dependency() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(FirstConsumer.class));
    assertDoesNotThrow(() -> builder.add(SecondConsumer.class));
    final var injector = assertDoesNotThrow(builder::build);

    final var originalFirstConsumer = assertDoesNotThrow(() -> injector.get(FirstConsumer.class));
    final var originalSecondConsumer = assertDoesNotThrow(() -> injector.get(SecondConsumer.class));
    assertTrue(originalFirstConsumer.value.startsWith("original"));
    assertTrue(originalSecondConsumer.value.startsWith("original"));
    assertNotEquals(originalFirstConsumer.value, originalSecondConsumer.value);

    final var copyBuilder = injector.copy();
    assertDoesNotThrow(() -> copyBuilder.replace(A.class, B.class));
    final var copyInjector = assertDoesNotThrow(copyBuilder::build);

    final var replacedFirstConsumer =
        assertDoesNotThrow(() -> copyInjector.get(FirstConsumer.class));
    final var replacedSecondConsumer =
        assertDoesNotThrow(() -> copyInjector.get(SecondConsumer.class));
    assertTrue(replacedFirstConsumer.value.startsWith("replacement"));
    assertTrue(replacedSecondConsumer.value.startsWith("replacement"));
    assertNotEquals(replacedFirstConsumer.value, replacedSecondConsumer.value);
  }
}
