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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import org.junit.jupiter.api.Test;

class PostConstructAdjusterTest {
  static class Dependency {
    final String value = "dependency";
  }

  static class MissingDependency {}

  static class Adjustable {
    @Inject Dependency dependency;

    Injector injector;
    String value;
  }

  static class CountsAdjustments {
    int adjustmentCount;
  }

  @Singleton
  static class SingletonCountsAdjustments {
    int adjustmentCount;
  }

  static class ThrowsFromAdjuster {}

  static class RequiresHiddenDependency {
    MissingDependency missingDependency;
  }

  static class Base {}

  static class Replacement extends Base {
    boolean adjusted;
  }

  @Test
  void adjusterRunsAfterInjectionAndCanUseInjector() {
    final Injector injector =
        Injector.injector()
            .add(Dependency.class)
            .add(
                Adjustable.class,
                (currentInjector, adjustable) -> {
                  adjustable.injector = currentInjector;
                  adjustable.value = adjustable.dependency.value;
                })
            .build();

    final var adjustable = injector.get(Adjustable.class);

    assertNotNull(adjustable.dependency, "Field injection must happen before adjustment");
    assertSame(injector, adjustable.injector, "Adjuster must receive current injector");
    assertEquals("dependency", adjustable.value, "Adjuster must be able to tweak instance state");
  }

  @Test
  void nonSingletonAdjusterRunsForEveryCreatedInstance() {
    final var adjustments = new AtomicInteger();
    final Injector injector =
        Injector.injector()
            .add(
                CountsAdjustments.class,
                (currentInjector, instance) ->
                    instance.adjustmentCount = adjustments.incrementAndGet())
            .build();

    final var first = injector.get(CountsAdjustments.class);
    final var second = injector.get(CountsAdjustments.class);

    assertEquals(1, first.adjustmentCount, "First instance must be adjusted once");
    assertEquals(2, second.adjustmentCount, "Second instance must be adjusted once too");
  }

  @Test
  void singletonAdjusterRunsOnce() {
    final var adjustments = new AtomicInteger();
    final Injector injector =
        Injector.injector()
            .add(
                SingletonCountsAdjustments.class,
                (currentInjector, instance) ->
                    instance.adjustmentCount = adjustments.incrementAndGet())
            .build();

    final var first = injector.get(SingletonCountsAdjustments.class);
    final var second = injector.get(SingletonCountsAdjustments.class);

    assertSame(first, second, "Singleton instance must be reused");
    assertEquals(1, first.adjustmentCount, "Singleton must be adjusted once");
    assertEquals(1, adjustments.get(), "Adjuster must be invoked once");
  }

  @Test
  void adjusterExceptionsPropagateUnchanged() {
    final var expected = new UnsupportedOperationException("boom");
    final Injector injector =
        Injector.injector()
            .add(
                ThrowsFromAdjuster.class,
                (currentInjector, instance) -> {
                  throw expected;
                })
            .build();

    final var thrown =
        assertThrows(
            UnsupportedOperationException.class, () -> injector.get(ThrowsFromAdjuster.class));

    assertSame(expected, thrown, "Adjuster exception must propagate unchanged");
  }

  @Test
  void hiddenAdjusterDependenciesFailAtRuntime() {
    final Injector injector =
        Injector.injector()
            .add(
                RequiresHiddenDependency.class,
                (currentInjector, instance) ->
                    instance.missingDependency = currentInjector.get(MissingDependency.class))
            .build();

    assertThrows(
        NoSuchElementException.class,
        () -> injector.get(RequiresHiddenDependency.class),
        "Dependencies only used inside adjusters are runtime lookups");
  }

  @Test
  void addFailsForNullAdjuster() {
    final var builder = Injector.injector();

    assertThrows(
        IllegalArgumentException.class,
        () -> builder.add(Adjustable.class, (BiConsumer<Injector, Adjustable>) null),
        "Null adjuster must throw an exception");
  }

  @Test
  void replacementAdjusterRunsForReplacementClass() {
    final Injector original = Injector.injector().add(Base.class).build();
    final Injector replaced =
        original
            .copy()
            .replace(
                Base.class,
                Replacement.class,
                (injector, replacement) -> replacement.adjusted = true)
            .build();

    final var base = replaced.get(Base.class);
    final var replacement =
        assertInstanceOf(Replacement.class, base, "Base must refer to replacement");

    assertTrue(replacement.adjusted, "Replacement adjuster must run");
  }

  @Test
  void replaceFailsForNullAdjuster() {
    final Injector injector = Injector.injector().add(Base.class).build();
    final var copyBuilder = injector.copy();

    assertThrows(
        IllegalArgumentException.class,
        () -> copyBuilder.replace(Base.class, Replacement.class, null),
        "Null replacement adjuster must throw an exception");
  }
}
