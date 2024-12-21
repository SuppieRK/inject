package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class ProvidesSingletonTest {
  static class A {
    @Provides
    @Singleton
    Long test() {
      return System.nanoTime();
    }
  }

  static class B {
    private final Long value;

    @Inject
    B(Long value) {
      this.value = value;
    }
  }

  static class C {
    private final Long value;

    @Inject
    C(Long value) {
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
