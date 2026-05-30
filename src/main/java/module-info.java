/** Lightweight dependency injection module. */
open module io.github.suppierk.inject {
  requires jakarta.inject;
  requires static transitive org.jspecify;

  exports io.github.suppierk.inject;
  exports io.github.suppierk.utils;
  exports io.github.suppierk.inject.graph;
  exports io.github.suppierk.inject.query;
}
