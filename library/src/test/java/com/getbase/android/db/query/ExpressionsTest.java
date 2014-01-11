package com.getbase.android.db.query;

import static com.getbase.android.db.query.Expressions.arg;
import static com.getbase.android.db.query.Expressions.coalesce;
import static com.getbase.android.db.query.Expressions.column;
import static com.getbase.android.db.query.Expressions.concat;
import static com.getbase.android.db.query.Expressions.count;
import static com.getbase.android.db.query.Expressions.ifNull;
import static com.getbase.android.db.query.Expressions.join;
import static com.getbase.android.db.query.Expressions.length;
import static com.getbase.android.db.query.Expressions.literal;
import static com.getbase.android.db.query.Expressions.max;
import static com.getbase.android.db.query.Expressions.min;
import static com.getbase.android.db.query.Expressions.not;
import static com.getbase.android.db.query.Expressions.nul;
import static com.getbase.android.db.query.Expressions.nullIf;
import static com.getbase.android.db.query.Expressions.sum;
import static org.fest.assertions.Assertions.assertThat;

import com.getbase.android.db.query.Expressions.Expression;
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
public class ExpressionsTest {
  private String mActual;
  private String mExpected;

  public ExpressionsTest(String actual, String expected) {
    mActual = actual;
    mExpected = expected;
  }

  private static final Map<Expression, String> TEST_CASES = ImmutableMap.<Expression, String>builder()
      .put(column("col_a"), "col_a")
      .put(not().column("col_a"), "NOT col_a")
      .put(column("col_a").eq().column("col_b"), "col_a == col_b")
      .put(column("col_a").eq().not().column("col_b"), "col_a == NOT col_b")
      .put(column("col_a").eq(column("col_b")), "col_a == (col_b)")
      .put(column("table_a", "col_a"), "table_a.col_a")
      .put(arg().eq().column("table_a", "col_a"), "? == table_a.col_a")
      .put(nul(), "NULL")
      .put(column("col_a").is().nul(), "col_a IS NULL")
      .put(column("col_a").is(nul()), "col_a IS (NULL)")
      .put(literal("WAT?").eq().column("col_a"), "'WAT?' == col_a")
      .put(literal(2900), "2900")
      .put(column("col_a").eq().literal(1500), "col_a == 1500")
      .put(sum(column("col_a")), "SUM(col_a)")
      .put(min(column("col_a")), "MIN(col_a)")
      .put(max(column("col_a")), "MAX(col_a)")
      .put(count(column("col_a")), "COUNT(col_a)")
      .put(count(), "COUNT(*)")
      .put(ifNull(column("col_a"), literal("unknown")), "ifnull(col_a, 'unknown')")
      .put(nullIf(column("col_a"), literal("")), "nullif(col_a, '')")
      .put(coalesce(column("col_a"), column("col_b"), literal("unknown")), "coalesce(col_a, col_b, 'unknown')")
      .put(length(column("col_a")), "length(col_a)")
      .put(concat(column("col_a"), literal(" at "), column("col_b")), "col_a || ' at ' || col_b")
      .put(join(" ", column("col_a"), column("col_b")), "col_a || ' ' || col_b")
      .build();

  @Parameters
  public static Collection<Object[]> data() {
    return FluentIterable
        .from(TEST_CASES.entrySet())
        .transform(new Function<Entry<Expression, String>, Object[]>() {
          @Override
          public Object[] apply(Entry<Expression, String> input) {
            return new Object[] {
                input.getKey().toRawSql(),
                input.getValue()
            };
          }
        })
        .toImmutableList();
  }

  @Test
  public void shouldBuildCorrectSql() throws Exception {
    assertThat(mActual).isEqualTo(mExpected);
  }
}
