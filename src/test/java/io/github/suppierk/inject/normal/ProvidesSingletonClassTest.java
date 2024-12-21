package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class ProvidesSingletonClassTest {
  @Singleton
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

  static class A {
    @Provides
    Value test() {
      return new Value();
    }
  }

  static class B {
    private final Value value;

    @Inject
    B(Value value) {
      this.value = value;
    }
  }

  static class C {
    private final Value value;

    @Inject
    C(Value value) {
      this.value = value;
    }
  }

  @Test
  void must_provide_both_value_and_the_object() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(B.class));
    assertDoesNotThrow(() -> builder.add(C.class));
    final var injector = assertDoesNotThrow(builder::build);

    assertEquals(injector.get(B.class).value, injector.get(C.class).value);
  }
}
