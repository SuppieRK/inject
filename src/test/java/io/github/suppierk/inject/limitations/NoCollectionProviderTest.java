package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NoCollectionProviderTest {
  static class DependencyFactory {
    @Provides
    Collection<String> collection() {
      return Set.of("test1", "test2");
    }
  }

  @Test
  void mustThrowException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(DependencyFactory.class),
        "Collections are not supported for exposure");
  }
}
