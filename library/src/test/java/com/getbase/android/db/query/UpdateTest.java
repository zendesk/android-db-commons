package com.getbase.android.db.query;

import static com.getbase.android.db.query.Expressions.column;
import static org.fest.assertions.api.ANDROID.assertThat;
import static org.fest.assertions.api.android.content.ContentValuesEntry.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

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
    Update
        .update()
        .table("test")
        .value("num", 666)
        .where("num=?", 0)
        .build()
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
    Update
        .update()
        .table("test")
        .value("num", 666)
        .where("num=?", 0)
        .build()
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
    Update
        .update()
        .table("test")
        .value("num", 666)
        .where("num=?", 0)
        .build()
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
    Update
        .update()
        .table("test")
        .value("num", 666)
        .where("num=?", 0)
        .where("t=?", "test")
        .build()
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
    Update
        .update()
        .table("test")
        .value("num", 666)
        .where(column("num").eq().arg(), 0)
        .build()
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
    Update
        .update()
        .table("test")
        .setColumn("num", "666")
        .build()
        .perform(mDb);

    verify(mDb, never()).update(anyString(), any(ContentValues.class), anyString(), any(String[].class));
    verify(mDb).compileStatement(anyString());
  }

  @Test
  public void shouldCopyColumnExpressionsDirectlyIntoStatement() throws Exception {
    Update
        .update()
        .table("test")
        .setColumn("num", "666")
        .build()
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET num=(666)"));
  }

  @Test
  public void shouldPassContentValuesArgsAsBoundArgsWhenCustomColumnExpressionsIsUsed() throws Exception {
    Update
        .update()
        .table("test")
        .setColumn("num", "666")
        .value("t", "666")
        .build()
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET num=(666), t=?"));
  }

  @Test
  public void shouldOverrideContentValuesAddedEarlierWithCustomColumnExpressionForTheSameColumn() throws Exception {
    Update
        .update()
        .table("test")
        .value("num", "667")
        .value("t", "666")
        .setColumn("num", "666")
        .build()
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET num=(666), t=?"));
  }

  @Test
  public void shouldOverrideCustomColumnExpressionAddedEarlierWithContentValuesForTheSameColumn() throws Exception {
    Update
        .update()
        .table("test")
        .setColumn("t", "666")
        .setColumn("num", "666")
        .value("num", "667")
        .build()
        .perform(mDb);

    verify(mDb).compileStatement(eq("UPDATE test SET t=(666), num=?"));
  }

  @Test
  public void shouldRevertToSimpleUpdateWhenAllCustomColumnExpressionsAreOverridden() throws Exception {
    Update
        .update()
        .table("test")
        .setColumn("num", "666")
        .value("num", "667")
        .value("t", "666")
        .build()
        .perform(mDb);

    verify(mDb).update(anyString(), any(ContentValues.class), anyString(), any(String[].class));
    verify(mDb, never()).compileStatement(anyString());
  }
}
