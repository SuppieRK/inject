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
  void null_is_not_allowed() {
    assertThrows(IllegalArgumentException.class, () -> Memoized.memoizedProvider(null));
    assertThrows(IllegalArgumentException.class, () -> Memoized.memoizedSupplier(null));
  }

  @Test
  void null_is_not_returned() {
    assertNotNull(Memoized.memoizedProvider(() -> 0L));
    assertNotNull(Memoized.memoizedSupplier(() -> 0L));

    assertNotNull(Memoized.memoizedSupplier(() -> 0L).toString());
  }

  @Test
  void null_from_provider_causes_error() {
    final var nullProvider =
        new Provider<>() {
          @Override
          public Object get() {
            return null;
          }
        };

    final var memoized = Memoized.memoizedProvider(nullProvider);

    assertThrows(IllegalArgumentException.class, memoized::get);
  }

  @Test
  void lock_test() {
    final var memoized = Memoized.memoizedSupplier(() -> 1L);

    assertTrue(memoized.isLockReleased());

    memoized.get();

    assertTrue(memoized.isLockReleased());
  }

  @Test
  void multiple_threads_invoke_provider_once() throws InterruptedException {
    final var provider = new CountingProvider();
    final var memoized = Memoized.memoizedProvider(provider);

    assertEquals("Memoized(null)", memoized.toString());
    assertTrue(memoized.isEmpty());
    assertFalse(memoized.isPresent());

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

    assertEquals(1, provider.count.get());
    assertEquals("Memoized(1)", memoized.toString());
    assertFalse(memoized.isEmpty());
    assertTrue(memoized.isPresent());
  }

  @Test
  void if_present_test() {
    final var memoizedValue = new AtomicLong(Long.MIN_VALUE);
    final var memoized = Memoized.memoizedSupplier(() -> 1L);

    memoized.ifPresent(memoizedValue::set);
    assertNotEquals(1L, memoizedValue.get());

    memoizedValue.set(Long.MIN_VALUE);
    memoized.get();

    memoized.ifPresent(memoizedValue::set);
    assertEquals(1L, memoizedValue.get());
  }

  @Test
  void if_present_or_else_test() {
    final var memoizedValue = new AtomicLong(Long.MIN_VALUE);
    final var orElseInvoked = new AtomicBoolean(false);

    final var memoized = Memoized.memoizedSupplier(() -> 1L);

    memoized.ifPresentOrElse(memoizedValue::set, () -> orElseInvoked.set(true));
    assertNotEquals(1L, memoizedValue.get());
    assertTrue(orElseInvoked.get());

    memoizedValue.set(Long.MIN_VALUE);
    orElseInvoked.set(false);
    memoized.get();

    memoized.ifPresentOrElse(memoizedValue::set, () -> orElseInvoked.set(true));
    assertEquals(1L, memoizedValue.get());
    assertFalse(orElseInvoked.get());
  }

  @Test
  void stream_test() {
    final var memoized = Memoized.memoizedSupplier(() -> 1L);

    final var emptyStream = memoized.stream().collect(Collectors.toList());
    assertEquals(List.of(), emptyStream);

    memoized.get();

    final var evaluatedStream = memoized.stream().collect(Collectors.toList());
    assertEquals(List.of(1L), evaluatedStream);
  }
}
