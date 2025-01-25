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
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Predicate to allow searching for a specific annotation with specific member values.
 *
 * <p>Member value consists of annotation method name and its value.
 */
public final class AnnotationPredicate implements Predicate<AnnotationWrapper> {
  private final boolean exactMatch;
  private final Class<? extends Annotation> annotationClass;
  private final Map<String, Object> memberValues;

  /**
   * Hidden constructor.
   *
   * @param exactMatch defines whether all member values must be present or only some of them
   * @param annotationClass to match against
   * @param memberValues to match against
   */
  private AnnotationPredicate(
      boolean exactMatch,
      Class<? extends Annotation> annotationClass,
      Map<String, Object> memberValues) {
    this.exactMatch = exactMatch;
    this.annotationClass = annotationClass;
    this.memberValues = Map.copyOf(memberValues);
  }

  /** {@inheritDoc} */
  @Override
  public boolean test(AnnotationWrapper annotationWrapper) {
    if (annotationWrapper == null) {
      return false;
    }

    if (!annotationClass.equals(annotationWrapper.annotation().annotationType())) {
      return false;
    }

    if (exactMatch) {
      return memberValues.equals(annotationWrapper.memberValues());
    }

    for (Map.Entry<String, Object> memberValueEntry : memberValues.entrySet()) {
      if (!memberValueEntry
          .getValue()
          .equals(annotationWrapper.memberValues().get(memberValueEntry.getKey()))) {
        return false;
      }
    }

    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof AnnotationPredicate)) return false;
    AnnotationPredicate that = (AnnotationPredicate) o;
    return exactMatch == that.exactMatch
        && Objects.equals(annotationClass, that.annotationClass)
        && Objects.equals(memberValues, that.memberValues);
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    return Objects.hash(exactMatch, annotationClass, memberValues);
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    if (exactMatch) {
      sb.append("Match exactly @").append(annotationClass.getSimpleName());
    } else {
      sb.append("Match similar to @").append(annotationClass.getSimpleName());
    }

    if (memberValues.isEmpty()) {
      return sb.toString();
    }

    return sb.append(memberValues).toString();
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
     * Sets predicate to match all member values.
     *
     * @param annotationClass to match
     * @return next step of the fluent builder
     * @throws IllegalArgumentException if class is {@code null}
     */
    public AnnotationMembers exactly(Class<? extends Annotation> annotationClass) {
      if (annotationClass == null) {
        throw new IllegalArgumentException("annotationClass cannot be null");
      }

      return new AnnotationMembers(true, annotationClass);
    }

    /**
     * Sets predicate to match only defined member values.
     *
     * @param annotationClass to match
     * @return next step of the fluent builder
     * @throws IllegalArgumentException if class is {@code null}
     */
    public AnnotationMembers like(Class<? extends Annotation> annotationClass) {
      if (annotationClass == null) {
        throw new IllegalArgumentException("annotationClass cannot be null");
      }

      return new AnnotationMembers(false, annotationClass);
    }
  }

  /** End of the fluent builder chain to construct the predicate. */
  public static final class AnnotationMembers {
    private final boolean exactMatch;
    private final Class<? extends Annotation> annotationClass;
    private final Map<String, Object> memberValues;

    /**
     * Internal constructor.
     *
     * @param exactMatch defines whether all member values must be present or only some of them
     * @param annotationClass to match against
     */
    private AnnotationMembers(boolean exactMatch, Class<? extends Annotation> annotationClass) {
      this.exactMatch = exactMatch;
      this.annotationClass = annotationClass;
      this.memberValues = new ConcurrentHashMap<>();
    }

    /**
     * Defines annotation method name with expected value to match.
     *
     * @param annotationMethodName to match
     * @param annotationMethodValue to match
     * @return this builder
     * @throws IllegalArgumentException if any of the arguments is {@code null}
     */
    public AnnotationMembers with(String annotationMethodName, Object annotationMethodValue) {
      if (annotationMethodName == null || annotationMethodName.isBlank()) {
        throw new IllegalArgumentException("annotationMethodName cannot be null or blank");
      }

      if (annotationMethodValue == null) {
        throw new IllegalArgumentException("annotationMethodValue cannot be null");
      }

      memberValues.put(annotationMethodName, annotationMethodValue);
      return this;
    }

    /**
     * @return ready to be used predicate
     */
    public AnnotationPredicate build() {
      return new AnnotationPredicate(exactMatch, annotationClass, memberValues);
    }
  }
}
