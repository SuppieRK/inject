package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class InjectedFieldValidationTest {
  static class StaticFieldConsumer {
    @Inject private static String value;
  }

  static class FinalFieldConsumer {
    @Inject private final String value = null;
  }

  @Test
  void staticInjectedFieldFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(StaticFieldConsumer.class),
        "Static field injection must fail during registration");
  }

  @Test
  void finalInjectedFieldFailsDuringRegistration() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(FinalFieldConsumer.class),
        "Final field injection must fail during registration");
  }
}
