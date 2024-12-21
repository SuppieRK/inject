package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

class PolymorphicDependencyTest {
  interface Poly {}

  static class PolyA implements Poly {}

  static class PolyB implements Poly {}

  static class Module {
    @Provides
    @Named("polyA")
    Poly polyA() {
      return new PolyA();
    }

    @Provides
    @Named("polyB")
    Poly polyB() {
      return new PolyB();
    }
  }

  static class A {
    private final Poly poly;

    @Inject
    A(@Named("polyA") Poly poly) {
      this.poly = poly;
    }
  }

  static class B {
    private final Poly poly;

    @Inject
    B(@Named("polyB") Poly poly) {
      this.poly = poly;
    }
  }

  @Test
  void must_provide_both_values_correctly() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(B.class));
    assertDoesNotThrow(() -> builder.add(Module.class));
    final var injector = assertDoesNotThrow(builder::build);

    assertNotNull(injector.get(A.class).poly);
    assertNotNull(injector.get(B.class).poly);
    assertEquals(PolyA.class, injector.get(A.class).poly.getClass());
    assertEquals(PolyB.class, injector.get(B.class).poly.getClass());
  }
}
