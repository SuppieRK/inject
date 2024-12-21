package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.Collection;
import org.junit.jupiter.api.Test;

class NoCollectionInjectionInFieldTest {
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
    @Inject private Collection<String> strings;

    A() {}
  }

  @Test
  void must_provide_both_values_correctly() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(Module.class));

    assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
  }
}
