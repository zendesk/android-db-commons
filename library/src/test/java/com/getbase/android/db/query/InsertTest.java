package com.getbase.android.db.query;

import static com.getbase.android.db.query.Query.select;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.android.content.ContentValuesEntry.entry;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.ContentValues;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class InsertTest {

  @Test
  public void shouldUseTableSpecifiedInIntoStep() throws Exception {
    Insert insert = Insert.insert().into("A").defaultValues("nullable_col");

    assertThat(insert.mTable).isEqualTo("A");
  }

  @Test
  public void shouldBuildTheInsertForDefaultValues() throws Exception {
    Insert insert = Insert.insert().into("A").defaultValues("nullable_col");

    assertThat(insert.mValues).isEmpty();
    assertThat(insert.mNullColumnHack).isEqualTo("nullable_col");
  }

  @Test
  public void shouldBuildTheInsertInSelectFormWithoutSpecifiedColumns() throws Exception {
    Query query = select().allColumns().from("B").build();
    Insert insert = Insert.insert().into("A").select(query);

    assertThat(insert.mQueryFormString).isEqualTo("INSERT INTO A " + query.mRawQuery);
  }

  @Test
  public void shouldUseQueryArgsForInsertInSelectForm() throws Exception {
    Query query = select().allColumns().from("B").where("col=?", 0).build();
    Insert insert = Insert.insert().into("A").select(query);

    assertThat(insert.mQueryFormString).isEqualTo("INSERT INTO A " + query.mRawQuery);
    assertThat(insert.mQueryFormArgs).containsOnly(query.mRawQueryArgs.toArray());
  }

  @Test
  public void shouldBuildTheInsertInSelectFormWithSpecifiedColumns() throws Exception {
    Query query = select().allColumns().from("B").build();
    Insert insert = Insert.insert().into("A").columns("a", "b", "c").select(query);

    assertThat(insert.mQueryFormString).isEqualTo("INSERT INTO A (a, b, c) " + query.mRawQuery);
  }

  @Test
  public void shouldConcatenateSpecifiedColumnsForInsertInSelectForm() throws Exception {
    Query query = select().allColumns().from("B").build();
    Insert insert = Insert.insert().into("A").columns("a", "b").columns("c").select(query);

    assertThat(insert.mQueryFormString).isEqualTo("INSERT INTO A (a, b, c) " + query.mRawQuery);
  }

  @Test
  public void shouldBuildInsertWithSingleValue() throws Exception {
    Insert insert = Insert.insert().into("A").value("col1", "val1").build();

    assertThat(insert.mValues).contains(entry("col1", "val1"));
  }

  @Test
  public void shouldNotModifyPassedContentValues() throws Exception {
    ContentValues values = new ContentValues();

    Insert.insert()
        .into("A")
        .values(values)
        .value("key", "value")
        .build();

    assertThat(values.containsKey("key")).isFalse();

    ContentValues valuesToConcatenate = new ContentValues();
    valuesToConcatenate.put("another_key", "another_value");

    Insert.insert()
        .into("A")
        .values(values)
        .values(valuesToConcatenate)
        .build();

    Assertions.assertThat(values.containsKey("another_key")).isFalse();
  }

  @Test
  public void shouldBuildInsertWithConcatenatedContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");

    ContentValues secondValues = new ContentValues();
    secondValues.put("col2", "val2");

    Insert insert = Insert.insert()
        .into("A")
        .values(firstValues)
        .values(secondValues)
        .build();

    assertThat(insert.mValues).contains(entry("col1", "val1"), entry("col2", "val2"));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenBySingleValue() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    values.put("col2", "val2");

    Insert insert = Insert.insert()
        .into("A")
        .values(values)
        .value("col2", null)
        .build();

    assertThat(insert.mValues).contains(entry("col1", "val1"), entry("col2", null));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenByOtherContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");
    firstValues.put("col2", "val2");

    ContentValues secondValues = new ContentValues();
    secondValues.putNull("col2");
    secondValues.put("col3", "val3");

    Insert insert = Insert.insert()
        .into("A")
        .values(firstValues)
        .values(secondValues)
        .build();

    assertThat(insert.mValues).contains(entry("col1", "val1"), entry("col3", "val3"), entry("col2", null));
  }
}
