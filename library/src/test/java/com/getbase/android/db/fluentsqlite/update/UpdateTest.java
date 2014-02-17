package com.getbase.android.db.fluentsqlite.update;

import static com.getbase.android.db.fluentsqlite.Expressions.arg;
import static com.getbase.android.db.fluentsqlite.Expressions.column;
import static com.getbase.android.db.fluentsqlite.query.QueryBuilder.select;
import static com.getbase.android.db.fluentsqlite.update.Update.update;
import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.android.content.ContentValuesEntry.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

import com.getbase.android.db.fluentsqlite.Expressions;

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
public class UpdateTest {

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
  public void shouldUpdateCorrectTableWhenDoingSimpleUpdate() throws Exception {
    update()
        .table("test")
        .value("num", 666)
        .where("num=?", 0)
        .perform(mDb);

    verify(mDb).update(
        eq("test"),
        any(ContentValues.class),
        anyString(),
        any(String[].class)
    );
  }

  @Test
  public void shouldPassCorrectValuesWhenDoingSimpleUpdate() throws Exception {
    update()
        .table("test")
        .value("num", 666)
        .where("num=?", 0)
        .perform(mDb);

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    verify(mDb).update(
        anyString(),
        contentValuesArgument.capture(),
        anyString(),
        any(String[].class)
    );
    assertThat(contentValuesArgument.getValue()).contains(entry("num", 666));
  }

  @Test
  public void shouldUseCorrectSelectionAndArgsWhenDoingSimpleUpdate() throws Exception {
    update()
        .table("test")
        .value("num", 666)
        .where("num=?", 0)
        .perform(mDb);

    verify(mDb).update(
        anyString(),
        any(ContentValues.class),
        eq("(num=?)"),
        eq(new String[] { "0" })
    );
  }

  @Test
  public void shouldConcatenateSelectionAndArgs() throws Exception {
    update()
        .table("test")
        .value("num", 666)
        .where("num=?", 0)
        .where("t=?", "test")
        .perform(mDb);

    verify(mDb).update(
        anyString(),
        any(ContentValues.class),
        eq("(num=?) AND (t=?)"),
        eq(new String[] { "0", "test" })
    );
  }

  @Test
  public void shouldBuildSelectionFromExpression() throws Exception {
    update()
        .table("test")
        .value("num", 666)
        .where(column("num").eq().arg(), 0)
        .perform(mDb);

    verify(mDb).update(
        anyString(),
        any(ContentValues.class),
        eq("(num == ?)"),
        eq(new String[] { "0" })
    );
  }

  @Test
  public void shouldUseSQLiteStatementWhenColumnExpressionIsUsed() throws Exception {
    update()
        .table("test")
        .setColumn("num", "666")
        .perform(mDb);

    verify(mDb, never()).update(anyString(), any(ContentValues.class), anyString(), any(String[].class));
    verify(mDb).compileStatement(anyString());
  }

  @Test
  public void shouldCopyColumnExpressionsDirectlyIntoStatement() throws Exception {
    update()
        .table("test")
        .setColumn("num", "666")
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET num=(666)"));
  }

  @Test
  public void shouldBuildColumnExpressionsWithSelection() throws Exception {
    update()
        .table("test")
        .setColumn("num", "666")
        .where("t=?", "test")
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET num=(666) WHERE (t=?)"));
  }

  @Test
  public void shouldBuildColumnExpressionsFromExpression() throws Exception {
    update()
        .table("test")
        .setColumn("num", Expressions.literal(666))
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET num=(666)"));
  }

  @Test
  public void shouldPassContentValuesArgsAsBoundArgsWhenCustomColumnExpressionsIsUsed() throws Exception {
    update()
        .table("test")
        .setColumn("num", "666")
        .value("t", "666")
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET num=(666), t=?"));
  }

  @Test
  public void shouldOverrideContentValuesAddedEarlierWithCustomColumnExpressionForTheSameColumn() throws Exception {
    update()
        .table("test")
        .value("num", "667")
        .value("t", "666")
        .setColumn("num", "666")
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET num=(666), t=?"));
  }

  @Test
  public void shouldOverrideCustomColumnExpressionAddedEarlierWithContentValuesForTheSameColumn() throws Exception {
    update()
        .table("test")
        .setColumn("t", "666")
        .setColumn("num", "666")
        .value("num", "667")
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET t=(666), num=?"));
  }

  @Test
  public void shouldRevertToSimpleUpdateWhenAllCustomColumnExpressionsAreOverridden() throws Exception {
    update()
        .table("test")
        .setColumn("num", "666")
        .value("num", "667")
        .value("t", "666")
        .perform(mDb);

    verify(mDb).update(anyString(), any(ContentValues.class), anyString(), any(String[].class));
    verify(mDb, never()).compileStatement(anyString());
  }

