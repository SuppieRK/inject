/*
 * MIT License
 *
 * Copyright 2025 Roman Khlebnov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR
 * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class InjectorYamlRenderingTest {

  @Singleton
  static class Dependency {}

  static class StableAlpha {}

  static class StableBeta {}

  static class StableConsumer {
    @Inject
    StableConsumer(StableBeta beta) {}
  }

  static class FieldA {}

  static class FieldB {}

  static class FieldConsumer {
    @Inject FieldB fieldB;

    @Inject FieldA fieldA;
  }

  static class ChainLeaf {}

  static class ChainMiddle {
    @Inject
    ChainMiddle(ChainLeaf leaf) {}
  }

  static class ChainRoot {
    @Inject
    ChainRoot(ChainMiddle middle) {}
  }

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
  void sameGraphMustRenderSameYamlRegardlessOfRegistrationOrder() {
    final Injector first =
        Injector.injector().add(StableConsumer.class, StableAlpha.class, StableBeta.class).build();
    final Injector second =
        Injector.injector().add(StableBeta.class, StableConsumer.class, StableAlpha.class).build();

    assertEquals(first.toString(), second.toString(), "Equivalent graphs must render identically");
  }

  @Test
  void injectedFieldsMustRenderInStableOrder() {
    final Injector injector =
        Injector.injector().add(FieldConsumer.class, FieldB.class, FieldA.class).build();

    final String yaml = injector.toString();
    final int consumerIndex = yaml.indexOf(FieldConsumer.class.getName());
    final int fieldsIndex = yaml.indexOf("fields:", consumerIndex);
    final int fieldAIndex = yaml.indexOf(FieldA.class.getName(), fieldsIndex);
    final int fieldBIndex = yaml.indexOf(FieldB.class.getName(), fieldsIndex);

    assertTrue(fieldAIndex > fieldsIndex, () -> "Expected FieldA in consumer fields:\n" + yaml);
    assertTrue(fieldBIndex > fieldsIndex, () -> "Expected FieldB in consumer fields:\n" + yaml);
    assertTrue(fieldAIndex < fieldBIndex, () -> "Expected field YAML to be sorted:\n" + yaml);
  }

  @Test
  void dependenciesMustRenderBeforeDependents() {
    final Injector injector =
        Injector.injector().add(ChainRoot.class, ChainMiddle.class, ChainLeaf.class).build();

    final String yaml = injector.toString();
    final int leafIndex = yaml.indexOf(ChainLeaf.class.getName());
    final int middleIndex = yaml.indexOf(ChainMiddle.class.getName());
    final int rootIndex = yaml.indexOf(ChainRoot.class.getName());

    assertTrue(leafIndex >= 0, () -> "Expected ChainLeaf in YAML:\n" + yaml);
    assertTrue(middleIndex >= 0, () -> "Expected ChainMiddle in YAML:\n" + yaml);
    assertTrue(rootIndex >= 0, () -> "Expected ChainRoot in YAML:\n" + yaml);
    assertTrue(leafIndex < middleIndex, () -> "Expected leaf before middle:\n" + yaml);
    assertTrue(middleIndex < rootIndex, () -> "Expected middle before root:\n" + yaml);
  }

  @Test
  void injectorDependenciesAppearInYamlAndClose() {
    final Injector injector = Injector.injector().add(Dependency.class, Service.class).build();

    // Exercise resolution and ensure the injector self-reference works.
    final Service service = injector.get(Service.class);
    assertSame(service.injector, injector, "Service must receive the current injector instance");

    final String yaml = injector.toString();

    assertTrue(
        yaml.contains("Service"),
        () -> "Expected Service entry in injector graph, but got:\n" + yaml);

    injector.close();

    assertTrue(service.closed, "Service close must be invoked when injector is closed");
  }
}
