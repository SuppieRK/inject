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

package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.suppierk.inject.providing.factories.FactoryProviderPolymorphicNamedTest.Polymorphic;
import io.github.suppierk.mocks.CustomQualifier;
import io.github.suppierk.mocks.NanoTimeValue;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class NestedNonStaticTests {
  @Nested
  class Consuming {
    @Nested
    class Constructor {
      @Nested
      class InjectInConstructorTest {
        class Consumer {
          private final NanoTimeValue value;

          @Inject
          Consumer(NanoTimeValue value) {
            this.value = value;
          }
        }

        @Test
        void mustCorrectlyInjectDependency() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(NanoTimeValue.class, Consumer.class),
              "There must be no problem with providing dependencies");
          final var injector =
              assertDoesNotThrow(
                  builder::build, "There must be no problem with constructing injector");

          final var exemplar =
              assertDoesNotThrow(
                  () -> injector.get(NanoTimeValue.class),
                  "There must be no problem with retrieving value");
          final var target =
              assertDoesNotThrow(
                  () -> injector.get(Consumer.class),
                  "There must be no problem with retrieving consumer");
          assertNotEquals(
              exemplar,
              target.value,
              "Because factory provides non-singleton dependency, injected values must be different");
        }
      }

      @Nested
      class InjectProviderInConstructorTest {
        class Consumer {
          private final NanoTimeValue value;

          @Inject
          Consumer(Provider<NanoTimeValue> value) {
            this.value = value.get();
          }
        }

        @Test
        void mustCorrectlyInjectDependency() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(NanoTimeValue.class, Consumer.class),
              "There must be no problem with providing dependencies");
          final var injector =
              assertDoesNotThrow(
                  builder::build, "There must be no problem with constructing injector");

          final var exemplar =
              assertDoesNotThrow(
                  () -> injector.get(NanoTimeValue.class),
                  "There must be no problem with retrieving value");
          final var target =
              assertDoesNotThrow(
                  () -> injector.get(Consumer.class),
                  "There must be no problem with retrieving consumer");
          assertNotEquals(
              exemplar,
              target.value,
              "Because factory provides non-singleton dependency, injected values must be different");
        }
      }

      @Nested
      class InjectSupplierInConstructorTest {
        class Consumer {
          private final NanoTimeValue value;

          @Inject
          Consumer(Supplier<NanoTimeValue> value) {
            this.value = value.get();
          }
        }

        @Test
        void mustCorrectlyInjectDependency() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(NanoTimeValue.class, Consumer.class),
              "There must be no problem with providing dependencies");
          final var injector =
              assertDoesNotThrow(
                  builder::build, "There must be no problem with constructing injector");

          final var exemplar =
              assertDoesNotThrow(
                  () -> injector.get(NanoTimeValue.class),
                  "There must be no problem with retrieving value");
          final var target =
              assertDoesNotThrow(
                  () -> injector.get(Consumer.class),
                  "There must be no problem with retrieving consumer");
          assertNotEquals(
              exemplar,
              target.value,
              "Because factory provides non-singleton dependency, injected values must be different");
        }
      }
    }

    @Nested
    class Field {
      @Nested
      class InjectInFieldTest {
        class Consumer {
          @Inject private NanoTimeValue value;
        }

        @Test
        void mustCorrectlyInjectDependency() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(NanoTimeValue.class, Consumer.class),
              "There must be no problem with providing dependencies");
          final var injector =
              assertDoesNotThrow(
                  builder::build, "There must be no problem with constructing injector");

          final var exemplar =
              assertDoesNotThrow(
                  () -> injector.get(NanoTimeValue.class),
                  "There must be no problem with retrieving value");
          final var target =
              assertDoesNotThrow(
                  () -> injector.get(Consumer.class),
                  "There must be no problem with retrieving consumer");
          assertNotEquals(
              exemplar,
              target.value,
              "Because factory provides non-singleton dependency, injected values must be different");
        }
      }

      @Nested
      class InjectProviderInFieldTest {
        class Consumer {
          @Inject private Provider<NanoTimeValue> valueProvider;
        }

        @Test
        void mustCorrectlyInjectDependency() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(NanoTimeValue.class, Consumer.class),
              "There must be no problem with providing dependencies");
          final var injector =
              assertDoesNotThrow(
                  builder::build, "There must be no problem with constructing injector");

          final var exemplar =
              assertDoesNotThrow(
                  () -> injector.get(NanoTimeValue.class),
                  "There must be no problem with retrieving value");
          final var target =
              assertDoesNotThrow(
                  () -> injector.get(Consumer.class),
                  "There must be no problem with retrieving consumer");
          assertNotEquals(
              exemplar,
              target.valueProvider.get(),
              "Because factory provides non-singleton dependency, injected values must be different");
        }
      }

      @Nested
      class InjectSupplierInFieldTest {
        class Consumer {
          @Inject private Supplier<NanoTimeValue> valueSupplier;
        }

        @Test
        void mustCorrectlyInjectDependency() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(NanoTimeValue.class, Consumer.class),
              "There must be no problem with providing dependencies");
          final var injector =
              assertDoesNotThrow(
                  builder::build, "There must be no problem with constructing injector");

          final var exemplar =
              assertDoesNotThrow(
                  () -> injector.get(NanoTimeValue.class),
                  "There must be no problem with retrieving value");
          final var target =
              assertDoesNotThrow(
                  () -> injector.get(Consumer.class),
                  "There must be no problem with retrieving consumer");
          assertNotEquals(
              exemplar,
              target.valueSupplier.get(),
              "Because factory provides non-singleton dependency, injected values must be different");
        }
      }
    }

    @Nested
    class Mixed {
      @Nested
      class DefaultAndInjectConstructorsTest {
        class Consumer {
          private final String value;

          Consumer() {
            this.value = String.valueOf(System.nanoTime());
          }

          @Inject
          Consumer(Long value) {
            this.value = value.toString();
          }
        }

        @Test
        void mustThrowAnException() {
          final var builder = Injector.injector();

          assertThrows(
              IllegalArgumentException.class,
              () -> builder.add(Consumer.class),
              "Class with both @Inject and default constructor must throw an exception");
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
        void allFieldsInjectedAsExpected() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(SimpleField.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(NamedField.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(NamedProviderField.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(SimpleLongField.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(NamedLongField.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(NamedProviderLongField.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(Consumer.class), "There must be no problem with adding consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          final var consumer = injector.get(Consumer.class);
          assertNotNull(consumer, "Consumer must be instantiated and not null");
          assertNotNull(
              consumer.simpleField, "Consumer's simpleField must be instantiated and not null");
          assertNotNull(
              consumer.namedField, "Consumer's namedField must be instantiated and not null");
          assertNotNull(
              consumer.simpleProviderField,
              "Consumer's simpleProviderField must be instantiated and not null");
          assertNotNull(
              consumer.namedProviderField,
              "Consumer's namedProviderField must be instantiated and not null");
          assertNotNull(
              consumer.simpleLongField,
              "Consumer's simpleLongField must be instantiated and not null");
          assertNotNull(
              consumer.namedLongField,
              "Consumer's namedLongField must be instantiated and not null");
          assertNotNull(
              consumer.simpleProviderLongField,
              "Consumer's simpleProviderLongField must be instantiated and not null");
          assertNotNull(
              consumer.namedProviderLongField,
              "Consumer's namedProviderLongField must be instantiated and not null");
        }
      }

      @Nested
      class InjectQualifiedClassAsSuperclassTest {
        @Named("testRunnable")
        class TestRunnable implements Runnable {
          private final long id;

          TestRunnable() {
            this.id = System.nanoTime();
          }

          @Override
          public void run() {
            // Do nothing
          }
        }

        class DependencyFactory {
          @Provides
          Runnable runnable() {
            return new TestRunnable();
          }
        }

        class Consumer implements Runnable {
          private final Runnable runnable;

          @Inject
          Consumer(@Named("testRunnable") Runnable runnable) {
            this.runnable = runnable;
          }

          @Override
          public void run() {
            runnable.run();
          }
        }

        @Test
        void mustThrowException() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(DependencyFactory.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(Consumer.class), "There must be no problem with adding consumer");
          assertThrows(
              IllegalArgumentException.class,
              builder::build,
              "Injector must not be created, because there is no dependency that would fit the expected key in Consumer");
        }
      }

      @Nested
      class MultipleInjectConstructorsTest {
        class Consumer {
          private final String value;

          @Inject
          Consumer(String value) {
            this.value = value;
          }

          @Inject
          Consumer(Long value) {
            this.value = value.toString();
          }
        }

        @Test
        void mustThrowAnException() {
          final var builder = Injector.injector();

          assertThrows(
              IllegalArgumentException.class,
              () -> builder.add(Consumer.class),
              "Class with multiple @Inject constructors must throw an exception");
        }
      }
    }
  }

  @Nested
  class Cycles {
    @Nested
    class CycleBreakWithProviderTest {
      class First {
        final Second second;

        @Inject
        First(Second second) {
          this.second = second;
        }
      }

      class Second {
        final Provider<First> firstProvider;

        @Inject
        Second(Provider<First> firstProvider) {
          this.firstProvider = firstProvider;
        }
      }

      @Test
      void cycleCanBeConstructedWithProvider() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(First.class), "First cycle element must not cause problems");
        assertDoesNotThrow(
            () -> builder.add(Second.class), "Second cycle element must not cause problems");
        final var injector =
            assertDoesNotThrow(
                builder::build, "There must be no problem with constructing injector");

        assertNotNull(
            injector.get(First.class).second.firstProvider.get(),
            "Cycle broken with Provider must result in correctly injected non-null value");
      }
    }

    @Nested
    class CycleBreakWithSupplierTest {
      class First {
        final Second second;

        @Inject
        First(Second second) {
          this.second = second;
        }
      }

      class Second {
        final Supplier<First> firstSupplier;

        @Inject
        Second(Supplier<First> firstSupplier) {
          this.firstSupplier = firstSupplier;
        }
      }

      @Test
      void cycleCanBeConstructedWithSupplier() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(First.class), "First cycle element must not cause problems");
        assertDoesNotThrow(
            () -> builder.add(Second.class), "Second cycle element must not cause problems");
        final var injector =
            assertDoesNotThrow(
                builder::build, "There must be no problem with constructing injector");

        assertNotNull(
            injector.get(First.class).second.firstSupplier.get(),
            "Cycle broken with Supplier must result in correctly injected non-null value");
      }
    }

    @Nested
    class OneClassCycleTest {
      class SelfCycle {
        final SelfCycle selfCycle;

        @Inject
        SelfCycle(SelfCycle selfCycle) {
          this.selfCycle = selfCycle;
        }
      }

      @Test
      void throwsExceptionForCycle() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(SelfCycle.class),
            "Self cycle element must throw an exception");
      }
    }

    @Nested
    class ThreeClassesCycleTest {
      class First {
        final Second second;

        @Inject
        First(Second second) {
          this.second = second;
        }
      }

      class Second {
        final Third third;

        @Inject
        Second(Third third) {
          this.third = third;
        }
      }

      class Third {
        final First first;

        @Inject
        Third(First first) {
          this.first = first;
        }
      }

      @Test
      void throwsExceptionForCycle123() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(First.class), "First cycle element must not cause problems");
        assertDoesNotThrow(
            () -> builder.add(Second.class), "Second cycle element must not cause problems");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Third.class),
            "Third cycle element must throw an exception");
      }

      @Test
      void throwsExceptionForCycle132() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(First.class), "First cycle element must not cause problems");
        assertDoesNotThrow(
            () -> builder.add(Third.class), "Second cycle element must not cause problems");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Second.class),
            "Third cycle element must throw an exception");
      }

      @Test
      void throwsExceptionForCycle213() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(Second.class), "First cycle element must not cause problems");
        assertDoesNotThrow(
            () -> builder.add(First.class), "Second cycle element must not cause problems");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Third.class),
            "Third cycle element must throw an exception");
      }

      @Test
      void throwsExceptionForCycle231() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(Second.class), "First cycle element must not cause problems");
        assertDoesNotThrow(
            () -> builder.add(Third.class), "Second cycle element must not cause problems");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(First.class),
            "Third cycle element must throw an exception");
      }

      @Test
      void throwsExceptionForCycle312() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(Third.class), "First cycle element must not cause problems");
        assertDoesNotThrow(
            () -> builder.add(First.class), "Second cycle element must not cause problems");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Second.class),
            "Third cycle element must throw an exception");
      }

      @Test
      void throwsExceptionForCycle321() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(Third.class), "First cycle element must not cause problems");
        assertDoesNotThrow(
            () -> builder.add(Second.class), "Second cycle element must not cause problems");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(First.class),
            "Third cycle element must throw an exception");
      }
    }

    @Nested
    class TwoClassesCycleTest {
      class First {
        final Second second;

        @Inject
        First(Second second) {
          this.second = second;
        }
      }

      class Second {
        final First first;

        @Inject
        Second(First first) {
          this.first = first;
        }
      }

      @Test
      void throwsExceptionForCycle() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(First.class), "First cycle element must not cause problems");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Second.class),
            "Second cycle element must throw an exception");
      }

      @Test
      void throwsExceptionForCycleRegardlessOfTheDeclarationOrder() {
        final var builder = Injector.injector();

        assertDoesNotThrow(
            () -> builder.add(Second.class), "First cycle element must not cause problems");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(First.class),
            "Second cycle element must throw an exception");
      }
    }
  }

  @Nested
  class Features {
    @Nested
    class CustomQualifierTest {
      class DependencyFactory {
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

      class NamedConsumer {
        private final String value;

        @Inject
        NamedConsumer(@Named("named") String value) {
          this.value = value;
        }
      }

      class CustomConsumer {
        private final String value;

        @Inject
        CustomConsumer(@CustomQualifier String value) {
          this.value = value;
        }
      }

      @Test
      void invokesFactoryMethodToProvideDependencies() {
        final var builder = Injector.injector();
        assertDoesNotThrow(
            () -> builder.add(DependencyFactory.class),
            "There must be no problem with adding dependency factory");
        assertDoesNotThrow(
            () -> builder.add(NamedConsumer.class),
            "There must be no problem with adding named consumer");
        assertDoesNotThrow(
            () -> builder.add(CustomConsumer.class),
            "There must be no problem with adding custom consumer");
        final var injector =
            assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

        assertTrue(
            injector.get(NamedConsumer.class).value.startsWith("named"),
            "Named consumer should have named dependency injected");
        assertTrue(
            injector.get(CustomConsumer.class).value.startsWith("custom"),
            "Custom consumer should have dependency marked with custom qualifier injected");
      }
    }
  }

  @Nested
  class Inheritance {
    @Nested
    class ConstructorAnnotationsInheritanceTest {
      class ParentConsumer {
        private final Long value;

        @Inject
        ParentConsumer(Long value) {
          this.value = value;
        }

        public Long getValue() {
          return value;
        }
      }

      class ChildConsumer extends ParentConsumer {
        ChildConsumer(Long value) {
          super(value);
        }
      }

      @Test
      void mustThrowException() {
        final var value = System.nanoTime();

        final var builder = Injector.injector();
        assertDoesNotThrow(() -> builder.add(value), "There must be no problem adding value");
        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(ChildConsumer.class),
            "Because child class must use super constructor in one of its constructors, child classes without constructors explicitly marked by @Inject must throw an exception");
      }
    }

    @Nested
    class FactoryInheritanceTest {
      class DependencyFactory {
        @Provides
        @Named("original")
        String original() {
          return "original" + System.nanoTime();
        }
      }

      class ChildDependencyFactory extends DependencyFactory {
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
      void invokesFactoryMethodToProvideDependencies() {
        final var builder = Injector.injector();
        assertDoesNotThrow(
            () -> builder.add(ChildDependencyFactory.class),
            "There must be no problem with adding child dependency factory");
        assertDoesNotThrow(
            () -> builder.add(Consumer.class), "There must be no problem with adding consumer");
        final var injector =
            assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

        final var consumer = injector.get(Consumer.class);
        assertTrue(
            consumer.original.startsWith("original"),
            "Consumer must have original dependency injected from original factory");
        assertTrue(
            consumer.additional.startsWith("additional"),
            "Consumer must have additional dependency injected from child factory");
      }
    }
  }

  @Nested
  class Limitations {
    @Nested
    class NoCollectionInjectionInFieldTest {
      class Consumer {
        @Inject private Collection<String> strings;
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Collections are not supported for injection");
      }
    }

    @Nested
    class NoCollectionInjectionTest {
      class Consumer {
        private final Collection<String> strings;

        @Inject
        Consumer(Collection<String> strings) {
          this.strings = strings;
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Collections are not supported for injection");
      }
    }

    @Nested
    class NoCollectionProviderTest {
      class DependencyFactory {
        @Provides
        Collection<String> collection() {
          return Set.of("test1", "test2");
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(DependencyFactory.class),
            "Collections are not supported for exposure");
      }
    }

    @Nested
    class NoMapInjectionInFieldTest {
      class Consumer {
        @Inject private Map<String, String> strings;
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Maps are not supported for injection");
      }
    }

    @Nested
    class NoMapInjectionTest {
      class Consumer {
        private final Map<String, String> strings;

        @Inject
        Consumer(Map<String, String> strings) {
          this.strings = strings;
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Maps are not supported for injection");
      }
    }

    @Nested
    class NoMapProviderTest {
      class DependencyFactory {
        @Provides
        Map<String, String> collection() {
          return Map.of("test1", "test2");
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(DependencyFactory.class),
            "Maps are not supported for exposure");
      }
    }

    @Nested
    class NoProviderCollectionInjectionTest {
      class Consumer {
        private final Provider<Collection<String>> strings;

        @Inject
        Consumer(Provider<Collection<String>> strings) {
          this.strings = strings;
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Provider wrapping Collection is not supported for injection");
      }
    }

    @Nested
    class NoProviderMapInjectionTest {
      class Consumer {
        private final Provider<Map<String, String>> strings;

        @Inject
        Consumer(Provider<Map<String, String>> strings) {
          this.strings = strings;
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Provider wrapping Map is not supported for injection");
      }
    }

    @Nested
    class NoSuitableConstructorTest {
      class Consumer {
        private final String value;

        Consumer(String value) {
          this.value = value;
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Class without default or @Inject constructor must not be supported");
      }
    }

    @Nested
    class NoSupplierCollectionInjectionTest {
      class Consumer {
        private final Supplier<Collection<String>> strings;

        @Inject
        Consumer(Supplier<Collection<String>> strings) {
          this.strings = strings;
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Supplier wrapping Collection is not supported for injection");
      }
    }

    @Nested
    class NoSupplierMapInjectionTest {
      class Consumer {
        private final Supplier<Map<String, String>> strings;

        @Inject
        Consumer(Supplier<Map<String, String>> strings) {
          this.strings = strings;
        }
      }

      @Test
      void mustThrowException() {
        final var builder = Injector.injector();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.add(Consumer.class),
            "Supplier wrapping Map is not supported for injection");
      }
    }

    @Nested
    class NoTransitiveResolutionTest {
      class First {
        final Second second;

        @Inject
        First(Second second) {
          this.second = second;
        }
      }

      class Second {
        final Third third;

        @Inject
        Second(Third third) {
          this.third = third;
        }
      }

      class Third {}

      @Test
      void doesNotResolveTransitiveDependenciesImplicitly() {
        final var builder = Injector.injector();
        assertDoesNotThrow(
            () -> builder.add(First.class),
            "There must be no problem with adding first dependency");
        assertThrows(
            IllegalArgumentException.class,
            builder::build,
            "There must be an exception, because Injector does not support transitive dependencies");
      }

      @Test
      void worksWhenTransitiveDependenciesDefinedExplicitly() {
        final var builder = Injector.injector();
        assertDoesNotThrow(
            () -> builder.add(First.class),
            "There must be no problem with adding first dependency");
        assertDoesNotThrow(
            () -> builder.add(Second.class),
            "There must be no problem with adding second dependency");
        assertDoesNotThrow(
            () -> builder.add(Third.class),
            "There must be no problem with adding third dependency");
        final var injector =
            assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

        assertNotNull(
            injector.get(First.class).second.third, "All dependencies must be instantiated");
      }
    }
  }

  @Nested
  class Providing {

    @Nested
    class Factories {
      @Nested
      class FactoryProviderAdjustmentTest {
        class DependencyProvider {
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
        void invokesFactoryMethodToProvideDependencies() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(DependencyProvider.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(Consumer.class), "There must be no problem with adding consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertEquals(
              "originalAdjusted",
              injector.get(Consumer.class).result,
              "Factory methods within the same class must be able to be invoked independently to provide the opportunity to chain and adjust dependencies as needed");
        }
      }

      @Nested
      class FactoryProviderConsumeAndAdjustTest {
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
        void adjustingFactoryShouldParticipateInCreationChain() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(Original.class),
              "There must be no problem with adding original dependency factory");
          assertDoesNotThrow(
              () -> builder.add(Adjuster.class),
              "There must be no problem with adding adjusting dependency factory");
          assertDoesNotThrow(
              () -> builder.add(Consumer.class), "There must be no problem with adding consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertEquals(
              "originalAdjusted",
              injector.get(Consumer.class).result,
              "Factory methods must be able to be recognized within consuming classes to provide the opportunity to chain and adjust dependencies as needed");
        }
      }

      @Nested
      class FactoryProviderNamedTest {
        class DependencyFactory {
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

        class NonNamedConsumer {
          private final String value;

          @Inject
          NonNamedConsumer(String value) {
            this.value = value;
          }
        }

        class NamedConsumer {
          private final String value;

          @Inject
          NamedConsumer(@Named("named") String value) {
            this.value = value;
          }
        }

        @Test
        void invokesFactoryMethodToProvideDependencies() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(DependencyFactory.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(NonNamedConsumer.class),
              "There must be no problem with adding non named consumer");
          assertDoesNotThrow(
              () -> builder.add(NamedConsumer.class),
              "There must be no problem with adding named consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertTrue(
              injector.get(NonNamedConsumer.class).value.startsWith("simple"),
              "Non named consumer should have non named dependency injected");
          assertTrue(
              injector.get(NamedConsumer.class).value.startsWith("named"),
              "Named consumer should have named dependency injected");
          assertNotEquals(
              injector.get(NonNamedConsumer.class).value,
              injector.get(NonNamedConsumer.class).value,
              "Because factory provides non-singleton dependency, injected non named values must be different");
          assertNotEquals(
              injector.get(NamedConsumer.class).value,
              injector.get(NamedConsumer.class).value,
              "Because factory provides non-singleton dependency, injected named values must be different");
        }
      }

      @Nested
      class FactoryProviderPolymorphicNamedTest {
        class FirstDescendant implements Polymorphic {
          private final Long value;

          public FirstDescendant() {
            this.value = System.nanoTime();
          }

          @Override
          public String getValue() {
            return "first" + value;
          }
        }

        class SecondDescendant implements Polymorphic {
          private final Long value;

          public SecondDescendant() {
            this.value = System.nanoTime();
          }

          @Override
          public String getValue() {
            return "second" + value;
          }
        }

        class DependencyProvider {
          @Provides
          @Named("firstDescendant")
          Polymorphic firstDescendant() {
            return new FirstDescendant();
          }

          @Provides
          @Named("secondDescendant")
          Polymorphic secondDescendant() {
            return new SecondDescendant();
          }
        }

        class FirstConsumer {
          private final Polymorphic polymorphic;

          @Inject
          FirstConsumer(@Named("firstDescendant") Polymorphic polymorphic) {
            this.polymorphic = polymorphic;
          }
        }

        class SecondConsumer {
          private final Polymorphic polymorphic;

          @Inject
          SecondConsumer(@Named("secondDescendant") Polymorphic polymorphic) {
            this.polymorphic = polymorphic;
          }
        }

        @Test
        void invokesFactoryMethodToProvideDependencies() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(DependencyProvider.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(FirstConsumer.class),
              "There must be no problem with adding first consumer");
          assertDoesNotThrow(
              () -> builder.add(SecondConsumer.class),
              "There must be no problem with adding second consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertTrue(
              injector.get(FirstConsumer.class).polymorphic.getValue().startsWith("first"),
              "First consumer should have first polymorphic dependency injected");
          assertTrue(
              injector.get(SecondConsumer.class).polymorphic.getValue().startsWith("second"),
              "Second consumer should have second polymorphic dependency injected");
        }
      }

      @Nested
      class FactoryProviderPolymorphicTest {
        class FirstDescendant implements Polymorphic {
          private final Long value;

          public FirstDescendant() {
            this.value = System.nanoTime();
          }

          @Override
          public String getValue() {
            return "first" + value;
          }
        }

        class SecondDescendant implements Polymorphic {
          private final Long value;

          public SecondDescendant() {
            this.value = System.nanoTime();
          }

          @Override
          public String getValue() {
            return "second" + value;
          }
        }

        class DependencyProvider {
          @Provides
          Polymorphic firstDescendant() {
            return new FirstDescendant();
          }

          @Provides
          Polymorphic secondDescendant() {
            return new SecondDescendant();
          }
        }

        @Test
        void invokesFactoryMethodToProvideDependencies() {
          final var builder = Injector.injector();
          assertThrows(
              IllegalArgumentException.class,
              () -> builder.add(DependencyProvider.class),
              "Non distinguishable polymorphic dependencies are not supported");
        }
      }

      @Nested
      class FactoryProviderTest {
        class DependencyFactory {
          @Provides
          Long dependency() {
            return System.nanoTime();
          }
        }

        class FirstConsumer {
          private final Long value;

          @Inject
          FirstConsumer(Long value) {
            this.value = value;
          }
        }

        class SecondConsumer {
          private final Long value;

          @Inject
          SecondConsumer(Long value) {
            this.value = value;
          }
        }

        @Test
        void invokesFactoryMethodToProvideDependencies() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(DependencyFactory.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(FirstConsumer.class),
              "There must be no problem with adding first consumer");
          assertDoesNotThrow(
              () -> builder.add(SecondConsumer.class),
              "There must be no problem with adding second consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertNotEquals(
              injector.get(FirstConsumer.class).value,
              injector.get(SecondConsumer.class).value,
              "Because factory provides non-singleton dependency, injected values must be different");
        }
      }

      @Nested
      class FactorySingletonProviderNamedTest {
        class DependencyFactory {
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

        class NonNamedConsumer {
          private final String value;

          @Inject
          NonNamedConsumer(String value) {
            this.value = value;
          }
        }

        class NamedConsumer {
          private final String value;

          @Inject
          NamedConsumer(@Named("named") String value) {
            this.value = value;
          }
        }

        @Test
        void invokesFactoryMethodToProvideDependencies() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(DependencyFactory.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(NonNamedConsumer.class),
              "There must be no problem with adding non named consumer");
          assertDoesNotThrow(
              () -> builder.add(NamedConsumer.class),
              "There must be no problem with adding named consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertTrue(
              injector.get(NonNamedConsumer.class).value.startsWith("simple"),
              "Non named consumer should have non named dependency injected");
          assertTrue(
              injector.get(NamedConsumer.class).value.startsWith("named"),
              "Named consumer should have named dependency injected");
          assertEquals(
              injector.get(NonNamedConsumer.class).value,
              injector.get(NonNamedConsumer.class).value,
              "Because factory provides singleton dependency, injected non named values must be equal");
          assertEquals(
              injector.get(NamedConsumer.class).value,
              injector.get(NamedConsumer.class).value,
              "Because factory provides singleton dependency, injected named values must be equal");
        }
      }

      @Nested
      class FactorySingletonProviderTest {
        class DependencyFactory {
          @Provides
          @Singleton
          Long dependency() {
            return System.nanoTime();
          }
        }

        class FirstConsumer {
          private final Long value;

          @Inject
          FirstConsumer(Long value) {
            this.value = value;
          }
        }

        class SecondConsumer {
          private final Long value;

          @Inject
          SecondConsumer(Long value) {
            this.value = value;
          }
        }

        @Test
        void invokesFactoryMethodToProvideDependencies() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(DependencyFactory.class),
              "There must be no problem with adding dependency factory");
          assertDoesNotThrow(
              () -> builder.add(FirstConsumer.class),
              "There must be no problem with adding first consumer");
          assertDoesNotThrow(
              () -> builder.add(SecondConsumer.class),
              "There must be no problem with adding second consumer");
          final var injector =
              assertDoesNotThrow(
                  builder::build, "There must be no problem with constructing injector");

          assertEquals(
              injector.get(FirstConsumer.class).value,
              injector.get(SecondConsumer.class).value,
              "Because factory provides singleton dependency, injected values must be equal");
        }
      }
    }

    @Nested
    class Objects {

      @Nested
      class ClassTest {
        class FirstConsumer {
          private final NanoTimeValue value;

          @Inject
          FirstConsumer(NanoTimeValue value) {
            this.value = value;
          }
        }

        class SecondConsumer {
          private final NanoTimeValue value;

          @Inject
          SecondConsumer(NanoTimeValue value) {
            this.value = value;
          }
        }

        @Test
        void invokesClassConstructorToProvideDependencies() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(NanoTimeValue.class), "There must be no problem with adding value");
          assertDoesNotThrow(
              () -> builder.add(FirstConsumer.class),
              "There must be no problem with adding first consumer");
          assertDoesNotThrow(
              () -> builder.add(SecondConsumer.class),
              "There must be no problem with adding second consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertNotEquals(
              injector.get(FirstConsumer.class).value,
              injector.get(SecondConsumer.class).value,
              "Because factory provides non-singleton dependency, injected values must be different");
        }
      }

      @Nested
      class SingletonClassTest {
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
            return java.util.Objects.equals(value, value1.value);
          }

          @Override
          public int hashCode() {
            return java.util.Objects.hashCode(value);
          }
        }

        class FirstConsumer {
          private final Value value;

          @Inject
          FirstConsumer(Value value) {
            this.value = value;
          }
        }

        class SecondConsumer {
          private final Value value;

          @Inject
          SecondConsumer(Value value) {
            this.value = value;
          }
        }

        @Test
        void invokesClassConstructorToProvideDependencies() {
          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(Value.class), "There must be no problem with adding value");
          assertDoesNotThrow(
              () -> builder.add(FirstConsumer.class),
              "There must be no problem with adding first consumer");
          assertDoesNotThrow(
              () -> builder.add(SecondConsumer.class),
              "There must be no problem with adding second consumer");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertEquals(
              injector.get(FirstConsumer.class).value,
              injector.get(SecondConsumer.class).value,
              "Because factory provides singleton dependency, injected values must be equal");
        }
      }
    }

    @Nested
    class Values {
      @Nested
      class ValueDependencyTest {
        class FirstConsumer {
          private final NanoTimeValue value;

          @Inject
          FirstConsumer(NanoTimeValue value) {
            this.value = value;
          }
        }

        @Test
        void providesAndInjectsValues() {
          final var value = new NanoTimeValue();

          final var builder = Injector.injector();
          assertDoesNotThrow(
              () -> builder.add(value),
              "There must be no problem with providing values as dependencies");
          assertDoesNotThrow(
              () -> builder.add(FirstConsumer.class),
              "There must be no problem with adding classes which inject values");
          final var injector =
              assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

          assertEquals(
              value, injector.get(NanoTimeValue.class), "Value must not be changed by injector");
          assertEquals(
              value,
              injector.get(FirstConsumer.class).value,
              "Injected Value must be the same as the provided Value");
        }
      }
    }
  }

  @Nested
  class Replacements {
    @Nested
    class ReplaceAndInjectInConstructorTest {
      class OriginalDependencyFactory {
        @Provides
        NanoTimeValue nanoTimeValue() {
          return new NanoTimeValue("original");
        }
      }

      class ReplacementDependencyFactory extends OriginalDependencyFactory {
        @Override
        NanoTimeValue nanoTimeValue() {
          return new NanoTimeValue("replacement");
        }
      }

      class Consumer {
        private final NanoTimeValue value;

        @Inject
        Consumer(NanoTimeValue value) {
          this.value = value;
        }
      }

      @Test
      void mustReplaceDependency() {
        final var builder = Injector.injector();
        assertDoesNotThrow(
            () -> builder.add(OriginalDependencyFactory.class),
            "There must be no problem adding dependency factory");
        assertDoesNotThrow(
            () -> builder.add(Consumer.class), "There must be no problem adding consumer");
        final var injector =
            assertDoesNotThrow(builder::build, "There must be no problem creating injector");

        final var consumer =
            assertDoesNotThrow(
                () -> injector.get(Consumer.class), "There must be no problem retrieving consumer");
        assertTrue(
            consumer.value.getValue().startsWith("original"),
            "Original value must start with expected prefix");

        final var copyBuilder = injector.copy();
        assertDoesNotThrow(
            () ->
                copyBuilder.replace(
                    OriginalDependencyFactory.class, ReplacementDependencyFactory.class),
            "There must be no problem replacing dependency factory");
        final var copyInjector =
            assertDoesNotThrow(copyBuilder::build, "There must be no problem creating injector");

        final var replacedFirstConsumer =
            assertDoesNotThrow(
                () -> copyInjector.get(Consumer.class),
                "There must be no problem retrieving consumer");
        assertTrue(
            replacedFirstConsumer.value.getValue().startsWith("replacement"),
            "Replacement value must start with expected prefix");
      }
    }

    @Nested
    class ReplaceAndInjectInFieldTest {
      class OriginalDependencyFactory {
        @Provides
        NanoTimeValue nanoTimeValue() {
          return new NanoTimeValue("original");
        }
      }

      class ReplacementDependencyFactory extends OriginalDependencyFactory {
        @Override
        NanoTimeValue nanoTimeValue() {
          return new NanoTimeValue("replacement");
        }
      }

      class Consumer {
        @Inject private NanoTimeValue value;
      }

      @Test
      void mustReplaceDependency() {
        final var builder = Injector.injector();
        assertDoesNotThrow(
            () -> builder.add(OriginalDependencyFactory.class),
            "There must be no problem adding dependency factory");
        assertDoesNotThrow(
            () -> builder.add(Consumer.class), "There must be no problem adding consumer");
        final var injector =
            assertDoesNotThrow(builder::build, "There must be no problem creating injector");

        final var consumer =
            assertDoesNotThrow(
                () -> injector.get(Consumer.class), "There must be no problem retrieving consumer");
        assertTrue(
            consumer.value.getValue().startsWith("original"),
            "Original value must start with expected prefix");

        final var copyBuilder = injector.copy();
        assertDoesNotThrow(
            () ->
                copyBuilder.replace(
                    OriginalDependencyFactory.class, ReplacementDependencyFactory.class),
            "There must be no problem replacing dependency factory");
        final var copyInjector =
            assertDoesNotThrow(copyBuilder::build, "There must be no problem creating injector");

        final var replacedFirstConsumer =
            assertDoesNotThrow(
                () -> copyInjector.get(Consumer.class),
                "There must be no problem retrieving consumer");
        assertTrue(
            replacedFirstConsumer.value.getValue().startsWith("replacement"),
            "Replacement value must start with expected prefix");
      }
    }

    @Nested
    class ReplaceAndInjectValueDependencyTest {
      class OriginalNanoTimeValue extends NanoTimeValue {
        public OriginalNanoTimeValue() {
          this("original");
        }

        public OriginalNanoTimeValue(String prefix) {
          super(prefix);
        }
      }

      class ReplacementNanoTimeValue extends OriginalNanoTimeValue {
        public ReplacementNanoTimeValue() {
          super("replacement");
        }
      }

      class Consumer {
        private final OriginalNanoTimeValue value;

        @Inject
        Consumer(OriginalNanoTimeValue value) {
          this.value = value;
        }
      }

      @Test
      void mustInjectReplacedDependency() {
        final var original = new OriginalNanoTimeValue();
        final var replacement = new ReplacementNanoTimeValue();

        final var builder = Injector.injector();
        assertDoesNotThrow(() -> builder.add(original), "There must be no problem adding value");
        assertDoesNotThrow(
            () -> builder.add(Consumer.class), "There must be no problem adding consumer");
        final var injector =
            assertDoesNotThrow(builder::build, "There must be no problem creating injector");

        final var originalConsumer =
            assertDoesNotThrow(
                () -> injector.get(Consumer.class), "There must be no problem retrieving consumer");
        assertEquals(original, originalConsumer.value, "The injected value must be original value");

        final var copyBuilder = injector.copy();
        assertDoesNotThrow(
            () -> copyBuilder.replace(original, replacement),
            "There must be no problem replacing value");
        final var copyInjector =
            assertDoesNotThrow(copyBuilder::build, "There must be no problem creating injector");

        final var replacedConsumer =
            assertDoesNotThrow(
                () -> copyInjector.get(Consumer.class),
                "There must be no problem retrieving consumer");
        assertEquals(
            replacement, replacedConsumer.value, "The injected value must be replacement value");
      }
    }
  }
}
