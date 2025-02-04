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

package io.github.suppierk.inject.features;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Singleton;
import java.io.Closeable;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class SingletonClosureTest {
  static final AtomicLong SINGLETON_CLOSED_AT = new AtomicLong(Long.MIN_VALUE);
  static final AtomicLong CLOSED_AT = new AtomicLong(Long.MIN_VALUE);

  @Singleton
  static class TestSingleton implements Closeable {
    @Override
    public void close() {
      SINGLETON_CLOSED_AT.compareAndSet(Long.MIN_VALUE, System.nanoTime());
    }
  }

  static class TestNonSingleton implements Closeable {
    @Override
    public void close() {
      CLOSED_AT.compareAndSet(Long.MIN_VALUE, System.nanoTime());
    }
  }

  static class DependencyFactory {
    @Provides
    TestSingleton closeableSingleton() {
      return new TestSingleton();
    }

    @Provides
    TestNonSingleton closeableNonSingleton() {
      return new TestNonSingleton();
    }
  }

  @BeforeEach
  void setUp() {
    SINGLETON_CLOSED_AT.set(Long.MIN_VALUE);
    CLOSED_AT.set(Long.MIN_VALUE);
  }

  @Test
  void whenSingletonIsCloseableThenCloseShouldBeInvoked() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(TestSingleton.class, TestNonSingleton.class),
        "There must be no problem with adding dependencies");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertDoesNotThrow(
        () -> injector.get(TestSingleton.class),
        "There must be no problem with requesting singleton instance");
    assertDoesNotThrow(
        () -> injector.get(TestNonSingleton.class),
        "There must be no problem with requesting non singleton instance");

    assertDoesNotThrow(injector::close, "There must be no problem with closing injector");

    assertNotEquals(
        Long.MIN_VALUE,
        SINGLETON_CLOSED_AT.get(),
        "Singleton's close() method must have been invoked");
    assertEquals(
        Long.MIN_VALUE,
        CLOSED_AT.get(),
        "Non singleton's close() method must not be invoked, since Injector does not have a reference to the created object");
  }

  @Test
  void whenSingletonIsCloseableButWasNeverRequestedThenCloseShouldNotBeInvoked() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(TestSingleton.class, TestNonSingleton.class),
        "There must be no problem with adding dependencies");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertDoesNotThrow(injector::close, "There must be no problem with closing injector");

    assertEquals(
        Long.MIN_VALUE,
        SINGLETON_CLOSED_AT.get(),
        "Singleton's close() method must not be invoked, since singleton instance was never created");
    assertEquals(
        Long.MIN_VALUE,
        CLOSED_AT.get(),
        "Non singleton's close() method must not be invoked, since Injector does not have a reference to the created object");
  }

  @Test
  void whenFactorySingletonIsCloseableThenCloseShouldBeInvoked() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(DependencyFactory.class),
        "There must be no problem with adding dependency factory");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertDoesNotThrow(
        () -> injector.get(TestSingleton.class),
        "There must be no problem with requesting singleton instance");
    assertDoesNotThrow(
        () -> injector.get(TestNonSingleton.class),
        "There must be no problem with requesting non singleton instance");

    assertDoesNotThrow(injector::close, "There must be no problem with closing injector");

    assertNotEquals(
        Long.MIN_VALUE,
        SINGLETON_CLOSED_AT.get(),
        "Singleton's close() method must have been invoked");
    assertEquals(
        Long.MIN_VALUE,
        CLOSED_AT.get(),
        "Non singleton's close() method must not be invoked, since Injector does not have a reference to the created object");
  }

  @Test
  void whenFactorySingletonIsCloseableButWasNeverRequestedThenCloseShouldNotBeInvoked() {
    final var builder = Injector.injector();
    assertDoesNotThrow(
        () -> builder.add(DependencyFactory.class),
        "There must be no problem with adding dependency factory");
    final var injector =
        assertDoesNotThrow(builder::build, "There must be no problem with creating injector");

    assertDoesNotThrow(injector::close, "There must be no problem with closing injector");

    assertEquals(
        Long.MIN_VALUE,
        SINGLETON_CLOSED_AT.get(),
        "Singleton's close() method must not be invoked, since singleton instance was never created");
    assertEquals(
        Long.MIN_VALUE,
        CLOSED_AT.get(),
        "Non singleton's close() method must not be invoked, since Injector does not have a reference to the created object");
  }
}
