package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Provider;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class FieldInformationTest {
  @Test
  void object_methods_must_work_as_expected() {
    EqualsVerifier.forClass(FieldInformation.class).verify();
  }

  @Test
  void nulls_are_not_allowed_for_certain_fields() {
    final var field = FieldInformation.class.getDeclaredFields()[0];
    final var qualifierKey = new Key<>(Object.class, null);
    final var wrapper = Provider.class;

    assertDoesNotThrow(() -> new FieldInformation(field, qualifierKey, wrapper));
    assertThrows(
        IllegalArgumentException.class, () -> new FieldInformation(null, qualifierKey, wrapper));
    assertThrows(IllegalArgumentException.class, () -> new FieldInformation(field, null, wrapper));
    assertDoesNotThrow(() -> new FieldInformation(field, qualifierKey, null));
  }

  @Test
  void toString_returns_non_null() {
    final var field = FieldInformation.class.getDeclaredFields()[0];
    final var qualifierKey = new Key<>(Object.class, null);
    final var wrapper = Provider.class;

    final var instance =
        assertDoesNotThrow(() -> new FieldInformation(field, qualifierKey, wrapper));
    assertNotNull(instance.toString());
  }
}
