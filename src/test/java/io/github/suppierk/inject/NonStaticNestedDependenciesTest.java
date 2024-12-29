package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class NonStaticNestedDependenciesTest {

  @Nested
  class AdjustingDependencyInMethodsTest {
    class Original {
      @Provides
      @Named("original")
      String original() {
        return "original";
      }

      @Provides
      @Named("adjusted")
      String adjusted(@Named("original") String original) {
        return original + "Adjusted";
      }
    }

    class Consumer {
      private final String result;

      @Inject
      Consumer(@Named("adjusted") String adjusted) {
        this.result = adjusted;
      }
    }

    @Test
    void adjusting_module_should_participate_in_creation_chain() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Original.class));
      assertDoesNotThrow(() -> builder.add(Consumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertEquals("originalAdjusted", injector.get(Consumer.class).result);
    }
  }

  @Nested
  class AdjustingDependencyTest {
    class Original {
      @Provides
      @Named("original")
      String original() {
        return "original";
      }
    }

    class Adjuster {
      private final String original;

      @Inject
      Adjuster(@Named("original") String original) {
        this.original = original;
      }

      @Provides
      @Named("adjusted")
      String adjusted() {
        return original + "Adjusted";
      }
    }

    class Consumer {
      private final String result;

      @Inject
      Consumer(@Named("adjusted") String adjusted) {
        this.result = adjusted;
      }
    }

    @Test
    void adjusting_module_should_participate_in_creation_chain() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Original.class));
      assertDoesNotThrow(() -> builder.add(Adjuster.class));
      assertDoesNotThrow(() -> builder.add(Consumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertEquals("originalAdjusted", injector.get(Consumer.class).result);
    }
  }

  @Nested
  class AmbiguousDependenciesTest {
    class AmbiguousProviderModule {
      @Provides
      String foo() {
        return "foo";
      }

      @Provides
      String bar() {
        return "bar";
      }
    }

    @Test
    void must_throw_illegal_argument_exception() {
      final var builder = Injector.injector();

      assertThrows(
          IllegalArgumentException.class, () -> builder.add(AmbiguousProviderModule.class));
    }
  }

  @Nested
  class AmbiguousSingletonDependenciesTest {
    class AmbiguousProviderModule {
      @Provides
      @Singleton
      String foo() {
        return "foo";
      }

      @Provides
      @Singleton
      String bar() {
        return "bar";
      }
    }

    @Test
    void must_throw_illegal_argument_exception() {
      final var builder = Injector.injector();

      assertThrows(
          IllegalArgumentException.class, () -> builder.add(AmbiguousProviderModule.class));
    }
  }

  @Nested
  class CircularDependenciesProviderBreakTest {
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    class B {
      final Provider<A> a;

      @Inject
      B(Provider<A> a) {
        this.a = a;
      }
    }

    @Test
    void must_allow_cycle_resolution_via_provider() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertNotNull(injector.get(A.class).b.a.get());
    }
  }

  @Nested
  class CircularDependenciesSupplierBreakTest {
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    class B {
      final Supplier<A> a;

      @Inject
      B(Supplier<A> a) {
        this.a = a;
      }
    }

    @Test
    void must_allow_cycle_resolution_via_provider() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertNotNull(injector.get(A.class).b.a.get());
    }
  }

  @Nested
  class CircularDependenciesTest {
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    class B {
      final A a;

      @Inject
      B(A a) {
        this.a = a;
      }
    }

    @Test
    void must_throw_illegal_argument_exception_for_AB_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_BA_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(B.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class CircularSingletonDependenciesProviderBreakTest {
    @Singleton
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    @Singleton
    class B {
      final Provider<A> a;

      @Inject
      B(Provider<A> a) {
        this.a = a;
      }
    }

    @Test
    void must_allow_cycle_resolution_via_provider() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertNotNull(injector.get(A.class).b.a.get());
    }
  }

  @Nested
  class CircularSingletonDependenciesSupplierBreakTest {
    @Singleton
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    @Singleton
    class B {
      final Supplier<A> a;

      @Inject
      B(Supplier<A> a) {
        this.a = a;
      }
    }

    @Test
    void must_allow_cycle_resolution_via_provider() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertNotNull(injector.get(A.class).b.a.get());
    }
  }

  @Nested
  class CircularSingletonDependenciesTest {
    @Singleton
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    @Singleton
    class B {
      final A a;

      @Inject
      B(A a) {
        this.a = a;
      }
    }

    @Test
    void must_throw_illegal_argument_exception_for_AB_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_BA_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(B.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class CustomQualifierSingletonTest {
    class A {
      @Provides
      @Singleton
      @Named("named")
      String named() {
        return "named" + System.nanoTime();
      }

      @Provides
      @Singleton
      @CustomQualifier
      String custom() {
        return "custom" + System.nanoTime();
      }
    }

    class B {
      private final String value;

      @Inject
      B(@Named("named") String value) {
        this.value = value;
      }
    }

    class C {
      private final String value;

      @Inject
      C(@CustomQualifier String value) {
        this.value = value;
      }
    }

    @Test
    void must_provide_both_value_and_the_object() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertTrue(injector.get(B.class).value.startsWith("named"));
      assertTrue(injector.get(C.class).value.startsWith("custom"));
      assertEquals(injector.get(B.class).value, injector.get(B.class).value);
      assertEquals(injector.get(C.class).value, injector.get(C.class).value);
    }
  }

  @Nested
  class CustomQualifierTest {
    class A {
      @Provides
      @Named("named")
      String named() {
        return "named" + System.nanoTime();
      }

      @Provides
      @CustomQualifier
      String custom() {
        return "custom" + System.nanoTime();
      }
    }

    class B {
      private final String value;

      @Inject
      B(@Named("named") String value) {
        this.value = value;
      }
    }

    class C {
      private final String value;

      @Inject
      C(@CustomQualifier String value) {
        this.value = value;
      }
    }

    @Test
    void must_provide_both_value_and_the_object() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertTrue(injector.get(B.class).value.startsWith("named"));
      assertTrue(injector.get(C.class).value.startsWith("custom"));
      assertNotEquals(injector.get(B.class).value, injector.get(B.class).value);
      assertNotEquals(injector.get(C.class).value, injector.get(C.class).value);
    }
  }

  @Nested
  class ExtendedCircularDependenciesTest {
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    class B {
      final C c;

      @Inject
      B(C c) {
        this.c = c;
      }
    }

    class C {
      final A a;

      @Inject
      C(A a) {
        this.a = a;
      }
    }

    @Test
    void must_throw_illegal_argument_exception_for_ABC_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(C.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_ACB_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_BAC_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(C.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_BCA_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_CAB_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(C.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_CBA_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(C.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class ExtendedCircularSingletonDependenciesTest {
    @Singleton
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    @Singleton
    class B {
      final C c;

      @Inject
      B(C c) {
        this.c = c;
      }
    }

    @Singleton
    class C {
      final A a;

      @Inject
      C(A a) {
        this.a = a;
      }
    }

    @Test
    void must_throw_illegal_argument_exception_for_ABC_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(C.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_ACB_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_BAC_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(C.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_BCA_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_CAB_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(C.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(B.class));
    }

    @Test
    void must_throw_illegal_argument_exception_for_CBA_cycle() {
      final var builder = Injector.injector();

      assertDoesNotThrow(() -> builder.add(C.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class InjectInConstructorTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      private final A a;

      @Inject
      B(A a) {
        this.a = a;
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.a);
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.a);
    }
  }

  @Nested
  class InjectInFieldTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      @Inject private A a;
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.a);
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.a);
    }
  }

  @Nested
  class InjectMixedTest {
    class SimpleField {
      @Provides
      String simpleField() {
        return "simpleField";
      }
    }

    class NamedField {
      @Provides
      @Named("namedField")
      String namedField() {
        return "namedField";
      }
    }

    class NamedProviderField {
      @Provides
      @Named("namedProviderField")
      String namedProviderField() {
        return "namedProviderField";
      }
    }

    class SimpleLongField {
      @Provides
      Long simpleLongField() {
        return 42L;
      }
    }

    class NamedLongField {
      @Provides
      @Named("namedLongField")
      Long namedLongField() {
        return 43L;
      }
    }

    class NamedProviderLongField {
      @Provides
      @Named("namedProviderLongField")
      Long namedProviderLongField() {
        return 45L;
      }
    }

    class Consumer {
      @Inject private String simpleField;

      @Inject
      @Named("namedField")
      private String namedField;

      @Inject private Provider<String> simpleProviderField;

      @Inject
      @Named("namedProviderField")
      private Provider<String> namedProviderField;

      private final Long simpleLongField;
      private final Long namedLongField;
      private final Provider<Long> simpleProviderLongField;
      private final Provider<Long> namedProviderLongField;

      @Inject
      Consumer(
          Long simpleLongField,
          @Named("namedLongField") Long namedLongField,
          Provider<Long> simpleProviderLongField,
          @Named("namedProviderLongField") Provider<Long> namedProviderLongField) {
        this.simpleLongField = simpleLongField;
        this.namedLongField = namedLongField;
        this.simpleProviderLongField = simpleProviderLongField;
        this.namedProviderLongField = namedProviderLongField;
      }
    }

    @Test
    void all_fields_injected_as_expected() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(SimpleField.class));
      assertDoesNotThrow(() -> builder.add(NamedField.class));
      assertDoesNotThrow(() -> builder.add(NamedProviderField.class));
      assertDoesNotThrow(() -> builder.add(SimpleLongField.class));
      assertDoesNotThrow(() -> builder.add(NamedLongField.class));
      assertDoesNotThrow(() -> builder.add(NamedProviderLongField.class));
      assertDoesNotThrow(() -> builder.add(Consumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var consumer = injector.get(Consumer.class);
      assertNotNull(consumer);
      assertNotNull(consumer.simpleField);
      assertNotNull(consumer.namedField);
      assertNotNull(consumer.simpleProviderField);
      assertNotNull(consumer.namedProviderField);
      assertNotNull(consumer.simpleLongField);
      assertNotNull(consumer.namedLongField);
      assertNotNull(consumer.simpleProviderLongField);
      assertNotNull(consumer.namedProviderLongField);
    }
  }

  @Nested
  class InjectProviderInConstructorTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      private final A a;

      @Inject
      B(Provider<A> a) {
        this.a = a.get();
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.a);
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.a);
    }
  }

  @Nested
  class InjectProviderInFieldTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      @Inject private Provider<A> a;

      public A getA() {
        return a.get();
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.getA());
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.getA());
    }
  }

  @Nested
  class InjectQualifiedClassAsSuperclassTest {
    @Singleton
    @Named("testA")
    class A implements Runnable {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public void run() {
        // Do nothing
      }
    }

    class C implements Runnable {
      private final Runnable r;

      @Inject
      C(@Named("testA") Runnable r) {
        this.r = r;
      }

      @Override
      public void run() {
        r.run();
      }
    }

    @Test
    void must_fail_to_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      assertThrows(IllegalArgumentException.class, builder::build);
    }
  }

  @Nested
  class InjectSingletonInConstructorTest {
    @Singleton
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      private final A a;

      @Inject
      B(A a) {
        this.a = a;
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.a);
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.a);
    }
  }

  @Nested
  class InjectSingletonInFieldTest {
    @Singleton
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      @Inject private A a;
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.a);
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.a);
    }
  }

  @Nested
  class InjectSingletonProviderInConstructorTest {
    @Singleton
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      private final A a;

      @Inject
      B(Provider<A> a) {
        this.a = a.get();
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.a);
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.a);
    }
  }

  @Nested
  class InjectSingletonProviderInFieldTest {
    @Singleton
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      @Inject private Provider<A> a;

      public A getA() {
        return a.get();
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.getA());
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.getA());
    }
  }

  @Nested
  class InjectSingletonSupplierInConstructorTest {
    @Singleton
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      private final A a;

      @Inject
      B(Supplier<A> a) {
        this.a = a.get();
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.a);
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.a);
    }
  }

  @Nested
  class InjectSingletonSupplierInFieldTest {
    @Singleton
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      @Inject private Supplier<A> a;

      public A getA() {
        return a.get();
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.getA());
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertEquals(exemplar, target.getA());
    }
  }

  @Nested
  class InjectSupplierInConstructorTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      private final A a;

      @Inject
      B(Supplier<A> a) {
        this.a = a.get();
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.a);
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.a);
    }
  }

  @Nested
  class InjectSupplierInFieldTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B {
      @Inject private Supplier<A> a;

      public A getA() {
        return a.get();
      }
    }

    @Test
    void must_correctly_inject_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.getA());
    }

    @Test
    void must_correctly_inject_dependency_if_declaration_order_was_reversed() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(A.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var exemplar = assertDoesNotThrow(() -> injector.get(A.class));
      final var target = assertDoesNotThrow(() -> injector.get(B.class));
      assertNotEquals(exemplar, target.getA());
    }
  }

  @Nested
  class ModuleExtensionTest {
    class A {
      @Provides
      @Named("original")
      String original() {
        return "original" + System.nanoTime();
      }
    }

    class B extends A {
      @Provides
      @Named("additional")
      String additional() {
        return "additional" + System.nanoTime();
      }
    }

    class Consumer {
      private final String original;
      private final String additional;

      @Inject
      Consumer(@Named("original") String original, @Named("additional") String additional) {
        this.original = original;
        this.additional = additional;
      }
    }

    @Test
    void module_extension_must_pickup_what_parent_provided() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(Consumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var consumer = injector.get(Consumer.class);
      assertTrue(consumer.original.startsWith("original"));
      assertTrue(consumer.additional.startsWith("additional"));
    }
  }

  @Nested
  class MultipleInjectConstructorsTest {
    class Value {
      private final String value;

      @Inject
      Value(String value) {
        this.value = value;
      }

      @Inject
      Value(Long value) {
        this.value = value.toString();
      }
    }

    @Test
    void should_throws_an_exception() {
      final var builder = Injector.injector();

      assertThrows(IllegalArgumentException.class, () -> builder.add(Value.class));
    }
  }

  @Nested
  class NoCollectionInjectionInFieldTest {
    class Module {
      @Provides
      @Named("stringA")
      String stringA() {
        return "stringA";
      }

      @Provides
      @Named("stringB")
      String stringB() {
        return "stringB";
      }
    }

    class A {
      @Inject private Collection<String> strings;

      A() {}
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Module.class));

      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class NoCollectionInjectionTest {
    class Module {
      @Provides
      @Named("stringA")
      String stringA() {
        return "stringA";
      }

      @Provides
      @Named("stringB")
      String stringB() {
        return "stringB";
      }
    }

    class A {
      private final Collection<String> strings;

      @Inject
      A(Collection<String> strings) {
        this.strings = strings;
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Module.class));

      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class NoCollectionProviderTest {
    class Module {
      @Provides
      Collection<String> collection() {
        return Set.of("test1", "test2");
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();

      assertThrows(IllegalArgumentException.class, () -> builder.add(Module.class));
    }
  }

  @Nested
  class NoMapInjectionInFieldTest {
    class Module {
      @Provides
      @Named("stringA")
      String stringA() {
        return "stringA";
      }

      @Provides
      @Named("stringB")
      String stringB() {
        return "stringB";
      }
    }

    class A {
      @Inject private Map<String, String> strings;

      A() {}
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Module.class));

      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class NoMapInjectionTest {
    class Module {
      @Provides
      @Named("stringA")
      String stringA() {
        return "stringA";
      }

      @Provides
      @Named("stringB")
      String stringB() {
        return "stringB";
      }
    }

    class A {
      private final Map<String, String> strings;

      @Inject
      A(Map<String, String> strings) {
        this.strings = strings;
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Module.class));

      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class NoMapProviderTest {
    class Module {
      @Provides
      Map<String, String> collection() {
        return Map.of("test1", "test2");
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();

      assertThrows(IllegalArgumentException.class, () -> builder.add(Module.class));
    }
  }

  @Nested
  class NoProviderCollectionInjectionTest {
    class Module {
      @Provides
      @Named("stringA")
      String stringA() {
        return "stringA";
      }

      @Provides
      @Named("stringB")
      String stringB() {
        return "stringB";
      }
    }

    class A {
      private final Provider<Collection<String>> strings;

      @Inject
      A(Provider<Collection<String>> strings) {
        this.strings = strings;
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Module.class));

      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class NoProviderMapInjectionTest {
    class Module {
      @Provides
      @Named("stringA")
      String stringA() {
        return "stringA";
      }

      @Provides
      @Named("stringB")
      String stringB() {
        return "stringB";
      }
    }

    class A {
      private final Provider<Map<String, String>> strings;

      @Inject
      A(Provider<Map<String, String>> strings) {
        this.strings = strings;
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Module.class));

      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class NoSuitableConstructorTest {
    class Value {
      private final String value;

      Value(String value) {
        this.value = value;
      }
    }

    @Test
    void should_throws_an_exception() {
      final var builder = Injector.injector();

      assertThrows(IllegalArgumentException.class, () -> builder.add(Value.class));
    }
  }

  @Nested
  class NoSupplierCollectionInjectionTest {
    class Module {
      @Provides
      @Named("stringA")
      String stringA() {
        return "stringA";
      }

      @Provides
      @Named("stringB")
      String stringB() {
        return "stringB";
      }
    }

    class A {
      private final Supplier<Collection<String>> strings;

      @Inject
      A(Supplier<Collection<String>> strings) {
        this.strings = strings;
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Module.class));

      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class NoSupplierMapInjectionTest {
    class Module {
      @Provides
      @Named("stringA")
      String stringA() {
        return "stringA";
      }

      @Provides
      @Named("stringB")
      String stringB() {
        return "stringB";
      }
    }

    class A {
      private final Supplier<Map<String, String>> strings;

      @Inject
      A(Supplier<Map<String, String>> strings) {
        this.strings = strings;
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Module.class));

      assertThrows(IllegalArgumentException.class, () -> builder.add(A.class));
    }
  }

  @Nested
  class NoTransitiveResolutionTest {
    class A {
      final B b;

      @Inject
      A(B b) {
        this.b = b;
      }
    }

    class B {
      final C c;

      @Inject
      B(C c) {
        this.c = c;
      }
    }

    class C {}

    @Test
    void does_not_resolve_transitive_dependencies_implicitly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertThrows(IllegalArgumentException.class, builder::build);
    }

    @Test
    void works_when_transitive_dependencies_defined_explicitly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertNotNull(injector.get(A.class).b.c);
    }
  }

  interface Poly {}

  @Nested
  class PolymorphicDependencyTest {
    class PolyA implements Poly {}

    class PolyB implements Poly {}

    class Module {
      @Provides
      @Named("polyA")
      Poly polyA() {
        return new PolyA();
      }

      @Provides
      @Named("polyB")
      Poly polyB() {
        return new PolyB();
      }
    }

    class A {
      private final Poly poly;

      @Inject
      A(@Named("polyA") Poly poly) {
        this.poly = poly;
      }
    }

    class B {
      private final Poly poly;

      @Inject
      B(@Named("polyB") Poly poly) {
        this.poly = poly;
      }
    }

    @Test
    void must_provide_both_values_correctly() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(Module.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertNotNull(injector.get(A.class).poly);
      assertNotNull(injector.get(B.class).poly);
      assertEquals(PolyA.class, injector.get(A.class).poly.getClass());
      assertEquals(PolyB.class, injector.get(B.class).poly.getClass());
    }
  }

  @Nested
  class ProvidesNamedSingletonTest {
    class A {
      @Provides
      @Singleton
      String nonNamed() {
        return "simple" + System.nanoTime();
      }

      @Provides
      @Singleton
      @Named("named")
      String named() {
        return "named" + System.nanoTime();
      }
    }

    class B {
      private final String value;

      @Inject
      B(String value) {
        this.value = value;
      }
    }

    class C {
      private final String value;

      @Inject
      C(@Named("named") String value) {
        this.value = value;
      }
    }

    @Test
    void must_provide_both_value_and_the_object() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertTrue(injector.get(B.class).value.startsWith("simple"));
      assertTrue(injector.get(C.class).value.startsWith("named"));
      assertEquals(injector.get(B.class).value, injector.get(B.class).value);
      assertEquals(injector.get(C.class).value, injector.get(C.class).value);
    }
  }

  @Nested
  class ProvidesNamedTest {
    class A {
      @Provides
      String nonNamed() {
        return "simple" + System.nanoTime();
      }

      @Provides
      @Named("named")
      String named() {
        return "named" + System.nanoTime();
      }
    }

    class B {
      private final String value;

      @Inject
      B(String value) {
        this.value = value;
      }
    }

    class C {
      private final String value;

      @Inject
      C(@Named("named") String value) {
        this.value = value;
      }
    }

    @Test
    void must_provide_both_value_and_the_object() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertTrue(injector.get(B.class).value.startsWith("simple"));
      assertTrue(injector.get(C.class).value.startsWith("named"));
      assertNotEquals(injector.get(B.class).value, injector.get(B.class).value);
      assertNotEquals(injector.get(C.class).value, injector.get(C.class).value);
    }
  }

  @Nested
  class ProvidesSingletonClassTest {
    @Singleton
    class Value {
      private final Long value;

      Value() {
        this.value = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof Value)) return false;
        Value value1 = (Value) o;
        return Objects.equals(value, value1.value);
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(value);
      }
    }

    class A {
      @Provides
      Value test() {
        return new Value();
      }
    }

    class B {
      private final Value value;

      @Inject
      B(Value value) {
        this.value = value;
      }
    }

    class C {
      private final Value value;

      @Inject
      C(Value value) {
        this.value = value;
      }
    }

    @Test
    void must_provide_both_value_and_the_object() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertEquals(injector.get(B.class).value, injector.get(C.class).value);
    }
  }

  @Nested
  class ProvidesSingletonTest {
    class A {
      @Provides
      @Singleton
      Long test() {
        return System.nanoTime();
      }
    }

    class B {
      private final Long value;

      @Inject
      B(Long value) {
        this.value = value;
      }
    }

    class C {
      private final Long value;

      @Inject
      C(Long value) {
        this.value = value;
      }
    }

    @Test
    void must_provide_both_value_and_the_object() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertEquals(injector.get(B.class).value, injector.get(C.class).value);
    }
  }

  @Nested
  class ProvidesTest {
    class A {
      @Provides
      Long test() {
        return System.nanoTime();
      }
    }

    class B {
      private final Long value;

      @Inject
      B(Long value) {
        this.value = value;
      }
    }

    class C {
      private final Long value;

      @Inject
      C(Long value) {
        this.value = value;
      }
    }

    @Test
    void must_provide_both_value_and_the_object() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      assertDoesNotThrow(() -> builder.add(C.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertNotEquals(injector.get(B.class).value, injector.get(C.class).value);
    }
  }

  @Nested
  class ReplaceAndInjectInConstructorTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      String getValue() {
        return "original" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B extends A {
      private final long id;

      B() {
        this.id = System.nanoTime();
      }

      @Override
      String getValue() {
        return "replacement" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof B)) return false;
        B b = (B) o;
        return id == b.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class FirstConsumer {
      private final A value;

      @Inject
      FirstConsumer(A value) {
        this.value = value;
      }
    }

    class SecondConsumer {
      private final A value;

      @Inject
      SecondConsumer(A value) {
        this.value = value;
      }
    }

    @Test
    void must_correctly_inject_replaced_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(FirstConsumer.class));
      assertDoesNotThrow(() -> builder.add(SecondConsumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var originalFirstConsumer = assertDoesNotThrow(() -> injector.get(FirstConsumer.class));
      final var originalSecondConsumer =
          assertDoesNotThrow(() -> injector.get(SecondConsumer.class));
      assertTrue(originalFirstConsumer.value.getValue().startsWith("original"));
      assertTrue(originalSecondConsumer.value.getValue().startsWith("original"));
      assertNotEquals(
          originalFirstConsumer.value.getValue(), originalSecondConsumer.value.getValue());

      final var copyBuilder = injector.copy();
      assertDoesNotThrow(() -> copyBuilder.replace(A.class, B.class));
      final var copyInjector = assertDoesNotThrow(copyBuilder::build);

      final var replacedFirstConsumer =
          assertDoesNotThrow(() -> copyInjector.get(FirstConsumer.class));
      final var replacedSecondConsumer =
          assertDoesNotThrow(() -> copyInjector.get(SecondConsumer.class));
      assertTrue(replacedFirstConsumer.value.getValue().startsWith("replacement"));
      assertTrue(replacedSecondConsumer.value.getValue().startsWith("replacement"));
      assertNotEquals(
          replacedFirstConsumer.value.getValue(), replacedSecondConsumer.value.getValue());
    }
  }

  @Nested
  class ReplaceAndInjectInFieldTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      String getValue() {
        return "original" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B extends A {
      private final long id;

      B() {
        this.id = System.nanoTime();
      }

      @Override
      String getValue() {
        return "replacement" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof B)) return false;
        B b = (B) o;
        return id == b.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class FirstConsumer {
      @Inject private A value;

      FirstConsumer() {}
    }

    class SecondConsumer {
      @Inject private A value;

      SecondConsumer() {}
    }

    @Test
    void must_correctly_inject_replaced_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(FirstConsumer.class));
      assertDoesNotThrow(() -> builder.add(SecondConsumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var originalFirstConsumer = assertDoesNotThrow(() -> injector.get(FirstConsumer.class));
      final var originalSecondConsumer =
          assertDoesNotThrow(() -> injector.get(SecondConsumer.class));
      assertTrue(originalFirstConsumer.value.getValue().startsWith("original"));
      assertTrue(originalSecondConsumer.value.getValue().startsWith("original"));
      assertNotEquals(
          originalFirstConsumer.value.getValue(), originalSecondConsumer.value.getValue());

      final var copyBuilder = injector.copy();
      assertDoesNotThrow(() -> copyBuilder.replace(A.class, B.class));
      final var copyInjector = assertDoesNotThrow(copyBuilder::build);

      final var replacedFirstConsumer =
          assertDoesNotThrow(() -> copyInjector.get(FirstConsumer.class));
      final var replacedSecondConsumer =
          assertDoesNotThrow(() -> copyInjector.get(SecondConsumer.class));
      assertTrue(replacedFirstConsumer.value.getValue().startsWith("replacement"));
      assertTrue(replacedSecondConsumer.value.getValue().startsWith("replacement"));
      assertNotEquals(
          replacedFirstConsumer.value.getValue(), replacedSecondConsumer.value.getValue());
    }
  }

  @Nested
  class ReplaceAndInjectProvidesSingletonTest {
    class A {
      @Provides
      @Singleton
      String getValue() {
        return "original" + System.nanoTime();
      }
    }

    class B extends A {
      @Override
      String getValue() {
        return "replacement" + System.nanoTime();
      }
    }

    class FirstConsumer {
      private final String value;

      @Inject
      FirstConsumer(String value) {
        this.value = value;
      }
    }

    class SecondConsumer {
      private final String value;

      @Inject
      SecondConsumer(String value) {
        this.value = value;
      }
    }

    @Test
    void must_correctly_inject_replaced_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(FirstConsumer.class));
      assertDoesNotThrow(() -> builder.add(SecondConsumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var originalFirstConsumer = assertDoesNotThrow(() -> injector.get(FirstConsumer.class));
      final var originalSecondConsumer =
          assertDoesNotThrow(() -> injector.get(SecondConsumer.class));
      assertTrue(originalFirstConsumer.value.startsWith("original"));
      assertTrue(originalSecondConsumer.value.startsWith("original"));
      assertEquals(originalFirstConsumer.value, originalSecondConsumer.value);

      final var copyBuilder = injector.copy();
      assertDoesNotThrow(() -> copyBuilder.replace(A.class, B.class));
      final var copyInjector = assertDoesNotThrow(copyBuilder::build);

      final var replacedFirstConsumer =
          assertDoesNotThrow(() -> copyInjector.get(FirstConsumer.class));
      final var replacedSecondConsumer =
          assertDoesNotThrow(() -> copyInjector.get(SecondConsumer.class));
      assertTrue(replacedFirstConsumer.value.startsWith("replacement"));
      assertTrue(replacedSecondConsumer.value.startsWith("replacement"));
      assertEquals(replacedFirstConsumer.value, replacedSecondConsumer.value);
    }
  }

  @Nested
  class ReplaceAndInjectProvidesTest {
    class A {
      @Provides
      String getValue() {
        return "original" + System.nanoTime();
      }
    }

    class B extends A {
      @Override
      String getValue() {
        return "replacement" + System.nanoTime();
      }
    }

    class FirstConsumer {
      private final String value;

      @Inject
      FirstConsumer(String value) {
        this.value = value;
      }
    }

    class SecondConsumer {
      private final String value;

      @Inject
      SecondConsumer(String value) {
        this.value = value;
      }
    }

    @Test
    void must_correctly_inject_replaced_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(FirstConsumer.class));
      assertDoesNotThrow(() -> builder.add(SecondConsumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var originalFirstConsumer = assertDoesNotThrow(() -> injector.get(FirstConsumer.class));
      final var originalSecondConsumer =
          assertDoesNotThrow(() -> injector.get(SecondConsumer.class));
      assertTrue(originalFirstConsumer.value.startsWith("original"));
      assertTrue(originalSecondConsumer.value.startsWith("original"));
      assertNotEquals(originalFirstConsumer.value, originalSecondConsumer.value);

      final var copyBuilder = injector.copy();
      assertDoesNotThrow(() -> copyBuilder.replace(A.class, B.class));
      final var copyInjector = assertDoesNotThrow(copyBuilder::build);

      final var replacedFirstConsumer =
          assertDoesNotThrow(() -> copyInjector.get(FirstConsumer.class));
      final var replacedSecondConsumer =
          assertDoesNotThrow(() -> copyInjector.get(SecondConsumer.class));
      assertTrue(replacedFirstConsumer.value.startsWith("replacement"));
      assertTrue(replacedSecondConsumer.value.startsWith("replacement"));
      assertNotEquals(replacedFirstConsumer.value, replacedSecondConsumer.value);
    }
  }

  @Nested
  class ReplaceAndInjectSingletonInConstructorTest {
    @Singleton
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      String getValue() {
        return "original" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    @Singleton
    class B extends A {
      private final long id;

      B() {
        this.id = System.nanoTime();
      }

      @Override
      String getValue() {
        return "replacement" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof B)) return false;
        B b = (B) o;
        return id == b.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class FirstConsumer {
      private final A value;

      @Inject
      FirstConsumer(A value) {
        this.value = value;
      }
    }

    class SecondConsumer {
      private final A value;

      @Inject
      SecondConsumer(A value) {
        this.value = value;
      }
    }

    @Test
    void must_correctly_inject_replaced_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(FirstConsumer.class));
      assertDoesNotThrow(() -> builder.add(SecondConsumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var originalFirstConsumer = assertDoesNotThrow(() -> injector.get(FirstConsumer.class));
      final var originalSecondConsumer =
          assertDoesNotThrow(() -> injector.get(SecondConsumer.class));
      assertTrue(originalFirstConsumer.value.getValue().startsWith("original"));
      assertTrue(originalSecondConsumer.value.getValue().startsWith("original"));
      assertEquals(originalFirstConsumer.value.getValue(), originalSecondConsumer.value.getValue());

      final var copyBuilder = injector.copy();
      assertDoesNotThrow(() -> copyBuilder.replace(A.class, B.class));
      final var copyInjector = assertDoesNotThrow(copyBuilder::build);

      final var replacedFirstConsumer =
          assertDoesNotThrow(() -> copyInjector.get(FirstConsumer.class));
      final var replacedSecondConsumer =
          assertDoesNotThrow(() -> copyInjector.get(SecondConsumer.class));
      assertTrue(replacedFirstConsumer.value.getValue().startsWith("replacement"));
      assertTrue(replacedSecondConsumer.value.getValue().startsWith("replacement"));
      assertEquals(replacedFirstConsumer.value.getValue(), replacedSecondConsumer.value.getValue());
    }
  }

  @Nested
  class ReplaceAndInjectSingletonInFieldTest {
    @Singleton
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      String getValue() {
        return "original" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    @Singleton
    class B extends A {
      private final long id;

      B() {
        this.id = System.nanoTime();
      }

      @Override
      String getValue() {
        return "replacement" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof B)) return false;
        B b = (B) o;
        return id == b.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class FirstConsumer {
      @Inject private A value;

      FirstConsumer() {}
    }

    class SecondConsumer {
      @Inject private A value;

      SecondConsumer() {}
    }

    @Test
    void must_correctly_inject_replaced_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(FirstConsumer.class));
      assertDoesNotThrow(() -> builder.add(SecondConsumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var originalFirstConsumer = assertDoesNotThrow(() -> injector.get(FirstConsumer.class));
      final var originalSecondConsumer =
          assertDoesNotThrow(() -> injector.get(SecondConsumer.class));
      assertTrue(originalFirstConsumer.value.getValue().startsWith("original"));
      assertTrue(originalSecondConsumer.value.getValue().startsWith("original"));
      assertEquals(originalFirstConsumer.value.getValue(), originalSecondConsumer.value.getValue());

      final var copyBuilder = injector.copy();
      assertDoesNotThrow(() -> copyBuilder.replace(A.class, B.class));
      final var copyInjector = assertDoesNotThrow(copyBuilder::build);

      final var replacedFirstConsumer =
          assertDoesNotThrow(() -> copyInjector.get(FirstConsumer.class));
      final var replacedSecondConsumer =
          assertDoesNotThrow(() -> copyInjector.get(SecondConsumer.class));
      assertTrue(replacedFirstConsumer.value.getValue().startsWith("replacement"));
      assertTrue(replacedSecondConsumer.value.getValue().startsWith("replacement"));
      assertEquals(replacedFirstConsumer.value.getValue(), replacedSecondConsumer.value.getValue());
    }
  }

  @Nested
  class ReplaceAndInjectValueDependencyTest {
    class A {
      private final long id;

      A() {
        this.id = System.nanoTime();
      }

      String getValue() {
        return "original" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof A)) return false;
        A a = (A) o;
        return id == a.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class B extends A {
      private final long id;

      B() {
        this.id = System.nanoTime();
      }

      @Override
      String getValue() {
        return "replacement" + id;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof B)) return false;
        B b = (B) o;
        return id == b.id;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(id);
      }
    }

    class Consumer {
      private final A value;

      @Inject
      Consumer(A value) {
        this.value = value;
      }
    }

    @Test
    void must_correctly_inject_replaced_dependency() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(new A()));
      assertDoesNotThrow(() -> builder.add(Consumer.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var originalConsumer = assertDoesNotThrow(() -> injector.get(Consumer.class));
      assertTrue(originalConsumer.value.getValue().startsWith("original"));

      final var copyBuilder = injector.copy();
      assertDoesNotThrow(() -> copyBuilder.replace(new A(), new B()));
      final var copyInjector = assertDoesNotThrow(copyBuilder::build);

      final var replacedConsumer = assertDoesNotThrow(() -> copyInjector.get(Consumer.class));
      assertTrue(replacedConsumer.value.getValue().startsWith("replacement"));
    }
  }

  @Nested
  class SelfLoopProviderBreakTest {
    @Singleton
    class Loop {
      final long value;

      final Provider<Loop> loopProvider;

      @Inject
      Loop(Provider<Loop> loopProvider) {
        this.value = System.nanoTime();
        this.loopProvider = loopProvider;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof Loop)) return false;
        Loop thatLoop = (Loop) o;
        return value == thatLoop.value;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(value);
      }
    }

    @Test
    void must_return_itself() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Loop.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var loop = assertDoesNotThrow(() -> injector.get(Loop.class));
      assertEquals(loop, loop.loopProvider.get());
    }
  }

  @Nested
  class SelfLoopSupplierBreakTest {
    @Singleton
    class Loop {
      final long value;

      final Provider<Loop> loopProvider;

      @Inject
      Loop(Provider<Loop> loopProvider) {
        this.value = System.nanoTime();
        this.loopProvider = loopProvider;
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof Loop)) return false;
        Loop thatLoop = (Loop) o;
        return value == thatLoop.value;
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(value);
      }
    }

    @Test
    void must_return_itself() {
      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(Loop.class));
      final var injector = assertDoesNotThrow(builder::build);

      final var loop = assertDoesNotThrow(() -> injector.get(Loop.class));
      assertEquals(loop, loop.loopProvider.get());
    }
  }

  @Nested
  class SelfLoopTest {
    class Loop {
      final Loop loop;

      @Inject
      Loop(Loop loop) {
        this.loop = loop;
      }
    }

    @Test
    void must_throw_illegal_argument_exception_for_self_loop() {
      final var builder = Injector.injector();

      assertThrows(IllegalArgumentException.class, () -> builder.add(Loop.class));
    }
  }

  @Nested
  class ValueDependencyTest {
    class Value {
      private final Long value;

      Value() {
        this.value = System.nanoTime();
      }

      @Override
      public boolean equals(Object o) {
        if (!(o instanceof Value)) return false;
        Value value1 = (Value) o;
        return Objects.equals(value, value1.value);
      }

      @Override
      public int hashCode() {
        return Objects.hashCode(value);
      }
    }

    class A {
      private final Value value;

      @Inject
      A(Value value) {
        this.value = value;
      }
    }

    class B {
      private final Value value;

      @Inject
      B(Value value) {
        this.value = value;
      }
    }

    @Test
    void name() {
      final var value = new Value();

      final var builder = Injector.injector();
      assertDoesNotThrow(() -> builder.add(value));
      assertDoesNotThrow(() -> builder.add(A.class));
      assertDoesNotThrow(() -> builder.add(B.class));
      final var injector = assertDoesNotThrow(builder::build);

      assertEquals(value, injector.get(Value.class));
      assertEquals(value, injector.get(A.class).value);
      assertEquals(value, injector.get(B.class).value);
    }
  }
}
