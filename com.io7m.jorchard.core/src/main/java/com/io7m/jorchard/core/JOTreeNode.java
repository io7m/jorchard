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

import com.io7m.jaffirm.core.Invariants;
import com.io7m.jnull.NullCheck;
import com.io7m.jnull.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BooleanSupplier;

/**
 * The default implementation of the {@link JOTreeNodeType} type.
 *
 * @param <A> The type of values contained within the tree
 */

public final class JOTreeNode<A> implements JOTreeNodeType<A>
{
  private final Collection<JOTreeNodeReadableType<A>> children_view_ro;
  private final Collection<JOTreeNodeType<A>> children;
  private final A value;
  private final Collection<JOTreeNodeType<A>> children_view;
  private final BooleanSupplier detach_check;
  private boolean recursing;
  private @Nullable JOTreeNodeType<A> parent;

  private JOTreeNode(
    final Collection<JOTreeNodeType<A>> in_children,
    final BooleanSupplier in_detach_check,
    final A in_value)
  {
    this.children =
      NullCheck.notNull(in_children, "Children");
    this.value =
      NullCheck.notNull(in_value, "Value");
    this.detach_check =
      NullCheck.notNull(in_detach_check, "Detach check");

    this.parent = null;
    this.children_view_ro = Collections.unmodifiableCollection(this.children);
    this.children_view = Collections.unmodifiableCollection(this.children);
    this.recursing = false;
  }

  /**
   * Create a new node with the given value. The node has no parent and no
   * children.
   *
   * @param in_value The value
   * @param <A>      The type of values
   *
   * @return A new node
   */

  public static <A> JOTreeNodeType<A> create(final A in_value)
  {
    return new JOTreeNode<>(new ArrayList<>(8), () -> true, in_value);
  }

  /**
   * Create a new node with the given value. The node has no parent and no
   * children. The node is equipped with a function that is evaluated each
   * before the node is detached from any node, with a {@code true} value
   * indicating that the node is allowed to be detached.
   *
   * @param in_value        The value
   * @param in_detach_check A detach check function
   * @param <A>             The type of values
   *
   * @return A new node
   */

  public static <A> JOTreeNodeType<A> createWithDetachCheck(
    final A in_value,
    final BooleanSupplier in_detach_check)
  {
    return new JOTreeNode<>(new ArrayList<>(8), in_detach_check, in_value);
  }

  @Override
  public boolean isDetachAllowed()
  {
    return this.detach_check.getAsBoolean();
  }

  @Override
  public A value()
  {
    return this.value;
  }

  @Override
  public Optional<JOTreeNodeReadableType<A>> parentReadable()
  {
    return Optional.ofNullable(this.parent);
  }

  @Override
  public Collection<JOTreeNodeReadableType<A>> childrenReadable()
  {
    return this.children_view_ro;
  }

  @Override
  public String toString()
  {
    return "[JOTreeNode " + this.value + "]";
  }

  @Override
  public JOTreeNodeType<A> setParent(
    final JOTreeNodeType<A> parent_new)
  {
    NullCheck.notNull(parent_new, "Parent");

    if (parent_new.isDescendantOf(this)) {
      final StringBuilder sb = new StringBuilder(
        "Cannot set a descendant of this node to be the parent of this node.");
      sb.append(System.lineSeparator());
      sb.append("  Descendant: ");
      sb.append(parent_new);
      sb.append(System.lineSeparator());
      sb.append("  This: ");
      sb.append(this);
      sb.append(System.lineSeparator());
      throw new JOTreeExceptionCycle(sb.toString());
    }

    if (this.parent != null) {
      this.checkDetach();
    }

    if (!this.recursing) {
      try {
        this.recursing = true;

        final JOTreeNodeType<A> parent_previous = this.parent;

        /*
         * Remove this child from the existing parent. If this fails,
         * nothing needs to be restored.
         */

        if (parent_previous != null) {
          parent_previous.childRemove(this);
        }

        /*
         * Add this node to the new parent. If this fails, the node is
         * detached.
         */

        try {
          parent_new.childAdd(this);
          this.parent = parent_new;
        } catch (RuntimeException | Error e) {
          this.parent = null;
          throw e;
        }

      } finally {
        this.recursing = false;
      }
    }

    return this;
  }

  private void checkDetach()
  {
    if (!this.isDetachAllowed()) {
      final StringBuilder sb =
        new StringBuilder("This node may not be detached.");
      sb.append(System.lineSeparator());
      sb.append("  This: ");
      sb.append(this);
      sb.append(System.lineSeparator());
      throw new JOTreeExceptionDetachDenied(sb.toString());
    }
  }

  @Override
  public Optional<JOTreeNodeType<A>> parent()
  {
    return Optional.ofNullable(this.parent);
  }

  @Override
  public JOTreeNodeType<A> detach()
  {
    if (this.parent != null) {
      this.checkDetach();
    }

    if (!this.recursing) {
      try {
        this.recursing = true;
        if (this.parent != null) {
          this.parent.childRemove(this);
          this.parent = null;
        }
      } finally {
        this.recursing = false;
      }
    }

    return this;
  }

  @Override
  public Collection<JOTreeNodeType<A>> children()
  {
    return this.children_view;
  }

  @Override
  public JOTreeNodeType<A> childRemove(
    final JOTreeNodeType<A> child)
  {
    if (!this.recursing) {
      try {
        this.recursing = true;
        child.detach();
        this.children.remove(child);
      } finally {
        this.recursing = false;
      }
    }

    return this;
  }

