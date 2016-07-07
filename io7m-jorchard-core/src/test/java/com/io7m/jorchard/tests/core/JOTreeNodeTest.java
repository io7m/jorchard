package com.io7m.jorchard.tests.core;

import com.io7m.jorchard.core.JOTreeNode;
import com.io7m.jorchard.core.JOTreeNodeType;

public final class JOTreeNodeTest extends JOTreeNodeContract
{
  @Override
  protected <A> JOTreeNodeType<A> create(final A x)
  {
    return JOTreeNode.create(x);
  }
}
