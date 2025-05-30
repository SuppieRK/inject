package io.github.suppierk.inject.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import io.github.suppierk.inject.FieldInformation;
import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.InjectorReference;
import io.github.suppierk.inject.Key;
import io.github.suppierk.inject.ParameterInformation;
import io.github.suppierk.utils.ConsoleConstants;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class ConstructsSingletonTest {
  static class Value {
    private final String first;
    private final Long second;

    Value(String first, Long second) {
      this.first = first;
      this.second = second;
    }

    static Constructor<Value> constructor() {
      try {
        return Value.class.getDeclaredConstructor(String.class, Long.class);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  void objectMethodsMustWorkAsExpected() {
    final var redInjector = Injector.injector().add("Red").build();
    final var blueInjector = Injector.injector().add("Blue").build();

    final var constructor = Value.constructor();
    final var redParameter = constructor.getParameters()[0];
    final var blueParameter = constructor.getParameters()[1];

    EqualsVerifier.simple()
        .forClass(ConstructsSingleton.class)
        .withPrefabValues(Injector.class, redInjector, blueInjector)
        .withPrefabValues(Parameter.class, redParameter, blueParameter)
        .withIgnoredFields("injectorReference", "constructor", "memoized", "onCloseConsumer")
        .verify();
  }

  @Test
  void copyTest() {
    final var injectorReference = new InjectorReference();
    final var constructor = Value.constructor();

    final var original =
        new ConstructsSingleton<>(injectorReference, constructor, List.of(), List.of());
    final var copy = original.copy(injectorReference);

    assertNotNull(copy, "Copy must be non null");
    assertEquals(original, copy, "Copy must be equal to original");
    assertNotSame(original, copy, "Copy must not be the same as original");
  }

  @Test
  void toStringMustBeNonNull() {
    final var injectorReference = new InjectorReference();
    final var constructor = Value.constructor();

    final var original =
        new ConstructsSingleton<>(injectorReference, constructor, List.of(), List.of());

    assertNotNull(original.toString(), "String must not be null");
    assertFalse(original.toString().isBlank(), "String must not be blank");
  }

  @Test
  void toYamlStringMustBeNonNull() {
    final var expectedYaml =
        Stream.of(
                "instance:",
                "  singleton: " + ConsoleConstants.blueBold("true"),
                "  constructor: [ ]",
                "  fields: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var constructor = Value.constructor();

    final var original =
        new ConstructsSingleton<>(injectorReference, constructor, List.of(), List.of());

    assertEquals(expectedYaml, original.toYamlString(0), "YAML string must match the expectation");
  }

  @Test
  void toYamlStringMustHaveCorrectIndentation() {
    final var expectedYaml =
        Stream.of(
                "  instance:",
                "    singleton: " + ConsoleConstants.blueBold("true"),
                "    constructor: [ ]",
                "    fields: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var constructor = Value.constructor();

    final var original =
        new ConstructsSingleton<>(injectorReference, constructor, List.of(), List.of());

    assertEquals(expectedYaml, original.toYamlString(1), "YAML string must match the expectation");
  }

  @Test
  void toYamlStringMustSupportParametersAndFields() throws Exception {
    final var expectedYaml =
        Stream.of(
                "instance:",
                "  singleton: " + ConsoleConstants.blueBold("true"),
                "  constructor:",
                "    - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "      annotations: [ ]",
                "  fields:",
                "    - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "      annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var constructor = Value.constructor();
    final var parameter = constructor.getParameters()[0];
    final var field = Value.class.getDeclaredField("first");

    final var original =
        new ConstructsSingleton<>(
            injectorReference,
            constructor,
            List.of(
                new ParameterInformation(parameter, new Key<>(parameter.getType(), null), null)),
            List.of(new FieldInformation(field, new Key<>(field.getType(), null), null)));

    assertEquals(expectedYaml, original.toYamlString(0), "YAML string must match the expectation");
  }

  @Test
  void toYamlStringMustSupportParametersAndFieldsWithCorrectIndentation() throws Exception {
    final var expectedYaml =
        Stream.of(
                "  instance:",
                "    singleton: " + ConsoleConstants.blueBold("true"),
                "    constructor:",
                "      - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "        annotations: [ ]",
                "    fields:",
                "      - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "        annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var constructor = Value.constructor();
    final var parameter = constructor.getParameters()[0];
    final var field = Value.class.getDeclaredField("first");

    final var original =
        new ConstructsSingleton<>(
            injectorReference,
            constructor,
            List.of(
                new ParameterInformation(parameter, new Key<>(parameter.getType(), null), null)),
            List.of(new FieldInformation(field, new Key<>(field.getType(), null), null)));

    assertEquals(expectedYaml, original.toYamlString(1), "YAML string must match the expectation");
  }
}
