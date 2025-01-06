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

package io.github.suppierk.inject.cookbook;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

@SuppressWarnings("unused")
class DeclareAndConsumeSingletonDependencyTest {
  @Singleton
  static class Value {
    private final long time;

    Value() {
      this.time = System.nanoTime();
    }

    public long time() {
      return time;
    }
  }

  static class Consumer {
    private final Value value;

    @Inject
    public Consumer(Value value) {
      this.value = value;
    }

    public long get() {
      return value.time();
    }
  }

  @Test
  void exampleMustWorkAsExpected() {
    final Injector injector = Injector.injector().add(Value.class).add(Consumer.class).build();

    assertEquals(
        injector.get(Consumer.class).get(),
        injector.get(Consumer.class).get(),
        "Because injected values are the same they must be equal");
  }
}
