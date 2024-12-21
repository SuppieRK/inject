package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

class SelfLoopTest {
  static class Loop {
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
