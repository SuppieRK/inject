package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.Collection;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NoProviderCollectionInjectionTest {
  static class Consumer {
    private final Provider<Collection<String>> strings;

    @Inject
    Consumer(Provider<Collection<String>> strings) {
      this.strings = strings;
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(Consumer.class),
        "Provider wrapping Collection is not supported for injection");
  }
}
