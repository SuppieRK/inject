package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NoMapProviderTest {
  static class Module {
    @Provides
    Map<String, String> collection() {
      return Map.of("test1", "test2");
    }
  }

  @Test
  void must_provide_both_values_correctly() {
    final var builder = Injector.injector();

    assertThrows(IllegalArgumentException.class, () -> builder.add(Module.class));
  }
}
