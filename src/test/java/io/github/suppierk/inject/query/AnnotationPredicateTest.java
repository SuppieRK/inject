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
    final var likePredicate = AnnotationPredicate.annotationPredicate().match(Named.class).build();
    assertEquals(
        "Matches jakarta.inject.Named annotation",
        likePredicate.toString(),
        "toString() must return expected value");

    final var likePredicateWithMemberPredicate =
        AnnotationPredicate.annotationPredicate()
            .match(Named.class)
            .where(named -> named.value().equals("test"))
            .build();
    assertEquals(
        "Matches jakarta.inject.Named annotation",
        likePredicateWithMemberPredicate.toString(),
        "toString() must return expected value");
  }

  @Nested
  @CustomQualifier
  @Named(value = "predicateTest")
  class Predicate {
    final Named namedAnnotation = getClass().getAnnotation(Named.class);
    final CustomQualifier customAnnotation = getClass().getAnnotation(CustomQualifier.class);

    @Test
    void returnsFalseForNullWrapper() {
      final var predicate = AnnotationPredicate.annotationPredicate().match(Named.class).build();
      assertFalse(predicate.test(null), "Null argument must return false");
    }

    @Test
    void returnsFalseForDifferentClass() {
      final var predicate = AnnotationPredicate.annotationPredicate().match(Named.class).build();
      assertFalse(predicate.test(customAnnotation), "Class mismatch must return false");
    }

    @Test
    void returnsTrueIfMemberValuesMatch() {
      final var predicate =
          AnnotationPredicate.annotationPredicate()
              .match(Named.class)
              .where(named -> named.value().equals("predicateTest"))
              .build();
      assertTrue(
          predicate.test(namedAnnotation),
          "Correct member values during matching must return true");
    }

    @Test
    void returnsFalseIfMemberValuesDoNotMatch() {
      final var predicate =
          AnnotationPredicate.annotationPredicate()
              .match(Named.class)
              .where(named -> named.value().equals("randomValue"))
              .build();
      assertFalse(
          predicate.test(namedAnnotation),
          "Incorrect member values during matching must return false");
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
          () -> builder.match(null),
          "Builder must not accept null class");
    }

    @Test
    void doesNotAcceptNullMemberValues() {
      final var builder = AnnotationPredicate.annotationPredicate().match(Named.class);

      assertThrows(
          IllegalArgumentException.class,
          () -> builder.where(null),
          "Builder must not accept null predicate");
    }

    @Test
    void returnsNonNullPredicate() {
      final var builder = AnnotationPredicate.annotationPredicate().match(Named.class);

      assertNotNull(builder.build(), "Predicate must not be null");
    }
  }
}
