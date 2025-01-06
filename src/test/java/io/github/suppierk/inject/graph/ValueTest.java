package io.github.suppierk.inject.graph;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.InjectorReference;
import io.github.suppierk.mocks.TimedCloseable;
import io.github.suppierk.utils.ConsoleConstants;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class ValueTest {
  @Test
  void objectMethodsMustWorkAsExpected() {
    final var redInjector = Injector.injector().add("Red").build();
    final var blueInjector = Injector.injector().add("Blue").build();

    EqualsVerifier.simple()
        .forClass(Value.class)
        .withPrefabValues(Injector.class, redInjector, blueInjector)
        .withIgnoredFields("injectorReference", "parentKeys", "onCloseConsumer")
        .verify();
  }

  @Test
  void nullConstructorArgumentThrowsException() {
    final var injectorReference = new InjectorReference();

    assertThrows(
        IllegalArgumentException.class,
        () -> new Value<>(injectorReference, null),
        "Null instance must throw an exception");
    assertThrows(
        IllegalArgumentException.class,
        () -> new Value<>(null, "A"),
        "Null injector reference must throw an exception");
  }

  @Test
  void copyTest() {
    final var injectorReference = new InjectorReference();

    final var original = new Value<>(injectorReference, "original");
    final var copy = original.copy(new InjectorReference());

    assertNotNull(copy, "Copy must not be null");
    assertEquals(original, copy, "Copy must be equal to original");
    assertNotSame(original, copy, "Copy must not be the same as original");
  }

  @Test
  void closeTest() {
    final var injectorReference = new InjectorReference();
    final var value = new TimedCloseable();

    final var original = new Value<>(injectorReference, value);

    assertDoesNotThrow(original::close, "Close must not throw an exception");
    assertTrue(value.wasCloseCalled(), "Underlying close must be called");
  }

  @Test
  void toStringMustBeNonNull() {
    final var injectorReference = new InjectorReference();
    final var original = new Value<>(injectorReference, "original");

    assertNotNull(original.toString(), "String must not be null");
    assertFalse(original.toString().isBlank(), "String must not be blank");
  }

  @Test
  void toYamlStringMustBeNonNull() {
    final var expectedYaml =
        Stream.of("instance:", "  singleton: " + ConsoleConstants.blueBold("true"))
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var original = new Value<>(injectorReference, "original");

    assertEquals(expectedYaml, original.toYamlString(0), "YAML string must match the expectation");
  }

  @Test
  void toYamlStringMustSupportIndentation() {
    final var expectedYaml =
        Stream.of("  instance:", "    singleton: " + ConsoleConstants.blueBold("true"))
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var original = new Value<>(injectorReference, "original");

    assertEquals(expectedYaml, original.toYamlString(1), "YAML string must match the expectation");
  }
}
