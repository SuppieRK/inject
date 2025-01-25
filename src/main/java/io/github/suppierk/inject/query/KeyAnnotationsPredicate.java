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

import io.github.suppierk.inject.AnnotationWrapper;
import io.github.suppierk.inject.Key;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

public final class KeyAnnotationsPredicate implements Predicate<Key<?>> {
  private static final byte NONE_MATCH = 0;
  private static final byte ANY_MATCH = 1;
  private static final byte ALL_MATCH = 2;

  private final byte mode;
  private final Set<AnnotationPredicate> annotationQueries;

  private KeyAnnotationsPredicate(byte mode, Set<AnnotationPredicate> annotationQueries) {
    this.mode = mode;
    this.annotationQueries = Set.copyOf(annotationQueries);
  }

  @Override
  public boolean test(Key<?> key) {
    if (key == null) {
      throw new IllegalArgumentException("Key must not be null");
    }

    if (annotationQueries.isEmpty()) {
      return true;
    }

    final Predicate<AnnotationWrapper> wrapperPredicate =
        annotationWrapper ->
            annotationQueries.stream().anyMatch(query -> query.test(annotationWrapper));

    if (NONE_MATCH == mode) {
      return key.annotationWrappers().stream().noneMatch(wrapperPredicate);
    } else if (ANY_MATCH == mode) {
      return key.annotationWrappers().stream().anyMatch(wrapperPredicate);
    } else {
      return !key.annotationWrappers().isEmpty()
          && key.annotationWrappers().stream().allMatch(wrapperPredicate);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof KeyAnnotationsPredicate)) return false;
    KeyAnnotationsPredicate that = (KeyAnnotationsPredicate) o;
    return mode == that.mode && Objects.equals(annotationQueries, that.annotationQueries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mode, annotationQueries);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();

    if (NONE_MATCH == mode) {
      result.append("None match [");
    } else if (ANY_MATCH == mode) {
      result.append("Any match [");
    } else {
      result.append("All match [");
    }

    boolean firstMember = true;
    for (AnnotationPredicate annotationQuery : annotationQueries) {
      if (firstMember) {
        firstMember = false;
      } else {
        result.append(", ");
      }

      result.append(annotationQuery.toString());
    }

    return result.append("]").toString();
  }

  public static KeyAnnotationsPredicate alwaysMatch() {
    return new KeyAnnotationsPredicate(ANY_MATCH, Set.of());
  }

  public static PredicateMode keyAnnotationPredicate() {
    return new PredicateMode();
  }

  public static final class PredicateMode {
    private PredicateMode() {
      // Cannot be instantiated directly
    }

    public AnnotationPredicates noneMatch() {
      return new AnnotationPredicates(NONE_MATCH);
    }

    public AnnotationPredicates anyMatch() {
      return new AnnotationPredicates(ANY_MATCH);
    }

    public AnnotationPredicates allMatch() {
      return new AnnotationPredicates(ALL_MATCH);
    }
  }

  public static final class AnnotationPredicates {
    private final byte mode;
    private final Set<AnnotationPredicate> annotationQueries;

    private AnnotationPredicates(final byte mode) {
      this.mode = mode;
      this.annotationQueries = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    public AnnotationPredicates having(
        Function<AnnotationPredicate.AnnotationClass, AnnotationPredicate.AnnotationMembers>
            annotationQueryModifier) {
      if (annotationQueryModifier == null) {
        throw new IllegalArgumentException("annotationQueryModifier cannot be null");
      }

      annotationQueries.add(
          annotationQueryModifier.apply(AnnotationPredicate.annotationPredicate()).build());
      return this;
    }

    public AnnotationPredicates having(AnnotationPredicate annotationPredicate) {
      if (annotationPredicate == null) {
        throw new IllegalArgumentException("annotationPredicate cannot be null");
      }

      annotationQueries.add(annotationPredicate);
      return this;
    }

    public KeyAnnotationsPredicate build() {
      return new KeyAnnotationsPredicate(mode, annotationQueries);
    }
  }
}
