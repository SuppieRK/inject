package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class BuilderFailureAtomicityTest {
  static class InvalidParameterizedProviderFactory {
    @Provides
    List<String> dependency() {
      return List.of("value");
    }
  }

  static class DuplicateProviderFactory {
    @Provides
    Long first() {
      return 1L;
    }

    @Provides
    Long second() {
      return 2L;
    }
  }

  static class ValidDependency {}

  static class CycleFirst {}

  static class CycleSecond {}

  static class ProviderCycleFactory {
    @Provides
    CycleFirst first(CycleSecond second) {
      return new CycleFirst();
    }

    @Provides
    CycleSecond second(CycleFirst first) {
      return new CycleSecond();
    }
  }

  @Test
  void failedAddDoesNotKeepClassNodeFromParameterizedProviderFailure() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(InvalidParameterizedProviderFactory.class),
        "Invalid @Provides method must fail registration");

    final var injector = assertDoesNotThrow(builder::build, "Failed add must leave builder usable");

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(InvalidParameterizedProviderFactory.class),
        "Failed add must not keep a partial class binding");
  }

  @Test
  void failedAddDoesNotKeepClassNodeFromDuplicateProviderFailure() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(DuplicateProviderFactory.class),
        "Duplicate @Provides methods must fail registration");

    final var injector = assertDoesNotThrow(builder::build, "Failed add must leave builder usable");

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(DuplicateProviderFactory.class),
        "Failed add must not keep a partial class binding");
    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(Long.class),
        "Failed add must not keep provider bindings registered before duplicate detection failed");
  }

  @Test
  void failedAddDoesNotKeepBindingsFromProviderCycleFailure() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(ProviderCycleFactory.class),
        "Provider cycles must fail registration");

    final var injector = assertDoesNotThrow(builder::build, "Failed add must leave builder usable");

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(ProviderCycleFactory.class),
        "Failed add must not keep a partial factory binding");
    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(CycleFirst.class),
        "Failed add must not keep provider bindings registered before cycle detection failed");
    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(CycleSecond.class),
        "Failed add must not keep the provider binding that failed cycle detection");
  }

  @Test
  void failedClassAddPreservesPreviousSuccessfulBindings() {
    final var builder = Injector.injector().add(ValidDependency.class);

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(InvalidParameterizedProviderFactory.class),
        "Invalid @Provides method must fail registration");

    final var injector = assertDoesNotThrow(builder::build, "Failed add must leave builder usable");

    assertDoesNotThrow(
        () -> injector.get(ValidDependency.class),
        "Failed add must preserve bindings registered before the failed call");
  }

  @Test
  void failedValueAddPreservesPreviousSuccessfulBindings() {
    final Integer registeredValue = 1_234_567;
    final var builder = Injector.injector().add(registeredValue);

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add("kept", (Object) null),
        "Invalid value in varargs batch must fail registration");

    final var injector = assertDoesNotThrow(builder::build, "Failed add must leave builder usable");

    assertSame(
        registeredValue,
        injector.get(Integer.class),
        "Failed add must preserve values registered before the failed call");
  }

  @Test
  void failedVarargsAddDoesNotKeepAnyClassFromBatch() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(ValidDependency.class, InvalidParameterizedProviderFactory.class),
        "Invalid class in varargs batch must fail registration");

    final var injector = assertDoesNotThrow(builder::build, "Failed add must leave builder usable");

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(ValidDependency.class),
        "Failed varargs add must not keep earlier class bindings from the same batch");
    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(InvalidParameterizedProviderFactory.class),
        "Failed varargs add must not keep failed class bindings from the same batch");
  }

  @Test
  void failedVarargsValueAddDoesNotKeepAnyValueFromBatch() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add("kept", (Object) null),
        "Invalid value in varargs batch must fail registration");

    final var injector = assertDoesNotThrow(builder::build, "Failed add must leave builder usable");

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(String.class),
        "Failed varargs value add must not keep earlier value bindings from the same batch");
  }

  @Test
  void duplicateInVarargsValueAddDoesNotKeepAnyValueFromBatch() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add("first", "second"),
        "Duplicate value in varargs batch must fail registration");

    final var injector = assertDoesNotThrow(builder::build, "Failed add must leave builder usable");

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(String.class),
        "Failed duplicate value add must not keep earlier value bindings from the same batch");
  }
}
