package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class InjectInConstructorTest {
  static class A {
    private final long id;

    A() {
      this.id = System.nanoTime();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof A)) return false;
      A a = (A) o;
      return id == a.id;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id);
    }
  }

  static class B {
    private final A a;

    @Inject
    B(A a) {
      this.a = a;
    }
  }

  @Test
  void must_correctly_inject_dependency() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class, B.class));
    final var injector = assertDoesNotThrow(builder::build);

    final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
    final var target = assertDoesNotThrow(() -> injector.get(B.class));
    assertNotEquals(exemplar, target.a);
  }

  @Test
  void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(B.class, A.class));
    final var injector = assertDoesNotThrow(builder::build);

    final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
    final var target = assertDoesNotThrow(() -> injector.get(B.class));
    assertNotEquals(exemplar, target.a);
  }
}
