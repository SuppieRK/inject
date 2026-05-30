package io.github.suppierk.inject.providing.factories;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Key;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Named;
import java.util.Set;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class FactoryProviderOverrideTest {
  static class ParentFactory {
    @Provides
    String dependency() {
      return "parent";
    }
  }

  static class ChildFactory extends ParentFactory {
    @Override
    @Provides
    String dependency() {
      return "child";
    }
  }

  @Named("parent")
  static class ParentQualifier {}

  @Named("child")
  static class ChildQualifier {}

  @Named("staticParent")
  static class StaticParentQualifier {}

  @Named("staticChild")
  static class StaticChildQualifier {}

  static class ParentWithPrivateProvider {
    @Provides
    @Named("parent")
    private String privateDependency() {
      return "parent";
    }
  }

  static class ChildWithSamePrivateProviderSignature extends ParentWithPrivateProvider {
    @Provides
    @Named("child")
    String privateDependency() {
      return "child";
    }
  }

  static class ParentWithStaticProvider {
    @Provides
    @Named("staticParent")
    static String staticDependency() {
      return "parent";
    }
  }

  static class ChildWithSameStaticProviderSignature extends ParentWithStaticProvider {
    @Provides
    @Named("staticChild")
    static String staticDependency() {
      return "child";
    }
  }

  @Test
  void overriddenProviderMethodUsesChildImplementation() {
    final var injector =
        assertDoesNotThrow(() -> Injector.injector().add(ChildFactory.class).build());

    assertEquals("child", injector.get(String.class));
  }

  @Test
  void privateParentProviderMethodIsNotOverriddenByChildMethodWithSameSignature() {
    final var injector =
        assertDoesNotThrow(
            () -> Injector.injector().add(ChildWithSamePrivateProviderSignature.class).build());

    assertEquals(
        "parent",
        injector.get(
            new Key<>(String.class, Set.of(ParentQualifier.class.getAnnotation(Named.class)))));
    assertEquals(
        "child",
        injector.get(
            new Key<>(String.class, Set.of(ChildQualifier.class.getAnnotation(Named.class)))));
  }

  @Test
  void staticParentProviderMethodIsNotOverriddenByChildMethodWithSameSignature() {
    final var injector =
        assertDoesNotThrow(
            () -> Injector.injector().add(ChildWithSameStaticProviderSignature.class).build());

    assertEquals(
        "parent",
        injector.get(
            new Key<>(
                String.class, Set.of(StaticParentQualifier.class.getAnnotation(Named.class)))));
    assertEquals(
        "child",
        injector.get(
            new Key<>(
                String.class, Set.of(StaticChildQualifier.class.getAnnotation(Named.class)))));
  }
}
