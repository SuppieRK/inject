package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.Collection;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NoCollectionInjectionTest {
  static class Consumer {
    private final Collection<String> strings;

    @Inject
    Consumer(Collection<String> strings) {
      this.strings = strings;
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(Consumer.class),
        "Collections are not supported for injection");
  }
}
