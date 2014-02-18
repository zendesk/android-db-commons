package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Expressions.arg;
import static com.getbase.android.db.fluentsqlite.Expressions.coalesce;
import static com.getbase.android.db.fluentsqlite.Expressions.column;
import static com.getbase.android.db.fluentsqlite.Expressions.literal;
import static com.getbase.android.db.fluentsqlite.QueryBuilder.select;
import static org.fest.assertions.Assertions.assertThat;

import com.getbase.android.db.fluentsqlite.Expressions.Expression;
import com.google.common.collect.ImmutableMap;

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

  @Test
  public void shouldReturnBoundArgsFromSubqueries() throws Exception {
    Expression expression = column("deleted").eq(arg())
        .and().column("id").in(
            select()
                .column("id")
                .from("table_a")
                .where(column("name").eq().arg(), "Smith")
        )
        .and().column("priority").eq().arg();

    assertThat(expression.getBoundArgs()).isEqualTo(ImmutableMap.of(1, "Smith"));
  }
}
