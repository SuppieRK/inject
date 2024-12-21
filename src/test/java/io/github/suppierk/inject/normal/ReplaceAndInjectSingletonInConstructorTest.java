package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class ReplaceAndInjectSingletonInConstructorTest {
  @Singleton
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

  @Singleton
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

  static class FirstConsumer {
    private final A value;

    @Inject
    FirstConsumer(A value) {
      this.value = value;
    }
  }

  static class SecondConsumer {
    private final A value;

    @Inject
    SecondConsumer(A value) {
      this.value = value;
    }
  }

  @Test
  void must_correctly_inject_replaced_dependency() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(FirstConsumer.class));
    assertDoesNotThrow(() -> builder.add(SecondConsumer.class));
    final var injector = assertDoesNotThrow(builder::build);

    final var originalFirstConsumer = assertDoesNotThrow(() -> injector.get(FirstConsumer.class));
    final var originalSecondConsumer = assertDoesNotThrow(() -> injector.get(SecondConsumer.class));
    assertTrue(originalFirstConsumer.value.getValue().startsWith("original"));
    assertTrue(originalSecondConsumer.value.getValue().startsWith("original"));
    assertEquals(originalFirstConsumer.value.getValue(), originalSecondConsumer.value.getValue());

    final var copyBuilder = injector.copy();
    assertDoesNotThrow(() -> copyBuilder.replace(A.class, B.class));
    final var copyInjector = assertDoesNotThrow(copyBuilder::build);

    final var replacedFirstConsumer =
        assertDoesNotThrow(() -> copyInjector.get(FirstConsumer.class));
    final var replacedSecondConsumer =
        assertDoesNotThrow(() -> copyInjector.get(SecondConsumer.class));
    assertTrue(replacedFirstConsumer.value.getValue().startsWith("replacement"));
    assertTrue(replacedSecondConsumer.value.getValue().startsWith("replacement"));
    assertEquals(replacedFirstConsumer.value.getValue(), replacedSecondConsumer.value.getValue());
  }
}
