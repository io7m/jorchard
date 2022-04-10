/*
 * Copyright © 2016 Mark Raynsford <code@io7m.com> https://www.io7m.com
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

package com.io7m.jorchard.tests.core;

import com.io7m.jorchard.core.JOTreeExceptionCycle;
import com.io7m.jorchard.core.JOTreeNodeForEachFunctionType;
import com.io7m.jorchard.core.JOTreeNodeMapFunctionType;
import com.io7m.jorchard.core.JOTreeNodeReadableType;
import com.io7m.jorchard.core.JOTreeNodeType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DelegatingNode<A> implements JOTreeNodeType<A>
{
  protected final JOTreeNodeType<A> actual;

  public DelegatingNode(final JOTreeNodeType<A> actual)
  {
    this.actual = actual;
  }

  @Override
  public JOTreeNodeType<A> detach()
  {
    return this.actual.detach();
  }

  @Override
  public List<JOTreeNodeType<A>> children()
  {
    return this.actual.children();
  }

  @Override
  public JOTreeNodeType<A> childRemove(final JOTreeNodeType<A> child)
  {
    return this.actual.childRemove(child);
  }

  @Override
  public JOTreeNodeType<A> childAdd(final JOTreeNodeType<A> child)
    throws JOTreeExceptionCycle
  {
    return this.actual.childAdd(child);
  }

  @Override
  public JOTreeNodeType<A> setParent(final JOTreeNodeType<A> new_parent)
    throws JOTreeExceptionCycle
  {
    return this.actual.setParent(new_parent);
  }

  @Override
  public Optional<JOTreeNodeType<A>> parent()
  {
    return this.actual.parent();
  }

  @Override
  public void childrenSortNodes(
    final Comparator<JOTreeNodeType<A>> comparator)
  {
    this.actual.childrenSortNodes(comparator);
  }

  @Override
  public boolean isDetachAllowed()
  {
    return this.actual.isDetachAllowed();
  }

  @Override
  public A value()
  {
    return this.actual.value();
  }

  @Override
  public boolean isRoot()
  {
    return this.actual.isRoot();
  }

  @Override
  public Optional<JOTreeNodeReadableType<A>> parentReadable()
  {
    return this.actual.parentReadable();
  }

  @Override
  public List<JOTreeNodeReadableType<A>> childrenReadable()
  {
    return this.actual.childrenReadable();
  }

  @Override
  public boolean isDescendantOf(final JOTreeNodeReadableType<A> other)
  {
    return this.actual.isDescendantOf(other);
  }

  @Override
  public <T> void forEachDepthFirst(
    final T context,
    final JOTreeNodeForEachFunctionType<A, T> f)
  {
    this.actual.forEachDepthFirst(context, f);
  }

  @Override
  public <T> void forEachBreadthFirst(
    final T context,
    final JOTreeNodeForEachFunctionType<A, T> f)
  {
    this.actual.forEachBreadthFirst(context, f);
  }

  @Override
  public <T, B> JOTreeNodeType<B> mapDepthFirst(
    final T context,
    final JOTreeNodeMapFunctionType<A, T, B> f)
  {
    return this.actual.mapDepthFirst(context, f);
  }

  @Override
  public <T, B> JOTreeNodeType<B> mapBreadthFirst(
    final T context,
    final JOTreeNodeMapFunctionType<A, T, B> f)
  {
    return this.actual.mapBreadthFirst(context, f);
  }
}
