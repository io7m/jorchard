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

package com.io7m.jorchard.generators;

import com.io7m.jnull.NullCheck;
import com.io7m.jorchard.core.JOTreeNode;
import com.io7m.jorchard.core.JOTreeNodeType;
import net.java.quickcheck.Generator;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A generator for trees.
 *
 * @param <T> The type of node values
 */

public final class JOTreeNodeGenerator<T> implements Generator<JOTreeNodeType<T>>
{
  private final Generator<T> gen;
  private final Generator<Integer> size_gen;
  private final double branch_chance;

  /**
   * Create a new generator.
   *
   * @param in_size_gen      A generator that will pick a size for the generated
   *                         tree
   * @param in_gen           A value generator
   * @param in_branch_chance The chance that a node will branch (in the range
   *                         {@code [0, 1]})
   * @param <T>              The type of tree nodes
   *
   * @return A new generator
   */

  public static <T> Generator<JOTreeNodeType<T>> create(
    final Generator<Integer> in_size_gen,
    final Generator<T> in_gen,
    final double in_branch_chance)
  {
    return new JOTreeNodeGenerator<T>(in_size_gen, in_gen, in_branch_chance);
  }

  private JOTreeNodeGenerator(
    final Generator<Integer> in_size_gen,
    final Generator<T> in_gen,
    final double in_branch_chance)
  {
    this.size_gen = NullCheck.notNull(in_size_gen, "Size generator");
    this.gen = NullCheck.notNull(in_gen, "Generator");
    this.branch_chance = in_branch_chance;
  }

  @Override
  public JOTreeNodeType<T> next()
  {
    final int size = this.size_gen.next().intValue();
    final JOTreeNodeType<T> root = JOTreeNode.create(this.gen.next());
    final AtomicInteger count = new AtomicInteger(size - 1);
    this.addNodes(root, count);
    return root;
  }

  private void addNodes(
    final JOTreeNodeType<T> node,
    final AtomicInteger count)
  {
    while (count.get() > 0) {
      final JOTreeNodeType<T> child = JOTreeNode.create(this.gen.next());
      node.childAdd(child);
      count.decrementAndGet();
      if (Math.random() < this.branch_chance) {
        this.addNodes(child, count);
      }
    }
  }
}
