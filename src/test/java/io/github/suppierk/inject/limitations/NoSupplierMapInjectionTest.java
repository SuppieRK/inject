package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NoSupplierMapInjectionTest {
  static class Consumer {
    private final Supplier<Map<String, String>> strings;

    @Inject
    Consumer(Supplier<Map<String, String>> strings) {
      this.strings = strings;
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(Consumer.class),
        "Supplier wrapping Map is not supported for injection");
  }
}
