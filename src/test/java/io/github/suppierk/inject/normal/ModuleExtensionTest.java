package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

class ModuleExtensionTest {
  static class A {
    @Provides
    @Named("original")
    String original() {
      return "original" + System.nanoTime();
    }
  }

  static class B extends A {
    @Provides
    @Named("additional")
    String additional() {
      return "additional" + System.nanoTime();
    }
  }

  static class Consumer {
    private final String original;
    private final String additional;

    @Inject
    Consumer(@Named("original") String original, @Named("additional") String additional) {
      this.original = original;
      this.additional = additional;
    }
  }

  @Test
  void module_extension_must_pickup_what_parent_provided() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(B.class));
    assertDoesNotThrow(() -> builder.add(Consumer.class));
    final var injector = assertDoesNotThrow(builder::build);

    final var consumer = injector.get(Consumer.class);
    assertTrue(consumer.original.startsWith("original"));
    assertTrue(consumer.additional.startsWith("additional"));
  }
}
