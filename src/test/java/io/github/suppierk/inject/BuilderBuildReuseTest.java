package io.github.suppierk.inject;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Test;

class BuilderBuildReuseTest {
  static class CapturesInjector {
    final Injector injector;

    @Inject
    CapturesInjector(Injector injector) {
      this.injector = injector;
    }
  }

  @Singleton
  static class SingletonService {}

  @Test
  void separateBuildsUseTheirOwnInjectorReference() {
    final var builder = Injector.injector().add(CapturesInjector.class);

    final var first = builder.build();
    final var second = builder.build();

    assertSame(first, first.get(CapturesInjector.class).injector);
    assertSame(second, second.get(CapturesInjector.class).injector);
  }

  @Test
  void separateBuildsDoNotShareSingletonInstances() {
    final var builder = Injector.injector().add(SingletonService.class);

    final var first = builder.build();
    final var second = builder.build();

    assertNotSame(first.get(SingletonService.class), second.get(SingletonService.class));
  }

  @Test
  void separateCopyBuilderBuildsUseTheirOwnInjectorReference() {
    final var builder = Injector.injector().add(CapturesInjector.class).build().copy();

    final var first = builder.build();
    final var second = builder.build();

    assertSame(first, first.get(CapturesInjector.class).injector);
    assertSame(second, second.get(CapturesInjector.class).injector);
  }

  @Test
  void separateCopyBuilderBuildsDoNotShareSingletonInstances() {
    final var builder = Injector.injector().add(SingletonService.class).build().copy();

    final var first = builder.build();
    final var second = builder.build();

    assertNotSame(first.get(SingletonService.class), second.get(SingletonService.class));
  }
}
