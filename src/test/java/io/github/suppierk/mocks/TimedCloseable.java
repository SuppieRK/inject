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

package io.github.suppierk.mocks;

import java.io.Closeable;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Test utility class which records the time on nanoseconds when the object's {@link #close()}
 * method was called.
 */
public final class TimedCloseable implements Closeable {
  private final AtomicLong closeCalledNs;

  public TimedCloseable() {
    this.closeCalledNs = new AtomicLong(Long.MIN_VALUE);
  }

  public boolean wasCloseCalled() {
    return closeCalledNs.get() != Long.MIN_VALUE;
  }

  @Override
  public void close() {
    closeCalledNs.compareAndSet(Long.MIN_VALUE, System.nanoTime());
  }
}
