package com.getbase.android.db.fluentsqlite.query;

import static com.getbase.android.db.fluentsqlite.Expressions.column;
import static com.getbase.android.db.fluentsqlite.Expressions.sum;
import static com.getbase.android.db.fluentsqlite.query.QueryBuilder.select;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.sqlite.SQLiteDatabase;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class QueryTest {

  @Mock
  private SQLiteDatabase mDb;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldBuildTheSimpleSelect() throws Exception {
    select().allColumns().from("table_a").perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheSimpleDistinctSelect() throws Exception {
    QueryBuilder.selectDistinct().allColumns().from("table_a").perform(mDb);

    verify(mDb).rawQuery(eq("SELECT DISTINCT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheUnionCompoundQuery() throws Exception {
    select().allColumns().from("table_a")
        .union()
        .select().allColumns().from("table_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheUnionCompoundQueryWithDistinctSelect() throws Exception {
    select().allColumns().from("table_a")
        .union()
        .selectDistinct().allColumns().from("table_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION SELECT DISTINCT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheUnionAllCompoundQuery() throws Exception {
    select().allColumns().from("table_a")
        .union().all()
        .select().allColumns().from("table_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION ALL SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheExceptCompoundQuery() throws Exception {
    select().allColumns().from("table_a")
        .except()
        .select().allColumns().from("table_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a EXCEPT SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheIntersectCompoundQuery() throws Exception {
    select().allColumns().from("table_a")
        .intersect()
        .select().allColumns().from("table_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a INTERSECT SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithSelection() throws Exception {
    select().allColumns().from("table_a")
        .where("column=?", 0)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (column=?)"), eq(new String[] { "0" }));
  }

  @Test
  public void shouldBuildTheQueryWithMultipleSelections() throws Exception {
    select().allColumns().from("table_a")
        .where("column=?", 0)
        .where("other_column=?", 1)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (column=?) AND (other_column=?)"), eq(new String[] { "0", "1" }));
  }

  @Test
  public void shouldBuildTheQueryWithLeftJoin() throws Exception {
    select().allColumns().from("table_a")
        .left().join("table_b")
        .on("column_a=?", 0)
        .where("column_b=?", 1)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LEFT JOIN table_b ON (column_a=?) WHERE (column_b=?)"), eq(new String[] { "0", "1" }));
  }

  @Test
  public void shouldBuildTheQueryWithCrossJoin() throws Exception {
    select().allColumns().from("table_a")
        .cross().join("table_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a CROSS JOIN table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithNaturalJoin() throws Exception {
    select().allColumns().from("table_a")
        .natural().join("table_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a NATURAL JOIN table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithAliasedJoin() throws Exception {
    select().allColumns().from("table_a")
        .join("table_b").as("b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b AS b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryJoinedWithSubquery() throws Exception {
    select().allColumns().from("table_a")
        .join(
            select().allColumns().from("table_b")
        )
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN (SELECT * FROM table_b)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryFromSubquery() throws Exception {
    select()
        .allColumns()
        .from(
            select().allColumns().from("table_a")
        )
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM (SELECT * FROM table_a)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildTheQueryWithJoinUsingColumnList() throws Exception {
    select().allColumns().from("table_a")
        .join("table_b")
        .using("col_b", "col_c")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b USING (col_b, col_c)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithSingleColumnProjection() throws Exception {
    select()
        .column("a")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithAliasedColumnProjection() throws Exception {
    select()
        .column("a").as("aaa")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a AS aaa FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldConcatenateProjections() throws Exception {
    select()
        .column("a")
        .column("b")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a, b FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryForAllColumnsFromSpecifiedTable() throws Exception {
    select()
        .allColumns().of("table_a")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT table_a.* FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithAliasedTable() throws Exception {
    select()
        .from("table_a").as("a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a AS a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptEmptyProjection() throws Exception {
    select()
        .column("a")
        .columns()
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptNullProjection() throws Exception {
    select()
        .columns((String[]) null)
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldSelectAllColumnsWhenProjectionIsNotSpecified() throws Exception {
    select()
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptNullSelection() throws Exception {
    select()
        .from("table_a")
        .where((String) null)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldAcceptNullSortOrder() throws Exception {
    select()
        .from("table_a")
        .orderBy((String) null)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithNumericLimit() throws Exception {
    select()
        .from("table_a")
        .limit(1)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LIMIT 1"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionLimit() throws Exception {
    select()
        .from("table_a")
        .limit("1+1")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LIMIT 1+1"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithNumericLimitOffset() throws Exception {
    select()
        .from("table_a")
        .limit(1)
        .offset(1)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LIMIT 1 OFFSET 1"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionLimitOffset() throws Exception {
    select()
        .from("table_a")
        .limit(1)
        .offset("1+1")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a LIMIT 1 OFFSET 1+1"), eq(new String[0]));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldAllowSettingTheLimitOnlyOnce() throws Exception {
    select()
        .from("table_a")
        .limit(1)
        .limit(1);
  }

  @Test
  public void shouldBuildQueryWithoutAnyTables() throws Exception {
    select()
        .column("1500")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT 1500"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithGroupByClause() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithMultipleGroupByClauses() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .groupBy("col_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a, col_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithGroupByAndHavingClause() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having("col_b=?", 1)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (col_b=?)"), eq(new String[] { "1" }));
  }

  @Test
  public void shouldBuildQueryWithGroupByAndMultipleHavingClauses() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having("col_b=?", 1)
        .having("col_c=?", 2)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (col_b=?) AND (col_c=?)"), eq(new String[] { "1", "2" }));
  }

  @Test
  public void shouldIgnoreNullLimit() throws Exception {
    select()
        .from("table_a")
        .limit(null)
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a"), eq(new String[0]));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowSettingValidNumericalOffsetAfterNullLimit() throws Exception {
    select()
        .from("table_a")
        .limit(null)
        .offset(1);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowSettingValidExpressionOffsetAfterNullLimit() throws Exception {
    select()
        .from("table_a")
        .limit(null)
        .offset("1+1");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldNotAllowHavingClauseWithoutGroupByClause() throws Exception {
    select()
        .from("table_a")
        .having("col_a=?", 1)
        .perform(mDb);
  }

  @Test
  public void shouldBuildQueryWithOrderByWithoutSpecifiedSorting() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithOrderByWithAscSort() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .asc()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a ASC"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithOrderByWithDescSort() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .desc()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a DESC"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithMultipleOrderByClauses() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .orderBy("col_b")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a, col_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithOrderByWithSpecifiedCollation() throws Exception {
    select()
        .from("table_a")
        .orderBy("col_a")
        .collate("NOCASE")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a COLLATE NOCASE"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInProjection() throws Exception {
    select()
        .expr(column("col_a"))
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT col_a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInOrderBy() throws Exception {
    select()
        .from("table_a")
        .orderBy(column("col_a"))
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a ORDER BY col_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInSelection() throws Exception {
    select()
        .from("table_a")
        .where(column("col_a").is().not().nul())
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a WHERE (col_a IS NOT NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInJoinConstraint() throws Exception {
    select()
        .from("table_a")
        .join("table_b")
        .on(column("table_a", "id").eq().column("table_b", "id_a"))
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a JOIN table_b ON (table_a.id == table_b.id_a)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInGroupByClause() throws Exception {
    select()
        .from("table_a")
        .groupBy(column("col_a"))
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithExpressionInHavingClause() throws Exception {
    select()
        .from("table_a")
        .groupBy("col_a")
        .having(sum(column("col_b")).gt().literal(0))
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a GROUP BY col_a HAVING (SUM(col_b) > 0)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithMultipleColumnsFromSingleTable() throws Exception {
    select()
        .columns("col_a", "col_b", "col_c").of("table_a")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT table_a.col_a, table_a.col_b, table_a.col_c FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingNullBuildByConvenienceMethod() throws Exception {
    select()
        .nul().as("col_a")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT NULL AS col_a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingNumericLiteralBuildByConvenienceMethod() throws Exception {
    select()
        .literal(1500).as("col_a")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT 1500 AS col_a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingObjectLiteralBuildByConvenienceMethod() throws Exception {
    select()
        .literal("test").as("col_a")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT 'test' AS col_a FROM table_a"), eq(new String[0]));
  }

  @Test
  public void shouldBuildQueryWithProjectionContainingFullyQualifiedTableBuildByConvenienceMethod() throws Exception {
    select()
        .column("table_a", "col_a")
        .from("table_a")
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT table_a.col_a FROM table_a"), eq(new String[0]));
  }
}
