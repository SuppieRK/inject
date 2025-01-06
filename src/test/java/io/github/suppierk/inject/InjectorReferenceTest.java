package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;

class InjectorReferenceTest {
  @Test
  void nullCannotBeSet() {
    final var reference = new InjectorReference();
    assertThrows(
        IllegalStateException.class,
        () -> reference.set(null),
        "Null value must throw an exception");
  }

  @Test
  void nullCannotBeReturned() {
    final var reference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());

    assertThrows(
        IllegalStateException.class,
        () -> reference.getNode(qualifier),
        "Null value returned must throw an exception");
  }
}
