package com.getbase.android.db.query;

import static com.getbase.android.db.query.Expressions.column;
import static com.getbase.android.db.query.Expressions.not;
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
