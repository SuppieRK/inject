/*
 * MIT License
 *
 * Copyright 2026 Roman Khlebnov
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

package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class CaptiveDependencyValidationTest {
  static class NonSingletonDependency {}

  @Singleton
  static class SingletonDependency {}

  static class ObjectDependency {}

  @Singleton
  static class ServiceDependency {}

  static class NonSingletonServiceDependency extends ServiceDependency {}

  @Singleton
  static class SingletonWithConstructorCaptiveDependency {
    @Inject
    SingletonWithConstructorCaptiveDependency(NonSingletonDependency dependency) {}
  }

  @Singleton
  static class SingletonWithFieldCaptiveDependency {
    @Inject NonSingletonDependency dependency;
  }

  @Singleton
  static class SingletonWithSingletonDependency {
    @Inject
    SingletonWithSingletonDependency(SingletonDependency dependency) {}
  }

  @Singleton
  static class SingletonWithObjectDependency {
    @Inject
    SingletonWithObjectDependency(ObjectDependency dependency) {}
  }

  @Singleton
  static class SingletonWithProviderDependency {
    @Inject
    SingletonWithProviderDependency(Provider<NonSingletonDependency> dependency) {}
  }

  @Singleton
  static class SingletonWithSupplierDependency {
    @Inject
    SingletonWithSupplierDependency(Supplier<NonSingletonDependency> dependency) {}
  }

  static class NonSingletonWithNonSingletonDependency {
    @Inject
    NonSingletonWithNonSingletonDependency(NonSingletonDependency dependency) {}
  }

  static class NonSingletonWithSingletonDependency {
    @Inject
    NonSingletonWithSingletonDependency(SingletonDependency dependency) {}
  }

  static class SingletonProviderMethodFactory {
    @Provides
    @Singleton
    SingletonProduct singletonProduct(NonSingletonDependency dependency) {
      return new SingletonProduct();
    }
  }

  static class SingletonReturnTypeProviderMethodFactory {
    @Provides
    SingletonReturnType singletonReturnType(NonSingletonDependency dependency) {
      return new SingletonReturnType();
    }
  }

  static class SingletonProduct {}

  @Singleton
  static class SingletonReturnType {}

  static class NonSingletonProduct {}

  @Singleton
  static class SingletonWithReplaceableDependency {
    @Inject
    SingletonWithReplaceableDependency(ServiceDependency dependency) {}
  }

  @Test
  void singletonMustNotDependDirectlyOnNonSingletonConstructorDependency() {
    final var builder =
        Injector.injector()
            .add(SingletonWithConstructorCaptiveDependency.class, NonSingletonDependency.class);

    final var exception =
        assertThrows(
            IllegalArgumentException.class,
            builder::build,
            "Singleton constructor dependency on non-singleton must be rejected");

    assertCaptiveDependencyMessage(exception);
  }

  @Test
  void singletonMustNotDependDirectlyOnNonSingletonFieldDependency() {
    final var builder =
        Injector.injector()
            .add(SingletonWithFieldCaptiveDependency.class, NonSingletonDependency.class);

    final var exception =
        assertThrows(
            IllegalArgumentException.class,
            builder::build,
            "Singleton field dependency on non-singleton must be rejected");

    assertCaptiveDependencyMessage(exception);
  }

  @Test
  void singletonMayDependDirectlyOnSingletonDependency() {
    assertDoesNotThrow(
        () ->
            Injector.injector()
                .add(SingletonWithSingletonDependency.class, SingletonDependency.class)
                .build(),
        "Singleton dependency on singleton must be allowed");
  }

  @Test
  void singletonMayDependDirectlyOnObjectDependency() {
    assertDoesNotThrow(
        () ->
            Injector.injector()
                .add(SingletonWithObjectDependency.class)
                .add(new ObjectDependency())
                .build(),
        "Singleton dependency on registered object must be allowed");
  }

  @Test
  void singletonMayDependOnNonSingletonProvider() {
    assertDoesNotThrow(
        () ->
            Injector.injector()
                .add(SingletonWithProviderDependency.class, NonSingletonDependency.class)
                .build(),
        "Singleton Provider dependency on non-singleton must be allowed");
  }

  @Test
  void singletonMayDependOnNonSingletonSupplier() {
    assertDoesNotThrow(
        () ->
            Injector.injector()
                .add(SingletonWithSupplierDependency.class, NonSingletonDependency.class)
                .build(),
        "Singleton Supplier dependency on non-singleton must be allowed");
  }

  @Test
  void nonSingletonMayDependDirectlyOnNonSingletonDependency() {
    assertDoesNotThrow(
        () ->
            Injector.injector()
                .add(NonSingletonWithNonSingletonDependency.class, NonSingletonDependency.class)
                .build(),
        "Non-singleton dependency on non-singleton must be allowed");
  }

  @Test
  void nonSingletonMayDependDirectlyOnSingletonDependency() {
    assertDoesNotThrow(
        () ->
            Injector.injector()
                .add(NonSingletonWithSingletonDependency.class, SingletonDependency.class)
                .build(),
        "Non-singleton dependency on singleton must be allowed");
  }

  @Test
  void singletonProviderMethodMustNotDependDirectlyOnNonSingletonDependency() {
    final var builder =
        Injector.injector().add(SingletonProviderMethodFactory.class, NonSingletonDependency.class);

    final var exception =
        assertThrows(
            IllegalArgumentException.class,
            builder::build,
            "Singleton provider method dependency on non-singleton must be rejected");

    assertCaptiveDependencyMessage(exception);
  }

  @Test
  void singletonReturnTypeProviderMethodMustNotDependDirectlyOnNonSingletonDependency() {
    final var builder =
        Injector.injector()
            .add(SingletonReturnTypeProviderMethodFactory.class, NonSingletonDependency.class);

    final var exception =
        assertThrows(
            IllegalArgumentException.class,
            builder::build,
            "Provider method returning @Singleton type and depending on non-singleton must be rejected");

    assertCaptiveDependencyMessage(exception);
  }

  @Test
  void nonSingletonProviderMethodMayDependDirectlyOnNonSingletonDependency() {
    assertDoesNotThrow(
        () ->
            Injector.injector()
                .add(NonSingletonFactory.class, NonSingletonDependency.class)
                .build(),
        "Non-singleton provider method dependency on non-singleton must be allowed");
  }

  @Test
  void replacementMustUseReplacementScopeForCaptiveDependencyValidation() {
    final var original =
        Injector.injector()
            .add(SingletonWithReplaceableDependency.class, ServiceDependency.class)
            .build();

    final var copyBuilder =
        original.copy().replace(ServiceDependency.class, NonSingletonServiceDependency.class);

    final var exception =
        assertThrows(
            IllegalArgumentException.class,
            copyBuilder::build,
            "Replacement scope must be used for captive dependency validation");

    assertCaptiveDependencyMessage(exception);
  }

  static class NonSingletonFactory {
    @Provides
    NonSingletonProduct nonSingletonProduct(NonSingletonDependency dependency) {
      return new NonSingletonProduct();
    }
  }

  private static void assertCaptiveDependencyMessage(IllegalArgumentException exception) {
    final var message = exception.getMessage();

    assertTrue(message.contains("Captive dependency"), "Message must identify captive dependency");
    assertTrue(message.contains("@Singleton"), "Message must mention singleton scope");
    assertTrue(message.contains("Provider"), "Message must suggest Provider as an alternative");
    assertTrue(message.contains("Supplier"), "Message must suggest Supplier as an alternative");
  }
}
