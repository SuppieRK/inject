package io.github.suppierk.inject.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.InjectorReference;
import io.github.suppierk.inject.Key;
import io.github.suppierk.utils.ConsoleConstants;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class RefersToTest {
  @Test
  void objectMethodsMustWorkAsExpected() {
    final var redInjector = Injector.injector().add("Red").build();
    final var blueInjector = Injector.injector().add("Blue").build();

    EqualsVerifier.simple()
        .forClass(RefersTo.class)
        .withPrefabValues(Injector.class, redInjector, blueInjector)
        .withIgnoredFields("injectorReference")
        .verify();
  }

  @Test
  void nullConstructorArgumentThrowsException() {
    final var injectorReference = new InjectorReference();
    final var parentQualifier = new Key<>(String.class, Set.of());

    assertThrows(
        IllegalArgumentException.class,
        () -> new RefersTo<>(injectorReference, null),
        "Null parent key must throw an exception");
    assertThrows(
        IllegalArgumentException.class,
        () -> new RefersTo<>(null, parentQualifier),
        "Null injector reference must throw an exception");
  }

  @Test
  void throwsNoSuchElementIfReferenceNotFound() {
    final var injector = Injector.injector().build();
    final var parentQualifier = new Key<>(String.class, Set.of());

    final var injectorReference = new InjectorReference();
    injectorReference.set(injector);

    final var node = new RefersTo<>(injectorReference, parentQualifier);
    assertThrows(
        NoSuchElementException.class,
        node::get,
        "When reference not found the exception must be thrown");
  }

  @Test
  void copyTest() {
    final var injector = Injector.injector().build();
    final var parentQualifier = new Key<>(String.class, Set.of());

    final var injectorReference = new InjectorReference();
    injectorReference.set(injector);

    final var original = new RefersTo<>(injectorReference, parentQualifier);
    final var copy = original.copy(injectorReference);

    assertNotNull(copy, "Copy must not be null");
    assertEquals(original, copy, "Copy must be equal to original");
    assertNotSame(original, copy, "Copy must not be the same as original");
  }

  @Test
  void toStringMustBeNonNull() {
    final var injector = Injector.injector().build();
    final var parentQualifier = new Key<>(String.class, Set.of());

    final var injectorReference = new InjectorReference();
    injectorReference.set(injector);

    final var original = new RefersTo<>(injectorReference, parentQualifier);

    assertNotNull(original.toString(), "String must not be null");
    assertFalse(original.toString().isBlank(), "String must not be blank");
  }

  @Test
  void toYamlStringMustBeNonNull() {
    final var expectedYaml =
        Stream.of(
                "references:",
                "  type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "  annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injector = Injector.injector().build();
    final var parentQualifier = new Key<>(String.class, Set.of());

    final var injectorReference = new InjectorReference();
    injectorReference.set(injector);

    final var original = new RefersTo<>(injectorReference, parentQualifier);

    assertEquals(expectedYaml, original.toYamlString(0), "YAML string must match the expectation");
  }

  @Test
  void toYamlStringMustSupportIndentation() {
    final var expectedYaml =
        Stream.of(
                "  references:",
                "    type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "    annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injector = Injector.injector().build();
    final var parentQualifier = new Key<>(String.class, Set.of());

    final var injectorReference = new InjectorReference();
    injectorReference.set(injector);

    final var original = new RefersTo<>(injectorReference, parentQualifier);

    assertEquals(expectedYaml, original.toYamlString(1), "YAML string must match the expectation");
  }
}
