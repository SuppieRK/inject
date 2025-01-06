package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NoProviderMapInjectionTest {
  static class Consumer {
    private final Provider<Map<String, String>> strings;

    @Inject
    Consumer(Provider<Map<String, String>> strings) {
      this.strings = strings;
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(Consumer.class),
        "Provider wrapping Map is not supported for injection");
  }
}
