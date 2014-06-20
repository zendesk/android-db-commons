package com.getbase.android.db.fluentsqlite;

import static com.getbase.android.db.fluentsqlite.Expressions.column;
import static com.getbase.android.db.fluentsqlite.Query.select;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.sqlite.SQLiteDatabase;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ViewActionsTest {

  @Mock
  private SQLiteDatabase db;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldCreateViewFromProvidedQuery() throws Exception {
    ViewActions
        .create()
        .view("view_a")
        .as(select().from("table_a"))
        .perform(db);

    Mockito.verify(db).execSQL("CREATE VIEW view_a AS SELECT * FROM table_a");
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldNotAllowUsingQueryWithBoundArgs() throws Exception {
    ViewActions
        .create()
        .view("view_a")
        .as(
            select()
                .from("table_a")
                .where(column("col_a").eq().arg(), "test")
        );
  }

  @Test
  public void shouldDropSpecifiedView() throws Exception {
    ViewActions
        .dropIfExists()
        .view("view_a")
        .perform(db);

    Mockito.verify(db).execSQL("DROP VIEW IF EXISTS view_a");
  }
}
