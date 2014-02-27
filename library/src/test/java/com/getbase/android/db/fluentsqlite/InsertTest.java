package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Insert.insert;
import static com.getbase.android.db.fluentsqlite.QueryBuilder.select;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.android.content.ContentValuesEntry.entry;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import com.getbase.android.db.fluentsqlite.Insert.DefaultValuesInsert;
import com.getbase.android.db.fluentsqlite.QueryBuilder.Query;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class InsertTest {

  @Mock
  private SQLiteDatabase mDb;

  @Mock
  private SQLiteStatement mStatement;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);

    when(mDb.compileStatement(anyString())).thenReturn(mStatement);
  }

  @Test
  public void shouldUseTableSpecifiedInIntoStepInInsertForDefaultValues() throws Exception {
    DefaultValuesInsert insert = insert().into("A").defaultValues("nullable_col");

    assertThat(insert.mTable).isEqualTo("A");
  }

  @Test
  public void shouldUseTableSpecifiedInIntoStepInInsertWithValues() throws Exception {
    Insert insert = insert().into("A").value("nullable_col", null);

    assertThat(insert.mTable).isEqualTo("A");
  }

  @Test
  public void shouldBuildTheInsertForDefaultValues() throws Exception {
    DefaultValuesInsert insert = insert().into("A").defaultValues("nullable_col");

    assertThat(insert.mNullColumnHack).isEqualTo("nullable_col");
  }

  @Test
  public void shouldBuildTheInsertInSelectFormWithoutSpecifiedColumns() throws Exception {
    Query query = select().allColumns().from("B");
    insert().into("A").resultOf(query).perform(mDb);

    verify(mDb).compileStatement(eq("INSERT INTO A " + query.toRawQuery().mRawQuery));
    verify(mStatement).executeInsert();
  }

  @Test
  public void shouldAllowUsingQueryWithBoundArgsForInsertInSelectForm() throws Exception {
    insert()
        .into("A")
        .resultOf(select()
            .allColumns()
            .from("B")
            .where("col=?", 0)
        )
        .perform(mDb);

    verify(mDb).compileStatement(eq("INSERT INTO A SELECT * FROM B WHERE (col=?)"));
    verify(mStatement).bindString(eq(1), eq("0"));
    verify(mStatement).executeInsert();
  }

  @Test
  public void shouldBuildTheInsertInSelectFormWithSpecifiedColumns() throws Exception {
    Query query = select().allColumns().from("B");
    insert().into("A").columns("a", "b", "c").resultOf(query).perform(mDb);

    verify(mDb).compileStatement(eq("INSERT INTO A (a, b, c) " + query.toRawQuery().mRawQuery));
    verify(mStatement).executeInsert();
  }

  @Test
  public void shouldConcatenateSpecifiedColumnsForInsertInSelectForm() throws Exception {
    Query query = select().allColumns().from("B");
    insert().into("A").columns("a", "b").columns("c").resultOf(query).perform(mDb);

    verify(mDb).compileStatement(eq("INSERT INTO A (a, b, c) " + query.toRawQuery().mRawQuery));
    verify(mStatement).executeInsert();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectNullColumnsListInInsertInSelectForm() throws Exception {
    Query query = select().allColumns().from("B");
    insert().into("A").columns((String[]) null).resultOf(query).perform(mDb);
  }

  @Test
  public void shouldBuildInsertWithSingleValue() throws Exception {
    Insert insert = insert().into("A").value("col1", "val1");

    assertThat(insert.mValues).contains(entry("col1", "val1"));
  }

  @Test
  public void shouldNotModifyPassedContentValues() throws Exception {
    ContentValues values = new ContentValues();

    insert()
        .into("A")
        .values(values)
        .value("key", "value");

    assertThat(values.containsKey("key")).isFalse();

    ContentValues valuesToConcatenate = new ContentValues();
    valuesToConcatenate.put("another_key", "another_value");

    insert()
        .into("A")
        .values(values)
        .values(valuesToConcatenate);

    Assertions.assertThat(values.containsKey("another_key")).isFalse();
  }

  @Test
  public void shouldBuildInsertWithConcatenatedContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");

    ContentValues secondValues = new ContentValues();
    secondValues.put("col2", "val2");

    Insert insert = insert()
        .into("A")
        .values(firstValues)
        .values(secondValues);

    assertThat(insert.mValues).contains(entry("col1", "val1"), entry("col2", "val2"));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenBySingleValue() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    values.put("col2", "val2");

    Insert insert = insert()
        .into("A")
        .values(values)
        .value("col2", null);

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

    Insert insert = insert()
        .into("A")
        .values(firstValues)
        .values(secondValues);

    assertThat(insert.mValues).contains(entry("col1", "val1"), entry("col3", "val3"), entry("col2", null));
  }

  @Test
  public void shouldPerformInsertWithDefaultValues() throws Exception {
    insert()
        .into("A")
        .defaultValues("nullable_col")
        .perform(mDb);

    verify(mDb).insert(eq("A"), eq("nullable_col"), isNull(ContentValues.class));
  }

  @Test
  public void shouldPerformInsertWithValues() throws Exception {
    insert()
        .into("A")
        .value("col_a", 42)
        .perform(mDb);

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    verify(mDb).insert(eq("A"), isNull(String.class), contentValuesArgument.capture());
    assertThat(contentValuesArgument.getValue()).contains(entry("col_a", 42));
  }
}
