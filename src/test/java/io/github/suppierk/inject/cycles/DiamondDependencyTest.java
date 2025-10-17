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

package io.github.suppierk.inject.cycles;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class DiamondDependencyTest {
  @Singleton
  static class Shared {}

  static class Left {
    private final Shared shared;

    @Inject
    Left(Shared shared) {
      this.shared = shared;
    }

    Shared shared() {
      return shared;
    }
  }

  static class Right {
    private final Shared shared;

    @Inject
    Right(Shared shared) {
      this.shared = shared;
    }

    Shared shared() {
      return shared;
    }
  }

  static class Top {
    private final Left left;
    private final Right right;

    @Inject
    Top(Left left, Right right) {
      this.left = left;
      this.right = right;
    }

    Left left() {
      return left;
    }

    Right right() {
      return right;
    }
  }

  @Test
  void diamondDependencyDoesNotTriggerCycleDetection() {
    final var builder = Injector.injector();
    builder.add(Shared.class);
    builder.add(Left.class);
    builder.add(Right.class);
    builder.add(Top.class);

    final Injector injector = assertDoesNotThrow(builder::build);
    final Top top = injector.get(Top.class);

    assertNotNull(top.left(), "Left dependency must be injected");
    assertNotNull(top.right(), "Right dependency must be injected");
    assertSame(
        top.left().shared(),
        top.right().shared(),
        "Shared dependency must be reusable across branches");
  }
}
