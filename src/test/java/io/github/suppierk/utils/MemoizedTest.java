package io.github.suppierk.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Provider;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class MemoizedTest {
  static class CountingProvider implements Provider<Long> {
    private final AtomicInteger count = new AtomicInteger(0);

    @Override
    public Long get() {
      count.incrementAndGet();
      return 1L;
    }
  }

  @Test
  void nullIsNotAllowed() {
    assertThrows(
        IllegalArgumentException.class,
        () -> Memoized.memoizedProvider(null),
        "Null provider must throw an exception");
    assertThrows(
        IllegalArgumentException.class,
        () -> Memoized.memoizedSupplier(null),
        "Null supplier must throw an exception");
  }

  @Test
  void nullIsNotReturned() {
    assertNotNull(
        Memoized.memoizedProvider(() -> 0L), "Memoized provider must return a non-null value");
    assertNotNull(
        Memoized.memoizedSupplier(() -> 0L), "Memoized supplier must return a non-null value");

    assertNotNull(Memoized.memoizedSupplier(() -> 0L).toString(), "String must not be null");
    assertFalse(
        Memoized.memoizedSupplier(() -> 0L).toString().isBlank(), "String must not be blank");
  }

  @Test
  void nullFromProviderCausesError() {
    final var nullProvider =
        new Provider<>() {
          @Override
          public Object get() {
            return null;
          }
        };

    final var memoized = Memoized.memoizedProvider(nullProvider);

    assertThrows(
        IllegalArgumentException.class, memoized::get, "Null return value must throw an exception");
  }

  @Test
  void lockTest() {
    final var memoized = Memoized.memoizedSupplier(() -> 1L);

    assertTrue(memoized.isLockReleased(), "Before value retrieval the lock must not be taken");

    memoized.get();

    assertTrue(memoized.isLockReleased(), "After value retrieval the lock must be released");
  }

  @Test
  void multipleThreadsInvokeProviderOnce() throws InterruptedException {
    final var provider = new CountingProvider();
    final var memoized = Memoized.memoizedProvider(provider);

    assertTrue(memoized.isEmpty(), "Before retrieval memoized must be empty");
    assertFalse(memoized.isPresent(), "Before retrieval memoized must not be present");

    Thread[] threads = new Thread[100];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new Thread(memoized::get);
    }

    for (Thread thread : threads) {
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    assertEquals(
        1,
        provider.count.get(),
        "During multithreaded retrieval value supplier must have been called once");
    assertFalse(memoized.isEmpty(), "After retrieval memoized must not be empty");
    assertTrue(memoized.isPresent(), "After retrieval memoized must be present");
  }

  @Test
  void ifPresentTest() {
    final var memoizedValue = new AtomicLong(Long.MIN_VALUE);
    final var memoized = Memoized.memoizedSupplier(() -> 1L);

    memoized.ifPresent(memoizedValue::set);
    assertNotEquals(
        1L, memoizedValue.get(), "Before value was requested ifPresent must not be invoked");

    memoizedValue.set(Long.MIN_VALUE);
    memoized.get();

    memoized.ifPresent(memoizedValue::set);
    assertEquals(1L, memoizedValue.get(), "After value was memoized ifPresent must be invoked");
  }

  @Test
  void ifPresentOrElseTest() {
    final var memoizedValue = new AtomicLong(Long.MIN_VALUE);
    final var orElseInvoked = new AtomicBoolean(false);

    final var memoized = Memoized.memoizedSupplier(() -> 1L);

    memoized.ifPresentOrElse(memoizedValue::set, () -> orElseInvoked.set(true));
    assertNotEquals(
        1L, memoizedValue.get(), "Before value was requested ifPresent part must not be invoked");
    assertTrue(orElseInvoked.get(), "Before value was requested orElse clause must be invoked");

    memoizedValue.set(Long.MIN_VALUE);
    orElseInvoked.set(false);
    memoized.get();

    memoized.ifPresentOrElse(memoizedValue::set, () -> orElseInvoked.set(true));
    assertEquals(
        1L, memoizedValue.get(), "After value was requested ifPresent part must be invoked");
    assertFalse(orElseInvoked.get(), "After value was requested orElse clause must not be invoked");
  }

  @Test
  void streamTest() {
    final var memoized = Memoized.memoizedSupplier(() -> 1L);

    final var emptyStream = memoized.stream().collect(Collectors.toList());
    assertEquals(
        List.of(), emptyStream, "Before value was requested stream must return an empty stream");

    memoized.get();

    final var evaluatedStream = memoized.stream().collect(Collectors.toList());
    assertEquals(
        List.of(1L),
        evaluatedStream,
        "After value was requested stream must return a single value stream");
  }
}
