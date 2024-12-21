package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class ExtendedCircularSingletonDependenciesTest {
  @Singleton
  static class A {
    final B b;

    @Inject
    A(B b) {
      this.b = b;
    }
  }

  @Singleton
  static class B {
    final C c;

    @Inject
    B(C c) {
      this.c = c;
    }
  }

  @Singleton
  static class C {
    final A a;

    @Inject
    C(A a) {
      this.a = a;
    }
  }

  @Test
  void must_throw_illegal_argument_exception_for_ABC_cycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(B.class));
    assertThrows(IllegalArgumentException.class, () -> builder.add(C.class));
  }

  @Test
  void must_throw_illegal_argument_exception_for_ACB_cycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(C.class));
    assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
  }

  @Test
  void must_throw_illegal_argument_exception_for_BAC_cycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(B.class));
    assertDoesNotThrow(() -> builder.add(A.class));
    assertThrows(IllegalArgumentException.class, () -> builder.add(C.class));
  }

  @Test
  void must_throw_illegal_argument_exception_for_BCA_cycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(B.class));
    assertDoesNotThrow(() -> builder.add(C.class));
    assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
  }

  @Test
  void must_throw_illegal_argument_exception_for_CAB_cycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(C.class));
    assertDoesNotThrow(() -> builder.add(A.class));
    assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
  }

  @Test
  void must_throw_illegal_argument_exception_for_CBA_cycle() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(C.class));
    assertDoesNotThrow(() -> builder.add(B.class));
    assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
  }
}
