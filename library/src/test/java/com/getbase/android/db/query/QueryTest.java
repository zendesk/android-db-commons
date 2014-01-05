package com.getbase.android.db.query;

import static org.fest.assertions.Assertions.assertThat;

import com.getbase.android.db.cursors.Cursors;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.MatrixCursor;

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
  public void shouldOverrideColumnsWithAliasedColumns() throws Exception {
    Query query = Query
        .select()
        .columns("a", "b", "c")
        .column("NULL").as("c")
        .from("table_a")
        .build();

    assertThat(query.mRawQueryArgs).isEmpty();
    assertThat(query.mRawQuery).isEqualTo("SELECT a, b, NULL AS c FROM table_a");
  }

  @Test
  public void shouldAcceptNullProjection() throws Exception {
    Query query = Query
        .select()
        .columns(null)
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
}
