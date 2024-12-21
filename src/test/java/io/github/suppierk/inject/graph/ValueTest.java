package io.github.suppierk.inject.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.InjectorReference;
import io.github.suppierk.utils.ConsoleConstants;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ValueTest {
  @Test
  void object_methods_must_work_as_expected() {
    final var redInjector = Injector.injector().add("Red").build();
    final var blueInjector = Injector.injector().add("Blue").build();

    EqualsVerifier.simple()
        .forClass(Value.class)
        .withPrefabValues(Injector.class, redInjector, blueInjector)
        .withIgnoredFields("injectorReference", "parentKeys")
        .verify();
  }

  @Test
  void null_constructor_argument_throws_exception() {
    final var injectorReference = new InjectorReference();

    assertThrows(IllegalArgumentException.class, () -> new Value<>(injectorReference, null));
    assertThrows(IllegalArgumentException.class, () -> new Value<>(null, "A"));
  }

  @Test
  void copy_test() {
    final var injectorReference = new InjectorReference();

    final var original = new Value<>(injectorReference, "original");
    final var copy = original.copy(new InjectorReference());

    assertNotNull(copy);
    assertEquals(original, copy);
    assertNotSame(original, copy);
  }

  @Test
  void to_string_must_be_non_null() {
    final var injectorReference = new InjectorReference();
    final var original = new Value<>(injectorReference, "original");

    assertNotNull(original.toString());
    assertFalse(original.toString().isBlank());
  }

  @Test
  void to_yaml_string_must_be_non_null() {
    final var expectedYaml =
        Stream.of("instance:", "  singleton: " + ConsoleConstants.blueBold("true"))
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var original = new Value<>(injectorReference, "original");

    assertEquals(expectedYaml, original.toYamlString(0));
  }

  @Test
  void to_yaml_string_must_support_indentation() {
    final var expectedYaml =
        Stream.of("  instance:", "    singleton: " + ConsoleConstants.blueBold("true"))
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var original = new Value<>(injectorReference, "original");

    assertEquals(expectedYaml, original.toYamlString(1));
  }
}
