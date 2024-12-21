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
  void indent_should_not_accept_negative_value() {
    assertThrows(IllegalArgumentException.class, () -> ConsoleConstants.indent(-1));
  }

  @Test
  void zero_indent_must_be_an_empty_string() {
    assertEquals(ConsoleConstants.EMPTY, ConsoleConstants.indent(0));
  }

  @Test
  void multiple_indents_must_be_as_expected() {
    assertEquals(ConsoleConstants.YAML_INDENT.repeat(2), ConsoleConstants.indent(2));
  }

  @Test
  void yellow_should_add_ansi_color_and_reset() {
    final var colored = ConsoleConstants.yellow("test");
    assertTrue(colored.startsWith(ConsoleConstants.YELLOW));
    assertTrue(colored.endsWith(ConsoleConstants.RESET));
  }

  @Test
  void blue_bold_should_add_ansi_color_and_reset() {
    final var colored = ConsoleConstants.blueBold("test");
    assertTrue(colored.startsWith(ConsoleConstants.BLUE_BOLD));
    assertTrue(colored.endsWith(ConsoleConstants.RESET));
  }

  @Test
  void cyan_bold_should_add_ansi_color_and_reset() {
    final var colored = ConsoleConstants.cyanBold("test");
    assertTrue(colored.startsWith(ConsoleConstants.CYAN_BOLD));
    assertTrue(colored.endsWith(ConsoleConstants.RESET));
  }
}
