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

import io.github.suppierk.inject.Key;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Predicate to allow searching for a specific key in {@link io.github.suppierk.inject.Injector}.
 */
public final class KeyAnnotationsPredicate implements Predicate<Key<?>> {
  private static final byte NONE_MATCH = 0;
  private static final byte ANY_MATCH = 1;
  private static final byte ALL_MATCH = 2;

  private final byte mode;
  private final Set<Predicate<Annotation>> annotationQueries;

  /**
   * Hidden constructor.
   *
   * @param mode to use during comparison
   * @param annotationQueries to search annotations with
   */
  private KeyAnnotationsPredicate(byte mode, Set<Predicate<Annotation>> annotationQueries) {
    this.mode = mode;
    this.annotationQueries = Set.copyOf(annotationQueries);
  }

  /** {@inheritDoc} */
  @Override
  public boolean test(Key<?> key) {
    if (key == null) {
      throw new IllegalArgumentException("Key must not be null");
    }

    if (annotationQueries.isEmpty()) {
      return true;
    }

    final Predicate<Annotation> wrapperPredicate =
        annotation -> annotationQueries.stream().anyMatch(query -> query.test(annotation));

    if (NONE_MATCH == mode) {
      return key.annotations().stream().noneMatch(wrapperPredicate);
    } else if (ANY_MATCH == mode) {
      return key.annotations().stream().anyMatch(wrapperPredicate);
    } else {
      return !key.annotations().isEmpty() && key.annotations().stream().allMatch(wrapperPredicate);
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof KeyAnnotationsPredicate)) return false;
    KeyAnnotationsPredicate that = (KeyAnnotationsPredicate) o;
    return mode == that.mode && Objects.equals(annotationQueries, that.annotationQueries);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(mode, annotationQueries);
  }

  /** {@inheritDoc} */
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
    for (Predicate<Annotation> annotationQuery : annotationQueries) {
      if (firstMember) {
        firstMember = false;
      } else {
        result.append(", ");
      }

      result.append(annotationQuery.toString());
    }

    return result.append("]").toString();
  }

  /**
   * @return predicate that always returns {@code true}
   */
  public static KeyAnnotationsPredicate alwaysMatch() {
    return new KeyAnnotationsPredicate(ANY_MATCH, Set.of());
  }

  /**
   * @return fluent builder to create predicate
   */
  public static PredicateMode keyAnnotationPredicate() {
    return new PredicateMode();
  }

  /** Start of the fluent builder chain to construct the predicate. */
  public static final class PredicateMode {
    /** Default constructor. */
    private PredicateMode() {
      // Cannot be instantiated directly
    }

    /**
     * Start creating a predicate which properties must not match to the parameters specified
     *
     * @return next step of the fluent builder
     */
    public AnnotationPredicates noneMatch() {
      return new AnnotationPredicates(NONE_MATCH);
    }

    /**
     * Start creating a predicate which properties must partially match to the parameters specified
     *
     * @return next step of the fluent builder
     */
    public AnnotationPredicates anyMatch() {
      return new AnnotationPredicates(ANY_MATCH);
    }

    /**
     * Start creating a predicate which properties must match to the parameters specified
     *
     * @return next step of the fluent builder
     */
    public AnnotationPredicates allMatch() {
      return new AnnotationPredicates(ALL_MATCH);
    }
  }

  /** End of the fluent builder chain to construct the predicate. */
  public static final class AnnotationPredicates {
    private final byte mode;
    private final Set<Predicate<Annotation>> annotationQueries;

    /**
     * Default constructor.
     *
     * @param mode of the predicate
     */
    private AnnotationPredicates(final byte mode) {
      this.mode = mode;
      this.annotationQueries = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }

    /**
     * Add annotation predicate via fluent builder style.
     *
     * @param annotationQueryModifier to add
     * @return current builder
     */
    public AnnotationPredicates having(
        Function<
                AnnotationPredicate.AnnotationClass,
                AnnotationPredicate.AnnotationMembersPredicate<?>>
            annotationQueryModifier) {
      if (annotationQueryModifier == null) {
        throw new IllegalArgumentException("annotationQueryModifier cannot be null");
      }

      annotationQueries.add(
          annotationQueryModifier.apply(AnnotationPredicate.annotationPredicate()).build());
      return this;
    }

    /**
     * Add annotation predicate from a variable
     *
     * @param annotationPredicate to add
     * @return current builder
     */
    public AnnotationPredicates having(AnnotationPredicate<?> annotationPredicate) {
      if (annotationPredicate == null) {
        throw new IllegalArgumentException("annotationPredicate cannot be null");
      }

      annotationQueries.add(annotationPredicate);
      return this;
    }

    /**
     * @return ready to be used predicate
     */
    public KeyAnnotationsPredicate build() {
      return new KeyAnnotationsPredicate(mode, annotationQueries);
    }
  }
}
