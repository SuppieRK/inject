package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class AmbiguousSingletonDependenciesTest {
  static class AmbiguousProviderModule {
    @Provides
    @Singleton
    String foo() {
      return "foo";
    }

    @Provides
    @Singleton
    String bar() {
      return "bar";
    }
  }

  @Test
  void must_throw_illegal_argument_exception() {
    final var builder = Injector.injector();

    assertThrows(IllegalArgumentException.class, () -> builder.add(AmbiguousProviderModule.class));
  }
}
