package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;
import org.junit.jupiter.api.Test;

class InjectorReferenceTest {
  @Test
  void null_cannot_be_set() {
    final var reference = new InjectorReference();
    assertThrows(IllegalStateException.class, () -> reference.set(null));
  }

  @Test
  void null_cannot_be_returned() {
    final var reference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());

    assertThrows(IllegalStateException.class, () -> reference.getNode(qualifier));
  }
}
