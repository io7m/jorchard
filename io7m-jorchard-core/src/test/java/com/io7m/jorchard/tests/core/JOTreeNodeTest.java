package com.io7m.jorchard.tests.core;

import com.io7m.jorchard.core.JOTreeNode;
import com.io7m.jorchard.core.JOTreeNodeType;

import java.util.function.BooleanSupplier;

public final class JOTreeNodeTest extends JOTreeNodeContract
{
  @Override
  protected <A> JOTreeNodeType<A> create(final A x)
  {
    return JOTreeNode.create(x);
  }

  @Override
  protected <A> JOTreeNodeType<A> createWithDetachCheck(
    final A x,
    final BooleanSupplier detach_check)
  {
    return JOTreeNode.createWithDetachCheck(x, detach_check);
  }
}
