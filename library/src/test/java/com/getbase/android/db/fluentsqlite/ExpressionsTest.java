package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Expressions.coalesce;
import static com.getbase.android.db.fluentsqlite.Expressions.column;
import static com.getbase.android.db.fluentsqlite.Expressions.literal;
import static com.getbase.android.db.fluentsqlite.query.QueryBuilder.select;

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

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectInOperatorWithSubqueryWithBoundArgs() throws Exception {
    column("id").in(
        select()
            .column("id")
            .from("table_a")
            .where(column("name").eq().arg(), "Smith")
    );
  }
}
