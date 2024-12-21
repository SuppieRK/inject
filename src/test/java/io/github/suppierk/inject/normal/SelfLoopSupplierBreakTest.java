package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.Objects;
import org.junit.jupiter.api.Test;

class SelfLoopSupplierBreakTest {
  @Singleton
  static class Loop {
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
