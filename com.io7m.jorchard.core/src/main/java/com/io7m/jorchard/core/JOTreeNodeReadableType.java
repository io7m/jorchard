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

import java.util.Collection;
import java.util.Optional;

/**
 * The type of readable tree nodes.
 *
 * @param <A> The type of values
 */

public interface JOTreeNodeReadableType<A>
{
  /**
   * @return {@code true} iff this node may be detached from its parent
   */

  boolean isDetachAllowed();

  /**
   * @return The value contained within the node
   */

  A value();

  /**
   * @return {@code true} iff this node is a root node
   */

  default boolean isRoot()
  {
    return !this.parentReadable().isPresent();
  }

  /**
   * @return A readable reference to the parent node
   */

  Optional<JOTreeNodeReadableType<A>> parentReadable();

  /**
   * @return A read-only collection containing the children of this node
   */

  Collection<JOTreeNodeReadableType<A>> childrenReadable();

  /**
   * @param other A node
   *
   * @return {@code true} iff this node is a descendant of {@code other}
   */

  boolean isDescendantOf(JOTreeNodeReadableType<A> other);

  /**
   * <p>Visit each node in the tree in depth-first order.</p>
   *
   * <p>The function allows for the use of a context value. This is useful for
   * avoiding the use of capturing lambdas, reducing GC pressure.</p>
   *
   * @param context A context value passed to each application of {@code f}
   * @param f       A function used to receive each node
   * @param <T>     The type of context values
   */

  <T> void forEachDepthFirst(
    T context,
    JOTreeNodeForEachFunctionType<A, T> f);

  /**
   * <p>Visit each node in the tree in breadth-first order.</p>
   *
   * <p>The function allows for the use of a context value. This is useful for
   * avoiding the use of capturing lambdas, reducing GC pressure.</p>
   *
   * @param context A context value passed to each application of {@code f}
   * @param f       A function used to receive each node
   * @param <T>     The type of context values
   */

  <T> void forEachBreadthFirst(
    T context,
    JOTreeNodeForEachFunctionType<A, T> f);

  /**
   * <p>Visit each node in the tree in depth-first order.</p>
   *
   * <p>The function allows for the use of a context value. This is useful for
   * avoiding the use of capturing lambdas, reducing GC pressure.</p>
   *
   * @param context A context value passed to each application of {@code f}
   * @param f       A function used to receive each node
   * @param <T>     The type of context values
   * @param <B>     The type of values in the returned tree
   *
   * @return A new structurally equal tree with nodes of type {@code B}
   */

  <T, B> JOTreeNodeType<B> mapDepthFirst(
    T context,
    JOTreeNodeMapFunctionType<A, T, B> f);

  /**
   * <p>Visit each node in the tree in breadth-first order.</p>
   *
   * <p>The function allows for the use of a context value. This is useful for
   * avoiding the use of capturing lambdas, reducing GC pressure.</p>
   *
   * @param context A context value passed to each application of {@code f}
   * @param f       A function used to receive each node
   * @param <T>     The type of context values
   * @param <B>     The type of values in the returned tree
   *
   * @return A new structurally equal tree with nodes of type {@code B}
   */

  <T, B> JOTreeNodeType<B> mapBreadthFirst(
    T context,
    JOTreeNodeMapFunctionType<A, T, B> f);
}
