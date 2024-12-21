package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

class NoTransitiveResolutionTest {
  static class A {
    final B b;

    @Inject
    A(B b) {
      this.b = b;
    }
  }

  static class B {
    final C c;

    @Inject
    B(C c) {
      this.c = c;
    }
  }

  static class C {}

  @Test
  void does_not_resolve_transitive_dependencies_implicitly() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class));
    assertThrows(IllegalArgumentException.class, builder::build);
  }

  @Test
  void works_when_transitive_dependencies_defined_explicitly() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(B.class));
    assertDoesNotThrow(() -> builder.add(C.class));
    final var injector = assertDoesNotThrow(builder::build);

    assertNotNull(injector.get(A.class).b.c);
  }
}
