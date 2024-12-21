package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
  void object_methods_must_work_as_expected() {
    final var method = Prefab.class.getDeclaredMethods()[0];
    final var parameters = method.getParameters();

    EqualsVerifier.forClass(ParameterInformation.class)
        .withPrefabValues(Parameter.class, parameters[0], parameters[1])
        .verify();
  }

  @Test
  void nulls_are_not_allowed_for_certain_fields() {
    final var parameter = ParameterInformation.class.getConstructors()[0].getParameters()[0];
    final var qualifierKey = new Key<>(Object.class, null);
    final var wrapper = Provider.class;

    assertDoesNotThrow(() -> new ParameterInformation(parameter, qualifierKey, wrapper));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ParameterInformation(null, qualifierKey, wrapper));
    assertThrows(
        IllegalArgumentException.class, () -> new ParameterInformation(parameter, null, wrapper));
    assertDoesNotThrow(() -> new ParameterInformation(parameter, qualifierKey, null));
  }

  @Test
  void toString_returns_non_null() {
    final var parameter = ParameterInformation.class.getConstructors()[0].getParameters()[0];
    final var qualifierKey = new Key<>(Object.class, null);
    final var wrapper = Provider.class;

    final var instance =
        assertDoesNotThrow(() -> new ParameterInformation(parameter, qualifierKey, wrapper));

    assertNotNull(instance.getParameter());
    assertNotNull(instance.getQualifierKey());
    assertNotNull(instance.getWrapper());
    assertNotNull(instance.toString());
  }
}
