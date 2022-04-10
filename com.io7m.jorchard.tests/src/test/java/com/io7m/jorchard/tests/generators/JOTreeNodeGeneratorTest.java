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

package com.io7m.jorchard.tests.generators;

import com.io7m.jorchard.core.JOTreeNodeType;
import com.io7m.jorchard.generators.JOTreeNodeGenerator;
import net.java.quickcheck.Generator;
import net.java.quickcheck.QuickCheck;
import net.java.quickcheck.characteristic.AbstractCharacteristic;
import net.java.quickcheck.generator.support.IntegerGenerator;
import net.java.quickcheck.generator.support.StringGenerator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public final class JOTreeNodeGeneratorTest
{
  private static final Logger LOG =
    LoggerFactory.getLogger(JOTreeNodeGeneratorTest.class);

  @Test
  public void testGenerate()
  {
    final int maximum_size = 100;
    final Generator<JOTreeNodeType<String>> gen =
      JOTreeNodeGenerator.create(
        new IntegerGenerator(1, maximum_size),
        new StringGenerator('a', 'z'),
        0.25);

    QuickCheck.forAllVerbose(
      gen,
      new AbstractCharacteristic<JOTreeNodeType<String>>()
      {
        @Override
        protected void doSpecify(final JOTreeNodeType<String> any)
          throws Throwable
        {
          JOTreeNodeGeneratorTest.this.dump(any);

          final AtomicInteger size = new AtomicInteger(0);
          any.forEachDepthFirst(
            size,
            (input, depth, node) -> size.incrementAndGet());

          LOG.debug("size: {}", Integer.valueOf(size.get()));

          Assertions.assertTrue(
            size.get() > 0,
            "Tree is non-empty");
          Assertions.assertTrue(
            size.get() <= maximum_size,
            "Tree has less than " + maximum_size);
        }
      });
  }

  private void dump(final JOTreeNodeType<String> any)
  {
    final StringBuilder sb = new StringBuilder(128);
    any.forEachDepthFirst(Integer.valueOf(0), (input, depth, node) -> {
      sb.setLength(0);
      for (int index = 0; index < depth; ++index) {
        sb.append(" ");
      }
      sb.append(node.value());
      LOG.debug("{}", sb);
    });
  }
}
