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

/**
 * Some console constants.
 *
 * @see <a href="https://stackoverflow.com/a/45444716">ANSI color options</a>
 */
public final class ConsoleConstants {
  public static final String EMPTY = "";

  // Indentation
  public static final String YAML_ITEM = "- ";
  public static final String YAML_EMPTY_ARRAY = " [ ]";
  static final String YAML_INDENT = "  ";

  // Reset
  static final String RESET = "\033[0m"; // Text Reset

  // Regular Colors
  static final String YELLOW = "\033[0;33m"; // YELLOW

  // Bold
  static final String BLUE_BOLD = "\033[1;34m"; // BLUE
  static final String CYAN_BOLD = "\033[1;36m"; // CYAN

  private ConsoleConstants() {
    // No instance
  }

  public static String indent(int level) {
    if (level < 0) {
      throw new IllegalArgumentException("Indentation level must be positive or zero");
    }

    if (level == 0) {
      return EMPTY;
    }

    return YAML_INDENT.repeat(level);
  }

  public static String yellow(String value) {
    return YELLOW + value + RESET;
  }

  public static String blueBold(String value) {
    return BLUE_BOLD + value + RESET;
  }

  public static String cyanBold(String value) {
    return CYAN_BOLD + value + RESET;
  }
}
