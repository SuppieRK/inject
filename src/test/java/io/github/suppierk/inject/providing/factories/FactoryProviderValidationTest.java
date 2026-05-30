package io.github.suppierk.inject.providing.factories;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class FactoryProviderValidationTest {
  static class VoidFactory {
    @Provides
    void dependency() {
      // Intentionally empty
    }
  }

  static class NullFactory {
    @Provides
    String dependency() {
      return null;
    }
  }

  static class SelfProvidedDependency {}

  static class FactoryDependingOnOwnProvidedDependency {
    @Inject
    FactoryDependingOnOwnProvidedDependency(SelfProvidedDependency dependency) {}

    @Provides
    SelfProvidedDependency dependency() {
      return new SelfProvidedDependency();
    }
  }

  @Test
  void voidProviderMethodFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(VoidFactory.class),
        "Void @Provides methods must fail during registration");
  }

  @Test
  void nonSingletonProviderMethodReturningNullFailsDuringRetrieval() {
    final var injector = Injector.injector().add(NullFactory.class).build();

    assertThrows(
        IllegalArgumentException.class,
        () -> injector.get(String.class),
        "Non-singleton @Provides methods must not return null dependencies");
  }

  @Test
  void factoryDependingOnOwnProvidedDependencyFailsAsCycle() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(FactoryDependingOnOwnProvidedDependency.class),
        "Factory constructors must not depend on dependencies provided by the same factory");
  }
}
