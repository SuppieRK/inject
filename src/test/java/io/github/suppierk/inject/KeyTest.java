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
  void object_methods_must_work_as_expected() {
    EqualsVerifier.forClass(Key.class).verify();
  }

  @Test
  void to_string_must_not_be_null() {
    final var key = new Key<>(String.class, null);

    assertNotNull(key.toString());
    assertFalse(key.toString().isEmpty());
  }

  @Test
  void to_yaml_string_must_not_be_null() {
    final var expectedYaml =
        Stream.of("type: " + ConsoleConstants.cyanBold(String.class.getName()), "annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var key = new Key<>(String.class, null);
    final var actualYaml = key.toYamlString(false, 0);

    assertNotNull(actualYaml);
    assertEquals(expectedYaml, actualYaml);
  }

  @Test
  void to_yaml_string_annotation_test() {
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

    assertNotNull(actualYaml);
    assertEquals(expectedYaml, actualYaml);
  }

  @Test
  void to_yaml_string_indentation_test() {
    final var expectedYaml =
        Stream.of(
                "  type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "  annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var key = new Key<>(String.class, null);
    final var actualYaml = key.toYamlString(false, 1);

    assertNotNull(actualYaml);
    assertEquals(expectedYaml, actualYaml);
  }

  @Test
  void to_yaml_string_negative_indentation_test() {
    final var key = new Key<>(String.class, null);

    assertThrows(IllegalArgumentException.class, () -> key.toYamlString(false, -1));
  }

  @Test
  void to_yaml_string_itemize_test() {
    final var expectedYaml =
        Stream.of(
                "- type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "  annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var key = new Key<>(String.class, null);
    final var actualYaml = key.toYamlString(true, 0);

    assertNotNull(actualYaml);
    assertEquals(expectedYaml, actualYaml);
  }

  @Test
  void to_yaml_string_itemize_and_indentation_test() {
    final var expectedYaml =
        Stream.of(
                "  - type: " + ConsoleConstants.cyanBold(String.class.getName()),
                "    annotations: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var key = new Key<>(String.class, null);
    final var actualYaml = key.toYamlString(true, 1);

    assertNotNull(actualYaml);
    assertEquals(expectedYaml, actualYaml);
  }

  @Nested
  class ClassBased {
    @Test
    void constructor_fails_for_null_class() {
      assertThrows(IllegalArgumentException.class, () -> new Key<>(null, null));
    }

    @Test
    void constructor_for_class_must_work_as_expected() {
      final var clazz = String.class;

      final var key = new Key<>(clazz, null);

      assertEquals(clazz, key.type(), "Dependency type must match");
      assertNotNull(key.annotations(), "Annotation must not be null");
      assertTrue(key.annotations().isEmpty(), "Annotation must not be null");
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
    void constructor_must_throw_expected_exception() {
      final var clazz = NotQualifierAnnotation.class;
      final var named = clazz.getAnnotation(NotQualifier.class);
      final var annotations = Set.<Annotation>of(named);

      assertThrows(IllegalArgumentException.class, () -> new Key<>(clazz, annotations));
    }
  }

  @Nested
  @Named("NamedAnnotation")
  class NamedAnnotation {
    @Test
    void constructor_must_work_as_expected() {
      final var clazz = NamedAnnotation.class;
      final var qualifier = clazz.getAnnotation(Named.class);

      final var key = new Key<>(clazz, Set.of(qualifier));

      assertEquals(clazz, key.type(), "Dependency type must match");
      assertNotNull(key.annotations(), "Annotations must not be null");
      assertTrue(key.annotations().contains(qualifier), "Annotation must be recorded");
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
    void constructor_must_work_as_expected() {
      final var clazz = CustomQualifierAnnotation.class;
      final var qualifier = clazz.getAnnotation(CustomQualifier.class);

      final var key = new Key<>(clazz, Set.of(qualifier));

      assertEquals(clazz, key.type(), "Dependency type must match");
      assertNotNull(key.annotations(), "Annotations must not be null");
      assertTrue(key.annotations().contains(qualifier), "Annotation must be recorded");
      assertNotNull(key.toString(), "toString must not be null");
    }
  }

  @Nested
  @Named("MultipleQualifierAnnotations")
  @CustomQualifier(customValue = "MultipleQualifierAnnotations456", customIntValue = 456)
  class MultipleQualifierAnnotations {
    @Test
    void constructor_must_work_as_expected() {
      final var clazz = MultipleQualifierAnnotations.class;
      final var firstQualifier = clazz.getAnnotation(Named.class);
      final var secondQualifier = clazz.getAnnotation(CustomQualifier.class);

      final var key = new Key<>(clazz, Set.of(firstQualifier, secondQualifier));

      assertEquals(clazz, key.type(), "Dependency type must match");
      assertNotNull(key.annotations(), "Annotations must not be null");
      assertTrue(key.annotations().contains(firstQualifier), "Annotation must be recorded");
      assertTrue(key.annotations().contains(secondQualifier), "Annotation must be recorded");
      assertNotNull(key.toString(), "toString must not be null");
    }
  }
}
