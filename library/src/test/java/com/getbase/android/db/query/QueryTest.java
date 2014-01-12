package com.getbase.android.db.query;

import static com.getbase.android.db.query.Expressions.column;
import static com.getbase.android.db.query.Expressions.sum;
import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class QueryTest {

  @Test
  public void shouldBuildTheSimpleSelect() throws Exception {
    Query query = Query.select().allColumns().from("table_a").build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a");
  }

  @Test
  public void shouldBuildTheSimpleDistinctSelect() throws Exception {
    Query query = Query.selectDistinct().allColumns().from("table_a").build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT DISTINCT * FROM table_a");
  }

  @Test
  public void shouldBuildTheUnionCompoundQuery() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .union()
        .select().allColumns().from("table_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a UNION SELECT * FROM table_b");
  }

  @Test
  public void shouldBuildTheUnionCompoundQueryWithDistinctSelect() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .union()
        .selectDistinct().allColumns().from("table_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a UNION SELECT DISTINCT * FROM table_b");
  }

  @Test
  public void shouldBuildTheUnionAllCompoundQuery() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .union().all()
        .select().allColumns().from("table_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a UNION ALL SELECT * FROM table_b");
  }

  @Test
  public void shouldBuildTheExceptCompoundQuery() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .except()
        .select().allColumns().from("table_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a EXCEPT SELECT * FROM table_b");
  }

  @Test
  public void shouldBuildTheIntersectCompoundQuery() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .intersect()
        .select().allColumns().from("table_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a INTERSECT SELECT * FROM table_b");
  }

  @Test
  public void shouldBuildTheQueryWithSelection() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .where("column=?", 0)
        .build();

    assertThat(query.mRawQueryArgs).containsOnly("0");
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a WHERE (column=?)");
  }

  @Test
  public void shouldBuildTheQueryWithMultipleSelections() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .where("column=?", 0)
        .where("other_column=?", 1)
        .build();

    assertThat(query.mRawQueryArgs).containsSequence("0", "1");
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a WHERE (column=?) AND (other_column=?)");
  }

  @Test
  public void shouldBuildTheQueryWithLeftJoin() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .left().join("table_b")
        .on("column_a=?", 0)
        .where("column_b=?", 1)
        .build();

    assertThat(query.mRawQueryArgs).containsSequence("0", "1");
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a LEFT JOIN table_b ON (column_a=?) WHERE (column_b=?)");
  }

  @Test
  public void shouldBuildTheQueryWithCrossJoin() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .cross().join("table_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a CROSS JOIN table_b");
  }

  @Test
  public void shouldBuildTheQueryWithNaturalJoin() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .natural().join("table_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a NATURAL JOIN table_b");
  }

  @Test
  public void shouldBuildTheQueryWithAliasedJoin() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .join("table_b").as("b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a JOIN table_b AS b");
  }

  @Test
  public void shouldBuildTheQueryJoinedWithSubquery() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .join(
            Query.select().allColumns().from("table_b").build()
        )
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a JOIN (SELECT * FROM table_b)");
  }

  @Test
  public void shouldBuildTheQueryFromSubquery() throws Exception {
    Query query = Query
        .select()
        .allColumns()
        .from(
            Query.select().allColumns().from("table_a").build()
        )
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM (SELECT * FROM table_a)");
  }

  @Test
  public void shouldBuildTheQueryWithJoinUsingColumnList() throws Exception {
    Query query = Query
        .select().allColumns().from("table_a")
        .join("table_b")
        .using("col_b", "col_c")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a JOIN table_b USING (col_b, col_c)");
  }

  @Test
  public void shouldBuildQueryWithSingleColumnProjection() throws Exception {
    Query query = Query
        .select()
        .column("a")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT a FROM table_a");
  }

  @Test
  public void shouldBuildQueryWithAliasedColumnProjection() throws Exception {
    Query query = Query
        .select()
        .column("a").as("aaa")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT a AS aaa FROM table_a");
  }

  @Test
  public void shouldConcatenateProjections() throws Exception {
    Query query = Query
        .select()
        .column("a")
        .column("b")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT a, b FROM table_a");
  }

  @Test
  public void shouldBuildQueryForAllColumnsFromSpecifiedTable() throws Exception {
    Query query = Query
        .select()
        .allColumns().of("table_a")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT table_a.* FROM table_a");
  }

  @Test
  public void shouldBuildQueryWithAliasedTable() throws Exception {
    Query query = Query
        .select()
        .from("table_a").as("a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a AS a");
  }

  @Test
  public void shouldAcceptEmptyProjection() throws Exception {
    Query query = Query
        .select()
        .column("a")
        .columns()
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT a FROM table_a");
  }

  @Test
  public void shouldAcceptNullProjection() throws Exception {
    Query query = Query
        .select()
        .columns((String[]) null)
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a");
  }

  @Test
  public void shouldSelectAllColumnsWhenProjectionIsNotSpecified() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a");
  }

  @Test
  public void shouldAcceptNullSelection() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .where((String)null)
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a");
  }

  @Test
  public void shouldAcceptNullSortOrder() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .orderBy((String)null)
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a");
  }

  @Test
  public void shouldBuildQueryWithNumericLimit() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .limit(1)
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a LIMIT 1");
  }

  @Test
  public void shouldBuildQueryWithExpressionLimit() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .limit("1+1")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a LIMIT 1+1");
  }

  @Test
  public void shouldBuildQueryWithNumericLimitOffset() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .limit(1)
        .offset(1)
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a LIMIT 1 OFFSET 1");
  }

  @Test
  public void shouldBuildQueryWithExpressionLimitOffset() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .limit(1)
        .offset("1+1")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a LIMIT 1 OFFSET 1+1");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldAllowSettingTheLimitOnlyOnce() throws Exception {
    Query
        .select()
        .from("table_a")
        .limit(1)
        .limit(1);
  }

  @Test
  public void shouldBuildQueryWithoutAnyTables() throws Exception {
    Query query = Query
        .select()
        .column("1500")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT 1500");
  }

  @Test
  public void shouldBuildQueryWithGroupByClause() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .groupBy("col_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a GROUP BY col_a");
  }

  @Test
  public void shouldBuildQueryWithMultipleGroupByClauses() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .groupBy("col_a")
        .groupBy("col_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a GROUP BY col_a, col_b");
  }

  @Test
  public void shouldBuildQueryWithGroupByAndHavingClause() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .groupBy("col_a")
        .having("col_b=?", 1)
        .build();

    assertThat(query.mRawQueryArgs).containsSequence("1");
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a GROUP BY col_a HAVING (col_b=?)");
  }

  @Test
  public void shouldBuildQueryWithGroupByAndMultipleHavingClauses() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .groupBy("col_a")
        .having("col_b=?", 1)
        .having("col_c=?", 2)
        .build();

    assertThat(query.mRawQueryArgs).containsSequence("1", "2");
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a GROUP BY col_a HAVING (col_b=?) AND (col_c=?)");
  }

  @Test
  public void shouldIgnoreNullLimit() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .limit(null)
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowSettingValidNumericalOffsetAfterNullLimit() throws Exception {
    Query
        .select()
        .from("table_a")
        .limit(null)
        .offset(1);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowSettingValidExpressionOffsetAfterNullLimit() throws Exception {
    Query
        .select()
        .from("table_a")
        .limit(null)
        .offset("1+1");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowHavingClauseWithoutGroupByClause() throws Exception {
    Query
        .select()
        .from("table_a")
        .having("col_a=?", 1)
        .build();
  }

  @Test
  public void shouldBuildQueryWithOrderByWithoutSpecifiedSorting() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .orderBy("col_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a ORDER BY col_a");
  }

  @Test
  public void shouldBuildQueryWithOrderByWithAscSort() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .orderBy("col_a")
        .asc()
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a ORDER BY col_a ASC");
  }

  @Test
  public void shouldBuildQueryWithOrderByWithDescSort() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .orderBy("col_a")
        .desc()
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a ORDER BY col_a DESC");
  }

  @Test
  public void shouldBuildQueryWithMultipleOrderByClauses() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .orderBy("col_a")
        .orderBy("col_b")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a ORDER BY col_a, col_b");
  }

  @Test
  public void shouldBuildQueryWithOrderByWithSpecifiedCollation() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .orderBy("col_a")
        .collate("NOCASE")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a ORDER BY col_a COLLATE NOCASE");
  }

  @Test
  public void shouldBuildQueryWithExpressionInProjection() throws Exception {
    Query query = Query
        .select()
        .expr(column("col_a"))
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT col_a FROM table_a");
  }

  @Test
  public void shouldBuildQueryWithExpressionInOrderBy() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .orderBy(column("col_a"))
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a ORDER BY col_a");
  }

  @Test
  public void shouldBuildQueryWithExpressionInSelection() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .where(column("col_a").is().not().nul())
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a WHERE (col_a IS NOT NULL)");
  }

  @Test
  public void shouldBuildQueryWithExpressionInJoinConstraint() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .join("table_b")
        .on(column("table_a", "id").eq().column("table_b", "id_a"))
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a JOIN table_b ON (table_a.id == table_b.id_a)");
  }

  @Test
  public void shouldBuildQueryWithExpressionInGroupByClause() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .groupBy(column("col_a"))
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a GROUP BY col_a");
  }

  @Test
  public void shouldBuildQueryWithExpressionInHavingClause() throws Exception {
    Query query = Query
        .select()
        .from("table_a")
        .groupBy("col_a")
        .having(sum(column("col_b")).gt().literal(0))
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT * FROM table_a GROUP BY col_a HAVING (SUM(col_b) > 0)");
  }

  @Test
  public void shouldBuildQueryWithMultipleColumnsFromSingleTable() throws Exception {
    Query query = Query
        .select()
        .columns("col_a", "col_b", "col_c").of("table_a")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT table_a.col_a, table_a.col_b, table_a.col_c FROM table_a");
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingNullBuildByConvenienceMethod() throws Exception {
    Query query = Query
        .select()
        .nul().as("col_a")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT NULL AS col_a FROM table_a");
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingNumericLiteralBuildByConvenienceMethod() throws Exception {
    Query query = Query
        .select()
        .literal(1500).as("col_a")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT 1500 AS col_a FROM table_a");
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingObjectLiteralBuildByConvenienceMethod() throws Exception {
    Query query = Query
        .select()
        .literal("test").as("col_a")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT 'test' AS col_a FROM table_a");
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingFullyQualifiedTableBuildByConvenienceMethod() throws Exception {
    Query query = Query
        .select()
        .column("table_a", "col_a")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT table_a.col_a FROM table_a");
  }
}
