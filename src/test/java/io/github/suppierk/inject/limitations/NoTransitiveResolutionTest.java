package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

class NoTransitiveResolutionTest {
  static class First {
    final Second second;

    @Inject
    First(Second second) {
      this.second = second;
    }
  }

  static class Second {
    final Third third;

    @Inject
    Second(Third third) {
      this.third = third;
    }
  }

  static class Third {}

  @Test
  void doesNotResolveTransitiveDependenciesImplicitly() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(First.class), "There must be no problem with adding first dependency");
    assertThrows(
        IllegalArgumentException.class,
        builder::build,
        "There must be an exception, because Injector does not support transitive dependencies");
  }

  @Test
  void worksWhenTransitiveDependenciesDefinedExplicitly() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(First.class), "There must be no problem with adding first dependency");
    assertDoesNotThrow(
        () -> builder.add(Second.class), "There must be no problem with adding second dependency");
    assertDoesNotThrow(
        () -> builder.add(Third.class), "There must be no problem with adding third dependency");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertNotNull(injector.get(First.class).second.third, "All dependencies must be instantiated");
  }
}
