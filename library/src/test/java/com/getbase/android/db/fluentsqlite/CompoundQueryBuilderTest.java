package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Query.select;
import static org.fest.assertions.Assertions.assertThat;
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

    assertThat(query.getTables()).containsOnly("table_a", "table_b");
  }
}
