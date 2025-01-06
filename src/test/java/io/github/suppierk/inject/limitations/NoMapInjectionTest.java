package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NoMapInjectionTest {
  static class Consumer {
    private final Map<String, String> strings;

    @Inject
    Consumer(Map<String, String> strings) {
      this.strings = strings;
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(Consumer.class),
        "Maps are not supported for injection");
  }
}
