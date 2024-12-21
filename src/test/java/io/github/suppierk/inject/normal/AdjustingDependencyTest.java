package io.github.suppierk.inject.normal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.suppierk.inject.Injector;
import io.github.suppierk.inject.Provides;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.junit.jupiter.api.Test;

class AdjustingDependencyTest {
  static class Original {
    @Provides
    @Named("original")
    String original() {
      return "original";
    }
  }

  static class Adjuster {
    private final String original;

    @Inject
    Adjuster(@Named("original") String original) {
      this.original = original;
    }

    @Provides
    @Named("adjusted")
    String adjusted() {
      return original + "Adjusted";
    }
  }

  static class Consumer {
    private final String result;

    @Inject
    Consumer(@Named("adjusted") String adjusted) {
      this.result = adjusted;
    }
  }

  @Test
  void adjusting_module_should_participate_in_creation_chain() {
    final var builder = Injector.injector();
    assertDoesNotThrow(() -> builder.add(Original.class));
    assertDoesNotThrow(() -> builder.add(Adjuster.class));
    assertDoesNotThrow(() -> builder.add(Consumer.class));
    final var injector = assertDoesNotThrow(builder::build);

    assertEquals("originalAdjusted", injector.get(Consumer.class).result);
  }
}
