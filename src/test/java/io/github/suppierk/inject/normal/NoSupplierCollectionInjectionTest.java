package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collection;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NoSupplierCollectionInjectionTest {
  static class Module {
    @Provides
    @Named("stringA")
    String stringA() {
      return "stringA";
    }

    @Provides
    @Named("stringB")
    String stringB() {
      return "stringB";
    }
  }

  static class A {
    private final Supplier<Collection<String>> strings;

    @Inject
    A(Supplier<Collection<String>> strings) {
      this.strings = strings;
    }
  }

  @Test
  void must_provide_both_values_correctly() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(Module.class));

    assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
  }
}
