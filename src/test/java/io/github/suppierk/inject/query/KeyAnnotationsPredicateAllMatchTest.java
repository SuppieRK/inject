package io.github.suppierk.inject.query;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.Key;
import io.github.suppierk.mocks.CustomQualifier;
import jakarta.inject.Named;
import java.util.Set;
import org.junit.jupiter.api.Test;

@Named("name")
@CustomQualifier
class KeyAnnotationsPredicateAllMatchTest {
  @Test
  void allMatchRequiresEveryQueryToMatchAtLeastOneAnnotation() {
    final var customOnlyKey =
        new Key<>(String.class, Set.of(getClass().getAnnotation(CustomQualifier.class)));
    final var bothAnnotationsKey =
        new Key<>(
            String.class,
            Set.of(
                getClass().getAnnotation(CustomQualifier.class),
                getClass().getAnnotation(Named.class)));
    final var predicate =
        KeyAnnotationsPredicate.keyAnnotationPredicate()
            .allMatch()
            .having(annotation -> annotation.match(CustomQualifier.class))
            .having(annotation -> annotation.match(Named.class))
            .build();

    assertFalse(
        predicate.test(customOnlyKey),
        "allMatch must fail when at least one annotation query has no match");
    assertTrue(
        predicate.test(bothAnnotationsKey),
        "allMatch must pass when every annotation query has a match");
  }
}