  @Override
  public JOTreeNodeType<A> childAdd(
    final JOTreeNodeType<A> child)
  {
    if (!this.recursing) {
      try {
        this.recursing = true;
        child.setParent(this);
        this.children.remove(child);
        this.children.add(child);
      } finally {
        this.recursing = false;
      }
    }

    return this;
  }

  @Override
  public boolean isDescendantOf(
    final JOTreeNodeReadableType<A> other)
  {
    NullCheck.notNull(other, "Other");
    if (Objects.equals(other, this)) {
      return true;
    }
    return this.parent != null && this.parent.isDescendantOf(other);
  }

  @Override
  public <T> void forEachDepthFirst(
    final T context,
    final JOTreeNodeForEachFunctionType<A, T> f)
  {
    NullCheck.notNull(context, "Context");
    NullCheck.notNull(f, "Function");

    final Deque<TraversalItem<A>> stack = new LinkedList<>();
    stack.push(new TraversalItem<>(0, this));

    while (!stack.isEmpty()) {
      final TraversalItem<A> next = stack.pop();
      f.apply(context, next.depth, next.node);
      final Collection<JOTreeNodeReadableType<A>> next_children =
        next.node.childrenReadable();
      for (final JOTreeNodeReadableType<A> child : next_children) {
        stack.push(new TraversalItem<>(next.depth + 1, child));
      }
    }
  }

  @Override
  public <T> void forEachBreadthFirst(
    final T context,
    final JOTreeNodeForEachFunctionType<A, T> f)
  {
    NullCheck.notNull(context, "Context");
    NullCheck.notNull(f, "Function");

    final Queue<TraversalItem<A>> queue = new LinkedList<>();
    queue.add(new TraversalItem<>(0, this));

    while (!queue.isEmpty()) {
      final TraversalItem<A> next = queue.poll();
      f.apply(context, next.depth, next.node);
      final Collection<JOTreeNodeReadableType<A>> next_children =
        next.node.childrenReadable();
      for (final JOTreeNodeReadableType<A> child : next_children) {
        queue.add(new TraversalItem<>(next.depth + 1, child));
      }
    }
  }

  @Override
  public <T, B> JOTreeNodeType<B> mapDepthFirst(
    final T context,
    final JOTreeNodeMapFunctionType<A, T, B> f)
  {
    NullCheck.notNull(context, "Context");
    NullCheck.notNull(f, "Function");

    final Deque<MapItem<A, B>> stack = new LinkedList<>();
    stack.push(new MapItem<>(0, null, this));

    JOTreeNodeType<B> root = null;
    while (!stack.isEmpty()) {
      final MapItem<A, B> next = stack.pop();
      final B r = f.apply(context, next.depth, next.node);

      final JOTreeNodeType<B> node = create(r);
      if (next.parent != null) {
        node.setParent(next.parent);
      } else {
        Invariants.checkInvariant(root == null, "Root may only be set once");
        Invariants.checkInvariant(next.depth == 0, "Root must be depth 0");
        root = node;
      }

      final Collection<JOTreeNodeReadableType<A>> next_children =
        next.node.childrenReadable();
      for (final JOTreeNodeReadableType<A> child : next_children) {
        stack.push(new MapItem<>(next.depth + 1, node, child));
      }
    }

    return NullCheck.notNull(root, "Root");
  }

  @Override
  public <T, B> JOTreeNodeType<B> mapBreadthFirst(
    final T context,
    final JOTreeNodeMapFunctionType<A, T, B> f)
  {
    NullCheck.notNull(context, "Context");
    NullCheck.notNull(f, "Function");

    final Queue<MapItem<A, B>> queue = new LinkedList<>();
    queue.add(new MapItem<>(0, null, this));

    JOTreeNodeType<B> root = null;
    while (!queue.isEmpty()) {
      final MapItem<A, B> next = queue.poll();
      final B r = f.apply(context, next.depth, next.node);

      final JOTreeNodeType<B> node = create(r);
      if (next.parent != null) {
        node.setParent(next.parent);
      } else {
        Invariants.checkInvariant(root == null, "Root may only be set once");
        Invariants.checkInvariant(next.depth == 0, "Root must be depth 0");
        root = node;
      }

      final Collection<JOTreeNodeReadableType<A>> next_children =
        next.node.childrenReadable();
      for (final JOTreeNodeReadableType<A> child : next_children) {
        queue.add(new MapItem<>(next.depth + 1, node, child));
      }
    }

    return NullCheck.notNull(root, "Root");
  }

  private static final class MapItem<A, B>
  {
    private final int depth;
    private final JOTreeNodeType<B> parent;
    private final JOTreeNodeReadableType<A> node;

    MapItem(
      final int in_depth,
      final JOTreeNodeType<B> in_parent,
      final JOTreeNodeReadableType<A> in_value)
    {
      this.depth = in_depth;
      this.parent = in_parent;
      if (this.depth > 0) {
        NullCheck.notNull(in_parent, "Parent");
      }
      this.node = NullCheck.notNull(in_value, "Value");
    }
  }

  private static final class TraversalItem<A>
  {
    private final int depth;
    private final JOTreeNodeReadableType<A> node;

    TraversalItem(
      final int in_depth,
      final JOTreeNodeReadableType<A> in_value)
    {
      this.depth = in_depth;
      this.node = NullCheck.notNull(in_value, "Value");
    }
  }
}
