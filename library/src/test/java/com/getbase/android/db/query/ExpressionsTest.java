package com.getbase.android.db.query;

import static com.getbase.android.db.query.Expressions.coalesce;
import static com.getbase.android.db.query.Expressions.literal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ExpressionsTest {
  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectCoalesceWithNoArguments() throws Exception {
    coalesce();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectCoalesceWithOneArguments() throws Exception {
    coalesce(literal(666));
  }
}
