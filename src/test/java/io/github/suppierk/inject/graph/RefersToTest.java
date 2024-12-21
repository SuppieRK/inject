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
  void object_methods_must_work_as_expected() {
    final var redInjector = Injector.injector().add("Red").build();
    final var blueInjector = Injector.injector().add("Blue").build();

    EqualsVerifier.simple()
        .forClass(RefersTo.class)
        .withPrefabValues(Injector.class, redInjector, blueInjector)
        .withIgnoredFields("injectorReference")
        .verify();
  }

  @Test
  void null_constructor_argument_throws_exception() {
    final var injectorReference = new InjectorReference();
    final var parentQualifier = new Key<>(String.class, Set.of());

    assertThrows(IllegalArgumentException.class, () -> new RefersTo<>(injectorReference, null));
    assertThrows(IllegalArgumentException.class, () -> new RefersTo<>(null, parentQualifier));
  }

  @Test
  void throws_no_such_element_if_reference_not_found() {
    final var injector = Injector.injector().build();
    final var parentQualifier = new Key<>(String.class, Set.of());

    final var injectorReference = new InjectorReference();
    injectorReference.set(injector);

    final var node = new RefersTo<>(injectorReference, parentQualifier);
    assertThrows(NoSuchElementException.class, node::get);
  }

  @Test
  void copy_test() {
    final var injector = Injector.injector().build();
    final var parentQualifier = new Key<>(String.class, Set.of());

    final var injectorReference = new InjectorReference();
    injectorReference.set(injector);

    final var original = new RefersTo<>(injectorReference, parentQualifier);
    final var copy = original.copy(injectorReference);

    assertNotNull(copy);
    assertEquals(original, copy);
    assertNotSame(original, copy);
  }

  @Test
  void to_string_must_be_non_null() {
    final var injector = Injector.injector().build();
    final var parentQualifier = new Key<>(String.class, Set.of());

    final var injectorReference = new InjectorReference();
    injectorReference.set(injector);

    final var original = new RefersTo<>(injectorReference, parentQualifier);

    assertNotNull(original.toString());
    assertFalse(original.toString().isBlank());
  }

  @Test
  void to_yaml_string_must_be_non_null() {
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

    assertEquals(expectedYaml, original.toYamlString(0));
  }

  @Test
  void to_yaml_string_must_support_indentation() {
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

    assertEquals(expectedYaml, original.toYamlString(1));
  }
}
