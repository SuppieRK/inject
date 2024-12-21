package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class ReplaceAndInjectValueDependencyTest {
  static class A {
    private final long id;

    A() {
      this.id = System.nanoTime();
    }

    String getValue() {
      return "original" + id;
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

  static class B extends A {
    private final long id;

    B() {
      this.id = System.nanoTime();
    }

    @Override
    String getValue() {
      return "replacement" + id;
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof B)) return false;
      B b = (B) o;
      return id == b.id;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id);
    }
  }

  static class Consumer {
    private final A value;

    @Inject
    Consumer(A value) {
      this.value = value;
    }
  }

  @Test
  void must_correctly_inject_replaced_dependency() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(new A()));
    assertDoesNotThrow(() -> builder.add(Consumer.class));
    final var injector = assertDoesNotThrow(builder::build);

    final var originalConsumer = assertDoesNotThrow(() -> injector.get(Consumer.class));
    assertTrue(originalConsumer.value.getValue().startsWith("original"));

    final var copyBuilder = injector.copy();
    assertDoesNotThrow(() -> copyBuilder.replace(new A(), new B()));
    final var copyInjector = assertDoesNotThrow(copyBuilder::build);

    final var replacedConsumer = assertDoesNotThrow(() -> copyInjector.get(Consumer.class));
    assertTrue(replacedConsumer.value.getValue().startsWith("replacement"));
  }
}