  @Test
  public void shouldNotModifyPassedContentValues() throws Exception {
    ContentValues values = new ContentValues();

    update()
        .table("A")
        .values(values)
        .value("key", "value");

    assertThat(values.containsKey("key")).isFalse();

    ContentValues valuesToConcatenate = new ContentValues();
    valuesToConcatenate.put("another_key", "another_value");

    update()
        .table("A")
        .values(values)
        .values(valuesToConcatenate);

    assertThat(values.containsKey("another_key")).isFalse();
  }

  @Test
  public void shouldBuildInsertWithConcatenatedContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");

    ContentValues secondValues = new ContentValues();
    secondValues.put("col2", "val2");

    update()
        .table("A")
        .values(firstValues)
        .values(secondValues)
        .perform(mDb);

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    verify(mDb).update(
        anyString(),
        contentValuesArgument.capture(),
        anyString(),
        any(String[].class)
    );
    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", "val2"));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenBySingleValue() throws Exception {
    ContentValues values = new ContentValues();
    values.put("col1", "val1");
    values.put("col2", "val2");

    update()
        .table("A")
        .values(values)
        .value("col2", null)
        .perform(mDb);

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    verify(mDb).update(
        anyString(),
        contentValuesArgument.capture(),
        anyString(),
        any(String[].class)
    );
    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col2", null));
  }

  @Test
  public void shouldPerformInsertWithContentValuesOverriddenByOtherContentValues() throws Exception {
    ContentValues firstValues = new ContentValues();
    firstValues.put("col1", "val1");
    firstValues.put("col2", "val2");

    ContentValues secondValues = new ContentValues();
    secondValues.putNull("col2");
    secondValues.put("col3", "val3");

    update()
        .table("A")
        .values(firstValues)
        .values(secondValues)
        .perform(mDb);

    ArgumentCaptor<ContentValues> contentValuesArgument = ArgumentCaptor.forClass(ContentValues.class);
    verify(mDb).update(
        anyString(),
        contentValuesArgument.capture(),
        anyString(),
        any(String[].class)
    );
    assertThat(contentValuesArgument.getValue()).contains(entry("col1", "val1"), entry("col3", "val3"), entry("col2", null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectColumnExpressionWithUnboundArgsPlaceholders() throws Exception {
    update().table("A").setColumn("id", arg());
  }

  @Test
  public void shouldUseBoundArgsFromColumnExpressions() throws Exception {
    update()
        .table("test")
        .setColumn("col_a", column("col_b").in(select().column("id").from("B").where("status=?", "new")))
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET col_a=(col_b IN (SELECT id FROM B WHERE (status=?)))"));
    verify(mStatement).bindString(eq(1), eq("new"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectSelectionWithExpressionWithTooManyArgsPlaceholders() throws Exception {
    update().table("A").value("col1", "val1").where(column("col2").eq().arg());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectSelectionWithExpressionWithTooFewArgsPlaceholders() throws Exception {
    update().table("A").value("col1", "val1").where(column("col2").eq().arg(), 1, 2);
  }

  @Test
  public void shouldBuildSelectionFromExpressionWithArgsPlaceholders() throws Exception {
    update()
        .table("A")
        .value("col1", "val1")
        .where(column("col2").eq().arg(), "val2")
        .perform(mDb);

    verify(mDb).update(
        anyString(),
        any(ContentValues.class),
        eq("(col2 == ?)"),
        eq(new String[] { "val2" })
    );
  }

  @Test
  public void shouldBuildSelectionFromExpressionWithBoundArgs() throws Exception {
    update()
        .table("A")
        .value("col1", "val1")
        .where(column("col2").in(select().column("id").from("B").where("status=?", "new")))
        .perform(mDb);

    verify(mDb).update(
        anyString(),
        any(ContentValues.class),
        anyString(),
        eq(new String[] { "new" })
    );
  }

  @Test
  public void shouldNotUseBoundArgsFromColumnExpressionsOverriddenByContentValues() throws Exception {
    update()
        .table("test")
        .setColumn("col_a", column("col_b").in(select().column("id").from("B").where("status=?", "new")))
        .value("col_a", 666)
        .perform(mDb);

    verify(mDb).update(
        anyString(),
        any(ContentValues.class),
        anyString(),
        eq(new String[0])
    );
  }

  @Test
  public void shouldOverrideBoundArgsFromColumnExpressionsIfTheExpressionForTheSameColumnIsSpecifiedTwice() throws Exception {
    update()
        .table("test")
        .setColumn("col_a", column("col_b").in(select().column("id").from("B").where("status=?", "new")))
        .setColumn("col_a", column("col_b").in(select().column("id").from("B").where("status=?", "old")))
        .perform(mDb);

    verify(mStatement).bindString(eq(1), eq("old"));
  }

  @Test
  public void shouldOverrideBoundArgsFromColumnExpressionsWithSimpleColumnExpression() throws Exception {
    update()
        .table("test")
        .setColumn("col_a", column("col_b").in(select().column("id").from("B").where("status=?", "new")))
        .setColumn("col_a", "666")
        .perform(mDb);

    verify(mStatement).executeUpdateDelete();
    verify(mStatement).close();
    verifyNoMoreInteractions(mStatement);
  }
}
