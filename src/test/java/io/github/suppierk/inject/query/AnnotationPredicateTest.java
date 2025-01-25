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

import io.github.suppierk.inject.AnnotationWrapper;
import io.github.suppierk.mocks.CustomQualifier;
import jakarta.inject.Named;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AnnotationPredicateTest {
  @Test
  void objectMethodsMustWorkAsExpected() {
    EqualsVerifier.forClass(AnnotationPredicate.class).verify();
  }

  @Test
  void toStringMustWorkAsExpected() {
    final var likePredicate = AnnotationPredicate.annotationPredicate().like(Named.class).build();
    assertEquals(
        "Match similar to @Named",
        likePredicate.toString(),
        "toString() must return expected value");

    final var exactPredicate =
        AnnotationPredicate.annotationPredicate().exactly(Named.class).build();
    assertEquals(
        "Match exactly @Named", exactPredicate.toString(), "toString() must return expected value");

    final var likePredicateWithMemberValues =
        AnnotationPredicate.annotationPredicate().like(Named.class).with("value", "test").build();
    assertEquals(
        "Match similar to @Named{value=test}",
        likePredicateWithMemberValues.toString(),
        "toString() must return expected value");
  }

  @Nested
  @CustomQualifier
  @Named(value = "predicateTest")
  class Predicate {
    final AnnotationWrapper namedAnnotationWrapper =
        new AnnotationWrapper(getClass().getAnnotation(Named.class));
    final AnnotationWrapper customAnnotationWrapper =
        new AnnotationWrapper(getClass().getAnnotation(CustomQualifier.class));

    @Test
    void returnsFalseForNullWrapper() {
      final var predicate = AnnotationPredicate.annotationPredicate().like(Named.class).build();
      assertFalse(predicate.test(null), "Null argument must return false");
    }

    @Test
    void returnsFalseForDifferentClass() {
      final var predicate = AnnotationPredicate.annotationPredicate().like(Named.class).build();
      assertFalse(predicate.test(customAnnotationWrapper), "Class mismatch must return false");
    }

    @Test
    void returnsFalseIfMemberValuesDoNotMatchExactly() {
      final var predicate = AnnotationPredicate.annotationPredicate().exactly(Named.class).build();
      assertFalse(
          predicate.test(namedAnnotationWrapper),
          "Missing member values during exact matching must return false");
    }

    @Test
    void returnsTrueIfMemberValuesMatchExactly() {
      final var predicate =
          AnnotationPredicate.annotationPredicate()
              .exactly(Named.class)
              .with("value", "predicateTest")
              .build();
      assertTrue(
          predicate.test(namedAnnotationWrapper),
          "Correct member values during exact matching must return true");
    }

    @Test
    void returnsTrueIfMemberValuesDoNotMatchForLikePredicate() {
      final var predicate = AnnotationPredicate.annotationPredicate().like(Named.class).build();
      assertTrue(
          predicate.test(namedAnnotationWrapper),
          "Missing member values during similar matching must return true");
    }

    @Test
    void returnsTrueIfMemberValuesMatchForLikePredicate() {
      final var predicate =
          AnnotationPredicate.annotationPredicate()
              .like(Named.class)
              .with("value", "predicateTest")
              .build();
      assertTrue(
          predicate.test(namedAnnotationWrapper),
          "Correct member values during similar matching must return true");
    }

    @Test
    void returnsFalseIfMemberValuesDoNotMatchForLikePredicate() {
      final var predicate =
          AnnotationPredicate.annotationPredicate().like(Named.class).with("value", 1).build();
      assertFalse(
          predicate.test(namedAnnotationWrapper),
          "Unexpected member values during similar matching must return false");
    }

    @Test
    void returnsFalseIfMemberValueIsMissing() {
      final var predicate =
          AnnotationPredicate.annotationPredicate()
              .like(Named.class)
              .with("other", "predicateTest")
              .build();
      assertFalse(
          predicate.test(namedAnnotationWrapper),
          "Incorrect member values during similar matching must return false");
    }

    @Test
    void returnsFalseIfMemberValueIsMissingInCustom() {
      final var predicate =
          AnnotationPredicate.annotationPredicate()
              .like(CustomQualifier.class)
              .with("other", "predicateTest")
              .build();
      assertFalse(
          predicate.test(customAnnotationWrapper),
          "Unexpected member values during similar matching must return false");
    }
  }

  @Nested
  class Builder {
    @Test
    void returnsNonNullBuilder() {
      assertNotNull(AnnotationPredicate.annotationPredicate(), "Builder must not be null");
    }

    @Test
    void doesNotAcceptNullClass() {
      final var builder = AnnotationPredicate.annotationPredicate();
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.exactly(null),
          "Builder must not accept null class");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.like(null),
          "Builder must not accept null class");
    }

    @Test
    void doesNotAcceptNullMemberValues() {
      final var builder = AnnotationPredicate.annotationPredicate().exactly(Named.class);

      assertThrows(
          IllegalArgumentException.class,
          () -> builder.with(null, "value"),
          "Builder must not accept null annotation method name");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.with("   ", "value"),
          "Builder must not accept blank annotation method name");
      assertThrows(
          IllegalArgumentException.class,
          () -> builder.with("name", null),
          "Builder must not accept null annotation method value");
    }

    @Test
    void returnsNonNullPredicate() {
      final var builder = AnnotationPredicate.annotationPredicate().like(Named.class);

      assertNotNull(builder.build(), "Predicate must not be null");
    }
  }
}
