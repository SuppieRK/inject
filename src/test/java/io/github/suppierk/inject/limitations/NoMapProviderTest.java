package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import java.util.Map;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NoMapProviderTest {
  static class DependencyFactory {
    @Provides
    Map<String, String> collection() {
      return Map.of("test1", "test2");
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(DependencyFactory.class),
        "Maps are not supported for exposure");
  }
}
