package io.github.suppierk.inject.limitations;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class UnsupportedGenericInjectionTest {
  static class WildcardWrapperConsumer {
    private final Provider<? extends Number> number;

    @Inject
    WildcardWrapperConsumer(Provider<? extends Number> number) {
      this.number = number;
    }
  }

  static class TypeVariableWrapperConsumer<T> {
    private final Provider<T> value;

    @Inject
    TypeVariableWrapperConsumer(Provider<T> value) {
      this.value = value;
    }
  }

  static class DirectTypeVariableConsumer<T> {
    private final T value;

    @Inject
    DirectTypeVariableConsumer(T value) {
      this.value = value;
    }
  }

  static class GenericArrayConsumer<T> {
    private final T[] values;

    @Inject
    GenericArrayConsumer(T[] values) {
      this.values = values;
    }
  }

  static class GenericArrayWrapperConsumer<T> {
    private final Provider<T[]> values;

    @Inject
    GenericArrayWrapperConsumer(Provider<T[]> values) {
      this.values = values;
    }
  }

  static class BaseWithGenericField<T> {
    @Inject private T value;
  }

  static class StringChildWithGenericField extends BaseWithGenericField<String> {}

  static class TypeVariableProviderFactory<T> {
    @Provides
    T value() {
      return null;
    }
  }

  static class GenericArrayProviderFactory<T> {
    @Provides
    T[] values() {
      return null;
    }
  }

  @Test
  void wildcardWrapperTypeFailsWithControlledException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(WildcardWrapperConsumer.class),
        "Wildcard wrapper types must fail with a controlled exception");
  }

  @Test
  void typeVariableWrapperTypeFailsWithControlledException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(TypeVariableWrapperConsumer.class),
        "Type variable wrapper types must fail with a controlled exception");
  }

  @Test
  void directTypeVariableInjectionFailsWithControlledException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(DirectTypeVariableConsumer.class),
        "Direct type variable injection must fail with a controlled exception");
  }

  @Test
  void genericArrayInjectionFailsWithControlledException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(GenericArrayConsumer.class),
        "Generic array injection must fail with a controlled exception");
  }

  @Test
  void genericArrayWrapperInjectionFailsWithControlledException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(GenericArrayWrapperConsumer.class),
        "Generic array wrapper injection must fail with a controlled exception");
  }

  @Test
  void inheritedGenericFieldInjectionFailsWithControlledException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(StringChildWithGenericField.class),
        "Inherited generic field injection must fail with a controlled exception");
  }

  @Test
  void typeVariableProviderReturnFailsWithControlledException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(TypeVariableProviderFactory.class),
        "Type variable @Provides return type must fail with a controlled exception");
  }

  @Test
  void genericArrayProviderReturnFailsWithControlledException() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(GenericArrayProviderFactory.class),
        "Generic array @Provides return type must fail with a controlled exception");
  }
}
