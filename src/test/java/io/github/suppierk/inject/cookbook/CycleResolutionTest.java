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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import io.github.suppierk.inject.Injector;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class CycleResolutionTest {
  static class Hello implements Supplier<String> {
    private final World world;

    @Inject
    Hello(World world) {
      this.world = world;
    }

    @Override
    public String get() {
      return "Hello, ";
    }

    public String getComplete() {
      return get() + world.get();
    }
  }

  static class World implements Supplier<String> {
    private final Provider<Hello> helloProvider;

    @Inject
    public World(Provider<Hello> helloProvider) {
      this.helloProvider = helloProvider;
    }

    @Override
    public String get() {
      return "World!";
    }

    public String getComplete() {
      return helloProvider.get().get() + get();
    }
  }

  @Test
  void exampleMustWorkAsExpected() {
    final Injector injector = Injector.injector().add(Hello.class).add(World.class).build();

    assertNotEquals(
        injector.get(Hello.class).get(),
        injector.get(World.class).get(),
        "get values must be different");
    assertEquals(
        injector.get(Hello.class).getComplete(),
        injector.get(World.class).getComplete(),
        "getComplete values must be equal");
  }
}
