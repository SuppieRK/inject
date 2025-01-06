package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Provider;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class FieldInformationTest {
  @Test
  void objectMethodsMustWorkAsExpected() {
    EqualsVerifier.forClass(FieldInformation.class).verify();
  }

  @Test
  void nullsAreNotAllowedForCertainFields() {
    final var field = FieldInformation.class.getDeclaredFields()[0];
    final var qualifierKey = new Key<>(Object.class, null);
    final var wrapper = Provider.class;

    assertThrows(
        IllegalArgumentException.class,
        () -> new FieldInformation(null, qualifierKey, wrapper),
        "Null field must throw an exception");
    assertThrows(
        IllegalArgumentException.class,
        () -> new FieldInformation(field, null, wrapper),
        "Null qualifier key must throw an exception");
    assertDoesNotThrow(
        () -> new FieldInformation(field, qualifierKey, null),
        "Null wrapper must throw an exception");
  }

  @Test
  void toStringReturnsNonNull() {
    final var field = FieldInformation.class.getDeclaredFields()[0];
    final var qualifierKey = new Key<>(Object.class, null);
    final var wrapper = Provider.class;

    final var instance = new FieldInformation(field, qualifierKey, wrapper);
    assertNotNull(instance.toString(), "String must not be null");
    assertFalse(instance.toString().isBlank(), "String must not be blank");
  }
}
