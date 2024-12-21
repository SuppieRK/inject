package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import org.junit.jupiter.api.Test;

class AmbiguousDependenciesTest {
  static class AmbiguousProviderModule {
    @Provides
    String foo() {
      return "foo";
    }

    @Provides
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
