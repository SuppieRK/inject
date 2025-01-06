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

package io.github.suppierk.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ConsoleConstantsTest {
  @Test
  void indentShouldNotAcceptNegativeValue() {
    assertThrows(
        IllegalArgumentException.class,
        () -> ConsoleConstants.indent(-1),
        "Negative indent must throw an exception");
  }

  @Test
  void zeroIndentMustBeAnEmptyString() {
    assertEquals(
        ConsoleConstants.EMPTY,
        ConsoleConstants.indent(0),
        "Zero indent must return an empty string");
  }

  @Test
  void multipleIndentsMustBeAsExpected() {
    assertEquals(
        ConsoleConstants.YAML_INDENT.repeat(2),
        ConsoleConstants.indent(2),
        "Positive indent must return expected number of spaces");
  }

  @Test
  void yellowShouldAddAnsiColorAndReset() {
    final var colored = ConsoleConstants.yellow("test");
    assertTrue(
        colored.startsWith(ConsoleConstants.YELLOW),
        "Colored string must start with expected ANSI code");
    assertTrue(colored.endsWith(ConsoleConstants.RESET), "Colored string must end with ANSI reset");
  }

  @Test
  void blueBoldShouldAddAnsiColorAndReset() {
    final var colored = ConsoleConstants.blueBold("test");
    assertTrue(
        colored.startsWith(ConsoleConstants.BLUE_BOLD),
        "Colored string must start with expected ANSI code");
    assertTrue(colored.endsWith(ConsoleConstants.RESET), "Colored string must end with ANSI reset");
  }

  @Test
  void cyanBoldShouldAddAnsiColorAndReset() {
    final var colored = ConsoleConstants.cyanBold("test");
    assertTrue(
        colored.startsWith(ConsoleConstants.CYAN_BOLD),
        "Colored string must start with expected ANSI code");
    assertTrue(colored.endsWith(ConsoleConstants.RESET), "Colored string must end with ANSI reset");
  }
}
