package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.CustomQualifier;
import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class CustomQualifierSingletonTest {
  static class A {
    @Provides
    @Singleton
    @Named("named")
    String named() {
      return "named" + System.nanoTime();
    }

    @Provides
    @Singleton
    @CustomQualifier
    String custom() {
      return "custom" + System.nanoTime();
    }
  }

  static class B {
    private final String value;

    @Inject
    B(@Named("named") String value) {
      this.value = value;
    }
  }

  static class C {
    private final String value;

    @Inject
    C(@CustomQualifier String value) {
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

    assertTrue(injector.get(B.class).value.startsWith("named"));
    assertTrue(injector.get(C.class).value.startsWith("custom"));
    assertEquals(injector.get(B.class).value, injector.get(B.class).value);
    assertEquals(injector.get(C.class).value, injector.get(C.class).value);
  }
}