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

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.function.Predicate;

/** Predicate to allow searching for a specific annotation with specific member values. */
public final class AnnotationPredicate<T extends Annotation> implements Predicate<Annotation> {
  private final Class<T> annotationClass;
  private final Predicate<T> annotationMembersPredicate;

  /**
   * Hidden constructor.
   *
   * @param annotationClass to match against
   * @param annotationMembersPredicate to match against
   */
  private AnnotationPredicate(Class<T> annotationClass, Predicate<T> annotationMembersPredicate) {
    this.annotationClass = annotationClass;
    this.annotationMembersPredicate = annotationMembersPredicate;
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("unchecked")
  public boolean test(Annotation annotation) {
    return annotation != null
        && annotationClass.equals(annotation.annotationType())
        && annotationMembersPredicate.test((T) annotation);
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AnnotationPredicate)) return false;
    AnnotationPredicate<?> that = (AnnotationPredicate<?>) o;
    return Objects.equals(annotationClass, that.annotationClass)
        && Objects.equals(annotationMembersPredicate, that.annotationMembersPredicate);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(annotationClass, annotationMembersPredicate);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Matches " + annotationClass.getName() + " annotation";
  }

  /**
   * @return fluent builder to create predicate
   */
  public static AnnotationClass annotationPredicate() {
    return new AnnotationClass();
  }

  /** Start of the fluent builder chain to construct the predicate. */
  public static final class AnnotationClass {
    /** Internal default constructor. */
    private AnnotationClass() {
      // Cannot be instantiated directly
    }

    /**
     * Sets predicate to match annotation.
     *
     * @param annotationClass to match
     * @return next step of the fluent builder
     * @throws IllegalArgumentException if class is {@code null}
     */
    public <T extends Annotation> AnnotationMembersPredicate<T> match(Class<T> annotationClass) {
      if (annotationClass == null) {
        throw new IllegalArgumentException("annotationClass cannot be null");
      }

      return new AnnotationMembersPredicate<>(annotationClass);
    }
  }

  /** End of the fluent builder chain to construct the predicate. */
  public static final class AnnotationMembersPredicate<T extends Annotation> {
    private final Class<T> annotationClass;
    private Predicate<T> membersPredicate;

    /**
     * Internal constructor.
     *
     * @param annotationClass to match against
     */
    public AnnotationMembersPredicate(Class<T> annotationClass) {
      this.annotationClass = annotationClass;
      this.membersPredicate = t -> true;
    }

    /**
     * Defines predicate to match annotation fields against expectations.
     *
     * @param membersPredicate to use for testing the annotation
     * @return this builder
     * @throws IllegalArgumentException if predicate is {@code null}
     */
    public AnnotationMembersPredicate<T> where(Predicate<T> membersPredicate) {
      if (membersPredicate == null) {
        throw new IllegalArgumentException("membersPredicate cannot be null");
      }

      this.membersPredicate = membersPredicate;
      return this;
    }

    /**
     * @return ready to be used predicate
     */
    public AnnotationPredicate<T> build() {
      return new AnnotationPredicate<>(annotationClass, membersPredicate);
    }
  }
}
