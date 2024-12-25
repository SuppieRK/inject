package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class ValueDependencyTest {
  static class Value {
    private final Long value;

    Value() {
      this.value = System.nanoTime();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof Value)) return false;
      Value value1 = (Value) o;
      return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }
  }

  static class SecondValue {
    private final Long value;

    SecondValue() {
      this.value = System.nanoTime();
    }

    @Override
    public boolean equals(Object o) {
      if (!(o instanceof SecondValue)) return false;
      SecondValue value1 = (SecondValue) o;
      return Objects.equals(value, value1.value);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(value);
    }
  }

  static class A {
    private final Value value;

    @Inject
    A(Value value) {
      this.value = value;
    }
  }

  static class B {
    private final Value value;

    @Inject
    B(Value value) {
      this.value = value;
    }
  }

  @Test
  void name() {
    final var value = new Value();
    final var secondValue = new SecondValue();

    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(value, secondValue));
    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(B.class));
    final var injector = assertDoesNotThrow(builder::build);

    assertEquals(value, injector.get(Value.class));
    assertEquals(secondValue, injector.get(SecondValue.class));
    assertEquals(value, injector.get(A.class).value);
    assertEquals(value, injector.get(B.class).value);
  }
}
