package io.github.suppierk.inject.providing.factories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class FactoryProviderQualifiedReturnTest {
  @Named("service")
  static class Service {}

  static class Factory {
    @Provides
    Service service() {
      return new Service();
    }
  }

  static class Consumer {
    private final Service service;

    @Inject
    Consumer(@Named("service") Service service) {
      this.service = service;
    }
  }

  @Test
  void providerMethodReturningQualifiedClassUsesClassQualifierForLookup() {
    final var injector = Injector.injector().add(Factory.class).build();

    assertDoesNotThrow(
        () -> injector.get(Service.class),
        "Provider methods returning a qualified class must be retrievable by that class");
  }

  @Test
  void providerMethodReturningQualifiedClassUsesClassQualifierForInjection() {
    final var injector = Injector.injector().add(Factory.class, Consumer.class).build();

    assertDoesNotThrow(
        () -> injector.get(Consumer.class),
        "Provider methods returning a qualified class must satisfy matching injection points");
  }
}
