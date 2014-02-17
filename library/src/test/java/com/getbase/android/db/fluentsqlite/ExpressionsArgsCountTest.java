package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Expressions.arg;
import static com.getbase.android.db.fluentsqlite.Expressions.column;
import static com.getbase.android.db.fluentsqlite.Expressions.literal;
import static com.getbase.android.db.fluentsqlite.QueryBuilder.select;
import static org.fest.assertions.Assertions.assertThat;

import com.getbase.android.db.fluentsqlite.Expressions.Expression;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.ParameterizedRobolectricTestRunner.Parameters;
import org.robolectric.annotation.Config;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

@RunWith(ParameterizedRobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ExpressionsArgsCountTest {
  private int mActual;
  private int mExpected;

  public ExpressionsArgsCountTest(int actual, int expected) {
    mActual = actual;
    mExpected = expected;
  }

  private static final Map<Expression, Integer> TEST_CASES = ImmutableMap.<Expression, Integer>builder()
      .put(literal(42), 0)
      .put(arg(), 1)
      .put(arg().eq().arg(), 2)
      .put(arg().eq(arg()), 2)
      .put(column("id").in(select().column("id").from("table_a").where(column("priority").eq().arg(), "1")), 1)
      .put(column("id").in(select().column("id").from("table_a").where("priority=?", "1")), 1)
      .build();

  @Parameters
  public static Collection<Object[]> data() {
    return FluentIterable
        .from(TEST_CASES.entrySet())
        .transform(new Function<Entry<Expression, Integer>, Object[]>() {
          @Override
          public Object[] apply(Entry<Expression, Integer> input) {
            return new Object[] {
                input.getKey().getArgsCount(),
                input.getValue()
            };
          }
        })
        .toList();
  }

  @Test
  public void shouldCountExpressionArgsSql() throws Exception {
    assertThat(mActual).isEqualTo(mExpected);
  }
}
