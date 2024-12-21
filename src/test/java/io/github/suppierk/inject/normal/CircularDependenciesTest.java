package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

class CircularDependenciesTest {
  static class A {
    final B b;

    @Inject
    A(B b) {
      this.b = b;
    }
  }

  static class B {
    final A a;

    @Inject
    B(A a) {
      this.a = a;
    }
  }

  @Test
  void must_throw_illegal_argument_exception_for_AB_cycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(A.class));
    assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
  }

  @Test
  void must_throw_illegal_argument_exception_for_BA_cycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(B.class));
    assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
  }
}
