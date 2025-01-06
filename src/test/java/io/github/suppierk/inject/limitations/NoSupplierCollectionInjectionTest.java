package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class NoSupplierCollectionInjectionTest {
  static class Consumer {
    private final Supplier<Collection<String>> strings;

    @Inject
    Consumer(Supplier<Collection<String>> strings) {
      this.strings = strings;
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(Consumer.class),
        "Supplier wrapping Collection is not supported for injection");
  }
}
