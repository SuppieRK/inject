/*
 * MIT License
 *
 * Copyright 2024 Roman Khlebnov
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

package io.github.suppierk.utils;

import jakarta.inject.Provider;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides simple implementation of the lazily computed value which gets evaluated once.
 *
 * <p>Can be used as both {@link Provider} and {@link Supplier} due to their contract equality
 * making this class useful for multiple scenarios.
 *
 * <p><b>NOTE</b>: due to the lazy nature of this value class it does not provide {@link
 * Object#equals(Object)} and {@link Object#hashCode()} implementations and <b>MUST NOT</b> be used
 * in {@link java.util.Set} or as a key in {@link java.util.Map}.
 *
 * @param <T> is the type of the value
 * @see <a href="https://en.wikipedia.org/wiki/Memoization">Memoization on Wikipedia</a>
 */
public final class Memoized<T> implements Provider<T>, Supplier<T> {
  private final Provider<T> provider;
  private final Lock lock;
  private final AtomicReference<T> value;

  /**
   * Default constructor.
   *
   * @param provider to invoke to compute the value
   * @throws IllegalArgumentException if {@link Provider} is {@code null}
   */
  private Memoized(Provider<T> provider) {
    if (provider == null) {
      throw new IllegalArgumentException("Provider is null");
    }

    this.provider = provider;
    this.lock = new ReentrantLock();
    this.value = new AtomicReference<>(null);
  }

  /**
   * Static factory method for {@link Memoized} class.
   *
   * @param provider to invoke to compute the value
   * @return new {@link Memoized} instance
   * @param <T> is the type of the value
   */
  public static <T> Memoized<T> memoizedProvider(Provider<T> provider) {
    return new Memoized<>(provider);
  }

  /**
   * Static factory method for {@link Memoized} class.
   *
   * @param supplier to invoke to compute the value
   * @return new {@link Memoized} instance
   * @param <T> is the type of the value
   * @throws IllegalArgumentException if {@link Supplier} is {@code null}
   */
  public static <T> Memoized<T> memoizedSupplier(Supplier<T> supplier) {
    if (supplier == null) {
      throw new IllegalArgumentException("Supplier is null");
    }

    return new Memoized<>(supplier::get);
  }

  /**
   * Double-checked locking with {@link Lock} instead of {@code synchronized} to facilitate its
   * usage on newer JVM versions affected by thread pinning problem.
   *
   * <p>Despite {@link AtomicReference}, we use explicit locking to solve the problem of avoiding
   * excessive {@link Provider} calls which would have occurred if we would use {@link
   * AtomicReference#compareAndSet(Object, Object)}.
   *
   * @return computed value
   * @throws IllegalArgumentException if {@link Provider} returns {@code null}
   * @see <a href="https://openjdk.org/jeps/491">JEP to fix thread pinning problem</a>
   */
  @Override
  public T get() {
    T localRef = value.get();

    if (localRef == null) {
      lock.lock();
      try {
        localRef = value.get();

        if (localRef == null) {
          final T computedValue = provider.get();

          if (computedValue == null) {
            throw new IllegalArgumentException("Provider returned null");
          }

          value.set(computedValue);
          localRef = computedValue;
        }
      } finally {
        lock.unlock();
      }
    }

    return localRef;
  }

  /**
   * Used in tests to determine that the lock is unlocked.
   *
   * @return {@code true} if there is some thread holding the lock
   */
  boolean isLockReleased() {
    return ((ReentrantLock) lock).getHoldCount() == 0;
  }

  /**
   * If a value is not evaluated, returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is not evaluated, otherwise {@code false}
   */
  public boolean isEmpty() {
    return value.get() == null;
  }

  /**
   * If a value is evaluated, returns {@code true}, otherwise {@code false}.
   *
   * @return {@code true} if a value is evaluated, otherwise {@code false}
   */
  public boolean isPresent() {
    return !isEmpty();
  }

  /**
   * If a value is evaluated, performs the given action with the value, otherwise does nothing.
   *
   * @param action the action to be performed if a value is evaluated
   * @throws NullPointerException if value is evaluated and the given action is {@code null}
   */
  public void ifPresent(Consumer<T> action) {
    if (isPresent()) {
      action.accept(value.get());
    }
  }

  /**
   * If a value is evaluated, performs the given action with the value, otherwise performs the given
   * empty-based action.
   *
   * @param action the action to be performed if a value is evaluated
   * @param emptyAction the empty-based action to be performed if no value is evaluated
   * @throws NullPointerException if a value is evaluated and the given action is {@code null}, or
   *     no value is evaluated and the given empty-based action is {@code null}.
   */
  public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
    if (isPresent()) {
      action.accept(value.get());
    } else {
      emptyAction.run();
    }
  }

  /**
   * If a value is evaluated, returns a sequential {@link Stream} containing only that value,
   * otherwise returns an empty {@code Stream}.
   *
   * @apiNote This method can be used to transform a {@code Stream} of memoized elements to a {@code
   *     Stream} of evaluated value elements:
   *     <pre>{@code
   * Stream<Memoized<T>> os = ..
   * Stream<T> s = os.flatMap(Memoized::stream)
   * }</pre>
   *
   * @return the memoized value as a {@code Stream}
   */
  public Stream<T> stream() {
    if (isEmpty()) {
      return Stream.empty();
    } else {
      return Stream.of(value.get());
    }
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return String.format("%s(%s)", getClass().getSimpleName(), value.get());
  }
}
