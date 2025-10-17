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

package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class InjectorYamlRenderingTest {

  static class Dependency {}

  @Singleton
  static class Service implements AutoCloseable {
    final Injector injector;
    final Dependency dependency;
    boolean closed;

    @Inject
    Service(Injector injector, Dependency dependency) {
      this.injector = injector;
      this.dependency = dependency;
    }

    @Override
    public void close() {
      closed = true;
    }
  }

  @Test
  void injectorDependenciesAppearInYamlAndClose() {
    final Injector injector = Injector.injector().add(Dependency.class, Service.class).build();

    // Exercise resolution and ensure the injector self-reference works.
    final Service service = injector.get(Service.class);
    assertTrue(service.injector == injector, "Service must receive the current injector instance");

    final String yaml = injector.toString();

    assertTrue(
        yaml.contains("Service"),
        () -> "Expected Service entry in injector graph, but got:\n" + yaml);

    injector.close();

    assertTrue(service.closed, "Service close must be invoked when injector is closed");
  }
}
