package io.github.suppierk.inject.graph;

import io.github.suppierk.inject.Injector;
import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class ProvidesSingletonTest {
  static class Value {
    private final String first;
    private final Long second;

    Value(String first, Long second) {
      this.first = first;
      this.second = second;
    }

    static Constructor<Value> constructor() {
      try {
        return Value.class.getDeclaredConstructor(String.class, Long.class);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Test
  void object_methods_must_work_as_expected() {
    final var redInjector = Injector.injector().add("Red").build();
    final var blueInjector = Injector.injector().add("Blue").build();

    final var constructor = Value.constructor();
    final var redParameter = constructor.getParameters()[0];
    final var blueParameter = constructor.getParameters()[1];

    EqualsVerifier.simple()
        .forClass(ProvidesSingleton.class)
        .withPrefabValues(Injector.class, redInjector, blueInjector)
        .withPrefabValues(Parameter.class, redParameter, blueParameter)
        .withIgnoredFields("injectorReference", "memoized", "onCloseConsumer")
        .verify();
  }
}
