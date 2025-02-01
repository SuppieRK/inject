package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.graph.Node;
import io.github.suppierk.inject.graph.Value;
import io.github.suppierk.inject.query.KeyAnnotationsPredicate;
import io.github.suppierk.utils.ConsoleConstants;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InjectorTest {
  static class First {
    final Second second;

    @Inject
    First(Second second) {
      this.second = second;
    }
  }

  static class Second {
    final Provider<First> firstProvider;

    @Inject
    Second(Provider<First> firstProvider) {
      this.firstProvider = firstProvider;
    }
  }

  static class TestValue {
    private final long time;

    TestValue() {
      this.time = System.nanoTime();
    }

    public long get() {
      return time;
    }
  }

  static class TestValueProvider {
    @Provides
    @Named(value = "firstValue")
    TestValue firstValue() {
      return new TestValue();
    }

    @Provides
    @Named(value = "secondValue")
    TestValue secondValue() {
      return new TestValue();
    }
  }

  @Test
  void equalsAndHashCodeAreCorrect() {
    final var injectorReference = new InjectorReference();

    EqualsVerifier.simple()
        .forClass(Injector.class)
        .withPrefabValues(
            Node.class, new Value<>(injectorReference, "A"), new Value<>(injectorReference, "B"))
        .withIgnoredFields("currentInjector")
        .verify();
  }

  @Test
  void selfReferenceIsCorrect() {
    final var injector = Injector.injector().build();
    assertEquals(
        injector, injector.get(Injector.class), "Self reference must be equal to the injector");
    assertSame(
        injector, injector.get(Injector.class), "Self reference must be the same as the injector");
  }

  @Test
  void getCallThrowsForNullArgument() {
    final var injector = Injector.injector().build();
    assertThrows(
        IllegalArgumentException.class,
        () -> injector.get((Key<?>) null),
        "Null argument is not allowed");
    assertThrows(
        IllegalArgumentException.class,
        () -> injector.get((Class<?>) null),
        "Null argument is not allowed");
  }

  @Test
  void getNonExistingKeyThrowsNoSuchElementException() {
    final var key = new Key<>(String.class, Set.of());
    final var injector = Injector.injector().build();

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(key),
        "Retrieving non existing key must throw NoSuchElementException");
  }

  @Test
  void getExistingKeyReturnsCorrectValue() {
    final var value = "testValue";
    final var key = new Key<>(String.class, Set.of());
    final var injector = Injector.injector().add(value).build();

    final var retrievedValue =
        assertDoesNotThrow(
            () -> injector.get(key),
            "Retrieving existing key must not throw NoSuchElementException");
    assertEquals(value, retrievedValue, "Retrieved value must match to the expected value");
  }

  @Test
  void findOneThrowsIllegalArgumentIfAnyOfArgumentsIsNull() {
    final var injector = Injector.injector().build();

    assertThrows(
        IllegalArgumentException.class,
        () -> injector.findOne(null),
        "Must throw IllegalArgumentException if class is null");
    assertThrows(
        IllegalArgumentException.class,
        () -> injector.findOne(null, KeyAnnotationsPredicate.alwaysMatch()),
        "Must throw IllegalArgumentException if class is null");
    assertThrows(
        IllegalArgumentException.class,
        () -> injector.findOne(String.class, null),
        "Must throw IllegalArgumentException if predicate is null");
  }

  @Test
  void findOneThrowsIllegalStateExceptionForMultipleOptions() {
    final var injector = Injector.injector().add(TestValueProvider.class).build();

    assertThrows(
        IllegalStateException.class,
        () -> injector.findOne(TestValue.class),
        "Must throw IllegalStateException when there are more than one possible option");
  }

  @Test
  void findOneReturnsEmptyOptionalForNoOptions() {
    final var injector = Injector.injector().add(TestValueProvider.class).build();

    final var result =
        assertDoesNotThrow(
            () -> injector.findOne(String.class),
            "Must not throw IllegalStateException when there are no options");
    assertNotNull(result, "Result must not be null");
    assertTrue(result.isEmpty(), "Result must be empty");
  }

  @Test
  void findOneReturnsNonEmptyOptionalForSingleOption() {
    final var injector = Injector.injector().add(TestValueProvider.class).build();

    final var result =
        assertDoesNotThrow(
            () ->
                injector.findOne(
                    TestValue.class,
                    KeyAnnotationsPredicate.keyAnnotationPredicate()
                        .allMatch()
                        .having(
                            annotation ->
                                annotation
                                    .match(Named.class)
                                    .where(named -> named.value().equals("firstValue")))
                        .build()),
            "Must not throw IllegalStateException when there is one option");
    assertNotNull(result, "Result must not be null");
    assertTrue(result.isPresent(), "Result must be present");
  }

  @Test
  void findAllThrowsIllegalArgumentIfAnyOfArgumentsIsNull() {
    final var injector = Injector.injector().build();

    assertThrows(
        IllegalArgumentException.class,
        () -> injector.findAll(null),
        "Must throw IllegalArgumentException if class is null");
    assertThrows(
        IllegalArgumentException.class,
        () -> injector.findAll(null, KeyAnnotationsPredicate.alwaysMatch()),
        "Must throw IllegalArgumentException if class is null");
    assertThrows(
        IllegalArgumentException.class,
        () -> injector.findAll(String.class, null),
        "Must throw IllegalArgumentException if predicate is null");
  }

  @Test
  void findAllReturnsMultipleOptions() {
    final var injector = Injector.injector().add(TestValueProvider.class).build();

    final var result =
        assertDoesNotThrow(
            () -> injector.findAll(TestValue.class),
            "Must not throw exception when there are more than one possible option");
    assertNotNull(result, "Result must not be null");
    assertEquals(2, result.size(), "Expecting 2 options available");
  }

  @Test
  void findAllReturnsEmptyListForNoOptions() {
    final var injector = Injector.injector().add(TestValueProvider.class).build();

    final var result =
        assertDoesNotThrow(
            () -> injector.findAll(String.class),
            "Must not throw IllegalStateException when there are no options");
    assertNotNull(result, "Result must not be null");
    assertTrue(result.isEmpty(), "Result must be empty");
  }

  @Test
  void copyTest() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(First.class));
    assertDoesNotThrow(() -> builder.add(Second.class));
    final var injector = assertDoesNotThrow(builder::build);

    final var injectorCopyBuilder = assertDoesNotThrow(injector::copy);
    final var injectorCopy = assertDoesNotThrow(injectorCopyBuilder::build);

    assertEquals(injector, injectorCopy, "Copy must be equal to the original");
    assertNotSame(injector, injectorCopy, "Copy must not be the same as original");
  }

  @Test
  void toStringForEmptyInjectorMustReturnExpectedValue() {
    final var expectedYaml = "injector: [ ]";

    final var injector = Injector.injector().build();

    assertEquals(expectedYaml, injector.toString(), "YAML string must match the expectation");
  }

  @Test
  void toStringForNonEmptyInjectorMustHaveTopologicalOrder() {
    final var expectedYaml =
        Stream.of(
                "injector:",
                "  - type: " + ConsoleConstants.cyanBold(Second.class.getName()),
                "    annotations: [ ]",
                "    instance:",
                "      singleton: " + ConsoleConstants.blueBold("false"),
                "      constructor:",
                "        - type: " + ConsoleConstants.cyanBold(First.class.getName()),
                "          annotations: [ ]",
                "      fields: [ ]",
                "",
                "  - type: " + ConsoleConstants.cyanBold(First.class.getName()),
                "    annotations: [ ]",
                "    instance:",
                "      singleton: " + ConsoleConstants.blueBold("false"),
                "      constructor:",
                "        - type: " + ConsoleConstants.cyanBold(Second.class.getName()),
                "          annotations: [ ]",
                "      fields: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injector = Injector.injector().add(First.class).add(Second.class).build();

    assertEquals(expectedYaml, injector.toString(), "YAML string must match the expectation");
  }

  @Nested
  class Builder {
    @Test
    void factoryMethodReturnsNonNull() {
      assertNotNull(Injector.injector(), "Injector builder factory method must not return null");
    }

    @Test
    void acceptsMultipleClasses() {
      final var builder = Injector.injector();
      assertDoesNotThrow(
          () -> builder.add(First.class, Second.class),
          "There must be no problem with adding multiple classes");
      final var injector =
          assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

      assertNotNull(injector.get(First.class), "Dependency must be present");
      assertNotNull(injector.get(Second.class), "Dependency must be present");
    }

    @Test
    void acceptsMultipleObjects() {
      final var second = new Second(() -> null);
      final var first = new First(second);

      final var builder = Injector.injector();
      assertDoesNotThrow(
          () -> builder.add(first, second), "There must be no problem with adding multiple values");
      final var injector =
          assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

      assertEquals(first, injector.get(First.class), "Dependency must be present");
      assertEquals(second, injector.get(Second.class), "Dependency must be present");
    }

    @Test
    void doesNotAcceptNull() {
      final var builder = Injector.injector();

      assertThrows(
          IllegalArgumentException.class, () -> builder.add(null), "Null argument is not allowed");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.add((Object) null),
          "Null argument is not allowed");
    }

    @Test
    void doesNotAcceptAbstractClasses() {
      final var builder = Injector.injector();

      assertThrows(
          IllegalArgumentException.class,
          () -> builder.add(AbstractMap.class),
          "Abstract class is not allowed");
    }

    @Test
    void doesNotAcceptInterfaces() {
      final var builder = Injector.injector();

      assertThrows(
          IllegalArgumentException.class,
          () -> builder.add(Cloneable.class),
          "Interface is not allowed");
    }

    @Test
    void doesNotAcceptClassDuplicates() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(String.class), "Simple class is allowed");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.add(String.class),
          "Duplication of the same class is not allowed");
    }

    @Test
    void doesNotAcceptValueDuplicates() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add("Original"), "Simple value is allowed");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.add("Duplicate"),
          "Duplicated value on the same class is not allowed");
    }
  }

  @Nested
  class CopyBuilder {
    class OldValue {
      OldValue() {}
    }

    class NewValue extends OldValue {
      NewValue() {
        super();
      }
    }

    class AnotherValue extends OldValue {
      AnotherValue() {
        super();
      }
    }

    @Test
    void doesNotAcceptNull() {
      final var builder = Injector.injector().build().copy();

      final var originalClass = HashMap.class;
      final var replacementClass = LinkedHashMap.class;

      final var original = new HashMap<String, String>();
      final var replacement = new LinkedHashMap<String, String>();

      assertThrows(
          IllegalArgumentException.class,
          () -> builder.replace(originalClass, null),
          "Null replacement class is not allowed");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.replace(null, replacementClass),
          "Null original class is not allowed");

      assertThrows(
          IllegalArgumentException.class,
          () -> builder.replace(original, null),
          "Null replacement value is not allowed");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.replace(null, replacement),
          "Null original value is not allowed");
    }

    @Test
    void doesNotReplaceSameClass() {
      final var injector = Injector.injector().add(OldValue.class).build();
      final var builder = injector.copy();

      assertNotNull(
          assertDoesNotThrow(
              () -> builder.replace(OldValue.class, OldValue.class),
              "Replacement must not throw an exception"),
          "Replacement call must return non-null builder instance");
      final var copiedInjector =
          assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

      assertEquals(
          copiedInjector,
          injector,
          "Copy of the injector after replacement should be equal to original");
    }

    @Test
    void doesNotReplaceSameObject() {
      final var injector = Injector.injector().add("Object").build();
      final var builder = injector.copy();

      assertNotNull(
          assertDoesNotThrow(
              () -> builder.replace("Object", "Object"), "Replacement must not throw an exception"),
          "Replacement call must return non-null builder instance");
      final var copiedInjector =
          assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

      assertEquals(
          copiedInjector,
          injector,
          "Copy of the injector after replacement should be equal to original");
    }

    @Test
    void doesNotAcceptNonExistentKey() {
      final var builder = Injector.injector().build().copy();

      assertThrows(
          IllegalArgumentException.class,
          () -> builder.replace(OldValue.class, NewValue.class),
          "Replacing missing dependency must throw an exception");
    }

    @Test
    void doesNotAcceptOverrideOfOverride() {
      final var builder = Injector.injector().add(OldValue.class).build().copy();

      assertNotNull(
          assertDoesNotThrow(
              () -> builder.replace(OldValue.class, NewValue.class),
              "Replacement must not throw an exception"),
          "Replacement call must return non-null builder instance");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.replace(OldValue.class, AnotherValue.class),
          "Replacing already replaced dependency must throw an exception");
    }

    @Test
    void doesNotAcceptOverrideToExistingKey() {
      final var builder =
          Injector.injector().add(OldValue.class).add(NewValue.class).build().copy();

      assertThrows(
          IllegalArgumentException.class,
          () -> builder.replace(OldValue.class, NewValue.class),
          "Replacement to already defined dependency must throw an exception");
    }
  }
}
