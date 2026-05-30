package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class InjectorCloseIdempotencyTest {
  @Singleton
  static class Resource implements AutoCloseable {
    private final AtomicInteger closeCount = new AtomicInteger();

    @Override
    public void close() {
      closeCount.incrementAndGet();
    }
  }

  @Test
  void closingInjectorTwiceClosesValueOnlyOnce() {
    final var resource = new Resource();
    final var injector = Injector.injector().add(resource).build();

    injector.close();
    injector.close();

    assertEquals(1, resource.closeCount.get(), "Value resources must be closed at most once");
  }

  @Test
  void closingInjectorTwiceClosesSingletonOnlyOnce() {
    final var injector = Injector.injector().add(Resource.class).build();
    final var resource = injector.get(Resource.class);

    injector.close();
    injector.close();

    assertEquals(1, resource.closeCount.get(), "Singleton must be closed at most once");
  }
}
