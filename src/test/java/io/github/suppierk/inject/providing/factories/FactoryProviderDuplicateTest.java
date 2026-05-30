package io.github.suppierk.inject.providing.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Key;
import io.github.suppierk.inject.Provides;
import java.util.Set;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class FactoryProviderDuplicateTest {
  static class DuplicateFactory {
    @Provides
    Long first() {
      return 1L;
    }

    @Provides
    Long second() {
      return 2L;
    }
  }

  @Test
  void duplicateProviderMethodReportsDuplicatedProvidedKey() {
    final var builder = Injector.injector();

    final var exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(DuplicateFactory.class),
            "Duplicate provider methods must fail");

    assertEquals(
        "Duplicate: " + new Key<>(Long.class, Set.of()),
        exception.getMessage(),
        "Duplicate provider error must report the duplicated provided key");
  }
}
