package com.getbase.android.db.query.delete;

import static com.getbase.android.db.query.delete.Delete.delete;
import static com.getbase.android.db.query.Expressions.column;
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
}
