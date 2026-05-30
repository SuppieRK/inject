package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class InjectorSelfReferenceQualifierTest {
  @Named("qualified")
  static class QualifierSource {}

  static class QualifiedInjectorConsumer {
    private final Injector injector;

    @Inject
    QualifiedInjectorConsumer(@Named("qualified") Injector injector) {
      this.injector = injector;
    }
  }

  @Test
  void qualifiedInjectorKeyDoesNotReturnUnqualifiedSelfReference() {
    final var qualifier = QualifierSource.class.getAnnotation(Named.class);
    final var injector = Injector.injector().build();
    final var qualifiedInjectorKey = new Key<>(Injector.class, Set.of(qualifier));

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(qualifiedInjectorKey),
        "Qualified Injector keys must not resolve to the unqualified self reference");
  }

  @Test
  void qualifiedInjectorInjectionFailsAsMissingBinding() {
    final var builder = Injector.injector().add(QualifiedInjectorConsumer.class);

    assertThrows(
        IllegalArgumentException.class,
        builder::build,
        "Qualified Injector injection points must not be satisfied by the unqualified self reference");
  }
}
