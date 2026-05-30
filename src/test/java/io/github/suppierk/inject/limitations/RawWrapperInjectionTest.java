package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

@SuppressWarnings({"rawtypes", "unused"})
class RawWrapperInjectionTest {
  static class RawProviderConsumer {
    private final Provider provider;

    @Inject
    RawProviderConsumer(Provider provider) {
      this.provider = provider;
    }
  }

  static class RawSupplierConsumer {
    private final Supplier supplier;

    @Inject
    RawSupplierConsumer(Supplier supplier) {
      this.supplier = supplier;
    }
  }

  static class RawProviderFieldConsumer {
    @Inject private Provider provider;
  }

  static class RawSupplierFieldConsumer {
    @Inject private Supplier supplier;
  }

  static class RawProviderFactory {
    @Provides
    Provider dependency() {
      return () -> "value";
    }
  }

  static class RawSupplierFactory {
    @Provides
    Supplier dependency() {
      return () -> "value";
    }
  }

  @Test
  void rawProviderInjectionFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(RawProviderConsumer.class),
        "Raw Provider injection must fail during registration");
  }

  @Test
  void rawSupplierInjectionFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(RawSupplierConsumer.class),
        "Raw Supplier injection must fail during registration");
  }

  @Test
  void rawProviderFieldInjectionFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(RawProviderFieldConsumer.class),
        "Raw Provider field injection must fail during registration");
  }

  @Test
  void rawSupplierFieldInjectionFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(RawSupplierFieldConsumer.class),
        "Raw Supplier field injection must fail during registration");
  }

  @Test
  void rawProviderReturnFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(RawProviderFactory.class),
        "Raw Provider @Provides return types must fail during registration");
  }

  @Test
  void rawSupplierReturnFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(RawSupplierFactory.class),
        "Raw Supplier @Provides return types must fail during registration");
  }
}
