package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class MultipleInjectConstructorsTest {
  static class Value {
    private final String value;

    @Inject
    Value(String value) {
      this.value = value;
    }

    @Inject
    Value(Long value) {
      this.value = value.toString();
    }
  }

  @Test
  void should_throws_an_exception() {
    final var builder = Injector.injector();

    assertThrows(IllegalArgumentException.class, () -> builder.add(Value.class));
  }
}
