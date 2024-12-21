package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

class ProvidesNamedTest {
  static class A {
    @Provides
    String nonNamed() {
      return "simple" + System.nanoTime();
    }

    @Provides
    @Named("named")
    String named() {
      return "named" + System.nanoTime();
    }
  }

  static class B {
    private final String value;

    @Inject
    B(String value) {
      this.value = value;
    }
  }

  static class C {
    private final String value;

    @Inject
    C(@Named("named") String value) {
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

    assertTrue(injector.get(B.class).value.startsWith("simple"));
    assertTrue(injector.get(C.class).value.startsWith("named"));
    assertNotEquals(injector.get(B.class).value, injector.get(B.class).value);
    assertNotEquals(injector.get(C.class).value, injector.get(C.class).value);
  }
}
