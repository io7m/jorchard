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
import com.io7m.jorchard.core.JOTreeExceptionDetachDenied;
import com.io7m.jorchard.core.JOTreeNodeForEachFunctionType;
import com.io7m.jorchard.core.JOTreeNodeMapFunctionType;
import com.io7m.jorchard.core.JOTreeNodeReadableType;
import com.io7m.jorchard.core.JOTreeNodeType;
import com.io7m.junreachable.UnimplementedCodeException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


public abstract class JOTreeNodeContract
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JOTreeNodeContract.class);

  private static <T> void dump(
    final JOTreeNodeReadableType<T> node)
  {
    final var sb = new StringBuilder(128);
    LOG.debug("dumping tree {}", node);
    node.forEachDepthFirst(Integer.valueOf(0), (input, depth, n) -> {
      sb.setLength(0);
      for (var index = 0; index < depth; ++index) {
        sb.append(" ");
      }
      sb.append(n.value());
      LOG.debug("{}", sb);
    });
  }

  protected abstract <A> JOTreeNodeType<A> create(A x);

  protected abstract <A> JOTreeNodeType<A> createWithDetachCheck(
    A x,
    BooleanSupplier detach_check);

  @Test
  public final void testForEachDepthFirst()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));
    final var n3 = this.create(Integer.valueOf(3));
    final var n4 = this.create(Integer.valueOf(4));
    final var n5 = this.create(Integer.valueOf(5));
    final var n6 = this.create(Integer.valueOf(6));
    final var n7 = this.create(Integer.valueOf(7));

    n7.setParent(n5);
    n6.setParent(n5);

    n4.setParent(n2);
    n3.setParent(n2);

    n5.setParent(n1);
    n2.setParent(n1);

    n1.setParent(n0);

    final List<Integer> order = new ArrayList<>(10);
    n0.forEachDepthFirst(order, (input, depth, node) -> {
      assertTrue(depth >= 0);
      assertTrue(depth <= 3);
      input.add(node.value());
    });

    assertEquals(8L, order.size());
    assertEquals(Integer.valueOf(0), order.get(0));
    assertEquals(Integer.valueOf(1), order.get(1));
    assertEquals(Integer.valueOf(2), order.get(2));
    assertEquals(Integer.valueOf(3), order.get(3));
    assertEquals(Integer.valueOf(4), order.get(4));
    assertEquals(Integer.valueOf(5), order.get(5));
    assertEquals(Integer.valueOf(6), order.get(6));
    assertEquals(Integer.valueOf(7), order.get(7));
  }

  @Test
  public final void testForEachBreadthFirst()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));
    final var n3 = this.create(Integer.valueOf(3));
    final var n4 = this.create(Integer.valueOf(4));
    final var n5 = this.create(Integer.valueOf(5));
    final var n6 = this.create(Integer.valueOf(6));
    final var n7 = this.create(Integer.valueOf(7));

    n7.setParent(n5);
    n6.setParent(n5);

    n4.setParent(n2);
    n3.setParent(n2);

    n5.setParent(n1);
    n2.setParent(n1);

    n1.setParent(n0);

    final List<Integer> order = new ArrayList<>(10);
    n0.forEachBreadthFirst(order, (input, depth, node) -> {
      assertTrue(depth >= 0);
      assertTrue(depth <= 3);
      input.add(node.value());

      LOG.debug("node {} {}", Integer.valueOf(depth), node.value());
    });

    assertEquals(8L, order.size());
    assertEquals(Integer.valueOf(0), order.get(0));
    assertEquals(Integer.valueOf(1), order.get(1));
    assertEquals(Integer.valueOf(5), order.get(2));
    assertEquals(Integer.valueOf(2), order.get(3));
    assertEquals(Integer.valueOf(7), order.get(4));
    assertEquals(Integer.valueOf(6), order.get(5));
    assertEquals(Integer.valueOf(4), order.get(6));
    assertEquals(Integer.valueOf(3), order.get(7));
  }

  @Test
  public final void testDetachWithoutParent()
  {
    final var n0 = this.create(Integer.valueOf(0));

    assertTrue(n0.isRoot());
    assertFalse(n0.parent().isPresent());

    assertEquals(n0, n0.detach());

    assertTrue(n0.isRoot());
    assertFalse(n0.parent().isPresent());
  }

  @Test
  public final void testDeniedDetach()
  {
    final var n1 = this.create(Integer.valueOf(1));
    final var n0 =
      this.createWithDetachCheck(Integer.valueOf(0), () -> false);

    n0.setParent(n1);

    assertThrows(JOTreeExceptionDetachDenied.class, () -> {
      n0.detach();
    });
  }

  @Test
  public final void testDeniedSetParent()
  {
    final var n2 =
      this.create(Integer.valueOf(2));
    final var n1 =
      this.create(Integer.valueOf(1));
    final var n0 =
      this.createWithDetachCheck(Integer.valueOf(0), () -> false);

    n0.setParent(n1);

    assertThrows(JOTreeExceptionDetachDenied.class, () -> {
      n0.setParent(n2);
    });
  }

  @Test
  public final void testDeniedChildRemove()
  {
    final var n1 = this.create(Integer.valueOf(1));
    final var n0 =
      this.createWithDetachCheck(Integer.valueOf(0), () -> false);

    n0.setParent(n1);

    assertThrows(JOTreeExceptionDetachDenied.class, () -> {
      n1.childRemove(n0);
    });
  }

  @Test
  public final void testIsRoot()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    assertTrue(n0.isRoot());
    assertTrue(n1.isRoot());

    assertEquals(n0, n0.setParent(n1));
    assertFalse(n0.isRoot());
    assertTrue(n1.isRoot());

    assertEquals(n0, n0.detach());
    assertTrue(n0.isRoot());
    assertTrue(n1.isRoot());
  }

  @Test
  public final void testIsDescendantOfSelf()
  {
    final var n0 = this.create(Integer.valueOf(0));
    assertTrue(n0.isDescendantOf(n0));
  }

  @Test
  public final void testIsDescendantOfUnrelated()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    assertFalse(n0.isDescendantOf(n1));
    assertFalse(n1.isDescendantOf(n0));
  }

  @Test
  public final void testIsDescendantOfUnrelatedMore()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));

    n2.setParent(n1);

    assertTrue(n0.isDescendantOf(n0));
    assertFalse(n0.isDescendantOf(n1));
    assertFalse(n0.isDescendantOf(n2));

    assertFalse(n1.isDescendantOf(n0));
    assertTrue(n1.isDescendantOf(n1));
    assertFalse(n1.isDescendantOf(n2));

    assertTrue(n2.isDescendantOf(n2));
    assertFalse(n2.isDescendantOf(n0));
    assertTrue(n2.isDescendantOf(n1));
  }

  @Test
  public final void testChildAddCorrect()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));
    final var n3 = this.create(Integer.valueOf(3));

    assertEquals(Integer.valueOf(0), n0.value());
    assertEquals(Integer.valueOf(1), n1.value());
    assertEquals(Integer.valueOf(2), n2.value());
    assertEquals(Integer.valueOf(3), n3.value());

    assertFalse(n0.parentReadable().isPresent());
    assertFalse(n1.parentReadable().isPresent());
    assertFalse(n2.parentReadable().isPresent());
    assertFalse(n3.parentReadable().isPresent());

    assertFalse(n0.parent().isPresent());
    assertFalse(n1.parent().isPresent());
    assertFalse(n2.parent().isPresent());
    assertFalse(n3.parent().isPresent());

    assertEquals(n0, n0.childAdd(n1));
    assertEquals(n0, n0.childAdd(n2));
    assertEquals(n0, n0.childAdd(n3));

    assertEquals(Optional.of(n0), n1.parentReadable());
    assertEquals(Optional.of(n0), n2.parentReadable());
    assertEquals(Optional.of(n0), n3.parentReadable());

    assertEquals(Optional.of(n0), n1.parent());
    assertEquals(Optional.of(n0), n2.parent());
    assertEquals(Optional.of(n0), n3.parent());

    final Collection<JOTreeNodeType<Integer>> n0_children = n0.children();
    final Collection<JOTreeNodeReadableType<Integer>> n0_children_ro = n0.childrenReadable();

    assertEquals(3L, n0_children.size());
    assertTrue(n0_children.contains(n1));
    assertTrue(n0_children.contains(n2));
    assertTrue(n0_children.contains(n3));

    assertEquals(3L, n0_children_ro.size());
    assertTrue(n0_children_ro.contains(n1));
    assertTrue(n0_children_ro.contains(n2));
    assertTrue(n0_children_ro.contains(n3));

    assertEquals(n0, n0.childRemove(n3));

    assertEquals(2L, n0_children.size());
    assertTrue(n0_children.contains(n1));
    assertTrue(n0_children.contains(n2));
    assertFalse(n0_children.contains(n3));

    assertEquals(2L, n0_children_ro.size());
    assertTrue(n0_children_ro.contains(n1));
    assertTrue(n0_children_ro.contains(n2));
    assertFalse(n0_children_ro.contains(n3));

    assertEquals(n0, n0.childRemove(n2));

    assertEquals(1L, n0_children.size());
    assertTrue(n0_children.contains(n1));
    assertFalse(n0_children.contains(n2));
    assertFalse(n0_children.contains(n3));

    assertEquals(1L, n0_children_ro.size());
    assertTrue(n0_children_ro.contains(n1));
    assertFalse(n0_children_ro.contains(n2));
    assertFalse(n0_children_ro.contains(n3));

    assertEquals(n0, n0.childRemove(n1));

    assertEquals(0L, n0_children.size());
    assertFalse(n0_children.contains(n1));
    assertFalse(n0_children.contains(n2));
    assertFalse(n0_children.contains(n3));

    assertEquals(0L, n0_children_ro.size());
    assertFalse(n0_children_ro.contains(n1));
    assertFalse(n0_children_ro.contains(n2));
    assertFalse(n0_children_ro.contains(n3));
  }

  @Test
  public final void testSetParentCorrect()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));
    final var n3 = this.create(Integer.valueOf(3));

    assertEquals(Integer.valueOf(0), n0.value());
    assertEquals(Integer.valueOf(1), n1.value());
    assertEquals(Integer.valueOf(2), n2.value());
    assertEquals(Integer.valueOf(3), n3.value());

    assertFalse(n0.parentReadable().isPresent());
    assertFalse(n1.parentReadable().isPresent());
    assertFalse(n2.parentReadable().isPresent());
    assertFalse(n3.parentReadable().isPresent());

    assertFalse(n0.parent().isPresent());
    assertFalse(n1.parent().isPresent());
    assertFalse(n2.parent().isPresent());
    assertFalse(n3.parent().isPresent());

    assertEquals(n1, n1.setParent(n0));
    assertEquals(n2, n2.setParent(n0));
    assertEquals(n3, n3.setParent(n0));

    assertEquals(Optional.of(n0), n1.parentReadable());
    assertEquals(Optional.of(n0), n2.parentReadable());
    assertEquals(Optional.of(n0), n3.parentReadable());

    assertEquals(Optional.of(n0), n1.parent());
    assertEquals(Optional.of(n0), n2.parent());
    assertEquals(Optional.of(n0), n3.parent());

    final Collection<JOTreeNodeType<Integer>> n0_children = n0.children();
    final Collection<JOTreeNodeReadableType<Integer>> n0_children_ro = n0.childrenReadable();

    assertEquals(3L, n0_children.size());
    assertTrue(n0_children.contains(n1));
    assertTrue(n0_children.contains(n2));
    assertTrue(n0_children.contains(n3));

    assertEquals(3L, n0_children_ro.size());
    assertTrue(n0_children_ro.contains(n1));
    assertTrue(n0_children_ro.contains(n2));
    assertTrue(n0_children_ro.contains(n3));

    n3.detach();

    assertEquals(2L, n0_children.size());
    assertTrue(n0_children.contains(n1));
    assertTrue(n0_children.contains(n2));
    assertFalse(n0_children.contains(n3));

    assertEquals(2L, n0_children_ro.size());
    assertTrue(n0_children_ro.contains(n1));
    assertTrue(n0_children_ro.contains(n2));
    assertFalse(n0_children_ro.contains(n3));

    n2.detach();

    assertEquals(1L, n0_children.size());
    assertTrue(n0_children.contains(n1));
    assertFalse(n0_children.contains(n2));
    assertFalse(n0_children.contains(n3));

    assertEquals(1L, n0_children_ro.size());
    assertTrue(n0_children_ro.contains(n1));
    assertFalse(n0_children_ro.contains(n2));
    assertFalse(n0_children_ro.contains(n3));

    n1.detach();

    assertEquals(0L, n0_children.size());
    assertFalse(n0_children.contains(n1));
    assertFalse(n0_children.contains(n2));
    assertFalse(n0_children.contains(n3));

    assertEquals(0L, n0_children_ro.size());
    assertFalse(n0_children_ro.contains(n1));
    assertFalse(n0_children_ro.contains(n2));
    assertFalse(n0_children_ro.contains(n3));
  }

  @Test
  public final void testSetParentTransfer()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));

    assertEquals(n1, n1.setParent(n0));

    assertFalse(n2.children().contains(n1));
    assertTrue(n0.children().contains(n1));
    assertEquals(n0, n1.parent().get());

    assertEquals(n1, n1.setParent(n2));

    assertTrue(n2.children().contains(n1));
    assertFalse(n0.children().contains(n1));
    assertEquals(n2, n1.parent().get());
  }

  @Test
  public final void testSetParentCyclic0()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));

    n0.setParent(n1);

    assertThrows(JOTreeExceptionCycle.class, () -> {
      n1.setParent(n0);
    });
  }

  @Test
  public final void testSetParentCyclic1()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));

    n0.setParent(n1);
    n1.setParent(n2);
    assertThrows(JOTreeExceptionCycle.class, () -> {
      n2.setParent(n0);
    });
  }

  @Test
  public final void testSortChildren()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));
    final var n3 = this.create(Integer.valueOf(3));

    n1.setParent(n0);
    n2.setParent(n0);
    n3.setParent(n0);

    n0.childrenSort(Comparator.naturalOrder());

    {
      final var nodes = n0.children();
      assertEquals(Integer.valueOf(1), nodes.get(0).value());
      assertEquals(Integer.valueOf(2), nodes.get(1).value());
      assertEquals(Integer.valueOf(3), nodes.get(2).value());
    }

    n0.childrenSort(Comparator.reverseOrder());

    {
      final var nodes = n0.children();
      assertEquals(Integer.valueOf(3), nodes.get(0).value());
      assertEquals(Integer.valueOf(2), nodes.get(1).value());
      assertEquals(Integer.valueOf(1), nodes.get(2).value());
    }
  }

  /**
   * Checks that a parent that raises an exception when a child attempts to
   * setParent it doesn't corrupt the tree.
   */

  @Test
  public final void testSetParentExceptionSafe()
  {
    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));

    final var n_timebomb =
      new ChildAddTimebomb<Integer>(this.create(Integer.valueOf(2)));

    n1.setParent(n0);
    assertTrue(n0.children().contains(n1));
    assertEquals(n0, n1.parent().get());

    var caught = false;

    try {
      n1.setParent(n_timebomb);
    } catch (final HostileImplementationException e) {
      assertEquals("Refusing to add", e.getMessage());
      caught = true;
    }

    assertTrue(caught);
    assertFalse(n0.children().contains(n1));
    assertFalse(n1.parent().isPresent());
  }

  /**
   * Checks that a parent that raises an exception when a child attempts to
   * setParent it doesn't corrupt the tree. This assumes that there isn't a
   * previous parent to restore.
   */

  @Test
  public final void testSetParentExceptionSafeNoPrevious()
  {
    final var n1 = this.create(Integer.valueOf(1));

    var caught = false;

    try {
      n1.setParent(new JOTreeNodeUnimplemented<Integer>()
      {
        @Override
        public JOTreeNodeType<Integer> childAdd(
          final JOTreeNodeType<Integer> child)
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
      assertEquals("Refusing to add", e.getMessage());
      caught = true;
    }

    assertTrue(caught);
    assertFalse(n1.parent().isPresent());
  }

  /**
   * Checks that a parent that raises an exception when it removes a child
   * doesn't corrupt the tree.
   */

  @Test
  public final void testDetachExceptionSafeNoPrevious()
  {
    final var n1 = this.create(Integer.valueOf(1));
    final var n_timebomb =
      new ChildRemoveTimebomb<Integer>(this.create(Integer.valueOf(0)));

    n1.setParent(n_timebomb);

    assertEquals(n_timebomb, n1.parent().get());
    assertTrue(n_timebomb.children().contains(n1));

    var caught = false;

    try {
      n1.detach();
    } catch (final HostileImplementationException e) {
      assertEquals("Refusing to remove", e.getMessage());
      caught = true;
    }

    assertTrue(caught);
    assertEquals(n_timebomb, n1.parent().get());
    assertTrue(n_timebomb.children().contains(n1));
  }

  /**
   * Checks that a child that raises an exception when tries to setParent
   * doesn't corrupt the tree.
   */

  @Test
  public final void testChildSetParentExceptionSafeNoPrevious()
  {
    final var n1 = this.create(Integer.valueOf(1));
    final var n_timebomb =
      new ChildSetParentTimebomb<Integer>(this.create(Integer.valueOf(0)));

    var caught = false;

    try {
      n1.childAdd(n_timebomb);
    } catch (final HostileImplementationException e) {
      assertEquals("Refusing to set parent", e.getMessage());
      caught = true;
    }

    assertTrue(caught);
    assertFalse(n_timebomb.parent().isPresent());
    assertFalse(n1.children().contains(n_timebomb));
  }

  /**
   * Checks that a child that raises an exception when it detaches doesn't
   * corrupt the tree.
   */

  @Test
  public final void testChildRemoveExceptionSafe()
  {
    final var n1 = this.create(Integer.valueOf(1));
    final var n_timebomb =
      new DetachTimebomb<Integer>(this.create(Integer.valueOf(0)));

    n1.childAdd(n_timebomb);

    assertEquals(n1, n_timebomb.parent().get());
    assertTrue(n1.children().contains(n_timebomb));

    var caught = false;

    try {
      n1.childRemove(n_timebomb);
    } catch (final HostileImplementationException e) {
      assertEquals("Refusing to detach", e.getMessage());
      caught = true;
    }

    assertTrue(caught);
    assertEquals(n1, n_timebomb.parent().get());
    assertTrue(n1.children().contains(n_timebomb));
  }

  @Test
  public final void testMapBreadthFirst()
  {
    final Map<String, JOTreeNodeReadableType<String>> nodes_s = new HashMap<>();
    final Map<Integer, JOTreeNodeReadableType<Integer>> nodes_i = new HashMap<>();

    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));
    final var n3 = this.create(Integer.valueOf(3));
    final var n4 = this.create(Integer.valueOf(4));
    final var n5 = this.create(Integer.valueOf(5));
    final var n6 = this.create(Integer.valueOf(6));
    final var n7 = this.create(Integer.valueOf(7));

    n7.setParent(n5);
    n6.setParent(n5);

    n4.setParent(n2);
    n3.setParent(n2);

    n5.setParent(n1);
    n2.setParent(n1);

    n1.setParent(n0);

    final List<Integer> order = new ArrayList<>(10);
    final var r =
      n0.mapBreadthFirst(order, (input, depth, node) -> {
        assertTrue(depth >= 0);
        assertTrue(depth <= 3);
        input.add(node.value());
        nodes_i.put(node.value(), node);
        return node.value().toString();
      });

    dump(n0);
    dump(r);

    r.forEachDepthFirst(Integer.valueOf(0), (input, depth, node) -> {
      assertFalse(nodes_s.containsKey(node.value()));
      nodes_s.put(node.value(), node);
    });

    assertEquals(8L, nodes_s.size());

    for (final var key : nodes_i.keySet()) {
      assertTrue(nodes_s.containsKey(key.toString()));

      final var node_i =
        nodes_i.get(key);
      final var node_s =
        nodes_s.get(key.toString());
      final Collection<JOTreeNodeReadableType<Integer>> children_i =
        node_i.childrenReadable();
      final Collection<JOTreeNodeReadableType<String>> children_s =
        node_s.childrenReadable();

      assertEquals(
        children_i.size(),
        (long) children_s.size());

      for (final var ci : children_i) {
        final var cs = nodes_s.get(ci.value().toString());
        if (cs.parentReadable().isPresent()) {
          final var csp = cs.parentReadable().get();
          final var cip = ci.parentReadable().get();
          assertEquals(csp.value(), cip.value().toString());
        }
      }
    }

    assertEquals(8L, order.size());
    assertEquals(Integer.valueOf(0), order.get(0));
    assertEquals(Integer.valueOf(1), order.get(1));
    assertEquals(Integer.valueOf(5), order.get(2));
    assertEquals(Integer.valueOf(2), order.get(3));
    assertEquals(Integer.valueOf(7), order.get(4));
    assertEquals(Integer.valueOf(6), order.get(5));
    assertEquals(Integer.valueOf(4), order.get(6));
    assertEquals(Integer.valueOf(3), order.get(7));
  }

  @Test
  public final void testMapDepthFirst()
  {
    final Map<String, JOTreeNodeReadableType<String>> nodes_s = new HashMap<>();
    final Map<Integer, JOTreeNodeReadableType<Integer>> nodes_i = new HashMap<>();

    final var n0 = this.create(Integer.valueOf(0));
    final var n1 = this.create(Integer.valueOf(1));
    final var n2 = this.create(Integer.valueOf(2));
    final var n3 = this.create(Integer.valueOf(3));
    final var n4 = this.create(Integer.valueOf(4));
    final var n5 = this.create(Integer.valueOf(5));
    final var n6 = this.create(Integer.valueOf(6));
    final var n7 = this.create(Integer.valueOf(7));

    n7.setParent(n5);
    n6.setParent(n5);

    n4.setParent(n2);
    n3.setParent(n2);

    n5.setParent(n1);
    n2.setParent(n1);

    n1.setParent(n0);

    final List<Integer> order = new ArrayList<>(10);
    final var r =
      n0.mapDepthFirst(order, (input, depth, node) -> {
        assertTrue(depth >= 0);
        assertTrue(depth <= 3);
        input.add(node.value());
        nodes_i.put(node.value(), node);
        return node.value().toString();
      });

    dump(n0);
    dump(r);

    r.forEachDepthFirst(Integer.valueOf(0), (input, depth, node) -> {
      assertFalse(nodes_s.containsKey(node.value()));
      nodes_s.put(node.value(), node);
    });

    assertEquals(8L, nodes_s.size());

    for (final var key : nodes_i.keySet()) {
      assertTrue(nodes_s.containsKey(key.toString()));

      final var node_i =
        nodes_i.get(key);
      final var node_s =
        nodes_s.get(key.toString());
      final Collection<JOTreeNodeReadableType<Integer>> children_i =
        node_i.childrenReadable();
      final Collection<JOTreeNodeReadableType<String>> children_s =
        node_s.childrenReadable();

      assertEquals(
        children_i.size(),
        (long) children_s.size());

      for (final var ci : children_i) {
        final var cs = nodes_s.get(ci.value().toString());
        if (cs.parentReadable().isPresent()) {
          final var csp = cs.parentReadable().get();
          final var cip = ci.parentReadable().get();
          assertEquals(csp.value(), cip.value().toString());
        }
      }
    }

    assertEquals(8L, order.size());
    assertEquals(Integer.valueOf(0), order.get(0));
    assertEquals(Integer.valueOf(1), order.get(1));
    assertEquals(Integer.valueOf(2), order.get(2));
    assertEquals(Integer.valueOf(3), order.get(3));
    assertEquals(Integer.valueOf(4), order.get(4));
    assertEquals(Integer.valueOf(5), order.get(5));
    assertEquals(Integer.valueOf(6), order.get(6));
    assertEquals(Integer.valueOf(7), order.get(7));
  }

  static class JOTreeNodeUnimplemented<A> implements JOTreeNodeType<A>
  {
    @Override
    public boolean isDetachAllowed()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public JOTreeNodeType<A> detach()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public List<JOTreeNodeType<A>> children()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public JOTreeNodeType<A> childRemove(
      final JOTreeNodeType<A> child)
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public JOTreeNodeType<A> childAdd(
      final JOTreeNodeType<A> child)
      throws JOTreeExceptionCycle
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public JOTreeNodeType<A> setParent(
      final JOTreeNodeType<A> new_parent)
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
    public void childrenSortNodes(
      final Comparator<JOTreeNodeType<A>> comparator)
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
    public List<JOTreeNodeReadableType<A>> childrenReadable()
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public boolean isDescendantOf(
      final JOTreeNodeReadableType<A> other)
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

    @Override
    public <T, B> JOTreeNodeType<B> mapDepthFirst(
      final T context,
      final JOTreeNodeMapFunctionType<A, T, B> f)
    {
      throw new UnimplementedCodeException();
    }

    @Override
    public <T, B> JOTreeNodeType<B> mapBreadthFirst(
      final T context,
      final JOTreeNodeMapFunctionType<A, T, B> f)
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
    public JOTreeNodeType<A> childRemove(final JOTreeNodeType<A> child)
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
    public JOTreeNodeType<A> childAdd(final JOTreeNodeType<A> child)
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
    public JOTreeNodeType<A> setParent(final JOTreeNodeType<A> new_parent)
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
    public JOTreeNodeType<A> detach()
    {
      throw new JOTreeNodeContract.HostileImplementationException(
        "Refusing to detach");
    }
  }
}
