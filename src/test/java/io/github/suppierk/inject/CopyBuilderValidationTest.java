package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertThrows;

import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class CopyBuilderValidationTest {
  static class Original {}

  static class CyclicReplacement extends Original {
    @Inject
    CyclicReplacement(Original original) {}
  }

  abstract static class AbstractReplacement extends Original {
    AbstractReplacement() {}
  }

  static class ParentToken {}

  static class ChildToken extends ParentToken {}

  static class UnrelatedToken {}

  @Test
  void replacementCycleFailsDuringBuild() {
    final var builder =
        Injector.injector()
            .add(Original.class)
            .build()
            .copy()
            .replace(Original.class, CyclicReplacement.class);

    assertThrows(
        IllegalArgumentException.class,
        builder::build,
        "Replacement cycle must fail during build instead of causing recursive resolution");
  }

  @Test
  void abstractReplacementClassFailsDuringReplacement() {
    final var builder = Injector.injector().add(Original.class).build().copy();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.replace(Original.class, AbstractReplacement.class),
        "Abstract replacement classes must be rejected consistently with regular registration");
  }

  @Test
  void replacingObjectWithUnassignableInstanceFails() {
    final var registered = new ParentToken();
    final var replacement = new UnrelatedToken();
    final var builder = Injector.injector().add(registered).build().copy();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.replace(registered, replacement),
        "Object replacement must verify that the replacement type is assignable to the original type");
  }

  @Test
  void replacingDifferentObjectInstanceOfSameOriginalClassFails() {
    final var registered = new ParentToken();
    final var notRegistered = new ParentToken();
    final var replacement = new ChildToken();
    final var builder = Injector.injector().add(registered).build().copy();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.replace(notRegistered, replacement),
        "Object replacement must verify that the original instance is registered");
  }
}
