package io.github.suppierk.inject.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.FieldInformation;
import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.InjectorReference;
import io.github.suppierk.inject.Key;
import io.github.suppierk.inject.ParameterInformation;
import io.github.suppierk.utils.ConsoleConstants;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class ProvidesNewTest {
  static class Value {
    String first;
    Long second;

    Value(String first, Long second) {
      this.first = first;
      this.second = second;
    }

    String method(String test) {
      return test;
    }

    static Constructor<Value> constructor() {
      try {
        return Value.class.getDeclaredConstructor(String.class, Long.class);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    static Method method() {
      try {
        return Value.class.getDeclaredMethod("method", String.class);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  void object_methods_must_work_as_expected() {
    final var redInjector = Injector.injector().add("Red").build();
    final var blueInjector = Injector.injector().add("Blue").build();

    final var constructor = Value.constructor();
    final var redParameter = constructor.getParameters()[0];
    final var blueParameter = constructor.getParameters()[1];

    EqualsVerifier.simple()
        .forClass(ProvidesNew.class)
        .withPrefabValues(Injector.class, redInjector, blueInjector)
        .withPrefabValues(Parameter.class, redParameter, blueParameter)
        .withIgnoredFields("injectorReference")
        .verify();
  }

  @Test
  void null_constructor_argument_throws_exception() {
    final var injectorReference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());
    final var method = Value.method();
    final var methodReturnClass = String.class;
    final var parameters = List.<ParameterInformation>of();
    final var fields = List.<FieldInformation>of();

    assertThrows(
        IllegalArgumentException.class,
        () -> new ProvidesNew<>(null, qualifier, method, methodReturnClass, parameters, fields));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProvidesNew<>(
                injectorReference, null, method, methodReturnClass, parameters, fields));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProvidesNew<>(
                injectorReference, qualifier, null, methodReturnClass, parameters, fields));
    assertThrows(
        IllegalArgumentException.class,
        () -> new ProvidesNew<>(injectorReference, qualifier, method, null, parameters, fields));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProvidesNew<>(
                injectorReference, qualifier, method, methodReturnClass, null, fields));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ProvidesNew<>(
                injectorReference, qualifier, method, methodReturnClass, parameters, null));
  }

  @Test
  void copy_test() {
    final var injectorReference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());
    final var method = Value.method();
    final var methodReturnClass = String.class;

    final var original =
        new ProvidesNew<>(
            injectorReference, qualifier, method, methodReturnClass, List.of(), List.of());
    final var copy = original.copy(injectorReference);

    assertNotNull(copy);
    assertEquals(original, copy);
    assertNotSame(original, copy);
  }

  @Test
  void to_string_must_be_non_null() {
    final var injectorReference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());
    final var method = Value.method();
    final var methodReturnClass = String.class;

    final var original =
        new ProvidesNew<>(
            injectorReference, qualifier, method, methodReturnClass, List.of(), List.of());

    assertNotNull(original.toString());
    assertFalse(original.toString().isBlank());
  }

  @Test
  void to_yaml_string_must_be_non_null() {
    final var expectedYaml =
        Stream.of(
                "instance:",
                "  singleton: " + ConsoleConstants.blueBold("false"),
                "  method: " + ConsoleConstants.cyanBold(Value.class.getName()) + ".method",
                "  parameters: [ ]",
                "  fields: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());
    final var method = Value.method();
    final var methodReturnClass = String.class;

    final var original =
        new ProvidesNew<>(
            injectorReference, qualifier, method, methodReturnClass, List.of(), List.of());

    assertEquals(expectedYaml, original.toYamlString(0));
  }

  @Test
  void to_yaml_string_must_have_correct_indentation() {
    final var expectedYaml =
        Stream.of(
                "  instance:",
                "    singleton: " + ConsoleConstants.blueBold("false"),
                "    method: " + ConsoleConstants.cyanBold(Value.class.getName()) + ".method",
                "    parameters: [ ]",
                "    fields: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());
    final var method = Value.method();
    final var methodReturnClass = String.class;

    final var original =
        new ProvidesNew<>(
            injectorReference, qualifier, method, methodReturnClass, List.of(), List.of());

    assertEquals(expectedYaml, original.toYamlString(1));
  }

  @Test
  void to_yaml_string_must_support_parameters_and_fields() throws Exception {
    final var expectedYaml =
        Stream.of(
                "instance:",
                "  singleton: " + ConsoleConstants.blueBold("false"),
                "  method: " + ConsoleConstants.cyanBold(Value.class.getName()) + ".method",
                "  parameters:",
                "    - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "      annotations: [ ]",
                "  fields:",
                "    - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "      annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());
    final var method = Value.method();
    final var methodReturnClass = String.class;
    final var parameter = Value.constructor().getParameters()[0];
    final var field = Value.class.getDeclaredField("first");

    final var original =
        new ProvidesNew<>(
            injectorReference,
            qualifier,
            method,
            methodReturnClass,
            List.of(
                new ParameterInformation(parameter, new Key<>(parameter.getType(), null), null)),
            List.of(new FieldInformation(field, new Key<>(field.getType(), null), null)));

    assertEquals(expectedYaml, original.toYamlString(0));
  }

  @Test
  void to_yaml_string_must_support_parameters_and_fields_with_correct_indentation()
      throws Exception {
    final var expectedYaml =
        Stream.of(
                "  instance:",
                "    singleton: " + ConsoleConstants.blueBold("false"),
                "    method: " + ConsoleConstants.cyanBold(Value.class.getName()) + ".method",
                "    parameters:",
                "      - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "        annotations: [ ]",
                "    fields:",
                "      - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "        annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injectorReference = new InjectorReference();
    final var qualifier = new Key<>(String.class, Set.of());
    final var method = Value.method();
    final var methodReturnClass = String.class;
    final var parameter = Value.constructor().getParameters()[0];
    final var field = Value.class.getDeclaredField("first");

    final var original =
        new ProvidesNew<>(
            injectorReference,
            qualifier,
            method,
            methodReturnClass,
            List.of(
                new ParameterInformation(parameter, new Key<>(parameter.getType(), null), null)),
            List.of(new FieldInformation(field, new Key<>(field.getType(), null), null)));

    assertEquals(expectedYaml, original.toYamlString(1));
  }
}
