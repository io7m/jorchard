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

package com.io7m.jorchard.tests.core;

import com.io7m.jorchard.core.JOTreeExceptionCycle;
import com.io7m.jorchard.core.JOTreeExceptionDetachDenied;
import com.io7m.jorchard.core.JOTreeNodeForEachFunctionType;
import com.io7m.jorchard.core.JOTreeNodeReadableType;
import com.io7m.jorchard.core.JOTreeNodeType;
import com.io7m.junreachable.UnimplementedCodeException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public abstract class JOTreeNodeContract
{
  @Rule public final ExpectedException expected = ExpectedException.none();

  protected abstract <A> JOTreeNodeType<A> create(final A x);

  protected abstract <A> JOTreeNodeType<A> createWithDetachCheck(
    final A x,
    final BooleanSupplier detach_check);

  @Test
  public final void testForEachDepthFirst()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n2 = this.create(Integer.valueOf(2));
    final JOTreeNodeType<Integer> n3 = this.create(Integer.valueOf(3));
    final JOTreeNodeType<Integer> n4 = this.create(Integer.valueOf(4));
    final JOTreeNodeType<Integer> n5 = this.create(Integer.valueOf(5));
    final JOTreeNodeType<Integer> n6 = this.create(Integer.valueOf(6));
    final JOTreeNodeType<Integer> n7 = this.create(Integer.valueOf(7));

    n7.setParent(n5);
    n6.setParent(n5);

    n4.setParent(n2);
    n3.setParent(n2);

    n5.setParent(n1);
    n2.setParent(n1);

    n1.setParent(n0);

    final List<Integer> order = new ArrayList<>(10);
    n0.forEachDepthFirst(order, (input, depth, node) -> {
      Assert.assertTrue(depth >= 0);
      Assert.assertTrue(depth <= 3);
      input.add(node.value());
    });

    Assert.assertEquals(8L, (long) order.size());
    Assert.assertEquals(Integer.valueOf(0), order.get(0));
    Assert.assertEquals(Integer.valueOf(1), order.get(1));
    Assert.assertEquals(Integer.valueOf(2), order.get(2));
    Assert.assertEquals(Integer.valueOf(3), order.get(3));
    Assert.assertEquals(Integer.valueOf(4), order.get(4));
    Assert.assertEquals(Integer.valueOf(5), order.get(5));
    Assert.assertEquals(Integer.valueOf(6), order.get(6));
    Assert.assertEquals(Integer.valueOf(7), order.get(7));
  }

  @Test
  public final void testForEachBreadthFirst()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n2 = this.create(Integer.valueOf(2));
    final JOTreeNodeType<Integer> n3 = this.create(Integer.valueOf(3));
    final JOTreeNodeType<Integer> n4 = this.create(Integer.valueOf(4));
    final JOTreeNodeType<Integer> n5 = this.create(Integer.valueOf(5));
    final JOTreeNodeType<Integer> n6 = this.create(Integer.valueOf(6));
    final JOTreeNodeType<Integer> n7 = this.create(Integer.valueOf(7));

    n7.setParent(n5);
    n6.setParent(n5);

    n4.setParent(n2);
    n3.setParent(n2);

    n5.setParent(n1);
    n2.setParent(n1);

    n1.setParent(n0);

    final List<Integer> order = new ArrayList<>(10);
    n0.forEachBreadthFirst(order, (input, depth, node) -> {
      Assert.assertTrue(depth >= 0);
      Assert.assertTrue(depth <= 3);
      input.add(node.value());
    });

    Assert.assertEquals(8L, (long) order.size());
    Assert.assertEquals(Integer.valueOf(0), order.get(0));
    Assert.assertEquals(Integer.valueOf(1), order.get(1));
    Assert.assertEquals(Integer.valueOf(5), order.get(2));
    Assert.assertEquals(Integer.valueOf(2), order.get(3));
    Assert.assertEquals(Integer.valueOf(7), order.get(4));
    Assert.assertEquals(Integer.valueOf(6), order.get(5));
    Assert.assertEquals(Integer.valueOf(4), order.get(6));
    Assert.assertEquals(Integer.valueOf(3), order.get(7));
  }

  @Test
  public final void testDetachWithoutParent()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));

    Assert.assertTrue(n0.isRoot());
    Assert.assertFalse(n0.parent().isPresent());

    n0.detach();

    Assert.assertTrue(n0.isRoot());
    Assert.assertFalse(n0.parent().isPresent());
  }

  @Test
  public final void testDeniedDetach()
  {
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n0 =
      this.createWithDetachCheck(Integer.valueOf(0), () -> false);

    n0.setParent(n1);

    this.expected.expect(JOTreeExceptionDetachDenied.class);
    n0.detach();
  }

  @Test
  public final void testDeniedSetParent()
  {
    final JOTreeNodeType<Integer> n2 = this.create(Integer.valueOf(2));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n0 =
      this.createWithDetachCheck(Integer.valueOf(0), () -> false);

    n0.setParent(n1);

    this.expected.expect(JOTreeExceptionDetachDenied.class);
    n0.setParent(n2);
  }

  @Test
  public final void testDeniedChildRemove()
  {
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n0 =
      this.createWithDetachCheck(Integer.valueOf(0), () -> false);

    n0.setParent(n1);

    this.expected.expect(JOTreeExceptionDetachDenied.class);
    n1.childRemove(n0);
  }

  @Test
  public final void testIsRoot()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    Assert.assertTrue(n0.isRoot());
    Assert.assertTrue(n1.isRoot());

    n0.setParent(n1);
    Assert.assertFalse(n0.isRoot());
    Assert.assertTrue(n1.isRoot());

    n0.detach();
    Assert.assertTrue(n0.isRoot());
    Assert.assertTrue(n1.isRoot());
  }

  @Test
  public final void testIsDescendantOfSelf()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    Assert.assertTrue(n0.isDescendantOf(n0));
  }

  @Test
  public final void testIsDescendantOfUnrelated()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    Assert.assertFalse(n0.isDescendantOf(n1));
    Assert.assertFalse(n1.isDescendantOf(n0));
  }

  @Test
  public final void testIsDescendantOfUnrelatedMore()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n2 = this.create(Integer.valueOf(2));

    n2.setParent(n1);

    Assert.assertTrue(n0.isDescendantOf(n0));
    Assert.assertFalse(n0.isDescendantOf(n1));
    Assert.assertFalse(n0.isDescendantOf(n2));

    Assert.assertFalse(n1.isDescendantOf(n0));
    Assert.assertTrue(n1.isDescendantOf(n1));
    Assert.assertFalse(n1.isDescendantOf(n2));

    Assert.assertTrue(n2.isDescendantOf(n2));
    Assert.assertFalse(n2.isDescendantOf(n0));
    Assert.assertTrue(n2.isDescendantOf(n1));
  }

  @Test
  public final void testChildAddCorrect()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n2 = this.create(Integer.valueOf(2));
    final JOTreeNodeType<Integer> n3 = this.create(Integer.valueOf(3));

    Assert.assertEquals(Integer.valueOf(0), n0.value());
    Assert.assertEquals(Integer.valueOf(1), n1.value());
    Assert.assertEquals(Integer.valueOf(2), n2.value());
    Assert.assertEquals(Integer.valueOf(3), n3.value());

    Assert.assertFalse(n0.parentReadable().isPresent());
    Assert.assertFalse(n1.parentReadable().isPresent());
    Assert.assertFalse(n2.parentReadable().isPresent());
    Assert.assertFalse(n3.parentReadable().isPresent());

    Assert.assertFalse(n0.parent().isPresent());
    Assert.assertFalse(n1.parent().isPresent());
    Assert.assertFalse(n2.parent().isPresent());
    Assert.assertFalse(n3.parent().isPresent());

    n0.childAdd(n1);
    n0.childAdd(n2);
    n0.childAdd(n3);

    Assert.assertEquals(Optional.of(n0), n1.parentReadable());
    Assert.assertEquals(Optional.of(n0), n2.parentReadable());
    Assert.assertEquals(Optional.of(n0), n3.parentReadable());

    Assert.assertEquals(Optional.of(n0), n1.parent());
    Assert.assertEquals(Optional.of(n0), n2.parent());
    Assert.assertEquals(Optional.of(n0), n3.parent());

    final Collection<JOTreeNodeType<Integer>> n0_children = n0.children();
    final Collection<JOTreeNodeReadableType<Integer>> n0_children_ro = n0.childrenReadable();

    Assert.assertEquals(3L, (long) n0_children.size());
    Assert.assertTrue(n0_children.contains(n1));
    Assert.assertTrue(n0_children.contains(n2));
    Assert.assertTrue(n0_children.contains(n3));

    Assert.assertEquals(3L, (long) n0_children_ro.size());
    Assert.assertTrue(n0_children_ro.contains(n1));
    Assert.assertTrue(n0_children_ro.contains(n2));
    Assert.assertTrue(n0_children_ro.contains(n3));

    n0.childRemove(n3);

    Assert.assertEquals(2L, (long) n0_children.size());
    Assert.assertTrue(n0_children.contains(n1));
    Assert.assertTrue(n0_children.contains(n2));
    Assert.assertFalse(n0_children.contains(n3));

    Assert.assertEquals(2L, (long) n0_children_ro.size());
    Assert.assertTrue(n0_children_ro.contains(n1));
    Assert.assertTrue(n0_children_ro.contains(n2));
    Assert.assertFalse(n0_children_ro.contains(n3));

    n0.childRemove(n2);

    Assert.assertEquals(1L, (long) n0_children.size());
    Assert.assertTrue(n0_children.contains(n1));
    Assert.assertFalse(n0_children.contains(n2));
    Assert.assertFalse(n0_children.contains(n3));

    Assert.assertEquals(1L, (long) n0_children_ro.size());
    Assert.assertTrue(n0_children_ro.contains(n1));
    Assert.assertFalse(n0_children_ro.contains(n2));
    Assert.assertFalse(n0_children_ro.contains(n3));

    n0.childRemove(n1);

    Assert.assertEquals(0L, (long) n0_children.size());
    Assert.assertFalse(n0_children.contains(n1));
    Assert.assertFalse(n0_children.contains(n2));
    Assert.assertFalse(n0_children.contains(n3));

    Assert.assertEquals(0L, (long) n0_children_ro.size());
    Assert.assertFalse(n0_children_ro.contains(n1));
    Assert.assertFalse(n0_children_ro.contains(n2));
    Assert.assertFalse(n0_children_ro.contains(n3));
  }

  @Test
  public final void testSetParentCorrect()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n2 = this.create(Integer.valueOf(2));
    final JOTreeNodeType<Integer> n3 = this.create(Integer.valueOf(3));

    Assert.assertEquals(Integer.valueOf(0), n0.value());
    Assert.assertEquals(Integer.valueOf(1), n1.value());
    Assert.assertEquals(Integer.valueOf(2), n2.value());
    Assert.assertEquals(Integer.valueOf(3), n3.value());

    Assert.assertFalse(n0.parentReadable().isPresent());
    Assert.assertFalse(n1.parentReadable().isPresent());
    Assert.assertFalse(n2.parentReadable().isPresent());
    Assert.assertFalse(n3.parentReadable().isPresent());

    Assert.assertFalse(n0.parent().isPresent());
    Assert.assertFalse(n1.parent().isPresent());
    Assert.assertFalse(n2.parent().isPresent());
    Assert.assertFalse(n3.parent().isPresent());

    n1.setParent(n0);
    n2.setParent(n0);
    n3.setParent(n0);

    Assert.assertEquals(Optional.of(n0), n1.parentReadable());
    Assert.assertEquals(Optional.of(n0), n2.parentReadable());
    Assert.assertEquals(Optional.of(n0), n3.parentReadable());

    Assert.assertEquals(Optional.of(n0), n1.parent());
    Assert.assertEquals(Optional.of(n0), n2.parent());
    Assert.assertEquals(Optional.of(n0), n3.parent());

    final Collection<JOTreeNodeType<Integer>> n0_children = n0.children();
    final Collection<JOTreeNodeReadableType<Integer>> n0_children_ro = n0.childrenReadable();

    Assert.assertEquals(3L, (long) n0_children.size());
    Assert.assertTrue(n0_children.contains(n1));
    Assert.assertTrue(n0_children.contains(n2));
    Assert.assertTrue(n0_children.contains(n3));

    Assert.assertEquals(3L, (long) n0_children_ro.size());
    Assert.assertTrue(n0_children_ro.contains(n1));
    Assert.assertTrue(n0_children_ro.contains(n2));
    Assert.assertTrue(n0_children_ro.contains(n3));

    n3.detach();

    Assert.assertEquals(2L, (long) n0_children.size());
    Assert.assertTrue(n0_children.contains(n1));
    Assert.assertTrue(n0_children.contains(n2));
    Assert.assertFalse(n0_children.contains(n3));

    Assert.assertEquals(2L, (long) n0_children_ro.size());
    Assert.assertTrue(n0_children_ro.contains(n1));
    Assert.assertTrue(n0_children_ro.contains(n2));
    Assert.assertFalse(n0_children_ro.contains(n3));

    n2.detach();

    Assert.assertEquals(1L, (long) n0_children.size());
    Assert.assertTrue(n0_children.contains(n1));
    Assert.assertFalse(n0_children.contains(n2));
    Assert.assertFalse(n0_children.contains(n3));

    Assert.assertEquals(1L, (long) n0_children_ro.size());
    Assert.assertTrue(n0_children_ro.contains(n1));
    Assert.assertFalse(n0_children_ro.contains(n2));
    Assert.assertFalse(n0_children_ro.contains(n3));

    n1.detach();

    Assert.assertEquals(0L, (long) n0_children.size());
    Assert.assertFalse(n0_children.contains(n1));
    Assert.assertFalse(n0_children.contains(n2));
    Assert.assertFalse(n0_children.contains(n3));

    Assert.assertEquals(0L, (long) n0_children_ro.size());
    Assert.assertFalse(n0_children_ro.contains(n1));
    Assert.assertFalse(n0_children_ro.contains(n2));
    Assert.assertFalse(n0_children_ro.contains(n3));
  }

  @Test
  public final void testSetParentTransfer()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n2 = this.create(Integer.valueOf(2));

    n1.setParent(n0);

    Assert.assertFalse(n2.children().contains(n1));
    Assert.assertTrue(n0.children().contains(n1));
    Assert.assertEquals(n0, n1.parent().get());

    n1.setParent(n2);

    Assert.assertTrue(n2.children().contains(n1));
    Assert.assertFalse(n0.children().contains(n1));
    Assert.assertEquals(n2, n1.parent().get());
  }

  @Test
  public final void testSetParentCyclic0()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));

    n0.setParent(n1);
    this.expected.expect(JOTreeExceptionCycle.class);
    n1.setParent(n0);
  }

  @Test
  public final void testSetParentCyclic1()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final JOTreeNodeType<Integer> n2 = this.create(Integer.valueOf(2));

    n0.setParent(n1);
    n1.setParent(n2);
    this.expected.expect(JOTreeExceptionCycle.class);
    n2.setParent(n0);
  }

  /**
   * Checks that a parent that raises an exception when a child attempts to
   * setParent it doesn't corrupt the tree.
   */

  @Test
  public final void testSetParentExceptionSafe()
  {
    final JOTreeNodeType<Integer> n0 = this.create(Integer.valueOf(0));
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));

    final ChildAddTimebomb<Integer> n_timebomb =
      new ChildAddTimebomb<>(this.create(Integer.valueOf(2)));

    n1.setParent(n0);
    Assert.assertTrue(n0.children().contains(n1));
    Assert.assertEquals(n0, n1.parent().get());

    boolean caught = false;

    try {
      n1.setParent(n_timebomb);
    } catch (final HostileImplementationException e) {
      Assert.assertEquals("Refusing to add", e.getMessage());
      caught = true;
    }

    Assert.assertTrue(caught);
    Assert.assertFalse(n0.children().contains(n1));
    Assert.assertFalse(n1.parent().isPresent());
  }

  /**
   * Checks that a parent that raises an exception when a child attempts to
   * setParent it doesn't corrupt the tree. This assumes that there isn't a
   * previous parent to restore.
   */

  @Test
  public final void testSetParentExceptionSafeNoPrevious()
  {
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));

    boolean caught = false;

    try {
      n1.setParent(new JOTreeNodeUnimplemented<Integer>()
      {
        @Override
        public void childAdd(final JOTreeNodeType<Integer> child)
          throws JOTreeExceptionCycle
        {
          throw new HostileImplementationException("Refusing to add");
        }

        @Override
        public boolean isDescendantOf(
          final JOTreeNodeReadableType<Integer> other)
        {
          return false;
        }
      });
    } catch (final HostileImplementationException e) {
      Assert.assertEquals("Refusing to add", e.getMessage());
      caught = true;
    }

    Assert.assertTrue(caught);
    Assert.assertFalse(n1.parent().isPresent());
  }

  /**
   * Checks that a parent that raises an exception when it removes a child
   * doesn't corrupt the tree.
   */

  @Test
  public final void testDetachExceptionSafeNoPrevious()
  {
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final ChildRemoveTimebomb<Integer> n_timebomb =
      new ChildRemoveTimebomb<>(this.create(Integer.valueOf(0)));

    n1.setParent(n_timebomb);

    Assert.assertEquals(n_timebomb, n1.parent().get());
    Assert.assertTrue(n_timebomb.children().contains(n1));

    boolean caught = false;

    try {
      n1.detach();
    } catch (final HostileImplementationException e) {
      Assert.assertEquals("Refusing to remove", e.getMessage());
      caught = true;
    }

    Assert.assertTrue(caught);
    Assert.assertEquals(n_timebomb, n1.parent().get());
    Assert.assertTrue(n_timebomb.children().contains(n1));
  }

  /**
   * Checks that a child that raises an exception when tries to setParent
   * doesn't corrupt the tree.
   */

  @Test
  public final void testChildSetParentExceptionSafeNoPrevious()
  {
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final ChildSetParentTimebomb<Integer> n_timebomb =
      new ChildSetParentTimebomb<>(this.create(Integer.valueOf(0)));

    boolean caught = false;

    try {
      n1.childAdd(n_timebomb);
    } catch (final HostileImplementationException e) {
      Assert.assertEquals("Refusing to set parent", e.getMessage());
      caught = true;
    }

    Assert.assertTrue(caught);
    Assert.assertFalse(n_timebomb.parent().isPresent());
    Assert.assertFalse(n1.children().contains(n_timebomb));
  }

  /**
   * Checks that a child that raises an exception when it detaches doesn't
   * corrupt the tree.
   */

  @Test
  public final void testChildRemoveExceptionSafe()
  {
    final JOTreeNodeType<Integer> n1 = this.create(Integer.valueOf(1));
    final DetachTimebomb<Integer> n_timebomb =
      new DetachTimebomb<>(this.create(Integer.valueOf(0)));

    n1.childAdd(n_timebomb);

    Assert.assertEquals(n1, n_timebomb.parent().get());
    Assert.assertTrue(n1.children().contains(n_timebomb));

    boolean caught = false;

    try {
      n1.childRemove(n_timebomb);
    } catch (final HostileImplementationException e) {
      Assert.assertEquals("Refusing to detach", e.getMessage());
      caught = true;
    }

    Assert.assertTrue(caught);
    Assert.assertEquals(n1, n_timebomb.parent().get());
    Assert.assertTrue(n1.children().contains(n_timebomb));
  }

  static class JOTreeNodeUnimplemented<A> implements JOTreeNodeType<A>
  {
    @Override
    public boolean isDetachAllowed()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public void detach()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public Collection<JOTreeNodeType<A>> children()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public void childRemove(final JOTreeNodeType<A> child)
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public void childAdd(final JOTreeNodeType<A> child)
      throws JOTreeExceptionCycle
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public void setParent(final JOTreeNodeType<A> new_parent)
      throws JOTreeExceptionCycle
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public Optional<JOTreeNodeType<A>> parent()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public A value()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public Optional<JOTreeNodeReadableType<A>> parentReadable()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public Collection<JOTreeNodeReadableType<A>> childrenReadable()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public boolean isDescendantOf(final JOTreeNodeReadableType<A> other)
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public <T> void forEachDepthFirst(
      final T context,
      final JOTreeNodeForEachFunctionType<A, T> f)
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public <T> void forEachBreadthFirst(
      final T context,
      final JOTreeNodeForEachFunctionType<A, T> f)
    {
      throw new UnimplementedCodeException();
    }
  }

  static final class HostileImplementationException extends RuntimeException
  {
    public HostileImplementationException(final String message)
    {
      super(message);
    }
  }

  private static final class ChildRemoveTimebomb<A> extends DelegatingNode<A>
  {
    ChildRemoveTimebomb(final JOTreeNodeType<A> actual)
    {
      super(actual);
    }

    @Override
    public void childRemove(final JOTreeNodeType<A> child)
    {
      throw new JOTreeNodeContract.HostileImplementationException(
        "Refusing to remove");
    }
  }

  private static final class ChildAddTimebomb<A> extends DelegatingNode<A>
  {
    ChildAddTimebomb(final JOTreeNodeType<A> actual)
    {
      super(actual);
    }

    @Override
    public void childAdd(final JOTreeNodeType<A> child)
      throws JOTreeExceptionCycle
    {
      throw new JOTreeNodeContract.HostileImplementationException(
        "Refusing to add");
    }
  }

  private static final class ChildSetParentTimebomb<A> extends DelegatingNode<A>
  {
    ChildSetParentTimebomb(final JOTreeNodeType<A> actual)
    {
      super(actual);
    }

    @Override
    public void setParent(final JOTreeNodeType<A> new_parent)
      throws JOTreeExceptionCycle
    {
      throw new JOTreeNodeContract.HostileImplementationException(
        "Refusing to set parent");
    }
  }

  private static final class DetachTimebomb<A> extends DelegatingNode<A>
  {
    DetachTimebomb(final JOTreeNodeType<A> actual)
    {
      super(actual);
    }

    @Override
    public void detach()
    {
      throw new JOTreeNodeContract.HostileImplementationException(
        "Refusing to detach");
    }
  }
}
