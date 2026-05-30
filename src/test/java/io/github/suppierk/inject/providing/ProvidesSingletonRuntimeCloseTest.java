package io.github.suppierk.inject.providing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class ProvidesSingletonRuntimeCloseTest {
  static class Resource implements AutoCloseable {
    boolean closed;

    @Override
    public void close() {
      closed = true;
    }
  }

  static class Factory {
    private final Resource resource = new Resource();

    @Provides
    @Singleton
    Object dependency() {
      return resource;
    }
  }

  @Test
  void closesRuntimeAutoCloseableReturnedAsObject() {
    final var injector = Injector.injector().add(Factory.class).build();
    final var resource = (Resource) injector.get(Object.class);

    injector.close();

    assertTrue(resource.closed, "Runtime AutoCloseable singleton must be closed");
  }
}
