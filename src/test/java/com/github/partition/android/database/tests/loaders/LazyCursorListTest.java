package com.github.partition.android.database.tests.loaders;

import static org.fest.assertions.Assertions.assertThat;

import com.github.partition.android.database.loaders.LazyCursorList;
import com.google.common.base.Function;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.database.Cursor;
import android.database.MatrixCursor;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LazyCursorListTest {

  @Test
  public void shouldAccessProperRow() throws Exception {
    final MatrixCursor cursor = new MatrixCursor(new String[] { "name" });
    for (int i = 0; i < 10; i++) {
      cursor.addRow(new Object[] { "Name" + i });
    }
    final LazyCursorList<String> list = new LazyCursorList<String>(cursor, new Function<Cursor, String>() {
      @Override
      public String apply(Cursor cursor) {
        return cursor.getString(0);
      }
    });
    assertThat(list.get(5)).isEqualTo("Name" + 5);
  }

  @Test
  public void shouldContainProperSize() throws Exception {
    final MatrixCursor cursor = new MatrixCursor(new String[] { "name" });
    for (int i = 0; i < 10; i++) {
      cursor.addRow(new Object[] { "Name" + i });
    }
    final LazyCursorList<String> list = new LazyCursorList<String>(cursor, new Function<Cursor, String>() {
      @Override
      public String apply(Cursor cursor) {
        return null;
      }
    });
    assertThat(list.size()).isEqualTo(cursor.getCount());
  }
}
