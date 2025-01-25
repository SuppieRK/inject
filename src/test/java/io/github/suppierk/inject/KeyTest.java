package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.utils.ConsoleConstants;
import jakarta.inject.Named;
import jakarta.inject.Qualifier;
import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KeyTest {
  @Test
  void objectMethodsMustWorkAsExpected() {
    EqualsVerifier.forClass(Key.class).verify();
  }

  @Test
  void toStringMustNotBeNull() {
    final var key = new Key<>(String.class, null);

    assertNotNull(key.toString(), "String must not be null");
    assertFalse(key.toString().isBlank(), "String must not be blank");
  }

  @Test
  void toYamlStringMustNotBeNull() {
    final var expectedYaml =
        Stream.of("type: " + ConsoleConstants.cyanBold(String.class.getName()), "annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var key = new Key<>(String.class, null);
    final var actualYaml = key.toYamlString(false, 0);

    assertEquals(expectedYaml, actualYaml, "YAML string must match the expectation");
  }

  @Test
  void toYamlStringAnnotationTest() {
    final var expectedYaml =
        Stream.of(
                "type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "annotations:",
                "  - '@"
                    + ConsoleConstants.yellow(Named.class.getName())
                    + "(value=\"NamedAnnotation\")'")
            .collect(Collectors.joining(String.format("%n")));

    final var clazz = NamedAnnotation.class;
    final var qualifier = clazz.getAnnotation(Named.class);
    final var key = new Key<>(String.class, Set.of(qualifier));
    final var actualYaml = key.toYamlString(false, 0);

    assertEquals(expectedYaml, actualYaml, "YAML string must match the expectation");
  }

  @Test
  void toYamlStringIndentationTest() {
    final var expectedYaml =
        Stream.of(
                "  type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "  annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var key = new Key<>(String.class, null);
    final var actualYaml = key.toYamlString(false, 1);

    assertEquals(expectedYaml, actualYaml, "YAML string must match the expectation");
  }

  @Test
  void toYamlStringNegativeIndentationTest() {
    final var key = new Key<>(String.class, null);

    assertThrows(
        IllegalArgumentException.class,
        () -> key.toYamlString(false, -1),
        "Negative YAML indentation must throw an exception");
  }

  @Test
  void toYamlStringItemizeTest() {
    final var expectedYaml =
        Stream.of(
                "- type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "  annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var key = new Key<>(String.class, null);
    final var actualYaml = key.toYamlString(true, 0);

    assertEquals(expectedYaml, actualYaml, "YAML string must match the expectation");
  }

  @Test
  void toYamlStringItemizeAndIndentationTest() {
    final var expectedYaml =
        Stream.of(
                "  - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "    annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var key = new Key<>(String.class, null);
    final var actualYaml = key.toYamlString(true, 1);

    assertEquals(expectedYaml, actualYaml, "YAML string must match the expectation");
  }

  @Nested
  class ClassBased {
    @Test
    void constructorFailsForNullClass() {
      assertThrows(
          IllegalArgumentException.class,
          () -> new Key<>(null, null),
          "Null class must throw an exception");
    }

    @Test
    void constructorForClassMustWorkAsExpected() {
      final var clazz = String.class;

      final var key = new Key<>(clazz, null);

      assertEquals(clazz, key.type(), "Dependency type must match");
      assertNotNull(key.annotationWrappers(), "Annotation must not be null");
      assertTrue(key.annotationWrappers().isEmpty(), "Annotation must not be null");
      assertNotNull(key.toString(), "toString must not be null");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface NotQualifier {
    String customValue();
  }

  @Nested
  @NotQualifier(customValue = "notQualifierCustomValue")
  class NotQualifierAnnotation {
    @Test
    void constructorMustThrowExpectedException() {
      final var clazz = NotQualifierAnnotation.class;
      final var named = clazz.getAnnotation(NotQualifier.class);
      final var annotations = Set.<Annotation>of(named);

      assertThrows(
          IllegalArgumentException.class,
          () -> new Key<>(clazz, annotations),
          "Non Qualifier annotations must throw an exception");
    }
  }

  @Nested
  @Named("NamedAnnotation")
  class NamedAnnotation {
    @Test
    void constructorMustWorkAsExpected() {
      final var clazz = NamedAnnotation.class;
      final var qualifier = clazz.getAnnotation(Named.class);

      final var key = new Key<>(clazz, Set.of(qualifier));

      assertEquals(clazz, key.type(), "Dependency type must match");
      assertNotNull(key.annotationWrappers(), "Annotations must not be null");
      assertTrue(
          key.annotationWrappers().stream()
              .anyMatch(wrapper -> wrapper.annotation().equals(qualifier)),
          "Annotation must be recorded");
      assertNotNull(key.toString(), "toString must not be null");
    }
  }

  @Qualifier
  @Retention(RetentionPolicy.RUNTIME)
  @interface CustomQualifier {
    String customValue();

    int customIntValue();
  }

  @Nested
  @CustomQualifier(customValue = "CustomQualifierAnnotation123", customIntValue = 123)
  class CustomQualifierAnnotation {
    @Test
    void constructorMustWorkAsExpected() {
      final var clazz = CustomQualifierAnnotation.class;
      final var qualifier = clazz.getAnnotation(CustomQualifier.class);

      final var key = new Key<>(clazz, Set.of(qualifier));

      assertEquals(clazz, key.type(), "Dependency type must match");
      assertNotNull(key.annotationWrappers(), "Annotations must not be null");
      assertTrue(
          key.annotationWrappers().stream()
              .anyMatch(wrapper -> wrapper.annotation().equals(qualifier)),
          "Annotation must be recorded");
      assertNotNull(key.toString(), "toString must not be null");
    }
  }

  @Nested
  @Named("MultipleQualifierAnnotations")
  @CustomQualifier(customValue = "MultipleQualifierAnnotations456", customIntValue = 456)
  class MultipleQualifierAnnotations {
    @Test
    void constructorMustWorkAsExpected() {
      final var clazz = MultipleQualifierAnnotations.class;
      final var firstQualifier = clazz.getAnnotation(Named.class);
      final var secondQualifier = clazz.getAnnotation(CustomQualifier.class);

      final var key = new Key<>(clazz, Set.of(firstQualifier, secondQualifier));

      assertEquals(clazz, key.type(), "Dependency type must match");
      assertNotNull(key.annotationWrappers(), "Annotations must not be null");
      assertTrue(
          key.annotationWrappers().stream()
              .anyMatch(wrapper -> wrapper.annotation().equals(firstQualifier)),
          "Annotation must be recorded");
      assertTrue(
          key.annotationWrappers().stream()
              .anyMatch(wrapper -> wrapper.annotation().equals(secondQualifier)),
          "Annotation must be recorded");
      assertNotNull(key.toString(), "toString must not be null");
    }
  }
}
