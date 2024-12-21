package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.graph.Node;
import io.github.suppierk.inject.graph.Value;
import io.github.suppierk.utils.ConsoleConstants;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class InjectorTest {
  static class A {
    final B b;

    @Inject
    A(B b) {
      this.b = b;
    }
  }

  static class B {
    final Provider<A> a;

    @Inject
    B(Provider<A> a) {
      this.a = a;
    }
  }

  @Test
  void equals_hash_code_test() {
    final var injectorReference = new InjectorReference();

    EqualsVerifier.simple()
        .forClass(Injector.class)
        .withPrefabValues(
            Node.class, new Value<>(injectorReference, "A"), new Value<>(injectorReference, "B"))
        .withIgnoredFields("currentInjector")
        .verify();
  }

  @Test
  void injector_self_reference_is_correct() {
    final var injector = Injector.injector().build();
    assertEquals(injector, injector.get(Injector.class));
    assertSame(injector, injector.get(Injector.class));
  }

  @Test
  void get_does_not_accept_null() {
    final var injector = Injector.injector().build();
    assertThrows(IllegalArgumentException.class, () -> injector.get(null));
  }

  @Test
  void copy_test() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(A.class));
    assertDoesNotThrow(() -> builder.add(B.class));
    final var injector = assertDoesNotThrow(builder::build);

    final var injectorCopyBuilder = assertDoesNotThrow(injector::copy);
    final var injectorCopy = assertDoesNotThrow(injectorCopyBuilder::build);

    assertNotSame(injector, injectorCopy);
    assertEquals(injector, injectorCopy);
  }

  @Test
  void to_string_must_be_non_null() {
    final var expectedYaml = "injector: [ ]";

    final var injector = Injector.injector().build();

    assertEquals(expectedYaml, injector.toString());
  }

  @Test
  void to_string_must_have_topological_order() {
    /**
     * injector: - type: io.github.suppierk.inject.normal.CircularDependenciesProviderBreakTest$B
     * annotations: [ ] instance: singleton: false constructor: - type:
     * io.github.suppierk.inject.normal.CircularDependenciesProviderBreakTest$A annotations: [ ]
     * fields: [ ]
     *
     * <p>- type: io.github.suppierk.inject.normal.CircularDependenciesProviderBreakTest$A
     * annotations: [ ] instance: singleton: false constructor: - type:
     * io.github.suppierk.inject.normal.CircularDependenciesProviderBreakTest$B annotations: [ ]
     * fields: [ ]
     */
    final var expectedYaml =
        Stream.of(
                "injector:",
                "  - type: " + ConsoleConstants.cyanBold(B.class.getName()),
                "    annotations: [ ]",
                "    instance:",
                "      singleton: " + ConsoleConstants.blueBold("false"),
                "      constructor:",
                "        - type: " + ConsoleConstants.cyanBold(A.class.getName()),
                "          annotations: [ ]",
                "      fields: [ ]",
                "",
                "  - type: " + ConsoleConstants.cyanBold(A.class.getName()),
                "    annotations: [ ]",
                "    instance:",
                "      singleton: " + ConsoleConstants.blueBold("false"),
                "      constructor:",
                "        - type: " + ConsoleConstants.cyanBold(B.class.getName()),
                "          annotations: [ ]",
                "      fields: [ ]")
            .collect(Collectors.joining(String.format("%n")));

    final var injector = Injector.injector().add(A.class).add(B.class).build();

    assertEquals(expectedYaml, injector.toString());
  }

  @Nested
  class Builder {
    @Test
    void does_not_accept_null() {
      final var builder = Injector.injector();

      assertThrows(IllegalArgumentException.class, () -> builder.add(null));
      assertThrows(IllegalArgumentException.class, () -> builder.add((Object) null));
    }

    @Test
    void does_not_accept_abstract_classes() {
      final var builder = Injector.injector();

      assertThrows(IllegalArgumentException.class, () -> builder.add(AbstractMap.class));
    }

    @Test
    void does_not_accept_interfaces() {
      final var builder = Injector.injector();

      assertThrows(IllegalArgumentException.class, () -> builder.add(Cloneable.class));
    }

    @Test
    void does_not_accept_class_duplicates() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(String.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(String.class));
    }

    @Test
    void does_not_accept_object_duplicates() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add("Original"));
      assertThrows(IllegalArgumentException.class, () -> builder.add("Duplicate"));
    }
  }

  @Nested
  class Copy {
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
    void does_not_accept_null() {
      final var builder = Injector.injector().build().copy();

      final var originalClass = HashMap.class;
      final var replacementClass = LinkedHashMap.class;

      final var original = new HashMap<String, String>();
      final var replacement = new LinkedHashMap<String, String>();

      assertThrows(IllegalArgumentException.class, () -> builder.replace(originalClass, null));
      assertThrows(IllegalArgumentException.class, () -> builder.replace(null, replacementClass));

      assertThrows(IllegalArgumentException.class, () -> builder.replace(original, null));
      assertThrows(IllegalArgumentException.class, () -> builder.replace(null, replacement));
    }

    @Test
    void does_not_replace_same_class() {
      final var injector = Injector.injector().add(OldValue.class).build();
      final var builder = injector.copy();

      assertNotNull(assertDoesNotThrow(() -> builder.replace(OldValue.class, OldValue.class)));
      final var copiedInjector = assertDoesNotThrow(builder::build);

      assertEquals(copiedInjector, injector);
    }

    @Test
    void does_not_replace_same_object() {
      final var injector = Injector.injector().add("Object").build();
      final var builder = injector.copy();

      assertNotNull(assertDoesNotThrow(() -> builder.replace("Object", "Object")));
      final var copiedInjector = assertDoesNotThrow(builder::build);

      assertEquals(copiedInjector, injector);
    }

    @Test
    void does_not_accept_non_existent_key() {
      final var builder = Injector.injector().build().copy();

      assertThrows(
          IllegalArgumentException.class, () -> builder.replace(OldValue.class, NewValue.class));
    }

    @Test
    void does_not_accept_override_of_override() {
      final var builder = Injector.injector().add(OldValue.class).build().copy();

      assertNotNull(assertDoesNotThrow(() -> builder.replace(OldValue.class, NewValue.class)));
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.replace(OldValue.class, AnotherValue.class));
    }

    @Test
    void does_not_accept_override_to_existing_key() {
      final var builder =
          Injector.injector().add(OldValue.class).add(NewValue.class).build().copy();

      assertThrows(
          IllegalArgumentException.class, () -> builder.replace(OldValue.class, NewValue.class));
    }
  }
}
