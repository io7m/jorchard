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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * The type of tree nodes.
 *
 * @param <A> The type of contained values.
 */

public interface JOTreeNodeType<A> extends JOTreeNodeReadableType<A>
{
  /**
   * Detach this node from its parent node.
   *
   * @return this
   */

  JOTreeNodeType<A> detach();

  /**
   * @return A read-only collection containing the children of this node
   */

  List<JOTreeNodeType<A>> children();

  /**
   * Remove a child from this node. Has no effect if the given node is not a
   * child of this node.
   *
   * @param child A child node
   *
   * @return this
   */

  JOTreeNodeType<A> childRemove(
    JOTreeNodeType<A> child);

  /**
   * Add a child to this node. Has no effect if the given node is already a
   * child of this node.
   *
   * @param child A child node
   *
   * @return this
   *
   * @throws JOTreeExceptionCycle Iff adding the child would introduce a cycle
   *                              in the tree
   */

  JOTreeNodeType<A> childAdd(
    JOTreeNodeType<A> child)
    throws JOTreeExceptionCycle;

  /**
   * Set this node's parent to {@code new_parent}.
   *
   * @param new_parent The new parent node
   *
   * @return this
   *
   * @throws JOTreeExceptionCycle Iff setting the parent would introduce a cycle
   *                              in the tree
   */

  JOTreeNodeType<A> setParent(
    JOTreeNodeType<A> new_parent)
    throws JOTreeExceptionCycle;

  /**
   * @return A reference to the parent node, if any
   */

  Optional<JOTreeNodeType<A>> parent();

  /**
   * Sort the children of this node. This will affect subsequent calls to {@link
   * #forEachDepthFirst(Object, JOTreeNodeForEachFunctionType)} and {@link
   * #forEachBreadthFirst(Object, JOTreeNodeForEachFunctionType)}, and will
   * affect the order of {@link #children()}.
   *
   * @param comparator A node comparator
   */

  void childrenSortNodes(
    Comparator<JOTreeNodeType<A>> comparator);

  /**
   * Sort the children of this node. This will affect subsequent calls to {@link
   * #forEachDepthFirst(Object, JOTreeNodeForEachFunctionType)} and {@link
   * #forEachBreadthFirst(Object, JOTreeNodeForEachFunctionType)}, and will
   * affect the order of {@link #children()}.
   *
   * @param comparator A node comparator
   */

  default void childrenSort(
    final Comparator<A> comparator)
  {
    this.childrenSortNodes(
      (o1, o2) -> comparator.compare(o1.value(), o2.value()));
  }
}
