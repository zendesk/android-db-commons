package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Query.intersect;
import static com.getbase.android.db.fluentsqlite.Query.select;
import static com.getbase.android.db.fluentsqlite.Query.union;
import static com.getbase.android.db.fluentsqlite.Query.unionAll;
import static com.google.common.truth.Truth.assertThat;
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
public class CompoundQueryBuilderTest {

  @Mock
  private SQLiteDatabase mDb;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldBuildUnionQuery() throws Exception {
    select(select().allColumns().from("table_a"))
        .union(select().allColumns().from("table_b"))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildUnionAllQuery() throws Exception {
    select(select().allColumns().from("table_a"))
        .unionAll(select().allColumns().from("table_b"))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION ALL SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildIntersectQuery() throws Exception {
    select(select().allColumns().from("table_a"))
        .intersect(select().allColumns().from("table_b"))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a INTERSECT SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildExceptQuery() throws Exception {
    select(select().allColumns().from("table_a"))
        .except(select().allColumns().from("table_b"))
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a EXCEPT SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test
  public void shouldBuildCompoundQueryWithOrderByAndLimit() throws Exception {
    select(select().allColumns().from("table_a"))
        .union(select().allColumns().from("table_b"))
        .orderBy("column_a")
        .collate("UNICODE")
        .asc()
        .limit(1500)
        .offset(2900)
        .build()
        .perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION SELECT * FROM table_b ORDER BY column_a COLLATE UNICODE ASC LIMIT 1500 OFFSET 2900"), eq(new String[0]));
  }

  @Test
  public void shouldGetCorrectTablesFromTheCompoundQuery() throws Exception {
    final Query query =
        select(select().allColumns().from("table_a"))
            .union(select().allColumns().from("table_b"))
            .build();

    assertThat(query.getTables()).containsExactly("table_a", "table_b");
  }

  @Test(expected = NullPointerException.class)
  public void shouldRejectNullQueryArrayFromUnionConvenienceMethod() throws Exception {
    union((Query[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectEmptyQueryArrayFromUnionConvenienceMethod() throws Exception {
    union();
  }

  @Test
  public void shouldBuildUnionQueryWithConvenienceMethod() throws Exception {
    Query union = union(
        select().allColumns().from("table_a").build(),
        select().allColumns().from("table_b").build()
    );

    union.perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test(expected = NullPointerException.class)
  public void shouldRejectNullQueryArrayFromUnionAllConvenienceMethod() throws Exception {
    unionAll((Query[]) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectEmptyQueryArrayFromUnionAllConvenienceMethod() throws Exception {
    unionAll();
  }

  @Test
  public void shouldBuildUnionAllQueryWithConvenienceMethod() throws Exception {
    Query unionAll = unionAll(
        select().allColumns().from("table_a").build(),
        select().allColumns().from("table_b").build()
    );

    unionAll.perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION ALL SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test(expected = NullPointerException.class)
  public void shouldRejectNullQueryArrayFromIntersectConvenienceMethod() throws Exception {
    intersect((Query[]) null);
  }

  @Test
  public void shouldBuildIntersectQueryWithConvenienceMethod() throws Exception {
    Query intersection = intersect(
        select().allColumns().from("table_a").build(),
        select().allColumns().from("table_b").build()
    );

    intersection.perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a INTERSECT SELECT * FROM table_b"), eq(new String[0]));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectEmptyQueryArrayFromIntersectConvenienceMethod() throws Exception {
    intersect();
  }

  @Test
  public void shouldBuildNestedCompoundQueries() throws Exception {
    Query intersection = intersect(
        select().allColumns().from("table_a").build(),
        union(
            select().allColumns().from("table_b").build(),
            select().allColumns().from("table_c").build()
        ),
        select().allColumns().from("table_d").build()
    );

    intersection.perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a INTERSECT SELECT * FROM (SELECT * FROM table_b UNION SELECT * FROM table_c) INTERSECT SELECT * FROM table_d"), eq(new String[0]));
  }

  @Test
  public void shouldBuildNestedCompoundQueriesStartingWithCompoundQuery() throws Exception {
    Query intersection = intersect(
        union(
            select().allColumns().from("table_b").build(),
            select().allColumns().from("table_c").build()
        ),
        select().allColumns().from("table_d").build(),
        unionAll(
            select().allColumns().from("table_e").build(),
            select().allColumns().from("table_f").build()
        )
    );

    intersection.perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM (SELECT * FROM table_b UNION SELECT * FROM table_c) INTERSECT SELECT * FROM table_d INTERSECT SELECT * FROM (SELECT * FROM table_e UNION ALL SELECT * FROM table_f)"), eq(new String[0]));
  }

  @Test
  public void shouldBuildIntersectionWithSingleCompoundQuery() throws Exception {
    Query query = intersect(
        select().from("table_a").union().select().from("table_b").build()
    );

    query.perform(mDb);

    verify(mDb).rawQuery(eq("SELECT * FROM table_a UNION SELECT * FROM table_b"), eq(new String[0]));
  }
}
