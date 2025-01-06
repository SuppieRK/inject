package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Provider;
import java.lang.reflect.Parameter;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ParameterInformationTest {
  @SuppressWarnings("unused")
  static class Prefab {
    void method(String red, Integer blue) {
      // Fake method for testing
    }
  }

  @Test
  void objectMethodsMustWorkAsExpected() {
    final var method = Prefab.class.getDeclaredMethods()[0];
    final var parameters = method.getParameters();

    EqualsVerifier.forClass(ParameterInformation.class)
        .withPrefabValues(Parameter.class, parameters[0], parameters[1])
        .verify();
  }

  @Test
  void nullsAreNotAllowedForCertainFields() {
    final var parameter = ParameterInformation.class.getConstructors()[0].getParameters()[0];
    final var qualifierKey = new Key<>(Object.class, null);
    final var wrapper = Provider.class;

    assertThrows(
        IllegalArgumentException.class,
        () -> new ParameterInformation(null, qualifierKey, wrapper),
        "Null parameter must throw an exception");
    assertThrows(
        IllegalArgumentException.class,
        () -> new ParameterInformation(parameter, null, wrapper),
        "Null key must throw an exception");
    assertDoesNotThrow(
        () -> new ParameterInformation(parameter, qualifierKey, null),
        "Null wrapper must throw an exception");
  }

  @Test
  void toStringReturnsNonNull() {
    final var parameter = ParameterInformation.class.getConstructors()[0].getParameters()[0];
    final var qualifierKey = new Key<>(Object.class, null);
    final var wrapper = Provider.class;

    final var instance = new ParameterInformation(parameter, qualifierKey, wrapper);

    assertNotNull(instance.toString(), "String must not be null");
    assertFalse(instance.toString().isBlank(), "String must not be blank");
  }
}
