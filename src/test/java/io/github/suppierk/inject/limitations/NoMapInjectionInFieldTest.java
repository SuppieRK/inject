package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NoMapInjectionInFieldTest {
  static class Consumer {
    @Inject private Map<String, String> strings;
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
