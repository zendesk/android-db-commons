package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Delete.delete;
import static com.getbase.android.db.fluentsqlite.Expressions.column;
import static com.getbase.android.db.fluentsqlite.Query.select;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
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
public class DeleteTest {

  @Mock
  private SQLiteDatabase mDb;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldBuildTheDeleteWithoutSelection() throws Exception {
    delete().from("A").perform(mDb);

    verify(mDb).delete(eq("A"), anyString(), any(String[].class));
  }

  @Test
  public void shouldBuildTheDeleteWithSingleSelection() throws Exception {
    delete().from("A").where("a IS NULL").perform(mDb);

    verify(mDb).delete(anyString(), eq("(a IS NULL)"), any(String[].class));
  }

  @Test
  public void shouldBuildTheDeleteWithSingleSelectionBuiltFromExpressions() throws Exception {
    delete().from("A").where(column("a").is().nul()).perform(mDb);

    verify(mDb).delete(anyString(), eq("(a IS NULL)"), any(String[].class));
  }

  @Test
  public void shouldBuildTheDeleteWithMultipleSelections() throws Exception {
    delete().from("A").where("a IS NULL").where("b IS NULL").perform(mDb);

    verify(mDb).delete(anyString(), eq("(a IS NULL) AND (b IS NULL)"), any(String[].class));
  }

  @Test
  public void shouldBuildTheDeleteWithBoundParams() throws Exception {
    delete().from("A").where("a=?", 0).perform(mDb);

    verify(mDb).delete(
        anyString(),
        eq("(a=?)"),
        eq(new String[] { "0" })
    );
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectSelectionWithExpressionWithTooManyArgsPlaceholders() throws Exception {
    delete().from("A").where(column("col2").eq().arg());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldRejectSelectionWithExpressionWithTooFewArgsPlaceholders() throws Exception {
    delete().from("A").where(column("col2").eq().arg(), 1, 2);
  }

  @Test
  public void shouldBuildSelectionFromExpressionWithArgsPlaceholders() throws Exception {
    delete()
        .from("A")
        .where(column("col2").eq().arg(), "val2")
        .perform(mDb);

    verify(mDb).delete(
        anyString(),
        eq("(col2 == ?)"),
        eq(new String[] { "val2" })
    );
  }

  @Test
  public void shouldBuildSelectionFromExpressionWithBoundArgs() throws Exception {
    delete()
        .from("A")
        .where(column("col2").in(select().column("id").from("B").where("status=?", "new").build()))
        .perform(mDb);

    verify(mDb).delete(
        anyString(),
        anyString(),
        eq(new String[] { "new" })
    );
  }

  @Test
  public void shouldAllowUsingNullArgumentsForSelection() throws Exception {
    delete()
        .from("table_a")
        .where("col_a IS NULL", (Object[]) null)
        .perform(mDb);

    verify(mDb).delete(eq("table_a"), eq("(col_a IS NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldAllowUsingNullArgumentsForSelectionWithExpression() throws Exception {
    delete()
        .from("table_a")
        .where(column("col_a").is().nul(), (Object[]) null)
        .perform(mDb);

    verify(mDb).delete(eq("table_a"), eq("(col_a IS NULL)"), eq(new String[0]));
  }

  @Test
  public void shouldAllowUsingNullSelectionWithNullArguments() throws Exception {
    delete()
        .from("table_a")
        .where((String) null)
        .perform(mDb);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowUsingNullSelectionWithArguments() throws Exception {
    delete()
        .from("table_a")
        .where((String) null, "I shall fail")
        .perform(mDb);
  }
}
