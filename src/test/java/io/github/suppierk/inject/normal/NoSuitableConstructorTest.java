package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NoSuitableConstructorTest {
  static class Value {
    private final String value;

    Value(String value) {
      this.value = value;
    }
  }

  @Test
  void should_throws_an_exception() {
    final var builder = Injector.injector();

    assertThrows(IllegalArgumentException.class, () -> builder.add(Value.class));
  }
}
