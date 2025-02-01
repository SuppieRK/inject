/*
 * MIT License
 *
 * Copyright 2025 Roman Khlebnov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the “Software”), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.suppierk.inject.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Key;
import io.github.suppierk.mocks.CustomQualifier;
import jakarta.inject.Named;
import java.util.Set;
import java.util.function.Function;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class KeyAnnotationsPredicateTest {
  @Test
  void objectMethodsMustWorkAsExpected() {
    EqualsVerifier.forClass(KeyAnnotationsPredicate.class).verify();
  }

  @Test
  void toStringMustWorkAsExpected() {
    final var customPredicate =
        AnnotationPredicate.annotationPredicate().match(CustomQualifier.class).build();
    final var namedPredicate =
        AnnotationPredicate.annotationPredicate()
            .match(Named.class)
            .where(named -> named.value().equals("predicateTest"))
            .build();

    final var allMatchPredicate =
        KeyAnnotationsPredicate.keyAnnotationPredicate().allMatch().having(customPredicate).build();

    assertEquals(
        "All match [Matches io.github.suppierk.mocks.CustomQualifier annotation]",
        allMatchPredicate.toString(),
        "toString() must return expected value");

    final var anyMatchPredicate =
        KeyAnnotationsPredicate.keyAnnotationPredicate().anyMatch().having(customPredicate).build();

    assertEquals(
        "Any match [Matches io.github.suppierk.mocks.CustomQualifier annotation]",
        anyMatchPredicate.toString(),
        "toString() must return expected value");

    final var noneMatchPredicate =
        KeyAnnotationsPredicate.keyAnnotationPredicate()
            .noneMatch()
            .having(customPredicate)
            .build();

    assertEquals(
        "None match [Matches io.github.suppierk.mocks.CustomQualifier annotation]",
        noneMatchPredicate.toString(),
        "toString() must return expected value");

    final var multipleMatchPredicate =
        KeyAnnotationsPredicate.keyAnnotationPredicate()
            .allMatch()
            .having(customPredicate)
            .having(namedPredicate)
            .build();

    assertTrue(
        Set.of(
                "All match [Matches io.github.suppierk.mocks.CustomQualifier annotation, Matches jakarta.inject.Named annotation]",
                "All match [Matches jakarta.inject.Named annotation, Matches io.github.suppierk.mocks.CustomQualifier annotation]")
            .contains(multipleMatchPredicate.toString()),
        "toString() must return expected value");
  }

  @Nested
  @CustomQualifier
  @Named(value = "predicateTest")
  class Predicate {
    final Key<String> zeroAnnotations = new Key<>(String.class, Set.of());

    final Key<String> namedAnnotation =
        new Key<>(String.class, Set.of(getClass().getAnnotation(Named.class)));

    final Key<String> customAnnotation =
        new Key<>(String.class, Set.of(getClass().getAnnotation(CustomQualifier.class)));

    final Key<String> twoAnnotations =
        new Key<>(
            String.class,
            Set.of(
                getClass().getAnnotation(Named.class),
                getClass().getAnnotation(CustomQualifier.class)));

    @Test
    void throwsExceptionForNullArgument() {
      final var predicate =
          KeyAnnotationsPredicate.keyAnnotationPredicate()
              .allMatch()
              .having(annotation -> annotation.match(CustomQualifier.class))
              .build();
      assertThrows(IllegalArgumentException.class, () -> predicate.test(null));
    }

    @Test
    void returnsTrueIfNoAnnotationQueriesPresent() {
      final var predicate = KeyAnnotationsPredicate.keyAnnotationPredicate().allMatch().build();

      assertTrue(predicate.test(twoAnnotations), "Must return true regardless of annotations");
      assertTrue(predicate.test(namedAnnotation), "Must return true regardless of annotations");
      assertTrue(predicate.test(customAnnotation), "Must return true regardless of annotations");
      assertTrue(predicate.test(zeroAnnotations), "Must return true regardless of annotations");
    }

    @Test
    void noneMatchTest() {
      final var predicate =
          KeyAnnotationsPredicate.keyAnnotationPredicate()
              .noneMatch()
              .having(annotation -> annotation.match(CustomQualifier.class))
              .build();

      assertFalse(
          predicate.test(twoAnnotations),
          "Must return false when more than custom annotation is present");
      assertTrue(
          predicate.test(namedAnnotation), "Must return true when no custom annotation is present");
      assertFalse(
          predicate.test(customAnnotation),
          "Must return false when only custom annotation is present");
      assertTrue(
          predicate.test(zeroAnnotations), "Must return true when no annotations are present");
    }

    @Test
    void anyMatchTest() {
      final var predicate =
          KeyAnnotationsPredicate.keyAnnotationPredicate()
              .anyMatch()
              .having(annotation -> annotation.match(CustomQualifier.class))
              .build();

      assertTrue(
          predicate.test(twoAnnotations),
          "Must return true when more than custom annotation is present");
      assertFalse(
          predicate.test(namedAnnotation),
          "Must return false when no custom annotation is present");
      assertTrue(
          predicate.test(customAnnotation),
          "Must return true when only custom annotation is present");
      assertFalse(
          predicate.test(zeroAnnotations), "Must return false when no annotations are present");
    }

    @Test
    void allMatchTest() {
      final var predicate =
          KeyAnnotationsPredicate.keyAnnotationPredicate()
              .allMatch()
              .having(annotation -> annotation.match(CustomQualifier.class))
              .build();

      assertFalse(
          predicate.test(twoAnnotations),
          "Must return false when more than custom annotation is present");
      assertFalse(
          predicate.test(namedAnnotation),
          "Must return false when no custom annotation is present");
      assertTrue(
          predicate.test(customAnnotation),
          "Must return true when only custom annotation is present");
      assertFalse(
          predicate.test(zeroAnnotations), "Must return false when no annotations are present");
    }
  }

  @Nested
  class Builder {
    @Test
    void returnsNonNullAlwaysMatchPredicate() {
      assertNotNull(
          KeyAnnotationsPredicate.alwaysMatch(), "Always match predicate must not be null");
    }

    @Test
    void returnsNonNullBuilder() {
      assertNotNull(KeyAnnotationsPredicate.keyAnnotationPredicate(), "Builder must not be null");
    }

    @Test
    void returnsNonNullSecondBuilderStage() {
      final var builder = KeyAnnotationsPredicate.keyAnnotationPredicate();

      assertNotNull(builder.allMatch(), "Second builder stage after allMatch must not be null");
      assertNotNull(builder.anyMatch(), "Second builder stage after anyMatch must not be null");
      assertNotNull(builder.noneMatch(), "Second builder stage after noneMatch must not be null");
    }

    @Test
    void havingDoesNotAcceptNulls() {
      final var builder = KeyAnnotationsPredicate.keyAnnotationPredicate().allMatch();

      assertThrows(
          IllegalArgumentException.class,
          () ->
              builder.having(
                  (Function<
                          AnnotationPredicate.AnnotationClass,
                          AnnotationPredicate.AnnotationMembersPredicate<?>>)
                      null));
      assertThrows(
          IllegalArgumentException.class, () -> builder.having((AnnotationPredicate<?>) null));
    }
  }
}
