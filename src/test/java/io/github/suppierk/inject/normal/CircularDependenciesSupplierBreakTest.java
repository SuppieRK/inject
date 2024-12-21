package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class CircularDependenciesSupplierBreakTest {
  static class A {
    final B b;

    @Inject
    A(B b) {
      this.b = b;
    }
  }

  static class B {
    final Supplier<A> a;

    @Inject
    B(Supplier<A> a) {
      this.a = a;
    }
  }

  @Test
  void must_allow_cycle_resolution_via_provider() {
    final var builder = Injector.injector();

    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(B.class));
    final var injector = assertDoesNotThrow(builder::build);

    assertNotNull(injector.get(A.class).b.a.get());
  }
}
