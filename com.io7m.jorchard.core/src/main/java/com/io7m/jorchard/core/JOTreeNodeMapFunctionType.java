/*
 * Copyright © 2016 <code@io7m.com> http://io7m.com
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
 * IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package com.io7m.jorchard.core;

/**
 * A function for traversing trees.
 *
 * @param <A> The type of tree values
 * @param <B> The type of returned values
 * @param <T> The type of threaded context values
 */

@FunctionalInterface
public interface JOTreeNodeMapFunctionType<A, T, B>
{
  /**
   * Visit a value in the tree.
   *
   * @param input The context value passed to the traversal
   * @param depth The depth of node relative to the starting node of the
   *              traversal
   * @param node  The current node
   *
   * @return A value of type {@code B}
   */

  B apply(
    T input,
    int depth,
    JOTreeNodeReadableType<A> node);
}
